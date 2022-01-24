/* this is for FIFO queue of objects - max limit size Queue - in general this should be more efficient than
*   a free form Queue
*/

package com.ipssi.gen.utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public  class  Queue
{
// uses one empty element between head and tail to determine if the Q is full.
// circular array implementation
	public boolean debug = true;
    static private final int MAXIMUM_Q_SIZE = 450;
    private Object[] qArray = new Object[MAXIMUM_Q_SIZE+1];
    private int head  = 0;
    private int tail  = 0;
    private int maxSize = MAXIMUM_Q_SIZE;
    private int maxSizePlusOne = maxSize + 1; // for convenience
    /*
    public synchronized Object read() //is now stack .... hack
    {
        Object retval;
        if (head == 0)
           return null;
        retval = qArray[--head];        
        return(retval);
    }
    public synchronized void write(Object inObj) throws FullQueueException
    {
        if (head >= maxSizePlusOne)
           throw new FullQueueException();
        qArray[head++] = inObj;
    }
    public synchronized int getSize() {
       return head;
    }
    */

    public synchronized Object read() throws EmptyQueueException
    {
        Object retval;
        if (head == tail) {
            throw new EmptyQueueException();
        }
        head = (head+1)%maxSizePlusOne;
        retval = qArray[head];
        if (debug)
        	recordBusy(retval);
        return(retval);
    }
    public synchronized void write(Object inObj) throws FullQueueException
    {
    	if (debug) {//check if the obj already exists
    		for (int i=head;i != tail; ) {
    			i = (i+1)%maxSizePlusOne;
    			if (inObj != null && inObj == qArray[i]) {
    				System.out.println("[@@@ Returning already returned conn]"+inObj+ " [Thread:]"+Thread.currentThread().getId());
    				(new Exception()).printStackTrace();
    				return;
    			}
    		}
    	}
        int temp = (tail+1)%maxSizePlusOne;
        if ((temp-head) == 0)
            throw new FullQueueException();
        qArray[temp] = inObj;
        tail = temp;
        if (debug)
        	removeBusy(inObj);
    }
    public synchronized Object peekQ() throws EmptyQueueException
    {
       if (tail == head) {
          throw new EmptyQueueException();
       }
       else {
          int temp = (head+1)%maxSizePlusOne;
          return qArray[temp];
       }
    }
    public synchronized int getQlength()
    {
        return ((tail < head) ? maxSizePlusOne - (head - tail) : tail-head);
    }

    public  Queue() throws  TooBigQueueException
    {
         this(MAXIMUM_Q_SIZE);
    }
    public  Queue(int size) throws TooBigQueueException
    {
        super();
        if (maxSize > MAXIMUM_Q_SIZE) throw new TooBigQueueException();
        maxSize = size;
        maxSizePlusOne = size+1;
    }
    public static void main(String args[])
    { // for testing
        try
        {
        Queue q = new Queue(2);
        for (int i=0; i < 2;i++) {
        	try {
           q.write(new Integer(i));
        	}
        	catch (Exception e1) {
        		e1.printStackTrace();        		
        	}
        }
        
        for (int i=0;i< 1; i++)
           System.out.println("Read1:"+((Integer)(q.read())).toString());
        for (int i=0;i<1;i++)
           q.write(new Integer(i+10));
        for (int i=0;i<2;i++) {
        	try {
           System.out.println("Read2:"+((Integer)(q.read())).toString());
        	}
        	catch (Exception e2) {
        		e2.printStackTrace();
        	}
        }
        try {
        System.out.println("Read3:"+((Integer)(q.read())).toString());
        }
        catch (Exception e4) {
        	e4.printStackTrace();
        }
        for (int i=0; i < 1;i++) {
        	try {
           q.write(new Integer(i+20));
        	}
        	catch (Exception e1) {
        		e1.printStackTrace();        		
        	}
        }
        try {
            System.out.println("Read4:"+((Integer)(q.read())).toString());
            }
            catch (Exception e4) {
            	e4.printStackTrace();            
            }
            try {
                System.out.println("Read5:"+((Integer)(q.read())).toString());
                }
                catch (Exception e4) {
                	e4.printStackTrace();
                }
        }
        catch (Exception e)
        {
           System.out.println("Caught Exception");
           e.printStackTrace();
        }
    }
    
    private  HashMap<Object, Triple<Long, Long, Integer>> busyConn = new HashMap<Object, Triple<Long, Long, Integer>>();
	private  long lastLostCheckAt = -1;
	private  long lostCheckFreqMS = 5*60*1000L;
	
	synchronized public void removeBusy(Object inObj) {
		if (inObj != null) {
			busyConn.remove(inObj);
			checkForLost(inObj);
		}
	}
	
	public synchronized  void recordBusy(Object retval) {
		if (retval != null) {
			Triple<Long, Long, Integer> currEntry = busyConn.get(retval);
			long currId = Thread.currentThread().getId();
			if (currEntry != null) {
				StringBuilder sb = new StringBuilder("ConnectionPoolError: Taking a busy conn:");
				printConnBusyInfo(retval, currEntry, sb);
				System.out.println(sb);
				(new Exception ("Busy")).printStackTrace();
			}
			else {
				busyConn.put(retval, new Triple<Long, Long, Integer>(System.currentTimeMillis(), Thread.currentThread().getId(), DBConnectionPool.getCancellableRelevantCallProc()));
			}
		}
	}
	
	private synchronized void checkForLost(Object obj) {
		if (obj != null) {
			long curr = System.currentTimeMillis();
			StringBuilder sb = new StringBuilder();
			if ((curr - lastLostCheckAt) >lostCheckFreqMS) {
				sb.append("CheckLostConn:").append(this.getQlength()).append(",").append(this.maxSize).append(",").append(busyConn.size()).append("\n");
				for (Map.Entry<Object, Triple<Long,Long, Integer>> entry : busyConn.entrySet()) {
					try {
						if (entry.getKey() == obj)
							continue;
						long gap = curr - entry.getValue().first;
						if (gap > 3 * lostCheckFreqMS) {
							sb.append("ConnectionPoolError:Lost Connection:");
							printConnBusyInfo(entry.getKey(), entry.getValue(), sb);
							if (false && gap > (1800*1000) && entry.getValue().third == 1) {//half an hour ... leads to hang
								sb.append("Force Closing connection:");
								Connection cn = (Connection) entry.getKey();
								busyConn.remove(entry.getKey());//to prevent recursion .. although also taken care elsewhere
								DBConnectionPool.returnConnectionToPoolNonWeb(cn, true);
							}
							sb.append("\n");
						}
						
					}
					catch (Exception e2) {
						e2.printStackTrace();
						//eat it
					}
				}
				if (sb.length() > 0)
					System.out.println(sb);
				lastLostCheckAt = curr;
			}
		}
	}
	private static void printConnBusyInfo(Object conn, Triple<Long, Long, Integer> tsThreadId, StringBuilder sb) {
		sb.append("(conn, threadId, at ts):(").append(conn).append(",").append(tsThreadId.second).append(",").append(new java.util.Date(tsThreadId.first)).append(")").append("Cancellable:").append(tsThreadId.third);
		
	}
	
	// Keep Connection list and check for reinit conn
	
	private  HashMap<Object, Long> connMap = new HashMap<Object, Long>();
	private  long reInitialzeConnAfterSec = DBConnectionPool.reInitialzeConnAfterSec;
	
	synchronized public void removeConn(Object inObj) {
		StringBuilder sb = null;
		if(reInitialzeConnAfterSec > 0)
			sb = new StringBuilder();
		if (inObj != null && reInitialzeConnAfterSec > 0) {
			long time = connMap.get(inObj);
			if(sb != null)
				printConnMap(inObj, time, sb);
			connMap.remove(inObj);
		}
		if(sb != null)
			System.out.println("[DBCP_QU]removeConn : "+sb.toString());
	}
	
	synchronized public void addConn(Object inObj) {
		StringBuilder sb = null;
		if(reInitialzeConnAfterSec > 0)
			sb = new StringBuilder();
		if (inObj != null && reInitialzeConnAfterSec > 0) {
			long time = System.currentTimeMillis();
			connMap.put(inObj, time);
			if(sb != null)
				printConnMap(inObj, time, sb);
		}
		if(sb != null)
			System.out.println("[DBCP_QU]addConn : "+sb.toString());
	}
	
	public synchronized  boolean validateConn(Object inObj) {
		if (inObj == null)
			return false;
		if (inObj != null && reInitialzeConnAfterSec > 0) {
			Long currEntry = connMap.get(inObj);
			long currTime = System.currentTimeMillis();
			if (currEntry != null) {
				if((currTime - currEntry.longValue()) >  reInitialzeConnAfterSec*1000)
					return false;
			}
		}
		return true;
	}
	public static void printConnMap(Object conn, Long time, StringBuilder sb) {
		sb.append("(conn, at ts):(").append(conn).append(",").append(new java.util.Date(time)).append(")");
		
	}
	public void printConnMapDetail(Object conn, Long time, StringBuilder sb) {
		sb.append("(conn, at ts):(").append(conn).append(",").append(new java.util.Date(time)).append(")").append("Tot connMap:"+ connMap.size());
		
	}
	
}
