package com.fuel50.devdb.model;

import java.util.Map;
import java.util.HashMap;

public class DatabaseSpec {
    private int version;
    private Defaults defaults;
    private Map<String, TableSpec> tables = new HashMap<>();

    // Getters and setters
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Defaults getDefaults() {
        return defaults;
    }

    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
    }

    public Map<String, TableSpec> getTables() {
        return tables;
    }

    public void setTables(Map<String, TableSpec> tables) {
        this.tables = tables;
    }

    public static class Defaults {
        private SubsetDefaults subset;
        private MaskingDefaults masking;

        public SubsetDefaults getSubset() {
            return subset;
        }

        public void setSubset(SubsetDefaults subset) {
            this.subset = subset;
        }

        public MaskingDefaults getMasking() {
            return masking;
        }

        public void setMasking(MaskingDefaults masking) {
            this.masking = masking;
        }
    }

    public static class SubsetDefaults {
        private String strategy;
        private Integer maxRows;
        private Integer timeWindowDays;

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public Integer getMaxRows() {
            return maxRows;
        }

        public void setMaxRows(Integer maxRows) {
            this.maxRows = maxRows;
        }

        public Integer getTimeWindowDays() {
            return timeWindowDays;
        }

        public void setTimeWindowDays(Integer timeWindowDays) {
            this.timeWindowDays = timeWindowDays;
        }
    }

    public static class MaskingDefaults {
        private String deterministicSalt;
        private String emailDomain;
        private boolean preserveLength;

        public String getDeterministicSalt() {
            return deterministicSalt;
        }

        public void setDeterministicSalt(String deterministicSalt) {
            this.deterministicSalt = deterministicSalt;
        }

        public String getEmailDomain() {
            return emailDomain;
        }

        public void setEmailDomain(String emailDomain) {
            this.emailDomain = emailDomain;
        }

        public boolean isPreserveLength() {
            return preserveLength;
        }

        public void setPreserveLength(boolean preserveLength) {
            this.preserveLength = preserveLength;
        }
    }
}