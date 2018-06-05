package com.c4e.mymashup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.c4e.mymashup.odata.C4CTicketODataClient;
import com.c4e.oauth.AuthInfoProcessor;

/**
 * Servlet implementation class MyMashupServiceServlet
 */
@WebServlet("/MyMashupServiceServlet")
public class MyMashupServiceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MyMashupServiceServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.getWriter().println("Hello World!");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String signature = request.getHeader("x-c4c-signature");
		String payload = getBody(request);
		
		AuthInfoProcessor processor = new AuthInfoProcessor(payload, signature);
		
		System.out.println("host : "  + processor.getHost());
		System.out.println("user : "  + processor.getUser());
		
		String json = getTicketsFromC4C(processor.getHost(), processor.getUser());
		response.setContentType("application/json");
		PrintWriter pw = response.getWriter();
		pw.println(json);
		pw.flush();
		pw.close();
	}

	private String getTicketsFromC4C(String host, String user) throws IOException {
		// "https://myXXXXXX.crm.ondemand.com/sap/byd/odata/v1/servicerequest"
		
		C4CTicketODataClient ticketODataClient = new C4CTicketODataClient(host, user);
		try {
			String ticketsJson = ticketODataClient.readTicketsAsJSON();
			String json = "{\"user\":\"" + user + "\", \"value\":";
			json += ticketsJson;
			json += "}";
			return json;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fallbackHardCodedResponse(user);
		}

	}

	private String fallbackHardCodedResponse(String user) {
		String json = "";
		json += "{\"user\":\"" + user + "\", \"value\":[";
		json += "]}";
		return json;
	}

	private String getBody(HttpServletRequest request) throws IOException {

		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

}
