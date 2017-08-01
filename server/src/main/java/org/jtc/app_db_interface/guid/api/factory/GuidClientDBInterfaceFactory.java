package org.jtc.app_db_interface.guid.api.factory;

import org.apache.log4j.Logger;
import org.jtc.app_db_interface.guid.api.ejb.GuidClient;
import org.jtc.app_db_interface.guid.api.ejb.GuidClientHome;
import org.jtc.common.api.factory.DBInterfaceFactory;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;
import org.jtc.common.util.property.PropertyHelper;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;

public class GuidClientDBInterfaceFactory extends DBInterfaceFactory {
    protected static final String DEFAULT_PROPERTIES_PATH_PREFIX = "GuidDBInterface";
    public static final String DEFAULT_PROVIDER_URL_PROP = "GuidDBInterface.PROVIDER_URL";
    public static final String DEFAULT_NAMESPACE_PROP = "GuidDBInterface.DEFAULT_NAMESPACE";
    private static final String EJB_NAME_PROP = "GuidDBInterface.GUID_CLIENT_EJB_NAME";
    private static HashMap instanceMap = new HashMap();
    private static Properties defaultProperties = null;
    private static Logger logger = Logger.getLogger(GuidClientDBInterfaceFactory.class);
    private String guidClientName = null;
    private String defaultNamespace = null;

    private GuidClientDBInterfaceFactory(Properties properties, String urlPropertyName) throws ApplicationErrorException {
        super(properties, urlPropertyName);
        this.guidClientName = this.getProperty("GuidDBInterface.GUID_CLIENT_EJB_NAME");
        this.defaultNamespace = this.getProperty("GuidDBInterface.DEFAULT_NAMESPACE");
    }

    public static GuidClientDBInterfaceFactory getInstance() throws ApplicationErrorException {
        return getInstance(defaultProperties, "GuidDBInterface.PROVIDER_URL");
    }

    public static GuidClientDBInterfaceFactory getInstance(Properties properties, String urlPropertyName) throws ApplicationErrorException {
        DBInterfaceKey key = new DBInterfaceKey(properties, urlPropertyName);
        GuidClientDBInterfaceFactory instance = (GuidClientDBInterfaceFactory)instanceMap.get(key);
        if (instance == null) {
            instance = new GuidClientDBInterfaceFactory(properties, urlPropertyName);
            instanceMap.put(key, instance);
        }

        return instance;
    }

    private GuidClient getGuidClient() throws SystemErrorException {
        GuidClient guidClient = null;
        GuidClientHome guidClientHome = (GuidClientHome)this.getCachedHome(this.guidClientName);
        int tries = 1;

        while(guidClient == null && tries <= 3) {
            try {
                if (guidClientHome == null) {
                    guidClientHome = (GuidClientHome)this.getEJBHome(this.guidClientName, GuidClientHome.class);
                }

                if (guidClientHome != null) {
                    this.addHomeToCache(this.guidClientName, guidClientHome);
                }

                guidClient = (GuidClient)narrow(guidClientHome.create(), GuidClient.class);
            } catch (Exception var5) {
                ++tries;
                guidClientHome = null;
                this.removeHomeFromCache(this.guidClientName);
                if (tries == 3) {
                    throw new SystemErrorException("Exception getting EJB " + this.guidClientName, var5);
                }
            }
        }

        return guidClient;
    }

    public long getGUIDBlock(long size, String namespace) throws SystemErrorException, ApplicationErrorException {
        if (namespace == null) {
            namespace = this.defaultNamespace;
        }

        GuidClient guidClient = this.getGuidClient();

        try {
            long blockStart = guidClient.getGUIDBlock(size, namespace);
            return blockStart;
        } catch (RemoteException var7) {
            throw new SystemErrorException("Remote Exception in getGUIDBlock() " + var7, var7);
        }
    }

    public long getGUIDBlock(long size) throws SystemErrorException, ApplicationErrorException {
        GuidClient guidClient = this.getGuidClient();

        try {
            long blockStart = guidClient.getGUIDBlock(size, this.defaultNamespace);
            return blockStart;
        } catch (RemoteException var6) {
            throw new SystemErrorException("Remote Exception in getGUIDBlock() " + var6, var6);
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

