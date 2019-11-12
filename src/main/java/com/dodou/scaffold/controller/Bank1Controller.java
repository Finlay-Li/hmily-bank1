package com.dodou.scaffold.controller;

import com.dodou.scaffold.dao.service.AccountInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Finlay
 * @ClassName: Bank1Controller
 * @Description:
 * @date: 2019-11-11 4:11 PM
 */
@RestController
public class Bank1Controller {
    @Autowired
    AccountInfoService accountInfoService;

    @PostMapping("/transfer")
    public Boolean transfer(@RequestParam("amount") Double amount) {
        this.accountInfoService.updateAccountBalance("1", amount);
        return true;
    }

}
