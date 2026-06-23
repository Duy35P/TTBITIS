package com.bitis.luckydraw.config;

import com.bitis.luckydraw.model.Staff;
import com.bitis.luckydraw.repository.StaffRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/pos/**").permitAll() // API POS Webhook không cần login
                .requestMatchers("/api/draw/**", "/draw", "/redeem").hasAnyRole("ADMIN", "STORE_STAFF")
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // Disable CSRF for API endpoints, keep for web forms
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * UserDetailsService dựa trên bảng Staff (Nhân viên / Admin).
     * Map role_id từ bảng staff thành Spring Security role.
     */
    @Bean
    public UserDetailsService userDetailsService(StaffRepository staffRepo) {
        return username -> {
            Staff staff = staffRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

            if (staff.getTrangThai() != 1) {
                throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + username);
            }

            return org.springframework.security.core.userdetails.User.builder()
                .username(staff.getUsername())
                .password(staff.getPassword())
                .roles(staff.getRoleId()) // role_id = "ADMIN" → ROLE_ADMIN
                .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
