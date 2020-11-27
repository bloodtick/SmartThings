/**
*  Copyright 2020 Bloodtick
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*  Fully Kiosk Browser Device
*  Thanks to Arn Burkhoff similar driver concerning TTS functions. 
*
*  Update: Bloodtick Jones
*  Date: 2020-09-07
*
*  1.0.00 2020-09-07 First release to support Hubitat. Must have Fully 1.40.3 or greater to use becuase of new command struture.
*  1.0.01 2020-09-08 Added runIn delay in parse for sendPostCmd
*  1.1.00 2020-09-26 Added alarm, chime and playsounds at request. Updates include preferences for three streams. Only tested on Fire HD 8.
*  1.1.01 2020-10-04 Corrected alarmOff in off() command
*  1.1.02 2020-10-06 Added else around "Image Capture" in capability
*  1.1.03 2020-11-21 Added "playText" based upon https://docs.hubitat.com/index.php?title=Driver_Capability_List#AudioNotification
*  1.1.04 2020-11-27 Added all commands from https://docs.hubitat.com/index.php?title=Driver_Capability_List#AudioNotification
*
*  NOTE: To use on Hubitat enviroment you need to comment out carouselTile() in the metadata area around line 124
*/

import groovy.json.*
import java.net.URLEncoder

private getVersionNum()   { return "1.1.04" }
private getVersionLabel() { return "Fully Kiosk Browser Device, version ${getVersionNum()}" }

Boolean isST() { return (getPlatform() == "SmartThings") }

metadata {
    definition (name: "Fully Kiosk Browser Device", namespace: "bloodtick", author: "Hubitat/SmartThings", ocfDeviceType: "oic.d.switch") {
        capability "Actuator"
        capability "Switch"
        capability "Switch Level"
        capability "Sensor"
        capability "Refresh"
        capability "Health Check"
        capability "Battery"
        capability "Speech Synthesis"
        if (isST()) 
            capability "Image Capture"
        else 
            capability "ImageCapture"
        capability "Touch Sensor"
        capability "Motion Sensor"
        capability "Tone"
        capability "Alarm"
        capability "AudioNotification"

        attribute "wifiSignalLevel", "number"
        attribute "volume", "number"
        attribute "screen", "string"
        attribute "screensaver", "string"
        attribute "currentPage", "string"
        attribute "injectJsCode", "string"
        attribute "altitude", "number"
        attribute "latitude", "number"
        attribute "longitude", "number"
        attribute "s3url", "string"
        attribute "s3key", "string"
        attribute "touch", "string"
        attribute "status", "string"

        command "screenOn"
        command "screenOff"
        command "startScreensaver"
        command "stopScreensaver"
        command "triggerMotion"
        command "toForeground"
        command "fetchSettings"
        command "fetchInfo"
        command "loadStartURL"
        command "loadURL", ["string"]
        command "setVolumeAndSpeak", ["number", "string"]
        command "setSpeechVolume", ["number", "number"]
        command "setVolume", ["number", "number"]
        command "setScreenBrightness", ["number"]
        command "setScreensaverTimeout", ["number"]
        command "setStringSetting", ["string", "string"]
        command "setBooleanSetting", ["string", "string"]
        command "getStringSetting", ["string", "string"]
        command "getBooleanSetting", ["string", "string"]
        command "sendGenericCommand", ["string"]
        command "speechTestAction"
        command "speechVolumeUpdate"
        command "getCamshot"
        command "getScreenshot"
        command "fetchImageS3", ["string"]
        command "chime"
        command "alarm"
        command "playSound",["string"]
        command "stopSound"
        command "alarmOff"
        command "setMediaVolume",["string"]
        command "setAlarmVolume",["string"]
        command "setNotifyVolume",["string"]
        //command "playText",["string", "number"]
        //command "playTextAndResume",["string", "number"]
    }

    // simulator metadata
    simulator {}

    // UI tile definitions    
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: false){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/tablet.png", backgroundColor:"#00a0dc", nextState:"off"
                attributeState "off", label:'${name}', action:"switch.on", icon:"https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/tablet.png", backgroundColor:"#ffffff", nextState:"on"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
        }

        //carouselTile("cameraDetails", "device.image", width: 2, height: 2) { } // Not Compatible with Hubitat. Uncomment for SmartThings Classic UI.

        valueTile("currentIP", "device.currentIP", height: 1, width: 3, decoration: "flat") {
            state "default", label:'[ Current IP ]\n${currentValue}'
        }
        valueTile("lastPoll", "device.lastPoll", height: 1, width: 3, decoration: "flat") {
            state "default", label:'[ Last Response ]\n${currentValue}'
        }        
        valueTile("appVersionName", "device.appVersionName", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ App Version ]\n${currentValue}'
        }        
        valueTile("screenBrightness", "device.screenBrightness", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Brightness ]\n${currentValue}'
        }
        valueTile("isScreenOn", "device.isScreenOn", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Screen On ]\n${currentValue}'
        }
        valueTile("isInScreensaver", "device.isInScreensaver", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Screen Saver ]\n${currentValue}'
        }
        valueTile("battery", "device.battery", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Battery ]\n${currentValue}%'
        }
        valueTile("wifiSignalLevel", "device.wifiSignalLevel", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Wifi Level ]\n${currentValue}'
        }
        valueTile("timeToScreensaverV2", "device.timeToScreensaverV2", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Screen Saver ]\n${currentValue} sec'
        }        
        standardTile("screensaver", "device.screensaver", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "off", label: 'ScrnSaver Off', action: "startScreensaver", icon: "https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/on-blue-3x.png", backgroundColor: "#ffffff"/*, nextState:"off"*/
            state "on", label: 'ScrnSaver On', action: "stopScreensaver", icon: "st.switches.switch.off", backgroundColor: "#ffffff"/*, nextState:"on"*/
        }        
        standardTile("screen", "device.screen", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "on", label: 'Screen On', action: "screenOff", icon: "https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/on-blue-3x.png", backgroundColor: "#ffffff"/*, nextState:"off"*/
            state "off", label: 'Screen Off', action: "screenOn", icon: "st.switches.switch.off", backgroundColor: "#ffffff"/*, nextState:"on"*/
        }        
        standardTile("speechTest", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "default", label:'Speak Test', action:"speechTestAction", icon:"https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/speaker-grey.png"
        }        
        controlTile("speechVolume", "device.volume", "slider", inactiveLabel: false, height: 1, width: 1, range:"(0..100)") {
            state "default", label:'${currentValue}', action:"speechVolumeUpdate"
        }        
        standardTile("refresh", "device.switch", inactiveLabel: false, height: 1, width: 2, decoration: "flat") {
            state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
        }
        standardTile("listSettings", "device.switch", inactiveLabel: false, height: 1, width: 2, decoration: "flat") {
            state "default", label:'Settings', action:"fetchSettings", icon:"st.secondary.refresh-icon"
        }
        standardTile("camshot", "device.image", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "default", label:'Camshot', action:"take", icon:"st.camera.take-photo"
        }
        standardTile("screenshot", "device.image", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "default", label:'Screenshot', action:"getScreenshot", icon:"st.motion.acceleration.inactive"
        }
        standardTile("alarm", "device.alarm", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "default", label:'Alarm On', action:"alarm", icon:"st.custom.sonos.unmuted"
        }
        standardTile("alarmOff", "device.alarm", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "default", label:'Alarm Off', action:"alarmOff", icon:"st.custom.sonos.muted"
        }
        standardTile("chime", "device.chime", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "default", label:'Chime', action:"chime", icon:"st.Electronics.electronics13"
        }

        main "switch"
        details(["switch","currentIP","lastPoll","appVersionName","isScreenOn","cameraDetails","wifiSignalLevel","isInScreensaver",
                 "battery","screen","screensaver","camshot","screenshot","refresh","speechTest","speechVolume","alarm","alarmOff","listSettings","chime"])
    }
}

