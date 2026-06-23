package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.service.AccountService;
import com.SpringBootMVC.ExpensesTracker.service.CategoryService;
import com.SpringBootMVC.ExpensesTracker.service.ExchangeRateService;
import com.SpringBootMVC.ExpensesTracker.service.ExpenseService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final ExchangeRateService exchangeRateService;

    @GetMapping("/")
    public String landingPage(HttpSession session, Model model){
        Client client = (Client) session.getAttribute("client");
        model.addAttribute("sessionClient", client);
        return "landing-page";
    }

    @GetMapping("/showAdd")
    public String addExpense(Model model, HttpSession session){
        Client client = (Client) session.getAttribute("client");
        model.addAttribute("expense", new ExpenseDTO());
        model.addAttribute("categories", categoryService.findExpenseCategories());
        model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
        return "add-expense";
    }

    @PostMapping("/submitAdd")
    public String submitAdd(
            @Valid @ModelAttribute("expense") ExpenseDTO expenseDTO,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        if (bindingResult.hasErrors()) {
            Client client = (Client) session.getAttribute("client");
            model.addAttribute("categories", categoryService.findExpenseCategories());
            model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
            return "add-expense";
        }

        Client client = (Client) session.getAttribute("client");
        expenseDTO.setClientId(client.getId());

        try {
            expenseService.save(expenseDTO);
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("categories", categoryService.findExpenseCategories());
            model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
            model.addAttribute("errorMessage", e.getMessage());
            return "add-expense";
        }
    }

    @GetMapping("/list")
    public String list(Model model, HttpSession session){
        Client client = (Client) session.getAttribute("client");
        model.addAttribute("categories", categoryService.findExpenseCategories());
        model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId())); // ← ДОБАВЛЕНО

        int clientId = client.getId();
        List<Expense> expenseList = expenseService.findAllExpensesByClientId(clientId);

        for (Expense expense : expenseList){
            expense.setCategoryName(categoryService.findCategoryById(expense.getCategory().getId()).getName());
            expense.setDate(expense.getDateTime().toLocalDate().toString());
            expense.setTime(expense.getDateTime().toLocalTime().toString());

            if (expense.getAccount() != null) {
                expense.setAccountName(expense.getAccount().getName());
                expense.setAccountCurrency(expense.getAccount().getCurrency());
                expense.setAmountInRub(
                        exchangeRateService.convertToRub(expense.getAmount(), expense.getAccount().getCurrency())
                                .setScale(2, java.math.RoundingMode.HALF_UP)
                );
            } else {
                expense.setAccountName("Без счёта");
                expense.setAccountCurrency("RUB");
                expense.setAmountInRub(expense.getAmount().setScale(2, java.math.RoundingMode.HALF_UP));
            }
        }

        model.addAttribute("expenseList", expenseList);
        model.addAttribute("filter", new FilterDTO());
        model.addAttribute("categories", categoryService.findExpenseCategories());
        model.addAttribute("months", getMonthsList());
        model.addAttribute("years", getYearsList(expenseList));

        return "list-page";
    }

    private List<Map<String, String>> getMonthsList() {
        List<Map<String, String>> months = new ArrayList<>();
        String[] monthNames = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        for (int i = 1; i <= 12; i++) {
            Map<String, String> month = new HashMap<>();
            month.put("value", String.format("%02d", i));
            month.put("name", monthNames[i - 1]);
            months.add(month);
        }

        return months;
    }

    private List<Integer> getYearsList(List<Expense> expenses) {
        Set<Integer> yearsSet = new TreeSet<>(Collections.reverseOrder()); // Сортировка по убыванию

        int currentYear = java.time.LocalDate.now().getYear();
        yearsSet.add(currentYear);
        yearsSet.add(currentYear + 1);
        yearsSet.add(currentYear + 2);

        for (Expense expense : expenses) {
            if (expense.getDateTime() != null) {
                yearsSet.add(expense.getDateTime().getYear());
            }
        }

        return new ArrayList<>(yearsSet);
    }

    @GetMapping("/showUpdate")
    public String showUpdate(@RequestParam("expId") int id, Model model, HttpSession session){
        Client client = (Client) session.getAttribute("client");
        Expense expense = expenseService.findExpenseById(id);
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setCategory(expense.getCategory().getName());
        expenseDTO.setDescription(expense.getDescription());
        expenseDTO.setDateTime(expense.getDateTime());

        int currentAccountId = 0;
        String currentAccountCurrency = "RUB";
        String currentAccountName = "Без счёта";

        if (expense.getAccount() != null) {
            expenseDTO.setAccountId(expense.getAccount().getId());
            currentAccountId = expense.getAccount().getId();
            currentAccountCurrency = expense.getAccount().getCurrency();
            currentAccountName = expense.getAccount().getName();
        }

        if (expense.getOriginalAmount() != null) {
            expenseDTO.setOriginalAmount(expense.getOriginalAmount());
            expenseDTO.setOriginalCurrency(expense.getOriginalCurrency() != null
                    ? expense.getOriginalCurrency()
                    : currentAccountCurrency);
        } else {
            expenseDTO.setOriginalAmount(expense.getAmount());
            expenseDTO.setOriginalCurrency(currentAccountCurrency);
        }

        model.addAttribute("expense", expenseDTO);
        model.addAttribute("expenseId", id);
        model.addAttribute("categories", categoryService.findExpenseCategories());
        model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
        model.addAttribute("currentAccountId", currentAccountId);
        model.addAttribute("currentAccountName", currentAccountName);
        model.addAttribute("currentAccountCurrency", currentAccountCurrency);

        return "update-page";
    }

    @PostMapping("/submitUpdate")
    public String update(
            @RequestParam("expId") int id,
            @ModelAttribute("expense") ExpenseDTO expenseDTO,
            HttpSession session,
            Model model) {
        Client client = (Client) session.getAttribute("client");

        Expense oldExpense = expenseService.findExpenseById(id);

        expenseDTO.setExpenseId(id);
        expenseDTO.setClientId(client.getId());
        expenseDTO.setCategory(oldExpense.getCategory().getName());

        try {
            expenseService.update(expenseDTO);
            return "redirect:/list";
        } catch (RuntimeException e) {
            model.addAttribute("expense", expenseDTO);
            model.addAttribute("expenseId", id);
            model.addAttribute("categories", categoryService.findExpenseCategories());
            model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
            model.addAttribute("errorMessage", e.getMessage());

            if (oldExpense.getAccount() != null) {
                model.addAttribute("currentAccountId", oldExpense.getAccount().getId());
                model.addAttribute("currentAccountName", oldExpense.getAccount().getName());
                model.addAttribute("currentAccountCurrency", oldExpense.getAccount().getCurrency());
            } else {
                model.addAttribute("currentAccountId", 0);
                model.addAttribute("currentAccountName", "Без счёта");
                model.addAttribute("currentAccountCurrency", "RUB");
            }

            return "update-page";
        }
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("expId") int id){
        expenseService.deleteExpenseById(id);
        return "redirect:/list";
    }

    @PostMapping("/processFilter")
    public String processFilter(@ModelAttribute("filter") FilterDTO filter, Model model, HttpSession session) {
        System.out.println("--------------------------------------------------------------");
        System.out.println("filter values : " + filter);

        Client client = (Client) session.getAttribute("client");
        List<Expense> expenseList = expenseService.findFilterResult(filter);
        System.out.println("size ----> " + expenseList.size());

        for (Expense expense : expenseList) {
            expense.setCategoryName(categoryService.findCategoryById(expense.getCategory().getId()).getName());
            expense.setDate((expense.getDateTime()).toLocalDate().toString());
            expense.setTime((expense.getDateTime()).toLocalTime().toString());

            if (expense.getAccount() != null) {
                expense.setAccountName(expense.getAccount().getName());
                expense.setAccountCurrency(expense.getAccount().getCurrency());
                expense.setAmountInRub(
                        exchangeRateService.convertToRub(expense.getAmount(), expense.getAccount().getCurrency())
                                .setScale(2, java.math.RoundingMode.HALF_UP)
                );
            } else {
                expense.setAccountName("Без счёта");
                expense.setAccountCurrency("RUB");
                expense.setAmountInRub(expense.getAmount().setScale(2, java.math.RoundingMode.HALF_UP));
            }
        }

        model.addAttribute("expenseList", expenseList);
        model.addAttribute("filter", filter);
        return "filter-result";
    }
}
