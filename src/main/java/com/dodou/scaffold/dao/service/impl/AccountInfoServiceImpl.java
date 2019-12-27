package com.dodou.scaffold.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dodou.scaffold.dao.mapper.AccountInfoMapper;
import com.dodou.scaffold.dao.service.AccountInfoService;
import com.dodou.scaffold.feign.Bank2Client;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Finlay
 * @ClassName: AccountInfoServiceImpl
 * @Description:
 * @date: 2019-11-11 10:40 AM
 */
@Service
@Slf4j
public class AccountInfoServiceImpl extends ServiceImpl implements AccountInfoService {

    @Autowired
    private AccountInfoMapper accountInfoMapper;
    @Autowired
    private Bank2Client bank2Client;

    /*
     * try幂等校验
     * try悬挂处理
     * 检查余额是够扣减金额
     * 扣减金额：先扣钱，再给李四加钱，因此没预留资源，从而幂等在try上操作（但是try又不会重试，还幂等个啥？）--->看了源码，定时任务确认不会重试try，因为try的异常根本就不记录，但是除开TCC之外的其他重复执行，你就不考虑？因为你没预留资源呀！
     * */
    @Override
    @Transactional
    @Hmily(confirmMethod = "confirm", cancelMethod = "cancel")
    public void updateAccountBalance(String accountNo, Double amount) {
        //获取全局事务ID
        HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
        String transId = hmilyTransactionContext.getTransId();
        log.info("bank1 try begin 开始执行----------------xid:{}", transId);
        //幂等校验：判断local_try_log中是否存在当前事务ID的try日志记录
        if (accountInfoMapper.isExistTry(transId) > 0) {
            log.info("AccountInfoServiceImpl updateAccountBalance: ------------------------try 已经执行过，不可重复执行,xid:{}", transId);
            return;
        }
        //try悬挂处理：如果confirm,cancel有一个先于try执行，则try不再执行
        if (accountInfoMapper.isExistConfirm(transId) > 0 || accountInfoMapper.isExistCancel(transId) > 0) {
            log.info("AccountInfoServiceImpl updateAccountBalance 悬挂处理被触发------------------------ 不允许执行try,xid:{}", transId);
        }
        //扣减金额
        if (accountInfoMapper.subtractAccountBalance(accountNo, amount) <= 0) {
            //扣减失败:抛出异常，用于本地事务回滚
            throw new RuntimeException("bank1 try 扣减金额失败------------------------ xid:{}" + transId);
        }
        //插入一条当前事务ID的try执行记录，用于幂等校验
        accountInfoMapper.addTry(transId);
        //远程调用转账
        if (!bank2Client.transfer(amount)) {
            throw new RuntimeException("bank1-------远程调用-----bank2转账服务失败,xid:{}" + transId);
        }
        if (amount == 2) {
            throw new RuntimeException("人为制造异常,xid:{}" + transId);
        }
        log.info("bank1 try end 结束执行----------------xid:{}", transId);
    }

    public void confirm(String accountNo, Double amount) {
        //写好日志提供服务追踪
        HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
        String transId = hmilyTransactionContext.getTransId();
        log.info("bank1 confirm begin 开始执行----------------xid:{},accountNo:{},amount:{}", transId, accountNo, amount);
    }

    /*
     * cancel幂等校验
     * cancel空回滚处理
     * 增加可用余额
     * */
    @Transactional
    public void cancel(String accountNo, Double amount) {
        //获取全局事务ID
        HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
        String transId = hmilyTransactionContext.getTransId();
        log.info("bank1 cancel begin 开始执行------------------------xid:{}", transId);
        //幂等校验：判断local_cancel_log中是否存在当前事务ID的日志记录
        if (accountInfoMapper.isExistCancel(transId) > 0) {
            log.info("AccountInfoServiceImpl cancel : cancel 已经执行过，不可重复执行,xid:{}", transId);
            return;
        }
        //空回滚处理：如果try没有执行，则cancel不允许执行
        if (accountInfoMapper.isExistTry(transId) <= 0) {
            log.info("bank1 空回滚处理被触发------------------------ 不允许cancel执行,xid:{}", transId);
            return;
        }
        //增加可用余额（回退金额）
        accountInfoMapper.addAccountBalance(accountNo, amount);
        //插入一条当前事务ID的cancel记录，用于幂等
        accountInfoMapper.addCancel(transId);
        log.info("bank1 cancel end 结束执行------------------------xid:{}", transId);

    }
}
