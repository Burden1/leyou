package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_spec_param")
public class SpecParam {
    //规格参数id 主键
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    //分类id
    private Long cid;
    //组id
    private Long groupId;
    //参数名称
    private String name;

    //是否位数字类型的参数
    private Integer numer;

    //数字类型参数的分类
    private String unit;

    //是否为sku的通用属性
    private Boolean generic;

    //是否用于搜索过滤
    private Boolean searching;

    //规格参数间隔
    private String segments;

}
