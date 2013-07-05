package com.iapplize.gcm.test.server;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.*;

import com.google.android.gcm.server.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@SuppressWarnings("serial")
public class GCM_Send_Message_Servlet extends HttpServlet {

	String resStr = "";
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String regId = req.getParameter("regId");
		String messageIn = req.getParameter("message");
		String fromEmail = req.getParameter("fromEmail");
		String toEmail = req.getParameter("toEmail");
		String fromRegId = req.getParameter("fromRegId");
		String toAll = req.getParameter("toAll");

		if (messageIn.equalsIgnoreCase("")) {
			messageIn = "Test From Android OK !!!";
		}


		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		
		
		Sender sender = new Sender("AIzaSyAI5W_oUInpIXybE-yuKz5Y7PsqtjL4RV8");
	
		//Result result = null;
		
		if(toAll== null){
		
			Message message = new Message.Builder().addData("message", messageIn).addData("fromUser", fromEmail)
					.addData("regId", fromRegId).build();
			
			
			responseGCM(datastore, sender.send(message, regId, 5), regId);
		
		}else{
			Query qUsers = new Query("users");

			// Date d1 = new Date(((new Date()).getTime()) - 18000000);
			Date d1 = new Date(((new Date()).getTime()) - 172800000);

			// Use CompositeFilter to set more than one filter
			qUsers.setFilter(new FilterPredicate("Updated_at",
					FilterOperator.GREATER_THAN_OR_EQUAL, d1));

			qUsers.addSort("Updated_at", Query.SortDirection.DESCENDING);

			// PreparedQuery contains the methods for fetching query results
			// from the datastore
			PreparedQuery pqUsers = datastore.prepare(qUsers);
			
			for (Entity resultUsers : pqUsers.asIterable(FetchOptions.Builder.withLimit(20)
					.offset(0))) {
				
				if (((String) resultUsers.getProperty("UnReg")).equalsIgnoreCase("NO")) {
					
					Message message = new Message.Builder().addData("message", messageIn).addData("fromUser", fromEmail)
							.addData("regId", fromRegId).build();
					
					responseGCM(datastore, sender.send(message, (String) resultUsers.getProperty("RegId"), 5), regId);
				}
			}
		}

		Entity massegeData = new Entity("messages");

		massegeData.setProperty("RegId", regId);
		massegeData.setProperty("message", messageIn);
		massegeData.setProperty("fromEmail", fromEmail);
		if(toAll != null){
			massegeData.setProperty("toAll", toAll);
		}else if(toEmail != null){
			massegeData.setProperty("toEmail", toEmail);
		}
		Date hireDate = new Date();
		massegeData.setProperty("Updated_at", hireDate);

		datastore.put(massegeData);

		resp.setContentType("text/plain");
		resp.getWriter().println("resStr : " + resStr);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("GCM Server");
	}
	
	private void responseGCM(DatastoreService datastore, Result result, String regId){
		Query q;
		
		if (result != null && result.getMessageId() != null) {

			String canonicalRegId = result.getCanonicalRegistrationId();
			if (canonicalRegId != null) {
				// same device has more than on registration ID: update
				// database
				
				q = new Query("users");

				// Use CompositeFilter to set more than one filter
				q.setFilter(new FilterPredicate("regId",
						Query.FilterOperator.EQUAL, regId));

				// PreparedQuery contains the methods for fetching query results
				// from the datastore
				PreparedQuery pq1 = datastore.prepare(q);
				
				
				String mac = "";
				
				for (Entity re : pq1.asIterable()) {
					
					mac = (String) re.getProperty("MacAddress");
					
				}
				
				
				Entity userData = new Entity("users",mac);

				userData.setProperty("UnReg", "NO");
				userData.setProperty("RegId", canonicalRegId);

				datastore.put(userData);
				
			}
		} else {

			String error = result.getErrorCodeName();
			
			resStr = "error : " + error;
			
			if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
				// application has been removed from device - unregister
				// database
				
				q = new Query("users");

				// Use CompositeFilter to set more than one filter
				q.setFilter(new FilterPredicate("regId",
						Query.FilterOperator.EQUAL, regId));

				// PreparedQuery contains the methods for fetching query results
				// from the datastore
				PreparedQuery pq1 = datastore.prepare(q);
				
				
				String mac = "";
				
				for (Entity re : pq1.asIterable()) {
					
					mac = (String) re.getProperty("MacAddress");
					
				}
				
				if(mac != null && !mac.equalsIgnoreCase("")){
					Entity userData = new Entity("users",mac);

					userData.setProperty("UnReg", "YES");
					
					datastore.put(userData);
				}
			}
		}
	}
}
