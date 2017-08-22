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
import com.ibm.dpft.engine.core.dbo.DPFTResMainDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDetailDboSet;
import com.ibm.dpft.engine.core.exception.DPFTDataFormatException;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileReader;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class EBNResDataFileReader extends DPFTFileReader {
	private static final String MSG_TYPE_01 = "01";
	private static final String MSG_TYPE_02 = "02";
	private static final String MSG_TYPE_03 = "03";
	private static final String STR_TCODE = "TCode=";
	private static final String STR_TYPE_02_MENUID = "CHM02";
	private static final String STR_TYPE_03_MENUID = "CPS02";
	private static final String STR_TYPE_03_1_MENUID = "PSL01";
	private static final String RES_CODE_CONTACT = "D";
	

	public EBNResDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
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
		
		DPFTResMainDboSet targetSet = (DPFTResMainDboSet) layout.getTargetTableDboSet();
		targetSet.setNeedAdd2DelTableFlg(false);
		
		HashMap<String, String> f_col_2_tgt_col_map = layout.getFileColumns2TargetColumnsMapping();
		DPFTDboSet oEbnSet = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet("O_EBN", "msg_typ in ('02', '03') and process_status='Output' AND OFFR_EFFECTIVEDATE > " + getEffectiveDate(new Date(), -60));
		HashMap<String, HashMap<String, String>> type02 = new HashMap<String, HashMap<String, String>>();
		HashMap<String, HashMap<String, String>> type03 = new HashMap<String, HashMap<String, String>>();
		for(HashMap<String, String> rowdata: read_data){
			//skip unrelated ebn response data
			String type = getMsgType(rowdata);
			if(type == null)
				continue;
			
			//EBN logic
			if(type.equals(MSG_TYPE_01)){
				DPFTDbo new_data = targetSet.add();
				new_data.setValue("chal_name", chal_name);
				new_data.setValue("process_time", timestamp);
				for(String col: rowdata.keySet()){
					if(f_col_2_tgt_col_map.get(col) == null)
						continue;
					new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
				}
				new_data.setValue("treatment_code", getTreatmentCode(rowdata.get("QUERY_STRING")));
				new_data.setValue("res_code", RES_CODE_CONTACT);
			}else if(type.equals(MSG_TYPE_02) ){
				type02.put(rowdata.get("COMPANY_UID"), rowdata);
			}else if(type.equals(MSG_TYPE_03)){
				type03.put(rowdata.get("COMPANY_UID"), rowdata);
			}
		}
		
		for(int i = 0; i < oEbnSet.count(); i++){
			DPFTDbo oEbn = oEbnSet.getDbo(i);
			
			HashMap<String, String> rowdata = null;
			if(oEbn.getString("msg_typ").equals(MSG_TYPE_02)){
				rowdata = type02.get(oEbn.getString("customer_id"));
			}else if(oEbn.getString("msg_typ").equals(MSG_TYPE_03)){
				rowdata = type03.get(oEbn.getString("customer_id"));
			}
			if(rowdata != null) {
				if(isEffectiveCampaign(oEbn, rowdata.get("ACCESS_TIME"))){
					DPFTDbo data = targetSet.add();
					data.setValue("chal_name", chal_name);
					data.setValue("process_time", timestamp);
					data.setValue("treatment_code", oEbn.getString("treatment_code"));
					data.setValue("customer_id",oEbn.getString("customer_id"));
					data.setValue("res_code", RES_CODE_CONTACT);
					data.setValue("res_date", rowdata.get("ACCESS_TIME"));
				}
			}
		}

		oEbnSet.close();
		targetSet.save();
		targetSet.close();
	}

	private String getTreatmentCode(String qstring) {
		String[] pairs = qstring.split(GlobalConstants.FILE_DELIMETER_AND);
		for(String pair: pairs){
			if(pair.indexOf(STR_TCODE) == -1)
				continue;
			return pair.substring(pair.indexOf("=") + 1);
		}
		return null;
	}

	private boolean isEffectiveCampaign(DPFTDbo oEbn, String datestring) throws DPFTRuntimeException {
		SimpleDateFormat sdf = new SimpleDateFormat(DPFTUtil.getSupportedFormat(datestring));
		Date sdate = oEbn.getDate("start_date");
		Date edate = oEbn.getDate("end_date");
		try {
			Date tdate = sdf.parse(datestring);
			return tdate.after(sdate) && tdate.before(edate);
		} catch (ParseException e) {
			Object[] params = {datestring};
			throw new DPFTDataFormatException("SYSTEM", "DPFT0024E", params, e);
		}
	}

	private String getMsgType(HashMap<String, String> rowdata) {
		String qstring = rowdata.get("QUERY_STRING");
		String menuid = rowdata.get("MENU_ID");
		if(isMsgType_01(qstring)){
			return MSG_TYPE_01;
		}else if(isMsgType_02(menuid)){
			return MSG_TYPE_02;
		}else if(isMsgType_03(menuid)){
			return MSG_TYPE_03;
		}
		return null;
	}

	private boolean isMsgType_03(String menuid) {
		if(menuid == null)
			return false;
		return menuid.equals(STR_TYPE_03_MENUID) || menuid.equals(STR_TYPE_03_1_MENUID);
	}

	private boolean isMsgType_02(String menuid) {
		if(menuid == null)
			return false;
		return menuid.equals(STR_TYPE_02_MENUID);
	}

	private boolean isMsgType_01(String qstring) {
		if(qstring == null)
			return false;
		return qstring.indexOf(STR_TCODE) != -1;
	}

	private static String getEffectiveDate(Date dt, int diff) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(dt);
        ca.add(Calendar.DATE, diff);
        return new SimpleDateFormat(GlobalConstants.DFPT_DATE_FORMAT).format(ca.getTime());
    }
}
