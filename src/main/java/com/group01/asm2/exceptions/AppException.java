package com.group01.asm2.exceptions;

public class AppException extends RuntimeException {
    public enum Type {
        VALIDATION,
        AUTHENTICATION,
        AUTHORIZATION,
        NOT_FOUND,
        CONFLICT,
        DATABASE,
        UNKNOWN
    }

    private final Type type;

    public AppException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public static AppException validation(String message) {
        return new AppException(Type.VALIDATION, message);
    }

    public static AppException authentication(String message) {
        return new AppException(Type.AUTHENTICATION, message);
    }

    public static AppException authorization(String message) {
        return new AppException(Type.AUTHORIZATION, message);
    }

    public static AppException notFound(String message) {
        return new AppException(Type.NOT_FOUND, message);
    }

    public static AppException conflict(String message) {
        return new AppException(Type.CONFLICT, message);
    }

    public static AppException database(String message) {
        return new AppException(Type.DATABASE, message);
    }

    public static AppException unknown(String message) {
        return new AppException(Type.UNKNOWN, message);
    }

    public Type getType() {
        return type;
    }
}