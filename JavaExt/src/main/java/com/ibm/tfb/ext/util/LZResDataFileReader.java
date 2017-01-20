package com.ibm.tfb.ext.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDetailDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileReader;
import com.ibm.tfb.ext.common.TFBUtil;

public class LZResDataFileReader extends DPFTFileReader {
	private final static Pattern schedule_time_pattern = Pattern.compile("(20\\d{2}|19\\d{2})(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])(0[0-9]|1[0-9]|2[0-3])([0-5][0-9])");
	private final static Pattern schedule_time_pattern2 = Pattern.compile("[_.](0[0-9]|1[0-9]|2[0-3])([0-5][0-9])[_.]");

	public LZResDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
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
		}
		targetSet.save();
		generateLZIbndCtrlRecord(targetSet, data_datetime);
		targetSet.close();
	}

	protected void generateLZIbndCtrlRecord(DPFTDboSet targetSet, String data_datetime) throws DPFTRuntimeException {
		TFBUtil.generateLZIbndCtrlRecord(layout.getString("target_tbl"), data_datetime, chal_name);
	}

	private String getDD() {
		SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DFPT_DATE_FORMAT);
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(new Date())).append(parseScheduledTime(fname));
		return sb.toString();
	}
	
	public String parseScheduledTime(String name) {
		//test Pattern 1
		Matcher m = schedule_time_pattern.matcher(name);
		String time = null;
		if(m.find()){
			time = m.group(4) + m.group(5);
		}
		if(time == null){
			//test Pattern 2
			m = schedule_time_pattern2.matcher(name);
			if(m.find()){
				time = m.group(1) + m.group(2);
			}
		}
		return time;
	}
}
