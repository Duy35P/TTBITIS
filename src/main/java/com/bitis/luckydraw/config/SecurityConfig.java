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
                .requestMatchers("/admin/login", "/css/**", "/js/**", "/plugins/**").permitAll()
                // Phân quyền theo yêu cầu
                .requestMatchers("/admin/campaigns/**").hasAnyAuthority("ROLE_ADMIN", "QL_CHIENDICH")
                .requestMatchers("/admin/vouchers/**").hasAnyAuthority("ROLE_ADMIN", "QL_VOUCHER")
                .requestMatchers("/admin/prizes/**").hasAnyAuthority("ROLE_ADMIN", "QL_GIAITHUONG", "QL_PHANBO")
                .requestMatchers("/admin/invoices/**").hasAnyAuthority("ROLE_ADMIN", "QL_HOADON")
                .requestMatchers("/admin/redemption/**").hasAnyAuthority("ROLE_ADMIN", "QL_KIEMTRAMA")
                .requestMatchers("/admin/profile/**").authenticated()
                .requestMatchers("/admin/customers/**").hasAnyAuthority("ROLE_ADMIN", "QL_KHACHHANG")
                .requestMatchers("/admin/staffs/**").hasAnyAuthority("ROLE_ADMIN", "QL_NHANVIEN")
                .requestMatchers("/admin/stores/**").hasAnyAuthority("ROLE_ADMIN", "QL_CUAHANG")
                .requestMatchers("/admin/turns/**").hasAnyAuthority("ROLE_ADMIN", "QL_LUOTQUAY")
                .requestMatchers("/admin", "/admin/").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll() // Allow customer-facing endpoints
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
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
