package com.ipssi.tracker.devicemodelinfo;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;

import static com.ipssi.tracker.common.util.Common.*;
import static com.ipssi.tracker.common.util.ApplicationConstants.DELETED;
//import static com.ipssi.tracker.common.util.ApplicationConstants.DELETE;




public class DeviceModelInfoDao {
	private static Logger logger = Logger.getLogger(DeviceModelInfoDao.class);
	
	boolean insertData(Connection conn, DeviceModelBean deviceModelBean) throws GenericException {
		int update = 0;
		boolean insertStatus = false;
		
		try{
			
			String insertData = DBQueries.DEVICEMODELINFO.INSERT_DATA_TO_DEVICE_MODEL_INFO;
			
			Timestamp timestamp = new Timestamp((new Date()).getTime());
			String modelName = deviceModelBean.getModelName();
			String battery = deviceModelBean.getBatteryFlag() ? "y" : "n";
			String buzzer = deviceModelBean.getBuzzerFlag() ? "y" : "n";
			String voice = deviceModelBean.getVoiceFlag() ? "y" : "n";
			String smsDisplay = deviceModelBean.getSmsDisplayFlag() ? "y" : "n";
			String ignition = deviceModelBean.getIgnitionFlag() ? "y" : "n";
			int ioPinCount = deviceModelBean.getIoPinCount();
			String cmd1 = deviceModelBean.getCommand1().trim();
			String cmd2 = deviceModelBean.getCommand2().trim();
			String cmd3 = deviceModelBean.getCommand3().trim();
			String cmd4 = deviceModelBean.getCommand4().trim();
			
			int id = 0;
			//" insert into device_model_info(name,internal_battery,ignition_on_off,voice,buzzer,io_count,display,updated_on)" +
			//" values(?,?,?,?,?,?,?,?)";
			
			PreparedStatement ps = conn.prepareStatement(insertData);
			
			ps.setString(1, modelName );
			ps.setString(2, battery);
			ps.setString(3, ignition);
			ps.setString(4, voice);
			ps.setString(5, buzzer);
			if (ioPinCount < 0)
				ioPinCount = Misc.getUndefInt();
			Misc.setParamInt(ps, ioPinCount, 6);
			ps.setString(7, smsDisplay);
			ps.setTimestamp(8, timestamp);
			ps.setInt(9,deviceModelBean.getModelProtocol());
			ps.setInt(10, deviceModelBean.getDeviceVersion());
			ps.setString(11, deviceModelBean.getCommandName());
			update = ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
			if (update > 0) {
				insertStatus = true;
			}
			
			if (cmd1 != "" || cmd2 != "" || cmd3 != "" && cmd4 != "") {
				insertData = DBQueries.DEVICEMODELINFO.INSERT_DATA_TO_DEVICE_MODEL_COMMANDS;
				ps = conn.prepareStatement(insertData);

				// "insert into device_model_commands(device_model_info_id, command1, command2, command3, command4) values(?,?,?,?,?)";

				ps.setInt(1, id);
				ps.setString(2, cmd1);
				ps.setString(3, cmd2);
				ps.setString(4, cmd3);
				ps.setString(5, cmd4);

				update = ps.executeUpdate();

				if (update > 0 && insertStatus) {
					insertStatus = true;
				} else {
					insertStatus = false;
				}
				ps.close();
			}
			
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}

		return insertStatus;
		
	}


