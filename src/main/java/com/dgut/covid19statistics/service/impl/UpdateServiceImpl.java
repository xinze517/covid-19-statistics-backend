package com.dgut.covid19statistics.service.impl;

import com.dgut.covid19statistics.domain.ConfirmedGlobal;
import com.dgut.covid19statistics.domain.DeathsGlobal;
import com.dgut.covid19statistics.domain.RecoveredGlobal;
import com.dgut.covid19statistics.function.GlobalNew;
import com.dgut.covid19statistics.mapper.ConfirmedGlobalMapper;
import com.dgut.covid19statistics.mapper.DeathsGlobalMapper;
import com.dgut.covid19statistics.mapper.RecoveredGlobalMapper;
import com.dgut.covid19statistics.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateServiceImpl implements UpdateService {

    private final RecoveredGlobalMapper recoveredGlobalMapper;
    private final ConfirmedGlobalMapper confirmedGlobalMapper;
    private final DeathsGlobalMapper deathsGlobalMapper;
    private final RestTemplate restTemplate;

    private final String[] baseList = {"confirmed", "deaths", "recovered"};
    //适用于 LocalDate 的格式化工具，线程安全
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");

    private String province_or_state;
    private String country_or_region;
    private List<String> dateList;

    /**
     * 多线程下载 csv 文件
     *
     * @return 下载结果
     */
    @Override
    public boolean downLoadFiles() {
        //临时文件夹
        File file = new File("./tmp");
        //调用 apache 的文件删除工具删除临时文件夹
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("删除文件夹失败");
            return false;
        }
        //创建临时文件夹
        boolean mkdir = file.mkdir();
        if (!mkdir) {
            log.error("创建文件夹失败");
            return false;
        }
        //设置本地代理解决github访问问题
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)));
        restTemplate.setRequestFactory(requestFactory);
        //定义请求头的接受类型
        RequestCallback requestCallback = request -> request.getHeaders()
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
        //下载文件
        String baseUrl = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_baseUrl_global.csv";
//建立多个线程并行下载
        CompletableFuture<?>[] futures = new CompletableFuture<?>[baseList.length];
        for (int i = 0; i < baseList.length; i++) {
            String base = baseList[i];
            //生成Url
            String url = baseUrl.replace("baseUrl", base);
            //目标地址
            String targetPath = "./tmp/" + base + ".csv";
            //对响应进行流式处理而不是将其全部加载到内存中
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> restTemplate.execute(url, HttpMethod.GET, requestCallback, clientHttpResponse -> {
                        Files.copy(clientHttpResponse.getBody(), Paths.get(targetPath));
                        return null;
                    }));
            //将任务添加到list中
            futures[i] = future;
        }
//所有任务并行执行，并等待所有任务全部完成
        CompletableFuture.allOf(futures).join();
        return true;
    }

    /**
     * 将新冠疫情中全球已痊愈人数数据添加到数据库中
     *
     * @return 添加结果
     */
    @Override