preferences {
    input(name:"deviceIp", type:"text", title: "Device IP Address", description: "Device IP Address", defaultValue: "127.0.0.1", required: true, displayDuringSetup: true)
    input(name:"devicePort", type:"number", title: "Device IP Port", description: "Default is port 2323", defaultValue: "2323", required: false, displayDuringSetup: true)
    input(name:"devicePassword", type:"string", title:"Fully Kiosk Browser Password", required: true, displayDuringSetup: true)
    input(name:"deviceMediaUrl", type:"string", title:"Audio Media URL (Chime)", defaultValue:"", required:false)
    input(name:"deviceAlarmUrl", type:"string", title:"Audio Alarm URL", defaultValue:"", required:false)
    input(name:"deviceMediaVolume", type:"number", title:"Media Volume (Chime)", range: "0..100", defaultValue:"100", required:false)
    input(name:"deviceAlarmVolume", type:"number", title:"Alarm Volume", range: "0..100", defaultValue:"100", required:false)
    input(name:"deviceNotfyVolume", type:"number", title:"Notify Volume", range: "0..100", defaultValue:"75", required:false)
    input(name:"deviceMediaStream", type:"number", title:"Media Stream (1-9, default 9)", description: "Media is 9 on Fire HD", range: "1..9", defaultValue:"9", required:false)
    input(name:"deviceAlarmStream", type:"number", title:"Alarm Stream (1-9, default 9)", description: "Alarm is 4 on Fire HD, but use 9 with FKB", range: "1..9", defaultValue:"9", required:false)
    input(name:"deviceNotfyStream", type:"number", title:"Notify Stream (1-9, default 9)", description: "Notify is 2 on Fire HD, but use 9 with FKB", range: "1..9", defaultValue:"9", required:false)
    input(name:"deviceAllowScreenOff", type: "bool", title: "Allow Screen Off Command", description: "Diverts screen off and on commands to screensaver on and off commands. Defaulted to off for Fire tablets", defaultValue: "false", displayDuringSetup: false)
    input(name:"devicePollRateSecs", type: "number", title: "Device Poll Rate (30-600 seconds)", description: "Default is 300 seconds", range: "30..600", defaultValue: "300", displayDuringSetup: false)
    input(name:"deviceS3url", type:"string", title:"AWS Lambda URL (optional)", required: false, displayDuringSetup: false)
    input(name:"deviceS3key", type:"string", title:"AWS Lambda X-Api-Key (if required)", required: false, displayDuringSetup: false)
    input(name:"deviceS3ret", type: "bool", title: "AWS Image Query", description: "Query AWS and fetch image into Smartthings Interface", defaultValue: "false", displayDuringSetup: false)
    input(name:"deviceLogEnable", type: "bool", title: "Enable debug logging", defaultValue: false) 
    input(name:"deviceTraceEnable", type: "bool", title: "Enable trace logging", defaultValue: false)
}

