/**
 *  Copyright 2015 SmartThings
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
 * Wemo Switch
 *
 * Author: Juan Risso (SmartThings)
 * Date: 2015-10-11
 *
 * Update: Bloodtick Jones
 * Date: 2019-03-22
 */
 metadata {
 	definition (name: "WeMo Insight Switch II", namespace: "bloodtick", author: "SmartThings") {
        capability "Actuator"
        capability "Switch"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Power Meter"
        capability "Contact Sensor"
        capability "Health Check"

        command "subscribe"
        command "resubscribe"
        command "unsubscribe"
        command "setOffline"
        command "reset"
 }

 // simulator metadata
 simulator {}

 // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"rich-control", type: "lighting", canChangeIcon: true){
            tileAttribute ("device.switch2", key: "PRIMARY_CONTROL") {
                 attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.off", backgroundColor:"#00a0dc", nextState:"turningOff"
                 attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
                 attributeState "turningOn", label:'${name}', /*action:"switch.off",*/ icon:"st.switches.switch.off", backgroundColor:"#00a0dc"
                 attributeState "turningOff", label:'${name}', /*action:"switch.on",*/ icon:"st.switches.switch.on", backgroundColor:"#ffffff"
                 //attributeState "offline", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ff0000"
                 attributeState "standby", label:'${name}', action:"switch.off", icon:"st.switches.switch.off", backgroundColor:"#cccccc", nextState:"turningOff"
 			}
            tileAttribute ("power", key: "SECONDARY_CONTROL") {
                attributeState "power", label:'${currentValue} W'
            }
        }

        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.off", backgroundColor:"#00a0dc", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.off", backgroundColor:"#00a0dc", nextState:"turningOff"
            state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
            //state "offline", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ff0000"
            //state "standby", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#cccccc"
        }

        valueTile("currentIP", "device.currentIP", height: 1, width: 3, decoration: "flat") {
            state "default", label:'[ Current IP ]\n${currentValue}'
        }
        valueTile("lastPoll", "device.lastPoll", height: 1, width: 3, decoration: "flat") {
            state "default", label:'[ Last Response ]\n${currentValue}'
        }
        valueTile("cost_tile", "device.cost_tile", height: 1, width: 2, decoration: "flat") {String.format("\$%5.2f", est_cost_today)
            state "default", label:'${currentValue}'
        }
        valueTile("power_tile", "device.power_tile", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }
        valueTile("onFor_tile", "device.onFor_tile", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }        
        valueTile("power_lo", "device.power_lo", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Low Power ]\n ${currentValue} W'
        }
        valueTile("power_hi", "device.power_hi", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ High Power ]\n ${currentValue} W'
        }
        standardTile("resetWatts", "device.switch", inactiveLabel: false, height: 1, width: 2, decoration: "flat") {
            state "default", label:'RESET', action:"reset"
        }
        valueTile("onNowFor_tile", "device.onNowFor_tile", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }        
        valueTile("onSince_tile", "device.onSince_tile", height: 1, width: 2, decoration: "flat") {
            state "default", label:'${currentValue}'
        }
        valueTile("contact", "device.contact", height: 1, width: 2, decoration: "flat") {
            state "default", label:'[ Contact ]\n${currentValue}\n'
        }        
        valueTile("debug_tile", "device.debug_tile", height: 1, width: 4, decoration: "flat") {
            state "default", label:'${currentValue}'
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, height: 1, width: 2, decoration: "flat") {
            state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
        }

        main(["switch"])
        details(["rich-control", "currentIP", "lastPoll", "onNowFor_tile", "onSince_tile", "contact", "power_tile", "onFor_tile", "cost_tile", "power_lo", "power_hi", "resetWatts", "debug_tile", "refresh"])
    }
    
    preferences {
        input "costperKWh", "decimal", title: "Cost per kWh in dollars", description: "Default is .15", range: "0.001..2", displayDuringSetup: false
        input "pollRateSecs", "number", title: "Poll Rate when Switch is not off", description: "Default is 300 seconds", range: "15..300", displayDuringSetup: false
        input "returnLastState", "bool", title: "Return to last known state", description: "Return switch to last known state if declared offline", defaultValue: "true", displayDuringSetup: false
		input "logPower", "bool", title: "Log Power Deltas", description: "Changes in power levels will be logged when enabled", defaultValue: "false", displayDuringSetup: false
        input "msgDebug", "bool", title: "Show Message Counts", description: "Display logged message counts and increase live logging", defaultValue: "false", displayDuringSetup: false
    }
}

