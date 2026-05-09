package com.group01.asm2.security;

public final class SecurityAction {
    private SecurityAction() {
    }

    public static final String CREATE = "create";
    public static final String READ = "read";
    public static final String READ_OWN = "read_own";
    public static final String READ_ANY = "read_any";
    public static final String UPDATE = "update";
    public static final String UPDATE_OWN = "update_own";
    public static final String DELETE = "delete";
    public static final String DELETE_OWN = "delete_own";
    public static final String APPROVE = "approve";
    public static final String MODERATE = "moderate";
    public static final String PROCESS = "process";
    public static final String EXPORT = "export";
}