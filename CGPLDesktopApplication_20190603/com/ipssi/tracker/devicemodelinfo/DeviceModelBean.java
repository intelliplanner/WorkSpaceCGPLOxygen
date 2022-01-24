package com.ipssi.tracker.devicemodelinfo;

public class DeviceModelBean {
	private int id;
	private String modelName;
	private boolean batteryFlag;
	private boolean ignitionFlag;
	private boolean buzzerFlag;
	private boolean voiceFlag;
	private boolean smsDisplayFlag;
	private int ioPinCount;
	private String command1;
	private String command2;
	private String command3;
	private String command4;
	private int modelProtocol;
	//add by balwant 
	
	private int deviceVersion;
	private String commandName;
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}

		
	public String getModelName(){
		return this.modelName;
	}
	
	public void setModelName(String modelName){
		this.modelName = modelName;
	}
	
	public boolean getBatteryFlag(){
		return this.batteryFlag;
	}
	
	public void setBatteryFlag(boolean batteryFlag){
		this.batteryFlag = batteryFlag;
	}
	
	public boolean getIgnitionFlag(){
		return this.ignitionFlag;
	}
	
	public void setIgnitionFlag(boolean ignitionFlag){
		this.ignitionFlag = ignitionFlag;
	}
	
	public boolean getBuzzerFlag(){
		return this.buzzerFlag;
	}
	
	public void setBuzzerFlag(boolean buzzerFlag){
		this.buzzerFlag = buzzerFlag;
	}
	
	public boolean getSmsDisplayFlag(){
		return this.smsDisplayFlag;
	}
	
	public void setSmsDisplayFlag(boolean smsDisplay){
		this.smsDisplayFlag = smsDisplay;
	}
	
	public boolean getVoiceFlag(){
		return this.voiceFlag;
	}
	
	public void setVoiceFlag(boolean voiceFlag){
		this.voiceFlag = voiceFlag;
	}
	
	public int getIoPinCount(){
		return this.ioPinCount;
	}
	
	public void setIoPinCount(int ioPinCount){
		this.ioPinCount = ioPinCount;
	}
	
	public String getCommand1(){
		return this.command1;
	}
	
	public void setCommand1(String command1){
		this.command1 = command1;
	}
	
	public String getCommand2(){
		return this.command2;
	}
	
	public void setCommand2(String command2){
		this.command2 = command2;
	}
	
	public String getCommand3(){
		return this.command3;
	}
	
	public void setCommand3(String command3){
		this.command3 = command3;
	}
	
	public String getCommand4(){
		return this.command4;
	}
	
	public void setCommand4(String command4){
		this.command4 = command4;
	}

	public void setModelProtocol(int modelProtocol) {
		this.modelProtocol = modelProtocol;
	}

	public int getModelProtocol() {
		return modelProtocol;
	}

	public int getDeviceVersion() {
		return deviceVersion;
	}

	public void setDeviceVersion(int deviceVersion) {
		this.deviceVersion = deviceVersion;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}
	
}
