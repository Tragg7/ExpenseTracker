package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.AccountDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.IncomeDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.TransferDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Account;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.entity.Income;
import com.SpringBootMVC.ExpensesTracker.repository.AccountRepository;
import com.SpringBootMVC.ExpensesTracker.repository.ClientRepository;
import com.SpringBootMVC.ExpensesTracker.repository.ExpenseRepository;
import com.SpringBootMVC.ExpensesTracker.repository.IncomeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final ExchangeRateService exchangeRateService;
    private final TransferService transferService;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

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
        account.setCurrency(accountDTO.getCurrency() != null ? accountDTO.getCurrency() : "RUB");
        account.setActive(accountDTO.isActive());
        account.setDescription(accountDTO.getDescription());

        Client client = clientRepository.findById(accountDTO.getClientId()).orElse(null);
        account.setClient(client);

        BigDecimal initialBalance = accountDTO.getBalance() != null ? accountDTO.getBalance() : BigDecimal.ZERO;
        account.setBalance(initialBalance);

        accountRepository.save(account);

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            IncomeDTO initialIncome = new IncomeDTO();
            initialIncome.setAmount(initialBalance);
            initialIncome.setDateTime(LocalDateTime.now());
            initialIncome.setDescription("Начальный баланс счёта '" + account.getName() + "'");
            initialIncome.setClientId(accountDTO.getClientId());
            initialIncome.setAccountId(account.getId());
            initialIncome.setCategory("Начальный баланс");
            initialIncome.setInitialBalance(true);

            transferService.saveInitialBalance(initialIncome);
        }
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

    @Transactional
    @Override
    public void deleteAccountById(int id) {
        Account account = accountRepository.findById(id).orElse(null);

        if (account == null) {
            throw new RuntimeException("Счёт не найден");
        }

        List<Expense> expenses = expenseRepository.findByAccountId(id);
        List<Income> incomes = incomeRepository.findByAccountId(id);

        if (!expenses.isEmpty() || !incomes.isEmpty()) {
            throw new RuntimeException(
                    "Нельзя удалить счёт '" + account.getName() + "', " +
                            "так как с ним связаны " +
                            (expenses.size() + incomes.size()) + " операций. " +
                            "Сначала удалите или перенесите все операции на другой счёт."
            );
        }

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

    @Override
    public boolean hasSufficientFunds(int accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        if (account == null) {
            return false;
        }
        return account.getBalance().compareTo(amount) >= 0;
    }

    @Override
    @Transactional
    public void addIncomeToAccount(int accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        if (account != null) {
            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);
        }
    }

    @Override
    @Transactional
    public boolean subtractExpenseFromAccount(int accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        if (account == null) {
            return false;
        }

        if (account.getBalance().compareTo(amount) < 0) {
            return false;
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        return true;
    }

    @Override
    @Transactional
    public void returnExpenseToAccount(int accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        if (account != null) {
            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);
        }
    }

    @Override
    @Transactional
    public void removeIncomeFromAccount(int accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        if (account != null) {
            account.setBalance(account.getBalance().subtract(amount));
            accountRepository.save(account);
        }
    }
}