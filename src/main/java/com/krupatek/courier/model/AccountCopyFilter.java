package com.krupatek.courier.model;

import java.util.Date;

public class AccountCopyFilter {
    private String docNoFilter = "";
    private String clientNameFilter = "";
    private Date dateFilter = null;

    public AccountCopyFilter(){

    }
    public String getDocNoFilter() {
        return docNoFilter;
    }
    public void setDocNoFilter(String docNoFilter) {
        this.docNoFilter = docNoFilter;
    }
    public String getClientNameFilter() {
        return clientNameFilter;
    }
    public void setClientNameFilter(String clientNameFilter) {
        this.clientNameFilter = clientNameFilter;
    }

    public Date getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(Date dateFilter) {
        this.dateFilter = dateFilter;
    }
}
