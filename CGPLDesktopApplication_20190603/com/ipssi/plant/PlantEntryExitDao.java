package com.ipssi.plant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import com.ipssi.gen.utils.SessionManager;

public class PlantEntryExitDao {
private SessionManager m_session;

public PlantEntryExitDao(SessionManager m_session) {
       this.m_session = m_session;
}

public void saveCaluseComment(int vehicleId,int userId, int cause, String comment,int entryOrExit){
	PreparedStatement ps = null;
	Connection conn = null;
	try {
		conn = m_session.getConnection();
		ps = conn.prepareStatement("insert into vehicle_interaction_notes values (?,?,?,?,?,?,?); ");
		ps.setInt(1, vehicleId);
		ps.setInt(2, userId);
		ps.setInt(3, cause);
		ps.setString(4, comment);
		ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
		ps.setString(6, null);
		ps.setString(7, entryOrExit == 0 ? "Entry" : "Exit");
		ps.execute();
	} catch (Exception e) {
		e.printStackTrace();
	}
}

}
