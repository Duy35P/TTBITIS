package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Customer;
import com.bitis.luckydraw.repository.CustomerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final CustomerRepository customerRepository;

    public AdminCustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "admin/customer-list";
    }

    @PostMapping("/toggle-status")
    public String toggleStatus(@RequestParam Long customerId, @RequestParam Integer status) {
        customerRepository.findById(customerId).ifPresent(customer -> {
            customer.setTrangThai(status);
            customerRepository.save(customer);
        });
        return "redirect:/admin/customers";
    }
}
