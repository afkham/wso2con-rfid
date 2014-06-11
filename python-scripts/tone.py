import wiringpi2
import time

#wiringpi2.wiringPiSetupGpio()
wiringpi2.wiringPiSetup()
#wiringpi2.wiringPiSetupSys()

PIN=7
FREQUENCY1=1500
FREQUENCY2=1600

wiringpi2.softToneCreate(PIN)

def playTone():
    wiringpi2.softToneWrite(PIN,FREQUENCY1)
    time.sleep(0.1)
    wiringpi2.softToneWrite(PIN,FREQUENCY2)
    time.sleep(0.2)
    wiringpi2.softToneWrite(PIN,0)

#while True:
#  playTone()
#  time.sleep(1)