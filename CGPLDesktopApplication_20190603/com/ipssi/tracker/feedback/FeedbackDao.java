/**
 * 
 */
package com.ipssi.tracker.feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;

/**
 * @author jai
 * 
 */
public class FeedbackDao {
	private SessionManager m_session;
	private Logger logger = Logger.getLogger(FeedbackDao.class); 
	/**
	 * @param m_session
	 */
	public FeedbackDao(SessionManager m_session) {
		this.m_session = m_session;
	}

	/**
	 * @param bean
	 * @throws GenericException 
	 */
	public boolean save(FeedbackBean bean) throws GenericException {
		boolean retVal = false;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.Feedback.INSERT_FEEDBACK);
			
			Timestamp ts = new Timestamp((new Date()).getTime()); 
			
			ps.setInt(1, bean.getUserId());
			ps.setString(2, bean.getFeedback());
			ps.setTimestamp(3, ts);
			
			retVal = ps.executeUpdate() > 0 ? true : false;
			
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
		}  finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException ex) {
					logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
					throw new GenericException(ex);
				}
			}
		}
		return retVal;
	}

	public ArrayList<FeedbackBean> getFeedbackList() throws GenericException {
		ArrayList<FeedbackBean> retVal = null;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.Feedback.FETCH_FEEDBACK);
			ResultSet rs = ps.executeQuery();
			retVal = new ArrayList<FeedbackBean>();
			while(rs.next()){
				FeedbackBean bean = new FeedbackBean();
				bean.setUserName(rs.getString("NAME"));
				bean.setFeedback(rs.getString("feedback"));
				retVal.add(bean);
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
		}  finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException ex) {
					logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
					throw new GenericException(ex);
				}
			}
		}
		return retVal;

	}

}
