package com.bitis.luckydraw.config;

import com.bitis.luckydraw.repository.StaffRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/quanly/login", "/css/**", "/js/**", "/plugins/**").permitAll()
                // Phân quyền theo yêu cầu
                .requestMatchers("/quanly/campaigns", "/quanly/campaigns/**").hasAnyAuthority("ROLE_ADMIN", "QL_CHIENDICH")
                .requestMatchers("/quanly/vouchers", "/quanly/vouchers/**").hasAnyAuthority("ROLE_ADMIN", "QL_VOUCHER")
                .requestMatchers("/quanly/prizes", "/quanly/prizes/**").hasAnyAuthority("ROLE_ADMIN", "QL_GIAITHUONG", "QL_PHANBO")
                .requestMatchers("/quanly/invoices", "/quanly/invoices/**").hasAnyAuthority("ROLE_ADMIN", "QL_HOADON")
                .requestMatchers("/quanly/redemption", "/quanly/redemption/**").hasAnyAuthority("ROLE_ADMIN", "QL_KIEMTRAMA")
                .requestMatchers("/quanly/profile", "/quanly/profile/**").authenticated()
                .requestMatchers("/quanly/customers", "/quanly/customers/**").hasAnyAuthority("ROLE_ADMIN", "QL_KHACHHANG")
                .requestMatchers("/quanly/staffs", "/quanly/staffs/**").hasAnyAuthority("ROLE_ADMIN", "QL_NHANVIEN")
                .requestMatchers("/quanly/stores", "/quanly/stores/**").hasAnyAuthority("ROLE_ADMIN", "QL_CUAHANG")
                .requestMatchers("/quanly/turns", "/quanly/turns/**").hasAnyAuthority("ROLE_ADMIN", "QL_LUOTQUAY")
                .requestMatchers("/quanly", "/quanly/").authenticated()
                .requestMatchers("/quanly/**").hasRole("ADMIN")
                .anyRequest().permitAll() // Allow customer-facing endpoints
            )
            .formLogin(form -> form
                .loginPage("/quanly/login")
                .loginProcessingUrl("/quanly/login")
                .defaultSuccessUrl("/quanly", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/quanly/logout")
                .logoutSuccessUrl("/quanly/login?logout")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // ponytail: One-time migration for old plaintext passwords to BCrypt
    @Bean
    public CommandLineRunner migratePasswords(StaffRepository repo, PasswordEncoder encoder) {
        return args -> {
            repo.findAll().forEach(staff -> {
                String pwd = staff.getPassword();
                if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("{noop}") && !pwd.startsWith("{bcrypt}")) {
                    staff.setPassword(encoder.encode(pwd));
                    repo.save(staff);
                }
            });
        };
    }
}
