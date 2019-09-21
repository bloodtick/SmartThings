/**
 *  Copyright 2019 SmartThings
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
 *  Date: 2019-08-24
 *
 */

import groovy.json.*
import java.net.URLEncoder

metadata {
    definition (name: "Fully Kiosk Browser Device", namespace: "bloodtick", author: "SmartThings") {
        capability "Actuator"
        capability "Switch"
        capability "Switch Level"
        capability "Sensor"
        capability "Refresh"
        capability "Health Check"
        capability "Battery"
        capability "Speech Synthesis"

        attribute "deviceInfo", "string"
        attribute "deviceSettings", "string"
        attribute "wifiSignalLevel", "number"
        attribute "speechVolume", "number"
        attribute "screen", "string"
        attribute "screenSaver", "string"
        attribute "currentPage", "string"
        attribute "injectJsCode", "string"

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
        command "setVolumeAndSpeak", ["number", "string", "number"]
        command "setSpeechVolume", ["number", "number"]
        command "setScreenBrightness", ["number"]
        command "setScreensaverTimeout", ["number"]
        command "setStringSetting", ["string", "string"]
        command "setBooleanSetting", ["string", "string"]
        command "getStringSetting", ["string", "string"]
        command "getBooleanSetting", ["string", "string"]
        command "sendGenericCommand", ["string"]
        command "speachTestAction"
        command "speechVolumeUpdate"
    }

    // simulator metadata
    simulator {}

    // UI tile definitions    
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: false){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/tablet.png", backgroundColor:"#00a0dc", nextState:"off"
                attributeState "off", label:'${name}', action:"switch.on", icon:"https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/tablet.png", backgroundColor:"#ffffff", nextState:"on"
                //attributeState "offline", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ff0000"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
        }

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
        valueTile("currentFragment", "device.currentFragment", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Display ]\n${currentValue}'
        }
        valueTile("batteryLevel", "device.batteryLevel", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Battery ]\n${currentValue}'
        }
        valueTile("wifiSignalLevel", "device.wifiSignalLevel", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Wifi Level ]\n${currentValue}'
        }
        valueTile("timeToScreensaverV2", "device.timeToScreensaverV2", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Screen Saver ]\n${currentValue} sec'
        }        
        standardTile("screenSaver", "device.screenSaver", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "off", label: 'ScreenSaver Off', action: "startScreensaver", icon: "https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/on-blue-3x.png", backgroundColor: "#ffffff"/*, nextState:"off"*/
            state "on", label: 'ScreenSaver On', action: "stopScreensaver", icon: "st.switches.switch.off", backgroundColor: "#ffffff"/*, nextState:"on"*/
        }        
        standardTile("screen", "device.screen", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "on", label: 'Screen On', action: "screenOff", icon: "https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/on-blue-3x.png", backgroundColor: "#ffffff"/*, nextState:"off"*/
            state "off", label: 'Screen Off', action: "screenOn", icon: "st.switches.switch.off", backgroundColor: "#ffffff"/*, nextState:"on"*/
        }        
        standardTile("speechTest", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "default", label:'Speak Test', action:"speachTestAction", icon:"https://raw.githubusercontent.com/bloodtick/SmartThings/master/images/speaker-grey.png"
        }        
        controlTile("speechVolume", "device.speechVolume", "slider", inactiveLabel: false, height: 1, width: 1, range:"(0..100)") {
            state "default", label:'${currentValue}', action:"speechVolumeUpdate"
        }        
        standardTile("refresh", "device.switch", inactiveLabel: false, height: 1, width: 2, decoration: "flat") {
            state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
        }
        standardTile("listSettings", "device.switch", inactiveLabel: false, height: 1, width: 2, decoration: "flat") {
            state "default", label:'Settings', action:"fetchSettings", icon:"st.secondary.refresh-icon"
        }

        main "switch"
        details(["switch","currentIP","lastPoll","appVersionName","isScreenOn","currentFragment","wifiSignalLevel","screenBrightness","timeToScreensaverV2",
                 "batteryLevel","screen","screenSaver","speechTest","speechVolume","refresh","listSettings"])
    }
}

