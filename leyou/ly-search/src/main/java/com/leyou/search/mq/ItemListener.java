package com.leyou.search.mq;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 搜索接受消息
 */
@Component
public class ItemListener {
    @Autowired
    private SearchService searchService;

    /**
     * 新增或者更改商品信息
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.insert.queue",durable = "true"),
            exchange = @Exchange(name="ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.update","item.insert"}
    ))
    public void listenInsertOrUpdate(Long spuId){
        //1.判断spuid是否存在
        if(spuId ==null){
            return;
        }
        //处理消息 对索引库进行新增或者修改
        searchService.createOrUpdateIndex(spuId);
    }

    /**
     * 删除商品
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void deleteIndex(Long spuId){
        if (spuId == null){
            return;
        }
        searchService.deleteById(spuId);
    }
}
