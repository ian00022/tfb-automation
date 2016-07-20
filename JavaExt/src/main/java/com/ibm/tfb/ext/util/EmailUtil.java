package com.ibm.tfb.ext.util;

import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Purpose: Send alerts for the records in mailbox which have PROCESS_STS = 'I'.
 * Usage: Step1. Initialize a EmailUtil object. Say "sa".
 *        Step2. Send alerts for all 'I' records with calling sa.send_alerts() method. 
 */
public class EmailUtil {
	private static Properties smtpProperties = null;
	private static Properties dbProperties = null;
	private static String smtp_host = null;
	private static String smtp_sender = null;
	private static String db_host = null;
	private static String db_port = null;
	private static String db_svc = null;
	private static String db_user = null;
	private static String db_pass = null;
	private static String db_url = null;

/*
 * EmailUtil constructor.
 */
	public EmailUtil() {
		try {
			get_smtpProp();
			get_dbProp();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

/*
 * send_alerts: EmailUtil method
 */
	public void send_alerts() throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		String query = "select PID, RECEIVER, MAIL_TITLE, MAIL_CONTENT from DM_MKT.MAILBOX where PROCESS_STS = 'I' order by PID";
		db_url = "jdbc:oracle:thin:@//" + db_host + ":" + db_port + "/" + db_svc;

		try {
			conn = DriverManager.getConnection(db_url, db_user, db_pass);
	        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	        ResultSet rs = stmt.executeQuery(query);
	        int rowCount = rs.last() ? rs.getRow() : 0;

	        if (rowCount == 0) {
	        	System.out.println("No mailbox records to be processed.");
	        	return;
	        }
// Rewind the result set to the first one.
	        rs.beforeFirst();
// Process each initialized element in mailbox one by one.
	        while (rs.next()) {
	        	long mail_pid = rs.getLong("PID");
	        	String receiver = rs.getString("RECEIVER");
	            String mail_title = rs.getString("MAIL_TITLE");
	            String mail_content = rs.getString("MAIL_CONTENT");

	            sendingMail(mail_pid, receiver, mail_title, mail_content);
	        }
		} catch (Exception e) {
			System.err.println(e);
		} finally { //finally block used to close resources
			try{
		    	if(stmt!=null)
		        	conn.close();
			} catch(SQLException se) {
				se.printStackTrace();
			}
			try{
				if(conn!=null)
					conn.close();
			} catch(SQLException se){
				se.printStackTrace();
			}
		} //end finally try
	}

/*
 * sendingMail: Send mails for the specified item in mailbox.
 */
	private void sendingMail(long pid, String receiver, String mail_title, String mail_content) throws Exception
	{
		Connection conn = null;
//		String sql = "select NVL(g.EMAIL, m.RECEIVER) ADDR from DM_MKT.MAILBOX m left join DM_MKT.MAILGROUP g on (m.RECEIVER = g.GROUP_NAME) where m.PID = " + Long.toString(pid);
		String sql = "select case when g.id is null then g.email else u.email end ADDR from DM_MKT.MAILBOX m left join DM_MKT.MAILGROUP g on (m.RECEIVER = g.GROUP_NAME) left join UNICAMPT.USM_USER u on (g.id = u.id) where m.PID = " + Long.toString(pid); 
		Statement stmt = null;

		// Get email addresses from MAILGROUP.
		try {
		// ########## Start to send email using SMTP without authentication
		// Sender's email ID needs to be mentioned
//			String from = "unica.ap.bank@fbt.com";

		// Get system properties
			Properties properties = System.getProperties();

		// Setup mail server
			properties.setProperty("mail.smtp.host", smtp_host);

		// Get the default Session object.
			Session session = Session.getDefaultInstance(properties);

		// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

		// Set From: header field of the header.
			message.setFrom(new InternetAddress(smtp_sender));

		// Set To: header field of the header.
			if (matchMailPattern(receiver)) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
			}
			else {
				conn = DriverManager.getConnection(db_url, db_user, db_pass);
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					String to = "";
					to = rs.getString("ADDR");
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
				}
			}

		// Set Subject: header field
		    message.setSubject(mail_title);

		// Now set the actual message
		    message.setText(mail_content);

		// Send message
		    Transport.send(message);
// Update this mailbox record to 'F' (Finished).
		    updMailSts(pid, "F", "Message is sent successfully.");
		    System.out.println("Sent message successfully....");
		}
		catch (Exception e) {
			System.err.println(e);
			updMailSts(pid, "E", e.toString());
		}
/*		catch (SQLException e) {
			System.err.println(e);
			updMailSts(pid, "E", e.toString());
//			ret = false;
		}
		catch (MessagingException mex) {
			mex.printStackTrace();
			updMailSts(pid, "E", mex.toString());
		}
*/
		finally {
			try{
		    	if(stmt!=null)
		        	stmt.close();
			} catch(SQLException se) {
				se.printStackTrace();
			}
			try{
				if(conn!=null)
					conn.close();
			} catch(SQLException se){
				se.printStackTrace();
			}
		}
//		return ret;
	}

