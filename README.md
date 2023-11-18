# Hoverdroid App
Android app companion for flashed hoverboards in UART mode.

# Getting started
1. Flash your hoverboard using the [EFeru repo](https://github.com/EFeru/hoverboard-firmware-hack-FOC)
2. Connect the hoverboard to a usb adapter
3. Connect the android to the usb adapter


# Changelog

November 2023
1. Peer to peer remote controller using nearby library

4 May 2022
1. Click on the header to change controller (Joystick or Lever)
2. Add Amazon Chime lib 

13 Apr 2022
1. Joystick Fragment
2. Serial Connection
3. First Commmit


#Videos

Fun Intro
[<img src="https://img.youtube.com/vi/sFhyfpzZz6A/hqdefault.jpg" width="600" height="300"
/>](https://www.youtube.com/embed/sFhyfpzZz6A)


# Collaboration

Please help!! This project as an angle on recycled robotics

# UX 

https://www.figma.com/file/kdszIk7J11U8u3yfG1jgMt/Untitled?node-id=0%3A1

# Future Plans

## Acceleromenter/Gyro Control on peer devices
The hoverboard needs to follow the phone direction taken from the gyroscope.


## Autonomous Drive
Using nearby library is possible to stream proximity camera to a linux server that can drive the
car.



## Autonomous Control
The new android phone have a built in depth depth camera. Is it feasible to build such a system ?



## Notes
Wireless debug
adb tcpip 5555
Get the device IP from wifi details of the phone
adb connect 192.168.1.133:5555

To be dev in xiaomi tap MIU version
=======
>>>>>>> 13cf253abbc7c41f1eeb5c8fabb2bc6e0f7a9589
