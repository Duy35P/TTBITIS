package com.bitis.luckydraw.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails extends User {

    private final String maStore;
    private final String tenNhanVien;
    private final List<String> assignedStores;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String maStore, String tenNhanVien, List<String> assignedStores) {
        super(username, password, authorities);
        this.maStore = maStore;
        this.tenNhanVien = tenNhanVien;
        this.assignedStores = assignedStores;
    }

    public String getMaStore() {
        return maStore;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public List<String> getAssignedStores() {
        return assignedStores;
    }

    public boolean isAdmin() {
        return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public List<String> getEffectiveStores() {
        if (isAdmin()) return null;
        if (assignedStores != null && !assignedStores.isEmpty()) return assignedStores;
        if (maStore != null) return List.of(maStore);
        return null;
    }
}
