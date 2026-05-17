package com.group01.asm2.utils;

/**
 * @author Group 01
 */

import com.group01.asm2.exceptions.AppException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public final class CsvWriterUtil {

    private CsvWriterUtil() {
    }

    public static <T> Path writeCsv(
        Path outputPath,
        List<String> headers,
        List<T> rows,
        Function<T, List<String>> rowMapper
    ) {
        if (outputPath == null) {
            throw AppException.validation("Export path is required.");
        }

        if (headers == null || headers.isEmpty()) {
            throw AppException.validation("CSV headers are required.");
        }

        try {
            Path parent = outputPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                writer.write(toCsvLine(headers));
                writer.newLine();

                for (T row : rows) {
                    writer.write(toCsvLine(rowMapper.apply(row)));
                    writer.newLine();
                }
            }

            return outputPath;
        } catch (IOException exception) {
            throw AppException.validation("Failed to export CSV file: " + exception.getMessage());
        }
    }

    private static String toCsvLine(List<String> values) {
        return String.join(",", values.stream()
            .map(CsvWriterUtil::escapeCsvValue)
            .toList());
    }

    private static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");

        boolean needsQuotes = escaped.contains(",")
            || escaped.contains("\"")
            || escaped.contains("\n")
            || escaped.contains("\r");

        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}