@Async("taskExecutor")
public CompletableFuture<Boolean> insertRecoveredGlobalToDB() {
    return this.insertToDB(baseList[2],
            recoveredGlobalMapper::getNewestDate,
            (s1, s2, t3, t4) -> new RecoveredGlobal(null, s1, s2, t3, t4),
            recoveredGlobalMapper::insertBatch);
}

    /**
     * 将新冠疫情中全球已确诊人数数据添加到数据库中
     *
     * @return 添加结果
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> insertConfirmedGlobalToDB() {
        return this.insertToDB(baseList[0],
                confirmedGlobalMapper::getNewestDate,
                (s1, s2, t3, t4) -> new ConfirmedGlobal(null, s1, s2, t3, t4),
                confirmedGlobalMapper::insertBatch);
    }

    /**
     * 将新冠疫情中全球已死亡人数数据添加到数据库中
     *
     * @return 添加结果
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> insertDeathsGlobalToDB() {
        return this.insertToDB(baseList[1],
                deathsGlobalMapper::getNewestDate,
                (s1, s2, t3, t4) -> new DeathsGlobal(null, s1, s2, t3, t4),
                deathsGlobalMapper::insertBatch);
    }

    /**
     * 通用解析csv，插入数据库方法
     *
     * @param base          文件名
     * @param getNewestDate 获取数据库中数据的最新日期
     * @param globalNew     创建构造对应的 global 对象的方法
     * @param insertBatch   传入对应的 mapper 以插入数据
     * @param <T>           global 类型
     * @return 插入数据结果
     */
    public <T> CompletableFuture<Boolean> insertToDB(String base,
                                                     Supplier<LocalDate> getNewestDate,
                                                     GlobalNew<String, LocalDate, Integer, T> globalNew,
                                                     Consumer<List<T>> insertBatch) {
        //读取的csv路径名
        String targetPath = "./tmp/" + base + ".csv";

        try {
            //解析csv文件
            CSVParser parser = formatCsv(targetPath);
            log.info(base + " global 数据准备完毕");
            //获取数据库中的最新记录
            //如果日期已存在于数据库，则不再读取
            //仅读取数据库中不存在的日期
            LocalDate date = getNewestDate.get();
            List<String> subList = subList(date);

            //创建数组
            List<T> globals = new ArrayList<>(100000);
            for (CSVRecord record : parser.getRecords()) {
                for (String s : subList) {
                    String pos = record.get(province_or_state);
                    String cor = record.get(country_or_region);
                    LocalDate d = LocalDate.parse(s, formatter);
                    Integer c = Integer.valueOf(record.get(s));
                    T t = globalNew.create(pos, cor, d, c);
                    globals.add(t);
                }
            }
            InsertTask<T> task = new InsertTask<>(globals, insertBatch);
            ForkJoinPool.commonPool().invoke(task);
            log.info(base + " global 数据批量插入完毕");

            return CompletableFuture.completedFuture(true);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(targetPath + "文件解析错误");
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 解析 csv 文件
     * 同时自动读取首行内容并设置至类实例变量
     *
     * @param filePath 文件路径.文件名
     * @return csv 多行记录
     * @throws IOException 解析错误
     */
    public CSVParser formatCsv(String filePath) throws IOException {
        File file = new File(filePath);
        Reader in = new FileReader(file);
        CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        initValue(parser);
        return parser;
    }

    /**
     * 初始化 csv 首行信息
     * 由于在多线程环境下读写，且仅需初始化一次，故做同步操作
     *
     * @param parser csv 解析器
     */
    public synchronized void initValue(CSVParser parser) {
        if (this.province_or_state == null) {
            List<String> headerNames = parser.getHeaderNames();
            this.province_or_state = headerNames.get(0);
            this.country_or_region = headerNames.get(1);
            this.dateList = headerNames.subList(4, headerNames.size());
        }
    }

    /**
     * 获取数据库中不存在的日期列表
     *
     * @param newestDate 数据库中记录最新的日期
     * @return 数据库中不存在的日期列表
     */
    public List<String> subList(LocalDate newestDate) {
        List<String> subList = this.dateList;
        if (newestDate != null) {
            String newestDateStr = newestDate.format(formatter);
            int i = dateList.indexOf(newestDateStr);
            //除去已经加入数据库的记录
            //如果失败返回-1，+1后为0，正好是起始
            //如果成功，则从找到的坐标往后划分
            subList = this.dateList.subList(i + 1, dateList.size());
        }
        return subList;
    }

}

class InsertTask<T> extends RecursiveAction {

    static final int THRESHOLD = 3000;
    private static final long serialVersionUID = -3113437318375208939L;
    List<T> list;
    Consumer<List<T>> consumer;

    public InsertTask(List<T> list, Consumer<List<T>> consumer) {
        this.list = list;
        this.consumer = consumer;
    }

    @Override
    protected void compute() {
        //不处理空列表
        if (list.isEmpty()) {
            return;
        }
        int start = 0;
        int end = list.size();
        if (end - start <= THRESHOLD) {
            //任务足够小，直接执行
            consumer.accept(list);
            return;
        }
        //任务过大，二分运行
        int middle = (end + start) / 2;
        InsertTask<T> task1 = new InsertTask<>(list.subList(start, middle), consumer);
        InsertTask<T> task2 = new InsertTask<>(list.subList(middle, end), consumer);
        invokeAll(task1, task2);
        task1.join();
        task2.join();
    }
}
