package org.jtc.common.util.guid;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * $LastChangedBy: cgoina $:
 * $LastChangedDate: 2009-12-11 09:12:57 -0800 (Fri, 11 Dec 2009) $:
 * $Revision: 30273 $:
 * $HeadURL: http://svn.jcvi.org/SE/LIMS/branches/LIMSMigrationToMySQL/common/utilities/main/src/java/org/jtc/common/util/guid/GuidBlock.java $:
 * <p/>
 * Description:
 */
public class GuidBlock {

    public static final String DEFAULT_GUID_HTTP_PREFIX = "http://guid.jtc.jcvsf.org:8080/guid/GuidClientServer?Request=GET&Size=";
    public static final String DEFAULT_GUID_NAMESPACE = "GUID_SERVLET";

    public GuidBlock() {
    }

    public long getGuidBlock(long size) throws Exception {
        return getGuidBlock(DEFAULT_GUID_NAMESPACE, DEFAULT_GUID_HTTP_PREFIX, size);
    }

    public long getGuidBlock(String namespace, long size) throws Exception {
        return getGuidBlock(namespace, DEFAULT_GUID_HTTP_PREFIX, size);
    }

    public long getGuidBlock(String namespace, String httpPrefix, long size)
            throws Exception {
        if (namespace == null || namespace.trim().length() == 0) {
            namespace = DEFAULT_GUID_NAMESPACE;
        }
        String httpRequest = httpPrefix + size + "&Namespace=" + namespace;
        long guidStart = 0;
        try {
            URL url = new URL(httpRequest);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            // Expecting return of form:
            //   <html><title>SUCCESS</title><body>
            //   <h2>Guid Start:</h2>
            //   1088028002087
            //   </body></html>
            str = in.readLine();
            if (str == null) {
                throw new Exception("Guid Http Parsing error");
            }
            if (str.matches(".*SUCCESS.*")) {
                str = in.readLine();
                if (str.matches(".*Start:.*")) {
                    str = in.readLine();
                    guidStart = Long.parseLong(str);
                }
            }
            in.close();
            if (guidStart <= 0) {
                throw new Exception("Invalid GUID start value " + guidStart);
            }
        } catch (MalformedURLException e) {
            throw new Exception("Exception during getGUIDBlock" + e, e);
        } catch (IOException e) {
            throw new Exception("Exception during getGUIDBlock" + e, e);
        } catch (Exception e) {
            throw new Exception("Exception during getGUIDBlock" + e, e);
        }
        return guidStart;
    }
}
