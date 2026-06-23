package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.IncomeDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.TransferDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Account;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Income;
import com.SpringBootMVC.ExpensesTracker.repository.IncomeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class TransferServiceImpl implements TransferService {

    @Lazy
    @Autowired
    private ExpenseService expenseService;

    @Lazy
    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private IncomeRepository incomeRepository;

    @Lazy
    @Autowired
    private AccountService accountService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Override
    @Transactional
    public void transfer(TransferDTO transferDTO) {
        if (transferDTO.getFromAccountId().equals(transferDTO.getToAccountId())) {
            throw new RuntimeException("Нельзя перевести средства на тот же счёт");
        }

        Account fromAccount = accountService.findAccountById(transferDTO.getFromAccountId());
        Account toAccount = accountService.findAccountById(transferDTO.getToAccountId());

        if (fromAccount == null || toAccount == null) {
            throw new RuntimeException("Один или оба счёта не найдены");
        }

        BigDecimal amountFrom = transferDTO.getAmount();
        String currencyFrom = fromAccount.getCurrency();
        String currencyTo = toAccount.getCurrency();

        BigDecimal amountTo;
        if (currencyFrom.equals(currencyTo)) {
            amountTo = amountFrom;
        } else {
            amountTo = exchangeRateService.convert(amountFrom, currencyFrom, currencyTo)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        if (!accountService.hasSufficientFunds(transferDTO.getFromAccountId(), amountFrom)) {
            throw new RuntimeException(
                    "Недостаточно средств на счёте '" + fromAccount.getName() +
                            "' (нужно " + amountFrom + " " + currencyFrom +
                            ", доступно " + fromAccount.getBalance() + " " + currencyFrom + ")"
            );
        }

        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setAmount(amountFrom);
        expenseDTO.setCategory("Перевод");
        expenseDTO.setDescription(transferDTO.getDescription() != null ?
                transferDTO.getDescription() + " → " + toAccount.getName() :
                "Перевод на счёт '" + toAccount.getName() + "'");
        expenseDTO.setDateTime(transferDTO.getDateTime() != null ?
                transferDTO.getDateTime() : LocalDateTime.now());
        expenseDTO.setClientId(transferDTO.getClientId());
        expenseDTO.setAccountId(transferDTO.getFromAccountId());
        expenseDTO.setOriginalAmount(amountFrom);
        expenseDTO.setOriginalCurrency(currencyFrom);

        expenseService.save(expenseDTO);

        IncomeDTO incomeDTO = new IncomeDTO();
        incomeDTO.setAmount(amountTo);
        incomeDTO.setCategory("Перевод");
        incomeDTO.setDescription(transferDTO.getDescription() != null ?
                transferDTO.getDescription() + " ← " + fromAccount.getName() :
                "Перевод со счёта '" + fromAccount.getName() + "'");
        incomeDTO.setDateTime(transferDTO.getDateTime() != null ?
                transferDTO.getDateTime() : LocalDateTime.now());
        incomeDTO.setClientId(transferDTO.getClientId());
        incomeDTO.setAccountId(transferDTO.getToAccountId());
        incomeDTO.setOriginalAmount(amountFrom);
        incomeDTO.setOriginalCurrency(currencyFrom);

        incomeService.save(incomeDTO);
    }

    @Override
    @Transactional
    public void saveInitialBalance(IncomeDTO incomeDTO) {
        Income income = new Income();
        income.setAmount(incomeDTO.getAmount());
        income.setDateTime(incomeDTO.getDateTime());
        income.setDescription(incomeDTO.getDescription());
        income.setClient(clientService.findClientById(incomeDTO.getClientId()));
        income.setIsInitialBalance(true);

        Category category = categoryService.findCategoryByName("Начальный баланс");
        if (category != null) {
            income.setCategory(category);
        }

        if (incomeDTO.getAccountId() > 0) {
            Account account = accountService.findAccountById(incomeDTO.getAccountId());
            income.setAccount(account);
            income.setOriginalCurrency(account.getCurrency());
        } else {
            income.setOriginalCurrency("RUB");
        }

        income.setOriginalAmount(incomeDTO.getAmount());
        incomeRepository.save(income);
    }
}