def installed() {
    settings.logPower = false
    settings.pollRateSecs = 300
    settings.costperKWh = 0.15f
    settings.returnLastState = true
    settings.msgDebug = false
    state.parse1 = state.parse2 = state.parse3 = state.parse4 = state.parse5 = state.parse6 = state.parse7 = state.parse8 = state.parse9 = 0
    state.lastState = "unknown"
	log.debug "Executing 'installed()' with settings: ${settings}"
    initialize()
}

def updated(){
	log.debug "Executing 'updated()' with new preferences"
    if (!msgDebug) sendEvent(name: "debug_tile", value: "", displayed: false)
    initialize()
    refresh()
}

def initialize() {
	log.debug "Executing 'initialize()'"
    sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device.hub.hardwareID}\"}", displayed: false)
	sendEvent(name: "checkInterval", value: 32 * 60, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
}

def ping() {
	log.debug "Executing 'ping()'"
	refresh()
}

def reset() {
	log.debug "Executing 'reset()'"
    sendEvent(name: "power_lo", value: "--", displayed: false)
    sendEvent(name: "power_hi", value: "--", displayed: false)
    state.parse1 = state.parse2 = state.parse3 = state.parse4 = state.parse5 = state.parse6 = state.parse7 = state.parse8 = state.parse9 = 0
    if (msgDebug) sendEvent(name: "debug_tile", value: "", displayed: false)
}

