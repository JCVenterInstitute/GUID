package org.jtc.app_db_interface.guid.api.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

public interface GuidClientHome extends EJBHome {
    GuidClient create() throws CreateException, RemoteException;
}
