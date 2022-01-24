package com.ipssi.rfid.connection;

public class ConnectionManager {

	private static boolean rfidReaderOneConnected = false;
	private static boolean rfidReaderTwoConnected = false;
	private static boolean rfidDesktopConnected = false;
	private static boolean weighBridgeConnected = false;
	private static boolean serverConnected = false;
	private static boolean biometricConnected = false;
	private static boolean barrierConnected = false;

	private static int rfidReaderOneRetry = 0;
	private static int rfidReaderTwoRetry = 0;
	private static int rfidDesktopRetry = 0;
	private static int weighBridgeRetry = 0;
	private static int serverRetry = 0;
	private static int biometricRetry = 0;
	private static int barrierRetry = 0;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static boolean isWeighBridgeConnected() {
		return weighBridgeConnected;
	}

	public static boolean isServerConnected() {
		return serverConnected;
	}

	public static boolean isBiometricConnected() {
		return biometricConnected;
	}

	public static boolean isBarrierConnected() {
		return barrierConnected;
	}

	public static void setWeighBridgeConnected(boolean weighBridgeConnected) {
		System.out.println("[WeighBridge Connection]:" + weighBridgeConnected);
		if (weighBridgeConnected) {
			weighBridgeRetry = 0;
		} else {
			if (weighBridgeRetry > 3) {
				System.out.println("Weigh Bridge Disconnected!!!");
			} else
				weighBridgeRetry++;
		}
		ConnectionManager.weighBridgeConnected = weighBridgeConnected;
	}

	public static void setServerConnected(boolean serverConnected) {
		System.out.println("[Server Connection]:" + serverConnected);
		if (serverConnected) {
			serverRetry = 0;
		} else {
			serverRetry++;
		}
		ConnectionManager.serverConnected = serverConnected;
	}

	public static void setBiometricConnected(boolean biometricConnected) {
		System.out.println("[Biometric Connection]:" + biometricConnected);
		if (biometricConnected) {
			biometricRetry = 0;
		} else {
			biometricRetry++;
		}
		ConnectionManager.biometricConnected = biometricConnected;
	}

	public static void setBarrierConnected(boolean barrierConnected) {
		System.out.println("[Barrier Connection]:" + barrierConnected);
		if (barrierConnected) {
			barrierRetry = 0;
		} else {
			if (barrierRetry > 3) {
				System.out.println("Barrier Disconnected!!!");
			} else
				barrierRetry++;
		}
		ConnectionManager.barrierConnected = barrierConnected;
	}

	public static boolean isRfidReaderOneConnected() {
		return rfidReaderOneConnected;
	}

	public static boolean isRfidReaderTwoConnected() {
		return rfidReaderTwoConnected;
	}

	public static boolean isRfidDesktopConnected() {
		return rfidDesktopConnected;
	}

	public static void setRfidReaderOneConnected(boolean rfidReaderOneConnected) {
		System.out.println("[RFID  One Connection]:" + rfidReaderOneConnected);
		if (rfidReaderOneConnected) {
			rfidReaderOneRetry = 0;
		} else {
			if (rfidReaderOneRetry > 3) {
				System.out.println("Reader One Disconnected!!!");
			} else
				rfidReaderOneRetry++;
		}
		ConnectionManager.rfidReaderOneConnected = rfidReaderOneConnected;
	}

	public static void setRfidReaderTwoConnected(boolean rfidReaderTwoConnected) {
		System.out.println("[RFID  Two Connection]:" + rfidReaderTwoConnected);
		if (rfidReaderTwoConnected) {
			rfidReaderTwoRetry = 0;
		} else {
			if (rfidReaderTwoRetry > 3) {
				System.out.println("Reader Two Disconnected!!!");
			} else
				rfidReaderTwoRetry++;
		}
		ConnectionManager.rfidReaderTwoConnected = rfidReaderTwoConnected;
	}

	public static void setRfidDesktopConnectedd(boolean rfidDesktopConnected) {
		System.out.println("[RFID  Desktop Connection]:" + rfidDesktopConnected);
		if (rfidDesktopConnected) {
			rfidDesktopRetry = 0;
		} else {
			if (rfidDesktopRetry > 3) {
				System.out.println("Desktop Reader Disconnected!!!");
			} else
				rfidDesktopRetry++;
		}
		ConnectionManager.rfidDesktopConnected = rfidDesktopConnected;
	}

}
