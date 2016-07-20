package com.ibm.tfb.ext.util;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.meta.DPFTFileMetaData;
import com.ibm.dpft.engine.core.util.DPFTCSVFileFormatter;

public class LTMReportGKFileFormatter extends DPFTCSVFileFormatter {
	public LTMReportGKFileFormatter(DPFTFileMetaData h_meta, DPFTFileMetaData d_meta) {
		super(h_meta, d_meta);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean canOutputRecord(DPFTDbo dbo) {
		return dbo.getString("process_status").equals(GlobalConstants.O_DATA_GK_EXCLUDE);
	}

}
