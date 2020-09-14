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
*  Bose SoundTouch II
*  Ported from https://github.com/SmartThingsCommunity/SmartThingsPublic/blob/master/devicetypes/smartthings/bose-soundtouch.src/bose-soundtouch.groovy
*
*  Update: Bloodtick Jones
*  Date: 2020-09-13
*
*  1.0.00 2020-09-13 First release to support Hubitat. Ported from old SmartThings base code. Probably very buggy.
*
*/

import groovy.json.*
import groovy.xml.XmlUtil

private getVersionNum()   { return "1.0.00" }
private getVersionLabel() { return "Bose SoundTouch II, version ${getVersionNum()}" }

Boolean isST() { return (getPlatform() == "SmartThings") }

// for the UI
metadata {
    definition (name: "Bose SoundTouch II", namespace: "bloodtick", author: "Hubitat/SmartThings", ocfDeviceType: "x.com.st.musicplayer") {
        capability "Switch"
        capability "Switch Level" // added for Alexa
        capability "Refresh"
        capability "Music Player"
        capability "AudioVolume"
        capability "Health Check"
        capability "Sensor"
        capability "Actuator"

        command "preset1"
        command "preset2"
        command "preset3"
        command "preset4"
        command "preset5"
        command "preset6"
        command "aux"
        
        attribute "preset1", "string"
        attribute "preset2", "string"
        attribute "preset3", "string"
        attribute "preset4", "string"
        attribute "preset5", "string"
        attribute "preset6", "string"
        
        attribute "manufacturer", "string"
        attribute "model", "string"
        attribute "deviceID", "string"

        //command "everywhereJoin"
        //command "everywhereLeave"

        //command "forceOff"
        //command "forceOn"
    }
    
    multiAttributeTile(name: "mediaPlayer", type:"mediaPlayer", width:6, height:4) {
        tileAttribute("device.status", key: "PRIMARY_CONTROL") {
            attributeState("paused", label:"Paused",)
            attributeState("playing", label:"Playing")
            attributeState("stopped", label:"Stopped")
        }
        tileAttribute("device.status", key: "MEDIA_STATUS") {
            attributeState("paused", label:"Paused", action:"music Player.play", nextState: "playing")
            attributeState("playing", label:"Playing", action:"music Player.pause", nextState: "paused")
            attributeState("stopped", label:"Stopped", action:"music Player.play", nextState: "playing")
        }
        tileAttribute("device.status", key: "PREVIOUS_TRACK") {
            attributeState("status", action:"music Player.previousTrack", defaultState: true)
        }
        tileAttribute("device.status", key: "NEXT_TRACK") {
            attributeState("status", action:"music Player.nextTrack", defaultState: true)
        }
        tileAttribute ("device.level", key: "SLIDER_CONTROL") {
            attributeState("level", action:"music Player.setLevel")
        }
        tileAttribute ("device.mute", key: "MEDIA_MUTED") {
            attributeState("unmuted", action:"music Player.mute", nextState: "muted")
            attributeState("muted", action:"music Player.unmute", nextState: "unmuted")
        }
        tileAttribute("device.trackDescription", key: "MARQUEE") {
            attributeState("trackDescription", label:"${currentValue}", defaultState: true)
        }
    }

	valueTile("1", "device.station1", decoration: "flat", width:2, height:1) {
        state "station1", label:'${currentValue}', action:"preset1"
    }
    valueTile("2", "device.station2", decoration: "flat", width:2, height:1) {
        state "station2", label:'${currentValue}', action:"preset2"
    }
    valueTile("3", "device.station3", decoration: "flat", width:2, height:1) {
        state "station3", label:'${currentValue}', action:"preset3"
    }
    valueTile("4", "device.station4", decoration: "flat", width:2, height:1) {
        state "station4", label:'${currentValue}', action:"preset4"
    }
    valueTile("5", "device.station5", decoration: "flat", width:2, height:1) {
        state "station5", label:'${currentValue}', action:"preset5"
    }
    valueTile("6", "device.station6", decoration: "flat", width:2, height:1) {
        state "station6", label:'${currentValue}', action:"preset6"
    }
    valueTile("aux", "device.switch", decoration: "flat", width:2, height:1) {
        state "default", label:'Auxiliary', action:"aux"
    }
    valueTile("everywhere", "device.everywhere", width:2, height:1, decoration:"flat") {
        state "join", label:"Join\nEverywhere", action:"everywhereJoin"
        state "leave", label:"Leave\nEverywhere", action:"everywhereLeave"
        // Final state is used if the device is in a state where joining is not possible
        state "unavailable", label:"Not Available"
    }
    standardTile("switch", "device.switch", decoration: "flat", width:2, height:1) {
        state "on", label: '${name}', backgroundColor: "#00a0dc", icon: "st.Electronics.electronics16"
        state "off",  label: '${name}', backgroundColor: "#ffffff", icon: "st.Electronics.electronics16"
    }
    standardTile("refresh", "device.refresh", decoration: "flat", width:2, height:1) {
        state "default", label:'', action:"refresh", icon:"st.secondary.refresh"
    }
    
    standardTile("status", "device.status", width: 1, height: 1, canChangeIcon: true) {
        state "playing", label: '${name}', backgroundColor: "#00a0dc", icon: "st.Electronics.electronics16"
        state "paused",  label: '${name}', backgroundColor: "#ffffff", icon: "st.Electronics.electronics16"
        state "stopped", label: '${name}', backgroundColor: "#ffffff", icon: "st.Electronics.electronics16"
    }

    main "status"
    details (["mediaPlayer", "1", "2", "3", "4", "5", "6", "aux", "switch", "refresh"])
}

