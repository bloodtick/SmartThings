/**
 *  Copyright 2021 Bloodtick Jones
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
 *  Virtual Momentary Webhook Switch
 *
 *  Author: bloodtick
 *  Date: 2020-05-09
 */
Boolean isST() { return (getPlatform() == "SmartThings") }

metadata {
    definition (name: "Virtual Momentary Webhook Switch", namespace: "bloodtick", author: "SmartThings/Hubitat", ocfDeviceType: "oic.d.switch") {
        capability "Actuator"
        capability "Switch"
        capability "Refresh"
        capability "Momentary"
        capability "Sensor"
        
        command "sendPostCmd", ["string"]
    }
}

preferences {
    input(name:"deviceIp", type:"text", title: "Device IP Address", description: "Device IP Address", defaultValue: "127.0.0.1", required: true, displayDuringSetup: true)
    input(name:"devicePort", type:"number", title: "Device IP Port", description: "Default is port 2323", range: "1..65535", defaultValue: "2323", required: false, displayDuringSetup: true)
    input(name:"cmd", type:"string", title:"API Endpoint", defaultValue:"", required:true)
    input(name:"delay", type: "number", title: "Delay Switching Off", defaultValue: 2)
    input(name:"deviceLogEnable", type: "bool", title: "Enable debug logging:", defaultValue: false) 
}

def installed() {
    settings.delay = 2
    settings.devicePort = 2323
    settings.deviceLogEnable = false
    sendEventOff()
}

def parse(String description) {
    logDebug "${device.displayName} executing 'parse()' ${description}"
}

def push() {
    if (device.currentValue("switch") != "on" && settings.cmd) {
        sendPostCmd(settings.cmd)
    }        

    sendEvent(name: "switch", value: "on", display: false)
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
    if (delay)
    	runIn(settings.delay, sendEventOff)
    else
        sendEventOff()	
    logInfo "${device.displayName} was pushed" + ((delay) ? " with delay ${settings.delay} sec" : "")
}

def on() {
    logDebug "${device.displayName} executing 'on()'"
    push()
}

def off() {
    logDebug "${device.displayName} executing 'off()'"
    push()
}

def sendEventOff() {
    sendEvent(name: "switch", value: "off", display: false)
}

def sendPostCmd(cmd) {
    logDebug "${device.displayName} executing 'sendPostCmd()' ${cmd}"

    def hubAction 	
    try {
        def param = [
            method: "POST",
            path: cmd,
            headers: [HOST: "${settings.deviceIp}:${settings.devicePort}"]
        ]
        hubAction = (isST()) ? physicalgraph.device.HubAction.newInstance(param) : hubitat.device.HubAction.newInstance(param)
    }
    catch (Exception e) {
        logError "sendPostCmd() $e on $hubAction"
    }
    if (hubAction) {
        try {
            sendHubCommand( hubAction )
        }
        catch (Exception e) {
            logError "sendPostCmd() $e on $sendHubCommand"
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

private logInfo(msg)  { log.info "${msg}" }
private logDebug(msg) { if(settings?.deviceLogEnable == true) { log.debug "${msg}" } }
private logTrace(msg) { if(settings?.deviceTraceEnable == true) { log.trace "${msg}" } }
private logWarn(msg)  { log.warn  "${msg}" } 
private logError(msg) { log.error  "${msg}" }