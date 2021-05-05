package com.dgut.covid19statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dgut.covid19statistics.domain.ConfirmedGlobal;
import com.dgut.covid19statistics.domain.Record;
import com.dgut.covid19statistics.mapper.ConfirmedGlobalMapper;
import com.dgut.covid19statistics.mapper.DeathsGlobalMapper;
import com.dgut.covid19statistics.mapper.RecoveredGlobalMapper;
import com.dgut.covid19statistics.service.ConfirmGlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfirmGlobalServiceImpl implements ConfirmGlobalService {

    private final ConfirmedGlobalMapper confirmedGlobalMapper;
    private final RecoveredGlobalMapper recoveredGlobalMapper;
    private final DeathsGlobalMapper deathsGlobalMapper;

    private final Comparator<Record> comparator = (o1, o2) -> {
        if (o1.getCount().equals(o2.getCount())) {
            return 0;
        } else if (o1.getCount() > o2.getCount()) {
            return -1;
        } else {
            return 1;
        }
    };

    @Override
    @Cacheable(value = "confirmCache", key = "#root.methodName.concat('-').concat(T(String).valueOf(#date))")
    public List<Record> countByAllCountry(LocalDate date) {
        return confirmedGlobalMapper.count(date, null, -1, "Country_or_Region", true);
    }

    @Override
    @Cacheable(value = "confirmCache", key = "#root.methodName.concat('-').concat(T(String).valueOf(#date)).concat('-').concat(T(String).valueOf(#limit))")
    public List<Record> increaseCountByAllCountry(LocalDate date, int limit) {
        //获取最早的日期
        LambdaQueryWrapper<ConfirmedGlobal> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ConfirmedGlobal::getDate)
                .orderByAsc(ConfirmedGlobal::getDate)
                .last("limit 1");
        ConfirmedGlobal one = confirmedGlobalMapper.selectOne(wrapper);
        LocalDate earlyDate = one.getDate();
        //查询当天数据
        List<Record> records1 = countByAllCountry(date);
        //判断当前日期是否晚于最早日期
        int compareTo = date.compareTo(earlyDate);
        if (compareTo > 0) {
            //查询前一天数据
            List<Record> records2 = countByAllCountry(date.minusDays(1));
            //数据相减
            Iterator<Record> it1 = records1.iterator();
            Iterator<Record> it2 = records2.iterator();
            while (it1.hasNext() && it2.hasNext()) {
                Record r1 = it1.next();
                Record r2 = it2.next();
                r1.setCount(r1.getCount() - r2.getCount());
            }
        }
        records1.sort(comparator);
        if (limit > 0) {
            return new ArrayList<>(records1.subList(0, limit));
        }
        return records1;
    }

    /**
     * 按人数对国家进行排名，可限制国家数
     *
     * @param limit 限制国家数
     * @return 各国对应日期的人数列表
     */
    @Override
    @Cacheable(value = "confirmCache", key = "#root.methodName.concat('-').concat(T(String).valueOf(#limit))")
    public Map<String, Object> countAllCountryByDates(int limit) {
        //切分完成
        //获取最新日期中排名前 limit 的国家
        List<Record> records = confirmedGlobalMapper.count(null, null, limit, "count", false);
        //生成国家列表
        List<String> countries = records.stream().map(Record::getCountryOrRegion).collect(Collectors.toList());
        //根据国家查询 chooseDate（指定日期列表）中的人数
        Map<String, List<Integer>> counts = new LinkedHashMap<>();
        countries.parallelStream().forEach(country -> {
            List<Integer> list = confirmedGlobalMapper.countByCountryOrRegion(country, null);
            counts.put(country, list);
        });
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("country", countries);
        dataset.put("counts", counts);
        return dataset;
    }

    @Override
    @Cacheable(value = "confirmCache", key = "#root.methodName")
    public Map<String, Object> loadForeignSituation() {
        //获取海外所有国家的每日确诊数据
        List<Integer> confirmedForeign = confirmedGlobalMapper.countSelective(Collections.singletonList("China"));
        //计算海外所有国家的每日新增确诊
        List<Integer> incConfirmedForeign = new ArrayList<>(confirmedForeign);
        for (int i = confirmedForeign.size() - 1; i > 0; i--) {
            incConfirmedForeign.set(i, confirmedForeign.get(i) - confirmedForeign.get(i - 1));
        }
        //获取海外所有国家的每日痊愈数据
        List<Integer> recoveredForeign = recoveredGlobalMapper.countSelective(Collections.singletonList("China"));
        //获取海外所有国家的每日死亡数据
        List<Integer> deathsForeign = deathsGlobalMapper.countSelective(Collections.singletonList("China"));
        Iterator<Integer> confirmedIterator = confirmedForeign.iterator();
        Iterator<Integer> recoveredIterator = recoveredForeign.iterator();
        Iterator<Integer> deathsIterator = deathsForeign.iterator();

        //计算海外所有国家的每日痊愈率
        List<String> recoveredForeignRate = new ArrayList<>(confirmedForeign.size());
        //计算海外所有国家的每日死亡率
        List<String> deathsForeignRate = new ArrayList<>(confirmedForeign.size());
        //计算海外所有国家每日现有确诊数据
        List<Integer> curConfirmedForeign = new ArrayList<>(confirmedForeign.size());
        DecimalFormat format = new DecimalFormat("#.####");
        while (confirmedIterator.hasNext() && recoveredIterator.hasNext() && deathsIterator.hasNext()) {
            Integer confirmed = confirmedIterator.next();
            Integer recovered = recoveredIterator.next();
            Integer deaths = deathsIterator.next();
            recoveredForeignRate.add(format.format((double) recovered / confirmed));
            deathsForeignRate.add(format.format((double) deaths / confirmed));
            curConfirmedForeign.add(confirmed - recovered - deaths);
        }
        //返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("confirmedForeign", confirmedForeign);
        result.put("incConfirmedForeign", incConfirmedForeign);
        result.put("recoveredForeignRate", recoveredForeignRate);
        result.put("deathsForeignRate", deathsForeignRate);
        result.put("curConfirmedForeign", curConfirmedForeign);
        return result;
    }

}
