package com.leyou.page.client;

import com.leyou.item.api.GoodApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface GoodsClient extends GoodApi {
}
