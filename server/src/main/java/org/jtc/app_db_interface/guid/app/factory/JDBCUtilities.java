package org.jtc.app_db_interface.guid.app.factory;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCUtilities {
    private static Logger logger = Logger.getLogger(JDBCUtilities.class);

    private JDBCUtilities() {
    }

    public static void closeResultSetAndStatement(ResultSet rs, Statement statement) {
        closeResultSet(rs);
        closeStatement(statement);
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception var2) {
                logger.warn("Unexpected error closing result set: " + var2, var2);
            }
        }

    }

    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception var2) {
                logger.warn("Unexpected error closing statement: " + var2, var2);
            }
        }

    }

    protected static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception var2) {
                logger.warn("Unexpected error closing connection", var2);
            }
        }

    }
}