preferences {
    input(name:"deviceIp", type:"text", title: "Device IP Address", description: "Device IP Address", defaultValue: "127.0.0.1", required: true, displayDuringSetup: true)
    input(name:"devicePort", type:"number", title: "Device IP Port", description: "Default is port 2323", defaultValue: "2323", required: false, displayDuringSetup: true)
    input(name:"devicePassword", type:"string", title:"Fully Kiosk Browser Password", required: true, displayDuringSetup: true)
    input(name:"deviceAllowScreenOff", type: "bool", title: "Allow Screen Off Command", description: "Diverts screen off and on commands to screensaver on and off commands", defaultValue: "false", displayDuringSetup: false)
    input(name:"devicePollRateSecs", type: "number", title: "Device Poll Rate (30-300 seconds)", description: "Default is 300 seconds", range: "30..300", defaultValue: "300", displayDuringSetup: false)
    input(name:"deviceMAC", type:"string", title:"MAC Address of Device", defaultValue: "Awaiting Device Response", required: false, displayDuringSetup: false)
    input(name:"deviceStoreDeviceConfig", type: "bool", title: "Display Configuration Information", description: "Store and display configuration information in Device Handler Attributes", defaultValue: "false", displayDuringSetup: false)
}

def installed() {
    settings.devicePort = "127.0.0.1"
    settings.devicePort = 2323
    settings.deviceAllowScreenOff = false
    settings.devicePollRateSecs = 300
    settings.deviceMAC = "Awaiting Device Response"
    settings.deviceStoreDeviceConfig = false
    sendEvent(name: "level", value: "50", displayed: false)
    sendEvent(name: "speechVolume", value: "50", displayed: false)
    log.debug "Executing 'installed()' with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Executing 'updated()' with new preferences: ${settings}"
    initialize()
}

def initialize() {
    log.debug "Executing 'initialize()'"
    unschedule()
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "battery", value: "100")
    if (device?.hub?.hardwareID ) {
        sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device.hub.hardwareID}\"}", displayed: false)
        sendEvent(name: "checkInterval", value: 1920, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
    } else {
    	log.info "This device is not yet assigned to a SmartThings Hub"
    }
    state.deviceInfo = ""
    state.listSettings = ""

    if (settings.deviceIp && settings.devicePort && settings.devicePassword) {
        sendEvent(name: "currentIP", value: "${settings.deviceIp}:${settings.devicePort}", displayed: false)
        // start collecting listSettings and deviceInfo from device. I HATE POLLING.       
        clrEvents()
        fetchSettings()
        fetchInfo() // auto refresh with checkInterval delay
        runEvery15Minutes(ping) // same as fetchSettings
    } 
}

def on() {
    screenOn()
}

def off() {
    screenOff()
}

def setLevel(level) {
    sendEvent(name: "level", value: level, descriptionText: "Screen Brightness is ${level}") // checked and updated in update()
    setScreenBrightness(level)
}

def setScreenBrightness(level) {
    def value = Math.round(level.toInteger()*2.55).toString()
    setStringSetting("screenBrightness", "${value}")
}

def speachTestAction() {
    setVolumeAndSpeak(device.currentValue("speechVolume").toInteger(), "Fully Kiosk Browser Speaking Test")
}

def speechVolumeUpdate(level) {
    sendEvent(name: "speechVolume", value: "${level}", descriptionText: "Audio Level is ${level}")
}

def setVolumeAndSpeak(level, text, stream=9) {
    setSpeechVolume(level)
    speak(text)
}

def setScreensaverTimeout(value) {
    setStringSetting("timeToScreensaverV2", "${value}")
}

def fetchSettings() {
    def cmd = "?type=json&password=${devicePassword}&cmd=listSettings"
    addEvent(["listSettings", null, null, cmd])    
}

def fetchInfo() {
    def cmd = "?type=json&password=${devicePassword}&cmd=deviceInfo"
    addEvent(["deviceInfo", null, null, cmd])
}

