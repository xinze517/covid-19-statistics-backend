package com.dgut.covid19statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dgut.covid19statistics.domain.Record;
import com.dgut.covid19statistics.domain.RecoveredGlobal;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * @Entity RecoveredGlobal
 */
@Repository
public interface RecoveredGlobalMapper extends BaseMapper<RecoveredGlobal> {

    /**
     * 批量插入数据
     * @param globals 数据列表
     */
    void insertBatch(List<RecoveredGlobal> globals);

    //获取最新日期
    LocalDate getNewestDate();

    //获取所有日期
    List<LocalDate> getAllDate();

    //根据国家获取人数
    List<Integer> countByCountryOrRegion(String countryOrRegion);

    //按日期获取各国家恢复人数
    List<Record> countByAllCountryOrRegion(LocalDate date);

    //统计某日全球确诊人数
    Integer countGlobalByDate(LocalDate date);

    /**
     * 统计全球每日确诊数据
     * @param ignores 忽略国家
     * @return 统计结果
     */
    List<Integer> countSelective(List<String> ignores);
}




