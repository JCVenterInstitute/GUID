package org.jtc.app_db_interface.guid.app.factory;

import org.apache.log4j.Logger;
import org.jtc.app_db_interface.guid.api.dto.CachedBlockInfoDTO;
import org.jtc.app_db_interface.guid.api.dto.GUIDBlockDTO;
import org.jtc.app_db_interface.guid.api.dto.GUIDNamespaceDTO;
import org.jtc.app_db_interface.guid.api.intf.CachedBlockInfo;
import org.jtc.app_db_interface.guid.api.intf.GUIDBlock;
import org.jtc.app_db_interface.guid.api.intf.GUIDNamespace;
import org.jtc.app_db_interface.guid.app.ejb.GuidBean;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

public class GuidFactory {
    private static Properties properties = null;
    private static final String PROPERTIES_PATH_PREFIX = "GuidDBInterface";
    private static final String NAMESPACE_MIN_LENGTH_PROP = "GuidDBInterface.NAMESPACE_MIN_LENGTH";
    private static final String NAMESPACE_MAX_LENGTH_PROP = "GuidDBInterface.NAMESPACE_MAX_LENGTH";
    private static boolean propertiesLoadedFlag = false;
    private static String CONTROLLED_NAMESPACES_PROP = "GuidDBInterface.CONTROLLED_NAMESPACES";
    private static String MIN_REQUEST_BLOCKSIZE_PROP = "GuidDBInterface.MIN_REQUEST_BLOCKSIZE";
    private static String MAX_REQUEST_BLOCKSIZE_PROP = "GuidDBInterface.MAX_REQUEST_BLOCKSIZE";
    private static long minRequestSize = 1L;
    private static long maxRequestSize = 1L;
    private static final String CACHE_BLOCKSIZE_PROP = "GuidDBInterface.CACHE_UPDATE_BLOCKSIZE";
    private static final String CACHE_COMMENT = "Request from EJB cache provider";
    private static Hashtable<String, CachedBlockInfoDTO> blockCache = new Hashtable();
    private static long cacheBlockSize = 0L;
    protected GuidBean ejb = null;
    private static Logger logger = Logger.getLogger(GuidFactory.class);

    public GuidFactory(GuidBean ejb) throws ApplicationErrorException {
        this.ejb = ejb;
        if (!propertiesLoadedFlag) {
            throw new ApplicationErrorException("Properties failed to load.");
        }
    }

    public long getGUIDCachedBlock(long requestSize, String namespace) throws SystemErrorException, ApplicationErrorException {
        if (requestSize >= 1L && requestSize <= cacheBlockSize) {
            Class var4 = GuidFactory.class;
            synchronized(GuidFactory.class) {
                CachedBlockInfoDTO cachedBlock = (CachedBlockInfoDTO)blockCache.get(namespace);
                if (cachedBlock != null && cachedBlock.hasSufficientGuidsForRequest(requestSize)) {
                    logger.debug("Retrieving guids from cached guid namespace " + namespace);
                } else {
                    this.updateCache(namespace);
                    cachedBlock = (CachedBlockInfoDTO)blockCache.get(namespace);
                }

                return cachedBlock.allocateGuids(requestSize);
            }
        } else {
            throw new ApplicationErrorException("Error: GUID request size of " + requestSize + " is invalid; " + "requests must be >= 1 and <= " + cacheBlockSize + " guids");
        }
    }

    private void updateCache(String namespace) throws SystemErrorException, ApplicationErrorException {
        Class var2 = GuidFactory.class;
        synchronized(GuidFactory.class) {
            logger.debug("Updating cached guid namespace " + namespace);
            long blockStart = this.retrieveGUIDBlock(namespace, cacheBlockSize, "Request from EJB cache provider");
            CachedBlockInfoDTO info = new CachedBlockInfoDTO(namespace, cacheBlockSize, blockStart);
            blockCache.put(namespace, info);
        }
    }

