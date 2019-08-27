package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class SearchResult extends PageResult<Goods> {
    //分类字段
    private List<Category> categories;
    //品牌字段
    private List<Brand> brands;

    //规格参数过滤条件key以及待选项option
    private List<Map<String,Object>> specs;



    public SearchResult(){
    }

    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
