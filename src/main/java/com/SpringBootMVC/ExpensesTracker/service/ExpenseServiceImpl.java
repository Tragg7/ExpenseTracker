package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Account;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.repository.ExpenseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ClientService clientService;
    private final CategoryService categoryService;
    private final EntityManager entityManager;
    private final AccountService accountService;
    private final ExchangeRateService exchangeRateService; // ← перенесено из field injection

    @Override
    public Expense findExpenseById(int id) {
        return expenseRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void save(ExpenseDTO expenseDTO) {
        if (expenseDTO.getAccountId() > 0) {
            if (!accountService.hasSufficientFunds(expenseDTO.getAccountId(), expenseDTO.getAmount())) {
                throw new RuntimeException("Недостаточно средств на счёте для выполнения операции");
            }
        }

        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getAmount());
        expense.setDateTime(expenseDTO.getDateTime());
        expense.setDescription(expenseDTO.getDescription());
        expense.setClient(clientService.findClientById(expenseDTO.getClientId()));
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        expense.setCategory(category);

        if (expenseDTO.getAccountId() > 0) {
            Account account = accountService.findAccountById(expenseDTO.getAccountId());
            expense.setAccount(account);
            expense.setOriginalCurrency(account.getCurrency());
        } else {
            expense.setOriginalCurrency("RUB");
        }

        expense.setOriginalAmount(expenseDTO.getAmount());

        expenseRepository.save(expense);

        if (expenseDTO.getAccountId() > 0) {
            accountService.subtractExpenseFromAccount(expenseDTO.getAccountId(), expenseDTO.getAmount());
        }
    }

    @Transactional
    @Override
    public void update(ExpenseDTO expenseDTO) {
        Expense existingExpense = expenseRepository.findById(expenseDTO.getExpenseId()).orElse(null);

        if (existingExpense == null) {
            throw new RuntimeException("Операция не найдена");
        }

        Account oldAccount = existingExpense.getAccount();
        int oldAccountId = oldAccount != null ? oldAccount.getId() : 0;
        int newAccountId = expenseDTO.getAccountId();

        BigDecimal amountInNewCurrency;

        if (oldAccountId == newAccountId) {
            amountInNewCurrency = expenseDTO.getAmount();

            existingExpense.setOriginalAmount(expenseDTO.getAmount());
        } else {
            BigDecimal originalAmount = existingExpense.getOriginalAmount();
            String originalCurrency = existingExpense.getOriginalCurrency();

            if (originalAmount == null) {
                originalAmount = existingExpense.getAmount();
                originalCurrency = oldAccount != null ? oldAccount.getCurrency() : "RUB";
            }

            if (newAccountId > 0) {
                Account newAccount = accountService.findAccountById(newAccountId);
                String newCurrency = newAccount.getCurrency();

                amountInNewCurrency = exchangeRateService.convert(
                        originalAmount,
                        originalCurrency,
                        newCurrency
                ).setScale(2, RoundingMode.HALF_UP);

                existingExpense.setOriginalCurrency(originalCurrency);
                existingExpense.setOriginalAmount(originalAmount);
            } else {
                amountInNewCurrency = exchangeRateService.convertToRub(
                        originalAmount,
                        originalCurrency
                ).setScale(2, RoundingMode.HALF_UP);
                existingExpense.setOriginalCurrency(originalCurrency);
                existingExpense.setOriginalAmount(originalAmount);
            }
        }

        if (oldAccount != null) {
            accountService.returnExpenseToAccount(oldAccountId, existingExpense.getAmount());
        }

        if (newAccountId > 0) {
            if (!accountService.hasSufficientFunds(newAccountId, amountInNewCurrency)) {
                Account newAccount = accountService.findAccountById(newAccountId);
                if (oldAccount != null) {
                    accountService.subtractExpenseFromAccount(oldAccountId, existingExpense.getAmount());
                }
                throw new RuntimeException(
                        "Недостаточно средств на счёте '" + newAccount.getName() +
                                "' (нужно " + amountInNewCurrency + " " + newAccount.getCurrency() +
                                ", доступно " + newAccount.getBalance() + " " + newAccount.getCurrency() + ")"
                );
            }
        }

        existingExpense.setAmount(amountInNewCurrency);
        existingExpense.setDateTime(expenseDTO.getDateTime());
        existingExpense.setDescription(expenseDTO.getDescription());
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        existingExpense.setCategory(category);

        if (newAccountId > 0) {
            Account newAccount = accountService.findAccountById(newAccountId);
            existingExpense.setAccount(newAccount);
        } else {
            existingExpense.setAccount(null);
        }

        expenseRepository.save(existingExpense);

        if (newAccountId > 0) {
            accountService.subtractExpenseFromAccount(newAccountId, amountInNewCurrency);
        }
    }

    @Override
    public List<Expense> findAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public List<Expense> findAllExpensesByClientId(int id) {
        List<Expense> expenses = expenseRepository.findByClientId(id);

        for (Expense expense : expenses) {
            if (expense.getAccount() != null) {
                expense.setAccountName(expense.getAccount().getName());
                expense.setAccountCurrency(expense.getAccount().getCurrency());

                BigDecimal amountInRub = exchangeRateService.convertToRub(
                        expense.getAmount(),
                        expense.getAccount().getCurrency()
                );
                expense.setAmountInRub(amountInRub.setScale(2, RoundingMode.HALF_UP));
            } else {
                expense.setAccountName("Без счёта");
                expense.setAccountCurrency("RUB");
                expense.setAmountInRub(expense.getAmount());
            }
        }

        return expenses;
    }

    @Transactional
    @Override
    public void deleteExpenseById(int id) {
        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense != null && expense.getAccount() != null) {
            accountService.returnExpenseToAccount(expense.getAccount().getId(), expense.getAmount());
        }
        expenseRepository.deleteById(id);
    }

    @Override
    public List<Expense> findFilterResult(FilterDTO filter) {
        System.out.println("=== ФИЛЬТР РАСХОДОВ ===");
        System.out.println("filter: " + filter);
        StringBuilder query = new StringBuilder("select e from Expense e where 1=1");

        if (filter.getAccountId() != null) {
            query.append(" AND e.account.id = ").append(filter.getAccountId());
        }

        String category = filter.getCategory();
        if (category != null && !"all".equals(category) && !"null".equals(category)) {
            Category cat = categoryService.findCategoryByName(category);
            if (cat != null) {
                query.append(" AND e.category.id = ").append(cat.getId());
            }
        }
        Integer from = filter.getFrom();
        Integer to = filter.getTo();
        if (from != null && to != null) {
            if (from > 0 || to > 0) {
                query.append(" AND e.amount between ").append(from).append(" and ").append(to);
            }
        } else if (from != null && from > 0) {
            query.append(" AND e.amount >= ").append(from);
        } else if (to != null && to > 0) {
            query.append(" AND e.amount <= ").append(to);
        }
        String year = filter.getYear();
        if (year != null && !"all".equals(year) && !"null".equals(year)) {
            query.append(" AND YEAR(e.dateTime) = ").append(year);
        }
        String month = filter.getMonth();
        if (month != null && !"all".equals(month) && !"null".equals(month)) {
            query.append(" AND MONTH(e.dateTime) = ").append(month);
        }
        TypedQuery<Expense> expenseTypedQuery = entityManager.createQuery(query.toString(), Expense.class);
        return expenseTypedQuery.getResultList();
    }
}