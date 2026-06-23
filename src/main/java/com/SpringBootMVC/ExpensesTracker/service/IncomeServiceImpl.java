package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.IncomeDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Account;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Income;
import com.SpringBootMVC.ExpensesTracker.repository.IncomeRepository;
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
public class IncomeServiceImpl implements IncomeService {
    private final IncomeRepository incomeRepository;
    private final ClientService clientService;
    private final CategoryService categoryService;
    private final EntityManager entityManager;
    private final AccountService accountService;
    private final ExchangeRateService exchangeRateService;

    @Override
    public Income findIncomeById(int id) {
        return incomeRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void save(IncomeDTO incomeDTO) {
        System.out.println(incomeDTO);

        Income income = new Income();
        income.setAmount(incomeDTO.getAmount());
        income.setDateTime(incomeDTO.getDateTime());
        income.setDescription(incomeDTO.getDescription());
        income.setClient(clientService.findClientById(incomeDTO.getClientId()));

        Category category = categoryService.findCategoryByName(incomeDTO.getCategory());
        income.setCategory(category);

        // Устанавливаем счёт и исходную валюту
        if (incomeDTO.getAccountId() > 0) {
            Account account = accountService.findAccountById(incomeDTO.getAccountId());
            income.setAccount(account);
            income.setOriginalCurrency(account.getCurrency());
        } else {
            income.setOriginalCurrency("RUB");
        }

        // Исходная сумма = введённая сумма (в валюте счёта)
        income.setOriginalAmount(incomeDTO.getAmount());

        incomeRepository.save(income);

        // Добавляем средства на счёт
        if (incomeDTO.getAccountId() > 0) {
            accountService.addIncomeToAccount(incomeDTO.getAccountId(), incomeDTO.getAmount());
        }
    }

    @Transactional
    @Override
    public void update(IncomeDTO incomeDTO) {
        Income existingIncome = incomeRepository.findById(incomeDTO.getIncomeId()).orElse(null);

        if (existingIncome == null) {
            throw new RuntimeException("Операция не найдена");
        }
        Account oldAccount = existingIncome.getAccount();
        int oldAccountId = oldAccount != null ? oldAccount.getId() : 0;
        int newAccountId = incomeDTO.getAccountId();

        BigDecimal originalAmount = existingIncome.getOriginalAmount();
        String originalCurrency = existingIncome.getOriginalCurrency();

        if (originalAmount == null) {
            originalAmount = existingIncome.getAmount();
            originalCurrency = oldAccount != null ? oldAccount.getCurrency() : "RUB";
        }

        BigDecimal amountInNewCurrency;

        if (oldAccountId == newAccountId) {
            amountInNewCurrency = incomeDTO.getAmount();

            existingIncome.setOriginalAmount(incomeDTO.getAmount());
        } else {
            if (newAccountId > 0) {
                Account newAccount = accountService.findAccountById(newAccountId);
                String newCurrency = newAccount.getCurrency();

                amountInNewCurrency = exchangeRateService.convert(
                        originalAmount,
                        originalCurrency,
                        newCurrency
                ).setScale(2, RoundingMode.HALF_UP);
            } else {
                amountInNewCurrency = exchangeRateService.convertToRub(
                        originalAmount,
                        originalCurrency
                ).setScale(2, RoundingMode.HALF_UP);
            }

            existingIncome.setOriginalAmount(originalAmount);
            existingIncome.setOriginalCurrency(originalCurrency);
        }

        if (oldAccount != null) {
            BigDecimal balanceAfterRemoval = oldAccount.getBalance().subtract(existingIncome.getAmount());
            if (balanceAfterRemoval.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Нельзя перенести операцию: баланс счёта '" + oldAccount.getName() +
                                "' станет отрицательным (" + balanceAfterRemoval.setScale(2, RoundingMode.HALF_UP) +
                                " " + oldAccount.getCurrency() + "). " +
                                "На этом счёте есть другие операции, которые не позволяют убрать этот доход."
                );
            }
        }

        if (oldAccount != null) {
            accountService.removeIncomeFromAccount(oldAccountId, existingIncome.getAmount());
        }

        existingIncome.setAmount(amountInNewCurrency);
        existingIncome.setDateTime(incomeDTO.getDateTime());
        existingIncome.setDescription(incomeDTO.getDescription());
        Category category = categoryService.findCategoryByName(incomeDTO.getCategory());
        existingIncome.setCategory(category);

        if (newAccountId > 0) {
            Account newAccount = accountService.findAccountById(newAccountId);
            existingIncome.setAccount(newAccount);
        } else {
            existingIncome.setAccount(null);
        }

        incomeRepository.save(existingIncome);

        if (newAccountId > 0) {
            accountService.addIncomeToAccount(newAccountId, amountInNewCurrency);
        }
    }

