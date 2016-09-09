package com.ibm.dpft.engine.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.FTPConfig;

public class DPFTBasicFTPUtil extends DPFTFileFTPUtil {
	

	public DPFTBasicFTPUtil(String ldir, String rdir, FTPConfig cfg) {
		super(ldir, rdir, cfg);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doFTP_Out(String[] c_out_list, String[] file_out_list) {
		// TODO Auto-generated method stub
		FTPClient ftp_client = new FTPClient();
		try{
			FTPConfig config = getConfig();
			ftp_client.connect(config.getHost(), config.getPort());
			ftp_client.login(config.getUser(), config.getPassword());
			ftp_client.enterLocalPassiveMode();
			ftp_client.setFileType(FTP.BINARY_FILE_TYPE);
			
			/*Read local files*/
			for(String filename : file_out_list){
				File localfile = new File(getLocalDir() + "/" + filename);
				InputStream in = new FileInputStream(localfile);
				String remoteDir = getRemoteDir() + filename;
				DPFTLogger.info(this, "Start FTP file:" + filename + " to Remote Dir:" + remoteDir);
				long time_start = System.currentTimeMillis();
				boolean done = ftp_client.storeFile(remoteDir , in);
				long time_end = System.currentTimeMillis();
				in.close();
				if(done)
					DPFTLogger.info(this, "Successfully FTP file:" + filename + "to Remote Dir:" + remoteDir + " Time Spent: " + String.format("%.3f", (float)((time_end-time_start)/1000)) + " Sec.");
			}
		}catch(Exception e){
			DPFTLogger.error(this, "Error when FTP Files :", e);
		}finally{
			try {
                if (ftp_client.isConnected()) {
                	ftp_client.logout();
                	ftp_client.disconnect();
                }
            } catch (IOException ex) {
            	DPFTLogger.error(this, "Error when disconnecting FTP Server :", ex);
            }
		}
	}

	@Override
	public int doFTP_Get(String[] c_in_list, String[] file_in_list) {
		// TODO Auto-generated method stub
		return GlobalConstants.ERROR_LEVEL_TRF_SUCCESS;
	}
	
}
