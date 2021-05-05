package com.dgut.covid19statistics.service.impl;

import com.dgut.covid19statistics.mapper.ConfirmedGlobalMapper;
import com.dgut.covid19statistics.mapper.DeathsGlobalMapper;
import com.dgut.covid19statistics.mapper.RecoveredGlobalMapper;
import com.dgut.covid19statistics.service.GlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GlobalServiceImpl implements GlobalService {

    private final RecoveredGlobalMapper recoveredGlobalMapper;
    private final ConfirmedGlobalMapper confirmedGlobalMapper;
    private final DeathsGlobalMapper deathsGlobalMapper;

    @Override
    @Cacheable(value = "globalCache", key = "#root.methodName")
    public List<LocalDate> getAllDate() {
        return recoveredGlobalMapper.getAllDate();
    }

    @Override
    @Cacheable(value = "globalCache", key = "#root.methodName")
    public LocalDate getNewestDate() {
        return recoveredGlobalMapper.getNewestDate();
    }

    @Override
    @Cacheable(value = "globalCache", key = "#root.methodName")
    public Map<String, Map<String, Integer>> overview() {
        //获取最新一天
        LocalDate newestDate = this.getNewestDate();

        //查询各类型
        Map<String, Integer> total = new HashMap<>();
        total.put("confirmed", confirmedGlobalMapper.countGlobalByDate(newestDate));
        total.put("deaths", deathsGlobalMapper.countGlobalByDate(newestDate));
        total.put("recovered", recoveredGlobalMapper.countGlobalByDate(newestDate));
        total.put("curConfirmed", total.get("confirmed") - total.get("deaths") - total.get("recovered"));
        //计算前一天
        LocalDate yesterday = newestDate.minusDays(1);
        Map<String, Integer> inc = new HashMap<>();
        Integer yConfirmed = confirmedGlobalMapper.countGlobalByDate(yesterday);
        Integer yDeaths = deathsGlobalMapper.countGlobalByDate(yesterday);
        Integer yRecovered = recoveredGlobalMapper.countGlobalByDate(yesterday);
        inc.put("confirmed", total.get("confirmed") - yConfirmed);
        inc.put("deaths", total.get("deaths") - yDeaths);
        inc.put("recovered", total.get("recovered") - yRecovered);
        inc.put("curConfirmed", inc.get("confirmed") - inc.get("deaths") - inc.get("recovered"));
        //结果
        Map<String, Map<String, Integer>> result = new HashMap<>();
        result.put("total", total);
        result.put("inc", inc);
        return result;
    }

    @Override
    @Cacheable(value = "globalCache", key = "#root.methodName.concat('-').concat(T(String).valueOf(#spilt))")
    public List<LocalDate> loadShowDate(int spilt) {
        //获取所有日期
        List<LocalDate> allDate = recoveredGlobalMapper.getAllDate();
        //划分日期为 spilt 份
        //判断 spilt 是否合法
        List<LocalDate> chooseDate = new ArrayList<>();
        if (spilt > 0) {
            int part = (int) Math.ceil((double) allDate.size() / (spilt - 1));
            for (int i = 0; i < spilt; i++) {
                int choose = Math.min(allDate.size() - 1, i * part);
                chooseDate.add(allDate.get(choose));
            }
        }
        return chooseDate;
    }
}
