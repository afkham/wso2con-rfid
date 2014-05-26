import MFRC522 as rfid
import signal
import RPi.GPIO as GPIO
import httplib

continue_reading = True
# Capture SIGINT
def end_read(signal,frame):
    global continue_reading
    print "Ctrl+C captured, ending read."
    continue_reading = False
    GPIO.output(POWER_LED,False)
    GPIO.cleanup() 
    httpServ.close()

signal.signal(signal.SIGINT, end_read)
httpServ = httplib.HTTPConnection("127.0.0.1", 8084)
httpServ.connect()

POWER_LED = 11
CARD_READ_LED = 12

# GPIO.setmode(GPIO.BCM) ## Use board pin numbering
GPIO.setup(POWER_LED, GPIO.OUT)
GPIO.setup(CARD_READ_LED, GPIO.OUT)
GPIO.output(POWER_LED,True)

while continue_reading:
    try:
      rfidValue = rfid.readRFID()
      if rfidValue != -1:
        GPIO.output(CARD_READ_LED,True)
        print "RFID=" + rfidValue
        httpServ.request('PUT', '/rfid', rfidValue)
        response = httpServ.getresponse()
        if response.status == httplib.OK:
          response.read()
        GPIO.output(CARD_READ_LED,False)
    except Exception, e:
        GPIO.output(CARD_READ_LED,False)
        print("Error :" + e)
