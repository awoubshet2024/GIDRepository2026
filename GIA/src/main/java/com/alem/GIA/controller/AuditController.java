package com.alem.GIA.controller;

import com.alem.GIA.audit.AuditLog;
import com.alem.GIA.repository.AuditLogRepository;
import com.alem.GIA.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")

public class AuditController {

    private final AuditService auditService;
    private final AuditLogRepository repo;
    public AuditController(AuditService auditService, AuditLogRepository repo){

        this.auditService = auditService;
        this.repo = repo;
    }

    @GetMapping
    public List<AuditLog> search(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action
    ) {
        return auditService.search(username, action, null, null);
    }

    @GetMapping("/entity/{name}/{id}")
    public List<AuditLog> byEntity(
            @PathVariable String name,
            @PathVariable String id
    ) {
        return auditService.search(null, null, null, null)
                .stream()
                .filter(l ->
                        name.equals(l.getEntityName()) &&
                                id.equals(l.getEntityId())
                )
                .toList();
    }

    // CSV EXPORT
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() throws Exception {

        List<AuditLog> logs = repo.findAll();

        StringBuilder sb = new StringBuilder();

        sb.append("username,action,entity,entityId,status,timestamp\n");

        for (AuditLog l : logs) {
            sb.append(l.getUsername()).append(",")
                    .append(l.getAction()).append(",")
                    .append(l.getEntityName()).append(",")
                    .append(l.getEntityId()).append(",")
                    .append(l.getStatus()).append(",")
                    .append(l.getTimestamp()).append("\n");
        }


        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=audit.csv")
                .header("Content-Type", "text/csv")
                .body(sb.toString().getBytes());


    }
}