preferences {
    input(name:"deviceIp", type:"text", title: "Device IP Address", description: "Local lan IPv4 Address", defaultValue: "127.0.0.1", required: true, displayDuringSetup: true)
    input(name:"devicePollRateSecs", type: "number", title: "Device Poll Rate (30-600 seconds)", description: "Default is 300 seconds", range: "30..600", defaultValue: "300", displayDuringSetup: false)
    input(name:"deviceLogEnable", type: "bool", title: "Enable debug logging", defaultValue: false) 
    input(name:"deviceTraceEnable", type: "bool", title: "Enable trace logging", defaultValue: false)
}

def installed() {
    settings.deviceIp = "127.0.0.1"
    settings.devicePollRateSecs = 300
    settings.deviceLogEnable = false
    settings.deviceTraceEnable = false
    sendEvent(name: "level", value: "0", displayed: false)
    sendEvent(name: "switch", value:"off", displayed: false)
    logDebug "Executing 'installed()' with settings: ${settings}"
    initialize()
}

def updated() {
    logDebug "Executing 'updated()' with new preferences: ${settings}"
    logInfo "${device.displayName} preferences saved"
    initialize()
}

def initialize() {
    logDebug "Executing 'initialize()'"
    unschedule()
    if (device?.hub?.hardwareID ) {
        sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "lan", scheme: "untracked", hubHardwareId: device.hub.hardwareID]), displayed: false, isStateChange: true)
        sendEvent(name: "checkInterval", value: 1920, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
        sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
    } else {
        logInfo "This device is not assigned to a SmartThings Hub"
    }
    runIn(5, refresh)
}

/**************************************************************************
* The following section simply maps the actions as defined in
* the metadata into onAction() calls.
*
* This is preferred since some actions can be dealt with more
* efficiently this way. Also keeps all user interaction code in
* one place.
*
*/
def off() {
    if (device.currentState("switch")?.value == "on") {
        onAction("off")
    }
}
def forceOff() {
    onAction("off")
}
def on() {
    if (device.currentState("switch")?.value == "off") {
        onAction("on")
    }
}
def forceOn() {
    onAction("on")
}

def volumeUp() { onAction("volumeUp") }
def volumeDown() { onAction("volumeDown") }
def preset1() { onAction("1") }
def preset2() { onAction("2") }
def preset3() { onAction("3") }
def preset4() { onAction("4") }
def preset5() { onAction("5") }
def preset6() { onAction("6") }
def aux() { onAction("aux") }
def refresh() { onAction("refresh") }
def setVolume(level) { setLevel(level) }
def setLevel(level,duration=0) { onAction("volume", level) }
def play() { onAction("play") }
def stop() { pause() }
def pause() { onAction("pause") }
def mute() { onAction("mute") }
def unmute() { onAction("unmute") }
def previousTrack() { onAction("previous") }
def nextTrack() { onAction("next") }
def playTrack(uri) { onAction("playTrack", uri) } // I have no idea if this will work. It doesn't on a Soundtouch 300.
def setTrack(value) { unsupported("setTrack") }
def resumeTrack(value) { unsupported("resumeTrack") }
def restoreTrack(value) { unsupported("restoreTrack") }
def playText(value) { unsupported("playText") }
def unsupported(func) { logInfo "${device.displayName} does not support ${func}" }
//def everywhereJoin() { onAction("ejoin") }
//def everywhereLeave() { onAction("eleave") }
/**************************************************************************/

