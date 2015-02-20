if [ ! -s PingSites.properties ]; then
    echo 'PingSites.properties not found, please run InitConfigs.sh and read the readme for more information.'
else
    javac -cp ".:./lib/*:./org/cory/libraries/*" *.java
    java -cp ".:./lib/*:./org/cory/libraries/*" MonitorApp
fi
