package com.ibm.tfb.ext.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTPrioritySettingDbo;
import com.ibm.dpft.engine.core.dbo.DPFTPrioritySettingDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;

public class MKTDMCustomerContactDboSet extends DPFTDboSet {

	public MKTDMCustomerContactDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
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
		for(int i = 0; i < this.count(); i++){
			MKTDMCustomerContactDbo dbo = (MKTDMCustomerContactDbo) this.getDbo(i);
			if(biz_type != null){
				if(dbo.find(cust_id, cont_cd, biz_type)){
					return dbo.getString("cont_info");
				}
			}else{
				if(dbo.find(cust_id, cont_cd)){
					return dbo.getString("cont_info");
				}
			}
		}
		return null;
	}

	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new MKTDMCustomerContactDbo(dboname, d, this);
	}

	public String getAddrByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_ADDR_COMM, biz_type);
	}
	
	public String getZipCodeByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_ZIPCD_COMM, biz_type);
	}
	
	public String getAddrCodeByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		return getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_ADDRCD_COMM, biz_type);
	}
	
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
	
	public String getMobileByBizType(String cust_id, String biz_type) throws DPFTRuntimeException {
		String mobile = getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_MOBILE_1, biz_type);
		if(mobile == null)
			mobile = getContactInfo(cust_id, TFBConstants.MKTDM_CONT_CD_MOBILE_2, biz_type);
		return mobile;
	}

	public String[] getPrioritizedAddr(String cust_id, String template, String p_code) throws DPFTRuntimeException {
		String[] addr_info = new String[3];
		
		DPFTPrioritySettingDboSet pSet = (DPFTPrioritySettingDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet("DPFT_PRIORITY_CODE_DEF", "template='" + template + "' and priority_code='" + p_code + "' and active=1");
		if(pSet.isEmpty()){
			Object[] params = {template, p_code};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0007E", params);
		}
		DPFTPrioritySettingDbo pr = (DPFTPrioritySettingDbo) pSet.getDbo(0);
		String[] p = pr.getPrioritySettings();
		addr_info[0] = getAddrByBizType(cust_id,p[0]);
		addr_info[1] = getZipCodeByBizType(cust_id, p[0]);
		addr_info[2] = getAddrCodeByBizType(cust_id, p[0]);
		if(addr_info[0] == null){
			addr_info[0] = getAddrByBizType(cust_id,p[1]);
			addr_info[1] = getZipCodeByBizType(cust_id, p[1]);
			addr_info[2] = getAddrCodeByBizType(cust_id, p[1]);
			if(addr_info[0] == null){
				addr_info[0] = getAddrByBizType(cust_id,p[2]);
				addr_info[1] = getZipCodeByBizType(cust_id, p[2]);
				addr_info[2] = getAddrCodeByBizType(cust_id, p[2]);
			}
		}
		return addr_info;
	}

	public String getPrioritizedEmail(String cust_id, String template, String p_code) throws DPFTRuntimeException {
		String email = null;
		DPFTPrioritySettingDboSet pSet = (DPFTPrioritySettingDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet("DPFT_PRIORITY_CODE_DEF", "template='" + template + "' and priority_code='" + p_code + "' and active=1");
		if(pSet.isEmpty()){
			Object[] params = {template, p_code};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0007E", params);
		}
		DPFTPrioritySettingDbo pr = (DPFTPrioritySettingDbo) pSet.getDbo(0);
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
		// TODO Auto-generated method stub
		String mobile = null;
		DPFTPrioritySettingDboSet pSet = (DPFTPrioritySettingDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet("DPFT_PRIORITY_CODE_DEF", "template='" + template + "' and priority_code='" + p_code + "' and active=1");
		if(pSet.isEmpty()){
			Object[] params = {template, p_code};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0007E", params);
		}
		DPFTPrioritySettingDbo pr = (DPFTPrioritySettingDbo) pSet.getDbo(0);
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


}
