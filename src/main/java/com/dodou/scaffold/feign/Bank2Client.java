package com.dodou.scaffold.feign;

import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: Finlay
 * @ClassName: Bank1Client
 * @Description:
 * @date: 2019-11-11 3:54 PM
 */
@FeignClient(name = "bank2")
public interface Bank2Client {
    //远程调用李四的微服务,true则表示调用成功
    @GetMapping("/transfer")
    @Hmily
    public Boolean transfer(@RequestParam("amount") Double amount);
}
