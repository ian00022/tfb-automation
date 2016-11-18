package com.ibm.dpft.engine.core.connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public class DPFTStatement {
	private StringBuilder stmt_builder = null;
	private String stmtype = null;
	DPFTConnector conn = null;
	private PreparedStatement pstmt = null;
	private String[] update_cols = null;
	private String[] insert_cols = null;
	private List<DPFTDbo> update_dbos = null;
	private List<DPFTDbo> insert_dbos = null;
	private List<DPFTDbo> delete_dbos = null;

	public DPFTStatement(String stmtype, DPFTConnector conn) {
		// TODO Auto-generated constructor stub
		if(stmtype.equalsIgnoreCase(GlobalConstants.DB_STMT_TYPE_QUERY))
			initQueryStatement();
		if(stmtype.equalsIgnoreCase(GlobalConstants.DB_STMT_TYPE_UPDATE))
			initUpdateStatement();
		if(stmtype.equalsIgnoreCase(GlobalConstants.DB_STMT_TYPE_INSERT))
			initInsertStatement();
		if(stmtype.equalsIgnoreCase(GlobalConstants.DB_STMT_TYPE_DELETE))
			initDeleteStatement();
		if(stmtype.equalsIgnoreCase(GlobalConstants.DB_STMT_TYPE_SQL))
			initSQLStatement();
		
		this.stmtype = stmtype;
		this.conn  = conn;
	}

	private void initSQLStatement() {
		stmt_builder = new StringBuilder();
	}

	private void initDeleteStatement() {
		// TODO Auto-generated method stub
		stmt_builder = new StringBuilder();
		stmt_builder.append("delete from ");
	}

	private void initInsertStatement() {
		// TODO Auto-generated method stub
		stmt_builder = new StringBuilder();
		stmt_builder.append("insert into ");
	}

	private void initUpdateStatement() {
		// TODO Auto-generated method stub
		stmt_builder = new StringBuilder();
		stmt_builder.append("update ");
	}

	private void initQueryStatement() {
		// TODO Auto-generated method stub
		stmt_builder = new StringBuilder();
		stmt_builder.append("select ");
	}

	public void prepareStatement() throws SQLException {
		// TODO Auto-generated method stub
		if(stmtype.equalsIgnoreCase(GlobalConstants.DB_STMT_TYPE_UPDATE)
		|| stmtype.equalsIgnoreCase(GlobalConstants.DB_STMT_TYPE_INSERT)
		|| stmtype.equalsIgnoreCase(GlobalConstants.DB_STMT_TYPE_DELETE)){
			conn.getDBConnectionInstance().setAutoCommit(false);
		}
		
		pstmt  = conn.getDBConnectionInstance().prepareStatement(stmt_builder.toString());
	}

	public DPFTStatement selectAll() {
		// TODO Auto-generated method stub
		if(stmt_builder.indexOf("select") != 0)
			return this;
		stmt_builder.append("* ");
		return this;
	}

	public DPFTStatement from(String tbname) {
		// TODO Auto-generated method stub
		stmt_builder.append("from ").append(tbname).append(" ");
		return this;
	}

	public DPFTStatement where(String whereclause) {
		/*where statement : col1=? and col2=? and col3 like ?...*/
		// TODO Auto-generated method stub
		if(whereclause.isEmpty())
			return this;
		
		stmt_builder.append("where ").append(whereclause);
		return this;
	}
	
	public DPFTStatement update(List<DPFTDbo> updateDbolist) {
		// TODO Auto-generated method stub
		DPFTDbo template  = updateDbolist.get(0);
		stmt_builder.append(template.getDboName()).append(" set ");
		stmt_builder.append(appendColumnValueString(template)).append(" ");
		update_dbos = updateDbolist;
		return this;
	}
	
	public DPFTStatement insert(List<DPFTDbo> newDbolist) {
		// TODO Auto-generated method stub
		DPFTDbo template  = newDbolist.get(0);
		stmt_builder.append(template.getDboName()).append(" (");
		StringBuilder sb_cols = new StringBuilder();
		StringBuilder sb_vals = new StringBuilder();
		List<String> col_list = template.getThisDboSet().getInsertCols();
		insert_cols = col_list.toArray(new String[col_list.size()]);
		for(String col: insert_cols){
			sb_cols.append(buildStmtColname(col)).append(",");
			sb_vals.append("?").append(",");
		}
		stmt_builder.append(sb_cols.substring(0, sb_cols.length()-1)).append(") values (")
		.append(sb_vals.substring(0, sb_vals.length()-1)).append(")");
		insert_dbos = newDbolist;
		return this;
	}
	
	private String buildStmtColname(String col) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		if(col.contains(GlobalConstants.Hyphen))
			sb.append("\"").append(col.toUpperCase()).append("\"");
		else
			sb.append(col.toUpperCase());
		return sb.toString();
	}

	public DPFTStatement delete(List<DPFTDbo> delDbolist) {
		// TODO Auto-generated method stub
		DPFTDbo template  = delDbolist.get(0);
		stmt_builder.append(template.getDboName()).append(" ");
		delete_dbos = delDbolist;
		return this;
	}

	private String appendColumnValueString(DPFTDbo dbo) {
		// TODO Auto-generated method stub
		String[] cols = dbo.getColumns();
		update_cols = new String[cols.length-1];
		StringBuilder sb1 = new StringBuilder();
		int i = 0;
		for(String col: cols){
			if(col.equalsIgnoreCase("PID"))
				continue;
			sb1.append(buildStmtColname(col)).append("=?,");
			update_cols[i] = col;
			i++;
		}
		return sb1.toString().substring(0, sb1.toString().length() - 1);
	}

	public void setBoolean(int i, boolean val) throws SQLException {
		// TODO Auto-generated method stub
		if(pstmt == null)
			return;
		pstmt.setBoolean(i, val);
	}
	
	public void setString(int i, String val) throws SQLException {
		// TODO Auto-generated method stub
		if(pstmt == null)
			return;
		pstmt.setString(i, val);
	}
	
	public void setDate(int i, Date date) throws SQLException {
		// TODO Auto-generated method stub
		if(pstmt == null)
			return;
		pstmt.setDate(i, new java.sql.Date(date.getTime()));
	}
	
	public void setInteger(int i, int val) throws SQLException {
		// TODO Auto-generated method stub
		if(pstmt == null)
			return;
		pstmt.setInt(i, val);
	}


	public List<HashMap<String, Object>> doQuery() throws SQLException {
		// TODO Auto-generated method stub
		if(pstmt == null)
			return null;
		DPFTLogger.debug(this, "Execute Query SQL :" + stmt_builder.toString());
		List<HashMap<String, Object>> dboset = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
//			DPFTLogger.debug(this, "Fetch next query record");
			HashMap<String, Object> data = new HashMap<String, Object>();
			int col_cnt = rs.getMetaData().getColumnCount();
			for(int i = 0; i < col_cnt; i++){
				int index = i+1;
				String colName = rs.getMetaData().getColumnName(index);
				int type = rs.getMetaData().getColumnType(index);
				if(type == Types.TIMESTAMP){
					if(rs.getTimestamp(index) == null)
						data.put(colName, null);
					else
						data.put(colName, new Date(rs.getTimestamp(index).getTime()));
				}else if(type == Types.DATE){
					if(rs.getDate(index) == null)
						data.put(colName, null);
					else
						data.put(colName, new Date(rs.getDate(index).getTime()));
				}else{
					data.put(colName, rs.getString(index));
				}
//				DPFTLogger.debug(this, "ColName :" + colName + " Col Value = " + rs.getString(index));
			}
			dboset.add(data);
		}
		rs.close();
		pstmt.close();
		return dboset;
	}
	
	public void doUpdate() throws SQLException, DPFTRuntimeException {
		// TODO Auto-generated method stub
		if(pstmt == null || update_dbos.isEmpty())
			return;
		DPFTLogger.debug(this, "Execute Update SQL :" + stmt_builder.toString());
		prepareBatch(update_dbos, update_cols, true);
		pstmt.executeBatch();
		DPFTLogger.info(this, update_dbos.get(0).getDboName() + " successfully updated " + update_dbos.size() + " records...");
	}
	
	public void doInsert() throws SQLException, DPFTRuntimeException {
		if(pstmt == null || insert_dbos.isEmpty())
			return;
		DPFTLogger.debug(this, "Execute Insert SQL :" + stmt_builder.toString());
		prepareBatch(insert_dbos, insert_cols, false);
		pstmt.executeBatch();
		DPFTLogger.info(this, insert_dbos.get(0).getDboName() + " successfully inserted " + insert_dbos.size() + " records...");
	}
	
	public void doDelete() throws SQLException, DPFTRuntimeException {
		// TODO Auto-generated method stub
		if(pstmt == null || delete_dbos.isEmpty())
			return;
		DPFTLogger.debug(this, "Execute Delete SQL :" + stmt_builder.toString());
		prepareBatch(delete_dbos, null, true);
		pstmt.executeBatch();
		DPFTLogger.info(this, delete_dbos.get(0).getDboName() + " successfully deleted " + delete_dbos.size() + " records...");
	}
	
	public void doSQL() throws SQLException {
		if(pstmt == null)
			return;
		DPFTLogger.debug(this, "Execute SQL :" + stmt_builder.toString());
		pstmt.execute();
	}

	private void prepareBatch(List<DPFTDbo> dbos, String[] cols, boolean has_pid) throws SQLException, DPFTRuntimeException {
		// TODO Auto-generated method stub
		for(DPFTDbo dbo: dbos){
			int col_length = 0;
			if(cols != null){
				for(int i = 0; i < cols.length; i++){
					int index = i+1;
					if(dbo.getColumnValue(cols[i]) == null){
						setString(index, "");
					}
					if(dbo.getColumnValue(cols[i]) instanceof String){
						setString(index, dbo.getString(cols[i]));
					}else if(dbo.getColumnValue(cols[i]) instanceof Date){
						setDate(index, dbo.getDate(cols[i]));
					}
				}
				col_length = cols.length;
			}
			if(has_pid){
				/*set whereclause pid*/
				setInteger(col_length + 1, dbo.getPrimaryKeyValue());
			}
			pstmt.addBatch();
		}	
	}

	public void close() throws SQLException {
		// TODO Auto-generated method stub
		if(pstmt == null)
			return;
		if(!pstmt.isClosed())
			pstmt.close();
	}

	public String getStatementType() {
		return stmtype;
	}
	
	public void setSQL(String script) {
		stmt_builder.append(script);
	}
}
