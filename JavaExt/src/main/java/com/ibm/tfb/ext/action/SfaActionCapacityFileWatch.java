package com.ibm.tfb.ext.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.action.DPFTActionFolderFilesWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.FileMetaDefDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.dpft.engine.core.util.DPFTcdFTPUtil;
import com.ibm.tfb.ext.common.TFBConstants;

public class SfaActionCapacityFileWatch extends DPFTActionFolderFilesWatch {
	private final static Pattern file_pattern = Pattern.compile("T_SFA_C\\d{9}");
	private String[] flist = null;

	@Override
	public String getFolderDir() {
		return DPFTEngine.getSystemProperties(TFBConstants.SFA_PROP_CAPACITY_FOLDER);
	}

	@Override
	public void postAction(File[] files) throws DPFTRuntimeException {
		ArrayList<String> filenames = new ArrayList<String>();
		for(File f: files){
			Matcher m = file_pattern.matcher(f.getName());
			if(m.find())
				filenames.add(f.getName());
		}
		
		if(!filenames.isEmpty()){
			//has file to transfer
			flist = filenames.toArray(new String[filenames.size()]);
			FileMetaDefDboSet meta = (FileMetaDefDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
					.getDboSet("DPFT_FILE_META_DEF", "chal_name='SFA'");
			meta.load();
			meta.close();
			DPFTcdFTPUtil ftputil = new DPFTcdFTPUtil(getFolderDir(), meta.getRemoteDir(), meta.getCDProfile());
			ftputil.doFTP_Out(null, flist);
			
			Object[] params = {meta.getRemoteDir(), getAllFileNameAsString()};
			DPFTUtil.pushNotification(new DPFTMessage("CUSTOM", "TFB00016I", params));
		}
	}

	private String getAllFileNameAsString() {
		StringBuilder sb = new StringBuilder();
		for(String fname: flist){
			sb.append(fname).append(GlobalConstants.FILE_EOL);
		}
		return sb.toString();
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {

	}

	@Override
	public void finish() throws DPFTRuntimeException {
		if(flist == null)
			return;
		
		for(String fname: flist){
			try {
				moveFile2CompleteFolder(fname);
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void clean() throws DPFTRuntimeException {
		
	}
	
	private void moveFile2CompleteFolder(String filename) throws IOException {
		String fdir = getFolderDir();
		File cp_dir = new File(fdir + File.separator + "archive");
		if(!cp_dir.exists())
			cp_dir.mkdirs();
		
		File org_file = new File(fdir + File.separator + filename);
		File cp_file = new File(fdir + File.separator + "archive" + File.separator + filename);
		InputStream fin = new FileInputStream(org_file);
		OutputStream fout = new FileOutputStream(cp_file);
		byte[] buffer = new byte[1024];
    		
    	int length;
    	//copy the file content in bytes 
        while ((length = fin.read(buffer)) > 0){
        	fout.write(buffer, 0, length);
        }
        fin.close();
        fout.close();
        fin = null;
        fout = null;
        //delete the original file
        if(!org_file.delete()){
        	DPFTLogger.debug(this, "Cannot Delete File " + org_file.getAbsolutePath());
        }else{
        	DPFTLogger.debug(this, "Delete File " + org_file.getAbsolutePath());
        }
	}

}
