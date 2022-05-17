package com.fanqie.common.config;

import com.fanqie.common.filter.AuthLoginFilter;
import com.fanqie.common.filter.SysIdentifierFilter;
import com.fanqie.common.shiro.ShiroRealm;
import com.fanqie.common.shiro.ShiroSessionManager;
import com.fanqie.common.util.SHA256Util;
import com.fanqie.common.filter.PreFilter;
import com.fanqie.common.shiro.ShiroSessionIdGenerator;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro配置类
 * @date: 2022/5/10 14:45
 * @author: fanqie
 */
@Configuration
public class ShiroConfig {

    private final String CACHE_KEY = "shiro:cache:";
    private final String SESSION_KEY = "shiro:session:";

    //Redis配置
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.timeout}")
    private int timeout;
    @Value("${spring.redis.password}")
    private String password;

    @Autowired
    ShiroService shiroService;


//    @Bean
    public FilterRegistrationBean preFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        PreFilter preFilter = new PreFilter();
        filterRegistrationBean.setFilter(preFilter);
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }

    /**
     * 开启Shiro-aop注解支持
     * 使用代理方式所以需要开启代码支持
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * Shiro基础配置 , 配置过滤器工厂，设置对应的过滤条件和跳转条件
     * anon: 无需认证即可访问
     * authc: 需要认证才能访问
     * user: 需要拥有 ‘记住我’ 才能用
     * perms: 拥有对某个资源权限才能访问
     * role: 拥有某个角色权限才能访问
     *   注意过滤器配置顺序不能颠倒
     *   ① 认证登录
     *   ② 认证自定义的token过滤器 - 判断token是否有效
     *   ③ 角色权限过滤：自定义的只需要满足其中一个角色即可访问  ;  roles[admin,guest] : 默认需要每个参数满足才算通过，相当于hasAllRoles()方法
     *   ④ 权限过滤 :认证自定义的url过滤器拦截权限  【注：多个过滤器用 , 分割】
     *   ⑤ 认证登录  【注：map不能存放相同key】
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactory(SecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        // 添加自定义过滤器
        Map<String, Filter> filters = new LinkedHashMap<>();
        // 正确的配置是需要我们自己new出来，不能通过@Bean注解初始化，否则将这个Filter交给Spring管理
//        filters.put("authLogin", new AuthLoginFilter());
        // 定义过滤器名称 【注：map里面key值对于的value要为authc才能使用自定义的过滤器】
        filters.put( "sys", new SysIdentifierFilter() );
        filters.put( "authc", new AuthLoginFilter());
        shiroFilterFactoryBean.setFilters(filters);

        // 从配置中读取白名单
        Map<String, String> filterChainDefinitionMap = shiroService.loadFilterChainDefinitionMap();
        // 配置登录接口不会被拦截
        filterChainDefinitionMap.put("/sys/**", "sys");
        filterChainDefinitionMap.put("/userLogin/**", "anon");
        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 安全管理器
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 自定义Ssession管理
        securityManager.setSessionManager(sessionManager());
        // 自定义Cache实现
        securityManager.setCacheManager(cacheManager());
        // 自定义Realm验证
        securityManager.setRealm(shiroRealm());
        return securityManager;
    }

    /**
     * 身份验证器
     */
    @Bean
    public ShiroRealm shiroRealm() {
        ShiroRealm shiroRealm = new ShiroRealm();
        shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return shiroRealm;
    }

    /**
     * 凭证匹配器
     * 将密码校验交给Shiro的SimpleAuthenticationInfo进行处理,在这里做匹配配置
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher shaCredentialsMatcher = new HashedCredentialsMatcher();
        // 散列算法:这里使用SHA256算法;
        shaCredentialsMatcher.setHashAlgorithmName(SHA256Util.HASH_ALGORITHM_NAME);
        // 散列的次数，比如散列两次，相当于 md5(md5(""));
        shaCredentialsMatcher.setHashIterations(SHA256Util.HASH_ITERATIONS);
        return shaCredentialsMatcher;
    }

    /**
     * 配置Redis管理器
     *  使用的是shiro-redis开源插件
     */
    @Bean
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host + ":" + port);
        redisManager.setTimeout(timeout);
//        redisManager.setPassword(password);
        return redisManager;
    }

    /**
     * 配置Cache管理器
     * 用于往Redis存储权限和角色标识
     * 使用的是shiro-redis开源插件
     */
    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        redisCacheManager.setKeyPrefix(CACHE_KEY);
        // 配置缓存的话要求放在session里面的实体类必须有个id标识
        redisCacheManager.setPrincipalIdFieldName("userId");
        return redisCacheManager;
    }

    /**
     * SessionID生成器
     */
    @Bean
    public ShiroSessionIdGenerator sessionIdGenerator(){
        return new ShiroSessionIdGenerator();
    }

    /**
     * 配置RedisSessionDAO
     * 使用的是shiro-redis开源插件
     */
    @Bean
    public RedisSessionDAO redisSessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager());
        redisSessionDAO.setSessionIdGenerator(sessionIdGenerator());
        redisSessionDAO.setKeyPrefix(SESSION_KEY);
        redisSessionDAO.setExpire(timeout);
        return redisSessionDAO;
    }

    /**
     * 配置Session管理器
     */
    @Bean
    public SessionManager sessionManager() {
        ShiroSessionManager shiroSessionManager = new ShiroSessionManager();
        shiroSessionManager.setSessionDAO(redisSessionDAO());
        return shiroSessionManager;
    }
}