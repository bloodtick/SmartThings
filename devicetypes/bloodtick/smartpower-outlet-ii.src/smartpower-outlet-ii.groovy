/*
 *	Copyright 2016 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *	use this file except in compliance with the License. You may obtain a copy
 *	of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *	License for the specific language governing permissions and limitations
 *	under the License.
 */
metadata {
	// Automatically generated. Make future change here.
	definition(name: "SmartPower Outlet II", namespace: "bloodtick", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch-power", ocfDeviceType: "oic.d.smartplug", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: true) {
        capability "Actuator"
        capability "Switch"
        capability "Power Meter"
        capability "Configuration"
        capability "Refresh"
        capability "Sensor"
        capability "Health Check"
        capability "Outlet"
        capability "Contact Sensor"

        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3200", deviceJoinName: "Outlet"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3200-Sgb", deviceJoinName: "Outlet"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "4257050-RZHAC", deviceJoinName: "Outlet"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 000F, 0B04", outClusters: "0019", manufacturer: "SmartThings", model: "outletv4", deviceJoinName: "Outlet"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019"
        fingerprint profileId: "0104", inClusters: "0000,0003,0006,0009,0B04", outClusters: "0019", manufacturer: "Samjin", model: "outlet", deviceJoinName: "Outlet"
        fingerprint profileId: "0010", inClusters: "0000 0003 0004 0005 0006 0008 0702 0B05", outClusters: "0019", manufacturer: "innr", model: "SP 120", deviceJoinName: "Innr Smart Plug EU"
        fingerprint profileId: "0104", inClusters: "0000,0002,0003,0004,0005,0006,0009,0B04,0702", outClusters: "0019,000A,0003,0406", manufacturer: "Aurora", model: "SmartPlug51AU", deviceJoinName: "Aurora SmartPlug"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0702,0B04,0B05,FC03", outClusters: "0019", manufacturer: "CentraLite", model: "3210-L", deviceJoinName: "Iris Smart Plug"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
					"http://cdn.device-gse.smartthings.com/Outlet/US/OutletUS1.jpg",
					"http://cdn.device-gse.smartthings.com/Outlet/US/OutletUS2.jpg"
			])
		}
        input(name:"logPower", type: "bool", title: "Log Power(W) Meter Events", description: "", defaultValue: "false", displayDuringSetup: false)
        input(name:"roundPower", type: "number", title: "Round Power(W) Meter Values by:", description: "0=decimal, 1-10=mod round to value", range: "0..10", defaultValue: 1, displayDuringSetup: false)
        input(name:"contactLo", type: "decimal", title: "Contact Open when Power(W) under:", description: "", range: "0.0..*", defaultValue: 0.0, displayDuringSetup: false)
        input(name:"contactHi", type: "decimal", title: "Contact Closed when Power(W) over:", description: "", range: "0.1..*", defaultValue: 0.1, displayDuringSetup: false)
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: 'Turning On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: 'Turning Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute("power", key: "SECONDARY_CONTROL") {
				attributeState "power", label: '${currentValue} W'
			}
		}

        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        standardTile("contact", "device.contact", inactiveLabel: true, width: 2, height: 2) {
            state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
            state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#CCCCCC")
        }

		main "switch"
		details(["switch", "contact", "refresh"])
	}
}

def installed() {
    settings.logPower = false
    settings.roundPower = 1
    settings.contactHi = 0.1
    settings.contactLo = 0.0
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    if(settings.contactLo>=settings.contactHi)
    	settings.contactHi = settings.contactLo + 0.1
    log.debug "Executing 'initialize()' with new preferences: ${settings}"    
}

// Parse incoming device messages to generate events
def parse(String description) {
	//log.debug "description is $description"

	def event = zigbee.getEvent(description)

	if (event) {
		if (event.name == "power") {
			def value = settings.roundPower!=0 ? (Math.round((event.value as Integer) / (10*settings.roundPower)) * settings.roundPower) : ((event.value as Integer) / 10)
			event = contactSensor( value)
		} else if (event.name == "switch") {
			event = createEvent(name: event.name, value: event.value )
		}
	} else {
		def cluster = zigbee.parse(description)

		if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
			if (cluster.data[0] == 0x00) {
				log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
				event = createEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
			} else {
				log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
				event = null
			}
		} else {
			log.warn "DID NOT PARSE MESSAGE for description : $description"
			log.debug "${cluster}"
		}
	}
	return event ? createEvent(event) : event
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.onOffRefresh()
}

def refresh() {
	zigbee.onOffRefresh() + zigbee.electricMeasurementPowerRefresh()
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	refresh() + zigbee.onOffConfig(0, 300) + zigbee.electricMeasurementPowerConfig()
}

def contactSensor( power ) {

    def event
    if(device.currentValue("switch")=="off") power = 0

    if (settings.logPower)
    	event = createEvent(name: "power", value: power, descriptionText: "Power is ${power} Watts")
    else
        event = createEvent(name: "power", value: power, displayed: false )  

    if (settings.contactHi>0 && power>=settings.contactHi)
    	sendEvent(name: "contact", value: "closed", descriptionText: "Contact is closed: ${power} Watts")
    else if (settings.contactLo>=power)
        sendEvent(name: "contact", value: "open", descriptionText: "Contact is open: ${power} Watts")

    return event
}