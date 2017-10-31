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

public class SSMIntResDataFileReader extends DPFTFileReader {

	public SSMIntResDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
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
		
		DPFTDboSet keySet = (DPFTDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet("RSP_MAIN_SMS_TEMP");
		keySet.load();
		Hashtable<Object, Object> ht = new Hashtable<Object, Object>();
		for(int i=0;i<keySet.count();i++){
			DPFTDbo keyObj = keySet.getDbo(i);
			ht.put(keyObj.getColumnValue("TREATMENT_CODE"), keyObj.getColumnValue("CHAL_NAME"));
		}
		
		DPFTDboSet targetSet = layout.getTargetTableDboSet();
		HashMap<String, String> f_col_2_tgt_col_map = layout.getFileColumns2TargetColumnsMapping();
		for(HashMap<String, String> rowdata: read_data){
			DPFTDbo new_data = targetSet.add();
			new_data.setValue("process_time", timestamp);
			new_data.setValue("res_code"      , "I");
			for(String col: rowdata.keySet()){
				if(f_col_2_tgt_col_map.get(col) == null)
					continue;
				new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
			}
			
			//Find recent same DestNo. in O_SSM Table, set treatment_code, customer_id info
			DPFTDbo oSsm = getRelatedOutboundData(new_data);
			if(oSsm == null){
				oSsm = getRelatedLZSOutboundData(new_data);
			}
			if(oSsm == null){
				oSsm = getRelatedQSMOutboundData(new_data);
			}
			if(oSsm == null){
				continue;
			}
			new_data.setValue("treatment_code", oSsm.getString("treatment_code"));
			new_data.setValue("customer_id"   , oSsm.getString("customer_id"));
			new_data.setValue("chal_name", ht.get(oSsm.getString("treatment_code")));
		}
		targetSet.save();
		targetSet.close();
	}

	private DPFTDbo getRelatedLZSOutboundData(DPFTDbo new_data) throws DPFTRuntimeException {
		DPFTDboSet set = new_data.getDboSet("O_LZS", "destno='" + new_data.getString("resv1") + "' and isinteractive='Y'");
		set.orderby("timestamp", DPFTDboSet.TYPE_DATETIME);
		set.close();
		return set.getDbo(0);
	}

	private DPFTDbo getRelatedOutboundData(DPFTDbo new_data) throws DPFTRuntimeException {
		DPFTDboSet set = new_data.getDboSet("O_SSM", "destno='" + new_data.getString("resv1") + "' and isinteractive='Y'");
		set.orderby("timestamp", DPFTDboSet.TYPE_DATETIME);
		set.close();
		return set.getDbo(0);
	}

	private DPFTDbo getRelatedQSMOutboundData(DPFTDbo new_data) throws DPFTRuntimeException {
		DPFTDboSet set = new_data.getDboSet("O_QSM", "destno='" + new_data.getString("resv1") + "' and isinteractive='Y'");
		set.orderby("timestamp", DPFTDboSet.TYPE_DATETIME);
		set.close();
		return set.getDbo(0);
	}
	
}
