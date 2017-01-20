package com.ibm.tfb.ext.util;

import java.util.ArrayList;

import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDbo;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.tfb.ext.common.TFBConstants;
import com.ibm.tfb.ext.common.TFBUtil;

public class LZHLResDataFileReader extends LZResDataFileReader {

	public LZHLResDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
		super(dir, resFileDataLayoutDbo, chal_name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void generateLZIbndCtrlRecord(DPFTDboSet targetSet, String data_datetime) throws DPFTRuntimeException {
		ArrayList<String> bu_code_list = new ArrayList<String>();
		for(int i = 0; i < targetSet.count(); i++){
			if(!targetSet.getDbo(i).isNull("bu_code")){
				if(!bu_code_list.contains(targetSet.getDbo(i).getString("bu_code")))
					bu_code_list.add(targetSet.getDbo(i).getString("bu_code"));
			}
		}
		if(bu_code_list.isEmpty()){
			TFBUtil.generateLZIbndCtrlRecord(layout.getString("target_tbl"), data_datetime, chal_name + "_" + TFBConstants.SFA_LZ_LEADTYPE_HL_BUCODE_H);
			TFBUtil.generateLZIbndCtrlRecord(layout.getString("target_tbl"), data_datetime, chal_name + "_" + TFBConstants.SFA_LZ_LEADTYPE_HL_BUCODE_T);
		}else{
			for(String bu_code: bu_code_list){
				TFBUtil.generateLZIbndCtrlRecord(layout.getString("target_tbl"), data_datetime, chal_name + "_" + bu_code);
			}
		}
	}
}
