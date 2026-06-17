package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.IncomeDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.entity.Income;
import com.SpringBootMVC.ExpensesTracker.service.CategoryService;
import com.SpringBootMVC.ExpensesTracker.service.IncomeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/income")
public class IncomeController {
    private final IncomeService incomeService;
    private final CategoryService categoryService;

    @Autowired
    public IncomeController(IncomeService incomeService, CategoryService categoryService) {
        this.incomeService = incomeService;
        this.categoryService = categoryService;
    }

    @GetMapping("/showAdd")
    public String addIncome(Model model) {
        model.addAttribute("income", new IncomeDTO());
        model.addAttribute("categories", categoryService.findIncomeCategories());
        return "add-income";
    }

    @PostMapping("/submitAdd")
    public String submitAdd(
            @Valid @ModelAttribute("income") IncomeDTO incomeDTO,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "categories",
                    categoryService.findIncomeCategories()
            );
            return "add-income";
        }

        Client client = (Client) session.getAttribute("client");
        incomeDTO.setClientId(client.getId());
        incomeService.save(incomeDTO);
        return "redirect:/income/list";
    }

    @GetMapping("/list")
    public String list(Model model, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        List<Income> incomeList = incomeService.findAllIncomesByClientId(client.getId());

        for (Income inc : incomeList) {
            inc.setCategoryName(inc.getCategory().getName());
            inc.setDate(inc.getDateTime().toLocalDate().toString());
            inc.setTime(inc.getDateTime().toLocalTime().toString());
        }

        model.addAttribute("incomeList", incomeList);
        model.addAttribute("filter", new FilterDTO());
        model.addAttribute("categories", categoryService.findIncomeCategories());
        return "list-incomes";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("incId") int id) {
        incomeService.deleteIncomeById(id);
        return "redirect:/income/list";
    }

    @GetMapping("/showUpdate")
    public String showUpdate(@RequestParam("incId") int id, Model model){
        Income expense = incomeService.findIncomeById(id);
        IncomeDTO incomeDTO = new IncomeDTO();
        incomeDTO.setAmount(expense.getAmount());
        incomeDTO.setCategory(expense.getCategory().getName());
        incomeDTO.setDescription(expense.getDescription());
        incomeDTO.setDateTime(expense.getDateTime());

        model.addAttribute("income", incomeDTO);
        model.addAttribute("incomeId", id);

        model.addAttribute(
                "categories",
                categoryService.findIncomeCategories()
        );

        return "update-income";
    }

    @PostMapping("/submitUpdate")
    public String update(@RequestParam("incId") int id, @ModelAttribute("income") IncomeDTO incomeDTO, HttpSession session){
        Client client = (Client) session.getAttribute("client");
        incomeDTO.setIncomeId(id);
        incomeDTO.setClientId(client.getId());
        incomeService.update(incomeDTO);
        return "redirect:/income/list";
    }

    @PostMapping("/processFilter")
    public String processFilter(@ModelAttribute("filter") FilterDTO filter, Model model){
        System.out.println("--------------------------------------------------------------");
        System.out.println("filter values : " + filter);
        List<Income> incomeList = incomeService.findFilterResult(filter);
        System.out.println("size ----> " + incomeList.size());
        System.out.println(incomeList);
        for (Income income : incomeList){
            income.setCategoryName(categoryService.findCategoryById(income.getCategory().getId()).getName());
            income.setDate((income.getDateTime()).toLocalDate().toString());
            income.setTime((income.getDateTime()).toLocalTime().toString());
        }
        model.addAttribute("incomeList", incomeList);
        model.addAttribute("filter", filter);
        return "filter-income-result";
    }
}
