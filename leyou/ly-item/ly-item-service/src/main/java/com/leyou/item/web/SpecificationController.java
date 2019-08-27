package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据分类id 查询规格参数组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupByCid(@PathVariable("cid")Long cid){
        return ResponseEntity.ok(specificationService.querySpecGroupByCid(cid));
    }

    /**
     * 根据组id gid 查
     * cid通过分类id查
     * searching:通过搜索的字段查
     * 查询规格参数
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamsList(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching,",required = false)Boolean searching
            ){
        return ResponseEntity.ok(specificationService.queryParamsList(gid,cid,searching));
    }

    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryGroupListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryGroupListByCid(cid));
    }
}
