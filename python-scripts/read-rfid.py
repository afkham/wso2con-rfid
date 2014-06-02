import MFRC522 as rfid
import tone as tone
import signal
import RPi.GPIO as GPIO
import httplib
from threading import Thread
from Queue import Queue
import time
import logging

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
    httpServ.close()

signal.signal(signal.SIGINT, end_read)
httpServ = httplib.HTTPConnection("127.0.0.1", 8084)

POWER_LED = 13
CARD_READ_LED = 11

# GPIO.setmode(GPIO.BCM) ## Use board pin numbering
GPIO.setup(POWER_LED, GPIO.OUT)
GPIO.setup(CARD_READ_LED, GPIO.OUT)
GPIO.output(POWER_LED,True)

rfidQueue = Queue(250)

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
                    log.info("RFID=" + rfidValue)
                    rfidQueue.put(rfidValue)
                    GPIO.output(CARD_READ_LED,False)
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
                    httpServ.connect()
                    rfidValue = rfidQueue.get()
                    rfidQueue.task_done()
                    httpServ.request('PUT', '/rfid', rfidValue)
                    response = httpServ.getresponse()
                    if response.status == httplib.OK:
                        response.read()
                    #repeat = False
                time.sleep(1)
            except Exception, e:
                log.error("Error while sending RFID to server: " + str(e))
                #repeat = True
                #time.sleep(0.5)

ProducerThread().start()
ConsumerThread().start()

while continue_reading:
    time.sleep(1)
