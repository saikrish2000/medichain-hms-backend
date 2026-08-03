package com.hospital.service;

import com.hospital.entity.AuditLog;
import com.hospital.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Slf4j
public class AuditService {

    private final AuditLogRepository repo;

    @Async
    public void log(String username, String action, String entityType, Long entityId, String details) {
        try {
            repo.save(AuditLog.builder()
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .status("SUCCESS")
                .build());
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    public Page<AuditLog> getAll(int page) {
        return repo.findAll(PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
