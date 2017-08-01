package org.jtc.app_db_interface.guid.api.intf;

import java.util.Date;

public interface GUIDNamespace {
    String getComment();

    void setComment(String var1);

    Date getDate();

    void setDate(Date var1);

    String getNamespace();

    void setNamespace(String var1);

    long getId();
}
