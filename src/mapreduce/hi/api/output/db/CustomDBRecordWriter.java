package mapreduce.hi.api.output.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;
import org.apache.hadoop.util.StringUtils;

public class CustomDBRecordWriter<K extends DBWritable, V extends DBWritable> extends RecordWriter<K, V>{

	private static final Log LOG = LogFactory.getLog(DBOutputFormat.class);
    private Connection connection;
    private PreparedStatement statement;

    public CustomDBRecordWriter() throws SQLException {
    }

    public CustomDBRecordWriter(Connection connection
        , PreparedStatement statement) throws SQLException {
      this.connection = connection;
      this.statement = statement;
      this.connection.setAutoCommit(true);
    }

    public Connection getConnection() {
      return connection;
    }
    
    public PreparedStatement getStatement() {
      return statement;
    }
    
    /** {@inheritDoc} */
    public void close(TaskAttemptContext context) throws IOException {
      try {
        statement.executeBatch();
        connection.commit();
      } catch (SQLException e) {
        try {
          connection.rollback();
        }
        catch (SQLException ex) {
          LOG.warn(StringUtils.stringifyException(ex));
        }
        throw new IOException(e.getMessage());
      } finally {
        try {
          statement.close();
          connection.close();
        }
        catch (SQLException ex) {
          throw new IOException(ex.getMessage());
        }
      }
    }

    /** {@inheritDoc} */
    public void write(K key, V value) throws IOException {
      try {
    	value.write(statement);
    	key.write(statement);
        statement.addBatch();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
}