/**
* Main point of interaction with things.
* This function is called by SmartThings Cloud with the resulting data from
* any action (see HubAction()).
*
* Conversely, to execute any actions, you need to return them as a single
* item or a list (flattened).
*
* @param data Data provided by the cloud
* @return an action or a list() of actions. Can also return null if no further
*         action is desired at this point.
*/
def parse(String event) {
    def data = parseLanMessage(event)
    def actions = []
    //logDebug "parse() header:${data.header}"
    //logDebug "parse() body:${data.body}"

    // List of permanent root node handlers
    def handlers = [
        "nowPlaying" : "boseParseNowPlaying",
        "volume" : "boseParseVolume",
        "presets" : "boseParsePresets",
        "zone" : "boseParseEverywhere",
        "info" : "boseParseInfo"
    ]

	if (!data.header) return null
    // Move any pending callbacks into ready state
    prepareCallbacks()

    def xml = new XmlSlurper().parseText(data.body)
    logTrace "parse() xml:${xml.text()}"    
    if (xml.text()=="unsupported device") {
        logInfo "Not supported by this device"
        return null
    }    

    // Let each parser take a stab at it
    handlers.each { node,func ->
        if (xml.name() == node)
        actions << "$func"(xml)
    }
    // If we have callbacks waiting for this...
    actions << processCallbacks(xml)

    // Be nice and helpful
    if (actions.size() == 0) {
        logWarn "parse(): Unhandled data = " + data
        return null
    }

    // Issue new actions
    return actions.flatten()
}

/**
* Called by health check if no events been generated in the last 12 minutes
* If device doesn't respond it will be marked offline (not available)
*/
def ping() {
    logTrace("ping()")    
    onAction("ping")
}

/**
* Responsible for dealing with user input and taking the
* appropiate action.
*
* @param user The user interaction
* @param data Additional data (optional)
* @return action(s) to take (or null if none)
*/
def onAction(String user, data=null) {
    logDebug "onAction(${user})"

    // get my current IP address. Will update id on ST. 
    getDeviceIP()

    // Process action
    def actions = null
    switch (user) {
        case "on":
            boseSetPowerState(true)
            break
        case "off":
            boseSetNowPlaying(null, "STANDBY")
            boseSetPowerState(false)
            break
        case "volume":
            actions = boseSetVolume(data)
            break
        case "volumeUp":
            def level = (device.currentValue("level").toInteger()+1)
            actions = boseSetVolume(level)
            break
        case "volumeDown":
            def level = (device.currentValue("level").toInteger()-1)
            actions = boseSetVolume(level)
        break         
            case "aux":
            boseSetNowPlaying(null, "AUX")
            //boseZoneReset()
            //sendEvent(name:"everywhere", value:"unavailable")
        case "1":
        case "2":
        case "3":
        case "4":
        case "5":
        case "6":
            actions = boseSetInput(user)
            break
        case "refresh":
            boseSetNowPlaying(null, "REFRESH")
            actions = [boseRefreshNowPlaying(), boseGetPresets(), boseGetVolume(), boseGetEverywhereState(), boseGetInfo()]
            break
        case "ping":
            if(device.currentState("switch")?.value == "on")
                actions = [boseGetVolume(), boseRefreshNowPlaying()]
            else
                actions = boseRefreshNowPlaying()
            break
        case "play":
            actions = [boseSetPlayMode(true), boseRefreshNowPlaying()]
            break
        case "pause":
            actions = [boseSetPlayMode(false), boseRefreshNowPlaying()]
            break
        case "previous":
            actions = [boseChangeTrack(-1), boseRefreshNowPlaying()]
            break
        case "next":
            actions = [boseChangeTrack(1), boseRefreshNowPlaying()]
            break
        case "mute":
            actions = boseSetMute(true)
            break
        case "unmute":
            actions = boseSetMute(false)
            break
        case "ejoin":
            actions = boseZoneJoin()
            break
        case "eleave":
            actions = boseZoneLeave()
            break
        case "playTrack":
            actions = bosePlayTrack(data)
            break
        default:
            log.error "Unhandled action: " + user
    }

    if(user=="ping" || user=="refresh") 
        runIn(settings.devicePollRateSecs, ping)
    else
        runIn(5, ping)        

    // Make sure we don't have nested lists
    if (actions instanceof List)
        return actions.flatten()
    return actions
}

