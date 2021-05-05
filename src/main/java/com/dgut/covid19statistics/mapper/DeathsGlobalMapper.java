package com.dgut.covid19statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dgut.covid19statistics.domain.DeathsGlobal;
import com.dgut.covid19statistics.domain.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * @Entity DeathsGlobal
 */
@Repository
public interface DeathsGlobalMapper extends BaseMapper<DeathsGlobal> {

    /**
     * 批量插入数据
     * @param globals 数据列表
     */
    void insertBatch(List<DeathsGlobal> globals);

    //获取最新日期
    LocalDate getNewestDate();

    //按日期获取各国家死亡人数
    List<Record> countByAllCountryOrRegion(LocalDate date);

    //统计某日全球死亡人数
    Integer countGlobalByDate(LocalDate date);

    /**
     * 统计全球每日死亡数据
     * @param ignores 忽略国家
     * @return 统计结果
     */
    List<Integer> countSelective(List<String> ignores);
}




