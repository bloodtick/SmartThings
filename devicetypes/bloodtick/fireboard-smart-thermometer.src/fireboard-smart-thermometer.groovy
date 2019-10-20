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
 *  FireBoard Smart Thermometer
 *   
 *
 *  Update: Bloodtick Jones
 *  Date: 2019-10-03
 *
 */
 
import groovy.json.*


metadata {
    definition(name: "FireBoard Smart Thermometer", namespace: "bloodtick", author: "SmartThings") {
        capability "Actuator"
        capability "Configuration"
        capability "Battery"
        capability "Refresh"
        capability "Temperature Measurement"
        capability "Health Check"
        capability "Sensor"
        capability "Switch"
        capability "Contact Sensor"

        attribute "deviceName", "string"
        attribute "deviceInfo", "string"
        attribute "wifiSignalLevel", "number"
        attribute "channel", "number"
        attribute "channelLabel", "string"
        attribute "channelCount", "number"

        attribute "channelTemp1", "string"
        attribute "channelTemp2", "string"
        attribute "channelTemp3", "string"
        attribute "channelTemp4", "string"
        attribute "channelTemp5", "string"
        attribute "channelTemp6", "string"

        attribute "channelTarg1", "string"
        attribute "channelTarg2", "string"
        attribute "channelTarg3", "string"
        attribute "channelTarg4", "string"
        attribute "channelTarg5", "string"
        attribute "channelTarg6", "string"

        attribute "channelLabel1", "string"
        attribute "channelLabel2", "string"
        attribute "channelLabel3", "string"
        attribute "channelLabel4", "string"
        attribute "channelLabel5", "string"
        attribute "channelLabel6", "string"

        command "setTargetTemp", ["number", "number"]

        command "channelAdd1"
        command "channelSub1"
        command "channelAdd5"
        command "channelSub5"
        command "channelAdd10"
        command "channelSub10"
        command "channelTarg1update"
        command "channelTarg2update"
        command "channelTarg3update"
        command "channelTarg4update"
        command "channelTarg5update"
        command "channelTarg6update"
    }

    preferences {
        input(name: "deviceUsername", type:"string", title:"FireBoard Username", required: true, displayDuringSetup: true)
        input(name: "devicePassword", type:"string", title:"FireBoard Password", required: true, displayDuringSetup: true)
        input(name: "deviceChannel", type: "enum", title: "Select Primary Channel", options: ["auto", "1", "2", "3", "4", "5", "6"], defaultValue: "auto", required: true)
        input(name: "deviceSelected", type: "number", title: "Select FireBoard Device", description: "Sort on device install date", range: "1..", defaultValue: "1", displayDuringSetup: true)
        input(name: "devicePollRateSecs", type: "number", title: "Device Poll Rate (10-60 seconds)", description: "Default is 60 seconds", range: "10..60", defaultValue: "60", displayDuringSetup: false)
        input(name: "deviceStoreDeviceConfig", type: "bool", title: "Display Configuration Information", description: "Store and display configuration information in Device Handler Attributes", defaultValue: "false", displayDuringSetup: false)
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 3, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState "temperature", label: '${currentValue}°', icon: "st.Outdoor.outdoor9",
                    backgroundColors: [
                        [value: 0, color: "#153591"],
                        [value: 30, color: "#1e9cbb"],
                        [value: 60, color: "#90d2a7"],
                        [value: 900, color: "#44b621"],
                        [value: 120, color: "#f1d801"],
                        [value: 150, color: "#d04e00"],
                        [value: 180, color: "#bc2323"]
                    ]
            }
            tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
                attributeState "power", label:'${currentValue}'
            }
        }
        valueTile("deviceName", "device.deviceName", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Name ]\n${currentValue}'
        }
        valueTile("battery", "device.battery", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Battery ]\n${currentValue}%'
        }
        valueTile("wifiSignalLevel", "device.wifiSignalLevel", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Wifi Level ]\n${currentValue}'
        }
        standardTile("switch", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"on"
            state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState:"off"
        }        
        standardTile("refresh", "device.refresh", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
            state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
        }
        valueTile("channelName1", "device.channelName1", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }
        valueTile("channelTarg1", "device.channelTarg1", height: 1, width: 2, decoration: "flat") {
            state "default", label:'Target\n${currentValue}°', action:"channelTarg1update", backgroundColor: "#cccccc"
        }        
        valueTile("channelName2", "device.channelName2", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }
        valueTile("channelTarg2", "device.channelTarg2", height: 1, width: 2, decoration: "flat") {
            state "default", label:'Target\n${currentValue}°', action:"channelTarg2update", backgroundColor: "#cccccc"
        }        
        valueTile("channelName3", "device.channelName3", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }
        valueTile("channelTarg3", "device.channelTarg3", height: 1, width: 2, decoration: "flat") {
            state "default", label:'Target\n${currentValue}°', action:"channelTarg3update", backgroundColor: "#cccccc"
        }
        valueTile("channelName4", "device.channelName4", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }
        valueTile("channelTarg4", "device.channelTarg4", height: 1, width: 2, decoration: "flat") {
            state "default", label:'Target\n${currentValue}°', action:"channelTarg4update", backgroundColor: "#cccccc"
        }        
        valueTile("channelName5", "device.channelName5", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }
        valueTile("channelTarg5", "device.channelTarg5", height: 1, width: 2, decoration: "flat") {
            state "default", label:'Target\n${currentValue}°', action:"channelTarg5update", backgroundColor: "#cccccc"
        }        
        valueTile("channelName6", "device.channelName6", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }
        valueTile("channelTarg6", "device.channelTarg6", height: 1, width: 2, decoration: "flat") {
            state "default", label:'Target\n${currentValue}°', action:"channelTarg6update", backgroundColor: "#cccccc"
        }
        valueTile("channelAdd1", "device.channelAdd1", inactiveLabel: true, height: 1, width: 1, decoration: "flat") {
            state "default", label:'+1°', action:"channelAdd1", backgroundColor: "#cccccc"
        }
        valueTile("channelSub1", "device.channelSub1", inactiveLabel: true, height: 1, width: 1, decoration: "flat") {
            state "default", label:'-1°', action:"channelSub1", backgroundColor: "#cccccc"
        }
        valueTile("channelAdd5", "device.channelAdd5", inactiveLabel: true, height: 1, width: 1, decoration: "flat") {
            state "default", label:'+5°', action:"channelAdd5", backgroundColor: "#cccccc"
        }
        valueTile("channelSub5", "device.channelSub5", inactiveLabel: true, height: 1, width: 1, decoration: "flat") {
            state "default", label:'-5°', action:"channelSub5", backgroundColor: "#cccccc"
        }
        // not displayed
        valueTile("error", "device.error", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}', icon:"st.secondary.configure", backgroundColor: "#e86d13"
        }
        // not displayed
        standardTile("contact", "device.contact", inactiveLabel: true, width: 2, height: 2) {
            state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
            state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#CCCCCC")
        }

        main "temperature"
        details(["temperature", "deviceName", "battery", "wifiSignalLevel",
                 "switch", "refresh", "channelSub1", "channelAdd1", "channelSub5", "channelAdd5",
                 "channelName1", "channelName2", "channelName3",
                 "channelTarg1", "channelTarg2", "channelTarg3",
                 "channelName4", "channelName5", "channelName6",
                 "channelTarg4", "channelTarg5", "channelTarg6"
                ])
    }
}