/**
* Joins this speaker into the everywhere zone
*/
def boseZoneJoin() {
    logTrace "boseZoneJoin()"

    def results = []
    def posts = parent.boseZoneJoin(this)

    for (post in posts) {
        if (post['endpoint'])
        results << bosePOST(post['endpoint'], post['body'], post['host'])
    }
    //sendEvent(name:"everywhere", value:"leave")
    results << boseRefreshNowPlaying()

    return results
}

/**
* Removes this speaker from the everywhere zone
*/
def boseZoneLeave() {
    logTrace "boseZoneLeave()"

    def results = []
    def posts = parent.boseZoneLeave(this)

    for (post in posts) {
        if (post['endpoint'])
        results << bosePOST(post['endpoint'], post['body'], post['host'])
    }
    //sendEvent(name:"everywhere", value:"join")
    results << boseRefreshNowPlaying()

    return results
}

/**
* Removes this speaker and any children WITHOUT
* signaling the speakers themselves. This is needed
* in certain cases where we know the user action will
* cause the zone to collapse (for example, AUX)
*/
def boseZoneReset() {
    logTrace "boseZoneReset()"

    parent.boseZoneReset()
}

/**
* Handles <nowPlaying></nowPlaying> information and can also
* perform addtional actions if there is a pending command
* stored in the state variable. For example, the power is
* handled this way.
*
* @param xmlData Data to parse
* @return command
*/
def boseParseNowPlaying(xmlData) {
    logTrace "boseParseNowPlaying(${xmlData})"

    def result = []

    // Perform display update, allow it to add additional commands
    if (boseSetNowPlaying(xmlData)) {
        result << boseRefreshNowPlaying()
    }

    return result
}

/**
* Parses volume data
*
* @param xmlData Data to parse
* @return command
*/
def boseParseVolume(xmlData) {
    logTrace "boseParseVolume(${xmlData})"

    def result = []

    def level = xmlData.actualvolume.text()
    def mute = (xmlData.muteenabled.text().toBoolean()) ? "muted" : "unmuted"

    if( device.currentValue("level").toString() != level ) {
        sendEvent(name:"level", value: level)
        logInfo "${device.displayName} level is ${level}"
    }
    if( device.currentValue("mute") != mute ) {
        sendEvent(name:"mute", value: mute)
        logInfo "${device.displayName} is ${mute}"
    }

    return result
}

/**
* Parses the result of the boseGetInfo() call
*
* @param xmlData
*/
def boseParseInfo(xmlData) {
    logTrace "boseParseInfo(${xmlData})"
    
    def model = xmlData.type.text()    
    def name = xmlData.name.text()
    
    sendEvent(name:"manufacturer", value: "Bose")
    if(model) sendEvent(name:"model", value: model)
    if(name) sendEvent(name:"name", value: name)
}

/**
* Parses the result of the boseGetEverywhereState() call
*
* @param xmlData
*/
def boseParseEverywhere(xmlData) {
    logTrace "boseParseEverywhere(${xmlData})"
    // No good way of detecting the correct state right now
}

/**
* Parses presets and updates the buttons
*
* @param xmlData Data to parse
* @return command
*/
def boseParsePresets(xmlData) {
    logTrace "boseParsePresets(${xmlData})"
    def result = []

    state.preset = [:]

    def missing = ["1", "2", "3", "4", "5", "6"]
    for (preset in xmlData.preset) {
        def id = preset.attributes()['id']
        def mediaSource = preset.ContentItem[0].attributes()['source']
        def name = preset.ContentItem.itemName[0].text()
        def imageUrl = preset.ContentItem.containerArt[0].text()
        
        def item = [:]
        item['id'] = id
        item['mediaSource'] = mediaSource
        item['name'] = name
        item['imageUrl'] = imageUrl        

        sendEvent(name:"station${id}", value: name)
        sendEvent(name:"preset${id}", value: item)
        missing = missing.findAll { it -> it != id }
        // Store the presets into the state for recall later
        state.preset["$id"] = XmlUtil.serialize(preset.ContentItem)
    }

    for (id in missing) {
        state.preset["$id"] = null
        sendEvent(name:"station${id}", value:"")
        sendEvent(name:"preset${id}", value:"")
    }

    return result
}

