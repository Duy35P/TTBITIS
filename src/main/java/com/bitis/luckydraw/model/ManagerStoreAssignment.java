package com.bitis.luckydraw.model;

import jakarta.persistence.*;

@Entity
@Table(name = "manager_store_assignment")
@IdClass(ManagerStoreAssignmentId.class)
public class ManagerStoreAssignment {

    @Id
    @Column(name = "username", length = 50)
    private String username;

    @Id
    @Column(name = "ma_store", length = 50)
    private String maStore;

    public ManagerStoreAssignment() {
    }

    public ManagerStoreAssignment(String username, String maStore) {
        this.username = username;
        this.maStore = maStore;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMaStore() {
        return maStore;
    }

    public void setMaStore(String maStore) {
        this.maStore = maStore;
    }
}
