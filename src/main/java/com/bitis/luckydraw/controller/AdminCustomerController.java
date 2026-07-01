package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Customer;
import com.bitis.luckydraw.model.CustomerTurn;
import com.bitis.luckydraw.repository.CustomerRepository;
import com.bitis.luckydraw.repository.CustomerTurnRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerTurnRepository customerTurnRepository;

    public AdminCustomerController(CustomerRepository customerRepository, CustomerTurnRepository customerTurnRepository) {
        this.customerRepository = customerRepository;
        this.customerTurnRepository = customerTurnRepository;
    }

    @GetMapping
    public String listCustomers(Model model) {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerTurn> turns = customerTurnRepository.findAll();
        
        Map<String, String> turnsMap = turns.stream()
            .collect(Collectors.groupingBy(
                CustomerTurn::getMaKhachHang,
                Collectors.mapping(
                    t -> t.getMaChienDich() + ": " + t.getLuotConLai(),
                    Collectors.joining(", ")
                )
            ));

        model.addAttribute("customers", customers);
        model.addAttribute("turnsMap", turnsMap);
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

    @GetMapping("/export-excel")
    public void exportExcel(jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<Customer> customers = customerRepository.findAll();
            List<CustomerTurn> turns = customerTurnRepository.findAll();
            
            Map<String, String> turnsMap = turns.stream()
                .collect(Collectors.groupingBy(
                    CustomerTurn::getMaKhachHang,
                    Collectors.mapping(
                        t -> t.getMaChienDich() + ": " + t.getLuotConLai(),
                        Collectors.joining(", ")
                    )
                ));

            String[] headers = {"Mã Khách Hàng", "Tên Khách Hàng", "Số Điện Thoại", "Trạng Thái", "Lượt Còn Lại"};
            List<String[]> data = customers.stream().map(c -> new String[]{
                c.getMaKhachHang(),
                c.getTenKhach() != null ? c.getTenKhach() : "",
                c.getPhone() != null ? c.getPhone() : "",
                (c.getTrangThai() != null && c.getTrangThai() == 1) ? "Hoạt động" : "Bị khóa",
                turnsMap.getOrDefault(c.getMaKhachHang(), "Không có")
            }).collect(Collectors.toList());
            
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "DanhSachKhachHang", headers, data);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