def screenOn() {
    if (settings.deviceAllowScreenOff==false) return stopScreensaver()
    //sendEvent(name: "screen", value: "on", displayed: false )   
    def cmd = "?type=json&password=${devicePassword}&cmd=screenOn"
    addEvent(["command", "screenOn", null, cmd])
}

def screenOff() {
    if (settings.deviceAllowScreenOff==false) return startScreensaver()
    //sendEvent(name: "screen", value: "off", displayed: false )    
    def cmd = "?type=json&password=${devicePassword}&cmd=screenOff"
    addEvent(["command", "screenOff", null, cmd])
}

def stopScreensaver() {
    //sendEvent(name: "screenSaver", value: "off", displayed: false )
    def cmd = "?type=json&password=${devicePassword}&cmd=stopScreensaver"
    addEvent(["command", "stopScreensaver", null, cmd])
}

def startScreensaver() {
    //sendEvent(name: "screenSaver", value: "on", displayed: false )
    def cmd = "?type=json&password=${devicePassword}&cmd=startScreensaver"
    addEvent(["command", "startScreensaver", null, cmd])
}

def triggerMotion() {
    def cmd = "?type=json&password=${devicePassword}&cmd=triggerMotion"
    addEvent(["command", "triggerMotion", null, cmd])
}

def toForeground() {
    def cmd = "?type=json&password=${devicePassword}&cmd=toForeground"
    addEvent(["command", "toForeground", null, cmd])
}

def loadStartURL() {
    def cmd = "?type=json&password=${devicePassword}&cmd=loadStartURL"
    addEvent(["command", "loadStartURL", null, cmd])
}

def loadURL(String value) {
    def cmd = "?type=json&password=${devicePassword}&cmd=loadURL&url=${value}"
    addEvent(["command", "loadURL", null, cmd])
}

def setSpeechVolume(level, stream=9) {
    speechVolumeUpdate(level)
    def cmd = "?type=json&password=${devicePassword}&cmd=setAudioVolume&level=${level}&stream=${stream}"
    addEvent(["command", "setAudioVolume", null, cmd])
}

def speak(String text) { // named for smartthing capability not fully method
    def cmd = "?type=json&password=${devicePassword}&cmd=textToSpeech&text=${URLEncoder.encode(text, "UTF-8")}"
    addEvent(["command", "textToSpeech", "${text}", cmd])
}

def sendGenericCommand(value) {
    def cmd = "?type=json&password=${devicePassword}&cmd=${value}"
    addEvent(["command", value, null, cmd])
}

def setStringSetting(String key, String value, nextRefresh=1) {
    def cmd = "?type=json&password=${devicePassword}&cmd=setStringSetting&key=${key}&value=${URLEncoder.encode(value, "UTF-8")}"
    addEvent(["setStringSetting", key, value, cmd])
    runIn(nextRefresh, fetchSettings)
}

def setBooleanSetting(String key, String value, nextRefresh=1) {
    def cmd = "?type=json&password=${devicePassword}&cmd=setBooleanSetting&key=${key}&value=${URLEncoder.encode(value, "UTF-8")}"
    addEvent(["setBooleanSetting", key, value, cmd])
    runIn(nextRefresh, fetchSettings)
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
    log.debug "Executing 'refresh()'"
    fetchInfo()
    // this is tricky. The preference can NOT be blank. So if you clear, will need 'Awaiting Device Response'
    // added by user or will auto fill in the second time around.
    if(state?.deviceInfo?.hasProperty('mac') && settings.deviceMAC=="Awaiting Device Response") {
        device.updateSetting("deviceMAC", settings.deviceMAC = state.deviceInfo.mac)
        log.debug "MAC address found and set to: ${settings.deviceMAC}"
    }
}

def poll() {
    log.debug "Executing 'poll()'"
    fetchInfo()
}

def ping() {
    log.debug "Executing 'ping()'"
    fetchSettings()
}

def clrEvents() {
    if(state.eventList) {
        log.debug "Clearing command queue: ${state.eventList}"
        sendEvent(name: "info", value: "", descriptionText: "Something went wrong and ${state.eventList.size()} command(s) deleted", isStateChange: true)
    }
    state.eventList = []
    state.counter=0
    state.txCounter=0
    state.rxCounter=0
}

