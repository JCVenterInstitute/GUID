package org.jtc.common.util.property;

import org.apache.log4j.Logger;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * $LastChangedBy: cgoina $:
 * $LastChangedDate: 2009-12-11 09:12:57 -0800 (Fri, 11 Dec 2009) $:
 * $Revision: 30273 $:
 * $HeadURL: http://svn.jcvi.org/SE/LIMS/branches/LIMSMigrationToMySQL/common/utilities/main/src/java/org/jtc/common/util/property/PropertyHelper.java $:
 * <p/>
 * Description: Utility class to assist in proaperty reading.  
 *              Will return the proper value or the default.
 *              Will also put out a log message if the logger is specified in the constructor.
 */
public class PropertyHelper {
    private static Logger logger = Logger.getLogger(PropertyHelper.class);
    private static String DEFAULT_HOSTNAME = "UNKNOWN";
    private static PropertyHelper instance = new PropertyHelper();

    public static PropertyHelper getInstance(){
        return instance;
    }

    public static void loadHostnameProperties(String propertyFileClasspathAddress,
                                              Properties p,
                                              String hostName) {
        String partialName;
        String searchString;
        InputStream inStream;
        searchString = propertyFileClasspathAddress + ".properties";
        logger.debug("Looking for " + searchString);
        inStream = PropertyHelper.class.getClassLoader().getResourceAsStream(searchString);
        if (inStream != null) {
            logger.info("Loading " + searchString);
            try {
                p.load(inStream);
            } catch (IOException ioEx) {
                logger.warn("Cannot load property file: " + searchString, ioEx);
            }
        }
        for (int i = 1; i < hostName.length() + 1; i++) {
            partialName = hostName.substring(0, i);
            searchString = propertyFileClasspathAddress + "-" + partialName + ".properties";
            logger.debug("Looking for " + searchString);
            inStream = PropertyHelper.class.getClassLoader().getResourceAsStream(searchString);
            if (inStream != null) {
                logger.info("Loading " + searchString);
                try {
                    p.load(inStream);
                } catch (IOException ioEx) {
                    logger.warn("Cannot load property file: " + searchString, ioEx);
                }
            }
        }
    }