def installed() {
    settings.deviceChannel = "auto"
    settings.deviceSelected = 1
    settings.devicePollRateSecs = 60
    settings.deviceStoreDeviceConfig = false
    sendEvent(name: "deviceName", value: "open", displayed: false)
    sendEvent(name: "battery", value: "100", displayed: false)
    sendEvent(name: "temperature", value: "0", unit: "F", displayed: false)
    sendEvent(name: "wifiSignalLevel", value: "10", displayed: false)
    sendEvent(name: "channelTarg1", value: "160", displayed: false)
    sendEvent(name: "channelTarg2", value: "160", displayed: false)
    sendEvent(name: "channelTarg3", value: "160", displayed: false)
    sendEvent(name: "channelTarg4", value: "160", displayed: false)
    sendEvent(name: "channelTarg5", value: "160", displayed: false)
    sendEvent(name: "channelTarg6", value: "160", displayed: false)
    off()
    log.debug "Executing 'installed()' with settings: ${settings}"
    initialize()
}

def updated() {
    unschedule()
    log.debug "Executing 'updated()' with new preferences: ${settings}"
    initialize()
}

def initialize() {
    log.debug "Executing 'initialize()'"
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
    sendEvent(name: "healthStatus", value: "online")
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme: "untracked"].encodeAsJson(), displayed: false)
    state.token = ""
    state.device = ""
    runIn(1, refresh)
}

def channelAdd1() {
    pushTargetTemp(state.target, 1)
}

def channelSub1() {
    pushTargetTemp(state.target, -1)
}

def channelAdd5() {
    pushTargetTemp(state.target, 5)
}

def channelSub5() {
    pushTargetTemp(state.target, -5)
}

def channelAdd10() {
    pushTargetTemp(state.target, 10)
}

