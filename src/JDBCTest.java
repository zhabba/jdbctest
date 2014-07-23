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
            System.err.println("Server type not specified at command line");
            return;
        } else {
            try {
                System.out.println("Reading properties file " + "\"" + args[0] + "\"");
                JDBCConnector = new JDBCConnector("./properties/" + args[0] + ".xml");
            } catch (Exception e) {
                System.err.println("Problem reading properties file " + "./properties/" + args[0] + ".xml");
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
