package com.ibm.tfb.ext.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTPrioritySettingDbo;
import com.ibm.dpft.engine.core.dbo.DPFTPrioritySettingDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.tfb.ext.common.TFBConstants;

public class MKTDMCustomerContactDboSet extends DPFTDboSet {
	private HashMap<String, String> vMap = null;

	public MKTDMCustomerContactDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void load() throws DPFTRuntimeException {
		super.load();
		if(vMap == null)
			vMap = new HashMap<String, String>();
		vMap.clear();
		for(int i = 0; i < count(); i++){
			String key = this.getDbo(i).getString("cust_id") + this.getDbo(i).getString("cont_cd") + this.getDbo(i).getString("biz_cat");
			vMap.put(key, this.getDbo(i).getString("cont_info"));
		}
	}
	
	@Override
	public void close() throws DPFTRuntimeException {
		super.close();
		vMap.clear();
		this.clear();
	}

	public String getEmail(String cust_id) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_EMAIL); 
	}
	
	public String getMobile(String cust_id) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_MOBILE_1); 
	}
	
	private String getContactInfo(String cust_id, String cont_cd) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getContactInfo(cust_id, cont_cd, null);
	}
	
	private String getContactInfo(String cust_id, String cont_cd, String biz_type) throws DPFTRuntimeException {
//		for(int i = 0; i < this.count(); i++){
//			MKTDMCustomerContactDbo dbo = (MKTDMCustomerContactDbo) this.getDbo(i);
//			if(biz_type != null){
//				if(dbo.find(cust_id, cont_cd, biz_type)){
//					return dbo.getString("cont_info");
//				}
//			}else{
//				if(dbo.find(cust_id, cont_cd)){
//					return dbo.getString("cont_info");
//				}
//			}
//		}
		String key = cust_id + cont_cd + biz_type;
		return vMap.get(key);
	}

	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new MKTDMCustomerContactDbo(dboname, d, this);
	}

	public String getChnNameByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_CHN_NAME, biz_type);
	}
	
	
	public String getAddrByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_ADDR_COMM, biz_type);
	}
	
	public String getZipCodeByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_ZIPCD_COMM, biz_type);
	}
	
	public String getResidentAddrCodeByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_RES_ADDR, biz_type);
	}
	
	public String getResidentZipCodeByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_RES_ZIP, biz_type);
	}
	
	public String getAddrCodeByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_ADDRCD_COMM, biz_type);
	}
	
		/* 
	 * add house address option start 
	 * house address zipcode
	 */
	public String getAddrByBizTypeH(String cust_id, String biz_type) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_HOU_COMM, biz_type);
	}

	public String getZipCodeByBizTypeH(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_HOUZIP_COMM, biz_type);
	}
	
	public String getAddrCodeByBizTypeH(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_HOUCD_COMM, biz_type);
	}
	/* 
	 * end 
	 */
	 
	public String getEmailByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_EMAIL, biz_type);
	}
	
	public String getDayTelByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_TEL_DAY, biz_type);
	}
	
	public String getNightTelByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_TEL_NIGHT, biz_type);
	}
	
	public String getOfficeTelAreaByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_TEL_OFF_ARE, biz_type);
	}
	
	public String getOfficeTelByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_TEL_OFF, biz_type);
	}
	
	public String getOfficeExtTelByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_TEL_OFF_EXT, biz_type);
	}

	public String getCommTelAreByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_COM_TEL_ARE, biz_type);
	}
	
	public String getCommTelByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_COM_TEL, biz_type);
	}
	
	public String getMobileByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		String mobile = getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_MOBILE_1, biz_type);
		if(mobile == null)
			mobile = getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_MOBILE_2, biz_type);
		return mobile;
	}

	public String[] getPrioritizedAddr(String cust_id, String template, String p_code) throws DPFTRuntimeException {
		String[] addr_info = new String[3];

		DPFTPrioritySettingDboSet pSet = DPFTEngine.getPriorityCodeSetting();
		DPFTPrioritySettingDbo pr = pSet.getPrioritySetting(template, p_code);
		if(pr == null){
			Object[] params = {template, p_code};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0007E", params);
		}
		if(p_code.equals("04") || p_code.equals("05") || p_code.equals("06")){
			String[] p = pr.getPrioritySettings();
			addr_info[0] = getAddrByBizTypeH(cust_id,p[0]);
			addr_info[1] = getZipCodeByBizTypeH(cust_id, p[0]);
			addr_info[2] = getAddrCodeByBizTypeH(cust_id, p[0]);
			if(addr_info[0] == null || addr_info[2] == null){
				addr_info[0] = getAddrByBizType(cust_id,p[1]);
				addr_info[1] = getZipCodeByBizType(cust_id, p[1]);
				addr_info[2] = getAddrCodeByBizType(cust_id, p[1]);
				if(addr_info[0] == null || addr_info[2] == null){
					addr_info[0] = getAddrByBizType(cust_id,p[2]);
					addr_info[1] = getZipCodeByBizType(cust_id, p[2]);
					addr_info[2] = getAddrCodeByBizType(cust_id, p[2]);
				}
			}
		}else{
			String[] p = pr.getPrioritySettings();
			addr_info[0] = getAddrByBizType(cust_id,p[0]);
			addr_info[1] = getZipCodeByBizType(cust_id, p[0]);
			addr_info[2] = getAddrCodeByBizType(cust_id, p[0]);
			if(addr_info[0] == null || addr_info[2] == null){
				addr_info[0] = getAddrByBizType(cust_id,p[1]);
				addr_info[1] = getZipCodeByBizType(cust_id, p[1]);
				addr_info[2] = getAddrCodeByBizType(cust_id, p[1]);
				if(addr_info[0] == null || addr_info[2] == null){
					addr_info[0] = getAddrByBizType(cust_id,p[2]);
					addr_info[1] = getZipCodeByBizType(cust_id, p[2]);
					addr_info[2] = getAddrCodeByBizType(cust_id, p[2]);
				}
			}
		}
		return addr_info;
	}
	
	
	public String[] getPrioritizedAddrWithoutAddrCode(String cust_id, String template, String p_code) throws DPFTRuntimeException {
		String[] addr_info = new String[2];

		DPFTPrioritySettingDboSet pSet = DPFTEngine.getPriorityCodeSetting();
		DPFTPrioritySettingDbo pr = pSet.getPrioritySetting(template, p_code);
		if(pr == null){
			Object[] params = {template, p_code};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0007E", params);
		}
		String[] p = pr.getPrioritySettings();
		addr_info[0] = getAddrByBizType(cust_id,p[0]);
		addr_info[1] = getZipCodeByBizType(cust_id, p[0]);
		if(addr_info[0] == null ){
			addr_info[0] = getAddrByBizType(cust_id,p[1]);
			addr_info[1] = getZipCodeByBizType(cust_id, p[1]);
			if(addr_info[0] == null){
				addr_info[0] = getAddrByBizType(cust_id,p[2]);
				addr_info[1] = getZipCodeByBizType(cust_id, p[2]);
			}
		}
		return addr_info;
	}

	public String getPrioritizedEmail(String cust_id, String template, String p_code) throws DPFTRuntimeException {
		String email = null;
		
		DPFTPrioritySettingDboSet pSet = DPFTEngine.getPriorityCodeSetting();
		DPFTPrioritySettingDbo pr = pSet.getPrioritySetting(template, p_code);
		if(pr == null){
			Object[] params = {template, p_code};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0007E", params);
		}
		
		String[] p = pr.getPrioritySettings();
		email = getEmailByBizType(cust_id, p[0]);
		if(email == null){
			email = getEmailByBizType(cust_id, p[1]);
			if(email == null){
				email = getEmailByBizType(cust_id, p[2]);
			}
		}
		return email;
	}

	public String getPrioritizedMobilePhone(String cust_id, String template, String p_code) throws DPFTRuntimeException {
		String mobile = null;
		
		DPFTPrioritySettingDboSet pSet = DPFTEngine.getPriorityCodeSetting();
		DPFTPrioritySettingDbo pr = pSet.getPrioritySetting(template, p_code);
		if(pr == null){
			Object[] params = {template, p_code};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0007E", params);
		}
		String[] p = pr.getPrioritySettings();
		mobile = getMobileByBizType(cust_id, p[0]);
		if(mobile == null){
			mobile = getMobileByBizType(cust_id, p[1]);
			if(mobile == null){
				mobile = getMobileByBizType(cust_id, p[2]);
			}
		}
		return mobile;
	}

	public String getOfficePhoneByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		
		String off_are = getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_TEL_OFF_ARE, biz_type);
		String off = getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_TEL_OFF, biz_type);
		String off_ext =  getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_TEL_OFF_EXT, biz_type);

		if(off_are == null && off == null)
			return null;
		
		return off_are + off + (off_ext == null ? "" : "#" + off_ext);
	}

	public String getCommPhoneByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		
		String comm_are = getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_COM_TEL_ARE, biz_type);
		String comm = getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_COM_TEL, biz_type);

		if(comm_are == null && comm == null)
			return null;
		
		return comm_are + comm;
	}	
	
}
