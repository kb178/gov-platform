package com.haikou.government.api.remote;

import com.haikou.government.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 消息服务远程调用接口
 *
 * @author gov-platform
 */
@FeignClient(value = "gov-message", contextId = "remoteMessageService")
public interface RemoteMessageService {

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    R<Boolean> sendSmsCode(@RequestParam("phone") String phone,
                           @RequestParam("code") String code);

    /**
     * 发送站内消息
     */
    @PostMapping("/inner/send")
    R<Boolean> sendInnerMessage(@RequestParam("userId") Long userId,
                                @RequestParam("title") String title,
                                @RequestParam("content") String content);
}
