/**
 * 
 */
package com.ipssi.gen.utils;

import java.io.Serializable;

/**
 * @author samarjit
 *
 */
public class Pair<T1, T2> implements Serializable{
	public T1 first;
	public T2 second;
	public Pair (T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	public boolean equals(Object obj) {
		if (! (obj instanceof Pair))
			return false;
		Pair<T1,T2> rhs = (Pair<T1,T2>)obj;
		return ((first != null && first.equals(rhs.first)) || (first == null && rhs.first == null)) &&
		    ((second != null && second.equals(rhs.second)) || (second == null && rhs.second == null))
		    ;		
	}
	public int hashCode() {
		int f = first == null ? -1 : first.hashCode();
		int s = second == null ? -1 : second.hashCode();
		return s*1031+f;
	}
	
	public String toString() {
		return "("+(first == null ? "null" : first.toString())+","+(second == null ? "null" : second.toString())+")";
	}
}
