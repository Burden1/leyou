package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400,"价格不能为空"),
    INVALID_FILE_TYPE(400,"文件格式错误"),
    CATEGORRY_NOT_FOND(404,"商品分类没查到"),
    BRAND_NOT_FOUND(404,"未找到该品牌" ),
    GROUP_NOT_FOND(404,"商品组查询为空"),
    GOODS_NOT_FOND(404,"商品未查到" ),
    BRAND_SAVE_ERROR(500,"品牌保存失败"),
    FILE_UPLOAD_ERROR(500,"文件上传失败"),
    PARAMS_CANNOT_BE_FOND(404,"商品规格参数位找到"),
    GOODS_SAVE_ERROR(500,"商品保存失败" ),
    GOODS_DETAIL_SAVE_ERROR(500,"商品详情保存失败" ),
    GOODS_DETAIL_NOT_FOND(404,"商品详情未查到" ),
    GOODS_SKU_NOT_FOND(404,"商品sku未找到" ),
    GOODS_STOCK_NOT_FOND(404,"商品库存未查到" ),
    GOODS_UPDATE_ERROR(500,"商品更新失败"),
    GOODS_DETAIL_UPDATE_ERROR(500,"商品详情更新失败"),
    GOODS_ID_NOTBE_NULL(400,"商品id不能为空"),
    INVALID_USER_DATA_TYPE(400,"无效的用户数据类型" ),
    INVALID_USERNAME_PASSWORD(400,"无效的用户名或密码"),
    INVALID_VERIFY_CODE(400,"无效的短信验证码"),
    UN_AUTHORIZE(400,"未授权"),
    STOCK_NOT_ENOUGH(500,"库存不够"),
    CART_NOT_FOUND(404,"购物车未查询到"),
    CREATE_ORDER_ERROR(500,"创建订单失败"),
    ORDER_NOT_FOUND(404,"订单未查询到"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情未查询到"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态查询失败"),
    ORDER_STATUS_ERROR(500,"订单状态错误"),
    INVALID_ORDER_PARAM(400,"无效的订单参数"),
    UPDATE_ORDER_STATUS_ERROR(500,"更新订单状态失败"),
    WX_PAY_ORDER_FAIL(500,"微信支付失败"),
    INVALID_SIGN_ERROR(500,"无效的签名")

    ;
    private int code;
    private String msg;

}
