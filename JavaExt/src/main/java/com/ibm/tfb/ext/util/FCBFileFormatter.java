package com.ibm.tfb.ext.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.FileDictionaryDboSet;
import com.ibm.dpft.engine.core.dbo.FileMetaDefDboSet;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.meta.DPFTFileMetaData;
import com.ibm.dpft.engine.core.util.DPFTCSVFileFormatter;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;

public class FCBFileFormatter extends DPFTCSVFileFormatter {
	private String i_file_name   = null;
	private String i_file_string = null;
	private String chal_name = null;
	private DPFTFileMetaData info_meta = null;

	public FCBFileFormatter(DPFTFileMetaData h_meta, DPFTFileMetaData d_meta, String chal_name) throws DPFTRuntimeException {
		super(h_meta, d_meta);
		this.chal_name = chal_name;
		info_meta = getInfoFileMeta();
	}

	@Override
	public void format(DPFTDboSet rs) throws DPFTRuntimeException {
		super.format(rs);
		if(rs.count() > 0){
			if(hasInfoFile()){
				formatInfoFileString(rs);
			}
		}
	}

	protected void formatInfoFileString(DPFTDboSet rs) throws DPFTRuntimeException {
		StringBuilder sb = new StringBuilder();
		DPFTDboSet infoSet = getInfoSet(rs, info_meta);
		
		/*build data header String*/
		if(info_meta.isContainHeader()){
			sb.append(buildHeaderString(info_meta.getFileColsInOrder())).append(GlobalConstants.FILE_EOL);
		}
		
		/*build data body*/
		for(int i = 0; i < infoSet.count(); i++){
			sb.append(buildDataString(infoSet.getDbo(i), info_meta)).append(GlobalConstants.FILE_EOL);
		}
		
		this.setInfoFileString(sb.toString());
		
		/*build data file name*/
		this.setInfoFileName(buildFileNameByPattern(info_meta.getFileName(), rs.getDbo(0)));
	}
	
	private DPFTDboSet getInfoSet(DPFTDboSet rs, DPFTFileMetaData meta) throws DPFTRuntimeException {
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		ArrayList<String> indexlist = new ArrayList<String>();
		for(int i = 0; i < rs.count(); i++){
			DPFTDbo dbo = rs.getDbo(i);
			if(!canOutputRecord(dbo) || dbo.getString("place_type").equals(TFBConstants.FCB_PLACE_TYPE_PIC))
				continue;
			String key = dbo.getString("offer_order") + dbo.getString("offer_code");
			if(indexlist.contains(key))
				continue;
			
			HashMap<String, Object> d = new HashMap<String, Object>();
			String[] cols = meta.getColsOrder();
			for(String col: cols){
				d.put(col, dbo.getColumnValue(col));
			}
			data.add(d);
			indexlist.add(key);
		}
		Collections.sort(data, new Comparator<HashMap<String, Object>>(){

			@Override
			public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
				int order1 = Integer.valueOf((String) o1.get("OFFER_ORDER"));
				int order2 = Integer.valueOf((String) o2.get("OFFER_ORDER"));
				return order1 - order2;
			}
			
		});
		return new DPFTDboSet(null, data);
	}

	private DPFTFileMetaData getInfoFileMeta() throws DPFTRuntimeException {
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		FileMetaDefDboSet meta = (FileMetaDefDboSet) connector.getDboSet("DPFT_FILE_META_DEF", "chal_name='" + chal_name + "' and active=1");
		FileDictionaryDboSet dicSet = (FileDictionaryDboSet) connector.getDboSet("DPFT_FILE_DIC", "chal_name='" +chal_name + "' and active=1");
		meta.load();
		dicSet.load();
		meta.close();
		dicSet.close();
		return new DPFTFileMetaData(meta, dicSet);
	}

	@Override
	public HashMap<String, String> getFormatFileList() {
		HashMap<String, String> flist = super.getFormatFileList();
		if(hasInfoFile()){
			flist.put(getInfoFileName(), getInfoFileString());
		}
		return flist;
	}
	
	@Override
	public HashMap<String, String> getFormatFileCharset() {
		HashMap<String, String> flist = super.getFormatFileCharset();
		if(hasInfoFile()){
			flist.put(getInfoFileName(), info_meta.getFileEncode());
		}
		return flist;
	}
	
	@Override
	public String[] getBatchCompressFiles() {
		ArrayList<String> flist = new ArrayList<String>(Arrays.asList(super.getBatchCompressFiles()));
		if(hasInfoFile())
			flist.add(getInfoFileName());
		return flist.toArray(new String[flist.size()]);
	}

	private boolean hasInfoFile() {
		return info_meta.getFileName() != null;
	}

	public String getInfoFileName() {
		return i_file_name;
	}

	public void setInfoFileName(String i_file_name) {
		this.i_file_name = i_file_name;
	}

	public String getInfoFileString() {
		return i_file_string;
	}

	public void setInfoFileString(String i_file_string) {
		this.i_file_string = i_file_string;
	}
}
