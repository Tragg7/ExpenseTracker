package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.AccountDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Account;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/list")
    public String listAccounts(Model model, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        List<Account> accounts = accountService.findAllAccountsByClientId(client.getId());

        BigDecimal totalBalanceInRub = accountService.getTotalBalanceInRub(client.getId());

        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalanceInRub", totalBalanceInRub);

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
    public String deleteAccount(@RequestParam("accountId") int id) {
        accountService.deleteAccountById(id);
        return "redirect:/accounts/list";
    }
}