def addEvent(event) {
    state.counter=state.counter+1
    def map = [type:event[0], key:event[1], value:event[2], postCmd:event[3], sequence:state.counter]
    state.eventList.add(map)
    //log.debug "Event list is: ${state.eventList}"
    runIn(20, clrEvents) // watchdog: needs to be less then settings.devicePollRateSecs
    if (state.eventList.size()==1)
    	sendPostCmd()
    return true
}

def pullEvent() {
    def event
    if (state.eventList && !state.eventList.isEmpty()) {
        event = state.eventList.remove(0)
    }
    //log.debug "Event list is: ${state.eventList} after pulling: ${event}"    
    return event
}

def peakEvent() {
    def event
    if (state.eventList && !state.eventList.isEmpty()) {
        event = state.eventList.get(0)
    }
    //log.debug "Event list is: ${state.eventList} after peaking: ${event}"    
    return event
}

def runPostCmd() {
    if (peakEvent()) {
        log.trace "Running sendPostCmd again since queue was not cleared"
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

        def cmd = event.postCmd
        log.debug "tx: ${state.txCounter} :: (${cmd})"

        if (device.currentValue("status") != "offline")
        	runIn(30, setOffline)
        if (settings.deviceIp!=null && settings.devicePort!=null)
        	setupNetworkID() // leave it here. 

        def hubAction 	
        try {
            hubAction = new physicalgraph.device.HubAction([
                method: "POST",
                path: cmd,
                headers: [HOST: "${settings.deviceIp}:${settings.devicePort}"] + ["X-Request-ID": UUID.randomUUID().toString()]] //nanohttpd doesn't seem to support X-Request-ID
                                                          )
        }
        catch (Exception e) {
            log.debug "Exception $e on $hubAction"
        }
        //log.debug "hubAction: '${hubAction}'"
        if (hubAction)
        	sendHubCommand( hubAction )
    }
    return event
}

def parse(String description) {
	if ( description=="updated" ) {
		log.debug "description: '${description}'"
        return
    }    
	def msg = parseLanMessage(description)
 	//log.debug "parsed lan msg: '${msg}'"
    if (msg.header!=null && msg.body!=null) {
    	def headerString = msg.header        
    	def bodyString = msg.body
    	def body = new JsonSlurper().parseText(bodyString)
       
        if (headerString.contains("200 OK")) {
            try {
                unschedule("setOffline")
            } catch (e) {
                log.error "unschedule(\"setOffline\")"
            }
			// everything was healthy. tell smartthings & decode.
            setOnline()
            
            decodePostResponse(body)   
     
         } else {
         	log.error "parse() header did not respond '200 OK': ${headerString}"
         }       
	} else {
    	log.error "parse() parseLanMessage could not decode: ${description}"
    }    
    
    runIn(20, clrEvents) // watchdog: needs to be less then settings.devicePollRateSecs
    sendPostCmd()
}

