package com.group01.asm2.db;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetMapper<T> {
    T map(ResultSet rs) throws Exception;
}