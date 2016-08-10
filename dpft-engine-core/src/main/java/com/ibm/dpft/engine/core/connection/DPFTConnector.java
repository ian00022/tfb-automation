package com.ibm.dpft.engine.core.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.exception.DPFTConnectionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public class DPFTConnector {
	private static DPFTConnector sys_conn = null;
	private static ArrayList<Connection> conn_pool = new ArrayList<Connection>();
	private Connection conn = null;
	private DPFTConfig db_cfg = null;
	private DPFTStatement stmt = null;

	public DPFTConnector(Connection conn, DPFTConfig cfg) {
		// TODO Auto-generated constructor stub
		this.conn = conn;
		db_cfg = cfg;
		
		add2ConnectionPool(conn);
		logPoolSummary();
	}
	
	private void add2ConnectionPool(Connection connection) {
		synchronized(conn_pool){
			conn_pool.add(connection);
		}
	}
	private void logPoolSummary() {
		synchronized(conn_pool){
			StringBuilder sb = new StringBuilder();
			sb.append("Connection POOL Summary:\n")
			  .append("Total JDBC Connection Instance = " + conn_pool.size() + " \n")
			  .append("Total Open Connection = " + getActiveConnectionCount() + " \n")
			  .append("Total Closed Connection = " + getClosedConnectionCount() + " \n");
			
			DPFTLogger.debug(this, "\n" + sb.toString());
		}
	}
	private int getClosedConnectionCount(){
		int count = 0;
		for(Connection conn: conn_pool){
			try {
				if(conn.isClosed())
					count++;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return count;
	}
	
	private int getActiveConnectionCount(){
		int count = 0;
		for(Connection conn: conn_pool){
			try {
				if(!conn.isClosed())
					count++;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return count;
	}
	
	public DPFTDboSet getDboSet(String tbname, String whereclause, DPFTDbo parent_dbo) throws DPFTRuntimeException {
		DPFTDboSet dboset = getDboSetInstance(tbname, whereclause);
		dboset.setParent(parent_dbo);
		return dboset;
	}

	public DPFTDboSet getDboSet(String tbname) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getDboSetInstance(tbname);
	}
	

	public DPFTDboSet getDboSet(String tbname, String whereclause) throws DPFTRuntimeException {
		return getDboSetInstance(tbname, whereclause);
	}

	
	DPFTStatement generateSQLStmt(String type) {
		// TODO Auto-generated method stub
		return new DPFTStatement(type, this);
	}

	public void setQuery(String tbname, String whereclause) throws SQLException {
		// TODO Auto-generated method stub
		setQuery(null, tbname, whereclause);
	}

	public void setQuery(String[] attrs, String tbname, String whereclause) throws SQLException {
		// TODO Auto-generated method stub
		stmt  = this.generateSQLStmt(GlobalConstants.DB_STMT_TYPE_QUERY);
		if(attrs == null){
			/*Select All Records*/
			stmt.selectAll().from(tbname).where(whereclause);
		}
		
		stmt.prepareStatement();
	}
	
	public DPFTConfig getDBConfig(){
		return db_cfg;
	}
	
	public Connection getDBConnectionInstance(){
		return conn;
	}

	public DPFTStatement getCurrentStatement() {
		// TODO Auto-generated method stub
		return stmt;
	}

	public List<HashMap<String, Object>> retrieveDataFromCurrentStmt() throws SQLException {
		// TODO Auto-generated method stub
		if(stmt == null)
			return null;
		return stmt.doQuery();
		
	}

	private DPFTDboSet getDboSetInstance(String tbname, String whereclause) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		try {
			synchronized(sys_conn){
				DPFTLogger.debug(this, "Access to System Connector (Synchronized Area)...");
				sys_conn.setQuery("DPFT_DBO_DEF", "active=? and tbname=?");
				sys_conn.getCurrentStatement().setBoolean(1, true);
				sys_conn.getCurrentStatement().setString(2, tbname);
				List<HashMap<String, Object>> infolist = sys_conn.retrieveDataFromCurrentStmt();
				for(HashMap<String, Object> info: infolist){
					String classname = (String) info.get("CLASSNAME");
					DPFTLogger.debug(this, "DboSetInstance class :" + classname);
					return (DPFTDboSet) Class.forName(classname)
							.getConstructor(DPFTConnector.class, String.class, String.class)
							.newInstance(DPFTConnectionFactory.initDPFTConnector(db_cfg, false), tbname, whereclause);
				}
				return new DPFTDboSet(DPFTConnectionFactory.initDPFTConnector(db_cfg, false), tbname, whereclause);
			}			
		} catch (Exception e) {
			throw new DPFTConnectionException("SYSTEM", "COMM0005E", e);
		}
	}

	private DPFTDboSet getDboSetInstance(String tbname) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getDboSetInstance(tbname, "");
	}

	public void close() throws SQLException {
		// TODO Auto-generated method stub
		if(stmt != null)
			stmt.close();
		if(conn != null)
			conn.close();
		logPoolSummary();
	}

	public void setUpdate(List<DPFTDbo> updateDbolist) throws SQLException {
		// TODO Auto-generated method stub
		if(updateDbolist.isEmpty()){
			DPFTLogger.debug(this, "No Records need update...");
			return;
		}
		
		stmt = this.generateSQLStmt(GlobalConstants.DB_STMT_TYPE_UPDATE);
		stmt.update(updateDbolist).where("pid=?");
		stmt.prepareStatement();
		
	}
	
	public void setInsert(List<DPFTDbo> newDbolist) throws SQLException {
		// TODO Auto-generated method stub
		if(newDbolist.isEmpty()){
			DPFTLogger.debug(this, "No Records need insert...");
			return;
		}
		
		stmt = this.generateSQLStmt(GlobalConstants.DB_STMT_TYPE_INSERT);
		stmt.insert(newDbolist);
		stmt.prepareStatement();
		
	}
	
	public void setDelete(List<DPFTDbo> delDbolist) throws SQLException {
		// TODO Auto-generated method stub
		if(delDbolist.isEmpty()){
			DPFTLogger.debug(this, "No Records need delete...");
			return;
		}
		
		stmt = this.generateSQLStmt(GlobalConstants.DB_STMT_TYPE_DELETE);
		stmt.delete(delDbolist).where("pid=?");
		stmt.prepareStatement();
		
	}

	public void commit() throws SQLException {
		// TODO Auto-generated method stub
		conn.commit();
	}

	public static DPFTConnector getSystemDBConnectorInstance() {
		return sys_conn;
	}

	public static void setSystemDBConnectorInstance(DPFTConnector sys_conn) {
		DPFTConnector.sys_conn = sys_conn;
	}

	public void doUpdate() throws SQLException, DPFTRuntimeException {
		// TODO Auto-generated method stub
		if(stmt.getStatementType().equals(GlobalConstants.DB_STMT_TYPE_UPDATE))
			stmt.doUpdate();
	}

	public void doInsert() throws SQLException, DPFTRuntimeException {
		// TODO Auto-generated method stub
		if(stmt.getStatementType().equals(GlobalConstants.DB_STMT_TYPE_INSERT)){
			stmt.doInsert();
		}
	}

	public void doDelete() throws SQLException, DPFTRuntimeException {
		// TODO Auto-generated method stub
		if(stmt.getStatementType().equals(GlobalConstants.DB_STMT_TYPE_DELETE)){
			stmt.doDelete();
		}
	}
	
	public DPFTDboSet execSQL(String script) throws SQLException {
		stmt = this.generateSQLStmt(GlobalConstants.DB_STMT_TYPE_SQL);
		stmt.setSQL(script);
		stmt.prepareStatement();
		return new DPFTDboSet(this, this.retrieveDataFromCurrentStmt());
	}
	
	public void truncate(String tablename) throws SQLException {
		stmt = this.generateSQLStmt(GlobalConstants.DB_STMT_TYPE_SQL);
		stmt.setSQL("truncate table " + tablename);
		stmt.prepareStatement();
		stmt.doSQL();
	}

	public static void clearClosedConnection() {
		synchronized(conn_pool){
			Iterator<Connection> ir = conn_pool.iterator();
			while(ir.hasNext()){
				Connection con = ir.next();
				try {
					if(con.isClosed())
						ir.remove();
				} catch (SQLException e) {
				}
			}
		}
	}
	
}
