#!/bin/bash

logfile="process-check.log"

case "$(ps -ef | grep java | wc -l)" in
1)  echo "java not running, restarting java:     $(date)" >> $logfile
    nohup /home/pi/wso2con-rfid/distribution/target/wso2con-rfid-0.0.1-SNAPSHOT/run.sh &
    ;;
3)  echo "java running, all OK:     $(date)" >> $logfile
    ;;
esac


case "$(ps -ef | grep read-rfid.py | wc -l)" in
1)  echo "python not running, restarting python:     $(date)" >> $logfile
    nohup sudo python /home/pi/wso2con-rfid/python-scripts/read-rfid.py &
    ;;
3)  echo "python running, all OK:     $(date)" >> $logfile
    ;;
esac