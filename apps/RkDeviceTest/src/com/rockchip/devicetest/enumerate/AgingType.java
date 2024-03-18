package com.rockchip.devicetest.enumerate;

public enum AgingType {

	CPU("cpu"),
	GPU("gpu"),
	VPU("vpu"),
	MEM("mem"),
        USBHOST("usbhost"),
	ETHERNET("ethernet"),
	THERMAL("thermal"),
	BUSMON("busmon"),
	FAILLISTS("faillists"),
	TESTPROCESS("testprocess");
        
	private String type;
	
	private AgingType(String type){
		this.type = type;
	}
	
	public static AgingType getType(String typ){
		if(typ==null) return null;
		for(AgingType at : AgingType.values()){
			if(typ.equals(at.type)){
				return at;
			}
		}
		return null;
	}

	public String getType() {
		return type;
	}
	
	
}
