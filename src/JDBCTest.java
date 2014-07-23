import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by zhabba on 22.07.14.
 */
public class JDBCTest {
    public static  void main(String[] args) {
        JDBCConnector JDBCConnector;
        Connection myConnection = null;
        if (args[0] == null) {
            System.err.println("Properties file not specified at command line");
            return;
        } else {
            try {
                System.out.println("Reading properties file " + args[0]);
                JDBCConnector = new JDBCConnector(args[0]);
            } catch (Exception e) {
                System.err.println("Problem reading properties file " + args[0]);
                e.printStackTrace();
                return;
            }
        }

        try {
            myConnection = JDBCConnector.getConnection();
        } catch (SQLException e) {
            JDBCConnector.printSQLException(e);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            JDBCConnector.closeConnection(myConnection);
        }
    }
}
