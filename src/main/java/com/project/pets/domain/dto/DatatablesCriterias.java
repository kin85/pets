package com.project.pets.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DatatablesCriterias {

    private int draw;
    private int start;
    private int length;
    private String searchValue;
    private List<ColumnOrder> order = new ArrayList<>();
    private List<ColumnDef> columns = new ArrayList<>();

    @Data
    public static class ColumnOrder {
        private int column;
        private String dir;
    }

    @Data
    public static class ColumnDef {
        private String data;
        private String name;
        private boolean searchable;
        private boolean orderable;
        private SearchColumn search;
    }

    @Data
    public static class SearchColumn {
        private String value;
        private boolean regex;
    }
}

