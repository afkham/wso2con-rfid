import wiringpi2
import time

#wiringpi2.wiringPiSetupGpio() 
wiringpi2.wiringPiSetup()
#wiringpi2.wiringPiSetupSys()

PIN=4
FREQUENCY1=349
FREQUENCY2=740

def playTone():
  wiringpi2.softToneCreate(PIN)
  wiringpi2.softToneWrite(PIN,FREQUENCY1)
  time.sleep(0.10)
  wiringpi2.softToneWrite(PIN,FREQUENCY1)
  time.sleep(0.10)
  wiringpi2.softToneWrite(PIN,0)
