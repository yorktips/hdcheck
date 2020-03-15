package com.tce.hdcheck;


import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
//import java.net.InetAddress;

public class App {
	static String gmailAccount="";
	static String gmailPassword="";
	
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
    	
    	for(String drive:drivers) {
    		checkDriveFreeSpace(drive,props);
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
}

