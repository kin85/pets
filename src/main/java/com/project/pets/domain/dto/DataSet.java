package com.project.pets.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DataSet<T> {

    private List<T> data;
    private long recordsTotal;
    private long recordsFiltered;

    public static <T> DataSet<T> of(List<T> data, long recordsTotal, long recordsFiltered) {
        return new DataSet<>(data, recordsTotal, recordsFiltered);
    }
}

