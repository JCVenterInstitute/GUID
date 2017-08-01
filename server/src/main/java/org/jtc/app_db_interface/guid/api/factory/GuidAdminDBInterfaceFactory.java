package org.jtc.app_db_interface.guid.api.factory;

import org.apache.log4j.Logger;
import org.jtc.app_db_interface.guid.api.ejb.GuidAdmin;
import org.jtc.app_db_interface.guid.api.ejb.GuidAdminHome;
import org.jtc.app_db_interface.guid.api.intf.CachedBlockInfo;
import org.jtc.app_db_interface.guid.api.intf.GUIDBlock;
import org.jtc.app_db_interface.guid.api.intf.GUIDNamespace;
import org.jtc.common.api.factory.DBInterfaceFactory;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;
import org.jtc.common.util.property.PropertyHelper;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class GuidAdminDBInterfaceFactory extends DBInterfaceFactory {
    protected static final String DEFAULT_PROPERTIES_PATH_PREFIX = "GuidDBInterface";
    public static final String DEFAULT_PROVIDER_URL_PROP = "GuidDBInterface.PROVIDER_URL";
    public static final String EJB_NAME_PROP = "GuidDBInterface.GUID_ADMIN_EJB_NAME";
    private static HashMap instanceMap = new HashMap();
    private static Properties defaultProperties = null;
    private static Logger logger = Logger.getLogger(GuidAdminDBInterfaceFactory.class);
    private String guidAdminName = null;

    private GuidAdminDBInterfaceFactory(Properties properties, String urlPropertyName) throws ApplicationErrorException {
        super(properties, urlPropertyName);
        this.guidAdminName = this.getProperty("GuidDBInterface.GUID_ADMIN_EJB_NAME");
    }

    public static GuidAdminDBInterfaceFactory getInstance() throws ApplicationErrorException {
        return getInstance(defaultProperties, "GuidDBInterface.PROVIDER_URL");
    }

    public static GuidAdminDBInterfaceFactory getInstance(Properties properties, String urlPropertyName) throws ApplicationErrorException {
        DBInterfaceKey key = new DBInterfaceKey(properties, urlPropertyName);
        GuidAdminDBInterfaceFactory instance = (GuidAdminDBInterfaceFactory)instanceMap.get(key);
        if (instance == null) {
            instance = new GuidAdminDBInterfaceFactory(properties, urlPropertyName);
            instanceMap.put(key, instance);
        }

        return instance;
    }

    private GuidAdmin getGuidAdmin() throws SystemErrorException {
        GuidAdmin guidAdmin = null;
        GuidAdminHome guidAdminHome = (GuidAdminHome)this.getCachedHome(this.guidAdminName);
        int tries = 1;

        while(guidAdmin == null && tries <= 3) {
            try {
                if (guidAdminHome == null) {
                    guidAdminHome = (GuidAdminHome)this.getEJBHome(this.guidAdminName, GuidAdminHome.class);
                }

                if (guidAdminHome != null) {
                    this.addHomeToCache(this.guidAdminName, guidAdminHome);
                }

                guidAdmin = (GuidAdmin)narrow(guidAdminHome.create(), GuidAdmin.class);
            } catch (Exception var5) {
                ++tries;
                guidAdminHome = null;
                this.removeHomeFromCache(this.guidAdminName);
                if (tries == 3) {
                    throw new SystemErrorException("Exception getting Info EJB", var5);
                }
            }
        }

        return guidAdmin;
    }

    public long getGUIDBlock(String namespace, long size, String comment) throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            long blockStart = guidAdmin.getGUIDBlock(namespace, size, comment);
            return blockStart;
        } catch (RemoteException var8) {
            throw new SystemErrorException("Remote Exception in getGUIDBlock() " + var8, var8);
        }
    }

    public GUIDBlock getGUIDAllocationInfoByGUID(long guid) throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            GUIDBlock guidBlock = guidAdmin.getGUIDAllocationInfoByGUID(guid);
            return guidBlock;
        } catch (RemoteException var5) {
            throw new SystemErrorException("Remote Exception in getGUIDBlock() " + var5, var5);
        }
    }

    public GUIDBlock[] getGUIDAllocationInfoByNamespace(String namespace) throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            GUIDBlock[] guidBlockList = guidAdmin.getGUIDAllocationInfoByNamespace(namespace);
            return guidBlockList;
        } catch (RemoteException var4) {
            throw new SystemErrorException("Remote Exception in getGUIDAllocationInfoByNamespace() " + var4, var4);
        }
    }

    public GUIDBlock[] getGUIDAllocationInfoAll() throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            GUIDBlock[] guidBlockList = guidAdmin.getGUIDAllocationInfoAll();
            return guidBlockList;
        } catch (RemoteException var3) {
            throw new SystemErrorException("Remote Exception in getGUIDAllocationInfoAll() " + var3, var3);
        }
    }

    public List<GUIDNamespace> getGUIDNamespaceInfoAll() throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            List<GUIDNamespace> guidNamespaceList = guidAdmin.getGUIDNamespaceInfo();
            return guidNamespaceList;
        } catch (RemoteException var3) {
            throw new SystemErrorException("Remote Exception in getGUIDNamespaceInfo() " + var3, var3);
        }
    }

    public GUIDNamespace getGUIDNamespaceInfo(String namespace) throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            GUIDNamespace guidNamespace = guidAdmin.getGUIDNamespaceInfo(namespace);
            return guidNamespace;
        } catch (RemoteException var4) {
            throw new SystemErrorException("Remote Exception in getGUIDNamespace(namespace) " + var4, var4);
        }
    }

    public String ping(String message) throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            String reply = guidAdmin.ping(message);
            if (!message.equals(reply)) {
                throw new SystemErrorException("ping() error: reply " + reply + " does not match message " + message);
            } else {
                return reply;
            }
        } catch (RemoteException var4) {
            throw new SystemErrorException("Remote Exception in ping() " + var4, var4);
        }
    }

    public String createGUIDNamespace(String namespace, String comment) throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            String message = guidAdmin.createGUIDNamespace(namespace, comment);
            return message;
        } catch (RemoteException var5) {
            throw new SystemErrorException("Remote Exception in createGUIDNamespace() " + var5, var5);
        }
    }

    public static String getEjbName() {
        return defaultProperties.getProperty("GuidDBInterface.GUID_ADMIN_EJB_NAME", (String)null);
    }

    public void refreshCachedNamespaces() throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            guidAdmin.refreshCachedNamespaces();
        } catch (RemoteException var3) {
            throw new SystemErrorException("Remote Exception in refreshCachedNamespaces() " + var3, var3);
        }
    }

    public Set<CachedBlockInfo> getCachedBlockInfo() throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            return guidAdmin.getCachedBlockInfo();
        } catch (RemoteException var3) {
            throw new SystemErrorException("Remote Exception in getCachedBlockInfo() " + var3, var3);
        }
    }

    public CachedBlockInfo getCachedBlockInfo(String namespace) throws SystemErrorException, ApplicationErrorException {
        GuidAdmin guidAdmin = this.getGuidAdmin();

        try {
            return guidAdmin.getCachedBlockInfo(namespace);
        } catch (RemoteException var4) {
            throw new SystemErrorException("Remote Exception in getCachedBlockInfo(namespace) " + var4, var4);
        }
    }

    static {
        try {
            defaultProperties = PropertyHelper.getHostnameProperties("GuidDBInterface");
        } catch (Exception var1) {
            logger.debug("Exception: " + var1);
            var1.printStackTrace();
        }

    }
}