def decodeInsightResponse( String[] params ) {
	log.debug "Executing 'decodeInsightResponse'"
    
	if (params.size()<10) {
    	log.error "Expecting at least 10 parameters. Recieved ${params.size()}"
        return
    }
   
    def value = (params[0].toInteger() == 0 ? "off" : (params[0].toInteger() == 1 ? "on" : "standby")) // 0 if off, 1 if on, 8 if on but Threshold load is off
    def lastchange = Math.round(params[1].toInteger()) //date in seconds since 1970/01/01 of lastchange
    def onFor = Math.round(params[2].toInteger() / 3600*10)/10 //Current’s activity in 1/10 hours [standby doesnt count]
    def onToday = Math.round(params[3].toInteger() / 3600*10)/10 //Today’s activity in 1/10 hours [does not include standby time]
    def onTotal = Math.round(params[4].toInteger() / 3600*10)/10 //Total activity in 1/10 hours [does not include standby time]
    def timeperiod = Math.round(params[5].toInteger() / 3600*10)/10 //Time Period over which averages are calculated in 1/10 hours
    def avgPower = Math.round(params[6].toInteger()) //Average power in watts when on
    def power = Math.round(params[7].toInteger() / 1000*10)/10 //Current power usage in watts
    def energy = Math.round(params[8].toInteger() / 60000) //Today's energy usage in mWh
    def energyT = Math.round(params[9].toFloat() / 60000) //Total energy usage in mWh
    // only returned with GetInsightParamsResponse.
    // use state.threshold as global and default to 2 first time around. 
    def threshold = state.threshold = (params.size()>=11 ? (Math.round(params[10].toInteger() / 1000)) : (state.threshold?:2)) //On-Standby Power Threshold in watts

    def lastchange_str = new Date(lastchange.toLong()*1000).format("yyyy-MM-dd h:mm:ss a", location.timeZone)
    def lastpoll_str = new Date().format("yyyy-MM-dd h:mm:ss a", location.timeZone)
    def onSince_tile_str = "[ ${value=="off" ? "Off" : value=="on" ? "On" : "Standby"} since ]\n ${lastchange_str}"
    def onNowFor_tile_str = "[ ${value=="on" ? "On now for" : "Last on for"} ]\n${String.format("%1.1f", onFor.toFloat())} hrs\n${threshold>0?"Threshold ${threshold} W":""}"
    def cost_tile_str = " "
    def power_tile_str = " "
  	def onFor_tile_str = " " 
    
    if (msgDebug) { 
        log.trace "[0] Status: $value" 
        log.trace "[1] Lastchange since 1970/01/01: $lastchange secs or $lastchange_str"
        log.trace "[2] On current: $onFor hrs (no standby)"
        log.trace "[3] On today: $onToday hrs (no standby)"
        log.trace "[4] On total: $onTotal hrs (per timeperiod, no standby)"
        log.trace "[5] Timeperiod: $timeperiod hrs"
        log.trace "[6] Power Avg when on: $avgPower watts"
        log.trace "[7] Power Now: $power watts"
        log.trace "[8] Energy: $energy mWh (used today)"            
        log.trace "[9] Energy: $energyT mWh (per timeperiod)"
        log.trace "[10] On/Standby Threshold: $threshold watts"
    }

	// after resetting stats via the WeMo app the timeperiod comes back negative.
    if (timeperiod>0) {
        def est_cost_today = Math.ceil((energy/1000)*(costperKWh?:0.15)*100)/100
        def avg_on_per_day = Math.round((onTotal/timeperiod)*24*100)/100  // does not include standby
        def avg_power_per_day = Math.round(energyT/timeperiod)
        def avg_cost_per_day = Math.ceil((24*avg_power_per_day/1000)*(costperKWh?:0.15)*100)/100
        def avg_cost_per_month = Math.ceil((24*avg_power_per_day/1000)*(costperKWh?:0.15)*100*(365/12))/100

		if (msgDebug) {
            log.trace "[*1] Estimated cost today: ${String.format("\$%1.2f", est_cost_today)}"
            log.trace "[*2] Avg On per day: $avg_on_per_day hrs (no standby)"
            log.trace "[*3] Avg power over timeperiod: $avg_power_per_day watts"
            log.trace "[*4] Avg cost per day: ${String.format("\$%1.2f", avg_cost_per_day)}"
            log.trace "[*5] Avg cost per month: ${String.format("\$%1.2f", avg_cost_per_month)}"
        }

		cost_tile_str = "[ Cost ]\n Est monthly: ${String.format("\$%1.2f", avg_cost_per_month)}\n Today: ${String.format("\$%1.2f", est_cost_today)}"
        power_tile_str = "[ Power ]\n Avg when on: ${String.format("%1.0f", avgPower.toFloat())} W\n Now: ${String.format("%1.1f", power.toFloat())} W"
        onFor_tile_str = "[ On For ]\n Avg per day: ${String.format("%1.1f", avg_on_per_day.toFloat())} hrs\n Today: ${String.format("%1.1f", onToday.toFloat())} hrs" 
    }

	// so the WeMo insite switch does not return from a power cycle to its last known state. lets try to help that.
	if(returnLastState && device.currentValue("status")=="offline" && state.lastState!="off" && value=="off") {
    	log.debug "Notice: switch returned from offline in different state. Returning to On() state"
    	sendHubCommand( on() )
        return
    }
    
    sendEvent(name: "switch", value: value=="off"?"off":"on", displayed: false)
    sendEvent(name: "switch2", value: value, descriptionText: "Switch is ${value}", displayed: true)
    state.lastState = value
    
    sendEvent(name: "power", value: power, descriptionText: "Power is ${power} Watts", displayed: logPower)
    sendEvent(name: "currentIP", value: "${getHostAddress()}", descriptionText: "IP is ${getHostAddress()}", displayed: true)
    sendEvent(name: "lastPoll", value: lastpoll_str, displayed: false)
	sendEvent(name: "power_tile", value: power_tile_str, displayed: false)
    sendEvent(name: "onFor_tile", value: onFor_tile_str, displayed: false)
	sendEvent(name: "cost_tile", value: cost_tile_str, displayed: false)
    sendEvent(name: "onSince_tile", value: onSince_tile_str, displayed: false)
    sendEvent(name: "onNowFor_tile", value: onNowFor_tile_str, displayed: false)

    if (value=="on")
    	sendEvent(name: "contact", value: "closed", descriptionText: "Contact is closed: ${power} Watts")
    else
    	sendEvent(name: "contact", value: "open", descriptionText: "Contact is open: ${power} Watts")

    if (!device.currentValue("power_hi") || device.currentValue("power_hi")=="--" || (power.toFloat()>(device.currentValue("power_hi")).toFloat() && value!="off"))
    	sendEvent(name: "power_hi", value: "${power}", descriptionText: "Power high is $power Watts", displayed: true)
    if (!device.currentValue("power_lo") || device.currentValue("power_lo")=="--" || (power.toFloat()<(device.currentValue("power_lo")).toFloat() && value!="off"))
    	sendEvent(name: "power_lo", value: "${power}", descriptionText: "Power low is $power Watts", displayed: true)
    
	if (value!="off" && pollRateSecs) {
    	log.trace "Run autoPollWeMoInsight in ${pollRateSecs.toInteger()} seconds"
    	runIn(pollRateSecs.toInteger(), autoPollWeMoInsight)
    } 
}

