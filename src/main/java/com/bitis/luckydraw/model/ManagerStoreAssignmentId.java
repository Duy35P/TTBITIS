package com.bitis.luckydraw.model;

import java.io.Serializable;
import java.util.Objects;

public class ManagerStoreAssignmentId implements Serializable {
    private String username;
    private String maStore;

    public ManagerStoreAssignmentId() {
    }

    public ManagerStoreAssignmentId(String username, String maStore) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagerStoreAssignmentId that = (ManagerStoreAssignmentId) o;
        return Objects.equals(username, that.username) && Objects.equals(maStore, that.maStore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, maStore);
    }
}
