package org.jtc.app_db_interface.guid.api.ejb;

import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;

import java.rmi.RemoteException;

public interface GuidClient {
    long getGUIDBlock(long var1, String var3) throws RemoteException, SystemErrorException, ApplicationErrorException;
}
