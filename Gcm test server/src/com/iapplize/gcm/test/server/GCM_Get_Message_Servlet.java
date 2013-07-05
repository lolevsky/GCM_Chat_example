package com.iapplize.gcm.test.server;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class GCM_Get_Message_Servlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	
		String regId = req.getParameter("regId");
		String userEmail = req.getParameter("userEmail");
		String withEmail = req.getParameter("withEmail");

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

//		Query q = new Query("users");
//
//		Filter emailFilter = new FilterPredicate("Email", FilterOperator.EQUAL,
//				userEmail);
//
//		Filter regIdFilter = new FilterPredicate("regId", FilterOperator.EQUAL,
//				regId);
//
//		// Use CompositeFilter to combine multiple filters
//		Filter emailAndRegIdFilter = CompositeFilterOperator.and(emailFilter,
//				regIdFilter);
//
//		q.setFilter(emailAndRegIdFilter);
//
//		PreparedQuery pq = datastore.prepare(q);
//
//		List<Entity> res = pq.asList(FetchOptions.Builder.withLimit(20));

		JSONArray jArray;
		try {
			jArray = new JSONArray();

			//if (res != null && res.size() > 0) {

				Query qMesssge = new Query("messages");
				
				Query qUsers = new Query("users");

				if (withEmail == null) {

//					Filter allFilter = new FilterPredicate("toAll",
//							FilterOperator.EQUAL, "true");

					Date d1 = new Date(((new Date()).getTime()) - 172800000);

					Filter dateFilter = new FilterPredicate("Updated_at",
							FilterOperator.GREATER_THAN_OR_EQUAL, d1);

//					Filter allAndDateFilter = CompositeFilterOperator.and(
//							allFilter, dateFilter);

					qMesssge.setFilter(dateFilter);
					
					qMesssge.addSort("Updated_at", Query.SortDirection.DESCENDING);

					PreparedQuery pqAll = datastore.prepare(qMesssge);

					JSONObject jsonObject;

					for (Entity result : pqAll.asIterable(FetchOptions.Builder
							.withLimit(50).offset(0))) {

						jsonObject = new JSONObject();
						jsonObject
								.put("message", result.getProperty("message"));
						jsonObject.put("fromEmail",
								result.getProperty("fromEmail"));
						jsonObject.put("toAll", result.getProperty("toAll"));
						jsonObject
								.put("toEmail", result.getProperty("toEmail"));
						jsonObject.put("Updated_at",
								((Date) result.getProperty("Updated_at")).getTime());
						
						
						Filter nameFilter = new FilterPredicate("Email",
								FilterOperator.EQUAL, result.getProperty("fromEmail"));
						
						qUsers.setFilter(nameFilter);
						PreparedQuery pqAllName = datastore.prepare(qUsers);
						for (Entity resultName : pqAllName.asIterable(FetchOptions.Builder
								.withLimit(1).offset(0))) {
						
							jsonObject
							.put("UserName", resultName.getProperty("UserName"));
							
						}

						jArray.put(jsonObject);
					}

				} else {

					Filter meFilter = new FilterPredicate("fromEmail",
							FilterOperator.EQUAL, userEmail);
					
					Filter otherFilter = new FilterPredicate("toEmail",
							FilterOperator.EQUAL, withEmail);
					
					Filter meAndOtherFilter = CompositeFilterOperator.and(
							meFilter, otherFilter);
					
					Filter meToFilter = new FilterPredicate("toEmail",
							FilterOperator.EQUAL, userEmail);
					
					Filter otherFromFilter = new FilterPredicate("fromEmail",
							FilterOperator.EQUAL, withEmail);
					
					Filter otherAndMeFilter = CompositeFilterOperator.and(
							meToFilter, otherFromFilter);
					
					Filter orFilter = CompositeFilterOperator.or(
							meAndOtherFilter, otherAndMeFilter);

					
//					Filter toAllFilter = new FilterPredicate("toAll",
//							FilterOperator.NOT_EQUAL, "false");
					
//					Filter finalFilter = CompositeFilterOperator.and(
//							toAllFilter, orFilter);

//					Date d1 = new Date(((new Date()).getTime()) - 172800000);
//
//					Filter dateFilter = new FilterPredicate("Updated_at",
//							FilterOperator.GREATER_THAN_OR_EQUAL, d1);

//					Filter allAndDateFilter = CompositeFilterOperator.and(
//							finalFilter, dateFilter);

//					qMesssge.setFilter(allAndDateFilter);
					
//					Filter allAndDateFilter = CompositeFilterOperator.and(
//							orFilter, dateFilter);
					
					qMesssge.setFilter(orFilter);
					qMesssge.addSort("Updated_at", Query.SortDirection.DESCENDING);

					PreparedQuery pqAll = datastore.prepare(qMesssge);

					JSONObject jsonObject;

					for (Entity result : pqAll.asIterable(FetchOptions.Builder
							.withLimit(50).offset(0))) {

						jsonObject = new JSONObject();
						if(result.getProperty("message") != null){
							jsonObject.put("message",result.getProperty("message"));
						}
						
						if(result.getProperty("fromEmail") != null){
							jsonObject.put("fromEmail",result.getProperty("fromEmail"));
						}	
//						if(result.getProperty("toAll") != null){
//							jsonObject.put("toAll", result.getProperty("toAll"));
//						}
						if(result.getProperty("toEmail") != null){
							jsonObject.put("toEmail", result.getProperty("toEmail"));
						}
						if(result.getProperty("Updated_at") != null){
							jsonObject.put("Updated_at",((Date) result.getProperty("Updated_at")).getTime());
						}
						
						Filter nameFilter = new FilterPredicate("Email",
								FilterOperator.EQUAL, result.getProperty("fromEmail"));
						
						qUsers.setFilter(nameFilter);
						PreparedQuery pqAllName = datastore.prepare(qUsers);
						for (Entity resultName : pqAllName.asIterable(FetchOptions.Builder
								.withLimit(1).offset(0))) {
						
							jsonObject
							.put("UserName", resultName.getProperty("UserName"));
							
						}

						jArray.put(jsonObject);
					}
				//}
			}

			resp.setContentType("text/plain");
			resp.getWriter().println(jArray.toString());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			resp.setContentType("text/plain");
			resp.getWriter().println("error : " + e.toString());
		}
		
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		
		resp.setContentType("text/plain");
		resp.getWriter().println("GCM Server");

	}

}