def installed() {
    settings.devicePort = "127.0.0.1"
    settings.devicePort = 2323
    settings.deviceMediaUrl =""
    settings.deviceAlarmUrl =""
    settings.deviceMediaVolume = 100
    settings.deviceAlarmVolume = 100
    settings.deviceNotfyVolume = 75
    settings.deviceMediaStream = 9
    settings.deviceAlarmStream = 9 // this is actually 4, but FKB on Fire HD is using 9
    settings.deviceNotfyStream = 9 // this is actually 2, but FKB on Fire HD is using 9
    settings.deviceAllowScreenOff = false
    settings.devicePollRateSecs = 300
    settings.deviceS3url =""
    settings.deviceS3key =""
    settings.deviceS3ret = false
    settings.deviceLogEnable = false
    settings.deviceTraceEnable = false
    sendEvent(name: "level", value: "50", displayed: false)
    logDebug "Executing 'installed()' with settings: ${settings}"
    initialize()
}

def updated() {
    logDebug "Executing 'updated()' with new preferences: ${settings}"
    initialize()
}

def initialize() {
    logInfo "Executing 'initialize()'"
    unschedule()
    sendEvent(name: "switch", value: "off", displayed: false)
    sendEvent(name: "battery", value: "100", displayed: false)
    speechVolumeUpdate(settings.deviceNotfyVolume)
    setS3url( settings.deviceS3url )
    setS3key( settings.deviceS3key )

    if (device?.hub?.hardwareID ) {
        sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "lan", scheme: "untracked", hubHardwareId: device.hub.hardwareID]), displayed: false)
        sendEvent(name: "checkInterval", value: 1920, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
    } else {
        logInfo "This device is not assigned to a SmartThings Hub"
    }

    state.deviceInfo = ""
    state.listSettings = ""
    state.eventListRunning = false
    state.eventList = []

    if (settings.deviceIp && settings.devicePort && settings.devicePassword) {
        sendEvent(name: "currentIP", value: "${settings.deviceIp}:${settings.devicePort}", displayed: false)
        // start collecting listSettings and deviceInfo from device. I HATE POLLING.       
        clrEvents()
        fetchSettings()
        fetchInfo() // auto refresh with checkInterval delay
        runEvery15Minutes(ping) // same as fetchSettings
    }
    // if webCoRE is installed. Update configuration values. 
    sendEvent(name: "touch", value: (new Date().format("yyyy-MM-dd h:mm:ss a", location.timeZone)), isStateChange: true, displayed: false)
}

def on() {
    screenOn()
}

def off() {
    screenOff()
    if(state?.alarm==true) { 
        alarmOff()
        state.alarm=false
    }
}

def setLevel(level) {
    sendEvent(name: "level", value: level, descriptionText: "Screen Brightness is ${level}") // checked and updated in update()
    setScreenBrightness(level)
}

def take() {
    logDebug "Executing 'take()'"
    getCamshot()
}

def setScreenBrightness(level) {
    def value = Math.round(level.toInteger()*2.55).toString()
    setStringSetting("screenBrightness", "${value}")
}

def speechTestAction() {
    setVolumeAndSpeak(device.currentValue("volume").toInteger(), "Fully Kiosk Browser Speaking Test")
}

def getCamshot() {
    if (state?.listSettings?.remoteAdmin && state?.listSettings?.remoteAdminCamshot && state?.listSettings?.motionDetection) {
        sendGenericCommand("getCamshot")
    } else {
        logDebug "getCamshot not configured - remoteAdmin:${state?.listSettings?.remoteAdmin} remoteAdminCamshot:${state?.listSettings?.remoteAdminCamshot} motionDetection:${state?.listSettings?.motionDetection}"
    }
}

def getScreenshot() {
    if (state?.listSettings?.remoteAdmin && state?.listSettings?.remoteAdminScreenshot) {
        if (device.currentValue("switch") != "on") on() // must be 'on' otherwise you get the screensaver
        sendGenericCommand("getScreenshot")
    } else {
        logDebug "getScreenshot not configured - remoteAdmin:${state?.listSettings?.remoteAdmin} remoteAdminScreenshot:${state?.listSettings?.remoteAdminScreenshot}"
    }
}

def playText(text, level=999)
{
    logDebug "Executing 'playText(${text})'"
    if(level>=0&&level<=100)
        setVolumeAndSpeak(level.toInteger(), text)
    else
        speak(text)
}

def playTextAndRestore(text, level=999)
{
    playText(text, level)
}

def playTextAndResume(text, level=999)
{
    playText(text, level)
}

