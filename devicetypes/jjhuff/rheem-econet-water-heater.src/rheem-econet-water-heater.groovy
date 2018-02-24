/**
 *  Rheem Econet Water Heater
 *
 *  Copyright 2017 Justin Huff
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
 */
metadata {
	definition (name: "Rheem Econet Water Heater", namespace: "jjhuff", author: "Justin Huff") {
        capability "Thermostat"
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
        capability "Switch"
		capability "Thermostat Heating Setpoint"
		
		command "heatLevelUp"
		command "heatLevelDown"
        command "togglevacation"
		command "updateDeviceData", ["string"]
	}

	simulator { }

	tiles {
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, width: 2, height: 2) {
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
        standardTile("lowerTemp", "device.lowerTemp"){
                state("temperature", label:'${currentValue}°')
        }
        standardTile("ambientTemp","device.ambientTemp"){
                state("temperature", label:'${currentValue}°')
        }
        standardTile("upperTemp", "device.upperTemp"){
                state("temperature", label:'${currentValue}°')
        }
		standardTile("heatLevelUp", "device.switch", canChangeIcon: false, decoration: "flat" ) {
			state("heatLevelUp",   action:"heatLevelUp",   icon:"st.thermostat.thermostat-up", backgroundColor:"#F7C4BA")
		}  
		standardTile("heatLevelDown", "device.switch", canChangeIcon: false, decoration: "flat") {
			state("heatLevelDown", action:"heatLevelDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#F7C4BA")
		}
        standardTile("togglevacation", "device.switch", canChangeIcon: false, decoration: "flat") {
			state("togglevacation", action:"togglevacation", label:"togglevacation", backgroundColor:"#F7C4BA")
		}
		standardTile("operatingMode", "device.operatingMode", canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state("Energy Saver",        action:"switchMode",    nextState: "Energy Saver",        label: 'Enrgy Save')
			state("Heat Pump Only",      action:"switchMode",    nextState: "Heat Pump Only",      label: 'Heat Pump')
			state("High Demand",         action:"switchMode",    nextState: "High Demand",         label: 'High Dem')
			state("Off",                 action:"switchMode",    nextState: "Off",                 label: 'Off')
			state("Electric-Only",       action:"switchMode",    nextState: "Electric-Only",       label: 'Electic')
		}

		standardTile("vacation", "device.vacation", canChangeIcon: false, decoration: "flat" ) {
       		state "Home", label: 'Home', backgroundColor: "#0063d6"
       		state("Away", label: 'Away', backgroundColor: "#66a8f4")
		}

		standardTile("switch", "device.switch", canChangeIcon: false, decoration: "flat" ) {
       		state "on", label: 'On', action: "switch.off",
          		icon: "st.switches.switch.on", backgroundColor: "#79b821"
       		state("off", label: 'Off', action: "switch.on",
          		icon: "st.switches.switch.off", backgroundColor: "#ffffff")
		}
        
		standardTile("refresh", "device.switch", decoration: "flat") {
			state("default", action:"refresh.refresh",        icon:"st.secondary.refresh")
		}
        
		main "heatingSetpoint"
		details(["heatingSetpoint", "heatLevelUp", "heatLevelDown", "switch", "operatingMode", "vacation", "refresh","togglevacation","lowerTemp","ambientTemp","upperTemp"])
	}
}

def parse(String description) { }

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
	/*heatingSetPoint = (heatingSetPoint < deviceData.minTemp)? deviceData.minTemp : heatingSetPoint
	heatingSetPoint = (heatingSetPoint > deviceData.maxTemp)? deviceData.maxTemp : heatingSetPoint
    */
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

def togglevacation(){
    def currentMode = device.currentValue("vacation")
    log.debug "Current mode: $currentMode"

    if (currentMode == "Away")
    {
      parent.setDeviceOnVacation(this.device, false)
    }
    else
    {
      parent.setDeviceOnVacation(this.device, true)
    }
}

def updateDeviceData(data) {
	sendEvent(name: "heatingSetpoint", value: data.setPoint, unit: "F")
    sendEvent(name: "switch", value: data.inUse ? "on" : "off")
    sendEvent(name: "operatingMode", value: data.mode)
    sendEvent(name: "vacation", value: data.isOnVacation? "Away":"Home")
    sendEvent(name: "lowerTemp", value: data.lowerTemp as Integer)
    sendEvent(name: "ambientTemp", value: data.ambientTemp as Integer)
    sendEvent(name: "upperTemp", value: data.upperTemp as Integer)
}
