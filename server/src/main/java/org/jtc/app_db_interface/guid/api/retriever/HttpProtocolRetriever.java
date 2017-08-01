package org.jtc.app_db_interface.guid.api.retriever;

import org.apache.log4j.Logger;
import org.jtc.common.util.property.PropertyHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

public class HttpProtocolRetriever {
    private static final String DEFAULT_PROPERTIES_PATH_PREFIX = "GuidDBInterface";
    private static final String HTTP_SUFFIX_PROP = "GuidDBInterface.Servlet.HTTP_SUFFIX";
    private static final String HTTP_SUFFIX_DEFAULT = "/guid/GuidClientServer?Request=GET&Size=";
    private String httpSuffix;

    public HttpProtocolRetriever() {
        Logger logger = Logger.getLogger(HttpProtocolRetriever.class);
        this.httpSuffix = "/guid/GuidClientServer?Request=GET&Size=";

        try {
            Properties p = PropertyHelper.getHostnameProperties("GuidDBInterface");
            this.httpSuffix = p.getProperty("GuidDBInterface.Servlet.HTTP_SUFFIX", "/guid/GuidClientServer?Request=GET&Size=");
        } catch (Exception var3) {
            logger.warn("Failed to obtain guid url suffix due to the following exception; defaulting suffix to " + this.httpSuffix, var3);
        }

    }

    public long getGUIDBlock(String serverNameAndPort, long size, String namespace) throws Exception {
        String httpRequest = "http://" + serverNameAndPort + this.httpSuffix + size;
        if (namespace != null) {
            httpRequest = httpRequest + "&Namespace=" + namespace;
        }

        long guidStart = 0L;

        try {
            URL url = new URL(httpRequest);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = in.readLine();
            if (str == null) {
                throw new Exception("Parsing error");
            } else {
                if (str.matches(".*SUCCESS.*")) {
                    str = in.readLine();
                    if (str.matches(".*Start:.*")) {
                        str = in.readLine();
                        guidStart = Long.parseLong(str);
                    }
                }

                in.close();
                if (guidStart <= 0L) {
                    throw new Exception("Invalid GUID start value " + guidStart);
                } else {
                    return guidStart;
                }
            }
        } catch (Exception var11) {
            throw new Exception("Exception during getGUIDBlock" + var11, var11);
        }
    }
}

