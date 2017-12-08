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
        
        attribute "maxSetPoint", "number"
        attribute "minSetPoint", "number" 
	}


    tiles {

		standardTile("switch", "device.switch", canChangeIcon: false, decoration: "flat" ) {
       		state "on", label: 'On', action: "switch.off",
          		icon: "st.switches.switch.on", backgroundColor: "#79b821"
       		state"off", label: 'Off', action: "switch.on",
          		icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        
		standardTile("refresh", "device.switch", decoration: "flat") {
			state("default", action:"refresh.refresh", icon:"st.secondary.refresh")
		}
        
         multiAttributeTile(name:"summary", type: "thermostat", width: 6, height: 4) {
        	tileAttribute("device.heatingSetpoint", key: "PRIMARY_CONTROL") {
				attributeState("heatingSetpoint", label:'${currentValue}°', unit:"dF", defaultState: true)
			}
			tileAttribute("changeHeatingSetPoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "heatLevelUp")
				attributeState("VALUE_DOWN", action: "heatLevelDown")
			}
			tileAttribute('thermostatOperatingStateDisplay', key: "OPERATING_STATE") {
				attributeState('idle', label: "idle", backgroundColor:"#d28de0")
				attributeState('heating', label: "heating", backgroundColor:"#ff9c14")	
                attributeState('off', label: "off", backGroundColor:"#21212")			
                attributeState('default', label: "idle", backgroundColor:"#d28de0", defaultState: true) 
			}
            tileAttribute ("device.water", key: "SECONDARY_CONTROL") {				
				attributeState "wet", 
					label:'Wet', 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#53a7c0"
                    attributeState "dry", 
					label:'Dry', 
					icon: "st.alarm.water.dry",
					backgroundColor:"#ffffff"			
			}
			
        }
        
		main "summary"
		details(["summary", "switch", "refresh"])
	}
}


def parse(String description) { }

def installed() {
	parent.refresh();
}

def refresh() {
	parent.refresh()
}

def on() {
   	parent.setDeviceEnabled(this.device, true)
    sendEvent(name: "switch", value: "off")
    parent.refresh();
}

def off() {
   	parent.setDeviceEnabled(this.device, false)
    sendEvent(name: "switch", value: "off")
    parent.refresh();
}

def setHeatingSetpoint(Number setPoint) {
	sendEvent(name: "heatingSetpoint", value: setPoint, unit: "F")
	parent.setDeviceSetPoint(this.device, setPoint)
}

def heatLevelUp() { 
	def setPoint = device.currentValue("heatingSetpoint")
    setPoint = setPoint + 1   
    if(setPoint <= device.currentValue("maxSetPoint") )
    {
        setHeatingSetpoint(setPoint)
    }
    else
    {
        sendEvent(name: "heatingSetpoint", value: setPoint-1, unit: "F", isStateChange: true, descriptionText:"Max Set Point of " + device.currentValue("maxSetPoint") + " has been reached.")
    }
}	

def heatLevelDown() { 
	def setPoint = device.currentValue("heatingSetpoint") 
    setPoint = setPoint - 1
    if(setPoint >= device.currentValue("minSetPoint") )
    {
        setHeatingSetpoint(setPoint)
    }
    else
    {
        sendEvent(name: "heatingSetpoint", value: setPoint+1, unit: "F", isStateChange: true, descriptionText:"Min Set Point of " + device.currentValue("minSetPoint") + " has been reached.")
    }
}

def updateDeviceData(data) {
	sendEvent(name: "heatingSetpoint", value: data.setPoint)    
    sendEvent(name: "water", value: data.hasCriticalAlert ? "wet" : "dry")
    sendEvent(name: "thermostatOperatingStateDisplay",  value: (data.isEnabled ? (data.inUse ? "heating" : "idle") : "off"))
    sendEvent(name: "switch", value: data.isEnabled ? "on" : "off")
    sendEvent(name: "minSetPoint", value: data.minSetPoint)    
    sendEvent(name: "maxSetPoint", value: data.maxSetPoint)
}