def channelSub10() {
    pushTargetTemp(state.target, -10)
}

def channelTarg1update() {
    state.target = 1
}

def channelTarg2update() {
    state.target = 2
}

def channelTarg3update() {
    state.target = 3
}

def channelTarg4update() {
    state.target = 4
}

def channelTarg5update() {
    state.target = 5
}

def channelTarg6update() {
    state.target = 6
}

def pushTargetTemp(channel, value) {
    def targ = (device.currentValue("channelTarg${channel}").toInteger() + value.toInteger());    
    setTargetTemp(channel, targ)
}

def setTargetTemp(channel, value) {
    sendEvent(name: "channelTarg${channel}", value: "${value}", displayed: false)
}

def parse(String description) {
    log.debug "description: $description"
}

def ping() {
    return getToken()
}

def off() {
    log.debug "Executing 'off()'"
    unschedule()
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "temperature", value: "0", displayed:false)
    sendEvent(name: "channel", value: "0", displayed:false) 
    sendEvent(name: "power", value: "Device Off", displayed: false)
    sendEvent(name: "contact", value: "open", displayed: false)
}

def on() {
    sendEvent(name: "switch", value: "on")
    refresh()
}

def refresh() {
    //log.debug "Executing 'refresh()'"
    unschedule()
    if(device.currentValue("switch")=="on") {
        runIn(1, startup)
    }
}

def startup() {
    log.debug "Executing 'startup()'"
    state.token = getToken()
    if(state.token) {
        state.device = getDevice(state.token, settings.deviceSelected)
        if(state.device) {
            updateDeviceInfo()
            runEvery1Minute(updateDeviceInfo)
        }
    }
    runIn(600, off) // see updateTemps()
}

def updateDeviceInfo() {
    //log.debug "Executing 'updateDeviceInfo()'" 
    runIn(settings.devicePollRateSecs, updateDeviceTemp)
    getDeviceInfo(state.token, state.device)    
}

def updateDeviceTemp() {
    //log.debug "Executing 'updateDeviceTemp()'"    
    getDeviceTemp(state.token, state.device)
    runIn(settings.devicePollRateSecs, updateDeviceTemp)
}

def getToken() {
    def response = null
    def params = [
        uri: "https://fireboard.io/api/rest-auth/login/",
        body: [
            username: settings.deviceUsername,
            password: settings.devicePassword
        ]
    ]
    try {
        httpPostJson(params) { resp ->
            //log.debug "response data: ${resp.data}"
            response = resp.data?.key
            log.debug "Response data key: ${response}"                        
        }
    } catch (e) {
        log.error "getToken() httpPostJson: $e"  
        sendEvent (name: "error", value: "--", descriptionText:"getToken error username and/or password", isStateChange: true)
    }
    return response
}

def getDevice( token, index ) {
    def response = null    
    def params = [
        uri: "https://fireboard.io/api/v1/devices.json",
        headers: [Authorization: "Token ${token}"]
    ]
    try {
        httpGet(params) { resp ->
            //log.debug "response data: ${resp.data}"            
            response = resp.data.sort{ it.created }
            if( response.size() ) {
                index > response.size() ? index=0 : index--
                    response = response[index]?.uuid
                log.debug "Number of devices: ${resp.data.size()} our uuid: ${response}"
            }
        }
    } catch (e) {
        log.error "getDevice() httpGet: $e"
        sendEvent (name: "error", value: "--", descriptionText:"getDevice error devices.json", isStateChange: true)
    }    
    return response
}

