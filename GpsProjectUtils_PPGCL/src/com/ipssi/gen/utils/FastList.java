/**
 * 
 */
package com.ipssi.gen.utils;

import java.io.Serializable;
import java.util.*;
/**
 * @author samarjit
 *
 */
public class FastList<T extends Comparable> implements Serializable {
	private static final long serialVersionUID = 1L;
     private List<T> m_list = new ArrayList<T>();
     private int m_listEndPlus1 = 0;
     private int m_listPosOfLastSearch = -1;
     
     public void clear() {
    	 m_list.clear();
    	 m_listEndPlus1 = 0;
    	 m_listPosOfLastSearch = -1;
     }
     public int size() {
    	 return m_listEndPlus1;
     }
     public void removeFromStart(int numToRemove) {
    	 if (numToRemove <= 0)
    		 return;
    	 if (numToRemove < m_listEndPlus1) {
    		for (int i=0, is = m_listEndPlus1-numToRemove; i<is;i++)
    			m_list.set(i, m_list.get(i+numToRemove));
    		if (m_listPosOfLastSearch < numToRemove) {
    			m_listPosOfLastSearch = -1;
    		}
    		else {
    			m_listPosOfLastSearch -= numToRemove;
    		}
    		m_listEndPlus1 -= numToRemove;
    		for (int i=m_listEndPlus1+numToRemove-1;i>=m_listEndPlus1;i--) //needed because no range based bin search
    			m_list.remove(i);
    	 }
    	 else {
    		 clear();
    	 }   
     }
     
     public void resize(int newsz) {//not validated
    	 if (newsz == 0) {
    		 clear();
    	 }
    	 else {
    		 for (int i = m_listEndPlus1-1; i >= newsz; i--)  //needed because no range based bin search
     			m_list.remove(i);
    		 m_listEndPlus1 = newsz;
        	 if (m_listPosOfLastSearch >= newsz)
        		 m_listPosOfLastSearch = -1;	 
    	 }
     }
     
     public T remove(int index) {
    	 if (index < 0 || index >= m_listEndPlus1)
    		 return null;
    	 T retval = m_list.remove(index);
    	 if (m_listPosOfLastSearch >= index)
    		 m_listPosOfLastSearch--;
    	 m_listEndPlus1--;
    	 return retval;
     }
     public Pair<Integer, Boolean> indexOf(Comparable<T> compVal) {//returns the index such that time is <= t
    	 Pair<Integer, Boolean> retval = indexOf_temp(compVal);
    	 return retval;
     }
     public Pair<Integer, Boolean> indexOf_temp(Comparable<T> compVal) //returns the index such that time is <= t 
     {//replace the name with indexOf
    	   if (compVal == null) {
    		   m_listPosOfLastSearch = -1;
    		   return new Pair<Integer, Boolean> (-1, false);
    	   }
           int startPos = 0;
           int endPosPlus1 = m_listEndPlus1;
      
           if (m_listPosOfLastSearch >= 0)
           {//check if the time of that is <= t and the one at the next is >=
                 Comparable curr = m_list.get(m_listPosOfLastSearch);
                 Comparable next = m_listEndPlus1 == m_listPosOfLastSearch + 1 ? null : m_list.get(m_listPosOfLastSearch + 1);
                 int currComp = curr.compareTo(compVal);
                 if (currComp == 0)
                	 return new Pair<Integer, Boolean>(m_listPosOfLastSearch, true);
                 int nextComp = next == null ? 1 : next.compareTo(compVal);
                 if (currComp < 0 && nextComp > 0)
                       return  new Pair<Integer, Boolean>(m_listPosOfLastSearch, false);
                //no point looking backward
                // Comparable prev = m_listPosOfLastSearch <= 0  ? null : (Comparable)m_list.get(m_listPosOfLastSearch - 1);
                // int prevComp = next == null ? 1 : prev.compareTo(compVal);
                // if (prevComp >= 0 && currComp < 0) {
                //	 m_listPosOfLastSearch--;
                //	 return new Pair<Integer, Boolean>(m_listPosOfLastSearch, prevComp == 0);                     	 
                // }

                 if (currComp > 0)
                       endPosPlus1 = m_listPosOfLastSearch;
                 else if (currComp < 0)
                       startPos = m_listPosOfLastSearch + 1;
                 int matchPos = Collections.binarySearch((List)m_list, compVal);
                 m_listPosOfLastSearch = matchPos < 0 ? -(matchPos+1)-1 : matchPos;
                 //return new Pair( matchPos < 0 ? -(matchPos+1)-1 : matchPos, matchPos >= 0);
                 return new Pair( m_listPosOfLastSearch, matchPos >= 0);
           }
           else {
        	   if (endPosPlus1 > 0) {
        		   int matchPos = Collections.binarySearch((List)m_list, compVal);
               	   m_listPosOfLastSearch = matchPos < 0 ? -(matchPos+1)-1 : matchPos;
                   //return new Pair( matchPos < 0 ? -(matchPos+1)-1 : matchPos, matchPos >= 0);
                   return new Pair( m_listPosOfLastSearch, matchPos >= 0);
	           }
	           else {
	        	    return new Pair(-1, false);
	           }
           }
     }
     public List<T> getUnderlyingList() {//code mish mash .. use this only for iterating thru all members
    	 return this.m_list;
     }
     
     public void replaceAt(int index, T compVal) {
    	 m_list.set(index, compVal);
     }
     
     public void addAtIndex(int index, T compVal) {
    	T existVal = get(index);
    	if (existVal != null && existVal.equals(compVal)) {
    		replaceAt(index, compVal);
    		return;
    	}
    	if (index == size())
    		m_list.add(compVal);
    	else
    		m_list.add(index,compVal);
    	 m_listEndPlus1++;
    	 if (m_listPosOfLastSearch >= index)
    		 m_listPosOfLastSearch++;
     }
     
