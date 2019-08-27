package com.leyou.item.web;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询商品
     * 请求方式Get 请求路径/spu/page
     * 请求参数page?key=&saleable=true&page=1&rows=5
     * 返回结果 ResponseEntity..
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows
    ){
        return ResponseEntity.ok(goodsService.querySpuByPage(key,saleable,page,rows));
    }

    /**
     * 商品新增
     * @param spu
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestParam Spu spu){
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spu的id查询详情
     * 查询sku及数据回显
     */
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> queryDetailBySpuId(@PathVariable("id")Long spuId){
        return ResponseEntity.ok(goodsService.queryDetailBySpuId(spuId));
    }

    /**
     * 根据spu查询下面的所有sku
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long spuId){
        return ResponseEntity.ok(goodsService.querySkuBySpuId(spuId));
    }

    /**
     * 商品修改
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        goodsService.updateGoods(spu);
       return  ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spuId查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    /**
     * 根据skuid集合查询所有的sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(goodsService.querySkuByIds(ids));
    }

    /**
     * 减库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts) {
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
