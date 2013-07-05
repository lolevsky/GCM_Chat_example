package com.iapplize.gcm.test.server;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@SuppressWarnings("serial")
public class GCM_Get_User_List_Servlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/plain");

		// ...
		// Get the Datastore Service
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		// The Query interface assembles a query
		Query q = new Query("users");

		// Date d1 = new Date(((new Date()).getTime()) - 18000000);
		Date d1 = new Date(((new Date()).getTime()) - 172800000);

		// Use CompositeFilter to set more than one filter
		q.setFilter(new FilterPredicate("Updated_at",
				FilterOperator.GREATER_THAN_OR_EQUAL, d1));

		q.addSort("Updated_at", Query.SortDirection.DESCENDING);

		// PreparedQuery contains the methods for fetching query results
		// from the datastore
		PreparedQuery pq = datastore.prepare(q);

		resp.getWriter().println("<root>");

		for (Entity result : pq.asIterable(FetchOptions.Builder.withLimit(20)
				.offset(0))) {

			if (((String) result.getProperty("UnReg")).equalsIgnoreCase("NO")) {

				resp.getWriter().println("<row>");
				resp.getWriter().println("<RegId>");
				resp.getWriter().println((String) result.getProperty("RegId"));
				resp.getWriter().println("</RegId>");
				if(result.getProperty("UserName") != null){
					resp.getWriter().println("<UserName>");
					resp.getWriter().println((String) result.getProperty("UserName"));
					resp.getWriter().println("</UserName>");
				}
				resp.getWriter().println("<Email>");
				resp.getWriter().println((String) result.getProperty("Email"));
				resp.getWriter().println("</Email>");
				resp.getWriter().println("<Updated_at>");
				resp.getWriter().println(
						((Date) result.getProperty("Updated_at")).getTime());
				resp.getWriter().println("</Updated_at>");
				resp.getWriter().println("<MacAddress>");
				resp.getWriter().println(
						(String) result.getProperty("MacAddress"));
				resp.getWriter().println("</MacAddress>");
				resp.getWriter().println("</row>");
			}
		}

		resp.getWriter().println("</root>");

		/*
		 * java.sql.Connection c = null;
		 * 
		 * try { DriverManager.registerDriver(new AppEngineDriver()); c =
		 * DriverManager
		 * .getConnection("jdbc:google:rdbms://gcmtestdatabase:gcmtestdb/gcmtest"
		 * );
		 * 
		 * String statement =
		 * "SELECT RegId, Email, Updated_at, MacAddress FROM users WHERE UNIX_TIMESTAMP(Updated_at) > UNIX_TIMESTAMP (now())-18000"
		 * ; ResultSet resultSet = c.createStatement().executeQuery(statement);
		 * 
		 * resp.getWriter().println("<root>");
		 * 
		 * while (resultSet.next()){ resp.getWriter().println("<row>");
		 * resp.getWriter().println("<RegId>");
		 * resp.getWriter().println(resultSet.getString("RegId"));
		 * resp.getWriter().println("</RegId>");
		 * resp.getWriter().println("<Email>");
		 * resp.getWriter().println(resultSet.getString("Email"));
		 * resp.getWriter().println("</Email>");
		 * resp.getWriter().println("<Updated_at>");
		 * resp.getWriter().println(resultSet.getString("Updated_at"));
		 * resp.getWriter().println("</Updated_at>");
		 * resp.getWriter().println("<MacAddress>");
		 * resp.getWriter().println(resultSet.getString("MacAddress"));
		 * resp.getWriter().println("</MacAddress>");
		 * resp.getWriter().println("</row>"); }
		 * 
		 * resp.getWriter().println("</root>");
		 * 
		 * } catch (SQLException e) { e.printStackTrace();
		 * 
		 * //resp.getWriter().println("Failure! - " + e.getMessage());
		 * resp.getWriter().println("<root/>");
		 * 
		 * } finally { if (c != null) try { c.close(); } catch (SQLException
		 * ignore) { } }
		 */

	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// TODO Auto-generated method stub
		resp.setContentType("text/plain");
		resp.getWriter().println("GCM Server");
	}
}