def decodePostResponse(body) {
    log.debug "Executing 'decodePostResponse()'"
    
    def event = pullEvent()
    state.rxCounter=state.rxCounter+1
       
    if (body.isScreenOn!=null) {    	
        log.debug "rx: ${state.rxCounter} :: deviceInfo"
        
        if (event==null || event.type!="deviceInfo")
        	log.error "deviceInfo event was expected but was: ${event}"
        
        state.deviceInfo = body 
        //log.debug "parseLanMessage body: '${state.deviceInfo}'"
    }
    else if (body.timeToScreensaverV2!=null) {
    	log.debug "rx: ${state.rxCounter} :: listSettings"
        
        if (event==null || event.type!="listSettings")
        	log.error "listSettings event was expected but was: ${event}"
        
        state.listSettings = body
		//log.debug "parseLanMessage body: '${state.listSettings}'"
    }
    else if (body.status && body.statustext && body.status.contains("OK")) {
		log.debug "rx: ${state.rxCounter} :: ${body.statustext}"

        if (event==null || (event.type!="command" && !event.type.contains("Setting"))) {
        	log.error "command or setting event was expected but was: ${event}"
            runIn(2, fetchSettings)
        }
        //log.debug "Processing event: ${body} with event: ${event}"
        switch (body.statustext) {
            case "Screesaver stopped": // misspelled return from Fully
            case "Screensaver stopped":
            	state.deviceInfo.currentFragment = "main"
                log.debug "${body.statustext}"
                break;
			case "Switching the screen on":
            	state.deviceInfo.isScreenOn = true
            	log.debug "${body.statustext}"
            	break;
            case "Screensaver started":
            	state.deviceInfo.currentFragment = "screensaver"
                log.debug "${body.statustext}"
                break;
            case "Switching the screen off":
            	state.deviceInfo.isScreenOn = false
            	log.debug "${body.statustext}"
            	break;            
            case "Saved":
            	if ( event && event.type=="setStringSetting" ) {
                	def logit = "setStringSetting ${event.key} was ${state.listSettings.get(event.key)} updating to ${event.value}"
                    if(state.listSettings."${event.key}" instanceof Integer) {
                    	state.listSettings."${event.key}" = Integer.valueOf(event.value)
                        log.debug "${logit} as Integer"
                    }
                    else if(state.listSettings."${event.key}" instanceof String) {
                    	state.listSettings."${event.key}" = String.valueOf(event.value)
                        log.debug "${logit} as String"
                    }
                    else log.error "${logit} was not completed"
                    break;
                } else          	
             	if ( event && event.type=="setBooleanSetting" ) {
                	log.debug "setBooleanSetting ${event.key} was ${state.listSettings.get(event.key)} updating to ${event.value} as Boolean"
                    state.listSettings."${event.key}" = Boolean.valueOf(event.value)
                    break;
                }
            case "Text To Speech Ok":
                sendEvent(name: "info", value: "", descriptionText: "TTS: '${event.value}'", isStateChange: true)
			default:
            	// i contacted fully support to ask about a generic code reply or sequence_id to validate because handling weird return calls
                // are ackward. they told me it was too difficult and they didnt understand why i needed them. oh well. 
				log.debug "statustext: '${body.statustext}' from event: ${event.type}:${event.key}"
            	break;
        }
    }
    else {
        log.error "unhandled event: ${event} with reponse:'${body}'"
    }

	def nextRefresh = update()
	log.debug "Refresh in ${nextRefresh} seconds"
    runIn(nextRefresh, refresh)
}

