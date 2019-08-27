package com.leyou.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页的返回结果封装类
 * @param <T>
 */
@Data
public class PageResult<T> {
    //总条数
    private Long total;
    //总页数
    private Integer totalPage;
    //当前页数据
    private List<T> items;
    public PageResult() {
    }

    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public PageResult(Long total, Integer totalPage, List<T> items) {
        this.total = total;
        this.totalPage = totalPage;
        this.items = items;
    }
}
