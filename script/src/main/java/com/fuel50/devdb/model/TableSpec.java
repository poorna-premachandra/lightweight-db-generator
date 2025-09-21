package com.fuel50.devdb.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class TableSpec {
    private String name;
    private SubsetSpec subset;
    private ForeignKeySpec fk;
    private int processingOrder;
    private PostLoadSpec postLoad;
    private Map<String, ColumnSpec> columns = new HashMap<>();

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubsetSpec getSubset() {
        return subset;
    }

    public void setSubset(SubsetSpec subset) {
        this.subset = subset;
    }

    public ForeignKeySpec getFk() {
        return fk;
    }

    public void setFk(ForeignKeySpec fk) {
        this.fk = fk;
    }

    public int getProcessingOrder() {
        return processingOrder;
    }

    public void setProcessingOrder(int processingOrder) {
        this.processingOrder = processingOrder;
    }

    public PostLoadSpec getPostLoad() {
        return postLoad;
    }

    public void setPostLoad(PostLoadSpec postLoad) {
        this.postLoad = postLoad;
    }

    public Map<String, ColumnSpec> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, ColumnSpec> columns) {
        this.columns = columns;
    }

    public static class SubsetSpec {
        private String strategy;
        private Integer timeWindowDays;
        private boolean root;
        private String orderBy;
        private Integer maxRows;

        // Getters and setters
        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public Integer getTimeWindowDays() {
            return timeWindowDays;
        }

        public void setTimeWindowDays(Integer timeWindowDays) {
            this.timeWindowDays = timeWindowDays;
        }

        public boolean isRoot() {
            return root;
        }

        public void setRoot(boolean root) {
            this.root = root;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public Integer getMaxRows() {
            return maxRows;
        }

        public void setMaxRows(Integer maxRows) {
            this.maxRows = maxRows;
        }
    }

    public static class ForeignKeySpec {
        private List<ForeignKeyReference> references;

        public List<ForeignKeyReference> getReferences() {
            return references;
        }

        public void setReferences(List<ForeignKeyReference> references) {
            this.references = references;
        }
    }

    public static class ForeignKeyReference {
        private String column;
        private String table;
        private String columnRef;

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getColumnRef() {
            return columnRef;
        }

        public void setColumnRef(String columnRef) {
            this.columnRef = columnRef;
        }
    }

    public static class PostLoadSpec {
        private Integer sequenceOffset;

        public Integer getSequenceOffset() {
            return sequenceOffset;
        }

        public void setSequenceOffset(Integer sequenceOffset) {
            this.sequenceOffset = sequenceOffset;
        }
    }
}