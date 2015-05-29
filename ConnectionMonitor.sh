#Author: Cory Ma
#Creation Date: 1/13/15
#Due Date: 4/9/15
#Course: CSC411
#Professor: Dr. Frye
#Assignment: Project
#Filename: InitConfigs.sh
#Purpose: Script file to compile and run the application.

if [ ! -s PingSites.properties ] || [ ! -s Emailer.properties ]; then
    echo 'A config file was not found, please run InitConfigs.sh and read the readme for more information.'
else
    javac -cp ".:./lib/*:./org/cory/libraries/*" *.java
    java -cp ".:./lib/*:./org/cory/libraries/*" MonitorApp
fi
