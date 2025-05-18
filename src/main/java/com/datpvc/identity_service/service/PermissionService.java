package com.datpvc.identity_service.service;

import com.datpvc.identity_service.dto.request.PermissionRequest;
import com.datpvc.identity_service.dto.response.PermissionResponse;
import com.datpvc.identity_service.entity.Permission;
import com.datpvc.identity_service.mapper.PermissionMapper;
import com.datpvc.identity_service.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll()
                .stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void delete(String permissionId) {
       permissionRepository.deleteById(permissionId);
    }
}