/**
* Based on <nowPlaying></nowPlaying>, updates the visual
* representation of the speaker
*
* @param xmlData The nowPlaying info
* @param override Provide the source type manually (optional)
*
* @return true if it would prefer a refresh soon
*/
def boseSetNowPlaying(xmlData, override=null) {
    logTrace "boseSetNowPlaying(xmlData: ${xmlData})"

    def needrefresh = false

    if (xmlData && xmlData.playStatus) {
        switch(xmlData.playStatus) {
            case "BUFFERING_STATE":
                sendEvent(name:"status", value:"buffering")
                needrefresh = true
                break
            case "PLAY_STATE":
                sendEvent(name:"status", value:"playing")
                break
            case "PAUSE_STATE":
                sendEvent(name:"status", value:"paused")
                break
            case "STOP_STATE":
                sendEvent(name:"status", value:"stopped")
                break
        }
    }

    // Some last parsing which only deals with actual data from device
    if (xmlData) {
        if (xmlData.attributes()['source'] == "STANDBY") {
            if(device.currentState("switch")?.value == "on") {
                sendEvent(name:"switch", value:"off")
                sendEvent(name:"status", value:"stopped")
                logInfo "${device.displayName} is off"
            }
        } else {
            if(device.currentState("switch")?.value != "on") {
                sendEvent(name:"switch", value:"on")
                logInfo "${device.displayName} is on"
            }
        }
        boseSetPlayerAttributes(xmlData)
    }

    // Do not allow a standby device or AUX to be master
    /*if (!parent.boseZoneHasMaster() && (override ? override : xmlData.attributes()['source']) == "STANDBY")
		sendEvent(name:"everywhere", value:"unavailable")
	else if ((override ? override : xmlData.attributes()['source']) == "AUX")
		sendEvent(name:"everywhere", value:"unavailable")
	else if (boseGetZone()) {
		logInfo "We're in the zone: " + boseGetZone()
		sendEvent(name:"everywhere", value:"leave")
	} else
		sendEvent(name:"everywhere", value:"join")
	*/

    return needrefresh
}

/**
* Updates the attributes exposed by the music Player capability
*
* @param xmlData The NowPlaying XML data
*/
def boseSetPlayerAttributes(xmlData) {
    logTrace "boseSetPlayerAttributes(xmlData: ${xmlData})"

    // Refresh attributes
    def trackDesc = "Standby"
    def trackData = [:]

    trackData["source"] = xmlData.attributes()['source']
    trackData["sourceAccount"] = xmlData.attributes()['sourceAccount']

    switch (xmlData.attributes()['source']) {
        case "PRODUCT":
            trackData["station"] = trackDesc = trackData.sourceAccount
            break
        case "STANDBY":
            trackData["station"] = trackDesc = "Standby"
            break
        case "AUX":
            trackData["station"] = trackDesc = "Auxiliary Input"
            break
        case "AIRPLAY":
            trackData["station"] = trackDesc = "AirPlay"
            if (!xmlData.attributes()['sourceAccount'].contains("AirPlay2"))
                break
        case "SPOTIFY":
        case "DEEZER":
        case "PANDORA":
        case "IHEART":
        case "STORED_MUSIC": // Tested on Synology NAS
        case "AMAZON":
        case "SIRIUSXM_EVEREST":
            trackData["artist"]  = xmlData.artist ? "${xmlData.artist.text()}" : ""
            trackData["track"]   = xmlData.track  ? "${xmlData.track.text()}"  : ""
            trackData["station"] = xmlData.stationName ? "${xmlData.stationName.text()}" : xmlData.attributes()['source']
            trackData["album"]   = xmlData.album ? "${xmlData.album.text()}" : ""
            trackDesc = trackData.artist + ": " + trackData.track 
            break
        case "INTERNET_RADIO":
            trackData["station"] = xmlData.stationName ? "${xmlData.stationName.text()}" : xmlData.attributes()['source']
            trackData["description"]  = xmlData.description ? "${xmlData.description.text()}" : ""
            trackDesc = trackData.station + ": " + trackData.description
            break
        default:
            trackDesc = trackData.source
    }

    logDebug "trackData: ${trackData}"
    sendEvent(name:"trackDescription", value: trackDesc, descriptionText: trackData)
}

/**
* Queries the state of the "play everywhere" mode
*
* @return command
*/
def boseGetEverywhereState() {
    logTrace "boseGetEverywhereState()"

    return boseGET("/getZone")
}

