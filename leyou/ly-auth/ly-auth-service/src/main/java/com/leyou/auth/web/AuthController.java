package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    @Value("${ly.jwt.cookieName}")
    private String cookieName;

    /**
     * 1.登录授权功能
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
            @RequestParam("password") String password, HttpServletRequest request,
            HttpServletResponse response) {
        // 登录
        String token = authService.login(username, password);
        // 写入cookie
        CookieUtils.setCookie(request, response, cookieName, token, 1800);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 2.校验用户登录状态
     * @param token
     * @param request
     * @param response
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue(value = "LY_TOKEN", required = false) String token, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            // 解析token
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 刷新token, 重新生成token,因为页面默认30分钟就重新登录
            String newToken = JwtUtils.generateToken(info, prop.getPrivateKey(), prop.getExpire());
            // 写入cookie
            CookieUtils.setCookie(request, response, cookieName, newToken, 1800);
            // 一登录,返回用户信息
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UN_AUTHORIZE);
        }
    }
}