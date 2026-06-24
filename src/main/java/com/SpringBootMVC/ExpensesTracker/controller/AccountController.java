package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.AccountDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Account;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.service.AccountService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/list")
    public String listAccounts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "active") String status,
            @RequestParam(required = false, defaultValue = "name_asc") String sort,
            Model model,
            HttpSession session) {

        Client client = (Client) session.getAttribute("client");

        List<Account> allClientAccounts = accountService.findAllAccountsByClientId(client.getId());
        model.addAttribute("allClientAccounts", allClientAccounts);

        List<Account> accounts = new java.util.ArrayList<>(allClientAccounts);

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            accounts = accounts.stream()
                    .filter(a -> a.getName().toLowerCase().contains(searchLower))
                    .toList();
        }

        if (currency != null && !currency.isEmpty()) {
            accounts = accounts.stream()
                    .filter(a -> currency.equals(a.getCurrency()))
                    .toList();
        }

        if (type != null && !type.isEmpty()) {
            accounts = accounts.stream()
                    .filter(a -> type.equals(a.getType()))
                    .toList();
        }

        if ("active".equals(status)) {
            accounts = accounts.stream()
                    .filter(Account::isActive)
                    .toList();
        } else if ("inactive".equals(status)) {
            accounts = accounts.stream()
                    .filter(a -> !a.isActive())
                    .toList();
        }

        if (sort != null) {
            switch (sort) {
                case "name_asc":
                    accounts = accounts.stream()
                            .sorted(Comparator.comparing(Account::getName))
                            .toList();
                    break;
                case "name_desc":
                    accounts = accounts.stream()
                            .sorted(Comparator.comparing(Account::getName).reversed())
                            .toList();
                    break;
                case "balance_desc":
                    accounts = accounts.stream()
                            .sorted(Comparator.comparing(Account::getBalance).reversed())
                            .toList();
                    break;
                case "balance_asc":
                    accounts = accounts.stream()
                            .sorted(Comparator.comparing(Account::getBalance))
                            .toList();
                    break;
            }
        }

        BigDecimal totalBalanceInRub = accountService.getTotalBalanceInRub(client.getId());

        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalanceInRub", totalBalanceInRub);
        model.addAttribute("search", search);
        model.addAttribute("currency", currency);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);

        return "list-accounts";
    }

    @GetMapping("/showAdd")
    public String showAddAccount(Model model) {
        model.addAttribute("account", new AccountDTO());
        return "add-account";
    }

    @PostMapping("/submitAdd")
    public String submitAddAccount(@ModelAttribute("account") AccountDTO accountDTO, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        accountDTO.setClientId(client.getId());
        accountService.save(accountDTO);
        return "redirect:/accounts/list";
    }

    @GetMapping("/showUpdate")
    public String showUpdateAccount(@RequestParam("accountId") int id, Model model) {
        Account account = accountService.findAccountById(id);
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(account.getId());
        accountDTO.setName(account.getName());
        accountDTO.setType(account.getType());
        accountDTO.setBalance(account.getBalance());
        accountDTO.setCurrency(account.getCurrency());
        accountDTO.setActive(account.isActive());
        accountDTO.setDescription(account.getDescription());

        model.addAttribute("account", accountDTO);
        return "update-account";
    }

    @PostMapping("/submitUpdate")
    public String submitUpdateAccount(@ModelAttribute("account") AccountDTO accountDTO) {
        accountService.update(accountDTO);
        return "redirect:/accounts/list";
    }

    @GetMapping("/delete")
    public String deleteAccount(@RequestParam("accountId") int id, RedirectAttributes redirectAttributes) {
        try {
            accountService.deleteAccountById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Счёт успешно удалён");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/accounts/list";
    }
}