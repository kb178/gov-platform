package com.haikou.government.api.remote;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.common.core.domain.LoginUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务远程调用接口
 *
 * @author gov-platform
 */
@FeignClient(value = "gov-system", contextId = "remoteUserService")
public interface RemoteUserService {

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/user/{userId}")
    R<LoginUser> getUserById(@PathVariable("userId") Long userId);

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/user/username/{username}")
    R<LoginUser> getUserByUsername(@PathVariable("username") String username);

    /**
     * 注册用户
     */
    @PostMapping("/user/register")
    R<Boolean> register(@RequestHeader("username") String username,
                        @RequestHeader("password") String password);
}