     public Pair<T, Boolean> add(T compVal) {
    	 Pair<Integer, Boolean> addAt = indexOf(compVal);
    	 if (!addAt.second.booleanValue()) {
    		 int posToAdd = addAt.first+1;
    		 if (posToAdd == this.m_listEndPlus1) {
    			 m_list.add(compVal);
    		 }
    		 else {
    			 m_list.add(posToAdd, compVal);
    		 }
    		 m_listEndPlus1++;
    		 m_listPosOfLastSearch++;
    		 if (m_listEndPlus1 != m_list.size()) {
            	 //System.out.println("[FastListErr:AftAddSecFalse]"+m_listEndPlus1+","+m_list.size()+","+m_listPosOfLastSearch);        	 
             }
    		 return new Pair(compVal, addAt.second);
    	 }
    	 T dataPart = m_list.get(addAt.first);
    	 if (m_listEndPlus1 != m_list.size()) {
        	 //System.out.println("[FastListErr:AftAddSecTrue]"+m_listEndPlus1+","+m_list.size()+","+m_listPosOfLastSearch);        	 
         }
    	 return new Pair(dataPart, addAt.second); //merging if needed, needs to be done separately    
     }
     
     public Pair<T, Boolean> addWithReplacement(T compVal) {
    	 Pair<Integer, Boolean> addAt = indexOf(compVal);
    	 if (!addAt.second.booleanValue()) {
    		 int posToAdd = addAt.first+1;
    		 if (posToAdd == this.m_listEndPlus1) {
    			 m_list.add(compVal);
    		 }
    		 else {
    			 m_list.add(posToAdd, compVal);
    		 }
    		 m_listEndPlus1++;
    		 m_listPosOfLastSearch++;
    		 
    		 return new Pair(compVal, addAt.second);
    	 }
    	 else {
    		 m_list.set(addAt.first, compVal);
    		 return new Pair(compVal, true);
    	 }
     }
     public T get(T compVal) {
    	 return get(compVal, 0, false);
     }
     
     public T get(T compVal, boolean strictLess) {
    	 return get(compVal, 0, strictLess);
     }
     
     public T get(T compVal, int relIndex) {
    	 return get(compVal, relIndex, false);
     }
     
     public T get(T compVal, int relIndex, boolean strictLess) {
         	 Pair<Integer, Boolean> index = indexOf(compVal);
         	 
         	 int arrIndex = index == null ? -1 : index.first+relIndex;
         	 if (strictLess && index.second) 
         		 arrIndex--;
         	 return  (arrIndex < 0 || arrIndex >= this.m_listEndPlus1) ? null : m_list.get(arrIndex);
     }
     
     public boolean isAtEnd(T compVal) {
    	 if (compVal == null)
    		 return false;
    	 T endVal = get(size()-1);
    	 if (endVal == null)
    		 return true;
    	 int cmp = compVal.compareTo(endVal);
    	 return cmp >= 0; //i.e. compVal is >= endVal	
    	// Pair<Integer, Boolean> pos = this.indexOf(compVal);
    	// return pos.first == size()-1;
     }
     
     public boolean isAtEnd(int pos) {
    	 return pos == size()-1;
     }
     
     public T get(int index) {
    	 return index < 0 || index >= m_listEndPlus1 ? null : m_list.get(index);
     }
     
     public String toString() {
    	 StringBuilder retval = new StringBuilder();
    	 retval.append("Items:[");
    	 if (m_list != null) {
    		 int i=0;
    		 for (T item:m_list) {
    			 retval.append(i++).append("  ").append(item).append(",").append("\r\n");
    		 }
    	 }
    	 retval.append("]");
    	 return retval.toString();
     }
     
     public void mergeAtLeft(List<T> dataList) {
    	 int sz = dataList == null ? 0 : dataList.size();
    	 if (sz == 0)
    		 return;
    	 spShiftRight(sz);
    	 for (int i=0;i<sz;i++)
    		 this.m_list.set(i, dataList.get(i));
    	 if (m_listPosOfLastSearch < sz)
    		 m_listPosOfLastSearch = -1;
     }
     
     public void mergeAtLeftReverse(List<T> dataList) {
    	 int sz = dataList == null ? 0 : dataList.size();
    	 if (sz == 0)
    		 return;
    	 spShiftRight(sz);
    	 for (int i=0;i<sz;i++)
    		 this.m_list.set(i, dataList.get(sz-1-i));
    	 if (m_listPosOfLastSearch < sz)
    		 m_listPosOfLastSearch = -1;
     }
     
     public void mergeAtRight(List<T> dataList) {
    	 int sz = dataList == null ? 0 : dataList.size();
    	 if (sz == 0)
    		 return;
    	 ArrayList<T> alist = (ArrayList<T>) m_list;
    	 int initSz= size();
     	alist.ensureCapacity(initSz+sz);
     	m_listEndPlus1 = initSz+sz;
     	for (int i=0;i<sz;i++)
     		alist.add(dataList.get(i));
     }
     
     private void spShiftRight(int num) {
    	int sz = size();
    	ArrayList<T> alist = (ArrayList<T>) m_list;
    	alist.ensureCapacity(sz+num);
    	for (int i=0;i<num;i++)
    		alist.add(null);
    	for (int i=sz-1;i>=0;i--)
    		alist.set(i+num, alist.get(i));
        m_listEndPlus1 = sz+num;
        m_listPosOfLastSearch = m_listPosOfLastSearch < 0 ? -1 : m_listPosOfLastSearch+num;

     }
     
     public void spShiftLeft(int num) {
    	resize(size()-num); 
     }
     
  
}
