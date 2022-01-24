
package com.ipssi.shift;
import java.sql.Date;
import com.ipssi.gen.utils.Misc;
public class ShiftBean {
		private String shiftName;
		private int startHour;
		private int startMin;
		private int stopMin;
		private int stopHour;
		private int shiftType;
		private int shiftNo;
		private Date validFrom;
		private Date validTo;
		private int id = Misc.getUndefInt();
		private double dur;
		public double getDur() {
			return dur;
		}
		public void setDur(double dur) {
			this.dur = dur;
		}
		public void setId(int id) {
			this.id = id;
		}
		public int getShiftNo() {
			return shiftNo;
		}
		public void setShiftNo(int shiftNo) {
			this.shiftNo = shiftNo;
		}
		public int getId() {
			return id;
		}
		public int getShiftType() {
			return shiftType;
		}
		public void setShiftType(int shiftType) {
			this.shiftType = shiftType;
		}
		public String getShiftName() {
			return shiftName;
		}
		public void setShiftName(String shiftName) {
			this.shiftName = shiftName;
		}
		public int getStartHour() {
			return startHour;
		}
		public void setStartHour(int startHour) {
			this.startHour = startHour;
		}
		public int getStartMin() {
			return startMin;
		}
		public void setStartMin(int startMin) {
			this.startMin = startMin;
		}
		public int getStopMin() {
			return stopMin;
		}
		public void setStopMin(int stopMin) {
			this.stopMin = stopMin;
		}
		public int getStopHour() {
			return stopHour;
		}
		public void setStopHour(int stopHour) {
			this.stopHour = stopHour;
		}
		public void setValidity(Date from, Date to) {
			if (to != null && from != null) {
				from.setYear(0);
				to.setYear(0);
				if (to.before(from))
					to.setYear(1);				
			}
			else {
				if (from != null)
					from.setYear(0);
				if (to != null)
					to.setYear(0);
			}
			validFrom = from;
			validTo = to;
		}
		public Date getValidFrom() {
			if (validFrom == null)
				return null;
			Date retval = new Date(validFrom.getTime());
			Date now = Misc.getCurrentTime();
			retval.setYear(retval.getYear()+now.getYear());
			return retval;
		}
		public Date getValidTo() {
			if (validTo == null)
				return null;
			Date retval = new Date(validTo.getTime());
			Date now = Misc.getCurrentTime();
			retval.setYear(retval.getYear()+now.getYear());
			return retval;
		}

}
