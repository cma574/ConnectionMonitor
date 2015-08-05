# ConnectionMonitor
## Synopsis
ConnectionMonitor is an application used for monitoring network latency and connectivity. It utilizes the system's ping command and uses the response to determine information about the destination. Results are written results to a log file, using average latency and standard deviations that are recalculated once every five hours to determine slow network speed.<br>
E-mailed reports will also be sent out once every few hours, depending on a set value, reporting on max latency and number of times each particular site went down. Should a site become unreachable for more than fifteen seconds, an e-mail will also be sent out reporting this after each unreachable site becomes reachable again. This delay is there to account for the occasional lost ping.<br>
This application was created to monitor and identify network problems and the locations that they might be occurring in, and should be used to test several different locations at the same time, at least one within the network and one outside of the network.<br>
## Setup and Running the Application
The computer must have jdk7, MySQL installed, and run off a Linux based machine.<br>
Before running for the first time, run the InitConfigs.sh script. This will create two .properties files. PingSites.properties and DBEmailer.properties, and populate it with a template to use both. Edit these with user's choice of text editors. A database must also be set up to receive the data. At this point, only MySQL is available in the code.<br><br>
PingSites.properties contains starting information on ping destinations, with each entry formatted in JSON format. Listed below is the template that is generated and an explanation of each field:
<pre>PingSite={"address":"", "average":, "stddev":, "tolerance":3}</pre>
PingSite - User defined identifier for the name of the field, replace this with whatever name you would like to associate with it without whitespace, i.e. Google, MyServer, etc.<br>
address - Destination for the ping command. Place this field inside the double quotes with the IP Address or URL of the destination of the ping.<br>
average - Starting average latency, calculated by the user. This field is a floating point value calculated in milliseconds.<br>
stddev - Starting standard deviation for the average, calculated by the user. This field is a floating point value calculated in milliseconds.<br>
tolerance - Number of standard deviations slower than the average the latency needs to be recorded as to be registered as a slow connection. This field needs to be an integer. The default is 3.<br><br>
DBEmailer.properties contains the information required to set up e-mail reporting and accessing the database. Currently the application has settings for Gmail addresses only. Listed below is the template generated and an explanation of each field:
<pre>DBName=
DBUser=
DBPassword=
EmailAddress=
EmailPassword=
ReportFrequency=
NotifyList=
EmergencyNotifyList=
StationName=</pre>
DBName - Name of the database<br>
DBUser - User name to access the database<br>
DBPassword - Password to access the database<br>
EmailAddress - Email address to send report from. This should be a Gmail address.<br>
EmailPassword - Email address' password<br>
ReportFrequency - Number of hours between regular reports<br>
NotifyList - E-mail addresses of people to send regular reports to. Separate each address with a ','.<br>
EmergencyNotifyList - E-mail addresses of people to send emergency reports to, should the site go down for 15 seconds or more. Separate each address with a ','.<br>
StationName - Optional setting for use if multiple ConnectionMonitors are implemented. This value with a ' - ' is appended to the beginning of the each email subject.<br><br>
A MySQL database must be set up with the credentials listed in DBEmailer.properties. Once the database is created, access the database and run the following commands:
<pre>CREATE TABLE IF NOT EXISTS log (pklogid BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, fksiteid INT, fkstatusid INT, pingtime DATETIME, ipaddress CHAR(15), latency FLOAT);
CREATE TABLE IF NOT EXISTS site (pksiteid INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name CHAR(15), address CHAR(30));
CREATE TABLE IF NOT EXISTS status (pkstatusid INT NOT NULL PRIMARY KEY, statustype CHAR(20));
INSERT INTO status VALUES (1, "Unreachable");
INSERT INTO status VALUES (2, "Reachable Again");
INSERT INTO status VALUES (3, "Slow");</pre>
To run the application, run the ConnectionMonitor.sh script. This will check if the properties files exist and are not empty before compiling the .java files and launching the application. There are no guarantees as to what will happen if the .properties files are not properly populated.<br>
To terminate the application gracefully type in q or Q then hit Enter.
## Planned Future Development
Encryption for passwords in config files<br>
Speed between sites<br>
Dynamic adjustment of report frequency<br>