/**
 *  Rheem Econet Water Heater
 *
 *  Copyright 2017 Scott Mark
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
 *  Last Updated : 2017-01-04
 *
 *  Based on https://github.com/copy-ninja/SmartThings_RheemEcoNet
 *  Based on https://github.com/jjhuff/SmartThings_RheemEcoNet
 */

metadata {
	definition (name: "Rheem Econet Water Heater", namespace: "kramttocs", author: "Scott Mark") {
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
        capability "Switch"
		capability "Thermostat Heating Setpoint"
        capability "Water Sensor"
        capability "Thermostat Operating State"
        
		
		command "heatLevelUp"
		command "heatLevelDown"
		command "updateDeviceData", ["string"]
	}


	tiles { 
    
       multiAttributeTile(name:"summary", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.heatingSetPoint", key: "PRIMARY_CONTROL") {
				attributeState("heatingSetpoint", label:'${currentValue}°')
			}	
            tileAttribute("device.heatingSetPoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "heatLevelUp")
				attributeState("VALUE_DOWN", action: "heatLevelDown")
			}
			tileAttribute('device.thermostatOperatingStateDisplay', key: "OPERATING_STATE") {
				attributeState('idle', backgroundColor:"#d28de0")			// ecobee purple/magenta
                
				attributeState('heating', backgroundColor:"#ff9c14")		// ecobee flame orange
				
                attributeState('off', backGroundColor:"#cccccc")			// grey
                attributeState('default', /* label: 'idle', */ backgroundColor:"#d28de0", defaultState: true) 
			
			
			}
            tileAttribute ("device.water", key: "SECONDARY_CONTROL") {
				attributeState "dry", 
					label:'Dry', 
					icon: "st.alarm.water.dry",
					backgroundColor:"#ffffff"
				attributeState "wet", 
					label:'Wet', 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#53a7c0"				
			}	
            
			
		} // End multiAttributeTile
        
       standardTile("refresh", "device.switch", decoration: "flat") {
			state("default", action:"refresh.refresh",        icon:"st.secondary.refresh")
		}
        standardTile("heatingSetpoint", "device.heatingSetpoint", decoration: "flat") {
			state("heatingSetpoint", label:'${currentValue}°',
				backgroundColors:[
					[value: 90,  color: "#f49b88"],
					[value: 100, color: "#f28770"],
					[value: 110, color: "#f07358"],
					[value: 120, color: "#ee5f40"],
					[value: 130, color: "#ec4b28"],
					[value: 140, color: "#ea3811"]					
				]
			)
		}
		main "summary"
		details(["summary","refresh", "heatingSetPoint"])
	}
}

def parse(String description) { }

def installed() {
	parent.refresh();
}

def refresh() {
	log.debug "refresh"
	parent.refresh()
}

def on() {
   	parent.setDeviceEnabled(this.device, true)
    sendEvent(name: "switch", value: "off")
}

def off() {
   	parent.setDeviceEnabled(this.device, false)
    sendEvent(name: "switch", value: "off")
}

def setHeatingSetpoint(Number setPoint) {
	sendEvent(name: "heatingSetpoint", value: setPoint, unit: "F")
	parent.setDeviceSetPoint(this.device, setPoint)
}

def heatLevelUp() { 
	def setPoint = device.currentValue("heatingSetpoint")
    setPoint = setPoint + 1
	setHeatingSetpoint(setPoint)
}	

def heatLevelDown() { 
	def setPoint = device.currentValue("heatingSetpoint")
    setPoint = setPoint - 1
    setHeatingSetpoint(setPoint)
}

def updateDeviceData(data) {
	
    log.debug "setpoint"
    log.debug data.setPoint
    sendEvent(name: "water", value: data.hasCriticalAlert ? "wet" : "dry")
    sendEvent(name: "thermostatOperatingStateDisplay", value: data.isEnabled ? (data.inUse ? "heating" : "idle") : "off")
    sendEvent(name: "heatingSetpoint", value: data.setPoint)
}



