# hdcheck
1. Check folder size:
   java -jar hdcheck.jar  <path>
   Example: java -jar 	hdcheck.jar  "c:\Program Files"
   
2. Monitor Drivers and Folders HD usage. 
   Send alter email once exceeds the limitation
   
   hdmonitor.properties:
   
#host.name is used to show the hostname in email subject.so you know which server has issue.
host.name=My Web Server

#To whom the alter email will be sent
mail.to=york9715@gmail.com
#here is the email subject
mail.error.title=Error: Hard Disk Free Space isn't enough 

#list all drivers here you want to monitor
#Alter email will be send once the Free Spacel is less than limitation
C\:=4G
D\:=2G

#List all pathes you want to monitor.
#Maximum 5 paths can be monitored.
#Different from Driver available space, Path's usage size will be monitored.
#Email will be sent once the usage size exceeds the limitation.
#PATH1 -PATH5
PATH1=C\:\\tools
C\:\\tools=400M

3. Use "Export -> Java -> Runnable JAR file -> Package required libraties into generated JAR"
  to genearate hdcheck.jar
  
   