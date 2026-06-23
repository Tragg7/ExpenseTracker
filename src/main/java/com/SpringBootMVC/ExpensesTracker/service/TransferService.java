package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.TransferDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.IncomeDTO;

public interface TransferService {
    void transfer(TransferDTO transferDTO);
    void saveInitialBalance(IncomeDTO incomeDTO);
}
