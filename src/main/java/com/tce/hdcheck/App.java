package com.tce.hdcheck;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
//import java.net.InetAddress;

public class App {
	static String gmailAccount="";
	static String gmailPassword="";
	static String emailTitleKey="";
	
	public static List<String> getAllDrivers(){
		List<String> drivers= new ArrayList<String>();
		
	   	File[] roots = File.listRoots();
    	for(int i = 0; i < roots.length ; i++) {
    		String drive=roots[i].getPath();
    		if (drive.endsWith("\\")) drive=drive.substring(0,drive.length()-1);
    		drivers.add(drive);
     	}
    	
    	return drivers;
	}
	
	public static void main(String[] args) {
		//You are using this app to query folder size by command line
		//>java -jar hdCheck.jar "C:\eclipse-bluemix"
		if (args != null && args.length > 0) {
			String folderName=args[0];
			System.out.println("" + folderName + " folder size checking..." );
			long size=getFolderSize(folderName);
			if (size<0) {
				System.out.println("" + size );
			}else{
				String formatedSize=formatSpace(size);
				System.out.println(formatedSize + "; " + size + "bytes");
			}
			
		//You are running Free HD check by sending alter email	
		}else{

			System.out.println("Free HD spaces checking.....");
			List<String> drivers=getAllDrivers();
	    	
	    	Properties props = new Properties();
	    	//String properties="C:\\eclipse-bluemix\\workspace\\HDCheck\\resource\\hdmonitor.properties";
	    	String properties="hdmonitor.properties";
	    	File f= new File(properties);
	    	if ( !f.exists() ) {
	    		System.out.println("Error: mising hdmonitor.properties\n");
	    		return;
	    	}
	    	
	    	try{    		
	    		props.load(new FileInputStream(properties));
	    	}catch(Exception e) {
	    		System.out.println("Error: can't load hdmonitor.properties\n");
	    		return;
	    	}
	    	
	    	
	    	//1. Check all drivers
	    	String body1=null;
	    	for(String drive:drivers) {
	    		String wkBody1=getCheckDriveFreeSpaceMsg(drive,props);
	    		//checkDriveFreeSpace(drive,props);
	    		if (wkBody1!=null && wkBody1.length()>1)
	    			if (body1==null || body1.length()<2)
	    				body1=wkBody1;
	    			else
	    				body1 += "\n" + wkBody1;
	    	}
	    	
	    	//2. Check the specified paths
	    	String body2=null;
	    	for(int nPathIdx=1; nPathIdx<=5;nPathIdx++) {
	    		String wkBody2=getCheckPathFreeSpaceMsg(nPathIdx,props);
	    		if (wkBody2!=null && wkBody2.length()>1)
	    			if (body2==null || body2.length()<2)
	    				body2=wkBody2;
	    			else
	    				body2 += "\n" + wkBody2;
	    	}
	    	
	    	String body=null;
	    	
	    	if (body1!=null && body1.length()>1) {
	    		if (body==null)
	    			body=body1;
	    		else
	    			body+="\n" + body1;
	    	}
	    	if (body2!=null && body2.length()>1) {
	    		if (body==null)
	    			body=body2;
	    		else
	    			body+="\n" + body2;
	    	}
	    	
	    	if (body!=null && body.length()>2) {
	    		boolean bSent= sendEmail(body,props);
	    		if (bSent)
	    			System.out.println(SendEmailTLS.getCurrentTime() + " sent email successfully.");
	    		else
	    			System.out.println(SendEmailTLS.getCurrentTime() + " sent email failed.");
	    	}

		}
    }
    
    public static long toLong(String space) {
    	if (space==null ) return 0L;
    	space=space.toUpperCase();
    	space=space.replace("KB", "K");
    	space=space.replace("MB", "M");
    	space=space.replace("GB", "G");
    	space=space.replace("TB", "T");
    	String min=space.substring(0,space.length()-1);
    	if (space.endsWith("K")) 
    		return Long.parseLong(min) * 1024;
    	if (space.endsWith("M")) 
    		return Long.parseLong(min) * 1024 * 1024;
    	if (space.endsWith("G")) 
    		return Long.parseLong(min) * 1024 * 1024 * 1024;
    	if (space.endsWith("T")) 
    		return Long.parseLong(min) * 1024 * 1024 * 1024 * 1024;
    	
    	return -1L;
    }
     
    
    public static long getFreeSpace(String drive) {
    
    	try{
    		if (drive!=null && drive.length()==2) {
    			File file = new File(drive);
    			long totalSpace = file.getUsableSpace(); //total disk space in bytes.
    			return totalSpace;
    		}
    	}catch(Exception e) {
    		
    	}
    	return 0L;
    }
    
