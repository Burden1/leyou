package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService {
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    ElasticsearchTemplate template;
    /**
     * 把查询返回的结果封装成一个Goods
     */
    public Goods buildGoods(Spu spu) {

        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //判断
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnum.CATEGORRY_NOT_FOND);
        }
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(names)) {
            throw new LyException(ExceptionEnum.CATEGORRY_NOT_FOND);
        }

        //查询品牌
        Brand brand = brandClient.queryBrandByBrandId(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        //1.搜索字段： 分类+品牌 +标题 +(规格参数不写了）
        String all = spu.getTitle() + StringUtils.join(names, " ") + brand.getName();


        //2.查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }

        //对sku进行处理，因为实际上很多字段不需要 我们需要封装需要的属性即可
        List<Map<String, Object>> skus = new ArrayList<>();

        //创建一个价格集合
        Set<Long> pricesSet = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            //设置需要属性
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            //因为image信息在spu里面 并且image都是以，连接的 所以我们需要分割
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            map.put("price", sku.getPrice());

            skus.add(map);
            //处理价格
            pricesSet.add(sku.getPrice());
        }

        //3.规格参数
        //查询规格参数
        List<SpecParam> params = specificationClient.queryParamsList(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnum.PARAMS_CANNOT_BE_FOND);
        }
        //查询商品详情
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spu.getId());
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOND);
        }

        //获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特有规格参数
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });
        //规格参数 ：key是规格参数的名字，值是规格参数的值
        Map<String, Object> specs = new HashMap<>();
        //对规格进行遍历，并封装spec，其中spec的key是规格参数的名称，值是商品详情中的值
        for (SpecParam param : params) {
            //key是规格参数的名称
            String key = param.getName();
            Object value = "";

            if (param.getGeneric()) {
                if (param.getNumer()== 0) {
                    //参数是通用属性，通过规格参数的ID从商品详情存储的规格参数中查出值
                    value = genericSpec.get(param.getId());
                }
                if (param.getNumer()!=null && param.getNumer() == 1) {
                    //参数是数值类型，处理成段，方便后期对数值类型进行范围过滤
                    value = chooseSegment(value.toString(), param);
                }
            } else {
                //参数不是通用类型
                value = specialSpec.get(param.getId());
            }
            value = (value == null ? "其他" : value);
            //存入map
            specs.put(key, value);
        }

        //构建goods对象
        Goods goods = new Goods();

        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setAll(all);
        goods.setPrice(pricesSet);//
        goods.setSkus(JsonUtils.serialize(skus));//
        goods.setSpecs(specs);//

        goods.setSubTitle(spu.getSubTitle());

        return goods;
    }



    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 搜索功能
     * @param request
     * @return
     */
    public PageResult<Goods> search(SearchRequest request) {
        String key = request.getKey();
        //判断是否有搜索条件，如果没有直接返null 不允许搜索全部商品
        if(StringUtils.isBlank(key)){
            return null;
        }
        //当前页
        int page = request.getPage() -1 ;
        System.out.println(page);
        int size = request.getSize();

        //1.创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //补：结果过滤：我们只要我们需要的属性：id 副标题 skus
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //2.分页
        queryBuilder.withPageable(PageRequest.of(page,size));
        //3.过滤
            //搜索条件
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);

        //4.后续补：聚合功能
            //1聚合分类
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
            //2.聚合品牌
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
            //3.查询 :因为用了聚合所以只能用template
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

            //4.解析结果
                //4.0解析分页结果
        long totalElements = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> goodsList = result.getContent();

                //4.1解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));
                //4.2解析品牌
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));

        //聚合规格参数
        List<Map<String,Object>> specs = null;
            //我们设置只有商品分类存在并且数量为1的时候可以聚合规格参数
        if (categories != null && categories.size() ==1){
            specs = buildSpecificationAgg(categories.get(0).getId(),basicQuery);
        }
        return new SearchResult(totalElements,totalPages,goodsList,categories,brands,specs);
    }

    /**
     * 搜索条件
     * @param request
     * @return
     */
    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //1.创布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //2.查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //3.过滤条件
        Map<String,String> map = request.getFilter();
        for (Map.Entry<String,String > entry : map.entrySet()){
            String key = entry.getKey();
            //处理key:即key值部署分类也不是品牌 即规格参数
            if (!"cid3".equals(key) && !"brandId".equals(key)){
                key = "specs."+key+".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return queryBuilder;
    }

    /**
     * 聚合规格参数
     * @param cid
     * @param basicQuery
     * @return
     */
    private List<Map<String,Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        //创建返回值规格参数的容器
        List<Map<String,Object>> specs = new ArrayList<>();
        //1.查询需要聚合的规格参数
        List<SpecParam> params = specificationClient.queryParamsList(null, cid, true);
        //2.聚合
            //2.1创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            //2.2带上查询条件
        queryBuilder.withQuery(basicQuery);
            //2.3聚合
        for (SpecParam param: params){
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        //3.获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //4.解析结果
        Aggregations aggs = result.getAggregations();
        for (SpecParam param :params){
            //规格参数的名称
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            //准备map
            Map<String,Object> map = new HashMap<>();
            map.put("k",name);
            map.put("options",terms.getBuckets().stream().map(b->b.getKeyAsString()).collect(Collectors.toList()));
            //添加map
            specs.add(map);
        }
        return specs;
    }

    /**
     * 4.2解析品牌结果
     * @param terms
     * @return
     */
    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> brandList = brandClient.queryBrandByIds(ids);
            if (CollectionUtils.isEmpty(brandList)) {
                throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
            }
            return brandList;
        }catch (Exception e){
            log.error("[搜索服务]查询品牌异常:",e);
            return null;
        }
    }

    /**
     * 4.1解析分类结果
     * @param terms
     * @return
     */
    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            //取到品牌的ids
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            //根据ids查询分类
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            if (CollectionUtils.isEmpty(categories)) {
                throw new LyException(ExceptionEnum.CATEGORRY_NOT_FOND);
            }
            return categories;
        }catch (Exception e) {
            log.error("[搜索服务]查询分类异常:",e);
            return null;
        }
    }

    /**
     * 接受消息更改或者新增商品信息
     * @param spuId
     */
    public void createOrUpdateIndex(Long spuId) {
        //1.查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //2.构建goods
        Goods goods = buildGoods(spu);
        //3.存入索引库
        goodsRepository.save(goods);
    }

    /**
     * 接受消息 通过spuId删除商品
     * @param spuId
     */
    public void deleteById(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
