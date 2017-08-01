package org.jtc.app_db_interface.guid.app.ejb;

import org.apache.log4j.Logger;
import org.jtc.app_db_interface.guid.api.ejb.GuidAdmin;
import org.jtc.app_db_interface.guid.api.intf.CachedBlockInfo;
import org.jtc.app_db_interface.guid.api.intf.GUIDBlock;
import org.jtc.app_db_interface.guid.api.intf.GUIDNamespace;
import org.jtc.app_db_interface.guid.app.factory.GuidFactory;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;
import org.jtc.common.ejb.app.StatelessSessionBase;

import java.util.List;
import java.util.Set;

public class GuidAdminBean extends StatelessSessionBase implements GuidAdmin, GuidBean {
    private static final Logger logger = Logger.getLogger(GuidAdminBean.class);
    private String dataSource = null;
    private String ejbUsername = null;

    public GuidAdminBean() {
        this.dataSource = this.getBeanProperty("EJBDataSource");
        this.ejbUsername = this.getBeanProperty("EJBUserIdentifier");
    }

    private String getBeanProperty(String propertyName) {
        String value = null;

        try {
            value = (String)this.getEJBProperty(propertyName);
        } catch (Exception var4) {
            logger.error(propertyName + " not defined in EJB Properties!!");
        }

        return value;
    }

    protected String getConfigResource() {
        return "GuidDBInterface";
    }

    public String getDataSource() {
        return this.dataSource;
    }

    public String getEjbUsername() {
        return this.ejbUsername;
    }

