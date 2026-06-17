package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.IncomeDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.entity.Income;
import com.SpringBootMVC.ExpensesTracker.repository.ExpenseRepository;
import com.SpringBootMVC.ExpensesTracker.repository.IncomeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeServiceImpl implements IncomeService{
    IncomeRepository incomeRepository;
    ClientService clientService;
    CategoryService categoryService;
    EntityManager entityManager;

    @Autowired
    public IncomeServiceImpl(IncomeRepository incomeRepository, ClientService clientService
            , CategoryService categoryService, EntityManager entityManager) {
        this.incomeRepository = incomeRepository;
        this.clientService = clientService;
        this.categoryService = categoryService;
        this.entityManager = entityManager;
    }


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
        incomeRepository.save(income);
    }

    @Override
    public void update(IncomeDTO incomeDTO) {
        Income existingIncome = incomeRepository.findById(incomeDTO.getIncomeId()).orElse(null);
        existingIncome.setAmount(incomeDTO.getAmount());
        existingIncome.setDateTime(incomeDTO.getDateTime());
        existingIncome.setDescription(incomeDTO.getDescription());
        Category category = categoryService.findCategoryByName(incomeDTO.getCategory());
        existingIncome.setCategory(category);
        incomeRepository.save(existingIncome);
    }

    public List<Income> findAllIncomes() {
        return incomeRepository.findAll();
    }

    @Override
    public List<Income> findAllIncomesByClientId(int id) {
        return incomeRepository.findByClientId(id);
    }

    @Override
    public void deleteIncomeById(int id) {
        incomeRepository.deleteById(id);
    }

    @Override
    public List<Income> findFilterResult(FilterDTO filter) {
        System.out.println("\n=== ФИЛЬТР ДОХОДОВ ===");
        System.out.println("Входящий filter: " + filter);
        StringBuilder query = new StringBuilder("select i from Income i where 1=1");
        String category = filter.getCategory();
        System.out.println("Категория: '" + category + "'");
        if (category != null && !"all".equals(category) && !"null".equals(category)) {
            Category cat = categoryService.findCategoryByName(category);
            System.out.println("Найденная категория: " + cat);
            if (cat != null) {
                query.append(" AND i.category.id = ").append(cat.getId());
                System.out.println("Добавлено условие по категории: " + cat.getId());
            }
        }
        Integer from = filter.getFrom();
        Integer to = filter.getTo();
        System.out.println("From: " + from + ", To: " + to);

        if (from != null && to != null) {
            if (from > 0 || to > 0) {
                query.append(" AND i.amount between ").append(from).append(" and ").append(to);
                System.out.println("Добавлено условие по сумме: between " + from + " and " + to);
            }
        } else if (from != null && from > 0) {
            query.append(" AND i.amount >= ").append(from);
            System.out.println("Добавлено условие: >= " + from);
        } else if (to != null && to > 0) {
            query.append(" AND i.amount <= ").append(to);
            System.out.println("Добавлено условие: <= " + to);
        }
        String year = filter.getYear();
        System.out.println("Год: '" + year + "'");
        if (year != null && !"all".equals(year) && !"null".equals(year)) {
            query.append(" AND YEAR(i.dateTime) = ").append(year);
            System.out.println("Добавлено условие по году: " + year);
        }
        String month = filter.getMonth();
        System.out.println("Месяц: '" + month + "'");
        if (month != null && !"all".equals(month) && !"null".equals(month)) {
            query.append(" AND MONTH(i.dateTime) = ").append(month);
            System.out.println("Добавлено условие по месяцу: " + month);
        }
        System.out.println("\nИТОГОВЫЙ ЗАПРОС: " + query.toString());
        System.out.println("===================\n");
        return entityManager.createQuery(query.toString(), Income.class).getResultList();
    }
}
