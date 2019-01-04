package org.jtc.common.ejb.api.factory;

import org.apache.log4j.Logger;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;
import org.jtc.common.util.property.PropertyHelper;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public abstract class EJBFactory {
    protected static final int MAX_RETRIES = 3;
    protected static final int ONE_HOUR = 3600000;
    protected Properties properties;
    protected Context initialContext;
    protected Logger logger;
    private HashMap homeInterfaceMap;

    protected EJBFactory(String propertiesFileName) throws Exception {
        this(PropertyHelper.getHostnameProperties(propertiesFileName));
    }

    protected EJBFactory(Properties properties) throws SystemErrorException, ApplicationErrorException {
        this(properties, (String)null);
    }

    protected EJBFactory(Properties properties, String providerUrlPropertyName) throws SystemErrorException, ApplicationErrorException {
        this.properties = new Properties();
        this.initialContext = null;
        this.logger = Logger.getLogger(this.getClass());
        this.homeInterfaceMap = new HashMap();

        try {
            this.properties = properties;
            String systemProviderUrl;
            if (this.logger.isDebugEnabled()) {
                Enumeration em = properties.keys();

                while(em.hasMoreElements()) {
                    systemProviderUrl = (String)em.nextElement();
                    this.logger.debug("loaded property: " + systemProviderUrl + " = " + properties.getProperty(systemProviderUrl));
                }
            }

            if (providerUrlPropertyName == null) {
                providerUrlPropertyName = this.getProviderURLProperyName();
            }

            String providerUrl = properties.getProperty(providerUrlPropertyName);
            systemProviderUrl = System.getProperty(this.getProviderURLProperyName());
            if (systemProviderUrl != null && systemProviderUrl.trim().length() > 0) {
                providerUrl = systemProviderUrl.trim();
            }

            if (providerUrl == null) {
                throw new ApplicationErrorException("Could not obtain PROVIDER_URL_PROP");
            } else {
                this.initialContext = this.getRemoteInitialContext(providerUrl);
                if (this.initialContext == null) {
                    throw new ApplicationErrorException("Received NULL initialContext");
                }
            }
        } catch (SystemErrorException var5) {
            throw var5;
        } catch (ApplicationErrorException var6) {
            throw var6;
        }
    }

    public Context getJNDIContext() {
        return this.initialContext;
    }

    public String getProperty(String propertyString) throws ApplicationErrorException {
        String propertyValue = this.properties.getProperty(propertyString);
        if (propertyValue == null) {
            throw new ApplicationErrorException("Property " + propertyString + " not found");
        } else {
            return propertyValue;
        }
    }

    public String getProperty(String propertyString, String defaultValue) throws ApplicationErrorException {
        String propertyValue = this.properties.getProperty(propertyString, defaultValue);
        return propertyValue;
    }

    protected abstract String getProviderURLProperyName();

    protected Object getEJBHome(String homeName, Class homeClass) {
        Object homeObject = null;
        int tries = 1;

        while(homeObject == null && tries <= 3) {
            try {
                homeObject = this.lookupEJBHome(homeName, homeClass);
            } catch (Exception var6) {
                ++tries;
                if (tries == 3) {
                    homeObject = null;
                    this.logger.error("Cannot get reference to EJB " + homeName + " " + var6, var6);
                }
            }
        }

        return homeObject;
    }

    protected Object getEJBInstance(String ejbName, Class ejbHomeClass, Class ejbObjectClass) throws SystemErrorException {
        this.logger.debug("Entering getEJBInstance " + ejbName + " of " + ejbHomeClass.getName());
        Object ejbInstance = null;

        try {
            EJBHome ejbHome = this.getCachedHome(ejbName);
            int tries = 1;

            while(ejbInstance == null && tries <= 3) {
                try {
                    if (ejbHome == null) {
                        ejbHome = (EJBHome)this.getEJBHome(ejbName, ejbHomeClass);
                    }

                    if (ejbHome != null) {
                        this.addHomeToCache(ejbName, ejbHome);
                        ejbInstance = this.narrow(this.createEJBInstance(ejbHome), ejbObjectClass);
                    }
                } catch (Exception var11) {
                    ++tries;
                    ejbHome = null;
                    this.removeHomeFromCache(ejbName);
                    if (tries == 3) {
                        throw new SystemErrorException("Exception getting an instance of " + ejbHomeClass.getName(), var11);
                    }
                }
            }
        } finally {
            this.logger.debug("Exiting getEJBInstance " + ejbName + " of " + ejbHomeClass.getName());
        }

        return ejbInstance;
    }

    protected abstract Object createEJBInstance(EJBHome var1) throws CreateException;

    protected Object narrow(Object ref, Class c) {
        return PortableRemoteObject.narrow(ref, c);
    }

    protected EJBHome getCachedHome(String homeKey) {
        this.logger.debug("'homeKey' as passed to 'getCachedHome': <" + homeKey + ">");
        this.logger.debug("homeInterfaceMap size: " + this.homeInterfaceMap.size());
        HomeInterfaceCacheNode cacheNode = (HomeInterfaceCacheNode)this.homeInterfaceMap.get(homeKey);
        if (cacheNode == null) {
            return null;
        } else if (cacheNode.getRetrievalTime() + 3600000L < System.currentTimeMillis()) {
            this.homeInterfaceMap.remove(homeKey);
            return null;
        } else {
            return cacheNode.getHome();
        }
    }

    protected void addHomeToCache(String homeKey, EJBHome home) {
        HomeInterfaceCacheNode cacheNode = new HomeInterfaceCacheNode(home, System.currentTimeMillis());
        this.homeInterfaceMap.put(homeKey, cacheNode);
        this.logger.debug("homeKey added to cache: " + homeKey);
    }

    protected void removeHomeFromCache(String homeKey) {
        this.homeInterfaceMap.remove(homeKey);
    }

    private Context getRemoteInitialContext(String providerUrl) throws SystemErrorException {
        this.properties.put("java.naming.factory.initial", "org.wildfly.naming.client.WildFlyInitialContextFactory");
        this.properties.put("java.naming.provider.url", providerUrl);
        this.properties.put("jboss.naming.client.ejb.context", true);

        for(int i = 0; i < 3; ++i) {
            try {
                InitialContext context = new InitialContext(this.properties);
                if (context != null) {
                    return context;
                }
            } catch (Exception var5) {
                this.logger.info("Failed to obtain InitialContext: " + var5, var5);
            }
        }

        throw new SystemErrorException("Could not obtain InitialContext");
    }

    private Object lookupEJBHome(String ejbName, Class ejbClass) throws NamingException {
        StringBuilder eb_builder = new StringBuilder();
        eb_builder.append("ejb:");
        if (ejbName.contains("JlimsAppDB")) {
            eb_builder.append("JLIMSDB-app/JLIMSDB-app-ejb");
        } else if (ejbName.contains("CommonDB")) {
            eb_builder.append("CommonDB-app/CommonDB-app-ejb");
        }

        eb_builder.append("//");
        eb_builder.append(ejbName);
        eb_builder.append("!");
        eb_builder.append(ejbClass.getName());
        String EJB_NAME = "ejb:JlimsNextGenWebApp/jlims_ejb_server//" + ejbName + "!" + ejbClass.getName();

        try {
            Object home = this.initialContext.lookup(EJB_NAME);
            return home;
        } catch (Exception var6) {
            this.logger.error("Failed to pickup EJB dependency: " + EJB_NAME);
            throw new RuntimeException(var6);
        }
    }
}
