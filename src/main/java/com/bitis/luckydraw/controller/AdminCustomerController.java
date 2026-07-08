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
    private final com.bitis.luckydraw.repository.RewardVoucherRepository rewardVoucherRepository;

    public AdminCustomerController(CustomerRepository customerRepository, CustomerTurnRepository customerTurnRepository, com.bitis.luckydraw.repository.RewardVoucherRepository rewardVoucherRepository) {
        this.customerRepository = customerRepository;
        this.customerTurnRepository = customerTurnRepository;
        this.rewardVoucherRepository = rewardVoucherRepository;
    }

    @GetMapping
    public String listCustomers(Model model) {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerTurn> turns = customerTurnRepository.findAll();
        List<com.bitis.luckydraw.dto.RewardVoucherListDto> vouchers = rewardVoucherRepository.getRewardVoucherList();
        
        Map<String, String> turnsMap = turns.stream()
            .collect(Collectors.groupingBy(
                CustomerTurn::getMaKhachHang,
                Collectors.mapping(
                    t -> t.getMaChienDich() + ": " + t.getLuotConLai(),
                    Collectors.joining(", ")
                )
            ));

        Map<String, String> prizesMap = vouchers.stream()
            .collect(Collectors.groupingBy(
                com.bitis.luckydraw.dto.RewardVoucherListDto::getMaKhachHang,
                Collectors.mapping(
                    com.bitis.luckydraw.dto.RewardVoucherListDto::getTenGiai,
                    Collectors.joining(", ")
                )
            ));

        model.addAttribute("customers", customers);
        model.addAttribute("turnsMap", turnsMap);
        model.addAttribute("prizesMap", prizesMap);
        return "admin/customer-list";
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasAuthority('QL_KHACHHANG')")
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
            List<com.bitis.luckydraw.dto.RewardVoucherListDto> vouchers = rewardVoucherRepository.getRewardVoucherList();
            
            Map<String, String> turnsMap = turns.stream()
                .collect(Collectors.groupingBy(
                    CustomerTurn::getMaKhachHang,
                    Collectors.mapping(
                        t -> t.getMaChienDich() + ": " + t.getLuotConLai(),
                        Collectors.joining(", ")
                    )
                ));

            Map<String, String> prizesMap = vouchers.stream()
                .collect(Collectors.groupingBy(
                    com.bitis.luckydraw.dto.RewardVoucherListDto::getMaKhachHang,
                    Collectors.mapping(
                        com.bitis.luckydraw.dto.RewardVoucherListDto::getTenGiai,
                        Collectors.joining(", ")
                    )
                ));

            String[] headers = {"Mã Khách Hàng", "Tên Khách Hàng", "Số Điện Thoại", "Trạng Thái", "Lượt Còn Lại", "Quà Đã Trúng"};
            List<String[]> data = customers.stream().map(c -> new String[]{
                c.getMaKhachHang(),
                c.getTenKhach() != null ? c.getTenKhach() : "",
                c.getPhone() != null ? c.getPhone() : "",
                (c.getTrangThai() != null && c.getTrangThai() == 1) ? "Hoạt động" : "Bị khóa",
                turnsMap.getOrDefault(c.getMaKhachHang(), "Không có"),
                prizesMap.getOrDefault(c.getMaKhachHang(), "Chưa trúng giải")
            }).collect(Collectors.toList());
            
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "DanhSachKhachHang", headers, data);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
