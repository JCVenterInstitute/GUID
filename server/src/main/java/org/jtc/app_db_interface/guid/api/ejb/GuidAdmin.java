package org.jtc.app_db_interface.guid.api.ejb;

import org.jtc.app_db_interface.guid.api.intf.CachedBlockInfo;
import org.jtc.app_db_interface.guid.api.intf.GUIDBlock;
import org.jtc.app_db_interface.guid.api.intf.GUIDNamespace;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface GuidAdmin {
    long getGUIDBlock(String var1, long var2, String var4) throws RemoteException, SystemErrorException, ApplicationErrorException;

    GUIDBlock getGUIDAllocationInfoByGUID(long var1) throws RemoteException, SystemErrorException, ApplicationErrorException;

    GUIDBlock[] getGUIDAllocationInfoByNamespace(String var1) throws RemoteException, SystemErrorException, ApplicationErrorException;

    GUIDBlock[] getGUIDAllocationInfoAll() throws RemoteException, SystemErrorException, ApplicationErrorException;

    List<GUIDNamespace> getGUIDNamespaceInfo() throws RemoteException, SystemErrorException, ApplicationErrorException;

    GUIDNamespace getGUIDNamespaceInfo(String var1) throws RemoteException, SystemErrorException, ApplicationErrorException;

    String ping(String var1) throws RemoteException, SystemErrorException, ApplicationErrorException;

    String createGUIDNamespace(String var1, String var2) throws RemoteException, SystemErrorException, ApplicationErrorException;

    void refreshCachedNamespaces() throws RemoteException, SystemErrorException, ApplicationErrorException;

    Set<CachedBlockInfo> getCachedBlockInfo() throws RemoteException, SystemErrorException, ApplicationErrorException;

    CachedBlockInfo getCachedBlockInfo(String var1) throws RemoteException, SystemErrorException, ApplicationErrorException;
}