def playTrack(trackuri, level=999)
{
    logDebug "Executing 'playTrack(${trackuri})'"
    if(level>=0&&level<=100) {
        setMediaVolume(level)
        runIn(10, "setMediaVolume")
    }
    playSound(trackuri)
}

def playTrackAndRestore(trackuri, level=999)
{
    playTrack(trackuri, level)
}

def playTrackAndResume(trackuri, level=999)
{
    playTrack(trackuri, level)
}

def speechVolumeUpdate(level) {
    sendEvent(name: "volume", value: "${level}", descriptionText: "Audio Level is ${level}")
}

def setVolumeAndSpeak(level, text) {
    setNotifyVolume(level)
    speak(text)
    runIn(5, "setNotifyVolume")
}

def setScreensaverTimeout(value) {
    setStringSetting("timeToScreensaverV2", "${value}")
}

def fetchSettings() {
    addEvent(["listSettings", null, null, "cmd=listSettings"])    
}

def fetchInfo() {
    addEvent(["deviceInfo", null, null, "cmd=deviceInfo"])
}

def screenOn() {
    if (settings.deviceAllowScreenOff==false) return stopScreensaver()
    addEvent(["command", "screenOn", null, "cmd=screenOn"])
}

def screenOff() {
    if (settings.deviceAllowScreenOff==false) return startScreensaver()
    addEvent(["command", "screenOff", null, "cmd=screenOff"])
}

def stopScreensaver() {
    addEvent(["command", "stopScreensaver", null, "cmd=stopScreensaver"])
}

def startScreensaver() {
    addEvent(["command", "startScreensaver", null, "cmd=startScreensaver"])
}

def triggerMotion() {
    addEvent(["command", "triggerMotion", null, "cmd=triggerMotion"])
}

def toForeground() {
    addEvent(["command", "toForeground", null, "cmd=toForeground"])
}

def loadStartURL() {
    addEvent(["command", "loadStartURL", null, "cmd=loadStartURL"])
}

def loadURL(String value) {
    addEvent(["command", "loadURL", null, "cmd=loadURL&url=${value}"])
}

def setSpeechVolume(level, stream=settings.deviceNotfyStream) { //backward compatibility
    setNotifyVolume(level) 
}

def setVolume(level, stream=settings.deviceNotfyStream) {
    addEvent(["command", "setAudioVolume", null, "cmd=setAudioVolume&level=${level}&stream=${stream}"])
}

def setMediaVolume(level=settings.deviceMediaVolume) {
    setVolume(level, settings.deviceMediaStream)
}

def setAlarmVolume(level=settings.deviceAlarmVolume) {
    setVolume(level, settings.deviceAlarmStream)
}

def setNotifyVolume(level=settings.deviceNotfyVolume) {
    sendEvent(name: "volume", value: "${level}", descriptionText: "Audio Level is ${volume}")
    setVolume(level, settings.deviceNotfyStream)
}

def speak(String text) { // named for smartthing capability not fully method
    addEvent(["command", "textToSpeech", "${text}", "cmd=textToSpeech&text=${URLEncoder.encode(text, "UTF-8")}"])
}

def strobe() {
    alarm()
}

def siren() {
    alarm()
}

def both() {
    alarm()
}

def alarm() {
	if(settings?.deviceAlarmUrl?.length()) {
        setAlarmVolume()
        state.alarm=true
        addEvent(["command", "alarm", "${url}", "cmd=playSound&url=${settings.deviceAlarmUrl}&loop=true"])
    } else logInfo "${device.displayName} alarm URL is not set"
}

def alarmOff() {
    stopSound()
}

def chime() {
    beep()
}    

def beep() {
	if(settings?.deviceMediaUrl?.length()) {
        setMediaVolume()
        state.alarm=false
        playSound(settings.deviceMediaUrl)
    } else logInfo "${device.displayName} media URL is not set"
}

def playSound(String url) {
    addEvent(["command", "playSound", "${url}", "cmd=playSound&url=${url}"])
}

def stopSound() {
    addEvent(["command", "stopSound", null, "cmd=stopSound"])
}

def sendGenericCommand(value) {
    addEvent(["command", value, null, "cmd=${URLEncoder.encode(value, "UTF-8")}"])
}

def setStringSetting(String key, String value, nextRefresh=1) {
    logDebug "Executing 'setStringSetting()' key:${key} value:${value}"
    addEvent(["setStringSetting", key, value, "cmd=setStringSetting&key=${key}&value=${URLEncoder.encode(value, "UTF-8")}"])
    nextRefresh ?: runIn(nextRefresh, fetchSettings)
}

def setBooleanSetting(String key, String value, nextRefresh=1) {
    logDebug "Executing 'setBooleanSetting()' key:${key} value:${value}"
    addEvent(["setBooleanSetting", key, value, "cmd=setBooleanSetting&key=${key}&value=${URLEncoder.encode(value, "UTF-8")}"])
    nextRefresh ?: runIn(nextRefresh, fetchSettings)
}

def getStringSetting(String key, String obj = 'listSettings') {
    if (state.get(obj)!=null && state.get(obj).get(key)!=null) {
        return state.get(obj).get(key)
    }
    return null
}

def getBooleanSetting(String key, String obj = 'listSettings') {
    if (state.get(obj)!=null && state.get(obj).get(key)!=null) {
        return state.get(obj).get(key)
    }
    return null
}

