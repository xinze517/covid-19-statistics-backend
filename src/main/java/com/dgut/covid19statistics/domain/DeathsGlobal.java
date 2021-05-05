package com.dgut.covid19statistics.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 
 * @TableName deaths_global
 */
@TableName(value ="deaths_global")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeathsGlobal implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String provinceOrState;

    /**
     * 
     */
    private String countryOrRegion;

    /**
     * 
     */
    private LocalDate date;

    /**
     * 
     */
    private Integer count;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}