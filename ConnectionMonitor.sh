if [ ! -s PingSites.properties ] || [ ! -s DBEmailer.properties ]; then
    echo 'A config file was not found, please run InitConfigs.sh and read the readme for more information.'
else
    javac -cp ".:./lib/*:./org/cory/libraries/*" *.java
    java -cp ".:./lib/*:./org/cory/libraries/*" MonitorApp
fi