def update() {

    def nextRefresh = settings.devicePollRateSecs.toInteger()

    if (state.deviceInfo && state.listSettings) {

        def lastpoll_str = new Date().format("yyyy-MM-dd h:mm:ss a", location.timeZone)
        sendEvent(name: "lastPoll", value: lastpoll_str, displayed: false)

        if (state.deviceInfo.currentFragment=="screensaver" && state.listSettings.screensaverBrightness!="")
        	state.deviceInfo.screenBrightness = state.listSettings.screensaverBrightness
        else
            state.deviceInfo.screenBrightness = state.listSettings.screenBrightness

        sendEvent(name: "screen", value: (state.deviceInfo.isScreenOn?"on":"off"), displayed: false )
        sendEvent(name: "screenSaver", value: (state.deviceInfo.currentFragment=="screensaver"?"on":"off"), displayed: false )

        if (state.deviceInfo.isScreenOn && state.deviceInfo.currentFragment!="screensaver") {

            if (device.currentValue("switch") != "on")
            	sendEvent(name: "switch", value: "on", descriptionText: "Fully Kiosk Browser is on")

            def level = Math.round(state.deviceInfo.screenBrightness.toInteger()/2.55)
            if (device.currentValue("level") != "${level}")
            	sendEvent(name: "level", value: "${level}", descriptionText: "Screen Brightness is ${level}")

            if(state.listSettings.timeToScreensaverV2.toInteger()>0)
            	nextRefresh = state.listSettings.timeToScreensaverV2.toInteger() + 1          
        }
        else {
            if (device.currentValue("switch") != "off")
            	sendEvent(name: "switch", value: "off", descriptionText: "Fully Kiosk Browser is off")            
        }

        log.debug "Brightness is: ${state.deviceInfo.screenBrightness} (${state.deviceInfo.screenBrightness.toInteger()*100/255}%)"
        log.debug "Screen On: ${state.deviceInfo.isScreenOn}"
        log.debug "Display is: ${state.deviceInfo.currentFragment}"
        log.debug "Screen Saver Timeout is: ${state.listSettings.timeToScreensaverV2} secs"
        log.debug "Screen Saver Brightness is: ${state.listSettings.screensaverBrightness} (${state.listSettings.screensaverBrightness.toInteger()*100/255}%)"

        sendEvent(name: "deviceSettings", value: (settings.deviceStoreDeviceConfig?(new JsonBuilder(state.listSettings)).toPrettyString():"disabled"), displayed: false)
        sendEvent(name: "deviceInfo", value: (settings.deviceStoreDeviceConfig?(new JsonBuilder(state.deviceInfo)).toPrettyString():"disabled"), displayed: false)

        sendEvent(name: "injectJsCode", value: "${state.listSettings.injectJsCode}", displayed: false)
        sendEvent(name: "currentPage", value: "${state.deviceInfo.currentPage}", displayed: false)
        sendEvent(name: "screenBrightness", value: "${state.deviceInfo.screenBrightness}", displayed: false)
        sendEvent(name: "battery", value: "${state.deviceInfo.batteryLevel}", displayed: false)
        sendEvent(name: "appVersionName", value: "${state.deviceInfo.appVersionName}", displayed: false)
        sendEvent(name: "isScreenOn", value: "${state.deviceInfo.isScreenOn?'true':'false'}", displayed: false)
        sendEvent(name: "currentFragment", value: "${state.deviceInfo.currentFragment}", displayed: false)
        sendEvent(name: "batteryLevel", value: "${state.deviceInfo.plugged?'plugged':"${state.deviceInfo.batteryLevel}%"}", displayed: false)
        sendEvent(name: "currentFragment", value: "${state.deviceInfo.currentFragment}", displayed: false)
        sendEvent(name: "wifiSignalLevel", value: "${state.deviceInfo.wifiSignalLevel}", displayed: false)
        sendEvent(name: "timeToScreensaverV2", value: "${state.listSettings.timeToScreensaverV2}", displayed: false)
    }
    return ((nextRefresh>settings.devicePollRateSecs.toInteger())?settings.devicePollRateSecs.toInteger():nextRefresh)
}

def setOnline() {
    sendEvent(name: "status", value: "online", displayed: true)
    sendEvent(name: "healthStatus", value: "online", displayed: false)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
}

def setOffline() {
    log.debug "Executing 'setOffline()'"
    //sendEvent(name: "switch", value: "offline", descriptionText: "The device is offline")
    sendEvent(name: "status", value: "offline", displayed: true, isStateChange: true)
    sendEvent(name: "healthStatus", value: "offline", displayed: false, isStateChange: true)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
}

def setupNetworkID() {
    def hosthex = convertIPtoHex(settings.deviceIp).toUpperCase()
    def porthex = convertPortToHex(settings.devicePort).toUpperCase()
    if("$hosthex:$porthex" != device.deviceNetworkId) {
        device.deviceNetworkId = "$hosthex:$porthex"
        log.info "Executing 'setupNetworkID()' setting to $hosthex:$porthex"
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

// Store the MAC address as the device ID so that it can talk to SmartThings
// https://github.com/stjohnjohnson/smartthings-mqtt-bridge/blob/master/devicetypes/stj/mqtt-bridge.src/mqtt-bridge.groovy
def setNetworkAddress() {
    // Setting Network Device Id
    def hex = "$settings.mac".toUpperCase().replaceAll(':', '')
    if (device.deviceNetworkId != "$hex") {
        device.deviceNetworkId = "$hex"
        log.debug "Device Network Id set to ${device.deviceNetworkId}"
    }
}
