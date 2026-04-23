package com.group01.asm2.configs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileUploadConfig {
    private FileUploadConfig(){}
    public static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    public static final int MAX_FILES = 10;
    public static final Path UPLOAD_DIR = Paths.get("uploads");
    public static final List<String> ALLOWED_IMAGE_TYPES = List.of("jpg","jpeg","png","webp");

    public static String getExtension(
        String fileName
    ){
        int dotIndex = fileName.lastIndexOf(".");

        if(dotIndex == -1){
            return "";
        }

        return fileName
                .substring(dotIndex + 1)
                .toLowerCase();
    }

    public static boolean isAllowedImageType(
        String extension
    ){
        return ALLOWED_IMAGE_TYPES.contains(
            extension.toLowerCase()
        );
    }

}