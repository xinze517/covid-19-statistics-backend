package com.dgut.covid19statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dgut.covid19statistics.domain.ConfirmedGlobal;
import com.dgut.covid19statistics.domain.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * @Entity ConfirmedGlobal
 */
@Repository
public interface ConfirmedGlobalMapper extends BaseMapper<ConfirmedGlobal> {

    /**
     * 批量插入数据
     * @param globals 数据列表
     */
    void insertBatch(List<ConfirmedGlobal> globals);

    //获取最新日期
    LocalDate getNewestDate();

    //根据国家获取人数
    List<Integer> countByCountryOrRegion(String countryOrRegion, List<LocalDate> dates);

    //统计某日全球确诊人数
    Integer countGlobalByDate(LocalDate date);

    /**
     * 按国家统计
     * @param date 按日期查找
     * @param country 按国家查找
     * @param limit 显示国家数
     * @param orderBy 排序依据
     * @param isAsc 是否升序
     * @return 记录列表
     */
    List<Record> count(LocalDate date, String country, Integer limit, String orderBy, Boolean isAsc);

    /**
     * 统计全球每日确诊数据
     * @param ignores 忽略国家
     * @return 统计结果
     */
    List<Integer> countSelective(List<String> ignores);
}




