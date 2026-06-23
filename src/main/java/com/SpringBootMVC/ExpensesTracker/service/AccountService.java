package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.AccountDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.IncomeDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.TransferDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    List<Account> findAllAccountsByClientId(int clientId);
    List<Account> findActiveAccountsByClientId(int clientId);
    Account findAccountById(int id);
    void save(AccountDTO accountDTO);
    void update(AccountDTO accountDTO);
    void deleteAccountById(int id);
    BigDecimal getTotalBalanceInRub(int clientId);
    boolean hasSufficientFunds(int accountId, BigDecimal amount);
    void addIncomeToAccount(int accountId, BigDecimal amount);
    boolean subtractExpenseFromAccount(int accountId, BigDecimal amount);
    void returnExpenseToAccount(int accountId, BigDecimal amount);
    void removeIncomeFromAccount(int accountId, BigDecimal amount);
}