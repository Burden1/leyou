package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    //1.注入service
    @Autowired
    private BrandService brandService;

    /**
     * 分页查询品牌
     * 需要得到四大条件：请求方式：Get
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "desc",defaultValue = "false")Boolean desc,
            @RequestParam(value = "key",required = false)String key
    ){
        return ResponseEntity.ok(brandService.queryBrandByPageAndSort(page,rows,sortBy,desc, key));
    }

    /**
     * 新增品牌
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids")List<Long> cids){
        //调用方法
        brandService.saveBrand(brand,cids);
        //返回新增成功 201
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据cid查询品牌
     */
    @GetMapping("/cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(brandService.queryBrandByCid(cid));
    }

    /**
     * 根据品牌id查询品牌
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandByBrandId(@PathVariable("id")Long id){
        return ResponseEntity.ok(brandService.queryById(id));
    }

    /**
     * 根据ids查询品牌
     */
    @GetMapping("brands")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(brandService.queryBrandByIds(ids));
    }
}
