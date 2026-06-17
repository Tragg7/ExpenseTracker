package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.AccountDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Account;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.repository.AccountRepository;
import com.SpringBootMVC.ExpensesTracker.repository.ClientRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final ExchangeRateService exchangeRateService;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              ClientRepository clientRepository,
                              ExchangeRateService exchangeRateService) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public List<Account> findAllAccountsByClientId(int clientId) {
        return accountRepository.findByClientId(clientId);
    }

    @Override
    public List<Account> findActiveAccountsByClientId(int clientId) {
        return accountRepository.findByClientIdAndIsActiveTrue(clientId);
    }

    @Override
    public Account findAccountById(int id) {
        return accountRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(AccountDTO accountDTO) {
        Account account = new Account();
        account.setName(accountDTO.getName());
        account.setType(accountDTO.getType());
        account.setBalance(accountDTO.getBalance() != null ? accountDTO.getBalance() : BigDecimal.ZERO);
        account.setCurrency(accountDTO.getCurrency() != null ? accountDTO.getCurrency() : "RUB");
        account.setActive(accountDTO.isActive());
        account.setDescription(accountDTO.getDescription());

        Client client = clientRepository.findById(accountDTO.getClientId()).orElse(null);
        account.setClient(client);

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void update(AccountDTO accountDTO) {
        Account existingAccount = accountRepository.findById(accountDTO.getId()).orElse(null);
        if (existingAccount != null) {
            existingAccount.setName(accountDTO.getName());
            existingAccount.setType(accountDTO.getType());
            existingAccount.setCurrency(accountDTO.getCurrency());
            existingAccount.setActive(accountDTO.isActive());
            existingAccount.setDescription(accountDTO.getDescription());
            accountRepository.save(existingAccount);
        }
    }

    @Override
    @Transactional
    public void deleteAccountById(int id) {
        accountRepository.deleteById(id);
    }

    @Override
    public BigDecimal getTotalBalanceInRub(int clientId) {
        List<Account> accounts = findActiveAccountsByClientId(clientId);
        BigDecimal totalRub = BigDecimal.ZERO;

        for (Account acc : accounts) {
            BigDecimal balanceInRub = exchangeRateService.convertToRub(acc.getBalance(), acc.getCurrency());
            totalRub = totalRub.add(balanceInRub);
        }

        return totalRub.setScale(2, RoundingMode.HALF_UP);
    }
}