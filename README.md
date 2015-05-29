# ConnectionMonitor
## Notes for Dr. Frye
The formatting for this readme is in MarkDown format, meant to be read by GitHub's readme system.
I have also included some scripts with some hard coded username's and passwords for the database I developed on, this is more of a proof of concept for how the database can currently be set up. In the future it would include commands to actually create the database, but I do not have permissions to do that right now so I haven't included it.
Also, so you can test it faster, I have adjusted the timing for regular e-mail reports to be sent out once every fifteen minutes instead of hourly.
## Synopsis
ConnectionMonitor is an application used for monitoring network latency and connectivity. It utilizes the system's ping command and uses the response to determine information about the destination. Results are written results to a log file, using average latency and standard deviations that are recalculated once every five hours to determine slow network speed.<br>
E-mailed reports will also be sent out once an hour reporting on max latency and number of times each particular site went down. Should a site become unreachable for more than fifteen seconds, an e-mail will also be sent out reporting this after each unreachable site becomes reachable again. This delay is there to account for the occasional lost ping.<br>
This application was created to monitor and identify network problems and the locations that they might be occurring in, and should be used to test several different locations at the same time, at least one within the network and one outside of the network.<br>
## Setup and Running the Application
The computer must have jdk7 and mySQL installed and run off a Linux based machine.<br>
Before running for the first time, run the InitConfigs.sh script. This will create two .properties files. PingSites.properties and Emailer.properties, and populate it with a template to use both. Edit these with user's choice of text editors.<br><br>
PingSites.properties contains starting information on ping destinations, with each entry formatted in JSON format. Listed below is the template that is generated and an explanation of each field:
<pre>PingSite={"address":"", "average":, "stddev":, "tolerance":3}</pre>
PingSite - User defined identifier for the name of the field, replace this with whatever name you would like to associate with it without whitespace, i.e. Google, MyServer, etc.<br>
address - Destination for the ping command. Place this field inside the double quotes with the IP Address or URL of the destination of the ping.<br>
average - Starting average latency, calculated by the user. This field is a floating point value calculated in milliseconds.<br>
stddev - Starting standard deviation for the average, calculated by the user. This field is a floating point value calculated in milliseconds.<br>
tolerance - Number of standard deviations slower than the average the latency needs to be recorded as to be registered as a slow connection. This field needs to be an integer. The default is 3.<br>
Emailer.properties contains the information required to set up e-mail reporting. Currently the application has settings for Gmail addresses only. Listed below is the template generated and an explanation of each field:
<pre>Email=
Password=
NotifyList=
EmergencyNotifyList=</pre>
Email - Email address to send report from. This should be a Gmail address.<br>
Password - Email address' password<br>
NotifyList - E-mail addresses of people to send regular reports to. Separate each address with a ','.<br>
emergencyNotifyList - E-mail addresses of people to send emergency reports to, should the site go down for 15 seconds or more. Separate each address with a ','.<br><br>
To run the application, run the ConnectionMonitor.sh script. This will check if the properties files exist and are not empty before compiling the .java files and launching the application. There are no guarantees as to what will happen if the .properties files are not properly populated.<br>
To terminate the application gracefully type in q or Q then hit Enter.
## Planned Future Development
ConfigManager for reading and writing back to the configs and creating PingSites on the fly<br>
Encryption for passwords in config files<br>
Speed between sites<br>
