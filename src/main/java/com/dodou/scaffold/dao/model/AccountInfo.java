package com.dodou.scaffold.dao.model;

/**
 * @author: Finlay
 * @ClassName: AccountInfo
 * @Description:
 * @date: 2019-11-11 10:30 AM
 */
import lombok.Data;

import java.io.Serializable;

@Data
public class AccountInfo implements Serializable {
    private Long id;
    private String accountName;
    private String accountNo;
    private String accountPassword;
    private Double accountBalance;

}
