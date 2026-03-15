package com.fablix.servlet;

import com.fablix.util.RedisSessionManager;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DatabaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String MASTER_JNDI = "java:comp/env/jdbc/moviedb-master";
    private static final String REPLICA_1_JNDI = "java:comp/env/jdbc/moviedb-replica-1";
    private static final String REPLICA_2_JNDI = "java:comp/env/jdbc/moviedb-replica-2";

    private final AtomicInteger readIndex = new AtomicInteger();
    private DataSource masterDataSource;
    private DataSource[] readDataSources;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            RedisSessionManager.init();
            InitialContext context = new InitialContext();
            masterDataSource = lookupDataSource(context, MASTER_JNDI);
            readDataSources = new DataSource[]{
                    lookupDataSource(context, REPLICA_1_JNDI),
                    lookupDataSource(context, REPLICA_2_JNDI)
            };
        } catch (NamingException e) {
            throw new UnavailableException("Unable to initialize JDBC data source: " + e.getMessage());
        }
    }

    protected Connection getConnection() throws SQLException {
        return getReadConnection();
    }

    protected Connection getReadConnection() throws SQLException {
        int index = Math.floorMod(readIndex.getAndIncrement(), readDataSources.length);
        return readDataSources[index].getConnection();
    }

    protected Connection getWriteConnection() throws SQLException {
        return masterDataSource.getConnection();
    }

    private DataSource lookupDataSource(InitialContext context, String jndiName) throws NamingException {
        return (DataSource) context.lookup(jndiName);
    }
}
