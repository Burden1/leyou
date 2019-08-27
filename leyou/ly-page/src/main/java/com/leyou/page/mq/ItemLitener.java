package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接收商品微服务发送的消息
 */
@Component
public class ItemLitener {
    @Autowired
    private PageService pageService;

    /*
    新增和更改
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.insert.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key={"item.insert","item.update"}
    ))
    public void listenInsertOrUpdate(Long spuId){
        pageService.createHtml(spuId);
    }

    /**
     * 删除
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void deleteHtml(Long spuId){
        pageService.deleteHtml(spuId);
    }
}