// parse events into attributes
def parse(String description) {

    def msg = parseLanMessage(description)
    def headerString = msg.header
	//log.debug "Parsing '${msg}'"
    if (headerString?.contains("SID: uuid:")) {
        def sid = (headerString =~ /SID: uuid:.*/) ? ( headerString =~ /SID: uuid:.*/)[0] : "0"
        sid -= "SID: uuid:".trim()
        updateDataValue("subscriptionId", sid)
 	}

    def result = []
    def bodyString = msg.body
    if (bodyString) {
        try {
            unschedule("setOffline")
        } catch (e) {
            log.error "unschedule(\"setOffline\")"
        }
        // everything was healthy. tell smartthings & decode.
        setOnline()
            
        def body = new XmlSlurper().parseText(bodyString)
 		if (body?.property?.TimeSyncRequest?.text()) {
        	log.trace "Parse[1]: TimeSyncRequest msg"
        	result << timeSyncResponse()
            state.parse1 = state.parse1+1
        } else if (body?.Body?.TimeSyncResponse?.text()) {
            def params = body?.Body?.TimeSyncResponse?.text()
            log.trace "Parse[2]: TimeSyncResponse msg = ${params}"
            state.parse2 = state.parse2+1    
        } else if (body?.property?.TimeZoneNotification?.text()) {
 			log.debug "Parse[3]: TimeZoneNotification msg = ${body?.property?.TimeZoneNotification?.text()}"
            state.parse3 = state.parse3+1
  		} else if (body?.Body?.SetBinaryStateResponse?.BinaryState?.text()) {
            def params = body?.Body?.SetBinaryStateResponse?.BinaryState?.text().split("\\|")
            log.trace "Parse[4]: SetBinaryStateResponse.BinaryState msg = ${params}"
            decodeInsightResponse( params )
            state.parse4 = state.parse4+1
 		} else if (body?.property?.BinaryState?.text()) {
            def params = body?.property?.BinaryState?.text().split("\\|")
            log.trace "Parse[5]: Property.BinaryState msg = ${params}"        
    		decodeInsightResponse( params )
            state.parse5 = state.parse5+1 
        } else if (body?.Body?.GetInsightParamsResponse?.InsightParams?.text()) {
            def params = body?.Body?.GetInsightParamsResponse?.InsightParams?.text().split("\\|") 
            log.trace "Parse[6]: GetInsightParamsResponse msg = ${params}"        
    		decodeInsightResponse( params )
            state.parse6 = state.parse6+1
 		} else if (body?.Body?.GetBinaryStateResponse?.BinaryState?.text()) {
            def params = body?.Body?.GetBinaryStateResponse?.BinaryState?.text().split("\\|")
            log.trace "Parse[7]: GetBinaryResponse.BinaryState msg = ${params}"
            decodeInsightResponse( params )
            state.parse7 = state.parse7+1
 		} else if (body?.property?.EnergyPerUnitCost?.text()) {
            def params = body?.property?.EnergyPerUnitCost?.text().split("\\|")
            log.trace "Parse[8]: Property.EnergyPerUnitCost msg = ${params}"
            state.parse8 = state.parse8+1      
        } else {
        	log.debug "Parse[9]: Failed to parse msg event '${msg}'"
            state.parse9 = state.parse9+1
        }
        
        if (msgDebug) {
        	def debug_tile_str = "${state.parse1}:${state.parse2}:${state.parse3}:${state.parse4}:${state.parse5}:${state.parse6}:${state.parse7}:${state.parse8}-${state.parse9}"
        	result << createEvent(name: "debug_tile", value: debug_tile_str, displayed: false)
        }
 	}
    
	result
}

private getTime() {
    // This is essentially System.currentTimeMillis()/1000, but System is disallowed by the sandbox.
    ((new GregorianCalendar().time.time / 1000l).toInteger()).toString()
}

