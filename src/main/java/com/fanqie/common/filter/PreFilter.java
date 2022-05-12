package com.fanqie.common.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description: PreFilter
 * @date: 2022/5/7 17:54
 * @author: fanqie
 */
public class PreFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("输出filter2的init方法");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        System.out.println("输出filter2的doFilter方法之前");
        filterChain.doFilter(request, response);
        System.out.println("输出filter2的doFilter方法之后");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
