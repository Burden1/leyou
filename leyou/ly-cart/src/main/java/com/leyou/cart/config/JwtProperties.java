package com.leyou.cart.config;

import com.leyou.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;

    private PublicKey publicKey;

    //对象一旦实例化后，就应该读取公钥和私钥,这个注解是在构造方法实例化执行
    @PostConstruct
    public void init() throws Exception {
        // 读取公钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
}