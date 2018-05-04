package com.ibm.tfb.ext.util;

import java.util.HashMap;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDetailDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;
import com.ibm.tfb.ext.dbo.MKTDMCustomerContactDboSet;

public class LZHIFResDataFileReader extends LZResDataFileReader {
	public LZHIFResDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
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
		String data_datetime = getDD();
		targetSet.reset("data_datetime='" + data_datetime + "'");
		if(!targetSet.isEmpty()){
			targetSet.deleteAll();
		}
		
		HashMap<String, String> f_col_2_tgt_col_map = layout.getFileColumns2TargetColumnsMapping();
		for(HashMap<String, String> rowdata: read_data){
			DPFTDbo new_data = targetSet.add();
			for(String col: rowdata.keySet()){
				if(f_col_2_tgt_col_map.get(col) == null)
					continue;
				new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
			}
			new_data.setValue("data_datetime", data_datetime);
			
			if(new_data.getString("custid") == null)
				continue;
					
			new_data.setValue("mobile_bnk" , getRelatedMobileData(new_data));
		}
		targetSet.save();
		generateLZIbndCtrlRecord(targetSet, data_datetime);
		targetSet.close();
	}


	private String getDD() {
		StringBuilder sb = new StringBuilder();
		sb.append(parseScheduledDate(fname)).append(parseScheduledTime(fname));
		return sb.toString();
	}
	
	private String getRelatedMobileData(DPFTDbo new_data) throws DPFTRuntimeException {

		MKTDMCustomerContactDboSet set = (MKTDMCustomerContactDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet(DPFTEngine.getSystemProperties("mktdb.tbl.cust.cont"), 
				"cust_id ='" + new_data.getString("custid") + 
				"' and cont_cd = '" + TFBConstants.MKTDM_CONT_CD_MOBILE_1 + "'");
		set.load();
		
		String mobile = null;
		if(!set.isEmpty()){
			mobile = set.getMobileByBizType(new_data.getString("custid"), TFBConstants.MKTDM_CONT_BIZTYPE_BNK);
		}
		set.close();
		
		return mobile;
	}
}
