package testing.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This servlet is just to test a query on the Mintern database, and can be used as reference
 * for later use.
 */
@WebServlet("/test")
public class TestServlet extends HttpServlet {
  // All the variables needed to connect to the local database.
  // P.S.: Change the timezone if needed (https://github.com/dbeaver/dbeaver/wiki/JDBC-Time-Zones).
  String url = "jdbc:mysql://localhost:3306/Mintern?useSSL=false&serverTimezone=America/Mexico_City";
  String user = "root";
  String password = "";

  // This is the query that will be executed.
  // Feel free to modify this value to match the query that you would like to test.
  String query = "SELECT * FROM User";
  
  /** 
   * This method runs the specified query above on the Mintern database and returns
   * the result as a JSON string.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // We begin the JSON string.
    String json = "{";
    // The connection and query are attempted.
    try (Connection connection = DriverManager.getConnection(url, user, password);
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet queryResult = preparedStatement.executeQuery()) {
          // All of the rows from the query are looped if it goes through.
          while (queryResult.next()) {
            // Get the int from the first column, in this case the id,
            // and format it for the JSON.
            json += "\"" + queryResult.getInt(1) + "\": ";
            // Get the string from the second column, in this case the name,
            // and format it for the JSON.
            json += "\"" + queryResult.getString(2) + "\"";
            // Add a comma to separate from the next element in the JSON.
            json += ", ";
          }
          // Delete the last whitespace and comma that were added to the JSON string.
          json = json.substring(0, json.length() - 2);
        } catch (SQLException exception) {
          // If the connection or the query don't go through, we get the log of what happened.
          Logger logger = Logger.getLogger(TestServlet.class.getName());
          logger.log(Level.SEVERE, exception.getMessage(), exception);
        }
    // We finish the JSON string.
    json += "}";
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