    public List<Income> findAllIncomes() {
        return incomeRepository.findAll();
    }

    @Override
    public List<Income> findAllIncomesByClientId(int id) {
        List<Income> incomes = incomeRepository.findByClientId(id);

        for (Income income : incomes) {
            if (income.getAccount() != null) {
                income.setAccountName(income.getAccount().getName());
                income.setAccountCurrency(income.getAccount().getCurrency());

                BigDecimal amountInRub = exchangeRateService.convertToRub(
                        income.getAmount(),
                        income.getAccount().getCurrency()
                );
                income.setAmountInRub(amountInRub.setScale(2, RoundingMode.HALF_UP));
            } else {
                income.setAccountName("Без счёта");
                income.setAccountCurrency("RUB");
                income.setAmountInRub(income.getAmount());
            }
        }
        return incomes;
    }

    @Transactional
    @Override
    public void deleteIncomeById(int id) {
        Income income = incomeRepository.findById(id).orElse(null);
        if (income != null && income.getAccount() != null) {
            Account account = income.getAccount();
            BigDecimal balanceAfterRemoval = account.getBalance().subtract(income.getAmount());
            if (balanceAfterRemoval.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Нельзя удалить операцию: баланс счёта '" + account.getName() +
                                "' станет отрицательным (" + balanceAfterRemoval.setScale(2, RoundingMode.HALF_UP) +
                                " " + account.getCurrency() + "). " +
                                "Сначала удалите или перенесите более поздние операции."
                );
            }
            accountService.removeIncomeFromAccount(income.getAccount().getId(), income.getAmount());
        }
        incomeRepository.deleteById(id);
    }

    @Override
    public List<Income> findFilterResult(FilterDTO filter) {
        System.out.println("\n=== ФИЛЬТР ДОХОДОВ ===");
        StringBuilder query = new StringBuilder("select i from Income i where 1=1");

        if (filter.getAccountId() != null) {
            query.append(" AND i.account.id = ").append(filter.getAccountId());
        }

        String category = filter.getCategory();
        if (category != null && !"all".equals(category) && !"null".equals(category)) {
            Category cat = categoryService.findCategoryByName(category);
            if (cat != null) {
                query.append(" AND i.category.id = ").append(cat.getId());
            }
        }

        Integer from = filter.getFrom();
        Integer to = filter.getTo();
        if (from != null && to != null) {
            if (from > 0 || to > 0) {
                query.append(" AND i.amount between ").append(from).append(" and ").append(to);
            }
        } else if (from != null && from > 0) {
            query.append(" AND i.amount >= ").append(from);
        } else if (to != null && to > 0) {
            query.append(" AND i.amount <= ").append(to);
        }

        String year = filter.getYear();
        if (year != null && !"all".equals(year) && !"null".equals(year)) {
            query.append(" AND YEAR(i.dateTime) = ").append(year);
        }

        String month = filter.getMonth();
        if (month != null && !"all".equals(month) && !"null".equals(month)) {
            query.append(" AND MONTH(i.dateTime) = ").append(month);
        }

        TypedQuery<Income> incomeTypedQuery = entityManager.createQuery(query.toString(), Income.class);
        return incomeTypedQuery.getResultList();
    }
}