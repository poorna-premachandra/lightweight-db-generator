package com.fuel50.devdb.model;

public class ColumnSpec {
    private String name;
    private String references;
    private String sensitivity;
    private String strategy;
    private boolean primaryKey;
    private MaskSpec mask;
    private SynthSpec synth;
    private boolean unique;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public String getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public MaskSpec getMask() {
        return mask;
    }

    public void setMask(MaskSpec mask) {
        this.mask = mask;
    }

    public SynthSpec getSynth() {
        return synth;
    }

    public void setSynth(SynthSpec synth) {
        this.synth = synth;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public static class MaskSpec {
        private String type;
        private String deterministicKey;
        private Integer percent;
        private boolean preserveDomain;

        // Getters and setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDeterministicKey() {
            return deterministicKey;
        }

        public void setDeterministicKey(String deterministicKey) {
            this.deterministicKey = deterministicKey;
        }

        public Integer getPercent() {
            return percent;
        }

        public void setPercent(Integer percent) {
            this.percent = percent;
        }

        public boolean isPreserveDomain() {
            return preserveDomain;
        }

        public void setPreserveDomain(boolean preserveDomain) {
            this.preserveDomain = preserveDomain;
        }
    }

    public static class SynthSpec {
        private String type;
        private String deterministicKey;
        private String domain;
        private String format;

        // Getters and setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDeterministicKey() {
            return deterministicKey;
        }

        public void setDeterministicKey(String deterministicKey) {
            this.deterministicKey = deterministicKey;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }
    }
}