/**
* Generates a remote key event
*
* @param key The name of the key
*
* @return command
*
* @note It's VITAL that it's done as two requests, or it will ignore the
*       the second key info.
*/
def boseKeypress(key) {
    logTrace "boseKeypress(key: ${key})"

    def press = "<key state=\"press\" sender=\"Gabbo\">${key}</key>"
    def release = "<key state=\"release\" sender=\"Gabbo\">${key}</key>"

    return [bosePOST("/key", press), bosePOST("/key", release)]
}

/**
* Pauses or plays current preset
*
* @param play If true, plays, else it pauses (depending on preset, may stop)
*
* @return command
*/
def boseSetPlayMode(boolean play) {
    logTrace "boseSetPlayMode(play: ${play})"

    return boseKeypress(play ? "PLAY" : "PAUSE")
}

/**
* Sets the volume in a deterministic way.
*
* @param New volume level, ranging from 0 to 100
*
* @return command
*/
def boseSetVolume(level) {
    logTrace "boseSetVolume(level: ${level})"

    def result = []
    int vol = Math.min(100, Math.max((int)level, 0))

    return [bosePOST("/volume", "<volume>${vol}</volume>"), boseGetVolume()]
}

/**
* Sets the mute state, unfortunately, for now, we need to query current
* state before taking action (no discrete mute/unmute)
*
* @param mute If true, mutes the system
* @return command
*/
def boseSetMute(boolean mute) {
    logTrace "boseSetMute(mute: ${mute})"

    queueCallback('volume', 'cb_boseSetMute', mute ? 'MUTE' : 'UNMUTE')
    return boseGetVolume()
}

/**
* Callback for boseSetMute(), checks current state and changes it
* if it doesn't match the requested state.
*
* @param xml The volume XML data
* @param mute The new state of mute
*
* @return command
*/
def cb_boseSetMute(xml, muted) {
    logTrace "cb_boseSetMute(xml: ${xml.muteenabled.text()}, muted: ${muted})"

    def result = []
    if ((xml.muteenabled.text() == 'false' && muted == 'MUTE') ||
        (xml.muteenabled.text() == 'true' && muted == 'UNMUTE'))
    {
        result << boseKeypress("MUTE")

        def mute = (muted == 'MUTE') ? "muted" : "unmuted"
        if( device.currentValue("mute") != mute ) {
            sendEvent(name:"mute", value: mute)
            logInfo "${device.displayName} is ${mute}"
        }
    }
    return result
}

/**
* Refreshes the state of the volume
*
* @return command
*/
def boseGetVolume() {
    logTrace "boseGetVolume()"

    return boseGET("/volume")
}

/**
* Changes the track to either the previous or next
*
* @param direction > 0 = next track, < 0 = previous track, 0 = no action
* @return command
*/
def boseChangeTrack(int direction) {
    logTrace "boseChangeTrack(direction: ${direction})"

    if (direction < 0) {
        return boseKeypress("PREV_TRACK")
    } else if (direction > 0) {
        return boseKeypress("NEXT_TRACK")
    }
    return []
}

/**
* Sets the input to preset 1-6 or AUX
*
* @param input The input (one of 1,2,3,4,5,6,aux)
*
* @return command
*
* @note If no presets have been loaded, it will first refresh the presets.
*/
def boseSetInput(input) {
    logTrace "boseSetInput(input: ${input})"

    def result = []

    if (!state.preset) {
        result << boseGetPresets()
        queueCallback('presets', 'cb_boseSetInput', input)
    } else {
        result << cb_boseSetInput(null, input)
    }
    return result
}

/**
* Callback used by boseSetInput(), either called directly by
* boseSetInput() if we already have presets, or called after
* retreiving the presets for the first time.
*
* @param xml The presets XML data
* @param input Desired input
*
* @return command
*
* @note Uses KEY commands for AUX, otherwise /select endpoint.
*       Reason for this is latency. Since keypresses are done
*       in pairs (press + release), you could accidentally change
*       the preset if there is a long delay between the two.
*/
def cb_boseSetInput(xml, input) {
    logTrace "cb_boseSetInput(${xml},${input})"

    def result = []

    if (input >= "1" && input <= "6" && state.preset["$input"])
    result << bosePOST("/select", state.preset["$input"])
    else if (input.toLowerCase() == "aux") {
        result << boseKeypress("AUX_INPUT")
    }

    // Horrible workaround... but we need to delay
    // the update by at least a few seconds...
    result << boseRefreshNowPlaying(3000)
    return result
}

