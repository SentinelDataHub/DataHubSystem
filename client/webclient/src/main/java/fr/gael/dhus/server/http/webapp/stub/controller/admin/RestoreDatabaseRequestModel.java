package fr.gael.dhus.server.http.webapp.stub.controller.admin;

import java.util.Date;

public class RestoreDatabaseRequestModel {

    private Long date;

    public Date getDate() {
        return new Date(this.date);
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
