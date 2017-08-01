package org.jtc.app_db_interface.guid.app.ejb;

import org.apache.log4j.Logger;
import org.jtc.app_db_interface.guid.api.ejb.GuidClient;
import org.jtc.app_db_interface.guid.app.factory.GuidFactory;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;
import org.jtc.common.ejb.app.StatelessSessionBase;

public class GuidClientBean extends StatelessSessionBase implements GuidClient, GuidBean {
    private static final Logger logger = Logger.getLogger(GuidClientBean.class);
    private String dataSource = null;
    private String ejbUsername = null;

    public GuidClientBean() {
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

    public long getGUIDBlock(long size, String namespace) throws SystemErrorException, ApplicationErrorException {
        long blockStart = 0L;

        try {
            GuidFactory guidFactory = new GuidFactory(this);
            blockStart = guidFactory.getGUIDCachedBlock(size, namespace);
        } catch (SystemErrorException var8) {
            throw new SystemErrorException(var8);
        } catch (ApplicationErrorException var9) {
            this.setRollbackOnly();
            throw new ApplicationErrorException(var9);
        } catch (Exception var10) {
            this.setRollbackOnly();
            String message = "Exception: " + var10;
            logger.error(message, var10);
            throw new ApplicationErrorException(message, var10);
        }

        logger.debug("Exiting getGUIDBlock");
        return blockStart;
    }
}

