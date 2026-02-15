package com.alem.GIA.model;

import com.alem.GIA.entity.Dependent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DependentResponse {
    String message;
    boolean result;
    Dependent dependent;
}
