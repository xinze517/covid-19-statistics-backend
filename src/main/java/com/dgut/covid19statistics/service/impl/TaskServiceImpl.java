package com.dgut.covid19statistics.service.impl;

import com.dgut.covid19statistics.service.TaskService;
import com.dgut.covid19statistics.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final UpdateService updateService;

    /**
     * 定时任务执行
     * 下载疫情数据并写入数据库
     */
    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void execute() {
        //下载文件
        boolean isDownloadSuccess = updateService.downLoadFiles();
        if (isDownloadSuccess) {
            Instant start = Instant.now();
            //将数据插入数据库
            CompletableFuture<Boolean> f1 = updateService.insertDeathsGlobalToDB();
            CompletableFuture<Boolean> f2 = updateService.insertRecoveredGlobalToDB();
            CompletableFuture<Boolean> f3 = updateService.insertConfirmedGlobalToDB();
            CompletableFuture.allOf(f1, f2, f3).join();
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            long millis = timeElapsed.toMillis();
            log.info("任务耗时：" + millis + "ms");
        }
    }

}