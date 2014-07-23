/*
 * Copyright (c) 1995, 2011, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCConnector {

    public String dbms;
    public String dbName;
    public String userName;
    public String password;
    public String urlString;

    private String driver;
    private String serverName;
    private int portNumber;
    private Properties prop;


    public JDBCConnector(String propertiesFileName) throws IOException {
        super();
        this.setProperties(propertiesFileName);
    }

    public static boolean ignoreSQLException(String sqlState) {
        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            return false;
        }
        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
            return true;
        // 42Y55: Table already exists in schema
        if (sqlState.equalsIgnoreCase("42Y55"))
            return true;
        return false;
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (ignoreSQLException(((SQLException) e).getSQLState()) == false) {
                    System.err.println("******************************************************************************");
                    e.printStackTrace(System.err);
                    System.err.println("     SQLState: " + ((SQLException) e).getSQLState());
                    System.err.println("     Error Code: " + ((SQLException) e).getErrorCode());
                    System.err.println("     Message: " + e.getMessage());
                    System.err.println("******************************************************************************");
                    Throwable t = ex.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    public static void closeConnection(Connection connArg) {
        System.out.println("Releasing all open resources ...");
        try {
            if (connArg != null) {
                connArg.close();
                connArg = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    private void setProperties(String fileName) throws IOException {
        this.prop = new Properties();
        FileInputStream fis = new FileInputStream(fileName);
        prop.loadFromXML(fis);

        this.dbms = this.prop.getProperty("dbms");
        this.driver = this.prop.getProperty("driver");
        this.dbName = this.prop.getProperty("database_name");
        this.userName = this.prop.getProperty("user_name");
        this.password = this.prop.getProperty("password");
        this.serverName = this.prop.getProperty("server_name");
        this.portNumber = Integer.parseInt(this.prop.getProperty("port_number"));

        System.out.println("Set the following properties:");
        System.out.println("dbms: " + dbms);
        System.out.println("driver: " + driver);
        System.out.println("dbName: " + dbName);
        System.out.println("userName: " + userName);
        System.out.println("serverName: " + serverName);
        System.out.println("portNumber: " + portNumber);

    }

    public Connection getConnection() throws SQLException {
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", this.userName);
        connectionProps.put("password", this.password);
        String currentUrlString = null;
        if (this.dbms.equals("mysql")) {
            currentUrlString = "jdbc:" + this.dbms + "://" + this.serverName + ":" + this.portNumber + "/";
            conn = DriverManager.getConnection(currentUrlString, connectionProps);
            this.urlString = currentUrlString + this.dbName;
            conn.setCatalog(this.dbName);
        } else if (this.dbms.equals("mssql")) {
            this.urlString = "jdbc:" + this.dbms + ":" + this.dbName;
            conn = DriverManager.getConnection(this.urlString, connectionProps);
        }
        System.out.println("***********************************");
        System.out.println("*      Connected to database      *");
        System.out.println("***********************************");
        return conn;
    }
}
