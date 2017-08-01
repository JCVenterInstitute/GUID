package org.jtc.common.api.factory;

import org.apache.log4j.Logger;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.ejb.EJBHome;
import java.util.Properties;
import java.util.HashMap;

/**
 * $LastChangedBy: cgoina $:
 * $LastChangedDate: 2010-03-12 11:47:23 -0500 (Fri, 12 Mar 2010) $:
 * $Revision: 30631 $:
 * $HeadURL: http://svn.jcvi.org/SE/LIMS/trunk/common/ejb/main/src/java/org/jtc/common/api/factory/DBInterfaceFactory.java $:
 * <p/>
 * @deprecated  use org.jtc.common.ejb.api.factory.EJBFactory instead
 *
 */
abstract public class DBInterfaceFactory {

    private static Logger logger = Logger.getLogger(DBInterfaceFactory.class);
    protected static final int MAX_RETRIES = 3;
    protected static final int ONE_HOUR = 1000 * 60 * 60;
    protected Context initialContext = null;
    protected Properties properties = null;
    protected HashMap homeInterfaceMap = null;

    protected static class DBInterfaceKey {
        private Properties properties;
        private String urlPropertyName;

        public DBInterfaceKey(Properties properties, String urlPropertyName) {
            this.properties = properties;
            this.urlPropertyName = urlPropertyName;
        }

        public Properties getProperties() {
            return properties;
        }

        public String getUrlNameProp() {
            return urlPropertyName;
        }

        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (properties == null ? 0 : properties.hashCode());
            hash = 31 * hash + (urlPropertyName == null ? 0 : urlPropertyName.hashCode());
            return hash;
        }

        public boolean equals(Object o) {
            if ( o instanceof DBInterfaceKey ) {
                DBInterfaceKey key = (DBInterfaceKey) o;
                if (key.getProperties().equals(properties) &&
                    urlPropertyName.equals(urlPropertyName)) {
                    return true;
                }
            }
            return false;
        }
    }

    protected DBInterfaceFactory(Properties properties, String urlNameProperty) throws ApplicationErrorException {
        try {
            if (properties==null)
                throw new Exception("properties argument to constructor must be non-null");
            this.properties = properties;
            String providerUrl = properties.getProperty(urlNameProperty);
            if (providerUrl==null)
                throw new Exception("providerUrl value not found in properties file");
            initialContext = getRemoteInitialContext(providerUrl);
            homeInterfaceMap = new HashMap();
            if (initialContext == null) throw new Exception("Received NULL initialContext");
        } catch (Exception ex) {
            throw new ApplicationErrorException("Exception in DBInterfaceFactory constructor" + ex, ex);
        }
    }

    private Context getRemoteInitialContext(String providerUrl) throws SystemErrorException {
        InitialContext context = null;
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        properties.put(Context.PROVIDER_URL, providerUrl);
        for (int i=0;i<DBInterfaceFactory.MAX_RETRIES;i++) {
            try {
                context = new InitialContext(properties);
                if (context != null)
                {
                    return context;
                }
            } catch (Exception ex) {
                DBInterfaceFactory.logger.info("Failed to obtain InitialContext: " + ex, ex);
            }
        }
        throw new SystemErrorException ("Could not obtain InitialContext");
    }

    public Context getJNDIContext() {
        return initialContext;
    }

    public String getProperty(String propertyString) throws ApplicationErrorException {
        String propertyValue = properties.getProperty(propertyString);
        if (propertyValue == null) {
            throw new ApplicationErrorException("Property " + propertyString + " not found");
        }
        return propertyValue;
    }

    protected static Object narrow(Object ref, Class c) {
        return PortableRemoteObject.narrow(ref, c);
    }

    private Object lookupEJBHome(String ejbName, Class ejbClass) throws NamingException {
        Object home = initialContext.lookup(ejbName);
        return DBInterfaceFactory.narrow(home, ejbClass);
    }

    protected Object getEJBHome(String homeName, Class homeClass) {
        Object homeObject = null;
        int tries = 1;
        while(homeObject==null && tries<=DBInterfaceFactory.MAX_RETRIES) {
            try {
                homeObject = lookupEJBHome(homeName, homeClass);
            }
            catch (Exception ex) {
                tries++;
                if (tries==DBInterfaceFactory.MAX_RETRIES) {
                    homeObject = null;
                    DBInterfaceFactory.logger.error("Cannot get reference to EJB " + homeName + " " + ex, ex);
                }
            }
        }
        return homeObject;
    }
    protected EJBHome getCachedHome(String homeKey) {
        HomeInterfaceCacheNode cacheNode=(HomeInterfaceCacheNode)homeInterfaceMap.get(homeKey);
        if (cacheNode==null) return null;
        if (cacheNode.getRetrievalTime()+DBInterfaceFactory.ONE_HOUR <System.currentTimeMillis()){
            homeInterfaceMap.remove(homeKey);
            return null;
        }
        return cacheNode.getHome();
    }

    protected void addHomeToCache(String homeKey,EJBHome home) {
        HomeInterfaceCacheNode cacheNode=new HomeInterfaceCacheNode(home,System.currentTimeMillis());
        homeInterfaceMap.put(homeKey,cacheNode);
    }

    protected void removeHomeFromCache(String homeKey){
        homeInterfaceMap.remove(homeKey);
    }
}
