package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.IncomeDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Income;

import java.util.List;

public interface IncomeService {
    Income findIncomeById(int id);
    void save(IncomeDTO incomeDTO);
    void update(IncomeDTO incomeDTO);
    List<Income> findAllIncomesByClientId(int id);
    void deleteIncomeById(int id);
    List<Income> findFilterResult(FilterDTO filter);
}
