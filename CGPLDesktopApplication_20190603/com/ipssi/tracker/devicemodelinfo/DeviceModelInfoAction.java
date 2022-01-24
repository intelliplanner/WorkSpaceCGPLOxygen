package com.ipssi.tracker.devicemodelinfo;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.web.ActionI;

import static com.ipssi.tracker.common.util.ApplicationConstants.*;
import static com.ipssi.tracker.common.util.Common.*;
import java.sql.*;

/**
 * 
 * @author jai
 *
 */
public class DeviceModelInfoAction implements ActionI{
	
	private static Logger logger = Logger.getLogger(DeviceModelInfoAction.class);
	
	private final String EDIT_DEVICE = "/deviceModelInfo.jsp";
	
	
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String actionForward = "";
		String action = "";
		boolean success = false;
		
		try{
			action = getParamAsString(request.getParameter(ACTION));
			Connection conn = InitHelper.helpGetDBConn(request);
			if (CREATE.equals(action)) {
				//success = process(request);
				success = true;
				request.setAttribute("deviceModelId", 0);
				request.setAttribute("deviceModelBean", null);
			} else  if (DELETE.equals(action)) {
				
				String checkDelete[] = request.getParameterValues("checkbox");
				DeviceModelInfoDao deviceModelDao = new DeviceModelInfoDao();
				success =  deviceModelDao.deleteDeviceModel(conn, checkDelete);
				request.setAttribute("deviceModelBean", null);
				
			} else if (EDIT.equals(action)) {
				
				DeviceModelBean deviceModelBean = new DeviceModelBean();
				deviceModelBean = processDevice(request);
				success = isNull(deviceModelBean) ? false : true;
				if (success)
					request.setAttribute("deviceModelBean", deviceModelBean);
			}
			else if (SAVE.equals(action)) {
				
				success = process(request);
				request.setAttribute("deviceModelBean", null);
				
			} else {
				
				request.setAttribute("deviceModelId", 0);
				success = true;
				
			}
			
			ArrayList<DeviceModelBean> deviceModelList = new ArrayList<DeviceModelBean>();
			DeviceModelInfoDao deviceModelDao = new DeviceModelInfoDao();
			deviceModelList = deviceModelDao.fetchModelList(conn);
			request.setAttribute("deviceModelList", deviceModelList);
			
		}
		catch(GenericException ex){
			logger.error(ex);
		}
		
		actionForward = sendResponse(action, success, request);
		
		return actionForward;
	}
	
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		actionForward = EDIT_DEVICE;
		return actionForward;
	}
	
	public boolean process(HttpServletRequest request) throws GenericException {

		DeviceModelBean deviceModelBean = new DeviceModelBean();
		deviceModelBean = populateFields(request);
		boolean insertStatus = false;

		if (isNull(deviceModelBean)) {
			return insertStatus;
		}
		Connection conn = InitHelper.helpGetDBConn(request);
		DeviceModelInfoDao deviceModelDao = new DeviceModelInfoDao();
		try {
			if (deviceModelBean.getId() <= 0) {

				insertStatus = deviceModelDao.insertData(conn, deviceModelBean);
			} else {
				insertStatus = deviceModelDao.updateData(conn, deviceModelBean);
			}
		} catch (GenericException e) {
			throw new GenericException(e);
		} catch (Exception e) {
			throw new GenericException(e);
		}
		return insertStatus;

	}
	
	private DeviceModelBean populateFields(HttpServletRequest request) throws GenericException {

		DeviceModelBean deviceModelBean = new DeviceModelBean();
		
		System.out.println("Action - Ignition - "+getParamAsString(request.getParameter("ignition")));
		
		deviceModelBean.setModelName(getParamAsString(request.getParameter("model")));
		deviceModelBean.setBatteryFlag("y".equals((getParamAsString(request.getParameter("internalBattery"))).toLowerCase()) ? true : false);
		deviceModelBean.setIgnitionFlag("y".equals((getParamAsString(request.getParameter("ignition"))).toLowerCase()) ? true : false);
		deviceModelBean.setBuzzerFlag("y".equals((getParamAsString(request.getParameter("buzzer"))).toLowerCase()) ? true : false);
		deviceModelBean.setSmsDisplayFlag("y".equals((getParamAsString(request.getParameter("smsDisplay"))).toLowerCase()) ? true : false);
		deviceModelBean.setVoiceFlag("y".equals((getParamAsString(request.getParameter("voice"))).toLowerCase()) ? true : false);
		deviceModelBean.setId(getParamAsInt(request.getParameter("id")));
		deviceModelBean.setIoPinCount(getParamAsInt(request.getParameter("numberIo")) < 0 ? -1 : getParamAsInt(request.getParameter("numberIo")) );
		
		deviceModelBean.setCommand1(getParamAsString(request.getParameter("cmd1")));
		deviceModelBean.setCommand2(getParamAsString(request.getParameter("cmd2")));
		deviceModelBean.setCommand3(getParamAsString(request.getParameter("cmd3")));
		deviceModelBean.setCommand4(getParamAsString(request.getParameter("cmd4")));
		
		deviceModelBean.setModelProtocol(getParamAsInt(request.getParameter("modelProtocol")));
		deviceModelBean.setDeviceVersion(getParamAsInt(request.getParameter("deviceVersion")));
		deviceModelBean.setCommandName(getParamAsString(request.getParameter("commandword")));
		return deviceModelBean;

	}
	
	public DeviceModelBean processDevice(HttpServletRequest request) throws GenericException {
		int id = 0;
		DeviceModelBean deviceModelBean = null;
		Connection conn = InitHelper.helpGetDBConn(request);
		try {

			id = getParamAsInt(request.getParameter("id"));
			DeviceModelInfoDao deviceModelDao = new DeviceModelInfoDao();
			
			deviceModelBean = deviceModelDao.fetchModelData(conn, id);
			
		} catch (NumberFormatException nfe) {
			logger.error(ExceptionMessages.INVALID_PARAM, nfe);
			throw new GenericException(nfe);
		}
		return deviceModelBean;
	}
	
}
