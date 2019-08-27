package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("brand")
public interface BrandApi {
    //通过品牌id 查询品牌
    @GetMapping("{id}")
    Brand queryBrandByBrandId(@PathVariable("id")Long id);

    //通过ids 查询品牌
    @GetMapping("brands")
    List<Brand> queryBrandByIds(@RequestParam("ids")List<Long> ids);
}
