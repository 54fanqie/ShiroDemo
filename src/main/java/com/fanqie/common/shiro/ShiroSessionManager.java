package com.fanqie.common.shiro;

import com.fanqie.common.util.IPUtils;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 自定义获取Token
 *
 * @date: 2022/5/11 11:17
 * @author: fanqie
 * @return
 */
public class ShiroSessionManager extends DefaultWebSessionManager {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    //定义常量
    private static final String AUTHORIZATION = "token";
    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";

    //重写构造器
    public ShiroSessionManager() {
        super();
        this.setDeleteInvalidSessions(true);
    }

    /**
     * 重写方法实现从请求头获取Token便于接口统一
     * 每次请求进来,Shiro会去从请求头找Authorization这个key对应的Value(Token)
     */
    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        if (!(request instanceof HttpServletRequest)) {
            log.debug("Current request is not an HttpServletRequest - cannot get session ID.  Returning null.");
            return null;
        }

        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        String ipAddr = IPUtils.getIpAddr(httpServletRequest);

        String requestUri = httpServletRequest.getRequestURI();
        log.info(">>>>>>>>>>>>>>>>>>>>> MySessionManager.getSessionId(), IP: {}, URI: {}", ipAddr, requestUri);
        // 先从请求头中获取 Authorization
        Serializable token = httpServletRequest.getHeader(AUTHORIZATION);
        // 如果请求头中有 Authorization 则其值为sessionId
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        if (token != null) {
            httpResponse.setHeader(AUTHORIZATION, (String) token);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, REFERENCED_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, token);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            log.info(">>>>>>>>>> MySessionManager.getSessionId(), 从Header中获取token: " + token);
            return token;
        }
        //sessionIdUrlRewritingEnabled的配置为false,不会在url的后面带上sessionID
        request.setAttribute(ShiroHttpServletRequest.SESSION_ID_URL_REWRITING_ENABLED, isSessionIdUrlRewritingEnabled());
        // 否则按默认规则从 cookie 取sessionId
        token = getReferencedSessionId(request, response);
        log.info(">>>>>>>>>> MySessionManager.getSessionId(), 使用默认模式从cookie获取sessionID为: " + token);
        return token;

    }

    /**
     * shiro默认从cookie中获取sessionId
     *
     * @param request  请求参数
     * @param response 响应参数
     * @return
     */
    private Serializable getReferencedSessionId(ServletRequest request, ServletResponse response) {
        String id = getSessionIdCookieValue(request, response);
        if (id != null) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,
                    ShiroHttpServletRequest.COOKIE_SESSION_ID_SOURCE);
        } else {
            //not in a cookie, or cookie is disabled - try the request URI as a fallback (i.e. due to URL rewriting):
            //try the URI path segment parameters first:
            id = getUriPathSegmentParamValue(request, ShiroHttpSession.DEFAULT_SESSION_ID_NAME);
            if (id == null) {
                //not a URI path segment parameter, try the query parameters:
                String name = getSessionIdName();
                id = request.getParameter(name);
                if (id == null) {
                    //try lowercase:
                    id = request.getParameter(name.toLowerCase());
                }
            }
            if (id != null) {
                request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,
                        ShiroHttpServletRequest.URL_SESSION_ID_SOURCE);
            }
        }
        if (id != null) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, id);
            //automatically mark it valid here.  If it is invalid, the
            //onUnknownSession method below will be invoked and we'll remove the attribute at that time.
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
        }
        // always set rewrite flag - SHIRO-361
        request.setAttribute(ShiroHttpServletRequest.SESSION_ID_URL_REWRITING_ENABLED, isSessionIdUrlRewritingEnabled());
        return id;
    }

    /**
     * copy from DefaultWebSessionManager
     *
     * @param request  请求参数
     * @param response 响应参数
     * @return
     */
    private String getSessionIdCookieValue(ServletRequest request, ServletResponse response) {
        if (!isSessionIdCookieEnabled()) {
            log.debug("Session ID cookie is disabled - session id will not be acquired from a request cookie.");
            return null;
        }
        if (!(request instanceof HttpServletRequest)) {
            log.debug("Current request is not an HttpServletRequest - cannot get session ID cookie.  Returning null.");
            return null;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        return getSessionIdCookie().readValue(httpRequest, WebUtils.toHttp(response));
    }

    private String getUriPathSegmentParamValue(ServletRequest servletRequest, String paramName) {
        if (!(servletRequest instanceof HttpServletRequest)) {
            return null;
        }
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        if (uri == null) {
            return null;
        }
        int queryStartIndex = uri.indexOf('?');
        if (queryStartIndex >= 0) {
            uri = uri.substring(0, queryStartIndex);
        }
        int index = uri.indexOf(';');
        if (index < 0) {
            //no path segment params - return:
            return null;
        }
        //there are path segment params, let's get the last one that may exist:
        final String TOKEN = paramName + "=";
        uri = uri.substring(index + 1);
        //we only care about the last JSESSIONID param:
        index = uri.lastIndexOf(TOKEN);
        if (index < 0) {
            //no segment param:
            return null;
        }
        uri = uri.substring(index + TOKEN.length());
        index = uri.indexOf(';');
        if (index >= 0) {
            uri = uri.substring(0, index);
        }
        return uri;
    }

    private String getSessionIdName() {
        String name = this.getSessionIdCookie() != null ? this.getSessionIdCookie().getName() : null;
        if (name == null) {
            name = ShiroHttpSession.DEFAULT_SESSION_ID_NAME;
        }
        return name;
    }
    private static final String DEVICE = "device";
    private static final String MOBILE = "mobile";

    /**
     * 存储会话id到response header中
     *
     * @param currentId 会话ID
     * @param request   HttpServletRequest
     * @param response  HttpServletResponse
     */
    private void storeSessionId(Serializable currentId, HttpServletRequest request, HttpServletResponse response) {
        if (currentId == null) {
            String msg = "sessionId cannot be null when persisting for subsequent requests.";
            throw new IllegalArgumentException(msg);
        }
        String idString = currentId.toString();
        //增加判断，如果请求头中包含DEVICE=MOBILE，则将sessionId放在header中返回
        if (StringUtils.hasText(request.getHeader(DEVICE)) && MOBILE.equals(request.getHeader(DEVICE))) {
            response.setHeader(AUTHORIZATION, idString);
        } else {
            Cookie template = getSessionIdCookie();
            Cookie cookie = new SimpleCookie(template);
            cookie.setValue(idString);
            cookie.saveTo(request, response);
        }
        log.trace("Set session ID cookie for session with id {}", idString);
    }

    /**
     * 设置deleteMe到response header中
     *
     * @param request  request
     * @param response HttpServletResponse
     */
    private void removeSessionIdCookie(HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.hasText(request.getHeader(AUTHORIZATION))) {
            response.setHeader(AUTHORIZATION, Cookie.DELETED_COOKIE_VALUE);
        } else {
            getSessionIdCookie().removeFrom(request, response);
        }
    }

    /**
     * 会话创建
     * Stores the Session's ID, usually as a Cookie, to associate with future requests.
     *
     * @param session the session that was just {@link #createSession created}.
     */
    @Override
    protected void onStart(Session session, SessionContext context) {
        super.onStart(session, context);
        if (!WebUtils.isHttp(context)) {
            log.debug("SessionContext argument is not HTTP compatible or does not have an HTTP request/response " +
                    "pair. No session ID cookie will be set.");
            return;
        }
        HttpServletRequest request = WebUtils.getHttpRequest(context);
        HttpServletResponse response = WebUtils.getHttpResponse(context);
        if (isSessionIdCookieEnabled()) {
            Serializable sessionId = session.getId();
            storeSessionId(sessionId, request, response);
        } else {
            log.debug("Session ID cookie is disabled.  No cookie has been set for new session with id {}", session.getId());
        }
        request.removeAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE);
        request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_IS_NEW, Boolean.TRUE);
    }

    /**
     * 会话失效
     *
     * @param s   Session
     * @param ese ExpiredSessionException
     * @param key SessionKey
     */
    @Override
    protected void onExpiration(Session s, ExpiredSessionException ese, SessionKey key) {
        super.onExpiration(s, ese, key);
        onInvalidation(key);
    }

    @Override
    protected void onInvalidation(Session session, InvalidSessionException ise, SessionKey key) {
        super.onInvalidation(session, ise, key);
        onInvalidation(key);
    }

    private void onInvalidation(SessionKey key) {
        ServletRequest request = WebUtils.getRequest(key);
        if (request != null) {
            request.removeAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID);
        }
        if (WebUtils.isHttp(key)) {
            log.debug("Referenced session was invalid.  Removing session ID cookie.");
            removeSessionIdCookie(WebUtils.getHttpRequest(key), WebUtils.getHttpResponse(key));
        } else {
            log.debug("SessionKey argument is not HTTP compatible or does not have an HTTP request/response " +
                    "pair. Session ID cookie will not be removed due to invalidated session.");
        }
    }

    /**
     * 会话销毁
     *
     * @param session Session
     * @param key     SessionKey
     */
    @Override
    protected void onStop(Session session, SessionKey key) {
        super.onStop(session, key);
        if (WebUtils.isHttp(key)) {
            HttpServletRequest request = WebUtils.getHttpRequest(key);
            HttpServletResponse response = WebUtils.getHttpResponse(key);
            log.debug("Session has been stopped (subject logout or explicit stop).  Removing session ID cookie.");
            removeSessionIdCookie(request, response);
        } else {
            log.debug("SessionKey argument is not HTTP compatible or does not have an HTTP request/response " +
                    "pair. Session ID cookie will not be removed due to stopped session.");
        }
    }

}