    public long retrieveGUIDBlock(String namespace, long requestSize, String comment) throws SystemErrorException, ApplicationErrorException {
        logger.debug("Retrieving " + requestSize + " guids for namespace " + namespace);
        if (this.namespaceControlled(namespace)) {
            throw new ApplicationErrorException("Error - namespace " + namespace + " is controlled.");
        } else if (requestSize >= minRequestSize && requestSize < maxRequestSize) {
            Class var5 = GuidFactory.class;
            synchronized(GuidFactory.class) {
                Connection connection = null;

                long var16;
                try {
                    connection = this.getConnection();
                    long namespaceID = this.getNamespaceIDByName(namespace, connection);
                    MaxAllocatedGUIDRetriever retriever = new MaxAllocatedGUIDRetriever();
                    long currentMax = retriever.retrieveMaxAllocatedGUID(connection);
                    long blockStartGuid = currentMax + 1L;
                    long blockEndGuid = currentMax + requestSize;
                    this.storeMaxAllocatedGUID(connection, currentMax, blockEndGuid);
                    this.storeGuidBlockRequest(connection, blockStartGuid, blockEndGuid, namespaceID, comment, requestSize, this.ejb.getEjbUsername());
                    var16 = blockStartGuid;
                } finally {
                    JDBCUtilities.closeConnection(connection);
                }

                return var16;
            }
        } else {
            throw new ApplicationErrorException("Error in retrieveGUIDBlock() - request size " + requestSize + " not >= " + minRequestSize + " && < " + maxRequestSize);
        }
    }

