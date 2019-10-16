package com.brillio.tms.token_service_mgmt.models;

import java.io.Serializable;

public class Applicant implements Serializable {

    private String name;

    public Applicant(String name) {
        this.name = name;
    }

    public Applicant() {
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{" +
                "name:" + name +
                '}';
    }
}
