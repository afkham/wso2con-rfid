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
    GPIO.cleanup() 
    httpServ.close()

signal.signal(signal.SIGINT, end_read)
httpServ = httplib.HTTPConnection("127.0.0.1", 8084)
httpServ.connect()

while continue_reading:
    rfidValue = rfid.readRFID()
    print "RFID=" + rfidValue
    httpServ.request('PUT', '/rfid', rfidValue)
    response = httpServ.getresponse()
    if response.status == httplib.OK:
      response.read()
