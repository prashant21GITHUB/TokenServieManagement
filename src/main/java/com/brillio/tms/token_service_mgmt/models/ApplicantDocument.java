package com.brillio.tms.token_service_mgmt.models;

import java.io.Serializable;

public class ApplicantDocument implements Serializable {

    private String applicantName;
    private Integer documentNum;

    public ApplicantDocument(String applicantName, Integer documentNum) {
        this.applicantName = applicantName;
        this.documentNum = documentNum;
    }

    public ApplicantDocument() {
    }

    public String getApplicantName() {
        return applicantName;
    }

    public Integer getDocumentNum() {
        return documentNum;
    }

    @Override
    public String toString() {
        return "{" +
                "applicantName:" + applicantName +
                ", documentNum:" + documentNum +
                '}';
    }
}
