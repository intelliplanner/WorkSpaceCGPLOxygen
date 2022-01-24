package com.ipssi.gen.utils;

import java.io.Serializable;

public class Quad <T1, T2, T3, T4> implements Serializable{ //not used
	public T1 first;
	public T2 second;
	public T3 third;
	public T4 fourth;
	public Quad (T1 first, T2 second, T3 third, T4 fourth) {
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}
	public boolean equals(Object obj) {
		if (! (obj instanceof Quad))
			return false;
		Quad <T1, T2, T3, T4> rhs = (Quad <T1, T2, T3, T4>)obj;
		return ((first != null && first.equals(rhs.first)) || (first == null && rhs.first == null)) &&
		    ((second != null && second.equals(rhs.second)) || (second == null && rhs.second == null)) &&
		    ((third != null && third.equals(rhs.third)) || (third == null && rhs.third == null)) &&
		    ((fourth != null && fourth.equals(rhs.fourth)) || (fourth == null && rhs.fourth == null))
		    ;		
	}
	public int hashCode() {
		int f = first == null ? -1 : first.hashCode();
		int s = second == null ? -1 : second.hashCode();
		int t = third == null ? -1 : third.hashCode();
		int fth = fourth == null ? -1 : fourth.hashCode();
		long hCode = fth*19+t*9+s*1031+f;
		if ( hCode > Integer.MAX_VALUE){
			hCode = hCode / 1000;
		}
		return (int) hCode;
	}
	
	public String toString() {
		return "("+(first == null ? "null" : first.toString())+","+(second == null ? "null" : second.toString())
		+","+(third == null ? "null" : third.toString())+","+(fourth == null ? "null" : fourth.toString())+")";
	}
}
