package org.jtc.common.ejb.app;

import org.apache.log4j.Logger;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;
import org.jtc.common.util.property.PropertyHelper;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

abstract public class StatelessSessionBase implements javax.ejb.SessionBean {

    protected final Logger logger = Logger.getLogger(this.getClass());

    private PropertyHelper config;
    private javax.ejb.SessionContext ctx;
    private String dataSource;

    public StatelessSessionBase() {
    }

    public void ejbActivate() {
    }

    public void ejbCreate() throws javax.ejb.CreateException {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    public void setSessionContext(javax.ejb.SessionContext sessionContext)
            throws javax.ejb.EJBException {
        this.ctx = sessionContext;
    }

    public String getUserName() {
        return ctx.getCallerPrincipal().getName();
    }

    abstract protected String getConfigResource();

    protected PropertyHelper getConfig() {
        if(config == null) {
            config = PropertyHelper.getInstance();
            if(!config.hasProperties()) {
                config.loadProperties(getConfigResource());
            }
        }
        return config;
    }

    protected String getDataSource() throws ApplicationErrorException {
        if (dataSource==null) {
            try {
                dataSource=(String)getEJBProperty("DataSource");
            } catch (Exception ex){
                logger.error("DataSource not defined in EJB Properties!!");
                throw new ApplicationErrorException("DataSource not defined in EJB Properties!!",ex);
            }
        }
        return dataSource;
    }

    protected Object getEJBProperty(String name) throws Exception {
        Object returnValue=null;
        try{
            Context initContext = new InitialContext();
            Context myEnv = (Context)initContext.lookup("java:comp/env");
            returnValue = myEnv.lookup(name);
        } catch (Exception ex) {
            String errorMessage = "Cannot retrieve "+name+" from EJB Deployment Descriptor Environment";
            logger.error(errorMessage,ex);
            throw ex;
        }
        return returnValue;
    }

    protected javax.ejb.SessionContext getSessionContext() {
        return this.ctx;
    }

    protected Connection getConnection(String jndiName)
            throws NamingException,SQLException {
        Connection connection = null;
        InitialContext ic = new InitialContext();
        javax.sql.DataSource ds;
        ds = (javax.sql.DataSource)ic.lookup("java:/"+jndiName);
        connection = ds.getConnection();
        return connection;
    }

    protected void returnConnection(Connection connection)
            throws SQLException {
        if (connection!=null) {
            connection.close();
        }
    }

    protected Connection getSystemConnection(String jndiName)
            throws SystemErrorException {
        Connection connection = null;
        try {
            connection = getConnection(jndiName);
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
        return connection;
    }

    protected void returnSystemConnection(Connection connection)
            throws SystemErrorException {
        try {
            returnConnection(connection);
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    protected void prepareRollback() {
        try {
            ctx.setRollbackOnly();
        } catch(IllegalStateException ex) {
            logger.debug("Error during transaction rollback",ex);
        }
    }

    //Created for GUIDClientServer - update GuidClientBean once project is found
    protected void setRollbackOnly() {
        try {
            ctx.setRollbackOnly();
        } catch(IllegalStateException ex) {
            logger.debug("Error during transaction rollback",ex);
        }
    }

}