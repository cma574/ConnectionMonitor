#Author: Cory Ma
#Creation Date: 1/27/15
#Due Date: 4/9/15
#Course: CSC411
#Professor: Dr. Frye
#Assignment: Project
#Filename: InitConfigs.sh
#Purpose: Script file with information to generate templates for config files.

fileCreated=false

if [ ! -s PingSites.properties ]; then
    echo 'PingSite={"site":"", "average":, "stddev":, "tolerance":3}' >> PingSites.properties
    echo 'PingSites.properties created, please fill in template before running ConnectionMonitor.'
    fileCreated=true
fi

if [ ! -s Emailer.properties ]; then
    echo 'Email='>> Emailer.properties
    echo 'Password='>>Emailer.properties
    echo 'NotifyList='>>Emailer.properties
    echo 'EmergencyNotifyList='>>Emailer.properties
    echo 'Emailer.properties created, please fill in template before running ConnectionMonitor.'
    fileCreated=true
fi

if [ fileCreated==true ]; then
    echo 'Check readme for more information on properties files.'
fi
