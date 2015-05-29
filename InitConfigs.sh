fileCreated=false

if [ ! -s PingSites.properties ]; then
    echo 'PingSite={"site":"", "average":, "stddev":, "tolerance":3}' >> PingSites.properties
    echo 'PingSites.properties created, please fill in template before running ConnectionMonitor.'
    fileCreated=true
fi

if [ ! -s DBEmailer.properties ]; then
	echo 'DBName='>>DBEmailer.properties
    echo 'DBUser='>>DBEmailer.properties
    echo 'DBPassword='>>DBEmailer.properties
    echo 'EmailAddress='>>DBEmailer.properties
    echo 'EmailPassword='>>DBEmailer.properties
    echo 'ReportFrequency='>>DBEmailer.properties
    echo 'NotifyList='>>DBEmailer.properties
    echo 'EmergencyNotifyList='>>DBEmailer.properties
    echo 'DBEmailer.properties created, please fill in template before running ConnectionMonitor.'
    fileCreated=true
fi

if [ fileCreated==true ]; then
    echo 'Check readme for more information on properties files.'
fi
