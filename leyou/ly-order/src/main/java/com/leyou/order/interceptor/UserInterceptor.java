package com.leyou.order.interceptor;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器：为了解析登录用户：必须知道登录的用户是谁 查token
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    private JwtProperties prop;

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) {
        // 获取cookie
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            // 解析token
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 传递user
            tl.set(info);
            // 放行
            return true;
        } catch (Exception e) {
            log.error("[购物车服务] 解析用户身份失败", e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        // 最后用完数据, 一定要清空
        tl.remove();
    }

    public static UserInfo getUser() {
        return tl.get();
    }
}