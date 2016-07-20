package com.ibm.tfb.ext.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.dbo.TFBMailGroupDbo;
import com.ibm.tfb.ext.dbo.TFBMailGroupDboSet;
import com.ibm.tfb.ext.dbo.TFBUsageCodeDbo;
import com.ibm.tfb.ext.dbo.TFBUsageCodeDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundControlDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundControlDboSet;
import com.ibm.dpft.engine.core.exception.DPFTConnectionException;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class TFBUtil {
	private static final Object lock = new Object();
	private static final Object lock2 = new Object();
	private static final Object lock3 = new Object();
	
	public static String getCustomerSelectINString(DPFTDboSet set, String colname) throws DPFTRuntimeException {
		if(set.isEmpty())
			return "";
		
		String fk_string = DPFTUtil.getFKQueryString(set.getDbo(0));
		StringBuilder sb = new StringBuilder();
		sb.append("select customer_id from ")
						.append("D_").append(set.getDbo(0).getString("chal_name"))
		  .append(" where ").append(fk_string);
		return sb.toString();
	}

	public static String getDestNameString(DPFTDbo dbo) {
		return dbo.getString("camp_code") + "||" + dbo.getString("treatment_code") + "||" + dbo.getString("customer_id");
	}

	public static void setSSMHeaderProperties(DPFTDbo new_dbo, DPFTDbo data) {
		new_dbo.setValue("destname", TFBUtil.getDestNameString(data));
		if(data.getString("isinteractive").equalsIgnoreCase("y")){
//			new_dbo.setValue("username", DPFTEngine.getSystemProperties("TFB_SSM_I_USRNAME"));
			new_dbo.setValue("password", DPFTEngine.getSystemProperties("TFB_SSM_I_USRPWD"));
			new_dbo.setValue("groupid" , DPFTEngine.getSystemProperties("TFB_SSM_I_GROUP_ID"));
		}else{
//			new_dbo.setValue("username", DPFTEngine.getSystemProperties("TFB_SSM_USRNAME"));
			new_dbo.setValue("password", DPFTEngine.getSystemProperties("TFB_SSM_USRPWD"));
			new_dbo.setValue("groupid" , DPFTEngine.getSystemProperties("TFB_SSM_GROUP_ID"));
		}
		new_dbo.setValue("priority", DPFTEngine.getSystemProperties("TFB_SSM_PRIORITY"));
		new_dbo.setValue("mid"     , DPFTEngine.getSystemProperties("TFB_SSM_MID"));
		new_dbo.setValue("objectid", DPFTEngine.getSystemProperties("TFB_SSM_OBJID"));
	}

	public static DPFTConfig getMKTDMConfig() {
		return DPFTUtil.getDBConfig("MKTDM");
	}

	public static void setESMHeaderProperties(DPFTDbo new_dbo, DPFTDbo data) {
		new_dbo.setValue("destname", TFBUtil.getDestNameString(data));
		if(data.getString("isinteractive").equalsIgnoreCase("y")){
//			new_dbo.setValue("username", DPFTEngine.getSystemProperties("TFB_ESM_I_USRNAME"));
			new_dbo.setValue("password", DPFTEngine.getSystemProperties("TFB_ESM_I_USRPWD"));
			new_dbo.setValue("groupid" , DPFTEngine.getSystemProperties("TFB_ESM_I_GROUP_ID"));
		}else{
//			new_dbo.setValue("username", DPFTEngine.getSystemProperties("TFB_ESM_USRNAME"));
			new_dbo.setValue("password", DPFTEngine.getSystemProperties("TFB_ESM_USRPWD"));
			new_dbo.setValue("groupid" , DPFTEngine.getSystemProperties("TFB_ESM_GROUP_ID"));
		}
		new_dbo.setValue("priority", DPFTEngine.getSystemProperties("TFB_ESM_PRIORITY"));
		new_dbo.setValue("mid"     , DPFTEngine.getSystemProperties("TFB_ESM_MID"));
		new_dbo.setValue("objectid", DPFTEngine.getSystemProperties("TFB_ESM_OBJID"));
	}

	public static String getBdmNextSeq() throws DPFTRuntimeException {
		synchronized(lock){
			DPFTDboSet seqSet = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet("DPFT_SEQ", "active=1 and id='" + TFBConstants.BDM_BARCODE_SEQ_ID + "'");
			seqSet.load();
			if(seqSet.isEmpty()){
				Object[] params = {TFBConstants.BDM_BARCODE_SEQ_ID};
				throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0009E", params);
			}
			DPFTDbo seq = seqSet.getDbo(0);
			int current_seq = Integer.valueOf(seq.getString("value"));
			int max_seq = Integer.valueOf(seq.getString("max_value"));
			int st_seq = Integer.valueOf(seq.getString("start_value"));
			String type = seq.getString("type");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date upd_date = seq.getDate("upd_date");
			Date sys_date = new Date();
			if(!sdf.format(upd_date).equals(sdf.format(sys_date))
					&& seq.getString("is_daily_reset").equalsIgnoreCase("y")){
				/*reset sequence*/
				seq.setValue("value", String.valueOf(st_seq));
				seq.setValue("type", TFBConstants.BDM_BARCODE_SEQ_TYPE_S);
				seq.setValue("upd_date", sys_date);
				current_seq = st_seq;
				type = TFBConstants.BDM_BARCODE_SEQ_TYPE_S;
			}else{
				current_seq++;
				if(current_seq > max_seq && type.equals(TFBConstants.BDM_BARCODE_SEQ_TYPE_S)){
					/*reset sequence change type to P*/
					seq.setValue("value", String.valueOf(st_seq));
					seq.setValue("type", TFBConstants.BDM_BARCODE_SEQ_TYPE_P);
					seq.setValue("upd_date", sys_date);
					type = TFBConstants.BDM_BARCODE_SEQ_TYPE_P;
					current_seq = st_seq;
				}else{
					if(current_seq > max_seq){
						Object[] params = {TFBConstants.BDM_BARCODE_SEQ_ID, max_seq};
						throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0010E", params);
					}
					seq.setValue("value", String.valueOf(current_seq));
					seq.setValue("upd_date", sys_date);
				}
			}
			seqSet.save();
			seqSet.close();
			return type + String.format("%06d", current_seq);
		}
	}

	public static String getNextActionPlanID() throws DPFTRuntimeException {
		synchronized(lock2){
			DPFTDboSet seqSet = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet("DPFT_SEQ", "active=1 and id='" + TFBConstants.WMS_APLN_SEQ_ID + "'");
			seqSet.load();
			if(seqSet.isEmpty()){
				Object[] params = {TFBConstants.WMS_APLN_SEQ_ID};
				throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0009E", params);
			}
			DPFTDbo seq = seqSet.getDbo(0);
			int current_seq = Integer.valueOf(seq.getString("value"));
			int max_seq = Integer.valueOf(seq.getString("max_value"));
			int st_seq = Integer.valueOf(seq.getString("start_value"));
			String type = seq.getString("type");
			Date sys_date = new Date();
			current_seq++;
			if(current_seq > max_seq){
				DPFTLogger.info(TFBUtil.class.getName(), "Info: Sequence exceed limit Reset Value...");
				current_seq = st_seq;
			}
			seq.setValue("value", String.valueOf(current_seq));
			seq.setValue("upd_date", sys_date);
			seqSet.save();
			seqSet.close();
			return type + String.format("%08d", current_seq);
		}
	}
	
	public static String getROCYearMonthString(Date date) {
		// TODO Auto-generated method stub
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(cal.get(Calendar.YEAR) - 1911)).append(String.format("%02d", cal.get(Calendar.MONTH)+1));
		return sb.toString();
	}

	public static void generateObndCtrlRecord(DPFTConnector connector, DPFTDboSet oSet, ArrayList<String> cell_code_list, String chal_name, boolean hasHObnd) throws DPFTRuntimeException {
		DPFTDbo dbo = oSet.getDbo(0);
		String qString = DPFTUtil.getFKQueryString(dbo);
		if(qString == null){
			throw new DPFTConnectionException("SYSTEM", "DPFT0011E");
		}
		if(hasHObnd){
			DPFTOutboundControlDboSet hObndSet = (DPFTOutboundControlDboSet) connector.getDboSet("H_OUTBOUND", qString);
			hObndSet.load();
			if(hObndSet.count() > 0){
				DPFTLogger.info(TFBUtil.class.getName(), "Records exist in H_OUTBOUND...Delete All Records...");
				hObndSet.deleteAll();
			}
			for(int i = 0; i < cell_code_list.size(); i++){
				DPFTOutboundControlDbo new_hobnd = (DPFTOutboundControlDbo) hObndSet.add();
				new_hobnd.setBasicInfo(dbo, "O_" + chal_name, chal_name);
				new_hobnd.setValue("cell_code", cell_code_list.get(i));
				new_hobnd.setTotalDataQuantity(oSet, cell_code_list.get(i));
			}	
			hObndSet.save();
			hObndSet.close();
		}
		
		for(int i = 0; i < cell_code_list.size(); i++){
			int total_exclude_gk = 0, total_exclude = 0, total = 0;
			String cell_code = cell_code_list.get(i);
			for(int j = 0; j < oSet.count(); j++){
				if(oSet.getDbo(j).getString("cell_code").equals(cell_code) 
						&& oSet.getDbo(j).getString("process_status").equals(GlobalConstants.O_DATA_GK_EXCLUDE)){
					total_exclude_gk++;
				}
				if(oSet.getDbo(j).getString("cell_code").equals(cell_code) 
						&& oSet.getDbo(j).getString("process_status").equals(GlobalConstants.O_DATA_EXCLUDE)){
					total_exclude++;
				}
				if(oSet.getDbo(j).getString("cell_code").equals(cell_code))
					total++;
			}
			int total_out = total - total_exclude_gk - total_exclude;
			Object[] params = {dbo.getString("camp_code"), cell_code, String.valueOf(total), String.valueOf(total_exclude_gk), String.valueOf(total_exclude), String.valueOf(total_out)};
			DPFTUtil.pushNotification(
					DPFTUtil.getCampaignOwnerEmail(dbo.getString("camp_code")), 
					new DPFTMessage("CUSTOM", "TFB00009I", params)
			);
		}
		
		DPFTInboundControlDboSet hIbndSet = (DPFTInboundControlDboSet) connector.getDboSet("H_INBOUND", qString);
		hIbndSet.taskComplete();
		hIbndSet.close();
	}
	
	public static String buildHeaderString(String[] cols, DPFTDbo data) {
		StringBuilder sb = new StringBuilder();
		for(String col: cols){
			Object value = data.getColumnValue(col);
			if(value != null){
				if(value instanceof Date){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					sb.append(sdf.format((Date)value));
				}else{
					sb.append((String)value);
				}
			}else{
				sb.append("");
			}
			sb.append(GlobalConstants.FILE_DELIMETER_AND);
		}
		return sb.substring(0, sb.length()-1);
	}

	public static void processUsageCode(DPFTDboSet oSet, String chal_name) throws DPFTRuntimeException {
		DPFTDbo dbo = oSet.getDbo(0);
		String qString = DPFTUtil.getFKQueryString(dbo);
		if(qString == null){
			throw new DPFTConnectionException("SYSTEM", "DPFT0011E");
		}
		
		TFBUsageCodeDboSet oUsgSet = (TFBUsageCodeDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet("O_USAGECODE", qString);
		oUsgSet.load();
		if(oUsgSet.count() > 0){
			DPFTLogger.info(TFBUtil.class.getName(), "Records exist in O_USAGECODE...Delete All Records...");
			oUsgSet.deleteAll();
		}
		for(int i = 0; i < oSet.count(); i++){
			if(oSet.getDbo(i).isNull("usagecode"))
				continue;
			TFBUsageCodeDbo new_usgcode = (TFBUsageCodeDbo) oUsgSet.add();
			new_usgcode.setInitialData(oSet.getDbo(i), chal_name);
		}
		oUsgSet.save();
		oUsgSet.close();
	}

	public static String getNextBranchNo(String cust_id) throws DPFTRuntimeException {
		synchronized(lock3){
			DPFTDboSet seqSet = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet("CUST_BRCH_NO", "customer_id='" + cust_id + "'");
			seqSet.load();
			if(seqSet.isEmpty()){
				DPFTDbo seq = seqSet.add();
				seq.setValue("customer_id", cust_id);
				seq.setValue("branch_no", "001");
				seqSet.save();
				seqSet.close();
				return "001";
			}
			DPFTDbo seq = seqSet.getDbo(0);
			int current_seq = Integer.valueOf(seq.getString("branch_no"));
			current_seq++;
			if(current_seq > 999){
				DPFTLogger.info(TFBUtil.class.getName(), "Info: Sequence exceed limit Reset Value...");
				current_seq = 1;
			}
			String val = String.format("%03d", current_seq);
			seq.setValue("branch_no", val);
			seqSet.save();
			seqSet.close();
			return val;
			
		}
	}

	public static String getSMSContactEmail(String cmp_owner_email) throws DPFTRuntimeException {
		if(cmp_owner_email != null){
			StringBuilder sb = new StringBuilder();
			sb.append(getMailGroup(TFBConstants.TFB_MAILGROUP_ITADM)).append(GlobalConstants.FILE_DELIMETER_COMMA).append(cmp_owner_email);
			return sb.toString();
		}
		return getMailGroup(TFBConstants.TFB_MAILGROUP_ITADM);
	}

	public static String getMailGroup(String group_id) throws DPFTRuntimeException {
		TFBMailGroupDboSet gSet = (TFBMailGroupDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet("MAILGROUP", "group_name='" + group_id + "'");
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < gSet.count(); i++){
			TFBMailGroupDbo mg = (TFBMailGroupDbo) gSet.getDbo(i);
			sb.append(mg.getEmail()).append(GlobalConstants.FILE_DELIMETER_COMMA);
		}
		if(sb.length() > 0)
			return sb.substring(0, sb.length()-1);
		return null;
	}

}
