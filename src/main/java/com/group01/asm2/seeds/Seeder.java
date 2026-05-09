package com.group01.asm2.seeds;

import java.sql.Connection;

public interface Seeder {
    void seed(Connection conn) throws Exception;
}