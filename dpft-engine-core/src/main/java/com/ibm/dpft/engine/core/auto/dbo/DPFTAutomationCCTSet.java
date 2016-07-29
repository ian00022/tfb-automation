package com.ibm.dpft.engine.core.auto.dbo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.exception.DPFTAutomationException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class DPFTAutomationCCTSet extends DPFTDboSet {
	private final static Pattern cmp_code_pattern = Pattern.compile("C\\d{9}");

	public DPFTAutomationCCTSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new DPFTAutomationCCT(dboname, d, this);
	}

	public int isAllFlowChartFinished(String flowchart_type) throws DPFTRuntimeException {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < count(); i++){
			if(!this.getDbo(i).getString("flowcharttype").equalsIgnoreCase(flowchart_type))
				continue;
			if(this.getDbo(i).isNull("cct_status"))
				continue;
			
			if(flowchart_type.equalsIgnoreCase(GlobalConstants.DPFT_AUTOMATION_UNICA_FLOWCHART_TYPE_G)){
				if(this.getDbo(i).getString("cct_status").indexOf(GlobalConstants.DPFT_AUTOMATION_FLOWCHAR_RUN_FAILED) != -1){
					Object[] params = {this.getDbo(i).getString("name"), this.getDbo(i).getString("cct_status")};
					throw new DPFTAutomationException("SYSTEM", "AUTO0009E", params);
				}else if(!this.getDbo(i).getString("cct_status").equals(GlobalConstants.DPFT_AUTOMATION_FLOWCHAR_RUN_SUCCESS)){
					return GlobalConstants.DPFT_AUTOMATION_PS_RC_WAITING;
				}
				sb.append("flowchart = ").append(this.getDbo(i).getString("name"))
				.append(", status = ").append(this.getDbo(i).getString("cct_status")).append(GlobalConstants.FILE_EOL);
			}else{
				if(this.getDbo(i).getString("cct_status").equals(GlobalConstants.DPFT_AUTOMATION_FLOWCHAR_RUN_INPRG)){
					return GlobalConstants.DPFT_AUTOMATION_PS_RC_WAITING;
				}
				
				if(flowchart_type.equalsIgnoreCase(GlobalConstants.DPFT_AUTOMATION_UNICA_FLOWCHART_TYPE_R)){
					sb.append("flowchart = ").append(this.getDbo(i).getString("name"))
					.append(", status = ").append(this.getDbo(i).getString("cct_status")).append(GlobalConstants.FILE_EOL);
				}else{
					//Notify Campaign Owner
					StringBuilder sb1 = new StringBuilder();
					sb1.append("flowchart = ").append(this.getDbo(i).getString("name"))
					.append(", status = ").append(this.getDbo(i).getString("cct_status")).append(GlobalConstants.FILE_EOL);
					Object[] p1 = {sb1.toString()};
					DPFTUtil.pushNotification(
							DPFTUtil.getCampaignOwnerEmail(parseCampaignCode(flowchart_type, this.getDbo(i).getString("name"))), 
							new DPFTMessage("SYSTEM", "DPFT0044I", p1)
					);
					sb.append(sb1.toString());
				}
			}
		}
		Object[] params = {sb.toString()};
		DPFTUtil.pushNotification(new DPFTMessage("SYSTEM", "DPFT0044I", params));
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_TRUE;
	}

	public String parseCampaignCode(String type, String name) {
		int iter = 0;
		if(type.equalsIgnoreCase(GlobalConstants.DPFT_AUTOMATION_UNICA_FLOWCHART_TYPE_C)){
			iter = 1;
		}else if(type.equalsIgnoreCase(GlobalConstants.DPFT_AUTOMATION_UNICA_FLOWCHART_TYPE_RP)){
			iter = 2;
		}
		Matcher m = cmp_code_pattern.matcher(name);
		int i = 0;
		String cmp_code = null;
		while(m.find() && i < iter){
			cmp_code = m.group(0);
			i++;
		}
		return cmp_code;
	}

	public String[] getRunnableFlowChartByType(String type) throws DPFTRuntimeException {
		if(type.equalsIgnoreCase(GlobalConstants.DPFT_AUTOMATION_UNICA_FLOWCHART_TYPE_G)
				|| type.equalsIgnoreCase(GlobalConstants.DPFT_AUTOMATION_UNICA_FLOWCHART_TYPE_R)){
			Pattern p = Pattern.compile(type + "_\\d{2}$");
			HashMap<Integer, String> map = new HashMap<Integer, String>();
			for(int i = 0; i < count(); i++){
				String name = this.getDbo(i).getString("name");
				Matcher m = p.matcher(name);
				if(m.find()){
					String gno = m.group(0);
					map.put(Integer.valueOf(gno.substring(gno.indexOf("_") + 1)), this.getDbo(i).getString("filename"));
				}
			}
			String[] flist = new String[map.size()];
			for(int i: map.keySet()){
				int index = i-1;
				flist[index] = map.get(i);
			}
			return flist;
		}else{
			ArrayList<String> filelist = new ArrayList<String>();
			ArrayList<Integer> idx = new ArrayList<Integer>();
			for(int i = 0; i < count(); i++){
				if(this.getDbo(i).getString("flowcharttype").equalsIgnoreCase(type)){
					filelist.add(this.getDbo(i).getString("filename"));
					idx.add(i);
				}
			}
			return filelist.toArray(new String[filelist.size()]);
		}
	}
}
