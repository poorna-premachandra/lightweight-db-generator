# DevDB - Lightweight Database Generator

DevDB is a tool designed to create lightweight development databases by sampling and masking production data. It helps developers work with realistic but safe data during development and testing.

## Key Features

- Samples data from production databases using configurable strategies
- Masks sensitive information like PII and secrets
- Preserves referential integrity through foreign key relationships
- Configurable via an YAML specification file
- Deterministic masking for consistent development environments
- Support for various data types and masking strategies

## Overview

The tool works by:

1. Reading a YAML specification file that defines tables, columns and masking rules
2. Connecting to source and target databases
3. Sampling a subset of records based on configured strategies
4. Applying masking and synthesis rules to sensitive data
5. Generating SQL statements to recreate the sampled dataset
6. Restoring the masked data to a development database

This allows developers to work with manageable subsets of production data while protecting sensitive information.

## Running Instructions

1. Initialize the services:

   ```
   docker compose up --build
   ```

2. Execute into the fuel50-local-db-generator container:

   ```
   docker exec -it [CONTAINER_ID] /bin/bash
   ```

3. Perform a dry run (optional):

   ```
   java -jar devdb-cli-1.0.0.jar generate -d
   ```

4. Generate the lightweight data script:

   ```
   java -jar devdb-cli-1.0.0.jar generate
   ```

5. Restore the data into the local database:

   ```
   java -jar devdb-cli-1.0.0.jar restore
   ```

6. Log into the local database to verify data population:
   ```
   mysql -h local-mysql -u root -p
   ```
