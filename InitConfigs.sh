fileCreated=false

if [ ! -s PingSites.properties ]; then
    echo 'PingSite={"site":"", "average":, "stddev":, "tolerance":1,\\\n notifyList:"", "emergencyNotifyList":""}' >> PingSites.properties
    echo 'PingSites.properties created, please fill in template before running ConnectionMonitor.'
    fileCreated=true
fi

if [ ! -s Emailer.properties ]; then
    echo 'Email='>> Emailer.properties
    echo 'Password='>>Emailer.properties
    echo 'Emailer.properties created, please fill in template before running ConnectionMonitor.'
    fileCreated=true
fi

if [ fileCreated==true ]; then
    echo 'Check readme for more information on properties files.'
fi
