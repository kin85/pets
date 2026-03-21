package com.project.pets.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class DatatablesResponse<T> {

    private int draw;
    private long recordsTotal;
    private long recordsFiltered;
    private List<T> data;

    public static <T> DatatablesResponse<T> build(DataSet<T> dataSet, DatatablesCriterias criterias) {
        DatatablesResponse<T> response = new DatatablesResponse<>();
        response.setDraw(criterias.getDraw());
        response.setRecordsTotal(dataSet.getRecordsTotal());
        response.setRecordsFiltered(dataSet.getRecordsFiltered());
        response.setData(dataSet.getData());
        return response;
    }
}