/*
 * updMailSts: Update process_sts in mailbox table.
 */
	private void updMailSts(long pid, String ProcessSts, String ProcessMsg) throws SQLException
	{
		Connection conn = null;
        String updSQL = "update dm_mkt.mailbox set PROCESS_STS = '" + ProcessSts + "', " +
        				"EXCEPT_MSG = '" + ProcessMsg + "', " +
        				"PROCESS_TIME = SYSDATE " +
        				"where PID = " + Long.toString(pid);
        Statement stmt = null;

        try {
        	conn = DriverManager.getConnection(db_url, db_user, db_pass);
        	stmt = conn.createStatement();
        	stmt.executeQuery(updSQL);
        } catch(SQLException e) {
        	System.err.println(e);
        } finally {
			try{
		    	if(stmt!=null)
		        	stmt.close();
			} catch(SQLException se) {
				se.printStackTrace();
			}
			try{
				if(conn!=null)
					conn.close();
			} catch(SQLException se){
				se.printStackTrace();
			}
        }
	}

/*
 * get_smtpProp: Get SMTP server properties.
 */
	private void get_smtpProp() throws Exception {
		smtpProperties = new Properties();
		String smtpPropFileName = "notification.properties";
		smtpProperties = new Properties();
		try {
//			InputStream in = getClass().getResourceAsStream(smtpPropFileName);
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(smtpPropFileName);
			smtpProperties.load(in);
			in.close();
			smtp_host = smtpProperties.getProperty("SMTP.Host.IP");
			smtp_sender = smtpProperties.getProperty("SMTP.Sender.Address");
		} catch (IOException e) {
				System.err.println("Fail to read properties file: " + smtpPropFileName + e);
		}
	}
	public String get_SMTP_HOST() {
		return smtp_host;
	}
	public String get_SMTP_SENDER() {
		return smtp_sender;
	}

/*
 * get_dbProp: Get DB properties to retrieve related mail alert information.
 */
	private void get_dbProp() throws Exception {
		String dbPropFileName = "config.properties";
		String cfgName = "MKTDM";
		dbProperties = new Properties();

		try {
//			InputStream in = getClass().getResourceAsStream(dbPropFileName);
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(dbPropFileName);
			dbProperties.load(in);
			in.close();
			db_host = dbProperties.getProperty(cfgName + ".db.host");
			db_port = dbProperties.getProperty(cfgName + ".db.port");
			db_svc = dbProperties.getProperty(cfgName + ".db.servicename");
			db_user = dbProperties.getProperty(cfgName + ".db.user");
			db_pass = dbProperties.getProperty(cfgName + ".db.password");
		} catch (IOException e) {
			System.err.println("Fail to read properties file: " + dbPropFileName + e);
		}
	}
	public String get_db_host() {
		return db_host;
	}

	private boolean matchMailPattern(String input) {
		String mailPattern = "(\\w*)\\@fbt\\.com";

// Create a Pattern object
	      Pattern r = Pattern.compile(mailPattern);

// Now create matcher object.
	      Matcher m = r.matcher(input);
	      return (m.find());
	}
}