	boolean updateData(Connection conn, DeviceModelBean deviceModelBean) throws GenericException {
		int update = 0;
		boolean insertStatus = false;

		try {
			
			String updateData = DBQueries.DEVICEMODELINFO.UPDATE_DATA_TO_DEVICE_MODEL_INFO; 
			
			Timestamp timestamp = new Timestamp((new Date()).getTime());
			String modelName = deviceModelBean.getModelName();
			String battery = deviceModelBean.getBatteryFlag() ? "y" : "n";
			String buzzer = deviceModelBean.getBuzzerFlag() ? "y" : "n";
			String voice = deviceModelBean.getVoiceFlag() ? "y" : "n";
			String smsDisplay = deviceModelBean.getSmsDisplayFlag() ? "y" : "n";
			String ignition = deviceModelBean.getIgnitionFlag() ? "y" : "n";
			int ioPinCount = deviceModelBean.getIoPinCount();
			int id = deviceModelBean.getId();
			String cmd1 = deviceModelBean.getCommand1();
			String cmd2 = deviceModelBean.getCommand2();
			String cmd3 = deviceModelBean.getCommand3();
			String cmd4 = deviceModelBean.getCommand4();
			int deviceVersion = deviceModelBean.getDeviceVersion();
			String commandName = deviceModelBean.getCommandName();
			
			//"update device_mode_info set name = ?, internal_battery = ?, ignition_on_off = ?, "+
			//"voice = ?, buzzer = ?, io_count = ?, display = ?, updated_on = ? where id = ?";
			
			PreparedStatement ps = conn.prepareStatement(updateData);
			
			ps.setString(1, modelName);
			ps.setString(2, battery);
			ps.setString(3, ignition);
			ps.setString(4, voice);
			ps.setString(5, buzzer);
			if (ioPinCount < 0)
				ioPinCount = Misc.getUndefInt();
			Misc.setParamInt(ps, ioPinCount, 6);
			ps.setString(7, smsDisplay);
			ps.setTimestamp(8, timestamp);
			ps.setInt(9, deviceModelBean.getModelProtocol());
			ps.setInt(10, deviceVersion);
			ps.setString(11, commandName);
			ps.setInt(12, id);
			
			update = ps.executeUpdate();
			
			if (update > 0) {
				insertStatus = true;
			}
			
			updateData = DBQueries.DEVICEMODELINFO.DELETE_DATA_FROM_DEVICE_MODEL_COMMANDS;
			ps = conn.prepareStatement(updateData);
			ps.setInt(1, id);
			ps.executeUpdate();
			ps.close();
			if (cmd1 != "" || cmd2 != "" || cmd3 != "" && cmd4 != "") {
				updateData = DBQueries.DEVICEMODELINFO.INSERT_DATA_TO_DEVICE_MODEL_COMMANDS;
				ps = conn.prepareStatement(updateData);

				// "insert into device_model_commands(device_model_info_id, command1, command2, command3, command4) values(?,?,?,?,?)";

				ps.setInt(1, id);
				ps.setString(2, cmd1);
				ps.setString(3, cmd2);
				ps.setString(4, cmd3);
				ps.setString(5, cmd4);
				
				update = ps.executeUpdate();
				if (update > 0 && insertStatus) {
					insertStatus = true;
				} else {
					insertStatus = false;
				}
				ps.close();
			}
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		
		return insertStatus;

	}
	
	ArrayList<DeviceModelBean> fetchModelList(Connection conn) throws GenericException {
		
		ArrayList<DeviceModelBean> deviceModelList = new ArrayList<DeviceModelBean>();
		try{
			String fetch = DBQueries.DEVICEMODELINFO.FETCH_DATA_FROM_DEVICE_MODEL_INFO ;
			
			PreparedStatement ps = conn.prepareStatement(fetch);
			ps.setInt(1, DELETED);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				DeviceModelBean deviceModelBean = new DeviceModelBean();
				
				deviceModelBean.setId(rs.getInt("id"));
				deviceModelBean.setModelName(rs.getString("name"));
							
				
				deviceModelBean.setBatteryFlag( "y".equals(rs.getString("internal_battery")) ? true : false );
				deviceModelBean.setIgnitionFlag("y".equals(rs.getString("ignition_on_off")) ? true : false );
				deviceModelBean.setBuzzerFlag( "y".equals(rs.getString("buzzer")) ? true : false );
				deviceModelBean.setVoiceFlag( "y".equals(rs.getString("voice")) ? true : false );
				deviceModelBean.setSmsDisplayFlag("y".equals(rs.getString("display")) ? true : false );
				deviceModelBean.setIoPinCount(Misc.getRsetInt(rs, "io_count",0));
				
				deviceModelBean.setModelProtocol(rs.getInt("device_model"));
				deviceModelBean.setDeviceVersion(rs.getInt("device_version"));
				deviceModelBean.setCommandName(rs.getString("command_word"));
				
				deviceModelList.add(deviceModelBean);
			}
			
			rs.close();
			ps.close();
			
		}catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);

			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return deviceModelList; 
	}
	
	boolean deleteDeviceModel(Connection conn, String[] id) throws GenericException{
		
		boolean result = false;
		int updateIo = 0;

		try {
			
			String deleteModel = DBQueries.DEVICEMODELINFO.DELETE_DATA_FROM_DEVICE_MODEL_INFO;
			
			PreparedStatement ps = conn.prepareStatement(deleteModel);
			for (int i = 0; i < id.length; i++) {
				
				ps.setInt(1,DELETED);
				ps.setInt(2, com.ipssi.gen.utils.Misc.getParamAsInt(id[i]));
				updateIo = ps.executeUpdate();
			}
			
			if (updateIo > 0) {
				result = true;
			}
			
			ps.close();

		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		
		return result;
	}
	
	DeviceModelBean fetchModelData(Connection conn, int id) throws GenericException{
		DeviceModelBean deviceModelBean = new DeviceModelBean();
		
		try{
			String fetch = DBQueries.DEVICEMODELINFO.FETCH_DATA_FOR_DEVICE_MODEL;
			
			PreparedStatement ps = conn.prepareStatement(fetch);
			ps.setInt(1,id);
			
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {			
				deviceModelBean.setId(id);
				deviceModelBean.setModelName(getParamAsString(rs.getString("name")));
				deviceModelBean.setBatteryFlag("y".equals(rs.getString("internal_battery")) ? true : false );
				deviceModelBean.setIgnitionFlag("y".equals(rs.getString("ignition_on_off")) ? true : false );
				deviceModelBean.setBuzzerFlag("y".equals(rs.getString("buzzer")) ? true : false );
				deviceModelBean.setVoiceFlag("y".equals(rs.getString("voice")) ? true : false );
				deviceModelBean.setSmsDisplayFlag("y".equals(rs.getString("display")) ? true : false );
				deviceModelBean.setIoPinCount(Misc.getRsetInt(rs, "io_count",0));
				
				deviceModelBean.setCommand1(getParamAsString(rs.getString("command1")));
				deviceModelBean.setCommand2(getParamAsString(rs.getString("command2")));
				deviceModelBean.setCommand3(getParamAsString(rs.getString("command3")));
				deviceModelBean.setCommand4(getParamAsString(rs.getString("command4")));
				
				deviceModelBean.setModelProtocol(rs.getInt("device_model"));
				deviceModelBean.setDeviceVersion(rs.getInt("device_version"));
				deviceModelBean.setCommandName(rs.getString("command_word"));
			}
			
			rs.close();
			ps.close();
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		} 		
		return deviceModelBean;
	}
}	