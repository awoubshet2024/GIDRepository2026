package com.alem.GIA.audit;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private Integer userId;

    private String action;

    private String resource;
    private String httpMethod;

    private String status;

    private String ipAddress;
    private String userAgent;

    // NEW FIELDS
    private String entityName;
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String beforeData;

    @Column(columnDefinition = "TEXT")
    private String afterData;

    private LocalDateTime timestamp = LocalDateTime.now();
}