    public static String formatMinHDSpace(String space, Long freeSpace) {
    	if ( space==null)
    		return  "" + freeSpace;
    	
    	space=space.toUpperCase();
    	space=space.replace("KB", "K");
    	space=space.replace("MB", "M");
    	space=space.replace("GB", "G");
    	space=space.replace("TB", "T");
    	
    	if (space.endsWith("K")) 
    		return Integer.toString((int)(freeSpace/1024)) + "K";
    	if (space.endsWith("M")) 
    		return Integer.toString((int)(freeSpace/(1024*1024))) + "M";
    	if (space.endsWith("G")) 
    		return Integer.toString((int)(freeSpace/(1024*1024*1024))) + "G";
    	if (space.endsWith("T")) 
    		return Integer.toString((int)(freeSpace/(1024*1024*1024*1024))) + "T";
    	return freeSpace.toString();
    }
    
    public static String getCheckDriveFreeSpaceMsg(String drive,Properties props ) {
    	String msg=null;
    	String msgAlways=null;
    	if (drive==null || props==null) return msg;
    	if (drive.trim().equals("")) return msg;
    	
    	String alwaysEmailSend=props.getProperty("always.email.send","No");
       	String min=props.getProperty(drive.toUpperCase());
       	//System.out.println("Checking " + drive )
       	if (min==null || min.equals("")) {
       		min=props.getProperty(drive.toLowerCase());
       	}
 
       	if (min==null || min.equals("")) {
       		return null;
       	}

       	
     	long lMin=toLong(min);
     	boolean bCheckOK=false;
    	long lFreeSpace=getFreeSpace(drive);
     	System.out.println("Checking " + drive + "; Minimum=" + min + "; Available=" +  formatMinHDSpace(min,lFreeSpace) + "" );
    	if (min==null || min.equals("")) bCheckOK=true;
       	
    	if (lMin < lFreeSpace) bCheckOK=true;
    	if (lMin<=0) bCheckOK=true;
    	
    	if (!bCheckOK)
    		emailTitleKey="Error";
    	
    	String body=drive + " free space=" + formatMinHDSpace(min,lFreeSpace) +
				"; Required minimum free space=" + min;
    	
    	if (alwaysEmailSend!=null && alwaysEmailSend.startsWith("Y"))
    		bCheckOK=false;
    	
    	if (!bCheckOK) {    		
    		return body;
    	}else{
    		return null;
    	}
    	
    }

