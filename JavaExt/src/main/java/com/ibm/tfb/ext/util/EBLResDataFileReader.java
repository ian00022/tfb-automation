package com.ibm.tfb.ext.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDetailDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileReader;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;

public class EBLResDataFileReader extends DPFTFileReader {

	public EBLResDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
		super(dir, resFileDataLayoutDbo, chal_name);
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
		ArrayList<Integer> index_without_dispatch = new ArrayList<Integer>();
		int index = 0;
		for(HashMap<String, String> rowdata: read_data){
			if(rowdata.get("RES_CODE").equals(TFBConstants.EBL_RES_CODE_DISPATCH)){
				DPFTDbo new_data = targetSet.add();
				new_data.setValue("chal_name", chal_name);
				new_data.setValue("process_time", timestamp);
				for(String col: rowdata.keySet()){
					if(f_col_2_tgt_col_map.get(col) == null)
						continue;
					new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
				}
				
				//add to dispatch reference dataset
				DPFTDbo new_data_dispatch = resIn35DaysSet.add();
				new_data_dispatch.setValue("chal_name", chal_name);
				new_data_dispatch.setValue("process_time", timestamp);
				for(String col: rowdata.keySet()){
					if(f_col_2_tgt_col_map.get(col) == null)
						continue;
					new_data_dispatch.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
				}
			}else{
				index_without_dispatch.add(index);
			}
			index++;
		}
		for(Integer i: index_without_dispatch){
			HashMap<String, String> rowdata = read_data.get(i);
			if(rowdata.get("RES_CODE").equals(TFBConstants.EBL_RES_CODE_OPENED)){
				DPFTDbo new_data = targetSet.add();
				new_data.setValue("chal_name", chal_name);
				new_data.setValue("process_time", timestamp);
				for(String col: rowdata.keySet()){
					if(f_col_2_tgt_col_map.get(col) == null)
						continue;
					new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
				}
			}else{
				//refer to dispatch data set
				String bill_month = rowdata.get("BILL_MONTH");
				String customer_id = rowdata.get("CUSTOMER_ID");
				resIn35DaysSet.unfilter();
				resIn35DaysSet.filter("CUSTOMER_ID", customer_id);
				resIn35DaysSet.filter("RESV6", bill_month);
				if(!resIn35DaysSet.isEmpty()){
					//has matched dispatch record in rsp_main
					for(int idx = 0; idx < resIn35DaysSet.count(); idx++){
						DPFTDbo new_data = targetSet.add();
						new_data.setValue("chal_name", chal_name);
						new_data.setValue("process_time", timestamp);
						for(String col: rowdata.keySet()){
							if(f_col_2_tgt_col_map.get(col) == null)
								continue;
							new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
						}
						new_data.setValue("treatment_code", resIn35DaysSet.getDbo(idx).getString("treatment_code"));
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
			}
		}
		targetSet.save();
		targetSet.close();
	}
	
	private DPFTDboSet get35DaysResDataSet() throws DPFTRuntimeException {
		StringBuilder query = new StringBuilder();
		query.append("to_date(res_date, 'YYYYMMDDHH24miss') >= sysdate-35 and ");
		query.append("chal_name='").append(chal_name).append("'")
			 .append(" and res_code='").append(TFBConstants.EBL_RES_CODE_DISPATCH).append("'");
		DPFTDboSet rtnSet = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
		.getDboSet("RSP_MAIN", query.toString());
		rtnSet.load();
		rtnSet.close();
		return rtnSet;
	}
}
