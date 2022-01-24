package com.ipssi.rfid.readers;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.ipssi.rfid.processor.Utils;

public class WeighBridgeTcp  {

	private String server;
	private int port;
	private int sleepTimeWhenConnectionNotAvail = 0;
	private int threadReadTimeOut = 0;
	private boolean isBusy = false;
	private int MAX_RETRY_COUNT = 3;
	private Socket socket;
	private OutputStream out = null;
	private InputStream in = null;

	public WeighBridgeTcp(String server, int port) {
		this.server = server;
		this.port = port;
		this.sleepTimeWhenConnectionNotAvail = 30000; // Convert.ToInt32(MysqlDB.prop.get("threadSleepWithNoConnection",
		// "300000"));
		this.threadReadTimeOut = 1000; // Convert.ToInt32(MysqlDB.prop.get("threadReadTimeOut", "5000"));
	}

	public boolean getConnection() {
		boolean connTrue = false;
		try {
			close();
			this.socket = ConnectSocket(server, port);
//			 this.socket.setSoTimeout(threadReadTimeOut);
			if (socket != null) {
				connTrue = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return connTrue;
	}

	private void closeOutStream() {
		try {
			if (out != null)
				out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void closeInStream() {
		try {
			if (in != null)
				in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private Socket ConnectSocket(String server, int port) {
		Socket s = null;
		Socket tempSocket;
		try {
			tempSocket = new Socket(server, port);
			tempSocket.setSoTimeout(threadReadTimeOut);
			if (tempSocket.isConnected()) {
				s = tempSocket;
				closeOutStream();
				closeInStream();
				in = s.getInputStream();
				out = s.getOutputStream();
				setConnected();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			setDisconnected();
			e.printStackTrace();
		} catch (IOException e) {
			setDisconnected();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	private void setConnected() {

	}

	private void setDisconnected() {

	}

	public void close() {
		try {
			if (socket != null) {
				socket.close();
			}
			socket = null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean open() {
		return getConnection();
	}

	public void Connect() {
		try {
			int retry = 0;
			boolean connTrue = getConnection();
			while (!connTrue) {
				if (retry < 2) {
					Thread.sleep(sleepTimeWhenConnectionNotAvail);
					connTrue = getConnection();
					retry++;
				} else {
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public byte[] executeCommand(String hexCommand) {
		byte[] command = Utils.HexStringToByteArray(hexCommand);
		if(command == null)
			return null;
		boolean isConnection = false;
		byte[] retval = null;
		int retryCount = 0;
		if (isBusy) {
			return null;
		}
		isBusy = true;
		try {
			if (socket == null || !socket.isConnected() || socket.isClosed()) {
				isConnection = getConnection();
			} else {
				isConnection = true;
			}
			synchronized (socket) {
				while (retryCount < MAX_RETRY_COUNT) {
					if (isConnection) {
						try {
							if (socket.isConnected() && out != null) {
								out.write(command, 0, command.length);
								out.flush();
							}
							retval = new byte[512];
							if (socket.isConnected() && in != null) {
								in.read(retval, 0, 512);
							}
						} catch (SocketException e) {
							close();
						} catch (IOException ex1) {
							setDisconnected();
							ex1.printStackTrace();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							System.out.println("WeighBridgeTcp.executeCommand");
						} finally {

						}
						if (retval != null) {
							break;
						}
						retryCount++;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			isBusy = false;
		}
		return retval;

	}
	public static void main(String[] args) {
		try{  
			ServerSocket ss=new ServerSocket(5000);  
			Socket s=ss.accept();//establishes connection   
			DataInputStream dis=new DataInputStream(s.getInputStream());  
			//String  str= dis.readLine();  
			OutputStream out = s.getOutputStream();
			String weight = "    740 kg    G 000000 A";
			out.write(weight.getBytes(), 0, weight.getBytes().length);
			out.flush();
			out.close();
			System.out.println("message= "+null);  
			while(true)
				System.out.println();
			//ss.close();  
		}catch(Exception e){System.out.println(e);}  
	}  
}
