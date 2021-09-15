package com.example.db;

import org.litepal.crud.LitePalSupport;

/**
 * Created by Admin
 * Date 2021/9/12
 */
public class TestDB extends LitePalSupport {

    private String dbStr;

    private String testStr;



    public String getDbStr() {
        return dbStr;
    }

    public void setDbStr(String dbStr) {
        this.dbStr = dbStr;
    }

    public String getTestStr() {
        return testStr;
    }

    public void setTestStr(String testStr) {
        this.testStr = testStr;
    }
}
