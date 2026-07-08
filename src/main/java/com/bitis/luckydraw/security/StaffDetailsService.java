package com.bitis.luckydraw.security;

import com.bitis.luckydraw.model.Staff;
import com.bitis.luckydraw.repository.StaffRepository;
import com.bitis.luckydraw.model.ManagerStoreAssignment;
import com.bitis.luckydraw.repository.ManagerStoreAssignmentRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bitis.luckydraw.model.PhanQuyen;
import com.bitis.luckydraw.repository.PhanQuyenRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class StaffDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;
    private final PhanQuyenRepository phanQuyenRepository;
    private final ManagerStoreAssignmentRepository managerStoreAssignmentRepository;

    public StaffDetailsService(StaffRepository staffRepository, 
                               PhanQuyenRepository phanQuyenRepository,
                               ManagerStoreAssignmentRepository managerStoreAssignmentRepository) {
        this.staffRepository = staffRepository;
        this.phanQuyenRepository = phanQuyenRepository;
        this.managerStoreAssignmentRepository = managerStoreAssignmentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Staff staff = staffRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found or locked"));
            
        if (staff.getTrangThai() != 1) {
            throw new UsernameNotFoundException("User not found or locked");
        }

        List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
        // Add prefix ROLE_ to the role ID from DB (e.g. ROLE_ADMIN, ROLE_MANAGER)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + staff.getRoleId()));
        
        List<PhanQuyen> permissions = phanQuyenRepository.findByIdRoleId(staff.getRoleId());
        for (PhanQuyen pq : permissions) {
            authorities.add(new SimpleGrantedAuthority(pq.getId().getMaChucNang()));
        }

        // ponytail: prepend {noop} for plain text if it hasn't been migrated yet, 
        // to prevent spring security from failing. The CommandLineRunner will migrate them eventually.
        String pwd = staff.getPassword();
        if (!pwd.startsWith("{bcrypt}") && !pwd.startsWith("$2a$") && !pwd.startsWith("{noop}")) {
            pwd = "{noop}" + pwd; 
        }

        List<String> assignedStores = null;
        List<ManagerStoreAssignment> msaList = managerStoreAssignmentRepository.findByUsername(username);
        if (msaList != null && !msaList.isEmpty()) {
            assignedStores = new ArrayList<>();
            for (ManagerStoreAssignment msa : msaList) {
                assignedStores.add(msa.getMaStore());
            }
        }

        return new CustomUserDetails(
                staff.getUsername(),
                pwd,
                authorities,
                staff.getMaStore(),
                staff.getTenNhanVien(),
                assignedStores
        );
    }
}
