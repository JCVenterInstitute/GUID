package org.jtc.app_db_interface.guid.api.dto;

import org.jtc.app_db_interface.guid.api.intf.GUIDNamespace;

import java.io.Serializable;
import java.util.Date;

public class GUIDNamespaceDTO implements GUIDNamespace, Serializable {
    private String comment;
    private Date date;
    private String namespace;
    private long id;

    public GUIDNamespaceDTO(long id, String namespace, Date date, String comment) {
        this.id = id;
        this.comment = comment;
        this.date = date;
        this.namespace = namespace;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public long getId() {
        return this.id;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("--GUIDNamespace info--\n");
        buffer.append("Id:          " + this.namespace + "\n");
        buffer.append("Namespace:   " + this.namespace + "\n");
        buffer.append("Comment:     " + this.comment + "\n");
        buffer.append("Date:        " + this.date + "\n");
        return buffer.toString();
    }
}