    public static Properties getHostnameProperties(String propertyFileClasspathAddress) {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhEx) {
            logger.error("getHostName - UnknownHostException", uhEx);
        }
        if (hostName==null) {
            hostName = DEFAULT_HOSTNAME;
        }
        return getHostnameProperties(propertyFileClasspathAddress, hostName);
    }

    public static Properties getHostnameProperties(String propertyFileClasspathAddress, String hostName) {
        Properties p = new Properties();
        loadHostnameProperties(propertyFileClasspathAddress, p, hostName);
        return p;
    }

    private String hostName = DEFAULT_HOSTNAME;
    private Properties property = new Properties();

    private PropertyHelper() {
    }

    public String getProperty(String propertyName, String defaultValue) {
        String propertyValue = property.getProperty(propertyName);
        if (propertyValue == null) {
            logger.warn(propertyName + " not found.  Using Default: " + defaultValue);
            return defaultValue;
        } else {
            return propertyValue;
        }
    }

    public int getProperty(String propertyName, int defaultValue) {
        String propertyValue = property.getProperty(propertyName);
        if (propertyValue == null) {
            logger.warn(propertyName + " not found.  Using Default: " + defaultValue);
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(propertyValue);
            } catch (NumberFormatException ex) {
                logger.warn(propertyName + " is not an Integer.  Using Default: " + defaultValue,ex);
                return defaultValue;
            }
        }
    }

    public long getProperty(String propertyName, long defaultValue) {
        String propertyValue = property.getProperty(propertyName);
        if (propertyValue == null) {
            logger.warn(propertyName + " not found.  Using Default: " + defaultValue);
            return defaultValue;
        } else {
            try {
                return Long.parseLong(propertyValue);
            } catch (NumberFormatException ex) {
                logger.warn(propertyName + " is not a Long.  Using Default: " + defaultValue,ex);
                return defaultValue;
            }
        }
    }

    public boolean getProperty(String propertyName, boolean defaultValue) {
        String propertyValue = property.getProperty(propertyName);
        if (propertyValue == null) {
            logger.warn(propertyName + " not found.  Using Default: " + defaultValue);
            return defaultValue;
        } else {
            try {
                return Boolean.valueOf(propertyValue).booleanValue();
            } catch (Exception ex) {
                logger.warn(propertyName + " is not a Booleanr.  Using Default: " + defaultValue,ex);
                return defaultValue;
            }
        }
    }

    public File getProperty(String propertyName, File defaultValue) {
        String propertyValue = property.getProperty(propertyName);
        if (propertyValue == null) {
            logger.warn(propertyName + " not found.  Using Default: " + defaultValue);
            return defaultValue;
        } else {
            try {
                return new File(propertyValue);
            } catch (Exception ex) {
                logger.warn(propertyName + " is not a valid File.  Using Default: " + defaultValue,ex);
                return defaultValue;
            }
        }
    }

    public void loadProperties(String propertyFileClasspathAddress) {
        loadProperties(propertyFileClasspathAddress, true);
    }

    public void loadProperties(String propertyFileClasspathAddress, boolean searchHostNameExtensions) {
        loadProperties(propertyFileClasspathAddress, searchHostNameExtensions, null);
    }

    /**
     *
     * @param propertyFileClasspathAddress - specifies the name of the properties file relative to
     * the classpath, e.g. resource/<App>
     * @param searchHostNameExtensions - if true will overlay property files based on the hostname.
     * The behavior is such that it will try to load each file in progressive order to spell out
     * the hostname.  For example, if the hostname is pdavies-w1, and propertyFileClasspathAddress
     * is resource/JTrace, the files to be loaded are:
     *      resource/JTrace.properties
     *      resource/JTrace-p.properties
     *      resource/JTrace-pd.properties
     *      resource/JTrace-pda.properties
     *      etc.
     * @param overRideMap - a map whose key is a keyword substitution for final values as loaded.
     * For example, if the Map contains %SEQUENCER_MODEL% and 3730, if %SEQUENCER_MODEL% is found in
     * a value for any of the properties, it will be replaced by 3730.
     */
    public void loadProperties(String propertyFileClasspathAddress,
                               boolean searchHostNameExtensions,
                               Map overRideMap) {
        Properties p = new Properties();
        InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(propertyFileClasspathAddress + ".properties");
        if (inStream != null) {
            logger.info("Loading " + propertyFileClasspathAddress + ".properties");
            try {
                p.load(inStream);
            } catch (IOException ioEx) {
                logger.warn("Cannot load base property file: " + propertyFileClasspathAddress + ".properties",ioEx);
            }
        }
        hostName = getHostName();
        if (searchHostNameExtensions && !hostName.equals(DEFAULT_HOSTNAME)) {
            loadHostnameProperties(propertyFileClasspathAddress, p, hostName);
        }
        List overRideKeyList=null;
        if (overRideMap!=null) overRideKeyList=new ArrayList(overRideMap.keySet());
        Set propertyEntries = p.keySet();
        String key;
        String value;
        for (Iterator it = propertyEntries.iterator(); it.hasNext();) {
            key = (String) it.next();
            value = p.getProperty(key);
            if (overRideMap!=null){
                for (int i=0;i<overRideKeyList.size();i++){
                    value = value.replaceAll((String)overRideKeyList.get(i), (String)overRideMap.get(overRideKeyList.get(i)));
                }
                p.put(key, value);
            }
            logger.debug("Property: " + key + " : " + value);
        }
        load(p);
    }

    public void load(Properties property){
        Enumeration e = property.keys();
        while(e.hasMoreElements()){
            String key = (String) e.nextElement();
            this.property.setProperty(key,property.getProperty(key));
        }
    }

    public boolean hasProperties() {
        return property.size() > 0;
    }

    public Set<Object> keySet() {
        return property.keySet();
    }

    private String getHostName() {
        try {
            if (hostName.equals(DEFAULT_HOSTNAME)) {
                hostName = InetAddress.getLocalHost().getHostName();
            }
        } catch (UnknownHostException uhEx) {
            logger.error("getHostName - UnknownHostException", uhEx);
        }
        return hostName;
    }

}


