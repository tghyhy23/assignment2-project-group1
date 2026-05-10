package com.group01.asm2.db;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface SqlBinder {
    void bind(PreparedStatement ps) throws Exception;
}