    private void storeMaxAllocatedGUID(Connection connection, long previousMaxAllocatedGuid, long currentMaxAllocatedGuid) throws ApplicationErrorException, SystemErrorException {
        ResultSet rs = null;
        Statement statement = null;

        try {
            String sql = "UPDATE jcvi_guid.max_allocated_guid SET maxid_value=" + currentMaxAllocatedGuid + " WHERE maxid_value=" + previousMaxAllocatedGuid;
            statement = connection.createStatement();
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated != 1) {
                throw new ApplicationErrorException("Error updating current max allocated guid id; " + sql + " statement updated " + rowsUpdated + " rows rather than 1");
            }
        } catch (ApplicationErrorException var14) {
            throw var14;
        } catch (Exception var15) {
            throw new SystemErrorException("Unexpected error updating current max allocated guid id: " + var15, var15);
        } finally {
            JDBCUtilities.closeResultSetAndStatement((ResultSet)rs, statement);
        }

    }

    private void storeGuidBlockRequest(Connection connection, long blockStartGuid, long blockEndGuid, long namespaceID, String comment, long requestSize, String createByUsername) throws ApplicationErrorException, SystemErrorException {
        ResultSet rs = null;
        Statement statement = null;

        try {
            String sql = "INSERT INTO jcvi_guid.guid_block_table (gblock_first_guid,  gblock_last_guid,  gblock_namespace_id,  gblock_creation_comment,  gblock_block_size,  gblock_create_date,  gblock_created_by) VALUES ( " + blockStartGuid + "," + blockEndGuid + "," + namespaceID + "," + "'" + comment + "'" + "," + requestSize + "," + "CURRENT_TIMESTAMP" + "," + "'" + createByUsername + "'" + ")";
            statement = connection.createStatement();
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated != 1) {
                throw new ApplicationErrorException("Error inserting new guid block allocation record; " + sql + " statement inserted " + rowsUpdated + " rows rather than 1");
            }
        } catch (ApplicationErrorException var20) {
            throw var20;
        } catch (Exception var21) {
            throw new SystemErrorException("Unexpected error inserting new guid block allocation record: " + var21, var21);
        } finally {
            JDBCUtilities.closeResultSetAndStatement((ResultSet)rs, statement);
        }

    }

    public GUIDBlock getGUIDAllocationInfoByGUID(long guid) throws SystemErrorException, ApplicationErrorException {
        String queryString = "SELECT gblock_first_guid, gblock_last_guid, gname_namespace, gblock_create_date, gblock_block_size, gblock_creation_comment FROM jcvi_guid.guid_namespace_table, jcvi_guid.guid_block_table WHERE gblock_first_guid <= " + guid + " AND gblock_last_guid  >= " + guid + " AND gblock_namespace_id = gname_id" + " ORDER BY gblock_create_date";
        List<GUIDBlock> guidBlockList = this.getGUIDAllocationInfoByQuery(queryString);
        if (guidBlockList.isEmpty()) {
            return null;
        } else if (guidBlockList.size() == 1) {
            return (GUIDBlock)guidBlockList.get(0);
        } else {
            throw new ApplicationErrorException("Guid " + guid + " exists in the following " + guidBlockList.size() + " blocks; " + " guids should only exist in 1 block" + guidBlockList);
        }
    }

    public GUIDBlock[] getGUIDAllocationInfoByNamespace(String namespace) throws SystemErrorException, ApplicationErrorException {
        String queryString = "SELECT gblock_first_guid, gblock_last_guid, gname_namespace, gblock_create_date, gblock_block_size, gblock_creation_comment FROM jcvi_guid.guid_namespace_table, jcvi_guid.guid_block_table WHERE gname_namespace like '" + namespace + "'" + " AND gblock_namespace_id = gname_id" + " ORDER BY gblock_create_date";
        List<GUIDBlock> guidBlockList = this.getGUIDAllocationInfoByQuery(queryString);
        return (GUIDBlock[])guidBlockList.toArray(new GUIDBlock[guidBlockList.size()]);
    }

    public GUIDBlock[] getGUIDAllocationInfoAll() throws SystemErrorException, ApplicationErrorException {
        String queryString = "SELECT gblock_first_guid, gblock_last_guid, gname_namespace, gblock_create_date, gblock_block_size, gblock_creation_comment FROM jcvi_guid.guid_namespace_table, jcvi_guid.guid_block_table WHERE gblock_namespace_id = gname_id ORDER BY gblock_create_date";
        List<GUIDBlock> guidBlockList = this.getGUIDAllocationInfoByQuery(queryString);
        return (GUIDBlock[])guidBlockList.toArray(new GUIDBlock[guidBlockList.size()]);
    }

    private List<GUIDBlock> getGUIDAllocationInfoByQuery(String queryString) throws SystemErrorException {
        ArrayList<GUIDBlock> guidBlockList = new ArrayList();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement = null;

        try {
            connection = this.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(queryString);

            while(rs.next()) {
                long firstGUID = rs.getLong(1);
                long lastGUID = rs.getLong(2);
                String queryNamespace = rs.getString(3);
                Timestamp timestamp = rs.getTimestamp(4);
                Date date = new Date(timestamp.getTime());
                long size = rs.getLong(5);
                String comment = rs.getString(6);
                GUIDBlock guidBlock = new GUIDBlockDTO(comment, date, firstGUID, lastGUID, queryNamespace, size);
                guidBlockList.add(guidBlock);
            }
        } catch (Exception var20) {
            throw new SystemErrorException("Unexpected error retrieving block allocation records for query " + queryString, var20);
        } finally {
            JDBCUtilities.closeResultSetAndStatement(rs, statement);
            JDBCUtilities.closeConnection(connection);
        }

        return guidBlockList;
    }

    public List<GUIDNamespace> getGUIDNamespaceInfo() throws SystemErrorException, ApplicationErrorException {
        Connection connection = null;

        List var2;
        try {
            connection = this.getConnection();
            var2 = this.getGUIDNamespaceInfoImpl((String)null, connection);
        } finally {
            JDBCUtilities.closeConnection(connection);
        }

        return var2;
    }

    public GUIDNamespace getGUIDNamespaceInfo(String namespace) throws SystemErrorException, ApplicationErrorException {
        Connection connection = null;

        GUIDNamespace var3;
        try {
            connection = this.getConnection();
            var3 = this.getGUIDNamespaceInfo(namespace, connection);
        } finally {
            JDBCUtilities.closeConnection(connection);
        }

        return var3;
    }

    protected long getNamespaceIDByName(String namespace, Connection connection) throws ApplicationErrorException, SystemErrorException {
        GUIDNamespace guidNamespace = this.getGUIDNamespaceInfo(namespace, connection);
        if (guidNamespace != null) {
            return guidNamespace.getId();
        } else {
            throw new ApplicationErrorException("Error - could not find namespace ID for namespace " + namespace);
        }
    }

    protected boolean namespaceExists(String namespace, Connection connection) throws ApplicationErrorException, SystemErrorException {
        GUIDNamespace guidNamespace = this.getGUIDNamespaceInfo(namespace, connection);
        return guidNamespace != null;
    }

    protected GUIDNamespace getGUIDNamespaceInfo(String namespace, Connection connection) throws SystemErrorException, ApplicationErrorException {
        List<GUIDNamespace> namespaceList = this.getGUIDNamespaceInfoImpl(namespace, connection);
        if (namespaceList.isEmpty()) {
            return null;
        } else if (namespaceList.size() == 1) {
            return (GUIDNamespace)namespaceList.get(0);
        } else {
            throw new ApplicationErrorException("Found " + namespaceList.size() + " guid namespaces for namespace " + namespace + "; should only find one namespace" + namespaceList);
        }
    }

    private List<GUIDNamespace> getGUIDNamespaceInfoImpl(String namespace, Connection connection) throws SystemErrorException, ApplicationErrorException {
        StringBuffer queryString = new StringBuffer("SELECT gname_id, gname_namespace, gname_create_date, gname_creation_comment FROM jcvi_guid.guid_namespace_table ");
        if (namespace != null) {
            queryString.append("WHERE gname_namespace = '" + namespace + "' ");
        }

        queryString.append("ORDER BY gname_create_date");
        List<GUIDNamespace> guidNamespaceList = new ArrayList();
        ResultSet rs = null;
        Statement statement = null;

        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(queryString.toString());
            if (rs == null) {
                throw new ApplicationErrorException("Received null result set");
            }

            while(rs.next()) {
                long id = rs.getLong(1);
                String guidNamespace = rs.getString(2);
                Timestamp timestamp = rs.getTimestamp(3);
                String comment = rs.getString(4);
                Date date = new Date(timestamp.getTime());
                guidNamespaceList.add(new GUIDNamespaceDTO(id, guidNamespace, date, comment));
            }
        } catch (ApplicationErrorException var17) {
            throw var17;
        } catch (Exception var18) {
            throw new SystemErrorException("Unexpected error retrieving guid namespace info: " + var18, var18);
        } finally {
            JDBCUtilities.closeResultSetAndStatement(rs, statement);
        }

        return guidNamespaceList;
    }

    public String ping(String message) throws SystemErrorException, ApplicationErrorException {
        return message;
    }

    public String createGUIDNamespace(String namespace, String comment) throws SystemErrorException, ApplicationErrorException {
        Connection connection = null;
        Statement statement = null;
        String result = null;

        try {
            connection = this.getConnection();
            int minimumNameLength = Integer.parseInt(getProperty("GuidDBInterface.NAMESPACE_MIN_LENGTH"));
            int maximumNameLength = Integer.parseInt(getProperty("GuidDBInterface.NAMESPACE_MAX_LENGTH"));
            if (namespace.length() > maximumNameLength || namespace.length() < minimumNameLength) {
                throw new ApplicationErrorException("Requested namespace " + namespace + " violates min/max length constraints");
            }

            if (this.namespaceExists(namespace, connection)) {
                result = "Namespace already exists";
            } else {
                String creationString = "INSERT INTO jcvi_guid.guid_namespace_table (gname_namespace,  gname_creation_comment,  gname_create_date,  gname_created_by ) VALUES ( '" + namespace + "'" + ", " + "'" + comment + "'" + ", " + "CURRENT_TIMESTAMP" + ", " + "'" + this.ejb.getEjbUsername() + "'" + ")";
                statement = connection.createStatement();
                int updateCount = statement.executeUpdate(creationString);
                if (updateCount != 1) {
                    throw new ApplicationErrorException("Error - update count for namespace creation should have been 1 but is " + updateCount);
                }

                result = "Done";
            }
        } catch (ApplicationErrorException var14) {
            throw var14;
        } catch (Exception var15) {
            throw new SystemErrorException("Unexpected error creating guid namespace: " + var15, var15);
        } finally {
            JDBCUtilities.closeStatement(statement);
            JDBCUtilities.closeConnection(connection);
        }

        return result;
    }

    protected boolean namespaceControlled(String namespace) throws ApplicationErrorException {
        StringTokenizer controlledNamespaceTokenizer = new StringTokenizer(getProperty(CONTROLLED_NAMESPACES_PROP), ", ");

        String controlledNamespace;
        do {
            if (!controlledNamespaceTokenizer.hasMoreTokens()) {
                return false;
            }

            controlledNamespace = controlledNamespaceTokenizer.nextToken();
        } while(!controlledNamespace.equals(namespace));

        return true;
    }

    protected static String getProperty(String key) throws ApplicationErrorException {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new ApplicationErrorException("Could not find value for key " + key);
        } else {
            return value;
        }
    }

    protected Connection getConnection() throws ApplicationErrorException {
        String jndiName = this.ejb.getDataSource();

        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource)ic.lookup("java:/" + jndiName);
            return ds.getConnection();
        } catch (Exception var4) {
            throw new ApplicationErrorException("Unable to obtain database connection for jndi name " + jndiName, var4);
        }
    }

    public void refreshCachedNamespaces() throws ApplicationErrorException, SystemErrorException {
        Class var1 = GuidFactory.class;
        synchronized(GuidFactory.class) {
            if (!blockCache.isEmpty()) {
                Connection connection = null;

                try {
                    connection = this.getConnection();
                    Iterator i$ = blockCache.keySet().iterator();

                    while(i$.hasNext()) {
                        String namespace = (String)i$.next();
                        this.updateCache(namespace);
                    }
                } finally {
                    JDBCUtilities.closeConnection(connection);
                }

            }
        }
    }

    public Set<CachedBlockInfo> getGuidBlockCacheInformation() throws SystemErrorException, ApplicationErrorException {
        Class var1 = GuidFactory.class;
        synchronized(GuidFactory.class) {
            HashSet<CachedBlockInfo> cacheInfo = new HashSet(blockCache.size());
            Iterator i$ = blockCache.values().iterator();

            while(i$.hasNext()) {
                CachedBlockInfo info = (CachedBlockInfoDTO)i$.next();
                cacheInfo.add(info);
            }

            return cacheInfo;
        }
    }

    public CachedBlockInfo getGuidBlockCacheInformation(String namespace) throws SystemErrorException, ApplicationErrorException {
        Class var2 = GuidFactory.class;
        synchronized(GuidFactory.class) {
            return (CachedBlockInfo)blockCache.get(namespace);
        }
    }

    static {
        try {
            properties = PropertyHelper.getHostnameProperties("GuidDBInterface");
            cacheBlockSize = Long.parseLong(getProperty("GuidDBInterface.CACHE_UPDATE_BLOCKSIZE"));
            minRequestSize = Long.parseLong(getProperty(MIN_REQUEST_BLOCKSIZE_PROP));
            maxRequestSize = Long.parseLong(getProperty(MAX_REQUEST_BLOCKSIZE_PROP));
            propertiesLoadedFlag = true;
        } catch (Exception var1) {
            var1.printStackTrace();
        }

    }
}

