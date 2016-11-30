# event-logger  
![Image 1](https://raw.githubusercontent.com/pedja1/event-logger/master/_market/device-2014-04-18-224223_nexus4_angle1.png "Image 1")
![Image 2](https://raw.githubusercontent.com/pedja1/event-logger/master/_market/device-2014-04-18-224223_nexus4_portrait.png "Image 2")


## Easily keep track of what your phone is doing.

### Event Logger will keep track of the following events:

☑ WiFi Enabled/Disabled  
☑ WiFi Connected/Disconnected  
☑ Bluetooth Enabled/Disabled  
☑ GPS Enabled/Disabled  
☑ Power Connected/Disconnected  
☑ Orientation Changed  
☑ Locale Changed  
☑ Screen On/Off  
☑ Screen Unlocked  
☑ SMS Received  
☑ App Started  
☑ Call Events(Incoming/Outgoing)  
☑ Headphones Plugged/Unplugged  
☑ Media Scanner Started/Stopped  
☑ Phone Booted  
☑ Phone Shutting Down/Restarting  
☑ User Changed Time/Date/Timezone  
☑ Airplane Mode On/Off  
☑ Battery Level Ok/Low  
☑ Wallpaper Changed  
☑ Volume Changed  

### Events that works only with xposed framework:
☑ Media Play/Pause  

### Events to be added in future releases:

☐ HDMI Plugged/Unplugged  
☐ NFC On/Off  
☐ Service Started/Stopped  
☐ Mobile Network On/Off  
☐ SD Card Removed/Inserted/Mounted/Unmounted  
Suggestions are welcome, but please be realistic  

## TODO:
☐ You tell me  

## Bugs:
☐ Sometimes events are added to the list without them actually happening(Headphone plugged, volume level changed, orientation changed)  
☐ When app is killed by the system due to low memory (on android 4.4) it doesn't get restarted - only happens on 4.4+  

## PERMISSIONS:
• ACCESS_WIFI_STATE - to determine if wifi is on/off  
• ACCESS_NETWORK_STATE - determine connectivity change (connected/disconnected)  
• BLUETOOTH - determine bluetooth state (on/off)  
• BLUETOOTH_ADMIN - determine bluetooth state (on/off)  
• RECEIVE_SMS - intercept incoming sms(only phone number is stored)  
• READ_PHONE_STATE - detect incoming calls  
• PROCESS_OUTGOING_CALLS - detect outgoing calls  
• INTERNET - used for ads and sending crash reports  
• RECEIVE_BOOT_COMPLETED - detect that phone has booted up  
• ACCESS_FINE_LOCATION - detect GPS state (on/off)  
• GET_TASKS - get running applications on a device  
• RESTART_EVENT_SERVICE - internal permission, used to restart service when killed by sistem  

[DOWNLOAD](http://play.google.com/store/apps/details?id=rs.pedjaapps.eventlogger)
