package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodeUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;
    private static final String KEY_PREFIX = "user:verify:phone:";
    /**
     * 1.校验数据
     * data：是要校验的数据
     * type：是要校验的数据类型 ：1.用户名 2.手机号
     * @param data
     * @param type
     * @return
     */
    public Boolean checkData(String data,Integer type){
        //1.创建User对象
        User record = new User();
        //2.判断数据类型
        switch (type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        //3.返回
        return userMapper.selectCount(record) == 0;
    }
    /**
     * 2.生成短信验证码
     * @param phone
     */
    public void sendCode(String phone) {
        //1.生成key
        String key = KEY_PREFIX + phone;
        //2.生成验证码
        String code = NumberUtils.generateCode(6);
        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);

        //3.发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
        //4.保存验证码
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }

    /**
     * 3.注册功能
     * @param user
     * @param code
     */
    public void register(User user, String code) {
        // 校验验证码, 从redis中取出验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(code, cacheCode)) {
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        // 生成盐
        String salt = CodeUtils.generateSalt();
        user.setSalt(salt);
        // 对密码加密
        user.setPassword(CodeUtils.md5Hex(user.getPassword(), salt));
        // 写入数据库
        user.setCreated(new Date());
        userMapper.insert(user);

    }

    /**
     * 4.根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    public User queryUserByUsernameAndPassword(String username, String password) {
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);
        if (user == null) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        // 校验密码
        if (!StringUtils.equals(user.getPassword(), CodeUtils.md5Hex(password, user.getSalt()))) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return user;
    }
}
