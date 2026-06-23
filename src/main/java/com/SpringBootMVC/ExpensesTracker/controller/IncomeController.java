package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.IncomeDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.entity.Income;
import com.SpringBootMVC.ExpensesTracker.service.AccountService;
import com.SpringBootMVC.ExpensesTracker.service.CategoryService;
import com.SpringBootMVC.ExpensesTracker.service.ExchangeRateService;
import com.SpringBootMVC.ExpensesTracker.service.IncomeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/income")
public class IncomeController {
    private final IncomeService incomeService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final ExchangeRateService exchangeRateService;

    @GetMapping("/showAdd")
    public String addIncome(Model model, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        model.addAttribute("income", new IncomeDTO());
        model.addAttribute("categories", categoryService.findIncomeCategories());
        model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
        return "add-income";
    }

    @PostMapping("/submitAdd")
    public String submitAdd(
            @Valid @ModelAttribute("income") IncomeDTO incomeDTO,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        Client client = (Client) session.getAttribute("client");

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findIncomeCategories());
            model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
            return "add-income";
        }

        incomeDTO.setClientId(client.getId());

        try {
            incomeService.save(incomeDTO);
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("categories", categoryService.findIncomeCategories());
            model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
            model.addAttribute("errorMessage", e.getMessage());
            return "add-income";
        }
    }

    @GetMapping("/list")
    public String list(Model model, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        List<Income> incomeList = incomeService.findAllIncomesByClientId(client.getId());

        for (Income inc : incomeList) {
            inc.setCategoryName(inc.getCategory().getName());
            inc.setDate(inc.getDateTime().toLocalDate().toString());
            inc.setTime(inc.getDateTime().toLocalTime().toString());

            if (inc.getAccount() != null) {
                inc.setAccountName(inc.getAccount().getName());
                inc.setAccountCurrency(inc.getAccount().getCurrency());
                inc.setAmountInRub(
                        exchangeRateService.convertToRub(inc.getAmount(), inc.getAccount().getCurrency())
                                .setScale(2, java.math.RoundingMode.HALF_UP)
                );
            } else {
                inc.setAccountName("Без счёта");
                inc.setAccountCurrency("RUB");
                inc.setAmountInRub(inc.getAmount().setScale(2, java.math.RoundingMode.HALF_UP));
            }
        }

        model.addAttribute("incomeList", incomeList);
        model.addAttribute("filter", new FilterDTO());
        model.addAttribute("categories", categoryService.findIncomeCategories());
        model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
        model.addAttribute("months", getMonthsList());
        model.addAttribute("years", getYearsList(incomeList));

        return "list-incomes";
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

    private List<Integer> getYearsList(List<Income> incomes) {
        Set<Integer> yearsSet = new TreeSet<>(Collections.reverseOrder());

        int currentYear = java.time.LocalDate.now().getYear();
        yearsSet.add(currentYear);
        yearsSet.add(currentYear + 1);
        yearsSet.add(currentYear + 2);

        for (Income income : incomes) {
            if (income.getDateTime() != null) {
                yearsSet.add(income.getDateTime().getYear());
            }
        }

        return new ArrayList<>(yearsSet);
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("incId") int id) {
        incomeService.deleteIncomeById(id);
        return "redirect:/income/list";
    }

    @GetMapping("/showUpdate")
    public String showUpdate(@RequestParam("incId") int id, Model model, HttpSession session){
        Client client = (Client) session.getAttribute("client");
        Income income = incomeService.findIncomeById(id);
        IncomeDTO incomeDTO = new IncomeDTO();
        incomeDTO.setAmount(income.getAmount());
        incomeDTO.setCategory(income.getCategory().getName());
        incomeDTO.setDescription(income.getDescription());
        incomeDTO.setDateTime(income.getDateTime());

        int currentAccountId = 0;
        String currentAccountCurrency = "RUB";
        String currentAccountName = "Без счёта";

        if (income.getAccount() != null) {
            incomeDTO.setAccountId(income.getAccount().getId());
            currentAccountId = income.getAccount().getId();
            currentAccountCurrency = income.getAccount().getCurrency();
            currentAccountName = income.getAccount().getName();
        }

        if (income.getOriginalAmount() != null) {
            incomeDTO.setOriginalAmount(income.getOriginalAmount());
            incomeDTO.setOriginalCurrency(income.getOriginalCurrency() != null
                    ? income.getOriginalCurrency()
                    : currentAccountCurrency);
        } else {
            incomeDTO.setOriginalAmount(income.getAmount());
            incomeDTO.setOriginalCurrency(currentAccountCurrency);
        }

        model.addAttribute("income", incomeDTO);
        model.addAttribute("incomeId", id);
        model.addAttribute("categories", categoryService.findIncomeCategories());
        model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
        model.addAttribute("currentAccountId", currentAccountId);
        model.addAttribute("currentAccountName", currentAccountName);
        model.addAttribute("currentAccountCurrency", currentAccountCurrency);

        return "update-income";
    }

    @PostMapping("/submitUpdate")
    public String update(
            @RequestParam("incId") int id,
            @ModelAttribute("income") IncomeDTO incomeDTO,
            HttpSession session,
            Model model) {
        Client client = (Client) session.getAttribute("client");
        incomeDTO.setIncomeId(id);
        incomeDTO.setClientId(client.getId());

        Income oldIncome = incomeService.findIncomeById(id);

        try {
            incomeService.update(incomeDTO);
            return "redirect:/income/list";
        } catch (RuntimeException e) {
            model.addAttribute("income", incomeDTO);
            model.addAttribute("incomeId", id);
            model.addAttribute("categories", categoryService.findIncomeCategories());
            model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
            model.addAttribute("errorMessage", e.getMessage());

            if (oldIncome.getAccount() != null) {
                model.addAttribute("currentAccountId", oldIncome.getAccount().getId());
                model.addAttribute("currentAccountName", oldIncome.getAccount().getName());
                model.addAttribute("currentAccountCurrency", oldIncome.getAccount().getCurrency());
            } else {
                model.addAttribute("currentAccountId", 0);
                model.addAttribute("currentAccountName", "Без счёта");
                model.addAttribute("currentAccountCurrency", "RUB");
            }
            return "update-income";
        }
    }

    @PostMapping("/processFilter")
    public String processFilter(@ModelAttribute("filter") FilterDTO filter, Model model, HttpSession session) {
        System.out.println("--------------------------------------------------------------");
        System.out.println("filter values : " + filter);

        Client client = (Client) session.getAttribute("client");
        List<Income> incomeList = incomeService.findFilterResult(filter);
        System.out.println("size ----> " + incomeList.size());

        for (Income income : incomeList) {
            income.setCategoryName(categoryService.findCategoryById(income.getCategory().getId()).getName());
            income.setDate((income.getDateTime()).toLocalDate().toString());
            income.setTime((income.getDateTime()).toLocalTime().toString());

            if (income.getAccount() != null) {
                income.setAccountName(income.getAccount().getName());
                income.setAccountCurrency(income.getAccount().getCurrency());
                income.setAmountInRub(
                        exchangeRateService.convertToRub(income.getAmount(), income.getAccount().getCurrency())
                                .setScale(2, java.math.RoundingMode.HALF_UP)
                );
            } else {
                income.setAccountName("Без счёта");
                income.setAccountCurrency("RUB");
                income.setAmountInRub(income.getAmount().setScale(2, java.math.RoundingMode.HALF_UP));
            }
        }

        model.addAttribute("incomeList", incomeList);
        model.addAttribute("filter", filter);
        model.addAttribute("categories", categoryService.findIncomeCategories());
        model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId())); // ← ДОБАВЛЕНО
        return "filter-income-result";
    }
}
