package com.ibm.dpft.engine.core.dbo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.exception.DPFTDboException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTDatetimeComparator;
import com.ibm.dpft.engine.core.util.DPFTDboComparator;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTNumberComparator;
import com.ibm.dpft.engine.core.util.DPFTStringComparator;

public class DPFTDboSet {
	public static final int TYPE_DATETIME = 1;
	public static final int TYPE_NUMBER = 2;
	public static final int TYPE_CHAR = 3;
	public static final int ORDER_DESC = 0;
	public static final int ORDER_ASC = 1;
	
	
	private DPFTConnector connector = null;
	private String dboname = null; 
	public String getDboname() {
		return dboname;
	}

	private List<DPFTDbo> dboset = null;
	private List<DPFTDbo> _dboset = null;
	private HashMap<String, String> filter = new HashMap<String, String>();
	private List<String> insert_cols = null;
	private String whereclause = null;
	private boolean tobeLoaded = false;
	private DPFTDbo parent = null;
	private boolean tobeRefreshed = true;
	
	public DPFTDboSet(DPFTConnector conn, String tbname) throws DPFTRuntimeException {
		// TODO Auto-generated constructor stub
		this(conn, tbname, "");
	}

	public DPFTDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		// TODO Auto-generated constructor stub
		this.connector = conn;
		this.dboname = tbname;
		if(whereclause == null)
			whereclause = "";
		this.setWhere(whereclause);
		
		initialize(whereclause);
	}

	public DPFTDboSet(DPFTConnector conn, List<HashMap<String, Object>> data) {
		dboset = convert2DboInstances(data);
		this.connector = conn;
	}

	void initialize(String whereclause) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		try {
			connector.setQuery(dboname, whereclause);
			tobeLoaded = true;
		} catch (SQLException e) {
			throw new DPFTDboException("SYSTEM", "DPFT0025E", e);
		}
		
	}

	public void setBoolean(int i, boolean val) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		try {
			connector.getCurrentStatement().setBoolean(i, val);
		} catch (SQLException e) {
			throw new DPFTDboException("SYSTEM", "DPFT0025E", e);
		}
	}

	public void load() throws DPFTRuntimeException {
		if(!tobeLoaded)
			return;
		try {
			dboset = convert2DboInstances(connector.retrieveDataFromCurrentStmt());
			tobeLoaded = false;
		} catch (Exception e) {
			throw new DPFTDboException("SYSTEM", "DPFT0026E", e);
		}
	}
	
	public void clear() {
		if(dboset != null)
			dboset.clear();
	}
	
	List<DPFTDbo> convert2DboInstances(List<HashMap<String, Object>> data) {
		// TODO Auto-generated method stub
		List<DPFTDbo> ds = new ArrayList<DPFTDbo>();
		for(HashMap<String, Object> d: data){
			DPFTDbo dbo = getDboInstance(this.dboname, d);
			ds.add(dbo);
		}
		return ds;
	}

	protected DPFTDbo getDboInstance(String name, HashMap<String, Object> d) {
		return new DPFTDbo(name, d, this);
	}
	
	protected DPFTDbo getNewDboInstance(String name, HashMap<String, Object> new_data) {
		// TODO Auto-generated method stub
		DPFTDbo dbo = getDboInstance(name, new_data);
		dbo.setTobeAdded(true);
		if(dboset == null)
			dboset = new ArrayList<DPFTDbo>();
		dboset.add(dbo);
		return dbo;
	}

	public boolean isEmpty() throws DPFTRuntimeException{
		if(tobeLoaded)
			load();
		return (dboset == null || dboset.isEmpty())?true:false;
	}
	
	public int count() throws DPFTRuntimeException{
		if(tobeLoaded)
			load();
		if(dboset == null)
			return 0;
		return dboset.size();
	}
	
	public DPFTDbo getDbo(int i) throws DPFTRuntimeException{
		if(tobeLoaded)
			load();
		if(i < dboset.size())
			return dboset.get(i);
		else
			return null;
	}

	public void reset() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		initialize(whereclause);
	}
	
	public void reset(String where) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		initialize(where);
	}

	public String getWhere() {
		return whereclause;
	}

	public void setWhere(String whereclause) {
		this.whereclause = whereclause;
	}

	public void close() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		try {
			connector.close();
		} catch (Exception e) {
			throw new DPFTDboException("SYSTEM", "COMM0003E", e);
		}
