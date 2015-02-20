# ConnectionMonitor
## Synopsis
ConnectionMonitor is an application used for monitoring network latency and connectivity. It utilizes the system's ping command and uses the response to determine information about the destination. Results are written results to a log file, using average latency and standard deviations that are recalculated once every six hours to determine slow network speed.
## Setup and Running the Application
The computer must have jdk7 installed and run off a Linux based machine.<br>
Before running for the first time, run the InitConfigs.sh script. This will create two .properties files. PingSites.properties and Emailer.properties, and populate it with a template to use both. Edit these with user's choice of text editors.<br><br>
PingSites.properties contains starting information on ping destinations, with each entry formatted in JSON format. Listed below is the template that is generated and an explanation of each field:
<pre>PingSite={"site":"", "average":, "stddev":, "tolerance":1,\
 notifyList:"", "emergencyNotifyList":""}</pre>
PingSite - User defined identifier for the name of the field, replace this with whatever name you would like to associate with it without whitespace, i.e. Google, MyServer, etc.<br>
site - Destination for the ping command. Place this field inside the double quotes with the IP Address or URL of the destination of the ping.<br>
average - Starting average latency, calculated by the user. This field is a floating point value calculated in milliseconds.<br>
stddev - Starting standard deviation for the average, calculated by the user. This field is a floating point value calculated in milliseconds.<br>
tolerance - Number of standard deviations slower than the average the latency needs to be recorded as to be registered as a slow connection. This field needs to be an integer. The default is 1.<br>
notifyList - E-mail address of people to send regular reports to. Place this field inside the double quotes, separating each address with a ','.<br>
emergencyNotifyList - E-mail address of people to send emergency reports to, should the site go down for 30 seconds or more. Place this field inside the double quotes, separating each address with a ','.<br>
Note: The \ is only there for human readability, so that Java will recognize the entry as continuing to the next line. If removed, the entire entry must be combined into one line.<br><br>
Emailer.properties contains the information required to set up e-mail reporting. Currently the application has settings for Gmail addresses only. Listed below is the template generated and an explanation of each field:
<pre>Email=
Password=</pre>
Email - Email address to send report from.<br>
Password - Email address' password<br>
Note: Currently Email reporting has not been implemented so this file is not yet needed.<br><br>
To run the application, run the ConnectionMonitor.sh script. This will check if the properties files exist and are not empty before compiling the .java files and launching the application. There are no guarantees as to what will happen if the .properties files are not properly populated.<br>
To terminate it, close the terminal window or use ^c.
## Planned Future Development
Database<br>
E-mail reporting<br>
Speed between sites<br>

