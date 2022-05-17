package com.fanqie.common.filter;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @description: SysIdentifierFilter
 * @date: 2022/5/17 14:25
 * @author: fanqie
 */
public class SysIdentifierFilter extends AccessControlFilter {
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        Subject subject = SecurityUtils.getSubject();

        HttpServletRequest httpServletRequest = WebUtils.toHttp(servletRequest);
        System.out.println("url "  + httpServletRequest.getRequestURI());
        System.out.println("host "  + httpServletRequest.getRequestURI());

        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        return false;
    }
}
