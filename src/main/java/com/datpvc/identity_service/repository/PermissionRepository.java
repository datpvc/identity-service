package com.datpvc.identity_service.repository;

import com.datpvc.identity_service.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    void deleteByName(String name);
}
