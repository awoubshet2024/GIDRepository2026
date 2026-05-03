package com.alem.GIA.exception;

import com.alem.GIA.model.MemberResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }




        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<MemberResponse> handleDuplicateEmail(DataIntegrityViolationException ex) {
            String email = "unknown";
            if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                String msg = ex.getCause().getMessage();
                if (msg.contains("member_email_key")) {
                    int start = msg.indexOf('(') + 1;
                    int end = msg.indexOf(')');
                    email = msg.substring(start, end);
                }
            }

            MemberResponse response = MemberResponse.builder()
                    .result(false)
                    .message("Member with email " + email + " already exists")
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, String>> handleMultipartException(MultipartException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "File upload failed: " + e.getMessage());
        error.put("details", "Maximum file size might be exceeded");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Import failed: " + e.getMessage());
        error.put("type", e.getClass().getSimpleName());
        return ResponseEntity.badRequest().body(error);
    }
    }

