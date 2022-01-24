package com.ipssi.rfid.readers;
import java.util.ArrayList;
import com.ipssi.gen.utils.Misc;
public class PeripheralConnectionStatus {
	public static volatile ArrayList<Peripheral> peripheralList = new ArrayList<>();
	public static enum PeripheralType{
		DATABASE,
		RFID,
		BARRIER,
		WEIGHBRIDGE,
		CENTRIC,
		WCS
	}
	public static enum PeripheralStatus{
		CONNECTED,
		DISCONNECTED
	}
	public static Peripheral getPeripheral(int id,PeripheralType type){
		Peripheral retval = null;
		for (int i = 0,is=peripheralList == null ? 0 : peripheralList.size(); i < is; i++) {
			if(id == peripheralList.get(i).getId() && type == peripheralList.get(i).getType()){
				retval = peripheralList.get(i);
				break;
			}
		}
		if(retval == null){
			retval = new Peripheral(id, type, Misc.getUndefInt(), null);
			peripheralList.add(retval);
		}
		return retval;
	}
	public static boolean updatePeripheralStatus(int id,PeripheralType type,PeripheralStatus status,long threshold){
		Peripheral p = getPeripheral(id, type);
		if(p != null){
			if(status != p.getStatus() || ( !Misc.isUndef(threshold) && (System.currentTimeMillis() - p.getLastCheck()) > threshold)){
				p.setStatus(status);
				p.setLastCheck(System.currentTimeMillis());
				return true;
			}
		}
		return false;
	}
	public static class Peripheral{
		private int id;
		private PeripheralType type;
		private long lastCheck;
		private PeripheralStatus status;
		
		
		public Peripheral(int id, PeripheralType type, long lastCheck, PeripheralStatus status) {
			super();
			this.id = id;
			this.type = type;
			this.lastCheck = lastCheck;
			this.status = status;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public PeripheralType getType() {
			return type;
		}
		public void setType(PeripheralType type) {
			this.type = type;
		}
		public long getLastCheck() {
			return lastCheck;
		}
		public void setLastCheck(long lastCheck) {
			this.lastCheck = lastCheck;
		}
		public PeripheralStatus getStatus() {
			return status;
		}
		public void setStatus(PeripheralStatus status) {
			this.status = status;
		}
	}
}
