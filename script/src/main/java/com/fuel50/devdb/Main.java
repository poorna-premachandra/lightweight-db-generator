package com.fuel50.devdb;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import com.fuel50.devdb.cli.GeneratorCommand;
import com.fuel50.devdb.cli.RestoreCommand;

@Command(name = "devdb", description = "DevDB - Lightweight Database Generator", mixinStandardHelpOptions = true, version = "1.0.0", subcommands = {
        GeneratorCommand.class,
        RestoreCommand.class
})
public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
