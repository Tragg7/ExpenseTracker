package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.DashboardFilterDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.OperationDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.entity.Income;
import com.SpringBootMVC.ExpensesTracker.repository.CategoryRepository;
import com.SpringBootMVC.ExpensesTracker.repository.ExpenseRepository;
import com.SpringBootMVC.ExpensesTracker.repository.IncomeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class DashboardController {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    // НОВЫЙ МЕТОД для AJAX-запроса категорий
    @GetMapping("/dashboard/categories")
    @ResponseBody
    public List<Category> getCategoriesByType(@RequestParam(required = false) String type) {
        System.out.println("=== AJAX запрос категорий, тип: " + type + " ===");

        if ("income".equals(type)) {
            return categoryRepository.findByType("INCOME");
        } else if ("expense".equals(type)) {
            return categoryRepository.findByType("EXPENSE");
        } else {
            return categoryRepository.findAll();
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "all") String period,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal amountFrom,
            @RequestParam(required = false) BigDecimal amountTo,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            HttpSession session,
            Model model) {

        // Нормализуем пустые строки в null
        operationType = isEmpty(operationType) ? "all" : operationType;
        category = isEmpty(category) ? null : category;
        dateFrom = isEmpty(dateFrom) ? null : dateFrom;
        dateTo = isEmpty(dateTo) ? null : dateTo;

        System.out.println("\n========== НОВЫЙ ЗАПРОС ==========");
        System.out.println("Страница: " + page);
        System.out.println("operationType: '" + operationType + "'");

        Client client = (Client) session.getAttribute("client");

        // Получаем категории в зависимости от выбранного типа операции
        List<Category> categories;
        if ("income".equals(operationType)) {
            categories = categoryRepository.findByType("INCOME");
        } else if ("expense".equals(operationType)) {
            categories = categoryRepository.findByType("EXPENSE");
        } else {
            categories = categoryRepository.findAll();
        }
        model.addAttribute("categories", categories);
        model.addAttribute("selectedOperationType", operationType); // Для сохранения выбора в форме

        List<Expense> expenses = expenseRepository.findByClientId(client.getId());
        List<Income> incomes = incomeRepository.findByClientId(client.getId());

        DashboardFilterDTO filter = new DashboardFilterDTO();
        filter.setOperationType(operationType);
        filter.setCategory(category);
        filter.setAmountFrom(amountFrom);
        filter.setAmountTo(amountTo);
        filter.setDateFrom(dateFrom);
        filter.setDateTo(dateTo);

        boolean hasFilter = (operationType != null && !"all".equals(operationType)) ||
                (category != null && !"all".equals(category)) ||
                amountFrom != null || amountTo != null ||
                dateFrom != null || dateTo != null;

        if (hasFilter) {
            expenses = filterExpenses(expenses, filter);
            incomes = filterIncomes(incomes, filter);
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime fromDate = null;

            switch (period) {
                case "day": fromDate = now.minusDays(1); break;
                case "week": fromDate = now.minusWeeks(1); break;
                case "month": fromDate = now.minusMonths(1); break;
                case "year": fromDate = now.minusYears(1); break;
                default: break;
            }

            if (fromDate != null) {
                final LocalDateTime filterDate = fromDate;
                expenses = expenses.stream().filter(e -> e.getDateTime().isAfter(filterDate)).toList();
                incomes = incomes.stream().filter(i -> i.getDateTime().isAfter(filterDate)).toList();
            }
        }

        return buildDashboard(expenses, incomes, model, page);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private List<Expense> filterExpenses(List<Expense> expenses, DashboardFilterDTO filter) {
        if ("income".equals(filter.getOperationType())) {
            return List.of();
        }
        if (!isEmpty(filter.getCategory()) && !"all".equals(filter.getCategory())) {
            expenses = expenses.stream()
                    .filter(e -> e.getCategory().getName().equals(filter.getCategory()))
                    .toList();
        }
        if (filter.getAmountFrom() != null) {
            expenses = expenses.stream()
                    .filter(e -> e.getAmount().compareTo(filter.getAmountFrom()) >= 0)
                    .toList();
        }
        if (filter.getAmountTo() != null) {
            expenses = expenses.stream()
                    .filter(e -> e.getAmount().compareTo(filter.getAmountTo()) <= 0)
                    .toList();
        }
        if (!isEmpty(filter.getDateFrom())) {
            LocalDate from = LocalDate.parse(filter.getDateFrom());
            expenses = expenses.stream()
                    .filter(e -> !e.getDateTime().toLocalDate().isBefore(from))
                    .toList();
        }
        if (!isEmpty(filter.getDateTo())) {
            LocalDate to = LocalDate.parse(filter.getDateTo());
            expenses = expenses.stream()
                    .filter(e -> !e.getDateTime().toLocalDate().isAfter(to))
                    .toList();
        }
        return expenses;
    }

    private List<Income> filterIncomes(List<Income> incomes, DashboardFilterDTO filter) {
        if ("expense".equals(filter.getOperationType())) {
            return List.of();
        }
        if (!isEmpty(filter.getCategory()) && !"all".equals(filter.getCategory())) {
            incomes = incomes.stream()
                    .filter(i -> i.getCategory().getName().equals(filter.getCategory()))
                    .toList();
        }
        if (filter.getAmountFrom() != null) {
            incomes = incomes.stream()
                    .filter(i -> i.getAmount().compareTo(filter.getAmountFrom()) >= 0)
                    .toList();
        }
        if (filter.getAmountTo() != null) {
            incomes = incomes.stream()
                    .filter(i -> i.getAmount().compareTo(filter.getAmountTo()) <= 0)
                    .toList();
        }
        if (!isEmpty(filter.getDateFrom())) {
            LocalDate from = LocalDate.parse(filter.getDateFrom());
            incomes = incomes.stream()
                    .filter(i -> !i.getDateTime().toLocalDate().isBefore(from))
                    .toList();
        }
        if (!isEmpty(filter.getDateTo())) {
            LocalDate to = LocalDate.parse(filter.getDateTo());
            incomes = incomes.stream()
                    .filter(i -> !i.getDateTime().toLocalDate().isAfter(to))
                    .toList();
        }
        return incomes;
    }

    private String buildDashboard(List<Expense> expenses, List<Income> incomes, Model model, int page) {
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        Map<String, BigDecimal> expenseMap = new LinkedHashMap<>();
        Map<String, BigDecimal> incomeMap = new LinkedHashMap<>();
        List<OperationDTO> operations = new ArrayList<>();

        for (Expense expense : expenses) {
            totalExpense = totalExpense.add(expense.getAmount());
            String category = expense.getCategory().getName();
            expenseMap.put(category, expenseMap.getOrDefault(category, BigDecimal.ZERO).add(expense.getAmount()));
            operations.add(new OperationDTO("EXPENSE", expense.getCategory().getName(), expense.getAmount(), expense.getDateTime()));
        }

        for (Income income : incomes) {
            totalIncome = totalIncome.add(income.getAmount());
            String category = income.getCategory().getName();
            incomeMap.put(category, incomeMap.getOrDefault(category, BigDecimal.ZERO).add(income.getAmount()));
            operations.add(new OperationDTO("INCOME", income.getCategory().getName(), income.getAmount(), income.getDateTime()));
        }

        operations.sort(Comparator.comparing(OperationDTO::getDateTime).reversed());

        int pageSize = 5;
        int totalElements = operations.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<OperationDTO> pagedOperations = totalElements > 0
                ? operations.subList(fromIndex, toIndex)
                : new ArrayList<>();

        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("balance", totalIncome.subtract(totalExpense));
        model.addAttribute("incomeCategories", incomeMap);
        model.addAttribute("expenseCategories", expenseMap);

        model.addAttribute("operations", pagedOperations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);

        return "dashboard";
    }
}