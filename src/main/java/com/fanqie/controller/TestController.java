package com.fanqie.controller;

import com.fanqie.base.SWJsonResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: TestController
 * @date: 2022/5/10 15:40
 * @author: fanqie
 */
@RestController
@RequestMapping("/api")
public class TestController {

    /**
     * 登录
     * @Author Sans
     * @CreateTime 2019/6/20 9:21
     */
    @RequestMapping("/test")
    public SWJsonResult test(){
        Map<String,Object> result = new HashMap<>();
        result.put("测试","成功");
        return new SWJsonResult(result);
    }
}
