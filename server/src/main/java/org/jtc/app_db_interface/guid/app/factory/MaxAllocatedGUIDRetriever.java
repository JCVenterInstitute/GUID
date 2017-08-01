package org.jtc.app_db_interface.guid.app.factory;

import org.apache.log4j.Logger;
import org.jtc.common.ejb.api.exception.ApplicationErrorException;
import org.jtc.common.ejb.api.exception.SystemErrorException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MaxAllocatedGUIDRetriever {
    private static final String MAX_GUID_RETRIEVAL_QUERY = "SELECT MAX(maxid_value) FROM jcvi_guid.max_allocated_guid FOR UPDATE";
    private static final String DEADLOCK_EXCEPTION_MESSAGE = "Deadlock found when trying to get lock";
    private static final String LOCK_WAIT_TIMEOUT_EXCEPTION_MESSAGE = "Lock wait timeout exceeded";
    private int maxRetrievalAttempts = 5;
    private long queryReattemptInterval = 5000L;
    private long queryCheckInterval = 100L;
    private long maxQueryWaitTime = 5000L;
    private Logger logger = Logger.getLogger(this.getClass());

    public MaxAllocatedGUIDRetriever() {
    }

    public long retrieveMaxAllocatedGUID(Connection connection) throws ApplicationErrorException, SystemErrorException {
        for(int i = 1; i <= this.maxRetrievalAttempts; ++i) {
            this.logger.debug("Starting attempt " + i + " to retrieve max allocated guid");

            try {
                long var3 = this.retrieveMaxAllocatedGUIDImpl(connection);
                return var3;
            } catch (MaxAllocatedGUIDRetriever.ConcurrentRequestException var11) {
                StringBuffer logMessage = new StringBuffer();
                logMessage.append("Attempt " + i + " to retrieve max allocated guid unsuccessful due to " + "(apparent) concurrent request from different application instance");
                if (i < this.maxRetrievalAttempts) {
                    logMessage.append("; will re-try retrieval in " + this.queryReattemptInterval + " milliseconds");
                }

                this.logger.info(logMessage.toString());
            } catch (Throwable var12) {
                throw new SystemErrorException("Unexpected failure retrieving max allocated guid", var12);
            } finally {
                this.logger.debug("Finished attempt " + i + " to retrieve max allocated guid");
            }

            if (i < this.maxRetrievalAttempts) {
                try {
                    Thread.sleep(this.queryReattemptInterval);
                } catch (InterruptedException var10) {
                    ;
                }
            }
        }

        throw new ApplicationErrorException("Could not successfully retrieve max allocated guid  after " + this.maxRetrievalAttempts + " retries");
    }

    private long retrieveMaxAllocatedGUIDImpl(Connection connection) throws Exception {
        Statement statement = null;

        long var7;
        try {
            statement = connection.createStatement();
            MaxAllocatedGUIDRetriever.QueryRunner retriever = new MaxAllocatedGUIDRetriever.QueryRunner(statement, "SELECT MAX(maxid_value) FROM jcvi_guid.max_allocated_guid FOR UPDATE");
            (new Thread(retriever)).start();
            long startTime = System.currentTimeMillis();

            while(!retriever.isRetrievalCompleted() && System.currentTimeMillis() - startTime < this.maxQueryWaitTime) {
                this.logger.debug("Waiting for retrieval completion");
                Thread.sleep(this.queryCheckInterval);
            }

            if (!retriever.isRetrievalCompleted()) {
                this.logger.debug("Cancelling running statement");
                statement.cancel();
                throw new MaxAllocatedGUIDRetriever.ConcurrentRequestException("Unable to retrieve max allocated guid from db after " + this.maxQueryWaitTime + " miliseconds; " + "; most likely cause is db deadlock due to extremely long running concurrent db transaction");
            }

            Exception retrievalException = retriever.getRetrievalException();
            if (retrievalException != null) {
                if (this.isPotentialConcurrentRequestException(retrievalException)) {
                    throw new MaxAllocatedGUIDRetriever.ConcurrentRequestException("Unable to retrieve max allocated guid from db; failure appears to be due to a concurrent data request: " + retrievalException, retrievalException);
                }

                throw retrievalException;
            }

            var7 = retriever.getMaxAllocatedGuid();
        } finally {
            this.logger.debug("Beginning to close query statement");
            JDBCUtilities.closeStatement(statement);
            this.logger.debug("Finished closing query statement");
        }

        return var7;
    }

    private boolean isPotentialConcurrentRequestException(Exception e) {
        String exceptionMessage = e.getMessage();
        return exceptionMessage != null && (exceptionMessage.contains("Deadlock found when trying to get lock") || exceptionMessage.contains("Lock wait timeout exceeded"));
    }

    private class ConcurrentRequestException extends Exception {
        public ConcurrentRequestException(String message) {
            super(message);
        }

        public ConcurrentRequestException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private class QueryRunner implements Runnable {
        private boolean retrievalCompleted;
        private Exception retrievalException;
        private Statement statement;
        private String sql;
        private long maxAllocatedGuid;

        public QueryRunner(Statement statement, String sql) {
            this.statement = statement;
            this.sql = sql;
            this.retrievalException = null;
            this.retrievalCompleted = false;
        }

        public void run() {
            ResultSet rs = null;

            try {
                rs = this.statement.executeQuery(this.sql);
                if (rs == null || !rs.next()) {
                    throw new Exception("Error retrieving current max allocated guid id; " + this.sql + " result set contains no data");
                }

                this.maxAllocatedGuid = rs.getLong(1);
                if (this.maxAllocatedGuid <= 0L) {
                    throw new Exception("Error in retrieveMaxAllocatedGUID() - " + this.maxAllocatedGuid + " is not a legal maximum allocated GUID value");
                }
            } catch (Exception var6) {
                this.retrievalException = var6;
            } finally {
                JDBCUtilities.closeResultSet(rs);
            }

            this.retrievalCompleted = true;
        }

        public boolean isRetrievalCompleted() {
            return this.retrievalCompleted;
        }

        public Exception getRetrievalException() {
            return this.retrievalException;
        }

        public long getMaxAllocatedGuid() {
            return this.maxAllocatedGuid;
        }
    }
}