/**
* Sets the power state of the bose unit
*
* @param device The device in-question
* @param enable True to power on, false to power off
*
* @return command
*
* @note Will first query state before acting since there
*       is no discreete call.
*/
def boseSetPowerState(boolean enable) {
    logTrace "boseSetPowerState(enable: ${enable})"

    bosePOST("/key", "<key state=\"press\" sender=\"Gabbo\">POWER</key>")
    bosePOST("/key", "<key state=\"release\" sender=\"Gabbo\">POWER</key>")
    boseGET("/now_playing")
    if (enable) {
        queueCallback('nowPlaying', "cb_boseConfirmPowerOn", 5)
    }
}

/**
* Callback function used by boseSetPowerState(), is used
* to handle the fact that we only have a toggle for power.
*
* @param xml The XML data from nowPlaying
* @param state The requested state
*
* @return command
*/
def cb_boseSetPowerState(xml, state) {
    logTrace "cb_boseSetPowerState(${xml},${state})"

    def result = []
    if ( (xml.attributes()['source'] == "STANDBY" && state == "POWERON") ||
        (xml.attributes()['source'] != "STANDBY" && state == "POWEROFF") )
    {
        result << boseKeypress("POWER")
        if (state == "POWERON") {
            result << boseRefreshNowPlaying()
            queueCallback('nowPlaying', "cb_boseConfirmPowerOn", 5)
        }
    }
    return result.flatten()
}

/**
* We're sometimes too quick on the draw and get a refreshed nowPlaying
* which shows standby (essentially, the device has yet to completely
* transition to awake state), so we need to poll a few times extra
* to make sure we get it right.
*
* @param xml The XML data from nowPlaying
* @param tries A counter which will decrease, once it reaches zero,
*              we give up and assume that whatever we got was correct.
* @return command
*/
def cb_boseConfirmPowerOn(xml, tries) {
    logTrace "cb_boseConfirmPowerOn(${xml},${tries})"

    def result = []
    def attempt = tries as Integer
    log.warn "boseConfirmPowerOn() attempt #$attempt"
    if (xml.attributes()['source'] == "STANDBY" && attempt > 0) {
        result << boseRefreshNowPlaying()
        queueCallback('nowPlaying', "cb_boseConfirmPowerOn", attempt-1)
    }
    return result
}

/**
* Requests an update on currently playing item(s)
*
* @param delay If set to non-zero, delays x ms before issuing
*
* @return command
*/
def boseRefreshNowPlaying(delay=500) {
    logDebug "boseRefreshNowPlaying(delay: ${delay})"

    if (delay > 0) {
        return ["delay ${delay}", boseGET("/now_playing")]
    }
    return boseGET("/now_playing")
}

def bosePlayTrack(url, vol="40", service="") {    
    logDebug "bosePlayTrack(url: ${url})"
    if (service.isEmpty()) service = "${device.displayName}"    
    // really don't know where the key is from or if it works.
    def data = "<?xml version=\"1.0\" encoding=\"UTF-8\"><play_info><app_key>Ml7YGAI9JWjFhU7D348e86JPXtisddBa</app_key><url>"+url+"</url><service>"+service+"</service><volume>"+vol+"</volume></play_info>" 

    bosePOST("/speaker", data)
}

/**
* Requests the list of presets
*
* @return command
*/
def boseGetPresets() {
    logTrace "boseGetPresets()"

    return boseGET("/presets")
}

/**
* Requests the info page
*
* @return command
*/
def boseGetInfo() {
    logTrace "boseGetInfo()"

    return boseGET("/info")
}

/**
* Utility function, makes GET requests to BOSE device
*
* @param path What endpoint
*
* @return command
*/
def boseGET(String path) {
    logTrace "Executing 'boseGET(${path})'"

    def hubAction 	
    try {
        def param = [
            method: "GET",
            path: path,
            headers: [HOST: getDeviceIP() + ":" + getDevicePort(), 'Content-Type':'text/xml; charset="utf-8"']]
        logTrace "boseGET param: ${param}"
        hubAction = (isST()) ? physicalgraph.device.HubAction.newInstance(param) : hubitat.device.HubAction.newInstance(param)
    }
    catch (Exception e) {
        logError "boseGET() $e on $hubAction"
    }
    if (hubAction) {
        try {
            sendHubCommand( hubAction )
        }
        catch (Exception e) {
            logError "boseGET() $e on $sendHubCommand"
        }
    }    
}

