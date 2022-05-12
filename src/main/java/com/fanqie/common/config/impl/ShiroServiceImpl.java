package com.fanqie.common.config.impl;

import com.fanqie.common.config.ShiroService;
import org.apache.shiro.ShiroException;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @description: ShiroServiceImpl
 * @date: 2022/5/10 09:34
 * @author: fanqie
 */
@Service
public class ShiroServiceImpl implements ShiroService {

    @Override
    public Map<String, String> loadFilterChainDefinitionMap() {
        // 权限控制map
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 配置过滤:不会被拦截的链接 -> 放行 start ----------------------------------------------------------
        // 白名单
//        filterChainDefinitionMap.put("/demo/**","anon");
        // 放行 end ----------------------------------------------------------
        // 从数据库或缓存中查取出来的url与resources对应则不会被拦截 放行
        return filterChainDefinitionMap;
    }


    @Override
    public void updatePermission(ShiroFilterFactoryBean shiroFilterFactoryBean, Integer roleId, Boolean isRemoveSession) {
        synchronized (this) {
            AbstractShiroFilter shiroFilter;
            try {
                shiroFilter = (AbstractShiroFilter) shiroFilterFactoryBean.getObject();
            } catch (Exception e) {
                throw new ShiroException("get ShiroFilter from shiroFilterFactoryBean error!");
            }
            PathMatchingFilterChainResolver filterChainResolver = (PathMatchingFilterChainResolver) shiroFilter.getFilterChainResolver();
            DefaultFilterChainManager manager = (DefaultFilterChainManager) filterChainResolver.getFilterChainManager();

            // 清空拦截管理器中的存储
            manager.getFilterChains().clear();
            // 清空拦截工厂中的存储,如果不清空这里,还会把之前的带进去
            //            ps:如果仅仅是更新的话,可以根据这里的 map 遍历数据修改,重新整理好权限再一起添加
            shiroFilterFactoryBean.getFilterChainDefinitionMap().clear();
            // 动态查询数据库中所有权限
            shiroFilterFactoryBean.setFilterChainDefinitionMap(loadFilterChainDefinitionMap());
            // 重新构建生成拦截
            Map<String, String> chains = shiroFilterFactoryBean.getFilterChainDefinitionMap();
            for (Map.Entry<String, String> entry : chains.entrySet()) {
                manager.createChain(entry.getKey(), entry.getValue());
            }
            System.out.println("--------------- 动态生成url权限成功！ ---------------");

            // 动态更新该角色相关联的用户shiro权限
            if(roleId != null){
                updatePermissionByRoleId(roleId,isRemoveSession);
            }
        }
    }

    @Override
    public void updatePermissionByRoleId(Integer roleId, Boolean isRemoveSession) {
        // 查询当前角色的用户shiro缓存信息 -> 实现动态权限
//        List<User> userList = userMapper.selectUserByRoleId(roleId);
//        // 删除当前角色关联的用户缓存信息,用户再次访问接口时会重新授权 ; isRemoveSession为true时删除Session -> 即强制用户退出
//        if ( !CollectionUtils.isEmpty( userList ) ) {
//            for (User user : userList) {
//                ShiroUtils.deleteCache(user.getUsername(), isRemoveSession);
//            }
//        }
        System.out.println("--------------- 动态修改用户权限成功！ ---------------");
    }



}
