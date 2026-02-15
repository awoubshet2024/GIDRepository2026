package com.alem.GIA.model;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IPaginationParam<T> {
    private long recordCount;
    private T data;
}