private getCallBackAddress() {
 	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private Integer convertHexToInt(hex) {
 	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
 	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
 	def ip = getDataValue("ip")
 	def port = getDataValue("port")
 	if (!ip || !port) {
 		def parts = device.deviceNetworkId.split(":")
 		if (parts.length == 2) {
 			ip = parts[0]
 			port = parts[1]
 		} else {
 			log.warn "Can't figure out ip and port for device: ${device.id}"
		 }
 	}
 	if (msgDebug) log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
 	return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

def on() {
log.debug "Executing 'on()'"
def turnOn = new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPAction: "urn:Belkin:service:basicevent:1#SetBinaryState"
Host: ${getHostAddress()}
Content-Type: text/xml
Content-Length: 333

<?xml version="1.0"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<SOAP-ENV:Body>
 <m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
<BinaryState>1</BinaryState>
 </m:SetBinaryState>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>""", physicalgraph.device.Protocol.LAN)
}

def off() {
log.debug "Executing 'off()'"
def turnOff = new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPAction: "urn:Belkin:service:basicevent:1#SetBinaryState"
Host: ${getHostAddress()}
Content-Type: text/xml
Content-Length: 333

<?xml version="1.0"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<SOAP-ENV:Body>
 <m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
<BinaryState>0</BinaryState>
 </m:SetBinaryState>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>""", physicalgraph.device.Protocol.LAN)
}

def subscribe() {
log.debug "Executing 'subscribe()' with 600 second timeout"
def address = getCallBackAddress()
def hostAddress = getHostAddress()
new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${hostAddress}
CALLBACK: <http://${address}/>
NT: upnp:event
TIMEOUT: Second-600
User-Agent: CyberGarage-HTTP/1.0


""", physicalgraph.device.Protocol.LAN)
}

def autoSubscribe() {
	sendHubCommand( subscribe() )
}

def autoPollWeMoInsight() {
	sendHubCommand( pollWeMoInsight() )
}

def refresh() {
    log.debug "Executing WeMo Switch 'autoSubscribe', then 'autoPollWeMoInsight'"
    unschedule("autoSubscribe")
    runEvery10Minutes(autoSubscribe)
    unschedule("autoPollWeMoInsight")
    runEvery5Minutes(autoPollWeMoInsight)
    
    [autoSubscribe(), autoPollWeMoInsight()]
}

def subscribe(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
         log.debug "Updating ip from $existingIp to $ip"    
    	 updateDataValue("ip", ip)
    	 //def ipvalue = convertHexToIP(getDataValue("ip"))
         //sendEvent(name: "currentIP", value: ipvalue, descriptionText: "IP changed to ${ipvalue}")
    }
 	if (port && port != existingPort) {
 		log.debug "Updating port from $existingPort to $port"
 		updateDataValue("port", port)
	}
	subscribe("${ip}:${port}")
}

def resubscribe() {
    log.debug "Executing 'resubscribe()'"
    def sid = getDeviceDataByName("subscriptionId")
new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}
TIMEOUT: Second-300


""", physicalgraph.device.Protocol.LAN)
}


def unsubscribe() {
    def sid = getDeviceDataByName("subscriptionId")
new physicalgraph.device.HubAction("""UNSUBSCRIBE publisher path HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}


""", physicalgraph.device.Protocol.LAN)
}


//TODO: Use UTC Timezone
def timeSyncResponse() {
log.debug "Executing 'timeSyncResponse()'"
new physicalgraph.device.HubAction("""POST /upnp/control/timesync1 HTTP/1.1
Content-Type: text/xml; charset="utf-8"
SOAPACTION: "urn:Belkin:service:timesync:1#TimeSync"
Content-Length: 376
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
 <s:Body>
  <u:TimeSync xmlns:u="urn:Belkin:service:timesync:1">
   <UTC>${getTime()}</UTC>
   <TimeZone>-05.00</TimeZone>
   <dst>0</dst>
   <DstSupported>1</DstSupported>
  </u:TimeSync>
 </s:Body>
</s:Envelope>
""", physicalgraph.device.Protocol.LAN)
}

def setOnline() {
    sendEvent(name: "status", value: "online", displayed: true)
    sendEvent(name: "healthStatus", value: "online", displayed: false)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
}

def setOffline() {
	log.debug "Executing 'setOffline()'"
    //sendEvent(name: "switch", value: "offline", descriptionText: "The device is offline")
    //sendEvent(name: "switch2", value: "offline", displayed: false)
	sendEvent(name: "status", value: "offline", displayed: true, isStateChange: true)
    sendEvent(name: "healthStatus", value: "offline", displayed: false, isStateChange: true)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
}

def poll() {
	log.debug "Executing 'poll()'"
}

def pollWeMoInsight() {
	log.debug "Executing 'pollWeMoInsight()'"
	if (device.currentValue("status") != "offline")
        runIn(30, setOffline)

	new physicalgraph.device.HubAction([
        'method': 'POST',
        'path': '/upnp/control/insight1',
        'body': """
                <?xml version="1.0" encoding="utf-8"?>
                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
                    <s:Body>
                        <u:GetInsightParams xmlns:u="urn:Belkin:service:insight:1"></u:GetInsightParams>
                    </s:Body>
                </s:Envelope>
                """,
        'headers': [
            'HOST': getHostAddress(),
            'Content-type': 'text/xml; charset=utf-8',

            'SOAPAction': "\"urn:Belkin:service:insight:1#GetInsightParams\""
        ]
    ], device.deviceNetworkId)
}