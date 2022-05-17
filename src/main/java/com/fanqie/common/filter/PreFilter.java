package com.fanqie.common.filter;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description: PreFilter
 * @date: 2022/5/7 17:54
 * @author: fanqie
 */
@WebFilter(urlPatterns = "/sys/*")
@Order(1)
public class PreFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("输出PreFilter的init方法");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        System.out.println("输出PreFilter的doFilter方法之前");
        filterChain.doFilter(request, response);
        System.out.println("输出PreFilter的doFilter方法之后");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
