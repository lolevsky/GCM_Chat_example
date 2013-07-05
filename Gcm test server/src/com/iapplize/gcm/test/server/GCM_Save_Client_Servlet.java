package com.iapplize.gcm.test.server;

import java.io.IOException;

import java.util.Date;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class GCM_Save_Client_Servlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String res = "";
		
		resp.setContentType("text/plain");

		String regId = req.getParameter("regId");
		String installationID = req.getParameter("installationID");
		String user_email = req.getParameter("user_email");
		String user_name = req.getParameter("user_name");

		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Entity userData = new Entity("users",installationID);

		userData.setProperty("UserId", regId);
		userData.setProperty("RegId", regId);
		userData.setProperty("Email", user_email);
		userData.setProperty("MacAddress", installationID);
		userData.setProperty("UnReg", "NO");
		if(user_name != null){
			userData.setProperty("UserName", user_name);
		}
		Date hireDate = new Date();
		userData.setProperty("Updated_at", hireDate);

		datastore.put(userData);
		
		res = "ok";
		
		/*java.sql.Connection c = null;

		try {
			DriverManager.registerDriver(new AppEngineDriver());
			c = DriverManager
					.getConnection("jdbc:google:rdbms://gcmtestdatabase:gcmtestdb/gcmtest");

			if (regId == "" || installationID == "" || user_email == "") {
				resp.getWriter().println("You are missing either a regId or a installationID or a user_email!");
			} else {
				String statement = "INSERT INTO users SET MacAddress = ? , RegId = ? , Email = ? ON DUPLICATE KEY UPDATE RegId = ? , Email = ?";
				PreparedStatement stmt = c.prepareStatement(statement);
				stmt.setString(1, installationID);
				stmt.setString(2, regId);
				stmt.setString(3, user_email);
				stmt.setString(4, regId);
				stmt.setString(5, user_email);
				int success = 2;
				success = stmt.executeUpdate();
				if (success == 1) {
				
					//res = "ok";
					
					//resp.getWriter().println("ok");
				} else if (success == 0) {
					
					resp.getWriter().println("Failure!");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			
			resp.getWriter().println("Failure! - " + e.getMessage());
			
		} finally {
			if (c != null)
				try {
					c.close();
				} catch (SQLException ignore) {
				}
		}*/

		//res = "ok";
		
		/*resp.getWriter().println("regId : " + regId);
		resp.getWriter().println("installationID : " + installationID);
		resp.getWriter().println("user_email : " + user_email);*/
		
		if(res.equalsIgnoreCase("ok")){
			resp.getWriter().println("ok");
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// TODO Auto-generated method stub
		resp.setContentType("text/plain");
		resp.getWriter().println("GCM Server");
	}
}
