/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sps.listeners;

import com.google.sps.classes.Utility;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import org.json.simple.JSONArray;

@WebListener("Creates a connection pool that is stored in the Servlet's context for later use.")
public class ConnectionPoolContextListener implements ServletContextListener {
  // Constants ussed to retrieve the keys to the database.
  private final static int FETCH_SQL_CLOUD_CONNECTION_NAME = 0;
  private final static int FETCH_SQL_CLOUD_USER = 1;
  private final static int FETCH_SQL_CLOUD_PASSWORD = 2;
  private final static int FETCH_SQL_CLOUD_DATABASE_NAME = 3;

  // Retrieve keys to the cloud database.
  private static final JSONArray arrayOfKeys = Utility.getKeys("CLOUD_SQL_ACCESS");
  private static final String SQL_CLOUD_CONNECTION_NAME = 
      arrayOfKeys.get(FETCH_SQL_CLOUD_CONNECTION_NAME);
  private static final String SQL_CLOUD_USER = arrayOfKeys.get(FETCH_SQL_CLOUD_USER);
  private static final String SQL_CLOUD_PASSWORD = arrayOfKeys.get(FETCH_SQL_CLOUD_PASSWORD);
  private static final String SQL_CLOUD_DATABASE_NAME = 
      arrayOfKeys.get(FETCH_SQL_CLOUD_DATABASE_NAME);

  private DataSource createConnectionPool() {
    // [START cloud_sql_mysql_servlet_create]
    // The configuration object specifies behaviors for the connection pool.
    HikariConfig config = new HikariConfig();

    // Configure which instance and what database user to connect with.
    config.setJdbcUrl(String.format("jdbc:mysql:///%s", SQL_CLOUD_DATABASE_NAME));
    config.setUsername(SQL_CLOUD_USER); // e.g. "root", "postgres"
    config.setPassword(SQL_CLOUD_PASSWORD); // e.g. "my-password"

    // maximumPoolSize limits the total number of concurrent connections this pool will keep. Ideal
    // values for this setting are highly variable on app design, infrastructure, and database.
    config.setMaximumPoolSize(20);
    // minimumIdle is the minimum number of idle connections Hikari maintains in the pool.
    // Additional connections will be established to meet this value unless the pool is full.
    config.setMinimumIdle(10);

    // setConnectionTimeout is the maximum number of milliseconds to wait for a connection checkout.
    // Any attempt to retrieve a connection from this pool that exceeds the set limit will throw an
    // SQLException.
    config.setConnectionTimeout(10000); // 10 seconds
    // idleTimeout is the maximum amount of time a connection can sit in the pool. Connections that
    // sit idle for this many milliseconds are retried if minimumIdle is exceeded.
    config.setIdleTimeout(600000); // 10 minutes

    // maxLifetime is the maximum possible lifetime of a connection in the pool. Connections that
    // live longer than this many milliseconds will be closed and reestablished between uses. This
    // value should be several minutes shorter than the database's timeout value to avoid unexpected
    // terminations.
    config.setMaxLifetime(1800000); // 30 minutes

    // For Java users, the Cloud SQL JDBC Socket Factory can provide authenticated connections.
    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.mysql.SocketFactory");
    config.addDataSourceProperty("cloudSqlInstance", SQL_CLOUD_CONNECTION_NAME);

    // Initialize the connection pool using the configuration object.
    DataSource pool = new HikariDataSource(config);
    return pool;
  }

  /**
   * Destroys pull of connections whenever the webapp is terminated.
   */
  @Override
  public void contextDestroyed(ServletContextEvent event) {
    if (Utility.localOrDeployed.equals("deploy")) {
      // This function is called when the Servlet is destroyed.
      HikariDataSource pool = (HikariDataSource) event.getServletContext().getAttribute("my-pool");
      if (pool != null) {
        pool.close();
      }
    }
  }

  /**
   * Creates pool of connections whenever the webapp is loaded.
   */
  @Override
  public void contextInitialized(ServletContextEvent event) {
    if (Utility.localOrDeployed.equals("deploy")) {
      // This function is called when the application starts and will safely create a connection pool
      // that can be used to connect to.
      ServletContext servletContext = event.getServletContext();
      DataSource pool = (DataSource) servletContext.getAttribute("my-pool");
      if (pool == null) {
        pool = createConnectionPool();
        servletContext.setAttribute("my-pool", pool);
      }
    }
  }
}
