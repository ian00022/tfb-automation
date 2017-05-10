package com.ibm.tfb.ext.util;

import java.util.HashMap;
import java.util.Hashtable;

import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDetailDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileReader;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class LTMResDataFileReader extends DPFTFileReader {
	private static final String lz_type = "SFA";

	public LTMResDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
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
		
		DPFTDboSet keySet = (DPFTDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet("RSP_MAIN_LTM_TEMP");
		keySet.load();
		Hashtable<Object, Object> ht = new Hashtable<Object, Object>();
		for(int i=0;i<keySet.count();i++){
			DPFTDbo keyObj = keySet.getDbo(i);
			ht.put(keyObj.getColumnValue("TREATMENT_CODE"), keyObj.getColumnValue("CHAL_NAME"));
		}
		DPFTDboSet targetSet = layout.getTargetTableDboSet();
		DPFTDboSet targetLzSet = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet("RSP_MAIN_LZ", "rownum <= 10");
		HashMap<String, String> f_col_2_tgt_col_map = layout.getFileColumns2TargetColumnsMapping();
		for(HashMap<String, String> rowdata: read_data){
			DPFTDbo new_data = null;
			if(rowdata.get("TREATMENT_CODE") != null && rowdata.get("TREATMENT_CODE").indexOf(lz_type) != -1)
				new_data = targetLzSet.add();
			else 
				new_data = targetSet.add();
			new_data.setValue("process_time", timestamp);
			for(String col: rowdata.keySet()){
				if(f_col_2_tgt_col_map.get(col) == null)
					continue;
				new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
				if(f_col_2_tgt_col_map.get(col).matches("TREATMENT_CODE"))
					new_data.setValue("chal_name", ht.get(rowdata.get(col)));
			}
		}

		targetLzSet.save();
		targetSet.save();
		targetLzSet.close();
		targetSet.close();

	}
}
