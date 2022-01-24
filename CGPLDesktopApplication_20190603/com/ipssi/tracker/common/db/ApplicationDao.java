package com.ipssi.tracker.common.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.ActionMasterBean;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.common.util.DimensionBean;
import com.ipssi.tracker.common.util.LogicalOperatorBean;
import com.ipssi.tracker.common.util.RegionBean;
import com.ipssi.tracker.common.util.SubjectBean;
import com.ipssi.tracker.rule.RuleTypeBean;

public class ApplicationDao {
	Logger logger = Logger.getLogger(ApplicationDao.class);

	public List<SubjectBean> getAllSubjects(Connection conn) throws GenericException {
		logger.info("Getting all subjects");
		String fetchSubjects = DBQueries.APPLICATION.FETCH_SUBJECTS;
		ResultSet rs = null;
		ArrayList<SubjectBean> subjectList = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchSubjects);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				subjectList = new ArrayList<SubjectBean>();
				SubjectBean subjectBean = null;
				while (rs.next()) {
					subjectBean = new SubjectBean();
					subjectBean.setId(rs.getInt("id"));
					subjectBean.setDescription((rs.getString("description")));
					subjectList.add(subjectBean);
				}
			}
			rs.close();
			contSt.close();
		} 
		catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return subjectList;

	}

	public List<RuleTypeBean> getAllRuleType(Connection conn) throws GenericException {
		logger.info("Getting all rule types");
		String fetchRuleType = DBQueries.APPLICATION.FETCH_RULE_TYPES;
		ResultSet rs = null;
		ArrayList<RuleTypeBean> ruleTypeList = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchRuleType);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				ruleTypeList = new ArrayList<RuleTypeBean>();
				RuleTypeBean ruleTypeBean = null;
				while (rs.next()) {
					ruleTypeBean = new RuleTypeBean();
					ruleTypeBean.setId(rs.getInt("id"));
					ruleTypeBean.setDescription((rs.getString("description")));
					ruleTypeBean.setShortCode(rs.getString("short_Code"));
					ruleTypeList.add(ruleTypeBean);
				}
			}
			rs.close();
			contSt.close();
		}
		catch (Exception ex) {
			logger.error(ExceptionMessages.DB_RET_CONN_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return ruleTypeList;

	}

	public List<DimensionBean> getAllDimensions(Connection conn) throws GenericException {
		logger.info("Getting all dimensions");
		String fetchDimensions = DBQueries.APPLICATION.FETCH_DIMENSIONS;
		ResultSet rs = null;
		ArrayList<DimensionBean> dimensionList = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchDimensions);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				dimensionList = new ArrayList<DimensionBean>();
				DimensionBean dimensionBean = null;
				while (rs.next()) {
					dimensionBean = new DimensionBean();
					dimensionBean.setId(rs.getInt("id"));
					dimensionBean.setDescription((rs.getString("description")));
					dimensionList.add(dimensionBean);
				}
			}
			rs.close();
			contSt.close();
		}
		catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return dimensionList;

	}

	public List<RegionBean> getAllRegions(Connection conn, int portNodeId) throws GenericException {
		logger.info("Getting all regions");
		String fetchRegions = DBQueries.APPLICATION.FETCH_REGIONS_UPPER;
		ResultSet rs = null;
		ArrayList<RegionBean> regionList = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchRegions);
			contSt.setInt(1, portNodeId);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				regionList = new ArrayList<RegionBean>();
				RegionBean regionBean = null;
				while (rs.next()) {
					regionBean = new RegionBean();
					regionBean.setId(rs.getInt("id"));
					regionBean.setShortCode(rs.getString("short_code"));
					regionList.add(regionBean);
				}
			}
			rs.close();
			contSt.close();
		}
		catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return regionList;
	}

	public List<LogicalOperatorBean> getAllLogicalOperators(Connection conn) throws GenericException {
		logger.info("Getting all logical operators");
		String fetchLogicalOperators = DBQueries.APPLICATION.FETCH_LOGICAL_OPERATORS;
		ResultSet rs = null;
		ArrayList<LogicalOperatorBean> logicalOperatorList = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchLogicalOperators);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				logicalOperatorList = new ArrayList<LogicalOperatorBean>();
				LogicalOperatorBean logicalOperatorBean = null;
				while (rs.next()) {
					logicalOperatorBean = new LogicalOperatorBean();
					logicalOperatorBean.setId(rs.getInt("id"));
					logicalOperatorBean.setDescription((rs.getString("description")));
					logicalOperatorList.add(logicalOperatorBean);
				}
			}
			rs.close();
			contSt.close();
		}
		catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return logicalOperatorList;
	}

	public List<ActionMasterBean> getAllActionMasters(Connection conn) throws GenericException {
		logger.info("Getting all actions");
		String fetchActionMasters = DBQueries.APPLICATION.FETCH_ACTION_MASTER;
		ResultSet rs = null;
		ArrayList<ActionMasterBean> actionMasterList = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchActionMasters);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				actionMasterList = new ArrayList<ActionMasterBean>();
				ActionMasterBean actionMasterBean = null;
				while (rs.next()) {
					actionMasterBean = new ActionMasterBean();
					actionMasterBean.setId(rs.getInt("id"));
					actionMasterBean.setAction(rs.getString("action"));
					actionMasterList.add(actionMasterBean);
				}
			}
			rs.close();
			contSt.close();
		}
		catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return actionMasterList;
	}
	public static ArrayList<MiscInner.PairIntStr> getBinList(Connection conn, int portNodeId) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.TripSetup.GET_BINLIST_FOR_ORG);
			//ps.setInt(1, listType);
			ps.setInt(1, portNodeId);
			ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(ps);
			ps.close();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static ArrayList<MiscInner.PairIntStr> getRoadSegments(Connection conn, int portNodeId) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.TripSetup.GET_ROADSEGMENT_FOR_ORG);
			//ps.setInt(1, listType);
			ps.setInt(1, portNodeId);
			ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(ps);
			ps.close();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public static ArrayList<MiscInner.PairIntStr> getRegions(Connection conn, int portNodeId) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.TripSetup.GET_REGIONS_FOR_ORG);
			//ps.setInt(1, listType);
			ps.setInt(1, portNodeId);
			ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(ps);
			ps.close();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public static ArrayList<MiscInner.PairIntStr> getOpStations(Connection conn, int portNodeId, int listType) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.TripSetup.GET_OPSTATION_FOR_ORG);
			ps.setInt(1, listType);
			ps.setInt(2, portNodeId);
			ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(ps);
			ps.close();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

