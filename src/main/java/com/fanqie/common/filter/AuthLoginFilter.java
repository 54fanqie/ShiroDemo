package com.fanqie.common.filter;


import com.alibaba.fastjson.JSONObject;
import com.fanqie.base.SWJsonResult;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义登录过滤器
 *
 * @author zhangkuan
 * @date 2020/01/06
 */
public class AuthLoginFilter extends AccessControlFilter {


    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse,
                                      Object mappedValue) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        // 这里配合APP需求我只需要做登录检测即可
        if (subject != null && subject.isAuthenticated()) {
            // TODO 登录检测通过，这里可以添加一些自定义操作
            System.out.println("执行了AuthLoginFilter   isAccessAllowed");
            return Boolean.TRUE;
        }
        // 登录检测失败返货False后会进入下面的onAccessDenied()方法
        return Boolean.FALSE;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest,
                                     ServletResponse servletResponse) throws Exception {
        PrintWriter out = null;
        System.out.println("执行了AuthLoginFilter   onAccessDenied");
        try {
            // 这里就很简单了，向Response中写入Json响应数据，需要声明ContentType及编码格式
            servletResponse.setCharacterEncoding("UTF-8");
            servletResponse.setContentType("application/json; charset=utf-8");
            out = servletResponse.getWriter();
            Map<String,Object> map = new HashMap<>();
            map.put("code",500);
            map.put("msg","未登录");
            out.write(JSONObject.toJSONString(new SWJsonResult(map)));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return Boolean.FALSE;
    }
}
