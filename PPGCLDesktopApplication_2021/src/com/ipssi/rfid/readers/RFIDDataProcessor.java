package com.ipssi.rfid.readers;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.ProcessStepProfile;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.beans.TPRBlockEntry;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;

public class RFIDDataProcessor {

	TAGListener tagListener = null;
	Object obj = new Object();
	boolean isRunning = false;
	int readerId;
	// Connection conn;
	int workStationType = Type.WorkStationType.GATE_IN_TYPE;
	int workStationId = Type.WorkStationType.GATE_IN_TYPE;
	int userId = Misc.getUndefInt();
	private Thread tagDataRead = null;

	public RFIDDataProcessor(int readerId, int workStationType, int workStationTypeId, int userId) {
		this.readerId = readerId;
		// this.conn = conn;
		this.workStationType = workStationType;
		this.workStationId = workStationTypeId;
		this.userId = userId;
		this.tagListener = tagListener;
	}

	public RFIDDataProcessor() {
	}

	public void setTagListener(TAGListener tagListener) {
		this.tagListener = tagListener;
	}

	synchronized public void pause() {
		isRunning = true;
	}

	synchronized public void Resume() {
		isRunning = false;
	}

	volatile boolean isTagReadThreadRunning = false;

	public void stopReadTagData() {
		if (tagDataRead == null) {
			return;
		}
		try {
			isTagReadThreadRunning = false;
			// tagDataRead.join();
			tagDataRead = null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	synchronized public void readTagData(final long sessionId, final String epc, final TAGListener handler) {
		stopReadTagData();
		try {
			tagDataRead = new Thread(new Runnable() {

				@Override
				public void run() {
					while (isTagReadThreadRunning) {
						try {
							RFIDTagInfo tag = RFIDMaster.getReader(readerId).getData(Utils.HexStringToByteArray(epc));
							if (tag != null && tag.epcId != null
									&& (Utils.ByteArrayToHexString(tag.epcId)
											.equalsIgnoreCase(Thread.currentThread().getName()))
									&& handler != null && isTagReadThreadRunning) {
								RFIDHolder holder = new RFIDHolder(null, tag);
								System.out.println("Read By Smart Read");
								holder.printData();
								handler.mergeData(sessionId, epc, holder);
								isTagReadThreadRunning = false;
								break;
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						} finally {
							try {
								Thread.sleep(TokenManager.refreshInterval);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			});
			isTagReadThreadRunning = true;
			tagDataRead.setName(epc);
			tagDataRead.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private int identifyVehicle(String vehicleName, String epc) {

		return Misc.getUndefInt();
	}

	synchronized public void processTag() {
		ArrayList<String> tags = null;
		Token token = null;
		String vehicleName = null;
		int vehicleId = Misc.getUndefInt();
		Connection conn = null;
		boolean destroyIt = false;
		StringBuilder sb = new StringBuilder();
		try {
			if (isRunning) {
				wait();
			} else if (isTagReadThreadRunning) {
				return;
			} else {
				isRunning = true;
			}
			if (isRunning && RFIDMaster.getReader(readerId) != null) {
				try {
					sb.setLength(0);
					conn = DBConnectionPool.getConnectionFromPoolNonWeb();
					tags = RFIDMaster.getReader(readerId).getRFIDTagList();
					if (tags != null && tags.size() > 0 && tagListener != null) {
						if (tags != null && tags.size() > 0) {
							sb.append("[RFID TAG List Size]:" + tags.size()).append("\n");
							for (String s : tags) {
								sb.append("[RFID Handler]:" + s).append("\n");
								if (TokenManager.isTokenAvailable(conn, s)) {
									sb.append("######### Token Available  ########").append("\n");
									if (RFIDMaster.getReader(readerId) != null) {

										if (true) {// tag != null) {
											/*
											 * boolean isTagValid = tag != null ? RFIDHolder.isCardValid(tag) :
											 * GateInDao.isTagExist(conn, s); if(!isTagValid){
											 * System.out.println("RFIDDataProcessor processTag : Invalid Tag");
											 * continue; }
											 */
											token = TokenManager.createToken(conn, s);
											if (token != null) {
												vehicleName = CacheTrack.standardizeName(token.getVehicleName());
												vehicleId = token.getVehicleId();
											} else {
												break;
											}
											RFIDTagInfo tag = workStationType <= Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE
													? RFIDMaster.getReader(readerId)
															.getData(Utils.HexStringToByteArray(s))
													: null;
											// use tag data upto weigh bridge in
											sb.append("[RFID Data Found]:" + (tag != null ? "YES" : "NO")).append("\n");
											RFIDHolder data = tag != null ? new RFIDHolder(null, tag) : null;
											if (data != null) {
												sb.append(data.toString());
												// data.printData();
											}
											sb.append("[Vehicle Found]:" + vehicleId + "," + vehicleName).append("\n");
											if (Misc.isUndef(vehicleId)) {
												com.ipssi.rfid.beans.Vehicle veh = null;
												if (data != null) {
													com.ipssi.rfid.beans.Vehicle refVehicle = new com.ipssi.rfid.beans.Vehicle();
													refVehicle.setStdName(data.getVehicleName());
													refVehicle.setStatus(1);
													ArrayList<Object> list = RFIDMasterDao.select(conn, refVehicle);
													if (list != null && list.size() > 0) {
														veh = (com.ipssi.rfid.beans.Vehicle) list.get(0);
														if (veh != null) {
															sb.append("[Data holder DB EPC]:" + veh.getEpcId())
																	.append("\n");
														}
													}
												}
												// need to varify
												if (veh == null || Misc.isUndef(veh.getId())
														|| !veh.getStdName().equalsIgnoreCase(vehicleName)
														|| (!Utils.isNull(veh.getEpcId())
																&& !(veh.getEpcId().equalsIgnoreCase(s)))) {
													sb.append(
															"Unregistered or Faulty RFID Card.\nplease go to registration.")
															.append("\n");
													tagListener.showMessage(
															"Unregistered or Faulty RFID Card.\nplease go to registration.");
													// GateInDao.saveEpcDetail(conn,s,workStationType, workStationId,
													// userId);
													TokenManager.returnToken(conn, token);
													continue;
												} else {

													Object[] options = { " No ", "  Yes  " };
													int answer = tagListener
															.promptMessage(
																	"Unregistered RFID Card\nIs Vehicle No is "
																			+ vehicleName + "\n please varify",
																	options);
													if (answer == 1) {
														vehicleId = veh.getId();
														token.setVehicleId(vehicleId);
														veh.setLastEPC(veh.getEpcId());
														veh.setEpcId(token.getEpcId());

														RFIDMasterDao.update(conn, veh, false);
														sb.append("Vehicle " + vehicleName + " registered to "
																+ token.getEpcId()).append("\n");
													} else {
														TokenManager.returnToken(conn, token);
														sb.append(
																"Unregistered or Faulty RFID Card.\nplease go to registration.")
																.append("\n");
														tagListener.showMessage(
																"Unregistered or Faulty RFID Card.\nplease go to registration.");
														continue;
													}
												}
											}
											if (token != null && !Misc.isUndef(token.getVehicleId()) && data != null) {
												data.setVehicleId(token.getVehicleId());
												if (Utils.isNull(data.getVehicleName())) {
													data.setVehicleName(token.getVehicleName());
												}
												sb.append("RFID HOLDER DATA : " + data.getVehicleId()).append("\n");
											}
											
											ProcessStepProfile processStepProfile =  ProcessStepProfile.getStandardProcessStepByMaterialCat(TokenManager.materialCat);
											Triple<TPRecord, Integer, Boolean> tprTriplet = TPRInformation
													.getLatestNonWeb(conn, vehicleId, data, vehicleName,
															workStationType, TokenManager.materialCat, true, true, processStepProfile );// (conn,
																										// vehicleId,
																										// data,
																										// vehicleName,
																										// TokenManager.createNewTPR,
																										// workStationType);
											TPStep tpStep = null;
											TPRBlockManager tprBlockManager = null;
											if (tprTriplet == null || tprTriplet.first == null) {
												sb.append("Not creating tpr at this step").append("\n");
												tagListener.setVehicleName(vehicleName);
												tagListener.showMessage("Sikpped Gate In.Go to gate In first.");// message
																												// will
																												// be
																												// changed
																												// according
																												// to
																												// scenario
												TokenManager.returnToken(conn, token);
												tagListener.clear(false, conn);
												break;
											} else if (tprTriplet.third) {
												// tagListener.setVehicleName(vehicleName);
												tagListener.manageTag(conn, token, tprTriplet.first, tpStep,
														tprBlockManager);
												tagListener.showMessage("Record Already Captured.");// message will be
																									// changed according
																									// to scenario
												tagListener.clear(false, conn);
												break;
											} else {
												tpStep = TPRInformation.getTpStep(conn, tprTriplet.first,
														workStationType, workStationId, userId);
												tprBlockManager = TPRBlockManager.getTprBlockStatus(conn,
														TokenManager.portNodeId, tprTriplet.first,
														TokenManager.currWorkStationType,
														!Misc.isUndef(tprTriplet.first.getMaterialCat())
																? tprTriplet.first.getMaterialCat()
																: TokenManager.materialCat);
												tagListener.manageTag(conn, token, tprTriplet.first, tpStep,
														tprBlockManager);
												if (workStationType == Type.WorkStationType.GATE_IN_TYPE
														&& TokenManager.useSmartRFRead && data == null) {
													readTagData(token.getLastSeen(), s, tagListener);
												}
												break;
											}
										}
									}
								}
							}
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
					destroyIt = true;
				} finally {
					isRunning = false;
					if (sb != null) {
						System.out.println(sb.toString());
					}
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
					notifyAll();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Triple<Token, TPRecord, TPRBlockManager> getTprecord(String vehicleName) {
		return getTprecord(vehicleName, Misc.getUndefInt(), true, true);
	}

	public Triple<Token, TPRecord, TPRBlockManager> getTprecord(String vehicleName, int vehicleId,
			boolean generateToken, boolean initTPR) {
		Triple<Token, TPRecord, TPRBlockManager> retval = null;
		Token token = null;
		Connection conn = null;
		boolean destroyIt = false;
		StringBuilder sb = new StringBuilder();
		// int vehicleId = Misc.getUndefInt();
		try {
			sb.setLength(0);
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (TokenManager.isTokenAvailable(conn, null, vehicleName) || !generateToken) {
				// pause();
				if (generateToken) {
					token = TokenManager.createToken(conn, null, vehicleName);
				}
				if (token != null) {
					vehicleName = token.getVehicleName();
					vehicleId = token.getVehicleId();
				}
				sb.append("[Vehicle Found]:" + vehicleId + "," + vehicleName).append("\n");
				ProcessStepProfile processStepProfile =  ProcessStepProfile.getStandardProcessStepByMaterialCat(TokenManager.materialCat);
				Triple<TPRecord, Integer, Boolean> tprTriplet = TPRInformation.getLatestNonWeb(conn, vehicleId, null,vehicleName, workStationType, TokenManager.materialCat,true,true,processStepProfile);
				TPStep tpStep = null;
				TPRBlockManager tprBlockManager = null;
				if (tprTriplet == null || tprTriplet.first == null) {
					sb.append("Not creating tpr at this step").append("\n");
					if (tagListener != null) {
						tagListener.showMessage("Sikpped Gate In.Go to gate In first.");// message will be changed
																						// according to scenario
						tagListener.setVehicleName(vehicleName);
					}
					if (generateToken) {
						TokenManager.returnToken(conn, token);
					}
					if (tagListener != null) {
						tagListener.clear(false, conn);
					}
				} else if (tprTriplet.third) {
					if (tagListener != null) {
						// tagListener.setVehicleName(vehicleName);
						if (initTPR) {
							tagListener.manageTag(conn, token, tprTriplet.first, tpStep, tprBlockManager);
						}
						tagListener.showMessage("Record Already Captured.");// message will be changed according to
																			// scenario
					}
					if (tagListener != null) {
						tagListener.clear(false, conn);
					}
				} else {
					tprBlockManager = TPRBlockManager.getTprBlockStatus(conn, TokenManager.portNodeId, tprTriplet.first,
							TokenManager.currWorkStationType,
							!Misc.isUndef(tprTriplet.first.getMaterialCat()) ? tprTriplet.first.getMaterialCat()
									: TokenManager.materialCat);
					// allowedForThisStep = TPRBlockStatusHelper.allowCurrentStep(conn, vehicleId,
					// tprTriplet != null ? tprTriplet.first : null, Misc.getUndefInt(),
					// workStationType, vehicleId,true,false);
					tpStep = TPRInformation.getTpStep(conn, tprTriplet.first, workStationType, workStationId, userId);
					retval = new Triple<Token, TPRecord, TPRBlockManager>(token, tprTriplet.first, tprBlockManager);
					if (tagListener != null) {
						if (initTPR) {
							tagListener.manageTag(conn, token, tprTriplet.first, tpStep, tprBlockManager);
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
		} finally {
			try {
				if (sb != null) {
					System.out.println(sb.toString());
				}
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// resume();
		}
		return retval;
	}

	private static String getBlockingReason(ArrayList<TPRBlockEntry> blockingEntries, Connection conn, TPRecord tpr) {
		String retval = "";
		try {
			for (int i = 0, is = blockingEntries == null ? 0 : blockingEntries.size(); i < is; i++) {
				TPRBlockEntry tprBlockEntry = blockingEntries.get(i);
				if (tprBlockEntry == null) {
					continue;
				}
				BlockingInstruction bInstruction = (BlockingInstruction) RFIDMasterDao.get(conn,
						BlockingInstruction.class, tprBlockEntry.getInstructionId());
				if (bInstruction == null) {
					continue;
				}
				String blockStr = (bInstruction.getType() == Type.BlockingInstruction.BLOCK_DUETO_STEP_JUMP
						? "Skip " + Type.WorkStationType.getString(tpr.getNextStepType())
						: Type.BlockingInstruction.getBlockingStr(bInstruction.getType()));
				if (retval.length() > 0) {
					if (retval.contains(blockStr)) {
						continue;
					}
					retval += "&nbsp;&nbsp;";
				}
				// retval += blockStr;
				retval += "<span style='background-color: red;color:white; margin-left:5px; padding:5px; font-size:16pt;'>&nbsp;"
						+ blockStr + "&nbsp;";
				/*
				 * if(tprBlockEntry.getType() == TPRBlockStatusHelper.BLOCK_STEP &&
				 * tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_AUTO) retval +=
				 * "<br>&nbsp;Skip "+
				 * Type.WorkStationType.getString(tpr.getNextStepType())+"&nbsp;";
				 */
				retval += "</span>";
			}
			if (retval != null && retval.length() > 0) {
				retval = "<html><body>" + retval + "</body></html>";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public void clearData(byte[] epc, int attempt) {
		try {
			if (RFIDMaster.getReader(0) != null) {
				// pause();
				RFIDMaster.getReader(0).clearData(epc, 5);
				// resume();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String getTprBlockStatus(TPRecord tprRecord, BlockingInstruction bIns, boolean add) {
		String retval = "";
		Connection conn = null;
		boolean destroyIt = false;
		if (tprRecord == null || bIns == null) {
			return "";
		}
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			TPRBlockEntry tprBlockEntry = new TPRBlockEntry();
			tprBlockEntry.setTprId(tprRecord.getTprId());
			tprBlockEntry.setType(0);
			tprBlockEntry.setStatus(1);
			tprBlockEntry.setWorkstationTypeId(TokenManager.currWorkStationType);
			tprBlockEntry.setInstructionId(bIns.getId());
			tprBlockEntry.setCreatedBy(TokenManager.userId);
			tprBlockEntry.setCreatedOn(new Date());
			tprBlockEntry.setCreateType(0);
			int id = Misc.getUndefInt();
			for (int i = 0, is = tprRecord.getBlockingEntries() == null ? 0
					: tprRecord.getBlockingEntries().size(); i < is; i++) {
				if (tprBlockEntry.getInstructionId() == tprRecord.getBlockingEntries().get(i).getInstructionId()) {
					id = i;
					break;
				}
			}
			if (add) {
				if (Misc.isUndef(id)) {
					if (tprRecord.getBlockingEntries() == null) {
						tprRecord.setBlockingEntries(new ArrayList<TPRBlockEntry>());
					}
					tprRecord.getBlockingEntries().add(tprBlockEntry);
				}
			} else {
				if (!Misc.isUndef(id) && tprRecord.getBlockingEntries() != null) {
					tprRecord.getBlockingEntries().remove(id);
				}
			}

			retval = getBlockingReason(tprRecord.getBlockingEntries(), conn, tprRecord);
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return retval;
	}

	public static void main(String[] arg) {
		String b = "10010110010100000100000011011111001001001010001100001011000001001100011011111111111111000100000011000001100100001011010001101101110000000000000011001011111000100100000000000000000000010010010001011110011110000100000101000101000100001011000001000001000001000001000001011010010111001111000100000101000101000100001011000001000001000001000001000011000010011101100000011000110101011011111100010000000000000000010001000000011110011110000100000101000100000110001001000001000001000001000001000001000000010010000000100000";
		byte data[] = Utils.GetBytesFromBinaryString(b);
		RFIDTagInfo tag = new RFIDTagInfo();
		tag.userData = data;
		tag.epcId = "EADASJDFk0001".getBytes();
		RFIDHolder rfid = new RFIDHolder("", tag);
		System.out.println();
	}
}
