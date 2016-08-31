package com.ibm.tfb.ext.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDetailDboSet;
import com.ibm.dpft.engine.core.exception.DPFTDataFormatException;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileReader;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;

public class ECBOpnDataFileReader extends DPFTFileReader {

	public ECBOpnDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
		super(dir, resFileDataLayoutDbo, chal_name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void write2TargetTable(String timestamp) throws DPFTRuntimeException {
		if(layout == null){
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0033E");
		}
		
		ResFileDataLayoutDetailDboSet layout_detail = layout.getLayoutDetail();
		if(layout_detail == null){
			Object[] params = {layout.getString("data_layout_id")};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0031E", params);
		}
		
		DPFTDboSet targetSet = layout.getTargetTableDboSet();
		HashMap<String, String> f_col_2_tgt_col_map = layout.getFileColumns2TargetColumnsMapping();
		DPFTDboSet resIn35DaysSet = get35DaysResDataSet();
		
		for(HashMap<String, String> rowdata: read_data){
			if(rowdata.get("RES_CODE").equals(TFBConstants.ECB_RES_CODE_FAILED)
					|| rowdata.get("RES_CODE").equals(TFBConstants.ECB_RES_CODE_OPENED)){
				DPFTDboSet resSet = resIn35DaysSet;
				resSet.unfilter();
				resSet.filter("resv1", rowdata.get("SYS_NO"));
				if(!resSet.isEmpty()){
					resSet.orderby("res_date", DPFTDboSet.TYPE_DATETIME);
					int start_index = getStartIndex(resSet, rowdata.get("RES_DATE"), rowdata.get("RES_CODE").equals(TFBConstants.ECB_RES_CODE_FAILED));
					if(start_index == -1){
						//No match records
						//Add to RSP_MAIN_DEL
						DPFTDbo new_data = targetSet.add();
						new_data.setValue("chal_name", chal_name);
						new_data.setValue("process_time", timestamp);
						for(String col: rowdata.keySet()){
							if(f_col_2_tgt_col_map.get(col) == null)
								continue;
							new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
						}
					}else{
						String dateString = resSet.getDbo(start_index).getString("res_date");
						for(int i = start_index; i < resSet.count(); i++){
							if(dateString.equals(resSet.getDbo(i).getString("res_date"))){
								DPFTDbo sub_new_data = targetSet.add();
								sub_new_data.setValue("chal_name", chal_name);
								sub_new_data.setValue("process_time", timestamp);
								for(String col: rowdata.keySet()){
									if(f_col_2_tgt_col_map.get(col) == null)
										continue;
									sub_new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
								}
								sub_new_data.setValue("treatment_code", resSet.getDbo(i).getString("treatment_code"));
								sub_new_data.setValue("customer_id", resSet.getDbo(i).getString("customer_id"));
								continue;
							}
							break;
						}
					}
				}else{
					//No match records
					//Add to RSP_MAIN_DEL
					DPFTDbo new_data = targetSet.add();
					new_data.setValue("chal_name", chal_name);
					new_data.setValue("process_time", timestamp);
					for(String col: rowdata.keySet()){
						if(f_col_2_tgt_col_map.get(col) == null)
							continue;
						new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
					}
				}
				
			}else{
				DPFTDbo new_data = targetSet.add();
				new_data.setValue("chal_name", chal_name);
				new_data.setValue("process_time", timestamp);
				for(String col: rowdata.keySet()){
					if(f_col_2_tgt_col_map.get(col) == null)
						continue;
					new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
					
					DPFTDbo oEcb = getRelatedOutboundData(new_data);
					if(oEcb == null)
						continue;
					
					new_data.setValue("customer_id"   , oEcb.getString("customer_id"));
				}
			}
		}
		targetSet.save();
		targetSet.close();
	}

	private int getStartIndex(DPFTDboSet resSet, String res_date, boolean isFailedCode) throws DPFTRuntimeException {
		SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DFPT_DATETIME_FORMAT);
		for(int i = 0; i < resSet.count(); i++){
			try {
				Date file_res_date = sdf.parse(res_date);
				if(resSet.getDbo(i).getDate("res_date").before(file_res_date)
				|| resSet.getDbo(i).getDate("res_date").equals(file_res_date)){
					if(isFailedCode){
						//check if within file_res_date - 10 days
						Calendar cal = Calendar.getInstance();
						cal.setTime(file_res_date);
						cal.add(Calendar.DAY_OF_MONTH, -10);
						Date file_res_date_minus_10_day = cal.getTime();
						if(resSet.getDbo(i).getDate("res_date").after(file_res_date_minus_10_day) 
						|| resSet.getDbo(i).getDate("res_date").equals(file_res_date_minus_10_day)){
							return i;
						}else{
							return -1;
						}
					}
					return i;
				}
			} catch (ParseException e) {
				Object[] params = {res_date};
				throw new DPFTDataFormatException("SYSTEM", "DPFT0024E", params, e);
			}
		}
		return -1;
	}

	private DPFTDboSet get35DaysResDataSet() throws DPFTRuntimeException {
		StringBuilder query = new StringBuilder();
		query.append("to_date(res_date, 'YYYYMMDDHH24miss') >= sysdate-35 and ");
		query.append("chal_name='").append(chal_name).append("'")
			 .append(" and res_code='").append(TFBConstants.ECB_RES_CODE_DISPATCH).append("'");
		DPFTDboSet rtnSet = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
		.getDboSet("RSP_MAIN", query.toString());
		rtnSet.load();
		rtnSet.close();
		return rtnSet;
	}

	private DPFTDbo getRelatedOutboundData(DPFTDbo new_data) throws DPFTRuntimeException {
		DPFTDboSet set = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
							.getDboSet("O_" + chal_name, "treatment_code='" + new_data.getString("treatment_code") + "' and sys_no='" + new_data.getString("resv1") + "'");
		set.load();
		set.close();
		if(!set.isEmpty())
			return set.getDbo(0);
		return null;
	}
}
