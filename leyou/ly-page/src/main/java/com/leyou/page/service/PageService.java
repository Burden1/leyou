package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specClient;
    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private TemplateEngine templateEngine;
    /**
     * 查询模型数据
     * @param spuId
     * @return
     */
    public Map<String,Object> loadModel(Long spuId) {
        Map<String ,Object> model = new HashMap<>();
        //1.查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //2.查询skus
        List<Sku> skus = spu.getSkus();
        //3.查询详情
        SpuDetail spuDetail = spu.getSpuDetail();
        //4.查询brand
        Brand brand = brandClient.queryBrandByBrandId(spu.getBrandId());
        //5.查询商品分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //6.查询规格参数
        List<SpecGroup> specs = specClient.queryGroupListByCid(spu.getCid3());

        //添加sku标题 副标题：因为item页面中只需要spu的连个属性：标题和副标题 我们单独添加
        model.put("spu",spu.getTitle());
        model.put("subTitle",spu.getSubTitle());

        model.put("skus",skus);
        model.put("detail",spuDetail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("specs",specs);
        return model;
    }


    public void createHtml(Long spuId){
        //1.上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //2.输出流
        File dest = new File("C:\\Users\\Administrator\\Desktop\\1",spuId+".html");

        //判断文件是否存在
        if (dest.exists()){
            dest.delete();
        }

        try {
            PrintWriter writer = new PrintWriter(dest, "UTF-8");
            templateEngine.process("item",context,writer);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[静态页服务]审核过程静态页异常",e);
        }
    }

    /**
     * 接受消息删除静态页Html
     * @param spuId
     */
    public void deleteHtml(Long spuId) {
        File dest = new File("C:\\Users\\Administrator\\Desktop\\1",spuId+".html");
        if (dest.exists()){
            dest.delete();
        }
    }
}
