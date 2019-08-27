package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class PageController {
    @Autowired
    private PageService pageService;

    @GetMapping("{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId,Model model){
        //1.查询模型数据
        Map<String,Object> attributes = pageService.loadModel(spuId);
        //2.准备模型数据
        model.addAllAttributes(attributes);
        //2.返回视图
        return "item";
    }
}
