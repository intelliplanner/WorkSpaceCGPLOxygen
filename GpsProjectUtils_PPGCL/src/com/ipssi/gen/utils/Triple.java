/**
 * 
 */
package com.ipssi.gen.utils;

import java.io.Serializable;

/**
 * @author samarjit
 *
 */
public class Triple<T1, T2, T3> implements Serializable{ //not used
	public T1 first;
	public T2 second;
	public T3 third;
	public Triple (T1 first, T2 second, T3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
	public boolean equals(Object obj) {
		if (! (obj instanceof Triple))
			return false;
		Triple<T1, T2, T3> rhs = (Triple<T1, T2, T3>)obj;
		return ((first != null && first.equals(rhs.first)) || (first == null && rhs.first == null)) &&
		    ((second != null && second.equals(rhs.second)) || (second == null && rhs.second == null)) &&
		    ((third != null && third.equals(rhs.third)) || (third == null && rhs.third == null))
		    ;		
	}
	public int hashCode() {
		int f = first == null ? -1 : first.hashCode();
		int s = second == null ? -1 : second.hashCode();
		int t = third == null ? -1 : third.hashCode();
		long hCode = t*9+s*1031+f;
		if ( hCode > Integer.MAX_VALUE){
			hCode = hCode / 1000;
		}
		return (int) hCode;
	}
	
	public String toString() {
		return "("+(first == null ? "null" : first.toString())+","+(second == null ? "null" : second.toString())
		+","+(third == null ? "null" : third.toString())+")";
	}
}
