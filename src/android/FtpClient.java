package se.mwthinker.ftpclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import java.io.File;
import java.io.InputStream;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import android.net.Uri;

import android.os.Build;
import android.os.StrictMode;
import android.os.Bundle;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;
import java.util.ArrayList;
import java.io.OutputStream;


public class FtpClient extends CordovaPlugin {
    public static final String TAG = FtpClient.class.getSimpleName();
	public CallbackContext callbackContext;
	public Context context;
		
	public static FTPClient ftpClient;	

	public static String FTPUrl = "";
	public static String NameFileDownload = "";
	public static String IPAddress = "";
	public static String Port = "";
	public static String FolderUpload = "";
	public static String UserName = "";
	public static String Pass = "";	 
	public static String Url = "";
	public static String FileName = "";
	public static String IndexRecord = "";

    
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG,"Init plugin");
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
        Log.v(TAG, "Plugin performs action:" + action);
        if (action == null || action == "") {
        	return false;
        } else {
        	try {        		
				if(action.equals("upload")) {
					IPAddress = args.getString(0).toString();
					Port = args.getString(1).toString();
					UserName = args.getString(2).toString();
					Pass = args.getString(3).toString();
					FolderUpload = args.getString(4).toString();
					Url = args.getString(5).toString();
					FileName = args.getString(6).toString();
					IndexRecord = args.getString(7).toString();
				}
				else {
					FTPUrl = args.getString(0).toString();
					NameFileDownload = args.getString(1).toString();
				}
							
				if (action.equals("downloadBinaryFile")) {					
					cordova.getThreadPool().execute(new Runnable() {
						public void run() {								
							ConnectURLServer();
						}
					});                 	
                } else if (action.equals("upload")) {									
					cordova.getThreadPool().execute(new Runnable() {
						public void run() {														
							ConnetAndLogin(IPAddress, Port, UserName, Pass, FolderUpload, Url, FileName, IndexRecord);														 															
						}
					});                     	
                } else {
                    // Action does not match!
                    return false;
                }
        	} catch (JSONException e) {
        		callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
                Log.v(TAG, "JSON_EXCEPTION" + e.getMessage());
        	}
        	return true;
        }
    }    
		
	public void ConnetAndLogin(String IPAddress1, String Port1, String UserName1, String Pass1, String FolderUpload1, String Url1, String FileName1, String IndexRecord1) {
        //Ip server        
		String server = IPAddress1;
		//Port server
		int port = Integer.parseInt(Port1);                
        String user = UserName1;
        String pass = Pass1;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            showServerReply(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
				this.callbackContext.error("Operation failed. Server reply code: " + replyCode);                
            }
            boolean success = ftpClient.login(user, pass);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            showServerReply(ftpClient);
            if (!success) {
				ftpClient.logout();
				ftpClient.disconnect();
				//401 Login failed
				PluginResult r = new PluginResult(PluginResult.Status.OK, "401");
				r.setKeepCallback(true);
				callbackContext.sendPluginResult(r);				                
            }			
			else
			{
				UploadSingleFileToServer(ftpClient, FolderUpload1, Url1, FileName1, IndexRecord1);					
			}						
        } catch (IOException ex) {            
			PluginResult r = new PluginResult(PluginResult.Status.ERROR);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);			
        }        
    }    

	public void UploadSingleFileToServer(FTPClient ftpClient, String FolderUpload2, String fullPathFile, String fileName2, String indexRecord2) throws IOException {
        try {
			InputStream input = new FileInputStream(new File(fullPathFile));
			String movePath = FolderUpload2 + fileName2;
			boolean success = ftpClient.storeFile(movePath, input);
			if (success) {                			
				ftpClient.logout();
				ftpClient.disconnect();
				//String ms = fullPathFile + "|Success|" + indexRecord2;
				//200 Upload success
				String ms = "200";
				PluginResult result = new PluginResult(PluginResult.Status.OK, ms);
			    result.setKeepCallback(true);
			    callbackContext.sendPluginResult(result);
			}
			else {
				ftpClient.logout();
				ftpClient.disconnect();
				//String ms = "Disconnect network. Upload file failed.";
				//403 Folder server not found
				String ms = "403";
				PluginResult result = new PluginResult(PluginResult.Status.OK, ms);
			    result.setKeepCallback(true);
			    callbackContext.sendPluginResult(result);			
			}
		}
		catch (IOException ex) {     
			//404 File client not found       
			PluginResult r = new PluginResult(PluginResult.Status.OK, "404");
			r.setKeepCallback(true);
			callbackContext.sendPluginResult(r);			
        }				
    } 

    private static void showServerReply(FTPClient ftpClient1) {
        String[] replies = ftpClient1.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }

	public void ConnectURLServer() {
		try {						
			DownloadFileFromServer(); 				
		} catch (IOException ex) {            
			PluginResult r = new PluginResult(PluginResult.Status.ERROR);
			r.setKeepCallback(true);
			callbackContext.sendPluginResult(r);			
		} 				
	}	

	public void DownloadFileFromServer() throws IOException {
		try {	
			context=this.cordova.getActivity().getApplicationContext(); 							
			String ftpUrl = FTPUrl + NameFileDownload;
			URL url = new URL(ftpUrl);
			URLConnection conn = url.openConnection();
			InputStream inputStream = conn.getInputStream();
			//String pathMove = "mnt/sdcard/SURVEY/" + NameFileDownload;
			//File traceFile = new File(((Context)this).getExternalFilesDir(null), "DatabaseTest.txt");
            //File traceFile = new File(Environment.getExternalStorageDirectory(), nameFileDownload);
			String NameFileDownloadNew = NameFileDownload.replace("_New", "");
			File pathMove = new File(("/data/data/" + context.getPackageName() + "/databases/"), NameFileDownloadNew);
			FileOutputStream outputStream = new FileOutputStream(pathMove);
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}			
			outputStream.close();
			inputStream.close();
			String ms = pathMove.getAbsolutePath().toString();
			PluginResult result = new PluginResult(PluginResult.Status.OK, ms);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result); 				
		} catch (IOException ex) {            
			PluginResult r = new PluginResult(PluginResult.Status.ERROR);
			r.setKeepCallback(true);
			callbackContext.sendPluginResult(r);			
		}		
	}
}