def refresh() {
    logDebug "Executing 'refresh()'"
    fetchInfo()
}

def poll() {
    logDebug "Executing 'poll()'"
    fetchInfo()
}

def ping() {
    logDebug "Executing 'ping()'"
    fetchSettings()
}

def clrEvents() {
    if(state.eventList) {
        logInfo "Clearing command queue: ${state.eventList}"
        sendEvent(name: "info", value: "", descriptionText: "Something went wrong and ${state.eventList.size()} command(s) deleted", isStateChange: true)
    }
    state.eventList = []
    state.eventListRunning = false
    state.counter=0
    state.txCounter=0
    state.rxCounter=0
}

def addEvent(event) {
    state.counter=state.counter+1
    def eventList = state.eventList
    eventList << [type:event[0], key:event[1], value:event[2], postCmd:event[3], sequence:state.counter]
    logTrace "Event list is: ${state.eventList}"
    runIn(20, clrEvents) // watchdog: needs to be less then settings.devicePollRateSecs
    if (state.eventListRunning==false) {
        //unschedule(clrEvents)
        unschedule(runPostCmd)
        sendPostCmdDelay()
    }
    return true
}

def pullEvent() {
    state.eventListRunning=true
    def eventList = state.eventList    
    if (eventList.size() == 0) {
        state.eventListRunning=false
        return
    }
    def event = eventList[0]
    eventList.remove(0)
    logTrace "Event list is: ${state.eventList} after pulling: ${event}"    
    return event
}

def peakEvent() {
    state.eventListRunning=true
    def eventList = state.eventList    
    if (eventList.size() == 0) {
        state.eventListRunning=false
        return
    }
    def event = eventList[0]
    logTrace "Event list is: ${state.eventList} after peaking: ${event}"    
    return event
}

def sendPostCmdDelay()
{
    if (isST())
    	runIn(0, sendPostCmd)
    else
        runInMillis(200, sendPostCmd) // Hubitat is actually faster.
}

def runPostCmd() {
    if (peakEvent()) {
        logInfo "Running sendPostCmd again since queue was not cleared"
        state.txCounter=-1
    }        
    sendPostCmd()
}

def sendPostCmd() {    
    def event = peakEvent()
    // have I seen this event already?
    if (event && state.txCounter!=event.sequence) {
        state.txCounter=event.sequence

        // not a best practice but if we didnt get a parse event this will retry the command
        // until the queue gets cleared in 10 seconds. appears to work okay. 
        runIn(10, runPostCmd) 

        def cmd = "?type=json&password=${devicePassword}&" + event.postCmd
        logDebug "tx: ${state.txCounter} :: (${cmd})"

        if (device.currentValue("status") != "offline")
            runIn(30, setOffline)
        if (settings.deviceIp!=null && settings.devicePort!=null)
            setupNetworkID() // leave it here.

        def hubAction 	
        try {
            def param = [
                method: "POST",
                path: cmd,
                headers: [HOST: "${settings.deviceIp}:${settings.devicePort}"] + ["X-Request-ID": UUID.randomUUID().toString()]] //nanohttpd doesn't seem to support X-Request-ID
            hubAction = (isST()) ? physicalgraph.device.HubAction.newInstance(param) : hubitat.device.HubAction.newInstance(param)
            if(event.key=="getCamshot" || event.key=="getScreenshot") { if (isST()) hubAction.options = [outputMsgToS3:true] else hubAction.options = [callback:parseImageHubitat] } 
        }
        catch (Exception e) {
            logError "sendPostCmd() $e on $hubAction"
        }
        //logDebug "hubAction: '${hubAction}'"
        if (hubAction) {
            try {
                sendHubCommand( hubAction )
            }
            catch (Exception e) {
                logError "sendPostCmd() $e on $sendHubCommand"
            }
        }

    }
    return event
}

def parse(String description) {
    if ( description=="updated" ) {
        logDebug "description: '${description}'"
        return
    }    
    def msg = parseLanMessage(description)
    //log.debug "parsed lan msg: '${msg}'"
    if (msg?.header && msg?.body) {
        def headerString = msg.header        
        def bodyString = msg.body
        def body = [:]
        try {
            body = new JsonSlurper().parseText(bodyString)
        }
        catch (Exception e) {
            logTrace "parse() exception ignored: $e"
        }

        if (headerString.contains("200 OK")) {
            unschedule("setOffline")
            setOnline()
            decodePostResponse(body)   
        } else {
            logTrace "parse() header did not respond '200 OK': ${headerString}"
        }       
    } else if (msg?.tempImageKey) {
        unschedule("setOffline")
        parseImageSmartThings(description)
    } else {
        logError "parse() parseLanMessage could not decode: ${msg}"
        pullEvent()
    }    

    runIn(20, clrEvents)  // watchdog: needs to be less then settings.devicePollRateSecs
    sendPostCmdDelay()
}

