package org.jtc.app_db_interface.guid.api.intf;

import java.util.Date;

public interface GUIDBlock {
    String getComment();

    void setComment(String var1);

    Date getDate();

    void setDate(Date var1);

    long getFirstGUID();

    void setFirstGUID(long var1);

    long getLastGUID();

    void setLastGUID(long var1);

    String getNamespace();

    void setNamespace(String var1);

    long getSize();

    void setSize(long var1);
}
