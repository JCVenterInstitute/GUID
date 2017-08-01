package org.jtc.app_db_interface.guid.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jtc.app_db_interface.guid.api.factory.GuidAdminDBInterfaceFactory;
import org.jtc.app_db_interface.guid.api.factory.GuidClientDBInterfaceFactory;
import org.jtc.app_db_interface.guid.api.intf.CachedBlockInfo;
import org.jtc.app_db_interface.guid.api.intf.GUIDNamespace;
import org.jtc.common.util.property.PropertyHelper;

public class GuidServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(GuidServlet.class);
    private static final String METHOD_ATTRIBUTE_NAME = "Request";
    private static final String SIZE_ATTRIBUTE_NAME = "Size";
    private static final String NAMESPACE_ATTRIBUTE_NAME = "Namespace";
    private static final String GET_METHOD = "GET";
    private static final String STATUS_CHECK_METHOD = "STATUS_CHECK";
    private static final String GUID_CACHE_CHECK_METHOD = "CACHE_CHECK";
    private static final String DEFAULT_NAMESPACE_PROP = "GuidDBInterface.Servlet.DefaultNamespace";
    private static final String MAX_BLOCK_PROP = "GuidDBInterface.CACHE_UPDATE_BLOCKSIZE";
    private static final String DEFAULT_PROPERTY_PATH = "GuidDBInterface";
    private static final int STATUS_OK = 1;
    private static final int STATUS_ERROR = 0;
    private static int status;
    private static long maximumRequestSize = 0L;
    private static String defaultNamespace = null;

    public GuidServlet() {
    }

    public void init() throws ServletException {
        super.init();
        if (status != 1) {
            throw new ServletException("properties not configured properly");
        } else {
            logger.info("Finished Servlet init()");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        String hostName = request.getRemoteHost();
        String requestMethod = request.getParameter("Request");
        if (requestMethod != null) {
            requestMethod = requestMethod.toUpperCase();
        } else {
            requestMethod = "";
        }

        if (requestMethod.equals("GET")) {
            this.getGuids(request, response);
        } else if (requestMethod.equals("STATUS_CHECK")) {
            this.statusCheck(request, response);
        } else if (requestMethod.equals("CACHE_CHECK")) {
            this.cacheCheck(request, response);
        } else {
            logger.warn("Request from: " + hostName + " did not specify a valid " + "Request");
            this.usage(response);
        }

    }

    private void getGuids(HttpServletRequest request, HttpServletResponse response) {
        String hostName = request.getRemoteHost();
        String requestSizeString = request.getParameter("Size");

        long requestSize;
        try {
            requestSize = Long.parseLong(requestSizeString);
        } catch (Throwable var8) {
            logger.warn("Request from: " + request.getRemoteHost() + " could not be parsed - size string is " + requestSizeString);
            this.usage(response);
            return;
        }

        String requestNamespace = request.getParameter("Namespace");
        if (requestNamespace == null) {
            logger.info("Received request from: " + hostName + " size=" + requestSize + " namespace=<default>");
        } else {
            logger.info("Received request from: " + hostName + " size=" + requestSize + " namespace=" + requestNamespace);
        }

        this.handleGuidRequest(response, requestSize, requestNamespace);
    }

    private void handleGuidRequest(HttpServletResponse response, long requestSize, String requestNamespace) {
        String namespace = this.getNamespace(requestNamespace);

        try {
            GuidClientDBInterfaceFactory factory = GuidClientDBInterfaceFactory.getInstance();
            long start = factory.getGUIDBlock(requestSize, namespace);
            response.getWriter().println("<html><title>SUCCESS</title><body>");
            response.getWriter().println("<h2>Guid Start:</h2>");
            response.getWriter().println(start);
            response.getWriter().println("</body></html>");
        } catch (Throwable var9) {
            logger.error("Exception handling Guid Request: " + var9, var9);
            this.failure(response, var9.toString());
        }

    }

    private String getNamespace(String namespace) {
        return namespace == null ? defaultNamespace : namespace;
    }

    private void statusCheck(HttpServletRequest request, HttpServletResponse response) {
        String namespace = this.getNamespace(request.getParameter("Namespace"));

        GuidAdminDBInterfaceFactory factory;
        try {
            factory = GuidAdminDBInterfaceFactory.getInstance();
            GUIDNamespace guidNamespace = factory.getGUIDNamespaceInfo(namespace);
            if (guidNamespace != null) {
                this.writeServerAvaliableMessage(response);
                return;
            }

            logger.info("Could not locate guid namespace " + namespace + " in status check, " + "rolling over to guid cache retrieval based check");
        } catch (Throwable var6) {
            logger.warn("Exception in primary server status check, rolling over to guid cache retrieval based check " + var6, var6);
        }

        try {
            factory = GuidAdminDBInterfaceFactory.getInstance();
            CachedBlockInfo blockInfo = factory.getCachedBlockInfo(namespace);
            if (blockInfo != null && !blockInfo.isFullyAllocated()) {
                this.writeServerAvaliableMessage(response);
            } else {
                this.failure(response, "Currently unable to server guids for namespace " + namespace);
            }
        } catch (Throwable var7) {
            this.failure(response, var7.toString());
        }

    }

    private void writeServerAvaliableMessage(HttpServletResponse response) throws IOException {
        response.getWriter().println("<html><title>SERVER AVAILABLE</title><body>");
        response.getWriter().println("<h2>Guid Server is available</h2>");
        response.getWriter().println("</body></html>");
    }

    private void failure(HttpServletResponse response, String message) {
        try {
            response.setStatus(500);
            response.getWriter().println("<html><title>FAILURE</title><body>");
            response.getWriter().println("<h2>Message:</h2>");
            if (message != null) {
                response.getWriter().println(message);
            }

            response.getWriter().println("</body></html>");
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    private void cacheCheck(HttpServletRequest request, HttpServletResponse response) {
        String pageHeader = "<html><title>SERVER CACHE GUID BLOCK INFO</title><body>";
        String pageFooter = "</body></html>";
        String targetNamespace = request.getParameter("Namespace");

        try {
            GuidAdminDBInterfaceFactory factory = GuidAdminDBInterfaceFactory.getInstance();
            if (targetNamespace == null) {
                Set<CachedBlockInfo> cachedBlockList = factory.getCachedBlockInfo();
                if (cachedBlockList.isEmpty()) {
                    response.getWriter().println(pageHeader);
                    response.getWriter().println("<h2>Guid Server contains no cached guid blocks</h2>");
                    response.getWriter().println(pageFooter);
                } else {
                    response.getWriter().println(pageHeader);
                    Iterator i$ = cachedBlockList.iterator();

                    while(i$.hasNext()) {
                        CachedBlockInfo blockInfo = (CachedBlockInfo)i$.next();
                        this.writeBlockInfo(response, blockInfo);
                    }

                    response.getWriter().println(pageFooter);
                }
            } else {
                CachedBlockInfo blockInfo = factory.getCachedBlockInfo(targetNamespace);
                if (blockInfo == null) {
                    response.getWriter().println(pageHeader);
                    response.getWriter().println("<h2>Guid Server contains no cached guid block for namespace " + targetNamespace + "</h2>");
                    response.getWriter().println(pageFooter);
                } else {
                    response.getWriter().println(pageHeader);
                    this.writeBlockInfo(response, blockInfo);
                    response.getWriter().println(pageFooter);
                }
            }
        } catch (Throwable var10) {
            this.failure(response, var10.toString());
        }

    }

    private void writeBlockInfo(HttpServletResponse response, CachedBlockInfo blockInfo) throws IOException {
        response.getWriter().println("<h2>Guid namespace: " + blockInfo.getGuidNamespace() + "</h2>");
        response.getWriter().println("<ul>");
        response.getWriter().println("<li>Guid namespace: " + blockInfo.getGuidNamespace() + "</li>");
        response.getWriter().println("<li>Block size: " + blockInfo.getBlockSize() + "</li>");
        response.getWriter().println("<li>Start guid: " + blockInfo.getBlockStartGuid() + "</li>");
        response.getWriter().println("<li>Number of guids allocated: " + blockInfo.getNumberOfGuidsAllocated() + "</li>");
        response.getWriter().println("<li>Remaining guids available: " + blockInfo.getRemainingCachedGuids() + "</li>");
        if (blockInfo.isFullyAllocated()) {
            response.getWriter().println("<li>Next available guid in cache: NONE</li>");
        } else {
            response.getWriter().println("<li>Next available guid in cache: " + blockInfo.getNextAvaliableGuid() + "</li>");
        }

        response.getWriter().println("</ul>");
    }

    private void usage(HttpServletResponse response) {
        try {
            response.setStatus(400);
            response.getWriter().println("<html><title>USAGE</title><body>");
            response.getWriter().println("<h2>Guid retrieval - Maximum request size is " + maximumRequestSize + "guids </h2>");
            response.getWriter().println("http://&lt;servername&gt;:&lt;port&gt;/guid/GuidClientServer?Request=GET&Size=&lt;size&gt;[&Namespace=&lt;namespace&gt;]<br>");
            response.getWriter().println("<br>Note: If provided, optional namespace argument must be pre-defined with GUIDAdmin tool to function properly</br>");
            response.getWriter().println("<h2>Server status check:</h2>");
            response.getWriter().println("http://&lt;servername&gt;:&lt;port&gt;/guid/GuidClientServer?Request=STATUS_CHECK[&Namespace=&lt;namespace&gt;]<br>");
            response.getWriter().println("<br>Note: If provided, optional namespace argument allows user to check if server able to provide guids for the specified namespace</br>");
            response.getWriter().println("<h2>Server guid cache info display:</h2>");
            response.getWriter().println("http://&lt;servername&gt;:&lt;port&gt;/guid/GuidClientServer?Request=CACHE_CHECK[&Namespace=&lt;namespace&gt;]<br>");
            response.getWriter().println("<br>Note: If no namespace provided, returns info for all server cached namespaces");
            response.getWriter().println("</body></html>");
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    static {
        try {
            logger.info("Starting static initializer...");
            Properties properties = PropertyHelper.getHostnameProperties("GuidDBInterface");
            defaultNamespace = (String)properties.get("GuidDBInterface.Servlet.DefaultNamespace");
            maximumRequestSize = Long.parseLong((String)properties.get("GuidDBInterface.CACHE_UPDATE_BLOCKSIZE"));
            status = 1;
            logger.info("Successfully finished static initializer.");
        } catch (Exception var1) {
            status = 0;
            logger.error("Unable to properly load properties file: " + var1, var1);
        }

    }
}
