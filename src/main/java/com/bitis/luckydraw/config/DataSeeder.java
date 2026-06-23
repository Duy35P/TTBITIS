package com.bitis.luckydraw.config;

import com.bitis.luckydraw.model.Staff;
import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.model.VaiTro;
import com.bitis.luckydraw.repository.StaffRepository;
import com.bitis.luckydraw.repository.StoreRepository;
import com.bitis.luckydraw.repository.VaiTroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(
            VaiTroRepository vaiTroRepo,
            StaffRepository staffRepo,
            StoreRepository storeRepo,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // 1. Seed vai trò (roles) nếu chưa có
            if (vaiTroRepo.count() == 0) {
                System.out.println("=== Seeding vai_tro ===");

                VaiTro admin = new VaiTro();
                admin.setRoleId("ADMIN");
                admin.setRoleName("Quản trị viên");
                admin.setMoTa("Toàn quyền quản lý hệ thống");
                vaiTroRepo.save(admin);

                VaiTro manager = new VaiTro();
                manager.setRoleId("MANAGER");
                manager.setRoleName("Quản lý");
                manager.setMoTa("Quản lý cửa hàng và chiến dịch");
                vaiTroRepo.save(manager);

                VaiTro storeStaff = new VaiTro();
                storeStaff.setRoleId("STORE_STAFF");
                storeStaff.setRoleName("Nhân viên bán hàng");
                storeStaff.setMoTa("Nhân viên tại cửa hàng, thực hiện đổi quà");
                vaiTroRepo.save(storeStaff);

                System.out.println("Đã seed 3 vai trò: ADMIN, MANAGER, STORE_STAFF");
            }

            // 2. Seed Staff (admin mặc định) nếu chưa có
            if (staffRepo.count() == 0) {
                System.out.println("=== Seeding staff ===");

                Staff adminStaff = new Staff();
                adminStaff.setUsername("admin");
                adminStaff.setPassword(passwordEncoder.encode("admin123"));
                adminStaff.setTenNhanVien("Administrator");
                adminStaff.setRoleId("ADMIN");
                adminStaff.setTrangThai(1);
                staffRepo.save(adminStaff);

                System.out.println("Đã seed tài khoản admin (username: admin / password: admin123)");
            }

            // 3. Seed Store mẫu nếu chưa có
            if (storeRepo.count() == 0) {
                System.out.println("=== Seeding store ===");

                Store store1 = new Store();
                store1.setTenCuaHang("Biti's Quận 1");
                store1.setDiaChiStore("123 Nguyễn Huệ, Q.1, TP.HCM");
                store1.setMaStore("BITIS-Q1");
                store1.setTrangThai(1);
                storeRepo.save(store1);

                Store store2 = new Store();
                store2.setTenCuaHang("Biti's Quận 7");
                store2.setDiaChiStore("456 Nguyễn Thị Thập, Q.7, TP.HCM");
                store2.setMaStore("BITIS-Q7");
                store2.setTrangThai(1);
                storeRepo.save(store2);

                System.out.println("Đã seed 2 cửa hàng mẫu");
            }

            System.out.println("=== DataSeeder hoàn tất ===");
        };
    }
}
