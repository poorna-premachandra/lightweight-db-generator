package com.fuel50.devdb.config;

import com.fuel50.devdb.model.DatabaseSpec;
import com.fuel50.devdb.model.TableSpec;
import com.fuel50.devdb.model.ColumnSpec;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class DatabaseSpecLoader {

    public static DatabaseSpec load(File configFile) throws IOException {
        Yaml yaml = new Yaml();

        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            Map<String, Object> data = yaml.load(inputStream);
            return parseSpec(data);
        }
    }

    @SuppressWarnings("unchecked")
    private static DatabaseSpec parseSpec(Map<String, Object> data) {
        DatabaseSpec spec = new DatabaseSpec();

        // Parse version
        if (data.containsKey("version")) {
            spec.setVersion((Integer) data.get("version"));
        }

        // Parse defaults
        if (data.containsKey("defaults")) {
            spec.setDefaults(parseDefaults((Map<String, Object>) data.get("defaults")));
        }

        // Parse tables
        if (data.containsKey("tables")) {
            Map<String, Object> tablesData = (Map<String, Object>) data.get("tables");
            Map<String, TableSpec> tables = new java.util.HashMap<>();

            for (Map.Entry<String, Object> entry : tablesData.entrySet()) {
                String tableName = entry.getKey();
                TableSpec tableSpec = parseTableSpec(tableName, (Map<String, Object>) entry.getValue());
                tables.put(tableName, tableSpec);
            }

            spec.setTables(tables);
        }

        return spec;
    }

    @SuppressWarnings("unchecked")
    private static DatabaseSpec.Defaults parseDefaults(Map<String, Object> data) {
        DatabaseSpec.Defaults defaults = new DatabaseSpec.Defaults();

        if (data.containsKey("subset")) {
            defaults.setSubset(parseSubsetDefaults((Map<String, Object>) data.get("subset")));
        }

        if (data.containsKey("masking")) {
            defaults.setMasking(parseMaskingDefaults((Map<String, Object>) data.get("masking")));
        }

        return defaults;
    }

    private static DatabaseSpec.SubsetDefaults parseSubsetDefaults(Map<String, Object> data) {
        DatabaseSpec.SubsetDefaults subset = new DatabaseSpec.SubsetDefaults();

        if (data.containsKey("strategy")) {
            subset.setStrategy((String) data.get("strategy"));
        }
        if (data.containsKey("max_rows")) {
            subset.setMaxRows((Integer) data.get("max_rows"));
        }
        if (data.containsKey("time_window_days")) {
            subset.setTimeWindowDays((Integer) data.get("time_window_days"));
        }

        return subset;
    }

    private static DatabaseSpec.MaskingDefaults parseMaskingDefaults(Map<String, Object> data) {
        DatabaseSpec.MaskingDefaults masking = new DatabaseSpec.MaskingDefaults();

        if (data.containsKey("deterministic_salt")) {
            masking.setDeterministicSalt((String) data.get("deterministic_salt"));
        }
        if (data.containsKey("email_domain")) {
            masking.setEmailDomain((String) data.get("email_domain"));
        }
        if (data.containsKey("preserve_length")) {
            masking.setPreserveLength((Boolean) data.get("preserve_length"));
        }

        return masking;
    }

    @SuppressWarnings("unchecked")
    private static TableSpec parseTableSpec(String tableName, Map<String, Object> data) {
        TableSpec table = new TableSpec();
        table.setName(tableName);

        if (data.containsKey("subset")) {
            table.setSubset(parseSubsetSpec((Map<String, Object>) data.get("subset")));
        }

        if (data.containsKey("fk")) {
            table.setFk(parseForeignKeySpec((Map<String, Object>) data.get("fk")));
        }

        if (data.containsKey("processing_order")) {
            table.setProcessingOrder((Integer) data.get("processing_order"));
        }

        if (data.containsKey("post_load")) {
            table.setPostLoad(parsePostLoadSpec((Map<String, Object>) data.get("post_load")));
        }

        if (data.containsKey("columns")) {
            Map<String, Object> columnsData = (Map<String, Object>) data.get("columns");
            Map<String, ColumnSpec> columns = new java.util.HashMap<>();

            for (Map.Entry<String, Object> entry : columnsData.entrySet()) {
                String columnName = entry.getKey();
                ColumnSpec columnSpec = parseColumnSpec(columnName, (Map<String, Object>) entry.getValue());
                columns.put(columnName, columnSpec);
            }

            table.setColumns(columns);
        }

        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableSpec.SubsetSpec parseSubsetSpec(Map<String, Object> data) {
        TableSpec.SubsetSpec subset = new TableSpec.SubsetSpec();

        if (data.containsKey("strategy")) {
            subset.setStrategy((String) data.get("strategy"));
        }
        if (data.containsKey("time_window_days")) {
            subset.setTimeWindowDays((Integer) data.get("time_window_days"));
        }
        if (data.containsKey("root")) {
            subset.setRoot((Boolean) data.get("root"));
        }
        if (data.containsKey("order_by")) {
            subset.setOrderBy((String) data.get("order_by"));
        }
        if (data.containsKey("max_rows")) {
            subset.setMaxRows((Integer) data.get("max_rows"));
        }

        return subset;
    }

    @SuppressWarnings("unchecked")
    private static TableSpec.ForeignKeySpec parseForeignKeySpec(Map<String, Object> data) {
        TableSpec.ForeignKeySpec fk = new TableSpec.ForeignKeySpec();

        if (data.containsKey("references")) {
            List<Map<String, Object>> referencesData = (List<Map<String, Object>>) data.get("references");
            List<TableSpec.ForeignKeyReference> references = new ArrayList<>();

            for (Map<String, Object> refData : referencesData) {
                TableSpec.ForeignKeyReference ref = new TableSpec.ForeignKeyReference();

                if (refData.containsKey("column")) {
                    ref.setColumn((String) refData.get("column"));
                }
                if (refData.containsKey("table")) {
                    ref.setTable((String) refData.get("table"));
                }
                if (refData.containsKey("column_ref")) {
                    ref.setColumnRef((String) refData.get("column_ref"));
                }

                references.add(ref);
            }

            fk.setReferences(references);
        }

        return fk;
    }

    private static TableSpec.PostLoadSpec parsePostLoadSpec(Map<String, Object> data) {
        TableSpec.PostLoadSpec postLoad = new TableSpec.PostLoadSpec();

        if (data.containsKey("sequence_offset")) {
            postLoad.setSequenceOffset((Integer) data.get("sequence_offset"));
        }

        return postLoad;
    }

    @SuppressWarnings("unchecked")
    private static ColumnSpec parseColumnSpec(String columnName, Map<String, Object> data) {
        ColumnSpec column = new ColumnSpec();
        column.setName(columnName);

        if (data.containsKey("references")) {
            column.setReferences((String) data.get("references"));
        }

        if (data.containsKey("sensitivity")) {
            column.setSensitivity((String) data.get("sensitivity"));
        }
        if (data.containsKey("strategy")) {
            column.setStrategy((String) data.get("strategy"));
        }
        if (data.containsKey("primary_key")) {
            column.setPrimaryKey((Boolean) data.get("primary_key"));
        }

        if (data.containsKey("mask")) {
            column.setMask(parseMaskSpec((Map<String, Object>) data.get("mask")));
        }

        if (data.containsKey("synth")) {
            column.setSynth(parseSynthSpec((Map<String, Object>) data.get("synth")));
        }

        if (data.containsKey("unique")) {
            column.setUnique((Boolean) data.get("unique"));
        }

        return column;
    }

    @SuppressWarnings("unchecked")
    private static ColumnSpec.MaskSpec parseMaskSpec(Map<String, Object> data) {
        ColumnSpec.MaskSpec mask = new ColumnSpec.MaskSpec();

        if (data.containsKey("type")) {
            mask.setType((String) data.get("type"));
        }
        if (data.containsKey("deterministic_key")) {
            mask.setDeterministicKey((String) data.get("deterministic_key"));
        }
        if (data.containsKey("percent")) {
            mask.setPercent((Integer) data.get("percent"));
        }
        if (data.containsKey("preserve_domain")) {
            mask.setPreserveDomain((Boolean) data.get("preserve_domain"));
        }

        return mask;
    }

    @SuppressWarnings("unchecked")
    private static ColumnSpec.SynthSpec parseSynthSpec(Map<String, Object> data) {
        ColumnSpec.SynthSpec synth = new ColumnSpec.SynthSpec();

        if (data.containsKey("type")) {
            synth.setType((String) data.get("type"));
        }
        if (data.containsKey("deterministic_key")) {
            synth.setDeterministicKey((String) data.get("deterministic_key"));
        }
        if (data.containsKey("domain")) {
            synth.setDomain((String) data.get("domain"));
        }
        if (data.containsKey("format")) {
            synth.setFormat((String) data.get("format"));
        }

        return synth;
    }

}