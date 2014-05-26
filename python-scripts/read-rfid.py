import MFRC522 as rfid
import signal
import RPi.GPIO as GPIO

continue_reading = True
# Capture SIGINT
def end_read(signal,frame):
    global continue_reading
    print "Ctrl+C captured, ending read."
    continue_reading = False
    GPIO.cleanup() # Suggested by Marjan Trutschl

signal.signal(signal.SIGINT, end_read)

while continue_reading:
    print rfid.readRFID()