//		connector = null;
	}

	public void save() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		if(tobeSaved() || tobeAdded() || tobeDeleted()){
			removeResidualsDbo();
			doSave();
			if(tobeRefreshed)
				refresh();
		}
		
	}
	
	protected void removeResidualsDbo() {
		Iterator<DPFTDbo> ir = dboset.iterator();
		while(ir.hasNext()){
			DPFTDbo dbo = ir.next();
			if(dbo.tobeAdded() && dbo.tobeDeleted())
				ir.remove();
		}
	}

	public void deleteAll() {
		for(DPFTDbo dbo: dboset){
			dbo.delete();
		}
	}

	public boolean tobeAdded() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		for(int i = 0; i < this.count(); i++){
			if(this.getDbo(i).tobeAdded())
				return true;
		}
		return false;
	}
	
	public boolean tobeDeleted() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		for(int i = 0; i < this.count(); i++){
			if(this.getDbo(i).tobeDeleted())
				return true;
		}
		return false;
	}

	public void refresh() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		initialize(getDbosWhereStringByPID(dboset));
		load();
		DPFTLogger.info(this, "Refreshed Current DboSet from DB...");
	}

	private String getDbosWhereStringByPID(List<DPFTDbo> dbos) {
		if(dbos.size() > 50 || dbos.isEmpty())
			return whereclause;
		
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		StringBuilder sb1 = new StringBuilder();
		sb.append("pid in (");
		for(DPFTDbo dbo : dbos){
			sb1.append("'").append(dbo.getPrimaryKeyValue()).append("',");
		}
		sb.append(sb1.substring(0, sb1.length()-1)).append(")");
		return sb.toString();
	}

	private void doSave() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		List<DPFTDbo> updateDbolist = new ArrayList<DPFTDbo>();
		List<DPFTDbo> newDbolist    = new ArrayList<DPFTDbo>();
		List<DPFTDbo> delDbolist    = new ArrayList<DPFTDbo>();
		for(int i = 0; i < this.count(); i++){
			DPFTDbo dbo = this.getDbo(i);
			if(dbo.tobeSaved())
				updateDbolist.add(dbo);
			if(dbo.tobeAdded())
				newDbolist.add(dbo);
			if(dbo.tobeDeleted())
				delDbolist.add(dbo);
		}	
		try {
			//Delete Records if any
			if(!delDbolist.isEmpty()){
				connector.setDelete(delDbolist);
				connector.doDelete();
				connector.commit();
			}
			
			//For Insert/Update case, do insert first (in order to get new pid)
			if(!newDbolist.isEmpty()){
				connector.setInsert(newDbolist);
				connector.doInsert();
				connector.commit();
				if(tobeRefreshed){
					reset();
					load();
				}
			}
			
			//Do update
			if(!updateDbolist.isEmpty()){
				connector.setUpdate(updateDbolist);
				connector.doUpdate();
				connector.commit();
			}
		} catch (SQLException e) {
			throw new DPFTDboException("SYSTEM", "DPFT0027E", e);
		}
	}

	public boolean tobeSaved() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		for(int i = 0; i < this.count(); i++){
			if(this.getDbo(i).tobeSaved())
				return true;
		}
		return false;
	}
	
	public DPFTDbo add() throws DPFTRuntimeException {
		if(tobeLoaded)
			load();
		if(insert_cols == null)
			insert_cols = new ArrayList<String>();
		HashMap<String, Object> new_data = new HashMap<String, Object>();
		return getNewDboInstance(this.dboname, new_data);
	}

	protected DPFTConnector getDBConnector() {
		return connector;
	}

	public void setParent(DPFTDbo parent_dbo) {
		parent = parent_dbo;
	}

	public DPFTDbo getParent(){
		return parent;
	}
	
	public void orderby(String col, int type) throws DPFTRuntimeException {
		orderby(col, type, ORDER_DESC);
	}

	public void orderby(String col, int type, int order) throws DPFTRuntimeException {
		if(tobeLoaded)
			load();
		
		if(type == TYPE_DATETIME)
			Collections.sort(dboset, new DPFTDatetimeComparator(col, order));
		if(type == TYPE_NUMBER)
			Collections.sort(dboset, new DPFTNumberComparator(col, order));
		if(type == TYPE_CHAR)
			Collections.sort(dboset, new DPFTStringComparator(col, order));
	}
	
	public void orderby(DPFTDboComparator comparator) throws DPFTRuntimeException {
		if(tobeLoaded)
			load();
		Collections.sort(dboset, comparator);
	}

	public void filter(String column, String value) {
		//Filter records where column value = value
		filter.put(column, value);
		_applyfilter();
	}
	
	public void unfilter(String column) {
		if(filter.containsKey(column))
			filter.remove(column);
		_applyfilter();
	}
	
	public void unfilter(){
		filter.clear();
		_applyfilter();
	}

	private void _applyfilter() {
		if(_dboset == null)
			_dboset = dboset;
		
		if(filter.isEmpty()){
			dboset = _dboset;
			_dboset = null;
			return;
		}
		
		ArrayList<DPFTDbo> filtered_dboset = new ArrayList<DPFTDbo>();
		for(DPFTDbo dbo: _dboset){
			int match_count = 0;
			for(String col: filter.keySet()){
				if(dbo.getColumnValue(col) instanceof String){
					if(dbo.getString(col).equals(filter.get(col)))match_count++;
				}
			}
			if(match_count == filter.size())
				filtered_dboset.add(dbo);
		}
		dboset = filtered_dboset;
	}
	
	public List<String> getInsertCols(){
		return insert_cols;
	}

	public void regInsertCol(String colname) {
		if(insert_cols == null)
			return;
		if(!insert_cols.contains(colname))
			insert_cols.add(colname);
	}

	public boolean isTobeRefresh() {
		return tobeRefreshed;
	}

	public void setRefresh(boolean tobeRefreshed) {
		this.tobeRefreshed = tobeRefreshed;
	}

	public HashMap<String, String> getKeyValueMap(String key_col, String value_col) throws DPFTRuntimeException {
		HashMap<String, String> map = new HashMap<String, String>();
		for(int i = 0; i < count(); i++){
			map.put(this.getDbo(i).getString(key_col), this.getDbo(i).getString(value_col));
		}
		return map;
	}
}
