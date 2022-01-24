package com.ipssi.gen.utils;

public class TrackQuery 
{
  public static String GET_POINTS_IN_RANGE = "select landmarks.id, landmarks.name, (landmarks.lowerX+landmarks.upperX)/2.0, (landmarks.lowerY+landmarks.upperY)/2.0 "+
                     " from "+
                     " port_nodes leaf join port_nodes anc on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
                     " join landmarks on (landmarks.port_node_id = anc.id) "+
                     " where "+
                     " (landmarks.lowerX+landmarks.upperX)/2.0 between ? and ? "+
                     " and (landmarks.lowerY+landmarks.upperY)/2.0 between ? and ? "
                     ;
                     
  public static String GET_REGIONS_CONTAINING_POINT =
	  				"select regions.id, regions.short_code, regions.equal_to_MBR, regions.lowerX, regions.lowerY, regions.upperX, regions.upperY, astext(regions.shape) wkt "+
                     " from "+
                     " port_nodes leaf join port_nodes anc on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
                     " join regions on (regions.port_node_id = anc.id) "+
                     " where "+
                     " regions.lowerX <= ? and regions.upperX >= ? "+
                     " and regions.lowerY <= ? and regions.upperY >= ? "
                     ;
                       
                                       
}