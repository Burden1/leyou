package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@RequestMapping("spec")
public interface SpecificationApi {
    /**
     查询规格参数
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("params")
    List<SpecParam> queryParamsList(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching,",required = false)Boolean searching
    );

    /**
     * 通过cid查询规格组
     * @param cid
     * @return
     */
    @GetMapping("group")
    List<SpecGroup> queryGroupListByCid(@RequestParam("cid") Long cid);
}