def parseImageSmartThings(String description) {
    def event = pullEvent()
    state.rxCounter=state.rxCounter+1

    def map = stringToMap(description)
    logDebug "parseImageSmartThings description: ${map}"

    if (map?.tempImageKey) {
        try {
            def strImageName = (java.util.UUID.randomUUID().toString().replaceAll('-', ''))
            strImageName += event.key=="getScreenshot" ? '.png' : '.jpg'
            logDebug "rx: ${state.rxCounter} :: image name: ${strImageName} tempImageKey: ${map.tempImageKey}"
            storeTemporaryImage(map.tempImageKey, strImageName)
            logInfo "${device.displayName} captured image '${strImageName}'"
            if(settings?.deviceS3url?.trim() && event.key=="getCamshot") {
                def strBase64Image = getImage(strImageName).bytes.encodeBase64()
                storeImageS3(strBase64Image)
            }
        } catch (Exception e) {
            logError "parseImageSmartThings() $e"
        }
    } else if (map.error) {
        logError "Error: ${map.error}"
    }
}

def parseImageHubitat(response) {    
    def event = pullEvent()
    state.rxCounter=state.rxCounter+1

    def map = parseLanMessage( response.description )
    logDebug "parseImageHubitat headers: ${map.headers}"

    if( map?.headers?.'Content-Type'.contains("image/jpeg") || map?.headers?.'Content-Type'.contains("image/png")) {
        unschedule("setOffline")
        setOnline()        
        try
        {
            def strImageName = (java.util.UUID.randomUUID().toString().replaceAll('-', ''))
            strImageName += map?.headers?.'Content-Type'.contains("image/jpeg") ? '.jpg' : '.png'
            logDebug "rx: ${state.rxCounter} :: image name: ${strImageName}"            
            // storeTemporaryImage( map.tempImageKey, strImageName )
            byte[] imageBytes = parseDescriptionAsMap(response.description).body.decodeBase64()
            logInfo "NOTICE: Image and screen shots to not work. Still need someplace to put the image with size: ${imageBytes.size()}"            
            logInfo "${device.displayName} captured image '${strImageName}'"
            if(settings?.deviceS3url?.trim() && event.key=="getCamshot") {
                def strBase64Image = parseDescriptionAsMap(response.description).body
                storeImageS3(strBase64Image)
            }
        }
        catch( Exception e )
        {
            logError "parseImageHubitat() $e"
        }
    }

    runIn(20, clrEvents) // watchdog: needs to be less then settings.devicePollRateSecs
    sendPostCmdDelay()
}

def decodePostResponse(body) {
    logDebug "Executing 'decodePostResponse()'"
    def event = pullEvent()
    state.rxCounter=state.rxCounter+1

    if (body.screenOn!=null) {    	
        logDebug "rx: ${state.rxCounter} :: deviceInfo"

        if (event==null || event.type!="deviceInfo")
        logInfo "deviceInfo event was expected but was: ${event}"        

        state.deviceInfo = body 
    }
    else if (body?.timeToScreensaverV2) {
        logDebug "rx: ${state.rxCounter} :: listSettings"

        if (event==null || event.type!="listSettings")
        logInfo "listSettings event was expected but was: ${event}"

        state.listSettings = body
    }
    else if (body?.status && body?.statustext && body.status.contains("OK")) {
        logDebug "rx: ${state.rxCounter} :: ${body.statustext}"

        if (event==null || (event.type!="command" && !event.type.contains("Setting"))) {
            logInfo "command or setting event was expected but was: ${event}"
            runIn(2, fetchSettings)
        }
        logTrace "Processing event: ${body} with event: ${event}"
        switch (body.statustext) {
            case "Screesaver stopped": // misspelled return from Fully early versions
            case "Screensaver stopped":
                state.deviceInfo.isInScreensaver = false
                //logDebug "status: ${body.statustext}, brightness: ${state.deviceInfo.screenBrightness}, listSettings: ${state.listSettings.screenBrightness}"
                if(!!state.deviceInfo && !state.listSettings?.screenBrightness.isEmpty()) state.deviceInfo.screenBrightness = state.listSettings.screenBrightness.toInteger()
                //logDebug "status: ${body.statustext}, brightness: ${state.deviceInfo.screenBrightness}, listSettings: ${state.listSettings.screenBrightness}"
                break;
            case "Switching the screen on":
                state.deviceInfo.screenOn = true
                logDebug "${body.statustext}"
                break;
            case "Screensaver started":
                state.deviceInfo.isInScreensaver = true
                //logDebug "status: ${body.statustext}, brightness: ${state.deviceInfo.screenBrightness}, listSettings: ${state.listSettings.screensaverBrightness}"
                if(!!state.deviceInfo && !state.listSettings?.screensaverBrightness.isEmpty()) state.deviceInfo.screenBrightness = state.listSettings.screensaverBrightness.toInteger()
                //logDebug "status: ${body.statustext}, brightness: ${state.deviceInfo.screenBrightness}, listSettings: ${state.listSettings.screensaverBrightness}"
                break;
            case "Switching the screen off":
                state.deviceInfo.screenOn = false
                logDebug "${body.statustext}"
                break;            
            case "Saved":
                if ( event && event.type=="setStringSetting" ) {
                    def logit = "setStringSetting ${event.key} was ${state.listSettings.get(event.key)} updating to ${event.value}"
                    if(state.listSettings."${event.key}" instanceof Integer) {
                        state.listSettings."${event.key}" = Integer.valueOf(event.value)
                        logDebug "${logit} as Integer"
                    }
                    else if(state.listSettings."${event.key}" instanceof String) {
                        state.listSettings."${event.key}" = String.valueOf(event.value)
                        logDebug "${logit} as String"
                    }
                    else logError "${logit} was not completed"
                    break;
                } else          	
                    if ( event && event.type=="setBooleanSetting" ) {
                        logDebug "setBooleanSetting ${event.key} was ${state.listSettings.get(event.key)} updating to ${event.value} as Boolean"
                        state.listSettings."${event.key}" = Boolean.valueOf(event.value)
                        break;
                    }
            case "Text To Speech Ok":
                sendEvent(name: "info", value: "", descriptionText: "TTS: '${event.value}'", isStateChange: true)
                default:
                    // i contacted fully support to ask about a generic code reply or sequence_id to validate because handling weird return calls
                    // are ackward. they told me it was too difficult and they didnt understand why i needed them. oh well. 
                    logDebug "statustext: '${body?.statustext}' from event: ${event?.type}:${event?.key}"
                	break;            
        }
        logInfo "${device.displayName} ${body.statustext}" 
    }
    else {
        logError "unhandled event: ${event} with reponse:'${body}'"
    }

    def nextRefresh = update()
    logDebug "Refresh in ${nextRefresh} seconds"
    runIn(nextRefresh, refresh)
}

