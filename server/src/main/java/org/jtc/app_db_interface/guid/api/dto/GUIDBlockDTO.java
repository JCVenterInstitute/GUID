package org.jtc.app_db_interface.guid.api.dto;

import org.jtc.app_db_interface.guid.api.intf.GUIDBlock;

import java.io.Serializable;
import java.util.Date;

public class GUIDBlockDTO implements GUIDBlock, Serializable {
    private String comment;
    private Date date;
    private long firstGUID;
    private long lastGUID;
    private String namespace;
    private long size;

    public GUIDBlockDTO(String comment, Date date, long firstGUID, long lastGUID, String namespace, long size) {
        this.comment = comment;
        this.date = date;
        this.firstGUID = firstGUID;
        this.lastGUID = lastGUID;
        this.namespace = namespace;
        this.size = size;
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

    public long getFirstGUID() {
        return this.firstGUID;
    }

    public void setFirstGUID(long firstGUID) {
        this.firstGUID = firstGUID;
    }

    public long getLastGUID() {
        return this.lastGUID;
    }

    public void setLastGUID(long lastGUID) {
        this.lastGUID = lastGUID;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("--GUIDBlock info--\n");
        buffer.append("Comment:     " + this.comment + "\n");
        buffer.append("Date:        " + this.date + "\n");
        buffer.append("FirstGUID:   " + this.firstGUID + "\n");
        buffer.append("LastGUID:    " + this.lastGUID + "\n");
        buffer.append("Namespace:   " + this.namespace + "\n");
        buffer.append("Size:        " + this.size + "\n");
        return buffer.toString();
    }
}

