import org.nd4j.jdbc.loader.api.JDBCNDArrayIO;
import javax.sql.DataSource;
import java.sql.*;

abstract class BaseLoader implements JDBCNDArrayIO {
    /**
     * Delete the given ndarray
     *
     * @param id the id of the ndarray to delete
     */
    @Override
    public void delete(String id) throws SQLException {
	Connection c = dataSource.getConnection();
	PreparedStatement p = c.prepareStatement(deleteStatement());
	p.setString(1, id);
	p.execute();

    }

    protected DataSource dataSource;

}