def update() {

    def nextRefresh = settings.devicePollRateSecs.toInteger()

    if (state?.deviceInfo && state?.listSettings) {

        def lastpoll_str = new Date().format("yyyy-MM-dd h:mm:ss a", location.timeZone)
        sendEvent(name: "lastPoll", value: lastpoll_str, displayed: false)

        sendEvent(name: "screen", value: (state.deviceInfo.screenOn?"on":"off"), displayed: false )        
        sendEvent(name: "screensaver", value: (state.deviceInfo.isInScreensaver?"on":"off"), displayed: false )

        if (state.deviceInfo.screenOn && !state.deviceInfo.isInScreensaver) {

            if (device.currentValue("switch") != "on") {
                sendEvent(name: "switch", value: "on", descriptionText: "Fully Kiosk Browser is on")
                sendEvent(name: "motion", value: "active", displayed: false)
                logInfo "${device.displayName} is on"
            }

            if(state.listSettings.timeToScreensaverV2.toInteger()>0)
            nextRefresh = state.listSettings.timeToScreensaverV2.toInteger() + 1          
        }
        else {
            if (device.currentValue("switch") != "off") {
                sendEvent(name: "switch", value: "off", descriptionText: "Fully Kiosk Browser is off")
                sendEvent(name: "motion", value: "inactive", displayed: false)
                logInfo "${device.displayName} is off"
            }
        }

        logDebug "Brightness is: ${state.deviceInfo.screenBrightness} (${state.deviceInfo.screenBrightness.toInteger()*100/255}%)"
        logDebug "Screen is: ${(state.deviceInfo.screenOn?"on":"off")}"
        logDebug "Screensaver is: ${(state.deviceInfo.isInScreensaver?"on":"off")}"
        logDebug "Screensaver timeout is: ${state.listSettings.timeToScreensaverV2} seconds"

        def level = Math.round(state.deviceInfo.screenBrightness.toInteger()/2.55)
        if (device.currentValue("level") != "${level}")
        sendEvent(name: "level", value: "${level}", descriptionText: "Screen Brightness is ${level}")

        sendEvent(name: "injectJsCode", value: "${state.listSettings.injectJsCode}", displayed: false)
        sendEvent(name: "currentPage", value: "${state.deviceInfo.currentPage}", displayed: false)
        sendEvent(name: "screenBrightness", value: "${state.deviceInfo.screenBrightness}", displayed: false)
        sendEvent(name: "battery", value: "${state.deviceInfo.batteryLevel}", displayed: false)
        sendEvent(name: "appVersionName", value: "${state.deviceInfo.appVersionName}", displayed: false)
        sendEvent(name: "isScreenOn", value: "${state.deviceInfo.screenOn?'true':'false'}", displayed: false)
        sendEvent(name: "isInScreensaver", value: "${state.deviceInfo.isInScreensaver}", displayed: false)
        sendEvent(name: "battery", value: "${state.deviceInfo.batteryLevel}", displayed: false)
        sendEvent(name: "wifiSignalLevel", value: "${state.deviceInfo.wifiSignalLevel}", displayed: false)
        sendEvent(name: "timeToScreensaverV2", value: "${state.listSettings.timeToScreensaverV2}", displayed: false)
        sendEvent(name: "altitude", value: "${state.deviceInfo.altitude}", displayed: false)
        sendEvent(name: "latitude", value: "${state.deviceInfo.locationLatitude}", displayed: false)
        sendEvent(name: "longitude", value: "${state.deviceInfo.locationLongitude}", displayed: false)
    }
    return ((nextRefresh>settings.devicePollRateSecs.toInteger())?settings.devicePollRateSecs.toInteger():nextRefresh)
}

