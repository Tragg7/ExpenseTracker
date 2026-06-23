package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.TransferDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.service.AccountService;
import com.SpringBootMVC.ExpensesTracker.service.TransferService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class TransferController {

    private final AccountService accountService;
    private final TransferService transferService;

    @GetMapping("/showTransfer")
    public String showTransfer(Model model, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        model.addAttribute("transfer", new TransferDTO());
        model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
        return "transfer";
    }

    @PostMapping("/submitTransfer")
    public String submitTransfer(
            @Valid @ModelAttribute("transfer") TransferDTO transferDTO,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        Client client = (Client) session.getAttribute("client");

        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
            return "transfer";
        }

        if (transferDTO.getFromAccountId().equals(transferDTO.getToAccountId())) {
            model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
            model.addAttribute("errorMessage", "Нельзя перевести средства на тот же счёт");
            return "transfer";
        }

        transferDTO.setClientId(client.getId());
        if (transferDTO.getDateTime() == null) {
            transferDTO.setDateTime(LocalDateTime.now());
        }

        try {
            transferService.transfer(transferDTO);
            return "redirect:/accounts/list";
        } catch (RuntimeException e) {
            model.addAttribute("accounts", accountService.findActiveAccountsByClientId(client.getId()));
            model.addAttribute("errorMessage", e.getMessage());
            return "transfer";
        }
    }
}