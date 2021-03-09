/**
    *  Copyright 2021 Bloodtick
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
    *  Virtual SharpTools Image Tile
    *
    *  Update: Bloodtick Jones
    *  Date: 2021-03-08
    *
    *  1.0.00 2021-03-09 First release to support Hubitat. 
    */
import groovy.json.*

    private getVersionNum()   { return "1.0.00" }
private getVersionLabel() { return "Virtual SharpTools Image Tile, version ${getVersionNum()}" }

metadata {
    definition (name: "Virtual SharpTools Image Tile", namespace: "bloodtick", author: "Hubitat", ocfDeviceType: "oic.d.switch") {
        capability "Actuator"
        capability "Switch"
        capability "PushableButton"
        capability "DoubleTapableButton"
        capability "Momentary"
        capability "Sensor"
        capability "Relay Switch"
        capability "Music Player"        

        command "push"
        command "doubleTap"
        command "toggle"
        command "updateImage", ["String"]
        command "updateDescription", ["String"]
    }
}

preferences {
    input(name:"deviceMomentary", type: "bool", title: "Enable Switch to act like Momentary button", defaultValue: false)
    input(name:"deviceTappedToggle", type: "bool", title: "Toggle Switch when Tile is single tapped", defaultValue: true)
    input(name:"deviceDoubleTappedToggle", type: "bool", title: "Toggle Switch when Tile is double tapped", defaultValue: true)    

    input(name:"deviceOnEnable", type: "bool", title: "Enable image and text updates when Switch is on", defaultValue: true)
    input(name:"deviceImageOn", type:"string", title:"Image when Switch is On", defaultValue:"", required:false)
    input(name:"deviceTextOn", type:"string", title:"Description when Switch is On", defaultValue:"", required:false)

    input(name:"deviceOffEnable", type: "bool", title: "Enable image and text updates when Switch is off", defaultValue: true)
    input(name:"deviceImageOff", type:"string", title:"Image when Switch is Off", defaultValue:"", required:false)
    input(name:"deviceTextOff", type:"string", title:"Description when Switch is Off", defaultValue:"", required:false)

    if (!deviceTappedToggle) {
        input(name:"deviceTappedEnable", type: "bool", title: "Enable image and text updates when Tile is single tapped", defaultValue: false)
        input(name:"deviceImageTapped", type:"string", title:"Image when Tile is single tapped", defaultValue:"", required:false)
        input(name:"deviceTextTapped", type:"string", title:"Description when Tile is single tapped", defaultValue:"", required:false)
    }   

    if (!deviceDoubleTappedToggle) {
        input(name:"deviceDoubleTappedEnable", type: "bool", title: "Enable image and text updates when Tile is double tapped", defaultValue: false)
        input(name:"deviceImageDoubleTapped", type:"string", title:"Image when Tile is double tapped", defaultValue:"", required:false)
        input(name:"deviceTextDoubleTapped", type:"string", title:"Description when Tile is double tapped", defaultValue:"", required:false)
    }

    input(name:"deviceLogEnable", type: "bool", title: "Enable debug logging", defaultValue: false)
}

def installed() {
    settings.deviceMomentary = false
    settings.deviceTappedToggle = true
    settings.deviceDoubleTappedToggle = true
    settings.deviceOnEnable = true
    settings.deviceOffEnable = true
    settings.deviceTappedEnable = false
    settings.deviceDoubleTappedEnable = false
    settings.deviceLogEnable = false
    sendEvent(name: "switch", value:"off", displayed: false)    
    logDebug "Executing 'installed()' with settings: ${settings}"
    updated()
}

def updated() {
    logDebug "Executing 'updated()' with new preferences: ${settings}"
    sendEvent(name: "numberOfButtons", value: 1, displayed: false)
    sendEvent(name: "status", value:"stopped", display: false, displayed: false)  // this prevents playing bars on image grapic    
    logInfo "${device.displayName} preferences saved"
    initialize()
}

def initialize() {
}

def parse(String description) {
    logDebug description
}

def updateImage(image) {
    def trackData = [:]
    trackData["albumArtUrl"] = image

    logDebug "trackData: ${trackData}"
    sendEvent(name:"trackData", value: JsonOutput.toJson(trackData), display: false, displayed: false)
}

def updateDescription(description) {
    def trackDesc = description

    logDebug "trackDescription: ${trackDesc}"
    sendEvent(name:"trackDescription", value: trackDesc, display: false, displayed: false)        
}

def on() {
    if (settings.deviceOnEnable) {
        updateImage(settings.deviceImageOn)
        updateDescription(settings.deviceTextOn)
    }
    sendEvent(name: "switch", value: "on", display: false)
    logInfo "${device.displayName} is on"

    if(settings.deviceMomentary) runIn(1, off)
}

def off() {
    if (settings.deviceOffEnable) {
        updateImage(settings.deviceImageOff)
        updateDescription(settings.deviceTextOff)
    }
    sendEvent(name: "switch", value: "off", display: false)
    logInfo "${device.displayName} is off"
}

def push() {
    play()
}

def play() {
    logInfo "${device.displayName} was pushed"
    sendEvent(name:"pushed", value: 1, descriptionText: "${device.displayName} was pushed", isStateChange: true )

    if (settings.deviceTappedToggle)
    toggle()    
    else if (settings.deviceTappedEnable) {        
        updateImage(settings.deviceImageTapped)
        updateDescription(settings.deviceTextTapped)
    }
}

def pause() {
    logDebug "pause() was pressed" // this should not happen
}

def doubleTap() {
    nextTrack()
}

def nextTrack() {
    logInfo "${device.displayName} was doubleTapped"
    sendEvent(name:"doubleTapped", value: 1, descriptionText: "${device.displayName} was doubleTapped", isStateChange: true )

    if (settings.deviceDoubleTappedToggle)
    toggle()
    else if (settings.deviceDoubleTappedEnable) {
        updateImage(settings.deviceImageDoubleTapped)
        updateDescription(settings.deviceTextDoubleTapped)
    }
}

def toggle() {
    logDebug "toggle() state"
    if(device.currentState("switch")?.value == "on")
    off()
    else
        on()    
}

private logInfo(msg)  { log.info "${msg}" }
private logDebug(msg) { if(settings?.deviceLogEnable == true) { log.debug "${msg}" } }
private logTrace(msg) { if(settings?.deviceTraceEnable == true) { log.trace "${msg}" } }
private logWarn(msg)  { log.warn  "${msg}" } 
private logError(msg) { log.error  "${msg}" }