def getDeviceInfo( token, device ) {
    def response = null    
    def params = [
        uri: "https://fireboard.io/api/v1/devices/${device}.json",
        headers: [Authorization: "Token ${token}"]
    ]
    try {
        httpGet(params) { resp ->
            //log.debug "response data: ${resp.data}"
            response = resp.data
            def date = new Date().parse("yyyy-MM-dd H:mm:ss Z", response?.device_log?.date) //2019-10-07 10:37:45 UTC
            log.debug "Device log date: ${date.format("yyyy-MM-dd h:mm:ss a", location.timeZone).toString()}"
            log.debug "Link Quality:${response?.device_log?.linkquality} Signal Level:${response?.device_log?.signallevel}"
            sendEvent(name: "deviceName", value: "${response?.title}", displayed: false)
            sendEvent(name: "channelCount", value: "${response?.channel_count}", displayed: false)
            sendEvent(name: "wifiSignalLevel", value: !!response?.device_log?.linkquality ? "${Math.round(evaluate(response?.device_log?.linkquality) * 10)}" : "0", displayed: false)
            sendEvent(name: "battery", value: "${Math.round(response?.device_log?.vBattPerRaw * 100)}", displayed: false)            
            response?.channels.sort{ it.channel+(it?.current_temp?0:10) }.eachWithIndex{ it, idx -> updateLabels(it.channel, it.channel_label, response?.degreetype, it.created, it?.current_temp==null, idx==0) }
            response?.latest_temps.sort{ it.channel }.eachWithIndex{ it, idx -> updateTemps(it.channel, it.temp, it.degreetype, it.created, idx==0) }
        }
    } catch (e) {
        log.error "getDeviceInfo() httpGet: $e"
        log.debug "response?.device_log?.signallevel: ${response?.device_log?.signallevel}"
        log.debug "response?.device_log?.linkquality: ${response?.device_log?.linkquality}"
        log.debug "response?.device_log?.vBattPerRaw: ${response?.device_log?.vBattPerRaw}"
        sendEvent (name: "error", value: "--", descriptionText:"getDeviceInfo error device.json", isStateChange: true)
    }
    sendEvent(name: "deviceInfo", value: response ? (settings.deviceStoreDeviceConfig?(new JsonBuilder(response)).toPrettyString():"disabled") : "error", displayed: false)
    return response    
}

def getDeviceTemp( token, device ) {
    def response = null    
    def params = [
        uri: "https://fireboard.io/api/v1/devices/${device}/temps.json",
        headers: [Authorization: "Token ${token}"]
    ]
    try {
        httpGet(params) { resp ->
            //log.debug "response data: ${resp.data}"        
            response = resp.data
            response.sort{ it.channel }.eachWithIndex{ it, idx -> updateTemps(it.channel, it.temp, it.degreetype, it.created, idx==0) }
        }
    } catch (e) {
        log.error "getDeviceTemp() httpGet: $e"
        sendEvent (name: "error", value: "--", descriptionText:"getDeviceTemp error temps.json", isStateChange: true)
    }
    return response    
}

def updateLabels( channel, channel_label, degreetype, created, inactive, auto ) {
    sendEvent(name: "channelLabel${channel}", value: "${channel_label}", displayed: false)
    if(inactive) updateTemps(channel, 0, degreetype, created, auto)
}

def updateTemps( channel, temp, degreetype, created, auto) {
    def unit = degreetype==1?"C":"F"    
    def date = new Date().parse("yyyy-MM-dd'T'H:mm:ss", created) //2019-10-07T11:17:32Z
    def age = Math.round(((new Date()).getTime() - date.getTime())/1000)
    def label = device.currentValue("channelLabel${channel}")
    log.debug "channel:${channel} temp:${temp}°${unit} age:${age.toString()} name:'${label}' ${auto?"(a)":""}"

    sendEvent(name: "channelName${channel}", value: "[ ${label} ]\n${temp}°${unit}", displayed: false)
    sendEvent(name: "channelTemp${channel}", value: "${temp}", unit: "${unit}", displayed: false)

    if(auto&&settings.deviceChannel.toString()=="auto" || settings.deviceChannel.toString()!="auto"&&settings.deviceChannel.toInteger()==channel) {    
        sendEvent(name: "temperature", value: "${temp}", unit: "${unit}", displayed: false)        
        sendEvent(name: "channelLabel", value: "${label}", displayed: false)
        sendEvent(name: "channel", value: channel, descriptionText: "Channel:${channel} Temp:${temp}°${unit} Age:${age}", displayed: true)
        updateContactSensor(channel, temp, label, unit, age)
    }
    // if we dont have any active temps in 10 minutes auto shut down
    if(!!temp) runIn(600, off)
}

def updateContactSensor( channel, temp, label, unit, age ) {
    def target = device.currentValue("channelTarg${channel}")
    def closed = temp.toBigDecimal()>=target.toBigDecimal()
    def power1 = "${label}: �"
    def power2 = "${label}: ${target}°${unit} ✓"
    def power4 = "${label}: ♥"
    def power3 = "${label}: ♡"

    if(age > 20) // temp reading older than 20 seconds. device drops the temp after 180 seconds.
    	sendEvent(name: "power", value: power1, displayed: false)
    else if(closed)
        sendEvent(name: "power", value: power2, displayed: false) // we reached our target temp
    else if(device.currentValue("power")==power4)
        sendEvent(name: "power", value: power3, displayed: false) // heartbeat
    else
        sendEvent(name: "power", value: power4, displayed: false) // heartbeat

    if(closed) {        
        sendEvent(name: "contact", value: "closed", descriptionText: "Contact is closed: ${temp}°${unit}")
    } else {
        sendEvent(name: "contact", value: "open", descriptionText: "Contact is open")
    }
}