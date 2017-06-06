package com.ibm.tfb.ext.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDetailDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileReader;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class ESMIntResDataFileReader extends DPFTFileReader {

	public ESMIntResDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
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
		DPFTConfig config = DPFTUtil.getSystemDBConfig();
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(config);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar c = Calendar.getInstance();  
		c.add(Calendar.MONTH, -1);
		String _sdf = sdf.format(c.getTime());
		DPFTDboSet _IDSet = (DPFTDboSet) connector.getDboSet("DPFT_IDMAPPING", "set_date < '"+ _sdf +"'");
		_IDSet.load();
		if(!_IDSet.isEmpty()){
			_IDSet.deleteAll();
			_IDSet.save();
		}
		_IDSet.close();
		DPFTDboSet IDSet;
		
		Map<String, String> tCode = new HashMap<String, String>();
		if(read_data.size() != 0){
			//  build treatment_code query string for load DPFT_IDMAPPING 
			StringBuilder sb = new StringBuilder();
			sb.append( "TREATMENT_CODE" + " in (");
			for(HashMap<String, String> rowdata: read_data){
				String[] ds = rowdata.get("DestName").split(GlobalConstants.FILE_DELIMETER_PIP);
				if(StringUtils.isNotBlank(ds[1]) && !tCode.containsKey(ds[1])){
					tCode.put(ds[1], ds[1]);
					sb.append("'" + ds[1] + "',");
				}
			}
			IDSet = (DPFTDboSet) connector.getDboSet("DPFT_IDMAPPING", sb.substring(0, sb.length()-1) + ")");
			IDSet.load();
			tCode.clear();
	
			for(int i=0; i <IDSet.count(); i++){
				DPFTDbo  IDDbo =  IDSet.getDbo(i);
				tCode.put(IDDbo.getString("TREATMENT_CODE")+IDDbo.getString("ID_NUMBER"), IDDbo.getString("CUSTOMER_ID"));
			}
			
			IDSet.close();
		}
		
		for(HashMap<String, String> rowdata: read_data){
			DPFTDbo new_data = targetSet.add();
			new_data.setValue("chal_name", chal_name);
			new_data.setValue("process_time", timestamp);
			new_data.setValue("res_code"    , "I");
			for(String col: rowdata.keySet()){
				if(f_col_2_tgt_col_map.get(col) == null)
					continue;
				new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
			}
			//Parse DestName field to get treatment code, customer_id
			String[] ds = new_data.getString("resv1").split(GlobalConstants.FILE_DELIMETER_PIP);
			
			if(ds.length == 3){
					String customerId = tCode.containsKey(ds[1]+ds[2]) ? tCode.get(ds[1]+ds[2]) : ds[2];
					new_data.setValue("treatment_code", ds[1]);
					new_data.setValue("customer_id", customerId);
					new_data.setValue("resv1", ds[0]+"||"+ds[1]+"||"+customerId);
			}
		}
		targetSet.save();
		targetSet.close();
	}
}
