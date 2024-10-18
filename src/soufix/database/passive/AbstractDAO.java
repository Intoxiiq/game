package soufix.database.passive;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import soufix.database.DAO;
import soufix.database.Database;
import soufix.main.Main;
import soufix.main.Logging;

import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.LoggerFactory;

import java.sql.*;

public abstract class AbstractDAO<T> implements DAO<T>
{

  protected HikariDataSource dataSource;
  protected Logger logger=(Logger)LoggerFactory.getLogger(AbstractDAO.class+" - [S]");

  public AbstractDAO(HikariDataSource dataSource)
  {
    this.dataSource=dataSource;
    logger.setLevel(Level.INFO);
  }

  protected void execute(String query)
  {
      Connection connection=null;
      Statement statement=null;
      try
      {
    	//  Logging.getInstance().write("MYSQLLOGS_passice",query);
        connection=dataSource.getConnection();
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        statement.execute(query);
        logger.debug("SQL request executed successfully {}",query);
      }
      catch(SQLException e)
      {
        logger.error("Can't execute SQL Request :"+query,e);
      } finally
      {
        close(statement);
        close(connection);
      }
  }

  protected void execute(PreparedStatement statement)
  {
      Connection connection=null;
      try
      {
    	 // Logging.getInstance().write("MYSQLLOGS_passice",statement.toString());
        connection=statement.getConnection();
        statement.execute();
        logger.debug("SQL request executed successfully {}",statement.toString());
      }
      catch(SQLException e)
      {
        e.printStackTrace();
        logger.error("Can't execute SQL Request :"+statement.toString(),e);
      } finally
      {
        close(statement);
        close(connection);
      }
  }

  protected Result getData(String query)
  {
      Connection connection=null;
      try
      {
        if(!query.endsWith(";"))
          query=query+";";
        //Logging.getInstance().write("MYSQLLOGS_passice",query);
        connection=dataSource.getConnection();
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Result result=new Result(connection,statement.executeQuery(query));
        logger.debug("SQL request executed successfully {}",query);
        return result;
      }
      catch(SQLException e)
      {
        logger.error("Can't execute SQL Request :"+query,e);
      }
      return null;
  }

  protected PreparedStatement getPreparedStatement(String query) throws SQLException
  {
    try
    {
      Connection connection=dataSource.getConnection();
      return connection.prepareStatement(query);
    }
    catch(SQLException e)
    {
      e.printStackTrace();
      logger.error("Can't get  datasource connection",e);
      dataSource.close();
      if(!Database.getStatics().initializeConnection())
        Main.stop("statics prepared statement failed");
      return null;
    }
  }

  protected void close(PreparedStatement statement)
  {
    if(statement==null)
      return;
    try
    {
      if(!statement.isClosed())
      {
        statement.clearParameters();
        statement.close();
      }
    }
    catch(Exception e)
    {
      logger.error("Can't close statement",e);
    }
  }

  protected void close(Connection connection)
  {
    if(connection==null)
      return;
    try
    {
      if(!connection.isClosed())
      {
        connection.close();
        logger.trace("{} released",connection);
      }
    }
    catch(Exception e)
    {
      logger.error("Can't close connection",e);
    }
  }

  protected void close(Statement statement)
  {
    if(statement==null)
      return;
    try
    {
      if(!statement.isClosed())
        statement.close();
    }
    catch(Exception e)
    {
      logger.error("Can't close statement",e);
    }
  }

  protected void close(ResultSet resultSet)
  {
    if(resultSet==null)
      return;
    try
    {
      if(!resultSet.isClosed())
        resultSet.close();
    }
    catch(Exception e)
    {
      logger.error("Can't close resultSet",e);
    }
  }

  protected void close(Result result)
  {
    if(result!=null)
    {
      if(result.resultSet!=null)
        close(result.resultSet);
      if(result.connection!=null)
        close(result.connection);
      logger.trace("Connection {} has been released",result.connection);
    }
  }

  protected void sendError(String msg, Exception e)
  {
    e.printStackTrace();
    logger.error("Error statics database "+msg+" : "+e.getMessage());
	  Logging.getInstance().write("EUUREBDD","Error statics database "+msg+" : "+e.getMessage());
		 
  }

  protected class Result
  {
    public final Connection connection;
    public final ResultSet resultSet;

    protected Result(Connection connection, ResultSet resultSet)
    {
      this.connection=connection;
      this.resultSet=resultSet;
    }
  }

public void load(Object obj, int kamas) {
	// TODO Auto-generated method stub
	
}
}