/**
* Utility function, makes a POST request to the BOSE device with
* the provided data.
*
* @param path What endpoint
* @param data What data
* @param address Specific ip and port (optional)
*
* @return command
*/
def bosePOST(String path, String data, String address=null) {
    logTrace "Executing 'bosePOST(${path})' data: ${data}"

    def hubAction 	
    try {
        def param = [
            method: "POST",
            path: path,
            body: data,
            headers: [HOST: address ?: (getDeviceIP() + ":" + getDevicePort()), 'Content-Type':'text/xml; charset="utf-8"']]
        logTrace "bosePOST param: ${param}"
        hubAction = (isST()) ? physicalgraph.device.HubAction.newInstance(param) : hubitat.device.HubAction.newInstance(param)
    }
    catch (Exception e) {
        logError "boseGET() $e on $hubAction"
    }
    if (hubAction) {
        try {
            sendHubCommand( hubAction )
        }
        catch (Exception e) {
            logError "boseGET() $e on $sendHubCommand"
        }
    }
}

/**
* Queues a callback function for when a specific XML root is received
* Will execute on subsequent parse() call(s), never on the current
* parse() call.
*
* @param root The root node that this callback should react to
* @param func Name of the function
* @param param Parameters for function (optional)
*/
def queueCallback(String root, String func, param=null) {
    if (!state.pending)
        state.pending = [:]
    if (!state.pending[root])
        state.pending[root] = []
    state.pending[root] << ["$func":"$param"]
}

/**
* Transfers the pending callbacks into readiness state
* so they can be executed by processCallbacks()
*
* This is needed to avoid reacting to queueCallbacks() within
* the same loop.
*/
def prepareCallbacks() {
    if (!state.pending)
        return
    if (!state.ready)
        state.ready = [:]
    state.ready << state.pending
    state.pending = [:]
}

/**
* Executes any ready callback for a specific root node
* with associated parameter and then clears that entry.
*
* If a callback returns data, it's added to a list of
* commands which is returned to the caller of this function
*
* Once a callback has been used, it's removed from the list
* of queued callbacks (ie, it executes only once!)
*
* @param xml The XML data to be examined and delegated
* @return list of commands
*/
def processCallbacks(xml) {
    logTrace "processCallbacks(xml: ${xml})"

    def result = []

    if (!state.ready)
    return result

    if (state.ready[xml.name()]) {
        state.ready[xml.name()].each { callback ->
            callback.each { func, param ->
                if (func != "func") {
                    logTrace "**** processCallbacks: ${func}"
                    if (param)
                        result << "$func"(xml, param)
                    else
                        result << "$func"(xml)
                }
            }
        }
        state.ready.remove(xml.name())
    }
    return result.flatten()
}

/**
* State managament for the Play Everywhere zone.
* This is typically called from the parent.
*
* A device is either:
*
* null = Not participating
* server = running the show
* client = under the control of the server
*
* @param newstate (see above for types)
*/
def boseSetZone(String newstate) {
    logTrace "boseSetZone(newstate: ${newstate})"

    state.zone = newstate

    // Refresh our state
    if (newstate) {
        sendEvent(name:"everywhere", value:"leave")
    } else {
        sendEvent(name:"everywhere", value:"join")
    }
}

/**
* Used by the Everywhere zone, returns the current state
* of zone membership (null, server, client)
* This is typically called from the parent.
*
* @return state
*/
def boseGetZone() {
    return state.zone
}

/**
* Sets the DeviceID of this particular device.
*
* Needs to be done this way since DNI is not always
* the same as DeviceID which is used internally by
* BOSE.
*
* @param devID The DeviceID
*/
def boseSetDeviceID(String devID) {
    state.deviceID = devID
}

/**
* Retrieves the DeviceID for this device
*
* @return deviceID
*/
def boseGetDeviceID() {
    return state.deviceID
}

/**
* Returns the IP of this device
*
* @return IP address
*/
def getDeviceIP() {
	if (isST()) setupNetworkID()
    return settings.deviceIp
    //return parent.resolveDNI2Address(device.deviceNetworkId)
}

def getDevicePort() { 
    return "8090"
}

def setupNetworkID() {
    def hosthex = convertIPtoHex(settings.deviceIp).toUpperCase()
    def porthex = convertPortToHex(getDevicePort()).toUpperCase()
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