//	public List<DimensionsBean> getAllDimensions() throws GenericException {
//		logger.info("Getting all Dimensionss");
//		Connection conn = null;
//		String fetchDimensions = DBQueries.APPLICATION.FETCH_SUBJECTS;
//		ResultSet rs = null;
//		ArrayList<DimensionBean> dimensionList = null;
//		try {
//			conn = getConnectionFromPool();
//			PreparedStatement contSt = conn.prepareStatement(fetchDimensions);
//			rs = contSt.executeQuery();
//			if (!Common.isNull(rs)) {
//				dimensionList = new ArrayList<DimensionBean>();
//				DimensionBean dimensionBean = null;
//				while (rs.next()) {
//					dimensionBean = new DimensionBean();
//					dimensionBean.setId(rs.getInt("id"));
//					dimensionBean.setDescription((rs.getString("description")));
//					dimensionList.add(dimensionBean);
//				}
//			}
//			returnConnectionToPool(conn);
//		} catch (SQLException sqlEx) {
//			try {
//				returnConnectionToPool(conn);
//				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
//				throw new GenericException(sqlEx);
//			} catch (Exception ex) {
//				logger.error(ExceptionMessages.DB_RET_CONN_PROBLEM, ex);
//				throw new GenericException(sqlEx);
//			}
//		} catch (Exception ex) {
//			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
//			throw new GenericException(ex);
//		}
//		return dimensionList;
//
//	}
}
