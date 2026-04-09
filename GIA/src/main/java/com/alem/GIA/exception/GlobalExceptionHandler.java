package com.alem.GIA.exception;

import com.alem.GIA.model.MemberResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    }