    /*
    public static boolean checkDriveFreeSpace(String drive,Properties props ) {
    	if (drive==null || props==null) return true;
    	if (drive.trim().equals("")) return true;
       	String min=props.getProperty(drive.toUpperCase());
       	if (min==null || min.equals("")) {
       		min=props.getProperty(drive.toLowerCase());
       	}
       	//System.out.println("Minimum=" + min + " for " + drive + "\n" );
    	if (min==null || min.equals("")) return true;
   	
    	String host=props.getProperty("mail.smtp.host", "smtp.gmail.com");
    	String port=props.getProperty("mail.smtp.port", "587");
    	String auth=props.getProperty("mail.smtp.auth", "true");
    	String from=props.getProperty("mail.from", "hdspacesmonitor@gmail.com");
    	gmailAccount=props.getProperty("mail.smtp.account", "hdspacesmonitor@gmail.com");
    	gmailPassword=props.getProperty("mail.smtp.password", "Psapi123!");
    	String to=props.getProperty("mail.to", "fan8118@gmail.com");
    	String cc=props.getProperty("mail.cc", "");
    	String hostname=props.getProperty("host.name", "Unknown");
    	String errorTitle=props.getProperty("mail.error.title", "Error: Hard Disk Free Space isn't enough");
    	String warningTitle=props.getProperty("mail.warning.title", "Warning: Hard Disk Free Space isn't enough");
     	long lMin=toLong(min);
    	long lFreeSpace=getFreeSpace(drive);
    	
    	System.out.println(SendEmailTLS.getCurrentTime() + " Checked " + drive + ", Minimum=" + min + "; useable space=" + lFreeSpace + "(" +  formatMinHDSpace(min,lFreeSpace) + ")\n");
    	
    	if (lMin < lFreeSpace) return true;
    	if (lMin<=0) return true;
    	
    	if (to.isEmpty()) {
    		System.out.println(SendEmailTLS.getCurrentTime() + " Error: Missing email recipients.\n");
    		return true;
    	}
    	
		InetAddress ip = null;
    	try{
    		ip = InetAddress.getLocalHost();
    		if (hostname==null || hostname.length()<1 && (ip.getHostName()!=null && ip.getHostName().length()>1))
    			hostname = ip.getHostName();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
		String subject=errorTitle + " in "  + hostname;
		String body=drive + " free space is " + formatMinHDSpace(min,lFreeSpace) +
				"\n\nRequired minimum free space is " + min;
				
    	String sSend=SendEmailTLS.send(gmailAccount, gmailPassword, to, subject, body);
    	if (!"OK".equalsIgnoreCase(sSend)) {
    		System.out.println(SendEmailTLS.getCurrentTime() + " your email was not sent out.mssage=" + body);
    	}
    	System.out.println(SendEmailTLS.getCurrentTime() + " sent successfully.title=" + subject + "\nbody=" + body);
  		return true;
    }
   */
    
    
    //Check path
    public static String getCheckPathFreeSpaceMsg(int nPathIex,Properties props ) {
    	if (nPathIex<1) 
    		return null;
    	
    	String alwaysEmailSend=props.getProperty("always.email.send", "No");
    	String sPath=props.getProperty("PATH" + nPathIex);
    	//System.out.println(sPath + "=" + sPath);
    	if (sPath==null)
    		return null;
    	
    	String sMax=props.getProperty(sPath);
    	    	
    	if (sMax==null || sMax.equals("")) return null;
     	long lMax=toLong(sMax);
    	long lCurUsageSpace=getFolderSize(sPath);
    	String msg=sPath + " checked. Current usage=" + formatMinHDSpace(sMax,lCurUsageSpace) + "; maximum=" + sMax + ".";
    	System.out.println(msg);
    	boolean bCheckOK=false;
    	
    	if (lMax <= 0 || lCurUsageSpace<=0) bCheckOK=true;
       	if (lCurUsageSpace < lMax) bCheckOK=true;
       	
    	if (!bCheckOK)
    		emailTitleKey="Error";

       	
       	if (alwaysEmailSend != null && alwaysEmailSend.startsWith("Y"))
       		bCheckOK=false;
       	
       	if (bCheckOK)
       		return null;
       	else
       		return msg;
    }
 
    	
    //Check path
    public static boolean sendEmail(String body,Properties props ) {
    	if (body==null || body.length()<2)
    		return true;
   	
    	String host=props.getProperty("mail.smtp.host", "smtp.gmail.com");
    	String port=props.getProperty("mail.smtp.port", "587");
    	String auth=props.getProperty("mail.smtp.auth", "true");
    	String from=props.getProperty("mail.from", "hdspacesmonitor@gmail.com");
    	gmailAccount=props.getProperty("mail.smtp.account", "hdspacesmonitor@gmail.com");
    	gmailPassword=props.getProperty("mail.smtp.password", "Psapi123!");
    	String to=props.getProperty("mail.to", "fan8118@gmail.com");
    	String cc=props.getProperty("mail.cc", "");
    	String hostname=props.getProperty("host.name", "Unknown");
    	String errorTitle=props.getProperty("mail.error.title", "Error: Hard Disk Free Space isn't enough");
    	String warningTitle=props.getProperty("mail.warning.title", "Warning: Hard Disk Free Space isn't enough");
    	String alwaysEmailSend=props.getProperty("always.email.send", "No");
    	
    	if (!"error".equalsIgnoreCase(emailTitleKey))
    		errorTitle=props.getProperty("mail.info.title", "Info: Hard Disk Free Space usage ");
    	
    	if (to.isEmpty()) {
    		System.out.println(SendEmailTLS.getCurrentTime() + " Error: Missing email recipients.\n");
    		return true;
    	}
    	
		InetAddress ip = null;
    	try{
    		ip = InetAddress.getLocalHost();
    		if (hostname==null || hostname.length()<1 && (ip.getHostName()!=null && ip.getHostName().length()>1))
    			hostname = ip.getHostName();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
		String subject=errorTitle + " in "  + hostname;
				
    	String sSend=SendEmailTLS.send(gmailAccount, gmailPassword, to, subject, body);
    	if (!"OK".equalsIgnoreCase(sSend)) {
    		System.out.println(SendEmailTLS.getCurrentTime() + " your email was not sent out.mssage=" + body);
    	}
    	System.out.println(SendEmailTLS.getCurrentTime() + " sent successfully.title=" + subject + "\nbody=" + body);
  		return true;
    }
    

    public static long getFolderSize(String path) {
    	try{
    		File folder = new File(path);
    		long size = FileUtils.sizeOfDirectory(folder);
    		return size; // in bytes
    	}catch(Exception e) {
    		System.out.println(e.getMessage());
    	}
    	return -1;
    }
    
    public static String formatSpace(long size){
    	String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = (int) (Math.log10(size) / 3);
        double unitValue = 1 << (unitIndex * 10);
     
        String readableSize = new DecimalFormat("#,##0.#")
                                    .format(size / unitValue) + ""
                                    + units[unitIndex];
        
        return readableSize;
    }    
    
    /*
    public static long folderSize(File directory) {
        long length = 0;

        if (directory.isFile())
             length += directory.length();
        else{
            for (File file : directory.listFiles()) {
                 if (file.isFile())
                     length += file.length();
                 else
                     length += folderSize(file);
            }
        }

        return length;
    }   

    
    public static long fileSize(File root) {
        if(root == null){
            return 0;
        }
        if(root.isFile()){
            return root.length();
        }
        try {
            if(isSymlink(root)){
                return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        long length = 0;
        File[] files = root.listFiles();
        if(files == null){
            return 0;
        }
        for (File file : files) {
            length += fileSize(file);
        }

        return length;
    }

    private static boolean isSymlink(File file) throws IOException {
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }
  */     
    
}