def setOnline() {
    logDebug "Executing 'setOnline()'"
    if(device.currentValue("status")!="online") {
        logInfo "${device.displayName} is ${device.currentValue("status")}"
        sendEvent(name: "status", value: "online", displayed: true)
        sendEvent(name: "healthStatus", value: "online", displayed: false)
        sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
    }
}

def setOffline() {
    logDebug "Executing 'setOffline()'"
    if(device.currentValue("status")!="offline") {
        logInfo "${device.displayName} is ${device.currentValue("status")}"
        sendEvent(name: "status", value: "offline", displayed: true, isStateChange: true)
        sendEvent(name: "healthStatus", value: "offline", displayed: false, isStateChange: true)
        sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
    }
}

def setupNetworkID() {
    def hosthex = convertIPtoHex(settings.deviceIp).toUpperCase()
    def porthex = convertPortToHex(settings.devicePort).toUpperCase()
    if("$hosthex:$porthex" != device.deviceNetworkId) {
        device.deviceNetworkId = "$hosthex:$porthex"
        logInfo "Executing 'setupNetworkID()' setting to $hosthex:$porthex"
    }
}

String convertIPtoHex(ip) {
    String hexip = ip.tokenize( '.' ).collect { String.format( '%02x', it.toInteger() ) }.join()
    return hexip
}

String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}


def setS3url(value) {
    settings.deviceS3url = value
    sendEvent(name: "s3url", value: value, displayed: false)
}

def setS3key(value) {
    settings.deviceS3key = value
    sendEvent(name: "s3key", value: value, displayed: false)
}

def storeImageS3(strBase64Image) {
    logDebug "Executing 'storeImageS3()' to ${settings.deviceS3url}"

    def params = [
        uri: settings.deviceS3url+'/store',
        body: JsonOutput.toJson([ 
            'device': "${device.displayName}", 
            'title': "${new Date().getTime()}.jpg", 
            'image': "${strBase64Image}",
            'altitude': device.currentValue("altitude"),
            'latitude': device.currentValue("latitude"), 
            'longitude': device.currentValue("longitude")
        ])
    ]
    if(settings?.deviceS3key?.trim()) { // you don't need to use x-api-key with lambda. but good idea.
        params['headers'] = [ "X-Api-Key": settings.deviceS3key ]
    }

    try {
        httpPostJson(params) { resp ->
            resp.headers.each { logTrace "${it.name} : ${it.value}" }
            logTrace "response contentType: ${resp.contentType}"
            logDebug "response data: ${resp.data}"
        }
    }
    catch (e) {
        logError e
    }
}

def fetchImageS3(strImageName) {

    if (deviceS3ret) {
        logDebug "Executing 'fetchImageS3()' to ${settings.deviceS3url}"

        def params = [
            uri: settings.deviceS3url+'/fetch',
            body: JsonOutput.toJson([ 
                'device': "${device.displayName}", 
                'title': "${strImageName}", 
            ])
        ]
        if(settings?.deviceS3key?.trim()) { // you don't need to use x-api-key with lambda. but good idea.
            params['headers'] = [ "X-Api-Key": settings.deviceS3key ]
        }

        try {
            httpPostJson(params) { resp ->
                resp.headers.each { logTrace "${it.name} : ${it.value}" }
                logTrace "response contentType: ${resp.contentType}"
                //logTrace "response data: ${data}"
                if (resp?.data?.body) {
                    storeImage(strImageName, (new ByteArrayInputStream(resp.data.body.decodeBase64())))
                    logInfo "${device.displayName} fetched S3 image '${strImageName}'"
                }
            }
        }
        catch (e) {
            logError e
        }
    }
}

private getPlatform() {
    String p = "SmartThings"
    if(state?.hubPlatform == null) {
        try { [dummy: "dummyVal"]?.encodeAsJson(); } catch (e) { p = "Hubitat" }
        // if (location?.hubs[0]?.id?.toString()?.length() > 5) { p = "SmartThings" } else { p = "Hubitat" }
        state?.hubPlatform = p
        logDebug ("hubPlatform: (${state?.hubPlatform})")
    }
    return state?.hubPlatform
}

private parseDescriptionAsMap( description )
{
    description.split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

private logInfo(msg)  { log.info "${msg}" }
private logDebug(msg) { if(settings?.deviceLogEnable == true) { log.debug "${msg}" } }
private logTrace(msg) { if(settings?.deviceTraceEnable == true) { log.trace "${msg}" } }
private logWarn(msg)  { log.warn  "${msg}" } 
private logError(msg) { log.error  "${msg}" }

// Stores the MAC address as the device ID so that it can talk to SmartThings
// Not used today. Maybe use to support dynamic DHCP. Today just static works.
// https://github.com/stjohnjohnson/smartthings-mqtt-bridge/blob/master/devicetypes/stj/mqtt-bridge.src/mqtt-bridge.groovy
def setNetworkAddress() {
    // Setting Network Device Id
    def hex = "$settings.mac".toUpperCase().replaceAll(':', '')
    if (device.deviceNetworkId != "$hex") {
        device.deviceNetworkId = "$hex"
        logDebug "Device Network Id set to ${device.deviceNetworkId}"
    }
}