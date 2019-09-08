package com.leyou.item.web;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    //自动注入service
    @Autowired
    private CategoryService categoryService;

    /*
     通过pid 查询商品分类信息
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryItemByPid(@RequestParam("pid")Long pid){
        //查询成功返回201
//        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.queryCategoryItemByPid(pid));
        //简写 直接返回ok

        return  ResponseEntity.ok(categoryService.queryCategoryItemByPid(pid));
    }

    /**
     * 根据商品分类id查询商品分类
     */
    @GetMapping("/list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids")List<Long> ids){
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }
}
