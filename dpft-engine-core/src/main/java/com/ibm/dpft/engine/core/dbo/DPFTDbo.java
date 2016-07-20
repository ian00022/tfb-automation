package com.ibm.dpft.engine.core.dbo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.ibm.dpft.engine.core.exception.DPFTDataFormatException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class DPFTDbo {
	private DPFTDboSet thisDboSet = null;
	private HashMap<String, Object> rowData = null;
	private HashMap<String, Object> changed_values = new HashMap<String, Object>();
	//Virtual data = non-persistence data
	private HashMap<String, Object> vData = new HashMap<String, Object>();
	private boolean tobeSaved = false;
	private String dboname = null;
	private boolean tobeAdded = false;
	private boolean tobeDeleted = false;
	
	public DPFTDbo(String dboname, HashMap<String, Object> data, DPFTDboSet thisDboSet) {
		// TODO Auto-generated constructor stub
		this.rowData  = data;
		this.dboname  = dboname;
		this.setThisDboSet(thisDboSet);
	}
	
	public String getString(String colname){
		return ((String) getColumnValue(colname) != null)? (String) getColumnValue(colname):(String) getVirtualData(colname);
	}

	public Date getDate(String colname) throws DPFTRuntimeException{
		Object value = (getColumnValue(colname) != null)?getColumnValue(colname):getVirtualData(colname);
		if(value instanceof String){
			String sv = (String)value;
			SimpleDateFormat sdf = new SimpleDateFormat(DPFTUtil.getSupportedFormat(sv));
			try {
				return sdf.parse(sv);
			} catch (ParseException e) {
				Object[] params = {sv};
				throw new DPFTDataFormatException("SYSTEM", "DPFT0024E", params, e);
			}
		}
		return (Date)value;
	}
	
	private Object getVirtualData(String colname) {
		String col = colname.toUpperCase();
		return vData.get(col);
	}
	
	public Object getColumnValue(String colname) {
		// TODO Auto-generated method stub
		String col = colname.toUpperCase();
		return (((changed_values.containsKey(col)))?changed_values.get(col):rowData.get(col));
	}

	public void setValue(String colname, Object value) {
		// TODO Auto-generated method stub
		String col = colname.toUpperCase();	
		if(!tobeAdded){
			//row data has value for the column
			if(rowData.get(col) instanceof Date){
				//Date type
				if(!((Date)rowData.get(col)).equals(value)){
					changed_values.put(col, value);
					tobeSaved = true;
				}else{
					if(changed_values.containsKey(col))
						changed_values.remove(col);
				}
			}else if(rowData.get(col) instanceof String){
				//String type
				if(!((String)rowData.get(col)).equals(value)){
					changed_values.put(col, value);
					tobeSaved = true;
				}else{
					if(changed_values.containsKey(col))
						changed_values.remove(col);
				}
			}else{
				//null
				if(rowData.get(col) == null && value != null){
					if(rowData.containsKey(col)){
						changed_values.put(col, value);
					}else{
						setNewValue(col, value);
					}
					tobeSaved = true;
				}else if(rowData.get(col) != null && value == null){
					changed_values.put(col, value);
					tobeSaved = true;
				}
			}
		}else{
			setNewValue(col, value);
		}
	}
	
	public void setValue(DPFTDbo dbo, String[] ignore_cols) {
		// TODO Auto-generated method stub
		String[] columns = dbo.getColumns();
		for(String col: columns){
			if(col.equalsIgnoreCase("pid"))
				continue;
			boolean isIgnore = false;
			for(String ig_col : ignore_cols){
				if(col.equalsIgnoreCase(ig_col)){
					isIgnore = true;
					break;
				}
			}
			if(isIgnore)
				continue;
			this.setValue(col, dbo.getColumnValue(col));
		}
	}
	
	public void setValue(DPFTDbo dbo) {
		// TODO Auto-generated method stub
		String[] columns = dbo.getColumns();
		for(String col: columns){
			if(col.equalsIgnoreCase("pid"))
				continue;
			this.setValue(col, dbo.getColumnValue(col));
		}
	}

	private void setNewValue(String colname, Object value) {
		// TODO Auto-generated method stub
		rowData.put(colname, value);
	}

	public boolean tobeSaved() {
		if(changed_values.isEmpty())
			tobeSaved = false;
		return tobeSaved;
	}

	public String getDboName() {
		return dboname;
	}

	public String[] getColumns() {
		// TODO Auto-generated method stub
		return rowData.keySet().toArray(new String[rowData.keySet().size()]);
	}
	
	public Integer getPrimaryKeyValue() {
		// TODO Auto-generated method stub
		return Integer.valueOf((String) rowData.get("PID"));
	}

	public boolean tobeAdded() {
		return tobeAdded;
	}

	public void setTobeAdded(boolean tobeAdded) {
		this.tobeAdded = tobeAdded;
	}

	public void delete() {
		tobeDeleted = true;
	}
	
	public boolean tobeDeleted() {
		return tobeDeleted;
	}

	public boolean isNull(String col) {
		if(this.getColumnValue(col) == null)
			return true;
		if(this.getColumnValue(col) instanceof String){
			return ((String)this.getColumnValue(col)).isEmpty();
		}
		return false;
	}

	public void setVirtualData(String col, String value) {
		// TODO Auto-generated method stub
		String colname = col.toUpperCase();
		vData.put(colname, value);
	}

	public DPFTDboSet getThisDboSet() {
		return thisDboSet;
	}

	public void setThisDboSet(DPFTDboSet thisDboSet) {
		this.thisDboSet = thisDboSet;
	}
	
	public DPFTDboSet getDboSet(String tbname, String whereclause) throws DPFTRuntimeException {
		return thisDboSet.getDBConnector().getDboSet(tbname, whereclause, this);
	}
	
	public DPFTDboSet getDboSet(String tbname) throws DPFTRuntimeException {
		return thisDboSet.getDBConnector().getDboSet(tbname, "", this);
	}

}
