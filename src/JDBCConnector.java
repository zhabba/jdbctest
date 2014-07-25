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
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class JDBCConnector {

    public String dbms;
    public String dbName;
    public String userName;
    public String password;

    private String driver;
    private String serverName;
    private int portNumber;
    private Properties prop;
    private String appRole;
    private String appPassword;


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
        this.appRole = this.prop.getProperty("appRole");
        this.appPassword = this.prop.getProperty("appPassword");

        System.out.println("Set the following properties:");
        System.out.println("dbms: " + dbms);
        System.out.println("driver: " + driver);
        System.out.println("dbName: " + dbName);
        System.out.println("userName: " + userName);
        System.out.println("serverName: " + serverName);
        System.out.println("portNumber: " + portNumber);
        System.out.println("appRole: " + appRole);
    }

    public Connection getConnection() throws SQLException {
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", this.userName);
        connectionProps.put("password", this.password);
        String currentUrlString = null;
        if (this.dbms.equals("mysql")) {
            //jdbc:mysql://[host][,failoverhost...][:port]/[database][?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]
            currentUrlString = "jdbc:" + this.dbms + "://" + this.serverName + ":" + this.portNumber + "/" + this.dbName;
        } else if (this.dbms.equals("sqlserver")) {
            // jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
            currentUrlString = "jdbc:" + this.dbms + "://" + this.serverName + ":" + this.portNumber + ";databaseName=" + this.dbName;
        }

        conn = DriverManager.getConnection(currentUrlString, connectionProps);
        conn.setCatalog(this.dbName);

        System.out.println("\n\n***********************************");
        System.out.println("*      Connected to database      *");
        System.out.println("***********************************\n\n");
        if (this.dbms.equals("sqlserver")) {
            Statement st = null;
            try {
                st = conn.createStatement(); //XXX Make this secure
                st.execute(String.format("EXEC sp_setapprole '%s', '%s'",
                        appRole, appPassword));
                System.out.println("\n\n***********************************");
                System.out.println("* Setup role executed successfully *");
                System.out.println("***********************************\n\n");
                System.out.println("Type your query:> ");
                Scanner sc = new Scanner(System.in);
                while (sc.hasNext()) {
                    String rawQuery = sc.nextLine();
                    try {
                        ResultSet out = st.executeQuery(rawQuery);
                        while (out.next() != false) {
                            int i = 1;
                            while (out.getString(i) != null) {
                                System.out.print(out.getString(i) + "\t");
                                i++;
                            }
                            System.out.println();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("\nType your query:> ");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (st != null) {
                    st.close();
                }
            }
        }
        return conn;
    }
}
