package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.repository.ExpenseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    ExpenseRepository expenseRepository;
    ClientService clientService;
    CategoryService categoryService;
    EntityManager entityManager;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, ClientService clientService
            , CategoryService categoryService, EntityManager entityManager) {
        this.expenseRepository = expenseRepository;
        this.clientService = clientService;
        this.categoryService = categoryService;
        this.entityManager = entityManager;
    }


    @Override
    public Expense findExpenseById(int id) {
        return expenseRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void save(ExpenseDTO expenseDTO) {
        System.out.println(expenseDTO);
        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getAmount());
        expense.setDateTime(expenseDTO.getDateTime());
        expense.setDescription(expenseDTO.getDescription());
        expense.setClient(clientService.findClientById(expenseDTO.getClientId()));
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        expense.setCategory(category);
        expenseRepository.save(expense);
    }

    @Override
    public void update(ExpenseDTO expenseDTO) {
        Expense existingExpense = expenseRepository.findById(expenseDTO.getExpenseId()).orElse(null);
        existingExpense.setAmount(expenseDTO.getAmount());
        existingExpense.setDateTime(expenseDTO.getDateTime());
        existingExpense.setDescription(expenseDTO.getDescription());
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        existingExpense.setCategory(category);
        expenseRepository.save(existingExpense);
    }

    @Override
    public List<Expense> findAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public List<Expense> findAllExpensesByClientId(int id) {
        return expenseRepository.findByClientId(id);
    }

    @Override
    public void deleteExpenseById(int id) {
        expenseRepository.deleteById(id);
    }

    @Override
    public List<Expense> findFilterResult(FilterDTO filter) {
        System.out.println("=== ФИЛЬТР РАСХОДОВ ===");
        System.out.println("filter: " + filter);
        StringBuilder query = new StringBuilder("select e from Expense e where 1=1");
        String category = filter.getCategory();
        if (category != null && !"all".equals(category) && !"null".equals(category)) {
            Category cat = categoryService.findCategoryByName(category);
            if (cat != null) {
                query.append(" AND e.category.id = ").append(cat.getId());
            }
        }
        Integer from = filter.getFrom();
        Integer to = filter.getTo();
        System.out.println("from=" + from + ", to=" + to);
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
        System.out.println("Итоговый запрос: " + query.toString());
        TypedQuery<Expense> expenseTypedQuery = entityManager.createQuery(query.toString(), Expense.class);
        return expenseTypedQuery.getResultList();
    }
}
