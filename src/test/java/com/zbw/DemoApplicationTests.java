package com.zbw;

import com.zbw.domain.User;
import com.zbw.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

@SpringBootTest
public class DemoApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Test
    public void contextLoads() {
        User user = userMapper.selectById(1);
        if (user != null) {
            System.out.println(user.getUserName());
        } else {
            System.out.println("null");
        }
    }
}