    public long getGUIDBlock(String namespace, long size, String comment) throws SystemErrorException, ApplicationErrorException {
        long blockStart;
        try {
            GuidFactory guidFactory = new GuidFactory(this);
            blockStart = guidFactory.retrieveGUIDBlock(namespace, size, comment);
        } catch (SystemErrorException var9) {
            throw new SystemErrorException(var9);
        } catch (ApplicationErrorException var10) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var10);
        } catch (Exception var11) {
            this.setRollbackOnly();
            String message = "Exception: " + var11;
            logger.error(message, var11);
            throw new ApplicationErrorException(message, var11);
        }

        logger.debug("Exiting getGUIDBlock");
        return blockStart;
    }

    public GUIDBlock getGUIDAllocationInfoByGUID(long guid) throws SystemErrorException, ApplicationErrorException {
        GUIDBlock guidBlock;
        try {
            GuidFactory guidFactory = new GuidFactory(this);
            guidBlock = guidFactory.getGUIDAllocationInfoByGUID(guid);
        } catch (SystemErrorException var6) {
            throw new SystemErrorException(var6);
        } catch (ApplicationErrorException var7) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var7);
        } catch (Exception var8) {
            this.setRollbackOnly();
            String message = "Exception: " + var8;
            logger.error(message, var8);
            throw new ApplicationErrorException(message, var8);
        }

        logger.debug("Exiting getGUIDAllocationInfoByGUID");
        return guidBlock;
    }

    public GUIDBlock[] getGUIDAllocationInfoByNamespace(String namespace) throws SystemErrorException, ApplicationErrorException {
        GUIDBlock[] guidBlockList;
        try {
            GuidFactory guidFactory = new GuidFactory(this);
            guidBlockList = guidFactory.getGUIDAllocationInfoByNamespace(namespace);
        } catch (SystemErrorException var5) {
            throw new SystemErrorException(var5);
        } catch (ApplicationErrorException var6) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var6);
        } catch (Exception var7) {
            this.setRollbackOnly();
            String message = "Exception: " + var7;
            logger.error(message, var7);
            throw new ApplicationErrorException(message, var7);
        }

        logger.debug("Exiting getGUIDAllocationInfoByNamespace");
        return guidBlockList;
    }

    public GUIDBlock[] getGUIDAllocationInfoAll() throws SystemErrorException, ApplicationErrorException {
        GUIDBlock[] guidBlockList;
        try {
            GuidFactory guidFactory = new GuidFactory(this);
            guidBlockList = guidFactory.getGUIDAllocationInfoAll();
        } catch (SystemErrorException var4) {
            throw new SystemErrorException(var4);
        } catch (ApplicationErrorException var5) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var5);
        } catch (Exception var6) {
            this.setRollbackOnly();
            String message = "Exception: " + var6;
            logger.error(message, var6);
            throw new ApplicationErrorException(message, var6);
        }

        logger.debug("Exiting getGUIDAllocationInfoAll");
        return guidBlockList;
    }

    public List<GUIDNamespace> getGUIDNamespaceInfo() throws SystemErrorException, ApplicationErrorException {
        List guidNamespaceList;
        try {
            GuidFactory guidFactory = new GuidFactory(this);
            guidNamespaceList = guidFactory.getGUIDNamespaceInfo();
        } catch (SystemErrorException var4) {
            throw new SystemErrorException(var4);
        } catch (ApplicationErrorException var5) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var5);
        } catch (Exception var6) {
            this.setRollbackOnly();
            String message = "Exception: " + var6;
            logger.error(message, var6);
            throw new ApplicationErrorException(message, var6);
        }

        logger.debug("Exiting getGUIDNamespaceInfo()");
        return guidNamespaceList;
    }

    public GUIDNamespace getGUIDNamespaceInfo(String namespace) throws SystemErrorException, ApplicationErrorException {
        GUIDNamespace guidNamespace;
        try {
            GuidFactory guidFactory = new GuidFactory(this);
            guidNamespace = guidFactory.getGUIDNamespaceInfo(namespace);
        } catch (SystemErrorException var5) {
            throw new SystemErrorException(var5);
        } catch (ApplicationErrorException var6) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var6);
        } catch (Exception var7) {
            this.setRollbackOnly();
            String message = "Exception: " + var7;
            logger.error(message, var7);
            throw new ApplicationErrorException(message, var7);
        }

        logger.debug("Exiting getGUIDNamespaceInfo(namespace)");
        return guidNamespace;
    }

    public String ping(String message) throws SystemErrorException, ApplicationErrorException {
        String reply;
        try {
            new GuidFactory(this);
            reply = message;
        } catch (ApplicationErrorException var5) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var5);
        } catch (Exception var6) {
            this.setRollbackOnly();
            String exceptionMessage = "Exception: " + var6;
            logger.error(exceptionMessage, var6);
            throw new ApplicationErrorException(exceptionMessage, var6);
        }

        logger.debug("Exiting ping");
        return reply;
    }

    public String createGUIDNamespace(String namespace, String comment) throws SystemErrorException, ApplicationErrorException {
        String statusReply;
        try {
            GuidFactory guidFactory = new GuidFactory(this);
            statusReply = guidFactory.createGUIDNamespace(namespace, comment);
        } catch (SystemErrorException var6) {
            throw new SystemErrorException(var6);
        } catch (ApplicationErrorException var7) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var7);
        } catch (Exception var8) {
            this.setRollbackOnly();
            String exceptionMessage = "Exception: " + var8;
            logger.error(exceptionMessage, var8);
            throw new ApplicationErrorException(exceptionMessage, var8);
        }

        logger.debug("Exiting createGUIDNamespace");
        return statusReply;
    }

    public void refreshCachedNamespaces() throws SystemErrorException, ApplicationErrorException {
        logger.debug("Entering refreshCachedNamespaces");

        try {
            GuidFactory guidFactory = new GuidFactory(this);
            guidFactory.refreshCachedNamespaces();
        } catch (SystemErrorException var3) {
            throw new SystemErrorException(var3);
        } catch (ApplicationErrorException var4) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var4);
        } catch (Exception var5) {
            this.setRollbackOnly();
            String exceptionMessage = "Exception: " + var5;
            logger.error(exceptionMessage, var5);
            throw new ApplicationErrorException(exceptionMessage, var5);
        }

        logger.debug("Exiting refreshCachedNamespaces");
    }

    public Set<CachedBlockInfo> getCachedBlockInfo() throws SystemErrorException, ApplicationErrorException {
        logger.debug("Entering getCachedBlockInfo()");

        Set blockInfo;
        try {
            GuidFactory guidFactory = new GuidFactory(this);
            blockInfo = guidFactory.getGuidBlockCacheInformation();
        } catch (SystemErrorException var4) {
            throw new SystemErrorException(var4);
        } catch (ApplicationErrorException var5) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var5);
        } catch (Exception var6) {
            this.setRollbackOnly();
            String exceptionMessage = "Exception: " + var6;
            logger.error(exceptionMessage, var6);
            throw new ApplicationErrorException(exceptionMessage, var6);
        }

        logger.debug("Exiting getCachedBlockInfo()");
        return blockInfo;
    }

    public CachedBlockInfo getCachedBlockInfo(String namespace) throws SystemErrorException, ApplicationErrorException {
        logger.debug("Entering getCachedBlockInfo(namespace)");

        CachedBlockInfo blockInfo;
        try {
            GuidFactory guidFactory = new GuidFactory(this);
            blockInfo = guidFactory.getGuidBlockCacheInformation(namespace);
        } catch (SystemErrorException var5) {
            throw new SystemErrorException(var5);
        } catch (ApplicationErrorException var6) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var6);
        } catch (Exception var7) {
            this.setRollbackOnly();
            String exceptionMessage = "Exception: " + var7;
            logger.error(exceptionMessage, var7);
            throw new ApplicationErrorException(exceptionMessage, var7);
        }

        logger.debug("Exiting getCachedBlockInfo(namespace)");
        return blockInfo;
    }
}
