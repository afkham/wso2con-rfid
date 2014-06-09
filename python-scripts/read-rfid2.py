import MFRC522 as rfid
import tone as tone
import signal
import RPi.GPIO as GPIO
from threading import Thread
from Queue import Queue
import time
import logging
import requests

log = logging.getLogger('rfid-reader')
hdlr = logging.FileHandler('/home/pi/wso2con-rfid/rfid-reader.log')
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
log.addHandler(hdlr) 
log.setLevel(logging.INFO)

continue_reading = True
# Capture SIGINT
def end_read(signal,frame):
    global continue_reading
    print  "Ctrl+C captured, ending read."
    log.info("Ctrl+C captured, ending read.")
    continue_reading = False
    GPIO.output(POWER_LED,False)
    GPIO.cleanup()

signal.signal(signal.SIGINT, end_read)

POWER_LED = 13
CARD_READ_LED = 11

# GPIO.setmode(GPIO.BCM) ## Use board pin numbering
GPIO.setup(POWER_LED, GPIO.OUT)
GPIO.setup(CARD_READ_LED, GPIO.OUT)
GPIO.output(POWER_LED,True)

rfidQueue = Queue(50)

class ProducerThread(Thread):
    def run(self):
        global rfidQueue
        global continue_reading
        while continue_reading:
            try:
                rfidValue = rfid.readRFID()
                if rfidValue != -1:
                    GPIO.output(CARD_READ_LED,True)
                    tone.playTone()
                    #log.info("RFID=" + rfidValue)
                    rfidQueue.put(rfidValue)
                    GPIO.output(CARD_READ_LED,False)
                    time.sleep(0.5)
                else:
                    time.sleep(0.005)
            except Exception, e:
                GPIO.output(CARD_READ_LED,False)
                log.info("Error :" + str(e))


class ConsumerThread(Thread):
    def run(self):
        global rfidQueue
        #repeat = True
        global continue_reading
        while continue_reading:
            try:
                if not rfidQueue.empty():
                    print "Items to be processed" 
                    rfidValue = rfidQueue.get()
                    print "Processing RFID " + rfidValue
                    rfidQueue.task_done()
                    print "Sending request..."
                    r = requests.put("http://127.0.0.1:8084/rfid",data=rfidValue)
                    print "Request sent"
                    print "status=" + str(r.status_code)
                    #repeat = False
                time.sleep(0.5)
            except Exception, e:
                log.error("Error while sending RFID to server: " + str(e))
                #repeat = True
                #time.sleep(0.5)

ProducerThread().start()
ConsumerThread().start()

while continue_reading:
    time.sleep(1)
