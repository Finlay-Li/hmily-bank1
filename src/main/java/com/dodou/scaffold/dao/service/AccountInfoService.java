package com.dodou.scaffold.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author: Finlay
 * @InterfaceName: AccountInfoService
 * @Description:
 * @date: 2019-11-11 10:39 AM
 */
public interface AccountInfoService extends IService {

    //更新账户
    public  void updateAccountBalance(String accountNo, Double amount);
}
