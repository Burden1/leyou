package com.leyou.item.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * 分页查询商品列表
     *
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @Transactional
    public PageResult<Spu> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        //1.分页
        PageHelper.startPage(page, rows);
        //2.过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //3.默认排序
        example.setOrderByClause("last_update_time DESC");
        //4.查询
        List<Spu> spus = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)) {
            log.error("商品未查询到");
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //5.解析分类和品牌的名称
        loadCnameAndBname(spus);

        //6.解析分页结果
        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(), spus);
    }

    /**
     * 解析分类和品牌名称
     *
     * @param spus
     * @return
     */
    private void loadCnameAndBname(List<Spu> spus) {
        for (Spu spu : spus) {
            //处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());

            System.out.println(names);

            spu.setCname(StringUtils.join(names, "/"));
            //处理品牌名称
            String bname = brandService.queryById(spu.getBrandId()).getName();
            spu.setBname(bname);
        }
    }

    /**
     * 商品新增
     *
     * @param spu
     */
    @Transactional
    public void saveGoods(Spu spu) {
        //1.新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        //2，新增detail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);

        //创建一个库存集合
        List<Stock> stockList = new ArrayList<>();

        //3.新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //4.批量新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            stockList.add(stock);
        }
        //4.批量新增库存
        stockMapper.insertList(stockList);

        //发送商品新增消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    /**
     * 根据spuid查询详情
     * @param spuId
     * @return
     */
    public SpuDetail queryDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOND);
        }
        return spuDetail;
    }

    /**
     * 根据spu查询下面的所有sku
     * @param spuId
     * @return
     */
    @Transactional
    public List<Sku> querySkuBySpuId(Long spuId) {
        //1.查询sku
        Sku sku =new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }

        //2.查询库存
            //得到sku的id集合
         List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            //通过id得到库存集合
        List<Stock> stockList = stockMapper.selectByIdList(ids);
            //判断
        if (CollectionUtils.isEmpty(stockList)){
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOND);
        }
            //把stock便成一个map 其key是sku的id 值是库存值
        Map<Long, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skuList.forEach(s->s.setStock(stockMap.get(s.getId())));
        return skuList;
    }

    /**
     * 商品修改
     * @param spu
     */
    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null){
            throw new LyException(ExceptionEnum.GOODS_ID_NOTBE_NULL);
        }
        //1.查询sku
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());

        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)){
            //删除sku
            skuMapper.delete(sku);
            //删除stock
                //首先拿到ids
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
                //通过ids删除
            stockMapper.deleteByIdList(ids);
        }

        //2.修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        //3.修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_UPDATE_ERROR);
        }

        //4.新增sku和stock
        saveSkuAndStock(spu);

        //发送商品更新消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }

    /**
     * 4.新增sku和stock
     * @param spu
     */
    private void saveSkuAndStock(Spu spu) {
        int count;
        //创建一个库存集合
        List<Stock> stockList = new ArrayList<>();

        //3.新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //4.批量新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            stockList.add(stock);
        }
        //4.批量新增库存
        stockMapper.insertList(stockList);
    }

    /**
     * 根据spuid查询spu
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        //1.查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //2.查询spu下面的所有sku
        spu.setSkus(querySkuBySpuId(id));
        //3.查询detail
        spu.setSpuDetail(queryDetailBySpuId(id));
        return spu;
    }

    /**
     * 根据skuid集合查询所有sku
     * 为了信息显示在购物车里面
     * @param ids
     * @return
     */
    public List<Sku> querySkuByIds(List<Long> ids) {
        // 查询 sku
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }
        // 填充库存
        loadStockInSku(ids, skus);
        return skus;
    }

    /**
     * 根据sku查询库存
     * @param ids
     * @param skus
     */
    private void loadStockInSku(List<Long> ids, List<Sku> skus) {
        // 查询库存
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)) {
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOND);
        }
        // 将库存转为 map，key 是 skuId，值是库存
        Map<Long, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(stock -> stock.getSkuId(), stock -> stock.getStock()));
        // 保存库存到 sku
        for (Sku sku : skus) {
            sku.setStock(stockMap.get(sku.getId()));
        }
    }

    /**
     * 远程调用 减库存
     * @param carts
     */
    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count != 1) {
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
