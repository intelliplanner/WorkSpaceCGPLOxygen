/*

  Notes on schema - wspace_id -> in non pj_basics, alt_basics, alt_lists, alt_dates, alt_taks, alt_profiles, alt_models is
  no longer used - However insert stmt still have the wspace_id - that may eventually be removed

*/
//Title:        Your Product Name
//Version:
//Copyright:   Copyright (c) 1999
//Author:      Your Name
//Company:     Your Company
//Description:
package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;

public class Queries {
	//TODO - now not using port_map_items
	//  public final static String GET_PORT_VERSION = "select port_map_items.map_type, port_map_items.id, port_map_items.created_on, port_map_items.isdefault, port_wksps.id, port_wksps.name, port_rset.id, port_rset.name from port_map_items, port_nodes, port_wksps, port_rset where (port_nodes.port_node_id IS NULL) and (port_nodes.id = port_map_items.port_node_id) and (port_map_items.port_wksp_id = port_wksps.id) and (port_wksps.id = port_rset.port_wksp_id (+)) and (port_wksps.is_for_prj_dw = 0) and (port_map_items.map_type <> ?) "+ //TOVERIFY
	//                      "and (((port_map_items.map_type <> ?) and (port_rset.port_wksp_id_rec_rset is not null)) or (port_map_items.map_type = ?)) order by port_map_items.map_type asc, isdefault desc";
	//  public final static String GET_PORT_VERSION = "select port_wksps.map_type, port_wksps.isdefault, port_wksps.id, port_wksps.name, port_rset.id, port_rset.name, port_rset.port_wksp_id_rec_rset from  port_wksps, port_rset where (port_wksps.port_node_id = 1) and (port_wksps.id = port_rset.port_wksp_id) and (port_wksps.is_for_prj_dw = 0) and (port_wksps.map_type <> ?) "+ //TOVERIFY
	//                      "and ((isdefault=1) or (isdefault=0 and map_type = 2)) order by port_wksps.map_type asc, port_wksps.id desc, port_rset.port_wksp_id_rec_rset nulls last";
	public final static String GET_PORT_VERSION = Misc.G_DO_ORACLE ? "select port_wksps.map_type, port_wksps.isdefault, port_wksps.id, port_wksps.name, port_rset.id, port_rset.name, port_rset.port_wksp_id_rec_rset,IS_AUTO_UPDATEABLE from  port_wksps, port_rset where (port_wksps.id = port_rset.port_wksp_id) and ((port_wksps.is_for_prj_dw = 0) or (port_wksps.is_for_prj_dw = 1 and port_wksps.map_type = 101)) "+ //TOVERIFY
		"and (isdefault=1)  order by port_wksps.map_type asc, port_wksps.id desc, port_rset.id desc, port_rset.port_wksp_id_rec_rset nulls last" 
                                                                 :"select port_wksps.map_type, port_wksps.isdefault, port_wksps.id, port_wksps.name, port_rset.id, port_rset.name, port_rset.port_wksp_id_rec_rset,IS_AUTO_UPDATEABLE from  port_wksps, port_rset where (port_wksps.id = port_rset.port_wksp_id) and ((port_wksps.is_for_prj_dw = 0) or (port_wksps.is_for_prj_dw = 1 and port_wksps.map_type = 101)) "+ //TOVERIFY
		"and (isdefault=1)  order by port_wksps.map_type asc, port_wksps.id desc, port_rset.id desc, port_rset.port_wksp_id_rec_rset desc";
   
   //NOTUSED 
	public final static String GET_PRJ_VERSION = Misc.G_DO_ORACLE ? "select map_type, pj_map_items.id, pj_map_items.date_created, isdefault, pj_map_items.wspace_id," + 
  "workspaces.name, wksp_labels.id1, wksp_labels.name1, coitems.userid from pj_map_items, workspaces, (select id id1, name name1, pj_map_id pj_map_id1 from labels where " +
  "wspace_id=?) wksp_labels, ((select distinct user_1_id userid, wspace_id wspaceid from pj_co_items where user_1_id = ?) union "+
  "(select distinct user_1_id userid, wspace_id wspaceid from pj_co_items where user_1_id = ?)) coitems where (pj_map_items.prj_id = ?) and (map_type <> ?) and " +
  "(pj_map_items.wspace_id = workspaces.id) and (pj_map_items.id  =wksp_labels.pj_map_id1 (+)) and (pj_map_items.wspace_id = coitems.wspaceid (+)) order by " +
  "pj_map_items.map_type asc, isdefault desc"
  : "select map_type, pj_map_items.id, pj_map_items.date_created, isdefault, pj_map_items.wspace_id, workspaces.name, wksp_labels.id1, wksp_labels.name1, coitems.userid from pj_map_items, workspaces, (select id id1, name name1, pj_map_id pj_map_id1 from labels where wspace_id=?) wksp_labels, ((select distinct user_1_id userid, wspace_id wspaceid from pj_co_items where user_1_id = ?)  "+
  " union (select distinct user_1_id userid, wspace_id wspaceid from pj_co_items where user_1_id = ?)) coitems where (pj_map_items.prj_id = ?) and (map_type <> ?) and (pj_map_items.wspace_id = workspaces.id) and (pj_map_items.id  *= wksp_labels.pj_map_id1 ) and (pj_map_items.wspace_id *= coitems.wspaceid ) order by pj_map_items.map_type asc, isdefault desc"; //TOVERIFY
	
  public final static String GET_PROFILE_SUCC = "select prof_outcomes.profe_case_ty, prof_outcomes.probability, alt_basics.revhandling, alt_basics.patent_exp_date from prof_outcomes, alt_basics where prof_outcomes.alt_profil_id = ? and alt_basics.id = ?";
	//  public final static String GET_MS_DUR_PORT_RSET_DELAY = "select milestones.mstn_id, milestones.mstn_status, milestones.duration, milestones.succ_prob, milestones.start_date, milestones.finish_dt, rec_times_ms.rec_t_ms_delay from milestones,rec_times_ms, port_results where (milestones.alt_date_id=?) and (port_results.port_rs_id(+) = ?) and (port_results.alt_id (+) = ?) and (rec_times_ms.ms_id (+) = milestones.id) and (rec_times_ms.port_resul_id  = port_results.id) order by milestones.mstn_id asc";
	// Changed since the Delay nodes themselves are tied to Milestones which in turn are uniquely tied to an alt_date etc.
/* 102307
	public final static String GET_MS_DUR_PORT_RSET_DELAY = Misc.G_DO_ORACLE ? "select milestones.mstn_id, milestones.mstn_status, milestones.duration, milestones.succ_prob, milestones.start_date, milestones.finish_dt, rec_times_ms.rec_t_ms_delay from milestones,rec_times_ms where (milestones.alt_date_id=?) and (rec_times_ms.ms_id (+) = milestones.id) order by milestones.mstn_id asc" : "select milestones.mstn_id, milestones.mstn_status, milestones.duration, milestones.succ_prob, milestones.start_date, milestones.finish_dt, rec_times_ms.rec_t_ms_delay from milestones,rec_times_ms where (milestones.alt_date_id=?) and (rec_times_ms.ms_id =* milestones.id) order by milestones.mstn_id asc";
*/
	//Queries for updating the revenue, costs etc.
	//Two basic insert mechanism for revenue updates -
	//Till X year shift-
	//From X year until end of time copy value as such.
	//Together these will help update revenue in 3 scenarios of loose the revenue (X is very large)
	//                patent protection ending in X year
	//                Simple Shift of year

	//MASSIVE CHANGES IN DELAY_VALS - basically now delay_vals are in quarters and that is it
	//These two must be exactly like each other
	/* - this was prior to removing val col in delay_vals thus id corresponds to quarterly delay values
	 */
//not used
	


	/*
	 */
	//These two must be exactly like each other
	//TODO-CHECK-FOR VAL_SCOPE in the query below
  //not used 
	
	//normcompletion is the normalized completion value - say the project is post milestone is 3 and is 0.75 done at time_dim_val, then this is marked as 3.75
	//Till Now done the regular value copying and shifting

	//The _NORM Strings refer to update on val columns, while _EXP refer to that on expected col.
	//Due to the fact of scaling by probability of outcome, the _NORM in case of rev and op cost
	//still exhibit the complexity of scaling. Indeed the _NORM and _EXP are different only in the
	//col being updated.

//not used
/* 102307
	public final static String DW_UPDATE_OTHER_MEASURES = "insert into dw_other_info (dw_alt_si_id, delay_imposed, dev_cost_cat, init_launch_date, npv_at_launch_var, npv_var, peak_rev, peak_rev_cat, risk_cat, tech_risk, npv, npv_at_launch, npv_dev_cost) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
//not used
	public final static String DW_UPDATE_NPV_ERR = "insert into dw_npv_err (npv_re, npv_im,  err_pt_ran_id, dw_alt_si_id) ( " +
		"select lfp.val_re, lfp.val_im,  epr2.id, ? from "+
		// 1st? - delay prescribed, 2nd ? - dw_alt_si_id
		"npv, log_fourier_pdf lfp, data, err_pt_range epr1, err_pt_range epr2 where (npv.alt_combined_id = ?) and (data.npv_id = npv.id) and (lfp.data_id = data.id) " +
		"and (npv.npv_type=?) " +
		//4th ? - modelID, same as 3rd;
		"and (epr1.id = lfp.err_pt_ran_id) " +
		//6th ?- prescribed delay same as 1st?; 7 th year till which shift has to occur (inclusive)
		//8th ?- the outcome profile case
		"and (epr2.id = (select min(erp3.id) from err_pt_range erp3 where erp3.high < epr1.val/?)))";
//not used
	public final static String DW_UPDATE_NPV_DEV_COST_ERR = "insert into dw_npv_err (npv_dev_cost_re, npv_dev_cost_im,  err_pt_ran_id, dw_alt_si_id) ( " +
		"select lfp.val_re, lfp.val_im,  epr2.id, ? from "+
		// 1st? - delay prescribed, 2nd ? - dw_alt_si_id
		"npv, log_fourier_pdf lfp, data, err_pt_range epr1, err_pt_range epr2 where (npv.alt_combined_id = ?) and (data.npv_id = npv.id) and (lfp.data_id = data.id) " +
		"and (npv.npv_type=?) " +
		//4th ? - modelID, same as 3rd;
		"and (epr1.id = lfp.err_pt_ran_id) " +
		//6th ?- prescribed delay same as 1st?; 7 th year till which shift has to occur (inclusive)
		//8th ?- the outcome profile case
		"and (epr2.id = (select min(erp3.id) from err_pt_range erp3 where erp3.high < epr1.val/?)))";
//not used
	public final static String DW_UPDATE_NPV_AT_LAUNCH_ERR = "insert into dw_npv_err (npv_at_launch_re, npv_at_launch_im,  err_pt_ran_id, dw_alt_si_id) ( " +
		"select lfp.val_re, lfp.val_im,  epr2.id, ? from "+
		// 1st? - delay prescribed, 2nd ? - dw_alt_si_id
		"npv, log_fourier_pdf lfp, data, err_pt_range epr1, err_pt_range epr2 where (npv.alt_combined_id = ?) and (data.npv_id = npv.id) and (lfp.data_id = data.id) " +
		"and (npv.npv_type=?) " +
		//4th ? - modelID, same as 3rd;
		"and (epr1.id = lfp.err_pt_ran_id) " +
		//6th ?- prescribed delay same as 1st?; 7 th year till which shift has to occur (inclusive)
		//8th ?- the outcome profile case
		"and (epr2.id = (select min(erp3.id) from err_pt_range erp3 where erp3.high < epr1.val/?)))";

	public final static String DW_DEL_ENTRY = "delete from dw_alt_short_info where alt_id = ? and port_rs_id = ?";
  //not used
	//public final static String INSERT_DW_ENTRY = "insert into dw_alt_short_info (id, alt_id, pj_id, ver_alt_basic_id, ver_alt_model_id, ver_alt_mstone_id, ver_alt_profile_id, ver_prj_basic_id, port_rs_id, fund_status, is_default_alt, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rating_id ) values( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  */
	/*
	 */
	//changed from yearly to quarterly view - 12/24
  /*102307
	public final static String GET_YEARLY_REV_TOT = "select sum(expected), sum(dw_rev.val), qtr_1900 from dw_rev, time_dims where (dw_alt_si_id = ?) and time_dim_val=time_dims.val and (time_dims.qtr_1900 >= ?)  group by time_dims.qtr_1900 order by qtr_1900 asc";
	public final static String GET_YEARLY_DEV_COST_TOT = "select sum(dw_cost.expected), sum(dw_cost.val), qtr_1900 from dw_cost,cost_items, time_dims where (dw_cost.dw_alt_si_id = ?) and time_dim_val=time_dims.val and (dw_cost.cost_li_id = cost_items.id) and (cost_items.alt_opcost_id IS NULL) and cost_items.to_include = 1 and (qtr_1900 >= ?)  group by qtr_1900  order by qtr_1900 asc";
	public final static String GET_YEARLY_OP_COST_TOT = "select sum(dw_cost.expected), sum(dw_cost.val), qtr_1900 from dw_cost,cost_items, time_dims where (dw_cost.dw_alt_si_id = ?) and (dw_cost.cost_li_id = cost_items.id) and (cost_items.alt_opcost_id IS NOT NULL) and time_dim_val = time_dims.val and cost_items.to_include = 1 and (qtr_1900 >= ?)  group by qtr_1900 order by qtr_1900 asc";
	public final static String DW_GET_NPV_ERR = "select sum(npv_re), sum(npv_im), sum(npv_at_launch_re), sum(npv_at_launch_im), sum(npv_dev_cost_re), sum(npv_dev_cost_im),dw_npv_err.err_pt_ran_id from dw_npv_err,err_pt_range where (dw_npv_err.dw_alt_si_id = ?) group by dw_npv_err.err_pt_ran_id order by dw_npv_err.err_pt_ran_id asc";
 
	public final static String GET_RAW_NPVS = "select npv_type, data.value from npv,data where npv.alt_combined_id=? and data.npv_id = npv.id";
 */

	//These two must be exactly like each other
  //used
	public final static String ALT_CRE_COSTITEMS_CF = Misc.G_DO_ORACLE ? 
  "insert into cost_items (alt_opcost_id, name, id, start_date, end_date, lineitem_id, expense_ty, for_achieving_milestone, cost_cent_id, classify1, classify2, classify3, classify4, classify5, to_include, task_internal_id, target_market, scen_id) values "+
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" //18 (aft scen_id) elems
  :
  "insert into cost_items (alt_opcost_id, name, start_date, end_date, lineitem_id, expense_ty, for_achieving_milestone, cost_cent_id, classify1, classify2, classify3, classify4, classify5, to_include, task_internal_id, target_market, scen_id) values "+
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; //18 (aft scen_id) elems
  
	//the order in which parameters are speced below for lookup is imp - trying to match alt_cre_fte_items and alt_cre_devcost_items so that
	//we can look up attributes in loop or appropriately set up the parameters - see helpPopulateAndExecuteResUpd
	public final static String ALT_CRE_COSTITEMS_DEV = Misc.G_DO_ORACLE ? 
  "insert into cost_items (alt_devcost_id, name, id, start_date, end_date, lineitem_id, expense_ty, for_achieving_milestone, cost_cent_id, classify1, classify2, classify3, classify4, classify5, to_include, task_internal_id, target_market, scen_id) values "+
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" //18 elems
    :
  "insert into cost_items (alt_devcost_id, name,  start_date, end_date, lineitem_id, expense_ty, for_achieving_milestone, cost_cent_id, classify1, classify2, classify3, classify4, classify5, to_include, task_internal_id, target_market, scen_id) values "+
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
    

	//the order in which parameters are speced below for lookup is imp - trying to match alt_cre_fte_items and alt_cre_devcost_items so that
	//we can look up attributes in loop or appropriately set up the parameters - see helpPopulateAndExecuteResUpd
	public final static String ALT_CRE_FTEITEMS = Misc.G_DO_ORACLE ? 
    "insert into fte_items (alt_fte_id, name, id, start_date, end_date, fte_lineitem_id, for_achieving_milestone, fte_head_id, classify1, classify2, classify3, classify4, classify5, to_include, task_internal_id, target_market, scen_id, for_skill, assignment_status) values "+
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" 
    : //19 elems
    "insert into fte_items (alt_fte_id, name,  start_date, end_date, fte_lineitem_id, for_achieving_milestone, fte_head_id, classify1, classify2, classify3, classify4, classify5, to_include, task_internal_id, target_market, scen_id, for_skill, assignment_status) values "+
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
	public final static String ALT_CRE_REVSEGS = Misc.G_DO_ORACLE ? 
  "insert into rev_segs (alt_rev_id, name, id, seg_id, mkt_type,  classify1, classify2, classify3, classify4, classify5, start_date, scen_id) values "+
		"(?,?,?,?,?,?,?,?,?,?,?,?)"
    :
  "insert into rev_segs (alt_rev_id, name,  seg_id, mkt_type,  classify1, classify2, classify3, classify4, classify5, start_date, scen_id) values "+
		"(?,?,?,?,?,?,?,?,?,?,?)"; //12 elems

	//THE ASSUMPTION Queries must be exactly similar
	public final static String ALT_CRE_ASS_FTE = Misc.G_DO_ORACLE ? "insert into assumptions (fte_item_id, name, id, assmptn_id, is_multiyear, added_by, added_on) values (?,?,?,?,?,?,sysdate)" 
                                                                : "insert into assumptions (fte_item_id, name, assmptn_id, is_multiyear, added_by, added_on) values (?,?,?,?,?,getdate())";

	public final static String ALT_CRE_ASS_REV = Misc.G_DO_ORACLE ? "insert into assumptions (rev_seg_id, name, id, assmptn_id, is_multiyear, added_by, added_on) values (?,?,?,?,?,?,sysdate)" 
                                                                : "insert into assumptions (rev_seg_id, name, assmptn_id, is_multiyear, added_by, added_on) values (?,?,?,?,?,?,getdate())"; 
	public final static String ALT_CRE_ASS_MOD = Misc.G_DO_ORACLE ? "insert into assumptions (alt_combined_id, name, id, assmptn_id, is_multiyear, added_by, added_on) values (?,?,?,?,?,?,sysdate)" 
                                                                : "insert into assumptions (alt_combined_id, name, assmptn_id, is_multiyear, added_by, added_on) values (?,?,?,?,?,getdate())";
	public final static String ALT_CRE_ASS_COST = Misc.G_DO_ORACLE ? "insert into assumptions (cost_li_id, name, id, assmptn_id, is_multiyear, added_by, added_on) values (?,?,?,?,?,?,sysdate)" : "insert into assumptions (cost_li_id, name, id, assmptn_id, is_multiyear, added_by, added_on) values (?,?,?,?,?,?,getdate())";

	//THE DATA Queries must be exactly similar
	public final static String ALT_CRE_DAT_ASS = "insert into data (assum_id, value, year, err_def_type, val_scope, val_dur) values (?,?,?,?,?,?)";
	public final static String ALT_CRE_DAT_NPV = "insert into data (npv_id, value, year, err_def_type, val_scope, val_dur) values (?,?,?,?,?,?)";
	public final static String ALT_CRE_DAT_REV = "insert into data (rev_seg_id, value, year, err_def_type, val_scope, val_dur) values (?,?,?,?,?,?)";
	public final static String ALT_CRE_DAT_COST = "insert into data (cost_li_id, value, year, err_def_type, val_scope, val_dur) values (?,?,?,?,?,?)";
	public final static String ALT_CRE_DAT_FTE = "insert into data (fte_item_id, value, year, err_def_type, val_scope, val_dur) values (?,?,?,?,?,?)";

	public final static String ALT_CRE_DAT_ALT_MOD = "insert into data (alt_devcost_id, value, year, err_def_type, val_scope, val_dur) values (?,?,?,?,?,?)";
	public final static String ALT_CRE_DAT_FUND = "insert into data (fund_id, value, year, err_def_type, val_scope, val_dur) values (?,?,?,?,?,?)";

	public final static String ALT_CRE_DAT_FUNDFIRM = "insert into data (fund_id_for_firm, value, year, err_def_type, val_scope, val_dur) values (?,?,?,?,?,?)";

	//THE ANNOTATION QUeries must be exactly similar
	public final static String ALT_CRE_ANNOT_CLAIM = "insert into annotations (claim_id, id, added_on, comments, added_by, url) values (?,?,?,?,?,?)";
	public final static String ALT_CRE_ANNOT_MOD = "insert into annotations (alt_combined_id, id, added_on, comments, added_by, url) values (?,?,?,?,?,?)";
	public final static String ALT_CRE_ANNOT_ASSUM = "insert into annotations (assum_id, id, added_on, comments, added_by, url) values (?,?,?,?,?,?)";


	public final static String ALT_CRE_NPV = Misc.G_DO_ORACLE ? "insert into npv (id, npv_type, alt_combined_id, scen_id) values (?,?,?,?)"
                                                            : "insert into npv (npv_type, alt_combined_id, scen_id) values (?,?,?)";
	// public final static String ALT_CRE_SENSIT = "insert into sensitivities (id, low_val, high_val, assum_id, alt_combined_id) values (?,?,?,?,?)";
	//modified!!
	public final static String ALT_CRE_SENSIT = "insert into sensitivities (id, low_val, high_val, var_name, alt_combined_id) values (?,?,?,?,?)";

	//ERR Queries must be similar

	public final static String ALT_CRE_ERR_DATA = "insert into err_vals (data_id, value, err_percent) values (?,?,?)";
	public final static String ALT_CRE_ERR_MS = "insert into err_vals (ms_id, value, err_percent) values (?,?,?)";





	public final static String GET_PORT_TREE = " id, name, port_node_id, external_code, org_type, def_currency_calc, def_rep_currency_calc, default_date_format, default_currency_scale, default_group_threshold, country_code, str_field1, str_field2, consolidation_status, lhs_number, rhs_number from port_nodes plist where status=1 order by hier_level, full_name";

	public final static String GET_PORT_PRJ_NODES = "select projects.port_node_id, projects.id, projects.name from projects";



	//Queries for web pages - Whew !!
	//before implementing shallow copy of projects - btw this query is wrong looks for alt_dates.id = workspaces.id
	/*
	 public final static String GET_PRJ_INFO_FRONT_PAGE =  "select projects.id, pj_basics.id, projects.name, pj_basics.thera_ind, pj_basics.maj_fran, workspaces.id, "+
	 "workspaces.name, max(alt_dates.next_min_ms), pj_map_items.map_type, pj_map_items.isdefault, port_nodes.name, projects.user_given_id "+
	 "from projects, pj_basics, workspaces, alt_dates, alternatives, alternatives_alt_lists, pj_map_items, port_nodes  where "+
	 "(projects.id = workspaces.prj_id) and (pj_basics.wspace_id = workspaces.id) and (alt_dates.wspace_id (+) = workspaces.id) "+
	 "and (pj_map_items.wspace_id = workspaces.id) "+
	 "and (projects.port_node_id = port_nodes.id) "+
	 "and (pj_map_items.map_type in (1,4)) and (pj_map_items.isdefault = 1) "+
	 "group by projects.id, workspaces.id, pj_basics.id, projects.name, pj_basics.thera_ind, pj_basics.maj_fran, port_nodes.name, "+
	 "workspaces.name, pj_map_items.map_type, pj_map_items.isdefault, projects.user_given_id "+
	 "order by projects.id desc, workspaces.id asc, pj_map_items.map_type asc, pj_map_items.isdefault desc ";
	 */
	// and (pj_basics.wspace_id = workspaces.id) and (alt_dates.id (+) = workspaces.id) "+
	
	public final static String GET_PLANS = "select plans.id, plans.name from plans";
	public final static String CREATE_PROJECT = Misc.G_DO_ORACLE ? //rajeev 040808
     "insert into projects (id, name, port_node_id, user_given_id, status, template_id, updated_by, created_on, updated_on, created_by) values  (?,?,?,?,?,?,?,sysdate,sysdate,?)"
     :
     "insert into projects (name, port_node_id, user_given_id, status, template_id, updated_by, created_on, updated_on, created_by) values  (?,?,?,?,?,?,getDate(), getDate(),?)";
	public final static String UPDATE_PROJECT = "update projects set name=?, user_given_id=?, port_node_id=? where id = ?";
	public final static String UPDATE_PROJECT_STATE = "update projects set status=? where id = ?";
	public final static String CREATE_WORKSPACE = Misc.G_DO_ORACLE ? 
  "insert into workspaces (id, name, prj_id, plan_id) values (?,?,?,?)"
  :
  "insert into workspaces (name, prj_id, plan_id) values (?,?,?)";

	//WSPACE_ID NOT BEING USED below ..
	public final static String CREATE_PJ_BASIC = Misc.G_DO_ORACLE ?
  "insert into pj_basics (unmet_need, name, n_lf_ext, maj_fran, pj_basic_desc, thera_ind, treat_ty, id, "+
		"treat_mech, maj_cust_seg, source, disease, wspace_id, prj_id, str_field1, str_field2) values " +
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    :
  "insert into pj_basics (unmet_need, name, n_lf_ext, maj_fran, pj_basic_desc, thera_ind, treat_ty,  "+
		"treat_mech, maj_cust_seg, source, disease, wspace_id, prj_id, str_field1, str_field2) values " +
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"    ;
	public final static String CREATE_PJ_HIST = Misc.G_DO_ORACLE ? "insert into pj_hist (id, prj_id, on_date, pj_hist_by, comments, action_type, rel_wksp, rel_wksp2) values "+
		"(?,?,sysdate,?,?,?,?,?)" 
                                                               : "insert into pj_hist ( prj_id, on_date, pj_hist_by, comments, action_type, rel_wksp, rel_wksp2) values "+
		"(?,getdate(),?,?,?,?,?)";
	public final static String CREATE_WKSP_HIST = Misc.G_DO_ORACLE ? "insert into wksp_hist (id, wspace_id, on_date, wksp_hist_by, action_type, from_state, to_state, comments, ref_item_type, ref_item_id, alt_id, state_is_pln) values "+
		"(?,?,sysdate,?,?,?,?,?,?,?,?,?)" 
                                                                 : "insert into wksp_hist (wspace_id, on_date, wksp_hist_by, action_type, from_state, to_state, comments, ref_item_type, ref_item_id, alt_id, state_is_pln) values "+
		"(?,getdate(),?,?,?,?,?,?,?,?,?)";
	//WSPACE_ID is not being used
	public final static String CREATE_ALT_LIST = Misc.G_DO_ORACLE ? 
  "insert into alt_lists (id,wspace_id, prj_id, pj_co_id) values (?,?,?,?)"
  :
  "insert into alt_lists (wspace_id, prj_id, pj_co_id) values (?,?,?)";
  
	public final static String CREATE_PJ_MAP_ITEM = Misc.G_DO_ORACLE ? "insert into pj_map_items (id, wspace_id, date_created, isdefault, map_type, prj_id, pj_basic_id, alt_list_id) values "+
		"(?,?,sysdate,?,?,?,?,?)" 
                                                                   : "insert into pj_map_items (wspace_id, date_created, isdefault, map_type, prj_id, pj_basic_id, alt_list_id) values "+
		"(?,getdate(),?,?,?,?,?)"; //@@@
	public final static String UPDATE_PJ_MAP_ITEM_PJ_BASIC = "update pj_map_items set pj_basic_id = ? where wspace_id = ? and isdefault = 1";

	public final static String GET_PRJ_TOP_LINE_INFO = "select projects.id, projects.user_given_id, projects.name, projects.status, projects.port_node_id from projects where projects.id=?";
//public final static String GET_ALT_TOP_LINE_INFO = "select projects.id, projects.user_given_id, projects.name, alternatives.id, alternatives.name, template_id, prj_template_id from projects,alternatives where projects.id=? and alternatives.prj_id=*projects.id and alternatives.id=*?";
	public final static String GET_ALT_TOP_LINE_INFO = Misc.G_DO_ORACLE ? "select projects.id, projects.user_given_id, projects.name, alternatives.id, alternatives.name, template_id, prj_template_id from projects,alternatives where projects.id=? and alternatives.prj_id(+)=projects.id and alternatives.id(+)=?" : "select projects.id, projects.user_given_id, projects.name, alternatives.id, alternatives.name, template_id, prj_template_id from projects inner join alternatives on alternatives.prj_id=projects.id where projects.id=? and alternatives.prj_id=?";
	public final static String GET_CURR_PJ_MAP_ITEM = "select distinct id, pj_basic_id, alt_list_id, risk_header_id from pj_map_items where (wspace_id = ?) and (isdefault = 1) ";
	public final static String CREATE_ALTERNATIVE  = Misc.G_DO_ORACLE ?
  "insert into alternatives (id,name, prj_id, is_primary) values (?,?,?,?)"
  :
  "insert into alternatives (name, prj_id, is_primary) values (?,?,?)";
	public final static String ASSOCIATE_ALTERNATIVE_ALT_LIST = "insert into alternatives_alt_lists (alt_list_id, alt_id) values (?,?)";

	//WSPACE_ID no longet used in alt_basics
	public final static String CREATE_ALT_BASIC  = Misc.G_DO_ORACLE ? 
  "insert into alt_basics (id, wspace_id, alt_id, alt_risk, alt_basic_desc, mkt_stgy, patent_exp_date, revhandling) values (?,?,?,?,?,?,?,?)"
  :
  "insert into alt_basics (wspace_id, alt_id, alt_risk, alt_basic_desc, mkt_stgy, patent_exp_date, revhandling) values (?,?,?,?,?,?,?)";
	public final static String CREATE_ALT_MKT_MAPPING = "insert into alt_mkt_items (tar_mkt_typ, alt_basic_id) values (?,?)";
	public final static String CREATE_ALT_MOL_MAPPING = "insert into alt_mol_items (tar_mol_tar_mol_id, alt_basic_id) values (?,?)";
	public final static String CREATE_ALT_MAP_ITEM = Misc.G_DO_ORACLE ?
  "insert into alt_map_items (id, alt_id, wspace_id, map_type, isdefault, alt_basic_id, alt_profil_id, alt_date_id, alt_task_id, alt_model_id, alt_rating_id) values (?,?,?,?,?,?,?,?,?,?,?)"
  :
  "insert into alt_map_items (alt_id, wspace_id, map_type, isdefault, alt_basic_id, alt_profil_id, alt_date_id, alt_task_id, alt_model_id, alt_rating_id) values (?,?,?,?,?,?,?,?,?,?)";
	// public final static String UPDATE_ALT_MAP_ITEM_ALT_BASIC_ID = "update alt_map_items set alt_basic_id=? where wspace_id=? and map_type=4 and isdefault=1";

	//
	// public final static String GET_ALT_LIST = "select alternatives.id, alternatives.name from alternatives, alt_lists, alternatives_alt_lists where alt_lists.prj_id=? and alt_lists.wspace_id=? and alternatives_alt_lists.alt_list_id = alt_lists.id and alternatives.id = alternatives_alt_lists.alt_id";
	public final static String GET_ALT_LIST = "select distinct alternatives.id, alternatives.name, alternatives.is_primary from alternatives, alt_map_items, alt_basics where alt_map_items.wspace_id = ? and alt_map_items.isdefault=1 and alt_basics.id = alt_map_items.alt_basic_id and alternatives.id=alt_basics.alt_id and alternatives.is_primary in (0,1) order by is_primary desc";
	//kind of funking looking but helps in avoiding situations where an alternative is only defined in a particular workspace - will however need to rectify this bug



	//Borrowed from tips mart
	// public final static String CREATE_USER = "begin tpUserAdmin.createUser(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?); end;";
	public final static String DELETE_ATTACHMENTS =  "begin tpUtilities.deleteAttachments(?); end;";

	public Queries() {

	}

	public static void main(String[] args) {
		new Queries();
	}


	//MORE QUERIES FOR THE WEB
	public static final String SET_MAPPING_PJ_MAP_ITEMS = "update pj_map_items set map_type = ?, isdefault = ? where wspace_id = ? and ((? is NULL) or map_type = ?) and isdefault = 1";
	public static final String SET_MAPPING_ALT_MAP_ITEMS = "update alt_map_items set map_type = ?, isdefault = ? where wspace_id = ? and ((? is NULL) or map_type = ?) and isdefault = 1";
	public static final String SET_MAPPING_MEASURE_ITEMS = "update measure_map_items set map_type=?, isdefault = ? where wspace_id = ? and ((? is NULL) or map_type = ?) and isdefault = 1";

	//newworkspace,
	public static final String COPYMAP_EXIST_PRJ = "select pj_map_items.id from pj_map_items where wspace_id = ? and isdefault=1";
	//newmap, newworkspace, old
	public static final String COPYMAP_INSERT_PRJ = Misc.G_DO_ORACLE ? "insert into pj_map_items (date_created, id, isdefault, map_type, pj_basic_id, prj_id, wspace_id, alt_list_id) "+
		"(select sysdate, seq_pj_map_items.nextval, 1, ?, temp.pjbid, temp.prj_id, ?, temp.altlid from "+
		"          (select distinct  pj_map_items.pj_basic_id pjbid, pj_map_items.prj_id, pj_map_items.alt_list_id altlid "+
		"           from pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault=1) temp "+
		") " : 
                                                                     "insert into pj_map_items (date_created, isdefault, map_type, pj_basic_id, prj_id, wspace_id, alt_list_id) "+
		"(select getdate(), 1, ?, temp.pjbid, temp.prj_id, ?, temp.altlid from "+
		"          (select distinct  pj_map_items.pj_basic_id pjbid, pj_map_items.prj_id, pj_map_items.alt_list_id altlid "+
		"           from pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault=1) temp "+
		") ";
	//newmap, old, new
	public static final String COPYMAP_UPDATE_PRJ = Misc.G_DO_ORACLE ? "update pj_map_items upd set (date_created, map_type, pj_basic_id, alt_list_id) "+
		"    = (select sysdate, ?, cpf.pj_basic_id, cpf.alt_list_id from "+
		"pj_map_items cpf where cpf.wspace_id = ? and cpf.isdefault = 1) where upd.wspace_id = ?  and upd.isdefault=1" : "update pj_map_items upd set (date_created, map_type, pj_basic_id, alt_list_id) "+
		"    = (select getdate(), ?, cpf.pj_basic_id, cpf.alt_list_id from "+
		"pj_map_items cpf where cpf.wspace_id = ? and cpf.isdefault = 1) where upd.wspace_id = ?  and upd.isdefault=1";

	//copy map - alt
	//new, old
	public static final String COPYMAP_CLEAN_ALT = Misc.G_DO_ORACLE ? "delete from alt_map_items upd where wspace_id = ? and not(Exists(select 1 from alt_map_items where wspace_id = ? and isdefault = 1 and upd.alt_id = alt_map_items.alt_id)) " : "delete from alt_map_items FROM alt_map_items upd where wspace_id = ? and not(Exists(select 1 from alt_map_items where wspace_id = ? and isdefault = 1 and upd.alt_id = alt_map_items.alt_id)) ";
	//newmap, new, old, new,old
	public static final String COPYMAP_INSERT_ALT = Misc.G_DO_ORACLE ? "insert into alt_map_items (id, isdefault, map_type, wspace_id, alt_id, alt_date_id, alt_model_id, alt_task_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id) " +
		"(select seq_alt_map_items.nextval, 1, ?, ?, temp.alt_id, temp.dateid, temp.modelid, temp.taskid, temp.basicid, temp.profilid, temp.alt_fte_id, temp.alt_devcost_id, temp.alt_rev_id, temp.alt_opcost_id, temp.alt_combined_id, temp.alt_work_id, temp.alt_rating_id from "+
		"         (select distinct alt_map_items.alt_id alt_id, alt_map_items.alt_date_id dateid, alt_map_items.alt_model_id modelid, alt_map_items.alt_task_id taskid, alt_map_items.alt_basic_id basicid, alt_map_items.alt_profil_id profilid, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id "+
		"         from alt_map_items, (select alt_id from alt_map_items where wspace_id = ? and isdefault = 1 "+
		"                              minus "+
		"                              select alt_id from alt_map_items where wspace_id = ? and isdefault = 1) altList "+
		"         where alt_map_items.wspace_id = ? "+
		"         and alt_map_items.isdefault = 1 and alt_map_items.alt_id = altList.alt_id) temp "+
		") " 
                                                                   : "insert into alt_map_items (isdefault, map_type, wspace_id, alt_id, alt_date_id, alt_model_id, alt_task_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id) " +
		"(select 1, ?, ?, temp.alt_id, temp.dateid, temp.modelid, temp.taskid, temp.basicid, temp.profilid, temp.alt_fte_id, temp.alt_devcost_id, temp.alt_rev_id, temp.alt_opcost_id, temp.alt_combined_id, temp.alt_work_id, temp.alt_rating_id from "+
		"         (select distinct alt_map_items.alt_id alt_id, alt_map_items.alt_date_id dateid, alt_map_items.alt_model_id modelid, alt_map_items.alt_task_id taskid, alt_map_items.alt_basic_id basicid, alt_map_items.alt_profil_id profilid, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id "+
		"         from alt_map_items, (select alt_id from alt_map_items where wspace_id = ? and isdefault = 1 "+
		"                              and Not Exists( "+
		"                              select alt_id from alt_map_items where wspace_id = ? and isdefault = 1)) altList "+
		"         where alt_map_items.wspace_id = ? "+
		"         and alt_map_items.isdefault = 1 and alt_map_items.alt_id = altList.alt_id) temp "+
		") ";
	//map, old, new
//MS SQL
	//public static final String COPYMAP_UPDATE_ALT = "update alt_map_items upd set (map_type, alt_date_id, alt_model_id, alt_task_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id) "+
	//	" = (select ?, alt_date_id, alt_model_id, alt_task_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id from alt_map_items "+
	//	"where wspace_id = ? and upd.alt_id = alt_map_items.alt_id and isdefault=1) where wspace_id = ? and isdefault = 1 ";
  public static final String COPYMAP_UPDATE_ALT = Misc.G_DO_ORACLE ? "update alt_map_items upd set (map_type, alt_date_id, alt_model_id, alt_task_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id) "+
		" = (select ?, alt_date_id, alt_model_id, alt_task_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id from alt_map_items "+
		"where wspace_id = ? and upd.alt_id = alt_map_items.alt_id and isdefault=1) where wspace_id = ? and isdefault = 1 " :
"UPDATE alt_map_items set map_type = ?, alt_date_id  = upd.alt_date_id, alt_model_id = upd.alt_model_id,alt_task_id = upd.alt_task_id, alt_basic_id = upd.alt_basic_id, alt_profil_id = upd.alt_profil_id, alt_fte_id = upd.alt_fte_id, alt_devcost_id = upd.alt_devcost_id, alt_rev_id = upd.alt_rev_id, alt_opcost_id = upd.alt_opcost_id, alt_combined_id = upd.alt_combined_id, alt_work_id = upd.alt_work_id, alt_rating_id = upd.alt_rating_id FROM alt_map_items upd WHERE alt_id = upd.alt_id AND upd.wspace_id = ? AND upd.isdefault=1 AND wspace_id = ? AND isdefault = 1 ";
//
//MSSQL
	//MEASURE
	//new, old
	public static final String COPYMAP_CLEAN_MEASURE = Misc.G_DO_ORACLE ? "delete from measure_map_items upd where wspace_id = ? and not(Exists(select 1 from measure_map_items where wspace_id = ? and isdefault = 1 and upd.alt_id = measure_map_items.alt_id and upd.measure_id = measure_map_items.measure_id)) " : "DELETE FROM measure_map_items FROM measure_map_items upd WHERE wspace_id = ? AND NOT (EXISTS(SELECT 1 FROM measure_map_items WHERE wspace_id = ? and isdefault = 1 AND upd.alt_id = measure_map_items.alt_id AND upd.measure_id = measure_map_items.measure_id)) ";
	//map, new, old,new, old
	public static final String COPYMAP_INSERT_MEASURE = Misc.G_DO_ORACLE ? "insert into measure_map_items (id, isdefault, map_type, wspace_id, alt_id, measure_id, alt_measure_id) "+
		"(select seq_measure_map_items.nextval, 1, ?, ?, temp.alt_id, temp.measure_id, temp.alt_measure_id  from "+
		"         (select distinct measure_map_items.alt_id alt_id, measure_map_items.measure_id measure_id, measure_map_items.alt_measure_id alt_measure_id "+
		"         from measure_map_items, "+
		"         (select measure_id, alt_id from measure_map_items where measure_map_items.wspace_id = ? and isdefault = 1 "+
		"          minus select measure_id, alt_id from measure_map_items where measure_map_items.wspace_id = ? "+
		"         ) measureAltList "+
		"         where measure_map_items.wspace_id = ? "+
		"         and measure_map_items.isdefault = 1 and measureAltList.alt_id = measure_map_items.alt_id and measureAltList.measure_id = measure_map_items.measure_id) temp "+
		")" 
                                                                      : "insert into measure_map_items (isdefault, map_type, wspace_id, alt_id, measure_id, alt_measure_id) "+
		"(select 1, ?, ?, temp.alt_id, temp.measure_id, temp.alt_measure_id  from "+
		"         (select distinct measure_map_items.alt_id alt_id, measure_map_items.measure_id measure_id, measure_map_items.alt_measure_id alt_measure_id "+
		"         from measure_map_items, "+
		"         (select measure_id, alt_id from measure_map_items where measure_map_items.wspace_id = ? and isdefault = 1 "+
		"          and Not Exists(select measure_id, alt_id from measure_map_items where measure_map_items.wspace_id = ? )"+
		"         ) measureAltList "+
		"         where measure_map_items.wspace_id = ? "+
		"         and measure_map_items.isdefault = 1 and measureAltList.alt_id = measure_map_items.alt_id and measureAltList.measure_id = measure_map_items.measure_id) temp "+
		")";
	//map, old, new
	public static final String COPYMAP_UPDATE_MEASURE = Misc.G_DO_ORACLE ? "update measure_map_items upd set (map_type, alt_measure_id) "+
		"= (select ?, max(alt_measure_id) from measure_map_items "+
		"where wspace_id = ? and upd.alt_id = measure_map_items.alt_id and upd.measure_id = measure_map_items.measure_id and isdefault=1) where wspace_id = ? and isdefault = 1 " : "UPDATE measure_map_items SET map_type = ?, alt_measure_id = (SELECT MAX(alt_measure_id) FROM measure_map_items where wspace_id = ? AND upd.alt_id = measure_map_items.alt_id AND upd.measure_id = measure_map_items.measure_id AND isdefault=1) FROM measure_map_items upd WHERE wspace_id = ? AND isdefault = 1 ";



	//    public static final String NEW_COPY_CREATE_PJ_MAP_ITEMS =
	//       "insert into pj_map_items (date_created, id, isdefault, map_type, pj_basic_id, prj_id, wspace_id, alt_list_id) "+
	//         "(select sysdate, seq_pj_map_items.nextval, 1, ?, temp.pjbid, temp.prj_id, ?, temp.altlid from "+
	//                    "(select distinct  pj_map_items.pj_basic_id pjbid, pj_map_items.prj_id, pj_map_items.alt_list_id altlid "+
	//                    "from pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault=1) temp "+
	//         ")";


	//    public static final String NEW_COPY_CREATE_ALT_MAP_ITEMS =
	//       "insert into alt_map_items (id, isdefault, map_type, wspace_id, alt_id, alt_date_id, alt_model_id, alt_task_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id) "+
	//         "(select seq_alt_map_items.nextval, 1, ?, ?, temp.alt_id, temp.dateid, temp.modelid, temp.taskid, temp.basicid, temp.profilid, temp.alt_fte_id, temp.alt_devcost_id, temp.alt_rev_id, temp.alt_opcost_id, temp.alt_combined_id from "+
	//                   "(select distinct alt_map_items.alt_id alt_id, alt_map_items.alt_date_id dateid, alt_map_items.alt_model_id modelid, alt_map_items.alt_task_id taskid, alt_map_items.alt_basic_id basicid, alt_map_items.alt_profil_id profilid, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id "+
	//                   "from alt_map_items "+
	//                   "where alt_map_items.wspace_id = ? "+
	//                   "and alt_map_items.isdefault = 1) temp "+
	//          ")";

	//    public static final String NEW_COPY_CREATE_MEASURE_MAP_ITEMS =
	//       "insert into measure_map_items (id, isdefault, map_type, wspace_id, alt_id, measure_id, alt_measure_id) "+
	//         "(select seq_measure_map_items.nextval, 1, ?, ?, temp.alt_id, temp.measure_id, temp.alt_measure_id  from "+
	//                   "(select distinct measure_map_items.alt_id alt_id, measure_map_items.measure_id measure_id, measure_map_items.alt_measure_id alt_measure_id "+
	//                   "from measure_map_items "+
	//                   "where measure_map_items.wspace_id = ? "+
	//                   "and measure_map_items.isdefault = 1) temp "+
	//          ")";



	public static final String GET_PROJECT_TEMPLATE = "select template_id from projects where id=?";
	public static final String GET_DEFAULT_PRJ_WORKSPACE = "select wspace_id from pj_map_items where prj_id = ? and (map_type = 1 or map_type = 4) and (isdefault=1) order by map_type asc, date_created desc";
	public static final String GET_PROJECT_DETAIL = "select distinct projects.user_given_id, projects.name, workspaces.plan_id,  pj_basics.pj_basic_desc,pj_basics.maj_fran, pj_basics. maj_cust_seg, "+ //TOVERIFY
		"pj_basics.n_lf_ext, pj_basics.treat_ty, pj_basics.treat_mech, pj_basics.disease, pj_basics.thera_ind, pj_basics.source, pj_basics.unmet_need, pj_basics.base_drug, projects.status, projects.port_node_id, pj_basics.str_field1, pj_basics.str_field2 "+
		"from projects, pj_basics, workspaces, pj_map_items where "+
		"projects.id = ? and workspaces.id = ? and pj_map_items.wspace_id = workspaces.id and pj_map_items.isdefault = 1 and pj_basics.id = pj_basic_id"; //TOVERIFY IF CORRECT
	public static final String GET_PROJECT_DETAIL_VER_SPECIFIC = "select projects.user_given_id, projects.name, workspaces.plan_id,  pj_basics.pj_basic_desc,pj_basics.maj_fran, pj_basics. maj_cust_seg, "+
		"pj_basics.n_lf_ext, pj_basics.treat_ty, pj_basics.treat_mech, pj_basics.disease, pj_basics.thera_ind, pj_basics.source, pj_basics.unmet_need, pj_basics.base_drug, projects.status, projects.port_node_id, pj_basics.str_field1, pj_basics.str_field2 "+
		"from projects, pj_basics, workspaces where "+
		"projects.id = ? and workspaces.id = ? and pj_basics.id = ?";

	//doesn't return a valid row if there are no alternatives - which however is needed
	public static final String GET_ALT_SUMMARY = "select distinct projects.name, projects.user_given_id, alternatives.name, alt_basics.alt_basic_desc, alt_basics.mkt_stgy, "+
		"alt_basics.patent_exp_date, alt_basics.revhandling, alt_basics.id "+
		"from projects, alternatives,  alt_basics,alt_map_items, workspaces "+
		"where alternatives.id = ? and workspaces.id=?  and workspaces.prj_id = projects.id and "+
		"alt_map_items.wspace_id = workspaces.id and alt_map_items.alt_id = alternatives.id and alt_map_items.isdefault=1 and "+
		"alt_basics.id = alt_map_items.alt_basic_id";

	public static final String GET_ALT_SUMMARY_VER = "select projects.name, projects.user_given_id, alternatives.name, alt_basics.alt_basic_desc, alt_basics.mkt_stgy, "+
		"alt_basics.patent_exp_date, alt_basics.revhandling, alt_basics.id "+
		"from projects, alternatives, alt_basics "+
		"where alt_basics.id = ? and alternatives.id = alt_basics.alt_id and projects.id = alternatives.prj_id ";

	public static final String GET_TARGET_MARKET_FOR_ALT = "select tar_mkt_typ from alt_mkt_items where alt_basic_id = ? order by tar_mkt_typ asc";

	//this is still buggy - basically we need to first create molecules specific for the project and select from that
  //NOTUSED
	public final static String GET_MOLECULES_LIST_FOR_ALT = Misc.G_DO_ORACLE ? "select tar_mol_id, name, alt_basic_id from tar_molecules, alt_mol_items where tar_mol_id = tar_mol_tar_mol_id(+) and alt_basic_id(+) = ?" :  "select tar_mol_id, name, alt_basic_id from tar_molecules, alt_mol_items where tar_mol_id *= tar_mol_tar_mol_id and alt_basic_id =* ?";

	public final static String UPDATE_ALT_INFO = "update alternatives set name=? where id=?";
	public final static String UPDATE_ALT_MAP_ITEM_BASIC = "update alt_map_items set alt_basic_id=? where wspace_id=? and alt_id=? and isdefault=1";

	//FOR ALTERNATIVE_PROFILE - reading stuff
	public final static String GET_PROFILE_CLAIMS = "select distinct claim_type.id, claim_type, description, achiev_rating, pref_rating, user_given_id, claim_type.alt_profil_id from "+
		"claim_type, alt_map_items where alt_map_items.wspace_id = ? and alt_id = ? and isdefault=1 and alt_map_items.alt_profil_id = claim_type.alt_profil_id and claim_type = ? order by claim_type.id";
	public final static String GET_PROFILE_CLAIMS_VER = "select claim_type.id, claim_type, description, achiev_rating, pref_rating, user_given_id, claim_type.alt_profil_id from "+
		"claim_type where claim_type.alt_profil_id = ? and claim_type = ? order by claim_type.id";

	public final static String GET_PROF_OUTCOMES =  "select distinct prof_outcomes.id, profe_case_ty, description, probability from "+
		"prof_outcomes, alt_map_items where alt_map_items.wspace_id = ? and alt_id = ? and isdefault=1 and alt_map_items.alt_profil_id = prof_outcomes.alt_profil_id order by profe_case_ty asc";
	public final static String GET_PROF_OUTCOMES_VER =  "select distinct prof_outcomes.id, profe_case_ty, description, probability from "+
		"prof_outcomes where prof_outcomes.alt_profil_id = ? order by profe_case_ty asc";

	public final static String GET_OUTCOMES_FOR_CLAIMS = "select prof_outcomes.profe_case_ty  from  prof_claim_items, prof_outcomes where prof_claim_items.claim_id = ? and prof_outcomes.id = prof_claim_items.pro_outc_id order by profe_case_ty";
	//Updating stuff
	//WSPACE_ID no longer used in create_alt_profile
	public final static String CREATE_ALT_PROFILE = Misc.G_DO_ORACLE ?
  "insert into alt_profiles (id, alt_id, wspace_id, alt_profil_desc, name) values (?,?,?,?,?)"
  :
  "insert into alt_profiles (alt_id, wspace_id, alt_profil_desc, name) values (?,?,?,?)";
	public final static String UPDATE_ALT_MAP_PROFILE = "update alt_map_items set alt_profil_id=? where wspace_id = ? and alt_id = ? and isdefault = 1";
	public final static String INSERT_PROF_OUTCOMES = Misc.G_DO_ORACLE ?
  "insert into prof_outcomes (id, alt_profil_id, probability, profe_case_ty, description) values (?,?,?,?,?)"
  :
  "insert into prof_outcomes (alt_profil_id, probability, profe_case_ty, description) values (?,?,?,?)";
	public final static String UPDATE_PROF_OUTCOMES = 
  "update  prof_outcomes set probability=?, description=? where alt_profil_id = ? and profe_case_ty = ?";
  
	public final static String INSERT_CLAIM_TYPE = Misc.G_DO_ORACLE ? 
  "insert into claim_type (id, claim_type, description, achiev_rating, pref_rating, user_given_id, alt_profil_id) values (?,?,?,?,?,?,?)"
  :
  "insert into claim_type (claim_type, description, achiev_rating, pref_rating, user_given_id, alt_profil_id) values (?,?,?,?,?,?)";
	public final static String INSERT_CLAIM_OUTCOMES_ITEM = "insert into prof_claim_items (pro_outc_id, claim_id)  (select prof_outcomes.id,? from prof_outcomes where profe_case_ty=? and alt_profil_id= ?)";


	//TASK RELATED STUFF
	public final static String GET_TASK_LIST = "select distinct tasks.id, tasks.name, tasks.task_id, tasks.user_given_id, tasks.import_task_id, tasks.task_cat_id from tasks, alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and isdefault = 1 and tasks.alt_task_id = alt_map_items.alt_task_id order by tasks.task_id asc";
	public final static String GET_PRED_LIST = "select distinct tasks.task_id, pred_map_delay from pred_maps,tasks where task_1_id_predof = ? and tasks.id = task_1_id";
	public final static String GET_DELIVERY_LIST = "select del_approaches.id, not_start_bef, start_date, end_date, duration, name, task_1_id_reco from del_approaches where  task_1_id = ?";
	public final static String GET_RESOURCE_REQS = "select qty,uom,fte_head_id, cost_cent_id,del_appr_id from task_reqs where del_appr_id = ?";

	//WSPACE_ID NO LONGER USED
	public final static String INSERT_ALT_TASK = "insert into alt_tasks (id, alt_id, wspace_id) values (?,?,?)";
	public final static String UPDATE_ALT_MAP_ITEM_TASK = "update alt_map_items set alt_task_id=? where wspace_id=? and alt_id=? and isdefault=1";
	public final static String INSERT_TASK_LIST = "insert into tasks (id, name, task_id, user_given_id, import_task_id, task_cat_id, alt_task_id) values (?,?,?,?,?,?,?)";
	public final static String INSERT_PRED_LIST = "insert into pred_maps (task_1_id, task_1_id_predof, pred_map_delay) "+
		" (select tasks.id, ?, ? from tasks where tasks.alt_task_id=? and tasks.task_id = ?)";
	public final static String INSERT_DELIVERY_LIST = "insert into  del_approaches (id, not_start_bef, start_date, end_date, duration, name, task_1_id_reco, task_1_id) values (?,?,?,?,?,?,?,?)";
	public final static String INSERT_RESOURCE_REQS = "insert into task_reqs (qty, uom, fte_head_id, cost_cent_id, del_appr_id, id) values (?,?,?,?,?,?)";
	//THESE TWO NEED TO BE EXACTLY LIKE EACH OTHER - used in taskHelper and Constraint Helper
	public final static String GET_COST_CENTERS = "select id, name from cost_centers order by id";
	public final static String GET_FTE_HEADS = "select id, name from fte_heads order by id";

	public final static String GET_MILE_STONES_LIST = "select distinct milestones.id, milestones.mstn_id, milestones.err_def_approach, start_date, finish_dt, mstn_status, succ_prob, comments, target_market, ms_status, variance  "+ //TOVERIFY
		" from milestones, alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and isdefault = 1 and milestones.alt_date_id = alt_map_items.alt_date_id order by target_market asc, milestones.mstn_id asc";
	public final static String GET_ERR_VALS_MS = "select err_percent, value from err_vals where ms_id=? order by err_percent asc";
	public final static String GET_MILE_STONES_LIST_VER = "select milestones.id, milestones.mstn_id, milestones.err_def_approach, start_date, finish_dt, mstn_status, succ_prob, comments, target_market, ms_status, variance  "+ //TOVERIFY
		" from milestones where  milestones.alt_date_id = ? order by target_market asc, milestones.mstn_id asc";


	//WSPACE_ID not USED
	public final static String INSERT_ALT_DATE = Misc.G_DO_ORACLE ? 
  "insert into alt_dates (id, alt_id, wspace_id) values (?,?,?)"
  :
  "insert into alt_dates (alt_id, wspace_id) values (?,?)";
  
	public final static String UPDATE_ALT_MAP_ITEM_DATE = "update alt_map_items set alt_date_id=? where wspace_id=? and alt_id=? and isdefault=1";
	public final static String INSERT_MILESTONE_LIST = Misc.G_DO_ORACLE ? "insert into milestones (id, mstn_id, start_date, finish_dt, duration, succ_prob, alt_date_id, err_def_approach, comments, cumm_prob, prev_cumm_prob, target_market, ms_status, variance) values (?,?,?,?,?,?,?,?,?,?, ?, ?, ?, ?)"
                                                                      : "insert into milestones (mstn_id, start_date, finish_dt, duration, succ_prob, alt_date_id, err_def_approach, comments, cumm_prob, prev_cumm_prob, target_market, ms_status, variance) values (?,?,?,?,?,?,?,?,?,?, ?, ?, ?)";
	public final static String INSERT_ERR_VAL_MS = "insert into err_vals (err_percent, value, ms_id) "+
		" (select ?,?,milestones.id from milestones where milestones.alt_date_id=? and milestones.mstn_id = ?)";

	//PORTFOLIO LEVEL STUFF
	public final static String GET_PORT_NODE_INFO = "select id, name, port_node_desc from port_nodes where id=?";
	//  public final static String GET_PORT_WORKSPACE = "select name, id, map_type, isdefault, port_wksp_desc from port_wksps where (port_wksp_id is null or port_node_id = 1) and (isdefault = 1)  and (port_wksps.is_for_prj_dw = 0)";
	public final static String GET_PORT_WORKSPACE = "select name, id, map_type, isdefault, port_wksp_desc from port_wksps where (port_wksp_id is null or port_node_id = 1) and (isdefault = 1)  and (port_wksps.is_for_prj_dw = 0) "+
		" and (map_type in (1,4) or id = ?)";

	public final static String GET_PORT_SPECIFIC_WORKSPACE = "select id from port_wksps where port_node_id=? and port_wksp_id = ?";
	public final static String CREATE_PORT_SPECIFIC_WORKSPACE = Misc.G_DO_ORACLE ? "insert into port_wksps (id, port_wksp_id, port_node_id,  is_for_prj_dw) values (?,?,?,0)"
                                                                               : "insert into port_wksps (port_wksp_id, port_node_id,  is_for_prj_dw) values (?,?,0)";

	//these two need to be exactly alike - modified instead we don't have versioning over it
	// public final static String GET_BUDGET_CONSTRAINT = "select cost_cent_id, getFiscalYear(year,?) as fyear, getQ1Q2LikeFiscalQuarter(year,?) as fquarter, penalty_calc_type, target, low, high, cnst_calc_as, penalty, penalty_range from " +
	//  " cnst_vals, constraints,port_wksps where port_wksps.port_node_id = ? and port_wksps.port_wksp_id=?  and (port_wksps.is_for_prj_dw = 0) and constraints.port_wksp_id = port_wksps.id and  cnst_vals.cnstrt_id = constraints.id and year >= ?  order by fyear asc, cost_cent_id asc, fquarter asc ";
	// public final static String GET_FTE_CONSTRAINT = "select fte_head_id, getFiscalYear(year,?) as fyear, getQ1Q2LikeFiscalQuarter(year,?) as fquarter, penalty_calc_type, target, low, high, cnst_calc_as, penalty, penalty_range from " +
	//  " cnst_vals, constraints,port_wksps where port_wksps.port_node_id = ? and port_wksps.port_wksp_id=?  and (port_wksps.is_for_prj_dw = 0)and constraints.port_wksp_id = port_wksps.id and  cnst_vals.cnstrt_id = constraints.id and year >= ?  order by fyear asc, fte_head_id asc, fquarter asc ";

	public final static String GET_BUDGET_CONSTRAINT = Misc.G_DO_ORACLE ? "select cost_cent_id, getFiscalYear(qtr_1900,?) as fyear, getQ1Q2LikeFiscalQuarter(qtr_1900,?) as fquarter, penalty_calc_type, target, low, high, cnst_calc_as, penalty, penalty_range, dept_id from " +
		" cnst_vals, constraints, time_dims where constraints.port_node_id = ?  and  cnst_vals.cnstrt_id = constraints.id and qtr_1900 >= ? and constraints.cost_cent_id is not null and time_dims.val = cnst_vals.year order by fyear asc, cost_cent_id asc, fquarter asc " :"select cost_cent_id, intelli.getFiscalYear(qtr_1900,?) as fyear, intelli.getQ1Q2LikeFiscalQuarter(qtr_1900,?) as fquarter, penalty_calc_type, target, low, high, cnst_calc_as, penalty, penalty_range, dept_id from " +
		" cnst_vals, constraints, time_dims where constraints.port_node_id = ?  and  cnst_vals.cnstrt_id = constraints.id and qtr_1900 >= ? and constraints.cost_cent_id is not null and time_dims.val = cnst_vals.year order by fyear asc, cost_cent_id asc, fquarter asc ";
	
  public final static String GET_FTE_CONSTRAINT = Misc.G_DO_ORACLE ? "select fte_head_id, getFiscalYear(qtr_1900,?) as fyear, getQ1Q2LikeFiscalQuarter(qtr_1900,?) as fquarter, penalty_calc_type, target, low, high, cnst_calc_as, penalty, penalty_range, dept_id from " +
		" cnst_vals, constraints, time_dims where constraints.port_node_id = ?  and  cnst_vals.cnstrt_id = constraints.id and qtr_1900 >= ? and constraints.fte_head_id is not null and time_dims.val = cnst_vals.year order by fyear asc, fte_head_id asc, fquarter asc " :"select fte_head_id, intelli.getFiscalYear(qtr_1900,?) as fyear, intelli.getQ1Q2LikeFiscalQuarter(qtr_1900,?) as fquarter, penalty_calc_type, target, low, high, cnst_calc_as, penalty, penalty_range, dept_id from " +
		" cnst_vals, constraints, time_dims where constraints.port_node_id = ?  and  cnst_vals.cnstrt_id = constraints.id and qtr_1900 >= ? and constraints.fte_head_id is not null and time_dims.val = cnst_vals.year order by fyear asc, fte_head_id asc, fquarter asc ";

	//modified from earlier where port_wksp_id instead of port_node_id
	//THESE two must be exactly be similar
	public final static String DEL_BUDGET_CONSTRAINT = "delete from constraints where cost_cent_id is not null and port_node_id = ?";
	public final static String DEL_FTE_CONSTRAINT = "delete from constraints where fte_head_id is not null and port_node_id = ?";

	//modified from earlier where port_wksp_id instead of port_node_id
	//These two must be exactly similar
	public final static String INSERT_BUDGET_CONSTRAINT = Misc.G_DO_ORACLE ? "insert into constraints (id, cost_cent_id, port_node_id, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?,?)" 
                                                                         : "insert into constraints (cost_cent_id, port_node_id, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?)" ;
	public final static String INSERT_FTE_CONSTRAINT = Misc.G_DO_ORACLE ? "insert into constraints (id, fte_head_id, port_node_id, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?,?)"
                                                                      : "insert into constraints (fte_head_id, port_node_id, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?)";

	public final static String INSERT_CNST_VALS = Misc.G_DO_ORACLE ? "insert into cnst_vals (cnst_calc_as, target, low, high, penalty_calc_type, penalty, penalty_range, year, cnstrt_id, id, val_scope) " +
		" (select ?,?,?,?,?,?,?,?,?, seq_cnstrt_vals.nextval, ? from dual)" 
                                                                 : "insert into cnst_vals (cnst_calc_as, target, low, high, penalty_calc_type, penalty, penalty_range, year, cnstrt_id, val_scope) " +
		" (select ?,?,?,?,?,?,?,?,?,? from dual)";

	//For PORTHELPER
	//TODO - CREATE_PORT_WORKSPACE is buggy assumes that port folio == 1 is the top level port
     //G_WATSON_031507_ROLLBACK
	public final static String CREATE_PORT_WORKSPACE = Misc.G_DO_ORACLE ?
  "insert into port_wksps (id, name, port_wksp_desc, plan_id, map_type, isdefault,port_node_id, is_for_prj_dw, cre_for_port_node_id, created_on) values (?,?,?,?,?,1,1,?,?, sysdate)" 
: "insert into port_wksps (name, port_wksp_desc, plan_id, map_type, isdefault,port_node_id, is_for_prj_dw, cre_for_port_node_id, created_on) values (?,?,?,?,1,1,?,?, getdate())";
//     public final static String CREATE_PORT_WORKSPACE = "insert into port_wksps (id, name, port_wksp_desc, plan_id, map_type, isdefault,port_node_id, is_for_prj_dw, cre_for_port_node_id) values (?,?,?,?,?,1,1,?,?)";

	public final static String CREATE_LABEL = Misc.G_DO_ORACLE ? "insert into labels (id, name, wspace_id, is_sys_generated, pj_map_id, description, created_by, created_on) values    (?,?,?,?,?,?,?,sysdate)" 
                                                             : "insert into labels (name, wspace_id, is_sys_generated, pj_map_id, description, created_by, created_on) values    (?,?,?,?,?,?,getdate())";

	//this is a
	public final static String CREATE_PJ_MAP_ITEM_FOR_LAB = Misc.G_DO_ORACLE ? "insert into pj_map_items (id, date_created, isdefault, map_type, prj_id, wspace_id, pj_basic_id, alt_list_id) " +
		"(select distinct ?,sysdate, 0, 4, pj_map_items.prj_id, ?, pj_map_items.pj_basic_id, pj_map_items.alt_list_id from pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault = 1)" 
                                                                           : "insert into pj_map_items (date_created, isdefault, map_type, prj_id, wspace_id, pj_basic_id, alt_list_id) " +
		"(select distinct getdate(), 0, 4, pj_map_items.prj_id, ?, pj_map_items.pj_basic_id, pj_map_items.alt_list_id from pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault = 1)";

	public final static String CREATE_ALT_MAP_ITEM_FOR_LAB = Misc.G_DO_ORACLE ?  "insert into alt_map_items (map_type, isdefault, label_id, id, alt_date_id, alt_model_id, alt_id, alt_task_id, wspace_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_rev_id, alt_devcost_id, alt_opcost_id, alt_combined_id, alt_rating_id, alt_work_id) "+
		" (select 4, 0, ?, seq_alt_map_items.nextval, temp.alt_date_id, temp.alt_model_id, temp.alt_id, temp.alt_task_id, temp.wspace_id, temp.alt_basic_id, temp.alt_profil_id, temp.alt_fte_id, temp.alt_rev_id, temp.alt_devcost_id, temp.alt_opcost_id, temp.alt_combined_id, alt_rating_id, alt_work_id from "+
		"(select distinct  alt_date_id, alt_model_id, alt_id, alt_task_id, wspace_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_rev_id, alt_devcost_id, alt_opcost_id, alt_combined_id, alt_rating_id, alt_work_id from alt_map_items where wspace_id = ? and isdefault = 1) temp "+
		" )" 
                                                                            : "insert into alt_map_items (map_type, isdefault, label_id, alt_date_id, alt_model_id, alt_id, alt_task_id, wspace_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_rev_id, alt_devcost_id, alt_opcost_id, alt_combined_id, alt_rating_id, alt_work_id) "+
		" (select 4, 0, ?,  temp.alt_date_id, temp.alt_model_id, temp.alt_id, temp.alt_task_id, temp.wspace_id, temp.alt_basic_id, temp.alt_profil_id, temp.alt_fte_id, temp.alt_rev_id, temp.alt_devcost_id, temp.alt_opcost_id, temp.alt_combined_id, alt_rating_id, alt_work_id from "+
		"(select distinct  alt_date_id, alt_model_id, alt_id, alt_task_id, wspace_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_rev_id, alt_devcost_id, alt_opcost_id, alt_combined_id, alt_rating_id, alt_work_id from alt_map_items where wspace_id = ? and isdefault = 1) temp "+
		" )";

	public final static String CREATE_MEASURE_MAP_ITEM_FOR_LAB = Misc.G_DO_ORACLE ? "insert into measure_map_items (map_type, isdefault, label_id, id,  alt_id, wspace_id, alt_measure_id, measure_id) "+
		" (select 4, 0, ?, seq_measure_map_items.nextval, temp.alt_id,  temp.wspace_id, temp.alt_measure_id, temp.measure_id from "+
		"(select distinct  alt_id, wspace_id, alt_measure_id, measure_id from measure_map_items where wspace_id = ? and isdefault = 1) temp "+
		" )" 
                                                                                : "insert into measure_map_items (map_type, isdefault, label_id,  alt_id, wspace_id, alt_measure_id, measure_id) "+
		" (select 4, 0, ?, temp.alt_id,  temp.wspace_id, temp.alt_measure_id, temp.measure_id from "+
		"(select distinct  alt_id, wspace_id, alt_measure_id, measure_id from measure_map_items where wspace_id = ? and isdefault = 1) temp "+
		" )";


	public final static String CREATE_PORT_WORKSPACE_PRJ_ASSOC = "insert into port_wksp_asso (map_type, use_latest, wspace_id, label_id, port_wksp_id, prj_id) values (?,?,?,?,?,?)";
     //G_WATSON_031507_ROLLBACK
	public final static String CREATE_PORT_RSET = Misc.G_DO_ORACLE ? "insert into port_rset (name, description, ID, port_wksp_id, port_wksp_id_rec_rset, is_auto_updateable, created_on) values (?,?,?,?,?,?, sysdate)" 
                                                                 : "insert into port_rset (name, description, port_wksp_id, port_wksp_id_rec_rset, is_auto_updateable, created_on) values (?,?,?,?,?, getdate())";

	public final static String COPY_PORT_RESULTS = Misc.G_DO_ORACLE ? "insert into port_results (port_rs_id, id, comments, fund_status, alt_id, tot_delay_mon, prj_id) (select ?, seq_detailed_port_rsets.nextval, comments, fund_status, alt_id, tot_delay_mon, prj_id from port_results where port_rs_id=?)" 
                                                                  : "insert into port_results (port_rs_id, comments, fund_status, alt_id, tot_delay_mon, prj_id) (select ?, comments, fund_status, alt_id, tot_delay_mon, prj_id from port_results where port_rs_id=?)";
/*rajeev 102307
	public final static String COPY_PORT_RESULTS_DETAILED_MILESTONE = Misc.G_DO_ORACLE ? "insert into rec_times_ms (comments, id, rec_t_ms_delay, ms_id, port_resul_id) "+
		" (select old_rset_det.comments, seq_detailed_port_rsets_ms.nextval, old_rset_det.rec_t_ms_delay, old_rset_det.ms_id, new_rset.id "+
		" from port_results old_rset, port_results new_rset, rec_times_ms old_rset_det " +
		" where new_rset.port_rs_id = ? and old_rset.port_rs_id=? and new_rset.alt_id = old_rset.alt_id and "+
		" old_rset_det.port_resul_id = old_rset.id)" : "insert into rec_times_ms (comments, id, rec_t_ms_delay, ms_id, port_resul_id) "+
		" (select old_rset_det.comments, intelli.getNextVal(0), old_rset_det.rec_t_ms_delay, old_rset_det.ms_id, new_rset.id "+
		" from port_results old_rset, port_results new_rset, rec_times_ms old_rset_det " +
		" where new_rset.port_rs_id = ? and old_rset.port_rs_id=? and new_rset.alt_id = old_rset.alt_id and "+
		" old_rset_det.port_resul_id = old_rset.id)";
*/
	// public static final String GET_ALL_PRJ_AND_SEL_FOR_PORT_WKSP = "select projects.id, projects.name, workspaces.id, workspaces.name, pj_map_items.map_type, pj_map_items.isdefault, port_wksp_asso.wspace_id, port_wksp_asso.use_latest, port_wksp_asso.label_id, port_wksp_asso.map_type from "+
	//        " projects, workspaces, pj_map_items, port_wksp_asso where pj_map_items.prj_id = projects.id and pj_map_items.wspace_id = workspaces.id and pj_map_items.isdefault = 1 and pj_map_items.map_type in (1,4) and "+
	//        " projects.status in (2,3,4,5) "+
	//        " port_wksp_asso.wspace_id(+) = pj_map_items.wspace_id and port_wksp_asso.port_wksp_id(+) = ? order by projects.id desc, workspaces.id desc ";

	//revised, simpler but probably above still works
	//  public static final String GET_ALL_PRJ_AND_SEL_FOR_PORT_WKSP =
	//   "select prjList.prj_id, prjList.prjName, prjList.wspace_id, prjList.wspace_name, prjList.map_type,1,assoc.wspace_id, assoc.use_latest, assoc.label_id, assoc.map_type "+
	//    "from "+
	//    "(select prj_id, map_type, use_latest, wspace_id, label_id from port_wksp_asso where port_wksp_id = ?) assoc "+
	//    ",(select projects.id prj_id, pj_map_items.map_type, workspaces.id  wspace_id, projects.name prjName, workspaces.name wspace_name "+
	//    "  from projects, workspaces, pj_map_items "+
	//    "  where workspaces.prj_id = projects.id "+
	//    "  and pj_map_items.wspace_id = workspaces.id "+
	//    "  and pj_map_items.isdefault = 1 "+
	//    "  and projects.status in (2,3,4,5) "+
	//    "  )prjList "+
	//    "where assoc.prj_id = prjList.prj_id "+
	//    "order by prj_id desc "+
	//    ";
  //NOTUSED
  
	public static final String GET_ALL_PRJ_AND_SEL_FOR_PORT_WKSP =
  //Misc.G_DO_ORACLE ? 
  "select projects.id, projects.name, workspaces.id, workspaces.name, pj_map_items.map_type, pj_map_items.isdefault,assoc.wspace_id, assoc.use_latest, assoc.label_id, assoc.map_type, assoc.prj_id "+
		"  from "+
		"  (select workspaces.id wspace_id from workspaces, pj_map_items, projects where "+
		"     projects.id = workspaces.prj_id and pj_map_items.wspace_id = workspaces.id "+
		"     and pj_map_items.isdefault = 1 and pj_map_items.map_type in (1,4) and projects.status in (2,3) "+
		"   union "+
		"   select wspace_id from port_wksp_asso where port_wksp_id = ? "+
		"  ) workspaceList, "+
		"  projects left outer join (select prj_id, map_type, use_latest, wspace_id, label_id from port_wksp_asso where port_wksp_id = ?) assoc on (assoc.prj_id = projects.id) , "+
		"  workspaces, pj_map_items "+
		"  where workspaceList.wspace_id = workspaces.id "+
		"  and workspaces.prj_id = projects.id "+
		"  and pj_map_items.wspace_id = workspaces.id "+
		"  and pj_map_items.isdefault = 1 "+		
		"  order by projects.name, projects.id desc, workspaces.id desc " ;
    //:
		//"select projects.id, projects.name, workspaces.id, workspaces.name, pj_map_items.map_type, pj_map_items.isdefault,assoc.wspace_id, assoc.use_latest, assoc.label_id, assoc.map_type, assoc.prj_id "+
		//"  from "+
		//"  (select workspaces.id wspace_id from workspaces, pj_map_items, projects where "+
		//"     projects.id = workspaces.prj_id and pj_map_items.wspace_id = workspaces.id "+
		//"     and pj_map_items.isdefault = 1 and pj_map_items.map_type in (1,4) and projects.status in (2,3) "+
		//"   union "+
		//"   select wspace_id from port_wksp_asso where port_wksp_id = ? "+
		//"  ) workspaceList, "+
		//"  (select prj_id, map_type, use_latest, wspace_id, label_id from port_wksp_asso where port_wksp_id = ?) assoc, "+
		//"  projects, workspaces, pj_map_items "+
		//"  where workspaceList.wspace_id = workspaces.id "+
		//"  and workspaces.prj_id = projects.id "+
		//"  and pj_map_items.wspace_id = workspaces.id "+
		//"  and pj_map_items.isdefault = 1 "+
		//"  and assoc.prj_id =* projects.id "+
		//"  order by projects.name, projects.id desc, workspaces.id desc ";
  
	public static final String REMOVE_ALL_PORT_WKSP_ASSO = "delete from port_wksp_asso where port_wksp_id = ?";

	public static final String GET_MAP_TYPES_FOR_WKSP = "select map_type from pj_map_items where wspace_id = ? order by map_type asc"; //if there are multiple mappings for a workspace then want the current to have more preference over baseline
	// NOTE that based on current schemes it should not happen that a wspace is asso with multiple map types

	public static final String GET_ALL_RSET = "select name,description,id, port_wksp_id_rec_rset from port_rset where port_wksp_id = ?";
//not used
/* 102307
	public static final String GET_PORT_RHS_RESULT_PRJ_LIST = Misc.G_DO_ORACLE ? "select "+
		"plist.prjId, plist.userGivenId, plist.name, plist.status "+
		",results.altId, results.altName, results.fundStatus, results.delayImposed, results.dwNPV, results.initLaunchDate "+
		"from (select port_rset.id rsId, projects.id prjId, projects.user_given_id userGivenId, projects.name name, projects.status status "+
		"from projects, port_wksps, port_wksp_asso, port_rset "+
		"where port_wksps.id = ? and port_rset.id = ?  and (port_wksps.is_for_prj_dw = 0) and port_wksp_asso.port_wksp_id = port_wksps.id "+
		"and projects.id = port_wksp_asso.prj_id "+
		") plist "+
		", (select alternatives.prj_id as prjId, port_results.port_rs_id as rsId, port_results.alt_id as altId, "+
		"alternatives.name as altName, port_results.fund_status as fundStatus, "+
		"dw_other_info.delay_imposed as delayImposed, dw_other_info.npv as dwNPV, "+
		"dw_other_info.init_launch_date as initLaunchDate "+
		"from port_results,  alternatives, dw_alt_short_info, dw_other_info "+
		"where port_results.port_rs_id= ? "+
		"and port_results.alt_id = alternatives.id and port_results.fund_status = 1 "+
		"and port_results.port_rs_id = dw_alt_short_info.port_rs_id(+) "+
		"and port_results.alt_id     = dw_alt_short_info.alt_id(+) "+
		"and dw_alt_short_info.id    = dw_other_info.dw_alt_si_id "+
		") results " +
		"where plist.rsId = results.rsId(+) and (plist.prjId = results.prjId(+)) "+
		"order by plist.prjid desc " : 
		"select "+
		"plist.prjId, plist.userGivenId, plist.name, plist.status "+
		",results.altId, results.altName, results.fundStatus, results.delayImposed, results.dwNPV, results.initLaunchDate "+
		"from (select port_rset.id rsId, projects.id prjId, projects.user_given_id userGivenId, projects.name name, projects.status status "+
		"from projects, port_wksps, port_wksp_asso, port_rset "+
		"where port_wksps.id = ? and port_rset.id = ?  and (port_wksps.is_for_prj_dw = 0) and port_wksp_asso.port_wksp_id = port_wksps.id "+
		"and projects.id = port_wksp_asso.prj_id "+
		") plist "+
		", (select alternatives.prj_id as prjId, port_results.port_rs_id as rsId, port_results.alt_id as altId, "+
		"alternatives.name as altName, port_results.fund_status as fundStatus, "+
		"dw_other_info.delay_imposed as delayImposed, dw_other_info.npv as dwNPV, "+
		"dw_other_info.init_launch_date as initLaunchDate "+
		"from port_results,  alternatives, dw_alt_short_info, dw_other_info "+
		"where port_results.port_rs_id= ? "+
		"and port_results.alt_id = alternatives.id and port_results.fund_status = 1 "+
		"and port_results.port_rs_id *= dw_alt_short_info.port_rs_id "+
		"and port_results.alt_id     *= dw_alt_short_info.alt_id "+
		"and dw_alt_short_info.id    = dw_other_info.dw_alt_si_id "+
		") results " +
		"where plist.rsId *= results.rsId and (plist.prjId *= results.prjId) "+
		"order by plist.prjid desc ";
*/

	public static final String SET_ALT_FUNDING_STATUS = "update port_results set fund_status=? where alt_id=? and port_rs_id=?";
	public static final String GET_FUNDED_ALTERNATIVE = "select alt_id, fund_status from port_results, alternatives where port_rs_id = ? and fund_status=1 and alternatives.id = port_results.alt_id and alternatives.prj_id = ?";


	public static final String GET_VER_INFO_FOR_RESULTSET_ALT =
		"select distinct pj_map_items.pj_basic_id, pj_map_items.alt_list_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_task_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id "+
		"from pj_map_items, alt_map_items, port_wksp_asso, labels "+
		"where port_wksp_asso.port_wksp_id = ? and port_wksp_asso.prj_id = ? "+
		"and alt_map_items.wspace_id = port_wksp_asso.wspace_id and pj_map_items.wspace_id = port_wksp_asso.wspace_id "+
		"and use_latest = 0 and alt_map_items.label_id = port_wksp_asso.label_id and alt_map_items.alt_id = ? and labels.id = port_wksp_asso.label_id and labels.pj_map_id = pj_map_items.id "+
		"union "+
		"select distinct  pj_map_items.pj_basic_id, pj_map_items.alt_list_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_task_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id "+
		"from pj_map_items, alt_map_items, port_wksp_asso "+
		"where port_wksp_asso.port_wksp_id = ? and port_wksp_asso.prj_id = ? and port_wksp_asso.map_type = 4 "+
		"and alt_map_items.wspace_id = port_wksp_asso.wspace_id and pj_map_items.wspace_id = port_wksp_asso.wspace_id "+
		"and  use_latest = 1 and alt_map_items.isdefault = 1 and alt_map_items.alt_id = ? and pj_map_items.isdefault = 1 "+
		"union "+
		"select distinct  pj_map_items.pj_basic_id, pj_map_items.alt_list_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_task_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  "+
		"from pj_map_items, alt_map_items, port_wksp_asso "+
		"where port_wksp_asso.port_wksp_id = ? and port_wksp_asso.prj_id = ? "+
		"and use_latest = 1 and port_wksp_asso.map_type <> 4 "+
		"and pj_map_items.prj_id = port_wksp_asso.prj_id and pj_map_items.map_type = port_wksp_asso.map_type and pj_map_items.isdefault = 1 "+
		"and alt_map_items.wspace_id = pj_map_items.wspace_id "+
		"and alt_map_items.isdefault = 1 and alt_map_items.map_type = port_wksp_asso.map_type and alt_map_items.alt_id = ? ";


	public static final String GET_NEXT_MILESTONE_WITH_DATEID = Misc.G_DO_ORACLE ? "select milestones.id, mstn_id from milestones where alt_date_id = ? and start_date > ? and finish_dt > sysdate order by mstn_id asc" :
		"select milestones.id, mstn_id from milestones where alt_date_id = ? and start_date > ? and finish_dt > getdate() order by mstn_id asc";

	public static final String GET_ALT_PORT_RESULT_ENTRY =
		"select id from port_results where alt_id = ? and port_rs_id = ?";
	public static final String CREATE_ALT_PORT_RESULT_ENTRY =
		"insert into port_results (id,alt_id, port_rs_id, fund_status, comments, tot_delay_mon, prj_id) values (?,?,?,?,?,0,?)";
	public static final String SET_ALT_PORT_RESULT_ENTRY_FUNDED =
		"update port_results set fund_status=? where id = ?";
    /*102307
	public static final String CLEAN_REC_MS_TIME =
		"delete from rec_times_ms where port_resul_id = ?";
	public static final String INSERT_REC_TIME_MS = Misc.G_DO_ORACLE ? "insert into rec_times_ms (id,rec_t_ms_delay, ms_id, port_resul_id, comments) (select seq_detailed_port_rsets_ms.nextval, ?,?,?,?)" : 
		"insert into rec_times_ms (id,rec_t_ms_delay, ms_id, port_resul_id, comments) (select intelli.getNextVal(0), ?,?,?,?)";
	public static final String SET_UNFUND_STATUS_DW_ENTRY_OTHER_ALT =
		"update dw_alt_short_info set fund_status=2 where alt_id <> ? and port_rs_id = ? and pj_id = ?";
	public static final String SET_UNFUND_STATUS_DW_ENTRY_MATCH_ALT =
		"update dw_alt_short_info set fund_status=2 where alt_id = ? and port_rs_id = ?";
  */
	public static final String COUNT_MILESTONES_DEFINED = "select count(*) from milestones where alt_date_id = ?";
	public static final String COUNT_PROF_OUTCOMES_DEFINED = "select count(*) from prof_outcomes where alt_profil_id = ?";

/* 102307
	public static final String GET_UNPREPARED_ALTERNATIVES = Misc.G_DO_ORACLE ? "select alternatives.prj_id, alternatives.id from port_wksp_asso, alternatives where port_wksp_asso.port_wksp_id = ? and alternatives.prj_id = port_wksp_asso.prj_id "+
		"minus "+
		"( select dw_alt_short_info.pj_id, dw_alt_short_info.alt_id from dw_alt_short_info where dw_alt_short_info.port_rs_id = ?)" : 
		"select alternatives.prj_id, alternatives.id from port_wksp_asso, alternatives where port_wksp_asso.port_wksp_id = ? and alternatives.prj_id = port_wksp_asso.prj_id "+
		"and Not Exists( "+
		"( select dw_alt_short_info.pj_id, dw_alt_short_info.alt_id from dw_alt_short_info where dw_alt_short_info.port_rs_id = ?))";

	public static final String SET_IS_DEF_DW_ALT_SHORT_INFO_WITH_PRIM_ALT =
		"update (select dw_alt_short_info.is_default_alt isdef from dw_alt_short_info, alternatives where dw_alt_short_info.port_rs_id = ? and alternatives.id = dw_alt_short_info.alt_id and alternatives.is_primary = 1) set isdef = 1";

	public static final String SET_IS_DEF_DW_ALT_SHORT_INFO_WITH_FIRST =
		"update dw_alt_short_info set is_default_alt = 1 where alt_id in (select  min(alt_id) from dw_alt_short_info where dw_alt_short_info.port_rs_id = ? group by pj_id having min(is_default_alt) = 0)";

	public static final String GET_DEFAULT_ALT_FROM_DW = "select alt_id from dw_alt_short_info where port_rs_id = ? and pj_id = ?";
  */
	public static final String GET_PRIM_ALT_FROM_ALTERNATIVES = "select id, alternatives.name from alternatives where prj_id = ? and is_primary = 1";
  /* 102307
	public static final String SET_IS_DEFAULT_FOR_MATCH_ALT = "update dw_alt_short_info set is_default_alt = ? where port_rs_id=? and alt_id = ?";
	public static final String SET_IS_DEFAULT_FOR_NOT_MATCH_ALT = "update dw_alt_short_info set is_default_alt = ? where port_rs_id=? and alt_id <> ? and pj_id=?";

	public static final String GET_PRJ_LIST_FOR_GANTT = //NOTVERIFIED
		"select alternatives.id, projects.id, altinfo.wkspid, m1.finish_dt, m2.finish_dt, alternatives.name, projects.name from "+
		"projects, alternatives, milestones m1, milestones m2, "+
		"(select  distinct alt_map_items.alt_id aid, alt_map_items.alt_date_id dtid, port_wksp_asso.wspace_id wkspid "+
		"from port_rset, port_wksps, port_results, alt_map_items, port_wksp_asso,alternatives "+
		"where port_rset.id = ? "+
		"and port_results.port_rs_id = port_rset.id "+
		"and port_rset.port_wksp_id = port_wksps.id "+
		"and port_wksps.id = port_wksp_asso.port_wksp_id "+
		"and (port_wksps.is_for_prj_dw = 0) "+
		"and alt_map_items.wspace_id = port_wksp_asso.wspace_id "+
		"and alt_map_items.isdefault = 1 "+
		"and alt_map_items.alt_id = port_results.alt_id "+
		"and port_results.fund_status = 1 "+
		"and alternatives.id = port_results.alt_id "+
		"and alternatives.prj_id = port_wksp_asso.prj_id "+
		") altinfo "+
		"where "+
		"alternatives.id = altInfo.aid "+
		"and projects.id = alternatives.prj_id "+
		"and m1.alt_date_id = altinfo.dtid "+
		"and m1.mstn_id = 0 "+
		"and m2.alt_date_id = altinfo.dtid "+
		"and m2.mstn_id = 5 "+
		"order by projects.id desc ";

	public static final String GET_TASK_LIST_FOR_GANTT_ALL = //NOTVERIFIED
		"select alternatives.id,alternatives.prj_id, altinfo.wkspid, del_approaches.start_date, del_approaches.end_date from "+
		"tasks, del_approaches, alternatives, "+
		"(select distinct alt_map_items.alt_id aid, alt_map_items.alt_task_id dtid, port_wksp_asso.wspace_id wkspid "+
		"from port_rset, port_wksps, port_results, alt_map_items, port_wksp_asso,alternatives "+
		"where port_rset.id = ? "+
		"and port_results.port_rs_id = port_rset.id "+
		"and port_rset.port_wksp_id = port_wksps.id "+
		"and port_wksps.id = port_wksp_asso.port_wksp_id "+
		"and (port_wksps.is_for_prj_dw = 0) "+
		"and alt_map_items.wspace_id = port_wksp_asso.wspace_id "+
		"and alt_map_items.isdefault = 1 "+
		"and alt_map_items.alt_id = port_results.alt_id "+
		"and port_results.fund_status = 1 "+
		"and alternatives.id = port_results.alt_id "+
		"and alternatives.prj_id = port_wksp_asso.prj_id "+
		") altinfo "+
		"where "+
		"tasks.alt_task_id = altinfo.dtid "+
		"and del_approaches.task_1_id = tasks.id "+
		"and del_approaches.task_1_id_reco = tasks.id "+
		"and alternatives.id = altinfo.aid "+
		"order by alternatives.prj_id desc";
*/
	public static final String GET_ALT_MAP_ENTRY = "select distinct alt_date_id, alt_model_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id, alt_work_id from alt_map_items where alt_id=? and wspace_id = ? and isdefault=1"; //TOVERIFY
	public static final String GET_ALT_MODEL_FILE_INFO = "select distinct file_name_file_name_id, file_name_file_name_id_excel from alt_models, alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and alt_map_items.alt_model_id = alt_models.id";
	public static final String GET_TEMPLATE_LIST = "select name, file_name_id from file_names where file_names.is_template = ?";

	public final static String COPY_CREATE_WORKSPACE = Misc.G_DO_ORACLE ? 
  "insert into workspaces (id, name, plan_id, prj_id) (select ?,(case when (? is null) then ('Copy Of ' || name) else ? end), plan_id, ? from workspaces where id = ? )" 
  :
  "insert into workspaces (name, plan_id, prj_id) (select (case when (? is null) then ('Copy Of ' + name) else ? end), plan_id, ? from workspaces where id = ? )" 
  ;


	//TODO this query will return all workspaces - historical and everything which may not make sense
	public static final String GET_WORKSPACE_FOR_PRJ = "select workspaces.id, workspaces.name from workspaces where workspaces.prj_id = ?";
	public static final String UPDATE_WORKSPACE_NAME = "update workspaces set name = ? where workspaces.id = ?";

	public static final String GET_ANA_PORT_INFO_FOR_WKSP = "select ana_port_wksp_id, ana_port_rset_id from workspaces where id = ?";
	public static final String UPDATE_ANA_PORT_INFO_FOR_WKSP = "update workspaces set ana_port_wksp_id = ?, ana_port_rset_id= ? where id = ?";
  /* 102307
	public static String GET_ALT_DW_TO_UPDATE_FOR_PRJ = Misc.G_DO_ORACLE ? "select distinct alt_map_items.alt_id aid, alt_map_items.alt_date_id did, alt_map_items.alt_basic_id bid, alt_map_items.alt_profil_id pid from alt_map_items where wspace_id = ? and isdefault = 1 "+
		"minus (select alt_id aid, ver_alt_mstone_id did, ver_alt_basic_id bid, ver_alt_profile_id pid from dw_alt_short_info where port_rs_id = ?)" :
		"select distinct alt_map_items.alt_id aid, alt_map_items.alt_date_id did, alt_map_items.alt_basic_id bid, alt_map_items.alt_profil_id pid from alt_map_items where wspace_id = ? and isdefault = 1 "+
		"and Not Exists((select alt_id aid, ver_alt_mstone_id did, ver_alt_basic_id bid, ver_alt_profile_id pid from dw_alt_short_info where port_rs_id = ?))";
*/
	public static String MAKE_PORT_WKSP_FOR_PRJ =
		"update port_wksps set is_for_prj_dw=1 where port_wksps.id = ?";

	/* - buggy
	 public static String GET_SUMM_MEASURE_FOR_TOOL =
	 "select sum(dw_other_info.npv), sum(dw_count.val), sum(dw_other_info.peak_rev) from "+
	 "dw_other_info, dw_alt_short_info, dw_count where dw_alt_short_info.port_rs_id = ? and dw_other_info.dw_alt_si_id = dw_alt_short_info.id and dw_count.dw_alt_si_id = dw_alt_short_info.id and dw_alt_short_info.fund_status=1";
	 */
/* 102307
	public static String GET_SUMM_MEASURE_FOR_TOOL =
		"select sum(dw_other_info.npv), sum(dw_alt_short_info.fund_status), sum(dw_other_info.peak_rev) from "+
		"dw_other_info, dw_alt_short_info where dw_alt_short_info.port_rs_id = ? and dw_other_info.dw_alt_si_id = dw_alt_short_info.id and dw_alt_short_info.fund_status=1";
*/

	// TODO this query returns all task - aks
	public static final String GET_TASK_LIST_ALL =
		"select alternatives.id,alternatives.prj_id, altinfo.wkspid, alternatives.name, projects.name from "+
		"alternatives, projects,"+
		"(select distinct alt_map_items.alt_id aid, alt_map_items.alt_task_id dtid, port_wksp_asso.wspace_id wkspid "+
		"from port_rset, port_wksps, port_results, alt_map_items, port_wksp_asso,alternatives "+
		"where port_rset.id = ? "+
		"and port_results.port_rs_id = port_rset.id "+
		"and port_rset.port_wksp_id = port_wksps.id "+
		"and port_wksps.id = port_wksp_asso.port_wksp_id "+
		"and alt_map_items.wspace_id = port_wksp_asso.wspace_id "+
		"and alt_map_items.isdefault = 1 "+
		"and alt_map_items.alt_id = port_results.alt_id "+
		"and port_results.fund_status = 1 "+
		"and alternatives.id = port_results.alt_id "+
		"and alternatives.prj_id = port_wksp_asso.prj_id "+
		") altinfo "+
		"where "+
		"alternatives.id = altinfo.aid "+
		"and alternatives.prj_id = projects.id";

	public static String GET_ALT_LIST_FROM_ONE_ALT = "select alt1.id, alt1.name from alternatives alt1, alternatives alt2 where "+
		"alt2.id = ? and alt1.prj_id = alt2.prj_id";

/*102307
	public static String GET_FUNDED_ALTS_FOR_PRJ = "select dw_alt_short_info.fund_status, alternatives.is_primary, dw_alt_short_info.alt_id, alternatives.name from dw_alt_short_info, alternatives where dw_alt_short_info.port_rs_id = ? and dw_alt_short_info.pj_id = ? and alternatives.prj_id = dw_alt_short_info.pj_id and fund_status = 1 or alternatives.is_primary = 1";
  */
	public static String GET_ALT_LIST_FOR_PRJ = "select alternatives.id from alternatives where prj_id=?";
  
  //this is same for msft/orcl ... we are going to mimic oracle in msft
	public static String CREATE_FILE_NAME = "insert into file_names (file_name_id, mime, name, is_template, extension) values (?,?,?,0,?)";
/*102307                                                           
	public static String GET_PRJ_STATUS_MEASURE = Misc.G_DO_ORACLE ?
		"select dw_alt_short_info.alt_id, alternatives.name, npv_dev_cost, npv, peak_rev, npv_at_launch, tech_risk, to_char(init_launch_date, '"+Misc.G_DEFAULT_DATE_FORMAT+"') from "+
		"dw_other_info, dw_alt_short_info, alternatives where dw_alt_short_info.port_rs_id = ? and dw_other_info.dw_alt_si_id = dw_alt_short_info.id and alternatives.id = dw_alt_short_info.alt_id"
    :
   "select dw_alt_short_info.alt_id, alternatives.name, npv_dev_cost, npv, peak_rev, npv_at_launch, tech_risk, convert(datetime,init_launch_date,101) from "+
		"dw_other_info, dw_alt_short_info, alternatives where dw_alt_short_info.port_rs_id = ? and dw_other_info.dw_alt_si_id = dw_alt_short_info.id and alternatives.id = dw_alt_short_info.alt_id";

	public static String GET_PORT_STATUS_MEASURE_NPV =
		"select sum(npv), count(*) from dw_alt_short_info, dw_other_info where dw_alt_short_info.port_rs_id = ? and fund_status = 1 and dw_other_info.dw_alt_si_id = dw_alt_short_info.id";

	public static String GET_PORT_STATUS_MEASURE_REV =
		"select sum(dw_rev.expected), time_dims.year from dw_alt_short_info, dw_rev, time_dims where dw_alt_short_info.port_rs_id = ? and fund_status = 1 and dw_rev.dw_alt_si_id = dw_alt_short_info.id and dw_rev.time_dim_val = time_dims.val and time_dims.year in (105, 106) group by time_dims.year order by time_dims.year";


	public static String GET_PORT_STATUS_MEASURE_COST =
		"select sum(dw_cost.expected), time_dims.year from dw_alt_short_info, dw_cost, time_dims, cost_items where dw_alt_short_info.port_rs_id = ? and fund_status = 1 and dw_cost.dw_alt_si_id = dw_alt_short_info.id and dw_cost.time_dim_val = time_dims.val and time_dims.year in (105, 106) and dw_cost.cost_li_id = cost_items.id and cost_items.alt_opcost_id is null group by time_dims.year order by time_dims.year";

	public static String GET_PORT_STATUS_MEASURE_COUNT =
		"select sum(dw_count.val), time_dims.year from dw_alt_short_info, dw_count, time_dims where dw_alt_short_info.port_rs_id = ? and fund_status = 1 and dw_count.dw_alt_si_id = dw_alt_short_info.id and dw_count.time_dim_val = time_dims.val and time_dims.year in (105, 106) group by time_dims.year order by time_dims.year";
*/
	// TODO this query returns rev for given port_rset_id and start_qtr  - aks
	//  public static String GET_PORT_REV_BY_QTR =
	//    "select alt_id, mkt_type, time_dim_val, SUM (val) from dw_alt_short_info, dw_rev, rev_segs "+
	//    "where dw_rev.dw_alt_si_id = dw_alt_short_info.id "+
	//    "and dw_rev.rev_seg_id = rev_segs.id "+
	//    "and dw_alt_short_info.port_rs_id = ? "+
	//    "and dw_rev.time_dim_val >= ? "+
	//    "group by alt_id, mkt_type, time_dim_val";


	//rajeev 081005 .. broke up into two to accomodate prj_id = ?

	//changed prof_outcome_ty to rev_segs.scen_id
	public static String GET_PORT_REV_BY_QTR_1 =  "select port_results.alt_id, mkt_type, time_id, alt_scen_list.delay_val, rev_segs.scen_id, sum(data.value * intelli.getPropIncludedSimpleCurrency(data.val_scope, data.year, time_id, 0, data.val_dur, alt_rev_model.currency_id,0,?)), 0, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5 from port_results, alt_scen_list, rev_segs, data, qtr_timeid, alt_rev_model where "+
		"port_results.port_rs_id = ? and ";
    
	public static String GET_PORT_REV_BY_QTR_2 = Misc.G_DO_ORACLE ? "port_results.ver_alt_rev_id = rev_segs.alt_rev_id and alt_rev_model.id = rev_segs.alt_rev_id and "+
		"alt_scen_list.alt_id = port_results.alt_id and alt_scen_list.scen_id = rev_segs.scen_id and "+
		"data.rev_seg_id = rev_segs.id "+
		"and qtr_timeid.time_id >= ? "+
		//"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 "+
    		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by port_results.alt_id, time_id, mkt_type, alt_scen_list.delay_val, rev_segs.scen_id, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5 "+
		"order by port_results.alt_id, delay_val, rev_segs.scen_id, mkt_type, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5, time_id" 
    : 
		"port_results.ver_alt_rev_id = rev_segs.alt_rev_id and  alt_rev_model.id = rev_segs.alt_rev_id and "+
		"alt_scen_list.alt_id = port_results.alt_id and alt_scen_list.scen_id = rev_segs.scen_id and "+
		"data.rev_seg_id = rev_segs.id "+
		"and qtr_timeid.time_id >= ? "+
		//"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 "+
    		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by port_results.alt_id, time_id, mkt_type, alt_scen_list.delay_val, rev_segs.scen_id, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5 "+
		"order by port_results.alt_id, delay_val, rev_segs.scen_id, mkt_type, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5, time_id";

	//rajeev 081005 .. broke in two to accomode prj_id = ? clause

	public static String GET_PORT_OP_COST_BY_QTR_1 = "select port_results.alt_id, cost_cent_id,  time_id, alt_scen_list.delay_val, cost_items.scen_id, sum(data.value* intelli.getPropIncludedSimpleCurrency(data.val_scope, data.year, time_id, 0, data.val_dur, alt_opcost_model.currency_id, 0, ?)), 0, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.target_market from port_results, alt_opcost_model, alt_scen_list, cost_items, data, qtr_timeid where "+
		"port_results.port_rs_id = ? and ";
		
	public static String GET_PORT_OP_COST_BY_QTR_2 = Misc.G_DO_ORACLE ? "port_results.ver_alt_opcost_id = cost_items.alt_opcost_id and port_results.ver_alt_opcost_id = alt_opcost_model.id and "+
		"alt_scen_list.alt_id = port_results.alt_id and cost_items.scen_id = alt_scen_list.scen_id and "+
		"data.cost_li_id = cost_items.id and "+
		"cost_items.to_include = 1 "+
		"and qtr_timeid.time_id >= ? "+
		//  "and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by port_results.alt_id, time_id, cost_cent_id, target_market, alt_scen_list.delay_val, cost_items.scen_id, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5  "+
		"order by port_results.alt_id, delay_val, cost_items.scen_id, cost_cent_id, target_market, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, time_id" 
    :
		"port_results.ver_alt_opcost_id = cost_items.alt_opcost_id and alt_opcost_model.id = port_results.ver_alt_opcost_id and "+
		"alt_scen_list.alt_id = port_results.alt_id and cost_items.scen_id = alt_scen_list.scen_id and "+
		"data.cost_li_id = cost_items.id and "+
		"cost_items.to_include = 1 "+
		"and qtr_timeid.time_id >= ? "+
		//  "and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by port_results.alt_id, time_id, cost_cent_id, target_market, alt_scen_list.delay_val, cost_items.scen_id, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5  "+
		"order by port_results.alt_id, delay_val, cost_items.scen_id, cost_cent_id, target_market, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, time_id";

	public static String GET_PORT_DEV_COST_BY_QTR_1 = Misc.G_DO_ORACLE ? "select port_results.alt_id, cost_cent_id, for_achieving_milestone, time_id, sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), 0, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.target_market, cost_items.scen_id from port_results, cost_items, data, qtr_timeid where "+
		"port_results.port_rs_id = ? and " : "select port_results.alt_id, cost_cent_id, for_achieving_milestone, time_id, sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), 0, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.target_market, cost_items.scen_id from port_results, cost_items, data, qtr_timeid where "+
		"port_results.port_rs_id = ? and ";
	public static String GET_PORT_DEV_COST_BY_QTR_2 = Misc.G_DO_ORACLE ? "port_results.ver_alt_devcost_id = cost_items.alt_devcost_id and "+
		"cost_items.alt_opcost_id is null and "+
		"data.cost_li_id = cost_items.id and "+
		"cost_items.to_include = 1 "+
		"and qtr_timeid.time_id >= ? "+
		//  "and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by port_results.alt_id, time_id, for_achieving_milestone, cost_items.scen_id, target_market, cost_cent_id, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		"order by port_results.alt_id, cost_cent_id, for_achieving_milestone, cost_items.scen_id, target_market, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify3, cost_items.classify4, cost_items.classify5, time_id " : 
		"port_results.ver_alt_devcost_id = cost_items.alt_devcost_id and "+
		"cost_items.alt_opcost_id is null and "+
		"data.cost_li_id = cost_items.id and "+
		"cost_items.to_include = 1 "+
		"and qtr_timeid.time_id >= ? "+
		//  "and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by port_results.alt_id, time_id, for_achieving_milestone, cost_items.scen_id, target_market, cost_cent_id, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		"order by port_results.alt_id, cost_cent_id, for_achieving_milestone, cost_items.scen_id, target_market, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify3, cost_items.classify4, cost_items.classify5, time_id ";

	// Queries for Planning section of the web site
	public static String GET_CURRENT_PLAN_ID = "select id, name, description, type, year from plans where type=?";
	public static String PLN_WKF_STEPS = "select name, finish_dt, pln_wkf_st_desc, id, is_start, is_lockdown, is_publish, p_owner, sec_owner from pln_wf_steps where plan_id = ? order by pos_in_seq";
     public static String PLN_WKF_STEPS_GEN = "select pln_wf_steps.name, finish_dt, pln_wkf_st_desc, pln_wf_steps.id, is_start, is_lockdown, is_publish, p_owner, sec_owner, pos_in_seq, plan_id from pln_wf_steps,plans where pln_wf_steps.plan_id = plans.id and plans.port_node_id = ? and type in (1,4) order by year, plan_id, pos_in_seq";
	public static String PLN_PRJ_WKF_STEPS = "select distinct pln_wf_status.id, workspaces.id, projects.name, pln_wf_status.finish_dt, pln_wf_status.init_fin_dt, pln_wf_steps.finish_dt, projects.id, pos_in_seq from "+
		"pln_wf_status, pln_wf_steps, projects, workspaces, pj_map_items where pln_wf_status.plan_id = ? and pln_wf_status.pln_wkf_st_id = pln_wf_steps.id and workspaces.id = pln_wf_status.wspace_id and projects.id = workspaces.prj_id and pj_map_items.wspace_id = workspaces.id and pj_map_items.isdefault = 1 order by projects.id desc, pos_in_seq";
	public static String GET_PLAN_SUMM_INFO = "select id, name, description, type, year,start_date from plans where id=?";
	public static String GET_LIST_OF_USERS = "select id, name from users";

	public static String CREATE_PLAN = "insert into plans (id,name,year,description, type, start_date, port_node_id) values (?,?,?,?,?,?, ?)";
     public static String UPDATE_PLAN = "update plans set name=?,year=?, description=?, type=?, start_date=?, port_node_id=? where plans.id = ?";
	public static String CREATE_PLAN_WF_STEPS = Misc.G_DO_ORACLE ? 
  "insert into pln_wf_steps (id, pln_wkf_st_desc, pos_in_seq, sec_owner, p_owner, name, finish_dt, plan_id, is_start, is_lockdown, is_publish, menu_template) values (seq_plan_wf_steps.nextval,?,?,?,?,?,?,?,?,?,?,?)" 
  : 
  "insert into pln_wf_steps (id, pln_wkf_st_desc, pos_in_seq, sec_owner, p_owner, name, finish_dt, plan_id, is_start, is_lockdown, is_publish, menu_template) values (intelli.getNextVal(0),?,?,?,?,?,?,?,?,?,?,?)";
     public static String UPDATE_PLAN_WF_STEPS = "update pln_wf_steps set pln_wkf_st_desc=?, pos_in_seq=?, sec_owner=?, p_owner=?, name=?, finish_dt=?, is_start=?, is_lockdown=?, is_publish=?, menu_template=? where id = ?";
     
     //FOLLOWING PROBABLY NO LONGER USED
	public static String GET_CURRENT_PRJ_WSPACE_ID = "select wspace_id, workspaces.prj_id from workspaces, pj_map_items, prj_portfolio_map where prj_portfolio_map.port_node_id=? and workspaces.prj_id=prj_portfolio_map.prj_id and isdefault=1 and (map_type=1 or map_type=4) and pj_map_items.wspace_id = workspaces.id order by workspaces.prj_id, map_type, date_created desc";
	public static String CREATE_PLN_WF_STATUS = Misc.G_DO_ORACLE ? "insert into pln_wf_status (id,  wspace_id, s_owner, init_fin_dt, p_owner, pln_wkf_st_id, plan_id) "+
		" (select seq_plan_wf_status.nextval, ?, sec_owner, finish_dt, p_owner, id, plan_id "+
          " from pln_wf_steps refPlan where refplan.plan_id=? and not exists(select 1 from pln_wf_status, pln_wf_steps existSteps where pln_wf_status.wspace_id = ? and existSteps.plan_id = ? and pln_wf_status.pln_wkf_st_id=existSteps.id and existSteps.pos_in_seq = refPlan.pos_in_seq)  )" :
          "insert into pln_wf_status (id,  wspace_id, s_owner, init_fin_dt, p_owner, pln_wkf_st_id, plan_id) "+
		" (select intelli.getNextVal(0), ?, sec_owner, finish_dt, p_owner, id, plan_id "+
          " from pln_wf_steps refPlan where refplan.plan_id=? and not exists(select 1 from pln_wf_status, pln_wf_steps existSteps where pln_wf_status.wspace_id = ? and existSteps.plan_id = ? and pln_wf_status.pln_wkf_st_id=existSteps.id and existSteps.pos_in_seq = refPlan.pos_in_seq)  )";
	//above query is used in two places - planning helper & WorkspaceMeta


	public static String UPDATE_PLAN_WF_STEPS_DTONLY = "update pln_wf_steps set p_owner=?, sec_owner=?, finish_dt=? where id=?";

	public static String PJ_WKF_STEPS = "select name, duration, pj_wf_st_desc, id, is_start, is_lockdown, is_publish, p_owner, s_owner from pj_wf_steps where plan_id = ? order by pos_in_seq";
	public static String CREATE_PRJ_WF_STEPS = Misc.G_DO_ORACLE ? "insert into pj_wf_steps (id, pj_wf_st_desc, pos_in_seq, s_owner, p_owner, name, duration, plan_id, is_start, is_lockdown, is_publish) values (seq_project_wf_steps.nextval,?,?,?,?,?,?,?,?,?,?)" :
   "insert into pj_wf_steps (id, pj_wf_st_desc, pos_in_seq, s_owner, p_owner, name, duration, plan_id, is_start, is_lockdown, is_publish) values (intelli.getNextVal(0),?,?,?,?,?,?,?,?,?,?)";
	public static String UPDATE_PRJ_WF_STEPS = "update pj_wf_steps set p_owner=?, s_owner=?, duration=? where id=?";

	public static String GET_ALT_MAP_ITEMS = "select id,alt_id,map_type, alt_date_id, alt_model_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id from alt_map_items where isdefault=1 and wspace_id=?";
	//  public static String GET_ALL_WORKSPACE_FOR_PRJ = "select workspaces.id, workspaces.name, pj_map_items.map_type from workspaces, pj_map_items where workspaces.prj_id=? and pj_map_items.wspace_id = workspaces.id and isdefault=1 and map_type in (1,2,4) order by map_type";
	public static String GET_ALL_WORKSPACE_FOR_PRJ = "select workspaces.id, workspaces.name, pj_map_items.map_type from workspaces, pj_map_items where workspaces.prj_id=? and pj_map_items.wspace_id = workspaces.id and isdefault=1 order by map_type, workspaces.id desc";

	public static String DO_PJ_MAP_SWITCH_LABEL = "update pj_map_items set pj_basic_id = (select distinct pj_basic_id from labels, pj_map_items where labels.id = ? and pj_map_items.id = pj_map_id) where wspace_id = ? and isdefault=1";
	public static String DO_ALT_MAP_SWITCH_LABEL =
		(Misc.G_DO_ORACLE) ?
		"update alt_map_items curr set (alt_date_id, alt_model_id, alt_task_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id ) = (select distinct alt_date_id, alt_model_id, alt_task_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id  from alt_map_items labelled where labelled.label_id = ? and labelled.alt_id = curr.alt_id) where curr.wspace_id = ? and curr.isdefault=1"
		:
		"update curr " +
		"set curr.alt_date_id = labelled.alt_date_id, curr.alt_model_id = labelled.alt_model_id, curr.alt_task_id = labelled.alt_task_id, " +
		"curr.alt_basic_id = labelled.alt_basic_id, curr.alt_profil_id = labelled.alt_profil_id, curr.alt_fte_id = labelled.alt_fte_id, " +
		"curr.alt_devcost_id = labelled.alt_devcost_id, curr.alt_rev_id = labelled.alt_rev_id, curr.alt_opcost_id = labelled.alt_opcost_id, " +
		"curr.alt_combined_id = labelled.alt_combined_id, curr.alt_work_id = labelled.alt_work_id, curr.alt_rating_id = labelled.alt_rating_id " +
		"from alt_map_items curr, alt_map_items labelled " +
		"where labelled.label_id = ? and labelled.alt_id = curr.alt_id " +
		"and curr.wspace_id = ? and curr.isdefault=1 ";

	public static String DO_MEASURE_MAP_SWITCH_LABEL =
		(Misc.G_DO_ORACLE) ?
		"update measure_map_items curr set (alt_measure_id) = (select distinct alt_measure_id from measure_map_items labelled where labelled.label_id = ? and labelled.alt_id = curr.alt_id and labelled.measure_id = curr.measure_id) where curr.wspace_id = ? and curr.isdefault=1"
		:
		"update curr " +
		"set curr.alt_measure_id = labelled.alt_measure_id " +
		"from measure_map_items curr, measure_map_items labelled " +
		"where labelled.label_id = ? " +
		"and labelled.alt_id = curr.alt_id and labelled.measure_id = curr.measure_id " +
		"and curr.wspace_id = ? and curr.isdefault=1 ";

	public static String DO_PJ_MAP_SWITCH_SCENARIO =
		(Misc.G_DO_ORACLE) ?
		"update pj_map_items curr set pj_basic_id = (select distinct ver_prj_basic_id from port_results where port_results.port_rs_id = ? and port_results.prj_id = curr.prj_id) where wspace_id = ? and isdefault=1"
		:
		"update pj_map_items " +
		"set pj_map_items.pj_basic_id = port_results.ver_prj_basic_id " +
		"from pj_map_items, port_results " +
		"where port_results.port_rs_id = ? and port_results.prj_id = pj_map_items.prj_id " +
		"and pj_map_items.wspace_id = ? and pj_map_items.isdefault=1 ";

	public static String DO_ALT_MAP_SWITCH_SCENARIO =
		(Misc.G_DO_ORACLE) ?
		"update alt_map_items curr set (alt_date_id, alt_model_id, alt_basic_id, alt_profil_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_work_id, alt_rating_id) = (select distinct ver_alt_mstone_id, ver_alt_model_id, ver_alt_basic_id, ver_alt_profile_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_work_id, ver_alt_rating_id  from port_results where port_results.port_rs_id = ? and port_results.alt_id = curr.alt_id) where curr.wspace_id = ? and curr.isdefault=1"
		:
		"update ami " +
		"set ami.alt_date_id = pr.ver_alt_mstone_id, ami.alt_model_id = pr.ver_alt_model_id, ami.alt_basic_id = pr.ver_alt_basic_id, " +
		"ami.alt_profil_id = pr.ver_alt_profile_id, ami.alt_fte_id = pr.ver_alt_fte_id, ami.alt_devcost_id = pr.ver_alt_devcost_id, " +
		"ami.alt_rev_id = pr.ver_alt_rev_id, ami.alt_opcost_id = pr.ver_alt_opcost_id, ami.alt_combined_id = pr.ver_alt_combined_id, " +
		"ami.alt_work_id = pr.ver_alt_work_id, ami.alt_rating_id = pr.ver_alt_rating_id " +
		"from alt_map_items ami, port_results pr " +
		"where pr.port_rs_id = ? and pr.alt_id = ami.alt_id " +
		"and ami.wspace_id = ? and ami.isdefault=1 ";

	//  public static String DO_MEASURE_MAP_SWITCH_SCENARIO = "update measure_map_items curr set (alt_measure_id) = (select distinct alt_measure_id from measure_map_items labelled where labelled.label_id = ? and labelled.alt_id = curr.alt_id and labelled.measure_id = curr.measure_id) where curr.wspace_id = ? and curr.isdefault=1";


	public static String GET_LABEL_INFO ="select name from labels where id=?";
	public static String GET_LABEL_LIST = "select labels.id, labels.name, labels.description, labels.created_by, labels.created_on, users.name, users.email from labels,users where wspace_id=? and is_sys_generated=0 and users.id = created_by order by created_on desc";
	public static String GET_HISTORY_FOR_PJ_SECTION = "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name from wksp_hist, users where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and users.id = wksp_hist.wksp_hist_by order by on_date desc";
	public static String GET_HISTORY_FOR_ALT_SECTION = "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name from wksp_hist, users where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and alt_id = ? and users.id = wksp_hist.wksp_hist_by order by on_date desc";
	public static String GET_HISTORY_FOR_ALT_SECTION_MEASURE = "select ver_info.verid, wksp_hist.comments, wksp_hist_by, on_date, users.name from (select min(wksp_hist.id) histid, ref_item_id verid from wksp_hist, alt_measures where wspace_id = ? and ref_item_type = ? and wksp_hist.alt_id = ? and alt_measures.measure_id = ? and alt_measures.id = ref_item_id group by ref_item_id) ver_info, wksp_hist, users where ver_info.histid = wksp_hist.id and users.id = wksp_hist.wksp_hist_by order by on_date desc";
	public static String GET_VERSION_CREATED_INFO = "select wksp_hist_by, users.name, on_date from wksp_hist, users where ref_item_type=? and ref_item_id=? and users.id = wksp_hist_by";

	public static String GET_HISTORY_FOR_ALT_MODEL_SECTION = Misc.G_DO_ORACLE ? "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, fileName.file_name_id," +
  "fileName.name, tempName.file_name_id, tempName.name, fileName.extension from wksp_hist, users, alt_models, file_names fileName, file_names tempName "+
  "where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and users.id = wksp_hist.wksp_hist_by and alt_models.id = wksp_hist.ref_item_id and "+ 
  "fileName.file_name_id = alt_models.file_name_file_name_id_excel and tempName.file_name_id(+) = alt_models.file_name_file_name_id order by on_date desc" : 
  "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, fileName.file_name_id, fileName.name, tempName.file_name_id, tempName.name, fileName.extension from "+ 
  "wksp_hist, users, alt_models, file_names fileName, file_names tempName where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and users.id = wksp_hist.wksp_hist_by"+ 
  "and alt_models.id = wksp_hist.ref_item_id and fileName.file_name_id = alt_models.file_name_file_name_id_excel and tempName.file_name_id =* alt_models.file_name_file_name_id " +
  "order by on_date desc";

//	public static String GET_HISTORY_FOR_ALT_REV_SECTION = "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, fileName.file_name_id, fileName.name, tempName.file_name_id, tempName.name, fileName.extension from wksp_hist, users, alt_rev_model, file_names fileName, file_names tempName where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and users.id = wksp_hist.wksp_hist_by and alt_rev_model.id = wksp_hist.ref_item_id and fileName.file_name_id = alt_rev_model.file_id and tempName.file_name_id(+) = alt_rev_model.file_template_id order by on_date desc";
     public static String GET_HISTORY_FOR_ALT_REV_SECTION = "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, "+Misc.getUndefInt()+", '', "+Misc.getUndefInt()+", '', '.xml' from wksp_hist, users, alt_rev_model where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and users.id = wksp_hist.wksp_hist_by and alt_rev_model.id = wksp_hist.ref_item_id order by on_date desc";
//	public static String GET_HISTORY_FOR_ALT_DEVCOST_SECTION = "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, fileName.file_name_id, fileName.name, tempName.file_name_id, tempName.name, fileName.extension from wksp_hist, users, alt_devcost_model, file_names fileName, file_names tempName where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and users.id = wksp_hist.wksp_hist_by and alt_devcost_model.id = wksp_hist.ref_item_id and fileName.file_name_id = alt_devcost_model.file_id and tempName.file_name_id(+) = alt_devcost_model.file_template_id order by on_date desc";
     public static String GET_HISTORY_FOR_ALT_DEVCOST_SECTION = "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, "+Misc.getUndefInt()+", '', "+Misc.getUndefInt()+", '', 'xml' from wksp_hist, users, alt_devcost_model where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and users.id = wksp_hist.wksp_hist_by and alt_devcost_model.id = wksp_hist.ref_item_id order by on_date desc";
	public static String GET_HISTORY_FOR_ALT_OPCOST_SECTION = Misc.G_DO_ORACLE ? "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, fileName.file_name_id,"+
  " fileName.name, tempName.file_name_id, tempName.name, fileName.extension from wksp_hist, users, alt_opcost_model, file_names fileName, file_names tempName where "+
  "wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and users.id = wksp_hist.wksp_hist_by and alt_opcost_model.id = wksp_hist.ref_item_id and "+
  "fileName.file_name_id = alt_opcost_model.file_id and tempName.file_name_id(+) = alt_opcost_model.file_template_id order by on_date desc" : 
  "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, fileName.file_name_id, fileName.name, tempName.file_name_id, tempName.name, fileName.extension from "+
  " wksp_hist, users, alt_opcost_model, file_names fileName, file_names tempName where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and"+
  " users.id = wksp_hist.wksp_hist_by and alt_opcost_model.id = wksp_hist.ref_item_id and fileName.file_name_id = alt_opcost_model.file_id and "+
  "tempName.file_name_id =* alt_opcost_model.file_template_id order by on_date desc";
	public static String GET_HISTORY_FOR_ALT_FTE_SECTION = Misc.G_DO_ORACLE ? "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, fileName.file_name_id, "+
  "fileName.name, tempName.file_name_id, tempName.name, fileName.extension from wksp_hist, users, alt_fte_model, file_names fileName, file_names tempName where "+
  " wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and users.id = wksp_hist.wksp_hist_by and alt_fte_model.id = wksp_hist.ref_item_id and "+
  " fileName.file_name_id = alt_fte_model.file_id and tempName.file_name_id(+) = alt_fte_model.file_template_id order by on_date desc" : 
  "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name, fileName.file_name_id, fileName.name, tempName.file_name_id, tempName.name, fileName.extension "+
  " from wksp_hist, users, alt_fte_model, file_names fileName, file_names tempName where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and "+
  "users.id = wksp_hist.wksp_hist_by and alt_fte_model.id = wksp_hist.ref_item_id and fileName.file_name_id = alt_fte_model.file_id and tempName.file_name_id =* alt_fte_model.file_template_id "+
  "order by on_date desc";
	public static String GET_HISTORY_FOR_ALT_COMBINED_MODEL_SECTION = "select ref_item_id, wksp_hist.comments, wksp_hist_by, on_date, users.name from wksp_hist, users, alt_combined_model where wksp_hist.wspace_id = ? and wksp_hist.ref_item_type = ?  and wksp_hist.alt_id=? and users.id = wksp_hist.wksp_hist_by and alt_combined_model.id = wksp_hist.ref_item_id order by on_date desc";

	//changed prof_outcome to scen_id
	public static String GET_REV_ASSUMPTIONS = "select distinct assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.rev_seg_id, data.value, data.year, rev_segs.scen_id, alt_scen_list.delay_val, rev_segs.name, rev_segs.mkt_type, data.val_scope "+
		"from alt_scen_list, rev_segs, assumptions, data, alt_rev_model where "+
		"rev_segs.alt_rev_id = ? and alt_rev_model.id = rev_segs.alt_rev_id and alt_rev_model.alt_id = alt_scen_list.alt_id and "+
		"rev_segs.scen_id = alt_scen_list.scen_id and "+
		"assumptions.rev_seg_id = rev_segs.id and "+
		"data.assum_id = assumptions.id "+
		"order by alt_scen_list.delay_val, rev_segs.scen_id, rev_segs.mkt_type, assumptions.name, assumptions.id, data.year";
	//changed prof_outcome to scen_id
	public static String GET_OPCOST_ASSUMPTIONS = "select distinct assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.cost_li_id, data.value, data.year, cost_items.scen_id, alt_scen_list.delay_val, cost_items.name, cost_items.cost_cent_id, data.val_scope, cost_items.target_market  "+
		"from alt_scen_list, alt_opcost_model, cost_items, assumptions, data where "+
		"cost_items.alt_opcost_id = ? and alt_opcost_model.id = cost_items.alt_opcost_id and alt_scen_list.alt_id = alt_opcost_model.alt_id and "+
		"cost_items.scen_id = alt_scen_list.scen_id and "+
		"assumptions.cost_li_id = cost_items.id and "+
		"data.assum_id = assumptions.id "+
		"order by alt_scen_list.delay_val, cost_items.scen_id, cost_items.cost_cent_id, assumptions.name, assumptions.id, data.year";
	//changed prof_outcome to scen_id

	public static String GET_DEV_ASSUMPTIONS = "select distinct assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.cost_li_id, data.value, data.year, cost_items.for_achieving_milestone, cost_items.name, cost_items.cost_cent_id, data.val_scope, cost_items.target_market, cost_items.scen_id  "+
		"from cost_items, assumptions, data where "+
		"cost_items.alt_devcost_id = ? and "+
		"assumptions.cost_li_id = cost_items.id and "+
		"data.assum_id = assumptions.id "+
		"order by cost_items.for_achieving_milestone, cost_items.cost_cent_id, assumptions.name, assumptions.id, data.year";

	public static String GET_FTE_ASSUMPTIONS = "select distinct assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.fte_item_id, data.value, data.year, fte_items.for_achieving_milestone, fte_items.name, fte_items.fte_head_id, data.val_scope, fte_items.target_market  "+
		"from fte_items, assumptions, data where "+
		"fte_items.alt_fte_id = ? and "+
		"assumptions.fte_item_id = fte_items.id and "+
		"data.assum_id = assumptions.id "+
		"order by fte_items.for_achieving_milestone, fte_items.fte_head_id, assumptions.name, assumptions.id, data.year";

	public static String GET_MODEL_ASSUMPTIONS = "select distinct assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.alt_combined_id, data.value, data.year, data.val_scope  "+
		"from assumptions, data where "+
		"assumptions.alt_combined_id = ? and "+
		"data.assum_id = assumptions.id "+
		"order by assumptions.name, assumptions.id, data.year";

	public static String GET_ASSUM_COMBO_ANNOTATIONS = "select comments, annotations.added_on from annotations, assumptions where assumptions.assmptn_id = ? and annotations.assum_id = assumptions.id order by annotations.added_on desc";

	public static String GET_REV_ASSUMPTIONS_ITEM_SPECIFIC = "select assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.rev_seg_id, data.value, data.year, rev_segs.scen_id, alt_scen_list.delay_val, rev_segs.name, rev_segs.mkt_type, data.val_scope  "+
		"from alt_scen_list, alt_rev_model, rev_segs, assumptions, data where "+
		"assumptions.id = ? and " +
		"assumptions.rev_seg_id = rev_segs.id and "+
		"alt_rev_model.id = rev_segs.alt_rev_id and alt_rev_model.alt_id = alt_scen_list.alt_id and rev_segs.scen_id = alt_scen_list.scen_id and "+
		"data.assum_id = assumptions.id "+
		"order by alt_scen_list.delay_val, rev_segs.scen_id, rev_segs.mkt_type, assumptions.name, assumptions.id, data.year";

	public static String GET_OPCOST_ASSUMPTIONS_ITEM_SPECIFIC = "select assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.cost_li_id, data.value, data.year, cost_items.scen_id, alt_scen_list.delay_val, cost_items.name, cost_items.cost_cent_id, data.val_scope, cost_items.target_market  "+
		"from alt_scen_list, alt_opcost_model, cost_items, assumptions, data where "+
		"assumptions.id = ? and " +
		"assumptions.cost_li_id = cost_items.id and "+
		"alt_opcost_model.id = cost_items.alt_opcost_id and alt_opcost_model.alt_id = alt_scen_list.alt_id and cost_items.scen_id = alt_scen_list.scen_id and "+
		"data.assum_id = assumptions.id "+
		"order by alt_scen_list.delay_val, cost_items.scen_id, cost_items.cost_cent_id, assumptions.name, assumptions.id, data.year";


	public static String GET_DEV_ASSUMPTIONS_ITEM_SPECIFIC = "select assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.cost_li_id, data.value, data.year, cost_items.for_achieving_milestone, cost_items.name, cost_items.cost_cent_id, data.val_scope,  cost_items.target_market, cost_items.scen_id  "+
		"from cost_items, assumptions, data where "+
		"assumptions.id = ? and " +
		"assumptions.cost_li_id = cost_items.id and "+
		"data.assum_id = assumptions.id "+
		"order by cost_items.for_achieving_milestone, cost_items.cost_cent_id, assumptions.name, assumptions.id, data.year";

	public static String GET_FTE_ASSUMPTIONS_ITEM_SPECIFIC = "select assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.fte_item_id, data.value, data.year, fte_items.for_achieving_milestone, fte_items.name, fte_items.fte_head_id, data.val_scope,  fte_items.target_market  "+
		"from fte_items, assumptions, data where "+
		"assumptions.id = ? and " +
		"assumptions.fte_item_id = fte_items.id and "+
		"data.assum_id = assumptions.id "+
		"order by fte_items.for_achieving_milestone, fte_items.fte_head_id, assumptions.name, assumptions.id, data.year";

	public static String GET_MODEL_ASSUMPTIONS_ITEM_SPECIFIC = "select assmptn_id, assumptions.id, assumptions.name, is_multiyear, assumptions.alt_combined_id, data.value, data.year, data.val_scope  "+
		"from assumptions, data where "+
		"assumptions.id = ? and " +
		"data.assum_id = assumptions.id "+
		"order by assumptions.name, assumptions.id, data.year";

	public static String GET_ASSUM_HISTORY = "select assumptions.id, assmptn_id, users.name, added_on, is_multiyear, data.value, data.year, data.val_scope  from assumptions, data, users where assumptions.assmptn_id = ? and data.assum_id = assumptions.id and users.id = assumptions.added_by order by assumptions.id desc, data.year";

	public static String GET_ASSUM_ANNOTATIONS = "select comments, annotations.added_on from annotations, assumptions where assumptions.id = ? and annotations.id = assumptions.id order by annotations.added_on desc";

	public static String GET_COMMENTS = "select comments, feedback.added_on, users.name from feedback,users,assumptions where assumptions.assmptn_id = ? and feedback.assum_id = assumptions.id and users.id = feedback.by_user order by feedback.by_user desc";
	public static String CREATE_COMMENTS = Misc.G_DO_ORACLE ? "insert into feedback (comments, by_user, assum_id, added_on) values (?,?,?,sysdate)" : "insert into feedback (comments, by_user, assum_id, added_on) values (?,?,?,getdate())";
	public static String GET_ASSUMPTION_INFO = "select assumptions.name from assumptions where assumptions.id = ?";

	//workflow related
	public static String GET_APPROPRIATE_PLANS = "select plans.id, plans.type, plans.name from plans, prj_portfolio_map where prj_portfolio_map.prj_id = ? and prj_portfolio_map.port_node_id = plans.port_node_id and plans.type in (1,4) order by par_level asc, plans.type desc, plans.added_on desc";

//	public static String GET_WORKSPACE_TYPE_CURR_INFO = "select map_type, wspace_id, projects.status from pj_map_items, projects, workspaces where (wspace_id = ? or (pj_map_items.prj_id = ? and pj_map_items.map_type = 1)) and isdefault = 1 and projects.id = ? and workspaces.id = pj_map_items.wspace_id order by map_type desc"; //used to load some info - we want that if current & base use same workspace, then
//G_WATSON_031507_ROLLBACK
     public static String GET_WORKSPACE_TYPE_CURR_INFO = 
     "select map_type, wspace_id, projects.status, projects.curr_workflow_step, projects.menu_template from pj_map_items, projects, workspaces where (wspace_id = ? or (pj_map_items.prj_id = ? and pj_map_items.map_type = 1)) and isdefault = 1 and projects.id = ? and workspaces.id = pj_map_items.wspace_id order by map_type desc"     ; //used to load some info - we want that if current & base use same workspace, then


	//the calling code treats this as current (though currently it only cares for == 4 status. Therefore arranged in desc order

//	public static String GET_PLN_WF_STATUS_STEPS = "select pln_wf_steps.id, pln_wf_steps.pos_in_seq, pln_wf_steps.pln_wkf_st_desc, pln_wf_steps.name, pln_wf_steps.is_start, pln_wf_steps.is_lockdown, pln_wf_steps.is_publish "+
//		",pjstep.wspace_id, pjstep.step_id, pjstep.s_owner, pjstep.p_owner, pjstep.start_dt, pjstep.init_fin_dt, pjstep.finish_dt, pln_wf_steps.duration, pjstep.puid, pjstep.suid "+
//		"from pln_wf_steps, "+
//		"(select users1.id puid, users2.id suid, users2.name s_owner, users1.name p_owner, pln_wf_status.start_dt start_dt, pln_wf_status.init_fin_dt init_fin_dt, pln_wf_status.finish_dt finish_dt, pln_wf_status.pln_wkf_st_id step_id, pln_wf_status.wspace_id wspace_id "+
//		"from workspaces, pln_wf_status, users users1, users users2 where workspaces.id = ? and pln_wf_status.wspace_id = workspaces.id and users1.id = p_owner and users2.id = s_owner) pjstep "+
//		"where pln_wf_steps.plan_id = ? and pln_wf_steps.id = pjstep.step_id(+) order by pln_wf_steps.pos_in_seq asc";

	public static String GET_PLN_WF_STATUS_STEPS = "select pln_wf_steps.id, pln_wf_steps.pos_in_seq, pln_wf_steps.pln_wkf_st_desc, pln_wf_steps.name, pln_wf_steps.is_start, pln_wf_steps.is_lockdown, pln_wf_steps.is_publish "+
		" ,pln_wf_status.wspace_id, pln_wf_status.pln_wkf_st_id, users2.name, users1.name, pln_wf_status.start_dt, pln_wf_status.init_fin_dt, pln_wf_status.finish_dt, pln_wf_steps.duration, users1.id, users2.id "+
		" from pln_wf_steps, pln_wf_status, users users1, users users2 "+
          " where pln_wf_status.wspace_id = ? "+
          " and pln_wf_steps.plan_id = ? "+
          " and pln_wf_status.p_owner = users1.id "+
          " and pln_wf_status.s_owner = users2.id "+
          " and pln_wf_status.pln_wkf_st_id = pln_wf_steps.id "+
          " order by pln_wf_steps.pos_in_seq ";

	public static String GET_STDPLN_WF_STATUS_STEPS = "select pln_wf_steps.id, pln_wf_steps.pos_in_seq, pln_wf_steps.pln_wkf_st_desc, pln_wf_steps.name, pln_wf_steps.is_start, pln_wf_steps.is_lockdown, pln_wf_steps.is_publish "+
		",1, 1, users2.name, users1.name, null, null, null, pln_wf_steps.duration, pln_wf_steps.p_owner, pln_wf_steps.sec_owner, pln_wf_steps.menu_template  "+
		"from pln_wf_steps, users users1, users users2 "+
          "where pln_wf_steps.plan_id = ? and users1.id = p_owner and users2.id = sec_owner order by pln_wf_steps.pos_in_seq";

     public static String GET_PORTLEVEL_USERS = "select p_owner, s_owner, smallerset.seq_no, users1.name, users2.name from port_workflow_user_id, prj_portfolio_map, "+
" (select min(par_level) portl, port_workflow_user_id.seq_no seq_no "+
" from prj_portfolio_map, port_workflow_user_id "+
" where prj_portfolio_map.prj_id = ? "+
" and port_workflow_user_id.plan_id = ? "+
" and prj_portfolio_map.port_node_id = port_workflow_user_id.port_node_id "+
" group by port_workflow_user_id.seq_no) smallerset, "+
" users users1, users users2 "+
" where "+
" prj_portfolio_map.prj_id = ? "+
" and prj_portfolio_map.par_level = smallerset.portl "+
" and port_workflow_user_id.plan_id = ? "+
" and port_workflow_user_id.port_node_id = prj_portfolio_map.port_node_id "+
" and users1.id = p_owner and users2.id = s_owner "+
" order by smallerset.seq_no";
//NOT USED
	public static String GET_PJ_WF_STATUS_STEPS = Misc.G_DO_ORACLE ? "select pj_wf_steps.id, pj_wf_steps.pos_in_seq, pj_wf_steps.pj_wf_st_desc, pj_wf_steps.name, pj_wf_steps.is_start, pj_wf_steps.is_lockdown, pj_wf_steps.is_publish, p_users.name, s_users.name, pj_wf_steps.duration "+
		",pjstep.step_id, pjstep.s_owner, pjstep.p_owner, pjstep.start_dt, pjstep.init_fin_dt, pjstep.finish_dt, p_users.id, s_users.id, pjstep.puid, pjstep.suid, -1 "+
		"from pj_wf_steps, users p_users, users s_users, "+
		"(select users1.id puid, users2.id suid, users2.name s_owner, users1.name p_owner, pj_wf_status.start_dt start_dt, pj_wf_status.init_fin_dt init_fin_dt, pj_wf_status.finish_dt finish_dt, pj_wf_status.pj_wf_st_id step_id "+
		"from pj_wf_status, users users1, users users2 where pj_wf_status.wspace_id = ? and users1.id = p_owner and users2.id = sec_owner) pjstep "+
		"where pj_wf_steps.plan_id = ? and p_users.id=pj_wf_steps.p_owner and s_users.id=pj_wf_steps.s_owner and pj_wf_steps.id = pjstep.step_id(+) order by pj_wf_steps.pos_in_seq asc" :"select pj_wf_steps.id, pj_wf_steps.pos_in_seq, pj_wf_steps.pj_wf_st_desc, pj_wf_steps.name, pj_wf_steps.is_start, pj_wf_steps.is_lockdown, pj_wf_steps.is_publish, p_users.name, s_users.name, pj_wf_steps.duration "+
		",pjstep.step_id, pjstep.s_owner, pjstep.p_owner, pjstep.start_dt, pjstep.init_fin_dt, pjstep.finish_dt, p_users.id, s_users.id, pjstep.puid, pjstep.suid, -1 "+
		"from pj_wf_steps, users p_users, users s_users, "+
		"(select users1.id puid, users2.id suid, users2.name s_owner, users1.name p_owner, pj_wf_status.start_dt start_dt, pj_wf_status.init_fin_dt init_fin_dt, pj_wf_status.finish_dt finish_dt, pj_wf_status.pj_wf_st_id step_id "+
		"from pj_wf_status, users users1, users users2 where pj_wf_status.wspace_id = ? and users1.id = p_owner and users2.id = sec_owner) pjstep "+
		"where pj_wf_steps.plan_id = ? and p_users.id=pj_wf_steps.p_owner and s_users.id=pj_wf_steps.s_owner and pj_wf_steps.id *= pjstep.step_id order by pj_wf_steps.pos_in_seq asc";

	public static String GET_SPECIAL_WORKSPACE = "select wspace_id, map_type from pj_map_items where pj_map_items.prj_id = ? and map_type in (1,2,3) and isdefault=1";

	public static String SET_SP_TO_NON_DEF_PJ  = "update pj_map_items set isdefault = 0 where pj_map_items.map_type = ? and pj_map_items.isdefault = 1 and pj_map_items.wspace_id = ?";
	public static String SET_SP_TO_NON_DEF_ALT = "update alt_map_items set isdefault = 0 where alt_map_items.map_type = ? and alt_map_items.isdefault = 1 and alt_map_items.wspace_id = ?";
	//public static String MAKE_SP_AS_SP_PJ = Misc.G_DO_ORACLE ? "insert into pj_map_items (date_created, isdefault, map_type, id, pj_basic_id, prj_id, wspace_id, alt_list_id) "+
	//	"(select sysdate, 1, ?, seq_pj_map_items.nextval, pj_basic_id, prj_id, wspace_id, alt_list_id from pj_map_Items where pj_map_items.wspace_id = ? and pj_map_items.isdefault = 1 and pj_map_items.map_type = ?)" : "insert into pj_map_items (date_created, isdefault, map_type, id, pj_basic_id, prj_id, wspace_id, alt_list_id) "+
	//	"(select getdate(), 1, ?, intelli.getNextVal(0), pj_basic_id, prj_id, wspace_id, alt_list_id from pj_map_Items where pj_map_items.wspace_id = ? and pj_map_items.isdefault = 1 and pj_map_items.map_type = ?)";
	//public static String MAKE_SP_AS_SP_ALT = Misc.G_DO_ORACLE ? "insert into alt_map_items (isdefault, map_type, id, alt_date_id, alt_model_id, alt_id, alt_task_id, alt_basic_id, alt_profil_id, wspace_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id ) "+
	//	"(select 1, ?, seq_alt_map_items.nextval, alt_date_id, alt_model_id, alt_id, alt_task_id, alt_basic_id, alt_profil_id, wspace_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from alt_map_Items where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and alt_map_items.map_type = ?)" : "insert into alt_map_items (isdefault, map_type, id, alt_date_id, alt_model_id, alt_id, alt_task_id, alt_basic_id, alt_profil_id, wspace_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id ) "+
	//	"(select 1, ?, intelli.getNextVal(0), alt_date_id, alt_model_id, alt_id, alt_task_id, alt_basic_id, alt_profil_id, wspace_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from alt_map_Items where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and alt_map_items.map_type = ?)";
	public static String MAKE_PROP_AS_CURRENT_PJ = "update pj_map_items set map_type = 1 where pj_map_items.wspace_id = ? and pj_map_items.isdefault = 1";
	public static String MAKE_PROP_AS_CURRENT_ALT = "update alt_map_items set map_type = 1 where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1";

	public static String UPD_CURR_AS_SP_PJ = "update pj_map_items set map_type = ? where pj_map_items.wspace_id = ? and pj_map_items.isdefault = 1 and map_type=1";
	public static String UPD_CURR_AS_SP_ALT = "update alt_map_items set map_type = ? where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and map_type=1";

	public static String UPD_WKSP_AS_SP_PJ = "update pj_map_items set map_type = ? where pj_map_items.wspace_id = ? and pj_map_items.isdefault = 1 and map_type=4";
	public static String UPD_WKSP_AS_SP_ALT = "update alt_map_items set map_type = ? where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and map_type=4";


	public static String CREATE_PJ_WF_STATUS = Misc.G_DO_ORACLE ? "insert into pj_wf_status (p_owner, sec_owner, duration, start_dt, finish_dt, init_finish_dt, wspace_id, pj_wf_st_id, id) values (?,?,?,?,?,?,?,?, seq_project_wf_status.nextval)" : "insert into pj_wf_status (p_owner, sec_owner, duration, start_dt, finish_dt, init_finish_dt, wspace_id, pj_wf_st_id, id) values (?,?,?,?,?,?,?,?, intelli.getNextVal(0))";

//	public static String UPDATE_PLN_WF_STATUS = "update pln_wf_status set p_owner=?, s_owner=?, finish_dt=?, start_dt=? where pln_wkf_st_id = ? and wspace_id = ?";
	public static String UPDATE_PLN_WF_STATUS = "update (select pln_wf_status.p_owner, pln_wf_status.s_owner, pln_wf_status.finish_dt, pln_wf_status.start_dt from pln_wf_status, "+
" pln_wf_steps "+
" where pln_wf_status.pln_wkf_st_id = pln_wf_steps.id "+
" and pln_wf_steps.plan_id = ? "+
" and pln_wf_status.wspace_id = ? "+
" and pln_wf_steps.pos_in_seq = ?) set p_owner=?, s_owner=?, finish_dt=?, start_dt=? ";

	public static String UPDATE_PJ_WF_STATUS = "update pj_wf_status set p_owner=?, sec_owner=?, finish_dt=?, start_dt=? where pj_wf_st_id = ? and wspace_id = ?";

	public static String SET_LOCKDOWN_STATE = "update workspaces set is_lockdown=? where id = ?";
  //TODO_INQUERY_IMPR
  public static String UPDATE_CURR_WORKFLOW_STATE = Misc.G_DO_ORACLE ? 
  "update projects set curr_workflow_step = ?, in_workflow_comment=?, in_workflow_date=sysdate where projects.id in (select prj_id from workspaces where workspaces.id = ?)" 
  : 
  "update projects set curr_workflow_step = ?, in_workflow_comment=?, in_workflow_date=getdate() from workspaces where workspaces.id=? and projects.id = workspaces.prj_id ";

	//  public static String GET_PORT_WKSP_MAP = "select port_map_items.port_wksp_id, port_map_items.map_type, port_wksps.name, port_wksps.port_wksp_desc, port_rset.id, port_rset.name, port_rset.description from port_map_items, port_wksps, port_rset where port_map_items.map_type in (1,2) and port_map_items.isdefault=1 and port_map_items.port_node_id=1 and port_wksps.id = port_map_items.port_wksp_id and port_rset.port_wksp_id_rec_rset=port_map_items.port_wksp_id";
	public static String GET_PORT_WKSP_MAP = "select port_wksps.id, port_wksps.map_type from  port_wksps where port_wksps.map_type in (1,2,7,11, 101) and port_wksps.isdefault=1";
	public static String REMOVE_PORT_RSETS = "delete from port_rset where port_wksp_id=?";

	public static String SET_PRIM_ALT_AT_ALT_LEVEL = "update alternatives set is_primary=? where prj_id=? and id=?";
	public static String SET_NOPRIM_ALT_AT_ALT_LEVEL = "update alternatives set is_primary=0 where prj_id=? and id <> ? and is_primary=1";
/* 102307
	public static String GET_PORT_REQ_MINUS_AV_DW = Misc.G_DO_ORACLE ? "select distinct alt_map_items.alt_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, pj_map_items.prj_id, pj_map_items.pj_basic_id, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from "+
		"pj_map_items, alt_map_items, port_wksp_asso where  "+
		"port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.use_latest = 1 and "+
		"port_wksp_asso.map_type = 4 and "+
		"pj_map_items.wspace_id = port_wksp_asso.wspace_id and "+
		"pj_map_items.isdefault = 1 and "+
		"alt_map_items.wspace_id = port_wksp_asso.wspace_id and "+
		"alt_map_items.isdefault = 1 "+
		" UNION "+
		"select alt_map_items.alt_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, pj_map_items.prj_id, pj_map_items.pj_basic_id, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from "+
		"pj_map_items, alt_map_items, port_wksp_asso where  "+
		"port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.use_latest = 1 and "+
		"port_wksp_asso.map_type <> 4 and "+
		"pj_map_items.prj_id = port_wksp_asso.prj_id and "+
		"pj_map_items.map_type = port_wksp_asso.map_type and "+
		"pj_map_items.isdefault = 1 and "+
		"alt_map_items.wspace_id = pj_map_items.wspace_id and "+
		"alt_map_items.isdefault = 1 "+
		" UNION "+
		"select alt_map_items.alt_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, pj_map_items.prj_id, pj_map_items.pj_basic_id, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from "+
		"labels, pj_map_items, alt_map_items, port_wksp_asso where "+
		"port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.use_latest = 0 and "+
		"port_wksp_asso.label_id = labels.id and "+
		"pj_map_items.id = labels.pj_map_id and "+
		"alt_map_items.label_id = labels.id "+
		" MINUS "+
		"(select alt_id, ver_alt_mstone_id, ver_alt_model_id, ver_alt_basic_id, ver_alt_profile_id, pj_id, ver_prj_basic_id, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rating_id  from "+
		"dw_alt_short_info where port_rs_id = ? "+
		") order by alt_id asc " : 
		"select distinct alt_map_items.alt_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, pj_map_items.prj_id, pj_map_items.pj_basic_id, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from "+
		"pj_map_items, alt_map_items, port_wksp_asso where  "+
		"port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.use_latest = 1 and "+
		"port_wksp_asso.map_type = 4 and "+
		"pj_map_items.wspace_id = port_wksp_asso.wspace_id and "+
		"pj_map_items.isdefault = 1 and "+
		"alt_map_items.wspace_id = port_wksp_asso.wspace_id and "+
		"alt_map_items.isdefault = 1 "+
		" UNION "+
		"select alt_map_items.alt_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, pj_map_items.prj_id, pj_map_items.pj_basic_id, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from "+
		"pj_map_items, alt_map_items, port_wksp_asso where  "+
		"port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.use_latest = 1 and "+
		"port_wksp_asso.map_type <> 4 and "+
		"pj_map_items.prj_id = port_wksp_asso.prj_id and "+
		"pj_map_items.map_type = port_wksp_asso.map_type and "+
		"pj_map_items.isdefault = 1 and "+
		"alt_map_items.wspace_id = pj_map_items.wspace_id and "+
		"alt_map_items.isdefault = 1 "+
		" UNION "+
		"select alt_map_items.alt_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, pj_map_items.prj_id, pj_map_items.pj_basic_id, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from "+
		"labels, pj_map_items, alt_map_items, port_wksp_asso where "+
		"port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.use_latest = 0 and "+
		"port_wksp_asso.label_id = labels.id and "+
		"pj_map_items.id = labels.pj_map_id and "+
		"alt_map_items.label_id = labels.id "+
		" and Not Exists( "+
		"(select alt_id, ver_alt_mstone_id, ver_alt_model_id, ver_alt_basic_id, ver_alt_profile_id, pj_id, ver_prj_basic_id, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rating_id  from "+
		"dw_alt_short_info where port_rs_id = ? "+
		")) order by alt_id asc ";
    */
	//This query is substantially similar to the above and tells the versions to be used for an alt
	public static String GET_PORT_REQ_FOR_ALT =
		"select distinct alt_map_items.alt_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, pj_map_items.prj_id, pj_map_items.pj_basic_id, alt_map_items.alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from "+
		"pj_map_items, alt_map_items, port_wksp_asso where  "+
		"port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.prj_id = ? and "+
		"port_wksp_asso.use_latest = 1 and "+
		"port_wksp_asso.map_type = 4 and "+
		"pj_map_items.wspace_id = port_wksp_asso.wspace_id and "+
		"pj_map_items.isdefault = 1 and "+
		"alt_map_items.wspace_id = port_wksp_asso.wspace_id and "+
		"alt_map_items.alt_id = ? and "+
		"alt_map_items.isdefault = 1 "+
		" UNION "+
		"select alt_map_items.alt_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, pj_map_items.prj_id, pj_map_items.pj_basic_id, alt_map_items.alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id   from "+
		"pj_map_items, alt_map_items, port_wksp_asso where  "+
		"port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.prj_id = ? and "+
		"port_wksp_asso.use_latest = 1 and "+
		"port_wksp_asso.map_type <> 4 and "+
		"pj_map_items.prj_id = port_wksp_asso.prj_id and "+
		"pj_map_items.map_type = port_wksp_asso.map_type and "+
		"pj_map_items.isdefault = 1 and "+
		"alt_map_items.wspace_id = pj_map_items.wspace_id and "+
		"alt_map_items.alt_id = ? and "+
		"alt_map_items.isdefault = 1 "+
		" UNION "+
		"select alt_map_items.alt_id, alt_map_items.alt_date_id, alt_map_items.alt_model_id, alt_map_items.alt_basic_id, alt_map_items.alt_profil_id, pj_map_items.prj_id, pj_map_items.pj_basic_id,  alt_map_items.alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id   from "+
		"labels, pj_map_items, alt_map_items, port_wksp_asso where "+
		"port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.prj_id = ? and "+
		"port_wksp_asso.use_latest = 0 and "+
		"port_wksp_asso.label_id = labels.id and "+
		"pj_map_items.id = labels.pj_map_id and "+
		"alt_map_items.alt_id = ? and "+
		"alt_map_items.label_id = labels.id ";
/*102307
	public static String GET_STATUS_FOR_ALT_FROM_ALL = Misc.G_DO_ORACLE ? "select alternatives.id, alternatives.is_primary, projects.status, dw_alt_short_info.is_default_alt, dw_alt_short_info.fund_status from "+
		"port_wksp_asso, alternatives, dw_alt_short_info, projects where port_wksp_asso.port_wksp_id = ? and port_wksp_asso.prj_id = projects.id and alternatives.prj_id = port_wksp_asso.prj_id and dw_alt_short_info.port_rs_id(+) = ? and dw_alt_short_info.alt_id(+)=alternatives.id order by alternatives.id" :"select alternatives.id, alternatives.is_primary, projects.status, dw_alt_short_info.is_default_alt, dw_alt_short_info.fund_status from "+
		"port_wksp_asso, alternatives, dw_alt_short_info, projects where port_wksp_asso.port_wksp_id = ? and port_wksp_asso.prj_id = projects.id and alternatives.prj_id = port_wksp_asso.prj_id and dw_alt_short_info.port_rs_id =* ? and dw_alt_short_info.alt_id =* alternatives.id order by alternatives.id";

	public static String DEL_DW_NON_REQ_ALT = "delete from dw_alt_short_info where port_rs_id = ? and alt_id not in (select alternatives.id from alternatives, port_wksp_asso where port_wksp_asso.port_wksp_id=? and alternatives.prj_id = port_wksp_asso.prj_id)";

	public static String SYNC_PORT_RESULTS_WITH_DW_INSERT = Misc.G_DO_ORACLE ? "insert into port_results (id, alt_id, port_rs_id, fund_status, tot_delay_mon) "+
		"select seq_detailed_port_rsets.nextval, diff.aid, ?, dw_alt_short_info.fund_status, 0 from "+
		"((select dw_alt_short_info.alt_id aid from dw_alt_short_info where port_rs_id = ?) minus (select port_results.alt_id aid from port_results where port_rs_id=?)) diff, dw_alt_short_info where dw_alt_short_info.port_rs_id = ? and diff.aid = dw_alt_short_info.alt_id " : "insert into port_results (id, alt_id, port_rs_id, fund_status, tot_delay_mon) "+
		"select intelli.getNextVal(0), diff.aid, ?, dw_alt_short_info.fund_status, 0 from "+
		"((select dw_alt_short_info.alt_id aid from dw_alt_short_info where port_rs_id = ? and Not Exists (select port_results.alt_id aid from port_results where port_rs_id=?)) diff, dw_alt_short_info where dw_alt_short_info.port_rs_id = ? and diff.aid = dw_alt_short_info.alt_id ";

	public static String SYNC_PORT_RESULTS_WITH_DW_DEL = "delete from port_results where port_rs_id = ? and alt_id not in (select alt_id from dw_alt_short_info where port_rs_id=?)";
*/
	public static String COUNT_ALTERNATIVES = "select count(*) from alternatives where prj_id=?";
/*102307
	public static String COPY_DW_ALT_SHORT_INFO = Misc.G_DO_ORACLE ? "insert into dw_alt_short_info (id, port_rs_id, temp_copy_of, pj_id, ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, alt_id, ver_alt_model_id, ver_prj_basic_id, fund_status, is_default_alt, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rating_id ) "+
		"(select seq_dw_alt_short_info.nextval, ?, id, pj_id, ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, alt_id, ver_alt_model_id, ver_prj_basic_id, fund_status, is_default_alt, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rating_id  from dw_alt_short_info where port_rs_id=?)" : "insert into dw_alt_short_info (id, port_rs_id, temp_copy_of, pj_id, ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, alt_id, ver_alt_model_id, ver_prj_basic_id, fund_status, is_default_alt, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rating_id ) "+
		"(select intelli.getNextVal(0), ?, id, pj_id, ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, alt_id, ver_alt_model_id, ver_prj_basic_id, fund_status, is_default_alt, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rating_id  from dw_alt_short_info where port_rs_id=?)";

	public static String COPY_DW_REV = "insert into dw_rev (val, expected, time_dim_val, dw_alt_si_id, rev_seg_id, val_scope) "+
		"(select dw_rev.val, dw_rev.expected, dw_rev.time_dim_val, dw_alt_short_info.id, dw_rev.rev_seg_id, val_scope from dw_rev, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_rev.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";

	public static String COPY_DW_COST = "insert into dw_cost (val, expected, time_dim_val, dw_alt_si_id, cost_li_id, val_scope) "+
		"(select dw_cost.val, dw_cost.expected, dw_cost.time_dim_val, dw_alt_short_info.id, dw_cost.cost_li_id, val_scope from dw_cost, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_cost.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";

	public static String COPY_DW_FTE = "insert into dw_fte (val, expected, time_dim_val, dw_alt_si_id, fte_item_id, val_scope) "+
		"(select dw_fte.val, dw_fte.expected, dw_fte.time_dim_val, dw_alt_short_info.id, dw_fte.fte_item_id, val_scope from dw_fte, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_fte.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";

	//TODO-CHECK check for VAL_SCOPE
	public static String COPY_DW_COUNT = "insert into dw_count (val, expected, time_dim_val, dw_alt_si_id, post_milestone, ispostlaunch, normcompletion) "+
		"(select dw_count.val, dw_count.expected, dw_count.time_dim_val, dw_alt_short_info.id, dw_count.post_milestone, dw_count.ispostlaunch, dw_count.normcompletion from dw_count, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_count.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";

	public static String COPY_DW_OTHER = "insert into dw_other_info (delay_imposed, npv_dev_cost, dev_cost_cat, npv, peak_rev, npv_at_launch, risk_cat, tech_risk, npv_at_launch_var, npv_var, peak_rev_cat, init_launch_date, dw_alt_si_id) "+
		"(select delay_imposed, npv_dev_cost, dev_cost_cat, npv, peak_rev, npv_at_launch, risk_cat, tech_risk, npv_at_launch_var, npv_var, peak_rev_cat, init_launch_date, dw_alt_short_info.id from dw_other_info, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_other_info.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";

	public static String COPY_DW_REV_ERR = "insert into dw_rev_err (val_im, val_re, expected_im, expected_re, time_dim_val, err_pt_ran_id, dw_alt_si_id, rev_seg_id) "+
		"(select val_im, val_re, expected_im, expected_re, time_dim_val, err_pt_ran_id, dw_alt_short_info.id, rev_seg_id from dw_rev_err, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_rev_err.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";

	public static String COPY_DW_COST_ERR = "insert into dw_cost_err (val_im, val_re, expected_im, expected_re, time_dim_val, err_pt_ran_id, dw_alt_si_id, cost_li_id) "+
		"(select val_im, val_re, expected_im, expected_re, time_dim_val, err_pt_ran_id, dw_alt_short_info.id, cost_li_id from dw_cost_err, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_cost_err.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";

	public static String COPY_DW_FTE_ERR = "insert into dw_fte_err (val_im, val_re, expected_im, expected_re, time_dim_val, err_pt_ran_id, dw_alt_si_id, fte_item_id) "+
		"(select val_im, val_re, expected_im, expected_re, time_dim_val, err_pt_ran_id, dw_alt_short_info.id, fte_item_id from dw_fte_err, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_fte_err.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";

	public static String COPY_DW_COUNT_ERR = "insert into dw_count_err (val_im, val_re, expected_im, expected_re, time_dim_val, err_pt_ran_id, dw_alt_si_id) "+
		"(select val_im, val_re, expected_im, expected_re, time_dim_val, err_pt_ran_id, dw_alt_short_info.id from dw_count_err, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_count_err.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";

	public static String COPY_DW_NPV_ERR = "insert into dw_npv_err (npv_dev_cost_re, npv_im, npv_at_launch_re, npv_re, npv_dev_cost_im, npv_at_launch_im, err_pt_ran_id, dw_alt_si_id) "+
		"(select npv_dev_cost_re, npv_im, npv_at_launch_re, npv_re, npv_dev_cost_im, npv_at_launch_im, err_pt_ran_id, dw_alt_short_info.id from dw_npv_err, dw_alt_short_info where dw_alt_short_info.temp_copy_of = dw_npv_err.dw_alt_si_id and dw_alt_short_info.port_rs_id=?)";
*/
	public static String GET_PRJ_WKSP_FOR_PORT_WKSP = "select pj_map_items.wspace_id, pj_map_items.prj_id from pj_map_items, port_wksp_asso where port_wksp_asso.port_wksp_id = ? and pj_map_items.map_type = ? and pj_map_items.prj_id=port_wksp_asso.prj_id and pj_map_items.isdefault=1";

	public static String SET_LABEL_PORT_WKSP = "update port_wksp_asso set use_latest=0, map_type=?, label_id = ? where wspace_id=? and port_wksp_id = ?";
	public static String COPY_PORT_WKSP_DETAIL = "insert into port_wksp_asso (map_type, use_latest, wspace_id, label_id, port_wksp_id, prj_id) (select map_type, use_latest, wspace_id, label_id, ?, prj_id from port_wksp_asso where port_wksp_id = ?)";

	public static String GET_PORT_WKSP_INFO = "select cre_for_port_node_id, port_wksps.name, port_rset.id, port_rset.name, port_rset.description from port_wksps,port_rset where port_wksps.id = ? and port_rset.port_wksp_id_rec_rset = port_wksps.id";

	public static String GET_PRJ_WKSP_FOR_PORT_WKSP_PROP = "select wspace_id, prj_id from port_wksp_asso where port_wksp_id = ? and map_type=4";
  //TODO_INQUERY .... not imp
	public static String DEL_SPECIFIC_PORT_RSET = "delete from port_results where port_rs_id=? and alt_id in (select alternatives.id from alternatives, port_wksp_asso where port_wksp_asso.port_wksp_id = ? and alternatives.prj_id = port_wksp_asso.prj_id)";
  /*102307
	public static String DEL_SPECIFIC_DW        = "delete from dw_alt_short_info where port_rs_id = ? and pj_id in (select prj_id from port_wksp_asso where port_wksp_id = ?)";
  */
  //TODO_INQUERY .... not imp
	public static String DEL_SPECIFIC_PORT_WKSP = "delete from port_wksp_asso where port_wksp_id = ? and prj_id in (select prj_id from port_wksp_asso where port_wksp_id = ?)";
	public static String ADD_SPECIFIC_PORT_WKSP = "insert into port_wksp_asso (map_type, use_latest, wspace_id, label_id, port_wksp_id, prj_id) (select map_type, use_latest, wspace_id, label_id, ?, prj_id from port_wksp_asso where port_wksp_id =?)";
	public static String SET_MAPTYPE_PORT_WKSP = "update port_wksp_asso set map_type=?, use_latest = ? where port_wksp_asso.port_wksp_id = ?";

	public static String SET_PORTWKSP_MAP_TYPE = "update port_wksps set map_type=?, isdefault=? where port_wksps.id = ?";
	public static String DELETE_PORTWKSP_SPECIAL = "update port_wksps set isdefault=0 where port_wksps.map_type = ?";
		public static String DELETE_PORTRESULTS_BYWKSP = Misc.G_DO_ORACLE ?
		"delete from (select port_results.id from port_results, port_rset where port_results.port_rs_id = port_rset.id and port_rset.port_wksp_id = ?)"
		:
		"delete from port_results where port_results.id in " +
		"(select port_results.id from port_results, port_rset where port_results.port_rs_id = port_rset.id " +
		"and port_rset.port_wksp_id = ?)";

	public static String DELETE_PORT_RSET_BYWKSP = "delete from port_rset where port_wksp_id = ?";
	public static String DELETE_DESIREDVER_BYWKSP = "delete from port_desired_ver where port_wksp_id = ?";
	public static String DELETE_PORTASSO_BYWKSP = "delete from port_wksp_asso where port_wksp_id=?";
	public static String DELETE_PORT_WKSP = "delete from port_wksps where id = ?";
	public static String CHANGE_PORT_WKSP_TYPE = "update port_wksps set map_type = ? where map_type = ? and isdefault=1";


	public static String GET_PRIMARY_ALTERNATIVE = "select alternatives.id, name from alternatives where prj_id=? and is_primary=1";
	public static String GET_PJ_WF_STEPS_COUNT="select count(*) from pj_wf_steps where plan_id=?";

	public static String GET_PORT_WORKSPACE_FROM_RSET = "select port_wksp_id from port_rset where id = ?";
	public static String GET_USER_PASSWORD_MATCH = "select id, name from users where username=? and password=? and isactive=1";
	public static String GET_USER_PASSWORD_MATCH_EXT = "select id, name, home_server, home_port, home_uid, (case when password=? then 1 else 0 end) mtc from users where username=? and isactive=1";

	public static String GET_MILESTONE_BY_ALT_DATE_ID = "select mstn_id, start_date, finish_dt, succ_prob, target_market, variance from milestones where alt_date_id = ? order by mstn_id";
	public static String GET_MILESTONES = "select mstn_id, start_date, finish_dt, succ_prob, target_market, variance from milestones,alt_map_items where milestones.alt_date_id = alt_map_items.alt_date_id and alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 order by mstn_id";

	public static String GET_PLAN_LIST = "select id, name from plans where type in (1,4) order by type desc, added_on desc";
	public static String GET_PLAN_INFO = "select id, name, description, type, year, port_node_id, start_date from plans where id=?";
/*
	public static String VERIFY_VER_EQUALITY = "select id from dw_alt_short_info where port_rs_id = ? and alt_id = ? and ver_prj_basic_id = ? and ver_alt_basic_id = ? and ver_alt_mstone_id = ? and ver_alt_profile_id = ? "+
		"and ver_alt_fte_id = ? and ver_alt_rev_id=? and ver_alt_devcost_id=? and ver_alt_opcost_id=? and ver_alt_work_id = ? and ver_alt_rating_id=?";
	public static String UPDATE_DW_ALT_FUND_STATUS = "update dw_alt_short_info set fund_status = ?, is_default_alt=? where port_rs_id=? and alt_id=?";
  */

	public static String GET_ALL_ROLES = "select id, name, role_desc, scope, external_code from role"; //rajeev 022108 must match GET_ROLE_LIMITED
	public static String GET_ALL_USERS = "select id, name, email from users where isActive=1 order by UPPER(name)";
	public static String GET_USER_DETAIL = "select id, name, email, username, phone, password from users where id = ?";
	public static String GET_USER_ROLES_ALL = "select user_roles.role_id from user_roles, user_roles_scope where user_roles.user_1_id = ? and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.all_scope is not null order by role_id";
	public static String GET_USER_ROLES_PRJ = "select user_roles.role_id, projects.id, projects.name from user_roles, user_roles_scope, projects where user_roles.user_1_id = ? and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.prj_id = projects.id  order by role_id";
	public static String GET_USER_ROLES_PORT = "select user_roles.role_id, user_roles_scope.port_node_id from user_roles, user_roles_scope where user_roles.user_1_id = ? and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.port_node_id is not null  order by role_id";

	public static String GET_USER_LIST_FOR_PRJ = "select temp.u_id, users.name, users.email from users, "+
		"((select distinct user_roles.user_1_id u_id from user_roles,role where role.id = user_roles.role_id and role.scope = 1 ) "+
		"union     (select distinct user_roles.user_1_id u_id from user_roles,role,user_roles_scope, prj_portfolio_map where role.id = user_roles.role_id and role.scope = 2 and user_roles_scope.user_role_id = user_roles.id and prj_portfolio_map.prj_id = ? and user_roles_scope.port_node_id = prj_portfolio_map.prj_id) "+
		"union     (select distinct user_roles.user_1_id u_id from user_roles,role,user_roles_scope where role.id = user_roles.role_id and role.scope = 3 and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.prj_id = ? and (user_roles_scope.wspace_id is null or user_roles_scope.wspace_id = ?)) "+
		") temp where temp.u_id = users.id";
	public static String GET_USER_ROLES_PRJ_SEL = Misc.G_DO_ORACLE ? "select distinct user_roles.role_id, user_roles_scope.wspace_id from user_roles, user_roles_scope where user_roles.user_1_id = ? and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.prj_id = ?  and (user_roles_scope.wspace_id is null or user_roles_scope.wspace_id = ?) order by role_id, user_roles_scope.wspace_id nulls first" 
                                                                 : "select distinct user_roles.role_id, user_roles_scope.wspace_id from user_roles, user_roles_scope where user_roles.user_1_id = ? and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.prj_id = ?  and (user_roles_scope.wspace_id is null or user_roles_scope.wspace_id = ?) order by role_id, user_roles_scope.wspace_id asc";
	public static String GET_USER_ROLES_PORT_SEL_FOR_PRJ = "select user_roles.role_id, user_roles_scope.port_node_id from user_roles, user_roles_scope, prj_portfolio_map where user_roles.user_1_id = ? and user_roles_scope.user_role_id = user_roles.id and prj_portfolio_map.prj_id = ? and user_roles_scope.port_node_id = prj_portfolio_map.port_node_id order by role_id";

	public static String GET_USER_LIST_FOR_PORT = "select temp.u_id, users.name, users.email from users, "+
		"((select distinct user_roles.user_1_id u_id from user_roles,role where role.id = user_roles.role_id and role.scope = 1 ) "+
		"union     (select distinct user_roles.user_1_id u_id from user_roles,role,user_roles_scope, prj_portfolio_map where role.id = user_roles.role_id and role.scope = 2 and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.port_node_id = ?) "+
		") temp where temp.u_id = users.id";
	public static String GET_USER_ROLES_PORT_SEL_FOR_PORT = "select user_roles.role_id, user_roles_scope.port_node_id from user_roles, user_roles_scope where user_roles.user_1_id = ? and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.port_node_id = ? order by role_id";

	public static String GET_ROLE_DETAIL = "select id, name, role_desc, scope, external_code from role where id=?"; //rajeev 022108
	public static String GET_PRIV_LIST_FOR_ROLE = "select priv_id from role_privs where role_id=?";

	public static String CREATE_USER = Misc.G_DO_ORACLE ? "insert into users (id, name, password, username, email, phone, isActive) values (?,?,?,?,?,?, 1)"
                                                      : "insert into users (name, password, username, email, phone, isActive) values (?,?,?,?,?, 1)";
	public static String UPDATE_USER = "update users set name=?, password=?, email=?, phone=?, isactive=1 where id=?";
	public static String DELETE_USER = "update users set isActive=0 where id=?";
	public static String INSERT_USER_ROLE = Misc.G_DO_ORACLE ? "insert into user_roles (id, user_1_id, role_id) values (?,?,?)"
                                                           :"insert into user_roles (user_1_id, role_id) values (?,?)";
//TODO_INQUERY ... not imp
	public static String DROP_USER_ALL_ROLE = "delete from user_roles where role_id in (select id from role where scope = 1) and user_1_id=?";
	public static String DELETE_ROLE_FOR_USER = "delete from user_roles where user_1_id = ?";

	public static String CREATE_ROLE = Misc.G_DO_ORACLE ? "insert into role (id, name, role_desc, scope, external_code) values (?,?,?,?,?)" //rajeev 022108
                                                      :"insert into role (name, role_desc, scope, external_code) values (?,?,?,?)";
	public static String UPDATE_ROLE = "update role set name=?, role_desc=?, external_code=? where id=?";
	public static String DROP_ROLE_PRIV = "delete from role_privs where role_id=?";
	public static String INSERT_ROLE_PRIV = "insert into role_privs (role_id, priv_id) values (?,?)";
	public static String DELETE_ROLE = "delete from role where id=?";
	//added by balwant for customization reports
	public static String GET_ALL_DESENDENT_PORT_NODE = "select leaf.id from port_nodes leaf join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)";
	public static String GET_MENU_REPORT_INFORMATION = "select report_definitions.id,page_context,optional_Menu_Name,name,title,help,for_port_node_id,for_user_id,menu_placeholder_id,menu_tag,component_file,menu_master.id as menumasterId from report_definitions join menu_report_definition on (report_definitions.id = menu_report_definition.report_definition_id ) join menu_master_report_definition on (menu_master_report_definition.report_definition_id = report_definitions.id) join menu_master on (menu_master.id = menu_master_report_definition.menu_master_id) where status = 1 and type = 0";
//TODO_INQUERY ... not imp
		public static String DROP_USER_PRJ_ROLE = Misc.G_DO_ORACLE ?
		"delete from (select user_roles.id from user_roles, user_roles_scope where user_roles.user_1_id = ? and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.prj_id = ? and (user_roles_scope.wspace_id is null or user_roles_scope.wspace_id = ?))"
		:
		"delete from user_roles where user_roles.id in (select user_roles.id from user_roles, " +
		"user_roles_scope where user_roles.user_1_id = ? and user_roles_scope.user_role_id = user_roles.id " +
		"and user_roles_scope.prj_id = ? " +
		"and (user_roles_scope.wspace_id is null or user_roles_scope.wspace_id = ?))";
//TODO_INQUERY    .. not imp
	public static String DROP_USER_PORT_ROLE = Misc.G_DO_ORACLE ?
		"delete from (select user_roles.id from user_roles, user_roles_scope where (user_roles.user_1_id = ? or ? is null) and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.port_node_id = ?)"
		:
			"  delete from user_roles using user_roles join user_roles_scope on (user_roles.id = user_roles_scope.user_role_id) where (user_roles.user_1_id = ? or ? is null) and user_roles_scope.port_node_id=?";
		//TODO - this is for MSSQL, "delete from user_roles where user_roles.id in (select user_roles.id from user_roles, " +
		//"user_roles_scope where (user_roles.user_1_id = ? or ? is null) " +
		//"and user_roles_scope.user_role_id = user_roles.id and user_roles_scope.port_node_id = ?)";

	public static String INSERT_USER_ROLE_SCOPE = "insert into user_roles_scope (user_role_id, all_scope, port_node_id, prj_id, wspace_id, grantable) values (?,?,?,?,?,?)"; //rajeev 022608

	//public static String GET_PROFILE_OUTCOME_INFO = "select port_results.alt_id, prof_outcomes.profe_case_ty, prof_outcomes.probability from port_results, prof_outcomes where port_rs_id=? and prof_outcomes.alt_profil_id = ver_alt_profile_id order by alt_id, profe_case_ty";
	public static String GET_PROFILE_OUTCOME_INFO = "select port_results.alt_id, alt_scen_list.scen_id, alt_scen_list.scen_prob, alt_scen_list.scen_name, alt_scen_list.scen_desc, alt_scen_list.delay_val from port_results, alt_scen_list where port_rs_id=? and alt_scen_list.alt_id = port_results.alt_id and (port_results.prj_id = ? or ? is null) order by port_results.alt_id, alt_scen_list.delay_val, alt_scen_list.scen_id";

	public static String GET_DELAY_CASE_INFO = "select distinct port_results.alt_id, alt_scen_list.delay_val from port_results, alt_scen_list where port_rs_id=? and alt_scen_list.alt_id = port_results.alt_id order by port_results.alt_id, delay_val";

	public static String INSERT_PRJ_PORTFOLIO_MAP = "insert into prj_portfolio_map (prj_id, port_node_id, par_level, parent_inserted) values (?,?,?,1)";
	public static String DEL_PRJ_PORTFOLIO_MAP_BY_PRJ = "delete from prj_portfolio_map where prj_id = ?";
	//TODO DEL_PRJ_PORTFOLIO_MAP_BY_PRJ

	public static String GET_ALL_PORT_RSET_DET_FOR_PORT_WKSP = "select pj_id, alt_id, fund_status, port_rset.id from port_rset, dw_alt_short_info where port_rset.port_wksp_id = ? and port_rset.id = dw_alt_short_info.port_rs_id and is_default_alt = 1 order by port_rs_id asc";


	public static String GET_SUCC_INFO_ALL = Misc.G_DO_ORACLE ? "select pj_id_impact_on, alt_id_impact_on, pj_id_impact_by, alt_id_impact_by, dep_factor_list.dep_id, isfactor, isMileStone, val, scenario_impacted, case_index, scenario_impactor_index from dep_factor_list, dep_detail "+
		"where dep_detail.dep_id = dep_factor_list.dep_id and "+
		"dep_detail.impactor_index = dep_factor_list.impactor_index "+
		"order by pj_id_impact_on, alt_id_impact_on nulls first, dep_factor_list.dep_id, scenario_impacted, case_index, dep_detail.impactor_index " 
                                                           : "select pj_id_impact_on, alt_id_impact_on, pj_id_impact_by, alt_id_impact_by, dep_factor_list.dep_id, isfactor, isMileStone, val, scenario_impacted, case_index, scenario_impactor_index from dep_factor_list, dep_detail "+
		"where dep_detail.dep_id = dep_factor_list.dep_id and "+
		"dep_detail.impactor_index = dep_factor_list.impactor_index "+
		"order by pj_id_impact_on, alt_id_impact_on asc, dep_factor_list.dep_id, scenario_impacted, case_index, dep_detail.impactor_index ";

	public static String GET_CORR_INFO     = Misc.G_DO_ORACLE ? "select pj_id_left, alt_id_left, pj_id_right, alt_id_right, val, corr_type from corr_info order  by pj_id_left, alt_id_left nulls first, pj_id_right, alt_id_right nulls last, corr_type" : "select pj_id_left, alt_id_left, pj_id_right, alt_id_right, val, corr_type from corr_info order  by pj_id_left, alt_id_left asc, pj_id_right, alt_id_right asc, corr_type";

	public static String GET_ALL_PRIV_FOR_USER = "select priv_id, prj_id, port_node_id, wspace_id, all_scope from user_roles, role_privs, user_roles_scope where user_roles.user_1_id = ? and role_privs.role_id = user_roles.role_id and user_roles_scope.user_role_id = user_roles.id order by priv_id";

	public static String GET_NEW_MENU_REPORT_INFO = "select report_definitions.id,for_port_node_id,for_user_id,user_preferences.value from report_definitions join menu_report_definition on (report_definitions.id = menu_report_definition.report_definition_id ) join menu_master_report_definition on (menu_master_report_definition.report_definition_id = report_definitions.id) join menu_master on (menu_master.id = menu_master_report_definition.menu_master_id)  "+
                                                    " left join user_preferences on (for_user_id = user_1_id and user_preferences.name='pv123') "+
                                                    " where status = 1 and type = 0 ";

	//MEASURE RELATED
	public static String GET_MEASURE_DATA = Misc.G_DO_ORACLE ? "select outcome_id, phase_id, time_val, val from measure_data, measure_case_index, measure_map_items where "+
		"measure_map_items.wspace_id = ? and "+
		"measure_map_items.alt_id    = ? and "+
		"measure_map_items.measure_id = ? and "+
		"measure_map_items.isdefault = 1 and "+
		"measure_data.alt_measure_id = measure_map_items.alt_measure_id and "+
		"measure_data.measure_case_index_id = measure_case_index.id and "+
		"measure_case_index.break_down = ? and "+
		"measure_case_index.measure_id = measure_map_items.measure_id "+
		"order by outcome_id nulls last, phase_id nulls first, time_val" : "select outcome_id, phase_id, time_val, val from measure_data, measure_case_index, measure_map_items where "+
		"measure_map_items.wspace_id = ? and "+
		"measure_map_items.alt_id    = ? and "+
		"measure_map_items.measure_id = ? and "+
		"measure_map_items.isdefault = 1 and "+
		"measure_data.alt_measure_id = measure_map_items.alt_measure_id and "+
		"measure_data.measure_case_index_id = measure_case_index.id and "+
		"measure_case_index.break_down = ? and "+
		"measure_case_index.measure_id = measure_map_items.measure_id "+
		"order by outcome_id asc, phase_id asc, time_val";

	public static String GET_MEASURE_DATA_VER = Misc.G_DO_ORACLE ? "select outcome_id, phase_id, time_val, val from measure_data, measure_case_index where "+
		"measure_data.alt_measure_id = ? and "+
		"measure_data.measure_case_index_id = measure_case_index.id and "+
		"measure_case_index.break_down = ? and "+
		"measure_case_index.measure_id = ? "+
		"order by outcome_id nulls last, phase_id nulls first, time_val" : "select outcome_id, phase_id, time_val, val from measure_data, measure_case_index where "+
		"measure_data.alt_measure_id = ? and "+
		"measure_data.measure_case_index_id = measure_case_index.id and "+
		"measure_case_index.break_down = ? and "+
		"measure_case_index.measure_id = ? "+
		"order by outcome_id desc, phase_id asc, time_val";


	public static String GET_MEASURE_UNCERT = Misc.G_DO_ORACLE ? "select outcome_id, phase_id, time_val, prob_val, val from measure_uncert_data, measure_case_index, measure_map_items where "+
		"measure_map_items.wspace_id = ? and "+
		"measure_map_items.alt_id    = ? and "+
		"measure_map_items.measure_id = ? and "+
		"measure_map_items.isdefault = 1 and "+
		"measure_uncert_data.alt_measure_id = measure_map_items.alt_measure_id and "+
		"measure_uncert_data.measure_case_index_id = measure_case_index.id and "+
		"measure_case_index.break_down = ? and "+
		"measure_case_index.measure_id = measure_map_items.measure_id "+
		"order by outcome_id nulls last, phase_id nulls first, time_val, prob_val" :
"select outcome_id, phase_id, time_val, prob_val, val from measure_uncert_data, measure_case_index, measure_map_items where "+
		"measure_map_items.wspace_id = ? and "+
		"measure_map_items.alt_id    = ? and "+
		"measure_map_items.measure_id = ? and "+
		"measure_map_items.isdefault = 1 and "+
		"measure_uncert_data.alt_measure_id = measure_map_items.alt_measure_id and "+
		"measure_uncert_data.measure_case_index_id = measure_case_index.id and "+
		"measure_case_index.break_down = ? and "+
		"measure_case_index.measure_id = measure_map_items.measure_id "+
		"order by outcome_id desc, phase_id asc, time_val, prob_val";

	public static String GET_MEASURE_UNCERT_VER = Misc.G_DO_ORACLE ? "select outcome_id, phase_id, time_val, prob_val, val from measure_uncert_data, measure_case_index where "+
		"measure_uncert_data.alt_measure_id = ? and "+
		"measure_uncert_data.measure_case_index_id = measure_case_index.id and "+
		"measure_case_index.break_down = ? and "+
		"measure_case_index.measure_id = ? "+
		"order by outcome_id nulls last, phase_id nulls first, time_val, prob_val" : "select outcome_id, phase_id, time_val, prob_val, val from measure_uncert_data, measure_case_index where "+
		"measure_uncert_data.alt_measure_id = ? and "+
		"measure_uncert_data.measure_case_index_id = measure_case_index.id and "+
		"measure_case_index.break_down = ? and "+
		"measure_case_index.measure_id = ? "+
		"order by outcome_id desc, phase_id asc, time_val, prob_val";


	public static String GET_MEASURE_UNCERT_SPEC = Misc.G_DO_ORACLE ? "select outcome_id, phase_id, uncert_spec, uncert_dist from measure_uncert_spec, measure_map_items where "+
		"measure_map_items.wspace_id = ? and "+
		"measure_map_items.alt_id    = ? and "+
		"measure_map_items.measure_id = ? and "+
		"measure_map_items.isdefault = 1 and "+
		"measure_uncert_spec.alt_measure_id = measure_map_items.alt_measure_id and "+
		"measure_uncert_spec.break_down = ? "+
		"order by outcome_id nulls last, phase_id nulls first" : "select outcome_id, phase_id, uncert_spec, uncert_dist from measure_uncert_spec, measure_map_items where "+
		"measure_map_items.wspace_id = ? and "+
		"measure_map_items.alt_id    = ? and "+
		"measure_map_items.measure_id = ? and "+
		"measure_map_items.isdefault = 1 and "+
		"measure_uncert_spec.alt_measure_id = measure_map_items.alt_measure_id and "+
		"measure_uncert_spec.break_down = ? "+
		"order by outcome_id desc, phase_id asc";

	public static String GET_MEASURE_UNCERT_SPEC_VER = Misc.G_DO_ORACLE ? "select outcome_id, phase_id, uncert_spec, uncert_dist from measure_uncert_spec where "+
		"measure_uncert_spec.alt_measure_id = ? and "+
		"measure_uncert_spec.break_down = ? "+
		"order by outcome_id nulls last, phase_id nulls first" : "select outcome_id, phase_id, uncert_spec, uncert_dist from measure_uncert_spec where "+
		"measure_uncert_spec.alt_measure_id = ? and "+
		"measure_uncert_spec.break_down = ? "+
		"order by outcome_id desc, phase_id asc";


	public static String GET_LAUNCH_DATE = "select start_date, finish_dt from milestones, alt_map_items where wspace_id = ? and alt_id = ? and isdefault=1 and milestones.alt_date_id = alt_map_items.alt_date_id order by mstn_id, finish_dt desc";

	public static String INSERT_ALT_MEASURE           = Misc.G_DO_ORACLE ? "insert into alt_measures (id, alt_id, measure_id, alt_model_id_excel_load, created_on) values (?,?,?,?,sysdate)"
                                                                       :"insert into alt_measures (alt_id, measure_id, alt_model_id_excel_load, created_on) values (?,?,?,getDate())";
  
	public static String INSERT_MEASURE_CASE_INDEX    = Misc.G_DO_ORACLE ? "insert into measure_case_index (id, measure_id, break_down, outcome_id, phase_id) values (?,?,?,?,?)"
                                                                       : "insert into measure_case_index (measure_id, break_down, outcome_id, phase_id) values (?,?,?,?)";
                                                                       
	public static String SELECT_MEASURE_CASE_INDEX_OC = "select id from measure_case_index where measure_id = ? and break_down = ? and outcome_id = ?";
	public static String SELECT_MEASURE_CASE_INDEX_MS = "select id from measure_case_index where measure_id = ? and break_down = ? and phase_id = ?";
	public static String INSERT_MEASURE_DATA          = "insert into measure_data (measure_case_index_id, alt_measure_id, val, time_val) values (?,?,?,?)"; //REPLACED WITH _NEW
	public static String INSERT_MEASURE_UNCERT_SPEC   = "insert into measure_uncert_spec (id, alt_measure_id,uncert_spec, uncert_dist, break_down, outcome_id, phase_id) values (?,?,?,?,?,?,?)";
	public static String INSERT_MEASURE_UNCERT_DATA   = "insert into measure_uncert_data (measure_case_index_id, alt_measure_id, val, time_val, prob_val, measure_uncert_spec_id, val_scope) values (?,?,?,?,?,?,?)";
	public static String SET_MAPPING_MEASURE          = "update measure_map_items set alt_measure_id=? where wspace_id=? and alt_id=? and measure_id = ? and isdefault=1";
	public static String CREATE_MAPPING_MEASURE       = Misc.G_DO_ORACLE ? "insert into measure_map_items (id, wspace_id, alt_id, map_type, isdefault, measure_id, label_id, alt_measure_id) values (seq_alt_measures.nextval, ?, ?, ?, 1, ?, null, ?)" 
                                                                       : "insert into measure_map_items (wspace_id, alt_id, map_type, isdefault, measure_id, label_id, alt_measure_id) values ( ?, ?, ?, 1, ?, null, ?)";

	//for completion ...


	//because of a quirk of isdefault =1 can be there multiple times in Merck demo, check for multiples of number of elements
	public static String COUNT_COMP_DATES = "select alt_id, count(*) from milestones, alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and milestones.alt_date_id = alt_map_items.alt_date_id and milestones.start_date is not null and milestones.finish_dt is not  null group by alt_id order by alt_id";
	public static String COUNT_COMP_PROFILE = "select alt_id, count(*) from prof_outcomes, alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and prof_outcomes.alt_profil_id = alt_map_items.alt_profil_id group by alt_id order by alt_id";

	//CHECK ON DATA EXISTING TAKES TOO MUCH TIME INSTEAD WE SIMPLIFY AND JUST CHECK FOR EXISTENCE OF RELEVANT SECTIONS


	public static String COUNT_COMP_REVENUE = "select alt_id, count(*) from rev_segs,  alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and rev_segs.alt_rev_id = alt_map_items.alt_rev_id group by alt_id order by alt_id";
	public static String COUNT_COMP_DEV_COST = "select alt_id, count(*) from cost_items, alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and cost_items.alt_devcost_id = alt_map_items.alt_devcost_id group by alt_id order by alt_id";
	public static String COUNT_COMP_FTE_ITEMS = "select alt_id, count(*) from fte_items, alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and fte_items.alt_fte_id = alt_map_items.alt_fte_id group by alt_id order by alt_id";

	public static String COUNT_COMP_MEASURE_ITEMS = "select measure_id, alt_id, count(*) from measure_data, measure_map_items where measure_map_items.wspace_id = ? and measure_map_items.isdefault = 1 and measure_data.alt_measure_id = measure_map_items.alt_measure_id group by measure_id, alt_id order by measure_id, alt_id";
	public static String COUNT_COMP_MEASURE_ITEMS_UNCERT = "select measure_id, alt_id, count(*) from measure_uncert_data, measure_map_items where measure_map_items.wspace_id = ? and measure_map_items.isdefault = 1 and measure_uncert_data.alt_measure_id = measure_map_items.alt_measure_id group by measure_id, alt_id order by measure_id, alt_id";

	public static String GET_HISTORY_WKSP_APPROVAL = "select comments, on_date, wksp_hist_by, users.email, from_state, to_state, pln_wf_steps1.name fromName, pln_wf_steps2.name toName  from wksp_hist, users, pln_wf_steps pln_wf_steps1, pln_wf_steps pln_wf_steps2 "+
		"where wspace_id = ? and "+
		"users.id = wksp_hist_by and "+
		"from_state = pln_wf_steps1.pos_in_seq and "+
		"to_state  = pln_wf_steps2.pos_in_seq and "+
		"state_is_pln = 1 "+
		"Union "+
		"(select comments, on_date, wksp_hist_by, users.email, from_state, to_state, pj_wf_steps1.name fromName, pj_wf_steps2.name toName  from wksp_hist, users, pj_wf_steps pj_wf_steps1, pj_wf_steps pj_wf_steps2 "+
		"where wspace_id = ? and "+
		"users.id = wksp_hist_by and "+
		"from_state = pj_wf_steps1.id and "+
		"to_state  = pj_wf_steps2.id and "+
		"state_is_pln = 0) "+
		"order by on_date desc";

	public static String GET_ALL_HIST = Misc.G_DO_ORACLE ? "select comments, on_date, wksp_hist_by, users.email, wksp_hist.alt_id, alternatives.name, ref_item_type, ref_item_id, 0 "+
		"from wksp_hist, users, alternatives where "+
		"wksp_hist.wspace_id=? and "+
		"users.id = wksp_hist_by and "+
		"alternatives.id(+) = alt_id and "+
		"(ref_item_type is null or ref_item_type <> ?) "+ //? is SECTION_ALT_MEASURE
		"union "+
		"(select comments, on_date, wksp_hist_by, users.email, wksp_hist.alt_id, alternatives.name, ref_item_type, ref_item_id, alt_measures.measure_id "+
		"from wksp_hist, users, alternatives, alt_measures where "+
		"wksp_hist.wspace_id=? and "+
		"users.id = wksp_hist_by and "+
		"alternatives.id = wksp_hist.alt_id and "+
		"(ref_item_type = ?) and "+
		"(ref_item_id = alt_measures.id) "+
		") "+
		"order by on_date desc" : "select comments, on_date, wksp_hist_by, users.email, wksp_hist.alt_id, alternatives.name, ref_item_type, ref_item_id, 0 "+
		"from wksp_hist, users, alternatives where "+
		"wksp_hist.wspace_id=? and "+
		"users.id = wksp_hist_by and "+
		"alternatives.id =* alt_id and "+
		"(ref_item_type is null or ref_item_type <> ?) "+ //? is SECTION_ALT_MEASURE
		"union "+
		"(select comments, on_date, wksp_hist_by, users.email, wksp_hist.alt_id, alternatives.name, ref_item_type, ref_item_id, alt_measures.measure_id "+
		"from wksp_hist, users, alternatives, alt_measures where "+
		"wksp_hist.wspace_id=? and "+
		"users.id = wksp_hist_by and "+
		"alternatives.id = wksp_hist.alt_id and "+
		"(ref_item_type = ?) and "+
		"(ref_item_id = alt_measures.id) "+
		") "+
		"order by on_date desc";
	public static String GET_PJ_HIST = Misc.G_DO_ORACLE ? "select comments, on_date, pj_hist_by, users.email, action_type, rel_wksp, rel_wksp2, workspaces1.name wkspName1, workspaces2.name wkspName2 from "+
		"pj_hist, workspaces workspaces1, workspaces workspaces2, users where "+
		"pj_hist.prj_id = ? and "+
		"pj_hist.pj_hist_by = users.id and "+
		"workspaces1.id(+) = rel_wksp and "+
		"workspaces2.id(+) = rel_wksp2 "+
		"order by on_date desc" : "select comments, on_date, pj_hist_by, users.email, action_type, rel_wksp, rel_wksp2, workspaces1.name wkspName1, workspaces2.name wkspName2 from "+
		"pj_hist, workspaces workspaces1, workspaces workspaces2, users where "+
		"pj_hist.prj_id = ? and "+
		"pj_hist.pj_hist_by = users.id and "+
		"workspaces1.id =* rel_wksp and "+
		"workspaces2.id =* rel_wksp2 "+
		"order by on_date desc";

	public static String GET_VER_FOR_PJ_LIKE = "select ref_item_id from wksp_hist where wspace_id = ? and ref_item_type = ? order by on_date desc";
	public static String GET_VER_FOR_ALT_LIKE = "select ref_item_id from wksp_hist where wspace_id = ? and ref_item_type = ? and alt_id = ? order by on_date desc";
	public static String GET_VER_FOR_MEASURE_LIKE = "select ref_item_id from wksp_hist,alt_measures where wspace_id = ? and ref_item_type = ? and wksp_hist.alt_id = ? and alt_measures.id = ref_item_id and alt_measures.measure_id = ? order by on_date desc";

	public static String GET_LAB_VER_FOR_PJ_LIKE = "select pj_basic_id,labels.name, risk_header_id from labels, pj_map_items where labels.id=? and pj_map_items.id = labels.pj_map_id";
	public static String GET_LAB_VER_FOR_ALT_LIKE = "select alt_basic_id, alt_profil_id, alt_date_id, alt_model_id, alt_task_id, labels.name, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id from alt_map_items,labels where alt_map_items.wspace_id=? and alt_id = ? and label_id=? and labels.id=label_id";
	public static String GET_LAB_VER_FOR_MEASURE_LIKE = "select alt_measure_id, labels.name from measure_map_items,labels where measure_map_items.wspace_id=? and alt_id = ? and label_id=? and measure_id=? and label_id=labels.id";

	public static String GET_WKSP_VER_FOR_PJ_LIKE = "select pj_basic_id, workspaces.name from pj_map_items, workspaces where wspace_id=? and isdefault=1 and workspaces.id = wspace_id";
	public static String GET_WKSP_VER_FOR_ALT_LIKE = "select alt_basic_id, alt_profil_id, alt_date_id, alt_model_id, alt_task_id, workspaces.name, alt_work_id, alt_fte_id, alt_devcost_id, alt_rev_id, alt_opcost_id, alt_combined_id, alt_rating_id  from alt_map_items, workspaces where wspace_id=? and alt_id = ? and isdefault=1 and workspaces.id = wspace_id";
	public static String GET_WKSP_VER_FOR_MEASURE_LIKE = "select alt_measure_id, workspaces.name from measure_map_items, workspaces where wspace_id=? and alt_id = ? and isdefault=1 and measure_id=? and workspaces.id = wspace_id";

 /*102307
	public static String UNCERT_REV_COMPONENT =
		"select dw_alt_short_info.alt_id, rev_seg_id, rev_segs.mkt_type, rev_segs.scen_id, data.year, "+
		"data.val_scope, err_def_type, err_vals.err_percent, err_vals.value from err_vals, dw_alt_short_info, "+
		"data,  rev_segs where dw_alt_short_info.port_rs_id=? "+
		"rev_segs.alt_rev_id = ver_alt_rev_id and data.rev_seg_id = rev_segs.id and "+
		"err_vals.data_id = data.id and data.id >= ? order by dw_alt_short_info.alt_id,  rev_seg_id, rev_segs.scen_id, data.year, err_vals.err_percent ";

	public static String UNCERT_REV_TOT = //TODO CF
		"select dw_alt_short_info.alt_id, -1,-1, cashflows.scen_id, data.year, data.val_scope, err_def_type, err_vals.err_percent, err_vals.value from err_vals, dw_alt_short_info, data "+
		"where dw_alt_short_info.port_rs_id=? and rev_segs.alt_rev_id = ver_alt_rev_id and "+
		"data.cf_id_trev = cashflows.id and err_vals.data_id = data.id and data.id >= ? order by dw_alt_short_info.alt_id,   cashflows.scen_id, data.year, err_vals.err_percent ";

	public static String UNCERT_OPCOST_COMPONENT = //TODO CF
		"select dw_alt_short_info.alt_id, cost_li_id,cost_cent_id, cost_items.scen_id, data.year, "+
		"data.val_scope, err_def_type, err_vals.err_percent, err_vals.value from err_vals, dw_alt_short_info, data, cashflows, cost_items where dw_alt_short_info.port_rs_id=? and cashflows.alt_opcost_id = ver_alt_opcost_id and "+
		"cost_items.cf_id = cashflows.id and cost_items.to_include = 1 and data.cost_li_id = cost_items.id and err_vals.data_id = data.id  and data.id >= ? order by "+
		"dw_alt_short_info.alt_id,  cost_li_id, cost_items.scen_id, data.year, err_vals.err_percent ";

	public static String UNCERT_OPCOST_TOT = //TODO CF
		"select dw_alt_short_info.alt_id, -1,-1, cashflows.scen_id, data.year, data.val_scope, err_def_type, err_vals.err_percent, "+
		"err_vals.value from err_vals, dw_alt_short_info, data, cashflows where "+
		"dw_alt_short_info.port_rs_id=? and cashflows.alt_opcost_id = ver_alt_opcost_id and "+
		"data.cf_id_topcost = cashflows.id and err_vals.data_id = data.id  and data.id >= ? order by "+
		"dw_alt_short_info.alt_id,   cashflows.scen_id, data.year, err_vals.err_percent ";

	public static String UNCERT_DEVCOST_COMPONENT =
		"select dw_alt_short_info.alt_id, cost_li_id,cost_cent_id, for_achieving_milestone, data.year, data.val_scope, err_def_type, err_vals.err_percent, err_vals.value from err_vals, dw_alt_short_info, data, cost_items where dw_alt_short_info.port_rs_id=? and cost_items.alt_devcost_id = ver_alt_devcost_id and cost_items.to_include = 1 and data.cost_li_id = cost_items.id and err_vals.data_id = data.id  and data.id >= ? order by dw_alt_short_info.alt_id,  cost_li_id, data.year, err_vals.err_percent ";

	public static String UNCERT_FTE_COMPONENT =
		"select dw_alt_short_info.alt_id, fte_item_id,fte_head_id, for_achieving_milestone, data.year, data.val_scope, err_def_type, err_vals.err_percent, err_vals.value from err_vals, dw_alt_short_info, data, fte_items where dw_alt_short_info.port_rs_id=? and fte_items.alt_fte_id = ver_alt_fte_id and data.fte_item_id = fte_items.id and err_vals.data_id = data.id and fte_items.to_include = 1 and data.id >= ? order by dw_alt_short_info.alt_id, fte_item_id, data.year, err_vals.err_percent ";

*/


	public static String GET_DESC_INFO_FOR_PROJ_MGMT = Misc.G_DO_ORACLE ? "select alt_works.file_name_file_name_id, alt_works.ISFILEINSYCNWITHDATA, alt_works.last_update_on, alt_works.status, alt_works.last_update_by, users.username, projects.template_id "+
		", prjLckInfo.name, datetime "+
		"from alt_works, users, alt_map_items, projects, alternatives  "+
		", (select  users.name, alt_id, datetime from prj_lock_status, users where wspace_id=? "+
		" and alt_id=? and type =  ? "+
		"and users.id = currentowner) prjLckInfo "+
		"where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and alt_map_items.alt_id = ? and alt_works.id = alt_map_items.alt_work_id and users.id(+) = alt_works.last_update_by and alternatives.id=alt_works.alt_id and projects.id = alternatives.prj_id "+
		"and prjLckInfo.alt_id(+)  = alt_works.alt_id" : "select alt_works.file_name_file_name_id, alt_works.ISFILEINSYCNWITHDATA, alt_works.last_update_on, alt_works.status, alt_works.last_update_by, users.username, projects.template_id "+
		", prjLckInfo.name, datetime "+
		"from alt_works, users, alt_map_items, projects, alternatives  "+
		", (select  users.name, alt_id, datetime from prj_lock_status, users where wspace_id=? "+
		" and alt_id=? and type =  ? "+
		"and users.id = currentowner) prjLckInfo "+
		"where alt_map_items.wspace_id = ? and alt_map_items.isdefault = 1 and alt_map_items.alt_id = ? and alt_works.id = alt_map_items.alt_work_id and users.id =* alt_works.last_update_by and alternatives.id=alt_works.alt_id and projects.id = alternatives.prj_id "+
		"and prjLckInfo.alt_id  =* alt_works.alt_id";

	public static String GET_DESC_INFO_FOR_PROJ_MGMT_BY_VER = Misc.G_DO_ORACLE ? "select alt_works.file_name_file_name_id, alt_works.ISFILEINSYCNWITHDATA, alt_works.last_update_on, alt_works.status, alt_works.last_update_by, users.username, projects.template_id "+
		", prjLckInfo.name, datetime "+
		"from alt_works, users,  projects, alternatives  "+
		", (select  users.name, alt_id, datetime from prj_lock_status, users where wspace_id=? "+
		" and alt_id=? and type =  ? "+
		"and users.id = currentowner) prjLckInfo "+
		"where "+
		"alt_works.id = ? and users.id(+) = alt_works.last_update_by and alternatives.id=alt_works.alt_id and projects.id = alternatives.prj_id "+
		"and prjLckInfo.alt_id(+)  = alt_works.alt_id" : "select alt_works.file_name_file_name_id, alt_works.ISFILEINSYCNWITHDATA, alt_works.last_update_on, alt_works.status, alt_works.last_update_by, users.username, projects.template_id "+
		", prjLckInfo.name, datetime "+
		"from alt_works, users,  projects, alternatives  "+
		", (select  users.name, alt_id, datetime from prj_lock_status, users where wspace_id=? "+
		" and alt_id=? and type =  ? "+
		"and users.id = currentowner) prjLckInfo "+
		"where "+
		"alt_works.id = ? and users.id =* alt_works.last_update_by and alternatives.id=alt_works.alt_id and projects.id = alternatives.prj_id "+
		"and prjLckInfo.alt_id =* alt_works.alt_id";

	//,min_dur, max_dur, p10_dur, p90_dur, distrib, interruptible, cc
	//the following two must be exactly alike in select clauses
	public static String GET_PROJ_PLAN_ITEMS = "select alt_work_items.id, parent_internal_id parent, start_date, end_date, complete, actual_start, actual_end, internal_id, ref_id, isConstant, is_split, status, type, alt_work_items.alt_work_id, name, pred, for_achieving_ms, classify1, classify2, classify3, classify4, classify5, rollup_policy,min_dur, max_dur, p10_dur, p90_dur, distrib, interruptible, cc, ismilestone, variancecode, user_comment, to_include, rollup_cost, notes, target_market "+
		"from alt_work_items, alt_map_items where "+
		"alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 "+
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id order by alt_work_items.id ";
	public static String GET_PROJ_PLAN_SPECIFIC = "select alt_work_items.id, parent_internal_id parent, start_date, end_date, complete, actual_start, actual_end, internal_id, ref_id, isConstant, is_split, status, type, alt_work_items.alt_work_id, name, pred, for_achieving_ms, classify1, classify2, classify3, classify4, classify5, rollup_policy,min_dur, max_dur, p10_dur, p90_dur, distrib, interruptible, cc, ismilestone, variancecode, user_comment, to_include, rollup_cost, notes, target_market  "+
		"from alt_work_items where "+
		"alt_work_items.alt_work_id = ?  order by alt_work_items.id ";

	public static String GET_PORT_LEVEL_1_TIME_INFO = "";

	//the following two must be exactly alike in select clauses .... not used
	//   public static String GET_REF_INFO_FOR_ALT_WORK = "select ref_alt_fte_id, ref_alt_devcost_id from alt_works, alt_map_items "+
	//   "where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and alt_works.id = alt_map_items.alt_work_id";
     public static String GET_REF_INFO_FOR_ALT_WORK_SPECIFIC = "select ref_alt_fte_id, ref_alt_devcost_id from alt_works where alt_works.id = ? ";

	/*
	 public static String GET_RES_NEED = "select alt_work_items.internal_id, fte_head_id, relmonth, val"+
	 "from alt_work_item_res_need, alt_work_items where alt_work_item_res_need.parent_internal_id = alt_work_items.id and alt_work_id = ? "+
	 "order by alt_work_item_res_need.parent_internal_id desc, fte_head_id asc";
	 */
	//Next two queries must be exactly the same ... except for the name just after scope
	public static String SET_REF_INFO_FOR_WORK = "update alt_works set ref_alt_fte_id=?, ref_alt_devcost_id=? where id=?";

	public static String GET_MAX_INTERNAL_ID = "select max(internal_id) from alt_work_items, alt_works where alt_works.alt_id = ? and alt_work_items.alt_work_id = alt_works.id";
	public static String GET_PLAN_LAST_FILE_ID = "select file_name_file_name_id, status from alt_works, alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and alt_works.id = alt_map_items.alt_work_id";

	public static String INSERT_ALT_WORK = Misc.G_DO_ORACLE ? "insert into alt_works (id, alt_id, wspace_id, file_name_file_name_id, isfileinsycnwithdata, last_update_on, status, last_update_by) values (?, ?, ?, ?, ?, sysdate, ?, ?) " :  "insert into alt_works (alt_id, wspace_id, file_name_file_name_id, isfileinsycnwithdata, last_update_on, status, last_update_by) values (?, ?, ?, ?, getdate(), ?, ?) ";
	public final static String UPDATE_ALT_MAP_WORK = "update alt_map_items set alt_work_id=? where wspace_id=? and alt_id=? and isdefault=1";
	public final static String INSERT_WORK_ITEM = (Misc.G_DO_ORACLE ? "insert into alt_work_items (id, " : "insert into alt_work_items ( ")+
  " parent_internal_id, alt_work_id, name, start_date, end_date, complete, actual_start, actual_end, internal_id, ref_id, isconstant, is_split, status, type, pred, for_achieving_ms "+
		",classify1, classify2, classify3, classify4, classify5 "+
//G_WATSON_031507_ROLLBACK
		",rollup_policy, min_dur, max_dur, p10_dur, p90_dur, distrib, interruptible, cc, wbs_level, ismilestone, variancecode, user_comment, to_include, rollup_cost, notes, target_market, status_calculated, lhs_number, rhs_number "+
//		",rollup_policy, min_dur, max_dur, p10_dur, p90_dur, distrib, interruptible, cc, wbs_level, ismilestone, variancecode, user_comment, to_include, rollup_cost, notes, target_market "+
		") values "+
    (Misc.G_DO_ORACLE ? "(?," : "(") +
		" ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? ,?, ?, ?, ?, ? "+
		",?,?,?,?,? "+
//G_WATSON_031507_ROLLBACK
		",?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?, ?, ?,?,? "+
//		",?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?, ? "+
		")";

	public final static String GET_PRJ_ALT_WKSP_MPP =  "select projects.id, projects.name, workspaces.id, workspaces.name,  alternatives.id, alternatives.name, pj_map_items.map_type, alt_map_items.alt_date_id, alt_profil_id "+
		"from projects, workspaces, alternatives, pj_map_items, alt_map_items "+
		"where "+
		"workspaces.prj_id = projects.id "+
		"and pj_map_items.wspace_id = workspaces.id "+
		"and pj_map_items.isdefault = 1 and pj_map_items.map_type in (1, 4) and projects.status in (1,2,3,4,7) " +
		"and alt_map_items.isdefault = 1 and alt_map_items.wspace_id = pj_map_items.wspace_id and alt_map_items.alt_id = alternatives.id "+
		"and alternatives.prj_id = projects.id "+
		"order by projects.id desc, workspaces.id asc, pj_map_items.map_type desc, alternatives.id desc ";
	/*
	 public final static String GET_PRJ_ALT_WKSP_MPP =  "select projects.id, projects.name, workspaces.id, workspaces.name,  alternatives.id, alternatives.name, pj_map_items.map_type "+
	 "from projects, workspaces, alternatives, pj_map_items where "+
	 "workspaces.prj_id = projects.id "+
	 "and pj_map_items.wspace_id = workspaces.id "+
	 "and isdefault = 1 and map_type in (1, 4) " +
	 "and alternatives.prj_id = projects.id "+
	 "order by projects.id desc, workspaces.id asc, pj_map_items.map_type desc, alternatives.id desc ";
	 */
	public final static String SET_FILE_ID_AS_LATEST = "update alt_works set ISFILEINSYCNWITHDATA=1, file_name_file_name_id=? where alt_works.id=?";

	//Not needed anymore ...  public final static String INSERT_SKILL_QTY = "insert into  alt_work_item_res_need (parent_internal_id, fte_head_id, relmonth, val) values (?,?,?,?)";

	public final static String GET_TEMPLATE_ID = "select template_id from projects where id = ?";

	public final static String GET_CURRENT_MAPS = "select alt_profil_id, alt_date_id, alt_fte_id, alt_devcost_id, alt_opcost_id, alt_rev_id, alt_combined_id, alt_rating_id from alt_map_items where alt_id=? and wspace_id=? and isdefault=1";

	public final static String CREATE_MODEL_FTE = Misc.G_DO_ORACLE ? "insert into alt_fte_model (id, alt_id, wspace_id, file_id, ref_date_Id, loaded_from) values (?,?,?,?, ?, ?) "
  :
  "insert into alt_fte_model ( alt_id, wspace_id, file_id, ref_date_Id, loaded_from) values (?,?,?, ?, ?) ";
	public final static String UPDATE_ALT_MAP_ITEM_FTE_MODEL = "update alt_map_items set alt_fte_id = ? where wspace_id = ? and alt_id = ? and isdefault = 1";

	public final static String CREATE_MODEL_REV = Misc.G_DO_ORACLE ? "insert into alt_rev_model (id, alt_id, wspace_id, file_id, ref_date_id, ref_profile_id, loaded_from) values (?,?,?,?, ?, ?, ?) "
                                                                 :"insert into alt_rev_model (alt_id, wspace_id, file_id, ref_date_id, ref_profile_id, loaded_from) values (?,?,?,?, ?,?) ";
                                                                 
	public final static String UPDATE_ALT_MAP_ITEM_REV_MODEL = "update alt_map_items set alt_rev_id = ? where wspace_id = ? and alt_id = ? and isdefault = 1";

	public final static String CREATE_MODEL_DEVCOST = Misc.G_DO_ORACLE ? "insert into alt_devcost_model (id, alt_id, wspace_id, file_id, ref_date_id, ref_fte_model, loaded_from) values (?,?,?,?, ?, ?, ?) "
                                                                     : "insert into alt_devcost_model (alt_id, wspace_id, file_id, ref_date_id, ref_fte_model, loaded_from) values (?,?,?,?, ?, ?) ";
	public final static String UPDATE_ALT_MAP_ITEM_DEVCOST_MODEL = "update alt_map_items set alt_devcost_id = ? where wspace_id = ? and alt_id = ? and isdefault = 1";

	public final static String CREATE_MODEL_OPCOST = Misc.G_DO_ORACLE ? "insert into alt_opcost_model (id, alt_id, wspace_id, file_id, ref_date_id, ref_profile_id, ref_rev_model) values (?,?,?,?, ?, ?, ?) "
                                                                    : "insert into alt_opcost_model (alt_id, wspace_id, file_id, ref_date_id, ref_profile_id, ref_rev_model) values (?,?,?,?, ?, ?) ";
	public final static String UPDATE_ALT_MAP_ITEM_OPCOST_MODEL = "update alt_map_items set alt_opcost_id = ? where wspace_id = ? and alt_id = ? and isdefault = 1";

	public final static String CREATE_MODEL_COMBINED = Misc.G_DO_ORACLE ? "insert into alt_combined_model (id, alt_id, wspace_id, file_id, ref_date_id, ref_profile_id, ref_rev_model, ref_devcost_model, ref_opcost_model) values (?,?,?,?, ?, ?, ?, ?, ?) "
  : "insert into alt_combined_model (alt_id, wspace_id, file_id, ref_date_id, ref_profile_id, ref_rev_model, ref_devcost_model, ref_opcost_model) values (?,?,?, ?, ?, ?, ?, ?)" ;
	public final static String UPDATE_ALT_MAP_ITEM_COMBINED_MODEL = "update alt_map_items set alt_combined_id = ? where wspace_id = ? and alt_id = ? and isdefault = 1";

//NOT USED IN AM CURRENTLY
	public static final String GET_ALT_REV_FILE_INFO = Misc.G_DO_ORACLE ? "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_rev_model.id from alt_rev_model, alt_map_items, file_names model, file_names template where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and alt_map_items.alt_rev_id = alt_rev_model.id and file_id=model.file_name_id and file_template_id = template.file_name_id(+)" : "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_rev_model.id from alt_rev_model, alt_map_items, file_names model, file_names template where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and alt_map_items.alt_rev_id = alt_rev_model.id and file_id=model.file_name_id and file_template_id *= template.file_name_id";
	public static final String GET_ALT_REV_FILE_INFO_VER = Misc.G_DO_ORACLE ? "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_rev_model.id  from alt_rev_model,  file_names model, file_names template where  alt_rev_model.id = ? and file_id=model.file_name_id and file_template_id = template.file_name_id(+)" : "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_rev_model.id  from alt_rev_model,  file_names model, file_names template where  alt_rev_model.id = ? and file_id=model.file_name_id and file_template_id *= template.file_name_id";
	public static final String GET_ALT_FTE_FILE_INFO = Misc.G_DO_ORACLE ? "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_fte_model.id from alt_fte_model, alt_map_items, file_names model, file_names template where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and alt_map_items.alt_fte_id = alt_fte_model.id and file_id=model.file_name_id and file_template_id = template.file_name_id(+)" :  "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_fte_model.id from alt_fte_model, alt_map_items, file_names model, file_names template where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and alt_map_items.alt_fte_id = alt_fte_model.id and file_id=model.file_name_id and file_template_id *= template.file_name_id";
	public static final String GET_ALT_FTE_FILE_INFO_VER = Misc.G_DO_ORACLE ? "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_fte_model.id  from alt_fte_model,  file_names model, file_names template where  alt_fte_model.id = ? and file_id=model.file_name_id and file_template_id = template.file_name_id(+)" : "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_fte_model.id  from alt_fte_model,  file_names model, file_names template where  alt_fte_model.id = ? and file_id=model.file_name_id and file_template_id *= template.file_name_id";
	public static final String GET_ALT_DEVCOST_FILE_INFO = Misc.G_DO_ORACLE ? "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_devcost_model.id from alt_devcost_model, alt_map_items, file_names model, file_names template where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and alt_map_items.alt_devcost_id = alt_devcost_model.id and file_id=model.file_name_id and "+
  "file_template_id = template.file_name_id(+)" : "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_devcost_model.id from alt_devcost_model, alt_map_items, file_names model, file_names template where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and alt_map_items.alt_devcost_id = alt_devcost_model.id and file_id=model.file_name_id and file_template_id *= template.file_name_id";
  ////////////////////////////////////////////////)))))))))))))))))))))))))))*****************************************************(((((((((((((((((((((((((((((((((\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public static final String GET_ALT_DEVCOST_FILE_INFO_VER = Misc.G_DO_ORACLE ? "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_devcost_model.id  from alt_devcost_model,  file_names model, file_names template where  alt_devcost_model.id = ? and file_id=model.file_name_id and file_template_id = template.file_name_id(+)" 
  : "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_devcost_model.id  from alt_devcost_model,  file_names model, file_names template where  alt_devcost_model.id = ? and file_id=model.file_name_id and file_template_id *= template.file_name_id";
	public static final String GET_ALT_OPCOST_FILE_INFO = Misc.G_DO_ORACLE ? "select distinct file_id, file_template_id, model.extension, template.extension, model.name, "+
  "template.name, alt_opcost_model.id from alt_opcost_model, alt_map_items, file_names model, file_names template where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? "+
  "and alt_map_items.isdefault = 1 and alt_map_items.alt_opcost_id = alt_opcost_model.id and file_id=model.file_name_id and file_template_id = template.file_name_id(+)" : 
  "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_opcost_model.id from alt_opcost_model, alt_map_items, "+
  "file_names model, file_names template where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and "+
  "alt_map_items.alt_opcost_id = alt_opcost_model.id and file_id=model.file_name_id and file_template_id *= template.file_name_id";
	public static final String GET_ALT_OPCOST_FILE_INFO_VER = Misc.G_DO_ORACLE ? "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_opcost_model.id  from alt_opcost_model,  file_names model, file_names template where  alt_opcost_model.id = ? and file_id=model.file_name_id and file_template_id = template.file_name_id(+)" :
  "select distinct file_id, file_template_id, model.extension, template.extension, model.name, template.name, alt_opcost_model.id  from alt_opcost_model,  file_names model, file_names template where  alt_opcost_model.id = ? and file_id=model.file_name_id and file_template_id *= template.file_name_id";

//no longer used
	public static String COPY_LT_DEVCOST_ITEMS  = Misc.G_DO_ORACLE ? "insert into cost_items(id, classify1, classify2, classify3, classify4, classify5, for_achieving_milestone, start_date, name, lineitem_id, expense_ty, end_date,  cost_cent_id, alt_devcost_id, is_short_term, task_internal_id, to_include, target_market, scen_id, temp_copy_of) "+
		"(select seq_cost_items.nextval, classify1, classify2, classify3, classify4, classify5, for_achieving_milestone, start_date, name, lineitem_id, expense_ty, end_date,  cost_cent_id,  ?, is_short_term, task_internal_id, to_include, target_market, scen_id, cost_items.id from cost_items "+
		"where alt_devcost_id = ?) " :
		"insert into cost_items(classify1, classify2, classify3, classify4, classify5, for_achieving_milestone, start_date, name, lineitem_id, expense_ty, end_date,  cost_cent_id, alt_devcost_id, is_short_term, task_internal_id, to_include, target_market, scen_id, temp_copy_of) "+
		"(select classify1, classify2, classify3, classify4, classify5, for_achieving_milestone, start_date, name, lineitem_id, expense_ty, end_date,  cost_cent_id,  ?, is_short_term, task_internal_id, to_include, target_market, scen_id, cost_items.id from cost_items "+
		"where alt_devcost_id = ?) ";//1? => new dec cost model, 2 => old

	public static String COPY_LT_FTE_ITEMS = Misc.G_DO_ORACLE ? "insert into fte_items(id, classify1, classify2, classify3, classify4, classify5, for_achieving_milestone, start_date, name, fte_lineitem_id, end_date,  fte_head_id, alt_fte_id, is_short_term, task_internal_id, to_include, target_market, scen_id, temp_copy_of, for_skill, assignment_status)  "+
		"(select seq_fte_items.nextval, classify1, classify2, classify3, classify4, classify5, for_achieving_milestone, start_date, name, fte_lineitem_id, end_date,  fte_head_id, ?, is_short_term, task_internal_id, to_include, target_market, scen_id, fte_items.id, fte_items.for_skill, fte_items.assignment_status from fte_items "+
		"where alt_fte_id = ?) " : 
		"insert into fte_items(classify1, classify2, classify3, classify4, classify5, for_achieving_milestone, start_date, name, fte_lineitem_id, end_date,  fte_head_id, alt_fte_id, is_short_term, task_internal_id, to_include, target_market, scen_id, temp_copy_of, for_skill, assignment_status)  "+
		"(select classify1, classify2, classify3, classify4, classify5, for_achieving_milestone, start_date, name, fte_lineitem_id, end_date,  fte_head_id, ?, is_short_term, task_internal_id, to_include, target_market, scen_id, fte_items.id, fte_items.for_skill, fte_items.assignment_status from fte_items "+
		"where alt_fte_id = ?) ";//1? => new dec cost model, 2 => old

	//   public static String COPY_LT_DATA_FTE_ITEMS =

	public static String COPY_CREATE_DATA_CI ="insert into data(year, err_def_type, value, cost_li_id, val_scope, val_dur) "+
		"(select data.year, err_def_type, value, cost_items.id, val_scope, val_dur "+
		"from data, cost_items, time_dims where cost_items.alt_devcost_id=? and data.cost_li_id = cost_items.temp_copy_of and data.year = time_dims.val and time_dims.mon_1900 > ?)";
		

	public static String COPY_CREATE_DATA_FTE = "insert into data(year, err_def_type, value, fte_item_id, val_scope, val_dur) "+
		"(select data.year, err_def_type, value, fte_items.id, val_scope, val_dur "+
		"from data, fte_items, time_dims where fte_items.alt_fte_id=? and data.fte_item_id = fte_items.temp_copy_of and data.year = time_dims.val and time_dims.mon_1900 > ?)";
		

	public static String GET_REV_SEG_INFO = "select rev_segs.name, mkt_type, time_dims.year, sum(data.value) from alt_map_items,  rev_segs, data, time_dims where alt_map_items.alt_id = ? and alt_map_items.workspace_id = ? and alt_map_items.isdefault = 1 and rev_segs.alt_rev_id = alt_map_items.alt_rev_id and data.rev_seg_id = rev_segs.id and time_dims.val = data.year group by rev_segs.id, time_dims.year order by rev_segs.id, time_dims.year";

	public static String GET_MSTONE_STAT_INFO = Misc.G_DO_ORACLE ? " select currdw.prj_id, currdw.alt_id, port_wksp_asso.wspace_id, projects.name, curr.start_date, curr.finish_dt, "+
		" baseline.start_date,baseline.finish_dt, curr.mstn_id, curr.target_market "+
		" from port_results currdw, port_results basedw,port_rset,port_wksp_asso, milestones curr, milestones baseline, projects "+
		" where  currdw.port_rs_id = ? and basedw.port_rs_id = ? "+
		"and currdw.alt_id(+) = basedw.alt_id and currdw.fund_status = 1 "+
		"and port_rset.id = currdw.port_rs_id and port_wksp_asso.port_wksp_id = port_rset.port_wksp_id "+
		"and port_wksp_asso.prj_id = currdw.prj_id and "+
		"curr.alt_date_id = currdw.ver_alt_mstone_id and "+
		"baseline.alt_date_id = basedw.ver_alt_mstone_id and "+
		"curr.mstn_id = baseline.mstn_id(+) and curr.target_market = baseline.target_market(+) and projects.id = currdw.prj_id "+
		"order by currdw.prj_id desc, curr.mstn_id asc " :
		" select currdw.prj_id, currdw.alt_id, port_wksp_asso.wspace_id, projects.name, curr.start_date, curr.finish_dt, "+
		" baseline.start_date,baseline.finish_dt, curr.mstn_id, curr.target_market "+
		" from port_results currdw, port_results basedw,port_rset,port_wksp_asso, milestones curr, milestones baseline, projects "+
		" where  currdw.port_rs_id = ? and basedw.port_rs_id = ? "+
		"and currdw.alt_id =* basedw.alt_id and currdw.fund_status = 1 "+
		"and port_rset.id = currdw.port_rs_id and port_wksp_asso.port_wksp_id = port_rset.port_wksp_id "+
		"and port_wksp_asso.prj_id = currdw.prj_id and "+
		"curr.alt_date_id = currdw.ver_alt_mstone_id and "+
		"baseline.alt_date_id = basedw.ver_alt_mstone_id and "+
		"curr.mstn_id *= baseline.mstn_id and curr.target_market *= baseline.target_market and projects.id = currdw.prj_id "+
		"order by currdw.prj_id desc, curr.mstn_id asc ";

	public static String GET_PROJECT_STATUS_INFO = Misc.G_DO_ORACLE ? "select alldat.pjid, alldat.altid, alldat.wspid, alldat.name, workInfo.name, workInfo.comments, workinfo.status "+
		"from "+
		"(select port_results.prj_id pjid, port_results.alt_id altid, "+
		" port_wksp_asso.wspace_id wspid, projects.name name from port_results, "+
		" port_rset,port_wksp_asso, projects "+
		" where port_results.port_rs_id = ? and "+
		" port_results.fund_status = 1 and port_rset.id = port_results.port_rs_id "+
		"and port_wksp_asso.port_wksp_id =port_rset.port_wksp_id and "+
		"port_wksp_asso.prj_id = port_results.prj_id and projects.id = port_results.prj_id) alldat, "+
		"(select port_results.prj_id pjid, name, comments, status from alt_work_items, port_results where "+
		"port_results.port_rs_id=? and fund_status=1 and "+
		"port_results.ver_alt_work_id =alt_work_items.alt_work_id and "+
		"status <> 1 order by prj_id desc, wbs_level asc) workInfo "+
		"where workinfo.pjid(+) = alldat.pjid " :
		"select alldat.pjid, alldat.altid, alldat.wspid, alldat.name, workInfo.name, workInfo.comments, workinfo.status "+
		"from "+
		"(select port_results.prj_id pjid, port_results.alt_id altid, "+
		" port_wksp_asso.wspace_id wspid, projects.name name from port_results, "+
		" port_rset,port_wksp_asso, projects "+
		" where port_results.port_rs_id = ? and "+
		" port_results.fund_status = 1 and port_rset.id = port_results.port_rs_id "+
		"and port_wksp_asso.port_wksp_id =port_rset.port_wksp_id and "+
		"port_wksp_asso.prj_id = port_results.prj_id and projects.id = port_results.prj_id) alldat, "+
		"(select port_results.prj_id pjid, name, comments, status from alt_work_items, port_results where "+
		"port_results.port_rs_id=? and fund_status=1 and "+
		"port_results.ver_alt_work_id =alt_work_items.alt_work_id and "+
		"status <> 1 order by prj_id desc, wbs_level asc) workInfo "+
		"where workinfo.pjid =* alldat.pjid ";

	//HMM... not used but has hardcoded 50703 in the port-rset id ... probably for current
  /* 102307
	public static String GET_PORT_TIMELINE = Misc.G_DO_ORACLE ? "select alldat.pjid, alldat.altid, alldat.wspid, alldat.name, workInfo.name, workInfo.start_date, workInfo.end_date, workInfo.for_achieving_ms "+
		"from "+
		"(select port_results.prj_id pjid, port_results.alt_id altid, "+
		" port_wksp_asso.wspace_id wspid, projects.name name from port_results, "+
		" port_rset,port_wksp_asso, projects "+
		" where port_results.port_rs_id = 50703 and "+
		" port_results.fund_status = 1 and port_rset.id = port_results.port_rs_id "+
		"and port_wksp_asso.port_wksp_id =port_rset.port_wksp_id and "+
		"port_wksp_asso.prj_id = port_results.prj_id and projects.id = port_results.prj_id) alldat, "+
		"(select port_results.prj_id pjid, name, start_date, end_date, for_achieving_ms from alt_work_items, port_results where "+
		"port_results.port_rs_id=50703 and fund_status=1 and "+
		"port_results.ver_alt_work_id =alt_work_items.alt_work_id  and "+
		"wbs_level = ? order by prj_id desc, for_achieving_ms asc, start_date asc) workInfo "+
		"where workinfo.pjid(+) = alldat.pjid " :
		"select alldat.pjid, alldat.altid, alldat.wspid, alldat.name, workInfo.name, workInfo.start_date, workInfo.end_date, workInfo.for_achieving_ms "+
		"from "+
		"(select port_results.prj_id pjid, port_results.alt_id altid, "+
		" port_wksp_asso.wspace_id wspid, projects.name name from port_results, "+
		" port_rset,port_wksp_asso, projects "+
		" where port_results.port_rs_id = 50703 and "+
		" port_results.fund_status = 1 and port_rset.id = port_results.port_rs_id "+
		"and port_wksp_asso.port_wksp_id =port_rset.port_wksp_id and "+
		"port_wksp_asso.prj_id = port_results.prj_id and projects.id = port_results.prj_id) alldat, "+
		"(select port_results.prj_id pjid, name, start_date, end_date, for_achieving_ms from alt_work_items, port_results where "+
		"port_results.port_rs_id=50703 and fund_status=1 and "+
		"port_results.ver_alt_work_id =alt_work_items.alt_work_id  and "+
		"wbs_level = ? order by prj_id desc, for_achieving_ms asc, start_date asc) workInfo "+
		"where workinfo.pjid =* alldat.pjid ";
*/
	public static String GET_REV_SUMM_DATA =
		"select rev_segs.scen_id, rev_segs.mkt_type, rev_segs.id, rev_segs.name, time_dims.year, sum(data.value), 0, alt_scen_list.scen_name "+
		"from rev_segs, data, time_dims, alt_scen_list where "+
		"rev_segs.alt_rev_id = ? and "+
		"alt_scen_list.alt_id = ? and "+
		"alt_scen_list.scen_id = rev_segs.scen_id and "+
		"data.rev_seg_id = rev_segs.id and "+
		"time_dims.val = data.year "+
		"group by rev_segs.scen_id, rev_segs.mkt_type, rev_segs.id, rev_segs.name, time_dims.year, alt_scen_list.scen_name "+
		"order by rev_segs.scen_id, rev_segs.mkt_type, rev_segs.id, time_dims.year ";

	public static String GET_OPCOST_SUMM_DATA =
		"select cost_items.scen_id, cost_items.cost_cent_id, cost_items.id, cost_items.name, time_dims.year, sum(data.value), 0, alt_scen_list.scen_name, cost_items.target_market "+
		"from cost_items, data, time_dims, alt_scen_list where "+
		"cost_items.alt_opcost_id = ? and "+
		"alt_scen_list.alt_id = ? and "+
		"cost_items.scen_id = alt_scen_list.scen_id and "+
		"data.cost_li_id = cost_items.id and "+
		"time_dims.val = data.year "+
		"group by cost_items.scen_id, cost_items.cost_cent_id, cost_items.id, cost_items.name, time_dims.year, alt_scen_list.scen_name, cost_items.target_market "+
		"order by cost_items.scen_id, cost_items.cost_cent_id, cost_items.id, time_dims.year, cost_items.target_market ";


	public static String GET_DEVCOST_SUMM_DATA = Misc.G_DO_ORACLE ? "select p1,p2,p11, p3,p4,p5, p12,'',p100 from ( "+
		"( "+
		"select cost_items.classify2 p1, cost_items.cost_cent_id p2, cost_items.id p11, cost_items.name p3, time_dims.year p4, sum(data.value) p5, cost_items.for_achieving_milestone p12,  cost_items.target_market p100 "+
		"from  cost_items, data, time_dims where "+
		"cost_items.alt_devcost_id = ? and "+
		"cost_items.task_internal_id is null and "+
		"data.cost_li_id = cost_items.id and "+
		"time_dims.val = data.year "+
		"group by cost_items.classify2, cost_items.cost_cent_id, cost_items.id, cost_items.name, time_dims.year, for_achieving_milestone, cost_items.target_market "+
		") "+
		"union "+
		"( "+
		"select cost_items.classify2 p1, cost_items.cost_cent_id p2, 0 p11, null p3, time_dims.year p4, sum(data.value) p5, cost_items.for_achieving_milestone p12,  cost_items.target_market p100 "+
		"from  cost_items, data, time_dims where "+
		"cost_items.alt_devcost_id = ? and "+
		"cost_items.task_internal_id is not null and "+
		"data.cost_li_id = cost_items.id and "+
		"time_dims.val = data.year "+
		"group by cost_items.classify2, cost_items.cost_cent_id, time_dims.year, for_achieving_milestone, cost_items.target_market "+
		") "+
		") "+
		"order by p1, p2 ,p11 nulls last, p12, p100, p4 " :
		"select p1,p2,p11, p3,p4,p5, p12,'',p100 from ( "+
		"( "+
		"select cost_items.classify2 p1, cost_items.cost_cent_id p2, cost_items.id p11, cost_items.name p3, time_dims.year p4, sum(data.value) p5, cost_items.for_achieving_milestone p12,  cost_items.target_market p100 "+
		"from  cost_items, data, time_dims where "+
		"cost_items.alt_devcost_id = ? and "+
		"cost_items.task_internal_id is null and "+
		"data.cost_li_id = cost_items.id and "+
		"time_dims.val = data.year "+
		"group by cost_items.classify2, cost_items.cost_cent_id, cost_items.id, cost_items.name, time_dims.year, for_achieving_milestone, cost_items.target_market "+
		") "+
		"union "+
		"( "+
		"select cost_items.classify2 p1, cost_items.cost_cent_id p2, 0 p11, null p3, time_dims.year p4, sum(data.value) p5, cost_items.for_achieving_milestone p12,  cost_items.target_market p100 "+
		"from  cost_items, data, time_dims where "+
		"cost_items.alt_devcost_id = ? and "+
		"cost_items.task_internal_id is not null and "+
		"data.cost_li_id = cost_items.id and "+
		"time_dims.val = data.year "+
		"group by cost_items.classify2, cost_items.cost_cent_id, time_dims.year, for_achieving_milestone, cost_items.target_market "+
		") "+
		") "+
		"order by p1, p2 ,p11 desc, p12, p100, p4 ";
	public static String GET_FTE_SUMM_DATA = //not fixed for for_skill, assignment_status
    Misc.G_DO_ORACLE ? "select p1,p2,p11, p3,p4,p5, p12,'',p100 from ( "+
		"( "+
		"select classify1 p1, fte_items.fte_head_id p2, fte_items.id p11, fte_items.name p3, time_dims.year p4, sum(data.value) p5, fte_items.for_achieving_milestone p12, fte_items.target_market p100  "+
		"from  fte_items, data, time_dims where "+
		"fte_items.alt_fte_id = ? and "+
		"fte_items.task_internal_id is null and "+
		"data.fte_item_id = fte_items.id and "+
		"time_dims.val = data.year "+
		"group by classify1, fte_items.fte_head_id, fte_items.id, fte_items.name, time_dims.year, for_achieving_milestone, fte_items.target_market "+
		") "+
		"union "+
		"( "+
		"select classify1 p1, fte_items.fte_head_id p2, 0 p11, null p3, time_dims.year p4, sum(data.value) p5, fte_items.for_achieving_milestone p12, fte_items.target_market p100  "+
		"from  fte_items, data, time_dims where "+
		"fte_items.alt_fte_id = ? and "+
		"fte_items.task_internal_id is not null and "+
		"data.fte_item_id = fte_items.id and "+
		"time_dims.val = data.year "+
		"group by  classify1, fte_items.fte_head_id, time_dims.year, for_achieving_milestone, fte_items.target_market "+
		") "+
		") "+
		"order by p1, p2 ,p11 nulls last, p12, p100, p4 " : 
		"select p1,p2,p11, p3,p4,p5, p12,'',p100 from ( "+
		"( "+
		"select classify1 p1, fte_items.fte_head_id p2, fte_items.id p11, fte_items.name p3, time_dims.year p4, sum(data.value) p5, fte_items.for_achieving_milestone p12, fte_items.target_market p100  "+
		"from  fte_items, data, time_dims where "+
		"fte_items.alt_fte_id = ? and "+
		"fte_items.task_internal_id is null and "+
		"data.fte_item_id = fte_items.id and "+
		"time_dims.val = data.year "+
		"group by classify1, fte_items.fte_head_id, fte_items.id, fte_items.name, time_dims.year, for_achieving_milestone, fte_items.target_market "+
		") "+
		"union "+
		"( "+
		"select classify1 p1, fte_items.fte_head_id p2, 0 p11, null p3, time_dims.year p4, sum(data.value) p5, fte_items.for_achieving_milestone p12, fte_items.target_market p100  "+
		"from  fte_items, data, time_dims where "+
		"fte_items.alt_fte_id = ? and "+
		"fte_items.task_internal_id is not null and "+
		"data.fte_item_id = fte_items.id and "+
		"time_dims.val = data.year "+
		"group by  classify1, fte_items.fte_head_id, time_dims.year, for_achieving_milestone, fte_items.target_market "+
		") "+
		") "+
		"order by p1, p2 ,p11 desc, p12, p100, p4 ";

	//query for getting the list of projects, the workspaces that should belong

	//get map_type info
	public static String PREP_PORT_GET_MAP = "select map_type from port_wksps where id = ? and isdefault = 1";

	//clean up port_wksp_assoc
	public static String PREP_PORT_CLEAN_ASSO = "delete from port_wksp_asso where port_wksp_id = ?";

	//get the current list of entries in the projects for autogen
	public static String PREP_PORT_SET_CURR_PRJ = "insert into port_wksp_asso (map_type, use_latest, port_wksp_id, prj_id, wspace_id) "+
		"( "+
		"select ?,1,?, projects.id, pj_map_items.wspace_id from projects, pj_map_items where projects.id = pj_map_items.prj_id and pj_map_items.map_type = ? and pj_map_items.isdefault = 1 and projects.status in (2,3,4))";

	public static String PREP_PORT_SET_TEMPLATE_PRJ = "insert into port_wksp_asso (map_type, use_latest, port_wksp_id, prj_id, wspace_id) "+
		"( "+
		"select 1,1,?, projects.id, pj_map_items.wspace_id from projects, pj_map_items where projects.id = pj_map_items.prj_id and pj_map_items.map_type = 1 and pj_map_items.isdefault = 1 and projects.status in (7))";

//TODO_INQUERY ... not imp
	public static String PREP_PORT_DEL_DEL_PROJ = "delete from port_wksp_asso where port_wksp_asso.port_wksp_id = ? and "+
		"port_wksp_asso.prj_id in (select projects.id "+
		"    from projects, port_wksp_asso "+
		"    where port_wksp_asso.port_wksp_id = ? "+
		"    and port_wksp_asso.prj_id = projects.id "+
		"    and (projects.status not in (1,2,3,4,5,7) or (port_wksp_asso.use_latest = 1 and "+
		"          port_wksp_asso.map_type in (4,5) and "+
		"          not(exists(select 1 from pj_map_items, workspaces where "+
		"                        pj_map_items.wspace_id = workspaces.id "+
		"                        and workspaces.id = port_wksp_asso.wspace_id "+
		"                        and pj_map_items.isdefault = 1 "+
		"                     ) "+
		"              ) "+
		"            ) "+
		"           ) "+
		"      )";
	//delete the desired ver
	public static String PREP_PORT_CLEAN_DESIRED = "delete from port_desired_ver where port_wksp_id = ?";

	//find the desired versions
	public static String PREP_PORT_POP_DESIRED = "insert into port_desired_ver (port_wksp_id, prj_id, alt_id "+
		", ver_alt_mstone_id "+
		", ver_alt_basic_id "+
		", ver_alt_profile_id "+
		", ver_alt_model_id "+
		", ver_prj_basic_id "+
		", ver_alt_work_id "+
		", ver_alt_fte_id "+
		", ver_alt_devcost_id "+
		", ver_alt_opcost_id "+
		", ver_alt_combined_id "+
		", ver_alt_rev_id "+
		", ver_alt_rating_id "+
		", is_default_alt "+
		", fund_status) "+
		"( "+
		"select ?, projects.id, alt_map_items.alt_id "+
		", alt_map_items.alt_date_id "+
		", alt_map_items.alt_basic_id "+
		", alt_map_items.alt_profil_id "+
		", alt_map_items.alt_model_id "+
		", pj_map_items.pj_basic_id "+
		", alt_map_items.alt_work_id "+
		", alt_map_items.alt_fte_id "+
		", alt_map_items.alt_devcost_id "+
		", alt_map_items.alt_opcost_id "+
		", alt_map_items.alt_combined_id "+
		", alt_map_items.alt_rev_id "+
		", alt_map_items.alt_rating_id "+
		", alternatives.is_primary "+
		", case when (projects.status = 3 or alternatives.is_primary = 0) then 2 else 1 end "+
		"from alt_map_items, alternatives, projects, pj_map_items, labels, port_wksp_asso "+
		"where "+
		"port_wksp_asso.port_wksp_id = ? "+
		"and port_wksp_asso.use_latest = 0 "+
		"and projects.id = port_wksp_asso.prj_id "+
		"and alternatives.prj_id = projects.id "+
		"and labels.id = port_wksp_asso.label_id "+
		"and labels.pj_map_id = pj_map_items.id "+
		"and alt_map_items.label_id = labels.id "+
		"and alternatives.id = alt_map_items.alt_id "+
		"and projects.id = pj_map_items.prj_id "+
		"union "+
		"( "+
		"select ?, projects.id, alt_map_items.alt_id "+
		", alt_map_items.alt_date_id "+
		", alt_map_items.alt_basic_id "+
		", alt_map_items.alt_profil_id "+
		", alt_map_items.alt_model_id "+
		", pj_map_items.pj_basic_id "+
		", alt_map_items.alt_work_id "+
		", alt_map_items.alt_fte_id "+
		", alt_map_items.alt_devcost_id "+
		", alt_map_items.alt_opcost_id "+
		", alt_map_items.alt_combined_id "+
		", alt_map_items.alt_rev_id "+
		", alt_map_items.alt_rating_id "+
		", alternatives.is_primary "+
		", case when (projects.status = 3 or alternatives.is_primary = 0) then 2 else 1 end "+
		"from alt_map_items, alternatives, projects, pj_map_items, port_wksp_asso "+
		"where port_wksp_asso.port_wksp_id = ? "+
		"and port_wksp_asso.map_type in (1,2,7) "+
		"and port_wksp_asso.use_latest = 1 "+
		"and projects.id = port_wksp_asso.prj_id "+
		"and alternatives.prj_id = projects.id "+
		"and alt_map_items.map_type = port_wksp_asso.map_type "+
		"and pj_map_items.map_type = port_wksp_asso.map_type "+
		"and alt_map_items.isdefault = 1 "+
		"and pj_map_items.isdefault = 1 "+
		"and alt_map_items.alt_id = alternatives.id "+
		"and projects.id = pj_map_items.prj_id "+
		") "+
		"union "+
		"( "+
		"select ?, projects.id, alt_map_items.alt_id "+
		", alt_map_items.alt_date_id "+
		", alt_map_items.alt_basic_id "+
		", alt_map_items.alt_profil_id "+
		", alt_map_items.alt_model_id "+
		", pj_map_items.pj_basic_id "+
		", alt_map_items.alt_work_id "+
		", alt_map_items.alt_fte_id "+
		", alt_map_items.alt_devcost_id "+
		", alt_map_items.alt_opcost_id "+
		", alt_map_items.alt_combined_id "+
		", alt_map_items.alt_rev_id "+
		", alt_map_items.alt_rating_id "+
		", alternatives.is_primary "+
		", case when (projects.status = 3 or alternatives.is_primary = 0) then 2 else 1 end "+
		"from alt_map_items, alternatives, projects, pj_map_items, port_wksp_asso "+
		"where port_wksp_asso.port_wksp_id = ? "+
		"and not(port_wksp_asso.map_type in (1,2,7)) "+
		"and port_wksp_asso.use_latest = 1 "+
		"and projects.id = port_wksp_asso.prj_id "+
		"and alternatives.prj_id = projects.id "+
		"and pj_map_items.wspace_id = port_wksp_asso.wspace_id "+
		"and alt_map_items.wspace_id = port_wksp_asso.wspace_id "+
		"and alt_map_items.isdefault = 1 "+
		"and pj_map_items.isdefault = 1 "+
		"and alt_map_items.alt_id = alternatives.id "+
		"and projects.id = pj_map_items.prj_id "+
		") "+
		") ";


	//query to insert missing things into .. will have to do two steps
	// one where no alternative from project is there

	// one where there are some alternatives from project in which case all alts are non-prim and fund_status is 2

	//this one inserts for project for which there are no alt defined in rset
	public static String PREP_PORT_INSERT_MISSING_PROJECT = Misc.G_DO_ORACLE ? "insert into port_results "+
		"(id, alt_id, prj_id, port_rs_id, is_default_alt, fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", tot_delay_mon) "+
		"( "+
		"select seq_detailed_port_rsets.nextval "+
		", port_desired_ver.alt_id "+
		", port_desired_ver.prj_id "+
		", port_rset.id "+
		", is_default_alt "+
		", fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", 0 "+
		"from port_rset, port_desired_ver, "+
		"  (select port_wksp_asso.prj_id prjid, port_rset.id port_rs_id from port_wksp_asso, port_rset where port_wksp_asso.port_wksp_id = ? "+
		"     and port_rset.port_wksp_id = port_wksp_asso.port_wksp_id "+
		"   minus "+
		"  select port_results.prj_id, port_rset.id from port_results, port_rset "+
		"     where port_rset.port_wksp_id = ? and port_results.port_rs_id = port_rset.id ) newPrjList "+
		"where port_desired_ver.port_wksp_id = ? "+
		"    and port_desired_ver.prj_id = newPrjList.prjId "+
		"    and port_rset.id = newPrjList.port_rs_id "+
		") " 
    : "insert into port_results "+
		"(alt_id, prj_id, port_rs_id, is_default_alt, fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", tot_delay_mon) "+
		"( "+
		"select  "+
		" port_desired_ver.alt_id "+
		", port_desired_ver.prj_id "+
		", port_rset.id "+
		", is_default_alt "+
		", fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", 0 "+
		"from port_rset, port_desired_ver, "+
		"  (select port_wksp_asso.prj_id prjid, port_rset.id port_rs_id from port_wksp_asso, port_rset where port_wksp_asso.port_wksp_id = ? "+
		"     and port_rset.port_wksp_id = port_wksp_asso.port_wksp_id "+
		"   and Not Exists( "+
		"  select port_results.prj_id, port_rset.id from port_results, port_rset "+
		"     where port_rset.port_wksp_id = ? and port_results.port_rs_id = port_rset.id and port_results.prj_id=port_wksp_asso.prj_id and port_results.port_rs_id = port_rset.id)) newPrjList "+
		"where port_desired_ver.port_wksp_id = ? "+
		"    and port_desired_ver.prj_id = newPrjList.prjId "+
		"    and port_rset.id = newPrjList.port_rs_id "+
		") ";

	//this one inserts alts that are not in port_rset but have projects in port_rset
	//in this case isdefault = 0 and is_fund status = 2 (unfunded
	public static String PREP_PORT_INSERT_MISSING_ALT = Misc.G_DO_ORACLE ? " insert into port_results "+
		"(id, alt_id, prj_id, port_rs_id, is_default_alt, fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", tot_delay_mon "+
		") "+
		"( "+
		"  select seq_detailed_port_rsets.nextval "+
		"  , port_desired_ver.alt_id "+
		"  , port_desired_ver.prj_id "+
		"  , port_rset.id "+
		"  , 0 "+
		"  , 2 "+
		"  , ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		"  , ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		"  , ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		"  , ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		"  , 0 "+
		"  from port_rset, port_desired_ver, "+
		"(select distinct alternatives.id alt_id, pjl.port_rs_id port_rs_id from alternatives, "+
		"         (select prj_id, port_rset.id port_rs_id from port_results, port_rset "+
		"           where port_rset.port_wksp_id=? "+
		"             and port_results.port_rs_id = port_rset.id) pjl "+
		" where alternatives.prj_id = pjl.prj_id "+
		"minus "+
		"(select alt_id, port_rset.id from port_results, port_rset where port_rset.port_wksp_id=? "+
		"             and port_results.port_rs_id = port_rset.id "+
		")) desl "+
		" where port_desired_ver.port_wksp_id = ? "+
		"    and port_desired_ver.alt_id = desl.alt_id "+
		"    and port_rset.id = desl.port_rs_id "+
		") " 
    : 
    " insert into port_results "+
		"(alt_id, prj_id, port_rs_id, is_default_alt, fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", tot_delay_mon "+
		") "+
		"( "+
		"  select  "+
		"   port_desired_ver.alt_id "+
		"  , port_desired_ver.prj_id "+
		"  , port_rset.id "+
		"  , 0 "+
		"  , 2 "+
		"  , ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		"  , ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		"  , ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		"  , ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		"  , 0 "+
		"  from port_rset, port_desired_ver, "+
		"(select distinct alternatives.id alt_id, pjl.port_rs_id port_rs_id from alternatives, "+
		"         (select prj_id, port_rset.id port_rs_id from port_results, port_rset "+
		"           where port_rset.port_wksp_id=? "+
		"             and port_results.port_rs_id = port_rset.id) pjl "+
		" where alternatives.prj_id = pjl.prj_id "+
		" and Not Exists "+
		"(select alt_id, port_rset.id from port_results, port_rset where port_rset.port_wksp_id=? "+
		"             and port_results.port_rs_id = port_rset.id and port_results.alt_id = alternatives.id and port_results.port_rs_id = pjl.port_rs_id "+
		")) desl "+
		" where port_desired_ver.port_wksp_id = ? "+
		"    and port_desired_ver.alt_id = desl.alt_id "+
		"    and port_rset.id = desl.port_rs_id "+
		") ";

	//Now update all auto_generated ones with system chosen version info
	public static String PREP_PORT_INSERT_UPDATE_AUTO = Misc.G_DO_ORACLE? "update "+
		" (select port_results.id, alt_id altid, "+
		"ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, "+
		"ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, "+
		"ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, "+
		"ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		"from port_results, port_rset "+
		"where port_rset.port_wksp_id = ? "+
		"and   port_results.port_rs_id = port_rset.id "+
		"and   port_rset.is_auto_updateable = 1) toupd "+
		"set "+
		"(ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, "+
		"ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, "+
		"ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, "+
		"ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id) "+
		"= "+
		"(select "+
		"ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, "+
		"ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, "+
		"ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, "+
		"ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id from port_desired_ver "+
		"where port_desired_ver.port_wksp_id = ? "+
		"and toupd.altid = port_desired_ver.alt_id "+
		") "
    :
    " update port_results "+
" set ver_alt_mstone_id = frm.ver_alt_mstone_id "+
" , ver_alt_basic_id = frm.ver_alt_basic_id "+
" , ver_alt_profile_id = frm.ver_alt_profile_id "+ 
" , ver_alt_model_id = frm.ver_alt_model_id "+
" , ver_prj_basic_id = frm.ver_prj_basic_id "+
" , ver_alt_work_id = frm.ver_alt_work_id "+
" , ver_alt_fte_id = frm.ver_alt_fte_id "+
" , ver_alt_devcost_id = frm.ver_alt_devcost_id "+
" , ver_alt_opcost_id = frm.ver_alt_opcost_id "+
" , ver_alt_combined_id = frm.ver_alt_combined_id "+
" , ver_alt_rev_id = frm.ver_alt_rev_id "+
" , ver_alt_rating_id = frm.ver_alt_rating_id "+
" from port_results toupd, port_desired_ver frm, port_rset "+
" where toupd.port_rs_id = port_rset.id "+
"    and frm.port_wksp_id = ? "+
"    and toupd.alt_id = frm.alt_id "+
"    and port_rset.port_wksp_id = ? "+ 
"    and port_rset.is_auto_updateable =1 ";


	//Get the list of entries that require dw_updates ..
  /*102307
	public static String PREP_PORT_GET_REQ_DW = Misc.G_DO_ORACLE ? "select distinct prj_id, alt_id, "+
		"ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, "+
		"ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, "+
		"ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, "+
		"ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		"from port_results, port_rset where port_rset.port_wksp_id = ? "+
		"and port_results.port_rs_id = port_rset.id "+
		"minus "+
		"select pj_id, alt_id, "+
		"ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, "+
		"ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, "+
		"ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, "+
		"ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		"from dw_alt_short_info " : "select distinct prj_id, alt_id, "+
		"ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, "+
		"ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, "+
		"ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, "+
		"ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		"from port_results, port_rset where port_rset.port_wksp_id = ? "+
		"and port_results.port_rs_id = port_rset.id "+
		"and Not Exists( "+
		"select pj_id, alt_id, "+
		"ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, "+
		"ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, "+
		"ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, "+
		"ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		"from dw_alt_short_info) ";
*/
	public static String PREP_PORT_DEL_EXTRA_RESULTS = "delete from port_results where "+
		"port_results.port_rs_id in "+
		"(select port_rset.id from port_rset where port_rset.port_wksp_id = ?) "+
		"and "+
		"not (port_results.alt_id in "+
		"(select alt_id from port_desired_ver where port_wksp_id = ?) "+
		") ";

	public static String PREP_GET_PORT_WKSPINFO = "select prj_id, wspace_id, map_type, use_latest, label_id from port_wksp_asso where port_wksp_asso.port_wksp_id = ?";
	public static String PORT_GET_WKSP_FRONT_PAGE = "select port_wksps.id, port_wksps.name, port_wksps.port_wksp_desc, port_wksps.map_type "+
//G_WATSON_031507_ROLLBACK
		" , port_rset.id, port_rset.name, port_wksps.created_on "+
//		" , port_rset.id, port_rset.name "+
		" from port_wksps, port_rset "+
		" where port_rset.port_wksp_id = port_wksps.id "+
		" and port_wksps.isdefault = 1 and port_wksps.IS_FOR_PRJ_DW = 0"+
		" order by port_wksps.id desc, port_rset.id desc";
	public static String DELETE_PORT_RESULTS = "delete from port_results where port_rs_id = ?";
	public static String DELETE_PORT_RSET = "delete from port_rset where id = ?";
	public static String GET_AUTOGEN_PORT_RSET = "select port_rset.id from port_rset where port_wksp_id = ? and IS_AUTO_UPDATEABLE = 1 order by id asc";
	public static String GET_PORT_RSET_SUMMARY = "select port_rset.name, port_rset.description, IS_AUTO_UPDATEABLE from port_rset where id = ?";
	public static String GET_PORT_RSET_DETAIL =
		"select "+
		"curr.prj_id, "+
		"curr.fund_status, "+
		"curr.ver_alt_mstone_id, "+
		"curr.ver_alt_basic_id, "+
		"curr.ver_alt_profile_id, "+
		"curr.ver_alt_model_id, "+
		"curr.ver_prj_basic_id, "+
		"curr.ver_alt_work_id, "+
		"curr.ver_alt_fte_id, "+
		"curr.ver_alt_devcost_id, "+
		"curr.ver_alt_opcost_id, "+
		"curr.ver_alt_combined_id, "+
		"curr.ver_alt_rev_id, "+
		"curr_ms.finish_dt, "+
		"curr_ms.mstn_id, "+
		"comp.prj_id, "+
		"comp.fund_status, "+
		"comp.ver_alt_mstone_id, "+
		"comp.ver_alt_basic_id, "+
		"comp.ver_alt_profile_id, "+
		"comp.ver_alt_model_id, "+
		"comp.ver_prj_basic_id, "+
		"comp.ver_alt_work_id, "+
		"comp.ver_alt_fte_id, "+
		"comp.ver_alt_devcost_id, "+
		"comp.ver_alt_opcost_id, "+
		"comp.ver_alt_combined_id, "+
		"comp.ver_alt_rev_id, "+
		"comp_ms.finish_dt, "+
		"comp_ms.mstn_id "+
		",assoc.wspace_id, assoc.map_type, assoc.label_id, assoc.use_latest, assoc.wsname, curr.alt_id, assoc.prjname "+
		"from port_results curr, port_results comp, "+
		"    milestones curr_ms, milestones comp_ms, "+
		"    (select port_wksp_asso.prj_id, port_wksp_asso.wspace_id, port_wksp_asso.map_type, port_wksp_asso.label_id, "+
		"            port_wksp_asso.use_latest, workspaces.name wsname, projects.name prjname"+
		"     from port_rset, port_wksp_asso, workspaces, projects where "+
		"          port_rset.id = ? and port_wksp_asso.port_wksp_id = port_rset.port_wksp_id "+
		"          and workspaces.id = port_wksp_asso.wspace_id and projects.id=port_wksp_asso.prj_id) assoc "+
		"where "+
		"    curr.port_rs_id = ? "+
		"and comp.port_rs_id = ? "+
		"and assoc.prj_id = curr.prj_id "+
		"and curr.alt_id = comp.alt_id "+
		"and curr.is_default_alt = 1 "+
		"and curr_ms.alt_date_id = curr.ver_alt_mstone_id "+
		"and comp_ms.alt_date_id = comp.ver_alt_mstone_id "+
		"and curr_ms.mstn_id = comp_ms.mstn_id "+
		"and curr_ms.target_market = comp_ms.target_market "+
		"order by curr.prj_id desc, curr_ms.mstn_id desc ";

	public static String COMP_WORKSPACE_LIST = "select workspaces.id, workspaces.name from workspaces, pj_map_items where pj_map_items.wspace_id = workspaces.id and workspaces.prj_id = ? and workspaces.id <> ? and isdefault=1";
	public static String COMP_SCENARIO_LIST = "select port_rset.port_wksp_id, port_rset.id, port_wksps.name, port_rset.name, port_rset.description from port_rset, port_wksps where "+
		"port_rset.port_wksp_id = port_wksps.id and port_wksps.map_type in (1,4) and port_rset.IS_AUTO_UPDATEABLE = 0 and port_wksps.IS_FOR_PRJ_DW = 0";
	public static String GET_SCENARIO_SPECIFIC_VERSION_PRJ = Misc.G_DO_ORACLE ? "select port_results.ver_prj_basic_id, port_wksps.name || '.' || port_rset.name from port_wksps, port_rset, port_results where port_results.port_rs_id = ? and port_results.prj_id = ? and port_results.port_rs_id = port_rset.id and port_rset.port_wksp_id = port_wksps.id" 
  : "select port_results.ver_prj_basic_id, port_wksps.name + '.' + port_rset.name from port_wksps, port_rset, port_results where port_results.port_rs_id = ? and port_results.prj_id = ? and port_results.port_rs_id = port_rset.id and port_rset.port_wksp_id = port_wksps.id";
	public static String GET_SCENARIO_SPECIFIC_VERSION_ALT = Misc.G_DO_ORACLE ? "select ver_alt_basic_id, ver_alt_profile_id, ver_alt_mstone_id, ver_alt_model_id, 1,"+
  " port_wksps.name || '.' || port_rset.name, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rating_id from "+
  " port_wksps, port_rset, port_results where port_results.port_rs_id = ? and port_results.alt_id = ? and port_results.port_rs_id = port_rset.id and "+
  "port_rset.port_wksp_id = port_wksps.id" : "select ver_alt_basic_id, ver_alt_profile_id, ver_alt_mstone_id, ver_alt_model_id, 1, port_wksps.name + '.' + port_rset.name, "+
  "ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_rev_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rating_id from port_wksps, port_rset, port_results "+
  "where port_results.port_rs_id = ? and port_results.alt_id = ? and port_results.port_rs_id = port_rset.id and port_rset.port_wksp_id = port_wksps.id";
	public static String GET_SCENARIO_NAME = Misc.G_DO_ORACLE ? "select  port_wksps.name || '.' || port_rset.name from port_wksps, port_rset where  port_rset.id = ? and port_rset.port_wksp_id = port_wksps.id" :  "select  port_wksps.name + '.' + port_rset.name from port_wksps, port_rset where  port_rset.id = ? and port_rset.port_wksp_id = port_wksps.id";
	public static String GET_MAP_TYPE = "select map_type from pj_map_items where wspace_id = ? and isdefault = 1 order by map_type asc";

	public static String GET_FTE_UTIL = Misc.G_DO_ORACLE ? "select util, rate, fte_head_id, classify1, classify2, classify3, classify4, classify5 from fte_head_details order by fte_head_id, classify1 nulls first, classify2 nulls first, classify3 nulls first, classify4 nulls first, classify5 nulls first" : "select util, rate, fte_head_id, classify1, classify2, classify3, classify4, classify5 from fte_head_details order by fte_head_id, classify1 asc, classify2 asc, classify3 asc, classify4 asc, classify5 asc";
	public static String DEL_FTE_UTIL = "delete from fte_head_details";
	public static String UPD_FTE_UTIL = "insert into fte_head_details (rate, util, fte_head_id, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?,?)";
	public static String UPD_ALT_DATES_POS = "update alt_dates set pos = ? where id = ?";
	public static String SET_PRJ_PLAN_TEMPLATE = "update projects set prj_template_id=? where id = ?";

	//alt work copy
	public static String COPY_ALT_WORK = //1=> new alt_work_id, 2=>user updating, old alt_work_id 
  Misc.G_DO_ORACLE ? "insert into alt_works (id, alt_id, wspace_id, file_name_file_name_id, "+
		"isfileinsycnwithdata, last_update_on, status, last_update_by, ref_alt_fte_id, ref_alt_devcost_id) "+
		"(select ?, alt_id, wspace_id, file_name_file_name_id, "+
		"0, sysdate, status, ?, ref_alt_fte_id, ref_alt_devcost_id from alt_works where alt_works.id = ?) " 
    :
		"insert into alt_works (alt_id, wspace_id, file_name_file_name_id, "+
		"isfileinsycnwithdata, last_update_on, status, last_update_by, ref_alt_fte_id, ref_alt_devcost_id) "+
		"(select alt_id, wspace_id, file_name_file_name_id, "+
		"0, getdate(), status, ?, ref_alt_fte_id, ref_alt_devcost_id from alt_works where alt_works.id = ?) ";

	//alt work items and hierarchy
	public static String COPY_ALT_WORK_ITEMS = //1->new alt_work_id 2->old alt_work_id 
  Misc.G_DO_ORACLE ? "insert into alt_work_items ( "+
		"  id, parent_internal_id, alt_work_id, name, start_date, end_date, complete "+
		"  ,actual_start, actual_end, internal_id, ref_id "+
		"  ,isconstant, is_split, status, type, pred "+
		"  ,for_achieving_ms, wbs_level, classify1 "+
		"  ,classify2, classify3, classify4, classify5 "+
		"  ,rollup_policy, min_dur, max_dur, p10_dur, p90_dur "+
		"  ,distrib, interruptible, cc, comments "+
		"  ,classify6, classify7, ismilestone "+
//G_WATSON_031507_ROLLBACK
		"  ,variancecode, user_comment, to_include, rollup_cost, notes, target_market, status_calculated, lhs_number, rhs_number "+
//		"  ,variancecode, user_comment, to_include, rollup_cost, notes, target_market "+
		") "+
		"(select seq_alt_work_items.nextval, parent_internal_id, ?, name, start_date, end_date, complete "+
		"  ,actual_start, actual_end, internal_id, ref_id "+
		"  ,isconstant, is_split, status, type, pred "+
		"  ,for_achieving_ms, wbs_level, classify1 "+
		"  ,classify2, classify3, classify4, classify5 "+
		"  ,rollup_policy, min_dur, max_dur, p10_dur, p90_dur "+
		"  ,distrib, interruptible, cc, comments "+
		"  ,classify6, classify7, ismilestone "+
//G_WATSON_031507_ROLLBACK
		"  ,variancecode, user_comment, to_include, rollup_cost, notes, target_market, status_calculated, lhs_number, rhs_number "+
//		"  ,variancecode, user_comment, to_include, rollup_cost, notes, target_market "+
		"from alt_work_items where alt_work_id=? "+
		") " :
		"insert into alt_work_items ( "+
		"  parent_internal_id, alt_work_id, name, start_date, end_date, complete "+
		"  ,actual_start, actual_end, internal_id, ref_id "+
		"  ,isconstant, is_split, status, type, pred "+
		"  ,for_achieving_ms, wbs_level, classify1 "+
		"  ,classify2, classify3, classify4, classify5 "+
		"  ,rollup_policy, min_dur, max_dur, p10_dur, p90_dur "+
		"  ,distrib, interruptible, cc, comments "+
		"  ,classify6, classify7, ismilestone "+
//G_WATSON_031507_ROLLBACK
		"  ,variancecode, user_comment, to_include, rollup_cost, notes, target_market, status_calculated, lhs_number, rhs_number "+
//		"  ,variancecode, user_comment, to_include, rollup_cost, notes, target_market "+
		") "+
		"(select parent_internal_id, ?, name, start_date, end_date, complete "+
		"  ,actual_start, actual_end, internal_id, ref_id "+
		"  ,isconstant, is_split, status, type, pred "+
		"  ,for_achieving_ms, wbs_level, classify1 "+
		"  ,classify2, classify3, classify4, classify5 "+
		"  ,rollup_policy, min_dur, max_dur, p10_dur, p90_dur "+
		"  ,distrib, interruptible, cc, comments "+
		"  ,classify6, classify7, ismilestone "+
//G_WATSON_031507_ROLLBACK
		"  ,variancecode, user_comment, to_include, rollup_cost, notes, target_market, status_calculated, lhs_number, rhs_number "+
//		"  ,variancecode, user_comment, to_include, rollup_cost, notes, target_market "+
		"from alt_work_items where alt_work_id=? "+
		") ";

	public static String GET_REF_FTE_DEVMODEL = "select ref_alt_fte_id, ref_alt_devcost_id, wspace_id from alt_works where id=?";
	public static String GET_PORTRSET_ALTINFO = "select ver_alt_mstone_id, ver_alt_work_id, ver_alt_basic_id, ver_prj_basic_id, ver_alt_fte_id, ver_alt_devcost_id from port_results where port_results.port_rs_id = ? and alt_id = ?";

	public static String UPD_WORK_ITEM_CHANGES = "update alt_work_items set start_date = ?, end_date = ?, to_include = ? where alt_work_id=? and internal_id=?";
	public static String UPD_PJPLAN_FTE_ITEM_CHANGES = "update fte_items set start_date = ?, end_date = ? where alt_fte_id = ? and task_internal_id=?";
	public static String UPD_PJPLAN_COST_ITEM_CHANGES = "update cost_items set start_date = ?, end_date = ? where cost_items.alt_devcost_id = ? and task_internal_id=?";


	public static String SET_PJPLAN_FTE_DATA_START = Misc.G_DO_ORACLE ? "update (select data.year from data, fte_items where "+
		"data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ? and fte_items.task_internal_id=?) upd set upd.year = intelli.getTimeId(intelli.getDateFor(upd.year)+?)" : "update (select data.year from data, fte_items where "+
		"data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ? and fte_items.task_internal_id=?) upd set upd.year = intelli.getTimeId(intelli.getDateFor(upd.year)+?)";
	public static String SET_PJPLAN_COST_DATA_START = Misc.G_DO_ORACLE ? "update (select data.year from data, cost_items where "+
		"data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? and cost_items.task_internal_id=?) upd set upd.year = getTimeId(getDateFor(upd.year)+?)" : "update (select data.year from data, cost_items where "+
		"data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? and cost_items.task_internal_id=?) upd set upd.year = intelli.getTimeId(intelli.getDateFor(upd.year)+?)";
    //TODO_INQUERY .. not imp
	public static String DEL_FTE_DATA = Misc.G_DO_ORACLE ?
		"delete from (select data.year from data, fte_items where "+
		"data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ? and fte_items.task_internal_id=?) "
		:
		"delete from data where data.year in (select data.year from data, fte_items where "+
		"data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ? and fte_items.task_internal_id=?) ";
    //TODO_INQUERY
	public static String DEL_COST_DATA = Misc.G_DO_ORACLE ? 
		"delete from (select data.year from data, cost_items where "+
		"data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? and cost_items.task_internal_id=?)"
		:
     "delete from data from cost_items where data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? and cost_items.task_internal_id= ? ";

	public static String DEL_FTE_ITEM = "delete from fte_items where alt_fte_id = ? and task_internal_id=?";
	public static String DEL_COST_ITEMS = "delete from cost_items where cost_items.alt_devcost_id = ? and task_internal_id=?";

	public static String SET_MS_FTE_DATA_START = Misc.G_DO_ORACLE ? "update (select data.year from data, fte_items where "+
		"data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ? and fte_items.for_achieving_milestone=? and (fte_items.target_market=? or fte_items.target_market is null)) upd set upd.year = getTimeId(getDateFor(upd.year)+?)" :  "update (select data.year from data, fte_items where "+
		"data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ? and fte_items.for_achieving_milestone=? and (fte_items.target_market=? or fte_items.target_market is null)) upd set upd.year = intelli.getTimeId(intelli.getDateFor(upd.year)+?)";
	public static String SET_MS_COST_DATA_START = Misc.G_DO_ORACLE ? "update (select data.year from data, cost_items where "+
		"data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ?  and cost_items.for_achieving_milestone=? and (cost_items.target_market=? or cost_items.target_market is null)) upd set upd.year = getTimeId(getDateFor(upd.year)+?)" :  "update (select data.year from data, cost_items where "+
		"data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ?  and cost_items.for_achieving_milestone=? and (cost_items.target_market=? or cost_items.target_market is null)) upd set upd.year = intelli.getTimeId(intelli.getDateFor(upd.year)+?)";
    //TODO_INQUERY 
		public static String DEL_MS_FTE_DATA = Misc.G_DO_ORACLE ?
		"delete from (select data.year from data, fte_items where "+
		"data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ?  and fte_items.for_achieving_milestone=? and (fte_items.target_market=? or fte_items.target_market is null)) "
		:    
		"delete from data from fte_items where data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ?  and fte_items.for_achieving_milestone=? and (fte_items.target_market=? or fte_items.target_market is null) ";
    //TODO_INQUERY
	public static String DEL_MS_COST_DATA = Misc.G_DO_ORACLE ?
		"delete from (select data.year from data, cost_items where "+
		"data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ?  and cost_items.for_achieving_milestone=? and (cost_items.target_market=? or cost_items.target_market is null))"
		:
    "delete from data from cost_items where data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ?  and cost_items.for_achieving_milestone=? and (cost_items.target_market=? or cost_items.target_market is null) ";

	public static String DEL_MS_FTE_ITEM = "delete from fte_items where alt_fte_id = ?  and fte_items.for_achieving_milestone=? and (fte_items.target_market=? or fte_items.target_market is null)";
	public static String DEL_MS_COST_ITEMS = "delete from cost_items where cost_items.alt_devcost_id = ?  and cost_items.for_achieving_milestone=? and (cost_items.target_market=? or cost_items.target_market is null)";

//TODO_INQUERY .. not imp
	public static String TOINCL_FTE = //1 ref fte, 3 of alt_work_id //CHANGE_HIER
		"update fte_items set to_include = 0 where alt_fte_id=? and task_internal_id in "+
		"(select children.internal_id from alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number ) "+
    " where ancs.to_include=0 or status=1 and ancs.alt_work_id = ? "+
    ") ";
	public static String TOINCL_COST = //1 ref devcost, 3 of alt_work_id //CHANGE_HIER
		"update cost_items set to_include = 0 where cost_items.alt_devcost_id =? and task_internal_id in "+
		"(select children.internal_id from  alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  "+
    " where ancs.to_include=0 or status=1 and ancs.alt_work_id = ? and (children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number) "+
    ") ";

	public static String COPY_PORT_RESULTS_NEW =Misc.G_DO_ORACLE ? "insert into port_results "+
		"(id, alt_id, prj_id, port_rs_id, is_default_alt, fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", tot_delay_mon) "+
		"( "+
		"select "+
		"seq_detailed_port_rsets.nextval, alt_id, prj_id, ?, is_default_alt, fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", tot_delay_mon "+
		"from port_results where port_rs_id = ? "+
		") " 
    : 
    "insert into port_results "+
		"( alt_id, prj_id, port_rs_id, is_default_alt, fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", tot_delay_mon) "+
		"( "+
		"select "+
		" alt_id, prj_id, ?, is_default_alt, fund_status "+
		", ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id "+
		", ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id "+
		", ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id "+
		", ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id "+
		", tot_delay_mon "+
		"from port_results where port_rs_id = ? "+
		") ";

	public static String UPD_PORT_RESULT_ALT = "update port_results set fund_status=?, is_default_alt=?, tot_delay_mon=? where port_results.port_rs_id = ? and alt_id = ?";
	public static String COPY_DATE_FROM_PORT_RS = Misc.G_DO_ORACLE ? 
  "insert into alt_dates (id, wspace_id, alt_id, pos) (select ?, wspace_id, port_results.alt_id, pos from alt_dates, port_results where port_rs_id=? and port_results.alt_id=? and ver_alt_mstone_id = alt_dates.id)"
  :
  "insert into alt_dates (wspace_id, alt_id, pos) (select wspace_id, port_results.alt_id, pos from alt_dates, port_results where port_rs_id=? and port_results.alt_id=? and ver_alt_mstone_id = alt_dates.id)";
	public static String UPD_PORT_RSET_INDIVIDUAL_VERSIONS =
		"update port_results upd set (ver_alt_mstone_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_work_id, ver_prj_basic_id) = "+
		"( select "+
		" (case when ? is null then ver_alt_mstone_id else ? end), "+
		" (case when ? is null then ver_alt_fte_id else ? end), "+
		" (case when ? is null then ver_alt_devcost_id else ? end), "+
		" (case when ? is null then ver_alt_work_id else ? end), "+
		" (case when ? is null then ver_prj_basic_id else ? end) "+
		"  from port_results where port_rs_id = upd.port_rs_id and alt_id = upd.alt_id "+
		") "+
		"where port_rs_id = ? and alt_id = ? ";

	public static String UPD_PORT_ISDEFAULT_ALT_FROM2 =
		"update port_results upd set is_default_alt=0 where port_rs_id = ? and is_default_alt = 1 and exists(select alt_id from port_results "+
		"  where port_rs_id = upd.port_rs_id "+
		"  and prj_id = upd.prj_id "+
		"  and is_default_alt = 2) ";

	public static String UPD_PORT_ISDEFAULT_SET_2_to_1 =
		"update port_results set is_default_alt=1 where is_default_alt=2 and port_rs_id=?";

	public static String GET_COUNT_PORT_RSET = "select count(*) from port_rset where port_wksp_id=?";
	public static String GET_CURR_ALT_MAP_ITEM = "select alt_map_items.alt_work_id from alt_map_items where wspace_id = ? and alt_id = ? and isdefault = 1";

	public static String STATUS_PORT_RSET_LIST = Misc.G_DO_ORACLE ? "select port_rset.id, port_wksps.id, port_wksps.name from port_wksps, port_rset where port_rset.port_wksp_id = port_wksps.id and port_wksps.map_type in (1,2,5,7,11) and isdefault=1 order by is_auto_updateable desc nulls last" :  "select port_rset.id, port_wksps.id, port_wksps.name from port_wksps, port_rset where port_rset.port_wksp_id = port_wksps.id and port_wksps.map_type in (1,2,5,7,11) and isdefault=1 order by is_auto_updateable desc";

	//Old
	//   public static String VAR_REP_MASTER_SEL_CLAUSE =
	//   "currInfo.prj_id, currInfo.alt_id, projects.status, currInfo.fund_status, "+
	//       " currInfo.internal_id, currInfo.type, currInfo.start_date, "+
	//       " currInfo.end_date, baseInfo.start_date, baseInfo.end_date, "+
	//       " currInfo.name itemName, projects.name, "+
	//       " currInfo.variancecode, currInfo.user_comment, currInfo.complete, currInfo.actual_end, "+
	//       " currInfo.alt_work_id, baseInfo.alt_work_id, currInfo.to_include, currInfo.status ";

	//   public static String VAR_REP_MASTER_PART_1 =
	//" from projects, port_results, pj_basics, "+
	//" (select alt_id, internal_id, start_date, end_date, type, alt_work_id "+
	//" from port_results, alt_work_items "+
	//" where port_results.port_rs_id = ? "+
	//"   and is_default_alt=1 "+
	//"   and alt_work_items.alt_work_id = port_results.ver_alt_work_id "+
	//"   and type in (";//1,2,3

	//   public static String VAR_REP_MASTER_PART_2 =
	//                 ")) baseInfo, "+
	//" (select alt_id, internal_id, start_date, end_date, type, prj_id, fund_status,name, variancecode, user_comment, alt_work_id "+
	//"        ,complete, actual_end, to_include, status "+
	//" from port_results, alt_work_items "+
	//" where port_results.port_rs_id = ? "+
	//"   and is_default_alt = 1 "+
	//"   and alt_work_items.alt_work_id = port_results.ver_alt_work_id "+
	//"   and type in (";

	//   public static String VAR_REP_MASTER_PART_3 =
	//                 ")) currInfo "+
	//" where port_results.port_rs_id = ? "+
	//"   and port_results.is_default_alt = 1 "+
	//"   and port_results.prj_id = projects.id "+
	//"   and port_results.ver_prj_basic_id = pj_basics.id "+
	//"   and port_results.prj_id = currInfo.prj_id(+) "+
	//"   and currInfo.alt_id = baseInfo.alt_id(+) "+
	//"   and currInfo.internal_id = baseInfo.internal_id(+) "+
	//" order by projects.name asc, currInfo.type asc, currInfo.end_date asc ";

	//NEW .... whew ...
	public static String VAR_REP_1 =
		"   currInfo.prj_id, currInfo.alt_id, currInfo.prj_status, currInfo.fund_status, "+
		"        currInfo.internal_id, currInfo.type, currInfo.start_date, "+
		"        currInfo.end_date, baseInfo.start_date, baseInfo.end_date, "+
		"        currInfo.itemName itemName, currInfo.prj_name, "+
		"        currInfo.variancecode, currInfo.user_comment, currInfo.complete, currInfo.actual_end, "+
		"        currInfo.alt_work_id, baseInfo.alt_work_id, currInfo.to_include, currInfo.status_calculated, currInfo.last_update_on, baseInfo.status_calculated "+
		"from "+
		"(select port_results.alt_id, internal_id, start_date, end_date, type, port_results.prj_id, fund_status, itemname, variancecode, user_comment, alt_work_id "+
		"        ,complete, actual_end, to_include, currList.status_calculated, rn, projects.status prj_status, projects.name prj_name, last_update_on, pj_basics.maj_fran as maj_fran "+
		""; //another select clause goes here but with mod names
	public static String VAR_REP_2 =
		"from "+
		"(select distinct internal_id, type, alt_work_id, start_date, end_date, variancecode, user_comment, complete, to_include, "+
//G_WATSON_ROLLBACK
//		"        status_calculated, actual_end, name itemName, rownum rn "+
		"        status status_calculated, actual_end, name itemName, rownum rn "+
		"  from alt_work_items "+
		"  where "+
		"  type in (";//1,3,32,5,52
	public static String VAR_REP_3 = //CHANGE_HIER_TODO
		"start with internal_id=0 "+
		"  and  Exists (select 0 from port_results "+
		"                        where port_results.port_rs_id=? "+
		"                        and is_default_alt = 1 "+
		"                        and ver_alt_work_id = alt_work_items.alt_work_id "+
		"              ) "+
    " connect by prior internal_id = parent_internal_id and prior alt_work_id = alt_work_id "+
		") currList, port_results, pj_basics, projects, alt_works "+
		"where "+
		"port_results.port_rs_id = ? "+
		"and is_default_alt = 1 "+
		"and currList.alt_work_id(+) = ver_alt_work_id "+
		"and projects.id = port_results.prj_id "+
		"and pj_basics.id = port_results.ver_prj_basic_id "+
		"and alt_works.id = ver_alt_work_id "+
          "and exists (select 1 from alt_work_items where alt_work_items.alt_work_id = ver_alt_work_id and alt_work_items.type = 30 and (alt_work_items.complete is null or alt_work_items.complete < 0.99)) "+
		") "+
		"currInfo, "+
//G_WATSON_031507_ROLLBACK
		"(select alt_id, internal_id, start_date, end_date, type, alt_work_id, status_calculated "+
//		"(select alt_id, internal_id, start_date, end_date, type, alt_work_id, status status_calculated "+
		"from port_results, alt_work_items "+
		"where port_results.port_rs_id = ? "+
		"and is_default_alt=1 "+
		"and alt_work_items.alt_work_id = port_results.ver_alt_work_id "+
		"and type in (";
	public static String VAR_REP_4_1 = Misc.G_DO_ORACLE ? ")) baseInfo "+
" , "+
" (select alt_id, min(end_date) file_date "+
" from port_results, alt_work_items where port_results.port_rs_id = ? and is_default_alt=1 and "+
" alt_work_items.alt_work_id = port_results.ver_alt_work_id and type = ? group by alt_id "+
" ) orderlist "+
" where    currInfo.alt_id = baseInfo.alt_id(+) "+
" and currInfo.internal_id = baseInfo.internal_id(+) "+
" and currInfo.alt_id = orderlist.alt_id(+) " : ")) baseInfo "+
" , "+
" (select alt_id, min(end_date) file_date "+
" from port_results, alt_work_items where port_results.port_rs_id = ? and is_default_alt=1 and "+
" alt_work_items.alt_work_id = port_results.ver_alt_work_id and type = ? group by alt_id "+
" ) orderlist "+
" where    currInfo.alt_id *= baseInfo.alt_id "+
" and currInfo.internal_id *= baseInfo.internal_id "+
" and currInfo.alt_id *= orderlist.alt_id ";
	public static String VAR_REP_4_2 = Misc.G_DO_ORACLE ? " order by orderlist.file_date nulls last, currinfo.alt_id desc, rn " :
		" order by orderlist.file_date asc, currinfo.alt_id desc, rn ";

	public static String VAR_REP_EVENT_PART_1 =
//G_WATSON_031507_ROLLBACK
		"select alt_work_items.start_date, alt_work_items.end_date, alt_work_items.type, alt_work_items.name, alt_map_items.wspace_id, alt_work_items.complete, alt_work_items.actual_end, alt_work_items.status_calculated, alt_work_items.actual_start, alt_work_items.internal_id, alt_work_items.name "+
//		"select alt_work_items.start_date, alt_work_items.end_date, alt_work_items.type, alt_work_items.name, alt_map_items.wspace_id, alt_work_items.complete, alt_work_items.actual_end, alt_work_items.status, alt_work_items.actual_start, alt_work_items.internal_id, alt_work_items.name "+
		" from alt_work_items, alt_map_items "+
		" where "+
		" alt_map_items.alt_id = ? "+
		" and alt_map_items.isdefault= 1 "+
		" and alt_map_items.map_type = 6 "+
		" and alt_work_items.alt_work_id = alt_map_items.alt_work_id "+
		" and alt_work_items.type in (" ;//1,2,3
	public static String VAR_REP_EVENT_PART_2 =
		") "+
		" order by alt_work_items.type asc, alt_map_items.wspace_id desc, alt_work_items.end_date asc ";

	public static String VAR_REP_HIST_PART_1 =
		"select currInfo.name, currInfo.type, currInfo.internal_id, currInfo.end_date, currInfo.status_calculated, baseInfo.end_date, baseInfo.status_calculated, hist.variancecode, hist.user_comment "+
		"from "+
//G_WATSON_031507_ROLLBACK
		"(select end_date, status_calculated, complete, internal_id, type "+
//		"(select end_date, status  status_calculated, complete, internal_id, type "+
		"from alt_work_items "+
		"where alt_work_items.alt_work_id = ? "+
		"   and type in (";//1,2,3
	public static String VAR_REP_HIST_PART_2 =
		")) baseInfo, "+
//G_WATSON_031507_ROLLBACK
		"(select end_date, status_calculated, complete, internal_id, type, name "+
//		"(select end_date, status status_calculated, complete, internal_id, type, name "+
		"from alt_work_items "+
		"where alt_work_items.alt_work_id = ? "+
		"   and type in (";//1,2,3
	public static String VAR_REP_HIST_PART_3 =
		")) currInfo, "+
		"(select type, internal_id, variancecode, user_comment, alt_work_items.alt_work_id "+
		"from "+
		"alt_map_items, wksp_hist, alt_work_items "+
		"where "+
		"alt_map_items.alt_id = ? "+
		"and alt_map_items.map_type = 1 "+
		"and alt_map_items.isdefault=1 "+
		"and wksp_hist.wspace_id = alt_map_items.wspace_id "+
		"and wksp_hist.action_type in (1,2) "+
		"and wksp_hist.ref_item_type = 8 "+
		"and wksp_hist.ref_item_id = alt_work_items.alt_work_id "+
		//"and alt_work_items.alt_work_id <> ? "+
		"and alt_work_items.alt_work_id > ? "+
		"and alt_work_items.alt_work_id <= ? "+
		"and alt_work_items.type in ("; //1,2,3
	public static String VAR_REP_HIST_PART_4 = Misc.G_DO_ORACLE ? ")) hist "+
		"where currInfo.internal_id = baseInfo.internal_id(+) "+
		"   and currInfo.internal_id = hist.internal_id(+) "+
		"order by currInfo.type, currInfo.internal_id, hist.alt_work_id desc" : 
		")) hist "+
		"where currInfo.internal_id *= baseInfo.internal_id "+
		"   and currInfo.internal_id *= hist.internal_id "+
		"order by currInfo.type, currInfo.internal_id, hist.alt_work_id desc";

 //this is the new VAR_REP_OVERALL_HIST ... uses variance related notes in user_comment field at internal_id=0
	public static String VAR_REP_OVERALL_HIST =
		" select alt_work_items.user_comment from alt_work_items where alt_work_items.alt_work_id = ? and internal_id = 0 ";

 //this is the old VAR_REP_OVERALL_HIST .... combined comments all the way through
//	public static String VAR_REP_OVERALL_HIST =
//		" select wksp_hist.comments "+
//		" from "+
//		" alt_map_items, wksp_hist "+
//		" where "+
//		" alt_map_items.alt_id = ? "+
//		" and alt_map_items.map_type = 1 "+
//		" and alt_map_items.isdefault=1 "+
//		" and wksp_hist.wspace_id = alt_map_items.wspace_id "+
//		" and wksp_hist.action_type in (1,2) "+
//		" and wksp_hist.ref_item_type = 8 "+
//		" order by on_date desc ";
	public static String CYCLE_REP_PART_1 =
//G_WATSON_031507_ROLLBACK
		"select alt_work_items.start_date, alt_work_items.end_date, alt_work_items.type, alt_work_items.status_calculated, projects.id, projects.name, alt_work_items.name, pj_basics.maj_fran "+
//		"select alt_work_items.start_date, alt_work_items.end_date, alt_work_items.type, alt_work_items.status, projects.id, projects.name, alt_work_items.name, pj_basics.maj_fran "+
		"from alt_work_items, alt_map_items, projects, alternatives, pj_map_items, pj_basics "+
		"where "+
		"projects.status in (2,3,4,5) "+
		"and alternatives.prj_id = projects.id "+
		"and alternatives.is_primary = 1 "+
		"and alt_map_items.alt_id = alternatives.id "+
		"and alt_map_items.map_type = 1 "+
		"and alt_map_items.isdefault = 1 "+
		"and pj_map_items.prj_id = projects.id "+
		"and pj_map_items.isdefault = 1 "+
		"and pj_map_items.map_type = 1 "+
		"and pj_basics.id = pj_map_items.pj_basic_id "+
		"and alt_work_items.alt_work_id = alt_map_items.alt_work_id "+
		//"and Round(Complete,2) = 1.00 "+
//		"and end_date >= ? and end_date <= ? "+
		"and alt_work_items.type in (";//1,2,3
	public static String CYCLE_REP_PART_2 =
	   //	") "+
		" order by alt_work_items.end_date";


	/*
	 * Queries for e-batch metrics report Sameer - 02072005
	 */

	public static String EBATCH_REP_SEL_CLAUSE_INTERNAL_1 = Misc.G_DO_ORACLE ? "select projects.name, pj_basics.thera_ind, " +
		"baseInfo.itemcount, currInfo.itemcount, " +
		"baseInfo.end_date, currInfo.end_date, currInfo.actual_end, " +
		"currInfo.alt_id, currInfo.alt_work_id, baseInfo.alt_work_id, currInfo.status_calculated " +
		"from projects, port_results, pj_basics, pj_map_items, " +
		"( " +
		"select pj_basics.num_field2 as itemCount, port_results.alt_id, " +
		"end_date , actual_end , port_results.prj_id, " +
//G_WATSON_031507_ROLLBACK
		"alt_work_items.alt_work_id, alt_work_items.status_calculated " +
//		"alt_work_items.alt_work_id, alt_work_items.status status_calculated " +
		"from port_results, alt_map_items, pj_map_items, alt_work_items, pj_basics " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
          //new
          "and port_results.prj_id = pj_map_items.prj_id and pj_map_items.map_type = alt_map_items.map_type and pj_map_items.isdefault=1 "+
          "and pj_basics.id = pj_map_items.pj_basic_id "+
          //end new
		"and alt_work_items.type = ? " +
		//"group by port_results.alt_id, prj_id, alt_work_items.alt_work_id " +
		") currInfo, " +
		"( " +
		"select pj_basics.num_field2 as itemcount, port_results.alt_id, " +
		"end_date as end_date, alt_work_items.alt_work_id " +
		"from port_results, pj_map_items, alt_map_items, alt_work_items,  pj_basics " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
          //new
          "and port_results.prj_id = pj_map_items.prj_id and pj_map_items.map_type = alt_map_items.map_type and pj_map_items.isdefault=1 "+
          "and pj_basics.id = pj_map_items.pj_basic_id "+
          //end new
		"and alt_work_items.type = ? " +
//		"group by port_results.alt_id, alt_work_items.alt_work_id " +
		") baseInfo " +
		"where port_results.port_rs_id = ? " +
		"and port_results.is_default_alt = 1 " +
		"and port_results.prj_id = projects.id " +
		"and projects.id = currInfo.prj_id " +
		"and pj_map_items.prj_id = currInfo.prj_id " +
		"and pj_map_items.map_type = ? " +
		"and pj_map_items.isdefault = 1 " +
		"and pj_map_items.pj_basic_id = pj_basics.id " +
		"and currInfo.alt_id = baseInfo.alt_id (+) "+
          "and ((TO_NUMBER(TO_CHAR(baseInfo.end_date, 'YYYY')) = ?) " +
          "     or (TO_NUMBER(TO_CHAR(currInfo.end_date, 'YYYY')) = ?) " +
          "    ) " :
		"select projects.name, pj_basics.thera_ind, " +
		"baseInfo.itemcount, currInfo.itemcount, " +
		"baseInfo.end_date, currInfo.end_date, currInfo.actual_end, " +
		"currInfo.alt_id, currInfo.alt_work_id, baseInfo.alt_work_id, currInfo.status_calculated " +
		"from projects, port_results, pj_basics, pj_map_items, " +
		"( " +
		"select pj_basics.num_field2 as itemCount, port_results.alt_id, " +
		"end_date , actual_end , port_results.prj_id, " +
//G_WATSON_031507_ROLLBACK
		"alt_work_items.alt_work_id, alt_work_items.status_calculated " +
//		"alt_work_items.alt_work_id, alt_work_items.status status_calculated " +
		"from port_results, alt_map_items, pj_map_items, alt_work_items, pj_basics " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
          //new
          "and port_results.prj_id = pj_map_items.prj_id and pj_map_items.map_type = alt_map_items.map_type and pj_map_items.isdefault=1 "+
          "and pj_basics.id = pj_map_items.pj_basic_id "+
          //end new
		"and alt_work_items.type = ? " +
		//"group by port_results.alt_id, prj_id, alt_work_items.alt_work_id " +
		") currInfo, " +
		"( " +
		"select pj_basics.num_field2 as itemcount, port_results.alt_id, " +
		"end_date as end_date, alt_work_items.alt_work_id " +
		"from port_results, pj_map_items, alt_map_items, alt_work_items,  pj_basics " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
          //new
          "and port_results.prj_id = pj_map_items.prj_id and pj_map_items.map_type = alt_map_items.map_type and pj_map_items.isdefault=1 "+
          "and pj_basics.id = pj_map_items.pj_basic_id "+
          //end new
		"and alt_work_items.type = ? " +
//		"group by port_results.alt_id, alt_work_items.alt_work_id " +
		") baseInfo " +
		"where port_results.port_rs_id = ? " +
		"and port_results.is_default_alt = 1 " +
		"and port_results.prj_id = projects.id " +
		"and projects.id = currInfo.prj_id " +
		"and pj_map_items.prj_id = currInfo.prj_id " +
		"and pj_map_items.map_type = ? " +
		"and pj_map_items.isdefault = 1 " +
		"and pj_map_items.pj_basic_id = pj_basics.id " +
		"and currInfo.alt_id *= baseInfo.alt_id "+
          "and ((TO_NUMBER(TO_CHAR(baseInfo.end_date, 'YYYY')) = ?) " +
          "     or (TO_NUMBER(TO_CHAR(currInfo.end_date, 'YYYY')) = ?) " +
          "    ) ";
//		"and pj_basics.maj_fran <> ? " +
	public static String EBATCH_REP_SEL_CLAUSE_INTERNAL_2 =          " order by projects.name ";

	public static String EBATCH_REP_SEL_CLAUSE_EXTERNAL = Misc.G_DO_ORACLE ? "select projects.name, pj_basics.thera_ind, " +
		"baseInfo.itemcount, currInfo.itemcount, " +
		"baseInfo.end_date, currInfo.end_date, currInfo.actual_end, " +
		"currInfo.alt_id, currInfo.alt_work_id, baseInfo.alt_work_id, currInfo.status_calculated " +
		"from projects, port_results, pj_basics, pj_map_items, " +
		"( " +
		"select pj_basics.num_field2 as itemcount, port_results.alt_id, " +
		"(end_date) as end_date, (actual_end) as actual_end, port_results.prj_id, " +
//G_WATSON_ROLLBACK_031507
//		"alt_work_items.alt_work_id, alt_work_items.status_calculated " +
		"alt_work_items.alt_work_id, alt_work_items.status status_calculated " +
		"from port_results, alt_map_items, pj_map_items, alt_work_items, pj_basics " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and alt_work_items.type = ? " +
          //new
          "and port_results.prj_id = pj_map_items.prj_id and pj_map_items.map_type = alt_map_items.map_type and pj_map_items.isdefault=1 "+
          "and pj_basics.id = pj_map_items.pj_basic_id "+
          //end new

//		"group by port_results.alt_id, prj_id, alt_work_items.alt_work_id " +
		") currInfo, " +
		"( " +
		"select pj_basics.num_field2 as itemcount, port_results.alt_id, " +
		"end_date, alt_work_items.alt_work_id " +
		"from port_results, alt_map_items, pj_map_items, alt_work_items, pj_basics " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and alt_work_items.type = ? " +
          //new
          "and port_results.prj_id = pj_map_items.prj_id and pj_map_items.map_type = alt_map_items.map_type and pj_map_items.isdefault=1 "+
          "and pj_basics.id = pj_map_items.pj_basic_id "+
          //end new

//		"group by port_results.alt_id, alt_work_items.alt_work_id " +
		") baseInfo " +
		"where port_results.port_rs_id = ? " +
		"and port_results.is_default_alt = 1 " +
		"and port_results.prj_id = projects.id " +
		"and projects.id = currInfo.prj_id " +
		"and pj_map_items.prj_id = currInfo.prj_id " +
		"and pj_map_items.map_type = ? " +
		"and pj_map_items.isdefault = 1 " +
		"and pj_map_items.pj_basic_id = pj_basics.id " +
		"and pj_basics.maj_fran = ? " +
		"and currInfo.alt_id = baseInfo.alt_id (+) "+
	     "and ((TO_NUMBER(TO_CHAR(baseInfo.end_date, 'YYYY')) = ?) " +
          "     or (TO_NUMBER(TO_CHAR(currInfo.end_date, 'YYYY')) = ?) " +
          "    ) "+
          "order by projects.name " :
		"select projects.name, pj_basics.thera_ind, " +
		"baseInfo.itemcount, currInfo.itemcount, " +
		"baseInfo.end_date, currInfo.end_date, currInfo.actual_end, " +
		"currInfo.alt_id, currInfo.alt_work_id, baseInfo.alt_work_id, currInfo.status_calculated " +
		"from projects, port_results, pj_basics, pj_map_items, " +
		"( " +
		"select pj_basics.num_field2 as itemcount, port_results.alt_id, " +
		"(end_date) as end_date, (actual_end) as actual_end, port_results.prj_id, " +
//G_WATSON_ROLLBACK_031507
//		"alt_work_items.alt_work_id, alt_work_items.status_calculated " +
		"alt_work_items.alt_work_id, alt_work_items.status status_calculated " +
		"from port_results, alt_map_items, pj_map_items, alt_work_items, pj_basics " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and alt_work_items.type = ? " +
          //new
          "and port_results.prj_id = pj_map_items.prj_id and pj_map_items.map_type = alt_map_items.map_type and pj_map_items.isdefault=1 "+
          "and pj_basics.id = pj_map_items.pj_basic_id "+
          //end new

//		"group by port_results.alt_id, prj_id, alt_work_items.alt_work_id " +
		") currInfo, " +
		"( " +
		"select pj_basics.num_field2 as itemcount, port_results.alt_id, " +
		"end_date, alt_work_items.alt_work_id " +
		"from port_results, alt_map_items, pj_map_items, alt_work_items, pj_basics " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and alt_work_items.type = ? " +
          //new
          "and port_results.prj_id = pj_map_items.prj_id and pj_map_items.map_type = alt_map_items.map_type and pj_map_items.isdefault=1 "+
          "and pj_basics.id = pj_map_items.pj_basic_id "+
          //end new

//		"group by port_results.alt_id, alt_work_items.alt_work_id " +
		") baseInfo " +
		"where port_results.port_rs_id = ? " +
		"and port_results.is_default_alt = 1 " +
		"and port_results.prj_id = projects.id " +
		"and projects.id = currInfo.prj_id " +
		"and pj_map_items.prj_id = currInfo.prj_id " +
		"and pj_map_items.map_type = ? " +
		"and pj_map_items.isdefault = 1 " +
		"and pj_map_items.pj_basic_id = pj_basics.id " +
		"and pj_basics.maj_fran = ? " +
		"and currInfo.alt_id *= baseInfo.alt_id  "+
	     "and ((TO_NUMBER(TO_CHAR(baseInfo.end_date, 'YYYY')) = ?) " +
          "     or (TO_NUMBER(TO_CHAR(currInfo.end_date, 'YYYY')) = ?) " +
          "    ) "+
          "order by projects.name ";

	/*
	 * End of queries for e-batch metrics report Sameer - 02072005
	 */

	/*
	 * Query for filing and launch metrics report -- Sameer 02092005
	 */

	public static String FILING_REP_SEL_CLAUSE_1 = Misc.G_DO_ORACLE ? "select projects.name, baseInfo.end_date, currInfo.end_date, " +
		"currInfo.actual_end, alt_basics.num_field1 from " +
		"projects, port_results, pj_basics, pj_map_items, alt_basics, " +
		"( " +
		"select port_results.alt_id, max(end_date) as end_date, " +
		"max(actual_end) as actual_end, prj_id, alt_basic_id " +
		"from port_results, alt_map_items, alt_work_items " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and alt_work_items.type = ? " +
		"group by port_results.alt_id, prj_id, alt_basic_id " +
		") currInfo, " +
		"( " +
		"select port_results.alt_id, " +
		"max(end_date) as end_date " +
		"from port_results, alt_map_items, alt_work_items " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and alt_work_items.type = ? " +
		"group by port_results.alt_id " +
		") baseInfo " +
		"where port_results.port_rs_id = ? " +
		"and port_results.is_default_alt = 1 " +
		"and port_results.prj_id = projects.id " +
		"and alt_basics.id = currInfo.alt_basic_id " +
		"and projects.id = currInfo.prj_id " +
		"and pj_map_items.prj_id = currInfo.prj_id " +
		"and pj_map_items.map_type = ? " +
		"and pj_map_items.isdefault = 1 " +
		"and pj_map_items.pj_basic_id = pj_basics.id " +
		"and currInfo.alt_id = baseInfo.alt_id (+) "+
          "and ((TO_NUMBER(TO_CHAR(baseInfo.end_date, 'YYYY')) = ?) " +
          "     or (TO_NUMBER(TO_CHAR(currInfo.end_date, 'YYYY')) = ?) " +
          "    ) " :
		"select projects.name, baseInfo.end_date, currInfo.end_date, " +
		"currInfo.actual_end, alt_basics.num_field1 from " +
		"projects, port_results, pj_basics, pj_map_items, alt_basics, " +
		"( " +
		"select port_results.alt_id, max(end_date) as end_date, " +
		"max(actual_end) as actual_end, prj_id, alt_basic_id " +
		"from port_results, alt_map_items, alt_work_items " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and alt_work_items.type = ? " +
		"group by port_results.alt_id, prj_id, alt_basic_id " +
		") currInfo, " +
		"( " +
		"select port_results.alt_id, " +
		"max(end_date) as end_date " +
		"from port_results, alt_map_items, alt_work_items " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and alt_work_items.type = ? " +
		"group by port_results.alt_id " +
		") baseInfo " +
		"where port_results.port_rs_id = ? " +
		"and port_results.is_default_alt = 1 " +
		"and port_results.prj_id = projects.id " +
		"and alt_basics.id = currInfo.alt_basic_id " +
		"and projects.id = currInfo.prj_id " +
		"and pj_map_items.prj_id = currInfo.prj_id " +
		"and pj_map_items.map_type = ? " +
		"and pj_map_items.isdefault = 1 " +
		"and pj_map_items.pj_basic_id = pj_basics.id " +
		"and currInfo.alt_id *= baseInfo.alt_id  "+
          "and ((TO_NUMBER(TO_CHAR(baseInfo.end_date, 'YYYY')) = ?) " +
          "     or (TO_NUMBER(TO_CHAR(currInfo.end_date, 'YYYY')) = ?) " +
          "    ) ";
	public static String FILING_REP_SEL_CLAUSE_2 = " order by currInfo.end_date ";


	/*
	 * End of query for filing metrics report -- Sameer 02092005
	 */

	/*
	 * Query for calendar report -- Sameer 02102005
	 */

	public static String CALENDAR_REP_SEL_CLAUSE_INTERNAL_1 =
		"select currInfo.month, " +
		"projects.name, pj_basics.maj_fran, pj_basics.thera_ind, " +
		"currInfo.itemcount, currInfo.alt_id, " +
		"currInfo.worktype " +
		"from projects, port_results, pj_basics, pj_map_items, " +
		"( " +
		"select count(*) as itemcount, port_results.alt_id, " +
		"TO_NUMBER(TO_CHAR(end_date, 'MM')) as month, " +
		"prj_id, alt_work_items.type as worktype " +
		"from port_results, alt_map_items, alt_work_items " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and TO_NUMBER(TO_CHAR(end_date, 'YYYY')) = ? " +
		"and alt_work_items.type in (?, ?, ?) " +
		"group by port_results.alt_id, prj_id, " +
		"alt_work_items.type, TO_NUMBER(TO_CHAR(end_date, 'MM')) " +
		") currInfo " +
		"where port_results.port_rs_id = ? " +
		"and port_results.is_default_alt = 1 " +
		"and port_results.prj_id = projects.id " +
		"and projects.id = currInfo.prj_id " +
		"and pj_map_items.prj_id = currInfo.prj_id " +
		"and pj_map_items.map_type = ? " +
		"and pj_map_items.isdefault = 1 " +
		"and pj_map_items.pj_basic_id = pj_basics.id ";
//		"and pj_basics.maj_fran <> ? " +
	public static String CALENDAR_REP_SEL_CLAUSE_INTERNAL_2 =		" order by currInfo.month asc, currInfo.worktype asc ";

	public static String CALENDAR_REP_SEL_CLAUSE_EXTERNAL =
		"select currInfo.month, " +
		"projects.name, pj_basics.maj_fran, pj_basics.thera_ind, " +
		"currInfo.itemcount, currInfo.alt_id, " +
		"currInfo.worktype " +
		"from projects, port_results, pj_basics, pj_map_items, " +
		"( " +
		"select count(*) as itemcount, port_results.alt_id, " +
		"TO_NUMBER(TO_CHAR(end_date, 'MM')) as month, " +
		"prj_id, alt_work_items.type as worktype " +
		"from port_results, alt_map_items, alt_work_items " +
		"where port_results.port_rs_id = ? " +
		"and is_default_alt = 1 " +
		"and port_results.alt_id = alt_map_items.alt_id " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.map_type = ? " +
		"and alt_map_items.alt_work_id = alt_work_items.alt_work_id " +
		"and TO_NUMBER(TO_CHAR(end_date, 'YYYY')) = ? " +
		"and alt_work_items.type in (?, ?, ?) " +
		"group by port_results.alt_id, prj_id, " +
		"alt_work_items.type, TO_NUMBER(TO_CHAR(end_date, 'MM')) " +
		") currInfo " +
		"where port_results.port_rs_id = ? " +
		"and port_results.is_default_alt = 1 " +
		"and port_results.prj_id = projects.id " +
		"and projects.id = currInfo.prj_id " +
		"and pj_map_items.prj_id = currInfo.prj_id " +
		"and pj_map_items.map_type = ? " +
		"and pj_map_items.isdefault = 1 " +
		"and pj_map_items.pj_basic_id = pj_basics.id " +
		"and pj_basics.maj_fran = ? " +
		"order by currInfo.month asc, currInfo.worktype asc ";

	//
	// End of query for calendar report -- Sameer 01102005
	//

	// rajeev 031005 - mod queries for calculatung times etc.
	//    port_results.alt_id, fte_head_id, for_achieving_milestone, data.year, sum(data.value), data.val_scope
	//--weekly
	public static String GET_FTE_BY_WEEK_1 = Misc.G_DO_ORACLE ? "select /*+ ordered */ "+
		"port_results.alt_id "+
		",fte_items.fte_head_id "+
		",fte_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)) "+
		",3 "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.target_market, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"port_results "+
		",fte_items "+
		",data "+
		",week_timeid "+
		"where "+
		"port_results.port_rs_id = ? " : "select /*+ ordered */ "+
		"port_results.alt_id "+
		",fte_items.fte_head_id "+
		",fte_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)) "+
		",3 "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.target_market, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"port_results "+
		",fte_items "+
		",data "+
		",week_timeid "+
		"where "+
		"port_results.port_rs_id = ? ";
	public static String GET_FTE_BY_WEEK_2 =
		"and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		"and data.fte_item_id = fte_items.id "+
		"and fte_items.to_include = 1 "+
		"and week_timeid.time_id >= ? "+
		"and week_timeid.time_id <= ? "+
		"and week_timeid.time_id >= data.year-12 "+
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"week_timeid.time_id "+
		",port_results.alt_id "+
		",fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status "+
		",fte_items.for_achieving_milestone "+
		",fte_items.target_market "+
		",fte_items.scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		"order by port_results.alt_id, fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status, "+
		"        fte_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		", week_timeid.time_id ";
	;

	//-- monthly
	public static String GET_FTE_BY_MONTH_1 = Misc.G_DO_ORACLE ? "select /*+ ordered */ "+
		"port_results.alt_id "+
		",fte_items.fte_head_id "+
		",fte_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)) "+
		",2 "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5,  fte_items.target_market, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"port_results "+
		",fte_items "+
		",data "+
		",month_timeid "+
		"where "+
		"port_results.port_rs_id = ? " : "select /*+ ordered */ "+
		"port_results.alt_id "+
		",fte_items.fte_head_id "+
		",fte_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)) "+
		",2 "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5,  fte_items.target_market, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"port_results "+
		",fte_items "+
		",data "+
		",month_timeid "+
		"where "+
		"port_results.port_rs_id = ? ";
	public static String GET_FTE_BY_MONTH_2 = Misc.G_DO_ORACLE ? "and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		"and data.fte_item_id = fte_items.id "+
		"and fte_items.to_include = 1 "+
		"and month_timeid.time_id >= ? "+
		"and month_timeid.time_id <= ? "+
		"and month_timeid.time_id >= trunc(data.year/35)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"month_timeid.time_id "+
		",port_results.alt_id "+
		",fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status "+
		",fte_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		"order by port_results.alt_id, fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status, "+
		"         fte_items.for_achieving_milestone "+
		",target_market  "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		", month_timeid.time_id "
	 :
		"and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		"and data.fte_item_id = fte_items.id "+
		"and fte_items.to_include = 1 "+
		"and month_timeid.time_id >= ? "+
		"and month_timeid.time_id <= ? "+
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"month_timeid.time_id "+
		",port_results.alt_id "+
		",fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status "+
		",fte_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		"order by port_results.alt_id, fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status, "+
		"         fte_items.for_achieving_milestone "+
		",target_market  "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		", month_timeid.time_id ";
	;

	//-- quarterly
	public static String GET_FTE_BY_QTR_1 = Misc.G_DO_ORACLE ? "select /*+ ordered */ "+
		"port_results.alt_id "+
		",fte_items.fte_head_id "+
		",fte_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)) "+
		",0 "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.target_market, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"port_results "+
		",fte_items "+
		",data "+
		",qtr_timeid "+
		"where "+
		"port_results.port_rs_id = ? " : "select /*+ ordered */ "+
		"port_results.alt_id "+
		",fte_items.fte_head_id "+
		",fte_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)) "+
		",0 "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.target_market, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"port_results "+
		",fte_items "+
		",data "+
		",qtr_timeid "+
		"where "+
		"port_results.port_rs_id = ? ";
	public static String GET_FTE_BY_QTR_2 = Misc.G_DO_ORACLE ?
  "and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		"and data.fte_item_id = fte_items.id "+
		"and fte_items.to_include = 1 "+
		"and qtr_timeid.time_id >= ? "+
		"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"qtr_timeid.time_id "+
		",port_results.alt_id "+
		",fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status "+
		",fte_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		"order by port_results.alt_id, fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status, "+
		"         fte_items.for_achieving_milestone "+
		", target_market "+
		", scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		", qtr_timeid.time_id "
		 :
		"and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		"and data.fte_item_id = fte_items.id "+
		"and fte_items.to_include = 1 "+
		"and qtr_timeid.time_id >= ? "+
		"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"qtr_timeid.time_id "+
		",port_results.alt_id "+
		",fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status "+
		",fte_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		"order by port_results.alt_id, fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status, "+
		"         fte_items.for_achieving_milestone "+
		", target_market "+
		", scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		", qtr_timeid.time_id "
		;

	//--year
	public static String GET_FTE_BY_YEAR_1 = Misc.G_DO_ORACLE ? "select /*+ ordered */ "+
		"port_results.alt_id "+
		",fte_items.fte_head_id "+
		",fte_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)) "+
		",1 "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.target_market, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"port_results "+
		",fte_items "+
		",data "+
		",year_timeid "+
		"where "+
		"port_results.port_rs_id = ? " : "select /*+ ordered */ "+
		"port_results.alt_id "+
		",fte_items.fte_head_id "+
		",fte_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)) "+
		",1 "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.target_market, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"port_results "+
		",fte_items "+
		",data "+
		",year_timeid "+
		"where "+
		"port_results.port_rs_id = ? ";
	public static String GET_FTE_BY_YEAR_2 = Misc.G_DO_ORACLE ? "and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		"and fte_items.to_include = 1 "+
		"and data.fte_item_id = fte_items.id "+
		"and year_timeid.time_id >= ? "+
		"and year_timeid.time_id <= ? "+
		"and year_timeid.time_id >= trunc(data.year/420)*420 "+
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"year_timeid.time_id "+
		",port_results.alt_id "+
		",fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status "+
		",fte_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		"order by port_results.alt_id, fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status, "+
		"         fte_items.for_achieving_milestone "+
		"         ,target_market "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		",year_timeid.time_id "
    :		
		"and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		"and fte_items.to_include = 1 "+
		"and data.fte_item_id = fte_items.id "+
		"and year_timeid.time_id >= ? "+
		"and year_timeid.time_id <= ? "+
		"and year_timeid.time_id >= cast((data.year/420) as int)*420 "+
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"year_timeid.time_id "+
		",port_results.alt_id "+
		",fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status "+
		",fte_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		"order by port_results.alt_id, fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status, "+
		"         fte_items.for_achieving_milestone "+
		"         ,target_market "+
		",scen_id "+
		",fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		",year_timeid.time_id "

		;

	//-- fte calc for alt_works

	//-- monthly
	public static String GET_RES_NEED_RAW = "select "+
		"fte_items.task_internal_id, 1 , fte_items.fte_head_id, "+
		"fte_items.for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, "+
		"fte_items.classify4, fte_items.classify5, data.year, data.value, '', data.val_scope, data.val_dur, fte_items.target_market, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"alt_works, fte_items, data "+
		"where alt_works.id = ? and fte_items.alt_fte_id = alt_works.ref_alt_fte_id "+
		"and fte_items.task_internal_id is not null "+
		"and data.fte_item_id = fte_items.id "+
		//  "group by task_internal_id, "+
		//  "fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status,fte_items.for_achieving_milestone, fte_items.target_market, fte_items.classify1, fte_items.classify2, "+
		//  "fte_items.classify3, fte_items.classify4, fte_items.classify5 "+
		"order by fte_items.task_internal_id,  "+
		"fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status,fte_items.for_achieving_milestone, fte_items.target_market, fte_items.classify1, fte_items.classify2, "+
		"fte_items.classify3, fte_items.classify4, fte_items.classify5 "
		;
	public static String GET_COST_NEED_RAW = "select "+
		"cost_items.task_internal_id, 1, cost_cent_id, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, data.year, data.value, name, val_scope, val_dur,  cost_items.target_market "+
		"from alt_works, cost_items, data "+
		"where alt_works.id = ? and cost_items.alt_devcost_id = alt_works.ref_alt_devcost_id "+
		"and data.cost_li_id = cost_items.id "+
		"and task_internal_id is not null "+
		//   "group by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, target_market, "+
		//   "classify1, classify2, classify3, classify4, classify5, name "+
		"order by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, target_market, "+
		"classify1, classify2, classify3, classify4, classify5, name "
		;

	public static String GET_RES_NEED = Misc.G_DO_ORACLE ? "select "+
		"fte_items.task_internal_id,  1, fte_items.fte_head_id, "+
		"fte_items.for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, "+
		//was  "fte_items.classify4, fte_items.classify5, month_timeid.time_id, sum(data.value) "+
		"fte_items.classify4, fte_items.classify5, month_timeid.time_id, sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), '', fte_items.target_market, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"alt_works, fte_items, data, month_timeid "+
		"where alt_works.id = ? and fte_items.alt_fte_id = alt_works.ref_alt_fte_id "+
		"and fte_items.task_internal_id is not null "+
		"and data.fte_item_id = fte_items.id "+
		"and month_timeid.time_id >= trunc(data.year/35)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by fte_items.task_internal_id,   "+
		"fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status,fte_items.for_achieving_milestone, fte_items.target_market, fte_items.classify1, fte_items.classify2, "+
		"fte_items.classify3, fte_items.classify4, fte_items.classify5, month_timeid.time_id "+
		"order by fte_items.task_internal_id,   "+
		"fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status,fte_items.for_achieving_milestone, fte_items.target_market, fte_items.classify1, fte_items.classify2, "+
		"fte_items.classify3, fte_items.classify4, fte_items.classify5, month_timeid.time_id "
		 : "select "+
		"fte_items.task_internal_id,  1, fte_items.fte_head_id, "+
		"fte_items.for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, "+
		//was  "fte_items.classify4, fte_items.classify5, month_timeid.time_id, sum(data.value) "+
		"fte_items.classify4, fte_items.classify5, month_timeid.time_id, sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), '', fte_items.target_market, fte_items.for_skill, fte_items.assignment_status "+
		"from "+
		"alt_works, fte_items, data, month_timeid "+
		"where alt_works.id = ? and fte_items.alt_fte_id = alt_works.ref_alt_fte_id "+
		"and fte_items.task_internal_id is not null "+
		"and data.fte_item_id = fte_items.id "+
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by fte_items.task_internal_id,   "+
		"fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status,fte_items.for_achieving_milestone, fte_items.target_market, fte_items.classify1, fte_items.classify2, "+
		"fte_items.classify3, fte_items.classify4, fte_items.classify5, month_timeid.time_id "+
		"order by fte_items.task_internal_id,   "+
		"fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status,fte_items.for_achieving_milestone, fte_items.target_market, fte_items.classify1, fte_items.classify2, "+
		"fte_items.classify3, fte_items.classify4, fte_items.classify5, month_timeid.time_id "
		;
	//--cost

	public static String GET_COST_NEED = Misc.G_DO_ORACLE ? "select "+
		//was   "cost_items.task_internal_id, lineitem_id, cost_cent_id, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, month_timeid.time_id, sum(data.value* intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), name "+
		"cost_items.task_internal_id, 1, cost_cent_id, for_achieving_milestone, classify1, " +
		"classify2, classify3, classify4, classify5, month_timeid.time_id, " +
		// sameer 06272006 -- intelli.getPropIncluded added
		"sum(data.value* intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		//"sum(data.value), " +
		"name, cost_items.target_market "+
		"from alt_works, cost_items, data, month_timeid "+
		"where alt_works.id = ? and cost_items.alt_devcost_id = alt_works.ref_alt_devcost_id "+
		"and data.cost_li_id = cost_items.id "+
		"and task_internal_id is not null "+
		"and month_timeid.time_id >= trunc(data.year/35)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, target_market, "+
		"classify1, classify2, classify3, classify4, classify5, month_timeid.time_id, name "+
		"order by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, target_market, "+
		"classify1, classify2, classify3, classify4, classify5, month_timeid.time_id, name "
		 :
     "select "+
		//was   "cost_items.task_internal_id, lineitem_id, cost_cent_id, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, month_timeid.time_id, sum(data.value* getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), name "+
		"cost_items.task_internal_id, 1, cost_cent_id, for_achieving_milestone, classify1, " +
		"classify2, classify3, classify4, classify5, month_timeid.time_id, " +
		// sameer 06272006 -- getPropIncluded added
		"sum(data.value* intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		//"sum(data.value), " +
		"name, cost_items.target_market "+
		"from alt_works, cost_items, data, month_timeid "+
		"where alt_works.id = ? and cost_items.alt_devcost_id = alt_works.ref_alt_devcost_id "+
		"and data.cost_li_id = cost_items.id "+
		"and task_internal_id is not null "+
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, target_market, "+
		"classify1, classify2, classify3, classify4, classify5, month_timeid.time_id, name "+
		"order by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, target_market, "+
		"classify1, classify2, classify3, classify4, classify5, month_timeid.time_id, name "
		;

	//--ratings ..
	public static String GET_RATINGS = "select dim_id, raw_score, weighted_score, val from ratings, alt_map_items where alt_map_items.alt_id = ? and alt_map_items.wspace_id = ? and isdefault=1 and alt_map_items.alt_rating_id = ratings.alt_rating_id";
	public static String CREATE_RATINGS = "insert into alt_ratings (id, alt_id, wspace_id) values (?,?,?)";
	public final static String UPDATE_ALT_MAP_ITEM_RATING = "update alt_map_items set alt_rating_id=? where wspace_id=? and alt_id=? and isdefault=1";
	public final static String CREATE_RATING_ENTRY = "insert into ratings (alt_rating_id, dim_id, raw_score, weighted_score, val) values (?,?,?,?,?)";

	/*
	 * Query for retreiving named resources  -- Sameer 03142005
	 */
	public static String NAMED_RES_SEL_CLAUSE =
		"select fte_heads.id, fte_heads.name, fte_heads.is_generic, " +
		"fte_heads.prim_skill, fte_heads.classify1, fte_heads.classify2, fte_heads.classify3, fte_heads.classify4, fte_heads.classify5, fte_heads.port_node_id " +
		"from fte_heads " +
		"where " +
		"fte_heads.is_generic = ? " +
		"and " +
		"fte_heads.is_active = ? " +
		"order by fte_heads.name";

	public static String NAMED_RES_SEC_SKILL_CLAUSE =
		"select fte_head_sec_skill.skill_id, fte_head_sec_skill.skill_level " +
		"from fte_head_sec_skill where " +
		"fte_head_sec_skill.fte_head_id = ? ";

	public static String NAMED_RES_SEL_CLAUSE_ADDNL = //port_node_id not used
		"select id, name, is_generic, " +
		"prim_skill, classify1, classify2, classify3, classify4, classify5, port_node_id " +
		"from fte_heads " +
		"where " +
		"is_generic = ? " +
		"and " +
		"is_active = ? " +
		"and " +
		"classify1 = ? " +
		"order by name ";

	public static String OFF_TIMES_SEL_CLAUSE =
		"select off_times.start_date, off_times.end_date, off_times.val_unavail " +
		"from off_times " +
		"where " +
		"off_times.fte_head_id = ? " +
		"and " +
		"off_times.end_date > ? " +
		"and off_times.is_block_time is null " +
		"order by off_times.end_date ";

	public static String GET_GENERIC_ID =
		"select fte_heads.prim_skill from fte_heads where id = ?";

	public static String DELETE_NAMED_RES_CLAUSE =
		"update fte_items set fte_head_id = ? " +
		"where fte_head_id = ? ";

	public static String INSERT_NAMED_RES =
	Misc.G_DO_ORACLE ?
		"insert into fte_heads " +
		"(id, name, ann_cost, is_generic, prim_skill, is_active) " +
		"values(?, ?, ?, ?, ?, ?)"
	:
	"insert into fte_heads " +
		"( name, ann_cost, is_generic, prim_skill, is_active) " +
		"values( ?, ?, ?, ?, ?)";
    

	public static String UPDATE_NAMED_RES =
		"update fte_heads set name = ?, ann_cost = ?, " +
		"is_generic = ?, prim_skill = ?, is_active = ?, " +
		"classify1 = ?,  classify2 = ? " +
		"where id = ? ";

	public static String DELETE_SEC_SKILLS_CLAUSE =
		"delete from fte_head_sec_skill where fte_head_id = ? ";

	public static String DELETE_OFF_TIMES_CLAUSE =
		"delete from off_times where fte_head_id = ? and off_times.is_block_time is null";

	public static String SEC_SKILLS_INSERT_CLAUSE =
		"insert into fte_head_sec_skill " +
		"(fte_head_id, skill_id) " +
		"values(?, ?) ";

	public static String OFF_TIMES_INSERT_CLAUSE =
		"insert into off_times " +
		"(fte_head_id, start_date, end_date, val_unavail) " +
		"values(?, ?, ?, ?) ";

	public static String DEACTIVATE_RESOURCE =
		"update fte_heads set is_active = 0 where id = ? ";

	//
	// End of query for retrieving named resources -- Sameer 03142005
	//

	//
	// Queries for retreiving cur assignments of resources
	//
	//modified rajeev 080905 ...
	public static String GET_CUR_ASSIGN_BY_WEEK = Misc.G_DO_ORACLE ? "select " +
		"fte_items.fte_head_id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)), 3  " +
		"from " +
		"port_results, fte_items, fte_heads, data, week_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id <> ? " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and fte_heads.id = fte_items.fte_head_id "+
		"and fte_heads.is_active = 1 "+
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 "+
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"week_timeid.time_id >= ? " +
		"and " +
		"week_timeid.time_id <= ? " +
		"and " +
		"week_timeid.time_id >= data.year - 12 " +
		"and " +
		"week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"fte_items.fte_head_id, week_timeid.time_id " +
		"order by fte_items.fte_head_id, week_timeid.time_id " 
    :
		"select " +
		"fte_items.fte_head_id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)), 3  " +
		"from " +
		"port_results, fte_items, fte_heads, data, week_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id <> ? " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and fte_heads.id = fte_items.fte_head_id "+
		"and fte_heads.is_active = 1 "+
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 "+
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"week_timeid.time_id >= ? " +
		"and " +
		"week_timeid.time_id <= ? " +
		"and " +
		"week_timeid.time_id >= data.year - 12 " +
		"and " +
		"week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"fte_items.fte_head_id, week_timeid.time_id " +
		"order by fte_items.fte_head_id, week_timeid.time_id ";

	//-- monthly
	public static String GET_CUR_ASSIGN_BY_MONTH = Misc.G_DO_ORACLE ? "select " +
		"fte_items.fte_head_id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), 2  " +
		"from " +
		"port_results, fte_items, fte_heads, data, month_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id <> ? " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and fte_heads.id = fte_items.fte_head_id "+
		"and fte_heads.is_active = 1 "+
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 "+
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"month_timeid.time_id >= ? " +
		"and " +
		"month_timeid.time_id <= ? " +
		"and " +
		"month_timeid.time_id >= trunc(data.year/35)*35 " +
		"and " +
		"month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"fte_items.fte_head_id, month_timeid.time_id " +
		"order by fte_items.fte_head_id, month_timeid.time_id " :
		"select " +
		"fte_items.fte_head_id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), 2  " +
		"from " +
		"port_results, fte_items, fte_heads, data, month_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id <> ? " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and fte_heads.id = fte_items.fte_head_id "+
		"and fte_heads.is_active = 1 "+
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 "+
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"month_timeid.time_id >= ? " +
		"and " +
		"month_timeid.time_id <= ? " +
		"and " +
		"month_timeid.time_id >= cast((data.year/35) as int)*35 " +
		"and " +
		"month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"fte_items.fte_head_id, month_timeid.time_id " +
		"order by fte_items.fte_head_id, month_timeid.time_id ";

	//-- quarterly
	public static String GET_CUR_ASSIGN_BY_QTR = Misc.G_DO_ORACLE ? "select " +
		"fte_items.fte_head_id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), 1 " +
		"from " +
		"port_results, fte_items, fte_heads, data, qtr_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id <> ? " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and fte_heads.id = fte_items.fte_head_id "+
		"and fte_heads.is_active = 1 "+
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 "+
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"qtr_timeid.time_id >= ? " +
		"and " +
		"qtr_timeid.time_id <= ? " +
		"and " +
		"qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"and " +
		"qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"fte_items.fte_head_id, qtr_timeid.time_id " +
		"order by fte_items.fte_head_id, qtr_timeid.time_id " 
    :
		"select " +
		"fte_items.fte_head_id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), 1 " +
		"from " +
		"port_results, fte_items, fte_heads, data, qtr_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id <> ? " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and fte_heads.id = fte_items.fte_head_id "+
		"and fte_heads.is_active = 1 "+
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 "+
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"qtr_timeid.time_id >= ? " +
		"and " +
		"qtr_timeid.time_id <= ? " +
		"and " +
		"qtr_timeid.time_id >= cast((data.year/105) as int)*105 " +
		"and " +
		"qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"fte_items.fte_head_id, qtr_timeid.time_id " +
		"order by fte_items.fte_head_id, qtr_timeid.time_id ";

	//--year
	public static String GET_CUR_ASSIGN_BY_YEAR = Misc.G_DO_ORACLE ? "select " +
		"fte_items.fte_head_id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), 0 " +
		"from " +
		"port_results, fte_items, fte_heads, data, year_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id <> ? " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and fte_heads.id = fte_items.fte_head_id "+
		"and fte_heads.is_active = 1 "+
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 "+
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"year_timeid.time_id >= ? " +
		"and " +
		"year_timeid.time_id <= ? " +
		"and " +
		"year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and " +
		"year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"fte_items.fte_head_id, year_timeid.time_id " +
		"order by fte_items.fte_head_id, year_timeid.time_id " 
    :
		"select " +
		"fte_items.fte_head_id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), 0 " +
		"from " +
		"port_results, fte_items, fte_heads, data, year_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id <> ? " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and fte_heads.id = fte_items.fte_head_id "+
		"and fte_heads.is_active = 1 "+
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 "+
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"year_timeid.time_id >= ? " +
		"and " +
		"year_timeid.time_id <= ? " +
		"and " +
		"year_timeid.time_id >= cast((data.year/420) as int)*420 " +
		"and " +
		"year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"fte_items.fte_head_id, year_timeid.time_id " +
		"order by fte_items.fte_head_id, year_timeid.time_id ";

	//
	// End of Queries for retreiving cur assignments of resources
	//

	//Queries for res_availability -- sameer 03222005
	public static String GET_RES_AVAIL_BY_WEEK = Misc.G_DO_ORACLE ? "select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)), " +
		"port_results.prj_id, alt_work_items.internal_id, " +
		"projects.name, alt_work_items.name " +
		"from " +
		"port_results, fte_items, data, week_timeid, " +
		"projects, alt_work_items " +
		"where " +
		"fte_items.fte_head_id = ? " +
		"and " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id = projects.id " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and " +
		"alt_work_items.internal_id = fte_items.task_internal_id " +
		"and " +
		"alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"week_timeid.time_id >= ? " +
		"and " +
		"week_timeid.time_id <= ? " +
		"and " +
		"week_timeid.time_id >= data.year - 12 " +
		"and " +
		"week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur *1.25+7"+
		"    else 1 "+
		"end) "+
		"group by " +
		"port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, week_timeid.time_id " +
		"order by port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, week_timeid.time_id " 
    :
		"select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)), " +
		"port_results.prj_id, alt_work_items.internal_id, " +
		"projects.name, alt_work_items.name " +
		"from " +
		"port_results, fte_items, data, week_timeid, " +
		"projects, alt_work_items " +
		"where " +
		"fte_items.fte_head_id = ? " +
		"and " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id = projects.id " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and " +
		"alt_work_items.internal_id = fte_items.task_internal_id " +
		"and " +
		"alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"week_timeid.time_id >= ? " +
		"and " +
		"week_timeid.time_id <= ? " +
		"and " +
		"week_timeid.time_id >= data.year - 12 " +
		"and " +
		"week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur *1.25+7"+
		"    else 1 "+
		"end) "+
		"group by " +
		"port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, week_timeid.time_id " +
		"order by port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, week_timeid.time_id ";

	//-- monthly
	public static String GET_RES_AVAIL_BY_MONTH = Misc.G_DO_ORACLE ? "select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"port_results.prj_id, alt_work_items.internal_id, " +
		"projects.name, alt_work_items.name " +
		"from " +
		"port_results, fte_items, data, month_timeid, " +
		"projects, alt_work_items " +
		"where " +
		"fte_items.fte_head_id = ? " +
		"and " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id = projects.id " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and " +
		"alt_work_items.internal_id = fte_items.task_internal_id " +
		"and " +
		"alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"month_timeid.time_id >= ? " +
		"and " +
		"month_timeid.time_id <= ? " +
		"and " +
		"month_timeid.time_id >= trunc(data.year/35)*35 " +
		"and " +
		"month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, month_timeid.time_id " +
		"order by port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, month_timeid.time_id " 
    : 
		"select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"port_results.prj_id, alt_work_items.internal_id, " +
		"projects.name, alt_work_items.name " +
		"from " +
		"port_results, fte_items, data, month_timeid, " +
		"projects, alt_work_items " +
		"where " +
		"fte_items.fte_head_id = ? " +
		"and " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id = projects.id " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and " +
		"alt_work_items.internal_id = fte_items.task_internal_id " +
		"and " +
		"alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"month_timeid.time_id >= ? " +
		"and " +
		"month_timeid.time_id <= ? " +
		"and " +
		"month_timeid.time_id >= cast((data.year/35) as int) *35 " +
		"and " +
		"month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, month_timeid.time_id " +
		"order by port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, month_timeid.time_id ";

	//-- quarterly
	public static String GET_RES_AVAIL_BY_QTR = Misc.G_DO_ORACLE ? "select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"port_results.prj_id, alt_work_items.internal_id, " +
		"projects.name, alt_work_items.name " +
		"from " +
		"port_results, fte_items, data, qtr_timeid " +
		"where " +
		"fte_items.fte_head_id = ? " +
		"and " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id = projects.id " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and " +
		"alt_work_items.internal_id = fte_items.task_internal_id " +
		"and " +
		"alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"qtr_timeid.time_id >= ? " +
		"and " +
		"qtr_timeid.time_id <= ? " +
		"and " +
		"qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"and " +
		"qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, qtr_timeid.time_id " +
		"order by port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, qtr_timeid.time_id " 
    :
    "select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"port_results.prj_id, alt_work_items.internal_id, " +
		"projects.name, alt_work_items.name " +
		"from " +
		"port_results, fte_items, data, qtr_timeid " +
		"where " +
		"fte_items.fte_head_id = ? " +
		"and " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id = projects.id " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and " +
		"alt_work_items.internal_id = fte_items.task_internal_id " +
		"and " +
		"alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"qtr_timeid.time_id >= ? " +
		"and " +
		"qtr_timeid.time_id <= ? " +
		"and " +
		"qtr_timeid.time_id >= cast((data.year/105) as int)*105 " +
		"and " +
		"qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, qtr_timeid.time_id " +
		"order by port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, qtr_timeid.time_id ";

	//--year
	public static String GET_RES_AVAIL_BY_YEAR = Misc.G_DO_ORACLE ? "select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"port_results.prj_id, alt_work_items.internal_id, " +
		"projects.name, alt_work_items.name " +
		"from " +
		"port_results, fte_items, data, year_timeid, " +
		"alt_work_items, projects " +
		"where " +
		"fte_items.fte_head_id = ? " +
		"and " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id = projects.id " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and " +
		"alt_work_items.internal_id = fte_items.task_internal_id " +
		"and " +
		"alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"year_timeid.time_id >= ? " +
		"and " +
		"year_timeid.time_id <= ? " +
		"and " +
		"year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and " +
		"year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, year_timeid.time_id " +
		"order by port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, year_timeid.time_id " 
    : 
		"select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"port_results.prj_id, alt_work_items.internal_id, " +
		"projects.name, alt_work_items.name " +
		"from " +
		"port_results, fte_items, data, year_timeid, " +
		"alt_work_items, projects " +
		"where " +
		"fte_items.fte_head_id = ? " +
		"and " +
		"port_results.port_rs_id = ? " +
		"and " +
		"port_results.prj_id = projects.id " +
		"and " +
		"port_results.fund_status = 1 " +
		"and " +
		"fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		"and " +
		"alt_work_items.internal_id = fte_items.task_internal_id " +
		"and " +
		"alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"data.fte_item_id = fte_items.id " +
		"and " +
		"year_timeid.time_id >= ? " +
		"and " +
		"year_timeid.time_id <= ? " +
		"and " +
		"year_timeid.time_id >= cast((data.year/420) as int)*420 " +
		"and " +
		"year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " +
		"port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, year_timeid.time_id " +
		"order by port_results.prj_id, projects.name, alt_work_items.internal_id, alt_work_items.name, year_timeid.time_id ";

	//End of queries for res_availability -- sameer 03222005

	//rajeev 041805 ... amgen related stuff needs to reviewed
	//rajeev 042705 ... fixes
	public static String UPDATE_PORT_WKSP_DESC = "update port_wksps set port_wksp_desc = ? where id = ?";
  //TODO_INQUERY
	public static String COUNT_NON_AUTOUPDATE_RSET = "select count(*), port_wksp_id from port_rset where is_auto_updateable=0 and port_wksp_id in (select port_wksp_id from port_rset where id = ?) group by port_wksp_id";

	//rajeev 051605 ... fixes/enhancements
  //TODO_INQUERY ... not imp
	public static String DEDUP_PORT_RESULTS //= "delete from port_results where port_rs_id=? and ((id, alt_id) not in (select min(id), alt_id from port_results where port_rs_id=? group by alt_id))";
  = "delete from port_results where port_rs_id=? and (id not in (select min(id) from port_results ps2 where ps2.port_rs_id=? and ps2.alt_id = port_results.alt_id))";
	//getting untouchables

	public static String GET_DEVCOST_LIKE_BY_MONTH_1 = //"select "+
		//group by clauses 
    Misc.G_DO_ORACLE ? "time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur) * milestones.prev_cumm_prob) "+
		",2 "+
		"from "+
		"port_results " 
    : 
		"time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur) * milestones.prev_cumm_prob) "+
		",2 "+
		"from "+
		"port_results ";

	//measure specific table
	//  ",fte_items "+
	public static String GET_DEVCOST_LIKE_BY_MONTH_2 =
		",data "+
		",month_timeid "+
		",milestones ";
	//join by tables
	public static String GET_DEVCOST_LIKE_BY_ANY_3 = Misc.G_DO_ORACLE ? ",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 minus select alt_id from port_results where port_rs_id=?) altList "+
		"where "+
		"port_results.port_rs_id = ? "+ //the curr one
		"and port_results.fund_status = 1 "+
		"and port_results.alt_id = altList.alt_id "+
		"and milestones.alt_date_id = port_results.ver_alt_mstone_id " 
    :
		",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 and Not Exists( select alt_id from port_results ps2 where ps2.port_rs_id=? and ps2.alt_id = port_results.alt_id)) altList "+
		"where "+
		"port_results.port_rs_id = ? "+ //the curr one
		"and port_results.fund_status = 1 "+
		"and port_results.alt_id = altList.alt_id "+
		"and milestones.alt_date_id = port_results.ver_alt_mstone_id ";
	public static String GET_DEVCOST_LIKE_BY_FTE_ANY_4 =
		//measure specific and clause
		"and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		"and data.fte_item_id = fte_items.id "+
		"and fte_items.to_include = 1 "+
		"and milestones.mstn_id = fte_items.for_achieving_milestone "+
		"and milestones.target_market = fte_items.target_market ";
	//group by specific join clause
	public static String GET_DEVCOST_LIKE_BY_MONTH_5 =
		//val_scope specific condition
    Misc.G_DO_ORACLE ? "and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= trunc(data.year/35)*35 "+
		"and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by " 
    :
		"and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= cast((data.year/35) as int)*35 "+
		"and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by ";
	public static String GET_DEVCOST_LIKE_BY_ANY_6 = //rajeev 081205 ... changed name
		//group by clauses
		"time_id "+
		"order by ";
	public static String GET_DEVCOST_LIKE_BY_ANY_7 = //rajeev 081205 ... changed name
		//group by clauses
		"time_id ";


	//      "select "
	//      +sel_list+optional ","
	//      + _1_M
	//      + " ,fte_items "
	//      + _2_M
	//      + ","+join_table_List
	//      +_3_R
	//      +_4_F
	//      +"and "+join_cond_list
	//      +_5_M
	//      +sel_list + Optional ","
	//      +_6_M
	//      +sel_list + Optional ","
	//      +_7_M;
	public static String GET_DEVCOST_LIKE_BY_WEEK_1 = //"select "+
		//group by clauses
    Misc.G_DO_ORACLE ? "time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur) * milestones.prev_cumm_prob) "+
		",3 "+
		"from "+
		"port_results "
    :
		"time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur) * milestones.prev_cumm_prob) "+
		",3 "+
		"from "+
		"port_results ";

	//measure specific table
	//  ",fte_items "+
	public static String GET_DEVCOST_LIKE_BY_WEEK_2 =
		",data "+
		",week_timeid "+
		",milestones ";
	//join by tables
	public static String GET_DEVCOST_LIKE_BY_DEVCOST_ANY_4 =
		//measure specific and clause
		"and cost_items.alt_devcost_id = port_results.ver_alt_devcost_id "+
		"and data.cost_li_id = cost_items.id "+
		"and cost_items.to_include = 1 "+
		"and milestones.mstn_id = cost_items.for_achieving_milestone "+
		"and milestones.target_market = cost_items.target_market ";
	//group by specific join clause
	public static String GET_DEVCOST_LIKE_BY_WEEK_5 =
		//val_scope specific condition
		"and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= data.year-12 "+
		"and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by ";

	public static String GET_DEVCOST_LIKE_BY_QTR_1 = //"select "+
		//group by clauses
		Misc.G_DO_ORACLE ? "time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur) * milestones.prev_cumm_prob) "+
		",0 "+
		"from "+
		"port_results " 
    :
    "time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur) * milestones.prev_cumm_prob) "+
		",0 "+
		"from "+
		"port_results ";

	//measure specific table
	//  ",fte_items "+
	public static String GET_DEVCOST_LIKE_BY_QTR_2 =
		",data "+
		",qtr_timeid "+
		",milestones ";
	//join by tables
	public static String GET_DEVCOST_LIKE_BY_QTR_5 =
		//val_scope specific condition
    Misc.G_DO_ORACLE ? 
    "and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= trunc(data.year/105)*105 "+
		"and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "
    :
		"and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= cast((data.year/105) as int)*105 "+
		"and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by ";

	public static String GET_DEVCOST_LIKE_BY_YEAR_1 = //"select "+
		//group by clauses
    Misc.G_DO_ORACLE ? 
    "time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur) * milestones.prev_cumm_prob) "+
		",1 "+
		"from "+
		"port_results "
    :
		"time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur) * milestones.prev_cumm_prob) "+
		",1 "+
		"from "+
		"port_results ";

	//measure specific table
	//  ",fte_items "+
	public static String GET_DEVCOST_LIKE_BY_YEAR_2 =
		",data "+
		",year_timeid "+
		",milestones ";
	//join by tables
	public static String GET_DEVCOST_LIKE_BY_YEAR_5 =
		//val_scope specific condition
    Misc.G_DO_ORACLE ? 
    "and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= trunc(data.year/420)*420 "+
		"and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "
    :
		"and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= cast((data.year/420) as int)*420 "+
		"and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by ";



	public static String GET_UNTOUCH_REV_BY_QTR_1 =
		//"select "+ //sel list + optional ","
    Misc.G_DO_ORACLE ?
    "time_id,  sum(data.value*alt_scen_list.scen_prob*milestones.cumm_prob*intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), 0 "+
		"from "+
		"port_results, rev_segs, data, milestones, alt_scen_list, qtr_timeid "
    :
		"time_id,  sum(data.value*alt_scen_list.scen_prob*milestones.cumm_prob* intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), 0 "+
		"from "+
		"port_results, rev_segs, data, milestones, alt_scen_list, qtr_timeid ";
	//"," join table list
	public static String GET_UNTOUCH_REV_BY_QTR_2 =
  Misc.G_DO_ORACLE ?
  ",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 minus select alt_id from port_results where port_rs_id=?) altList "+
		" where "+
		"port_results.port_rs_id = ? and "+
		"port_results.fund_status = 1 and "+
		"port_results.alt_id = altList.alt_id and "+
		//   "port_results.alt_id not in (select alt_id from port_results where port_rs_id=?) and "+
		"port_results.ver_alt_rev_id = rev_segs.alt_rev_id and "+
		"alt_scen_list.delay_val = 0 and "+
		"data.rev_seg_id = rev_segs.id "+
		"and qtr_timeid.time_id >= ? "+
		//"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		" and milestones.alt_date_id = port_results.ver_alt_mstone_id "+
		" and milestones.mstn_id = ? "+
		" and milestones.target_market = rev_segs.mkt_type "+
		" and alt_scen_list.alt_id = port_results.alt_id "+
		" and alt_scen_list.scen_id = rev_segs.scen_id "
    :
    ",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 and Not Exists( select alt_id from port_results where port_rs_id=?)) altList "+
		" where "+
		"port_results.port_rs_id = ? and "+
		"port_results.fund_status = 1 and "+
		"port_results.alt_id = altList.alt_id and "+
		//   "port_results.alt_id not in (select alt_id from port_results where port_rs_id=?) and "+
		"port_results.ver_alt_rev_id = rev_segs.alt_rev_id and "+
		"alt_scen_list.delay_val = 0 and "+
		"data.rev_seg_id = rev_segs.id "+
		"and qtr_timeid.time_id >= ? "+
		//"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		" and milestones.alt_date_id = port_results.ver_alt_mstone_id "+
		" and milestones.mstn_id = ? "+
		" and milestones.target_market = rev_segs.mkt_type "+
		" and alt_scen_list.alt_id = port_results.alt_id "+
		" and alt_scen_list.scen_id = rev_segs.scen_id ";
	//and join cond_list   "group by "+
	//selList + Optional ","
	public static String GET_UNTOUCH_REV_BY_QTR_3 =
		" time_id "+
		" order by ";
	public static String GET_UNTOUCH_REV_BY_QTR_4 =
		//selList + Optional ","
		" time_id";


	public static String GET_UNTOUCH_OPCOST_BY_QTR_1 =
		//"select "+ //sel list + optional ","
    Misc.G_DO_ORACLE ?
    "time_id,  sum(data.value*alt_scen_list.scen_prob*milestones.cumm_prob * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), 0 "+
		"from "+
		"port_results,  cost_items, data, milestones, alt_scen_list, qtr_timeid "
    :
		"time_id,  sum(data.value*alt_scen_list.scen_prob*milestones.cumm_prob * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), 0 "+
		"from "+
		"port_results,  cost_items, data, milestones, alt_scen_list, qtr_timeid ";
	//"," join table list
	public static String GET_UNTOUCH_OPCOST_BY_QTR_2 =
  Misc.G_DO_ORACLE ? 
  ",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 minus select alt_id from port_results where port_rs_id=?) altList "+
		" where "+
		"port_results.port_rs_id = ? and "+
		"port_results.fund_status = 1 and "+
		"port_results.alt_id = altList.alt_id and "+
		//   "port_results.alt_id not in (select alt_id from port_results where port_rs_id=?) and "+
		"port_results.ver_alt_opcost_id = cost_items.alt_opcost_id and "+
		"alt_scen_list.delay_val = 0 and "+
		"data.cost_li_id = cost_items.id "+
		"and qtr_timeid.time_id >= ? "+
		//"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		" and milestones.alt_date_id = port_results.ver_alt_mstone_id "+
		" and milestones.mstn_id = ? "+
		" and milestones.target_market = cost_items.target_market "+
		" and alt_scen_list.alt_id = port_results.alt_id "+
		" and alt_scen_list.scen_id = cost_items.scen_id "
    :
		",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 and Not Exists( select alt_id from port_results where port_rs_id=?)) altList "+
		" where "+
		"port_results.port_rs_id = ? and "+
		"port_results.fund_status = 1 and "+
		"port_results.alt_id = altList.alt_id and "+
		//   "port_results.alt_id not in (select alt_id from port_results where port_rs_id=?) and "+
		"port_results.ver_alt_opcost_id = cost_items.alt_opcost_id and "+
		"alt_scen_list.delay_val = 0 and "+
		"data.cost_li_id = cost_items.id "+
		"and qtr_timeid.time_id >= ? "+
		//"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		" and milestones.alt_date_id = port_results.ver_alt_mstone_id "+
		" and milestones.mstn_id = ? "+
		" and milestones.target_market = cost_items.target_market "+
		" and alt_scen_list.alt_id = port_results.alt_id "+
		" and alt_scen_list.scen_id = cost_items.scen_id ";
	//and join cond_list   "group by "+
	//selList + Optional ","
	public static String GET_UNTOUCH_OPCOST_BY_QTR_3 =
		" time_id "+
		" order by ";
	public static String GET_UNTOUCH_OPCOST_BY_QTR_4 =
		//selList + Optional ","
		" time_id ";

	public static String VERIFY_LOGIN_WKSP_1 = "select priv_id, users.id from users, user_roles, role_privs where users.username = ? and users.password = ? and isactive=1 and user_roles.user_1_id = users.id and role_privs.role_id = user_roles.role_id and priv_id in ";
	public static String VERIFY_LOGIN_WKSP_2 = " union (select 1, users.id from users where users.username = ? and users.password = ? and isactive=1 and users.id=1)";
	public final static String GET_PRJ_ALT_WKSP_MPP_1 =  "select projects.id, projects.name, workspaces.id, workspaces.name,  alternatives.id, alternatives.name, pj_map_items.map_type, alt_map_items.alt_date_id, alt_profil_id, prj_lock_status.currentowner, prj_lock_status.datetime, users.name "+
		"from projects, workspaces, alternatives, pj_map_items, alt_map_items, (";
	public final static String GET_PRJ_ALT_WKSP_MPP_2 = Misc.G_DO_ORACLE ? 
  ") privProjList, prj_lock_status, users "+ //has 3 params of user_id
		"where "+
		"privProjList.prj_id = projects.id "+
		"and workspaces.prj_id = projects.id "+
		"and pj_map_items.wspace_id = workspaces.id "+
		"and prj_lock_status.wspace_id(+) = pj_map_items.wspace_id " +
		"and prj_lock_status.type(+) = ? " +
		"and users.id(+) = prj_lock_status.currentowner " +
		"and pj_map_items.isdefault = 1 and pj_map_items.map_type in (1, 4) and projects.status in (1,2,3,4,7) " +
		"and alt_map_items.isdefault = 1 and alt_map_items.wspace_id = pj_map_items.wspace_id and alt_map_items.alt_id = alternatives.id "+
		"and alternatives.prj_id = projects.id "+
		"order by projects.name asc, projects.id desc, workspaces.id asc, pj_map_items.map_type desc, alternatives.id desc "
    :
    ") privProjList, prj_lock_status, users "+ //has 3 params of user_id
		"where "+
		"privProjList.prj_id = projects.id "+
		"and workspaces.prj_id = projects.id "+
		"and pj_map_items.wspace_id = workspaces.id "+
		"and prj_lock_status.wspace_id =* pj_map_items.wspace_id " +
		"and prj_lock_status.type =* ? " +
		"and users.id =* prj_lock_status.currentowner " +
		"and pj_map_items.isdefault = 1 and pj_map_items.map_type in (1, 4) and projects.status in (1,2,3,4,7) " +
		"and alt_map_items.isdefault = 1 and alt_map_items.wspace_id = pj_map_items.wspace_id and alt_map_items.alt_id = alternatives.id "+
		"and alternatives.prj_id = projects.id "+
		"order by projects.name asc, projects.id desc, workspaces.id asc, pj_map_items.map_type desc, alternatives.id desc ";

	public final static String GET_FTE_DATA_FOR_UPD = "select fte_items.id, data.val_scope, data.val_dur, data.year, data.value from fte_items, data where fte_items.alt_fte_id = ? and fte_items.task_internal_id = ? and to_include = 1 and data.fte_item_id = fte_items.id order by fte_items.id, data.year";
	public final static String GET_COST_DATA_FOR_UPD = "select cost_items.id, data.val_scope, data.val_dur, data.year, data.value from cost_items, data where cost_items.alt_devcost_id = ? and cost_items.task_internal_id = ? and to_include = 1 and data.cost_li_id = cost_items.id order by cost_items.id, data.year";

     public final static String GET_MS_FTE_DATA_FOR_UPD = "select fte_items.id, data.val_scope, data.val_dur, data.year, data.value from fte_items, data where fte_items.alt_fte_id = ? and fte_items.for_achieving_milestone = ? and (fte_items.target_market = ? or fte_items.target_market is null) and to_include = 1 and data.fte_item_id = fte_items.id order by fte_items.id, data.year";
	public final static String GET_MS_COST_DATA_FOR_UPD = "select cost_items.id, data.val_scope, data.val_dur, data.year, data.value from cost_items, data where cost_items.alt_devcost_id = ? and cost_items.for_achieving_milestone = ? and (cost_items.target_market = ? or cost_items.target_market is null) and to_include = 1 and data.cost_li_id = cost_items.id order by cost_items.id, data.year";

     public final static String UPD_MS_FTE_ITEM_DATE = "update fte_items set start_date = start_date+?+(start_date+?-?)*?, end_date = end_date+?+(end_date+?-?)*? where alt_fte_id = ?  and fte_items.for_achieving_milestone = ? and (fte_items.target_market = ? or fte_items.target_market is null) and start_date is not null and end_date is not null "; //shift, shift, msNewStart, frac ... shift, shift, msNewStart, frac
     public final static String UPD_MS_COST_ITEM_DATE = "update cost_items set start_date = start_date+?+(start_date+?-?)*?, end_date = end_date+?+(end_date+?-?)*? where cost_items.alt_devcost_id = ?  and cost_items.for_achieving_milestone = ? and (cost_items.target_market = ? or cost_items.target_market is null) and start_date is not null and end_date is not null "; //shift, shift, msNewStart, frac ... shift, shift, msNewStart, frac

	public static String UPDATE_PORTRSET_WITH_ORIG = "update port_results upd set (ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id) "+
		" = (select ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id from port_results, port_rset where port_rset.port_wksp_id = ? and is_auto_updateable=1 and port_results.port_rs_id = port_rset.id and alt_id=? and rownum <=1) "+
		" where port_rs_id = ? and alt_id=? ";


	public static String GET_USER_ACCOUNT_INFO =
		"select id, name, username, password, email, phone " +
		"from users " +
		"where " +
		"id = ?";

	public static String UPDATE_USER_ACCOUNT_INFO =
		"update users " +
		"set " +
		"password = ?, " +
		"email = ?, " +
		"phone = ? " +
		",last_password_change=now() "+
		"where id = ?";

	public static String CHECK_USER_NAME =
		"select id from users where username = ?";


	//rajeev 052405 ... for Watson IT demo ..
	public static String SHORT_TERM_FTE_LISTS = "select fte_items.name, task_internal_id, start_date, end_date, "+
		" data.year, data.val_scope, data.val_dur, data.value, "+
		" fte_head_id, fte_heads.name, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.target_market, fte_items.for_skill, fte_items.assignment_status, fte_items.id "+
		" from fte_items, data, alt_map_items, fte_heads "+
		" where alt_map_items.alt_id = ? "+
		" and alt_map_items.wspace_id = ? "+
		" and alt_map_items.isdefault = 1 "+
		" and alt_map_items.alt_fte_id = fte_items.alt_fte_id "+
		" and fte_items.id = data.fte_item_id "+
		" and fte_items.fte_head_id = fte_heads.id "+
		" and to_include = 1 "+
		" order by task_internal_id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, data.year ";

	public static String SHORT_TERM_COST_LISTS = "select name, task_internal_id, start_date, end_date, "+
		" data.year, data.val_scope, data.val_dur, data.value, "+
		" cost_cent_id, 'a', for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, target_market, 1,1,cost_items.id "+
		" from cost_items, data, alt_map_items "+
		" where alt_map_items.alt_id = ? "+
		" and alt_map_items.wspace_id = ? "+
		" and alt_map_items.isdefault = 1 "+
		" and alt_map_items.alt_devcost_id = cost_items.alt_devcost_id "+
		" and cost_items.id = data.cost_li_id "+
		" and to_include = 1 "+
		" order by task_internal_id, name, cost_cent_id, data.year ";

	//060905 - rajeev - get off time by week for each of the named resources

	public static String GET_RES_OFFTIME_BY_WEEK =
  Misc.G_DO_ORACLE ?
  "select fte_head_id, time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 3) " +
		"					else " +
		"					Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 3, Round(end_date-start_date+1,0)) " +
		"				end) " +
		")v, 3, purpose " +
		"from off_times, fte_heads , week_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= getTimeId(start_date) -12) " +
		"and (end_date is null or time_id <= getTimeId(end_date)) " +
		"group by fte_head_id, time_id, purpose " +
		"order by fte_head_id, time_id, purpose"
    :
		"select fte_head_id, time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then intelli.getDuration(time_id, 3) " +
		"					else " +
		"					cast((end_date-start_date+1) as numeric) * intelli.getPropIncluded(5, intelli.getTimeId(start_date), time_id, 3, cast((end_date-start_date+1) as numeric)) " +
		"				end) " +
		")v, 3, purpose " +
		"from off_times, fte_heads , week_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= intelli.getTimeId(start_date) -12) " +
		"and (end_date is null or time_id <= intelli.getTimeId(end_date)) " +
		"group by fte_head_id, time_id, purpose " +
		"order by fte_head_id, time_id, purpose";

	public static String GET_RES_OFFTIME_BY_MONTH =
  Misc.G_DO_ORACLE ?
  "select fte_head_id, time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 2) " +
		"					else " +
		"					Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 2, Round(end_date-start_date+1,0)) " +
		"				end) " +
		")v, 2, purpose " +
		"from off_times, fte_heads , month_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= trunc(getTimeId(start_date)/35)*35) " +
		"and (end_date is null or time_id <= getTimeId(end_date)) " +
		"group by fte_head_id, time_id, purpose " +
		"order by fte_head_id, time_id, purpose"
    :
		"select fte_head_id, time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then intelli.getDuration(time_id, 2) " +
		"					else " +
		"					cast((end_date-start_date+1) as numeric) * intelli.getPropIncluded(5, intelli.getTimeId(start_date), time_id, 2, cast((end_date-start_date+1) as numeric)) " +
		"				end) " +
		")v, 2, purpose " +
		"from off_times, fte_heads , month_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= cast((intelli.getTimeId(start_date)/35) as int)*35) " +
		"and (end_date is null or time_id <= intelli.getTimeId(end_date)) " +
		"group by fte_head_id, time_id, purpose " +
		"order by fte_head_id, time_id, purpose";

	public static String GET_RES_OFFTIME_BY_QTR = Misc.G_DO_ORACLE ? 
  "select fte_head_id, time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 0) " +
		"					else " +
		"					Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 0, Round(end_date-start_date+1,0)) " +
		"				end) " +
		")v, 0, purpose " +
		"from off_times, fte_heads , qtr_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= trunc(getTimeId(start_date)/105)*105) " +
		"and (end_date is null or time_id <= getTimeId(end_date)) " +
		"group by fte_head_id, time_id, purpose " +
		"order by fte_head_id, time_id, purpose"
    :
		"select fte_head_id, time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then intelli.getDuration(time_id, 0) " +
		"					else " +
		"					cast((end_date-start_date+1) as numeric) * intelli.getPropIncluded(5, intelli.getTimeId(start_date), time_id, 0, cast((end_date-start_date+1) as numeric)) " +
		"				end) " +
		")v, 0, purpose " +
		"from off_times, fte_heads , qtr_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= cast((intelli.getTimeId(start_date)/105) as int)*105) " +
		"and (end_date is null or time_id <= intelli.getTimeId(end_date)) " +
		"group by fte_head_id, time_id, purpose " +
		"order by fte_head_id, time_id, purpose";

	public static String GET_RES_OFFTIME_BY_YEAR = Misc.G_DO_ORACLE ?
  "select fte_head_id, time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 1) " +
		"					else " +
		"					Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 1, Round(end_date-start_date+1,0)) " +
		"				end) " +
		")v, 1, purpose " +
		"from off_times, fte_heads , year_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= trunc(getTimeId(start_date)/420)*420) " +
		"and (end_date is null or time_id <= getTimeId(end_date)) " +
		"group by fte_head_id, time_id, purpose " +
		"order by fte_head_id, time_id, purpose"
    :
		"select fte_head_id, time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then intelli.getDuration(time_id, 1) " +
		"					else " +
		"					cast((end_date-start_date+1) as Numeric) * intelli.getPropIncluded(5, intelli.getTimeId(start_date), time_id, 1, cast((end_date-start_date+1) as numeric)) " +
		"				end) " +
		")v, 1, purpose " +
		"from off_times, fte_heads , year_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= cast((intelli.getTimeId(start_date)/420) as int)*420) " +
		"and (end_date is null or time_id <= intelli.getTimeId(end_date)) " +
		"group by fte_head_id, time_id, purpose " +
		"order by fte_head_id, time_id, purpose";

	public static String UPDATE_PORTRSET_WITH_ORIG_ALL_PRJ = "update port_results upd set (ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id) "+
		" = (select ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id from port_results, port_rset where port_rset.port_wksp_id = ? and is_auto_updateable=1 and port_results.port_rs_id = port_rset.id and alt_id=upd.alt_id and prj_id=upd.prj_id and rownum <=1) "+
		" where port_rs_id = ? and prj_id=? ";

	//(case when (? is null) then ('Copy Of ' || name) else ? end)
	public static String UPDATE_TASK_CLASSIFY = "update alt_work_items "+
		"set classify1 = (case when ? is null then classify1 else ? end), "+
		"classify2 = (case when ? is null then classify2 else ? end), "+
		"classify3 = (case when ? is null then classify3 else ? end), "+
		"classify4 = (case when ? is null then classify4 else ? end), "+
		"classify5 = (case when ? is null then classify5 else ? end) "+
		" where alt_work_items.internal_id = ? and alt_work_id = ?";
	public static String UPDATE_TASK_FTE_ITEM_CLASSIFY = "update fte_items "+
		"set classify1 = (case when ? is null then classify1 else ? end), "+
		"classify2 = (case when ? is null then classify2 else ? end), "+
		"classify3 = (case when ? is null then classify3 else ? end), "+
		"classify4 = (case when ? is null then classify4 else ? end), "+
		"classify5 = (case when ? is null then classify5 else ? end) "+
		" where task_internal_id = ? and alt_fte_id = ?";
	public static String UPDATE_FTE_ITEM_CLASSIFY = "update fte_items "+
		"set fte_head_id = (case when ? is null then fte_head_id else ? end), "+
		"classify1 = (case when ? is null then classify1 else ? end), "+
		"classify2 = (case when ? is null then classify2 else ? end), "+
		"classify3 = (case when ? is null then classify3 else ? end), "+
		"classify4 = (case when ? is null then classify4 else ? end), "+
		"classify5 = (case when ? is null then classify5 else ? end) "+
		" where alt_fte_id = ? and "+ //the classify clauses will come later
		" task_internal_id = ? and "+
		" fte_head_id = ? ";
//Not used
	public final static String COPY_CREATE_PJ_BASIC =  
		"insert into pj_basics (id, wspace_id, prj_id, str_field5, str_field2, num_field1, int_field10, unmet_need, str_field1, num_field14, int_field6, name, "+
		"str_field9, str_field3, num_field11, num_field4, int_field13, int_field5, n_lf_ext, maj_fran, pj_basic_desc, "+
		"str_field11, int_field11, thera_ind, num_field9, str_field6, num_field7, num_field3, int_field15, "+
		"treat_ty, num_field5, int_field7, int_field3, num_field10, treat_mech, maj_cust_seg, version_id, "+
		"num_field2, int_field14, int_field8, int_field1, str_field12, num_field12, num_field6, int_field2, "+
		"source, min_fran, disease, str_field7, int_field9, str_field10, str_field8, str_field4, num_field8, "+
		"int_field4, int_field12, num_field13, base_drug, date_field1, date_field2, date_field3, date_field4, date_field5) "+
		"(select ?, wspace_id, pj_basics.prj_id,  str_field5, str_field2, num_field1, int_field10, unmet_need, str_field1, num_field14, int_field6, name, "+
		"str_field9, str_field3, num_field11, num_field4, int_field13, int_field5, n_lf_ext, maj_fran, pj_basic_desc, "+
		"str_field11, int_field11, thera_ind, num_field9, str_field6, num_field7, num_field3, int_field15, "+
		"treat_ty, num_field5, int_field7, int_field3, num_field10, treat_mech, maj_cust_seg, version_id, "+
		"num_field2, int_field14, int_field8, int_field1, str_field12, num_field12, num_field6, int_field2, "+
		"source, min_fran, disease, str_field7, int_field9, str_field10, str_field8, str_field4, num_field8, "+
		"int_field4, int_field12, num_field13, base_drug, date_field1, date_field2, date_field3, date_field4, date_field5 "+
		"from pj_basics, port_results where port_results.port_rs_id = ? and alt_id = ? and pj_basics.id = port_results.ver_prj_basic_id)";

	// Sameer 06/28/2005
	public static String GET_PRJ_LOCK_STATUS_FOR_USER_QUERY =
		"select prj_lock_status.currentowner, users.name from prj_lock_status, users " +
		"where users.id = prj_lock_status.currentowner " +
		"and wspace_id = ? " +
		"and alt_id = ? " +
		"and type = ? ";

	public static String DELETE_PRJ_LOCK_STATUS_QUERY =
		"delete from prj_lock_status where " +
		"wspace_id = ? " +
		"and alt_id = ? " +
		"and type = ? ";

	public static String SET_PRJ_LOCK_STATUS_FOR_USER_QUERY = Misc.G_DO_ORACLE ? 
  "insert into prj_lock_status (wspace_id, type, datetime, alt_id, currentowner) " +
	"values (?, ?, sysdate, ?, ?)"
    :
		"insert into prj_lock_status (wspace_id, type, datetime, alt_id, currentowner) " +
		"values (?, ?, getdate(), ?, ?)";

	public static String INSERT_PRJ_FLOW_HIST_QUERY = Misc.G_DO_ORACLE ?
  "insert into prj_flow_hist (wspace_id, alt_id, type, action, action_by, action_time) " +
		"values (?, ?, ?, ?, ?, sysdate)"
    :
		"insert into prj_flow_hist (wspace_id, alt_id, type, action, action_by, action_time) " +
		"values (?, ?, ?, ?, ?, getdate())";

	public static String GET_PRJ_FLOW_HIST_QUERY =
		"select prj_flow_hist.action, prj_flow_hist.action_by, prj_flow_hist.action_time, " +
		"users.name from prj_flow_hist, users " +
		"where " +
		"users.id = prj_flow_hist.action_by " +
		"and wspace_id = ? " +
		"and alt_id = ? " +
		"and type = ? " +
		"order by prj_flow_hist.action_time desc";

	public final static String DEL_PRJ_LOCK_STATUS = "delete from prj_lock_status where alt_id=? and wspace_id=?";
	public final static String UPDATE_PRJ_LOCK_STATUS_HIST = Misc.G_DO_ORACLE ?
  "insert into prj_flow_hist (wspace_id, alt_id, type, action, action_by, action_time) values (?,?,?,?,?,sysdate)"
  :
  "insert into prj_flow_hist (wspace_id, alt_id, type, action, action_by, action_time) values (?,?,?,?,?,getdate())";

	//080605 rajeev ... to support arbitrary measures//$$$$$$$
	public static String GET_CUSTOM_UNTOUCH_NONREVDEV_QTR_1 =
		//"select "+ //sel list + optional ","
    Misc.G_DO_ORACLE ?
    "time_id,  sum(measure_data.val*intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 0, measure_data.val_dur)), 0 "+
		"from "
    :
		"time_id,  sum(measure_data.val* intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 0, measure_data.val_dur)), 0 "+
		"from ";
	public static String GET_CUSTOM_UNTOUCH_NONREVDEV_QTR_2 =
		"qtr_time_id ";
	public static String GET_CUSTOM_UNTOUCH_NONREVDEV_ANY_3 =
  Misc.G_DO_ORACLE ? 
  ",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 minus select alt_id from port_results where port_rs_id=?) altList "+
		" where "+
		"port_results.port_rs_id = ? "+
		"and port_results.fund_status = 1 "+
		"and port_results.alt_id = altList.alt_id "+
		" and time_id >= ? "
    :
		",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 and Not Exists( select alt_id from port_results where port_rs_id=?)) altList "+
		" where "+
		"port_results.port_rs_id = ? "+
		"and port_results.fund_status = 1 "+
		"and port_results.alt_id = altList.alt_id "+
		" and time_id >= ? ";


	public static String GET_CUSTOM_UNTOUCH_REV_QTR_1 =
		//"select "+ //sel list + optional ","
		Misc.G_DO_ORACLE ?
    "time_id,  sum(measure_data.val*prof_outcomes.probability*milestones.cumm_prob*intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 0, measure_data.val_dur)), 0 "+
		"from "
    :
    "time_id,  sum(measure_data.val*prof_outcomes.probability*milestones.cumm_prob* intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 0, measure_data.val_dur)), 0 "+
		"from ";
	public static String GET_CUSTOM_UNTOUCH_REV_QTR_2 =
		"qtr_time_id, milestones, prof_outcomes ";
	public static String GET_CUSTOM_UNTOUCH_REV_ANY_3 = 
  Misc.G_DO_ORACLE ?
  ",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 minus select alt_id from port_results where port_rs_id=?) altList "+
		" where "+
		"port_results.port_rs_id = ? "+
		"and port_results.fund_status = 1 "+
		"and port_results.alt_id = altList.alt_id "+
		"and port_results.ver_alt_mstone_id = milestones.alt_date_id "+
		" and prof_outcomes.alt_profil_id = port_results.ver_alt_profile_id "+
		" and time_id >= ? "+
		" and milestones.mstn_id = ? "+
		" and milestones.target_market = measure_case_index.target_market "+
		" and prof_outcomes.profe_case_ty = measure_case_index.outcome_or_phase_id "
    :
		",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 and Not Exists( select alt_id from port_results where port_rs_id=?)) altList "+
		" where "+
		"port_results.port_rs_id = ? "+
		"and port_results.fund_status = 1 "+
		"and port_results.alt_id = altList.alt_id "+
		"and port_results.ver_alt_mstone_id = milestones.alt_date_id "+
		" and prof_outcomes.alt_profil_id = port_results.ver_alt_profile_id "+
		" and time_id >= ? "+
		" and milestones.mstn_id = ? "+
		" and milestones.target_market = measure_case_index.target_market "+
		" and prof_outcomes.profe_case_ty = measure_case_index.outcome_or_phase_id ";



	public static String GET_CUSTOM_UNTOUCH_DEVCOST_MONTH_1 = //"select "+
		//group by clauses
    Misc.G_DO_ORACLE ? 
    "time_id "+
		",sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 2, measure_data.val_dur) * milestones.prev_cumm_prob) "+
		",2 "+
		"from "
    :
		"time_id "+
		",sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 2, measure_data.val_dur) * milestones.prev_cumm_prob) "+
		",2 "+
		"from ";

	//measure specific table
	//  ",fte_items "+
	public static String GET_CUSTOM_UNTOUCH_DEVCOST_MONTH_2 =
		",month_timeid "+
		",milestones ";
	//join by tables

	public static String GET_CUSTOM_UNTOUCH_DEVCOST_ANY_3 =
  Misc.G_DO_ORACLE ?
  ",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 minus select alt_id from port_results where port_rs_id=?) altList "+
		"where "+
		"port_results.port_rs_id = ? "+ //the curr one
		"and port_results.fund_status = 1 "+
		"and port_results.alt_id = altList.alt_id "+
		"and milestones.alt_date_id = port_results.ver_alt_mstone_id "+
		"and milestones.mstn_id = measure_case_index.outcome_or_phase_id "+
		"and milestones.target_market = measure_case_index.target_market "
    :
		",(select alt_id from port_results where port_results.port_rs_id = ? and fund_status = 1 and Not Exists( select alt_id from port_results where port_rs_id=?)) altList "+
		"where "+
		"port_results.port_rs_id = ? "+ //the curr one
		"and port_results.fund_status = 1 "+
		"and port_results.alt_id = altList.alt_id "+
		"and milestones.alt_date_id = port_results.ver_alt_mstone_id "+
		"and milestones.mstn_id = measure_case_index.outcome_or_phase_id "+
		"and milestones.target_market = measure_case_index.target_market ";
	//group by specific join clause
	public static String GET_CUSTOM_UNTOUCH_ANY_MONTH_5 =
		Misc.G_DO_ORACLE ?
    "and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= trunc(measure_data.time_val/35)*35 "+
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"    when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		"end) "+
		"group by "
    :
    //val_scope specific condition
		"and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= cast((measure_data.time_val/35) as int)*35 "+
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"    when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		"end) "+
		"group by ";
	public static String GET_CUSTOM_UNTOUCH_ANY_ANY_6 = //rajeev 081205 ... changed name
		//group by clauses
		"time_id "+
		"order by ";
	public static String GET_CUSTOM_UNTOUCH_ANY_ANY_7 = //rajeev 081205 ... changed name
		//group by clauses
		"time_id ";

	public static String GET_CUSTOM_UNTOUCH_DEVCOST_QTR_1 = //"select "+
		//group by clauses
    Misc.G_DO_ORACLE ?
    "time_id "+
		",sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 0, measure_data.val_dur) * milestones.prev_cumm_prob) "+
		",0 "+
		"from "
    :
		"time_id "+
		",sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 0, measure_data.val_dur) * milestones.prev_cumm_prob) "+
		",0 "+
		"from ";

	//measure specific table
	//  ",fte_items "+
	public static String GET_CUSTOM_UNTOUCH_DEVCOST_QTR_2 =
		",qtr_timeid "+
		",milestones ";
	public static String GET_CUSTOM_UNTOUCH_ANY_QTR_5 =
  Misc.G_DO_ORACLE ?
  "and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= trunc(measure_data.time_val/105)*105 "+
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"    when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		"end) "+
		"group by "
    :
		//val_scope specific condition
		"and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= cast((measure_data.time_val/105) as int)*105 "+
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"    when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		"end) "+
		"group by ";

	//join by tables
	public static String GET_CUSTOM_UNTOUCH_DEVCOST_WEEK_1 = //"select "+
		//group by clauses
    Misc.G_DO_ORACLE ?
    "time_id "+
		",sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 3, measure_data.val_dur) * milestones.prev_cumm_prob) "+
		",3 "+
		"from "
    :
		"time_id "+
		",sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 3, measure_data.val_dur) * milestones.prev_cumm_prob) "+
		",3 "+
		"from ";

	//measure specific table
	//  ",fte_items "+
	public static String GET_CUSTOM_UNTOUCH_DEVCOST_WEEK_2 =
		",week_timeid "+
		",milestones ";
	//join by tables

	public static String GET_CUSTOM_UNTOUCH_ANY_WEEK_5 =
		//val_scope specific condition
		"and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= measure_data.time_val-12 "+
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"    when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		"end) "+
		"group by ";
	public static String GET_CUSTOM_UNTOUCH_DEVCOST_YEAR_1 = //"select "+
		//group by clauses
    Misc.G_DO_ORACLE ?
    "time_id "+
		",sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 1, measure_data.val_dur) * milestones.prev_cumm_prob) "+
		",1 "+
		"from "
    :
		"time_id "+
		",sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 1, measure_data.val_dur) * milestones.prev_cumm_prob) "+
		",1 "+
		"from ";

	//measure specific table
	//  ",fte_items "+
	public static String GET_CUSTOM_UNTOUCH_DEVCOST_YEAR_2 =
		",year_timeid "+
		",milestones ";
	//join by tables

	public static String GET_CUSTOM_UNTOUCH_ANY_YEAR_5 =
		//val_scope specific condition
    Misc.G_DO_ORACLE ? 
    "and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= trunc(measure_data.time_val/420)*420 "+
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"    when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		"end) "+
		"group by "
    :
		"and time_id >= ? "+
		"and time_id <= ? "+
		"and time_id >= cast((measure_data.time_val/420) as int)*420 "+
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"    when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		"end) "+
		"group by ";

//TODO_INQUERY
	public static String CLEAN_PORT_RESULTS_MEASURE = "delete from port_results_measure where port_rs_id=?";
//TODO_INQUERY  
	public static String PREP_PORT_POP_DESIRED_MEASURE = "insert into port_results_measure (port_rs_id, alt_id, prj_id "+
		", measure_id "+
		", alt_measure_id "+
		") "+
		"( "+
		"select ?, measure_map_items.alt_id, port_wksp_asso.prj_id "+
		", measure_map_items.measure_id "+
		", measure_map_items.alt_measure_id "+
		"from measure_map_items, alternatives, labels, port_wksp_asso "+
		"where "+
		"port_wksp_asso.port_wksp_id = ? "+
		"and port_wksp_asso.use_latest = 0 "+
		"and alternatives.prj_id = port_wksp_asso.prj_id "+
		"and alternatives.id = measure_map_items.alt_id "+
		"and measure_map_items.label_id = port_wksp_asso.label_id "+
		"union "+
		"( "+
		"select ?,  measure_map_items.alt_id, port_wksp_asso.prj_id  "+
		", measure_map_items.measure_id "+
		", measure_map_items.alt_measure_id "+
		"from measure_map_items, alternatives, port_wksp_asso "+
		"where port_wksp_asso.port_wksp_id = ? "+
		"and port_wksp_asso.map_type in (1,2,7) "+
		"and port_wksp_asso.use_latest = 1 "+
		"and alternatives.prj_id = port_wksp_asso.prj_id "+
		"and measure_map_items.map_type = port_wksp_asso.map_type "+
		"and measure_map_items.isdefault = 1 "+
		"and measure_map_items.alt_id = alternatives.id "+
		") "+
		"union "+
		"( "+
		"select ?,  measure_map_items.alt_id, port_wksp_asso.prj_id  "+
		", measure_map_items.measure_id "+
		", measure_map_items.alt_measure_id "+
		"from measure_map_items, alternatives,  port_wksp_asso "+
		"where port_wksp_asso.port_wksp_id = ? "+
		"and not(port_wksp_asso.map_type in (1,2,7)) "+
		"and port_wksp_asso.use_latest = 1 "+
		"and alternatives.prj_id = port_wksp_asso.prj_id "+
		"and measure_map_items.wspace_id = port_wksp_asso.wspace_id "+
		"and measure_map_items.isdefault = 1 "+
		"and measure_map_items.alt_id = alternatives.id "+
		") "+
		") ";


	public static String GET_OTHER_MEASURE_BY_QTR_1 = 
  
  "select port_results_measure.measure_id, port_results_measure.alt_id, target_market, outcome_or_phase_id, break_down, time_id "+
		",sum(measure_data.val * intelli.getPropIncludedSimpleCurrency(measure_data.val_scope, measure_data.time_val, time_id, ?, measure_data.val_dur, alt_measures.currency_id, 0, ?)), ? "+
		", classify1, classify2, classify3, classify4, classify5, scen_id "+
		"from port_results_measure, alt_measures, measure_data, measure_case_index, ";//+ qtr_timeid "+
		
    
    
	public static String GET_OTHER_MEASURE_BY_QTR_2 =
     Misc.G_DO_ORACLE ?
    //" where port_results_measure.port_rs_id = ? "+ in Query Construct itself
    "and measure_data.alt_measure_id = port_results_measure.alt_measure_id and alt_measures.id = port_results_measure.alt_measure_id "+
		"and measure_data.measure_case_index_id = measure_case_index.id "+
		"and time_id >= ? "+		
    "and time_id <= ? "+    
		"and time_id >= trunc(measure_data.time_val/?)*? "+
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"   when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by port_results_measure.measure_id, port_results_measure.alt_id,  measure_case_index.scen_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, time_id "+
		"order by port_results_measure.measure_id, port_results_measure.alt_id,  measure_case_index.scen_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, time_id "    
    :
    //" where port_results_measure.port_rs_id = ? "+ in Query Construct itself
		"and measure_data.alt_measure_id = port_results_measure.alt_measure_id  and alt_measures.id = port_results_measure.alt_measure_id "+
		"and measure_data.measure_case_index_id = measure_case_index.id "+
		"and time_id >= ? "+		
    "and time_id <= ? "+    
		"and time_id >= cast((measure_data.time_val/?) as int)*? "+
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"   when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by port_results_measure.measure_id, port_results_measure.alt_id,  measure_case_index.scen_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, time_id "+
		"order by port_results_measure.measure_id, port_results_measure.alt_id,  measure_case_index.scen_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, time_id ";

	public static String GET_MEASURE_DATA_BY_YEAR_PART1 =
//sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur))
//		"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+

"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val * intelli.getPropIncludedSimpleCurrency(measure_data.val_scope, measure_data.time_val, time_id, 1, measure_data.val_dur, alt_measures.currency_id, ?, ?)), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"from alt_measures, measure_data, measure_case_index, year_timeid "+
		"where alt_measures.id = ? and measure_data.alt_measure_id = alt_measures.id "+
		"and measure_data.measure_case_index_id = measure_case_index.id ";
//    : MS part ... not different
//"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 1, measure_data.val_dur)), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
//		"from measure_data, measure_case_index, year_timeid "+
//		"where measure_data.alt_measure_id = ? "+
//		"and measure_data.measure_case_index_id = measure_case_index.id ";
     public static String GET_MEASURE_DATA_BY_YEAR_PART2 =
     Misc.G_DO_ORACLE ?
     " and time_id >= trunc(measure_data.time_val/420)*420 "+
		" and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"   when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		" end) "+
		"group by measure_case_index.id,name, time_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"order by scen_id, measure_case_index.id, name, target_market, break_down, outcome_or_phase_id, classify1, classify2, classify3, classify4, classify5, time_id"
    :
		" and time_id >= cast((measure_data.time_val/420) as int)*420 "+
		" and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"   when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		" end) "+
		"group by measure_case_index.id,name, time_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"order by scen_id, measure_case_index.id, name, target_market, break_down, outcome_or_phase_id, classify1, classify2, classify3, classify4, classify5, time_id";
	public static String GET_MEASURE_DATA_BY_QTR_PART1 =
//sum(data.value * getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur))
//		"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+

"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val * intelli.getPropIncludedSimpleCurrency(measure_data.val_scope, measure_data.time_val, time_id, 0, measure_data.val_dur, alt_measures.currency_id, ?, ?)), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"from alt_measures, measure_data, measure_case_index, qtr_timeid "+
		"where alt_measures.id = ? and measure_data.alt_measure_id = alt_measures.id "+
		"and measure_data.measure_case_index_id = measure_case_index.id ";
//    :
//		"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 0, measure_data.val_dur)), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
//		"from measure_data, measure_case_index, qtr_timeid "+
//		"where measure_data.alt_measure_id = ? "+
//		"and measure_data.measure_case_index_id = measure_case_index.id ";
     public static String GET_MEASURE_DATA_BY_QTR_PART2 = 
     Misc.G_DO_ORACLE ?
     " and time_id >= trunc(measure_data.time_val/105)*105 "+
		" and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"   when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		" end) "+

		"group by measure_case_index.id,name, time_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"order by scen_id, measure_case_index.id, name, target_market, break_down, outcome_or_phase_id, classify1, classify2, classify3, classify4, classify5, time_id"
    :
		" and time_id >= cast((measure_data.time_val/105) as int)*105 "+
		" and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"   when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		" end) "+

		"group by measure_case_index.id,name, time_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"order by scen_id, measure_case_index.id, name, target_market, break_down, outcome_or_phase_id, classify1, classify2, classify3, classify4, classify5, time_id";
	public static String GET_MEASURE_DATA_BY_MONTH_PART1 =
//sum(data.value * getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur))
//		"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+

"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val * intelli.getPropIncludedSimpleCurrency(measure_data.val_scope, measure_data.time_val, time_id, 2, measure_data.val_dur, alt_measures.currency_id,?,?)), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"from alt_measures, measure_data, measure_case_index, month_timeid "+
		"where alt_measures.id = ? and measure_data.alt_measure_id = alt_measures.id "+
		"and measure_data.measure_case_index_id = measure_case_index.id ";
//    :
//		"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 2, measure_data.val_dur)), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
//		"from measure_data, measure_case_index, month_timeid "+
//		"where measure_data.alt_measure_id = ? "+
//		"and measure_data.measure_case_index_id = measure_case_index.id ";
     public static String GET_MEASURE_DATA_BY_MONTH_PART2 =
     Misc.G_DO_ORACLE ?
     " and time_id >= trunc(measure_data.time_val/35)*35 "+
		" and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"   when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		" end) "+
		"group by measure_case_index.id,name, time_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"order by scen_id, measure_case_index.id, name, target_market, break_down, outcome_or_phase_id, classify1, classify2, classify3, classify4, classify5, time_id"
    :
		" and time_id >= cast((measure_data.time_val/35) as int)*35 "+
		" and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 "+
		"    when (measure_data.val_scope=1) then 420 "+
		"   when (measure_data.val_scope=2) then 35 "+
		"    when (measure_data.val_scope=3) then 12 "+
		"    when (measure_data.val_scope=5) then measure_data.val_dur*1.26 "+
		"    else 1 "+
		" end) "+
		"group by measure_case_index.id,name, time_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"order by scen_id, measure_case_index.id, name, target_market, break_down, outcome_or_phase_id, classify1, classify2, classify3, classify4, classify5, time_id";

	public static String GET_MEASURE_SUMM_INFO =
		"select alt_measures.id, alt_measures.alt_model_id_excel_load from alt_measures,  measure_map_items where measure_map_items.alt_id = ? and measure_map_items.measure_id = ? and measure_map_items.wspace_id = ?  and measure_map_items.isdefault = 1  and alt_measures.id = measure_map_items.alt_measure_id";

	public static String GET_MEASURE_SUMM_INFO_VER =
		"select alt_measures.id, alt_measures.alt_model_id_excel_load from alt_measures where id = ?";

	public static String CREATE_MEASURE_CASE_INDEX = Misc.G_DO_ORACLE ?
		"insert into measure_case_index (id, name, measure_id, break_down, target_market, outcome_or_phase_id, classify1, classify2, classify3, classify4, classify5, scen_id) values (?,?,?,?,?,?,?,?,?,?,?,?)"
    :
    "insert into measure_case_index (name, measure_id, break_down, target_market, outcome_or_phase_id, classify1, classify2, classify3, classify4, classify5, scen_id) values (?,?,?,?,?,?,?,?,?,?,?)";

	public static String INSERT_MEASURE_DATA_NEW  = 
  "insert into measure_data (measure_case_index_id, alt_measure_id, val, time_val, val_scope, val_dur) values (?,?,?,?,?,?)";


	public static String COPY_PJ_BASIC_WITH_CURR = Misc.G_DO_ORACLE ? //081808 ... changed
		"insert into pj_basics (id, wspace_id, prj_id, str_field5, str_field2, num_field1, int_field10, unmet_need, str_field1, num_field14, int_field6, name, " +
		"str_field9, str_field3, num_field11, num_field4, int_field13, int_field5, n_lf_ext, maj_fran, pj_basic_desc, " +
		"str_field11, int_field11, thera_ind, num_field9, str_field6, num_field7, num_field3, int_field15, " +
		"treat_ty, num_field5, int_field7, int_field3, num_field10, treat_mech, maj_cust_seg, version_id, " +
		"num_field2, int_field14, int_field8, int_field1, str_field12, num_field12, num_field6, int_field2, " +
		"source, min_fran, disease, str_field7, int_field9, str_field10, str_field8, str_field4, num_field8, " +
		"int_field4, int_field12, num_field13, base_drug, date_field1, date_field2, date_field3, date_field4, date_field5 " +
	",is_validated, last_validated_on, last_validated_by, str_field13, str_field14, str_field15, str_field16, str_field17 " +
	",int_field16, int_field17, int_fieldxx1, int_fieldxx2, int_fieldxx3, int_fieldxx4, date_field6, date_field7, date_field8, date_field9, date_field10 " +
	") " +
		"(select ?, pj_map_items.wspace_id, pj_basics.prj_id,  str_field5, str_field2, num_field1, int_field10, unmet_need, str_field1, num_field14, int_field6, name, " +
		"str_field9, str_field3, num_field11, num_field4, int_field13, int_field5, n_lf_ext, maj_fran, pj_basic_desc, " +
		"str_field11, int_field11, thera_ind, num_field9, str_field6, num_field7, num_field3, int_field15, " +
		"treat_ty, num_field5, int_field7, int_field3, num_field10, treat_mech, maj_cust_seg, version_id, " +
		"num_field2, int_field14, int_field8, int_field1, str_field12, num_field12, num_field6, int_field2, " +
		"source, min_fran, disease, str_field7, int_field9, str_field10, str_field8, str_field4, num_field8, " +
		"int_field4, int_field12, num_field13, base_drug, date_field1, date_field2, date_field3, date_field4, date_field5 " +
	",is_validated, last_validated_on, last_validated_by, str_field13, str_field14, str_field15, str_field16, str_field17 " +
	",int_field16, int_field17, int_fieldxx1, int_fieldxx2, int_fieldxx3, int_fieldxx4, date_field6, date_field7, date_field8, date_field9, date_field10 " +
		"from pj_basics, pj_map_items where pj_map_items.prj_id=? and pj_map_items.wspace_id=? and pj_basics.id = pj_map_items.pj_basic_id and pj_map_items.isdefault=1)"
	:
	"insert into pj_basics (wspace_id, prj_id, str_field5, str_field2, num_field1, int_field10, unmet_need, str_field1, num_field14, int_field6, name, " +
		"str_field9, str_field3, num_field11, num_field4, int_field13, int_field5, n_lf_ext, maj_fran, pj_basic_desc, " +
		"str_field11, int_field11, thera_ind, num_field9, str_field6, num_field7, num_field3, int_field15, " +
		"treat_ty, num_field5, int_field7, int_field3, num_field10, treat_mech, maj_cust_seg, version_id, " +
		"num_field2, int_field14, int_field8, int_field1, str_field12, num_field12, num_field6, int_field2, " +
		"source, min_fran, disease, str_field7, int_field9, str_field10, str_field8, str_field4, num_field8, " +
		"int_field4, int_field12, num_field13, base_drug, date_field1, date_field2, date_field3, date_field4, date_field5 " +
	",is_validated, last_validated_on, last_validated_by, str_field13, str_field14, str_field15, str_field16, str_field17 " +
	",int_field16, int_field17, int_fieldxx1, int_fieldxx2, int_fieldxx3, int_fieldxx4, date_field6, date_field7, date_field8, date_field9, date_field10 " +
	") " +
		"(select pj_map_items.wspace_id, pj_basics.prj_id,  str_field5, str_field2, num_field1, int_field10, unmet_need, str_field1, num_field14, int_field6, name, " +
		"str_field9, str_field3, num_field11, num_field4, int_field13, int_field5, n_lf_ext, maj_fran, pj_basic_desc, " +
		"str_field11, int_field11, thera_ind, num_field9, str_field6, num_field7, num_field3, int_field15, " +
		"treat_ty, num_field5, int_field7, int_field3, num_field10, treat_mech, maj_cust_seg, version_id, " +
		"num_field2, int_field14, int_field8, int_field1, str_field12, num_field12, num_field6, int_field2, " +
		"source, min_fran, disease, str_field7, int_field9, str_field10, str_field8, str_field4, num_field8, " +
		"int_field4, int_field12, num_field13, base_drug, date_field1, date_field2, date_field3, date_field4, date_field5 " +
	",is_validated, last_validated_on, last_validated_by, str_field13, str_field14, str_field15, str_field16, str_field17 " +
	",int_field16, int_field17, int_fieldxx1, int_fieldxx2, int_fieldxx3, int_fieldxx4, date_field6, date_field7, date_field8, date_field9, date_field10 " +
		"from pj_basics, pj_map_items where pj_map_items.prj_id=? and pj_map_items.wspace_id=? and pj_basics.id = pj_map_items.pj_basic_id and pj_map_items.isdefault=1)";

	public static String UPDATE_BASIC_DYN_1  =  "update pj_basics set ";
	public static String UPDATE_BASIC_DYN_2  =  "  where id=?";
	public static String UPDATE_PROJECT_DYN_1 = "update projects set ";
	public static String UPDATE_PROJECT_DYN_2 = " where id=? ";

	public final static String ALT_CRE_NPV_EXT = Misc.G_DO_ORACLE ? "insert into npv (id, npv_type, alt_combined_id, target_market, classify1, classify2, classify3, classify4, classify5, scen_id) values (?,?,?,?,?,?,?,?,?,?)"
                                                                : "insert into npv (npv_type, alt_combined_id, target_market, classify1, classify2, classify3, classify4, classify5, scen_id) values (?,?,?,?,?,?,?,?,?)";
	public final static String GET_DATA_IN_NPV_FROM_PORT_RSET_1 =
//091007		"select port_results.alt_id, npv.npv_type, npv.target_market, npv.classify1, npv.classify2, npv.classify3, npv.classify4, npv.classify5, data.value, npv.scen_id, intelli.getCurrencyConversion(alt_combined_model.currency_id, 0, ?, ?, ?)  from port_results, alt_combined_model, npv, data where "+
		"select port_results.alt_id, npv.npv_type, npv.target_market, npv.classify1, npv.classify2, npv.classify3, npv.classify4, npv.classify5, data.value, npv.scen_id, ?  from port_results, alt_combined_model, npv, data where "+ //the last ? is for backward compatability
		"port_results.port_rs_id = ? and alt_combined_model.id = port_results.ver_alt_combined_id and npv.alt_combined_id = port_results.ver_alt_combined_id and data.npv_id = npv.id and npv.npv_type in (";
	public final static String GET_DATA_IN_NPV_FROM_PORT_RSET_2 = " order by npv.npv_type, port_results.alt_id";

	///////////// 082005
	public final static String GET_MIN_MAX_FOR_MEASURE_DATA = Misc.G_DO_ORACLE ? 
  "select min(measure_data.time_val), max(intelli.getEndOfTimeIdIncl(measure_data.time_val, measure_data.val_scope, measure_data.val_dur)) from measure_data where alt_measure_id=?"
  :
  "select min(measure_data.time_val), max(intelli.getEndOfTimeIdIncl(measure_data.time_val, measure_data.val_scope, measure_data.val_dur)) from measure_data where alt_measure_id=?";
	public final static String GET_MIN_MAX_FOR_REV_DATA =
  Misc.G_DO_ORACLE ?
  "select min(data.year), max(intelli.getEndOfTimeIdIncl(data.year, data.val_scope, data.val_dur)) from data, rev_segs where data.rev_seg_id = rev_segs.id and rev_segs.alt_rev_id = ?"
  :
	"select min(data.year), max(intelli.getEndOfTimeIdIncl(data.year, data.val_scope, data.val_dur)) from data, rev_segs where data.rev_seg_id = rev_segs.id and rev_segs.alt_rev_id = ?";
	public final static String GET_MIN_MAX_FOR_OPCOST_DATA =
  Misc.G_DO_ORACLE ?
  "select min(data.year), max(intelli.getEndOfTimeIdIncl(data.year, data.val_scope, data.val_dur)) from data, cost_items where data.cost_li_id = cost_items.id and cost_items.alt_opcost_id = ?"
  :
	"select min(data.year), max(intelli.getEndOfTimeIdIncl(data.year, data.val_scope, data.val_dur)) from data, cost_items where data.cost_li_id = cost_items.id and cost_items.alt_opcost_id = ?";
	public final static String GET_MIN_MAX_FOR_DEVCOST_DATA =
  Misc.G_DO_ORACLE ?
  "select min(data.year), max(intelli.getEndOfTimeIdIncl(data.year, data.val_scope, data.val_dur)) from data, cost_items where data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ?"
  :
	"select min(data.year), max(intelli.getEndOfTimeIdIncl(data.year, data.val_scope, data.val_dur)) from data, cost_items where data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ?";
	public final static String GET_MIN_MAX_FOR_FTE_DATA =
  Misc.G_DO_ORACLE ?
  "select min(data.year), max(intelli.getEndOfTimeIdIncl(data.year, data.val_scope, data.val_dur)) from data, fte_items where data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ?"
  :
	"select min(data.year), max(intelli.getEndOfTimeIdIncl(data.year, data.val_scope, data.val_dur)) from data, fte_items where data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ?";

	public static String GET_DATA_BY_YEAR_REV_PART1 = //doesn't work for custom dur or off-cycle data.year //changed from seg_id to id
  Misc.G_DO_ORACLE ?
  "select rev_segs.id, rev_segs.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"mkt_type, rev_segs.scen_id, classify1, classify2, classify3, classify4, classify5,0,rev_segs.scen_id "+ //0,0 fillers
		"from data, rev_segs, year_timeid  "+
		"where rev_segs.alt_rev_id = ? "+
		"and data.rev_seg_id = rev_segs.id "
    :
		"select rev_segs.id, rev_segs.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"mkt_type, rev_segs.scen_id, classify1, classify2, classify3, classify4, classify5,0,rev_segs.scen_id "+ //0,0 fillers
		"from data, rev_segs, year_timeid  "+
		"where rev_segs.alt_rev_id = ? "+
		"and data.rev_seg_id = rev_segs.id ";
     public static String GET_DATA_BY_YEAR_REV_PART2 =
     Misc.G_DO_ORACLE ?
     " and time_id >= trunc(data.year/420)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id "+
		"order by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id "
    :
		" and time_id >= cast((data.year/420) as int)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id "+
		"order by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id ";
	public static String GET_DATA_BY_QTR_REV_PART1 = //doesn't work for custom dur or off-cycle data.year
  Misc.G_DO_ORACLE ?
  "select rev_segs.id, rev_segs.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"mkt_type, rev_segs.scen_id, classify1, classify2, classify3, classify4, classify5,0,rev_segs.scen_id "+ //0,0 fillers
		"from data, rev_segs, qtr_timeid  "+
		"where rev_segs.alt_rev_id = ? "+
		"and data.rev_seg_id = rev_segs.id "
    :
		"select rev_segs.id, rev_segs.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"mkt_type, rev_segs.scen_id, classify1, classify2, classify3, classify4, classify5,0,rev_segs.scen_id "+ //0,0 fillers
		"from data, rev_segs, qtr_timeid  "+
		"where rev_segs.alt_rev_id = ? "+
		"and data.rev_seg_id = rev_segs.id ";
     public static String GET_DATA_BY_QTR_REV_PART2 =
     Misc.G_DO_ORACLE ?
     " and time_id >= trunc(data.year/105)*105 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id "+
		"order by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id "
    :
		" and time_id >= cast((data.year/105) as int)*105 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id "+
		"order by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id ";
	public static String GET_DATA_BY_MONTH_REV_PART1 = //doesn't work for custom dur or off-cycle data.year
  Misc.G_DO_ORACLE ?
  "select rev_segs.id, rev_segs.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"mkt_type, rev_segs.scen_id, classify1, classify2, classify3, classify4, classify5,0,rev_segs.scen_id "+ //0,0 fillers
		"from data, rev_segs, month_timeid  "+
		"where rev_segs.alt_rev_id = ? "+
		"and data.rev_seg_id = rev_segs.id "
    :
		"select rev_segs.id, rev_segs.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"mkt_type, rev_segs.scen_id, classify1, classify2, classify3, classify4, classify5,0,rev_segs.scen_id "+ //0,0 fillers
		"from data, rev_segs, month_timeid  "+
		"where rev_segs.alt_rev_id = ? "+
		"and data.rev_seg_id = rev_segs.id ";
     public static String GET_DATA_BY_MONTH_REV_PART2 = 
     Misc.G_DO_ORACLE ?
     " and time_id >= trunc(data.year/35)*35 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id "+
		"order by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id "
    :
		" and time_id >= cast((data.year/35) as int)*35 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id "+
		"order by rev_segs.scen_id, mkt_type, classify1, classify2, classify3, classify4, classify5, rev_segs.id, name, time_id ";

	public static String GET_DATA_BY_YEAR_OPCOST_PART1 = //doesn't work for custom dur or off-cycle data.year
  Misc.G_DO_ORACLE ?
  "select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"target_market, cost_items.scen_id, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id "+
		"from data, cost_items, year_timeid  "+
		"where cost_items.alt_opcost_id = ? "
    :
		"select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"target_market, cost_items.scen_id, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id "+
		"from data, cost_items, year_timeid  "+
		"where cost_items.alt_opcost_id = ? ";

     public static String GET_DATA_BY_YEAR_OPCOST_PART2 =
     Misc.G_DO_ORACLE ?
     "and data.cost_li_id = cost_items.id "+
		" and time_id >= trunc(data.year/420)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "+
		"order by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "
    :
		"and data.cost_li_id = cost_items.id "+
		" and time_id >= cast((data.year/420) as int)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "+
		"order by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id ";

	public static String GET_DATA_BY_QTR_OPCOST_PART1 = //doesn't work for custom dur or off-cycle data.year
  Misc.G_DO_ORACLE ?
  	"select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"target_market, cost_items.scen_id, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id "+
		"from data, cost_items, qtr_timeid  "+
		"where cost_items.alt_opcost_id = ? "
    :
		"select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"target_market, cost_items.scen_id, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id "+
		"from data, cost_items, qtr_timeid  "+
		"where cost_items.alt_opcost_id = ? ";

     public static String GET_DATA_BY_QTR_OPCOST_PART2 =
     Misc.G_DO_ORACLE ?
     "and data.cost_li_id = cost_items.id "+
		" and time_id >= trunc(data.year/105)*105 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "+
		"order by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "
    :
		"and data.cost_li_id = cost_items.id "+
		" and time_id >= cast((data.year/105) as int)*105 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "+
		"order by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id ";
	public static String GET_DATA_BY_MONTH_OPCOST_PART1 = //doesn't work for custom dur or off-cycle data.year
  Misc.G_DO_ORACLE ?
  "select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"target_market, cost_items.scen_id, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id "+
		"from data, cost_items, month_timeid  "+
		"where cost_items.alt_opcost_id = ? "
    :
		"select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"target_market, cost_items.scen_id, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id "+
		"from data, cost_items, month_timeid  "+
		"where cost_items.alt_opcost_id = ? ";

     public static String GET_DATA_BY_MONTH_OPCOST_PART2 =
		Misc.G_DO_ORACLE ?
    "and data.cost_li_id = cost_items.id "+
		" and time_id >= trunc(data.year/35)*35 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "+
		"order by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "
    :
    "and data.cost_li_id = cost_items.id "+
		" and time_id >= cast((data.year/35) as int)*35 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "+
		"order by cost_items.scen_id, target_market, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id ";

//TO CHECK WHY THIS SHOULD NOT id ... Mittal (was cost_items.lineitem_id
	public static String GET_DATA_BY_YEAR_DEVCOST_PART1 =
  Misc.G_DO_ORACLE ?
  	"select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id, task_internal_id  "+
		"from data, cost_items, year_timeid  "+
		"where cost_items.alt_devcost_id = ? "
    :
		"select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id, task_internal_id  "+
		"from data, cost_items, year_timeid  "+
		"where cost_items.alt_devcost_id = ? ";

     public static String GET_DATA_BY_YEAR_DEVCOST_PART2 =
     Misc.G_DO_ORACLE ?
     "and data.cost_li_id = cost_items.id "+
		" and time_id >= trunc(data.year/420)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

		"group by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id, task_internal_id "+
		"order by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "
    :
		"and data.cost_li_id = cost_items.id "+
		" and time_id >= cast((data.year/420) as int)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

		"group by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id, task_internal_id "+
		"order by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id ";

	public static String GET_DATA_BY_QTR_DEVCOST_PART1 =
  Misc.G_DO_ORACLE ?
  "select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id, task_internal_id  "+
		"from data, cost_items, qtr_timeid  "+
		"where cost_items.alt_devcost_id = ? "
    :
		"select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id, task_internal_id  "+
		"from data, cost_items, qtr_timeid  "+
		"where cost_items.alt_devcost_id = ? ";

     public static String GET_DATA_BY_QTR_DEVCOST_PART2 =
     Misc.G_DO_ORACLE ?
     "and data.cost_li_id = cost_items.id "+
		" and time_id >= trunc(data.year/105)*105 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

		"group by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "
    :
		"and data.cost_li_id = cost_items.id "+
    
		" and time_id >= cast((data.year/105) as int)*105 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

		"group by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id ";
	public static String GET_DATA_BY_MONTH_DEVCOST_PART1 =
  Misc.G_DO_ORACLE ?
  "select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id, task_internal_id  "+
		"from data, cost_items, month_timeid  "+
		"where cost_items.alt_devcost_id = ? "
    :
		"select cost_items.id, cost_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_cent_id, cost_items.scen_id, task_internal_id  "+
		"from data, cost_items, month_timeid  "+
		"where cost_items.alt_devcost_id = ? ";

     public static String GET_DATA_BY_MONTH_DEVCOST_PART2 =
     Misc.G_DO_ORACLE ?
     "and data.cost_li_id = cost_items.id "+
		" and time_id >= trunc(data.year/35)*35 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

		"group by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id "
    :
		"and data.cost_li_id = cost_items.id "+
		" and time_id >= cast((data.year/35) as int)*35 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

		"group by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, cost_items.id, name, cost_cent_id, time_id ";

	public static String GET_DATA_BY_YEAR_FTE = //doesn't work for custom dur or off-cycle data.year
  Misc.G_DO_ORACLE ?
  "select fte_items.id, fte_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_head_id, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status, fte_heads.name, task_internal_id "+
		"from data, fte_items, year_timeid, fte_heads  "+
		"where fte_items.alt_fte_id = ? "+
		"and data.fte_item_id = fte_items.id "+
		" and time_id >= trunc(data.year/420)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

          "and fte_items.fte_head_id = fte_heads.id "+
		"group by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name "
    :
		"select fte_items.id, fte_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_head_id, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status, fte_heads.name, task_internal_id "+
		"from data, fte_items, year_timeid, fte_heads  "+
		"where fte_items.alt_fte_id = ? "+
		"and data.fte_item_id = fte_items.id "+
		" and time_id >= cast((data.year/420) as int)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

          "and fte_items.fte_head_id = fte_heads.id "+
		"group by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name ";

	public final static String GET_DATA_IN_NPV_BY_CURR_PART1 =
		"select npv.npv_type, npv.target_market, npv.classify1, npv.classify2, npv.classify3, npv.classify4, npv.classify5, data.value, npv.scen_id from npv, data, alt_map_items where "+
		"alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault=1 and npv.alt_combined_id = alt_map_items.alt_combined_id ";
     public final static String GET_DATA_IN_NPV_BY_CURR_PART2 =     " and data.npv_id = npv.id order by npv_type, target_market, classify1, classify2, classify3, classify4, classify5";

	public final static String GET_DATA_IN_NPV_BY_VER_PART1 =
		"select npv.npv_type, npv.target_market, npv.classify1, npv.classify2, npv.classify3, npv.classify4, npv.classify5, data.value, npv.scen_id from npv, data where "+
		"npv.alt_combined_id = ? ";
     	public final static String GET_DATA_IN_NPV_BY_VER_PART2 =     " and data.npv_id = npv.id order by npv_type, target_market, classify1, classify2, classify3, classify4, classify5";

	//082605 - new queries
	public static String GET_DEVCOST_BY_WEEK_1 = 
  
  "select /*+ ordered */ "+
		"port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur, alt_devcost_model.currency_id, 0 , ?)) "+
		",3 "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.target_market, cost_items.scen_id "+
		"from "+
		"port_results "+
    ",alt_devcost_model "+
		",cost_items "+
		",data "+
		",week_timeid "+
		"where "+
		"port_results.port_rs_id = ? ";
  public static String GET_DEVCOST_BY_WEEK_2 =
		"and cost_items.alt_devcost_id = port_results.ver_alt_devcost_id "+
    "and port_results.ver_alt_devcost_id = alt_devcost_model.id "+
		"and data.cost_li_id = cost_items.id "+
		"and cost_items.to_include = 1 "+
		"and week_timeid.time_id >= ? "+
		"and week_timeid.time_id <= ? "+
		"and week_timeid.time_id >= data.year-12 "+
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"week_timeid.time_id "+
		",port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",cost_items.target_market "+
		",cost_items.scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		"order by port_results.alt_id, cost_items.cost_cent_id, "+
		"        cost_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		", week_timeid.time_id ";
	;

	//-- monthly
	public static String GET_DEVCOST_BY_MONTH_1 = 
  "select /*+ ordered */ "+
		"port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncludedSimpleCurrency(data.val_scope, data.year, time_id, 2, data.val_dur, alt_devcost_model.currency_id, 0 , ?)) "+
		",2 "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5,  cost_items.target_market, cost_items.scen_id "+
		"from "+
		"port_results "+
    ",alt_devcost_model "+
		",cost_items "+
		",data "+
		",month_timeid "+
		"where "+
		"port_results.port_rs_id = ? ";
    
	public static String GET_DEVCOST_BY_MONTH_2 =
  Misc.G_DO_ORACLE ?
  "and cost_items.alt_devcost_id = port_results.ver_alt_devcost_id "+
  "and port_results.ver_alt_devcost_id = alt_devcost_model.id "+
		"and data.cost_li_id = cost_items.id "+
		"and cost_items.to_include = 1 "+
		"and month_timeid.time_id >= ? "+
		"and month_timeid.time_id <= ? "+
		"and month_timeid.time_id >= trunc(data.year/35)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"month_timeid.time_id "+
		",port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		"order by port_results.alt_id, cost_items.cost_cent_id, "+
		"         cost_items.for_achieving_milestone "+
		",target_market  "+
		",scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		", month_timeid.time_id "
	:

		"and cost_items.alt_devcost_id = port_results.ver_alt_devcost_id "+
    "and port_results.ver_alt_devcost_id = alt_devcost_model.id "+    
		"and data.cost_li_id = cost_items.id "+
		"and cost_items.to_include = 1 "+
		"and month_timeid.time_id >= ? "+
		"and month_timeid.time_id <= ? "+
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"    when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"month_timeid.time_id "+
		",port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		"order by port_results.alt_id, cost_items.cost_cent_id, "+
		"         cost_items.for_achieving_milestone "+
		",target_market  "+
		",scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		", month_timeid.time_id ";
	;

	//-- quarterly
	public static String GET_DEVCOST_BY_QTR_1 = 

  "select /*+ ordered */ "+
		"port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncludedSimpleCurrency(data.val_scope, data.year, time_id, 0, data.val_dur, alt_devcost_model.currency_id, 0 , ?)) "+
		",0 "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.target_market, cost_items.scen_id "+
		"from "+
		"port_results "+
    ",alt_devcost_model "+
		",cost_items "+
		",data "+
		",qtr_timeid "+
		"where "+
		"port_results.port_rs_id = ? ";
	public static String GET_DEVCOST_BY_QTR_2 =
  Misc.G_DO_ORACLE ?
"and cost_items.alt_devcost_id = port_results.ver_alt_devcost_id "+
"and port_results.ver_alt_devcost_id = alt_devcost_model.id "+
		"and data.cost_li_id = cost_items.id "+
		"and cost_items.to_include = 1 "+
		"and qtr_timeid.time_id >= ? "+
		"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"qtr_timeid.time_id "+
		",port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		"order by port_results.alt_id, cost_items.cost_cent_id, "+
		"         cost_items.for_achieving_milestone "+
		", target_market "+
		", scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		", qtr_timeid.time_id "
		:
    "and cost_items.alt_devcost_id = port_results.ver_alt_devcost_id "+
    "and port_results.ver_alt_devcost_id = alt_devcost_model.id "+    
		"and data.cost_li_id = cost_items.id "+
		"and cost_items.to_include = 1 "+
		"and qtr_timeid.time_id >= ? "+
		"and qtr_timeid.time_id <= ? "+
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 "+
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"qtr_timeid.time_id "+
		",port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		"order by port_results.alt_id, cost_items.cost_cent_id, "+
		"         cost_items.for_achieving_milestone "+
		", target_market "+
		", scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		", qtr_timeid.time_id "
		;

	//--year
	public static String GET_DEVCOST_BY_YEAR_1 = 
  
  "select /*+ ordered */ "+
		"port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",time_id "+
		",sum(data.value * intelli.getPropIncludedSimpleCurrency(data.val_scope, data.year, time_id, 1, data.val_dur, alt_devcost_model.currency_id, 0 , ?)) "+
		",1 "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.target_market, cost_items.scen_id "+
		"from "+
		"port_results "+
    ",alt_devcost_model "+
		",cost_items "+
		",data "+
		",year_timeid "+
		"where "+
		"port_results.port_rs_id = ? ";
  public static String GET_DEVCOST_BY_YEAR_2 = 
  Misc.G_DO_ORACLE ?
  "and cost_items.alt_devcost_id = port_results.ver_alt_devcost_id "+
  "and port_results.ver_alt_devcost_id = alt_devcost_model.id "+
		"and cost_items.to_include = 1 "+
		"and data.cost_li_id = cost_items.id "+
		"and year_timeid.time_id >= ? "+
		"and year_timeid.time_id <= ? "+
		"and year_timeid.time_id >= trunc(data.year/420)*420 "+
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"year_timeid.time_id "+
		",port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.scen_id "+
		"order by port_results.alt_id,  cost_items.cost_cent_id, "+
		"         cost_items.for_achieving_milestone "+
		"         ,target_market "+
		", scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		",year_timeid.time_id "
		:
		"and cost_items.alt_devcost_id = port_results.ver_alt_devcost_id "+
    "and port_results.ver_alt_devcost_id = alt_devcost_model.id "+    
		"and cost_items.to_include = 1 "+
		"and data.cost_li_id = cost_items.id "+
		"and year_timeid.time_id >= ? "+
		"and year_timeid.time_id <= ? "+
		"and year_timeid.time_id >= cast((data.year/420) as int)*420 "+
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by "+
		"year_timeid.time_id "+
		",port_results.alt_id "+
		",cost_items.cost_cent_id "+
		",cost_items.for_achieving_milestone "+
		",target_market "+
		",scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.scen_id "+
		"order by port_results.alt_id,  cost_items.cost_cent_id, "+
		"         cost_items.for_achieving_milestone "+
		"         ,target_market "+
		", scen_id "+
		",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 "+
		",year_timeid.time_id "
		;

	public static String GET_PORT_REV_BY_QTR_SCHERING_HACK_1 =
		"select port_results.alt_id, mkt_type, data.year, alt_scen_list.delay_val, " +
		"rev_segs.scen_id, " +
		"sum(data.value), " +
		"data.val_scope, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5 from port_results, alt_scen_list, rev_segs, data where "+
		"port_results.port_rs_id = ? and ";
	public static String GET_PORT_REV_BY_QTR_SCHERING_HACK_2 =
		"port_results.ver_alt_rev_id = rev_segs.alt_rev_id and "+
		"rev_segs.scen_id = alt_scen_list.scen_id and "+
		"port_results.alt_id = alt_scen_list.alt_id and "+
		"data.rev_seg_id = rev_segs.id "+
		"and data.year >= ? "+
		"group by port_results.alt_id, data.year, mkt_type, alt_scen_list.delay_val, rev_segs.scen_id, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5, data.val_scope "+
		"order by port_results.alt_id, delay_val, rev_segs.scen_id, mkt_type, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5,  data.year, data.val_scope";

	//rajeev 081005 .. broke in two to accomode prj_id = ? clause
	public static String GET_PORT_OP_COST_BY_QTR_SCHERING_HACK_1 =
		"select port_results.alt_id, cost_cent_id,  data.year, alt_scen_list.delay_val, cost_items.scen_id, sum(data.value), data.val_scope, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.target_market from port_results, alt_scen_list, cost_items, data where "+
		"port_results.port_rs_id = ? and ";
	public static String GET_PORT_OP_COST_BY_QTR_SCHERING_HACK_2 =
		"port_results.ver_alt_opcost_id = cost_items.alt_opcost_id and "+
		"port_results.alt_id = alt_scen_list.alt_id and cost_items.scen_id = alt_scen_list.scen_id and "+
		"data.cost_li_id = cost_items.id and "+
		"cost_items.to_include = 1 "+
		"and data.year >= ? "+
		//  "and qtr_timeid.time_id <= ? "+
		"group by port_results.alt_id, data.year, cost_cent_id, target_market, alt_scen_list.delay_val, cost_items.scen_id, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, data.val_scope  "+
		"order by port_results.alt_id, delay_val, cost_items.scen_id, cost_cent_id, target_market, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, data.year, data.val_scope ";


	//rajeev 091705 ...
	public static String  CREATE_EMPTY_PJ_BASICS = Misc.G_DO_ORACLE ? "insert into pj_basics (id, prj_id, wspace_id) values (?,?,?)"
                                                                  :"insert into pj_basics (prj_id, wspace_id) values (?,?)";

	//rajeev 122405 ... for portfolio scenario
	public static String PREP_PORT_SCENARIO_DEL_EXTRA_RESULTS = "delete from portfolio_scenario where "+
		"port_rs_id in "+
		"(select port_rset.id from port_rset where port_rset.port_wksp_id = ?) "+
		"and "+
		"not (alt_id in "+
		"(select alt_id from port_desired_ver where port_wksp_id = ?) "+
		") ";

	public static String GET_PORT_SCENARIO_BY_PRJ = "select port_rs_id, portfolio_scenario.alt_id, ms_id, target_market, action, amount, scope from portfolio_scenario, alternatives where portfolio_scenario.port_rs_id = ? and alternatives.prj_id = ? and alternatives.id = portfolio_scenario.alt_id order by alt_id, seq_number";
	public static String GET_PORT_SCENARIO_ALL    = "select port_rs_id, portfolio_scenario.alt_id, ms_id, target_market, action, amount, scope from portfolio_scenario where portfolio_scenario.port_rs_id = ? order by alt_id, seq_number";
	public static String DEL_PORT_SCENARIO        = "delete from portfolio_scenario where port_rs_id = ? and alt_id = ? and seq_number >= ? ";
	public static String INSERT_PORT_SCENARIO     = "insert into portfolio_scenario (port_rs_id, alt_id, ms_id, target_market, action, amount, scope, seq_number) values (?,?,?,?,?,?,?,?)";


	// rajeev 030906
  /*
	public static String MKTG_CAMPAIGN_SUMM_INFO = "select mktg_campaign.code, mktg_campaign.name, mktg_campaign.definition_type, mktg_campaign.offer_id from mktg_campaign "+
		"where mktg_campaign.id = ?";

	public static String MKTG_SCHED_SUMM_INFO = "select mktg_schedule.code, mktg_schedule.name, mktg_campaign.name, mktg_campaign.definition_type from mktg_campaign, mktg_schedule "+
		"where mktg_schedule.id = ? and mktg_campaign.id = mktg_schedule.campaign_id ";

	public static String MKTG_GET_FCST_PARAM = 
  Misc.G_DO_ORACLE ?
  "select campaign_id, sched_id, channel_id, parameter_type, parameter_value, lower_value, upper_value "+
		"from mktg_parameters "+
		"where "+
		"campaign_id = ? "+
		"and (? is null or sched_id is null or ? = sched_id) "+
		"order by "+
		"sched_id asc nulls first "+
		",channel_id asc nulls first "+
		",parameter_type asc"
    :
  "select campaign_id, sched_id, channel_id, parameter_type, parameter_value, lower_value, upper_value "+
		"from mktg_parameters "+
		"where "+
		"campaign_id = ? "+
		"and (? is null or sched_id is null or ? = sched_id) "+
		"order by "+
		"sched_id asc  "+
		",channel_id asc  "+
		",parameter_type asc";

	public static String MKTG_GET_FCST_RAMPUP_PARAM = Misc.G_DO_ORACLE ?
  "select campaign_id, sched_id, channel_id,  time_period, value, lower_value, upper_value, scope "+
		"from mktg_rampup_parameters "+
		"where "+
		"campaign_id = ? "+
		"and (? is null or sched_id is null or ? = sched_id) "+
		"order by "+
		"sched_id asc nulls first "+
		",channel_id asc nulls first "+
		",time_period asc "
    :
    "select campaign_id, sched_id, channel_id,  time_period, value, lower_value, upper_value, scope "+
		"from mktg_rampup_parameters "+
		"where "+
		"campaign_id = ? "+
		"and (? is null or sched_id is null or ? = sched_id) "+
		"order by "+
		"sched_id asc  "+
		",channel_id asc "+
		",time_period asc ";

	public static String MKTG_GET_CHANNEL_FOR_CAMPAIGN = "select channel_id from mktg_campaign_channel where campaign_id = ?";
	public static String MKTG_GET_SCHED_FOR_CAMPAIGN = "select id, code, name from mktg_schedule where campaign_id = ? and status not in (4) order by id asc"; //TODO for DELETED
	public static String MKTG_GET_REF_SCHED_NAME = "select mktg_schedule.id, mktg_schedule.code, mktg_schedule.name from  mktg_schedule where mktg_schedule.id = ?";
	public static String MKTG_GET_REF_CAMPAIGN_NAME = "select mktg_campaign.id, mktg_campaign.code, mktg_campaign.name from  mktg_campaign where mktg_campaign.id = ?";

	public static String MKTG_GET_MATCHING_CAMPAIGN_FOR_CAMPAIGN = "select "+
		"(case when r.purpose = c.purpose then 1 else 0 end) field1 "+
		",(select count(*) from mktg_campaign_channel where "+
		"  mktg_campaign_channel.campaign_id = r.id and channel_id in "+
		"  (select channel_id from mktg_campaign_channel where campaign_id = c.id) "+
		") field2 "+
		",(select count(*) from mktg_customer_attrib rca, mktg_customer_attrib cca where "+
		"  rca.campaign_id = r.id "+
		"  and cca.campaign_id = c.id "+
		"  and rca.attrib_id = cca.attrib_id "+
		"  and rca.attrib_val = cca.attrib_val "+
		") field3 "+
		",(select abs(TO_NUMBER(TO_CHAR(min(rs.start_date), 'MM'))- "+
		"             TO_NUMBER(TO_CHAR(min(cs.start_date), 'MM')) "+
		"            ) from mktg_schedule rs, mktg_schedule cs "+
		"   where rs.campaign_id = r.id "+
		"   and cs.campaign_id = c.id "+
		" ) field4 "+
		",(select abs(max(rs.end_date)-min(rs.start_date) - "+
		"             (max(cs.end_date)-min(cs.start_date)) "+
		"            ) from mktg_schedule rs, mktg_schedule cs "+
		"   where rs.campaign_id = r.id "+
		"   and cs.campaign_id = c.id "+
		" ) field5 "+
		",r.id "+
		",r.code "+
		",r.name "+
		",r.description "+
		"from mktg_campaign r, mktg_campaign c "+
		"where c.id = ? "+
		"and r.status in (1000) "+
		"order by field1 desc, field2 desc, field3 desc, field4 asc, field5 asc ";

	public static String MKTG_GET_MATCHING_CAMPAIGN_FOR_CHANNEL = "select "+
		"(case when r.purpose = c.purpose then 1 else 0 end) field1 "+
		",(select count(*) from mktg_campaign_channel where "+
		"  mktg_campaign_channel.campaign_id = r.id and channel_id in "+
		"  (select channel_id from mktg_campaign_channel where campaign_id = c.id) "+
		") field2 "+
		",(select count(*) from mktg_customer_attrib rca, mktg_customer_attrib cca where "+
		"  rca.campaign_id = r.id "+
		"  and cca.campaign_id = c.id "+
		"  and rca.attrib_id = cca.attrib_id "+
		"  and rca.attrib_val = cca.attrib_val "+
		") field3 "+
		",(select abs(TO_NUMBER(TO_CHAR(min(rs.start_date), 'MM'))- "+
		"             TO_NUMBER(TO_CHAR(min(cs.start_date), 'MM')) "+
		"            ) from mktg_schedule rs, mktg_schedule cs "+
		"   where rs.campaign_id = r.id "+
		"   and cs.campaign_id = c.id "+
		" ) field4 "+
		",(select abs(max(rs.end_date)-min(rs.start_date) - "+
		"             (max(cs.end_date)-min(cs.start_date)) "+
		"            ) from mktg_schedule rs, mktg_schedule cs "+
		"   where rs.campaign_id = r.id "+
		"   and cs.campaign_id = c.id "+
		" ) field5 "+
		",r.id "+
		",r.code "+
		",r.name "+
		",r.description "+
		"from mktg_campaign r, mktg_campaign c "+
		"where c.id = ? "+
		"and r.status in (1000) "+
		"and exists(select channel_id from mktg_campaign_channel where mktg_campaign_channel.campaign_id = r.id and mktg_campaign_channel.channel_id = ?) "+
		"order by field1 desc, field2 desc, field3 desc, field4 asc, field5 asc ";

	//for sched level stuff
	public static String MKTG_GET_MATCHING_SCHED_FOR_SCHED = "select "+
		"(case when r.purpose = c.purpose then 1 else 0 end) field1 "+
		",((select count(*) from mktg_campaign_channel where "+
		"  mktg_campaign_channel.campaign_id = r.id and channel_id in "+
		"  (select channel_id from mktg_campaign_channel where campaign_id = c.id) "+
		")+(case when rs.channel_id = cs.channel_id then 10 else 0 end)) field2 "+
		",(select count(*) from mktg_customer_attrib rca, mktg_customer_attrib cca where "+
		"  rca.campaign_id = r.id "+
		"  and cca.campaign_id = c.id "+
		"  and rca.attrib_id = cca.attrib_id "+
		"  and rca.attrib_val = cca.attrib_val "+
		") field3 "+
		",abs(to_number(to_char(rs.start_date,'MM'))-to_number(to_char(cs.start_date,'MM'))) field4 "+
		",abs((rs.end_date-rs.start_date) - (cs.end_date - cs.start_date)) field5 "+
		",rs.id "+
		",rs.code "+
		",rs.name "+
		",rs.description "+
		"from mktg_campaign r, mktg_campaign c, mktg_schedule rs, mktg_schedule cs "+
		"where cs.id = ? "+
		"and cs.campaign_id = c.id "+
		"and rs.campaign_id = r.id "+
		"and r.status in (1000) "+
		"order by field1 desc, field2 desc, field3 desc, field4 asc, field5 asc ";
//season = ta, purpose = indication, site region sched.channel, total_anticipated_response = size
     public static String MKTG_GET_MATCHING_SCHED_FOR_SCHED_COVANCE = "select "+
		"(case when r.season = c.season then 10 else 0 end) field1 "+
          ",(case when r.purpose = c.purpose then 10 else 0 end) field2 "+
          ",(case when rs.channel_id = cs.channel_id then 10 else 0 end) field3 "+
          ",(case when rs.total_anticipated_response is null or rs.total_anticipated_response=0 then 0 "+
          "else (1-abs(rs.total_anticipated_response-rs.total_anticipated_response)/ "+
          "(rs.total_anticipated_response+rs.total_anticipated_response))*10 end) field4 "+
          ",(case when rs.end_date is null or rs.start_date is null or rs.end_date=rs.start_date then 0 "+
          "else (1-abs((rs.end_date-rs.start_date)-(cs.end_date-cs.start_date))/ "+
          "((rs.end_date-rs.start_date)+(cs.end_date-cs.start_date)))*10 end) field5 "+
		",rs.id "+
		",rs.code "+
		",rs.name "+
		",rs.description "+
		"from mktg_campaign r, mktg_campaign c, mktg_schedule rs, mktg_schedule cs "+
		"where cs.id = ? "+
		"and cs.campaign_id = c.id "+
		"and rs.campaign_id = r.id "+
		"and r.status in (1000) "+
		"order by field1 desc, field2 desc, field3 desc, field4 desc, field5 desc ";


	public static String MKTG_DELETE_BY_CAMPAIGN_PARAMS = "delete from mktg_parameters where campaign_id = ?";
	public static String MKTG_DELETE_BY_CAMPAIGN_RAMPUP = "delete from mktg_rampup_parameters where campaign_id = ?";
	public static String MKTG_DELETE_BY_SCHED_PARAMS = "delete from mktg_parameters where sched_id = ?";
	public static String MKTG_DELETE_BY_SCHED_RAMPUP = "delete from mktg_rampup_parameters where sched_id = ?";
	public static String MKTG_UPDATE_CAMP_DEFINITION_TYPE = "update mktg_campaign set definition_type = ? where id = ?";
	public static String MKTG_INSERT_PARAM = "insert into mktg_parameters (campaign_id, sched_id, channel_id, parameter_type, parameter_value, lower_value, upper_value) values (?,?,?,?,?,?,?)";
	public static String MKTG_INSERT_RAMPUP = "insert into mktg_rampup_parameters (campaign_id, sched_id, channel_id, time_period, value, lower_value, upper_value, scope) values (?,?,?,?,?,?,?,?)";
	public static String MKTG_GET_OFFER_PREMIUM_DETAIL =
		"select mktg_offer.id, mktg_offer.initial_quantity, mktg_offer.initial_size_uom "+
		",mktg_offer.repeat_quantity, mktg_offer.repeat_size_uom, mktg_offer.frequency "+
		",mktg_item_group_list.id, mktg_item_group_list.item_sub_type "+
		",(mktg_item_group_list.min_quantity+mktg_item_group_list.min_quantity)/2, mktg_item_group_list.expected_quantity "+
		",mktg_item_group_detail.item_id, mktg_item_group_detail.quantity, mktg_item_group_detail.size_uom "+
		",mktg_item_group_detail.expected_percentage "+
          ",mktg_item_group_detail.visit_sequence, mktg_item_group_detail.visit_name, mktg_item_group_detail.visit_skippable, mktg_item_group_detail.visit_type, mktg_item_group_detail.visit_week, mktg_item_group_detail.expected_delay  "+
		"from mktg_offer, mktg_item_group_list, mktg_item_group_detail "+
		"where (mktg_offer.id = ? or ? is null) "+
		"and mktg_item_group_list.offer_id = mktg_offer.id "+
		"and mktg_item_group_detail.group_id = mktg_item_group_list.id "+
		"order by mktg_offer.id,  mktg_item_group_list.item_sub_type desc, mktg_item_group_list.id, mktg_item_group_detail.visit_sequence, mktg_item_group_detail.item_id ";

	public static String MKTG_GET_OTHER_MEASURE_BY_WEEK_1_1 =
		"select mktg_for_data.measure_id, ";
	public static String MKTG_GET_OTHER_MEASURE_BY_WEEK_1_2 = //mktg_for_data.sched_id or mktg_for_data.item_id
		" temp_item_index, 1, 1, 1, mktg_data.time_id "+
		",sum(mktg_data.val), ? "+ //2 is the scope .. will need to change to weekly later
		", classify1, classify2, classify3, classify4, classify5,1 "+ //last 1 for scen id
		"from mktg_data, mktg_case_index, mktg_for_data where "+
		"1 = 1  ";

	public static String MKTG_GET_OTHER_MEASURE_BY_WEEK_2_1 =
		"and mktg_data.case_index_id = mktg_case_index.id "+
		"and mktg_data.for_data_id = mktg_for_data.id "+
		"and time_id >= ? "+
		"group by mktg_for_data.measure_id, "; //mktg_for_data.sched_id or mktg_for_data.item_id

	public static String MKTG_GET_OTHER_MEASURE_BY_WEEK_2_2 =
		",  classify1, classify2, classify3, classify4, classify5, time_id "+
		"order by mktg_for_data.measure_id, temp_item_index,  classify1, classify2, classify3, classify4, classify5, time_id ";

	//    "select port_results.alt_id, npv.npv_type, npv.target_market, npv.classify1, npv.classify2, npv.classify3, npv.classify4, npv.classify5, data.value from
	public final static String MKTG_GET_CAMPAIGN_PARAM_1_1 =
		"select mktg_schedule.id i1, 3023 m1, 1, mktg_parameters.parameter_type v1, 2,null,null,null,mktg_parameters.parameter_value, 1,lower_value, upper_value "+
		"from mktg_schedule, mktg_parameters where "+
		"mktg_schedule.campaign_id = mktg_parameters.campaign_id "+
		"and (mktg_parameters.sched_id is null or mktg_parameters.sched_id = mktg_schedule.id) "+
		"and (mktg_parameters.channel_id is null or mktg_parameters.channel_id = mktg_schedule.channel_id) "+
		"and 3023 in (";
	public final static String MKTG_GET_CAMPAIGN_PARAM_1_2 =
		"union (select mktg_schedule.id i1, 3024 m2, 1, time_period v1, 2,null,null,null,value, 1, lower_value, upper_value "+
		"from mktg_schedule, mktg_rampup_parameters where "+
		"mktg_schedule.campaign_id = mktg_rampup_parameters.campaign_id "+
		"and (mktg_rampup_parameters.sched_id is null or mktg_rampup_parameters.sched_id = mktg_schedule.id) "+
		"and (mktg_rampup_parameters.channel_id is null or mktg_rampup_parameters.channel_id = mktg_schedule.channel_id) "+
		"and 3024 in (";

	public final static String MKTG_GET_CAMPAIGN_PARAM_1_3 =
		"union (select mktg_sched_item_adjust.sched_id i1, 3034 m2, 1, mktg_sched_item_adjust.item_id v1, 2,null,null,null,adjust,1, adjust,adjust "+
		"from mktg_sched_item_adjust where "+
		" 3034 in (";
	public final static String MKTG_GET_CAMPAIGN_PARAM_2 ="  order by i1, v1";

	public static String MKTG_DELETE_BY_CAMPAIGN_PARAMS_SP = "delete from mktg_parameters where campaign_id = ? and parameter_type = ?";
	public static String MKTG_DELETE_BY_SCHED_PARAMS_SP = "delete from mktg_parameters where sched_id = ?  and parameter_type = ?";

	public static String MKTG_GET_KEYCODE_META = "select is_type, dim_id from mktg_keycode_info where  val_id is null and is_type is not null";
	public static String MKTG_GET_KEYCODE_SETUP = "select is_type, dim_id, val_id, minval, maxval, incr, textval,val from mktg_keycode_info where val_id is not null and ((is_type=0 and dim_id = ?) or (is_type=1 and dim_id = ?) or (is_type=2 and dim_id = ?))  order by is_type, val_id";
	public static String MKTG_DEL_MISMATCH_DIM = "delete from mktg_keycode_info where (is_type = ?)";
	public static String MKTG_INSERT_DIM = "insert into mktg_keycode_info (is_type, dim_id) values (?,?)";
	public static String MKTG_INSERT_VAL_PREFIX_SUFFIX = "insert into mktg_keycode_info (is_type, dim_id, val_id, textval) values (?,?,?,?)";
	public static String MKTG_UPDATE_VAL_PREXIF_SUFFIX = "update mktg_keycode_info set textval = ? where is_type=? and dim_id = ? and val_id = ?";
	public static String MKTG_INSERT_VAL_KEYSEQ = "insert into mktg_keycode_info (is_type, dim_id, val_id, minval,maxval,incr,val) values (?,?,?,?,?,?,?)";
	public static String MKTG_UPDATE_VAL_KEYSEQ = "update mktg_keycode_info set minval=?, maxval=?, incr=? where is_type=? and dim_id = ? and (val_id = ?)";

	public static String MKTG_KEY_BEST_VAL = "select t.minval, t.maxval, o.val, t.incr "+
		"from mktg_keycode_info o, "+
		"     (select val_id, minval, maxval, val, incr from mktg_keycode_info where is_type=2 and val_id=?) t "+
		"where "+
		"  o.is_type=2 "+
		"  and t.val_id <> o.val_id "+
		"  and (t.val+t.incr) >= o.minval "+
		"  and (t.val+t.incr) <= o.val "+
		"  and t.maxval >= o.minval "+
		"union (select minval, maxval, val, incr from mktg_keycode_info where is_type=2 and val_id=?) "+
		"order by val desc ";

	public static String MKTG_UPDATE_KEY_CODE="update mktg_keycode_info set val = ? where val_id=? and is_type=2";
	public static String MKTG_GET_PREFIX_SUUFIX = "select textval from mktg_keycode_info where is_type = ? and val_id=?";
	public static String MKTG_GET_ITEM_SUMM_INFO="select sku, name, sku_group_id,item_sub_type from mktg_item_detail where id=?";

	public static String MKTG_ITEM_SEL_1 = 
  Misc.G_DO_ORACLE ?
  "select item.id, sku_group.id, sku_group.name, item.sku, item.name, item.description, item.price, item.custom_field_1 "+
		"from mktg_item_detail item, mktg_item_detail sku_group "+
		"where item.sku_group_id = sku_group.id(+) "+
		"and item.item_type in (1,3,4) "+
		"and item.status in (1,2,3) "
    :
		"select item.id, sku_group.id, sku_group.name, item.sku, item.name, item.description, item.price, item.custom_field_1 "+
		"from mktg_item_detail item, mktg_item_detail sku_group "+
		"where item.sku_group_id *= sku_group.id "+
		"and item.item_type in (1,3,4) "+
		"and item.status in (1,2,3) ";

	public static String MKTG_ITEM_SEL_2 =
		" order by sku_group.name, item.name";

	public static String MKTG_GET_OVERLAPPING_SCHED_FOR_SCHED = "select "+
		"(case when r.purpose = c.purpose then 1 else 0 end) field1 "+
		",((select count(*) from mktg_campaign_channel where "+
		"  mktg_campaign_channel.campaign_id = r.id and channel_id in "+
		"  (select channel_id from mktg_campaign_channel where campaign_id = c.id) "+
		")+(case when rs.channel_id = cs.channel_id then 10 else 0 end)) field2 "+
		",(select count(*) from mktg_customer_attrib rca, mktg_customer_attrib cca where "+
		"  rca.campaign_id = r.id "+
		"  and cca.campaign_id = c.id "+
		"  and rca.attrib_id = cca.attrib_id "+
		"  and rca.attrib_val = cca.attrib_val "+
		") field3 "+
		",abs(to_number(to_char(rs.start_date,'MM'))-to_number(to_char(cs.start_date,'MM'))) field4 "+
		",abs((rs.end_date-rs.start_date) - (cs.end_date - cs.start_date)) field5 "+
		",rs.id "+
		",rs.code "+
		",rs.name "+
		",rs.description "+
		",r.name "+
		",rs.campaign_id "+
		",rs.channel_id "+
		",r.purpose "+
		",rs.start_date "+
		",rs.end_date "+
		"from mktg_campaign r, mktg_campaign c, mktg_schedule rs, mktg_schedule cs "+
		"where cs.id = ? "+
		"and cs.campaign_id = c.id "+
		"and rs.campaign_id = r.id "+
		"and not(r.status in (1000)) "+
		"and rs.id <> cs.id "+
		"and (rs.start_date <= cs.start_date and rs.end_date >= cs.start_date) "+
		"order by field1 desc, field3 desc, field4 asc, field2 desc, field5 asc ";

	public static String MKTG_GET_IMPACT_PARAMETER =
  Misc.G_DO_ORACLE ?
  "select to_campaign_id,to_campaign_sched_id,parameter_type,parameter_value, mktg_campaign.name, mktg_schedule.name from "+
		"mktg_impact_on_other_campaign, mktg_impact_data, mktg_campaign, mktg_schedule "+
		"where "+
		"from_campaign_sched_id = ? "+
		"and mktg_impact_id = mktg_impact_on_other_campaign.id "+
		"and mktg_campaign.id(+) = to_campaign_id "+
		"and mktg_schedule.id(+) = to_campaign_sched_id "+
		"order by mktg_campaign.id nulls first, mktg_schedule.id nulls first, parameter_type "
    :
		"select to_campaign_id,to_campaign_sched_id,parameter_type,parameter_value, mktg_campaign.name, mktg_schedule.name from "+
		"mktg_impact_on_other_campaign, mktg_impact_data, mktg_campaign, mktg_schedule "+
		"where "+
		"from_campaign_sched_id = ? "+
		"and mktg_impact_id = mktg_impact_on_other_campaign.id "+
		"and mktg_campaign.id =* to_campaign_id "+
		"and mktg_schedule.id =* to_campaign_sched_id "+
		"order by mktg_campaign.id asc, mktg_schedule.id asc, parameter_type ";

	public static String MKTG_DEL_IMPACT_DATA_SCHED = "delete from mktg_impact_data where mktg_impact_id in (select id from mktg_impact_on_other_campaign where from_campaign_sched_id = ?)";
	public static String MKTG_DEL_IMPACT_SCHED = "delete from mktg_impact_on_other_campaign where from_campaign_sched_id = ?";
	public static String MKTG_INS_IMPACT_SCHED = "insert into mktg_impact_on_other_campaign (id, from_campaign_id, from_campaign_sched_id, to_campaign_id, to_campaign_sched_id) values (?,?,?,?,?)";
	public static String MKTG_INS_IMPACT_SCHED_DATA = "insert into mktg_impact_data (mktg_impact_id, parameter_type, parameter_value) values (?,?,?)";

     public static String MKTG_GET_SITE_INFO = "select mktg_case_index.classify1, mktg_data.time_id, mktg_data.val, mktg_case_index.classify2, mktg_case_index.classify3 "+
" from mktg_case_index, mktg_for_data, mktg_data "+
" where mktg_for_data.sched_id = ? "+
" and mktg_for_data.measure_id = 3056 "+
" and mktg_case_index.measure_id = 3056 "+
" and mktg_data.case_index_id = mktg_case_index.id "+
" and mktg_data.for_data_id = mktg_for_data.id order by mktg_data.time_id, mktg_data.case_index_id";

    public static String MKTG_INSERT_FOR_DATA = "insert into mktg_for_data (id,measure_id,sched_id) values (?, ?, ?)";
    public static String MKTG_GET_FOR_DATA_INDEX = "select id from mktg_for_data where measure_id = ? and sched_id = ?";
    public static String MKTG_GET_CASE_INDEX_INDEX = "select id from mktg_case_index where measure_id = ? and (classify1 = ? or classify1 is null) and (classify2 = ? or classify2 is null) and (classify3 = ? or classify3 is null)";
    public static String MKTG_INSERT_CASE_INDEX_INDEX = "insert into mktg_case_index (id,measure_id,classify1, classify2, classify3) values (?, ?, ?,?,?)";
    public static String MKTG_DEL_MKTG_DATA = "delete from mktg_data where mktg_data.for_data_id in (select mktg_for_data.id from mktg_for_data where measure_id = ? and sched_id = ?)";
    public static String MKTG_INSERT_MKTG_DATA = "insert into mktg_data (case_index_id, for_data_id, time_id, val) values (?,?,?,?)";
	//from sameer 031607
	public static String GET_MKTG_ITEMS_LIST_QUERY = 
  Misc.G_DO_ORACLE ?
  "select mktg_item_detail.id, mktg_item_detail.sku, mktg_item_detail.name, mktg_item_detail.item_type, " +
		"mktg_item_detail.date_available, mktg_item_detail.description, mktg_item_detail.status, " +
		"users.name, mktg_item_detail.create_date " +
		"from mktg_item_detail, users " +
		"where mktg_item_detail.user_id = users.id(+) " + //+ added by rajeev ... not imp
		"and mktg_item_detail.status <> 4 " +
		"order by "
    :
		"select mktg_item_detail.id, mktg_item_detail.sku, mktg_item_detail.name, mktg_item_detail.item_type, " +
		"mktg_item_detail.date_available, mktg_item_detail.description, mktg_item_detail.status, " +
		"users.name, mktg_item_detail.create_date " +
		"from mktg_item_detail, users " +
		"where mktg_item_detail.user_id *= users.id " + //+ added by rajeev ... not imp
		"and mktg_item_detail.status <> 4 " +
		"order by ";

	public static String SELECT_MKTG_ITEM_DETAIL_QUERY =
		"select id, sku, name, description, item_type, measurement_unit, status, " +
		"price, sku_group_id, user_id, create_date, item_sub_type, lead_time, date_available, " +
		"price_unit, custom_field_1, custom_field_2, custom_field_3, custom_field_4 " +
		"from mktg_item_detail " +
		"where id = ?";

	public static String SELECT_SKU_GROUPS_QUERY =
		"select id, sku, name from mktg_item_detail where item_type = 2";

	public static String UPDATE_MKTG_ITEM_DETAIL_QUERY =
		"update mktg_item_detail set sku = ?, name = ?, description = ?, item_type = ?, " +
		"measurement_unit = ?, status = ?, price = ?, sku_group_id = ?, " +
		"item_sub_type = ?, date_available = ?, " +
		"custom_field_1 = ?, custom_field_2 = ?, custom_field_3 = ?, custom_field_4 = ? " +
		"where id = ?";

	public static String CREATE_MKTG_ITEM_DETAIL_QUERY = 
  Misc.G_DO_ORACLE ?
  "insert into mktg_item_detail(sku, name, description, item_type, measurement_unit, " +
		"status, price, sku_group_id, user_id, create_date, item_sub_type, " +
		"date_available, custom_field_1, custom_field_2, custom_field_3, custom_field_4, id) " +
		"values (?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate, ?, ?, ?, ?, ?, ?, ?)"
    :
		"insert into mktg_item_detail(sku, name, description, item_type, measurement_unit, " +
		"status, price, sku_group_id, user_id, create_date, item_sub_type, " +
		"date_available, custom_field_1, custom_field_2, custom_field_3, custom_field_4, id) " +
		"values (?, ?, ?, ?, ?, ?, ?, ?, ?, getdate(), ?, ?, ?, ?, ?, ?, ?)";

	public static String GET_MKTG_OFFERS_LIST_QUERY =
  Misc.G_DO_ORACLE ?
  "select mktg_offer.id, mktg_offer.offer_code, mktg_offer.name, mktg_offer.offer_type, " +
		"mktg_offer.date_available, mktg_offer.description, mktg_offer.status, users.name, " +
		"mktg_offer.create_date from mktg_offer, users " +
		"where mktg_offer.user_id = users.id(+) " +
		"and mktg_offer.status <> 4 " +
		"order by "
    :
		"select mktg_offer.id, mktg_offer.offer_code, mktg_offer.name, mktg_offer.offer_type, " +
		"mktg_offer.date_available, mktg_offer.description, mktg_offer.status, users.name, " +
		"mktg_offer.create_date from mktg_offer, users " +
		"where mktg_offer.user_id *= users.id " +
		"and mktg_offer.status <> 4 " +
		"order by ";

	public static String SELECT_MKTG_OFFER_DETAIL_QUERY_BASIC =
		"select id, offer_code, name, offer_type, date_available, status, description, " +
		"initial_quantity, initial_size_uom, initial_price, price_classification_uom, " +
		"frequency, repeat_quantity, repeat_size_uom " +
		"from mktg_offer " +
		"where id = ?";

	public static String SELECT_MKTG_OFFER_DETAIL_QUERY_REGULAR_ITEM_LIST =
		"select mktg_item_group_list.id, mktg_item_detail.id, mktg_item_detail.name, " +
		"mktg_item_group_detail.quantity, mktg_item_group_detail.size_uom " +
		"from mktg_item_group_list, mktg_item_detail, mktg_item_group_detail " +
		"where mktg_item_group_list.offer_id = ? " +
		"and mktg_item_group_list.id = mktg_item_group_detail.group_id " +
		"and mktg_item_detail.id = mktg_item_group_detail.item_id " +
		"and mktg_item_group_list.item_sub_type = 2 " + //was mktg_item_detail.
		"order by mktg_item_detail.name";

	public static String SELECT_MKTG_OFFER_DETAIL_QUERY_PROMO_ITEM_LIST =
		"select mktg_item_group_list.id, mktg_item_group_list.min_quantity, mktg_item_group_list.max_quantity, " +
		"mktg_item_group_list.price, mktg_item_group_list.price_classification_uom, " +
		"mktg_item_detail.id, mktg_item_detail.name, mktg_item_group_detail.quantity, " +
		"mktg_item_group_detail.size_uom " +
          ",mktg_item_group_detail.visit_sequence, mktg_item_group_detail.visit_name, mktg_item_group_detail.visit_skippable, mktg_item_group_detail.visit_type, mktg_item_group_detail.visit_week  "+
		"from mktg_item_group_list, mktg_item_detail, mktg_item_group_detail " +
		"where mktg_item_group_list.offer_id = ? " +
		"and mktg_item_group_detail.group_id = mktg_item_group_list.id " +
		"and mktg_item_detail.id = mktg_item_group_detail.item_id " +
		"and mktg_item_group_list.item_sub_type = 1 " + //was mktg_item_detail.
		"order by mktg_item_group_list.id, visit_sequence, mktg_item_detail.name";

	public static String UPDATE_MKTG_OFFER =
		"update mktg_offer set offer_code = ?, name = ?, offer_type = ?, date_available = ?, " +
		"status = ?, description = ?, initial_quantity = ?, initial_size_uom = ?, " +
		"initial_price = ?, price_classification_uom = ?, frequency = ?, " +
		"repeat_quantity = ?, repeat_size_uom = ?" +
		"where id = ?";

	public static String INSERT_MKTG_OFFER = 
  Misc.G_DO_ORACLE ?
    "insert into mktg_offer (id, offer_code, name, offer_type, date_available, " +
		"status, description, initial_quantity, initial_size_uom, initial_price, " +
		"price_classification_uom, frequency, repeat_quantity, repeat_size_uom, " +
		"user_id, create_date) " +
		"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate)"
    :
		"insert into mktg_offer (id, offer_code, name, offer_type, date_available, " +
		"status, description, initial_quantity, initial_size_uom, initial_price, " +
		"price_classification_uom, frequency, repeat_quantity, repeat_size_uom, " +
		"user_id, create_date) " +
		"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, getdate())";

	public static String INSERT_MKTG_ITEM_GROUP_REGULAR =
		"insert into mktg_item_group_list (id, offer_id, item_sub_type) " +
		"values(?, ?, ?)";

	public static String INSERT_MKTG_ITEM_GROUP_PREMIUM =
		"insert into mktg_item_group_list (id, offer_id, min_quantity, max_quantity, " +
		"price, price_classification_uom, item_sub_type) " +
		"values(?, ?, ?, ?, ?, ?, ?)";

	public static String UPDATE_MKTG_ITEM_GROUP_PREMIUM =
		"update mktg_item_group_list set offer_id = ?, min_quantity = ?, max_quantity = ?, " +
		"price = ?, price_classification_uom = ?, item_sub_type = ? " +
		"where id = ?";

	public static String INSERT_MKTG_ITEM_GROUP_DETAIL =
		"insert into mktg_item_group_detail (group_id, item_id, quantity, size_uom, visit_sequence, visit_name, visit_skippable, visit_type, visit_week) " +
		"values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	public static String DELETE_MKTG_ITEM_GROUP =

		"delete from mktg_item_group_list where offer_id = ? and id = ?";

	public static String SELECT_MKTG_OFFER_GROUP_IDS =
		"select id from mktg_item_group_list where offer_id = ? and item_sub_type = ?";

	public static String DELETE_MKTG_ITEM_GROUP_DETAIL =
		"delete from mktg_item_group_detail where group_id = ?";

	public static String SELECT_MKTG_ITEMS_QUERY =
		"select id, name from mktg_item_detail where " +
		"item_sub_type = ? " +
		"and status <> 4 " +
		"order by sku_group_id, name";

	public static String SELECT_MKTG_CAMPAIGNS_LIST_QUERY = Misc.G_DO_ORACLE ?
  "select mktg_campaign.id, mktg_campaign.code, mktg_campaign.name, mktg_campaign.start_date, " +
		"mktg_campaign.end_date, mktg_campaign.purpose, mktg_campaign.status, users.name, " +
		"mktg_campaign.create_date " +
		"from mktg_campaign, users " +
		"where mktg_campaign.user_id = users.id(+) " +
		"and mktg_campaign.status <> 4 " +
		"order by "
    :
		"select mktg_campaign.id, mktg_campaign.code, mktg_campaign.name, mktg_campaign.start_date, " +
		"mktg_campaign.end_date, mktg_campaign.purpose, mktg_campaign.status, users.name, " +
		"mktg_campaign.create_date " +
		"from mktg_campaign, users " +
		"where mktg_campaign.user_id *= users.id " +
		"and mktg_campaign.status <> 4 " +
		"order by ";

	public static String SELECT_MKTG_SCHEDULES_LIST_QUERY =
  Misc.G_DO_ORACLE ?
  "select mktg_schedule.id, mktg_schedule.code, mktg_schedule.name, " +
		"mktg_schedule.activity, mktg_schedule.channel_id, mktg_schedule.start_date, " +
		"mktg_schedule.end_date, mktg_schedule.status, users.name, " +
		"mktg_schedule.create_date, mktg_offer.name " +
		"from mktg_schedule, users, mktg_offer " +
		"where mktg_schedule.user_id = users.id(+) " +
		"and mktg_schedule.offer_id = mktg_offer.id(+)" +
		"and mktg_schedule.status <> 4 " +
		"and mktg_schedule.campaign_id = ? " +
		"order by "
    :
		"select mktg_schedule.id, mktg_schedule.code, mktg_schedule.name, " +
		"mktg_schedule.activity, mktg_schedule.channel_id, mktg_schedule.start_date, " +
		"mktg_schedule.end_date, mktg_schedule.status, users.name, " +
		"mktg_schedule.create_date, mktg_offer.name " +
		"from mktg_schedule, users, mktg_offer " +
		"where mktg_schedule.user_id *= users.id " +
		"and mktg_schedule.offer_id *= mktg_offer.id" +
		"and mktg_schedule.status <> 4 " +
		"and mktg_schedule.campaign_id = ? " +
		"order by ";

	public static String SELECT_MKTG_CAMPAIGN_DETAIL_QUERY =
		"select id, code, name, description, " +
		"parent_campaign_id, start_date, end_date, " +
		"cf1, cf2, cf3, cf4, status, purpose, season, activity, offer_id, channel_id, year, cf5 " +
		"from mktg_campaign where " +
		"mktg_campaign.id = ?";

	public static String SELECT_MKTG_CAMPAIGNS_NAME =
		"select name, id from mktg_campaign where " +
		"mktg_campaign.id <> ?";

	public static String SELECT_MKTG_CAMPAIGN_TARGET_REGION =
		"select region_id from mktg_campaign_region where " +
		"campaign_id = ?";

	public static String SELECT_MKTG_CAMPAIGN_CHANNEL =
		"select channel_id from mktg_campaign_channel where " +
		"campaign_id = ?";

	public static String UPDATE_MKTG_CAMPAIGN_DETAIL_QUERY =
		"update mktg_campaign set code = ?, name = ?, description = ?, parent_campaign_id = ?, " +
		"start_date = ?, end_date = ?, cf1 = ?, cf2 = ?, cf3 = ?, cf4 = ?, status = ?, " +
		"purpose = ?, season = ?, activity = ?, offer_id = ?, channel_id = ?, year = ?, " +
		"cf5 = ? " +
		"where id = ?";

	public static String CREATE_MKTG_CAMPAIGN_DETAIL_QUERY =
  Misc.G_DO_ORACLE ?
  "insert into mktg_campaign (id, code, name, description, parent_campaign_id, " +
		"start_date, end_date, cf1, cf2, cf3, cf4, status, purpose, season, activity, " +
		"user_id, create_date, offer_id, channel_id, year, cf5) " +
		"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate, ?, ?, ?, ?)"
    :
		"insert into mktg_campaign (id, code, name, description, parent_campaign_id, " +
		"start_date, end_date, cf1, cf2, cf3, cf4, status, purpose, season, activity, " +
		"user_id, create_date, offer_id, channel_id, year, cf5) " +
		"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, getdate(), ?, ?, ?, ?)";

	public static String SELECT_MKTG_SCHEDULE_DETAIL_QUERY =
		"select id, name, channel_id, activity, code, status, start_date, end_date, " +
		"repeat_interval, description, cf1, cf2, cf3, cf4, cf5 " +
		",offer_id, total_anticipated_response "+
		"from mktg_schedule where " +
		"id = ? " +
		"and campaign_id = ?";

	public static String UPDATE_MKTG_SCHEDULE_DETAIL_QUERY =
		"update mktg_schedule set name = ?, channel_id = ?, activity = ?, code = ?, " +
		"status = ?, start_date = ?, end_date = ?, repeat_interval = ?, description = ?, " +
		"cf1 = ?, cf2 = ?, cf3 = ?, cf4 = ?, cf5 = ?, offer_id = ?, total_anticipated_response = ? " +
		"where id = ?";

	public static String CREATE_MKTG_SCHEDULE_DETAIL_QUERY =
  Misc.G_DO_ORACLE ?
  "insert into mktg_schedule (id, campaign_id, name, channel_id, activity, " +
		"code, status, start_date, end_date, repeat_interval, description, " +
		"cf1, cf2, cf3, cf4, cf5, " +
		"user_id, create_date, offer_id, total_anticipated_response) " +
		"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
		"?, ?, ?, ?, ?, sysdate, ?,?)"
    :
		"insert into mktg_schedule (id, campaign_id, name, channel_id, activity, " +
		"code, status, start_date, end_date, repeat_interval, description, " +
		"cf1, cf2, cf3, cf4, cf5, " +
		"user_id, create_date, offer_id, total_anticipated_response) " +
		"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
		"?, ?, ?, ?, ?, getdate(), ?,?)";

	// End queries for forecast -- Sameer 03/09/2006

	//rajeev updates 031606
	public static String MKTG_GET_CHANNEL_LIST_FOR_CAMPAIGN = "select channel_id from mktg_campaign_channel where campaign_id = ?";
	public static String MKTG_GET_REGION_LIST_FOR_CAMPAIGN = "select region_id from mktg_campaign_region where campaign_id = ?";
	public static String MKTG_DEL_CHANNEL_LIST_FOR_CAMPAIGN = "delete from mktg_campaign_channel where campaign_id = ?";
	public static String MKTG_DEL_REGION_LIST_FOR_CAMPAIGN = "delete from mktg_campaign_region where campaign_id = ?";
	public static String MKTG_INS_CHANNEL_LIST_FOR_CAMPAIGN = "insert into mktg_campaign_channel (campaign_id, channel_id) values (?,?)";
	public static String MKTG_INS_REGION_LIST_FOR_CAMPAIGN = "insert into mktg_campaign_region (campaign_id, region_id) values (?,?)";

	public static String MKTG_GET_AVAILABLE_OFFER = Misc.G_DO_ORACLE ?
  "select ( mktg_offer.name || ' - ' || mktg_offer.offer_code) n, mktg_offer.id from mktg_offer where status in (1,2) order by n"
  :
  "select ( mktg_offer.name + ' - ' + mktg_offer.offer_code) n, mktg_offer.id from mktg_offer where status in (1,2) order by n";
	public static String MKTG_GET_SKU_GROUP = Misc.G_DO_ORACLE ?
  "select ( mktg_item_detail.sku || ' - ' || mktg_item_detail.name) n, id from mktg_item_detail where item_type = 2 and status <> 4 "
  :
  "select ( mktg_item_detail.sku + ' - ' + mktg_item_detail.name) n, id from mktg_item_detail where item_type = 2 and status <> 4 ";

	//redemption queries -- sameer03162006

	public static String SELECT_MKTG_OFFER_REDEMPTION_REGULAR =
		"select mktg_item_group_list.id, mktg_item_detail.id, " +
		"mktg_item_detail.name, " +
		"mktg_item_group_detail.expected_percentage " +
		"from mktg_item_group_list, mktg_item_group_detail, " +
		"mktg_item_detail where " +
		"mktg_item_group_list.offer_id = ? " +
		"and mktg_item_group_list.item_sub_type = ? " +
		"and mktg_item_group_detail.group_id = mktg_item_group_list.id " +
		"and mktg_item_detail.id = mktg_item_group_detail.item_id " +
		"order by mktg_item_detail.sku_group_id, mktg_item_detail.name";

	public static String SELECT_MKTG_OFFER_REDEMPTION_PREMIUM =
		"select mktg_item_group_list.id, mktg_item_group_list.min_quantity, " +
		"mktg_item_group_list.max_quantity, mktg_item_group_list.expected_quantity, " +
		"mktg_item_detail.id, mktg_item_detail.name, " +
		"mktg_item_group_detail.expected_percentage " +
          ",mktg_item_group_detail.visit_sequence, mktg_item_group_detail.visit_name, mktg_item_group_detail.visit_skippable, mktg_item_group_detail.visit_type, mktg_item_group_detail.visit_week  "+
		"from mktg_item_group_list, mktg_item_group_detail, mktg_item_detail " +
		"where mktg_item_group_list.offer_id = ? " +
		"and mktg_item_group_list.item_sub_type = ? " +
		"and mktg_item_group_detail.group_id = mktg_item_group_list.id " +
		"and mktg_item_detail.id = mktg_item_group_detail.item_id " +
		"order by mktg_item_detail.sku_group_id, mktg_item_detail.name";

	public static String UPDATE_MKTG_OFFER_REDEMPTION_GROUP_DETAIL =
		"update mktg_item_group_detail set expected_percentage = ?, expected_delay=? " +
		"where group_id = ? " +
		"and item_id = ? " +
          "and (? is null or visit_sequence is null or visit_sequence = ?)" ;

	public static String UPDATE_MKTG_OFFER_REDEMPTION_GROUP_LIST =
		"update mktg_item_group_list set expected_quantity = ? " +
		"where offer_id = ? " +
		"and id = ?";

	public static String SELECT_TOTAL_ITEMS_IN_GROUP_LIST =
		"select count(mktg_item_group_detail.item_id), mktg_item_group_detail.group_id " +
		"from mktg_item_group_detail, mktg_item_group_list " +
		"where mktg_item_group_list.offer_id = ? " +
		"and mktg_item_group_list.item_sub_type = ? " +
		"and mktg_item_group_detail.group_id = mktg_item_group_list.id " +
		"group by mktg_item_group_detail.group_id";

	public static String SELECT_MKTG_OFFER_REDEMPTION_PREMIUM_FOR_GROUP =
		"select mktg_item_group_list.min_quantity, " +
		"mktg_item_group_list.max_quantity, mktg_item_group_list.expected_quantity, " +
		"mktg_item_detail.id, mktg_item_detail.name, " +
		"mktg_item_group_detail.expected_percentage " +
		"from mktg_item_group_list, mktg_item_group_detail, mktg_item_detail " +
		"where mktg_item_group_list.offer_id = ? " +
		"and mktg_item_group_list.item_sub_type = ? " +
		"and mktg_item_group_list.id = ? " +
		"and mktg_item_group_detail.group_id = mktg_item_group_list.id " +
		"and mktg_item_detail.id = mktg_item_group_detail.item_id " +
		"order by mktg_item_detail.sku_group_id, mktg_item_detail.name";

	public static String SELECT_TOTAL_ITEMS_IN_GROUP_LIST_PREMIUM =
		"select count(mktg_item_group_detail.item_id), mktg_item_group_detail.group_id, " +
		"min(mktg_item_group_list.min_quantity), min(mktg_item_group_list.max_quantity), " +
		"min(mktg_item_group_list.expected_quantity) " +
		"from mktg_item_group_detail, mktg_item_group_list " +
		"where mktg_item_group_list.offer_id = ? " +
		"and mktg_item_group_list.item_sub_type = ? " +
		"and mktg_item_group_detail.group_id = mktg_item_group_list.id " +
		"group by mktg_item_group_detail.group_id";

	public static String SELECT_ITEM_DETAIL_FOR_OFFER_GROUP =
		"select mktg_item_group_detail.item_id, mktg_item_detail.name, " +
		"mktg_item_group_detail.expected_percentage " +
          ",visit_sequence, visit_name, visit_skippable, visit_type, visit_week, expected_delay "+
		"from mktg_item_group_detail, mktg_item_detail " +
		"where mktg_item_group_detail.group_id = ? " +
		"and mktg_item_detail.id = mktg_item_group_detail.item_id order by visit_sequence";

	// End queries for forecast -- Sameer 03/09/2006
	//rajeev 031606
	public static String MKTG_GET_OFFER_SUMM = "select offer_code, name from mktg_offer where id = ?";

	//sameer ...
	//contents queries

	public static String GET_CONTENTS_LIST =
		"select a.id contentid, a.moniker moniker, a.content_metadata_id mdataid, " +
		"a.item_id objid, b.sku objname, 't1' objtype " +
		"from mktg_contents a, mktg_item_detail b " +
		"where a.item_id = b.id " +
		"and a.is_public = 1 " +
		"union " +
		"( " +
		"select a.id contentid, a.moniker moniker, a.content_metadata_id mdataid, " +
		"a.offer_id objid, b.name objname, 't2' objtype " +
		"from mktg_contents a, mktg_offer b " +
		"where a.offer_id = b.id " +
		"and a.is_public = 1 " +
		") " +
		"union " +
		"( " +
		"select a.id contentid, a.moniker moniker, a.content_metadata_id mdataid, " +
		"a.campaign_id objid, b.name objname, 't3' objtype " +
		"from mktg_contents a, mktg_campaign b " +
		"where a.campaign_id = b.id " +
		"and a.is_public = 1 " +
		") " +
		"union " +
		"( " +
		"select a.id contentid, a.moniker moniker, a.content_metadata_id mdataid, " +
		"a.sched_id objid, b.name objname, 't4' objtype " +
		"from mktg_contents a, mktg_schedule b " +
		"where a.sched_id = b.id " +
		"and a.is_public = 1 " +
		") " +
		"union " +
		"( " +
		"select a.id contentid, a.moniker moniker, a.content_metadata_id mdataid, " +
		"1 objid, 'objname' objname, 't5' objtype " +
		"from mktg_contents a " +
		"where a.is_public = 1 " +
		")";

	public static String SELECT_CONTENT_DETAIL_QUERY =
		"select a.id, a.text, a.link_id, a.offer_id, a.item_id, a.campaign_id, " +
		"a.sched_id, a.content_metadata_id, a.moniker from mktg_contents a " +
		"where a.id = ? and a.is_public = 1";

	public static String SELECT_MKTG_OFFER_NAME_QUERY =
		"select name from mktg_offer where id = ?";

	public static String SELECT_MKTG_ITEM_SKU_QUERY =
		"select sku from mktg_item_detail where id = ?";

	public static String SELECT_MKTG_CAMPAIGN_NAME_QUERY =
		"select name from mktg_campaign where id = ?";

	public static String SELECT_MKTG_SCHEDULE_NAME_QUERY =
		"select name from mktg_schedule where id = ?";

	public static String INSERT_MKTG_CONTENT_FOR_ITEM_QUERY =
		"insert into mktg_contents(id, text, link_id, item_id, content_metadata_id, " +
		"moniker, is_public) values (?, ?, ?, ?, ?, ?, 1)";

	public static String INSERT_MKTG_CONTENT_FOR_OFFER_QUERY =
		"insert into mktg_contents(id, text, link_id, offer_id, content_metadata_id, " +
		"moniker, is_public) values (?, ?, ?, ?, ?, ?, 1)";

	public static String INSERT_MKTG_CONTENT_FOR_CAMPAIGN_QUERY =
		"insert into mktg_contents(id, text, link_id, campaign_id, content_metadata_id, " +
		"moniker, is_public) values (?, ?, ?, ?, ?, ?, 1)";

	public static String INSERT_MKTG_CONTENT_FOR_SCHEDULE_QUERY =
		"insert into mktg_contents(id, text, link_id, sched_id, content_metadata_id, " +
		"moniker, is_public) values (?, ?, ?, ?, ?, ?, 1)";

	public static String UPDATE_MKTG_CONTENT_FOR_ITEM_QUERY =

		"update mktg_contents set text = ?, moniker = ? " +
		"where id = ? and item_id = ? and content_metadata_id = ?";

	public static String UPDATE_MKTG_CONTENT_FOR_OFFER_QUERY =
		"update mktg_contents set text = ?, moniker = ? " +
		"where id = ? and offer_id = ? and content_metadata_id = ?";

	public static String UPDATE_MKTG_CONTENT_FOR_CAMPAIGN_QUERY =
		"update mktg_contents set text = ?, moniker = ? " +
		"where id = ? and campaign_id = ? and content_metadata_id = ?";

	public static String UPDATE_MKTG_CONTENT_FOR_SCHEDULE_QUERY =
		"update mktg_contents set text = ?, moniker = ? " +
		"where id = ? and schedule_id = ? and content_metadata_id = ?";

	public static String UPDATE_MKTG_CONTENT_BASIC_QUERY =
		"update mktg_contents set moniker = ?, text = ?, content_metadata_id = ? " +
		"where id = ? ";

	public static String INSERT_MKTG_CONTENT_BASIC_QUERY =
		"insert into mktg_contents (id, moniker, text, content_metadata_id, is_public) " +
		"values (?, ?, ?, ?, 1)";

	public static String SELECT_CONTENT_FOR_ITEM_QUERY =
		"select id, text, moniker from mktg_contents " +
		"where item_id = ? and content_metadata_id = ? " +
		"and is_public = 1";

	public static String SELECT_CONTENT_FOR_OFFER_QUERY =
		"select id, text, moniker from mktg_contents " +
		"where offer_id = ? and content_metadata_id = ? " +
		"and is_public = 1";

	public static String SELECT_CONTENT_FOR_CAMPAIGN_QUERY =
		"select id, text, moniker from mktg_contents " +
		"where campaign_id = ? and content_metadata_id = ? " +
		"and is_public = 1";

	public static String SELECT_CONTENT_FOR_SCHEDULE_QUERY =
		"select id, text, moniker from mktg_contents " +
		"where sched_id = ? and content_metadata_id = ? " +
		"and is_public = 1";

	public static String SELECT_CONTENTS_FROM_REPOSITOTY =
		"select id, text, moniker, content_metadata_id from mktg_contents " +
		"where content_metadata_id = ? and is_public = 1 " +
		"and link_id is NULL";


	//end content queries

	//rajeev 033106 ..
	

	public static String MKTG_GET_SCHED_WITH_NO_REF = 
  Misc.G_DO_ORACLE ?
  "select mktg_schedule.id, campaign_id from mktg_schedule where status in (1,3) "+
		"minus(select sched_id, campaign_id from mktg_parameters where parameter_type=100000 "+
		"and parameter_value is not null)"
  :
  "select mktg_schedule.id, campaign_id from mktg_schedule where status in (1,3) "+
		"and Not Exists((select sched_id, campaign_id from mktg_parameters where parameter_type=100000 "+
		"and parameter_value is not null))";
	public static String MKTG_CLEAN_SCHED_WITH_NO_REF = "delete from mktg_parameters where parameter_type=100000 and parameter_value is null";

	public static String MKTG_GET_HISTORICAL_ITEM_SALE_DATA = Misc.G_DO_ORACLE ?
  "  select il.iid, sdv.v from "+
		"( "+
		"select "+
		"1 "+
		",gd.item_id iid "+
		"from mktg_item_group_list gl, mktg_item_group_detail gd where "+
		"1=1 "+
		"and gd.group_id = gl.id "+
		"and gl.item_sub_type=1 "+
		"and offer_id=? "+
		") il "+
		", "+
		"( "+
		"select sum(mktg_data.val) v "+
		",mktg_for_data.item_id i "+
		"from "+
		"mktg_data, mktg_for_data "+
		"where "+
		"mktg_data.for_data_id = mktg_for_data.id "+
		"and mktg_data.case_index_id=3030 "+
		"and mktg_data.time_id >= ? " +
		"and mktg_data.time_id <= ? " +
		"group by mktg_for_data.item_id "+
		") sdv "+
		"where "+
		"sdv.i(+) = il.iid "
    :
		"  select il.iid, sdv.v from "+
		"( "+
		"select "+
		"1 "+
		",gd.item_id iid "+
		"from mktg_item_group_list gl, mktg_item_group_detail gd where "+
		"1=1 "+
		"and gd.group_id = gl.id "+
		"and gl.item_sub_type=1 "+
		"and offer_id=? "+
		") il "+
		", "+
		"( "+
		"select sum(mktg_data.val) v "+
		",mktg_for_data.item_id i "+
		"from "+
		"mktg_data, mktg_for_data "+
		"where "+
		"mktg_data.for_data_id = mktg_for_data.id "+
		"and mktg_data.case_index_id=3030 "+
		"and mktg_data.time_id >= ? " +
		"and mktg_data.time_id <= ? " +
		"group by mktg_for_data.item_id "+
		") sdv "+
		"where "+
		"sdv.i =* il.iid ";

	public static String MKTG_GET_HISTORICAL_ITEM_SALE_DATA_PER_OFFER = Misc.G_DO_ORACLE ?
  "  select il.iid, sdv.v from "+
		"( "+
		"select "+
		"1 "+
		",gd.item_id iid "+
		"from mktg_item_group_list gl, mktg_item_group_detail gd where "+
		"1=1 "+
		"and gd.group_id = gl.id "+
		"and gl.item_sub_type=1 "+
		"and offer_id=? "+
		") il "+
		", "+
		"( "+
		"select sum(mktg_data.val) v "+
		",mktg_for_data.item_id i "+
		"from "+
		"mktg_data, mktg_for_data, mktg_schedule "+
		"where "+
		"mktg_data.for_data_id = mktg_for_data.id "+
		"and mktg_schedule.id = mktg_for_data.sched_id "+
		"and mktg_schedule.offer_id = ? "+
		"and mktg_data.case_index_id=3030 "+
		"and mktg_data.time_id >= ? " +
		"and mktg_data.time_id <= ? " +
		"group by mktg_for_data.item_id "+
		") sdv "+
		"where "+
		"sdv.i(+) = il.iid "
    :
		"  select il.iid, sdv.v from "+
		"( "+
		"select "+
		"1 "+
		",gd.item_id iid "+
		"from mktg_item_group_list gl, mktg_item_group_detail gd where "+
		"1=1 "+
		"and gd.group_id = gl.id "+
		"and gl.item_sub_type=1 "+
		"and offer_id=? "+
		") il "+
		", "+
		"( "+
		"select sum(mktg_data.val) v "+
		",mktg_for_data.item_id i "+
		"from "+
		"mktg_data, mktg_for_data, mktg_schedule "+
		"where "+
		"mktg_data.for_data_id = mktg_for_data.id "+
		"and mktg_schedule.id = mktg_for_data.sched_id "+
		"and mktg_schedule.offer_id = ? "+
		"and mktg_data.case_index_id=3030 "+
		"and mktg_data.time_id >= ? " +
		"and mktg_data.time_id <= ? " +
		"group by mktg_for_data.item_id "+
		") sdv "+
		"where "+
		"sdv.i =* il.iid ";


	public static String MKTG_GET_OFFER_WISE_TOTAL_ITEM_SALE =
		"select count(*) v, min(mktg_data.time_id), max(mktg_data.time_id) "+
		"from "+
		"mktg_data, mktg_for_data, mktg_schedule "+
		"where "+
		"mktg_data.for_data_id = mktg_for_data.id "+
		"and mktg_schedule.id = mktg_for_data.sched_id "+
		"and mktg_schedule.offer_id = ? "+
		"and mktg_data.case_index_id=3030 "+
		"and mktg_data.time_id >= ? " +
		"and mktg_data.time_id <= ? ";

	public static String MKTG_GET_OFFER_WISE_TOTAL_RESP =
		"select sum(mktg_data.val) "+
		"from "+
		"mktg_data, mktg_for_data, mktg_schedule "+
		"where "+
		"mktg_data.for_data_id = mktg_for_data.id "+
		"and mktg_schedule.id = mktg_for_data.sched_id "+
		"and mktg_schedule.offer_id = ? "+
		"and mktg_data.case_index_id=3028 "+
		"and mktg_data.time_id >= ? " +
		"and mktg_data.time_id <= ? ";

	public static String MKTG_GET_RELEVANT_OFFER_DETAIL =
		"select gl.offer_id oid "+
		",gl.id gid "+
		",gd.item_id iid "+
		",gl.min_quantity mi "+
		",gl.max_quantity mx "+
		",gd.quantity "+
		",gd.expected_percentage "+
		",gl.expected_quantity "+
          ",gd.visit_sequence vsid"+
          ",gd.expected_delay "+
          ",gd.visit_type "+
          ",gd.visit_week "+
          ",gd.visit_skippable "+
		"from mktg_item_group_list gl, mktg_item_group_detail gd where "+
		"1=1 "+
		"and gd.group_id = gl.id "+
		"and gl.item_sub_type=1 "+
		"and offer_id=? "+
		"order by gid, vsid, iid";


	// Sameer 03/23/2006
	public static String SELECT_MKTG_CAMPAIGN_AVAILABLE =
		"select name, id from mktg_campaign";

	// sameer 04032006

	
	public static String GET_MEASURE_DATA_BY_YEAR_MOD_PART1 =
		"select measure_case_index.measure_id, 1, time_id, sum(measure_data.val), " +
		"target_market, outcome_or_phase_id, break_down, classify1, classify2, " +
		"classify3, classify4, classify5 " +
		"from measure_data, measure_case_index, year_timeid, measure_map_items " +
		"where target_market = ? " +
		"and measure_map_items.wspace_id = ? " +
		"and measure_map_items.alt_id = ? " +
		"and measure_map_items.isdefault = 1 ";

	public static String GET_MEASURE_DATA_BY_YEAR_MOD_PART2 =
		"and measure_data.alt_measure_id = measure_map_items.alt_measure_id " +
		"and measure_data.measure_case_index_id = measure_case_index.id " +
		"and measure_case_index.measure_id = measure_map_items.measure_id " +
		"and measure_data.time_val >= time_id and measure_data.time_val < time_id+35*12 " +
		"group by measure_case_index.measure_id, time_id, target_market, " +
		"outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, " +
		"classify5 " +
		"order by measure_case_index.measure_id, target_market, break_down, " +
		"outcome_or_phase_id, classify1, classify2, classify3, classify4, classify5, " +
		"time_id";

	public static String MKTG_GET_OFFER_FOR_SELECT = "select mktg_offer.name, mktg_offer.id from mktg_offer where  status <> 4 order by mktg_offer.name";
     public static String MKTG_UPDATE_OFFER_CODE_FOR_CAMPAIGN = "update mktg_campaign set offer_id = ? where id = ?";

     public static String MKTG_GET_AVERAGE_VISIT_GAP = "select avg(curr.on_date - prev.on_date)/7, curr.visit_seq from "+
          " visit_patient_actual curr, visit_patient_actual prev "+
          " , "+
          " (select mktg_schedule.id sched_id from mktg_schedule,mktg_campaign where "+
          " mktg_campaign.offer_id = ? and mktg_schedule.campaign_id = mktg_campaign.id) schedlist "+
          " where "+
          " curr.sched_id = schedlist.sched_id "+
          " and prev.sched_id = curr.sched_id "+
          " and prev.patient_id = curr.patient_id "+
          " and ( "+
          "  (curr.visit_seq < 100000 and prev.visit_seq = curr.visit_seq-1) or "+
          "  (curr.visit_seq >= 100000 and prev.visit_seq = 0) "+
          "  ) "+
          " and (curr.on_date <= ?) "+
          " group by curr.visit_seq order by curr.visit_seq";

       public static String MKTG_GET_VISIT_COUNTS_AT = Misc.G_DO_ORACLE ?
       " select actualCountAtX.cnt act, mustCountAtX.cnt must, mustCountAtX.vs "+
          " from "+
          "     (select count(*) cnt, visit_seq vs "+
          "        from "+
          "        visit_patient_actual "+
          "        , "+
          "        (select mktg_schedule.id sched_id from mktg_schedule "+
          "         ,mktg_campaign where "+
          "        mktg_campaign.offer_id = ? and "+
          "        mktg_schedule.campaign_id = mktg_campaign.id) schedlist "+
          "        where "+
          "        visit_patient_actual.sched_id = schedlist.sched_id "+
          "        and (visit_patient_actual.on_date <= ?) "+
          "        group by visit_seq "+
          "     ) ActualcountAtX "+
          "     , "+
          "     ( "+
          " select count(*) cnt, "+
          " seql.vs "+
          "  from "+
          " (select visit_sequence vs from mktg_item_group_detail, mktg_item_group_list "+
          " where mktg_item_group_list.offer_id = ? "+
          " and mktg_item_group_detail.group_id = mktg_item_group_list.id) seql "+
          " , "+
          " (select distinct sched_id, patient_id from visit_patient_actual) master "+
          "  , "+
          " (select avg(curr.on_date - prev.on_date) + 1 * "+
          "                    stddev(curr.on_date-prev.on_date) durgap "+
          "                    , curr.visit_seq vs from "+
          "                    visit_patient_actual curr, visit_patient_actual prev "+
          "                    , "+
          "                    (select mktg_schedule.id sched_id from mktg_schedule "+
          "                    ,mktg_campaign where "+
          "                    mktg_campaign.offer_id = ? and "+
          "                    mktg_schedule.campaign_id = mktg_campaign.id) schedlist "+
          "                    where "+
          "                     curr.sched_id = schedlist.sched_id "+
          "                     and prev.sched_id = curr.sched_id "+
          "                     and prev.patient_id = curr.patient_id "+
          "                     and prev.visit_seq = 0 "+
          "                     and (curr.on_date <= ?) "+
          "                   group by curr.visit_seq "+
          "                 ) gapinfo "+
          " , "+
          "  (select mktg_schedule.id sched_id from mktg_schedule "+
          "     ,mktg_campaign where "+
          "  mktg_campaign.offer_id = ? "+
          "  and mktg_schedule.campaign_id = mktg_campaign.id) schedlist "+
          "  where "+
          "  master.sched_id = schedList.sched_id "+
          "  and gapInfo.vs =seql.vs "+
          "  and ( "+
          "     exists (select 1 from  visit_patient_actual il where "+
          "         il.sched_id = master.sched_id "+
          "         and il.patient_id = master.patient_id "+
          "         and ( "+
          "                (il.visit_seq >= seql.vs "+
          "                  and il.visit_seq < 100000) "+
          "                or "+
          "                (il.visit_seq >= 300000) "+
          "             ) "+
          "         and (il.on_date <= ?) "+
          "      ) "+
          "      or "+
          "      exists(select 1 "+
          "         from  visit_patient_actual il "+
          "          where "+
          "          il.sched_id = master.sched_id "+
          "          and master.patient_id = il.patient_id "+
          "          and il.visit_seq = 0 "+
          "          and gapinfo.vs = seql.vs "+
          "          and (?-il.on_date) >= gapinfo.durgap "+
          "       ) "+
          "    ) "+
          "  group by seql.vs "+
          "  ) mustCountAtX "+
          "  where mustCountAtX.vs = actualCountAtX.vs(+) "+
          "  order by mustCountAtX.vs "
          :
          " select actualCountAtX.cnt act, mustCountAtX.cnt must, mustCountAtX.vs "+
          " from "+
          "     (select count(*) cnt, visit_seq vs "+
          "        from "+
          "        visit_patient_actual "+
          "        , "+
          "        (select mktg_schedule.id sched_id from mktg_schedule "+
          "         ,mktg_campaign where "+
          "        mktg_campaign.offer_id = ? and "+
          "        mktg_schedule.campaign_id = mktg_campaign.id) schedlist "+
          "        where "+
          "        visit_patient_actual.sched_id = schedlist.sched_id "+
          "        and (visit_patient_actual.on_date <= ?) "+
          "        group by visit_seq "+
          "     ) ActualcountAtX "+
          "     , "+
          "     ( "+
          " select count(*) cnt, "+
          " seql.vs "+
          "  from "+
          " (select visit_sequence vs from mktg_item_group_detail, mktg_item_group_list "+
          " where mktg_item_group_list.offer_id = ? "+
          " and mktg_item_group_detail.group_id = mktg_item_group_list.id) seql "+
          " , "+
          " (select distinct sched_id, patient_id from visit_patient_actual) master "+
          "  , "+
          " (select avg(curr.on_date - prev.on_date) + 1 * "+
          "                    stddev(curr.on_date-prev.on_date) durgap "+
          "                    , curr.visit_seq vs from "+
          "                    visit_patient_actual curr, visit_patient_actual prev "+
          "                    , "+
          "                    (select mktg_schedule.id sched_id from mktg_schedule "+
          "                    ,mktg_campaign where "+
          "                    mktg_campaign.offer_id = ? and "+
          "                    mktg_schedule.campaign_id = mktg_campaign.id) schedlist "+
          "                    where "+
          "                     curr.sched_id = schedlist.sched_id "+
          "                     and prev.sched_id = curr.sched_id "+
          "                     and prev.patient_id = curr.patient_id "+
          "                     and prev.visit_seq = 0 "+
          "                     and (curr.on_date <= ?) "+
          "                   group by curr.visit_seq "+
          "                 ) gapinfo "+
          " , "+
          "  (select mktg_schedule.id sched_id from mktg_schedule "+
          "     ,mktg_campaign where "+
          "  mktg_campaign.offer_id = ? "+
          "  and mktg_schedule.campaign_id = mktg_campaign.id) schedlist "+
          "  where "+
          "  master.sched_id = schedList.sched_id "+
          "  and gapInfo.vs =seql.vs "+
          "  and ( "+
          "     exists (select 1 from  visit_patient_actual il where "+
          "         il.sched_id = master.sched_id "+
          "         and il.patient_id = master.patient_id "+
          "         and ( "+
          "                (il.visit_seq >= seql.vs "+
          "                  and il.visit_seq < 100000) "+
          "                or "+
          "                (il.visit_seq >= 300000) "+
          "             ) "+
          "         and (il.on_date <= ?) "+
          "      ) "+
          "      or "+
          "      exists(select 1 "+
          "         from  visit_patient_actual il "+
          "          where "+
          "          il.sched_id = master.sched_id "+
          "          and master.patient_id = il.patient_id "+
          "          and il.visit_seq = 0 "+
          "          and gapinfo.vs = seql.vs "+
          "          and (?-il.on_date) >= gapinfo.durgap "+
          "       ) "+
          "    ) "+
          "  group by seql.vs "+
          "  ) mustCountAtX "+
          "  where mustCountAtX.vs *= actualCountAtX.vs "+
          "  order by mustCountAtX.vs ";
          */
  public static String UPDATE_WORKSPACE_CLASSIFY = "update workspaces set classify1=?, classify2=?, classify3=?, classify4=?, classify5=? where workspaces.id = ?";
	public static String GET_MAX_WORKSPACE_CLASSIFY = "select max(classify1), max(classify2), max(classify3), max(classify4), max(classify5) from workspaces where workspaces.prj_id = ?";          
  public final static String GET_DATA_IN_NPV_BY_VER_MOD_PART1 =
		"select npv.npv_type, npv.target_market, npv.classify1, npv.classify2, npv.classify3, npv.classify4, npv.classify5, data.value from npv, data where "+
		"npv.target_market = ? and npv.alt_combined_id = ? and data.npv_id = npv.id ";

	public final static String GET_DATA_IN_NPV_BY_VER_MOD_PART2 =
		"order by npv_type, target_market, classify1, classify2, classify3, classify4, classify5";

	public static String GET_FILE_NAME_FROM_ID = "select name, original_name from file_names where file_name_id = ?";

  //this is going to be the same in msft/orcl ... mimic orcl's sequence in msft
	public static String CREATE_FILE_NAME_MOD = "insert into file_names (file_name_id, mime, name, is_template, extension, original_name) values (?,?,?,0,?, ?)";
  
	//new since sync 041206 ... rajeev

	public static String PREP_NPV_FOR_COPY = "update npv set temp_copy_of = null where alt_combined_id = ?";
  //TODO_INQUERY ... not imp
	public static String COPY_NPV_ITEMS = Misc.G_DO_ORACLE ? 
     "insert into npv (id, npv_type, alt_combined_id, target_market, classify1, classify2, classify3, classify4, classify5, temp_copy_of) (select seq_npv.nextval, n2.npv_type, ? ,n2.target_market, n2.classify1, n2.classify2, n2.classify3, n2.classify4, n2.classify5,  n2.id from npv n2 where n2.alt_combined_id = ? and n2.npv_type not in (select npv_type from npv where alt_combined_id=?))"
     :
     "insert into npv (npv_type, alt_combined_id, target_market, classify1, classify2, classify3, classify4, classify5, temp_copy_of) (select  n2.npv_type, ? ,n2.target_market, n2.classify1, n2.classify2, n2.classify3, n2.classify4, n2.classify5,  n2.id from npv n2 where n2.alt_combined_id = ? and n2.npv_type not in (select npv_type from npv where alt_combined_id=?))";
	public static String COPY_NPV_DATA_ITEMS = Misc.G_DO_ORACLE ?
  "insert into data (year, value, val_dur, val_scope, err_def_type, npv_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, npv n2 where d2.npv_id = n2.temp_copy_of and n2.alt_combined_id=?) "
    :
    "insert into data  (year, value, val_dur, val_scope, err_def_type, npv_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, npv n2 where d2.npv_id = n2.temp_copy_of and n2.alt_combined_id=?) ";
	public static String GET_MILESTONE_DATE_ID_FOR_PRJ =
		"select workspaces.classify1, workspaces.id, alt_map_items.alt_date_id "
		+
		"from workspaces, alt_map_items " +
		"where workspaces.prj_id = ? " +
		"and workspaces.id = alt_map_items.wspace_id " +
		"and alt_map_items.isdefault = 1 " +
		"and workspaces.classify1 is not null " +
		"order by workspaces.classify1, workspaces.id desc";

	public static String GET_MILESTONE_COMBINED_ID_FOR_PRJ =
		"select workspaces.classify1, workspaces.id, alt_map_items.alt_combined_id " +
		"from workspaces, alt_map_items " +
		"where workspaces.prj_id = ? " +
		"and workspaces.id = alt_map_items.wspace_id " +
		"and alt_map_items.isdefault = 1 " +
		"and workspaces.classify1 is not null " +
		"order by workspaces.classify1, workspaces.id desc";

	public static String GET_TOTAL_FOR_NPV_TYPE =
		"select sum(data.value) from data, npv " +
		"where npv.npv_type = ? " +
		"and npv.alt_combined_id = ? " +
		"and data.npv_id = npv.id";

	public static String GET_TOTAL_FOR_NPV_TYPE_BY_PHASE =
		"select sum(data.value) from data, npv " +
		"where npv.npv_type = ? " +
		"and npv.alt_combined_id = ? " +
		"and npv.classify2 = ? " +
		"and data.npv_id = npv.id";

	public static String GET_MILESTONE_ALT_MEASURE_ID_FOR_PRJ =
		"select workspaces.classify1, workspaces.id, measure_map_items.alt_measure_id " +
		"from workspaces, measure_map_items " +
		"where workspaces.prj_id = ? " +
		"and workspaces.id = measure_map_items.wspace_id " +
		"and measure_map_items.alt_id = ? " +
		"and measure_map_items.isdefault = 1 " +
		"and measure_map_items.measure_id = ? " +
		"and workspaces.classify1 is not null " +
		"order by workspaces.classify1, workspaces.id desc";

	public static String GET_TOTAL_FOR_MEASURE_TYPE =
		"select sum(measure_data.val) from measure_data " +
		"where measure_data.alt_measure_id = ? " +
		"and measure_data.time_val >= ? " +
		"and measure_data.time_val < ?";

	// end sameer 04122006

	//rajeev 042006 ... new
	public static String GET_SCEN_INFO = "select scen_id, scen_name, scen_desc, scen_prob, delay_val from alt_scen_list where alt_id = ? order by scen_id";
	public static String UPD_SCEN_INFO = "update alt_scen_list set scen_name = ?, scen_desc = ?, scen_prob = ?, delay_val = ? where alt_id = ? and scen_id = ?";
	public static String INS_SCEN_INFO = "insert into alt_scen_list (alt_id, scen_id, scen_name, scen_desc, scen_prob, delay_val) values (?, ?, ?, ?, ?,?)";
	public static String GET_ALL_MEASURE_MAPS = "select measure_id, alt_measure_id from measure_map_items where alt_id = ? and wspace_id = ? and isdefault=1";
	//  public static String GET_DATA_LOAD_FACTORS = "select distinct is_copy, file_id, scen_id, target_market, classify1, classify2, classify3, classify4, classify5 from model_scen_spec where ref_model_id=? and ((? <> -1 and section_id = ?) or (? = -1 and measure_id = ?)) order by file_id desc";
	public static String GET_DATA_LOAD_FACTORS = "select distinct is_copy, file_id, scen_id, target_market, classify1, classify2, classify3, classify4, classify5 from model_scen_spec where ref_model_id=? and measure_id = ? order by file_id desc";


	public static String COPY_REV_SEGS_PARTIAL = Misc.G_DO_ORACLE ?
  "insert into rev_segs (id, alt_rev_id, temp_copy_of, name, classify1, classify2, classify3, classify4, classify5, start_date, start_year, mkt_type, seg_id, is_short_term, scen_id) " +
		"(select seq_rev_segs.nextval, ?, rev_segs.id, rev_segs.name, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5, rev_segs.start_date, rev_segs.start_year, rev_segs.mkt_type, rev_segs.seg_id, rev_segs.is_short_term, rev_segs.scen_id from rev_segs where rev_segs.alt_rev_id = ? " //and/or clause of what to copy followed by );
    :
  "insert into rev_segs (alt_rev_id, temp_copy_of, name, classify1, classify2, classify3, classify4, classify5, start_date, start_year, mkt_type, seg_id, is_short_term, scen_id) " +
		"(select ?, rev_segs.id, rev_segs.name, rev_segs.classify1, rev_segs.classify2, rev_segs.classify3, rev_segs.classify4, rev_segs.classify5, rev_segs.start_date, rev_segs.start_year, rev_segs.mkt_type, rev_segs.seg_id, rev_segs.is_short_term, rev_segs.scen_id from rev_segs where rev_segs.alt_rev_id = ? "; //and/or clause of what to copy followed by );
	public static String COPY_OPCOST_PARTIAL = Misc.G_DO_ORACLE ? 
  "insert into cost_items (id, alt_opcost_id, temp_copy_of, name, classify1, classify2, classify3, classify4, classify5, start_date, end_date, target_market, lineitem_id, is_short_term, scen_id, cost_cent_id, expense_ty, for_achieving_milestone, task_internal_id, to_include, is_fte) " +
		"(select seq_cost_items.nextval, ?, cost_items.id, cost_items.name, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, "+ 
    "cost_items.classify5, cost_items.start_date, cost_items.end_date, cost_items.target_market, cost_items.lineitem_id, cost_items.is_short_term, cost_items.scen_id, "+ 
    " cost_items.cost_cent_id, cost_items.expense_ty, cost_items.for_achieving_milestone, cost_items.task_internal_id, cost_items.to_include, cost_items.is_fte from "+ 
    " cost_items where cost_items.alt_opcost_id = ? " //and.or clause followed by );
:
  "insert into cost_items (alt_opcost_id, temp_copy_of, name, classify1, classify2, classify3, classify4, classify5, start_date, end_date, target_market, lineitem_id, is_short_term, scen_id, cost_cent_id, expense_ty, for_achieving_milestone, task_internal_id, to_include, is_fte) " +
		"(select  ?, cost_items.id, cost_items.name, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.start_date, cost_items.end_date, cost_items.target_market, cost_items.lineitem_id, cost_items.is_short_term, cost_items.scen_id, cost_items.cost_cent_id, cost_items.expense_ty, cost_items.for_achieving_milestone, cost_items.task_internal_id, cost_items.to_include, cost_items.is_fte from cost_items where cost_items.alt_opcost_id = ? ";//and.or clause followed by );
	public static String COPY_DEVCOST_PARTIAL = Misc.G_DO_ORACLE ?
  "insert into cost_items (id, alt_devcost_id, temp_copy_of, name, classify1, classify2, classify3, classify4, classify5, start_date, end_date, target_market, lineitem_id, is_short_term, scen_id, cost_cent_id, expense_ty, for_achieving_milestone, task_internal_id, to_include, is_fte) " +
		"(select seq_cost_items.nextval, ?, cost_items.id, cost_items.name, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, "+ 
    " cost_items.classify5, cost_items.start_date, cost_items.end_date, cost_items.target_market, cost_items.lineitem_id, cost_items.is_short_term, cost_items.scen_id, "+ 
    " cost_items.cost_cent_id, cost_items.expense_ty, cost_items.for_achieving_milestone, cost_items.task_internal_id, cost_items.to_include, cost_items.is_fte from cost_items "+
    " where cost_items.alt_devcost_id = ? "//and.or clause followed by );
    :
  "insert into cost_items (alt_devcost_id, temp_copy_of, name, classify1, classify2, classify3, classify4, classify5, start_date, end_date, target_market, lineitem_id, is_short_term, scen_id, cost_cent_id, expense_ty, for_achieving_milestone, task_internal_id, to_include, is_fte) " +
		"(select  ?, cost_items.id, cost_items.name, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.start_date, cost_items.end_date, cost_items.target_market, cost_items.lineitem_id, cost_items.is_short_term, cost_items.scen_id, cost_items.cost_cent_id, cost_items.expense_ty, cost_items.for_achieving_milestone, cost_items.task_internal_id, cost_items.to_include, cost_items.is_fte from cost_items where cost_items.alt_devcost_id = ? ";//and.or clause followed by );
	public static String COPY_FTE_PARTIAL = Misc.G_DO_ORACLE ?
  "insert into fte_items (id, alt_fte_id, temp_copy_of, name, classify1, classify2, classify3, classify4, classify5, start_date, end_date, target_market, fte_lineitem_id, is_short_term, scen_id, fte_head_id, for_achieving_milestone, task_internal_id, to_include, for_skill, assignment_status) " +
		"(select seq_fte_items.nextval, ?, fte_items.id, fte_items.name, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, "+ 
    " fte_items.start_date, fte_items.end_date, fte_items.target_market, fte_items.fte_lineitem_id, fte_items.is_short_term, fte_items.scen_id, fte_items.fte_head_id, "+
    " fte_items.for_achieving_milestone, fte_items.task_internal_id, fte_items.to_include, fte_items.for_skill, fte_items.assignment_status from fte_items where "+
    " fte_items.alt_fte_id = ? "//and.or clause followed by );
  :
  "insert into fte_items (alt_fte_id, temp_copy_of, name, classify1, classify2, classify3, classify4, classify5, start_date, end_date, target_market, fte_lineitem_id, is_short_term, scen_id, fte_head_id, for_achieving_milestone, task_internal_id, to_include, for_skill, assignment_status) " +
		"(select  ?, fte_items.id, fte_items.name, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.start_date, fte_items.end_date, fte_items.target_market, fte_items.fte_lineitem_id, fte_items.is_short_term, fte_items.scen_id, fte_items.fte_head_id, fte_items.for_achieving_milestone, fte_items.task_internal_id, fte_items.to_include, fte_items.for_skill, fte_items.assignment_status from fte_items where fte_items.alt_fte_id = ? ";//and.or clause followed by );
	public static String COPY_NPV_PARTIAL = Misc.G_DO_ORACLE ?
  "insert into npv (id, alt_combined_id, npv_type, temp_copy_of, classify1, classify2, classify3, classify4, classify5, target_market, scen_id) " +
		"(select seq_npv.nextval, ?, npv_type, npv.id, npv.classify1, npv.classify2, npv.classify3, npv.classify4, npv.classify5, npv.target_market, npv.scen_id from npv where npv.alt_combined_id = ? and npv_type = ? "//and.or clause followed by );
    :
  "insert into npv (alt_combined_id, npv_type, temp_copy_of, classify1, classify2, classify3, classify4, classify5, target_market, scen_id) " +
		"(select ?, npv_type, npv.id, npv.classify1, npv.classify2, npv.classify3, npv.classify4, npv.classify5, npv.target_market, npv.scen_id from npv where npv.alt_combined_id = ? and npv_type = ? ";//and.or clause followed by );
	public static String COPY_MEASURE_PARTIAL = "insert into measure_data (alt_measure_id, measure_case_index_id, val, time_val, val_scope, val_dur) "+
		"(select ?, measure_case_index_id, val, time_val, val_scope, val_dur from measure_data, measure_case_index where measure_data.alt_measure_id = ? and measure_data.measure_case_index_id = measure_case_index.id ";//and clause followed by )

	public static String COPY_REV_DATA_ITEMS = Misc.G_DO_ORACLE ?
  "insert into data d1 (year, value, val_dur, val_scope, err_def_type, rev_seg_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, rev_segs n2 where n2.alt_rev_id=? and d2.rev_seg_id = n2.temp_copy_of)"
    :
    "insert into data  (year, value, val_dur, val_scope, err_def_type, rev_seg_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, rev_segs n2 where n2.alt_rev_id=? and d2.rev_seg_id = n2.temp_copy_of)";
	public static String COPY_NPV_DATA_ITEMS_SIMPLE = Misc.G_DO_ORACLE ?
  "insert into data d1 (year, value, val_dur, val_scope, err_def_type, npv_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, npv n2 where n2.alt_combined_id = ? and n2.npv_type = ? and d2.npv_id = n2.temp_copy_of) "
    :
    "insert into data  (year, value, val_dur, val_scope, err_def_type, npv_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, npv n2 where n2.alt_combined_id = ? and n2.npv_type = ? and d2.npv_id = n2.temp_copy_of) ";
	public static String COPY_DEVCOST_DATA_ITEMS = Misc.G_DO_ORACLE ?
  "insert into data d1 (year, value, val_dur, val_scope, err_def_type, cost_li_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, cost_items n2 where n2.alt_devcost_id = ? and d2.cost_li_id = n2.temp_copy_of)"
    :
  "insert into data  (year, value, val_dur, val_scope, err_def_type, cost_li_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, cost_items n2 where n2.alt_devcost_id = ? and d2.cost_li_id = n2.temp_copy_of)";
	public static String COPY_OPCOST_DATA_ITEMS = Misc.G_DO_ORACLE ?
  "insert into data d1 (year, value, val_dur, val_scope, err_def_type, cost_li_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, cost_items n2 where n2.alt_opcost_id=? and d2.cost_li_id = n2.temp_copy_of)"
    :
  "insert into data (year, value, val_dur, val_scope, err_def_type, cost_li_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, cost_items n2 where n2.alt_opcost_id=? and d2.cost_li_id = n2.temp_copy_of)";
	public static String COPY_FTE_DATA_ITEMS = Misc.G_DO_ORACLE ?
  "insert into data d1 (year, value, val_dur, val_scope, err_def_type, fte_item_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, fte_items n2 where n2.alt_fte_id = ? and d2.fte_item_id = n2.temp_copy_of)"
    :
  "insert into data  (year, value, val_dur, val_scope, err_def_type, fte_item_id) "+
		"(select d2.year, d2.value, d2.val_dur, d2.val_scope, d2.err_def_type, n2.id from data d2, fte_items n2 where n2.alt_fte_id = ? and d2.fte_item_id = n2.temp_copy_of)";
//TODO_INQUERY 
	public static String DELETE_MEASURE_DATA = Misc.G_DO_ORACLE ? "delete from measure_data where alt_measure_id = ? and measure_case_index_id in (select measure_case_index.id from measure_case_index where measure_id = ? " ////and or clause and then )
  :
  "delete from measure_data from measure_case_index where (measure_data.alt_measure_id = ? and measure_data.measure_case_index_id = measure_case_index.id and measure_case_index.measure_id = ?  "; ////and or clause and then )
	public static String DELETE_MEASURE_DATA_ALL = "delete from measure_data where alt_measure_id = ? ";
//TODO_INQUERY  
		public static String DELETE_DATA_REV = Misc.G_DO_ORACLE ?
		"delete from (select data.id from data, rev_segs where rev_segs.alt_rev_id = ? and data.rev_seg_id = rev_segs.id "//and or clause followed by )// and rev_segs.temp_copy_of = 1)";
		:
		"delete from data from rev_segs where (rev_segs.alt_rev_id = ? and data.rev_seg_id = rev_segs.id ";
//TODO_INQUERY    
	public static String DELETE_DATA_OPCOST = Misc.G_DO_ORACLE ?
		"delete from (select data.id from data, cost_items where cost_items.alt_opcost_id = ? and data.cost_li_id = cost_items.id "//and or clause followed by )// and rev_segs.temp_copy_of = 1)";
		:
		"delete from data from cost_items where (cost_items.alt_opcost_id = ? and data.cost_li_id = cost_items.id ";//and or clause followed by )// and rev_segs.temp_copy_of = 1)";
//TODO_INQUERY    
	public static String DELETE_DATA_DEVCOST = Misc.G_DO_ORACLE ?
		"delete from (select data.id from data, cost_items where cost_items.alt_devcost_id = ? and data.cost_li_id = cost_items.id "//and or clause followed by )
		:
		"delete from data from cost_items where (cost_items.alt_devcost_id = ? and data.cost_li_id = cost_items.id ";//and or clause followed by )
//TODO_INQUERY    
	public static String DELETE_DATA_FTE = Misc.G_DO_ORACLE ?
		"delete from (select data.id from data, fte_items where fte_items.alt_fte_id = ? and data.fte_item_id = fte_items.id "//and or clause followed by )
		:
		"delete from data from fte_items where (fte_items.alt_fte_id = ? and data.fte_item_id = fte_items.id ";//and or clause followed by )
//TODO_INQUERY    
	public static String DELETE_DATA_NPV = Misc.G_DO_ORACLE ?
		"delete from (select data.id from data, npv where npv.alt_combined_id =? and npv.npv_type = ? and data.npv_id = npv.id "//and or clause followed by )
		:
		"delete from data from npv where (npv.alt_combined_id =? and npv.npv_type = ? and data.npv_id = npv.id ";//and or clause followed by )
		public static String DELETE_REV_SEGS = 
		"delete from rev_segs where (rev_segs.alt_rev_id = ? ";//and or clause followed by )
//TODO_INQUERY    
	public static String DELETE_OPCOST_ITEMS = 
		"delete from cost_items where (cost_items.alt_opcost_id = ? ";//and or clause followed by )

	public static String DELETE_DEVCOST_ITEMS = "delete from cost_items where (cost_items.alt_devcost_id = ? ";//and or clause followed by )
	public static String DELETE_FTE_ITEMS = "delete from fte_items where (fte_items.alt_fte_id=? ";//and or clause followed by )
	public static String DELETE_NPV = "delete from npv where (npv.alt_combined_id=? and npv.npv_type = ? ";//and or clause followed by )
	public static String DELETE_MEASURE_CASE_INDEX = "delete from measure_case_index where measure_id=? and not exists(select 1 from measure_data, measure_case_index where measure_data.measure_case_index_id = measure_case_index.id)";

	//   public static String COPY_MODEL_SCEN_SPEC_PARTIAL = "insert into model_scen_spec (is_copy, ref_model_id, measure_id, section_id, file_id, scen_id, target_market, classify1, classify2, classify3, classify4, classify5) "+
	//       "(select 1, ?, measure_id, section_id, file_id, scen_id, target_market, classify1, classify2, classify3, classify4, classify5 from model_scen_spec where ref_model_id = ? and ((? <> -1 and section_id = ?) or (? = -1 and measure_id = ?)) "; //followed by and.or and ))
	public static String COPY_MODEL_SCEN_SPEC_PARTIAL = "insert into model_scen_spec (is_copy, ref_model_id, measure_id,  file_id, scen_id, target_market, classify1, classify2, classify3, classify4, classify5) "+
		"(select 1, ?, measure_id,  file_id, scen_id, target_market, classify1, classify2, classify3, classify4, classify5 from model_scen_spec where ref_model_id = ? and measure_id = ? "; //followed by and.or and ))

	// public static String DELETE_MODEL_SCEN_SPEC_PARTIAL = "delete from model_scen_spec where ref_model_id = ? and ((? <> -1 and section_id = ?) or (? = -1 and measure_id = ?)) "; //and.or clause
	public static String DELETE_MODEL_SCEN_SPEC_PARTIAL = "delete from model_scen_spec where ref_model_id = ? and measure_id = ? "; //and.or clause
	//   public static String INSERT_MODEL_SCEN_SPEC = "insert into model_scen_spec (is_copy, ref_model_id, section_id, measure_id, file_id, scen_id, target_market, classify1, classify2, classify3, classify4, classify5) values (0,?,?,?,?,?,?,?,?,?,?,?)";
	public static String INSERT_MODEL_SCEN_SPEC = "insert into model_scen_spec (is_copy, ref_model_id, measure_id, file_id, scen_id, target_market, classify1, classify2, classify3, classify4, classify5) values (0,?,?,?,?,?,?,?,?,?,?)";
//TODO_INQUERY
	public static String SCEN_DATA_REV_DEL = Misc.G_DO_ORACLE ? "delete from data where data.id in (select data.id from data, rev_segs,  alt_rev_model where data.rev_seg_id = rev_segs.id  and rev_segs.alt_rev_id = alt_rev_model.id and alt_rev_model.alt_id = ? and rev_segs.scen_id in " //followed by )
  :
  "delete from data from rev_segs, alt_rev_model where (data.rev_seg_id = rev_segs.id  and rev_segs.alt_rev_id = alt_rev_model.id and alt_rev_model.alt_id = ? and rev_segs.scen_id in "; //followed by )
  //TODO_INQUERY
	public static String SCEN_DATA_OPCOST_DEL = Misc.G_DO_ORACLE ? "delete from data where data.id in (select data.id from data, cost_items,  alt_opcost_model where data.cost_li_id = cost_items.id and cost_items.alt_opcost_id = alt_opcost_model.id and alt_opcost_model.alt_id = ? and cost_items.scen_id in "
  :
  "delete from data from cost_items,alt_opcost_model where (data.cost_li_id = cost_items.id and cost_items.alt_opcost_id = alt_opcost_model.id and alt_opcost_model.alt_id = ? and cost_items.scen_id in ";//followed by )
  //TODO_INQUERY
	public static String SCEN_DATA_DEVCOST_DEL = Misc.G_DO_ORACLE ? "delete from data where data.id in (select data.id from data, cost_items, alt_devcost_model where data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = alt_devcost_model.id and alt_devcost_model.alt_id = ? and scen_id in "
  :
  "delete from data from cost_items, alt_devcost_model where (data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = alt_devcost_model.id and alt_devcost_model.alt_id = ? and scen_id in ";//followed by )
  //TODO_INQUERY
	public static String SCEN_DATA_FTE_DEL = Misc.G_DO_ORACLE ? "delete from data where data.id in (select data.id from data, fte_items, alt_fte_model where data.fte_item_id = fte_items.id and fte_items.alt_fte_id = alt_fte_model.id and alt_fte_model.alt_id = ? and  scen_id in "
  :
  "delete from data from fte_items, alt_fte_model where (data.fte_item_id = fte_items.id and fte_items.alt_fte_id = alt_fte_model.id and alt_fte_model.alt_id = ? and  scen_id in ";//followed by )
  //TODO_INQUERY
	public static String SCEN_DATA_NPV_DEL = Misc.G_DO_ORACLE ? "delete from data where data.id in (select data.id from data, npv, alt_combined_model where data.npv_id = npv.id and npv.alt_combined_id = alt_combined_model.id and alt_combined_model.alt_id = ? and scen_id in "
  :
  "delete from data from npv, alt_combined_model where (data.npv_id = npv.id and npv.alt_combined_id = alt_combined_model.id and alt_combined_model.alt_id = ? and scen_id in ";//followed by )
  //TODO_INQUERY
	public static String SCEN_DATA_MEASURE_DEL = Misc.G_DO_ORACLE ?
  "delete from measure_data where alt_measure_id in (select alt_measures.id from alt_measures where alt_id = ? ) and measure_case_index_id in (select measure_case_index.id from measure_case_index where scen_id in " //followed by )
  :
  "delete from measure_data from alt_measures, measure_case_index where (measure_data.alt_measure_id = alt_measures.id and alt_id = ? and measure_data.measure_case_index_id = measure_case_index.id and scen_id in "; //followed by )
//TODO_INQUERY
	public static String SCEN_REV_DEL = Misc.G_DO_ORACLE ? "delete from rev_segs where id in (select rev_segs.id from rev_segs, alt_rev_model where  rev_segs.alt_rev_id = alt_rev_model.id and alt_rev_model.alt_id = ? and rev_segs.scen_id in "
  :
  "delete from rev_segs from alt_rev_model where (rev_segs.alt_rev_id = alt_rev_model.id and alt_rev_model.alt_id = ? and rev_segs.scen_id in ";//followed by )
//TODO_INQUERY  
	public static String SCEN_OPCOST_DEL = Misc.G_DO_ORACLE ? "delete from cost_items where id in (select cost_items.id from cost_items,  alt_opcost_model where cost_items.alt_opcost_id = alt_opcost_model.id and alt_opcost_model.alt_id = ? and cost_items.scen_id in "
  :
  "delete from cost_items from alt_opcost_model where (cost_items.alt_opcost_id = alt_opcost_model.id and alt_opcost_model.alt_id = ? and cost_items.scen_id in ";//followed by )
  //TODO_INQUERY
	public static String SCEN_DEVCOST_DEL = Misc.G_DO_ORACLE ? "delete from cost_items where id in (select cost_items.id from cost_items, alt_devcost_model where cost_items.alt_devcost_id = alt_devcost_model.id and alt_devcost_model.alt_id = ? and scen_id in "
  :
  "delete from cost_items from alt_devcost_model where (cost_items.alt_devcost_id = alt_devcost_model.id and alt_devcost_model.alt_id = ? and scen_id in ";//followed by )
  //TODO_INQUERY
	public static String SCEN_FTE_DEL = Misc.G_DO_ORACLE ? "delete from fte_items where id in (select fte_items.id from fte_items, alt_fte_model where fte_items.alt_fte_id = alt_fte_model.id and alt_fte_model.alt_id = ? and  scen_id in "
  :
  "delete from fte_items from alt_fte_model where (fte_items.alt_fte_id = alt_fte_model.id and alt_fte_model.alt_id = ? and  scen_id in ";//followed by )
  //TODO_INQUERY
	public static String SCEN_NPV_DEL = Misc.G_DO_ORACLE ? "delete from npv where id in (select npv.id from npv, alt_combined_model where npv.alt_combined_id = alt_combined_model.id and alt_combined_model.alt_id = ? and scen_id in "
  :
  "delete from npv from alt_combined_model where (npv.alt_combined_id = alt_combined_model.id and alt_combined_model.alt_id = ? and scen_id in ";//followed by )
  //TODO_INQUERY

//TODO_INQUERY
	public static String DEL_MODEL_SPEC_SCEN_REV = Misc.G_DO_ORACLE ? "delete from model_scen_spec where measure_id = 19 and ref_model_id in (select id from alt_rev_model where alt_id = ?) and scen_id in "
  :
  "delete from model_scen_spec from alt_rev_model where model_scen_spec.measure_id = 19 and model_scen_spec.ref_model_id = alt_rev_model.id and alt_rev_model.alt_id = ? and scen_id in "; //no )
//TODO_INQUERY  
	public static String DEL_MODEL_SPEC_SCEN_OPCOST = Misc.G_DO_ORACLE ? "delete from model_scen_spec where measure_id = 42 and ref_model_id in (select id from alt_opcost_model where alt_id = ?) and scen_id in "
  :
  "delete from model_scen_spec from alt_opcost_model where measure_id = 42 and ref_model_id = alt_opcost_model.id and alt_opcost_model.alt_id = ? and scen_id in "; //no )
  //TODO_INQUERY
	public static String DEL_MODEL_SPEC_SCEN_DEVCOST = Misc.G_DO_ORACLE ? "delete from model_scen_spec where measure_id = 41 and ref_model_id in (select id from alt_devcost_model where alt_id = ?) and scen_id in "
  :
  "delete from model_scen_spec from alt_devcost_model where measure_id = 41 and ref_model_id = alt_devcost_model.id and alt_devcost_model.alt_id = ? and scen_id in "; //no )//no )
  //TODO_INQUERY
	public static String DEL_MODEL_SPEC_SCEN_FTE = Misc.G_DO_ORACLE ? "delete from model_scen_spec where measure_id = 26 and ref_model_id in (select id from alt_fte_model where alt_id = ?) and scen_id in "
  :
  "delete from model_scen_spec from alt_fte_model where measure_id = 26 and ref_model_id = alt_fte_model.id and alt_fte_model.alt_id = ? and scen_id in "; //no )//no )
  //TODO_INQUERY
	//   public static String DEL_MODEL_SPEC_SCEN_NPV = "delete from model_scen_spec where section_id = 13 and (ref_model_id) in (select alt_combined_id from npv, alt_combined_model where npv.alt_combined_id = alt_combined_model.id and alt_id = ?) and scen_id in "; //no )
	public static String DEL_MODEL_SPEC_SCEN_NPV = Misc.G_DO_ORACLE ? "delete from model_scen_spec where (measure_id, ref_model_id) in (select npv_type, alt_combined_id from npv, alt_combined_model where npv.alt_combined_id = alt_combined_model.id and alt_id = ?) and scen_id in "
  :
  "delete from model_scen_spec from npv, alt_combined_id where measure_id = npv_type and ref_model_id=alt_combined_id.id and  npv.alt_combined_id = alt_combined_model.id and alt_combined_model.alt_id = ? and scen_id in "; //no )//no )
  //TODO_INQUERY
	public static String DEL_MODEL_SPEC_SCEN_MEASURE = Misc.G_DO_ORACLE ? "delete from model_scen_spec where (measure_id,ref_model_id) in (select measure_id, id from alt_measures where alt_id=?) and scen_id in "
  :
  "delete from model_scen_spec from alt_measures where model_scen_spec.measure_id = alt_measures.measure_id and model_scen_spec.ref_model_id = alt_measures.id and alt_measures.alt_id=? and scen_id in "; //no )//no )
	public static String DEL_MODEL_SPEC_SCEN_MEASURE_CASE = "delete from measure_case_index where not exists(select 1 from measure_data, measure_case_index where measure_data.measure_case_index_id = measure_case_index.id) and scen_id in "; //no )
	public static String DEL_SCENARIO_BYSCEN = "delete from alt_scen_list where alt_id = ? and scen_id in ";

	public static String GET_DEP_INFO = "select to_prj_id, to_ms_id, rule_type, prj_dep_or_id, from_prj_id, from_ms_id, succ_or_incl from port_results, prj_dep_ors, prj_dep_ands where port_results.port_rs_id = ? and port_results.prj_id = prj_dep_ors.to_prj_id and prj_dep_or_id = prj_dep_ors.id order by rule_type, to_prj_id, to_ms_id, prj_dep_or_id, from_prj_id";
	public static String GET_DEP_INCL_RULES_FOR_PROJECT =
		"select prj_dep_ors.id, prj_dep_ors.name, prj_dep_ands.from_prj_id, " +
		"projects.name, " +
		"(case " +
		"	when prj_dep_ands.succ_or_incl = 1 then 'Include' " +
		"	when prj_dep_ands.succ_or_incl = 0 then 'Exclude' " +
		"	else 'Include' " +
		"end) incl " +
		"from prj_dep_ors, prj_dep_ands, projects " +
		"where prj_dep_ors.to_prj_id = ? " +
		"and prj_dep_ors.rule_type = ? " +
		"and prj_dep_ands.prj_dep_or_id = prj_dep_ors.id " +
		"and projects.id = prj_dep_ands.from_prj_id " +
		"order by prj_dep_ors.id";

	public static String GET_DEP_GONOGO_RULES_FOR_PROJECT =
		"select prj_dep_ors.id, prj_dep_ors.name, prj_dep_ands.from_prj_id, " +
		"projects.name, " +
		"(case " +
		"	when prj_dep_ands.succ_or_incl = 1 then 'Success' " +
		"	when prj_dep_ands.succ_or_incl = 0 then 'Failure' " +
		"	else 'Success' " +
		"end) incl, " +
		"prj_dep_ors.to_ms_id, prj_dep_ands.from_ms_id " +
		"from prj_dep_ors, prj_dep_ands, projects " +
		"where prj_dep_ors.to_prj_id = ? " +
		"and prj_dep_ors.rule_type = ? " +
		"and prj_dep_ands.prj_dep_or_id = prj_dep_ors.id " +
		"and projects.id = prj_dep_ands.from_prj_id " +
		"order by prj_dep_ors.id, prj_dep_ors.to_ms_id";

	public static String GET_INCL_RULE_DETAIL =
		"select prj_dep_ors.id, prj_dep_ors.name, prj_dep_ands.from_prj_id, " +
		"projects.name, prj_dep_ands.succ_or_incl " +
		"from prj_dep_ors, prj_dep_ands, projects " +
		"where prj_dep_ors.id = ? " +
		"and prj_dep_ands.prj_dep_or_id = prj_dep_ors.id " +
		"and projects.id = prj_dep_ands.from_prj_id " +
		"order by prj_dep_ands.from_prj_id";

	public static String GET_GONOGO_RULE_DETAIL =
		"select prj_dep_ors.id, prj_dep_ors.name, prj_dep_ands.from_prj_id, " +
		"projects.name, prj_dep_ands.succ_or_incl, " +
		"prj_dep_ands.from_ms_id " +
		"from prj_dep_ors, prj_dep_ands, projects " +
		"where prj_dep_ors.id = ? " +
		"and prj_dep_ors.to_ms_id = ? " +
		"and prj_dep_ands.prj_dep_or_id = prj_dep_ors.id " +
		"and projects.id = prj_dep_ands.from_prj_id " +
		"order by prj_dep_ands.from_prj_id, prj_dep_ands.from_ms_id";

	public static String GET_PROJECT_LIST_EXCLUDING_CURRENT =
		"select name, id from projects where id <> ? and status in (2,5) " +
		"order by name";
	public static String GET_TEMPLATE_LIST_EXCLUDING_CURRENT =
		"select name, id from projects where id <> ? and status in (7) " +
		"order by name";

	public static String INSERT_PRJ_DEP_OR_RULE =
		"insert into prj_dep_ors (id, to_prj_id, to_ms_id, rule_type, name) " +
		" values (?, ?, ?, ?, ?)";

	public static String DELETE_PRJ_DEP_OR_RULE =
		"delete from prj_dep_ors where id = ?";

	public static String DELETE_ALL_PRJ_DEP_ANDS_FOR_RULE =
		"delete from prj_dep_ands where prj_dep_or_id = ?";

	public static String INSERT_PRJ_DEP_AND_RULE =
		"insert into prj_dep_ands (prj_dep_or_id, from_prj_id, from_ms_id, succ_or_incl) " +
		"values (?, ?, ?, ?)";

	// end sameer 04252006
	//rajeev 051406 TODO_INQUERY .. later
	public static String GET_TEAMLIST_FOR_PRJ = "select distinct fte_heads.id, fte_heads.name from fte_heads, fte_items, alt_works where (alt_works.id = ? or alt_works.id in (select alt_map_items.alt_work_id from alt_map_items where alt_map_items.alt_id = ? and alt_map_items.wspace_id = ? and isdefault=1)) and fte_items.alt_fte_id = alt_works.ref_alt_fte_id and fte_items.fte_head_id = fte_heads.id and fte_heads.is_generic = 0 order by fte_heads.name asc";
	public static String GET_PRIMSKILL_FOR_NAMED_RES = "select prim_skill, id from fte_heads";

	// sameer 04272006
/*
	public static String GET_RES_ALLOCATION_BY_YEAR_PART_1 =
		"select port_results.prj_id, projects.name, tl.name, tl.id, tl.internal_id, " +
		"fte_items.fte_head_id, fte_heads.name, year_timeid.time_id, " +
		"sum(data.value * getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)), " +
		"fte_heads.prim_skill, tl.alt_work_id, fte_items.assignment_status " +
		"from " +
		"(select aw1.name, aw1.id, aw1.internal_id, aw1.alt_work_id " +
		"	from alt_work_items aw1, port_results " +
		"	where aw1.wbs_level < ? and port_results.port_rs_id = ? " +
		"	and aw1.alt_work_id = port_results.ver_alt_work_id " +
		"	and not exists " +
		"		(select 1 from alt_work_items aw2 where " +
		"			aw2.alt_work_id = aw1.alt_work_id and " +
		"			aw2.parent_internal_id = aw1.internal_id " +
		"		) " +
		"	union " +
		"	select alt_work_items.name, alt_work_items.id, " +
		"	alt_work_items.internal_id, alt_work_items.alt_work_id " +
		"	from alt_work_items, port_results " +
		"	where alt_work_items.wbs_level = ? " +
		"	and port_results.port_rs_id = ? " +
		"	and alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		") tl, fte_items, data, alt_works, port_results, year_timeid, projects, fte_heads " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.prj_id = projects.id ";

	public static String GET_RES_ALLOCATION_BY_YEAR_PART_1_1 =
		"and projects.port_node_id = ? ";

	public static String GET_RES_ALLOCATION_BY_YEAR_PART_1_2 =
		"and (? is null or port_results.alt_id = ?) " +
		"and alt_works.id = port_results.ver_alt_work_id " +
		"and alt_works.id = tl.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and data.fte_item_id = fte_items.id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.assignment_status in ( ";

	//"and fte_items.assignment_status = -1 " +
	//"and fte_heads.prim_skill = -1 " +

	public static String GET_RES_ALLOCATION_BY_YEAR_PART_2 =
		" ) " +
		"and fte_heads.prim_skill in ( ";

	public static String GET_RES_ALLOCATION_BY_YEAR_PART_3 =
		" ) " +
		" and fte_items.task_internal_id in " +
		"(select alt_work_items.internal_id from " +
		"	alt_work_items " +
		"	start with alt_work_items.id = tl.id " +
		"	connect by prior alt_work_items.internal_id = " +
		"	alt_work_items.parent_internal_id and alt_work_items.alt_work_id = " +
		"	tl.alt_work_id " +
		") " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"year_timeid.time_id >= ? " +
		"and " +
		"year_timeid.time_id <= ? " +
		"and " +
		"year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and " +
		"year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"                                    when (data.val_scope=1) then 420 " +
		"                                    when (data.val_scope=2) then 35 " +
		"                                    when (data.val_scope=3) then 12 " +
		"                                    when (data.val_scope=5) then data.val_dur " +
		"                                    else 1 " +
		"                                end) " +
		"group by port_results.prj_id, projects.name, tl.id, tl.internal_id, " +
		"tl.name, fte_items.fte_head_id, fte_heads.name, year_timeid.time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		"fte_items.assignment_status " +
		"order by port_results.prj_id, projects.name, tl.id, tl.internal_id, " +
		"tl.name, fte_items.fte_head_id, fte_heads.name, year_timeid.time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		"fte_items.assignment_status ";
*/
//working stuff
	public static String GET_RES_ALLOCATION_BY_QTR_PART_0_0 = 
  Misc.G_DO_ORACLE ?
  " select port_results.prj_id, projects.name, tl.name, tl.id, tl.internal_id, " +
		" fte_items.fte_head_id, fte_heads.name, time_id, " +
		" sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)), " +
		" fte_heads.prim_skill, tl.alt_work_id, fte_items.assignment_status " +
		" from " +
		" (select aw1.name, aw1.id, aw1.internal_id, aw1.alt_work_id " +
		"	from alt_work_items aw1, port_results, projects, pj_basics " +
          "    where "+
          "    port_results.port_rs_id = ? " +
          "    and port_results.prj_id = projects.id "+
          "    and port_results.ver_prj_basic_id = pj_basics.id " //put pj sel clause here
:          
		" select port_results.prj_id, projects.name, tl.name, tl.id, tl.internal_id, " +
		" fte_items.fte_head_id, fte_heads.name, time_id, " +
		" sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)), " +
		" fte_heads.prim_skill, tl.alt_work_id, fte_items.assignment_status " +
		" from " +
		" (select aw1.name, aw1.id, aw1.internal_id, aw1.alt_work_id " +
		"	from alt_work_items aw1, port_results, projects, pj_basics " +
          "    where "+
          "    port_results.port_rs_id = ? " +
          "    and port_results.prj_id = projects.id "+
          "    and port_results.ver_prj_basic_id = pj_basics.id "; //put pj sel clause here
	public static String GET_RES_ALLOCATION_BY_QTR_PART_0_1 =
		"	and aw1.wbs_level < ? "+
		"	and aw1.alt_work_id = port_results.ver_alt_work_id " +
		"	and not exists " +
		"		(select 1 from alt_work_items aw2 where " +
		"			aw2.alt_work_id = aw1.alt_work_id and " +
		"			aw2.parent_internal_id = aw1.internal_id " +
		"		) " +
		"	union " +
		"	select alt_work_items.name, alt_work_items.id, " +
		"	alt_work_items.internal_id, alt_work_items.alt_work_id " +
		"	from alt_work_items, port_results, projects, pj_basics " +
		"	where "+
          "    port_results.port_rs_id = ? "+
          "    and port_results.prj_id = projects.id "+
          "    and port_results.ver_prj_basic_id = pj_basics.id "; //put pj sel clause here
	public static String GET_RES_ALLOCATION_BY_QTR_PART_0_2 =
          "    and alt_work_items.wbs_level = ? " +
		"	and alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		" ) tl, fte_items, data, alt_works, port_results, ";//qtr_timeid
     public static String GET_RES_ALLOCATION_BY_QTR_PART_0_3 = ", projects, fte_heads, pj_basics " +
		" where " +
		" port_results.port_rs_id = ? " +
		" and port_results.prj_id = projects.id "+
          " and port_results.ver_prj_basic_id = pj_basics.id ";// put pj sel clause here

	public static String GET_RES_ALLOCATION_BY_QTR_PART_1_2 =
		" and alt_works.id = port_results.ver_alt_work_id " +
		" and alt_works.id = tl.alt_work_id " +
		" and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		" and data.fte_item_id = fte_items.id " +
		" and fte_items.fte_head_id = fte_heads.id " ;
//		" and fte_items.assignment_status in ( )"; ... these will be added if necessary
//        " and fte_heads.prim_skill in () "; ... these will be added if necessary

	public static String GET_RES_ALLOCATION_BY_QTR_PART_3 = Misc.G_DO_ORACLE ? //CHANGE_HIER
  " and fte_items.task_internal_id in " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.alt_work_id = " +
		"	tl.alt_work_id and ancs.id = t1.id " +
		" ) " +
		" and " +
		" fte_items.to_include = 1 " +
		" and " +
		" time_id >= ? " +
		" and " +
		" time_id < ? " +
//          " and time_id >= trunc(data.year/?)*? "+ //for efficiency sake
          " and isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1"+
		" group by port_results.prj_id, projects.name, tl.id, tl.internal_id, " +
		" tl.name, fte_items.fte_head_id, fte_heads.name, time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		" fte_items.assignment_status " +
		" order by port_results.prj_id, projects.name, tl.alt_work_id, tl.internal_id, " +
		" tl.name, fte_items.fte_head_id, fte_heads.name, time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		" fte_items.assignment_status "
    :
		" and fte_items.task_internal_id in " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.alt_work_id = " +
		"	tl.alt_work_id  " +
		" ) " +
		" and " +
		" fte_items.to_include = 1 " +
		" and " +
		" time_id >= ? " +
		" and " +
		" time_id < ? " +
//          " and time_id >= trunc(data.year/?)*? "+ //for efficiency sake
          " and intelli.isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1"+
		" group by port_results.prj_id, projects.name, tl.id, tl.internal_id, " +
		" tl.name, fte_items.fte_head_id, fte_heads.name, time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		" fte_items.assignment_status " +
		" order by port_results.prj_id, projects.name, tl.alt_work_id, tl.internal_id, " +
		" tl.name, fte_items.fte_head_id, fte_heads.name, time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		" fte_items.assignment_status ";

     /////////////////////
	public static String GET_RES_ALLOCATION_AGG_PART_0 = Misc.G_DO_ORACLE ?
  //		" select port_results.prj_id, projects.name,
          " '', "+Misc.getUndefInt()+", "+Misc.getUndefInt()+", " +
		" fte_items.fte_head_id, fte_heads.name, time_id, " +
		" sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)), " +
		" fte_heads.prim_skill, "+Misc.getUndefInt()+", fte_items.assignment_status " +
		" from fte_items, data, port_results, "//qtr_timeid
:
//		" select port_results.prj_id, projects.name,
          " '', "+Misc.getUndefInt()+", "+Misc.getUndefInt()+", " +
		" fte_items.fte_head_id, fte_heads.name, time_id, " +
		" sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)), " +
		" fte_heads.prim_skill, "+Misc.getUndefInt()+", fte_items.assignment_status " +
		" from fte_items, data, port_results, ";//qtr_timeid
     public static String GET_RES_ALLOCATION_AGG_PART_1 = ", projects, fte_heads, pj_basics " +
		" where " +
		" port_results.port_rs_id = ? " +
		" and port_results.prj_id = projects.id "+
          " and port_results.ver_prj_basic_id = pj_basics.id ";// put pj sel clause here
	public static String GET_RES_ALLOCATION_AGG_PART_2 =
		" and fte_items.alt_fte_id = port_results.ver_alt_fte_id " +
		" and data.fte_item_id = fte_items.id " +
		" and fte_items.fte_head_id = fte_heads.id " ;
//		" and fte_items.assignment_status in ( )"; ... these will be added if necessary
//        " and fte_heads.prim_skill in () "; ... these will be added if necessary

	public static String GET_RES_ALLOCATION_AGG_PART_3 =
  Misc.G_DO_ORACLE ?
  " and " +
		" fte_items.to_include = 1 " +
		" and " +
		" time_id >= ? " +
		" and " +
		" time_id < ? " +
          " and isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1"+
		" group by " //projects.id, projects.name,
  :
		" and " +
		" fte_items.to_include = 1 " +
		" and " +
		" time_id >= ? " +
		" and " +
		" time_id < ? " +
          " and intelli.isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1"+
		" group by "; //projects.id, projects.name,
     public static String GET_RES_ALLOCATION_AGG_PART_4 =
		" fte_items.fte_head_id, fte_heads.name, time_id, fte_heads.prim_skill,  " +
		" fte_items.assignment_status " +
		" order by "; //projects.name,
     public static String GET_RES_ALLOCATION_AGG_PART_5 =
		" fte_items.fte_head_id, fte_heads.name, time_id, fte_heads.prim_skill, " +
		" fte_items.assignment_status ";



	public static String GET_RES_ALLOCATION_BY_MONTH_PART_1 =
		"select port_results.prj_id, projects.name, tl.name, tl.id, tl.internal_id, " +
		"fte_items.fte_head_id, fte_heads.name, month_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, month_timeid.time_id, 2, data.val_dur)), " +
		"fte_heads.prim_skill, tl.alt_work_id, fte_items.assignment_status " +
		"from " +
		"(select aw1.name, aw1.id, aw1.internal_id, aw1.alt_work_id " +
		"	from alt_work_items aw1, port_results " +
		"	where aw1.wbs_level < ? and port_results.port_rs_id = ? " +
		"	and aw1.alt_work_id = port_results.ver_alt_work_id " +
		"	and not exists " +
		"		(select 1 from alt_work_items aw2 where " +
		"			aw2.alt_work_id = aw1.alt_work_id and " +
		"			aw2.parent_internal_id = aw1.internal_id " +
		"		) " +
		"	union " +
		"	select alt_work_items.name, alt_work_items.id, " +
		"	alt_work_items.internal_id, alt_work_items.alt_work_id " +
		"	from alt_work_items, port_results " +
		"	where alt_work_items.wbs_level = ? " +
		"	and port_results.port_rs_id = ? " +
		"	and alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		") tl, fte_items, data, alt_works, port_results, month_timeid, projects, fte_heads " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.prj_id = projects.id ";

	public static String GET_RES_ALLOCATION_BY_MONTH_PART_1_1 =
		"and projects.port_node_id = ? ";

	public static String GET_RES_ALLOCATION_BY_MONTH_PART_1_2 =
		"and (? is null or port_results.alt_id = ?) " +
		"and alt_works.id = port_results.ver_alt_work_id " +
		"and alt_works.id = tl.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and data.fte_item_id = fte_items.id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.assignment_status in ( ";

	//"and fte_items.assignment_status = -1 " +
	//"and fte_heads.prim_skill = -1 " +

	public static String GET_RES_ALLOCATION_BY_MONTH_PART_2 =
		" ) " +
		"and fte_heads.prim_skill in ( ";

	public static String GET_RES_ALLOCATION_BY_MONTH_PART_3 =
		" ) " +
		"and fte_items.task_internal_id in " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.alt_work_id = " +
		"	tl.alt_work_id  " +
		" ) " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"month_timeid.time_id >= ? " +
		"and " +
		"month_timeid.time_id <= ? " +
		"and " +
		"month_timeid.time_id >= trunc(data.year/35)*35 " +
		"and " +
		"month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"                                    when (data.val_scope=1) then 420 " +
		"                                    when (data.val_scope=2) then 35 " +
		"                                    when (data.val_scope=3) then 12 " +
		"                                    when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"                                    else 1 " +
		"                                end) " +
		"group by port_results.prj_id, projects.name, tl.id, tl.internal_id, " +
		"tl.name, fte_items.fte_head_id, fte_heads.name, month_timeid.time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		"fte_items.assignment_status " +
		"order by port_results.prj_id, projects.name, tl.id, tl.internal_id, " +
		"tl.name, fte_items.fte_head_id, fte_heads.name, month_timeid.time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		"fte_items.assignment_status ";

	public static String GET_RES_ALLOCATION_BY_WEEK_PART_1 =
		"select port_results.prj_id, projects.name, tl.name, tl.id, tl.internal_id, " +
		"fte_items.fte_head_id, fte_heads.name, week_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)), " +
		"fte_heads.prim_skill, tl.alt_work_id, fte_items.assignment_status " +
		"from " +
		"(select aw1.name, aw1.id, aw1.internal_id, aw1.alt_work_id " +
		"	from alt_work_items aw1, port_results " +
		"	where aw1.wbs_level < ? and port_results.port_rs_id = ? " +
		"	and aw1.alt_work_id = port_results.ver_alt_work_id " +
		"	and not exists " +
		"		(select 1 from alt_work_items aw2 where " +
		"			aw2.alt_work_id = aw1.alt_work_id and " +
		"			aw2.parent_internal_id = aw1.internal_id " +
		"		) " +
		"	union " +
		"	select alt_work_items.name, alt_work_items.id, " +
		"	alt_work_items.internal_id, alt_work_items.alt_work_id " +
		"	from alt_work_items, port_results " +
		"	where alt_work_items.wbs_level = ? " +
		"	and port_results.port_rs_id = ? " +
		"	and alt_work_items.alt_work_id = port_results.ver_alt_work_id " +
		") tl, fte_items, data, alt_works, port_results, week_timeid, projects, fte_heads " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.prj_id = projects.id ";

	public static String GET_RES_ALLOCATION_BY_WEEK_PART_1_1 =
		"and projects.port_node_id = ? ";

	public static String GET_RES_ALLOCATION_BY_WEEK_PART_1_2 =
		"and (? is null or port_results.alt_id = ?) " +
		"and alt_works.id = port_results.ver_alt_work_id " +
		"and alt_works.id = tl.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and data.fte_item_id = fte_items.id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.assignment_status in ( ";

	//"and fte_items.assignment_status = -1 " +
	//"and fte_heads.prim_skill = -1 " +

	public static String GET_RES_ALLOCATION_BY_WEEK_PART_2 =
		" ) " +
		"and fte_heads.prim_skill in ( ";

	public static String GET_RES_ALLOCATION_BY_WEEK_PART_3 =
		" ) " +
		"and fte_items.task_internal_id in " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.alt_work_id = " +
		"	tl.alt_work_id and ancs.id = t1.id  " +
		" ) " +
		"and " +
		"fte_items.to_include = 1 " +
		"and " +
		"week_timeid.time_id >= ? " +
		"and " +
		"week_timeid.time_id <= ? " +
		"and " +
		"week_timeid.time_id >= data.year - 12 " +
		"and " +
		"week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"                                    when (data.val_scope=1) then 420 " +
		"                                    when (data.val_scope=2) then 35 " +
		"                                    when (data.val_scope=3) then 12 " +
		"                                    when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"                                    else 1 " +
		"                                end) " +
		"group by port_results.prj_id, projects.name, tl.id, tl.internal_id, " +
		"tl.name, fte_items.fte_head_id, fte_heads.name, week_timeid.time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		"fte_items.assignment_status " +
		"order by port_results.prj_id, projects.name, tl.id, tl.internal_id, " +
		"tl.name, fte_items.fte_head_id, fte_heads.name, week_timeid.time_id, fte_heads.prim_skill, tl.alt_work_id, " +
		"fte_items.assignment_status ";

	// end sameer 04272006

	// sameer 05032006
/*
	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_YEAR_1_OLD =
		"select fte_heads.id, fte_heads.name, combo.tid, combo.v " +
		"from fte_heads, " +
		"(select fte_items.fte_head_id fh, " +
		"year_timeid.time_id tid, " +
		"sum(data.value * getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)) v, " +
		"datagr.gv gv " +
		"from port_results, alt_works aw, fte_items, data, year_timeid, " +
		"(select sum(data.value * getSimplePropIncluded(data.year, data.val_scope, data.val_dur, ?, ?)) gv, " +
		"fte_items.fte_head_id fh from data, fte_items, port_results, alt_works aw " +
		"where port_results.port_rs_id = ? " +
		"and port_results.ver_alt_work_id = aw.id " +
		"and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"and data.fte_item_id = fte_items.id " +
		"and fte_items.to_include = 1 " +
		"and data.year >= ? -(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur " +
		"else 1 " +
		"end) " +
		"and data.year <= ? group by fte_items.fte_head_id " +
		") datagr " +
		"where port_results.port_rs_id = ? " +
		"and port_results.ver_alt_work_id = aw.id " +
		"and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and datagr.fh = fte_items.fte_head_id " +
		"and year_timeid.time_id >= ? " +
		"and year_timeid.time_id <= ? " +
		"and year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and year_timeid.time_id < data.year + (case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur " +
		"else 1 " +
		"end) " +
		"group by fte_items.fte_head_id, time_id, datagr.gv " +
		") combo ";

	// ", fte_head_sec_skill " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_YEAR_2_OLD =
		"where " +
		"combo.fh(+) = fte_heads.id " +
		"and fte_heads.id <> ? " +
		"and fte_heads.is_active = 1 " +
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 ";

	// and fte_heads.prim_skill in ()
	// and fte_head_sec_skill.fte_head_id = fte_heads.id
	// and fte_head_sec_skill.skill_id in ()

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_YEAR_3_OLD =
		"order by combo.gv desc nulls first, fte_heads.id, combo.tid";
*/
	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_YEAR_1_0 = Misc.G_DO_ORACLE ?
  "select fte_heads.id, fte_heads.name, tot.tid, tot.v " +
		"from fte_heads, " +
		"( " +
		"	select fh, tid, sum(v) v from " +
		"	( " +
		"		select  combo.fh, combo.tid, combo.v " +
		"		from  " +
		"			(select fte_items.fte_head_id fh, " +
		"			time_id tid, " +
		"			(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)) v " +
		"			from port_results,  fte_items, data, "//year_timeid
    :
		"select fte_heads.id, fte_heads.name, tot.tid, tot.v " +
		"from fte_heads, " +
		"( " +
		"	select fh, tid, sum(v) v from " +
		"	( " +
		"		select  combo.fh, combo.tid, combo.v " +
		"		from  " +
		"			(select fte_items.fte_head_id fh, " +
		"			time_id tid, " +
		"			(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)) v " +
		"			from port_results,  fte_items, data, ";//year_timeid
      public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_YEAR_1_1 = Misc.G_DO_ORACLE ?
      "			where port_results.port_rs_id = ? " +
		"			and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"			and fte_items.to_include = 1 " +
		"			and data.fte_item_id = fte_items.id " +
		"			and time_id >= ? " +
		"			and time_id < ? " +
          "              and isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1 "+
		"			) combo " +
		"		union " +
		"			( " +
		"			select fte_heads.id fh, time_id tid, " +
		"			(val_unavail * (case " +
		"								when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, ?) " +
		"								else " +
		"								Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, ?, Round(end_date-start_date+1,0)) " +
		"							end) " +
		"			)v " +
		"			from off_times, fte_heads , "//year_timeid
  :
		"			where port_results.port_rs_id = ? " +
		"			and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"			and fte_items.to_include = 1 " +
		"			and data.fte_item_id = fte_items.id " +
		"			and time_id >= ? " +
		"			and time_id < ? " +
          "              and intelli.isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1 "+
		"			) combo " +
		"		union " +
		"			( " +
		"			select fte_heads.id fh, time_id tid, " +
		"			(val_unavail * (case " +
		"								when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then intelli.getDuration(time_id, ?) " +
		"								else " +
		"								cast((end_date-start_date+1) as numeric) * intelli.getPropIncluded(5, intelli.getTimeId(start_date), time_id, ?, cast((end_date-start_date+1) as numeric)) " +
		"							end) " +
		"			)v " +
		"			from off_times, fte_heads , ";//year_timeid
        public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_YEAR_1_2 =  //year_timeid " +  
        Misc.G_DO_ORACLE ?
        "			where fte_heads.is_active = 1 " +
		"			and is_generic is not null and is_generic <> 1 " +
		"			and off_times.fte_head_id = fte_heads.id " +
		"			and (end_date >= ? or end_date is null) " +
		"			and time_id >= ? " +
		"			and time_id < ? " +
		"			and (start_date is null or time_id >= getTimeId(start_date)) " +
		"			and (end_date is null or time_id <= getTimeId(end_date)) " +
		"			) " +
		"	) t " +
		"	group by tid, fh " +
		") tot "
    :
		"			where fte_heads.is_active = 1 " +
		"			and is_generic is not null and is_generic <> 1 " +
		"			and off_times.fte_head_id = fte_heads.id " +
		"			and (end_date >= ? or end_date is null) " +
		"			and time_id >= ? " +
		"			and time_id < ? " +
		"			and (start_date is null or time_id >= intelli.getTimeId(start_date)) " +
		"			and (end_date is null or time_id <= intelli.getTimeId(end_date)) " +
		"			) " +
		"	) t " +
		"	group by tid, fh " +
		") tot ";

	// ", fte_head_sec_skill " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_YEAR_2 =
  Misc.G_DO_ORACLE ? 
  "where " +
		"tot.fh(+) = fte_heads.id " +
//		"and fte_heads.id <> ? " + // will be added to fte_heads prim skill area
		"and fte_heads.is_active = 1 " +
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 "
    :
		"where " +
		"tot.fh =* fte_heads.id " +
//		"and fte_heads.id <> ? " + // will be added to fte_heads prim skill area
		"and fte_heads.is_active = 1 " +
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 ";

	// "and fte_heads.prim_skill in (2) " +
	// "and fte_head_sec_skill.fte_head_id = fte_heads.id " +
	// "and fte_head_sec_skill.skill_id in (1) " +
	// "and fte_heads.classify1 in (5) " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_YEAR_3 =
		"order by fte_heads.name, tot.tid";

     public static String GET_CURR_AVAIL_GENERIC_0 = Misc.G_DO_ORACLE ?
     " select fid, fte_heads.name, tid, sum(v) from "+
" ( "+
" select constraints.fte_head_id fid, time_id tid, sum(cnst_vals.target * "+
" intelli.getPropIncluded(cnst_vals.val_scope, cnst_vals.year, time_id, ?, 300)) v "+
" from "+
" constraints, cnst_vals, "//month_timeid "+
:
     " select fid, fte_heads.name, tid, sum(v) from "+
" ( "+
" select constraints.fte_head_id fid, time_id tid, sum(cnst_vals.target * "+
" intelli.getPropIncluded(cnst_vals.val_scope, cnst_vals.year, time_id, ?, 300)) v "+
" from "+
" constraints, cnst_vals, ";//month_timeid "+
     public static String GET_CURR_AVAIL_GENERIC_1 =
" where "+
" cnst_vals.cnstrt_id = constraints.id "+
" and (? is null or constraints.fte_head_id = ?) ";//-- classify1 constraints if any
     public static String GET_CURR_AVAIL_GENERIC_2 = Misc.G_DO_ORACLE ?
     " and  time_id >= ? "+
" and  time_id < ? "+
" and isInTimeWindow(time_id, ?, cnst_vals.year, cnst_vals.val_scope, 300) = 1 "+
" group by constraints.fte_head_id, time_id "+
" union all "+
" select fte_items.fte_head_id fid, time_id tid, -1*sum(data.value * "+
" intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)) v "+
" from "+
" port_results, projects, pj_basics, fte_items, data, "//month_timeid "+
:
" and  time_id >= ? "+
" and  time_id < ? "+
" and intelli.isInTimeWindow(time_id, ?, cnst_vals.year, cnst_vals.val_scope, 300) = 1 "+
" group by constraints.fte_head_id, time_id "+
" union all "+
" select fte_items.fte_head_id fid, time_id tid, -1*sum(data.value * "+
" intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)) v "+
" from "+
" port_results, projects, pj_basics, fte_items, data, ";//month_timeid "+
     public static String GET_CURR_AVAIL_GENERIC_3 =
" where port_results.port_rs_id = ? "+
" and port_results.prj_id = projects.id "+
" and port_results.ver_prj_basic_id = pj_basics.id ";//-- pjSelClause
     public static String GET_CURR_AVAIL_GENERIC_4 = Misc.G_DO_ORACLE ?
     " and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
" and data.fte_item_id = fte_items.id "+
" and (? is null or fte_items.fte_head_id = ?) "+
" and fte_items.assignment_status in (1,2) "+
" and  fte_items.to_include = 1 "+
" and  time_id >= ? "+
" and  time_id < ? "+
" and isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1 "+
" group by fte_items.fte_head_id, time_id "+
" ) avl, fte_heads "+
" where fte_heads.id = avl.fid "+
" group by fid, tid, fte_heads.name "+
" order by fid, tid "
:
" and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
" and data.fte_item_id = fte_items.id "+
" and (? is null or fte_items.fte_head_id = ?) "+
" and fte_items.assignment_status in (1,2) "+
" and  fte_items.to_include = 1 "+
" and  time_id >= ? "+
" and  time_id < ? "+
" and intelli.isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1 "+
" group by fte_items.fte_head_id, time_id "+
" ) avl, fte_heads "+
" where fte_heads.id = avl.fid "+
" group by fid, tid, fte_heads.name "+
" order by fid, tid ";
/*
	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_QTR_1_OLD =
		"select fte_heads.id, fte_heads.name, combo.tid, combo.v " +
		"from fte_heads, " +
		"(select fte_items.fte_head_id fh, " +
		"qtr_timeid.time_id tid, " +
		"sum(data.value * getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)) v, " +
		"datagr.gv gv " +
		"from port_results, alt_works aw, fte_items, data, qtr_timeid, " +
		"(select sum(data.value * getSimplePropIncluded(data.year, data.val_scope, data.val_dur, ?, ?)) gv, " +
		"fte_items.fte_head_id fh from data, fte_items, port_results, alt_works aw " +
		"where port_results.port_rs_id = ? " +
		"and port_results.ver_alt_work_id = aw.id " +
		"and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"and data.fte_item_id = fte_items.id " +
		"and fte_items.to_include = 1 " +
		"and data.year >= ? -(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur " +
		"else 1 " +
		"end) " +
		"and data.year <= ? group by fte_items.fte_head_id " +
		") datagr " +
		"where port_results.port_rs_id = ? " +
		"and port_results.ver_alt_work_id = aw.id " +
		"and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and datagr.fh = fte_items.fte_head_id " +
		"and qtr_timeid.time_id >= ? " +
		"and qtr_timeid.time_id <= ? " +
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"and qtr_timeid.time_id < data.year + (case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur " +
		"else 1 " +
		"end) " +
		"group by fte_items.fte_head_id, time_id, datagr.gv " +
		") combo ";

	// ", fte_head_sec_skill " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_QTR_2_OLD =
		"where " +
		"combo.fh(+) = fte_heads.id " +
		"and fte_heads.id <> ? " +
		"and fte_heads.is_active = 1 " +
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 ";

	// and fte_heads.prim_skill in ()
	// and fte_head_sec_skill.fte_head_id = fte_heads.id
	// and fte_head_sec_skill.skill_id in ()

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_QTR_3_OLD =
		"order by combo.gv desc nulls first, fte_heads.id, combo.tid";
*/
	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_QTR_1 =
		"select fte_heads.id, fte_heads.name, tot.tid, tot.v " +
		"from fte_heads, " +
		"( " +
		"	select fh, tid, sum(v) v from " +
		"	( " +
		"		select  combo.fh, combo.tid, combo.v " +
		"		from  " +
		"			(select fte_items.fte_head_id fh, " +
		"			qtr_timeid.time_id tid, " +
		"			(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)) v " +
		"			from port_results, alt_works aw, fte_items, data, qtr_timeid " +
		"			where port_results.port_rs_id = ? " +
		"			and port_results.ver_alt_work_id = aw.id " +
		"			and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"			and fte_items.to_include = 1 " +
		"			and data.fte_item_id = fte_items.id " +
		"			and qtr_timeid.time_id >= ? " +
		"			and qtr_timeid.time_id <= ? " +
		"			and qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"			and qtr_timeid.time_id < data.year + (case when (data.val_scope = 0) then 105 " +
		"														when (data.val_scope=1) then 420 " +
		"														when (data.val_scope=2) then 35 " +
		"														when (data.val_scope=3) then 12 " +
		"														when (data.val_scope=5) then data.val_dur*.125+7 " +
		"														else 1 " +
		"													end) " +
		"			) combo " +
		"		union " +
		"			( " +
		"			select fte_heads.id fh, time_id tid, " +
		"			(val_unavail * (case " +
		"								when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 0) " +
		"								else " +
		"								Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 0, Round(end_date-start_date+1,0)) " +
		"							end) " +
		"			)v " +
		"			from off_times, fte_heads , qtr_timeid " +
		"			where fte_heads.is_active = 1 " +
		"			and is_generic is not null and is_generic <> 1 " +
		"			and off_times.fte_head_id = fte_heads.id " +
		"			and (end_date >= ? or end_date is null) " +
		"			and time_id >= ? " +
		"			and time_id <= ? " +
		"			and (start_date is null or time_id >= trunc(getTimeId(start_date)/105)*105) " +
		"			and (end_date is null or time_id <= getTimeId(end_date)) " +
		"			) " +
		"	) t " +
		"	group by tid, fh " +
		") tot ";

	// ", fte_head_sec_skill " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_QTR_2 =
		"where " +
		"tot.fh(+) = fte_heads.id " +
		"and fte_heads.id <> ? " +
		"and fte_heads.is_active = 1 " +
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 ";

	// "and fte_heads.prim_skill in (2) " +
	// "and fte_head_sec_skill.fte_head_id = fte_heads.id " +
	// "and fte_head_sec_skill.skill_id in (1) " +
	// "and fte_heads.classify1 in (5) " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_QTR_3 =
		"order by fte_heads.name, tot.tid";
/*
	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_MONTH_1_OLD =
		"select fte_heads.id, fte_heads.name, combo.tid, combo.v " +
		"from fte_heads, " +
		"(select fte_items.fte_head_id fh, " +
		"month_timeid.time_id tid, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)) v, " +
		"datagr.gv gv " +
		"from port_results, alt_works aw, fte_items, data, month_timeid, " +
		"(select sum(data.value * getSimplePropIncluded(data.year, data.val_scope, data.val_dur, ?, ?)) gv, " +
		"fte_items.fte_head_id fh from data, fte_items, port_results, alt_works aw " +
		"where port_results.port_rs_id = ? " +
		"and port_results.ver_alt_work_id = aw.id " +
		"and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"and data.fte_item_id = fte_items.id " +
		"and fte_items.to_include = 1 " +
		"and data.year >= ? -(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur " +
		"else 1 " +
		"end) " +
		"and data.year <= ? group by fte_items.fte_head_id " +
		") datagr " +
		"where port_results.port_rs_id = ? " +
		"and port_results.ver_alt_work_id = aw.id " +
		"and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and datagr.fh = fte_items.fte_head_id " +
		"and month_timeid.time_id >= ? " +
		"and month_timeid.time_id <= ? " +
		"and month_timeid.time_id >= trunc(data.year/35)*35 " +
		"and month_timeid.time_id < data.year + (case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur " +
		"else 1 " +
		"end) " +
		"group by fte_items.fte_head_id, time_id, datagr.gv " +
		") combo ";

	// ", fte_head_sec_skill " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_MONTH_2_OLD =
		"where " +
		"combo.fh(+) = fte_heads.id " +
		"and fte_heads.id <> ? " +
		"and fte_heads.is_active = 1 " +
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 ";

	// and fte_heads.prim_skill in ()
	// and fte_head_sec_skill.fte_head_id = fte_heads.id
	// and fte_head_sec_skill.skill_id in ()

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_MONTH_3_OLD =
		"order by combo.gv desc nulls first, fte_heads.id, combo.tid";
*/
	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_MONTH_1 =
		"select fte_heads.id, fte_heads.name, tot.tid, tot.v " +
		"from fte_heads, " +
		"( " +
		"	select fh, tid, sum(v) v from " +
		"	( " +
		"		select  combo.fh, combo.tid, combo.v " +
		"		from  " +
		"			(select fte_items.fte_head_id fh, " +
		"			month_timeid.time_id tid, " +
		"			(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)) v " +
		"			from port_results, alt_works aw, fte_items, data, month_timeid " +
		"			where port_results.port_rs_id = ? " +
		"			and port_results.ver_alt_work_id = aw.id " +
		"			and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"			and fte_items.to_include = 1 " +
		"			and data.fte_item_id = fte_items.id " +
		"			and month_timeid.time_id >= ? " +
		"			and month_timeid.time_id <= ? " +
		"			and month_timeid.time_id >= trunc(data.year/35)*35 " +
		"			and month_timeid.time_id < data.year + (case when (data.val_scope = 0) then 105 " +
		"														when (data.val_scope=1) then 420 " +
		"														when (data.val_scope=2) then 35 " +
		"														when (data.val_scope=3) then 12 " +
		"														when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"														else 1 " +
		"													end) " +
		"			) combo " +
		"		union " +
		"			( " +
		"			select fte_heads.id fh, time_id tid, " +
		"			(val_unavail * (case " +
		"								when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 2) " +
		"								else " +
		"								cast((end_date-start_date+1) as numeric) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 2, cast((end_date-start_date+1) as numeric)) " +
		"							end) " +
		"			)v " +
		"			from off_times, fte_heads , month_timeid " +
		"			where fte_heads.is_active = 1 " +
		"			and is_generic is not null and is_generic <> 1 " +
		"			and off_times.fte_head_id = fte_heads.id " +
		"			and (end_date >= ? or end_date is null) " +
		"			and time_id >= ? " +
		"			and time_id <= ? " +
		"			and (start_date is null or time_id >= trunc(getTimeId(start_date)/35)*35) " +
		"			and (end_date is null or time_id <= getTimeId(end_date)) " +
		"			) " +
		"	) t " +
		"	group by tid, fh " +
		") tot ";

	// ", fte_head_sec_skill " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_MONTH_2 =
		"where " +
		"tot.fh(+) = fte_heads.id " +
		"and fte_heads.id <> ? " +
		"and fte_heads.is_active = 1 " +
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 ";

	// "and fte_heads.prim_skill in (2) " +
	// "and fte_head_sec_skill.fte_head_id = fte_heads.id " +
	// "and fte_head_sec_skill.skill_id in (1) " +
	// "and fte_heads.classify1 in (5) " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_MONTH_3 =
		"order by fte_heads.name, tot.tid";
/*
	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_WEEK_1_OLD =
		"select fte_heads.id, fte_heads.name, combo.tid, combo.v " +
		"from fte_heads, " +
		"(select fte_items.fte_head_id fh, " +
		"week_timeid.time_id tid, " +
		"sum(data.value * getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)) v, " +
		"datagr.gv gv " +
		"from port_results, alt_works aw, fte_items, data, week_timeid, " +
		"(select sum(data.value * getSimplePropIncluded(data.year, data.val_scope, data.val_dur, ?, ?)) gv, " +
		"fte_items.fte_head_id fh from data, fte_items, port_results, alt_works aw " +
		"where port_results.port_rs_id = ? " +
		"and port_results.ver_alt_work_id = aw.id " +
		"and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"and data.fte_item_id = fte_items.id " +
		"and fte_items.to_include = 1 " +
		"and data.year >= ? -(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur " +
		"else 1 " +
		"end) " +
		"and data.year <= ? group by fte_items.fte_head_id " +
		") datagr " +
		"where port_results.port_rs_id = ? " +
		"and port_results.ver_alt_work_id = aw.id " +
		"and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and datagr.fh = fte_items.fte_head_id " +
		"and week_timeid.time_id >= ? " +
		"and week_timeid.time_id <= ? " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year + (case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur " +
		"else 1 " +
		"end) " +
		"group by fte_items.fte_head_id, time_id, datagr.gv " +
		") combo ";

	// ", fte_head_sec_skill " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_WEEK_2_OLD =
		"where " +
		"combo.fh(+) = fte_heads.id " +
		"and fte_heads.id <> ? " +
		"and fte_heads.is_active = 1 " +
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 ";

	// and fte_heads.prim_skill in ()
	// and fte_head_sec_skill.fte_head_id = fte_heads.id
	// and fte_head_sec_skill.skill_id in ()

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_WEEK_3_OLD =
		"order by combo.gv desc nulls first, fte_heads.id, combo.tid";
*/
	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_WEEK_1 =
		"select fte_heads.id, fte_heads.name, tot.tid, tot.v " +
		"from fte_heads, " +
		"( " +
		"	select fh, tid, sum(v) v from " +
		"	( " +
		"		select  combo.fh, combo.tid, combo.v " +
		"		from  " +
		"			(select fte_items.fte_head_id fh, " +
		"			week_timeid.time_id tid, " +
		"			(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)) v " +
		"			from port_results, alt_works aw, fte_items, data, week_timeid " +
		"			where port_results.port_rs_id = ? " +
		"			and port_results.ver_alt_work_id = aw.id " +
		"			and fte_items.alt_fte_id = aw.ref_alt_fte_id " +
		"			and fte_items.to_include = 1 " +
		"			and data.fte_item_id = fte_items.id " +
		"			and week_timeid.time_id >= ? " +
		"			and week_timeid.time_id <= ? " +
		"			and week_timeid.time_id >= data.year - 12 " +
		"			and week_timeid.time_id < data.year + (case when (data.val_scope = 0) then 105 " +
		"														when (data.val_scope=1) then 420 " +
		"														when (data.val_scope=2) then 35 " +
		"														when (data.val_scope=3) then 12 " +
		"														when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"														else 1 " +
		"													end) " +
		"			) combo " +
		"		union " +
		"			( " +
		"			select fte_heads.id fh, time_id tid, " +
		"			(val_unavail * (case " +
		"								when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 3) " +
		"								else " +
		"								Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 3, Round(end_date-start_date+1,0)) " +
		"							end) " +
		"			)v " +
		"			from off_times, fte_heads , week_timeid " +
		"			where fte_heads.is_active = 1 " +
		"			and is_generic is not null and is_generic <> 1 " +
		"			and off_times.fte_head_id = fte_heads.id " +
		"			and (end_date >= ? or end_date is null) " +
		"			and time_id >= ? " +
		"			and time_id <= ? " +
		"			and (start_date is null or time_id >= getTimeId(start_date) -12) " +
		"			and (end_date is null or time_id <= getTimeId(end_date)) " +
		"			) " +
		"	) t " +
		"	group by tid, fh " +
		") tot ";

	// ", fte_head_sec_skill " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_WEEK_2 =
		"where " +
		"tot.fh(+) = fte_heads.id " +
		"and fte_heads.id <> ? " +
		"and fte_heads.is_active = 1 " +
		"and fte_heads.is_generic is not null and fte_heads.is_generic <> 1 ";

	// "and fte_heads.prim_skill in (2) " +
	// "and fte_head_sec_skill.fte_head_id = fte_heads.id " +
	// "and fte_head_sec_skill.skill_id in (1) " +
	// "and fte_heads.classify1 in (5) " +

	public static String GET_CURR_ASSIGN_FOR_REPLACEMENT_WEEK_3 =
		"order by fte_heads.name, tot.tid";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_YEAR =//CHANGE_HIER
		"select fte_items.id, year_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)), fte_items.start_date, fte_items.end_date  " +
		"from " +
		"alt_work_items, alt_works, fte_items, year_timeid, data " +
    ", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? " +
		" ) t1 " +		
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, year_timeid.time_id, fte_items.start_date, fte_items.end_date  " +
		"order by fte_items.id, year_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_QTR =
		"select fte_items.id, qtr_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, qtr_timeid.time_id, 0, data.val_dur)), fte_items.start_date, fte_items.end_date  " +
		"from " +
		"alt_work_items, alt_works, fte_items, qtr_timeid, data " +
    ", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ?  " +
		" ) t1 " +		
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, qtr_timeid.time_id, fte_items.start_date, fte_items.end_date  " +
		"order by fte_items.id, qtr_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_MONTH_0 = Misc.G_DO_ORACLE ?
  "select fte_items.id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		"from " +
		"alt_work_items, alt_works, fte_items, "//month_timeid
:    
		"select fte_items.id, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		"from " +
		"alt_work_items, alt_works, fte_items, ";//month_timeid
	public static String GET_RES_ALLOCATION_BY_TASK_BY_MONTH_1 = Misc.G_DO_ORACLE ?
         ", data " +
		", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+    
		" ) t1 " +
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
          " and isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1"+
		"group by fte_items.id, time_id, fte_items.start_date, fte_items.end_date " +
		"order by fte_items.id, time_id "
:    
          ", data " +
    ", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+    
		" ) t1 " +
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
          " and intelli.isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1"+
		"group by fte_items.id, time_id, fte_items.start_date, fte_items.end_date " +
		"order by fte_items.id, time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_AGG_0 = Misc.G_DO_ORACLE ?
  " select fte_items.id, time_id, " +
		" sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		" from " +
		" port_results, fte_items, "//month_timeid
:    
		" select fte_items.id, time_id, " +
		" sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, ?, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		" from " +
		" port_results, fte_items, ";//month_timeid
	public static String GET_RES_ALLOCATION_BY_TASK_AGG_1 = Misc.G_DO_ORACLE ?
   " , data " +
		" where " +
          " port_results.port_rs_id = ? "+
          " and (? is null or port_results.prj_id = ?) "+
          " and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		" and fte_items.fte_head_id = ? " +
		" and fte_items.to_include = 1 " +
		" and data.fte_item_id = fte_items.id " +
          " and isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1 "+
		" group by fte_items.id, time_id, fte_items.start_date, fte_items.end_date " +
		" order by fte_items.id, time_id "
:    
          " , data " +
		" where " +
          " port_results.port_rs_id = ? "+
          " and (? is null or port_results.prj_id = ?) "+
          " and fte_items.alt_fte_id = port_results.ver_alt_fte_id "+
		" and fte_items.fte_head_id = ? " +
		" and fte_items.to_include = 1 " +
		" and data.fte_item_id = fte_items.id " +
          " and intelli.isInTimeWindow(time_id, ?, data.year, data.val_scope, data.val_dur) = 1 "+
		" group by fte_items.id, time_id, fte_items.start_date, fte_items.end_date " +
		" order by fte_items.id, time_id ";
/*
	public static String GET_RES_ALLOCATION_BY_TASK_BY_WEEK =
		"select fte_items.id, week_timeid.time_id, " +
		"sum(data.value * getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)), fte_items.start_date, fte_items.end_date  " +
		"from " +
		"alt_work_items, alt_works, fte_items, week_timeid, data " +
		",(select alt_work_items.internal_id from " +
		"alt_work_items " +
		"start with alt_work_items.id = ?  " +
		"connect by prior alt_work_items.internal_id = " +
		"alt_work_items.parent_internal_id and prior alt_work_items.alt_work_id = " +
		"alt_work_items.alt_work_id " +
		") tl " +
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, week_timeid.time_id, fte_items.start_date, fte_items.end_date  " +
		"order by fte_items.id, week_timeid.time_id ";
*/
	public static String GET_PARTICULAR_RES_AVAIL_YEAR = Misc.G_DO_ORACLE ?
  "select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)) " +
		"from  " +
		"port_results, fte_items,  data, year_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= ? " +
		"and year_timeid.time_id <= ? " +
		"and year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by time_id " +
		"order by time_id "
:    
		"select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)) " +
		"from  " +
		"port_results, fte_items,  data, year_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= ? " +
		"and year_timeid.time_id <= ? " +
		"and year_timeid.time_id >= cast((data.year/420) as int)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by time_id " +
		"order by time_id ";

	public static String GET_PARTICULAR_RES_AVAIL_QTR = Misc.G_DO_ORACLE ?
  "select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)) " +
		"from  " +
		"port_results, fte_items,  data, qtr_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= ? " +
		"and qtr_timeid.time_id <= ? " +
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by time_id " +
		"order by time_id "
:    
  
		"select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)) " +
		"from  " +
		"port_results, fte_items,  data, qtr_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= ? " +
		"and qtr_timeid.time_id <= ? " +
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by time_id " +
		"order by time_id ";

	public static String GET_PARTICULAR_RES_AVAIL_MONTH = Misc.G_DO_ORACLE ?
  "select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)) " +
		"from  " +
		"port_results, fte_items,  data, month_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= ? " +
		"and month_timeid.time_id <= ? " +
		"and month_timeid.time_id >= trunc(data.year/35)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by time_id " +
		"order by time_id "
:    
		"select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)) " +
		"from  " +
		"port_results, fte_items,  data, month_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= ? " +
		"and month_timeid.time_id <= ? " +
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by time_id " +
		"order by time_id ";

	public static String GET_PARTICULAR_RES_AVAIL_WEEK = Misc.G_DO_ORACLE ?
  "select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)) " +
		"from  " +
		"port_results, fte_items,  data, week_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= ? " +
		"and week_timeid.time_id <= ? " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by time_id " +
		"order by time_id "
:    
		"select " +
		"time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 3, data.val_dur)) " +
		"from  " +
		"port_results, fte_items,  data, week_timeid " +
		"where " +
		"port_results.port_rs_id = ? " +
		"and port_results.ver_alt_fte_id = fte_items.alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= ? " +
		"and week_timeid.time_id <= ? " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by time_id " +
		"order by time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_YEAR_TIME_LIMITED = Misc.G_DO_ORACLE ?
  "select fte_items.id, year_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, year_timeid, data " +
    ", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+    
		" ) t1 " +		
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= ? " +
		"and year_timeid.time_id <= ? " +
		"and year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, year_timeid.time_id " +
		"order by fte_items.id, year_timeid.time_id "
:    
		"select fte_items.id, year_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, year_timeid, data " +
    ", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+  
		" ) t1 " +		
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= ? " +
		"and year_timeid.time_id <= ? " +
		"and year_timeid.time_id >= cast((data.year/420) as int)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, year_timeid.time_id " +
		"order by fte_items.id, year_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_QTR_TIME_LIMITED = Misc.G_DO_ORACLE ?
  "select fte_items.id, qtr_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, qtr_timeid.time_id, 0, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, qtr_timeid, data " +
		", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+    
		" ) t1 " +
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= ? " +
		"and qtr_timeid.time_id <= ? " +
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, qtr_timeid.time_id " +
		"order by fte_items.id, qtr_timeid.time_id "
:    
		"select fte_items.id, qtr_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, qtr_timeid.time_id, 0, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, qtr_timeid, data " +
		", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+
		" ) t1 " +
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= ? " +
		"and qtr_timeid.time_id <= ? " +
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, qtr_timeid.time_id " +
		"order by fte_items.id, qtr_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_MONTH_TIME_LIMITED = Misc.G_DO_ORACLE ?
  "select fte_items.id, month_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, month_timeid.time_id, 2, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, month_timeid, data " +
		", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+    
		" ) t1 " +
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= ? " +
		"and month_timeid.time_id <= ? " +
		"and month_timeid.time_id >= trunc(data.year/35)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, month_timeid.time_id " +
		"order by fte_items.id, month_timeid.time_id "
:    
		"select fte_items.id, month_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, month_timeid.time_id, 2, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, month_timeid, data " +
		", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+    
		" ) t1 " +
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= ? " +
		"and month_timeid.time_id <= ? " +
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, month_timeid.time_id " +
		"order by fte_items.id, month_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_WEEK_TIME_LIMITED = Misc.G_DO_ORACLE ?
  "select fte_items.id, week_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, week_timeid, data " +
		", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+    
		" ) t1 " +
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= ? " +
		"and week_timeid.time_id <= ? " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, week_timeid.time_id " +
		"order by fte_items.id, week_timeid.time_id "
:    
 		"select fte_items.id, week_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, week_timeid, data " +
		", (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.id = ? "+    
		" ) t1 " +
		"where " +
		"alt_work_items.id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= ? " +
		"and week_timeid.time_id <= ? " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, week_timeid.time_id " +
		"order by fte_items.id, week_timeid.time_id ";

	public static String UPDATE_ASSIGNMENT_STATUS =
		"update fte_items set assignment_status = ? where id = ? ";

	public static String UPDATE_ASSIGNMENT_STATUS_AND_RESOURCE =
		"update fte_items set assignment_status = ?, " +
		"fte_head_id = (select (case when fi.for_skill is not null then fi.for_skill else fte_heads.prim_skill end) from fte_items fi, fte_heads where fi.id = ? and fi.fte_head_id=fte_heads.id) " +
		"where id = ? ";

	// end sameer 05032006
	public static String GET_ACTUALS_DATA = Misc.G_DO_ORACLE ?
  "select alternatives.prj_id, projects.name, time_id, " +
		"sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, " +
		"measure_data.time_val, time_id, 3, measure_data.val_dur)) " +
		"from measure_map_items,alternatives,  measure_data, measure_case_index, "+
		"week_timeid, projects where " +
		"alternatives.id = measure_map_items.alt_id " +
		"and alternatives.is_primary = 1 " +
		"and alternatives.prj_id = projects.id " +
		"and measure_map_items.measure_id in (5019) " +
		"and measure_data.alt_measure_id = measure_map_items.alt_measure_id " +
		"and measure_data.measure_case_index_id = measure_case_index.id " +
		"and measure_case_index.break_down = ? " +
		"and time_id = ? " +
		"and time_id >= measure_data.time_val - 12 " +
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope=0) then 105 " +
		"		when (measure_data.val_scope=1) then 420 " +
		"		when (measure_data.val_scope=2) then 35 " +
		"       when (measure_data.val_scope=3) then 12 " +
		"		when (measure_data.val_scope=5) then measure_data.val_dur*1.26 " +
		"       else 1 " +
		"       end) " +
		"group by time_id,alternatives.prj_id, projects.name " +
		"order by time_id,alternatives.prj_id, projects.name"
:    
		"select alternatives.prj_id, projects.name, time_id, " +
		"sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, " +
		"measure_data.time_val, time_id, 3, measure_data.val_dur)) " +
		"from measure_map_items,alternatives,  measure_data, measure_case_index, "+
		"week_timeid, projects where " +
		"alternatives.id = measure_map_items.alt_id " +
		"and alternatives.is_primary = 1 " +
		"and alternatives.prj_id = projects.id " +
		"and measure_map_items.measure_id in (5019) " +
		"and measure_data.alt_measure_id = measure_map_items.alt_measure_id " +
		"and measure_data.measure_case_index_id = measure_case_index.id " +
		"and measure_case_index.break_down = ? " +
		"and time_id = ? " +
		"and time_id >= measure_data.time_val - 12 " +
		"and time_id < measure_data.time_val+(case when (measure_data.val_scope=0) then 105 " +
		"		when (measure_data.val_scope=1) then 420 " +
		"		when (measure_data.val_scope=2) then 35 " +
		"       when (measure_data.val_scope=3) then 12 " +
		"		when (measure_data.val_scope=5) then measure_data.val_dur*1.26 " +
		"       else 1 " +
		"       end) " +
		"group by time_id,alternatives.prj_id, projects.name " +
		"order by time_id,alternatives.prj_id, projects.name";

	// sameer 05192006

	public static String GET_ALL_ALLOCATION_FOR_TASK_YEAR = Misc.G_DO_ORACLE ?
  "select fte_items.fte_head_id, fte_heads.name, " +
		"fte_heads.prim_skill, fte_items.assignment_status, year_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)), " +
		"fte_heads.is_generic " +
		"from " +
		"alt_work_items, alt_works, fte_items, fte_heads, year_timeid, data, " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
		" where ancs.internal_id = ? and ancs.alt_work_id = ? "+    
		" ) t1 " +
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status in (0, 1, 2) " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= ? " +
		"and year_timeid.time_id < ? " +
		"and year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"				when (data.val_scope=1) then 420 " +
		"				when (data.val_scope=2) then 35 " +
		"				when (data.val_scope=3) then 12 " +
		"				when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"				else 1 " +
		"				end) " +
		"group by fte_items.fte_head_id, fte_items.assignment_status, " +
		"year_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic " +
		"order by fte_items.fte_head_id, fte_items.assignment_status, " +
		"year_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic"
:    
		"select fte_items.fte_head_id, fte_heads.name, " +
		"fte_heads.prim_skill, fte_items.assignment_status, year_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)), " +
		"fte_heads.is_generic " +
		"from " +
		"alt_work_items, alt_works, fte_items, fte_heads, year_timeid, data, " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +
    " where ancs.internal_id = ? and ancs.alt_work_id = ? "+    
		" ) t1 " +
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status in (0, 1, 2) " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= ? " +
		"and year_timeid.time_id < ? " +
		"and year_timeid.time_id >= cast((data.year/420) as int)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"				when (data.val_scope=1) then 420 " +
		"				when (data.val_scope=2) then 35 " +
		"				when (data.val_scope=3) then 12 " +
		"				when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"				else 1 " +
		"				end) " +
		"group by fte_items.fte_head_id, fte_items.assignment_status, " +
		"year_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic " +
		"order by fte_items.fte_head_id, fte_items.assignment_status, " +
		"year_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic";

	public static String GET_ALL_ALLOCATION_FOR_TASK_QTR = Misc.G_DO_ORACLE ?
  "select fte_items.fte_head_id, fte_heads.name, " +
		"fte_heads.prim_skill, fte_items.assignment_status, qtr_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, qtr_timeid.time_id, 0, data.val_dur)), " +
		"fte_heads.is_generic " +
		"from " +
		"alt_work_items, alt_works, fte_items, fte_heads, qtr_timeid, data, " +
    " (select children.internal_id from " +
		"  alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number ) 	 " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+    
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status in (0, 1, 2) " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= ? " +
		"and qtr_timeid.time_id < ? " +
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"				when (data.val_scope=1) then 420 " +
		"				when (data.val_scope=2) then 35 " +
		"				when (data.val_scope=3) then 12 " +
		"				when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"				else 1 " +
		"				end) " +
		"group by fte_items.fte_head_id, fte_items.assignment_status, " +
		"qtr_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic " +
		"order by fte_items.fte_head_id, fte_items.assignment_status, " +
		"qtr_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic"
    :
		"select fte_items.fte_head_id, fte_heads.name, " +
		"fte_heads.prim_skill, fte_items.assignment_status, qtr_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, qtr_timeid.time_id, 0, data.val_dur)), " +
		"fte_heads.is_generic " +
		"from " +
		"alt_work_items, alt_works, fte_items, fte_heads, qtr_timeid, data, " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+    
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status in (0, 1, 2) " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= ? " +
		"and qtr_timeid.time_id < ? " +
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"				when (data.val_scope=1) then 420 " +
		"				when (data.val_scope=2) then 35 " +
		"				when (data.val_scope=3) then 12 " +
		"				when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"				else 1 " +
		"				end) " +
		"group by fte_items.fte_head_id, fte_items.assignment_status, " +
		"qtr_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic " +
		"order by fte_items.fte_head_id, fte_items.assignment_status, " +
		"qtr_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic";

	public static String GET_ALL_ALLOCATION_FOR_TASK_MONTH = Misc.G_DO_ORACLE ?
  "select fte_items.fte_head_id, fte_heads.name, " +
		"fte_heads.prim_skill, fte_items.assignment_status, month_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, month_timeid.time_id, 2, data.val_dur)), " +
		"fte_heads.is_generic " +
		"from " +
		"alt_work_items, alt_works, fte_items, fte_heads, month_timeid, data, " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+    
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status in (0, 1, 2) " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= ? " +
		"and month_timeid.time_id < ? " +
		"and month_timeid.time_id >= trunc(data.year/35)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"				when (data.val_scope=1) then 420 " +
		"				when (data.val_scope=2) then 35 " +
		"				when (data.val_scope=3) then 12 " +
		"				when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"				else 1 " +
		"				end) " +
		"group by fte_items.fte_head_id, fte_items.assignment_status, " +
		"month_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic " +
		"order by fte_items.fte_head_id, fte_items.assignment_status, " +
		"month_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic"
:    
		"select fte_items.fte_head_id, fte_heads.name, " +
		"fte_heads.prim_skill, fte_items.assignment_status, month_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, month_timeid.time_id, 2, data.val_dur)), " +
		"fte_heads.is_generic " +
		"from " +
		"alt_work_items, alt_works, fte_items, fte_heads, month_timeid, data, " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+    
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status in (0, 1, 2) " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= ? " +
		"and month_timeid.time_id < ? " +
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"				when (data.val_scope=1) then 420 " +
		"				when (data.val_scope=2) then 35 " +
		"				when (data.val_scope=3) then 12 " +
		"				when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"				else 1 " +
		"				end) " +
		"group by fte_items.fte_head_id, fte_items.assignment_status, " +
		"month_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic " +
		"order by fte_items.fte_head_id, fte_items.assignment_status, " +
		"month_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic";

	public static String GET_ALL_ALLOCATION_FOR_TASK_WEEK = Misc.G_DO_ORACLE ?
  "select fte_items.fte_head_id, fte_heads.name, " +
		"fte_heads.prim_skill, fte_items.assignment_status, week_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)), " +
		"fte_heads.is_generic " +
		"from " +
		"alt_work_items, alt_works, fte_items, fte_heads, week_timeid, data, " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+    
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status in (0, 1, 2) " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= ? " +
		"and week_timeid.time_id < ? " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"				when (data.val_scope=1) then 420 " +
		"				when (data.val_scope=2) then 35 " +
		"				when (data.val_scope=3) then 12 " +
		"				when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"				else 1 " +
		"				end) " +
		"group by fte_items.fte_head_id, fte_items.assignment_status, " +
		"month_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic " +
		"order by fte_items.fte_head_id, fte_items.assignment_status, " +
		"month_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic"
:    
		"select fte_items.fte_head_id, fte_heads.name, " +
		"fte_heads.prim_skill, fte_items.assignment_status, week_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)), " +
		"fte_heads.is_generic " +
		"from " +
		"alt_work_items, alt_works, fte_items, fte_heads, week_timeid, data, " +
		" (select children.internal_id from " +
		"	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = fte_heads.id " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status in (0, 1, 2) " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= ? " +
		"and week_timeid.time_id < ? " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"				when (data.val_scope=1) then 420 " +
		"				when (data.val_scope=2) then 35 " +
		"				when (data.val_scope=3) then 12 " +
		"				when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"				else 1 " +
		"				end) " +
		"group by fte_items.fte_head_id, fte_items.assignment_status, " +
		"month_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic " +
		"order by fte_items.fte_head_id, fte_items.assignment_status, " +
		"month_timeid.time_id, fte_heads.name, fte_heads.prim_skill, fte_heads.is_generic";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_YEAR_USING_INTERNAL_ID = Misc.G_DO_ORACLE ?
  "select fte_items.id, year_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)), fte_items.start_date, fte_items.end_date  " +
		"from " +
		"alt_work_items, alt_works, fte_items, year_timeid, data " +
    ", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +				
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, year_timeid.time_id, fte_items.start_date, fte_items.end_date  " +
		"order by fte_items.id, year_timeid.time_id "
:    
		"select fte_items.id, year_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)), fte_items.start_date, fte_items.end_date  " +
		"from " +
		"alt_work_items, alt_works, fte_items, year_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= cast((data.year/420) as int)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, year_timeid.time_id, fte_items.start_date, fte_items.end_date  " +
		"order by fte_items.id, year_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_QTR_USING_INTERNAL_ID = Misc.G_DO_ORACLE ?
  "select fte_items.id, qtr_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, qtr_timeid.time_id, 0, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		"from " +
		"alt_work_items, alt_works, fte_items, qtr_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, qtr_timeid.time_id, fte_items.start_date, fte_items.end_date  " +
		"order by fte_items.id, qtr_timeid.time_id "
:    
		"select fte_items.id, qtr_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, qtr_timeid.time_id, 0, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		"from " +
		"alt_work_items, alt_works, fte_items, qtr_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, qtr_timeid.time_id, fte_items.start_date, fte_items.end_date  " +
		"order by fte_items.id, qtr_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_MONTH_USING_INTERNAL_ID = Misc.G_DO_ORACLE ?
  "select fte_items.id, month_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, month_timeid.time_id, 2, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		"from " +
		"alt_work_items, alt_works, fte_items, month_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= trunc(data.year/35)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, month_timeid.time_id, fte_items.start_date, fte_items.end_date " +
		"order by fte_items.id, month_timeid.time_id "
:    
		"select fte_items.id, month_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, month_timeid.time_id, 2, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		"from " +
		"alt_work_items, alt_works, fte_items, month_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, month_timeid.time_id, fte_items.start_date, fte_items.end_date " +
		"order by fte_items.id, month_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_WEEK_USING_INTERNAL_ID = Misc.G_DO_ORACLE ?
  "select fte_items.id, week_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		"from " +
		"alt_work_items, alt_works, fte_items, week_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+    
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, week_timeid.time_id, fte_items.start_date, fte_items.end_date " +
		"order by fte_items.id, week_timeid.time_id "
:    
		"select fte_items.id, week_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)), fte_items.start_date, fte_items.end_date " +
		"from " +
		"alt_work_items, alt_works, fte_items, week_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, week_timeid.time_id, fte_items.start_date, fte_items.end_date " +
		"order by fte_items.id, week_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_YEAR_TIME_LIMITED_USING_INTERNAL_ID = Misc.G_DO_ORACLE ?
  "select fte_items.id, year_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, year_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= ? " +
		"and year_timeid.time_id <= ? " +
		"and year_timeid.time_id >= trunc(data.year/420)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, year_timeid.time_id " +
		"order by fte_items.id, year_timeid.time_id "
:    
		"select fte_items.id, year_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, year_timeid.time_id, 1, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, year_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and year_timeid.time_id >= ? " +
		"and year_timeid.time_id <= ? " +
		"and year_timeid.time_id >= cast((data.year/420) as int)*420 " +
		"and year_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, year_timeid.time_id " +
		"order by fte_items.id, year_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_QTR_TIME_LIMITED_USING_INTERNAL_ID =
  Misc.G_DO_ORACLE ?
  	"select fte_items.id, qtr_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, qtr_timeid.time_id, 0, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, qtr_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= ? " +
		"and qtr_timeid.time_id <= ? " +
		"and qtr_timeid.time_id >= trunc(data.year/105)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, qtr_timeid.time_id " +
		"order by fte_items.id, qtr_timeid.time_id "
:    
		"select fte_items.id, qtr_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, qtr_timeid.time_id, 0, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, qtr_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and qtr_timeid.time_id >= ? " +
		"and qtr_timeid.time_id <= ? " +
		"and qtr_timeid.time_id >= cast((data.year/105) as int)*105 " +
		"and qtr_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, qtr_timeid.time_id " +
		"order by fte_items.id, qtr_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_MONTH_TIME_LIMITED_USING_INTERNAL_ID = Misc.G_DO_ORACLE ?
  	"select fte_items.id, month_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, month_timeid.time_id, 2, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, month_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= ? " +
		"and month_timeid.time_id <= ? " +
		"and month_timeid.time_id >= trunc(data.year/35)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, month_timeid.time_id " +
		"order by fte_items.id, month_timeid.time_id "
:    
		"select fte_items.id, month_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, month_timeid.time_id, 2, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, month_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and month_timeid.time_id >= ? " +
		"and month_timeid.time_id <= ? " +
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 " +
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, month_timeid.time_id " +
		"order by fte_items.id, month_timeid.time_id ";

	public static String GET_RES_ALLOCATION_BY_TASK_BY_WEEK_TIME_LIMITED_USING_INTERNAL_ID = Misc.G_DO_ORACLE ?
  	"select fte_items.id, week_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, week_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= ? " +
		"and week_timeid.time_id <= ? " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, week_timeid.time_id " +
		"order by fte_items.id, week_timeid.time_id "
:    
		"select fte_items.id, week_timeid.time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, week_timeid.time_id, 3, data.val_dur)) " +
		"from " +
		"alt_work_items, alt_works, fte_items, week_timeid, data " +
		", (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
		" where "+
    " ancs.internal_id = ? "+
    " and ancs.alt_work_id = ? "+
		" ) t1 " +		
		"where " +
		"alt_work_items.internal_id = ? " +
		"and alt_works.id = alt_work_items.alt_work_id " +
		"and alt_work_items.alt_work_id = ? " +
		"and fte_items.alt_fte_id = alt_works.ref_alt_fte_id " +
		"and fte_items.fte_head_id = ? " +
		"and fte_items.task_internal_id = tl.internal_id " +
		"and fte_items.to_include = 1 " +
		"and fte_items.assignment_status = ? " +
		"and data.fte_item_id = fte_items.id " +
		"and week_timeid.time_id >= ? " +
		"and week_timeid.time_id <= ? " +
		"and week_timeid.time_id >= data.year - 12 " +
		"and week_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 " +
		"when (data.val_scope=1) then 420 " +
		"when (data.val_scope=2) then 35 " +
		"when (data.val_scope=3) then 12 " +
		"when (data.val_scope=5) then data.val_dur*1.25+7 " +
		"else 1 " +
		"end) " +
		"group by fte_items.id, week_timeid.time_id " +
		"order by fte_items.id, week_timeid.time_id ";

	//end sameer 05192006

	// sameer 05302006

	public static String GET_BLOCK_TIME =
		"select off_times.purpose, off_times.val_unavail " +
		"from off_times " +
		"where " +
		"off_times.is_block_time = 1 " +
		"and off_times.fte_head_id = ?";

	public static String DELETE_BLOCK_TIMES_CLAUSE =
		"delete from off_times where fte_head_id = ? and off_times.is_block_time = 1";

	public static String BLOCK_TIMES_INSERT_CLAUSE =
		"insert into off_times " +
		"(fte_head_id, purpose, val_unavail, is_block_time) " +
		"values(?, ?, ?, 1) ";
//NOT USED
	public static String GET_ALT_ID_FOR_ACTUALS = Misc.G_DO_ORACLE ?
  "select alt_measure_id, alt_map_items.wspace_id, " +
		"alternatives.id,alternatives.prj_id from alt_map_items, alternatives, " +
		"measure_map_items where " +
		"alternatives.prj_id = ? " +
		"and alternatives.is_primary = 1 " +
		"and alt_map_items.map_type = 1 " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.alt_id = alternatives.id " +
		"and alt_map_items.alt_id = measure_map_items.alt_id(+) " +
		"and measure_map_items.measure_id(+) = ? " +
		"and measure_map_items.wspace_id(+) = alt_map_items.wspace_id"
:    
		"select alt_measure_id, alt_map_items.wspace_id, " +
		"alternatives.id,alternatives.prj_id from alt_map_items, alternatives, " +
		"measure_map_items where " +
		"alternatives.prj_id = ? " +
		"and alternatives.is_primary = 1 " +
		"and alt_map_items.map_type = 1 " +
		"and alt_map_items.isdefault = 1 " +
		"and alt_map_items.alt_id = alternatives.id " +
		"and alt_map_items.alt_id *= measure_map_items.alt_id " +
		"and measure_map_items.measure_id =* ? " +
		"and measure_map_items.wspace_id =* alt_map_items.wspace_id";

	public static String DELETE_ACTUALS_DATA =
		"delete from measure_data where " +
		"time_val >= ? " +
		"and (measure_case_index_id in " +
		"	(select measure_case_index.id from measure_case_index where " +
		"	measure_id = ? " +
		"	and break_down = ? " +
		"	) " +
		")";

	public static String GET_MEASURE_CASE_INDEX_ID =
		"select id from measure_case_index where measure_id = ? " +
		"and break_down = ?";

	public static String GET_OFFTIME_BY_WEEK_FOR_PARTICULAR_RES = Misc.G_DO_ORACLE ?
  "select time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 3) " +
		"					else " +
		"					Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 3, Round(end_date-start_date+1,0)) " +
		"				end) " +
		")v, 3 " +
		"from off_times, fte_heads , week_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and fte_heads.id = ? " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= getTimeId(start_date) -12) " +
		"and (end_date is null or time_id <= getTimeId(end_date)) " +
		"group by time_id " +
		"order by time_id"
:    
		"select time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then intelli.getDuration(time_id, 3) " +
		"					else " +
		"					cast((end_date-start_date+1) as numeric) * intelli.getPropIncluded(5, intelli.getTimeId(start_date), time_id, 3, Cast((end_date-start_date+1) as numeric)) " +
		"				end) " +
		")v, 3 " +
		"from off_times, fte_heads , week_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and fte_heads.id = ? " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= intelli.getTimeId(start_date) -12) " +
		"and (end_date is null or time_id <= intelli.getTimeId(end_date)) " +
		"group by time_id " +
		"order by time_id";

	public static String GET_OFFTIME_BY_MONTH_FOR_PARTICULAR_RES = Misc.G_DO_ORACLE ?
  	"select time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 2) " +
		"					else " +
		"					Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 2, Round(end_date-start_date+1,0)) " +
		"				end) " +
		")v, 2 " +
		"from off_times, fte_heads , month_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and fte_heads.id = ? " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= trunc(getTimeId(start_date)/35)*35) " +
		"and (end_date is null or time_id <= getTimeId(end_date)) " +
		"group by time_id " +
		"order by time_id"
:    
		"select time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then intelli.getDuration(time_id, 2) " +
		"					else " +
		"					cast((end_date-start_date+1) as numeric) * intelli.getPropIncluded(5, intelli.getTimeId(start_date), time_id, 2, cast((end_date-start_date+1) as numeric)) " +
		"				end) " +
		")v, 2 " +
		"from off_times, fte_heads , month_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and fte_heads.id = ? " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= cast((intelli.getTimeId(start_date)/35) as int)*35) " +
		"and (end_date is null or time_id <= intelli.getTimeId(end_date)) " +
		"group by time_id " +
		"order by time_id";

	public static String GET_OFFTIME_BY_QTR_FOR_PARTICULAR_RES = Misc.G_DO_ORACLE ?
  "select time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 1) " +
		"					else " +
		"					Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 0, Round(end_date-start_date+1,0)) " +
		"				end) " +
		")v, 0 " +
		"from off_times, fte_heads , qtr_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and fte_heads.id = ? " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= trunc(getTimeId(start_date)/105)*105) " +
		"and (end_date is null or time_id <= getTimeId(end_date)) " +
		"group by time_id " +
		"order by time_id"
:    
		"select time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then intelli.getDuration(time_id, 1) " +
		"					else " +
		"					cast((end_date-start_date+1) as numeric) * intelli.getPropIncluded(5, intelli.getTimeId(start_date), time_id, 0, cast((end_date-start_date+1) as numeric)) " +
		"				end) " +
		")v, 0 " +
		"from off_times, fte_heads , qtr_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and fte_heads.id = ? " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= cast((intelli.getTimeId(start_date)/105) as int)*105) " +
		"and (end_date is null or time_id <= intelli.getTimeId(end_date)) " +
		"group by time_id " +
		"order by time_id";

	public static String GET_OFFTIME_BY_YEAR_FOR_PARTICULAR_RES = Misc.G_DO_ORACLE ?
  "select time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then getDuration(time_id, 0) " +
		"					else " +
		"					Round(end_date-start_date+1,0) * intelli.getPropIncluded(5, getTimeId(start_date), time_id, 1, Round(end_date-start_date+1,0)) " +
		"				end) " +
		")v, 1 " +
		"from off_times, fte_heads , year_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and fte_heads.id = ? " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= trunc(getTimeId(start_date)/420)*420) " +
		"and (end_date is null or time_id <= getTimeId(end_date)) " +
		"group by time_id " +
		"order by time_id"
:    
		"select time_id, " +
		"sum(val_unavail * (case " +
		"					when off_times.start_date is null or off_times.end_date is null or off_times.is_block_time = 1 then intelli.getDuration(time_id, 0) " +
		"					else " +
		"					cast((end_date-start_date+1) as numeric) * intelli.getPropIncluded(5, intelli.getTimeId(start_date), time_id, 1, cast((end_date-start_date+1) as numeric)) " +
		"				end) " +
		")v, 1 " +
		"from off_times, fte_heads , year_timeid " +
		"where is_active = 1 " +
		"and is_generic is not null and is_generic <> 1 " +
		"and fte_heads.id = ? " +
		"and off_times.fte_head_id = fte_heads.id " +
		"and (end_date >= ? or end_date is null) " +
		"and time_id >= ? " +
		"and time_id <= ? " +
		"and (start_date is null or time_id >= cast((intelli.getTimeId(start_date)/420) as int)*420) " +
		"and (end_date is null or time_id <= intelli.getTimeId(end_date)) " +
		"group by time_id " +
		"order by time_id";

	// end sameer 05302006
	//rajeev 061506
  //TODO_INQUERY .... later
	public static String DATA_UPD_PRESERVE_PAST_FTE = Misc.G_DO_ORACLE ?
  "update "+
		"(select data.id i, data.value v, data.val_dur d, data.val_scope s, data.year y, getSimplePropIncluded(year, val_scope, val_dur, intelli.getDateFor(year), ?) p, intelli.getDateFor(year) dt "+
		"from data "+
		"where "+
		"data.fte_item_id in (select fte_items.id from fte_items where alt_fte_id = ? and task_internal_id = ? and fte_items.fte_head_id in ( " //followed by ))
:    
		"update "+
		"(select data.id i, data.value v, data.val_dur d, data.val_scope s, data.year y, intelli.getSimplePropIncluded(year, val_scope, val_dur, intelli.getDateFor(year), ?) p, intelli.getDateFor(year) dt "+
		"from data "+
		"where "+
		"data.fte_item_id in (select fte_items.id from fte_items where alt_fte_id = ? and task_internal_id = ? and fte_items.fte_head_id in ( "; //followed by ))
	//TODO_INQUERY
  public static String DATA_UPD_PRESERVE_PAST_DATA_APPENDIX =
		") dataset "+
		"set dataset.s = (case when p >= 0.0001 and p <= 0.9995 then 5 else s end) "+
		",dataset.d = (case when p >= 0.0001 and p <= 0.9995 then round(?-dt) else d end) "+
		",dataset.v = dataset.v * p ";
//TODO_INQUERY    .... later
	public static String DATA_UPD_PRESERVE_PAST_COST = Misc.G_DO_ORACLE ?
  "update "+
		"(select data.id i, data.value v, data.val_dur d, data.val_scope s, data.year y, getSimplePropIncluded(year, val_scope, val_dur, intelli.getDateFor(year), ?) p, intelli.getDateFor(year) dt "+
		"from data "+
		"where "+
		"data.cost_li_id in (select cost_items.id from cost_items where cost_items.alt_devcost_id = ? and task_internal_id = ? and cost_items.cost_cent_id in ( "
:    
		"update "+
		"(select data.id i, data.value v, data.val_dur d, data.val_scope s, data.year y, intelli.getSimplePropIncluded(year, val_scope, val_dur, intelli.getDateFor(year), ?) p, intelli.getDateFor(year) dt "+
		"from data "+
		"where "+
		"data.cost_li_id in (select cost_items.id from cost_items where cost_items.alt_devcost_id = ? and task_internal_id = ? and cost_items.cost_cent_id in ( ";
	//TODO_INQUERY
  public static String DELETE_FTE_DATA = Misc.G_DO_ORACLE ? "delete from data where value >= -0.00005 and value <= 0.00005 and fte_item_id in (select fte_items.id from fte_items where alt_fte_id = ?)"
  :
  "delete from data from fte_items where value >= -0.00005 and value <= 0.00005 and fte_item_id=fte_items.id and alt_fte_id = ?";
	//TODO_INQUERY
  public static String DELETE_COST_DATA = Misc.G_DO_ORACLE ? "delete from data where value >= -0.00005 and value <= 0.00005 and cost_li_id in (select cost_items.id from cost_items where cost_items.alt_devcost_id = ?)"
  :
  "delete from data from cost_items where value >= -0.00005 and value <= 0.00005 and cost_li_id = cost_items.id and cost_items.alt_devcost_id = ?";
  
//  public static String DELETE_FTE_ITEM_SCEN = "delete from fte_items f1 where f1.alt_fte_id = ? "+
//		"and not(exists (select 1 from fte_items, data where fte_items.id = f1.id and data.fte_item_id = fte_items.id)) ";

//	public static String DELETE_COST_ITEM_SCEN = "delete from cost_items f1 where f1.alt_devcost_id = ? "+
//		"and not(exists (select 1 from cost_items, data where cost_items.id = f1.id and data.cost_li_id = cost_items.id)) ";

//	public static String DELETE_OPCOST_ITEM_SCEN = "delete from cost_items f1 where f1.alt_opcost_id = ? "+
//		"and not(exists (select 1 from cost_items, data where cost_items.id = f1.id and data.cost_li_id = cost_items.id)) ";

//	public static String DELETE_REV_ITEM_SCEN = "delete from rev_segs f1 where f1.alt_rev_id = ? "+
//		"and not(exists (select 1 from rev_segs, data where rev_segs.id = f1.id and data.rev_seg_id = rev_segs.id)) ";

//	public static String DELETE_NPV_ITEM_SCEN = "delete from npv f1 where f1.alt_combined_id = ? "+
//		"and not(exists (select 1 from npv, data where npv.id = f1.id and data.npv_id = npv.id)) ";

     public static String DELETE_MEASURE_ITEM_SCEN =
" delete from measure_case_index f1 where measure_id = ? and "+
" not (exists (select 1 from "+
" measure_data "+
" "+
" where measure_data.measure_case_index_id = f1.id "+
")) ";

	//the order in which parameters are speced below for lookup is imp - trying to match alt_cre_fte_items and alt_cre_devcost_items so that
	//we can look up attributes in loop or appropriately set up the parameters - see helpPopulateAndExecuteResUpd
	public static String FTE_ITEM_LOOKUP = "select id from fte_items where alt_fte_id = ? and task_internal_id = ? "+
		"and (for_achieving_milestone = ?) "+
		"and fte_head_id = ? "+
		"and (? is null or ? = classify1) "+
		"and (? is null or ? = classify2) "+
		"and (? is null or ? = classify3) "+
		"and (? is null or ? = classify4) "+
		"and (? is null or ? = classify5) "+
		"and (? is null or ? = target_market) "+
		"and ((? is null and scen_id = 1) or ? = scen_id) "+
		"and (? is null or ? = for_skill) "+
		"and (? is null or ? = assignment_status)";

	//the order in which parameters are speced below for lookup is imp - trying to match alt_cre_fte_items and alt_cre_devcost_items so that
	//we can look up attributes in loop or appropriately set up the parameters - see helpPopulateAndExecuteResUpd
	public static String COST_ITEM_LOOKUP = "select id from cost_items where cost_items.alt_devcost_id = ? and task_internal_id = ? "+
		"and (for_achieving_milestone = ?) "+
		"and cost_cent_id = ? "+
		"and (? is null or ? = classify1) "+
		"and (? is null or ? = classify2) "+
		"and (? is null or ? = classify3) "+
		"and (? is null or ? = classify4) "+
		"and (? is null or ? = classify5) "+
		"and (? is null or ? = target_market) "+
		"and ((? is null and scen_id = 1) or ? = scen_id) ";

	public static String GET_MAX_FTE_LINEID = "select max(fte_lineitem_id) from fte_items where alt_fte_id = ?";
	public static String GET_MAX_COST_LINEID = "select max(lineitem_id) from cost_items where cost_items.alt_devcost_id = ?";

	public static String UPDATE_START_END_AW = "update alt_work_items set start_date = ?, end_date = ? where alt_work_id = ? and internal_id = ?";
	public static String UPDATE_FTE_START_END = "update fte_items set (start_date,end_date) = (select start_date,end_date from alt_work_items where alt_work_items.alt_work_id = ? and alt_work_items.internal_id = fte_items.task_internal_id) where alt_fte_id = ?";
	public static String UPDATE_COST_START_END = "update cost_items set (start_date,end_date) = (select start_date,end_date from alt_work_items where alt_work_items.alt_work_id = ? and alt_work_items.internal_id = cost_items.task_internal_id) where cost_items.alt_devcost_id = ?";
	public static String COPY_PORTFOLIO_SCENARIOS = "insert into portfolio_scenario (port_rs_id, alt_id, ms_id, target_market, action, amount, scope, seq_number) (select ?, alt_id, ms_id, target_market, action, amount, scope, seq_number from portfolio_scenario where port_rs_id = ?)";
	public static String UPDATE_PORTRSET_WITH_ORIG_ALT_MEASURE_ALL_PRJ = "update port_results_measure upd set (alt_measure_id, measure_id) "+
		" = (select alt_measure_id, measure_id from port_results_measure, port_rset where port_rset.port_wksp_id = ? and is_auto_updateable=1 and port_results_measure.port_rs_id = port_rset.id and port_results_measure.alt_id = upd.alt_id and rownum <=1) "+
		" where port_rs_id = ? and prj_id = ? ";
  //below not used
	public static String DELETE_PORT_SCENARIO_FOR_PRJ = "delete from (select port_rs_id, alt_id from portfolio_scenario, alternatives where port_rs_id = ? and alt_id = alternatives.id and alternatives.prj_id = ?)";
    
	public static String UPDATE_PORTRSET_WITH_ORIG_ALL = "update port_results upd set (ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id) "+
		" = (select ver_alt_mstone_id, ver_alt_basic_id, ver_alt_profile_id, ver_alt_model_id, ver_prj_basic_id, ver_alt_work_id, ver_alt_fte_id, ver_alt_devcost_id, ver_alt_opcost_id, ver_alt_combined_id, ver_alt_rev_id, ver_alt_rating_id from port_results, port_rset where port_rset.port_wksp_id = ? and is_auto_updateable=1 and port_results.port_rs_id = port_rset.id and alt_id=upd.alt_id and prj_id=upd.prj_id and rownum <=1) "+
		" where port_rs_id = ?";

	public static String UPDATE_PORTRSET_WITH_ORIG_ALT_MEASURE_ALL = "update port_results_measure upd set (alt_measure_id, measure_id) "+
		" = (select alt_measure_id, measure_id from port_results_measure, port_rset where port_rset.port_wksp_id = ? and is_auto_updateable=1 and port_results_measure.port_rs_id = port_rset.id and port_results_measure.alt_id = upd.alt_id and rownum <=1) "+
		" where port_rs_id = ?";
	public static String DELETE_PORT_SCENARIO_FOR_ALL = "delete from portfolio_scenario where port_rs_id = ?";
	public static String GET_PORT_SCEN_HIST = "select prj_id, ref1, ref2, ref3, ref4, ref5, action, description from portfolio_scenario_history where port_rs_id = ? order by seq_id";

	public static String COPY_PORTFOLIO_HIST = "insert into portfolio_scenario_history (port_rs_id, seq_id, prj_id, ref1, ref2, ref3, ref4, ref5, action, description) (select ?, seq_id, prj_id, ref1, ref2, ref3, ref4, ref5, action, description from portfolio_scenario_history where port_rs_id = ?)";

	public static String PREP_PORT_HIST_DEL_EXTRA_RESULTS = "delete from portfolio_scenario_history where "+
		"port_rs_id in "+
		"(select port_rset.id from port_rset where port_rset.port_wksp_id = ?) "+
		"and "+
		"not (prj_id in "+
		"(select prj_id from port_desired_ver where port_wksp_id = ?) "+
		") ";

	public static String INSERT_PORT_HIST = "insert into portfolio_scenario_history(port_rs_id, seq_id, action, prj_id, ref1, ref2, ref3, ref4, ref5, description) values (?,?,?,?,?,?,?,?,?,?) ";


	public static String GET_FTE_ASSUMPTIONS_NEW =
		"select fte_items.target_market, fte_items.for_skill, fte_items.for_achieving_milestone, " +
		"sum(data.value) " +
		"from fte_items, data where " +
		" fte_items.alt_fte_id = ? " +
		"and data.fte_item_id = fte_items.id " +
		"group by fte_items.target_market, fte_items.for_achieving_milestone, fte_items.for_skill";
//		"order by fte_items.for_achieving_milestone, fte_items.for_skill ";
	public static String GET_DEVCOST_ASSUMPTIONS_NEW =
		"select cost_items.target_market, cost_items.cost_cent_id, cost_items.for_achieving_milestone, " +
		"sum(data.value) " +
		"from cost_items, data where " +
		" cost_items.alt_devcost_id = ? " +
		"and data.cost_li_id = cost_items.id " +
		"group by cost_items.target_market, cost_items.for_achieving_milestone, cost_items.cost_cent_id";

	// sameer 06262006
  //TODO_*=
	public static String GET_PORT_ACTION_FOR_ALL_PROJECTS = Misc.G_DO_ORACLE ?
  "select portfolio_scenario_history.prj_id, projects.name, portfolio_scenario_history.seq_id, " +
		"portfolio_scenario_history.action, portfolio_scenario_history.description, " +
		"portfolio_scenario_history.ref1, portfolio_scenario_history.ref2, portfolio_scenario_history.ref3, " +
		"portfolio_scenario_history.ref4, portfolio_scenario_history.ref5 " +
		"from portfolio_scenario_history, projects	" +
		"where portfolio_scenario_history.port_rs_id = ? " +
		  "and portfolio_scenario_history.prj_id = projects.id(+) " +
		"order by portfolio_scenario_history.seq_id desc"
		:
		//"select portfolio_scenario_history.prj_id, projects.name, portfolio_scenario_history.seq_id, " +
		//"portfolio_scenario_history.action, portfolio_scenario_history.description, " +
		//"portfolio_scenario_history.ref1, portfolio_scenario_history.ref2, portfolio_scenario_history.ref3, " +
		//"portfolio_scenario_history.ref4, portfolio_scenario_history.ref5 " +
		//"from portfolio_scenario_history, projects	" +
		//"where portfolio_scenario_history.port_rs_id = ? " +
		//"and portfolio_scenario_history.prj_id *= projects.id " +
		//"order by portfolio_scenario_history.seq_id desc";
		"select portfolio_scenario_history.prj_id, projects.name, portfolio_scenario_history.seq_id, " +
		"portfolio_scenario_history.action, portfolio_scenario_history.description, " +
		"portfolio_scenario_history.ref1, portfolio_scenario_history.ref2, portfolio_scenario_history.ref3, " +
		"portfolio_scenario_history.ref4, portfolio_scenario_history.ref5 " +
		"from portfolio_scenario_history " +
		"left outer join projects on portfolio_scenario_history.prj_id = projects.id " +
		"where portfolio_scenario_history.port_rs_id = ? " +
		"order by portfolio_scenario_history.seq_id desc ";
//TODO_*=
	public static String GET_PORT_ACTION_FOR_PARTICULAR_PROJECT = Misc.G_DO_ORACLE ?
  "select portfolio_scenario_history.prj_id, projects.name, portfolio_scenario_history.seq_id, " +
		"portfolio_scenario_history.action, portfolio_scenario_history.description, " +
		"portfolio_scenario_history.ref1, portfolio_scenario_history.ref2, portfolio_scenario_history.ref3, " +
		"portfolio_scenario_history.ref4, portfolio_scenario_history.ref5 " +
		"from portfolio_scenario_history, projects " +
		"where portfolio_scenario_history.port_rs_id = ? " +
		"and (portfolio_scenario_history.prj_id = ? or portfolio_scenario_history.prj_id is null) " +
		  "and portfolio_scenario_history.prj_id = projects.id(+) " +
		"order by portfolio_scenario_history.seq_id desc"
		:
		//"select portfolio_scenario_history.prj_id, projects.name, portfolio_scenario_history.seq_id, " +
		//"portfolio_scenario_history.action, portfolio_scenario_history.description, " +
		//"portfolio_scenario_history.ref1, portfolio_scenario_history.ref2, portfolio_scenario_history.ref3, " +
		//"portfolio_scenario_history.ref4, portfolio_scenario_history.ref5 " +
		//"from portfolio_scenario_history, projects " +
		//"where portfolio_scenario_history.port_rs_id = ? " +
		//"and (portfolio_scenario_history.prj_id = ? or portfolio_scenario_history.prj_id is null) " +
		//  "and portfolio_scenario_history.prj_id *= projects.id " +
		//"order by portfolio_scenario_history.seq_id desc";
		"select portfolio_scenario_history.prj_id, projects.name, portfolio_scenario_history.seq_id, " +
		"portfolio_scenario_history.action, portfolio_scenario_history.description, " +
		"portfolio_scenario_history.ref1, portfolio_scenario_history.ref2, portfolio_scenario_history.ref3, " +
		"portfolio_scenario_history.ref4, portfolio_scenario_history.ref5 " +
		"from portfolio_scenario_history " +
		"left outer join projects on portfolio_scenario_history.prj_id = projects.id " +
		"where portfolio_scenario_history.port_rs_id = ? " +
		"and (portfolio_scenario_history.prj_id = ? or portfolio_scenario_history.prj_id is null) " +
		"order by portfolio_scenario_history.seq_id desc ";
	// end sameer 06262006


     public static String GET_DATA_BY_MONTH_FTE = //doesn't work for custom dur or off-cycle data.year
     Misc.G_DO_ORACLE ?
     "select fte_items.id, fte_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_head_id, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status, fte_heads.name, task_internal_id "+
		"from data, fte_items, month_timeid, fte_heads  "+
		"where fte_items.alt_fte_id = ? "+
		"and data.fte_item_id = fte_items.id "+
		" and time_id >= trunc(data.year/35)*35 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

          "and fte_items.fte_head_id = fte_heads.id "+
		"group by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name "
:    
		"select fte_items.id, fte_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		"target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_head_id, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status, fte_heads.name, task_internal_id "+
		"from data, fte_items, month_timeid, fte_heads  "+
		"where fte_items.alt_fte_id = ? "+
		"and data.fte_item_id = fte_items.id "+
		" and time_id >= cast((data.year/35) as int)*35 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

          "and fte_items.fte_head_id = fte_heads.id "+
		"group by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name ";

     public static String GET_DATA_BY_QUARTER_FTE = //doesn't work for custom dur or off-cycle data.year
     Misc.G_DO_ORACLE ?
"select fte_items.id, fte_items.name, time_id, " +
		"sum(data.value * getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_head_id, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status, fte_heads.name, task_internal_id "+
		"from data, fte_items, qtr_timeid, fte_heads  "+
		"where fte_items.alt_fte_id = ? "+
		"and data.fte_item_id = fte_items.id "+
		" and time_id >= trunc(data.year/105)*105 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
          "and fte_items.fte_head_id = fte_heads.id "+
		"group by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name "
:    
		"select fte_items.id, fte_items.name, time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 0, data.val_dur)), " +
		"target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_head_id, fte_items.scen_id, fte_items.for_skill, fte_items.assignment_status, fte_heads.name, task_internal_id "+
		"from data, fte_items, qtr_timeid, fte_heads  "+
		"where fte_items.alt_fte_id = ? "+
		"and data.fte_item_id = fte_items.id "+
		" and time_id >= cast((data.year/105) as int)*105 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
          "and fte_items.fte_head_id = fte_heads.id "+
		"group by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name, task_internal_id  "+
		"order by scen_id, target_market, for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, fte_items.classify4, fte_items.classify5, fte_items.id, fte_items.name, fte_head_id, fte_items.for_skill, fte_items.assignment_status, time_id, fte_heads.name ";

     public static String RISK_REGISTER = "select risk_items.id, risk_items.risk_id, risk_template_detail.hier_name, short_name, risk_items.description, overall_likelihood, overall_impact, mitigation_strategy, mitigation_desc, getRiskFunction(risk_items.id, risk_items.risk_status, risk_items.time_start, risk_items.time_end, ?) rst, risk_headers.risk_template_id "+
          "from risk_items, pj_map_items, risk_template_map, risk_headers, risk_template_detail "+
          "where "+
          "pj_map_items.wspace_id = ? "+
          "and pj_map_items.isdefault = 1 "+
          "and risk_headers.id = pj_map_items.risk_header_id "+
          "and risk_items.risk_header_id = risk_headers.id "+
          "and risk_template_map.risk_template_id = risk_headers.risk_template_id "+
          "and risk_template_detail.risk_tmplt_item = risk_items.category "+
          "and risk_template_map.anc_id = ? "+
          "and risk_items.category = risk_template_map.leaf_id "+
          "and risk_items.risk_status <> 0 "+
          "and (getRiskFunction(risk_items.id, risk_items.risk_status, risk_items.time_start, risk_items.time_end, ?) = ? or ? is null) "+
          "order by risk_items.risk_id ";

     public static String GET_RISK_TEMPLATES = "select id, name, description from risk_templates where status <> 0 order by name";

     public static String GET_RISK_INFO = "select risk_tmplt_item, name, description, parent_id, is_category from risk_template_detail where risk_template_id = ? order by risk_tmplt_item";

     public static String GET_RISK_IDENTIFY = Misc.G_DO_ORACLE ?
     "select risk_items.id, risk_items.category, risk_template_detail.hier_name, risk_items.is_upside, risk_items.short_name, risk_items.description, risk_items.applies_to_type, risk_items.applies_to_id, task_list.tjn, risk_items.time_start, risk_items.time_end, task_list.aid, risk_items.risk_condition "+
          "from risk_items, risk_template_detail, risk_headers "+
          ",(select alt_work_items.name tjn, alternatives.id aid, alt_work_items.internal_id iid from alternatives, workspaces, alt_map_items, alt_work_items "+
          "where workspaces.id = ? "+
          "and alt_map_items.wspace_id = workspaces.id "+
          "and alt_map_items.isdefault = 1 "+
          "and alt_map_items.alt_id = alternatives.id "+
          "and alternatives.is_primary = 1 "+
          "and alt_work_items.alt_work_id = alt_map_items.alt_work_id "+
          ") task_list "+
          "where "+
          "risk_items.id = ? "+
          "and risk_headers.id = risk_items.risk_header_id "+
          "and risk_headers.risk_template_id = risk_template_detail.risk_template_id "+
//          "and risk_template_detail.risk_template_id = ? "+
          "and risk_template_detail.risk_tmplt_item = risk_items.category "+
          "and risk_items.applies_to_id = task_list.iid(+)"
:          
     "select risk_items.id, risk_items.category, risk_template_detail.hier_name, risk_items.is_upside, risk_items.short_name, risk_items.description, risk_items.applies_to_type, risk_items.applies_to_id, task_list.tjn, risk_items.time_start, risk_items.time_end, task_list.aid, risk_items.risk_condition "+
          "from risk_items, risk_template_detail, risk_headers "+
          ",(select alt_work_items.name tjn, alternatives.id aid, alt_work_items.internal_id iid from alternatives, workspaces, alt_map_items, alt_work_items "+
          "where workspaces.id = ? "+
          "and alt_map_items.wspace_id = workspaces.id "+
          "and alt_map_items.isdefault = 1 "+
          "and alt_map_items.alt_id = alternatives.id "+
          "and alternatives.is_primary = 1 "+
          "and alt_work_items.alt_work_id = alt_map_items.alt_work_id "+
          ") task_list "+
          "where "+
          "risk_items.id = ? "+
          "and risk_headers.id = risk_items.risk_header_id "+
          "and risk_headers.risk_template_id = risk_template_detail.risk_template_id "+
//          "and risk_template_detail.risk_template_id = ? "+
          "and risk_template_detail.risk_tmplt_item = risk_items.category "+
          "and risk_items.applies_to_id *= task_list.iid";

     public static String CREATE_RISK_HEADERS = "insert into risk_headers (id, wspace_id, prj_id, risk_template_id) values (?,?,?,?)";
     public static String UPDATE_RISK_MAP_ITEM = "update pj_map_items set risk_header_id = ? where wspace_id = ? and isdefault=1";
     public static String CREATE_RISK_ITEM = "insert into risk_items (id, risk_header_id, risk_id, risk_status)  (select ?, ?, count(*)+1,1 from risk_items where risk_header_id = ?)";
     public static String UPDATE_RISK_ITEM_IDENTIFY = "update risk_items set category=?, short_name=?, is_upside=?, description=?, applies_to_type=?, applies_to_id=?, time_start=?, time_end=?, risk_condition=? where risk_items.id=?";
     public static String GET_RISK_ASSESS = "select risk_items.short_name, impact_detail, prob_field1, prob_field2, prob_field3, prob_field4, prob_field5, overall_likelihood, impact_field1, impact_field2, impact_field3, impact_field4, impact_field5, overall_impact from risk_items where id = ?";
     public static String UPDATE_RISK_ITEM_ASSESS = "update risk_items set impact_detail=?, overall_likelihood=?, impact_field1=?, impact_field2=?, impact_field3=?, impact_field4=?, overall_impact=? where risk_items.id = ?";
     public static String GET_RISK_MITIGATE_HEADER = "select risk_items.short_name, overall_likelihood, impact_field1, impact_field2, impact_field3, impact_field4, overall_impact, post_prob_overall, post_impact_field1, post_impact_field2, post_impact_field3, post_impact_field4, post_impact_overall, residual_desc, mitigation_strategy, mitigation_desc from risk_items where risk_items.id = ?";
     public static String GET_RISK_ACTION_ITEMS = "select id, action, in_plan, ownership_group from risk_action_items where risk_id=? and mitigation_or_contingent=? order by id";
     public static String UPDATE_RISK_ITEM_MITIGATE = "update risk_items set mitigation_strategy=?, mitigation_desc=?, residual_desc=?, post_prob_overall=?, post_impact_field1=?, post_impact_field2=?, post_impact_field3=?, post_impact_field4=?, post_impact_overall=? where risk_items.id=?";
     public static String DELETE_RISK_ACTION_ITEMS = "delete from risk_action_items where risk_id=?";
     public static String CREATE_RISK_ACTION_ITEM = "insert into risk_action_items (id, risk_id, mitigation_or_contingent, ownership_group, in_plan, action) values (?,?,?,?,?,?)";



     public static String CREATE_CONTINGENCY_FTE = Misc.G_DO_ORACLE ?
     "insert into risk_contingency(val_scope, line_type, risk_id, for_achieving_milestone, target_market, skill_id, time_id, val_dur, val) "+
          "( "+
          "  select 5,1,risk_items.id, combo.ms, combo.tm, combo.fs, combo.tid, ceil(combo.dur* "+
          "      ( "+
          "         case when (risk_items.post_impact_field3 = 1) then ? "+
          "	   when (risk_items.post_impact_field3 = 2) then ? "+
          "	   when (risk_items.post_impact_field3 = 3) then ? "+
          "	   when (risk_items.post_impact_field3 = 4) then ? "+
          "	   else ? "+
          "         end "+
          "      ) "+
          "    )  "+ //--end of ceil
          "   ,dv* "+
          "     ( "+
          "         case when (risk_items.post_impact_field1 = 1) then ? "+
          "	   when (risk_items.post_impact_field1 = 2) then ? "+
          "	   when (risk_items.post_impact_field1 = 3) then ? "+
          "	   when (risk_items.post_impact_field1 = 4) then ? "+
          "	   else ? "+
          "         end "+
          "      )  "+ //--end of dv mult
          "  from risk_items, "+
          "     ( "+
//          "        select risk_items.id rid, fte_items.for_achieving_milestone ms, fte_items.target_market tm, fte_items.for_skill fs, getTimeId(msfin.finish_dt+1) tid, (msfin.finish_dt-msfin.start_date+1) dur, sum(data.value) dv "+
//          "        from risk_items, fte_items, data, milestones msfin "+
//          "        where risk_items.risk_header_id = ? "+
//          "        and risk_items.applies_to_type = 0 "+
//          "        and msfin.alt_date_id = ? "+
//          "        and msfin.mstn_id = fte_items.for_achieving_milestone "+
//          "        and msfin.target_market = fte_items.target_market "+
//          "        and fte_items.alt_fte_id = ? "+
//          "        and data.fte_item_id = fte_items.id "+
//          "        and (fte_items.scen_id = 1 or fte_items.scen_id is null) "+
//          "        group by "+
//          "        risk_items.id, fte_items.for_achieving_milestone, fte_items.target_market, msfin.finish_dt,  fte_items.for_skill "+
//          "        union "+
//          "        ( "+
          "        select risk_items.id rid, fte_items.for_achieving_milestone ms, fte_items.target_market tm, fte_items.for_skill fs, getTimeId(msfin.finish_dt+1) tid, (msfin.finish_dt-msfin.start_date+1) dur, sum(data.value) dv "+
          "        from risk_items, fte_items, data, milestones msfin "+
          "        where risk_items.risk_header_id = ? "+
          "        and fte_items.alt_fte_id = ? "+
          "        and applies_to_type in (0,1) "+
          "        and msfin.alt_date_id = ? "+
          "        and ((msfin.mstn_id = applies_to_id and applies_to_type = 1) or (applies_to_type = 0)) "+
          "        and msfin.mstn_id = fte_items.for_achieving_milestone "+
          "        and msfin.target_market = fte_items.target_market "+
          "        and data.fte_item_id = fte_items.id "+
          "        and (fte_items.scen_id = 1 or fte_items.scen_id is null) "+
          "        group by "+
          "        risk_items.id, fte_items.for_achieving_milestone, fte_items.target_market, msfin.finish_dt, msfin.start_date, fte_items.for_skill "+
//          "        )  "+//-- union ms date
          "        union "+
          "        ( "+
          "        select /*+ ordered */ "+
          "        risk_items.id rid, fte_items.for_achieving_milestone ms, fte_items.target_market tm, fte_items.for_skill fs, getTimeId(awi.end_date+1) tid, (awi.end_date-awi.start_date+1) dur, sum(data.value) dv "+
          "        from risk_items, alt_work_items awi, fte_items, data "+
          "        where "+
          "        risk_items.risk_header_id = ? "+
          "        and risk_items.applies_to_type = 2 "+
          "        and awi.alt_work_id = ? and awi.internal_id = risk_items.applies_to_id "+
          "        and fte_items.alt_fte_id = ? "+
          "        and fte_items.task_internal_id in "+
          " (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
          " where "+
          " ancs.id = awi.id "+  
          " )  " +		
          "        and data.fte_item_id = fte_items.id "+
          "        group by risk_items.id, fte_items.for_achieving_milestone, fte_items.target_market, fte_items.for_skill, awi.end_date, awi.start_date "+
          "        )  "+//--end of union internal
          "    ) combo "+
          "    where risk_items.id = combo.rid "+
          "    and risk_items.risk_header_id = ? "+
          "    and not(exists(select 1 from risk_contingency where risk_contingency.risk_id = risk_items.id)) "+
          ")  "
:          
          "insert into risk_contingency(val_scope, line_type, risk_id, for_achieving_milestone, target_market, skill_id, time_id, val_dur, val) "+
          "( "+
          "  select 5,1,risk_items.id, combo.ms, combo.tm, combo.fs, combo.tid, ceil(combo.dur* "+
          "      ( "+
          "         case when (risk_items.post_impact_field3 = 1) then ? "+
          "	   when (risk_items.post_impact_field3 = 2) then ? "+
          "	   when (risk_items.post_impact_field3 = 3) then ? "+
          "	   when (risk_items.post_impact_field3 = 4) then ? "+
          "	   else ? "+
          "         end "+
          "      ) "+
          "    )  "+ //--end of ceil
          "   ,dv* "+
          "     ( "+
          "         case when (risk_items.post_impact_field1 = 1) then ? "+
          "	   when (risk_items.post_impact_field1 = 2) then ? "+
          "	   when (risk_items.post_impact_field1 = 3) then ? "+
          "	   when (risk_items.post_impact_field1 = 4) then ? "+
          "	   else ? "+
          "         end "+
          "      )  "+ //--end of dv mult
          "  from risk_items, "+
          "     ( "+
//          "        select risk_items.id rid, fte_items.for_achieving_milestone ms, fte_items.target_market tm, fte_items.for_skill fs, getTimeId(msfin.finish_dt+1) tid, (msfin.finish_dt-msfin.start_date+1) dur, sum(data.value) dv "+
//          "        from risk_items, fte_items, data, milestones msfin "+
//          "        where risk_items.risk_header_id = ? "+
//          "        and risk_items.applies_to_type = 0 "+
//          "        and msfin.alt_date_id = ? "+
//          "        and msfin.mstn_id = fte_items.for_achieving_milestone "+
//          "        and msfin.target_market = fte_items.target_market "+
//          "        and fte_items.alt_fte_id = ? "+
//          "        and data.fte_item_id = fte_items.id "+
//          "        and (fte_items.scen_id = 1 or fte_items.scen_id is null) "+
//          "        group by "+
//          "        risk_items.id, fte_items.for_achieving_milestone, fte_items.target_market, msfin.finish_dt,  fte_items.for_skill "+
//          "        union "+
//          "        ( "+
          "        select risk_items.id rid, fte_items.for_achieving_milestone ms, fte_items.target_market tm, fte_items.for_skill fs, intelli.getTimeId(msfin.finish_dt+1) tid, (msfin.finish_dt-msfin.start_date+1) dur, sum(data.value) dv "+
          "        from risk_items, fte_items, data, milestones msfin "+
          "        where risk_items.risk_header_id = ? "+
          "        and fte_items.alt_fte_id = ? "+
          "        and applies_to_type in (0,1) "+
          "        and msfin.alt_date_id = ? "+
          "        and ((msfin.mstn_id = applies_to_id and applies_to_type = 1) or (applies_to_type = 0)) "+
          "        and msfin.mstn_id = fte_items.for_achieving_milestone "+
          "        and msfin.target_market = fte_items.target_market "+
          "        and data.fte_item_id = fte_items.id "+
          "        and (fte_items.scen_id = 1 or fte_items.scen_id is null) "+
          "        group by "+
          "        risk_items.id, fte_items.for_achieving_milestone, fte_items.target_market, msfin.finish_dt, msfin.start_date, fte_items.for_skill "+
//          "        )  "+//-- union ms date
          "        union "+
          "        ( "+
          "        select /*+ ordered */ "+
          "        risk_items.id rid, fte_items.for_achieving_milestone ms, fte_items.target_market tm, fte_items.for_skill fs, intelli.getTimeId(awi.end_date+1) tid, (awi.end_date-awi.start_date+1) dur, sum(data.value) dv "+
          "        from risk_items, alt_work_items awi, fte_items, data "+
          "        where "+
          "        risk_items.risk_header_id = ? "+
          "        and risk_items.applies_to_type = 2 "+
          "        and awi.alt_work_id = ? and awi.internal_id = risk_items.applies_to_id "+
          "        and fte_items.alt_fte_id = ? "+
          "        and fte_items.task_internal_id in "+
          " (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
          " where "+
          " ancs.id = awi.id "+
    
          " )  " +		            
          "        and data.fte_item_id = fte_items.id "+
          "        group by risk_items.id, fte_items.for_achieving_milestone, fte_items.target_market, fte_items.for_skill, awi.end_date, awi.start_date "+
          "        )  "+//--end of union internal
          "    ) combo "+
          "    where risk_items.id = combo.rid "+
          "    and risk_items.risk_header_id = ? "+
          "    and not(exists(select 1 from risk_contingency where risk_contingency.risk_id = risk_items.id)) "+
          ")  ";//-- end of select query

     public static String CREATE_CONTINGENCY_COST = Misc.G_DO_ORACLE ?
     "insert into risk_contingency(val_scope, line_type, risk_id, for_achieving_milestone, target_market, skill_id, time_id, val_dur, val) "+
          "( "+
          "  select 5,2,risk_items.id, combo.ms, combo.tm, combo.fs, combo.tid, ceil(combo.dur* "+
          "      ( "+
          "         case when (risk_items.post_impact_field3 = 1) then ? "+
          "	   when (risk_items.post_impact_field3 = 2) then ? "+
          "	   when (risk_items.post_impact_field3 = 3) then ? "+
          "	   when (risk_items.post_impact_field3 = 4) then ? "+
          "	   else ? "+
          "         end "+
          "      ) "+
          "    )  "+//--end of ceil
          "   ,dv* "+
          "     ( "+
          "         case when (risk_items.post_impact_field2 = 1) then ? "+
          "	   when (risk_items.post_impact_field2 = 2) then ? "+
          "	   when (risk_items.post_impact_field2 = 3) then ? "+
          "	   when (risk_items.post_impact_field2 = 4) then ? "+
          "	   else ? "+
          "         end "+
          "      ) "+//--end of dv mult
          "  from risk_items, "+
          "     ( "+
//          "        select risk_items.id rid, cost_items.for_achieving_milestone ms, cost_items.target_market tm, cost_items.cost_cent_id fs, getTimeId(msfin.finish_dt+1) tid, (msfin.finish_dt-msfin.start_date+1) dur, sum(data.value) dv "+
//          "        from risk_items, cost_items, data, milestones msfin "+
//          "        where risk_items.risk_header_id = ? "+
//          "        and risk_items.applies_to_type = 0 "+
//          "        and msfin.alt_date_id = ? "+
//          "        and msfin.mstn_id = fte_items.for_achieving_milestone "+
//          "        and msfin.target_market = ? "+
//          "        and msstart.mstn_id = ? "+
//          "        and msstart.target_market = msfin.target_market "+
//          "        and cost_items.alt_devcost_id = ? "+
//          "        and data.cost_li_id = cost_items.id "+
//          "        and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
//          "        group by "+
//          "        risk_items.id, cost_items.for_achieving_milestone, cost_items.target_market, msfin.finish_dt, msstart.start_date, cost_items.cost_cent_id "+
//          "        union "+
//          "        ( "+
          "        select risk_items.id rid, cost_items.for_achieving_milestone ms, cost_items.target_market tm, cost_items.cost_cent_id fs, getTimeId(msfin.finish_dt+1) tid, (msfin.finish_dt-msfin.start_date+1) dur, sum(data.value) dv "+
          "        from risk_items, cost_items, data, milestones msfin "+
          "        where risk_items.risk_header_id = ? "+
          "        and cost_items.alt_devcost_id = ? "+
          "        and applies_to_type in (0,1) "+
          "        and msfin.alt_date_id = ? "+
          "        and ((msfin.mstn_id = applies_to_id and applies_to_type = 1) or (applies_to_type = 0)) "+
          "        and msfin.mstn_id = cost_items.for_achieving_milestone "+
          "        and msfin.target_market = cost_items.target_market "+
          "        and data.cost_li_id = cost_items.id "+
          "        and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
          "        group by "+
          "        risk_items.id, cost_items.for_achieving_milestone, cost_items.target_market, msfin.finish_dt, msfin.start_date, cost_items.cost_cent_id "+
//          "        ) "+//-- union ms date
          "        union "+
          "        ( "+
          "        select /*+ ordered */ "+
          "        risk_items.id rid, cost_items.for_achieving_milestone ms, cost_items.target_market tm, cost_items.cost_cent_id fs, getTimeId(awi.end_date+1) tid, (awi.end_date-awi.start_date+1) dur, sum(data.value) dv "+
          "        from risk_items, alt_work_items awi, cost_items, data "+
          "        where "+
          "        risk_items.risk_header_id = ? "+
          "        and risk_items.applies_to_type = 2 "+
          "        and awi.alt_work_id = ? and awi.internal_id = risk_items.applies_to_id "+
          "        and cost_items.alt_devcost_id = ? "+
          "        and cost_items.task_internal_id in "+
          " (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
          " where "+
          " ancs.id = awi.id "+
    
          " )  " +		            
          "        group by risk_items.id, cost_items.for_achieving_milestone, cost_items.target_market, cost_items.cost_cent_id, awi.end_date, awi.start_date "+
          "        )  "+//--end of union internal
          "    ) combo "+
          "    where risk_items.id = combo.rid "+
          "    and risk_items.risk_header_id = ? "+
          "    and not(exists(select 1 from risk_contingency where risk_contingency.risk_id = risk_items.id)) "+
          ") "
:          
          "insert into risk_contingency(val_scope, line_type, risk_id, for_achieving_milestone, target_market, skill_id, time_id, val_dur, val) "+
          "( "+
          "  select 5,2,risk_items.id, combo.ms, combo.tm, combo.fs, combo.tid, ceil(combo.dur* "+
          "      ( "+
          "         case when (risk_items.post_impact_field3 = 1) then ? "+
          "	   when (risk_items.post_impact_field3 = 2) then ? "+
          "	   when (risk_items.post_impact_field3 = 3) then ? "+
          "	   when (risk_items.post_impact_field3 = 4) then ? "+
          "	   else ? "+
          "         end "+
          "      ) "+
          "    )  "+//--end of ceil
          "   ,dv* "+
          "     ( "+
          "         case when (risk_items.post_impact_field2 = 1) then ? "+
          "	   when (risk_items.post_impact_field2 = 2) then ? "+
          "	   when (risk_items.post_impact_field2 = 3) then ? "+
          "	   when (risk_items.post_impact_field2 = 4) then ? "+
          "	   else ? "+
          "         end "+
          "      ) "+//--end of dv mult
          "  from risk_items, "+
          "     ( "+
//          "        select risk_items.id rid, cost_items.for_achieving_milestone ms, cost_items.target_market tm, cost_items.cost_cent_id fs, getTimeId(msfin.finish_dt+1) tid, (msfin.finish_dt-msfin.start_date+1) dur, sum(data.value) dv "+
//          "        from risk_items, cost_items, data, milestones msfin "+
//          "        where risk_items.risk_header_id = ? "+
//          "        and risk_items.applies_to_type = 0 "+
//          "        and msfin.alt_date_id = ? "+
//          "        and msfin.mstn_id = fte_items.for_achieving_milestone "+
//          "        and msfin.target_market = ? "+
//          "        and msstart.mstn_id = ? "+
//          "        and msstart.target_market = msfin.target_market "+
//          "        and cost_items.alt_devcost_id = ? "+
//          "        and data.cost_li_id = cost_items.id "+
//          "        and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
//          "        group by "+
//          "        risk_items.id, cost_items.for_achieving_milestone, cost_items.target_market, msfin.finish_dt, msstart.start_date, cost_items.cost_cent_id "+
//          "        union "+
//          "        ( "+
          "        select risk_items.id rid, cost_items.for_achieving_milestone ms, cost_items.target_market tm, cost_items.cost_cent_id fs, intelli.getTimeId(msfin.finish_dt+1) tid, (msfin.finish_dt-msfin.start_date+1) dur, sum(data.value) dv "+
          "        from risk_items, cost_items, data, milestones msfin "+
          "        where risk_items.risk_header_id = ? "+
          "        and cost_items.alt_devcost_id = ? "+
          "        and applies_to_type in (0,1) "+
          "        and msfin.alt_date_id = ? "+
          "        and ((msfin.mstn_id = applies_to_id and applies_to_type = 1) or (applies_to_type = 0)) "+
          "        and msfin.mstn_id = cost_items.for_achieving_milestone "+
          "        and msfin.target_market = cost_items.target_market "+
          "        and data.cost_li_id = cost_items.id "+
          "        and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
          "        group by "+
          "        risk_items.id, cost_items.for_achieving_milestone, cost_items.target_market, msfin.finish_dt, msfin.start_date, cost_items.cost_cent_id "+
//          "        ) "+//-- union ms date
          "        union "+
          "        ( "+
          "        select /*+ ordered */ "+
          "        risk_items.id rid, cost_items.for_achieving_milestone ms, cost_items.target_market tm, cost_items.cost_cent_id fs, intelli.getTimeId(awi.end_date+1) tid, (awi.end_date-awi.start_date+1) dur, sum(data.value) dv "+
          "        from risk_items, alt_work_items awi, cost_items, data "+
          "        where "+
          "        risk_items.risk_header_id = ? "+
          "        and risk_items.applies_to_type = 2 "+
          "        and awi.alt_work_id = ? and awi.internal_id = risk_items.applies_to_id "+
          "        and cost_items.alt_devcost_id = ? "+
          "        and cost_items.task_internal_id in "+
          " (select children.internal_id from " +
    "	 alt_work_items ancs join alt_work_items children on (children.alt_work_id = ancs.alt_work_id and children.lhs_number >= ancs.lhs_number and children.rhs_number <= ancs.rhs_number )  " +    
          " where "+
          " ancs.id = awi.id "+
    
          " )  " +		            
          "        group by risk_items.id, cost_items.for_achieving_milestone, cost_items.target_market, cost_items.cost_cent_id, awi.end_date, awi.start_date "+
          "        )  "+//--end of union internal
          "    ) combo "+
          "    where risk_items.id = combo.rid "+
          "    and risk_items.risk_header_id = ? "+
          "    and not(exists(select 1 from risk_contingency where risk_contingency.risk_id = risk_items.id)) "+
          ") ";

     public static String DELETE_RISK_CONTINGENCY = "delete from risk_contingency where (risk_id = ?) or (? is null and risk_id in (select id from risk_items where risk_header_id = ?))";

     public static String GET_CONTINGENT_DATA = Misc.G_DO_ORACLE ?
     "select "+
          " for_achieving_milestone, skill_id, qtr_timeid.time_id, sum(val * intelli.getPropIncluded(val_scope, risk_contingency.time_id, qtr_timeid.time_id, 0, val_dur) "+
          "  * (case when ?=1 then 1 else (case when risk_items.post_prob_overall = 1 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     else ? "+
          "                                end) "+
          "     end) "+
          "   ) * (?) "+
          " from risk_items, risk_contingency, qtr_timeid "+
          " where risk_items.risk_header_id = ? "+
          " and risk_contingency.risk_id = risk_items.id "+
          " and qtr_timeid.time_id >= ? "+
          " and qtr_timeid.time_id <= ? "+
          " and risk_contingency.line_type = ? "+
          " and qtr_timeid.time_id >= trunc(risk_contingency.time_id/105)*105 "+
          " and qtr_timeid.time_id < risk_contingency.time_id+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
          " group by for_achieving_milestone, skill_id, qtr_timeid.time_id "+
          " order by for_achieving_milestone, skill_id, qtr_timeid.time_id "
:          
     "select "+
          " for_achieving_milestone, skill_id, qtr_timeid.time_id, sum(val * intelli.getPropIncluded(val_scope, risk_contingency.time_id, qtr_timeid.time_id, 0, val_dur) "+
          "  * (case when ?=1 then 1 else (case when risk_items.post_prob_overall = 1 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     else ? "+
          "                                end) "+
          "     end) "+
          "   ) * (?) "+
          " from risk_items, risk_contingency, qtr_timeid "+
          " where risk_items.risk_header_id = ? "+
          " and risk_contingency.risk_id = risk_items.id "+
          " and qtr_timeid.time_id >= ? "+
          " and qtr_timeid.time_id <= ? "+
          " and risk_contingency.line_type = ? "+
          " and qtr_timeid.time_id >= cast((risk_contingency.time_id/105) as int)*105 "+
          " and qtr_timeid.time_id < risk_contingency.time_id+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
          " group by for_achieving_milestone, skill_id, qtr_timeid.time_id "+
          " order by for_achieving_milestone, skill_id, qtr_timeid.time_id ";

     public static String GET_CONTINGENT_DUR = "select "+
          " ms, sum(d "+
          "  * (case when ?=1 then 1 else (case when risk_items.post_prob_overall = 1 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     else ? "+
          "                                end) "+
          "     end) "+
          "   ) * (?) "+
          " from risk_items, (select distinct risk_contingency.risk_id rid, risk_contingency.val_dur d, risk_contingency.for_achieving_milestone ms from risk_contingency) rc "+
          " where risk_items.risk_header_id = ? "+
          " and rc.rid = risk_items.id "+
          " group by ms "+
          " order by ms ";

     public static String GET_RISK_MONITOR = "select risk_items.short_name, getRiskFunction(risk_items.id, risk_items.risk_status, risk_items.time_start, risk_items.time_end, ?) rst from risk_items where risk_items.id = ?";
     public static String GET_RISK_MONITOR_ITEMS = "select name, curr_val, threshold, must_exceed_thresh, date_to_check from risk_monitor where risk_id = ? order by name";
     public static String UPDATE_RISK_ITEM_MONITOR = "update risk_items set risk_status=? where id = ?";
     public static String GET_RISK_MONITOR_NOTES = "select notes, on_date, users.username from risk_notes, users where risk_notes.risk_id = ? and users.id = risk_notes.by_user order by on_date desc";
     public static String DELETE_RISK_MONITOR_ITEMS = "delete from risk_monitor where risk_id = ?";
     public static String CREATE_RISK_MONITOR_ITEM = "insert into risk_monitor(risk_id, name, curr_val, threshold, date_to_check, must_exceed_thresh) values (?,?,?,?,?,?)";
     public static String CREATE_RISK_MONITOR_NOTE = Misc.G_DO_ORACLE ?
     "insert into risk_notes(risk_id, notes, on_date, by_user) values (?,?,sysdate,?)"
     :
     "insert into risk_notes(risk_id, notes, on_date, by_user) values (?,?,getdate(),?)";

     public static String GET_CONTINGENT_DATA_ANNUAL = Misc.G_DO_ORACLE ? 
      "select "+
          " for_achieving_milestone, skill_id, year_timeid.time_id, sum(val * intelli.getPropIncluded(val_scope, risk_contingency.time_id, year_timeid.time_id, 1, val_dur) "+
          "  * (case when ?=1 then 1 else (case when risk_items.post_prob_overall = 1 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     else ? "+
          "                                end) "+
          "     end) "+
          "   ) * (?) "+
          " from risk_items, risk_contingency, year_timeid "+
          " where risk_items.risk_header_id = ? "+
          " and risk_contingency.risk_id = risk_items.id "+
          " and year_timeid.time_id >= ? "+
          " and year_timeid.time_id <= ? "+
          " and risk_contingency.line_type = ? "+
          " and year_timeid.time_id >= trunc(risk_contingency.time_id/420)*420 "+
          " and year_timeid.time_id < risk_contingency.time_id+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
          " group by for_achieving_milestone, skill_id, year_timeid.time_id "+
          " order by for_achieving_milestone, skill_id, year_timeid.time_id "
:          
     "select "+
          " for_achieving_milestone, skill_id, year_timeid.time_id, sum(val * intelli.getPropIncluded(val_scope, risk_contingency.time_id, year_timeid.time_id, 1, val_dur) "+
          "  * (case when ?=1 then 1 else (case when risk_items.post_prob_overall = 1 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     else ? "+
          "                                end) "+
          "     end) "+
          "   ) * (?) "+
          " from risk_items, risk_contingency, year_timeid "+
          " where risk_items.risk_header_id = ? "+
          " and risk_contingency.risk_id = risk_items.id "+
          " and year_timeid.time_id >= ? "+
          " and year_timeid.time_id <= ? "+
          " and risk_contingency.line_type = ? "+
          " and year_timeid.time_id >= cast((risk_contingency.time_id/420) as int)*420 "+
          " and year_timeid.time_id < risk_contingency.time_id+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
          " group by for_achieving_milestone, skill_id, year_timeid.time_id "+
          " order by for_achieving_milestone, skill_id, year_timeid.time_id ";
     //this has the same columns as GET_CONTINGENT_DATA_ANNUAL
     public static String GET_CONTINGENT_DATA_TOTAL = "select "+
          " for_achieving_milestone, skill_id, 1, sum(val "+
          "  * (case when ?=1 then 1 else (case when risk_items.post_prob_overall = 1 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     when risk_items.post_prob_overall = 2 then ? "+
          "                                     else ? "+
          "                                end) "+
          "     end) "+
          "   ) * (?) "+
          " from risk_items, risk_contingency "+
          " where risk_items.risk_header_id = ? "+
          " and risk_contingency.risk_id = risk_items.id "+
          " and risk_contingency.line_type = ? "+
          " group by for_achieving_milestone, skill_id "+
          " order by for_achieving_milestone, skill_id ";

     public static String GET_MEASURE_MAP_ITEMS_SPECIFIC_CONTINGENT = "select measure_id, alt_measure_id from measure_map_items where alt_id = ? and wspace_id = ? and isdefault=1 and measure_id in (212,213,214,215,216,217) order by measure_id";
     public static String GET_MEASURE_MAP_ITEMS_SPECIFIC_CONTINGENT_LABEL = "select measure_id, alt_measure_id from measure_map_items where alt_id = ? and label_id = ? and isdefault=1 and measure_id in (212,213,214,215,216,217) order by measure_id";

     public static String GET_MEASURE_DATA_CONTINGENT_ANNUAL = Misc.G_DO_ORACLE ?
     
		"select outcome_or_phase_id, break_down, time_id, sum(measure_data.val * intelli.getPropIncluded(val_scope, time_val, time_id, 1, val_dur)) " +
		"from measure_data, measure_case_index, year_timeid " +
		"where measure_data.alt_measure_id = ? " +
		"and measure_data.measure_case_index_id = measure_case_index.id " +
          "and (measure_case_index.scen_id = 1 or measure_case_index.scen_id is null) "+
          "and time_id >= ? "+
          "and time_id <= ? "+
          " and time_id >= trunc(time_val/420)*420 "+
          " and time_id < time_val+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
		"group by break_down, outcome_or_phase, time_id "+
		"order by break_down, outcome_or_phase, time_id"
:    
		"select outcome_or_phase_id, break_down, time_id, sum(measure_data.val * intelli.getPropIncluded(val_scope, time_val, time_id, 1, val_dur)) " +
		"from measure_data, measure_case_index, year_timeid " +
		"where measure_data.alt_measure_id = ? " +
		"and measure_data.measure_case_index_id = measure_case_index.id " +
          "and (measure_case_index.scen_id = 1 or measure_case_index.scen_id is null) "+
          "and time_id >= ? "+
          "and time_id <= ? "+
          " and time_id >= cast((time_val/420) as int)*420 "+
          " and time_id < time_val+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
		"group by break_down, outcome_or_phase, time_id "+
		"order by break_down, outcome_or_phase, time_id";

     public static String GET_MEASURE_DATA_CONTINGENT_TOT =
		"select outcome_or_phase_id, break_down, sum(measure_data.val) " +
		"from measure_data, measure_case_index" +
		"where measure_data.alt_measure_id = ? " +
		"and measure_data.measure_case_index_id = measure_case_index.id " +
          "and (measure_case_index.scen_id = 1 or measure_case_index.scen_id is null) "+
		"group by break_down, outcome_or_phase "+
		"order by break_down, outcome_or_phase";

     public static String GET_FTE_DATA_CONTINGENT_ANNUAL = Misc.G_DO_ORACLE ?
     "select /*+ ordered */ for_achieving_milestone, for_skill, time_id, sum(data.value * intelli.getPropIncluded(val_scope, year, time_id, 1, val_dur)) " +
		"from fte_items, data, year_timeid " +
		"where fte_items.alt_fte_id = ? " +
          "and (fte_items.scen_id = 1 or fte_items.scen_id is null) "+
		"and data.fte_item_id = fte_items.id "+
          "and time_id >= ? "+
          "and time_id <= ? "+
          " and time_id >= trunc(year/420)*420 "+
          " and time_id < year+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
		"group by for_skill, for_achieving_milestone, time_id "+
		"order by for_skill, for_achieving_milestone, time_id "
:    
		"select /*+ ordered */ for_achieving_milestone, for_skill, time_id, sum(data.value * intelli.getPropIncluded(val_scope, year, time_id, 1, val_dur)) " +
		"from fte_items, data, year_timeid " +
		"where fte_items.alt_fte_id = ? " +
          "and (fte_items.scen_id = 1 or fte_items.scen_id is null) "+
		"and data.fte_item_id = fte_items.id "+
          "and time_id >= ? "+
          "and time_id <= ? "+
          " and time_id >= cast((year/420) as int)*420 "+
          " and time_id < year+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
		"group by for_skill, for_achieving_milestone, time_id "+
		"order by for_skill, for_achieving_milestone, time_id ";
     public static String GET_FTE_DATA_CONTINGENT_TOT =
		"select /*+ ordered */ for_achieving_milestone, for_skill,  sum(data.value) " +
		"from fte_items, data " +
		"where fte_items.alt_fte_id = ? " +
          "and (fte_items.scen_id = 1 or fte_items.scen_id is null) "+
		"and data.fte_item_id = fte_items.id "+
		"group by for_skill, for_achieving_milestone "+
		"order by for_skill, for_achieving_milestone ";


     public static String GET_COST_DATA_CONTINGENT_ANNUAL = Misc.G_DO_ORACLE ?
     "select /*+ ordered */ for_achieving_milestone, cost_cent_id, time_id, sum(data.value * intelli.getPropIncluded(val_scope, year, time_id, 1, val_dur)) " +
		"from cost_items, data, year_timeid " +
		"where cost_items.alt_devcost_id = ? " +
          "and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
		"and data.cost_li_id = cost_items.id "+
          "and time_id >= ? "+
          "and time_id <= ? "+
          " and time_id >= trunc(year/420)*420 "+
          " and time_id < year+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
		"group by cost_cent_id, for_achieving_milestone, time_id "+
		"order by cost_cent_id, for_achieving_milestone, time_id "
    :
		"select /*+ ordered */ for_achieving_milestone, cost_cent_id, time_id, sum(data.value * intelli.getPropIncluded(val_scope, year, time_id, 1, val_dur)) " +
		"from cost_items, data, year_timeid " +
		"where cost_items.alt_devcost_id = ? " +
          "and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
		"and data.cost_li_id = cost_items.id "+
          "and time_id >= ? "+
          "and time_id <= ? "+
          " and time_id >= cast((year/420) as int)*420 "+
          " and time_id < year+(case when (val_scope = 0) then 105 "+
          "     when (val_scope=1) then 420 "+
          "     when (val_scope=2) then 35 "+
          "     when (val_scope=3) then 12 "+
          "     when (val_scope=5) then val_dur "+
          "     else 1 "+
          "     end) "+
		"group by cost_cent_id, for_achieving_milestone, time_id "+
		"order by cost_cent_id, for_achieving_milestone, time_id ";

     public static String GET_COST_DATA_CONTINGENT_TOT =
		"select /*+ ordered */ for_achieving_milestone, cost_cent_id,  sum(data.value) " +
		"from cost_items, data " +
		"where cost_items.alt_devcost_id = ? " +
          "and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
		"and data.cost_li_id = cost_items.id "+
		"group by cost_cent_id, for_achieving_milestone "+
		"order by cost_cent_id, for_achieving_milestone ";

//fte, cost, rev, measure_items, dates,
     public static String CONTRACT_GET_COST_DATA_TOT = Misc.G_DO_ORACLE ?
     
		"select /*+ ordered */ workspaces.classify1, alt_map_items.map_type, sum(data.value) " +
		"from alt_map_items, workspaces, cost_items, data " +
          "where alt_map_items.alt_id = ? and isdefault = 1 "+
          "and ((alt_map_items.map_type = 1) or (alt_map_items.wspace_id = workspaces.id and workspaces.classify1 is not null)) "+
          "and cost_items.alt_devcost_id = alt_map_items.alt_devcost_id "+
          "and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
		"and data.cost_li_id = cost_items.id "+
          "and (? is null or cost_items.for_achieving_milestone  = ?) "+
		"group by alt_map_items.map_type, workspaces.classify1 "+
		"order by alt_map_items.map_type, workspaces.classify1 nulls first "
:    
		"select /*+ ordered */ workspaces.classify1, alt_map_items.map_type, sum(data.value) " +
		"from alt_map_items, workspaces, cost_items, data " +
          "where alt_map_items.alt_id = ? and isdefault = 1 "+
          "and ((alt_map_items.map_type = 1) or (alt_map_items.wspace_id = workspaces.id and workspaces.classify1 is not null)) "+
          "and cost_items.alt_devcost_id = alt_map_items.alt_devcost_id "+
          "and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
		"and data.cost_li_id = cost_items.id "+
          "and (? is null or cost_items.for_achieving_milestone  = ?) "+
		"group by alt_map_items.map_type, workspaces.classify1 "+
		"order by alt_map_items.map_type, workspaces.classify1 asc ";

     public static String CONTRACT_GET_FTE_DATA_TOT = Misc.G_DO_ORACLE ?
     
		"select /*+ ordered */ workspaces.classify1, alt_map_items.map_type, sum(data.value) " +
		"from alt_map_items, workspaces, fte_items, data " +
          "where alt_map_items.alt_id = ? and isdefault = 1 "+
          "and ((alt_map_items.map_type = 1) or (alt_map_items.wspace_id = workspaces.id and workspaces.classify1 is not null)) "+
          "and fte_items.alt_fte_id = alt_map_items.alt_fte_id "+
          "and (fte_items.scen_id = 1 or fte_items.scen_id is null) "+
		"and data.fte_item_id = fte_items.id "+
          "and (? is null or fte_items.for_achieving_milestone  = ?) "+
		"group by alt_map_items.map_type, workspaces.classify1 "+
		"order by alt_map_items.map_type, workspaces.classify1 nulls first "
:    
		"select /*+ ordered */ workspaces.classify1, alt_map_items.map_type, sum(data.value) " +
		"from alt_map_items, workspaces, fte_items, data " +
          "where alt_map_items.alt_id = ? and isdefault = 1 "+
          "and ((alt_map_items.map_type = 1) or (alt_map_items.wspace_id = workspaces.id and workspaces.classify1 is not null)) "+
          "and fte_items.alt_fte_id = alt_map_items.alt_fte_id "+
          "and (fte_items.scen_id = 1 or fte_items.scen_id is null) "+
		"and data.fte_item_id = fte_items.id "+
          "and (? is null or fte_items.for_achieving_milestone  = ?) "+
		"group by alt_map_items.map_type, workspaces.classify1 "+
		"order by alt_map_items.map_type, workspaces.classify1 asc ";

     public static String CONTRACT_GET_MEASURE_DATA_TOT = Misc.G_DO_ORACLE ?
     
         "select /*+ ordered */ workspaces.classify1, measure_map_items.map_type, sum(measure_data.val), avg(measure_data.val) " +
		"from measure_map_items, workspaces, measure_case_index, measure_data " +
          "where measure_map_items.alt_id = ? and isdefault = 1 and measure_map_items.measure_id =? "+
          "and ((measure_map_items.map_type = 1) or (measure_map_items.wspace_id = workspaces.id and workspaces.classify1 is not null)) "+
          "and (measure_case_index.measure_id = measure_map_items.measure_id) "+
          "and (? is null or measure_case_index.outcome_or_phase_id is null or measure_case_index.outcome_or_phase_id = ?) "+
          "and (? is null or measure_case_index.break_down is null or measure_case_index.break_down = ?) "+          
          "and (measure_case_index.id = measure_data.measure_case_index_id) "+
		"and measure_data.alt_measure_id = measure_map_items.alt_measure_id "+
		"and measure_data.time_val >= ? " +
		"and measure_data.time_val < ? "+

		"group by measure_map_items.map_type, workspaces.classify1 "+
		"order by measure_map_items.map_type, workspaces.classify1 nulls first "
:    
         "select /*+ ordered */ workspaces.classify1, measure_map_items.map_type, sum(measure_data.val), avg(measure_data.val) " +
		"from measure_map_items, workspaces, measure_case_index, measure_data " +
          "where measure_map_items.alt_id = ? and isdefault = 1 and measure_map_items.measure_id =? "+
          "and ((measure_map_items.map_type = 1) or (measure_map_items.wspace_id = workspaces.id and workspaces.classify1 is not null)) "+
          "and (measure_case_index.measure_id = measure_map_items.measure_id) "+
          "and (? is null or measure_case_index.outcome_or_phase_id is null or measure_case_index.outcome_or_phase_id = ?) "+
          "and (? is null or measure_case_index.break_down is null or measure_case_index.break_down = ?) "+          
          "and (measure_case_index.id = measure_data.measure_case_index_id) "+
		"and measure_data.alt_measure_id = measure_map_items.alt_measure_id "+
		"and measure_data.time_val >= ? " +
		"and measure_data.time_val < ? "+

		"group by measure_map_items.map_type, workspaces.classify1 "+
		"order by measure_map_items.map_type, workspaces.classify1 asc ";


     public static String CONTRACT_GET_OPCOST_DATA_TOT = Misc.G_DO_ORACLE ?
     
		"select /*+ ordered */ workspaces.classify1, alt_map_items.map_type, sum(data.value) " +
		"from alt_map_items, workspaces, cost_items, data " +
          "where alt_map_items.alt_id = ? and isdefault = 1 "+
          "and ((alt_map_items.map_type = 1) or (alt_map_items.wspace_id = workspaces.id and workspaces.classify1 is not null)) "+
          "and cost_items.alt_opcost_id = alt_map_items.alt_opcost_id "+
          "and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
		"and data.cost_li_id = cost_items.id "+
          "and (? is null or cost_items.cost_cent_id  = ?) "+
		"group by alt_map_items.map_type, workspaces.classify1 "+
		"order by alt_map_items.map_type, workspaces.classify1 nulls first "
:    
		"select /*+ ordered */ workspaces.classify1, alt_map_items.map_type, sum(data.value) " +
		"from alt_map_items, workspaces, cost_items, data " +
          "where alt_map_items.alt_id = ? and isdefault = 1 "+
          "and ((alt_map_items.map_type = 1) or (alt_map_items.wspace_id = workspaces.id and workspaces.classify1 is not null)) "+
          "and cost_items.alt_opcost_id = alt_map_items.alt_opcost_id "+
          "and (cost_items.scen_id = 1 or cost_items.scen_id is null) "+
		"and data.cost_li_id = cost_items.id "+
          "and (? is null or cost_items.cost_cent_id  = ?) "+
		"group by alt_map_items.map_type, workspaces.classify1 "+
		"order by alt_map_items.map_type, workspaces.classify1 asc ";

        public static final String GET_ALT_MEASURE_FILE_INFO = Misc.G_DO_ORACLE ?
        "select distinct ALT_MODEL_ID_EXCEL_LOAD, 1, model.extension, '', model.name, '', alt_measures.id from alt_measures, measure_map_items, file_names model where measure_map_items.wspace_id = ? and measure_map_items.alt_id = ? and measure_map_items.isdefault = 1 and measure_map_items.measure_id = ? and measure_map_items.alt_measure_id = alt_measures.id and ALT_MODEL_ID_EXCEL_LOAD=model.file_name_id(+) "
        :        
        "select distinct ALT_MODEL_ID_EXCEL_LOAD, 1, model.extension, '', model.name, '', alt_measures.id from alt_measures, measure_map_items, file_names model where measure_map_items.wspace_id = ? and measure_map_items.alt_id = ? and measure_map_items.isdefault = 1 and measure_map_items.measure_id = ? and measure_map_items.alt_measure_id = alt_measures.id and ALT_MODEL_ID_EXCEL_LOAD *= model.file_name_id ";

        public static final String GET_PROD_FAMILY =
           "select fp.id, fpa.id, fpw.id from projects fp, alternatives fpa, workspaces fpw, pj_map_items fpm "+
           "  where fp.id in (select pj_basics.int_field6 from  pj_basics, pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault = 1 and pj_map_items.pj_basic_id = pj_basics.id) "+
           "  and fpa.prj_id = fp.id "+
           "  and fpa.is_primary = 1 "+
           "  and fpw.prj_id = fp.id "+
           "  and fpm.prj_id = fp.id "+
           "  and (map_type = 1 or map_type = 4) and (isdefault=1) order by map_type asc, fpm.date_created desc ";

	public static String GET_DATA_BY_YEAR_REV_WIZ = //doesn't work for custom dur or off-cycle data.year 
  Misc.G_DO_ORACLE ?
  	"select time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"mkt_type, classify2 "+ //classify2 == prod cat
		"from data, rev_segs, year_timeid  "+
		"where rev_segs.alt_rev_id = ? "+
		"and data.rev_seg_id = rev_segs.id "+
          "and classify1 in (0,1) "+
		" and time_id >= trunc(data.year/420)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

		"group by classify2, mkt_type, time_id "+
		"order by mkt_type, classify2, time_id "
:    
		"select time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"mkt_type, classify2 "+ //classify2 == prod cat
		"from data, rev_segs, year_timeid  "+
		"where rev_segs.alt_rev_id = ? "+
		"and data.rev_seg_id = rev_segs.id "+
          "and classify1 in (0,1) "+
		" and time_id >= cast((data.year/420) as int)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+

		"group by classify2, mkt_type, time_id "+
		"order by mkt_type, classify2, time_id ";

	public static String GET_DATA_BY_YEAR_OPCOST_WIZ = //doesn't work for custom dur or off-cycle data.year
  Misc.G_DO_ORACLE ?
  	"select  time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"target_market,  classify1,cost_items.cost_cent_id  "+ //prod cat
		"from data, cost_items, year_timeid  "+
		"where cost_items.alt_opcost_id = ? "+
		"and data.cost_li_id = cost_items.id "+
		" and time_id >= trunc(data.year/420)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by target_market, cost_items.cost_cent_id , classify1,time_id  "+
		"order by target_market, cost_items.cost_cent_id , classify1,time_id "
:    
		"select  time_id, " +
		"sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)), " +
		"target_market,  classify1,cost_items.cost_cent_id  "+ //prod cat
		"from data, cost_items, year_timeid  "+
		"where cost_items.alt_opcost_id = ? "+
		"and data.cost_li_id = cost_items.id "+
		" and time_id >= cast((data.year/420) as int)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		"group by target_market, cost_items.cost_cent_id , classify1,time_id  "+
		"order by target_market, cost_items.cost_cent_id , classify1,time_id ";
	public static String GET_MEASURE_DATA_BY_YEAR_WIZ_MARKET = 
//sum(data.value * getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur))
//		"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
Misc.G_DO_ORACLE ?
    "select time_id, sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 1, measure_data.val_dur)), target_market, classify1 "+
		"from measure_data, measure_case_index, year_timeid "+
		"where measure_data.alt_measure_id = ? "+
		"and measure_data.measure_case_index_id = measure_case_index.id "+
		"and measure_data.time_val >= time_id and measure_data.time_val < time_id+35*12 "+
		"group by target_market, classify1, time_id "+ //the product cat
		"order by target_market, classify1, time_id "
:    
		"select time_id, sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 1, measure_data.val_dur)), target_market, classify1 "+
		"from measure_data, measure_case_index, year_timeid "+
		"where measure_data.alt_measure_id = ? "+
		"and measure_data.measure_case_index_id = measure_case_index.id "+
		"and measure_data.time_val >= time_id and measure_data.time_val < time_id+35*12 "+
		"group by target_market, classify1, time_id "+ //the product cat
		"order by target_market, classify1, time_id ";

	public static String GET_MEASURE_DATA_BY_YEAR_WIZ_UNIT = Misc.G_DO_ORACLE ?
  "select time_id, sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 1, measure_data.val_dur)), target_market, classify2 "+
		"from measure_data, measure_case_index, year_timeid "+
		"where measure_data.alt_measure_id = ? "+
		"and measure_data.measure_case_index_id = measure_case_index.id "+
          "and classify1 in (0,1) "+
		"and measure_data.time_val >= time_id and measure_data.time_val < time_id+35*12 "+
		"group by target_market, classify2, time_id "+ //the product cat
		"order by target_market, classify2, time_id "
:    
//sum(data.value * getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur))
//		"select measure_case_index.id, measure_case_index.name, time_id, sum(measure_data.val), target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, scen_id "+
		"select time_id, sum(measure_data.val * intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 1, measure_data.val_dur)), target_market, classify2 "+
		"from measure_data, measure_case_index, year_timeid "+
		"where measure_data.alt_measure_id = ? "+
		"and measure_data.measure_case_index_id = measure_case_index.id "+
          "and classify1 in (0,1) "+
		"and measure_data.time_val >= time_id and measure_data.time_val < time_id+35*12 "+
		"group by target_market, classify2, time_id "+ //the product cat
		"order by target_market, classify2, time_id ";

     public static String GET_MULT_PRJ_ITEMS_0 =
     "select "+
"alt_work_items.id, parent_internal_id parent, alt_work_items.start_date, alt_work_items.end_date, complete "+
",actual_start, actual_end "+
",internal_id, ref_id, isConstant, is_split "+
",alt_work_items.status, alt_work_items.type "+
",alt_work_items.alt_work_id "+
",alt_work_items.name "+
",pred,alt_work_items.for_achieving_ms "+
",alt_work_items.classify1 "+
",alt_work_items.classify2 "+
",alt_work_items.classify3 "+
",alt_work_items.classify4 "+
",alt_work_items.classify5 "+
",rollup_policy,min_dur, max_dur, p10_dur, p90_dur, distrib,interruptible "+
",cc, ismilestone, variancecode, user_comment "+
",alt_work_items.to_include "+
",rollup_cost, notes, alt_work_items.target_market "+
",alt_works.alt_id ";
     public static String GET_MULT_PRJ_ITEMS_1 =
"from "+
"alt_work_items "+
", alt_works "+
",( "+
"select distinct task_internal_id tid, fte_items.alt_fte_id fid, projects.id project_id "+
",alternatives.id alternative_id "+
",alt_map_items.wspace_id workspace_id "+
"from "+
"projects, alternatives, pj_basics, fte_items, alt_map_items, pj_map_items "+
"where "+
"projects.status in (2) "+
"and alternatives.prj_id = projects.id "+
"and alternatives.is_primary = 1 "+
"and alt_map_items.alt_id = alternatives.id "+
"and alt_map_items.map_type = 1 "+
"and alt_map_items.isdefault = 1 "+
"and pj_map_items.prj_id = projects.id "+
"and pj_map_items.map_type = 1 "+
"and pj_map_items.isdefault = 1 "+
"and pj_map_items.pj_basic_id = pj_basics.id "+
"and fte_items.alt_fte_id = alt_map_items.alt_fte_id ";
     public static String GET_MULT_PRJ_ITEMS_2 =
") taskList "+
"where "+
"alt_work_items.alt_work_id = alt_works.id "+
"and alt_works.ref_alt_fte_id = taskList.fid "+
"and alt_work_items.internal_id = taskList.tid "+
"order by alt_work_items.alt_work_id, internal_id ";

     public static String GET_MULT_PRJ_ITEMS_UPPER_LEVEL_1 =
"from "+
"alt_work_items, alt_works "+
"where alt_works.id = alt_work_items.alt_work_id and (alt_work_items.alt_work_id, alt_work_items.internal_id) in ";

     public static String GET_MULT_PRJ_ITEMS_UPPER_LEVEL_2 =
"order by alt_work_items.alt_work_id, internal_id ";

     public static String GET_MULT_RES_NEED_1 = Misc.G_DO_ORACLE ?
     "select "+
"fte_items.task_internal_id,  1, fte_items.fte_head_id "+
",fte_items.for_achieving_milestone, fte_items.classify1 "+
",fte_items.classify2, fte_items.classify3 "+
",fte_items.classify4, fte_items.classify5 "+
",month_timeid.time_id "+
", sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)) "+
", '' "+
", fte_items.target_market, fte_items.for_skill "+
", fte_items.assignment_status "+
", alt_map_items.alt_id "+
"from "+
"projects, alternatives, pj_basics, fte_items, alt_map_items, pj_map_items "+
", data, month_timeid "+
"where "+
"projects.status in (2) "+
"and alternatives.prj_id = projects.id "+
"and alternatives.is_primary = 1 "+
"and alt_map_items.alt_id = alternatives.id "+
"and alt_map_items.map_type = ? "+
"and alt_map_items.isdefault = 1 "+
"and pj_map_items.prj_id = projects.id "+
"and pj_map_items.map_type = 1 "+
"and pj_map_items.isdefault = 1 "+
"and pj_map_items.pj_basic_id = pj_basics.id "+
"and fte_items.alt_fte_id = alt_map_items.alt_fte_id "+
"and data.fte_item_id = fte_items.id "+
"and month_timeid.time_id >= trunc(data.year/35)*35 "+
"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
"when (data.val_scope=1) then 420 "+
"when (data.val_scope=2) then 35 "+
"when (data.val_scope=3) then 12 "+
"when (data.val_scope=5) then data.val_dur*1.25+7 "+
"else 1 "+
"end) "
:
"select "+
"fte_items.task_internal_id,  1, fte_items.fte_head_id "+
",fte_items.for_achieving_milestone, fte_items.classify1 "+
",fte_items.classify2, fte_items.classify3 "+
",fte_items.classify4, fte_items.classify5 "+
",month_timeid.time_id "+
", sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)) "+
", '' "+
", fte_items.target_market, fte_items.for_skill "+
", fte_items.assignment_status "+
", alt_map_items.alt_id "+
"from "+
"projects, alternatives, pj_basics, fte_items, alt_map_items, pj_map_items "+
", data, month_timeid "+
"where "+
"projects.status in (2) "+
"and alternatives.prj_id = projects.id "+
"and alternatives.is_primary = 1 "+
"and alt_map_items.alt_id = alternatives.id "+
"and alt_map_items.map_type = ? "+
"and alt_map_items.isdefault = 1 "+
"and pj_map_items.prj_id = projects.id "+
"and pj_map_items.map_type = 1 "+
"and pj_map_items.isdefault = 1 "+
"and pj_map_items.pj_basic_id = pj_basics.id "+
"and fte_items.alt_fte_id = alt_map_items.alt_fte_id "+
"and data.fte_item_id = fte_items.id "+
"and month_timeid.time_id >= cast((data.year/35) as int)*35 "+
"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
"when (data.val_scope=1) then 420 "+
"when (data.val_scope=2) then 35 "+
"when (data.val_scope=3) then 12 "+
"when (data.val_scope=5) then data.val_dur*1.25+7 "+
"else 1 "+
"end) ";


    public static String GET_MULT_RES_NEED_2 =
"group by fte_items.task_internal_id "+
",   fte_items.fte_head_id, fte_items.for_skill "+
", fte_items.assignment_status,fte_items.for_achieving_milestone, fte_items.target_market "+
", fte_items.classify1, fte_items.classify2 "+
", fte_items.classify3, fte_items.classify4, fte_items.classify5, month_timeid.time_id "+
", alt_map_items.alt_id "+
"order by fte_items.task_internal_id "+
",fte_items.fte_head_id, fte_items.for_skill "+
", fte_items.assignment_status,fte_items.for_achieving_milestone, fte_items.target_market "+
", fte_items.classify1, fte_items.classify2 "+
", fte_items.classify3, fte_items.classify4, fte_items.classify5, month_timeid.time_id ";

	public static String GET_MULT_RES_NEED_RAW_1 = "select "+
"fte_items.task_internal_id, 1 , fte_items.fte_head_id, "+
"fte_items.for_achieving_milestone, fte_items.classify1, fte_items.classify2, fte_items.classify3, "+
"fte_items.classify4, fte_items.classify5, data.year, data.value, '', data.val_scope, data.val_dur, fte_items.target_market, fte_items.for_skill, fte_items.assignment_status, alt_map_items.alt_id "+
"from "+
"projects, alternatives, pj_basics, fte_items, alt_map_items, pj_map_items "+
", data "+
"where "+
"projects.status in (2) "+
"and alternatives.prj_id = projects.id "+
"and alternatives.is_primary = 1 "+
"and alt_map_items.alt_id = alternatives.id "+
"and alt_map_items.map_type = ? "+
"and alt_map_items.isdefault = 1 "+
"and pj_map_items.prj_id = projects.id "+
"and pj_map_items.map_type = 1 "+
"and pj_map_items.isdefault = 1 "+
"and pj_map_items.pj_basic_id = pj_basics.id "+
"and fte_items.alt_fte_id = alt_map_items.alt_fte_id "+
"and data.fte_item_id = fte_items.id ";


     public static String GET_MULT_RES_NEED_RAW_2 =
"order by fte_items.task_internal_id,  "+
"fte_items.fte_head_id, fte_items.for_skill, fte_items.assignment_status,fte_items.for_achieving_milestone, fte_items.target_market, fte_items.classify1, fte_items.classify2, "+
"fte_items.classify3, fte_items.classify4, fte_items.classify5 "
;

	public static String GET_MULT_COST_NEED_RAW_1 = "select "+
		"cost_items.task_internal_id, 1, cost_cent_id, for_achieving_milestone, cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, data.year, data.value, cost_items.name, val_scope, val_dur,  cost_items.target_market, taskList.aid "+
"from "+
"cost_items "+
", data "+
",( "+
"select distinct task_internal_id tid "+
",alternatives.id aid "+
",alt_map_items.alt_devcost_id adid "+
"from "+
"projects, alternatives, pj_basics, fte_items, alt_map_items, pj_map_items "+
"where "+
"projects.status in (2) "+
"and alternatives.prj_id = projects.id "+
"and alternatives.is_primary = 1 "+
"and alt_map_items.alt_id = alternatives.id "+
"and alt_map_items.map_type = ? "+
"and alt_map_items.isdefault = 1 "+
"and pj_map_items.prj_id = projects.id "+
"and pj_map_items.map_type = 1 "+
"and pj_map_items.isdefault = 1 "+
"and pj_map_items.pj_basic_id = pj_basics.id "+
"and fte_items.alt_fte_id = alt_map_items.alt_fte_id ";

//

	public static String GET_MULT_COST_NEED_RAW_2 =
          " ) taskList "+
          "where cost_items.alt_devcost_id = taskList.adid "+
          "and cost_items.task_internal_id = taskList.tid "+
          "and data.cost_li_id = cost_items.id "+
          "order by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, target_market, "+
		"cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.name "
		;
	public static String GET_MULT_COST_NEED_1 = Misc.G_DO_ORACLE ?
  "select "+
		//was   "cost_items.task_internal_id, lineitem_id, cost_cent_id, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, month_timeid.time_id, sum(data.value* getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), name "+
		"cost_items.task_internal_id, 1, cost_cent_id, for_achieving_milestone, cost_items.classify1, " +
		"cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, month_timeid.time_id, " +
		// sameer 06272006 -- getPropIncluded added
		"sum(data.value* intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		//"sum(data.value), " +
		"cost_items.name, cost_items.target_market "+
          ",taskList.aid "+
"from "+
" cost_items "+
", data, month_timeid "+
",( "+
"select distinct task_internal_id tid "+
",alternatives.id aid "+
",alt_map_items.alt_devcost_id adid "+
"from "+
"projects, alternatives, pj_basics, fte_items, alt_map_items, pj_map_items "+
"where "+
"projects.status in (2) "+
"and alternatives.prj_id = projects.id "+
"and alternatives.is_primary = 1 "+
"and alt_map_items.alt_id = alternatives.id "+
"and alt_map_items.map_type = ? "+
"and alt_map_items.isdefault = 1 "+
"and pj_map_items.prj_id = projects.id "+
"and pj_map_items.map_type = 1 "+
"and pj_map_items.isdefault = 1 "+
"and pj_map_items.pj_basic_id = pj_basics.id "+
"and fte_items.alt_fte_id = alt_map_items.alt_fte_id "
:
"select "+
		//was   "cost_items.task_internal_id, lineitem_id, cost_cent_id, for_achieving_milestone, classify1, classify2, classify3, classify4, classify5, month_timeid.time_id, sum(data.value* getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), name "+
		"cost_items.task_internal_id, 1, cost_cent_id, for_achieving_milestone, cost_items.classify1, " +
		"cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, month_timeid.time_id, " +
		// sameer 06272006 -- getPropIncluded added
		"sum(data.value* intelli.getPropIncluded(data.val_scope, data.year, time_id, 2, data.val_dur)), " +
		//"sum(data.value), " +
		"cost_items.name, cost_items.target_market "+
          ",taskList.aid "+
"from "+
" cost_items "+
", data, month_timeid "+
",( "+
"select distinct task_internal_id tid "+
",alternatives.id aid "+
",alt_map_items.alt_devcost_id adid "+
"from "+
"projects, alternatives, pj_basics, fte_items, alt_map_items, pj_map_items "+
"where "+
"projects.status in (2) "+
"and alternatives.prj_id = projects.id "+
"and alternatives.is_primary = 1 "+
"and alt_map_items.alt_id = alternatives.id "+
"and alt_map_items.map_type = ? "+
"and alt_map_items.isdefault = 1 "+
"and pj_map_items.prj_id = projects.id "+
"and pj_map_items.map_type = 1 "+
"and pj_map_items.isdefault = 1 "+
"and pj_map_items.pj_basic_id = pj_basics.id "+
"and fte_items.alt_fte_id = alt_map_items.alt_fte_id ";
/*
          */

	public static String GET_MULT_COST_NEED_2 =
  Misc.G_DO_ORACLE ?
       " ) taskList "+
"where "+
"cost_items.alt_devcost_id = taskList.adid "+
"and cost_items.task_internal_id = taskList.tid "+
"and data.cost_li_id = cost_items.id "+
		"and month_timeid.time_id >= trunc(data.year/35)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, cost_items.target_market, "+
		"cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, month_timeid.time_id, cost_items.name, taskList.aid "+
		"order by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, cost_items.target_market, "+
		"cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, month_timeid.time_id, cost_items.name "
:    
       " ) taskList "+
"where "+
"cost_items.alt_devcost_id = taskList.adid "+
"and cost_items.task_internal_id = taskList.tid "+
"and data.cost_li_id = cost_items.id "+
		"and month_timeid.time_id >= cast((data.year/35) as int)*35 "+
		"and month_timeid.time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		"end) "+
		"group by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, cost_items.target_market, "+
		"cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, month_timeid.time_id, cost_items.name, taskList.aid "+
		"order by cost_items.task_internal_id,  cost_cent_id, for_achieving_milestone, cost_items.target_market, "+
		"cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, month_timeid.time_id, cost_items.name "
  	     ;
	public static String GET_MULT_PRJ_LEVEL_0 =
          "select projects.id, alternatives.id, alt_map_items.wspace_id, projects.name "+
          "from projects, alternatives, alt_map_items, pj_basics, pj_map_items "+
          "where "+
"projects.status in (2) "+
"and alternatives.prj_id = projects.id "+
"and alternatives.is_primary = 1 "+
"and alt_map_items.alt_id = alternatives.id "+
"and alt_map_items.map_type = 1 "+
"and alt_map_items.isdefault = 1 "+
"and pj_map_items.prj_id = projects.id "+
"and pj_map_items.map_type = 1 "+
"and pj_map_items.isdefault = 1 "+
"and pj_map_items.pj_basic_id = pj_basics.id "
  	     ;
	public static String GET_TEAMLIST_FOR_PRJ_MULT =
"select distinct fte_heads.name, fte_heads.id "+
"from "+
"projects, alternatives, pj_basics, fte_items, alt_map_items, pj_map_items, fte_heads "+
"where "+
"projects.status in (2) "+
"and alternatives.prj_id = projects.id "+
"and alternatives.is_primary = 1 "+
"and alt_map_items.alt_id = alternatives.id "+
"and alt_map_items.map_type = 1 "+
"and alt_map_items.isdefault = 1 "+
"and pj_map_items.prj_id = projects.id "+
"and pj_map_items.map_type = 1 "+
"and pj_map_items.isdefault = 1 "+
"and pj_map_items.pj_basic_id = pj_basics.id "+
"and fte_items.alt_fte_id = alt_map_items.alt_fte_id "+
"and fte_items.fte_head_id = fte_heads.id "+
"and fte_heads.is_generic = 0 ";

    public static String UPDATE_FTE_NAME = "update fte_heads set name = ? where id = ?";
    public static String DEL_CURR_CHANGE_LIST = "delete from curr_change_list";
    public static String GET_CURR_CHANGE_LIST = "select prj_id from curr_change_list";

    public static String COPY_ALT_BASIC_WITH_CURR = Misc.G_DO_ORACLE ? 
		"insert into alt_basics (id, wspace_id, alt_id, str_field10, str_field9, int_field7, int_field2, int_field9, int_field6, num_field7, int_field3, version_id, "+
		"num_field15, num_field8, num_field5, num_field2, int_field5, str_field6, str_field1, num_field12, num_field3, "+
		"num_field9, alt_risk, num_field14, num_field1, int_field4, str_field5, str_field3, num_field6, "+
		"num_field4, int_field1, int_field8, alt_basic_desc, str_field7, num_field10, mkt_stgy, str_field8, "+
		"str_field4, num_field13, patent_exp_date, str_field2, num_field11, ae_cout_id, revhandling "+
          ") "+
		"(select ?, alt_map_items.wspace_id, alt_basics.alt_id, str_field10, str_field9, int_field7, int_field2, int_field9, int_field6, num_field7, int_field3, version_id, "+
		"num_field15, num_field8, num_field5, num_field2, int_field5, str_field6, str_field1, num_field12, num_field3, "+
		"num_field9, alt_risk, num_field14, num_field1, int_field4, str_field5, str_field3, num_field6, "+
		"num_field4, int_field1, int_field8, alt_basic_desc, str_field7, num_field10, mkt_stgy, str_field8, "+
		"str_field4, num_field13, patent_exp_date, str_field2, num_field11, ae_cout_id, revhandling "+
		"from alt_basics, alt_map_items where alt_map_items.alt_id=? and alt_map_items.wspace_id=? and alt_basics.id = alt_map_items.alt_basic_id)"
    :
    "insert into alt_basics (wspace_id, alt_id, str_field10, str_field9, int_field7, int_field2, int_field9, int_field6, num_field7, int_field3, version_id, "+
		"num_field15, num_field8, num_field5, num_field2, int_field5, str_field6, str_field1, num_field12, num_field3, "+
		"num_field9, alt_risk, num_field14, num_field1, int_field4, str_field5, str_field3, num_field6, "+
		"num_field4, int_field1, int_field8, alt_basic_desc, str_field7, num_field10, mkt_stgy, str_field8, "+
		"str_field4, num_field13, patent_exp_date, str_field2, num_field11, ae_cout_id, revhandling "+
          ") "+
		"(select alt_map_items.wspace_id, alt_basics.alt_id, str_field10, str_field9, int_field7, int_field2, int_field9, int_field6, num_field7, int_field3, version_id, "+
		"num_field15, num_field8, num_field5, num_field2, int_field5, str_field6, str_field1, num_field12, num_field3, "+
		"num_field9, alt_risk, num_field14, num_field1, int_field4, str_field5, str_field3, num_field6, "+
		"num_field4, int_field1, int_field8, alt_basic_desc, str_field7, num_field10, mkt_stgy, str_field8, "+
		"str_field4, num_field13, patent_exp_date, str_field2, num_field11, ae_cout_id, revhandling "+
		"from alt_basics, alt_map_items where alt_map_items.alt_id=? and alt_map_items.wspace_id=? and alt_basics.id = alt_map_items.alt_basic_id)";
    public static String INSERT_MULTI_ATTRIBUTE = "insert into prj_multi_attrib (prj_id, attrib_id, int_val, date_val, double_val, str_val, instance_id, row_num) values (?,?,?,?,?,?,?,?)";
//TODO_INQUERY
    public static String DELETE_DEVCOST_DATA_ALL = Misc.G_DO_ORACLE ? "delete from data where cost_li_id in (select cost_items.id from cost_items where cost_items.alt_devcost_id = ?)"
    :
    "delete from data from cost_items where cost_li_id = cost_items.id and cost_items.alt_devcost_id = ?";



    public static String SPLIT_SPANNING_INTERVAL_1 =
    " insert into data "+
    " ( "+
    " value, year, val_scope, val_dur ";
    // ,cost_li_id
    public static String SPLIT_SPANNING_INTERVAL_2 = Misc.G_DO_ORACLE ?
    
    " ) "+
    " (select "+
    " data.value * "+
    " intelli.getPropIncludedSpecial(val_scope, year "+
    " , ?, ?+3650, val_dur) "+ //? date for one day after end of time val
    " ,? "+ //the timeid of one day after end of interval
    " , 5  "+
    " , intelli.getDateFor(year)+val_dur-?  "
:    
    " ) "+
    " (select "+
    " data.value * "+
    " intelli.getPropIncludedSpecial(val_scope, year "+
    " , ?, ?+3650, val_dur) "+ //? date for one day after end of time val
    " ,? "+ //the timeid of one day after end of interval
    " , 5  "+
//    " , cast(intelli.getDateFor(year) as numeric)+val_dur-cast(? as numeric)  "; //date for one day after
    " , datediff(d, ?, year+val_dur) ";

//" , cost_li_id from data
//" , cost_items
//" where
//" data.cost_li_id = cost_items.id
//" and cost_items.alt_devcost_id = -1
//" and npv_type = -1 //if npv
//" a clause marking the selection of scenario factors that we care for

    public static String SPLIT_SPANNING_INTERVAL_3 = Misc.G_DO_ORACLE ?
     " and year < ?  "+ //the start timeInd
    " and intelli.getDateFor(year)+val_dur > ?  "+ //the date for one day after
    " ) "
:    
    " and year < ?  "+ //the start timeInd
//$$%    " and cast(intelli.getDateFor(year) as numeric)+val_dur > cast(? as numeric)  "+ //the date for one day after
    " and intelli.getDateFor(year)  > ? - val_dur  "+ //the date for one day after
    " ) ";


//    public static String SPLIT_DATA_LINE_SPANNING_INTERVAL_2 =

    public static String SPLIT_SPANNING_INTERVAL_MEASURE_1 = Misc.G_DO_ORACLE ?
    " insert into measure_data "+
    " ( "+
    " val, time_val, val_scope, val_dur "+
    " ,measure_case_index_id "+
    " ,alt_measure_id "+
    " ) "+
    " (select "+
    " measure_data.val * "+
    " intelli.getPropIncludedSpecial(measure_data.val_scope, measure_data.time_val "+
    " , ?, ?+3650, measure_data.val_dur) "+ //? date for one day after end of time val
    " ,? "+ //the timeid of one day after end of interval
    " , 5  "+
    " , intelli.getDateFor(measure_data.time_val)+val_dur-?  "+ //date for one day after
    " ,measure_case_index_id "+
    " ,alt_measure_id "+
    " from measure_data "+
    " , measure_case_index "+
    " where "+
    " measure_data.measure_case_index_id = measure_case_index.id "+
    " and measure_data.alt_measure_id = ? "
:    
    " insert into measure_data "+
    " ( "+
    " val, time_val, val_scope, val_dur "+
    " ,measure_case_index_id "+
    " ,alt_measure_id "+
    " ) "+
    " (select "+
    " measure_data.val * "+
    " intelli.getPropIncludedSpecial(measure_data.val_scope, measure_data.time_val "+
    " , ?, ?+3650, measure_data.val_dur) "+ //? date for one day after end of time val
    " ,? "+ //the timeid of one day after end of interval
    " , 5  "+
    //" , cast(intelli.getDateFor(measure_data.time_val) as numeric)+val_dur-cast(? as numeric)  "+ //date for one day after
    " , datediff(d, ?, intelli.getDateFor(measure_data.time_val)+val_dur) "+
    " ,measure_case_index_id "+
    " ,alt_measure_id "+
    " from measure_data "+
    " , measure_case_index "+
    " where "+
    " measure_data.measure_case_index_id = measure_case_index.id "+
    " and measure_data.alt_measure_id = ? "; //alt_measure_id

    public static String SPLIT_SPANNING_INTERVAL_MEASURE_2 = Misc.G_DO_ORACLE ?
    
    " and time_val < ?  "+ //the start timeInd
    " and intelli.getDateFor(time_val)+val_dur > ?  "+ //the date for one day after
    " ) "
:    
    " and time_val < ?  "+ //the start timeInd
//$$%    " and cast(intelli.getDateFor(time_val) as numeric)+val_dur > cast(? as numeric)  "+ //the date for one day after
    " and intelli.getDateFor(time_val) > ? - val_dur "+
    " ) ";

    public static String UPDATE_DATA_LHS_FOR_SPLIT_1 =
" update "+
" (select data.id, data.value, data.val_scope, data.val_dur, data.year "+
" from data ";
//, cost_items
//where
//data.cost_li_id = cost_items.id
//and cost_items.alt_devcost_id = -1
    public static String UPDATE_DATA_LHS_FOR_SPLIT_2 = Misc.G_DO_ORACLE ?
    
" and data.year < ? "+ //the start time id
" and intelli.getDateFor(data.year)+data.val_dur > ?  "+ //the endDataExcl
" ) dataList "+
" set value = value * "+
" (1 - intelli.getPropIncludedSpecial(val_scope, year, ?, ?+3650, val_dur)) "+ //endDateExcl
" , val_scope = 5 "+
" , val_dur = ? - intelli.getDateFor(year) "
:
" and data.year < ? "+ //the start time id
//$$%" and cast(intelli.getDateFor(data.year) as numeric)+data.val_dur > cast(? as numeric)  "+ //the endDataExcl
" and intelli.getDateFor(data.year) > ?-data.val_dur  "+ //the endDataExcl
" ) dataList "+
" set value = value * "+
" (1 - intelli.getPropIncludedSpecial(val_scope, year, ?, ?+3650, val_dur)) "+ //endDateExcl
" , val_scope = 5 "+
//" , val_dur = cast(? as numeric) - cast(intelli.getDateFor(year) as numeric) ";
" , val_dur = datediff(d, intelli.getDateFor(year), ?) ";

    public static String UPDATE_DATA_LHS_FOR_SPLIT_MEASURE_1 = Misc.G_DO_ORACLE ?
    " update "+
" (select  measure_data.val value, measure_data.val_scope, measure_data.val_dur, measure_data.time_val year "+
" from measure_data, measure_case_index "+
" where alt_measure_id = ? and measure_case_index_id = measure_case_index.id "
:
" update "+
" (select  measure_data.val value, measure_data.val_scope, measure_data.val_dur, measure_data.time_val year "+
" from measure_data, measure_case_index "+
" where alt_measure_id = ? and measure_case_index_id = measure_case_index.id ";

    public static String UPDATE_DATA_LHS_FOR_SPLIT_MEASURE_2 = Misc.G_DO_ORACLE ?
    " and measure_data.time_val < ? "+ //the start time id
" and intelli.getDateFor(measure_data.time_val)+measure_data.val_dur > ?  "+ //the endDataExcl
" ) dataList "+
" set value = value * "+
" (1 - intelli.getPropIncludedSpecial(val_scope, year, ?, ?+3650, val_dur)) "+ //endDateExcl
" , val_scope = 5 "+
" , val_dur = ? - intelli.getDateFor(year) "
:
" and measure_data.time_val < ? "+ //the start time id
//$$%" and cast(intelli.getDateFor(measure_data.time_val) as numeric)+measure_data.val_dur > cast(? as numeric)  "+ //the endDataExcl
" and intelli.getDateFor(measure_data.time_val) > ?-measure_data.val_dur  "+ //the endDataExcl
" ) dataList "+
" set value = value * "+
" (1 - intelli.getPropIncludedSpecial(val_scope, year, ?, ?+3650, val_dur)) "+ //endDateExcl
" , val_scope = 5 "+
//" , val_dur = cast(? as numeric) - cast(intelli.getDateFor(year) as numeric) ";
" , val_dur = datediff(d, intelli.getDateFor(year), ?) ";


    public static String UPDATE_DATA_OVERLAP_1 = Misc.G_DO_ORACLE ?
    " update "+
" (select data.id, data.value, data.val_scope, data.val_dur, data.year "+
" , (1 - intelli.getPropIncludedSpecial(val_scope, year, ?, ?, val_dur)) prop "+
" from data "
:
" update "+
" (select data.id, data.value, data.val_scope, data.val_dur, data.year "+
" , (1 - intelli.getPropIncludedSpecial(val_scope, year, ?, ?, val_dur)) prop "+
" from data ";
//, cost_items
//where
//data.cost_li_id = cost_items.id
//and cost_items.alt_devcost_id = -1
    public static String UPDATE_DATA_OVERLAP_2 = Misc.G_DO_ORACLE ?
    " and ( "+
"      (data.year >= ? and data.year < ?) "+
"   or (data.year < ? and intelli.getDateFor(data.year)+val_dur >= ?) "+
" ) "+
" ) dataList "+
" set value = value * prop "+
" , val_scope = 5 "+
" , val_dur = val_dur * prop "+
" , year = (case when year < ? then year else ? end) "
:
" and ( "+
"      (data.year >= ? and data.year < ?) "+
//$$%"   or (data.year < ? and cast(intelli.getDateFor(data.year) as numeric)+val_dur >= cast(? as numeric)) "+
"   or (data.year < ? and (intelli.getDateFor(data.year) ) >= ?-val_dur ) "+
" ) "+
" ) dataList "+
" set value = value * prop "+
" , val_scope = 5 "+
" , val_dur = val_dur * prop "+
" , year = (case when year < ? then year else ? end) ";

    public static String UPDATE_DATA_OVERLAP_MEASURE_1 = Misc.G_DO_ORACLE ?
    " update "+
" (select measure_data.val value, measure_data.val_scope, measure_data.val_dur, measure_data.time_val year "+
" , (1 - intelli.getPropIncludedSpecial(val_scope, time_val, ?, ?, val_dur)) prop "+
" from measure_data, measure_case_index "+
" where measure_data.alt_measure_id = ? and measure_case_index_id = measure_case_index.id "
:
" update "+
" (select measure_data.val value, measure_data.val_scope, measure_data.val_dur, measure_data.time_val year "+
" , (1 - intelli.getPropIncludedSpecial(val_scope, time_val, ?, ?, val_dur)) prop "+
" from measure_data, measure_case_index "+
" where measure_data.alt_measure_id = ? and measure_case_index_id = measure_case_index.id ";

    public static String UPDATE_DATA_OVERLAP_MEASURE_2 = Misc.G_DO_ORACLE ?
    " and ( "+
"      (measure_data.time_val >= ? and measure_data.time_val < ?) "+
"   or (measure_data.time_val < ? and intelli.getDateFor(measure_data.time_val)+val_dur >= ?) "+
" ) "+
" ) dataList "+
" set value = value * prop "+
" , val_scope = 5 "+
" , val_dur = val_dur * prop "+
" , year = (case when year < ? then year else ? end) "
:
" and ( "+
"      (measure_data.time_val >= ? and measure_data.time_val < ?) "+
//$$%"   or (measure_data.time_val < ? and cast(intelli.getDateFor(measure_data.time_val) as numeric)+val_dur >= cast(? as numeric)) "+
"   or (measure_data.time_val < ? and (intelli.getDateFor(measure_data.time_val) ) >= ?-val_dur) "+
" ) "+
" ) dataList "+
" set value = value * prop "+
" , val_scope = 5 "+
" , val_dur = val_dur * prop "+
" , year = (case when year < ? then year else ? end) ";

   public static String DELETE_DATA_WHERE_0_1 =
" delete (select data.id from "+
" data ";
//, cost_items where data.cost_li_id = ? and cost_items.alt_devcost_model=?
   public static String DELETE_DATA_WHERE_0_2 =
" and data.value >= -0.0005 and data.value <= 0.0005)";

   public static String DELETE_DATA_WHERE_0_MEASURE_1 =
" delete from measure_data where alt_measure_id = ? and val >= -0.0005 and val <= 0.0005 ";
//TODO_INQUERY
   public static String DELETE_MEASURE_DATA_GLOB =
   Misc.G_DO_ORACLE ?
" delete "+
" from measure_data "+
" where "+
" val >=-0.0005 and val <= 0.0005 "+
" and alt_measure_id in (select alt_measure_id from "+
" measure_map_items where isdefault=1 and wspace_id=? and alt_id=?)"
:
" delete "+
" from measure_data from measure_map_items "+
" where "+
" val >=-0.0005 and val <= 0.0005 "+
" and measure_data.alt_measure_id = measure_map_items.alt_measure_id and "+
" isdefault=1 and wspace_id=? and alt_id=?";


   public static String DELETE_MEASURE_CASEINDEX_GLOB =
" delete from measure_case_index mci "+
" where not exists (select 1 from  measure_data "+
" where measure_case_index_id = mci.id) ";

   public static String SET_WKSP_MENU_TEMPLATE = 
   "update projects set menu_template=? where projects.id in (select prj_id from workspaces where workspaces.id = ?)";
   

   public static String SETUP_PRJ_PLANDATE_SUMMARY = Misc.G_DO_ORACLE ? 
   "insert into prj_plan_date_helper (project_id) (select projects.id from projects minus (select project_id from prj_plan_date_helper))"
   :
   "insert into prj_plan_date_helper (project_id) (select projects.id from projects where  Not Exists( (select project_id from prj_plan_date_helper)))";

   public static String UPD_PRJ_PLANDATE_SUMMARY_KNOWN_MEASURES = Misc.G_DO_ORACLE ?
    " (select max(on_date) from wksp_hist, pj_map_items "+
" where "+
" pj_map_items.wspace_id = wksp_hist.wspace_id "+
" and isdefault=1 "+
" and map_type in (1) "+
" and prj_id = plh.project_id "+
" and ref_item_type = ? "+
" ) " 
:
" (select max(on_date) from wksp_hist, pj_map_items "+
" where "+
" pj_map_items.wspace_id = wksp_hist.wspace_id "+
" and isdefault=1 "+
" and map_type in (1) "+
" and prj_id = prj_plan_date_helper.project_id "+
" and ref_item_type = ? "+
" ) ";

   public static String UPD_PRJ_PLANDATE_SUMMARY_FLEX_MEASURES =Misc.G_DO_ORACLE ? 
   " (select max(on_date) from wksp_hist, measure_map_items, alternatives "+
" where "+
" measure_map_items.wspace_id = wksp_hist.wspace_id "+
" and isdefault=1 "+
" and map_type in (1) "+
" and alternatives.id = measure_map_items.alt_id "+
" and alternatives.prj_id = plh.project_id "+
" and ref_item_type = 7 "+ //section_alt_measure
" and ref_item_id = measure_map_items.alt_measure_id "+
" and measure_map_items.measure_id = ? "+
" ) "
:
" (select max(on_date) from wksp_hist, measure_map_items, alternatives "+
" where "+
" measure_map_items.wspace_id = wksp_hist.wspace_id "+
" and isdefault=1 "+
" and map_type in (1) "+
" and alternatives.id = measure_map_items.alt_id "+
" and alternatives.prj_id = prj_plan_date_helper.project_id "+
" and ref_item_type = 7 "+ //section_alt_measure
" and ref_item_id = measure_map_items.alt_measure_id "+
" and measure_map_items.measure_id = ? "+
" ) ";

  public static String CREATE_SIMPLE_TASK = Misc.G_DO_ORACLE ?
  "insert into alt_work_items(alt_work_id, id, internal_id, parent_internal_id, pred, name, type, wbs_level, for_achieving_ms, start_date, end_date, target_market, classify1, classify2, classify3, classify4, classify5) "+
  " values (?, seq_alt_work_items.nextval, ?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?)"
  :
  "insert into alt_work_items(alt_work_id,  internal_id, parent_internal_id, pred, name, type, wbs_level, for_achieving_ms, start_date, end_date, target_market, classify1, classify2, classify3, classify4, classify5) "+
  " values (?, ?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?)";
//TODO_INQUERY

  public static String UPDATE_FTE_DATA_PARTIAL_SIMPLE = Misc.G_DO_ORACLE ? "update data set data.value = ? * data.value where data.fte_item_id in (select id from fte_items where alt_fte_id = ? and for_skill = ? and target_market = ? and for_achieving_milestone in ( " // ") )"
  :
  "update data set data.value = ? * data.value from fte_items where (data.fte_item_id = fte_items.id and alt_fte_id = ? and for_skill = ? and target_market = ? and for_achieving_milestone in ( "; // ") )";
  public static String DELETE_FTE_DATA_PARTIAL_SIMPLE = Misc.G_DO_ORACLE ? "delete from data where data.fte_item_id in (select id from fte_items where alt_fte_id = ? and for_skill = ? and target_market = ? and for_achieving_milestone in ( "
  :
  "delete from data from fte_items where (data.fte_item_id = fte_items.id and alt_fte_id = ? and for_skill = ? and target_market = ? and for_achieving_milestone in ( "; // ") )";// ") )";
  public static String DELETE_FTE_ITEM_PARTIAL_SIMPLE = "delete from fte_items where alt_fte_id = ? and for_skill = ? and target_market = ? and for_achieving_milestone in ( "; // ") "

  public static String UPDATE_COST_DATA_PARTIAL_SIMPLE = Misc.G_DO_ORACLE ? "update data set data.value = ? * data.value where data.cost_li_id in (select id from cost_items where cost_items.alt_devcost_id = ? and cost_cent_id = ? and target_market = ? and for_achieving_milestone in ( "
  :
  "update data set data.value = ? * data.value from cost_items where (data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? and cost_cent_id = ? and target_market = ? and for_achieving_milestone in ( "; // ") )";
  public static String DELETE_COST_DATA_PARTIAL_SIMPLE = Misc.G_DO_ORACLE ? "delete from data where data.cost_li_id in (select id from cost_items where cost_items.alt_devcost_id = ? and cost_cent_id = ? and target_market = ? and for_achieving_milestone in ( "
  :
  "delete from data from cost_items where (data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? and cost_cent_id = ? and target_market = ? and for_achieving_milestone in ( "; // ") )"// ") )"
  public static String DELETE_COST_ITEM_PARTIAL_SIMPLE = "delete from cost_items where cost_items.alt_devcost_id = ? and cost_cent_id = ? and target_market = ? and for_achieving_milestone in ( "; // ") ";

  public static String UPDATE_REV_DATA_TIME = Misc.G_DO_ORACLE ?
  "update data set data.year = getTimeId(getDateFor(data.year)+?), data.value = ? * data.value where data.rev_seg_id in (select id from rev_segs where alt_rev_id = ? and mkt_type = ? ) "
  :
  "update data set data.year = intelli.getTimeId(intelli.getDateFor(data.year)+?), data.value = ? * data.value from rev_segs where (data.rev_seg_id = rev_segs.id and alt_rev_id = ? and mkt_type = ? ) ";
  public static String UPDATE_OPCOST_DATA_TIME =Misc.G_DO_ORACLE ?
  "update data set data.year = getTimeId(getDateFor(data.year)+?), data.value = ? * data.value where data.cost_li_id in (select id from cost_items where alt_opcost_id = ? and target_market = ? ) "
  :
  "update data set data.year = intelli.getTimeId(intelli.getDateFor(data.year)+?), data.value = ? * data.value from cost_items where (data.cost_li_id = cost_items.id and alt_opcost_id = ? and target_market = ? ) ";
  public static String GET_PEAK_ANNUAL_REV = //doesn't work for custom dur or off-cycle data.year
  Misc.G_DO_ORACLE ?
   "select max(ann_rev.dv) from "+
		"( select  " +
		" sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)) dv, time_id " +
		" from data, rev_segs, year_timeid  "+
		" where rev_segs.alt_rev_id = ? "+
		" and data.rev_seg_id = rev_segs.id "+
		" and time_id >= trunc(data.year/420)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		" group by time_id) ann_rev "
    :
          "select max(ann_rev.dv) from "+
		"( select  " +
		" sum(data.value * intelli.getPropIncluded(data.val_scope, data.year, time_id, 1, data.val_dur)) dv, time_id " +
		" from data, rev_segs, year_timeid  "+
		" where rev_segs.alt_rev_id = ? "+
		" and data.rev_seg_id = rev_segs.id "+
		" and time_id >= cast((data.year/420) as int)*420 "+
		" and time_id < data.year+(case when (data.val_scope = 0) then 105 "+
		"    when (data.val_scope=1) then 420 "+
		"   when (data.val_scope=2) then 35 "+
		"    when (data.val_scope=3) then 12 "+
		"    when (data.val_scope=5) then data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) "+
		" group by time_id) ann_rev ";

   public static String GET_PORTNODE_INFO =
   "select id, name, full_name, port_node_desc, default_currency, default_date_format, default_reporting_currency, default_currency_scale, default_group_threshold, org_type, external_code, hier_level, country_code, locale_id, default_threshold1, default_threshold2, default_threshold3, default_threshold4, consolidation_status, classify2, classify3, classify4, classify5, str_field1, str_field2, str_field3, str_field4, str_field5 from port_nodes where id = ? ";
   
   public static String GET_ALLOWED_CURRENCIES = "select currency_id from allowed_currency where port_node_id = ?";
   public static String GET_PORTFOLIO_CHILDREN = "select id, name, full_name, port_node_desc, default_currency, default_date_format, default_reporting_currency, default_currency_scale, default_group_threshold, org_type, external_code from port_nodes where port_node_id = ? ";

   public static String UPDATE_PRJ_PORT_MAP = "update prj_portfolio_map set port_node_id = ?, par_level = ? where port_node_id = ?";
   public static String DEL_PORT_NODE = "update  port_nodes  set status = 0 where id = ?";
   public static String DEL_ALLOWED_CURRENCIES = "delete from allowed_currency where port_node_id = ?";

   public static String INSERT_PORT_NODES = Misc.G_DO_ORACLE ?
   "insert into port_nodes (id, name, full_name, port_node_desc, port_node_id, default_currency, status, default_date_format, default_reporting_currency, org_type, default_group_threshold, external_code, default_currency_scale, hier_level, country_code, locale_id, default_threshold1, default_threshold2, default_threshold3, default_threshold4, consolidation_status, classify2, classify3, classify4, classify5, str_field1, str_field2, str_field3, str_field4, str_field5) "+
   " values (?,?,?,?,?,?,1,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
   :
   "insert into port_nodes (name, full_name, port_node_desc, port_node_id, default_currency, status, default_date_format, default_reporting_currency, org_type, default_group_threshold, external_code, default_currency_scale, hier_level, country_code, locale_id, default_threshold1, default_threshold2, default_threshold3, default_threshold4, consolidation_status, classify2, classify3, classify4, classify5, str_field1, str_field2, str_field3, str_field4, str_field5) "+
   " values (?,?,?,?,?,1,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
   
   public static String UPDATE_PORT_NODES =
   "update port_nodes set name=?, full_name=?, port_node_desc=?,  default_currency=?, default_date_format=?, default_reporting_currency=?, org_type=?, default_group_threshold=?, external_code=?, default_currency_scale=?, hier_level = ?, country_code=?, locale_id=?, default_threshold1=?, default_threshold2=?, default_threshold3=?, default_threshold4=?, consolidation_status=?, classify2=?, classify3=?, classify4=?, classify5=?, str_field1=?, str_field2=?, str_field3=?, str_field4=?, str_field5=? where id = ? ";
   
   public static String INSERT_ALLOWED_CURRENCIES = "insert into allowed_currency (port_node_id, currency_id) values (?,?)";

	 public static String GET_USER_AND_ROLE_FOR_PORT = "select user_roles.user_1_id, users.name, role.id, role.name, role.role_desc, user_roles_scope.grantable from user_roles_scope, user_roles,role, users where user_roles_scope.port_node_id = ? and user_roles.id = user_roles_scope.user_role_id and role.id = user_roles.role_id and users.id = user_roles.user_1_id order by users.name asc"; //rajeev 022608
   public static String GET_WORKFLOW_RULE = //rajeev_090407
   "select users.id, users.name, workflow_user.wf_role, workflow_user.classify1, workflow_user.classify2, workflow_user.classify3, workflow_user.classify4, workflow_user.classify5, workflow_user.thresh, workflow_user.is_override, workflow_user.is_default from workflow_user, users where users.id = workflow_user.user_id and port_node_id = ? order by workflow_user.wf_role asc, classify1, classify2, classify3, classify4, classify5 ";
   public static String DEL_WORKFLOW_RULE = "delete from workflow_user where port_node_id = ? ";
   public static String INS_WORKFLOW_RULE = "insert into workflow_user (wf_role, user_id, port_node_id, classify1, classify2, classify3, classify4, classify5, thresh, is_override, is_default) values (?,?,?,?,?,?,?,?,?,?,?) ";

//TODO_*=
	public static String GET_WORKSPACE_STATE_INFO = Misc.G_DO_ORACLE ?
	"select wf_step, start_date, end_date, users.name, users.id, users.email, workflow_status.type_of, workflow_status.seq, workflow_status.comments, workflow_status.approver_role from workflow_status, users where workflow_status.user_id = users.id(+) and wspace_id = ? and (? is null or ? = wf_step) order by seq, workflow_status.start_date" //order by workflow_status.start_date is needed to handle a bug in earlier sequence no. being gen.
	:
		//"select wf_step, start_date, end_date, users.name, users.id, users.email, workflow_status.type_of, workflow_status.seq, workflow_status.comments, workflow_status.approver_role from workflow_status, users where workflow_status.user_id *= users.id and wspace_id = ? and (? is null or ? = wf_step) order by seq";
		 "select wf_step, start_date, end_date, users.name, users.id, users.email, " +
		 "workflow_status.type_of, workflow_status.seq, workflow_status.comments, " +
		 "workflow_status.approver_role " +
		 "from workflow_status " +
		 "left outer join users on workflow_status.user_id = users.id " +
		 "where wspace_id = ? and (? is null or ? = wf_step) " +
		 "order by seq ";
   //TODO_INQUERY
   public static String GET_CURRWORKFLOW_STATE = 
   "select projects.curr_workflow_step, projects.in_workflow_comment from projects, workspaces where projects.id = workspaces.prj_id and workspaces.id = ? ";
   
   public static String GET_PRJ_ATTRIB_REMAINING = " from projects, pj_basics, alternatives, alt_basics, workspaces, pj_map_items, alt_map_items " +
                                                   " where projects.id = ? "+
                                                   " and workspaces.id = ? "+
                                                   " and alternatives.id = ? "+
                                                   " and pj_map_items.wspace_id = workspaces.id "+
                                                   " and pj_map_items.isdefault = 1 "+
                                                   " and alt_map_items.wspace_id = workspaces.id "+
                                                   " and alt_map_items.isdefault = 1 "+
                                                   " and alt_map_items.alt_id = alternatives.id "+
                                                   " and pj_basics.id = pj_map_items.pj_basic_id "+
                                                   " and alt_basics.id = alt_map_items.alt_basic_id ";

   public static String GET_APPROVER_ROLE_USER_PARTIAL = "select workflow_user.wf_role, workflow_user.user_id, users.name, users.email "+
      " from workflow_user, users, prj_portfolio_map "+
      " where prj_portfolio_map.prj_id = ? "+
      " and workflow_user.port_node_id = prj_portfolio_map.port_node_id "+
      " and users.id = workflow_user.user_id "+
      " and wf_role in " ; //( role_list )
      //" and (workflow_user.classify1 is null or workflow_user.classify1 = -1 or workflow_user.classify1 = -1) for each valid crit
      // order by wf_role "
      // ,par_level desc
      //,(
      //(case when classify1 = -1 then 1 else 0 end)
      //+ (case when classify2 = -1 then 1 else 0 end)
      //) desc

   public static String GET_APPROVAL_RECORD = //CAPEX_REMOVE WkspStepMgr.g_keepPastApproval ? 
                                                                             "select wspace_id, wf_step, user_id, start_date, end_date, type_of, comments, approver_role from workflow_status where wf_step = ? and (user_id = ? or ? is null) and wspace_id = ? and (approver_role = ? or ? is null) and (? is null or seq = ?)"
                                                                            //CAPEX_REMOVE : "select wspace_id, wf_step, user_id, start_date, end_date, type_of, comments, approver_role from workflow_status where wf_step = ? and (user_id = ? or ? is null) and wspace_id = ? and (approver_role = ? or ? is null) ";
                                                                             ;
   public static String UPDATE_APPROVAL_RECORD = //CAPEX_REMOVE WkspStepMgr.g_keepPastApproval ? 
                                                       "update workflow_status set end_date = ?, type_of = ?, comments = (case when (? is null) then comments else ? end), user_id = (case when (? is null) then user_id else ? end) where (? is null or seq = ? ) and wf_step = ? and (user_id = ? or (? is null and user_id is null)) and wspace_id = ? and (approver_role = ? or (? is null and approver_role is null))"
                                                                               //CAPEX_REMOVE : "update workflow_status set end_date = ?, type_of = ?, comments = (case when (? is null) then comments else ? end), user_id = (case when (? is null) then user_id else ? end), seq = (case when (? is null) then seq else ? end) where wf_step = ? and (user_id = ? or (? is null and user_id is null)) and wspace_id = ? and (approver_role = ? or (? is null and approver_role is null))"
                                                                               ;
   public static String INSERT_APPROVAL_RECORD = "insert into workflow_status (wf_step, start_date, end_date, type_of, user_id, wspace_id, seq, comments, approver_role) values (?,?,?,?,?,?,?,?,?)";
   public static String DELETE_WORKFLOW_STATUS = //CAPEX_REMOVE WkspStepMgr.g_keepPastApproval ? 
                                                           "delete from workflow_status where wspace_id = ? and wf_step = ? and ((? is null ) or user_id = ?) and ((? is null ) or approver_role = ?) and (? is null or seq = ?) "
                                                                               //CAPEX_REMOVE : "delete from workflow_status where wspace_id = ? and wf_step = ? and ((? is null ) or user_id = ?) and ((? is null ) or approver_role = ?)"
                                                                               ;

   public static String GET_DEFAULT_WKSP_STATE_ALT_ETC_1 = "select projects.id, workspaces.id, alternatives.id, projects.status, projects.simple_scen_incl, pj_basics.int_field10 from projects, pj_map_items, workspaces, alternatives, pj_basics where  1=1 ";
   public static String GET_DEFAULT_WKSP_STATE_ALT_ETC_2 =Misc.G_DO_ORACLE ? 
   " and pj_map_items.prj_id = projects.id and workspaces.prj_id = projects.id and alternatives.prj_id = projects.id and pj_map_items.wspace_id = workspaces.id and (map_type = 1 or map_type = 4) and (isdefault=1) and pj_basics.id = pj_map_items.pj_basic_id order by projects.id desc, map_type asc, date_created desc,is_primary desc nulls last, alternatives.id desc, workspaces.id desc "
   :
   " and pj_map_items.prj_id = projects.id and workspaces.prj_id = projects.id and alternatives.prj_id = projects.id and pj_map_items.wspace_id = workspaces.id and (map_type = 1 or map_type = 4) and (isdefault=1)  and pj_basics.id = pj_map_items.pj_basic_id order by projects.id desc, map_type asc, date_created desc,is_primary desc , alternatives.id desc, workspaces.id desc ";
   

   public static String GET_WORKFLOW_APPROVAL_MATRIX = "select portfolio_id, id, wf_step, classify1, classify2, classify3, classify4, classify5, thresh, role_id from wkf_approval_packages, wkf_approval_package_roles where wkf_approval_package_roles.wkf_app_id = wkf_approval_packages.id and portfolio_id=? order by wf_step, id";
   //TODO_INQUERY .. not imp
   public static String DEL_WORKFLOW_APPROVAL_ROLE = "delete from wkf_approval_package_roles where wkf_app_id in (select id from wkf_approval_packages where portfolio_id = ?)";
   public static String DEL_WORKFLOW_APPROVAL_PACKAGE = "delete from wkf_approval_packages where portfolio_id = ?";
   public static String INS_WORKFLOW_APPROVAL_PACKAGE =Misc.G_DO_ORACLE ? "insert into wkf_approval_packages (id, wf_step, portfolio_id, classify1, classify2, classify3, classify4, classify5, thresh, is_bakup) values (?,?,?,?,?,?,?,?,?,?) "
   :
                                                                        "insert into wkf_approval_packages ( wf_step, portfolio_id, classify1, classify2, classify3, classify4, classify5, thresh, is_bakup) values (?,?,?,?,?,?,?,?,?) ";
   public static String INS_WORKFLOW_APPROVAL_ROLE = "insert into wkf_approval_package_roles (wkf_app_id, role_id) values (?,?) ";

   public static String GET_GLOBAL_DATA_ALL = "select data_code, int_field1, int_field2, int_field3, int_field4, int_field5, date_field1, date_field2, date_field3, date_field4, date_field5 from misc_global";
   public static String GET_GLOBAL_DATA = "select data_code, int_field1, int_field2, int_field3, int_field4, int_field5, date_field1, date_field2, date_field3, date_field4, date_field5 from misc_global where data_code=?";   
   public static String UPDATE_GLOBAL_DATA = "update misc_global set int_field1=?, int_field2=?, int_field3=?, int_field4=?, int_field5=?, date_field1=?, date_field2=?, date_field3=?,date_field4=?,date_field5=? where data_code = ?";
   public static String INSERT_GLOBAL_DATA = "insert into misc_global (int_field1,int_field2,int_field3,int_field4, int_field5, date_field1, date_field2, date_field3, date_field4, date_field5, data_code) values (?,?,?,?,?,  ?,?,?,?,?,  ?)";

   public static String UPDATE_MEASURE_MAP_ITEM = "update measure_map_items set alt_measure_id = ? where measure_id = ? and wspace_id = ? and alt_id = ? and isdefault = 1";
   public static String GET_MIN_MAX_DATE_OF_MS = "select min(start_date), max(finish_dt) from milestones, alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault = 1 and milestones.alt_date_id = alt_map_items.alt_date_id and (milestones.finish_dt - milestones.start_date) >= 0.25";         
/******************************  TILL HERE SENT TO SHYAM FOR PORT ************************/
   public static String GET_INFO_COMPLETE = "select  project_id, section, iscomplete, isread, ishidden from info_complete where project_id = ? and (section = ? or ? is null) and object_type = ?"; //rajeev 021908
   public static String GET_INFO_COMPLETE_PROP = "select  project_id, section, iscomplete, isread, ishidden from info_complete where project_id = ? and (section = ? or (? is null and section like '$v%')) and object_type = ? "; //rajeev 021908
   public static String UPDATE_INFO_COMPLETE = "update info_complete set iscomplete = (case when (? = -1) then ( iscomplete) else ? end), isread = (case when (? = -1) then ( isread) else ? end) where project_id = ? and ((? is null and not(section like '$v%')) or section = ?) and object_type = ?";//rajeev 021908
   public static String CREATE_INFO_COMPLETE = "insert into info_complete (iscomplete, isread, project_id, section, object_type) values ((case when (? = -1) then (0) else ? end),(case when (? = -1) then ( 0) else ? end),?,?,?)"; //rajeev 021908
   public static String GET_APPROVAL_PACKAGE = "select wkf_approval_packages.portfolio_id, wkf_approval_packages.wf_step, wkf_approval_packages.id, wkf_approval_packages.thresh, wkf_approval_packages.classify1, wkf_approval_packages.classify2, wkf_approval_packages.classify3, wkf_approval_packages.classify4, wkf_approval_packages.classify5, wkf_approval_package_roles.role_id from wkf_approval_packages, "+
   " wkf_approval_package_roles, port_nodes where wkf_approval_packages.id = wkf_approval_package_roles.wkf_app_id and port_nodes.id = wkf_approval_packages.portfolio_id order by wkf_approval_packages.wf_step, port_nodes.hier_level desc, wkf_approval_packages.id asc";

   /*********** 053107 **************/
   public static String UPDATE_START_END_COST_ITEMS_BY_DATA = Misc.G_DO_ORACLE ? "update cost_items ci set (start_date, end_date) = (select min(intelli.getDateFor(data.year)), max(intelli.getDateFor(intelli.getEndOfTimeIdIncl(data.year, data.val_dur, data.val_scope))) from data where data.cost_li_id = ci.id and start_date is null and end_date is null and ci.alt_devcost_id = ? and ci.alt_devcost_id=?)" //double alt_devcost-id param for compat with sql server qery
                                                                               :
         " update cost_items "+
         " set  "+
         " start_date = da.mi , "+
         " end_date = da.mx "+
         " from cost_items ci, (select min(intelli.getDateFor(dai.year)) mi, max(intelli.getDateFor(dai.year)) mx, cost_items.id cid from data dai, cost_items where dai.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? group by cost_items.id) da "+
         " where  "+
         " da.cid = ci.id and "+
         " ci.start_date is null and "+
         " ci.end_date is null and  "+
         " ci.alt_devcost_id = ? ";

//TODO_INQUERY
   public static String UPDATE_DATA_SET_PAST_TO_ZERO = Misc.G_DO_ORACLE ?
    "update data set data.value = data.value * intelli.getPropIncludedMaster(data.val_scope, data.year, -1, 1, ?, -1, data.val_dur,1) where data.cost_li_id in (select cost_items.id from cost_items where cost_items.alt_devcost_id = ?)"
    :
    "update data set data.value = data.value * intelli.getPropIncludedMaster(data.val_scope, data.year, -1, 1, ?, -1, data.val_dur,1) from cost_items where data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? ";   
    
       //1st is curr date, 2nd is alt_devcost_id
//TODO_INQUERY       
   public static String SET_DATA_TO_START_TODAY = Misc.G_DO_ORACLE ?
   "update data set data.year = ?, val_scope = 5, val_dur = intelli.getEndOfDateExcl(data.year, data.val_dur, data.val_scope) - ? where data.cost_li_id in (select cost_items.id from cost_items where cost_items.alt_devcost_id = ?) and data.year < ? and (data.value > 0.000005 or data.value < -0.000005)"
   :
//   "update data set data.year = ?, val_scope = 5, val_dur = cast(intelli.getEndOfDateExcl(data.year, data.val_dur, data.val_scope) - ? as numeric) from cost_items where data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? and data.year < ? and (data.value > 0.000005 or data.value < -0.000005)";
   "update data set data.year = ?, val_scope = 5, val_dur = datediff(d, ?, intelli.getEndOfDateExcl(data.year, data.val_dur, data.val_scope)) from cost_items where data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? and data.year < ? and (data.value > 0.000005 or data.value < -0.000005)";
        //1st:currTimId    2:currdate  3:alt dev cost id, 4:currTimeId

//TODO_INQUERY
   public static String SET_DATA_PROP_OF_REMAINING = Misc.G_DO_ORACLE ? "update data set data.value = data.value * (1+ intelli.getPropIncluded(data.val_scope,data.year,?,?,data.val_dur)*(?-1)) where "+
                " data.cost_li_id in (select cost_items.id from cost_items where cost_items.alt_devcost_id = ? "//and classify2 in (1))
               //1=>timeIdOfScopedTimeVal, 2=>scope, 3=>factor, 4=> alt_devcost_id,
               :
               "update data set data.value = data.value * (1+ intelli.getPropIncluded(data.val_scope,data.year,?,?,data.val_dur)*(?-1)) from cost_items where "+
                " (data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? ";//and classify2 in (1))
               //1=>timeIdOfScopedTimeVal, 2=>scope, 3=>factor, 4=> alt_devcost_id,
//TODO_INQUERY .. all and similar below not imp currently

   public static String SET_MS_STATUS_FINISH_DATE = "update milestones set finish_dt=?, ms_status=?, start_date = (case when (?-start_date) >= 1 then start_date else ?-1 end)  where mstn_id = ? and alt_date_id in (select alt_date_id from alt_map_items where wspace_id = ? and alt_id=? and isdefault=1)"; //081808
   public static String SET_MS_STATUS_START_DATE = "update milestones set start_date=?, ms_status=?, finish_dt = (case when (finish_dt-?) >= 1 then finish_dt else ?+1 end)   where mstn_id = ? and alt_date_id in (select alt_date_id from alt_map_items where wspace_id = ? and alt_id=? and isdefault=1)"; //081808
   public static String UPDATE_ALTERNATIVE_FLAG = "update alternatives set is_primary=? where prj_id=? and is_primary=0 and exists(select 1 from alternatives where prj_id=? and is_primary=1)";

   public static String SIMPLE_SHIFT_GLB_MEASURE = "update measure_data set time_val = time_val+?*35 where alt_measure_id in (select alt_measure_id from measure_map_items where wspace_id = ? and alt_id = ? and isdefault=1 and measure_id in (5047, 5048)) and time_val >= ?";
         //do for rev, op cost, fte, dev cost
   public static String SIMPLE_SHIFT_GLB_DATA_REV = "update data set year = year+?*35 where rev_seg_id in (select rev_segs.id from alt_map_items,rev_segs where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault =1 and rev_segs.alt_rev_id = alt_map_items.alt_rev_id) and year >= ?"; //rajeev 022608
   public static String SIMPLE_SHIFT_GLB_DATA_OPCOST = "update data set year = year+?*35 where cost_li_id in (select cost_items.id from alt_map_items,cost_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault =1 and cost_items.alt_opcost_id = alt_map_items.alt_opcost_id) and year >= ?"; //rajeev 022608
   public static String SIMPLE_SHIFT_GLB_DATA_DEVCOST = "update data set year = year+?*35 where cost_li_id in (select cost_items.id from alt_map_items,cost_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault =1 and cost_items.alt_devcost_id = alt_map_items.alt_devcost_id) and year >= ?"; //rajeev 022608
   public static String SIMPLE_SHIFT_GLB_DATA_FTE = "update data set year = year+?*35 where fte_item_id in (select fte_items.id from alt_map_items,fte_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault =1 and fte_items.alt_fte_id = alt_map_items.alt_fte_id) and year >= ?"; //rajeev 022608
   //uses oracle specific Add_months function
      public static String SIMPLE_SHIFT_GLB_COST_DATE = 
	   (Misc.G_DO_ORACLE) ?
	   "update cost_items set start_date = Add_months(start_date, ?), end_date = Add_months(end_date, ?) where cost_items.alt_devcost_id in (select alt_devcost_id from alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault=1)"
	   :
	   "update cost_items set start_date = DATEADD(M, ?, start_date), end_date = DATEADD(M, ?, end_date) where cost_items.alt_devcost_id in (select alt_devcost_id from alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault=1)";
	public static String SIMPLE_SHIFT_GLB_FTE_DATE =
		(Misc.G_DO_ORACLE) ?
		"update fte_items set start_date = Add_months(start_date, ?), end_date = Add_months(end_date, ?) where fte_items.alt_fte_id in (select alt_fte_id from alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault=1)"
		:
		"update fte_items set start_date = DATEADD(M, ?, start_date), end_date = DATEADD(M, ?, end_date) where fte_items.alt_fte_id in (select alt_fte_id from alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault=1)";
   public static String SIMPLE_SHIFT_GLB_WORK_DATE = 
	   (Misc.G_DO_ORACLE) ?
	   "update alt_work_items set start_date = Add_months(start_date, ?), end_date = Add_months(end_date, ?) where alt_work_items.alt_work_id in (select alt_work_id from alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault=1)"
	   :
	   "update alt_work_items set start_date = DATEADD(M, ?, start_date), end_date = DATEADD(M, ?, end_date) where alt_work_items.alt_work_id in (select alt_work_id from alt_map_items where alt_map_items.wspace_id = ? and alt_map_items.alt_id = ? and alt_map_items.isdefault=1)";
////TODO_INQUERY
   public static String SET_MEASURE_TEMP_COPY_OF_NULL = Misc.G_DO_ORACLE ? "update measure_case_index set temp_copy_of=null where id in (select measure_case_index_id from measure_data where alt_measure_id=?)"
   :
   "update measure_case_index set temp_copy_of=null from (select distinct measure_case_index_id mcid from measure_data where measure_data.alt_measure_id = ?) mcidl  where measure_case_index.id = mcidl.mcid ";
   public static String SET_DEVCOST_TEMP_COPY_OF_NULL = "update cost_items set temp_copy_of=null where alt_devcost_id=?";   
   public static String GET_SCENARIO_SPECIFIC_VERSION_MEASURE = "select alt_measure_id, port_wksps.name || '.' || port_rset.name from port_wksps, port_rset, port_results_measure where port_results_measure.port_rs_id = ? and port_results_measure.alt_id = ? and port_results_measure.port_rs_id = port_rset.id and port_rset.port_wksp_id = port_wksps.id and measure_id=?";  
   
   public static String DEL_DATA_REV_IF_ZERO = Misc.G_DO_ORACLE ? "delete from data where data.rev_seg_id in (select rev_segs.id from rev_segs where rev_segs.alt_rev_id = ?) and data.value >= -0.000005 and data.value <= 0.000005"
   :
   "delete from data from rev_segs where data.rev_seg_id = rev_segs.id and rev_segs.alt_rev_id = ? and data.value >= -0.000005 and data.value <= 0.000005";
   public static String DEL_DATA_DEVCOST_IF_ZERO = Misc.G_DO_ORACLE ? "delete from data where data.cost_li_id in (select cost_items.id from cost_items where cost_items.alt_devcost_id = ?) and data.value >= -0.000005 and data.value <= 0.000005"
   :
   "delete from data from cost_items where data.cost_li_id = cost_items.id and cost_items.alt_devcost_id = ? and data.value >= -0.000005 and data.value <= 0.000005";
   public static String DEL_DATA_OPCOST_IF_ZERO = Misc.G_DO_ORACLE ? "delete from data where data.cost_li_id in (select cost_items.id from cost_items where cost_items.alt_opcost_id = ?) and data.value >= -0.000005 and data.value <= 0.000005"
   :
   "delete from data from cost_items where data.cost_li_id = cost_items.id and cost_items.alt_opcost_id = ? and data.value >= -0.000005 and data.value <= 0.000005";   
   public static String DEL_DATA_FTE_IF_ZERO = Misc.G_DO_ORACLE ? "delete from data where data.fte_item_id in (select fte_items.id from fte_items where fte_items.alt_fte_id = ?) and data.value >= -0.000005 and data.value <= 0.000005"
   :
   "delete from data from fte_items where data.fte_item_id = fte_items.id and fte_items.alt_fte_id = ? and data.value >= -0.000005 and data.value <= 0.000005";   
   public static String DEL_DATA_NPV_IF_ZERO = Misc.G_DO_ORACLE ? "delete from data where data.npv_id in (select npv.id from npv where npv.alt_combined_id = ?) and data.value >= -0.000005 and data.value <= 0.000005"
   :
   "delete from data from npv where data.npv_id =npv.id and npv.alt_combined_id = ? and data.value >= -0.000005 and data.value <= 0.000005";   
 //below not needed right now but is valid for selective deletes in measure
 //public static String DEL_DATA_MEASURE_IF_ZERO = "delete from measure_data where measure_data.alt_measure_id = ? and  measure_data.val >= -0.000005 and measure_data.val <= 0.000005";   
   public static String DEL_LINE_REV_IF_ZERO = "delete from rev_segs where rev_segs.alt_rev_id = ? and not(exists(select 1 from data where data.rev_seg_id = rev_segs.id))";
   public static String DEL_LINE_DEVCOST_IF_ZERO = "delete from cost_items where cost_items.alt_devcost_id = ? and not(exists(select 1 from data where data.cost_li_id = cost_items.id))";
   public static String DEL_LINE_OPCOST_IF_ZERO = "delete from cost_items where cost_items.alt_opcost_id = ? and not(exists(select 1 from data where data.cost_li_id = cost_items.id))";   
   public static String DEL_LINE_FTE_IF_ZERO = "delete from fte_items where fte_items.alt_fte_id = ? and not(exists(select 1 from data where data.fte_item_id = fte_items.id))";      
   public static String DEL_LINE_NPV_IF_ZERO = "delete from npv where npv.alt_combined_id = ? and not(exists(select 1 from data where data.npv_id = npv.id))";   
   //public static String DEL_LINE_MEASURE_IF_ZERO = "delete from measure_case_index where mes.alt_combined_id = ? and not(exists(select 1 from data where data.npv_id = npv.id))";   public static String DEL_LINE_NPV_IF_ZERO = "delete from npv where npv.alt_combined_id = ? and not(exists(select 1 from data where data.npv_id = npv.id))";      
   
   //added 060807 ... after  merge send to Shyam
   public static String UPDATE_MENU_STATUS = "update projects set menu_template = ? where id = ?";
   public static String UPDATE_OVERRIDE_STATUS = "update projects set is_override = ? where id = ?";
   // not needed public static String SET_NOPRIM_ALT_AT_ALT_LEVEL_GEN = "update alternatives set is_primary=? where prj_id=? and is_primary <> 1 and is_primary=?";
   public static String MARK_LABEL_DELETED = "update labels set is_sys_generated = ? where id = ?";
   
   public static String GET_LABEL_NAME = "select name from labels where id=?";
   public static String GET_WORKSPACE_NAME = "select name from workspaces where id=?";
   public static String GET_PORTRSET_SCENARIO_NAME = Misc.G_DO_ORACLE ? "select port_wksps.name || '.' || port_rset.name from port_rset join port_wksps on port_rset.port_wksp_id = port_wksps.id where port_rset.id = ? " 
                                                                      : "select port_wksps.name + '.' + port_rset.name from port_rset join port_wksps on port_rset.port_wksp_id = port_wksps.id where port_rset.id = ? " ;
   public static String UPDATE_CURRENCY_ID_SNIPPET =  " set currency_id = (case when ?=1 then (select currency from projects, workspaces where workspaces.id = ? and projects.id = workspaces.prj_id) "
                              + "                         else (select def_currency_calc from port_nodes, projects, workspaces where workspaces.id = ? and projects.id = workspaces.prj_id and port_nodes.id = projects.port_node_id) "
                              + "                   end) "
                              + "where id = ? ";
   public static String GET_CURRENCY_DEVCOST = "select currency_id from alt_devcost_model where id = ?";
   public static String GET_CURRENCY_MEASURE = "select currency_id from alt_measures where id = ?";
    // Added Sameer after getting latest 06102007
            public static String GET_USER_MATCH = "select id, name from users where username=? and isactive=1";
            public static String GET_PROJECT_ID_FROM_CODE = "select id from projects where user_given_id = ? and status in (1,2,3,4,5,7) ";
	public static String GET_PROJECT_ID_FROM_EXT_REF =
		"select distinct pj_basics.prj_id from pj_basics, pj_map_items, projects where pj_basics.str_field4 = ? " +
		"and pj_basics.prj_id = pj_map_items.prj_id " +
		"and pj_map_items.isdefault = 1 and pj_map_items.map_type = 1 " +
		"and pj_basics.prj_id = projects.id " +
		"and projects.port_node_id = ? " +
		"and projects.status in (1,2,3,4,5,7) ";
            public static String GET_CURRENCY_LISTS_ID = "select id from currency_lists where month = ? and type = ?";
            public static String DELETE_CURRENCY_RATE_FOR_CURRENCY_ID = "delete from currency_rates where currency_lists_id = ?";
            public static String INSERT_CURRENCY_LISTS = Misc.G_DO_ORACLE ? "insert into currency_lists (id, month, type) values (?, ?, ?)"
            :
            "insert into currency_lists ( month, type) values ( ?, ?)";
            public static String INSERT_CURRENCY_RATES =
                    "insert into currency_rates (currency_lists_id, currency_numeric_code, start_date, end_date, conversion_rate) " +
                    "values (?, ?, ?, ?, ?)";

            public static String PORT_NODES_GET_VALUE_FOR_EXTERNAL_CODE = 
                    "select id, default_reporting_currency, org_type, " +
                    "country_code, consolidation_status from port_nodes where external_code = ?";
            public static String PORT_NODES_GET_ID_FOR_EXTERNAL_CODE =
                    "select id from port_nodes where external_code = ?";
            public static String PORT_NODES_INSERT_ID = Misc.G_DO_ORACLE ?
                    "insert into port_nodes (id, external_code, status) values (?, ?, 1)"
                    :
                    "insert into port_nodes (external_code, status) values (?, 1)";
            public static String PORT_NODES_INSERT_FILELOAD = Misc.G_DO_ORACLE ?
                    "insert into port_nodes (name, id, port_node_id, status, " +
                    "default_reporting_currency, org_type, external_code, country_code, " +
                    "consolidation_status values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    :
                    "insert into port_nodes (name,  port_node_id, status, " +
                    "default_reporting_currency, org_type, external_code, country_code, " +
                    "consolidation_status values ( ?, ?, ?, ?, ?, ?, ?, ?)";
            public static String PORT_NODES_UPDATE_FILELOAD =
                    "update port_nodes set port_node_id = ?, name = ?, default_reporting_currency = ?, " +
                    "org_type = ?, country_code = ?, consolidation_status = ?, full_name = ?, port_node_desc = ? " +
                    "where id = ?";

            public static String PORT_NODES_INITIALIZE_HIERARCHY =
                    "update port_nodes set hier_level = (case when id=1 then 0 else null end ) ";
            public static String PORT_NODES_GET_UNASSIGNED_HIER_LEVEL =
                    "select id from port_nodes where hier_level is null";
            public static String PORT_NODES_SET_HIER_LEVEL =
                    "update port_nodes set hier_level = ? + 1 where hier_level is null " +
                    "and id in (select port_nodes.id from port_nodes p2 where p2.hier_level = ? and " +
                    "p2.id = port_nodes.port_node_id)";

            // end Added Sameer    

			public static String ORG_DEF_SETUP_TOP = "update port_nodes set default_currency=(case when default_currency is null then 0 else default_currency end), " +
								   " default_reporting_currency=(case when default_reporting_currency is null then 0 else default_reporting_currency end) " +
				   " where port_nodes.id = 1 ";


			public static String ORG_DEF_CURR_SETUP = "update port_nodes set def_currency_calc=default_currency, def_rep_currency_calc=default_reporting_currency ";


			/*public static String ORG_DEF_UPDATE_BUD_CURRENCY = "update port_nodes set def_currency_calc=(select def_currency_calc from port_nodes p2 where p2.id = port_nodes.port_node_id) " +
					   " where port_nodes.def_currency_calc is null " +
					   " and port_nodes.port_node_id in (select id from port_nodes p2 where p2.id = port_nodes.port_node_id and p2.def_currency_calc is not null) ";*/
			public static String ORG_DEF_UPDATE_BUD_CURRENCY = " update port_nodes p1 join port_nodes p2 on (p1.port_node_id=p2.id and p2.def_currency_calc is not null and  p1.def_currency_calc is null) set p1.def_currency_calc=p2.def_currency_calc ";
			public static String ORG_DEF_CHECK_BUD_CURRENCY = "select 1 from dual where exists (select 1 from port_nodes where def_currency_calc is null)";


			public static String ORG_DEF_UPDATE_REP_CURRENCY = "update port_nodes set def_rep_currency_calc=(select def_rep_currency_calc from port_nodes p2 where p2.id = port_nodes.port_node_id) " +
						" where port_nodes.def_rep_currency_calc is null " +
						" and port_nodes.port_node_id in (select id from port_nodes p2 where p2.id = port_nodes.port_node_id and p2.def_rep_currency_calc is not null)";
			public static String ORG_DEF_CHECK_REP_CURRENCY = " select 1 from dual where exists (select 1 from port_nodes where def_rep_currency_calc is null)";

			public static String MAP_DELETE_ALL = "delete from prj_portfolio_map";
      
			public static String MAP_INITIALIZE = "insert into prj_portfolio_map (prj_id, port_node_id, par_level,parent_inserted) (select projects.id, projects.port_node_id,1,0 from projects  where projects.status in (1,2,4,7))";
			public static String MAP_INSERT_PARENT = "insert into prj_portfolio_map (prj_id, port_node_id, par_level,parent_inserted) (select prj_portfolio_map.prj_id, port_nodes.port_node_id, prj_portfolio_map.par_level+1,2 " +
							   " from prj_portfolio_map, port_nodes where port_nodes.id = prj_portfolio_map.port_node_id and port_nodes.port_node_id is not null and parent_inserted=0)";
			public static String MAP_UPDATE_FLAG = "update prj_portfolio_map set parent_inserted=(case when parent_inserted = 0 then 1 else 0 end) where (parent_inserted = 0 or parent_inserted=2) ";
			public static String MAP_CHECK_IF_MORE_UPDATES = "select 1 from dual where exists (select 1 from prj_portfolio_map where parent_inserted = 0) ";


			//CHANGED since 061907 merege
			public static String PRIVS_AVAILABLE_AT_PROJECT_LEVEL = " select distinct role_privs.priv_id from user_roles, roles_privs, user_roles_scope " +
		 "where " +
		 "user_1_id = ? " +
		 "and role_privs.role_id = user_roles.role_id " +
		 "and user_roles_scope.user_role_id = user_roles.id " +
		 "and user_roles_scope.prj_id = ? " +
		 "union " +
		 "select role_privs.priv_id from user_roles_scope, user_roles, role_privs, prj_portfolio_map " +
		 "where " +
		 "user_1_id = ? " +
		 "and role_privs.role_id = user_roles.role_id " +
		 "and user_roles_scope.user_role_id = user_roles.id " +
		 "and prj_portfolio_map.prj_id = ? " +
		 "and prj_portfolio_map.port_node_id = user_roles_scope.port_node_id " +
		 "union " +
		 "select distinct role_privs.priv_id from user_roles_scope, user_roles, role_privs " +
		 "where " +
		 "user_1_id = ? " +
		 "and role_privs.role_id = user_roles.role_id " +
		 "and user_roles_scope.user_role_id = user_roles.id and all_scope is not null  ";

			public static String PRIVS_AVAILABLE_AT_PORTFOLIO_LEVEL_1 =
		 "select distinct role_privs.priv_id from user_roles_scope, user_roles, role_privs" +
		 "where " +
		 "user_1_id = ? " +
		 "and role_privs.role_id = user_roles.role_id " +
		 "and user_roles_scope.user_role_id = user_roles.id " +
		 "and user_roles_scope.port_node_id in (";
			public static String PRIVS_AVAILABLE_AT_PORTFOLIO_LEVEL_2 =
		 ") " +
		 "union " +
		 "select distinct role_privs.priv_id from user_roles_scope, user_roles, role_privs " +
		 "where " +
		 "user_1_id = ? " +
		 "and role_privs.role_id = user_roles.role_id " +
		 "and user_roles_scope.user_role_id = user_roles.id and all_scope is not null  ";
			public static String PRIVS_AVAILABLE_AT_GLOBAL_LEVEL =
		 "select distinct role_privs.priv_id from user_roles_scope, user_roles, role_privs " +
		 "where " +
		 "user_1_id = ? " +
		 "and role_privs.role_id = user_roles.role_id " +
		 "and user_roles_scope.user_role_id = user_roles.id and all_scope is not null  ";

			public static String GET_PORT_NODE_FOR_PRJ = "select port_node_id from projects where id=?";
      
      public static String DELETE_CALCULATED_DATA_PARTIAL = Misc.G_DO_ORACLE ? "delete from data where data.cost_li_id in (select cost_items.id from cost_items where alt_opcost_id = ?  "
      :
      "delete from data from cost_items where (data.cost_li_id=cost_items.id and alt_opcost_id = ?  ";
      public static String DELETE_CALCULATED_LINE_PARTIAL = "delete from cost_items where alt_opcost_id = ?  ";
 //071007     
      //public static String GET_FCST_SCOPINGLIST_BY_SCOPE_DIFFDIM = //select distinct measure_case_index.classify2
      // ", measure_data.val_scope from measure_data, measure_case_index where measure_data.alt_measure_id = ? and measure_data.measure_case_index_id = measure_case_index.id and val_scope in (0,1) order by measure_data.val_scope ";

	public static String GET_FCST_SCOPINGLIST_BY_SCOPE_DIFFDIM = //select distinct measure_case_index.classify2
	   ", measure_data.val_scope from measure_data, measure_case_index where measure_data.alt_measure_id = ? and measure_data.measure_case_index_id = measure_case_index.id and val_scope in (0,1) " +
	   " and measure_data.time_val < ? " +
	   " and measure_data.time_val > ? - (case when (measure_data.val_scope = 0) then 105 " +
	   "                                  when (measure_data.val_scope = 1) then 420 " +
	   "                                   end ) " +
	   " order by measure_data.val_scope ";
       
      public static String SET_ACTUAL_CUTOFF = "update projects set actual_cutoff_monthval = ? where projects.id = ?";
      public static String GET_ACTCUTOFF_MONTH = "select actual_cutoff_monthval from projects where projects.id = ?";
      public static String UPDATE_WKF_INITIATOR = "update projects set wkf_init_user = ? where projects.id = ?";
      public static String SET_WKF_REWORK_STATUS = "update projects set wkf_in_rework = ? where projects.id = ?";
      public static String IS_APPROVAL_NEEDED = "select count(*) from projects, workspaces, workflow_status where projects.id = ? and workspaces.prj_id = projects.id and workflow_status.wspace_id = workspaces.id and (not (user_id is null) or user_id > 0) and type_of = 0";
      
      //changes since 062207
      //PORT_NODES_INITIALIZE_HIERARCHY
	  // Sameer 07112007
	  public static String DATA_RECONCILIATION_CHECK =
		  "select port_node_id from data_reconciliation where port_node_id = ? " +
		  "and year_time_id = ? and month_time_id = ? and data_type = ?";
	  public static String DATA_RECONCILIATION_UPDATE =
		  "update data_reconciliation set fixed_assets = ?, grants = ?, disposals = ?, adjustments = ? " +
		  "where port_node_id = ? and year_time_id = ? and month_time_id = ? and data_type = ?";
	public static String DATA_RECONCILIATION_INSERT =
		"insert into data_reconciliation (port_node_id, year_time_id, month_time_id, fixed_assets, " +
	  "grants, disposals, adjustments, data_type, notes, currency, bpm_adj_currency, capex_adj_currency) " +
	  "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	  public static String DATA_RECONCILIATION_UPDATE_ADJUSTMENT =
		  "update data_reconciliation set adjustments = ?, notes = ? where port_node_id = ? and year_time_id = ? " +
		  "and month_time_id = ? and data_type = ?";

	// TODO: Remove after testing YTD currency
	//public static String DATA_RECONCILIATION_GET =
	//    "select sum(fixed_assets * intelli.getPropIncludedSimpleCurrency(2, month_time_id, month_time_id, " +
	//              "2, 1, data_reconciliation.currency, ?, ?)), " +
	//    "sum(grants * intelli.getPropIncludedSimpleCurrency(2, month_time_id, month_time_id, " +
	//          "2, 1, data_reconciliation.currency, ?, ?)), " +
	//    "sum(disposals * intelli.getPropIncludedSimpleCurrency(2, month_time_id, month_time_id, " +
	//          "2, 1, data_reconciliation.currency, ?, ?)), " +
	//    "sum(adjustments * intelli.getPropIncludedSimpleCurrency(2, month_time_id, month_time_id, " +
	//          "2, 1, data_reconciliation.bpm_adj_currency, ?, ?)) " +
	//    "from data_reconciliation " +
	//    "join port_nodes leaf on (data_reconciliation.port_node_id = leaf.id), " +
	//    "port_nodes anc where anc.id = ? " +
	//    "and leaf.lhs_number >= anc.lhs_number " +
	//    "and leaf.rhs_number <= anc.rhs_number " +
	//    "and not exists " +
	//    "( " +
	//    "select 1 from data_reconciliation ascdat join port_nodes ascdatnode on " +
	//    "(ascdatnode.id = ascdat.port_node_id) " +
	//    "and ascdatnode.lhs_number < leaf.lhs_number " +
	//    "and ascdatnode.rhs_number > leaf.rhs_number " +
	//    "and anc.lhs_number <= ascdatnode.lhs_number " +
	//    "and anc.rhs_number >= ascdatnode.rhs_number " +
	//    ") " +
	//    "and year_time_id = ? and data_type = ? and month_time_id in ";

	public static String DATA_RECONCILIATION_GET =
		"select sum(bycurr.fa * intelli.getYTDCurrencyConversion(bycurr.cu, ?, byCurr.ti+35)), " +
		"sum(bycurr.gr * intelli.getYTDCurrencyConversion(bycurr.cu, ?, byCurr.ti+35)), " +
		"sum(bycurr.dis * intelli.getYTDCurrencyConversion(bycurr.cu, ?, byCurr.ti+35)), " +
		"sum(bycurr.adj * intelli.getYTDCurrencyConversion(bycurr.cua, ?, byCurr.ti+35)) " +
		"from ( " +
		"	select sum(fixed_assets * intelli.getPropIncluded(2, month_time_id, month_time_id, " +
		"				  2, 1)) fa, " +
		"	sum(grants * intelli.getPropIncluded(2, month_time_id, month_time_id, " +
		"		  2, 1)) gr, " +
		"	sum(disposals * intelli.getPropIncluded(2, month_time_id, month_time_id, " +
		"		  2, 1)) dis, " +
		"	sum(adjustments * intelli.getPropIncluded(2, month_time_id, month_time_id, " +
		"		  2, 1)) adj, currency cu, bpm_adj_currency cua, (?) ti " +
		"	from data_reconciliation " +
		"	join port_nodes leaf on (data_reconciliation.port_node_id = leaf.id), " +
		"	port_nodes anc where anc.id = ? " +
		"	and leaf.lhs_number >= anc.lhs_number " +
		"	and leaf.rhs_number <= anc.rhs_number " +
		"	and not exists " +
		"	( " +
		"	select 1 from data_reconciliation ascdat join port_nodes ascdatnode on " +
		"	(ascdatnode.id = ascdat.port_node_id) where 1=1 " +
		"	and ascdatnode.lhs_number < leaf.lhs_number " +
		"	and ascdatnode.rhs_number > leaf.rhs_number " +
		"	and anc.lhs_number <= ascdatnode.lhs_number " +
		"	and anc.rhs_number >= ascdatnode.rhs_number " +
		"	) " +
		"	and year_time_id = ? and data_type = ? and month_time_id >= ? and month_time_id <= ? " +
		"	group by data_reconciliation.currency, data_reconciliation.bpm_adj_currency " +
		") bycurr ";

	//public static String DATA_RECONCILIATION_ALL_MONTHS_GET =
	//    "select month_time_id, " +
	//    "sum(fixed_assets * intelli.getPropIncludedSimpleCurrency(2, month_time_id, month_time_id, " +
	//              "2, 1, data_reconciliation.currency, ?, ?)), " +
	//    "sum(grants * intelli.getPropIncludedSimpleCurrency(2, month_time_id, month_time_id, " +
	//              "2, 1, data_reconciliation.currency, ?, ?)), " +
	//    "sum(disposals * intelli.getPropIncludedSimpleCurrency(2, month_time_id, month_time_id, " +
	//              "2, 1, data_reconciliation.currency, ?, ?)), " +
	//    "sum(adjustments * intelli.getPropIncludedSimpleCurrency(2, month_time_id, month_time_id, " +
	//              "2, 1, data_reconciliation.bpm_adj_currency, ?, ?)) " +
	//    "from data_reconciliation " +
	//    "join port_nodes leaf on (data_reconciliation.port_node_id = leaf.id), " +
	//    "port_nodes anc where anc.id = ? " +
	//    "and leaf.lhs_number >= anc.lhs_number " +
	//    "and leaf.rhs_number <= anc.rhs_number " +
	//    "and not exists " +
	//    "( " +
	//    "select 1 from data_reconciliation ascdat join port_nodes ascdatnode on " +
	//    "(ascdatnode.id = ascdat.port_node_id) " +
	//    "and ascdatnode.lhs_number < leaf.lhs_number " +
	//    "and ascdatnode.rhs_number > leaf.rhs_number " +
	//    "and anc.lhs_number <= ascdatnode.lhs_number " +
	//    "and anc.rhs_number >= ascdatnode.rhs_number " +
	//    ") " +
	//    "and year_time_id = ? and data_type = ? and month_time_id >= ? and month_time_id <= ? " +
	//    "group by month_time_id order by month_time_id";

	public static String DATA_RECONCILIATION_ALL_MONTHS_GET =
		"select " +
		"sum( cumm_by_curr.fa * intelli.getYTDCurrencyConversion(cumm_by_curr.cu, ?, cumm_by_curr.ti+35)), " +
		"sum( cumm_by_curr.gr * intelli.getYTDCurrencyConversion(cumm_by_curr.cu, ?, cumm_by_curr.ti+35)), " +
		"sum( cumm_by_curr.dis * intelli.getYTDCurrencyConversion(cumm_by_curr.cu, ?, cumm_by_curr.ti+35)), " +
		"sum( cumm_by_curr.adj * intelli.getYTDCurrencyConversion(cumm_by_curr.cua, ?, cumm_by_curr.ti+35)), " +
		"cumm_by_curr.ti " +
		"from " +
		"( " +
		"	select " +
		"	sum(detailed_by_curr.fa) fa, " +
		"	sum(detailed_by_curr.gr) gr, " +
		"	sum(detailed_by_curr.dis) dis, " +
		"	sum(detailed_by_curr.adj) adj, " +
		"	detailed_by_curr.cu cu, detailed_by_curr.cua cua, mon_for_cumm.time_id ti " +
		"	from month_timeid mon_for_cumm, " +
		"	( " +
		"		select " +
		"		sum(fixed_assets * intelli.getPropIncluded(2, month_time_id, month_time_id, " +
		"		2, 1)) fa, " +
		"		sum(grants * intelli.getPropIncluded(2, month_time_id, month_time_id, " +
		"		2, 1)) gr, " +
		"		sum(disposals * intelli.getPropIncluded(2, month_time_id, month_time_id, " +
		"		2, 1)) dis, " +
		"		sum(adjustments * intelli.getPropIncluded(2, month_time_id, month_time_id, " +
		"		2, 1)) adj, " +
		"		currency cu, bpm_adj_currency cua, month_time_id ti " +
		"		from data_reconciliation " +
		"		join port_nodes leaf on (data_reconciliation.port_node_id = leaf.id), " +
		"		port_nodes anc where anc.id = ? " +
		"		and leaf.lhs_number >= anc.lhs_number " +
		"		and leaf.rhs_number <= anc.rhs_number " +
		"		and not exists " +
		"		( " +
		"		select 1 from data_reconciliation ascdat join port_nodes ascdatnode on " +
		"		(ascdatnode.id = ascdat.port_node_id) where 1=1 " +
		"		and ascdatnode.lhs_number < leaf.lhs_number " +
		"		and ascdatnode.rhs_number > leaf.rhs_number " +
		"		and anc.lhs_number <= ascdatnode.lhs_number " +
		"		and anc.rhs_number >= ascdatnode.rhs_number " +
		"		) " +
		"		and year_time_id = ? " +
		"		and data_type = ? " +
		"		and month_time_id >= ? " +
		"		and month_time_id <= ? " +
		"		group by currency, bpm_adj_currency, month_time_id " +
		"	) detailed_by_curr " +
		"	where " +
		"	mon_for_cumm.time_id >= (?) " +
		"	and mon_for_cumm.time_id < (?) " +
    "	and detailed_by_curr.ti <= mon_for_cumm.time_id " +
		"	group by detailed_by_curr.cu, detailed_by_curr.cua, mon_for_cumm.time_id " +
		") cumm_by_curr " +
		"group by cumm_by_curr.ti ";
	  
	public static String DATA_RECONCILIATION_GET_ADJUSTMENT =
		  		"select sum(bycurr.adj * intelli.getYTDCurrencyConversion(bycurr.cu, ?, byCurr.ti+35)) " +
		"from ( " +
		"	select sum(adjustments * intelli.getPropIncluded(2, month_time_id, month_time_id, " +
		"					2, 1)) adj, capex_adj_currency cu, (?) ti " +
		"	from data_reconciliation, port_nodes " +
		  "where data_reconciliation.port_node_id = ? " +
		  "and port_nodes.id = data_reconciliation.port_node_id " +
		  "and year_time_id = ? " +
		"	and data_type = ? and month_time_id >= ? and month_time_id <= ? " +
		"	group by data_reconciliation.capex_adj_currency " +
		") bycurr ";

		
		//TODO: Remove after testing YTD currency
	  public static String RECONCILIATION_CAPEX_DATA_PERIOD_GET =
		  "select sum(measure_data.val * intelli.getPropIncludedSimpleCurrency(" +
						  "measure_data.val_scope, measure_data.time_val, time_id, 2, measure_data.val_dur, " +
						  "alt_measures.currency_id, ?, ?)" +
					  ") " +
		  "from projects, measure_map_items, alternatives, alt_measures, measure_data, measure_case_index, " +
		  "month_timeid, prj_portfolio_map " +
		  "where " +
		  "prj_portfolio_map.port_node_id = ? " +
		  "and projects.id = prj_portfolio_map.prj_id " +
		  "and projects.status in (2) " +
		  "and alternatives.prj_id = projects.id " +
		  "and alternatives.is_primary = 1 " +
		  "and measure_map_items.alt_id = alternatives.id " +
		  "and measure_map_items.isdefault = 1 " +
		  "and measure_map_items.measure_id = ? " +
		  "and measure_map_items.map_type in (1) " +
		  "and alt_measures.id = measure_map_items.alt_measure_id " +
		  "and time_id >= ? " +
		  "and time_id <= ? " +
		  "and measure_data.alt_measure_id = measure_map_items.alt_measure_id " +
		  "and measure_case_index.id = measure_data.measure_case_index_id " +
		  "and time_id >= cast((measure_data.time_val/35) as int)*35 " +
		  "and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 " +
											  "when (measure_data.val_scope=1) then 420 " +
											  "when (measure_data.val_scope=2) then 35 " +
											  "when (measure_data.val_scope=3) then 12 " +
											  "when (measure_data.val_scope=5) then measure_data.val_dur*1.26 " +
											  "else 1 " +
											  "end) ";

		// TODO; Remove after testing YTD currency
	  //public static String RECONCILIATION_CAPEX_DATA_ALL_MONTHS_GET_PART1 =
	  //    "select time_id, sum(measure_data.val * intelli.getPropIncludedSimpleCurrency(" +
	  //                    "measure_data.val_scope, measure_data.time_val, time_id, 2, measure_data.val_dur, " +
	  //                    "alt_measures.currency_id, ?, ?)" +
	  //                ") " +
	  //    "from projects, measure_map_items, alternatives, alt_measures, measure_data, measure_case_index, " +
	  //    "month_timeid, prj_portfolio_map " +
	  //    "where " +
	  //    "prj_portfolio_map.port_node_id = ? " +
	  //    "and projects.id = prj_portfolio_map.prj_id " +
	  //    "and projects.status in (2) " +
	  //    "and alternatives.prj_id = projects.id " +
	  //    "and alternatives.is_primary = 1 " +
	  //    "and measure_map_items.alt_id = alternatives.id " +
	  //    "and measure_map_items.isdefault = 1 " +
	  //    "and measure_map_items.measure_id = ? " +
	  //    "and measure_map_items.map_type in (1) " +
	  //    "and alt_measures.id = measure_map_items.alt_measure_id " +
	  //    "and time_id >= ? " +
	  //    "and time_id <= ? " +
	  //    "and measure_data.alt_measure_id = measure_map_items.alt_measure_id " +
	  //    "and measure_case_index.id = measure_data.measure_case_index_id " +
	  //    "and time_id >= cast((measure_data.time_val/35) as int)*35 " +
	  //    "and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 " +
	  //                                        "when (measure_data.val_scope=1) then 420 " +
	  //                                        "when (measure_data.val_scope=2) then 35 " +
	  //                                        "when (measure_data.val_scope=3) then 12 " +
	  //                                        "when (measure_data.val_scope=5) then measure_data.val_dur*1.26 " +
	  //                                        "else 1 " +
	  //                                        "end) ";
	  //public static String RECONCILIATION_CAPEX_DATA_ALL_MONTHS_GET_PART2 =
	  //    "group by time_id order by time_id ";
      
    //public static String GET_CURRENCY_RATES_1 = "select cl.type, currency_numeric_code, start_date, end_date, conversion_rate from (select max(id) i1, type from currency_lists group by type) cl, currency_rates where currency_rates.currency_lists_id = cl.i1 and end_date >= ? ";//order by cl.type, start_date";
    //" select id, month, type from currency_lists, (select max(month) m , type t from currency_lists where month <= ? group by type) sel where sel.m = currency_lists.month and sel.t = currency_lists.type ";
    public static String GET_CURRENCY_RATES_1 = "select cl.type, currency_numeric_code, start_date, end_date, conversion_rate from (select id i1, month, type from currency_lists, (select max(month) m , type t from currency_lists where month <= ? group by type) sel where sel.m = currency_lists.month and sel.t = currency_lists.type) cl, currency_rates where currency_rates.currency_lists_id = cl.i1 and end_date >= ? ";//order by cl.type, start_date";
    
    public static String GET_OVERRIDE_ROLES = "select wf_role, prj_portfolio_map.prj_id from prj_portfolio_map, workflow_user where "+ ////rajeev_090407
           " workflow_user.user_id = ? and workflow_user.is_override = 1 and workflow_user.port_node_id = prj_portfolio_map.port_node_id "; 
           // " and prj_portfolio_map.prj_id in ";

	// Sameer August 2007

	public static String GET_CURRENCY_RATES =
		"select currency_numeric_code, conversion_rate, start_date, end_date from currency_rates, currency_lists " +
		"where currency_lists.id = currency_rates.currency_lists_id " +
		"and currency_lists.month = ? " +
		"and currency_lists.type = ? "+
    "and (currency_rates.currency_numeric_code = ? or ? is null) "+
    "order by currency_numeric_code, start_date ";
/*
	public static String GET_MEETINGS_LIST =
		"select id, date, status, notes from meeting_details order by date desc";
	public static String MEETING_DETAILS_INSERT =
		Misc.G_DO_ORACLE ?
		"insert into meeting_details values (?, ?, ?, ?)"
		:
		"insert into meeting_details values (?, ?, ?)";
	public static String MEETING_DETAILS_UPDATE =
		"update meeting_details set date = ?, status = ?, notes = ? where id = ?";
	public static String MEETING_DETAILS_DELETE =
		"delete from meeting_details where id = ?";
*/
	// End Sameer August 2007
  //rajeev Sep 10 '07
  public static String GET_CURRENCY_LISTS = //"select max(id), type from currency_lists group by type";
   " select id, month, type from currency_lists, (select max(month) m , type t from currency_lists where month <= ? group by type) sel where sel.m = currency_lists.month and sel.t = currency_lists.type ";
  public static String ADD_PRE_CURRENCY_ENTRIES = 
" insert into currency_rates (currency_lists_id, currency_numeric_code, start_date, end_date, conversion_rate) "+
" ( "+
" select sl.currency_lists_id, sl.currency_numeric_code, ?, sl.mid, conversion_rate from "+
" currency_rates, "+
" ( "+
" select min(start_date) mid, currency_lists_id, currency_numeric_code from currency_rates  "+
" where  "+
" start_date > ?  "+
" and (currency_lists_id = ?) "+
" group by currency_lists_id, currency_numeric_code "+
" ) sl "+
" where sl.mid = currency_rates.start_date "+
" and sl.currency_numeric_code = currency_rates.currency_numeric_code "+
" and sl.currency_lists_id = currency_rates.currency_lists_id "+
" ) ";

  public static String ADD_POST_CURRENCY_ENTRIES = 
" insert into currency_rates (currency_lists_id, currency_numeric_code, start_date, end_date, conversion_rate) "+
" ( "+
" select sl.currency_lists_id, sl.currency_numeric_code, sl.mid, ?, conversion_rate from "+
" currency_rates, "+
" ( "+
" select max(end_date) mid, currency_lists_id, currency_numeric_code from currency_rates  "+
" where  "+
" end_date < ?  "+
" and (currency_lists_id = ?) "+
" group by currency_lists_id, currency_numeric_code "+
" ) sl "+
" where sl.mid = currency_rates.end_date "+
" and sl.currency_numeric_code = currency_rates.currency_numeric_code "+
" and sl.currency_lists_id = currency_rates.currency_lists_id "+
" ) ";
	
	// Sameer sep 07
	
	public static String GET_PENDING_APPROVALS_FOR_USER =
		"select projects.id, projects.name, projects.curr_workflow_step, workflow_status.approver_role, " +
		"workflow_status.wspace_id, projects.port_node_id, workflow_status.start_date " +
		"from projects, workspaces, workflow_status, prj_portfolio_map " +
		"where workflow_status.type_of = 0 " +
		"and (workflow_status.user_id = ? or ? is null) " +
		"and workflow_status.wspace_id = workspaces.id " +
		"and workspaces.prj_id = projects.id " +
		"and projects.status = 2 " +
		"and projects.id = prj_portfolio_map.prj_id " +
		"and prj_portfolio_map.port_node_id = ? " +
		"order by projects.name ";
		
	public static String REASSIGN_PENDING_APPROVAL_FOR_USER =
		"update workflow_status set user_id = ? " +
		"where user_id = ? and type_of = 0 and wspace_id = ? " +
		"and wf_step = ? and approver_role = ?";
		
	public static String GET_APPROVING_ROLES_FOR_USER =
		"select wf_role, classify1, classify2, classify3, classify4, classify5, thresh, is_override " +
		"from workflow_user where user_id = ? and port_node_id = ? order by wf_role";
		
	public static String REASSIGN_APPROVING_ROLE_FOR_USER =
		"update workflow_user set user_id = ?, classify1 = ?, classify2 = ?, classify3 = ?, " +
		"classify4 = ?, classify5 = ?, thresh = ? " +
		"where user_id = ? and wf_role = ? and port_node_id = ?";
		
	public static String GET_USER_ROLES_VIEW_GLOBAL =
		"select users.id, users.name, role.name from users, user_roles, role " +
		"where users.id = user_roles.user_1_id and user_roles.role_id = role.id " +
		"and role.scope = ? order by users.name ";
		
	public static String GET_USER_ROLES_VIEW_PORTFOLIO_SPECIFIC =
		"select users.id, users.name, user_roles_scope.port_node_id, port_nodes.name portnodename, role.name " +
		"from users, user_roles, role, user_roles_scope " +
		"left outer join port_nodes on user_roles_scope.port_node_id = port_nodes.id " +
		"where users.id = user_roles.user_1_id " +
		"and user_roles.id = user_roles_scope.user_role_id " +
		"and user_roles.role_id = role.id " +
		"and role.scope = ? " +
		"order by users.name, user_roles_scope.port_node_id ";
	
	// End Sameer sep 07

	//rajeev 100907
	public static String GET_WORKFLOW_STATE_BY_ID = " select curr_workflow_step from projects where id = ?";
  
  
  public static String CLEAN_AUTO_PORT_BY_CHANGE_LIST = Misc.G_DO_ORACLE ?
  "delete from port_results where ((port_results.port_rs_id, port_results.prj_id) in (select port_results.port_rs_id, port_results.prj_id from "+
  "port_wksps, port_rset, curr_change_list, port_results where  port_wksps.map_type in (1,2,7) and port_rset.port_wksp_id = port_wksps.id and port_results.port_rs_id = port_rset.id and port_rset.is_auto_updateable = 1 and port_results.prj_id = curr_change_list.prj_id)) "
  :
  "delete from port_results from port_wksps, port_rset, curr_change_list where  port_wksps.map_type in (1,2,7) and port_rset.port_wksp_id = port_wksps.id and port_results.port_rs_id = port_rset.id and port_rset.is_auto_updateable = 1 and port_results.prj_id = curr_change_list.prj_id ";
  
  public static String CLEAN_AUTO_PORT_MEASURE_BY_CHANGE_LIST = Misc.G_DO_ORACLE ?
  "delete from port_results_measure where ((port_results_measure.port_rs_id, port_results_measure.prj_id) in (select port_results_measure.port_rs_id, port_results_measure.prj_id from "+
  "port_wksps, port_rset, curr_change_list, port_results_measure where  port_wksps.map_type in (1,2,7) and port_rset.port_wksp_id = port_wksps.id and port_results_measure.port_rs_id = port_rset.id and port_rset.is_auto_updateable = 1 and port_results_measure.prj_id = curr_change_list.prj_id)) "
  :
  "delete from port_results_measure from port_wksps, port_rset, curr_change_list where  port_wksps.map_type in (1,2,7) and port_rset.port_wksp_id = port_wksps.id and port_results_measure.port_rs_id = port_rset.id and port_rset.is_auto_updateable = 1 and port_results_measure.prj_id = curr_change_list.prj_id ";
  
  public static String PORT_SIMPLE_INSERT_CHANGE = 
  Misc.G_DO_ORACLE ?
" insert into port_results( "+
" id, "+
" port_rs_id, alt_id, prj_id, fund_status "+
" ,ver_alt_mstone_id "+
" ,ver_alt_basic_id "+
" ,ver_alt_profile_id "+
" ,ver_alt_model_id "+
" ,ver_prj_basic_id "+
" ,ver_alt_work_id "+
" ,ver_alt_fte_id "+
" ,ver_alt_devcost_id "+
" ,ver_alt_opcost_id "+
" ,ver_alt_combined_id "+
" ,is_default_alt "+
" ,ver_alt_rev_id "+
" ,tot_delay_mon "+
" ,ver_alt_rating_id "+
" ) "+
" (select seq_detailed_port_rsets.nextval, port_rset.id, alt_map_items.alt_id, pj_map_items.prj_id, 1 "+
" ,alt_date_id "+
" ,alt_basic_id "+
" ,alt_profil_id "+
" ,alt_model_id "+
" ,pj_basic_id "+
" ,alt_work_id "+
" ,alt_fte_id "+
" ,alt_devcost_id "+
" ,alt_opcost_id "+
" ,alt_combined_id "+
" ,alternatives.is_primary "+
" ,alt_rev_id "+
" ,0 "+
" ,alt_rating_id "+
" from "+
" port_wksps, port_rset, curr_change_list, alternatives, projects, alt_map_items, pj_map_items "+
" where "+
" port_wksps.map_type in (1,2,7) "+
" and port_wksps.id = port_rset.port_wksp_id "+
" and port_rset.is_auto_updateable =  1 "+
" and curr_change_list.prj_id = alternatives.prj_id "+
" and projects.id = alternatives.prj_id "+
" and projects.status in (2) "+
" and pj_map_items.wspace_id = alt_map_items.wspace_id "+
" and alt_map_items.alt_id = alternatives.id "+
" and alt_map_items.map_type = port_wksps.map_type "+
" and alt_map_items.isdefault = 1 "+
" and pj_map_items.prj_id = projects.id "+
" and pj_map_items.isdefault = 1 "+
" and pj_map_items.map_type = alt_map_items.map_type "+
" )   "
  
  :
" insert into port_results( "+
" port_rs_id, alt_id, prj_id, fund_status "+
" ,ver_alt_mstone_id "+
" ,ver_alt_basic_id "+
" ,ver_alt_profile_id "+
" ,ver_alt_model_id "+
" ,ver_prj_basic_id "+
" ,ver_alt_work_id "+
" ,ver_alt_fte_id "+
" ,ver_alt_devcost_id "+
" ,ver_alt_opcost_id "+
" ,ver_alt_combined_id "+
" ,is_default_alt "+
" ,ver_alt_rev_id "+
" ,tot_delay_mon "+
" ,ver_alt_rating_id "+
" ) "+
" (select port_rset.id, alt_map_items.alt_id, pj_map_items.prj_id, 1 "+
" ,alt_date_id "+
" ,alt_basic_id "+
" ,alt_profil_id "+
" ,alt_model_id "+
" ,pj_basic_id "+
" ,alt_work_id "+
" ,alt_fte_id "+
" ,alt_devcost_id "+
" ,alt_opcost_id "+
" ,alt_combined_id "+
" ,alternatives.is_primary "+
" ,alt_rev_id "+
" ,0 "+
" ,alt_rating_id "+
" from "+
" port_wksps, port_rset, curr_change_list, alternatives, projects, alt_map_items, pj_map_items "+
" where "+
" port_wksps.map_type in (1,2,7) "+
" and port_wksps.id = port_rset.port_wksp_id "+
" and port_rset.is_auto_updateable =  1 "+
" and curr_change_list.prj_id = alternatives.prj_id "+
" and projects.id = alternatives.prj_id "+
" and projects.status in (2) "+
" and pj_map_items.wspace_id = alt_map_items.wspace_id "+
" and alt_map_items.alt_id = alternatives.id "+
" and alt_map_items.map_type = port_wksps.map_type "+
" and alt_map_items.isdefault = 1 "+
" and pj_map_items.prj_id = projects.id "+
" and pj_map_items.isdefault = 1 "+
" and pj_map_items.map_type = alt_map_items.map_type "+
" )   ";
  
  public static String PORT_SIMPLE_UPDATE_MEASURE = 
"  insert into port_results_measure( "+
"  port_rs_id, alt_id, prj_id "+
"  ,measure_id "+
"  ,alt_measure_id "+
"  ) "+
"  (select port_rset.id, alt_id, alternatives.prj_id "+
"  ,measure_id "+
"  ,alt_measure_id "+
"  from "+
"  port_wksps, port_rset, curr_change_list, alternatives, projects, measure_map_items "+
"  where "+
"  port_wksps.map_type in (1,2,7) "+
"  and port_wksps.id = port_rset.port_wksp_id "+
"  and port_rset.is_auto_updateable = 1 "+
"  and curr_change_list.prj_id = alternatives.prj_id "+
"  and projects.id = alternatives.prj_id "+
"  and projects.status in (2) "+
"  and measure_map_items.alt_id = alternatives.id "+
"  and measure_map_items.map_type = port_wksps.map_type "+
"  and measure_map_items.isdefault = 1 "+
"  ) ";
//rajeev 102507
   public static String DELETE_CURRENCY_LISTS = "delete from currency_lists where id = ?";
   public static String COPY_CURRENCY_RATES = "insert into currency_rates(currency_lists_id, currency_numeric_code, start_date, end_date, conversion_rate) "+
" (select ?, currency_numeric_code, start_date, end_date, conversion_rate from currency_rates where currency_lists_id=?) ";

  public static String GET_OLD_MEETING_ID = "select id from meeting_details where  status in (2,3) order by status asc, meeting_date desc";
  public static String GET_NEW_MEETING_ID = "select id from meeting_details where  status in (0,1) order by status desc, meeting_date asc";
  public static String GET_MEETING_DETAIL = "select id, meeting_date, status, notes, str_field1, str_field2, int_field1, int_field2 from meeting_details where id = ?";
  public static String GET_DOCUMENTS_FOR_MEETING = "select file_names.file_name_id, meeting_file_type, file_names.name, file_names.extension, file_names.original_name from meeting_files, file_names where meeting_files.meeting_id = ? and file_names.file_name_id = meeting_files.file_name_id order by meeting_file_type, file_names.file_name_id ";
  public static String GET_PROJECT_LIST_FOR_MEETING = "select projects.id from projects, workflow_status, workspaces where workspaces.id = workflow_status.wspace_id and projects.id = workspaces.prj_id and (workflow_status.meeting_id = ? or projects.curr_workflow_step = ?)";
  public static String CREATE_MEETING = Misc.G_DO_ORACLE ? "insert into meeting_details (id, status, meeting_date, created_by, create_date) values (?,?,?, ?, sysdate)" : "insert into meeting_details (status, meeting_date, created_by, create_date) values (?,?,?, getDate())";
  public static String UPDATE_MEETING_DETAILS = Misc.G_DO_ORACLE ? "update meeting_details set status=?, meeting_date = ?, notes = ?, str_field1 = ?, update_date = sysdate where id = ? " 
                                                                 : "update meeting_details set status=?, meeting_date = ?, notes = ?, str_field1 = ?, update_date = getDate() where id = ? " ;
  public static String DELETE_MEETING_FILES = "delete from meeting_files where meeting_id = ?";
  public static String CREATE_MEETING_FILES = "insert into meeting_files (meeting_id, file_name_id, meeting_file_type) values (?,?,?)";
  public static String CLEAR_ASSIGNMENT_IN_CURR_MEETING = "update workflow_status set meeting_id = null where wf_step=? and meeting_id = ?";
  public static String SET_ASSIGNMENT_IN_CURR_MEETING =  Misc.G_DO_ORACLE ? "update workflow_status set meeting_id = ? where wf_step = ? and seq = ? and workflow_status.wspace_id in (select workspaces.id from workspaces where workspaces.prj_id = ?)"
  :
  "update workflow_status set meeting_id = ? from workspaces where wf_step = ? and seq = ? and workflow_status.wspace_id =workspaces.id and workspaces.prj_id = ?";
  public static String GET_MAX_SEQ_FOR_MEETING = "select max(seq) from workflow_status, workspaces where workflow_status.wspace_id = workspaces.id and wf_step = ? and workspaces.prj_id = ? ";
  public static String GET_MEETING_LIST =
		"select id, meeting_date, status, notes from meeting_details order by meeting_date desc";
  public static String DELETE_MEETING = "delete from meeting_details where id = ?";
  public static String GET_PRJ_IDETC_FROM_ORDER_ID = "select orders.prj_id, alternatives.id, pj_map_items.wspace_id from orders, alternatives, pj_map_items where orders.id = ? "+
  " and alternatives.prj_id = orders.prj_id and alternatives.is_primary = 1 and pj_map_items.prj_id = alternatives.prj_id and (pj_map_items.map_type = 1 or pj_map_items.map_type = 4) "+
  " and (pj_map_items.isdefault=1) order by pj_map_items.map_type asc, pj_map_items.date_created desc";
  public static String GET_PRJ_IDETC_FROM_ALTERNATIVE_ID = "select alternatives.prj_id, alternatives.id, pj_map_items.wspace_id from alternatives, pj_map_items where alternatives.id = ? "+
  " and pj_map_items.prj_id = alternatives.prj_id and (pj_map_items.map_type = 1 or pj_map_items.map_type = 4) "+
  " and (pj_map_items.isdefault=1) order by pj_map_items.map_type asc, pj_map_items.date_created desc";
  public static String GET_PRJ_IDETC_FROM_WORKSPACE_ID = "select alternatives.prj_id, alternatives.id, workspaces.id from alternatives, workspaces where workspaces.id = ? "+
  " and workspaces.prj_id = alternatives.prj_id and alternatives.is_primary = 1 ";
  public static String INSERT_PRJ_MULTI_ATTRIBUTE_EXTENDED = "insert into prj_multi_attrib (prj_id, attrib_id, int_val, date_val, double_val, str_val, instance_id, row_num, created_on, created_by, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?,?, ? ,? ,?, ?,?,?,?)";  
  public static String INSERT_ORDER_MULTI_ATTRIBUTE_EXTENDED = "insert into order_multi_attrib (order_id, attrib_id, int_val, date_val, double_val, str_val, instance_id, row_num, created_on, created_by, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?,?, ? ,? ,?, ?,?,?,?)";  
  //Merge from sameer 112707 - 1st
  	public static String GET_SUPPLIERS_FOR_ORDER = 
		"select o_s.supplier_id, suppliers.name, o_s.bid_status, o_s.numeric_field1, " +
		"o_s.numeric_field2, o_s.numeric_field4, o_s.framework_agreement, o_s.numeric_field3 " +
		"from suppliers, orders_suppliers o_s " + 
		"where o_s.order_id = ? " +
		"and o_s.supplier_id = suppliers.id " +
		"order by suppliers.name ";
		
	public static String GET_SUPPLIERS_LIST =
		"select id, name from suppliers where is_active = 1 order by name ";
		
	public static String GET_PAYMENT_TERMS_FOR_ORDER =
		"select str_field7, str_field8 from order_details where order_id = ? ";
		
	public static String GET_DOCUMENTS_FOR_ORDER =
		"select o_f.file_name_id, f_n.name, f_n.original_name, f_n.extension " +
		"from orders_files o_f, file_names f_n where o_f.order_id = ? and o_f.order_file_type = ? " +
		"order by f_n.original_name ";
		
	public static String GET_FUNCTIONAL_BREAKDOWN_FOR_ORDER =
		"select cio.cost_item_id, cb.name, " +
		"(select sum(cid1.value) from pur_cost_item_data cid1 where cid1.cost_type = 0 and cio.cost_item_id = cid1.cost_item_id ) bud_cost, " +
		"(select sum(cid2.value) from pur_cost_item_data cid2 where cid2.cost_type = 1 and cio.cost_item_id = cid2.cost_item_id) ord_cost, " +
		"sum(cio.value) " +
		"from pur_cost_items_orders cio, pur_cost_breakdown cb, pur_cost_items ci " +
		"where cio.order_id = ? " +
		"and cio.cost_item_id = ci.id " +
		"and ci.cbs_id = cb.id " +
		"group by cio.cost_item_id, cb.name ";
		
	public static String GET_ORDERS_SUMMARY1 =
		"select prj.name, prj.id, ord.id, ord.order_number, ord.order_status, " +
		"o_d.description, o_d.int_field1, u.name, o_d.int_field5, o_d.numeric_field1, " +
		"o_d.date_field1, o_d.date_field2, o_d.date_field4, o_d.str_field3, " +
		"o_d.str_field1, s.id, s.name, o_s.numeric_field1, o_s.numeric_field2 " +
		"from projects prj, orders ord, order_details o_d, users u, suppliers s, orders_suppliers o_s " +
		"where prj.id = ord.prj_id " +
		"and ord.id = o_d.order_id " + 
		"and o_d.int_field4 = u.id " +
		"and o_s.order_id = ord.id " +
		"and o_s.supplier_id = s.id " +
		"order by prj.name, ord.order_number ";

	public static String GET_ORDERS_SUMMARY =
		"select prj.name, prj.id, ord.id, ord.order_number, ord.order_status, " +
		"o_d.description, o_d.int_field1, u.name, o_d.int_field5, o_d.numeric_field1, " +
		"o_d.date_field1, o_d.date_field2, o_d.date_field4, o_d.str_field3, " +
		"o_d.str_field1, s.id, s.name, o_s.numeric_field1, o_s.numeric_field2 " +
		"from orders ord " +
		"inner join projects prj on prj.id = ord.prj_id " +
		"inner join order_details o_d on o_d.order_id = ord.id " +
		"left outer join orders_suppliers o_s on o_s.order_id = ord.id " +
		"left outer join suppliers s on s.id = o_s.supplier_id " +
		"left outer join users u on u.id = o_d.int_field4 " +
		"order by prj.name, ord.order_number ";

	public static String GET_ORDERS_FOR_PROJECT =
		"select prj.name, prj.id, ord.id, ord.order_number, ord.order_status, " +
		"o_d.description, o_d.int_field1, u.name, o_d.int_field5, o_d.numeric_field1, " +
		"o_d.date_field1, o_d.date_field2, o_d.date_field4, o_d.str_field3, " +
		"o_d.str_field1, s.id, s.name, o_s.numeric_field1, o_s.numeric_field2 " +
		"from orders ord " +
		"inner join projects prj on prj.id = ord.prj_id " +
		"inner join order_details o_d on o_d.order_id = ord.id " +
		"left outer join orders_suppliers o_s on o_s.order_id = ord.id " +
		"left outer join suppliers s on s.id = o_s.supplier_id " +
		"left outer join users u on u.id = o_d.int_field4 " +
		"where ord.prj_id = ? " +
		"order by prj.name, ord.order_number ";

	public static String GET_COST_ITEMS_FOR_PROJECT =
		"select c_i.id, c_i.cost_item_id, c_i.prj_id, c_i.cbs_id, c_b.name, c_i.status, " +
		"c_i_de.int_field1, c_i_de.int_field2, c_i_de.int_field3, c_i_de.int_field4, c_i_de.date_field1, " +
		"c_i_da.cost_category, c_i_da.cost_type, c_i_da.value, c_i.hier_level, " +
		"c_i.cbs_description, c_i_de.str_field1, c_i.lhs_number " +
		"from pur_cost_items c_i " +
		"left outer join pur_cost_breakdown c_b on c_b.id = c_i.cbs_id " +
		"left outer join pur_cost_item_details c_i_de on c_i_de.cost_item_id = c_i.id " +
		"left outer join pur_cost_item_data c_i_da on c_i_da.cost_item_id = c_i.id " +
		"where c_i.prj_id = ? " +
		"order by c_i.hier_level, c_i.lhs_number ";

	public static String GET_SUPPLIERS_FOR_COST_ITEMS_OF_PROJECT =
		"select c_i.id cid, c_i_s.supplier_id sid, s.name sn " +
		"from pur_cost_items c_i " +
		"inner join pur_cost_items_suppliers c_i_s on c_i_s.cost_item_id = c_i.id " +
		"inner join suppliers s on s.id = c_i_s.supplier_id " +
		"where c_i.prj_id = ? " +
		"and c_i_s.bid_status = ? " +
		//CHANGE: No longer doing union with order suppliers -- Sameer 043008
		//"union " +
		//"select c_i.id, o_s.supplier_id, s.name " +
		//"from pur_cost_items c_i " +
		//"inner join pur_cost_items_orders c_i_o on c_i_o.cost_item_id = c_i.id " +
		//"inner join orders_suppliers o_s on o_s.order_id = c_i_o.order_id " +
		//"inner join suppliers s on s.id = o_s.supplier_id " +
		//"where c_i.prj_id = ? " +
		//"and o_s.bid_status = ? " +
		"order by cid, sn ";

	public static String GET_LINKED_ORDERS_FOR_COST_ITEMS_OF_PROJECT =
		"select c_i.id, o.id, o.order_number, c_i_o.cost_category, c_i_o.value, c_i_o.pdm " +
		"from pur_cost_items c_i " +
		"inner join pur_cost_items_orders c_i_o on c_i.id = c_i_o.cost_item_id " +
		"inner join orders o on o.id = c_i_o.order_id " +
		"where c_i.prj_id = ? " +
		"order by c_i.id, o.order_number, c_i_o.cost_category ";
		
	public static String CREATE_PUR_COST_ITEM_FOR_PROJECT =
		Misc.G_DO_ORACLE ?
		"insert into pur_cost_items (id, prj_id, cost_item_id, cbs_id, status, hier_level, cbs_description) " +
		"values (?, ?, ?, ?, ?, ?, ?) " 
		:
		"insert into pur_cost_items (prj_id, cost_item_id, cbs_id, status, hier_level, cbs_description) " +
		"values (?, ?, ?, ?, ?, ?) ";

	public static String DELETE_PUR_COST_ITEM =
		"delete from pur_cost_items where id = ? ";

	public static String DELETE_PUR_COST_ITEMS_MULTI =
		"delete from pur_cost_items where id in ( ";
		
	public static String CREATE_PUR_COST_ITEM_DETAILS =
		"insert into pur_cost_item_details (cost_item_id, int_field1, int_field2, int_field3, int_field4, " +
		"int_field5, str_field1, str_field2, str_field3, str_field4, str_field5, numeric_field1, numeric_field2, " +
		"numeric_field3, numeric_field4, numeric_field5, date_field1, date_field2, date_field3, date_field4, " +
		"date_field5, supplier_awarded) " +
		"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

	public static String DELETE_PUR_COST_ITEM_DETAILS =
		"delete from pur_cost_item_details where cost_item_id = ? ";

	public static String DELETE_PUR_COST_ITEMS_DETAILS_MULTI =
		"delete from pur_cost_item_details where cost_item_id in ( ";
	
	public static String CREATE_PUR_COST_ITEM_DATA =
		"insert into pur_cost_item_data (cost_item_id, cost_category, cost_type, value) " +
		"values (?, ?, ?, ?) ";

	public static String DELETE_PUR_COST_ITEM_DATA =
		"delete from pur_cost_item_data where cost_item_id = ? ";

	public static String DELETE_PUR_COST_ITEM_DATA_MULTI =
		"delete from pur_cost_item_data where cost_item_id in ( ";

	public static String DELETE_PUR_COST_ITEM_DATA_FOR_COST_TYPE =
		"delete from pur_cost_item_data where cost_item_id = ? and cost_type = ? ";

	public static String CREATE_PUR_COST_ITEMS_ORDERS =
		"insert into pur_cost_items_orders (cost_item_id, order_id, cost_category, value, pdm) " +
		"values (?, ?, ?, ?, ?) ";

	public static String DELETE_PUR_COST_ITEMS_ORDERS =
		"delete from pur_cost_items_orders where cost_item_id = ? ";

	public static String DELETE_PUR_COST_ITEMS_ORDERS_MULTI =
		"delete from pur_cost_items_orders where cost_item_id in ( ";

	public static String CREATE_PUR_COST_ITEMS_SUPPLIERS =
		"insert into pur_cost_items_suppliers (cost_item_id, supplier_id, bid_status) values (?, ?, ?) ";
		
	public static String DELETE_PUR_COST_ITEMS_SUPPLIERS =
		"delete from pur_cost_items_suppliers where cost_item_id = ? and bid_status = ? ";

	public static String DELETE_PUR_COST_ITEMS_SUPPLIERS_MULTI =
		"delete from pur_cost_items_suppliers where cost_item_id in ( ";
		
	public static String GET_PUR_COST_ITEM_PARAMETERS =
			"select c_i.id, c_p.parameter_id, c_p.value " +
			"from pur_cost_items c_i " +
			"inner join pur_cost_parameters c_p on c_p.cost_item_id = c_i.id " +
			"where c_i.prj_id = ? " +
			"order by c_i.id, c_p.parameter_id ";

	public static String GET_PUR_COST_ITEM_PARAMETERS_WITH_NAME =
		"select c_i.id, c_p.parameter_id, p.name, p.type, c_p.value " +
		"from pur_cost_items c_i " +
		"inner join pur_cost_parameters c_p on c_p.cost_item_id = c_i.id " +
		"inner join parameters p on p.id = c_p.parameter_id " +
		"where c_i.prj_id = ? " +
		"order by c_i.id, p.type, c_p.parameter_id ";
		
	public static String CREATE_PUR_COST_PARAMETERS =
		"insert into pur_cost_parameters (cost_item_id, parameter_id, value) " +
		"values (?, ?, ?) ";
		
	public static String DELETE_PUR_COST_PARAMETERS =
		"delete from pur_cost_parameters where cost_item_id = ? ";

	public static String DELETE_PUR_COST_PARAMETERS_MULTI =
		"delete from pur_cost_parameters where cost_item_id in ( ";

	public static String GET_ORDER_DETAILS =
		"select o.order_number, o.order_status, o_d.description, o_d.int_field1, o_d.int_field2, " +
		"o_d.int_field3, o_d.int_field4, o_d.int_field5, o_d.int_field6, o_d.int_field7, o_d.int_field8, " +
		"o_d.int_field9, o_d.int_field10, o_d.str_field1, o_d.str_field2, o_d.str_field3, " +
		"o_d.str_field4, o_d.str_field5, o_d.str_field6, o_d.str_field7, o_d.str_field8, o_d.str_field9, " +
		"o_d.str_field10, o_d.numeric_field1, o_d.numeric_field2, o_d.numeric_field3, o_d.numeric_field4, " +
		"o_d.numeric_field5, o_d.date_field1, o_d.date_field2, o_d.date_field3, o_d.date_field4, o_d.date_field5 " +
		"from orders o " +
		"inner join order_details o_d on o_d.order_id = o.id " +
		"where o.id = ? ";

	public static String CREATE_ORDER =
		Misc.G_DO_ORACLE ?
		"insert into orders (id, order_number, order_status, prj_id) values (?, ?, ?, ?, ?) "
		:
		"insert into orders (order_number, order_status, prj_id) values (?, ?, ?, ?) ";

	public static String UPDATE_ORDER =
		"update orders set order_number = ?, order_status = ? where order_id = ? ";

	public static String CREATE_ORDER_DETAIL =
		"insert into order_details (order_id, description, classify1, classify2, classify3, classify4, " +
		"classify5, int_field1, int_field2, int_field3, int_field4, int_field5, int_field6, int_field7, " +
		"int_field8, int_field9, int_field10, str_field1, str_field2, str_field3, str_field4, str_field5, " +
		"str_field6, str_field7, str_field8, str_field9, str_field10, numeric_field1, numeric_field2, " +
		"numeric_field3, numeric_field4, numeric_field5, date_field1, date_field2, date_field3, date_field4, " +
		"date_field5) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
		"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

	public static String DELETE_ORDER_DETAIL =
		"delete from order_details where order_id = ? ";

	public static String DELETE_SUPPLIERS_FOR_ORDER =
		"delete from orders_suppliers where order_id = ? ";

	public static String CREATE_SUPPLIERS_FOR_ORDERS =
		"insert into orders_suppliers (order_id, supplier_id, numeric_field1, numeric_field2, numeric_field3, " +
		"numeric_field4, numeric_field5, bid_status, framework_agreement) values (?, ?, ?, ?, ? , ?, ?, ?, ?) ";

	public static String GET_PARAMETER_LIST =
		"select id, name, data_type, is_perf from parameters where type = ? ";
    //rajeev 112807 ... after sameer give
  public static String CREATE_ORDER_SIMPLE =
		Misc.G_DO_ORACLE ?
		"insert into orders (id, prj_id, created_by) values (?, ?, ?) " //rajeev 040808
		:
		"insert into orders ( prj_id, created_by) values (?, ?) "; //rajeev 040808
    
  public static String CREATE_ORDER_DETAIL_SIMPLE = "insert into order_details (order_id) values (?) ";
  
  //// rajeev 120107 ... for CapEx ... but common
  public static String GET_CUTOFF_ONGOING = "select projects.actual_cutoff_monthval, pj_basics.int_field6, pj_basics.int_field15  from projects, pj_basics, pj_map_items "+
     " where projects.id = ? and pj_map_items.prj_id = projects.id and pj_map_items.map_type = 1 and pj_map_items.isdefault=1 and pj_basics.id = pj_map_items.pj_basic_id ";
  public static String UPDATE_PORT_NODE_PARENT = "update port_nodes set port_node_id = ? where port_nodes.id = ? ";
  public static String UPDATE_PARENT_OF_CHILDREN_TO_NEW = "update port_nodes  set port_node_id = ? where port_nodes.port_node_id = ?";
  public static String UPDATE_PROJECTS_PORT_NODE = "update projects set port_node_id = ? where port_node_id = ? ";

  //rajeev 121307 ... search related
  public static String CREATE_SEARCH = Misc.G_DO_ORACLE ? "insert into saved_search (id, for_object_type, description, for_prj_id, for_cbs_id, status, temp_search_criteria, temp_view_criteria, temp_ccbs_view_param_list, temp_ccbs_view_item_id, created_on, updated_on, created_by, stored_results) values (?,?,?,?,?,?,?,?,?,?,sysdate, sysdate,?,?)"
														: "insert into saved_search (    for_object_type, description, for_prj_id, for_cbs_id, status, temp_search_criteria, temp_view_criteria, temp_ccbs_view_param_list, temp_ccbs_view_item_id, created_on, updated_on, created_by, stored_results) values (  ?,?,?,?,?,?,?,?,?,getDate(), getDate(), ?,?)";

  public static String GET_SEARCH_SUMMARY = "select for_object_type, description, status, temp_search_criteria, temp_view_criteria, temp_ccbs_view_param_list, temp_ccbs_view_item_id, stored_results, criteria1, view_criteria, ccbs_view_param_list, ccbs_view_item_id from saved_search where saved_search.id = ? ";
  public static String DELETE_SEARCH_RESULT_DETAIL = "delete from saved_search_result where search_id = ? and (? is null or is_permanent = ?)";

 // sameer 11302007
 public static String CREATE_SUPPLIER =
	 Misc.G_DO_ORACLE ?
	 "insert into suppliers (id, name, is_active, is_global, creation_status, created_by, created_on) " +
	 "values (?, ?, ?, ?, ?, ?, ?) "
	 :
	 "insert into suppliers (name, is_active, is_global, creation_status, created_by, created_on) " +
	 "values (?, ?, ?, ?, ?, ?) ";

 public static String CREATE_SUPPLIER_DETAIL =
	 "insert into supplier_details " +
	 "(supplier_id, int_field1, int_field2, int_field3, int_field4, int_field5, str_field1, " +
	 "str_field2, str_field3, str_field4, str_field5, date_field1, date_field2, date_field3, " +
	 "date_field4, date_field5) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

 public static String CREATE_SUPPLIER_COST_CHARACTERISIC =
	 "insert into supplier_cost_characteristic (supplier_id, cbs_id) values (?, ?) ";

 public static String CREATE_SUPPLIER_GEOGRAPHY =
	 "insert into supplier_geography (supplier_id, geography_id) values (?, ?) ";

 public static String CREATE_SUPPLIER_MULTI_ATTRIB =
	 "insert into supplier_multi_attrib (supplier_id, attrib_id, instance_id, row_num, int_val, date_val, " +
	 "double_val, str_val, classify1, classify2, classify3, classify4, classify5, created_on, created_by) " +
	 "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

	public static String INSERT_SUPPLIER_MULTI_ATTRIBUTE_EXTENDED = "insert into supplier_multi_attrib (supplier_id, attrib_id, int_val, date_val, double_val, str_val, instance_id, row_num, created_on, created_by, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?,?, ? ,? ,?, ?,?,?,?)";  

 public static String GET_SUPPLIERS_LIST_DETAIL =
	 "select s.id, s.name, s.is_global, s.creation_status, s_d.int_field1, s_d.int_field2, " +
	 "s_d.int_field3, s_d.int_field4, s_d.int_field5, s_d.str_field1, s_d.str_field2, " +
	 "s_d.str_field3, s_d.str_field4, s_d.str_field5, s_d.date_field1, s_d.date_field2, " +
	 "s_d.date_field3, s_d.date_field4, s_d.date_field5, " +
	 "s.created_by, users.name, s.created_on " +
	 "from suppliers s " +
	 "inner join supplier_details s_d on s_d.supplier_id = s.id " +
	 "left outer join users on users.id = s.created_by " +
	 "where s.is_active = 1 ";

 public static String GET_SUPPLIER_DETAIL =
	 "select s.name, s.is_active, s.is_global, s.creation_status, s_d.int_field1, s_d.int_field2, " +
	 "s_d.int_field3, s_d.int_field4, s_d.int_field5, s_d.str_field1, s_d.str_field2, " +
	 "s_d.str_field3, s_d.str_field4, s_d.str_field5, s_d.date_field1, s_d.date_field2, " +
	 "s_d.date_field3, s_d.date_field4, s_d.date_field5, s.created_by, s.created_on " +
	 "from suppliers s " +
	 "inner join supplier_details s_d on s_d.supplier_id = s.id " +
	 "where s.id = ? ";

 public static String GET_SUPPLIER_COST_CHARACTERISTIC =
	 "select cbs_id from supplier_cost_characteristic where supplier_id = ? ";

 public static String GET_SUPPLIER_GEOGRAPHY =
	 "select geography_id from supplier_geography where supplier_id = ? ";

 public static String GET_SUPPLIER_MULTI_ATTRIB =
	 "select int_val, date_val, double_val, str_val, classify1, classify2, classify3, classify4, classify5 " +
	 "created_on, created_by " +
	 "from supplier_multi_attrib " +
	 "where supplier_id = ? " +
	 "and attrib_id = ? ";

 public static String DELETE_SUPPLIER_DETAIL =
	 "delete from supplier_details where supplier_id = ? ";

 public static String DELETE_SUPPLIER_COST_CHARACTERISTIC =
	 "delete from supplier_cost_characteristic where supplier_id = ? ";

 public static String DELETE_SUPPLIER_GEOGRAPHY =
	 "delete from supplier_geography where supplier_id = ? ";

 public static String DELETE_SUPPLIER_MULTI_ATTRIB =
	 "delete from supplier_multi_attrib where supplier_id = ? and attrib_id = ? ";

 public static String DELETE_SUPPLIER =
	 "delete from suppliers where id = ? ";

 public static String UPDATE_SUPPLIER =
	 "update suppliers set name = ?, is_global = ?, creation_status = ? where id = ? ";

 public static String UPDATE_SUPPLIER_DETAIL =
	 "update supplier_details set int_field1 = ?, int_field2 = ?, int_field3 = ?, int_field4 = ?, " +
	 "int_field5 = ?, str_field1 = ?, str_field2 = ?, str_field3 = ?, str_field4 = ?, " +
	 "str_field5 = ?, date_field1 = ?, date_field2 = ?, date_field3 = ?, date_field4 = ?, " +
	 "date_field5 = ? where supplier_id = ? ";

 public static String UPDATE_SUPPLIER_CREATION_STATUS =
	 "update suppliers set creation_status = 0 where id = ? ";

 public static String GET_ORDERS_FOR_PROJECT_MOD_FOR_SUMMARY =
	 "select ord.id, ord.order_number, ord.order_status, " +
	 "o_d.description, o_d.int_field1, o_d.int_field2, o_d.int_field4, " +
	 "o_d.int_field6, o_d.int_field7, o_d.str_field4, o_d.str_field5, " +
	 "o_d.numeric_field1, o_d.date_field1, o_d.date_field4, o_d.date_field5, u.name, o_d.int_field3 " +
	 "from orders ord " +
	 "inner join order_details o_d on o_d.order_id = ord.id " +
	 "left outer join users u on u.id = o_d.int_field4 " +
	 "where ord.prj_id = ? " +
	 "order by ord.order_number ";

 //public static String GET_ORDER_SUMMARY_INFO =
 //	"select 

 public static String DATA_RECONCILIATION_UPDATE_BPM_DATA =
	 "update data_reconciliation set fixed_assets = ?, grants = ?, disposals = ?, " +
	 "currency = ? " +
	 "where port_node_id = ? and year_time_id = ? and month_time_id = ? and data_type = ?";

 public static String DATA_RECONCILIATION_UPDATE_BPM_ADJUSTMENTS =
	 "update data_reconciliation set adjustments = ?,  bpm_adj_currency = ? " +
	 "where port_node_id = ? and year_time_id = ? and month_time_id = ? and data_type = ?";

 public static String DATA_RECONCILIATION_UPDATE_CAPEX_ADJUSTMENT =
	   "update data_reconciliation set adjustments = ?, capex_adj_currency = ?, notes = ? " +
	   "where port_node_id = ? and year_time_id = ? " +
	   "and month_time_id = ? and data_type = ?";

 public static String GET_INVOLVED_SUPPLIERS_FOR_ORDER =
	 "select o_s.supplier_id, suppliers.name, o_s.bid_status " +
	 "from suppliers, orders_suppliers o_s " +
	 "where o_s.order_id = ? " +
	 "and o_s.supplier_id = suppliers.id " +
	 "and o_s.bid_status in (2, 3) " +
	 "order by suppliers.name ";

 // TODO : rewite query to get the amounts as well
 public static String GET_ORDER_SUMMARY_FOR_LINK_ORDER =
	 "select o.order_number, o.order_status, o_d.description " +
	 "from orders o, order_details o_d " +
	 "where o.id = ? " +
	 "and o_d.order_id = o.id ";

 public static String UPDATE_COST_ITEM_STATUS =
	 "update pur_cost_items set status = ? where id = ? ";

	public static String UPDATE_SEARCH_STATUS =
		"update saved_search set status = 1 where id = ? ";

	public static String INSERT_SAVED_SEARCH_ACCESS =
		"insert into saved_search_access (search_id, user_id) values (?, ?) ";

	public static String DELETE_SAVED_SEARCH_ACCESS =
		"delete from saved_search_access where search_id = ? ";

	public static String SAVE_SAVED_SEARCH =
		"update saved_search set for_prj_id = ?, status = ?, name = ?, description = ?, stored_results = ?, " +
		"ccbs_view_item_id = temp_ccbs_view_item_id, ccbs_view_param_list = temp_ccbs_view_param_list, " +
		"view_criteria = temp_view_criteria, updated_on = ?, " +
		"criteria1 = " +
		"(CASE WHEN ? = 1 THEN (select temp_search_criteria from saved_search where id = ?) " +
		"ELSE null END) where id = ? ";

	public static String MAKE_SEARCH_PERMANENT =
		"update saved_search_result set is_permanent = ? where search_id = ? and is_permanent = ? ";

	public static String GET_SAVED_SEARCHES =
		"select distinct ss.id, ss.for_object_type, ss.description, ss.for_prj_id, ss.for_cbs_id, " +
		"ss.status, ss.criteria1, ss.created_on, " +
		"ss.updated_on, ss.created_by, ss.stored_results, ss.name, p.name, u.name, ss.is_public " +
		"from saved_search ss " +
		"inner join users u on u.id = ss.created_by " +
		"left outer join saved_search_access ssa on ssa.search_id = ss.id " +
		"left outer join projects p on p.id = ss.for_prj_id " +
		"where (ss.status = 1 or ss.status = 2) and (ss.created_by = ? or ss.is_public = 1 or ssa.user_id = ? or ? = 1) " +		
		"order by ss.updated_on desc ";

	public static String DELETE_NON_PERMANENT_SEARCH_RESULTS =
		"delete from saved_search_result where search_id = ? and is_permanent = ? ";

	public static String REMOVE_TEMP_SAVED_SEARCH =
		"update saved_search set temp_ccbs_view_item_id = null, temp_ccbs_view_param_list = null, " +
		"temp_view_criteria = null, temp_search_criteria = null where id = ? ";

 public static String REMEMBER_SAVED_SEARCH_RESULTS =
		" update saved_search_result set saved_search_result.is_permanent = ?   "+    
    " where saved_search_result.is_permanent = ? and search_id = ? and item_id in ( ";
	

	public static String REMOVE_SAVED_SEARCH_RESULTS =
		"delete from saved_search_result where search_id = ? and item_id in ( ";

	public static String DELETE_SAVED_SEARCH_MULTI =
		"delete from saved_search where id in ( ";

	public static String DELETE_SAVED_SEARCH_ACCESS_MULTI =
		"delete from saved_search_access where search_id in ( ";

	public static String DELETE_SAVED_SEARCH_RESULTS_MULTI =
		"delete from saved_search_result where search_id in ( ";

	//rajeev 122107 
	public static String GET_DEVCOST_BY_TIMEID_1 =
	 "select /*+ ordered */ " +
		   "port_results.alt_id " +
		   ",cost_items.cost_cent_id " +
		   ",cost_items.for_achieving_milestone " +
		   ",data.year " +
		   ",sum(data.value) " +
		   ",data.val_scope " +
	   ",data.val_dur " +
		   ",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.target_market, cost_items.scen_id " +
		   "from " +
		   "port_results " +
	   ",alt_devcost_model " +
		   ",cost_items " +
		   ",data " +
		   "where " +
		   "port_results.port_rs_id = ? ";

	public static String GET_DEVCOST_BY_TIMEID_2 =
	  "and cost_items.alt_devcost_id = port_results.ver_alt_devcost_id " +
	  "and port_results.ver_alt_devcost_id = alt_devcost_model.id " +
			"and cost_items.to_include = 1 " +
			"and data.cost_li_id = cost_items.id " +
			"group by " +

			"port_results.alt_id " +
			",cost_items.cost_cent_id " +
			",cost_items.for_achieving_milestone " +
			",target_market " +
			",scen_id " +
			",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5, cost_items.scen_id " +
		",data.year, data.val_scope, data.val_dur " +
			"order by port_results.alt_id,  cost_items.cost_cent_id, " +
			"         cost_items.for_achieving_milestone " +
			"         ,target_market " +
			", scen_id " +
			",cost_items.classify1, cost_items.classify2, cost_items.classify3, cost_items.classify4, cost_items.classify5 " +
			",data.year "
			;
//select port_results_measure.measure_id, port_results_measure.alt_id, target_market, outcome_or_phase_id, break_down, measure_data.time_val 
	public static String GET_OTHER_MEASURE_BY_TIMEID_1 =

  "select port_results_measure.measure_id, port_results_measure.alt_id, target_market, outcome_or_phase_id, break_down, measure_data.time_val " +
		",sum(measure_data.val), measure_data.val_scope, measure_data.val_dur " +
		", classify1, classify2, classify3, classify4, classify5, scen_id " +
		"from port_results_measure inner join measure_data on (measure_data.alt_measure_id = port_results_measure.alt_measure_id) "+
    "inner join measure_case_index on (measure_case_index.id = measure_data.measure_case_index_id) ";//+ qtr_timeid "+



	public static String GET_OTHER_MEASURE_BY_TIMEID_2 =

	//" where port_results_measure.port_rs_id = ? "+ in Query Construct itself		
		"group by port_results_measure.measure_id, port_results_measure.alt_id,  measure_case_index.scen_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, measure_data.time_val, measure_data.val_scope, measure_data.val_dur " +
		"order by port_results_measure.measure_id, port_results_measure.alt_id,  measure_case_index.scen_id, target_market, outcome_or_phase_id, break_down, classify1, classify2, classify3, classify4, classify5, measure_data.time_val ";

	public static String UPDATE_SEARCH_TEMP_FIELDS = "update saved_search set temp_search_criteria=?, temp_view_criteria=?, temp_ccbs_view_param_list=?, temp_ccbs_view_item_id=? where id = ?";

	public static String GET_SEARCH_DETAILS = //rajeev 020608
		"select ss.name, ss.description, ss.for_prj_id, projects.name, ss.status, ss.is_public, ss.created_by from saved_search ss " +
		"left outer join projects on projects.id = ss.for_prj_id " +
		"where ss.id = ? ";

	public static String GET_USERS_FOR_SEARCH =
		"select ssa.user_id, users.name from saved_search_access ssa " +
		"inner join users on ssa.user_id = users.id " +
		"where ssa.search_id = ? " +
		"order by users.name ";
    
  //rajeev 010408
  public static String GET_APPROP_SAVED_SEARCH_ITEM_FOR_CCBS = " select distinct asked_pur_cost_items.id itemid, saved_search_result.is_permanent ispm from "+
    " saved_search_result join pur_cost_items filtered_pur_cost_items "+
    " on (saved_search_result.item_id = filtered_pur_cost_items.id and saved_search_result.search_id=?) "+
    " join pur_cost_items asked_pur_cost_items on  "+
    " (asked_pur_cost_items.cbs_id = ? "+
    " and asked_pur_cost_items.prj_id = filtered_pur_cost_items.prj_id "+
    " and ( "+
    " (asked_pur_cost_items.lhs_number <= filtered_pur_cost_items.lhs_number and asked_pur_cost_items.rhs_number >= filtered_pur_cost_items.rhs_number) "+
    " or "+
    " (asked_pur_cost_items.lhs_number >= filtered_pur_cost_items.lhs_number and asked_pur_cost_items.rhs_number <= filtered_pur_cost_items.rhs_number) "+
    " ) "+
    " ) ";
    
  public static String GET_APPROP_SAVED_SEARCH_ITEM_FOR_OTHERS = " select saved_search_result.item_id itemid, saved_search_result.is_permanent ispm  from saved_search_result where saved_search_result.search_id = ? ";
  
  public static String UPDATE_PORT_LHS_RHS = "update port_nodes set lhs_number = ?, rhs_number = ? where id = ?";
  
  public static String GET_MILESTONE_DETAILS_WKSPBENCH = "select port_results.alt_id, milestones.mstn_id, milestones.target_market, milestones.ms_status, milestones.start_date,milestones.finish_dt,milestones.succ_prob "+
                       "from port_results join milestones on (milestones.alt_date_id = port_results.ver_alt_mstone_id) where port_results.port_rs_id = ? and   (finish_dt-start_date)>0.5";
                       
  public static String MARK_PRJ_AS_NEEDING_VALIDATION = 
  Misc.G_DO_ORACLE ?
  "update pj_basics  set pj_basics.is_validated = ? where pj_basics.id in (select pj_map_items.pj_basic_id from pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault=1) "
  :
  "update pj_basics  set pj_basics.is_validated = ? from pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault=1 and pj_map_items.pj_basic_id = pj_basics.id";
  public static String UPDATE_VALIDATION_INFO = 
  Misc.G_DO_ORACLE ?
  "update pj_basics  set pj_basics.is_validated = ?, pj_basics.last_validated_on = getDate(), pj_basics.last_validated_by = ? where pj_basics.id in (select pj_map_items.pj_basic_id from pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault=1) "
  :
  "update pj_basics  set pj_basics.is_validated = ?, pj_basics.last_validated_on = getDate(), pj_basics.last_validated_by = ? from pj_map_items where pj_map_items.wspace_id = ? and pj_map_items.isdefault=1 and pj_map_items.pj_basic_id = pj_basics.id";

  // Sameer Jan 08
  public static String UPDATE_LABELS_WITH_PROJECT_INFO =
  Misc.G_DO_ORACLE ?
    "update labels set (pj_current_workflow_step, pj_currency, pj_actual_cutoff_monthval) = (select projects.curr_workflow_step, "+
	  "projects.currency, " +
	  "projects.actual_cutoff_monthval " +      
	  "from projects, pj_map_items " +
	  "where "+
	  "and pj_map_items.id = ? " +
	  "and pj_map_items.prj_id = projects.id ) where labels.id = ? "
  :
	  "update labels set " +
	  "pj_current_workflow_step = projects.curr_workflow_step, " +
	  "pj_currency = projects.currency, " +
	  "pj_actual_cutoff_monthval = projects.actual_cutoff_monthval " +
	  "from labels, projects, pj_map_items " +
	  "where labels.id = ? " +
	  "and pj_map_items.id = ? " +
	  "and pj_map_items.prj_id = projects.id ";

  public static String DO_PROJECT_INFO_SWITCH_LABEL =
  Misc.G_DO_ORACLE ?
  "update projects set (curr_workflow_step, currency, actual_cutoff_monthval) = (select "+
    " labels.pj_current_workflow_step, labels.pj_currency, labels.pj_actual_cutoff_monthval " +
	  "from projects, pj_map_items " +
	  "where "+
	  "and pj_map_items.id = labels.pj_map_id " +
	  "and pj_map_items.prj_id = projects.id) "+
    "where labels.id = ? " 
  :
	  "update projects set " +
	  "curr_workflow_step = labels.pj_current_workflow_step, " +
	  "currency = labels.pj_currency, " +
	  "actual_cutoff_monthval = labels.pj_actual_cutoff_monthval " +
	  "from projects, labels, pj_map_items " +
	  "where labels.id = ? " +
	  "and pj_map_items.id = labels.pj_map_id " +
	  "and pj_map_items.prj_id = projects.id ";
  //011508 .. rajeev
    
  public static String UPDATE_PORT_NODE_OF_PRJ = "update projects set port_node_id=? where id = ?";
  
  public static String MAP_INITIALIZE_MULTI = "insert into prj_portfolio_map (prj_id, port_node_id, par_level,parent_inserted) (select prj_multi_attrib.prj_id, prj_multi_attrib.classify1,1,0 from prj_multi_attrib join projects on projects.id=prj_multi_attrib.prj_id where projects.status in (1,2,4,7) and prj_multi_attrib.attrib_id=278)";
  public static String UPDATE_PRJ_PORTFOLIO_MAP_FOR_DUPLI_STEP1 = 
  Misc.G_DO_ORACLE ?
  "update prj_portfolio_map set parent_inserted = 3 where (port_node_id, prj_id) in (select port_node_id, prj_id from prj_portfolio_map group by port_node_id, prj_id having count(*) > 1) "
  :
  "update prj_portfolio_map set parent_inserted = 3 from  (select port_node_id, prj_id from prj_portfolio_map group by port_node_id, prj_id having count(*) > 1) frm where frm.port_node_id=prj_portfolio_map.port_node_id and frm.prj_id = prj_portfolio_map.prj_id";
  public static String UPDATE_PRJ_PORTFOLIO_MAP_FOR_DUPLI_STEP2 = "insert into prj_portfolio_map (port_node_id, prj_id, par_level, parent_inserted) (select port_node_id, prj_id, max(par_level), 1 from prj_portfolio_map where parent_inserted=3 group by port_node_id, prj_id)";
  public static String UPDATE_PRJ_PORTFOLIO_MAP_FOR_DUPLI_STEP3 = "delete from prj_portfolio_map where parent_inserted = 3";
  public static String GET_PORT_PRJ_MULTI_ATTRIB_SNIPP = " select prj_multi_attrib.prj_id, prj_multi_attrib.attrib_id, classify1, classify2, classify3, classify4, classify5, int_val, double_val, str_val, date_val from port_results join prj_multi_attrib on (prj_multi_attrib.prj_id = port_results.prj_id) where port_results.port_rs_id = ? and is_default_alt = 1 ";
  public static String CREATE_GENERAL_PARAM = Misc.G_DO_ORACLE ? "insert into general_param_lov(id, parameter_id, name, sn, description, status, updated_by, updated_on) values (?, ?, ?,?,?,1,?, sysdate) "
                                                                : 
                                                                 "insert into general_param_lov(    parameter_id, name, sn, description, status, updated_by, updated_on) values (   ?, ?,?,?,1,?, getDate())";        
public static String DELETE_GENERAL_PARAM = Misc.G_DO_ORACLE ? "update general_param_lov set status=0, updated_on=sysdate, updated_by=?  where parameter_id = ? and id = ? "
                                                             : "update general_param_lov set status=0, updated_on=getDate(), updated_by=?  where parameter_id = ? and id = ? ";
 public static String GET_GENERAL_PARAM_NAME = "select id, name from general_param_lov where parameter_id = ? and id = ? ";                                                            
 
 public static String INSERT_SUPP_MULTI_ATTRIB_FOR_PARAM = "insert into supplier_multi_attrib( supplier_id, row_num, instance_id, attrib_id, int_val, str_val, double_val, date_val, classify1, classify2, classify3, classify4, classify5, created_on, created_by) "+
       " (select ?, case when mx.v is null then 0 else mx.v+1 end, 0, ?, ?, ?, ?, ?,?,?,?,?,?,?,? "+
       " from dual left outer join (select max(row_num) v, max(int_val) iv, dual.dummy d from supplier_multi_attrib,dual where supplier_id=? and attrib_id=? group by dual.dummy) mx on mx.d = dual.dummy) ";
 public static String INSERT_PRJ_MULTI_ATTRIB_FOR_PARAM = "insert into prj_multi_attrib( prj_id, row_num, instance_id, attrib_id, int_val, str_val, double_val, date_val, classify1, classify2, classify3, classify4, classify5, created_on, created_by) "+
       " (select ?, case when mx.v is null then 0 else mx.v+1 end, 0, ?, ?, ?, ?, ?,?,?,?,?,?,?,? "+
       " from dual left outer join (select max(row_num) v, max(int_val) iv, dual.dummy d from prj_multi_attrib,dual where prj_id=? and attrib_id=? group by dual.dummy) mx on mx.d = dual.dummy) ";
 public static String INSERT_ORDER_MULTI_ATTRIB_FOR_PARAM = "insert into order_multi_attrib( order_id, row_num, instance_id, attrib_id, int_val, str_val, double_val, date_val, classify1, classify2, classify3, classify4, classify5, created_on, created_by) "+
       " (select ?, case when mx.v is null then 0 else mx.v+1 end, 0, ?, ?, ?, ?, ?,?,?,?,?,?,?,? "+
       " from dual left outer join (select max(row_num) v, max(int_val) iv, dual.dummy d from order_multi_attrib,dual where order_id=? and attrib_id=? group by dual.dummy) mx on mx.d = dual.dummy) ";

 public static String GET_ORDER_TEMPLATE = "select template_id from orders where orders.id = ?";
	
	public static String CREATE_SUPPLIER_SIMPLE =
			  Misc.G_DO_ORACLE ?
			  "insert into suppliers (id, name, is_active, created_by, created_on) values (?, null, ?, ?, ?) "
			  :
			  "insert into suppliers (name, is_active, created_by, created_on) values (null, ?, ?, ?) ";
    
  public static String CREATE_SUPPLIER_DETAIL_SIMPLE = "insert into supplier_details (supplier_id) values (?) ";

  // Sameer 012308
  public static String INSERT_PARAMETERS =
	  Misc.G_DO_ORACLE ?
	  "insert into parameters (id, name, type, data_type, delete_flag, is_perf) values (?, ?, ?, ?, ?, ?)"
	  :
	  "insert into parameters (name, type, data_type, delete_flag, is_perf) values (?, ?, ?, ?, ?)";

  public static String UPDATE_PARAMETERS = "update parameters set name = ?, type = ?, data_type = ?, delete_flag = ?, is_perf = ? where id = ?";

  public static String MARK_PARAMETERS_DELETE_FLAG = "update parameters set delete_flag = ? where type = ?";

  public static String DELETE_MARKED_PARAMETERS = "delete from parameters where delete_flag = ? and type = ?";

	public static String GET_WBS_ITEMS_LIST =
		"select id, name, parent_wbs_id, hier_level, is_active from pur_cost_breakdown " +
		"order by parent_wbs_id, lhs_number ";

  public static String GET_PARAMETERS_FOR_WBS_ITEM =
	  "select parameters.id, parameters.name, parameters.type, parameters.data_type, " +
	  "wbs_parameters.is_mandatory " +
	  "from parameters, wbs_parameters " +
	  "where wbs_parameters.wbs_id = ? " +
	  "and wbs_parameters.parameter_id = parameters.id " +
	  "order by parameters.type ";

  public static String GET_MANDATORY_COST_CATEGORY_FOR_WBS_ITEM =
	  "select category_id from wbs_mandatory_cost_category where wbs_id = ? ";

  public static String GET_PARAMETERS_FOR_WBS_ITEM_BY_TYPE =
	  "select parameters.id, parameters.name, parameters.type, parameters.data_type, " +
	  "wbs_parameters.is_mandatory " +
	  "from parameters, wbs_parameters " +
	  "where wbs_parameters.wbs_id = ? " +
	  "and wbs_parameters.parameter_id = parameters.id " +
	  "and parameters.type = ? ";

  public static String GET_WBS_ITEMS_PARAMETERS =
	  "select wbs_parameters.wbs_id, wbs_parameters.parameter_id, wbs_parameters.is_mandatory, " +
	  "parameters.name, parameters.type, parameters.data_type, parameters.is_perf " +
	  "from wbs_parameters " +
	  "inner join parameters on wbs_parameters.parameter_id = parameters.id " +
	  "order by wbs_parameters.wbs_id, parameters.type ";

  public static String GET_WBS_ITEMS_PARAMETERS_BY_TYPE =
	  "select wbs_parameters.wbs_id, wbs_parameters.parameter_id, wbs_parameters.is_mandatory, " +
	  "parameters.name, parameters.data_type, parameters.is_perf " +
	  "from wbs_parameters " +
	  "inner join parameters on wbs_parameters.parameter_id = parameters.id " +
	  "where parameters.type = ? " +
	  "order by wbs_parameters.wbs_id ";

  public static String GET_WBS_ITEMS_MANDATORY_COST_CATEGORIES =
	  "select wbs_id, category_id from wbs_mandatory_cost_category order by wbs_id ";

	public static String INSERT_WBS_ITEM =
		Misc.G_DO_ORACLE ?
		"insert into pur_cost_breakdown (id, name, parent_wbs_id, hier_level, is_active) values (?, ?, ?, ?, ?) "
		:
		"insert into pur_cost_breakdown (name, parent_wbs_id, hier_level, is_active) values (?, ?, ?, ?) ";

  public static String UPDATE_WBS_ITEM_NAME =
	  "update pur_cost_breakdown set name = ? where id = ? ";

  public static String DELETE_WBS_MANDATORY_COST_CATEGORY =
	  "delete from wbs_mandatory_cost_category where wbs_id = ? ";

  public static String DELETE_WBS_PARAMETERS_BY_TYPE =
  Misc.G_DO_ORACLE ?
   "delete from wbs_parameters where wbs_parameters.parameter_id in (select parameters.id from parameters where parameters.type = ?) and wbs_parameters.wbs_id = ? "
  :
	  "delete wbs_parameters from wbs_parameters inner join parameters on " +
	  "wbs_parameters.parameter_id = parameters.id where parameters.type = ? " +
	  "and wbs_parameters.wbs_id = ? ";

  public static String DELETE_SPECIFIC_PARAMETERS_FOR_WBS =
  Misc.G_DO_ORACLE ?
  "delete from parameters where parameters.id in (select wbs_parameters.parameter_id from wbs_parameters where wbs_parameters.wbs_id = ?) and parameters.type = 1 "
	:
	  "delete parameters from parameters inner join wbs_parameters on " +
	  "parameters.id = wbs_parameters.parameter_id where wbs_parameters.wbs_id = ? " +
	  "and parameters.type = 1";

  public static String DELETE_WBS_PARAMETERS =
	  "delete from wbs_parameters where wbs_id = ? ";

  public static String MARK_PARAMETERS_DELETE_FLAG_FOR_TYPE_FOR_WBS =
	  "update parameters set delete_flag = ? where id in " +
	  "(select wbs_parameters.parameter_id from wbs_parameters inner join parameters on " +
	  "wbs_parameters.parameter_id = parameters.id where wbs_id = ? and parameters.type = ?) ";

  public static String DELETE_WBS_PARAMETERS_MULTI =
	  "delete from wbs_parameters where wbs_id in ( ";

  public static String DELETE_WBS_MANDATORY_COST_CATEGORY_MULTI =
	  "delete from wbs_mandatory_cost_category where wbs_id in ( ";

  public static String DELETE_PUR_COST_BREAKDOWN_MULTI =
	  "delete from pur_cost_breakdown where id in ( ";

  public static String INSERT_WBS_PARAMETERS =
	  "insert into wbs_parameters (wbs_id, parameter_id, is_mandatory) values (?, ?, ?) ";

  public static String INSERT_WBS_MANDATORY_COST_CATEGORY =
	  "insert into wbs_mandatory_cost_category (wbs_id, category_id) values (?, ?) ";

	public static String GET_ORDERS_SUPPLIERS_FOR_PERF_REP = "select orders_suppliers_a.bid_status, orders_suppliers_a.supplier_id, suppliers.name, orders_suppliers_a.numeric_field1, orders_suppliers_a.numeric_field2,orders_suppliers_a.numeric_field4, supplier_multi_attrib.classify1 " +
		", (case when orders_suppliers_min.numeric_field3 is null or orders_suppliers_min.numeric_field3 = 0 then null else (orders_suppliers_a.numeric_field3- orders_suppliers_min.numeric_field3)/orders_suppliers_min.numeric_field3 end) " +
		", orders_suppliers_a.numeric_field3 " +
    ", intelli.getCurrConvOnDateInfl(order_details.int_field2,?,?,(case when (order_details.date_field3 is null) then order_details.date_field2 else order_details.date_field3 end),0) "+
		" from orders_suppliers orders_suppliers_a join suppliers on (suppliers.id = orders_suppliers_a.supplier_id) join order_details on (order_details.order_id = orders_suppliers_a.order_id) left outer join supplier_multi_attrib on (supplier_multi_attrib.attrib_id = 8084 and supplier_multi_attrib.supplier_id = orders_suppliers_a.supplier_id and supplier_multi_attrib.row_num = orders_suppliers_a.framework_agreement) " +
		" left outer join orders_suppliers orders_suppliers_min on (orders_suppliers_a.order_id = orders_suppliers_min.order_id and orders_suppliers_min.bid_status=3) " +
		" where orders_suppliers_a.order_id = ? order by orders_suppliers_a.bid_status desc";

	public static String DELETE_ALL_SUPPLIER_MULTI_ATTRIB =
	 "delete from supplier_multi_attrib where supplier_id = ? ";

	public static String UPDATE_SUPPLIER_CREATION_STATUS_MULTI =
	 "update suppliers set creation_status = 0 where id in ( ";

	public static String DELETE_SUPPLIER_DETAIL_MULTI =
	 "delete from supplier_details where supplier_id in ( ";

	public static String DELETE_SUPPLIER_MULTI_ATTRIB_MULTI =
		"delete from supplier_multi_attrib where supplier_id in ( ";

	public static String DELETE_SUPPLIER_MULTI =
		"delete from suppliers where id in ( ";

	public static String DELETE_SUPPLIER_FROM_ORDER_SUPPLIERS_MULTI =
		"delete from orders_suppliers where supplier_id in ( ";

	public static String DELETE_SUPPLIER_FROM_COST_ITEMS_SUPPLIERS_MULTI =
		"delete from pur_cost_items_suppliers where supplier_id in ( ";

	public static String DELETE_ORDER_DETAIL_MULTI =
	 "delete from order_details where order_id in ( ";

	public static String DELETE_ORDER_MULTI_ATTRIB_MULTI =
		"delete from order_multi_attrib where order_id in ( ";

	public static String DELETE_ORDER_MULTI =
		"delete from orders where id in ( ";

	public static String DELETE_ORDER_FROM_ORDER_SUPPLIERS_MULTI =
		"delete from orders_suppliers where order_id in ( ";

	public static String DELETE_ORDER_FROM_COST_ITEMS_ORDERS_MULTI =
		"delete from pur_cost_items_orders where order_id in ( ";

	public static String DELETE_ORDER_FILES_MULTI =
		"delete from orders_files where order_id in ( ";

	public static String GET_YTD_RATES = "select v_ytd_curr_rate_at_time.currency_numeric_code, v_ytd_curr_rate_at_time.month, v_ytd_curr_rate_at_time.conversion_rate from v_ytd_curr_rate_at_time where month >= ? ";
	//rajeev 021108    
	public static String SAVE_SAVED_SEARCH_W_CRIT =
		"update saved_search set for_prj_id = ?, status = ?, name = ?, description = ?, stored_results = ?, " +
		"ccbs_view_item_id = temp_ccbs_view_item_id, ccbs_view_param_list = temp_ccbs_view_param_list, " +
		"view_criteria = temp_view_criteria, updated_on = ?, " +
		"criteria1 = temp_search_criteria " +
    ",is_public = ? "+
		"where id = ? ";

	public static String SAVE_SAVED_SEARCH_WO_CRIT =
		 "update saved_search set for_prj_id = ?, status = ?, name = ?, description = ? " +
		 ", updated_on = ? " +
     ",is_public = ? "+
		 "where id = ? ";

	public static String COPY_SAVED_SEARCH = Misc.G_DO_ORACLE ?
	  "insert into saved_search (id, for_object_type, description, for_prj_id, for_cbs_id, status, criteria1, created_on, updated_on, created_by, stored_results, temp_search_criteria, temp_view_criteria, view_criteria, temp_ccbs_view_item_id, ccbs_view_item_id, temp_ccbs_view_param_list, ccbs_view_param_list, name) " +
	   "  (select ?, for_object_type, description, for_prj_id, for_cbs_id, status, criteria1, sysdate, sysdate, ?, stored_results, temp_search_criteria, temp_view_criteria, view_criteria, temp_ccbs_view_item_id, ccbs_view_item_id, temp_ccbs_view_param_list, ccbs_view_param_list, name from saved_search where id = ? )"
	:
	   "insert into saved_search (for_object_type, description, for_prj_id, for_cbs_id, status, criteria1, created_on, updated_on, created_by, stored_results, temp_search_criteria, temp_view_criteria, view_criteria, temp_ccbs_view_item_id, ccbs_view_item_id, temp_ccbs_view_param_list, ccbs_view_param_list, name) " +
	   "  (select for_object_type, description, for_prj_id, for_cbs_id, status, criteria1, getDate(), getDate(), ?, stored_results, temp_search_criteria, temp_view_criteria, view_criteria, temp_ccbs_view_item_id, ccbs_view_item_id, temp_ccbs_view_param_list, ccbs_view_param_list, name from saved_search where id = ? )";

	public static String COPY_SAVED_SEARCH_DETAIL = "insert into saved_search_result (search_id, item_id, is_permanent) (select ?, item_id, is_permanent from saved_search_result where search_id = ?)";

	public static String RECONCILIATION_CAPEX_DATA_PERIOD_GET_PART1 =
		"select sum( bycurr.dv * intelli.getYTDCurrencyConversion(bycurr.cu, ?, byCurr.ti+35)) " +
		"from ( " +
		"	select sum(measure_data.val * " +
		"			intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 2, measure_data.val_dur) " +
		"		  ) dv, alt_measures.currency_id cu, ? ti " +
		"	from projects, measure_map_items, alternatives, alt_measures, measure_data, measure_case_index, " +
		"	month_timeid, " +
		"   ( " +
		"		select prj_multi_attrib.prj_id, sum(prj_multi_attrib.double_val) dv from prj_multi_attrib " +
		"		join prj_portfolio_map " +
		"		on ( " +
		"			prj_portfolio_map.prj_id = prj_multi_attrib.prj_id and prj_multi_attrib.attrib_id=278 " +
		"			and prj_portfolio_map.port_node_id = ? " +
		"		) " +
		"		join port_nodes leaf on (leaf.id = prj_multi_attrib.classify1) " +
		"		join port_nodes anc on (anc.id = prj_portfolio_map.port_node_id) " +
		"		where " +
		"		leaf.lhs_number >= anc.lhs_number and leaf.rhs_number <= anc.rhs_number " +
		"		group by prj_multi_attrib.prj_id " +
		"	) pjpmapl " +
		"	where " +
		"	pjpmapl.prj_id = projects.id " +
		"	and projects.status in (2) " +
		"	and alternatives.prj_id = projects.id " +
		"	and alternatives.is_primary = 1 " +
		"	and measure_map_items.alt_id = alternatives.id " +
		"	and measure_map_items.isdefault = 1 " +
		"	and measure_map_items.measure_id = ? " +
		"	and measure_map_items.map_type in (1) " +
		"	and alt_measures.id = measure_map_items.alt_measure_id " +
		"	and time_id >= ? " +
		"	and time_id <= ? " +
		"	and measure_data.alt_measure_id = measure_map_items.alt_measure_id " +
		"	and measure_case_index.id = measure_data.measure_case_index_id " +
		"	and time_id >= cast((measure_data.time_val/35) as int)*35 " +
		"	and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 " +
		"									   when (measure_data.val_scope=1) then 420 " +
		"									   when (measure_data.val_scope=2) then 35 " +
		"									   when (measure_data.val_scope=3) then 12 " +
		"									   when (measure_data.val_scope=5) then measure_data.val_dur*1.26 " +
		"									   else 1 " +
		"									   end) ";

	public static String RECONCILIATION_CAPEX_DATA_PERIOD_GET_PART2 =
		"	group by alt_measures.currency_id " +
		") bycurr ";

	public static String RECONCILIATION_CAPEX_DATA_ALL_MONTHS_GET_PART1 =
		"select sum( cumm_by_curr.dv * intelli.getYTDCurrencyConversion(cumm_by_curr.cu, ?, cumm_by_curr.ti+35)), cumm_by_curr.ti " +
		"from " +
		"( " +
		"	select sum(detailed_by_curr.dv) dv, detailed_by_curr.cu cu, mon_for_cumm.time_id ti " +
		"	from month_timeid mon_for_cumm, " +
		"	( " +
		"		select sum(measure_data.val * " +
		"					intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 2, measure_data.val_dur) " +
		"				) dv, " +
		"		alt_measures.currency_id cu, time_id ti " +
		"		from projects, measure_map_items, alternatives, alt_measures, measure_data, measure_case_index, " +
		"		month_timeid, " +
		"		( " +
		"			select prj_multi_attrib.prj_id, sum(prj_multi_attrib.double_val) dv " +
		"			from prj_multi_attrib " +
		"			join prj_portfolio_map " +
		"			on ( " +
		"				prj_portfolio_map.prj_id = prj_multi_attrib.prj_id and prj_multi_attrib.attrib_id=278 " +
		"				and prj_portfolio_map.port_node_id = ? " +
		"			) " +
		"			join port_nodes leaf on (leaf.id = prj_multi_attrib.classify1) " +
		"			join port_nodes anc on (anc.id = prj_portfolio_map.port_node_id) " +
		"			where " +
		"			leaf.lhs_number >= anc.lhs_number and leaf.rhs_number <= anc.rhs_number " +
		"			group by prj_multi_attrib.prj_id " +
		"		) pjpmapl " +
		"		where " +
		"		pjpmapl.prj_id = projects.id " +
		"		and projects.status in (2) " +
		"		and alternatives.prj_id = projects.id " +
		"		and alternatives.is_primary = 1 " +
		"		and measure_map_items.alt_id = alternatives.id " +
		"		and measure_map_items.isdefault = 1 " +
		"		and measure_map_items.measure_id = ? " +
		"		and measure_map_items.map_type in (1) " +
		"		and alt_measures.id = measure_map_items.alt_measure_id " +
		"		and time_id >= ? " +
		"		and time_id < ? + 420 " +
		"		and measure_data.alt_measure_id = measure_map_items.alt_measure_id " +
		"		and measure_case_index.id = measure_data.measure_case_index_id " +
		"		and time_id >= cast((measure_data.time_val/35) as int)*35 " +
		"		and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 " +
		"										   when (measure_data.val_scope=1) then 420 " +
		"										   when (measure_data.val_scope=2) then 35 " +
		"										   when (measure_data.val_scope=3) then 12 " +
		"										   when (measure_data.val_scope=5) then measure_data.val_dur*1.26 " +
		"										   else 1 " +
		"										   end) ";

	public static String RECONCILIATION_CAPEX_DATA_ALL_MONTHS_GET_PART2 =
		"		group by alt_measures.currency_id, time_id " +
		"	) detailed_by_curr " +
		"	where " +
		"	mon_for_cumm.time_id >= ? " +
		"	and mon_for_cumm.time_id < ? " +
		"	and detailed_by_curr.ti <= mon_for_cumm.time_id " +
		"	group by detailed_by_curr.cu, mon_for_cumm.time_id " +
		") cumm_by_curr " +
		"group by cumm_by_curr.ti ";

	public static String RECONCILIATION_GET_NOTES =
		"select notes from data_reconciliation, port_nodes " +
		"where data_reconciliation.port_node_id = ? " +
		"and port_nodes.id = data_reconciliation.port_node_id " +
		"and year_time_id = ? and data_type = ? and month_time_id = ? ";
    
  public static String UPDATE_OTHER_ORDER_TYPE_INFO = "update orders set order_number = ?, order_status=?, template_id = ? where orders.id = ?";
  public static String UPDATE_OTHER_ORDER_TYPE_INFO_DETAIL = "update order_details set port_node_id = (select port_node_id from projects where projects.id = ?), date_field2 = "+
               " (select min(milestones.start_date) from projects join alternatives on (alternatives.prj_id = projects.id and alternatives.is_primary=1) join alt_map_items on (alt_map_items.alt_id = alternatives.id and map_type=2 and isdefault=1) join milestones on (milestones.alt_date_id = alt_map_items.alt_date_id) where mstn_id = 1 and projects.id = ?) "+
               " ,int_field2 = (select currency from projects where projects.id = ?) "+
               " where order_details.order_id = ? ";
  public static String GET_ROLE_LIMITED = " select role.id, role.name, role.role_desc, role.scope, role.external_code "+//must match GET_ALL_ROLES
           " from user_roles join user_roles_scope on (user_role_id = user_roles.id) join role on (role.id = user_roles.role_id) "+
           " where "+
           " user_roles.user_1_id = ? "+ //userid
           " and ( "+
           " ? is null or user_roles_scope.port_node_id is null or "+ //port
           " user_roles_scope.port_node_id in (select anc.id from port_nodes anc, port_nodes leaf where leaf.id = ? "+ //port
           " and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
           " ) "+
           " and (? is null or ? = prj_id or prj_id is null) "+ //prj, prj
           " and grantable = 1 "+ //rajeev 022608
           " order by role.name asc ";
  public static String GET_GEN_PRIV_AVAILABLE = " select role_privs.priv_id, object_type, object_id from object_role_assignment join role_privs on role_privs.role_id = object_role_assignment.role_id where user_1_id = ? order by role_privs.priv_id";
  public static String GET_PORT_NODE_FOR_ORDER = "select port_node_id from order_details where order_id=?";
  
  public static String DELETE_INDIVIDUAL_ACCESS_PRJ = " delete from user_roles_scope where prj_id = ? ";
  public static String DELETE_ROLE_GRANTS_WITH_NO_SCOPE = " delete from user_roles where not exists(select 1 from user_roles_scope where user_role_id = user_roles.id) ";
  public static String DELETE_INDIVIDUAL_ACCESS_GEN_OBJ = " delete from object_role_assignment where object_type = ? and object_id = ? ";

  public static String GET_PUR_PROJECT_STATUS =
			"select pj_basics.int_field12 from pj_basics inner join pj_map_items on pj_basics.id = pj_map_items.pj_basic_id " +
			"and pj_map_items.prj_id = ? and pj_map_items.wspace_id = ? and pj_map_items.isdefault = ? and pj_map_items.map_type = ? ";

  public static String GET_SUPPLIER_BY_NAME =
	  "select id from suppliers where is_active = 1 and name = ?";
    
  public static String GET_ORDER_SPECIFIC_CORE = //gets the org id, order currency, template id, project id, default alt id, default workspace id  for the order
    " select order_details.port_node_id, order_details.int_field2, orders.template_id, orders.prj_id, alternatives.id, pj_map_items.wspace_id "+
    " from orders join order_details on (orders.id = order_details.order_id) join pj_map_items on (pj_map_items.prj_id = orders.prj_id and pj_map_items.map_type=1 and pj_map_items.isdefault=1) join alternatives on (alternatives.prj_id = orders.prj_id and is_primary=1) "+
    " where orders.id = ? ";
    
  public static String GET_MAX_SPECIFIC_PARAMETERS =
		"select max(param_cnt.cnt) from ( " +
		"	select count(*) cnt from wbs_parameters, parameters where " +
		"	wbs_parameters.parameter_id = parameters.id and parameters.type = 1 group by wbs_id " +
		") param_cnt ";
  
	public static String GET_COUNT_OF_COMMON_PARAMETERS =
		"select count(*) from parameters where type = 0";

	public static String GET_CHILDREN_OF_PUR_COST_ITEM =
		"select distinct leaf.id from pur_cost_items anc join pur_cost_items leaf on anc.prj_id = leaf.prj_id " +
		"and anc.lhs_number < leaf.lhs_number and anc.rhs_number > leaf.rhs_number and anc.id = ? ";

	public static String UPDATE_COST_ITEM_STATUS_AND_DESC =
	 "update pur_cost_items set status = ?, cbs_description = ? where id = ? ";    

  public static String GET_CUSTOM_PRJINFO_FOR_ORDER_PREFILL = 
  //org, currency, start_date, end_date, Strategy (8056/str_field8), highlight (8058/str_field10), areasof concern (8057/str_field9)
   "select projects.port_node_id, projects.currency, pj_basics.date_field1, milestones.finish_dt, pj_basics.str_field8, pj_basics.str_field10, pj_basics.str_field9, pj_basics.date_field2, pj_basics.int_field14 "+
   "from projects join pj_map_items on (pj_map_items.prj_id = projects.id and pj_map_items.map_type = 1 and pj_map_items.isdefault=1) "+
   "join alternatives on (alternatives.prj_id = projects.id and alternatives.is_primary=1) "+
   "join alt_map_items on (alt_map_items.alt_id = alternatives.id and alt_map_items.map_type = 1 and alt_map_items.isdefault=1) "+
   "join pj_basics on (pj_basics.id = pj_map_items.pj_basic_id) "+
   "join milestones on (milestones.alt_date_id = alt_map_items.alt_date_id and milestones.target_market=0 and milestones.mstn_id = 1) "+
   "where projects.id = ?";
public static String REMEMBER_SAVED_SEARCH_RESULTS_CCBS = //NOT_USED
		" update saved_search_result set saved_search_result.is_permanent = 1 from saved_search_result  "+
    " join pur_cost_items leaf on leaf.id = saved_search_result.item_id "+
    " join pur_cost_items anc on (anc.prj_id = leaf.prj_id "+
    " and "+
    " ( "+
    " (leaf.lhs_number >= anc.lhs_number and leaf.rhs_number <= anc.rhs_number) "+
    " or "+
   " (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number) "+
   " ) "+
   ") "+
   " where saved_search_result.is_permanent = 0 and search_id = -1 and item_id in ( ";

	public static String REMOVE_SAVED_SEARCH_RESULTS_CCBS =
		"delete from saved_search_result where search_id = ? and item_id in ( ";

  public static String GET_INFLATION_RATE = "select year, value from inflation_factor order by year";   
  public static String DELETE_INFLATION_RATE = "delete from inflation_factor";
  public static String INSERT_INFLATION_RATE = "insert into inflation_factor(year, value) values (?,?)";

  public static String UPDATE_LHS_RHS_FOR_CBS =
	  "update pur_cost_items set lhs_number = ?, rhs_number = ? where id = ? and prj_id = ? ";

  public static String UPDATE_LHS_RHS_FOR_WBS =
	  "update pur_cost_breakdown set lhs_number = ?, rhs_number = ? where id = ? ";

  public static String GET_CHILDREN_OF_PUR_COST_BREAKDOWN =
	  "select distinct leaf.id from pur_cost_breakdown leaf,  pur_cost_breakdown anc " +
	  "where anc.lhs_number < leaf.lhs_number and anc.rhs_number > leaf.rhs_number and anc.id = ? order by leaf.id";

  public static String SET_DELETED_PUR_COST_BREAKDOWN_MULTI =
  Misc.G_DO_ORACLE ?
  "update pur_cost_breakdown set is_active = 0, name = '(deleted) ' || name " +
	  "where id in ( "
  :
	  "update pur_cost_breakdown set is_active = 0, name = '(deleted) ' + name " +
	  "where id in ( ";

	public static String GET_ACTIVE_WBS_ITEMS_LIST =
	  "select id, name, parent_wbs_id, hier_level from pur_cost_breakdown where is_active = 1 " +
	  "order by lhs_number ";
  //042508
 public static String UPDATE_SEARCH_RESULT_STATUS =
  "update saved_search_result set is_permanent = is_permanent+10 where search_id = ?";
 public static String INSERT_VIEWCCBS_ID_SEARCH_RESULT =
  "insert into saved_search_result (search_id, item_id, is_permanent) (select distinct search_id, anc.id, saved_search_result.is_permanent-10 "+
  "from saved_search_result join pur_cost_items leaf on (saved_search_result.search_id = ? and saved_search_result.item_id = leaf.id) "+
  " join pur_cost_items anc on (anc.prj_id = leaf.prj_id and anc.cbs_id = ? "+
  " and "+
  " ( "+
  " (leaf.lhs_number >= anc.lhs_number and leaf.rhs_number <= anc.rhs_number) "+ //anc is anc of item in search result
  " or "+
  " (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number) "+ //anc is desc of item in search result
  " ) "+
  ") "+
  ") ";
 public static String DELETE_FILTERCCBS_ID_SEARCH_RESULT =  "delete from saved_search_result where search_id = ? and is_permanent >= 10";
 
 public static String GET_FUNCTIONAL_BREAKDOWN_FOR_ORDER_NEW =
		"select cio.cost_item_id, ci.cbs_description, cb.name, " +
		"(select sum(cid1.value) from pur_cost_item_data cid1 where cid1.cost_type = 0 and cio.cost_item_id = cid1.cost_item_id ) bud_cost, " +
		"(select sum(cid2.value) from pur_cost_item_data cid2 where cid2.cost_type = 1 and cio.cost_item_id = cid2.cost_item_id) ord_cost, " +
		"sum(cio.value) " +    
    ",intelli.getCurrConvOnDateInfl(projects.currency, order_details.int_field2, ?, milestones.start_date,0) "+
		"from pur_cost_items_orders cio join order_details on (cio.order_id = order_details.order_id) join pur_cost_items ci on (ci.id=cio.cost_item_id) join pur_cost_breakdown cb on (cb.id = ci.cbs_id)  join projects on (projects.id = ci.prj_id) " +
    "join alternatives on (alternatives.prj_id = projects.id and is_primary=1) join alt_map_items on (alt_map_items.alt_id = alternatives.id and alt_map_items.map_type=1 and alt_map_items.isdefault=1) "+
    "join milestones on (milestones.alt_date_id = alt_map_items.alt_date_id and milestones.mstn_id=1) "+
		"where cio.order_id = ? " +
		"group by cio.cost_item_id, cb.name, ci.cbs_description, order_details.int_field2, order_details.date_field3, order_details.date_field2, milestones.start_date, projects.currency ";
    
    
//rajeev 051008
 public static String GET_CURR_PRJ_VERSION = "select alt_map_items.alt_work_id, projects.id, alternatives.id, alt_map_items.wspace_id from projects join alternatives on (alternatives.prj_id = projects.id) join alt_map_items on (alt_map_items.alt_id = alternatives.id and is_default=1 and map_type in (1,4)) where ((? = projects.id and ? = alternatives.id) or (? is null and projects.status in (2,7)))";
 
 public static String GET_COUNT_OF_MEASURE_DATA = "select count(*), sum(measure_data.val)  from measure_map_items join measure_data on (measure_map_items.alt_measure_id = measure_data.alt_measure_id and isdefault = 1) where measure_map_items.alt_id = ? and measure_map_items.wspace_id = ? and measure_map_items.measure_id = ?";
 public static String GET_LOADED_FROM_REV = "select loaded_from from alt_map_items join alt_rev_model on (alt_rev_model.id = alt_map_items.alt_rev_id) where alt_map_items.alt_id = ? and alt_map_items.wspace_id = ? and alt_map_items.isdefault=1 ";

	public static String GET_USER_LAST_LOGIN =
		"select max(ts) as last_login from user_login_track where user_id = ? ";

	public static String INSERT_USER_LOGIN_TRACK =
		"insert into user_login_track (user_id, ts,host_ip) values (?, ?, ?) ";

	public static String INSERT_OBJECT_COUNTERS =
		"insert into object_counters (obj_id, obj_type, user_id, section, ts) values (?, ?, ?, ?, ?) ";

	public static String GET_OBJ_COUNTER_FOR_USER_IN_PERIOD =
		"select count(*) from object_counters where obj_type = ? and user_id  = ? and ts >= ? and ts <= ? ";

	public static String GET_OBJECT_COUNTER_IN_PERIOD =
		"select count(*) from object_counters where ts >= ? and ts <= ? ";

	public static String GET_LOGIN_BY_USER_IN_PERIOD =
		"select count(*) from user_login_track where user_id = ? and ts >= ? and ts <= ? ";

	public static String GET_TOTAL_LOGIN_IN_PERIOD =
		"select count(*) from user_login_track where ts >= ? and ts <= ? ";

	public static String GET_OBJ_COUNTER_FOR_OBJ_IN_PERIOD =
		"select count(*) from object_counters where obj_type = ? and ts >= ? and ts <= ? ";
    
  //052908 ...
  public static String GET_CONSTRAINT_DATA_MINMAX = "select min(year), max(year) from constraint_header join constraint_data on (constraint_header_id = constraint_header.id and measure_id = ?)";

  public static String GET_CONSTRAINT_DATA_1 =

	 "select constraint_header.id, time_id, " +
		" sum(constraint_data.value * intelli.getPropIncluded(constraint_data.val_scope, constraint_data.year, time_id, ?, constraint_data.val_dur)), " +
	" constraint_header.port_node_id, " +
		" constraint_header.classify1, constraint_header.classify2, constraint_header.classify3, constraint_header.classify4, constraint_header.classify5, constraint_header.fte_skill_or_cost_cent " +
		" from constraint_header join constraint_data on (constraint_header.id = constraint_data.constraint_header_id) ,  "; //time_id app
  public static String GET_CONSTRAINT_DATA_2 =   
     Misc.G_DO_ORACLE ?
		" where constraint_header.measure_id = ? "+		
		" and time_id >= trunc(constraint_data.year/35)*35 "
    :
    " where constraint_header.measure_id = ? "+		
		" and time_id >= cast(constraint_data.year/35 as int)*35 ";
  public static String GET_CONSTRAINT_DATA_3 = 
		" and time_id < constraint_data.year+(case when (constraint_data.val_scope = 0) then 105 "+
		"    when (constraint_data.val_scope=1) then 420 "+
		"   when (constraint_data.val_scope=2) then 35 "+
		"    when (constraint_data.val_scope=3) then 12 "+
		"    when (constraint_data.val_scope=5) then constraint_data.val_dur*1.25+7 "+
		"    else 1 "+
		" end) ";
 public static String GET_CONSTRAINT_DATA_4 =
		" group by constraint_header.port_node_id,  constraint_header.classify1, constraint_header.classify2, constraint_header.classify3, constraint_header.classify4, constraint_header.classify5, constraint_header.id, fte_skill_or_cost_cent,   time_id  "+
		" order by constraint_header.port_node_id,  constraint_header.classify1, constraint_header.classify2, constraint_header.classify3, constraint_header.classify4, constraint_header.classify5, constraint_header.id, fte_skill_or_cost_cent,   time_id  ";

 public static String DEL_CONTRAINT_DATA = "delete from constraint_data where constraint_header_id in (select id from constraint_header where measure_id = ?)";
 public static String DEL_CONSTRAINT_HEADER = "delete from constraint_header where measure_id = ?";
 public static String CREATE_CONSTRAINT_HEADER = Misc.G_DO_ORACLE ?
  " insert into constraint_header (id, measure_id, port_node_id, fte_skill_or_cost_cent, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?,?,?) "
  :
  " insert into constraint_header (measure_id, port_node_id, fte_skill_or_cost_cent, classify1, classify2, classify3, classify4, classify5) values (?,?,?,?,?,?,?,?) ";
 public static String INSERT_CONSTRAINT_DATA =
  "insert into constraint_data (constraint_header_id,  value, year, val_scope, val_dur) values (?,?,?,?,?)";

 public static String GET_GEN_MEASURE_BY_TIMEID_1 =

  "select constraint_header.measure_id, port_node_id, fte_skill_or_cost_cent,  constraint_data.year " +
		",sum(constraint_data.val), constraint_data.val_scope, constraint_data.val_dur " +
		", classify1, classify2, classify3, classify4, classify5 " +
		"from constraint_header inner join constraint_data on (constraint_header.id = constraint_data.constraint_header_id) ";




 public static String GET_GEN_MEASURE_BY_TIMEID_2 =

 //" where port_results_measure.port_rs_id = ? "+ in Query Construct itself		
	 "group by constraint_header.measure_id, port_node_id, fte_skill_or_cost_cent,  constraint_data.val_scope, constraint_data.val_dur, classify1, classify2, classify3, classify4, classify5, constraint_data.year  " +
	 "order by constraint_header.measure_id, port_node_id, fte_skill_or_cost_cent,  constraint_data.val_scope, constraint_data.val_dur, classify1, classify2, classify3, classify4, classify5, constraint_data.year  ";
 //081808 - rajeev
 public static String CUSTOM_UPD_APPROVAL_DATE_FIELD =
 Misc.G_DO_ORACLE ? "ORACLE_TODO"
 :
 "update pj_basics set date_field6 = (case when pj_basics.int_field6 = 0 then milestones.start_date else date_field6 end) " +
 "                   ,date_field7 = (case when pj_basics.int_field6 = 1 then milestones.start_date else date_field7 end) " +
 "from pj_map_items join pj_basics on (pj_basics.id = pj_map_items.pj_basic_id and pj_map_items.isdefault=1) " +
 "join alternatives on (alternatives.prj_id = pj_map_items.prj_id and alternatives.is_primary=1) " +
 "join alt_map_items on (alt_map_items.alt_id = alternatives.id and alt_map_items.wspace_id=pj_map_items.wspace_id and alt_map_items.isdefault=1) " +
 "join milestones on (milestones.alt_date_id = alt_map_items.alt_date_id and mstn_id=1 and target_market=0) " +
 "where pj_map_items.wspace_id = ? ";

 public static String GET_TARGET_APPROVAL_DATE_ENV = "select date_field6, date_field7, date_field8, projects.place_holder_type from projects join pj_map_items on (pj_map_items.prj_id = projects.id and pj_map_items.isdefault=1 and pj_map_items.map_type = 1) join pj_basics on (pj_basics.id = pj_map_items.pj_basic_id) where projects.id = ?";

 public static String SET_NEXTAUTH_IF_DIFF = "update pj_basics set date_field8 = (case when (case when date_field7 is not null then date_field7 else date_field6 end) not between ?-0.5 and ?+0.5 then ? else date_field8 end) where pj_basics.id in (select pj_map_items.pj_basic_id from pj_map_items where isdefault=1 and wspace_id = ?) ";

 public static String UNSET_MS_START = "update milestones set ms_status=?, start_date = (case when ? is null then start_date else ? end), finish_dt = (case when ? is null then finish_dt else (case when ? > finish_dt-0.5 then ?+1 else finish_dt end) end)  where mstn_id = ? and milestones.alt_date_id in (select alt_date_id from alt_map_items where wspace_id = ? and alt_id = ? and isdefault=1) and target_market=? ";
 public static String UNSET_MS_FIN = "update milestones set ms_status=?, finish_dt = (case when ? is null then finish_dt else ? end), start_date = (case when ? is null then start_date else (case when ? < start_date+0.5 then ?-1 else start_date end) end)  where mstn_id = ? and milestones.alt_date_id in (select alt_date_id from alt_map_items where wspace_id = ? and alt_id = ? and isdefault=1) and target_market=? ";

	public static String CHECK_FOR_SUPPLIER_CBS =
		"select 1 from dual where exists ( " +
		"	select int_val from supplier_multi_attrib where int_val = ? and supplier_id = ? and attrib_id = ? " +
		") ";

	public static String USAGE_WEEKLY_GROUP_PART =
		" ( " +
		"	select count(*) cnt, object_counters.obj_id objid, object_counters.user_id uid, " +
		"	week_timeid.time_id tid, intelli.getEndOfTimeIdIncl(week_timeid.time_id, 0, 3) teid, object_counters.obj_type objt " +
		"	from object_counters, week_timeid " +
		"	where intelli.getDateFor(week_timeid.time_id) <= object_counters.ts " +
		"	and dateadd(dd, 7, intelli.getDateFor(week_timeid.time_id)) > object_counters.ts " +
		"	group by week_timeid.time_id, object_counters.obj_id, object_counters.user_id, object_counters.obj_type " +
		") tv, ";

	public static String USAGE_MONTHLY_GROUP_PART =
		" ( " +
		"	select count(*) cnt, object_counters.obj_id objid, object_counters.user_id uid, " +
		"	month_timeid.time_id tid, intelli.getEndOfTimeIdIncl(month_timeid.time_id, 0, 2) teid, object_counters.obj_type objt " +
		"	from object_counters, month_timeid " +
		"	where intelli.getDateFor(month_timeid.time_id) <= object_counters.ts " +
		"	and dateadd(month, 1, intelli.getDateFor(month_timeid.time_id)) > object_counters.ts " +
		"	group by month_timeid.time_id, object_counters.obj_id, object_counters.user_id, object_counters.obj_type " +
		") tv, ";

	public static String USAGE_QUARTERLY_GROUP_PART =
		" ( " +
		"	select count(*) cnt, object_counters.obj_id objid, object_counters.user_id uid, " +
		"	qtr_timeid.time_id tid, intelli.getEndOfTimeIdIncl(qtr_timeid.time_id, 0, 0) teid, object_counters.obj_type objt " +
		"	from object_counters, qtr_timeid " +
		"	where intelli.getDateFor(qtr_timeid.time_id) <= object_counters.ts " +
		"	and dateadd(month, 3, intelli.getDateFor(qtr_timeid.time_id)) > object_counters.ts " +
		"	group by qtr_timeid.time_id, object_counters.obj_id, object_counters.user_id, object_counters.obj_type " +
		") tv, ";

	public static String USAGE_YEARLY_GROUP_PART =
		" ( " +
		"	select count(*) cnt, object_counters.obj_id objid, object_counters.user_id uid, " +
		"	year_timeid.time_id tid, intelli.getEndOfTimeIdIncl(year_timeid.time_id, 0, 1) teid, object_counters.obj_type objt " +
		"	from object_counters, year_timeid " +
		"	where intelli.getDateFor(year_timeid.time_id) <= object_counters.ts " +
		"	and dateadd(year, 1, intelli.getDateFor(year_timeid.time_id)) > object_counters.ts " +
		"	group by year_timeid.time_id, object_counters.obj_id, object_counters.user_id, object_counters.obj_type " +
		") tv, ";

	public static String USAGE_PROJECTS_PART_1 =
		"select tv.cnt, tv.objid, projects.id, projects.user_given_id, projects.name, users.name, " +
		"projects.port_node_id, tv.tid, tv.uid, tv.teid " +
		"from projects, ";

	public static String USAGE_PROJECTS_PART_2 =
		" prj_portfolio_map, users " +
		"where " +
		"projects.id  = tv.objid " +
		"and prj_portfolio_map.port_node_id = ? " +
		"and projects.id = prj_portfolio_map.prj_id " +
		"and tv.objt = ? " +
		"and tv.tid >= ? " +
		"and tv.tid <= ? " +
		"and users.id = tv.uid ";
		//and users.id = ? 

	public static String USAGE_ORDERS_PART_1 =
		"select tv.cnt, tv.objid, projects.id, projects.user_given_id, projects.name, users.name, " +
		"projects.port_node_id, tv.tid, tv.uid, tv.teid, orders.order_number " +
		"from orders, ";

	public static String USAGE_ORDERS_PART_2 =
		"projects, prj_portfolio_map, users " +
		"where " +
		"orders.id = tv.objid " +
		"and projects.id = orders.prj_id " +
		"and prj_portfolio_map.port_node_id = ? " +
		"and projects.id = prj_portfolio_map.prj_id " +
		"and tv.objt = ? " +
		"and tv.tid >= ? " +
		"and tv.tid <= ? " +
		"and users.id = tv.uid ";
		//and users.id = ? 

	public static String USAGE_SUPPLIERS_PART_1 =
		"select tv.cnt, tv.objid, -1111111, '', suppliers.name, users.name, " +
		"-1111111, tv.tid, tv.uid, tv.teid " +
		"from suppliers, ";

	public static String USAGE_SUPPLIERS_PART_2 =
		" users " +
		"where " +
		"suppliers.id = tv.objid " +
		"and tv.objt = ? " +
		"and tv.tid >= ? " +
		"and tv.tid <= ? " +
		"and users.id = tv.uid ";
		//and users.id = ? 

	public static String UPDATE_ORDER_LAST_UPDATE_INFO =
		"update order_details set updated_by = ?, updated_on = ? where order_id = ? ";

	public static String USERS_LOGIN_GROUP_PART_NEW =
		"( " +
		"	select count(*) cnt, min(ts) mi, max(ts) mx, user_login_track.user_id uid, " +
		"	label "+
		"	from user_login_track join @period on (ts between start_time and end_time) " +
		
		"	group by label, user_id " +
		") tv ";

	public static String USERS_LOGIN_WEEKLY_GROUP_PART =
		"( " +
		"	select count(*) cnt, user_login_track.user_id uid, " +
		"	week_timeid.time_id tid, intelli.getEndOfTimeIdIncl(week_timeid.time_id, 0, 3) teid " +
		"	from user_login_track, week_timeid " +
		"	where intelli.getDateFor(week_timeid.time_id) <= user_login_track.ts " +
		"	and dateadd(dd, 7, intelli.getDateFor(week_timeid.time_id)) > user_login_track.ts " +
		"	group by week_timeid.time_id, user_login_track.user_id " +
		") tv ";

	public static String USERS_LOGIN_MONTHLY_GROUP_PART =
		"( " +
		"	select count(*) cnt, user_login_track.user_id uid, " +
		"	month_timeid.time_id tid, intelli.getEndOfTimeIdIncl(month_timeid.time_id, 0, 2) teid " +
		"	from user_login_track, month_timeid " +
		"	where intelli.getDateFor(month_timeid.time_id) <= user_login_track.ts " +
		"	and dateadd(month, 1, intelli.getDateFor(month_timeid.time_id)) > user_login_track.ts " +
		"	group by month_timeid.time_id, user_login_track.user_id " +
		") tv ";

	public static String USERS_LOGIN_QUARTERLY_GROUP_PART =
		"( " +
		"	select count(*) cnt, user_login_track.user_id uid, " +
		"	month_timeid.time_id tid, intelli.getEndOfTimeIdIncl(month_timeid.time_id, 0, 0) teid " +
		"	from user_login_track, month_timeid " +
		"	where intelli.getDateFor(month_timeid.time_id) <= user_login_track.ts " +
		"	and dateadd(month, 3, intelli.getDateFor(month_timeid.time_id)) > user_login_track.ts " +
		"	group by month_timeid.time_id, user_login_track.user_id " +
		") tv ";

	public static String USERS_LOGIN_YEARLY_GROUP_PART =
		"( " +
		"	select count(*) cnt, user_login_track.user_id uid, " +
		"	year_timeid.time_id tid, intelli.getEndOfTimeIdIncl(year_timeid.time_id, 0, 1) teid " +
		"	from user_login_track, year_timeid " +
		"	where intelli.getDateFor(year_timeid.time_id) <= user_login_track.ts " +
		"	and dateadd(year, 1, intelli.getDateFor(year_timeid.time_id)) > user_login_track.ts " +
		"	group by year_timeid.time_id, user_login_track.user_id " +
		") tv ";

	public static String USERS_LOGIN_COUNT_PART_1 =
		"select tv.cnt, users.name, tv.tid, tv.teid, tv.uid " +
		"from users, ";

	public static String USERS_LOGIN_COUNT_PART_2 =
		"where " +
		"tv.tid >= ? " +
		"and tv.tid <= ? " +
		"and users.id = tv.uid ";
		//and users.id = -1

	public static String GET_CCBS_RELATED_ENTRIES_COUNT_PART_1 =
		"select pur_cost_breakdown.id, pur_cost_breakdown.name, pur_cost_breakdown.lhs_number, " +
		"count(pur_cost_items.cbs_id) ccbs_cnt, " +
		"count(distinct pur_cost_items.prj_id) prj_cnt, count(distinct pur_cost_items_orders.order_id) ord_cnt, " +
		"count(distinct pur_cost_items_suppliers.supplier_id) sup_cnt, sum(ct.cost) cost_total " +
		"from pur_cost_breakdown " +
		"left outer join pur_cost_items on pur_cost_breakdown.id = pur_cost_items.cbs_id " +
		"left outer join pur_cost_items_orders on pur_cost_items.id = pur_cost_items_orders.cost_item_id " +
		"left outer join pur_cost_items_suppliers on pur_cost_items.id = pur_cost_items_suppliers.cost_item_id " +
		"left outer join " +
		"( " +
		"	select pur_cost_item_data.cost_item_id cid, sum(pur_cost_item_data.value) cost " +
		"	from pur_cost_item_data " +
		"	where " +
		"	pur_cost_item_data.cost_type = ( " +
		"		select max(pcd.cost_type) from pur_cost_item_data pcd " +
		"		where pcd.cost_item_id = pur_cost_item_data.cost_item_id " +
		"	) " +
		"	group by pur_cost_item_data.cost_item_id " +
		") ct on pur_cost_items.id = ct.cid ";

	public static String GET_CCBS_RELATED_ENTRIES_COUNT_PART_2 =
		"group by pur_cost_breakdown.lhs_number, pur_cost_breakdown.id, pur_cost_breakdown.name " +
		"order by pur_cost_breakdown.lhs_number ";
    
 //rajeev - for new ytd+monthly mix 010509
 /*
  public static String RECONCILIATION_CAPEX_DATA_PERIOD_GET_MULTI_YTD_PART1 = 
    " select sum( bycurr.dv * intelli.getYTDCurrencyConversion(bycurr.cu, ?, byCurr.ti+35)), bycurr.pni, bycurr.pnname, bycurr.pncode 	"+
		" from ( 	"+
		" 	select sum(measure_data.val * 	"+
		" 			intelli.getPropIncluded(measure_data.val_scope, measure_data.time_val, time_id, 2, measure_data.val_dur) 	"+
		" 		  ) dv, alt_measures.currency_id cu, ? ti, pjpmapl.pni pni, pjpmapl.pnname pnname, pjpmapl.pncode pncode	"+
		" 	from projects, measure_map_items, alternatives, alt_measures, measure_data, measure_case_index, 	"+
		" 	month_timeid, 	"+
		"    ( 	"+
		" 		select prj_multi_attrib.prj_id, midanc.id pni, midanc.name pnname, midanc.external_code pncode, sum(prj_multi_attrib.double_val) dv from 	"+
    "             port_nodes supanc join port_nodes midanc on (	"+
    "               supanc.id = ?	"+
    "               and midanc.lhs_number >= supanc.lhs_number 	"+
    "               and midanc.rhs_number <= supanc.rhs_number	"+
    "               and midanc.org_type in (?,?,?,?)	"+
    "             )	"+
    "             join prj_portfolio_map on (prj_portfolio_map.port_node_id = midanc.id)	"+
    "             join prj_multi_attrib on (prj_portfolio_map.prj_id = prj_multi_attrib.prj_id and prj_multi_attrib.attrib_id=278)					"+
		" 		group by prj_multi_attrib.prj_id, midanc.id, midanc.name, midanc.external_code	"+
		" 	) pjpmapl 	"+
		" 	where 	"+
		" 	pjpmapl.prj_id = projects.id 	"+
		" 	and projects.status in (2) 	"+
		" 	and alternatives.prj_id = projects.id 	"+
		" 	and alternatives.is_primary = 1 	"+
		" 	and measure_map_items.alt_id = alternatives.id 	"+
		" 	and measure_map_items.isdefault = 1 	"+
		" 	and measure_map_items.measure_id = ? 	"+
		" 	and measure_map_items.map_type in (1) 	"+
		" 	and alt_measures.id = measure_map_items.alt_measure_id 	"+
		" 	and time_id >= ? 	"+
		" 	and time_id <= ?	"+
		" 	and measure_data.alt_measure_id = measure_map_items.alt_measure_id 	"+
		" 	and measure_case_index.id = measure_data.measure_case_index_id 	"+
		" 	and time_id >= cast((measure_data.time_val/35) as int)*35 	"+
		" 	and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 	"+
		" 									   when (measure_data.val_scope=1) then 420 	"+
		" 									   when (measure_data.val_scope=2) then 35 	"+
		" 									   when (measure_data.val_scope=3) then 12 	"+
		" 									   when (measure_data.val_scope=5) then measure_data.val_dur*1.26 	"+
		" 									   else 1 	"+
		" 									   end) 	";
*/
public static String RECONCILIATION_CAPEX_DATA_PERIOD_GET_MULTI_YTD_PART1 = 
    " select sum( bycurr.dv * intelli.getYTDCurrencyConversion(bycurr.cu, ?, byCurr.ti+35)), bycurr.pni, bycurr.pnname, bycurr.pncode 	"+
		" from ( 	"+
		" 	select sum(measure_data.val * 	"+
		" 			intelli.getPropIncludedSpecial(measure_data.val_scope, measure_data.time_val, ?, ?, measure_data.val_dur) 	"+
		" 		  ) dv, alt_measures.currency_id cu, ? ti, pjpmapl.pni pni, pjpmapl.pnname pnname, pjpmapl.pncode pncode	"+
		" 	from projects, measure_map_items, alternatives, alt_measures, measure_data, measure_case_index, 	"+
		//" 	month_timeid, 	"+
		"    ( 	"+
		" 		select prj_multi_attrib.prj_id, midanc.id pni, midanc.name pnname, midanc.external_code pncode, sum(prj_multi_attrib.double_val) dv from 	"+
    "             port_nodes supanc join port_nodes midanc on (	"+
    "               supanc.id = ?	"+
    "               and midanc.lhs_number >= supanc.lhs_number 	"+
    "               and midanc.rhs_number <= supanc.rhs_number	"+
    "               and midanc.org_type in (?,?,?,?)	"+
    "             )	"+
    "             join prj_portfolio_map on (prj_portfolio_map.port_node_id = midanc.id)	"+
    "             join prj_multi_attrib on (prj_portfolio_map.prj_id = prj_multi_attrib.prj_id and prj_multi_attrib.attrib_id=278)					"+
		" 		group by prj_multi_attrib.prj_id, midanc.id, midanc.name, midanc.external_code	"+
		" 	) pjpmapl 	"+
		" 	where 	"+
		" 	pjpmapl.prj_id = projects.id 	"+
		" 	and projects.status in (2) 	"+
		" 	and alternatives.prj_id = projects.id 	"+
		" 	and alternatives.is_primary = 1 	"+
		" 	and measure_map_items.alt_id = alternatives.id 	"+
		" 	and measure_map_items.isdefault = 1 	"+
		" 	and measure_map_items.measure_id = ? 	"+
		" 	and measure_map_items.map_type in (1) 	"+
		" 	and alt_measures.id = measure_map_items.alt_measure_id 	"+
		//" 	and time_id >= ? 	"+
		//" 	and time_id <= ?	"+
    " and time_val >= ? "+ //for AM assuming data doesn't span year .. must be beg of year
    " and time_val < ? "+ //for AM assuming data doesn't span year .. must be end of year
    
		" 	and measure_data.alt_measure_id = measure_map_items.alt_measure_id 	"+
		" 	and measure_case_index.id = measure_data.measure_case_index_id 	"+
		//" 	and time_id >= cast((measure_data.time_val/35) as int)*35 	"+
		//" 	and time_id < measure_data.time_val+(case when (measure_data.val_scope = 0) then 105 	"+
		//" 									   when (measure_data.val_scope=1) then 420 	"+
		//" 									   when (measure_data.val_scope=2) then 35 	"+
		//" 									   when (measure_data.val_scope=3) then 12 	"+
		//" 									   when (measure_data.val_scope=5) then measure_data.val_dur*1.26 	"+
		//" 									   else 1 	"+
		//" 									   end) 	";
    " and intelli.isInTimeWindowNew(?, ?, time_val, val_scope, val_dur) = 1 ";

   public static String RECONCILIATION_CAPEX_DATA_PERIOD_GET_MULTI_YTD_PART2 = 
	 "		group by alt_measures.currency_id, pjpmapl.pni, pnname, pncode	"+
	 "		) bycurr 	"+
   "	     group by pni, pnname, pncode order by pnname	";

     public static String RECONCILIATION_CAPEX_DATA_PERIOD_GET_MULTI_MONTHLY_PART1 = 
		"	select sum(measure_data.val * 	"+
		"				intelli.getPropIncludedSpecialCurrency(measure_data.val_scope, measure_data.time_val, ?, ?, measure_data.val_dur, alt_measures.currency_id, ?, ?) 	"+
		"			  ) dv, pjpmapl.pni pni, pjpmapl.pnname pnname, pjpmapl.pncode pncode	"+
		"		from projects, measure_map_items, alternatives, alt_measures, measure_data, measure_case_index, 				"+
		 "	  ( 	"+
		"			select prj_multi_attrib.prj_id, midanc.id pni, midanc.name pnname, midanc.external_code pncode, sum(prj_multi_attrib.double_val) dv from 	"+
     "	           port_nodes supanc join port_nodes midanc on (	"+
     "	             supanc.id = ?	"+
     "	             and midanc.lhs_number >= supanc.lhs_number 	"+
     "	             and midanc.rhs_number <= supanc.rhs_number	"+
     "	             and midanc.org_type in (?,?,?,?)	"+
     "	           )	"+
     "	           join prj_portfolio_map on (prj_portfolio_map.port_node_id = midanc.id)	"+
     "	           join prj_multi_attrib on (prj_portfolio_map.prj_id = prj_multi_attrib.prj_id and prj_multi_attrib.attrib_id=278)					"+
		"			group by prj_multi_attrib.prj_id, midanc.id, midanc.name, midanc.external_code	"+
		"		) pjpmapl 	"+
		"		where 	"+
		"		pjpmapl.prj_id = projects.id 	"+
		"		and projects.status in (2) 	"+
		"		and alternatives.prj_id = projects.id 	"+
		"		and alternatives.is_primary = 1 	"+
		"		and measure_map_items.alt_id = alternatives.id 	"+
		"		and measure_map_items.isdefault = 1 	"+
		"		and measure_map_items.measure_id = ? 	"+
		"		and measure_map_items.map_type in (1) 	"+
		"		and alt_measures.id = measure_map_items.alt_measure_id 	"+
		"		and measure_data.alt_measure_id = measure_map_items.alt_measure_id 	"+
		"		and measure_case_index.id = measure_data.measure_case_index_id 	"+
    "	        and measure_data.time_val >= ? "+//--for AM efficiency assuming that non year cross	and must be beg of year
    "	        and measure_data.time_val < ? "+//--for AM efficiency assuming that non year cross	and must be beg of next year
    "	        and intelli.isInTimeWindowNew(?, ?, measure_data.time_val, measure_data.val_scope, measure_data.val_dur) = 1				";
    public static String RECONCILIATION_CAPEX_DATA_PERIOD_GET_MULTI_MONTHLY_PART2 = 
		"		group by  pjpmapl.pni, pnname, pncode order by pnname	";
		

    public static String RECONCILIATION_BPM_DATA_PERIOD_GET_MULTI_YTD = 
    " select sum(( "+
    " (case when bycurr.fa is null then 0 else bycurr.fa end)+ "+
    " (case when bycurr.gr is null then 0 else bycurr.gr end) "+
    " ) "+
    " * intelli.getYTDCurrencyConversion(bycurr.cu, ?, byCurr.ti+35))+		"+
    " sum(case when bycurr.adj is null then 0 else bycurr.adj * intelli.getYTDCurrencyConversion(bycurr.cua, ?, byCurr.ti+35) end), 	"+
    "    bycurr.pni, pnname, pncode	"+
		" from ( 	"+
    " select "+
    " sum(fixed_assets) fa, sum(grants) gr, sum(adjustments) adj, "+
		//" 	sum(case when fixed_assets  is null then 0 else fixed_assets * intelli.getPropIncluded(2, month_time_id, month_time_id, 	"+
		//" 				  2, 1) end) fa, 	"+
    //" sum(case when grants is null then 0 else grants * intelli.getPropIncluded(2, month_time_id, month_time_id, 	"+
		//" 				  2, 1) end) gr, 	"+
    //"         	sum(case when adjustments is null then 0 else adjustments * intelli.getPropIncluded(2, month_time_id, month_time_id, 	"+
		//" 		  2, 1) end) adj,	"+
    "         midanc.id pni,	midanc.name pnname, midanc.external_code pncode, "+
		" 	currency cu, bpm_adj_currency cua, ? ti 	"+
		" 	from data_reconciliation 	"+
		" 	join 	"+
    " port_nodes leaf on (data_reconciliation.port_node_id = leaf.id "+
    //" 	and year_time_id = ? and data_type = ? and month_time_id >= ? and month_time_id <= ? 	"+ ..to turn on change param stuff
    " )	"+
    " join port_nodes midanc on (midanc.lhs_number <= leaf.lhs_number and midanc.rhs_number >= leaf.rhs_number and midanc.org_type in (?,?,?,?))	"+
    " join port_nodes supanc  on (	"+
    "               supanc.id = ?	"+
    "               and midanc.lhs_number >= supanc.lhs_number 	"+
    "               and midanc.rhs_number <= supanc.rhs_number	"+
    "             )	"+
    "         where 1=1 	"+
		" 	and not exists 	"+
		" 	( 	"+
		" 	select 1 from data_reconciliation ascdat join port_nodes ascdatnode on 	"+
		" 	(ascdatnode.id = ascdat.port_node_id) where 1=1 	"+
		" 	and ascdatnode.lhs_number < leaf.lhs_number 	"+
		" 	and ascdatnode.rhs_number > leaf.rhs_number 	"+
		" 	and midanc.lhs_number <= ascdatnode.lhs_number 	"+
		" 	and midanc.rhs_number >= ascdatnode.rhs_number 	"+
		" 	) 	"+
		" 	and year_time_id = ? and data_type = ? and month_time_id >= ? and month_time_id <= ? 	"+
		" 	group by data_reconciliation.currency, data_reconciliation.bpm_adj_currency, midanc.id, midanc.name, midanc.external_code 	"+
		" ) bycurr 	"+
    "     group by pni, pnname, pncode order by pnname	";


    public static String RECONCILIATION_BPM_DATA_PERIOD_GET_MULTI_MONTHLY = 
		"	select sum(( "+
    " (case when fixed_assets is null then 0 else fixed_assets end) + (case when grants is null then 0 else grants end) "+
    " ) * "+
		"				intelli.getPropIncludedSpecialCurrency(2, month_time_id, ?, ?, 30, currency, ?, ?)  "+
    "	                 ) +  "+
    "	        	sum(case when adjustments is null then 0 else adjustments * intelli.getPropIncludedSpecialCurrency(2, month_time_id, ?, ?, 30, bpm_adj_currency, ?, ?)  "+
    "	                 end ) fa, "+
    "	        midanc.id pni, midanc.name pnname, midanc.external_code pncode "+
		"		from data_reconciliation  "+
		"		join  "+
    "	port_nodes leaf on (data_reconciliation.port_node_id = leaf.id "+
    //"		and year_time_id = ? and data_type = ? and month_time_id >= ? and month_time_id <= ?  "+
    " ) "+
    "	join port_nodes midanc on (midanc.lhs_number <= leaf.lhs_number and midanc.rhs_number >= leaf.rhs_number and midanc.org_type in (?,?,?,?)) "+
    "	join port_nodes supanc  on ( "+
    "	              supanc.id = ? "+
    "	              and midanc.lhs_number >= supanc.lhs_number  "+
    "	              and midanc.rhs_number <= supanc.rhs_number "+
    "	            ) "+
    "	        where 1=1              "+
		"		and not exists  "+
		"		(  "+
		"		select 1 from data_reconciliation ascdat join port_nodes ascdatnode on  "+
		"		(ascdatnode.id = ascdat.port_node_id) where 1=1  "+
		"		and ascdatnode.lhs_number < leaf.lhs_number  "+
		"		and ascdatnode.rhs_number > leaf.rhs_number  "+
		"		and midanc.lhs_number <= ascdatnode.lhs_number  "+
		"		and midanc.rhs_number >= ascdatnode.rhs_number  "+
		"		)  "+
		"		and year_time_id = ? and data_type = ? and month_time_id >= ? and month_time_id <= ?  "+
		"		group by  midanc.id, midanc.name, midanc.external_code order by midanc.name  ";
    
    //AFTER_MERGE
    public static String VERSION_GENERATOR = Misc.G_DO_ORACLE ? "insert into version_generator(id, on_date, obj_type, obj_id, by_user, for_tag) values(?,sysdate,?,?,?)"
                   :
                   "insert into version_generator(on_date, obj_type, obj_id, by_user, for_tag) values(getDate(),?,?,?,?)"
                   ;
    public static String GET_ALL_VERSION = "select version_generator.id, version_generator.on_date, version_generator.obj_id, version_generator.by_user, users.name, for_tag from version_generator left outer join users on (by_user = users.id) where obj_type=? and obj_id=? and (for_tag=? or for_tag=?) order by on_date desc";
    
	public static String GET_USER_PAGE_PREF = "select menu_tag, varname, val from filter_prefs join filter_pref_items on (filter_prefs.id=filter_pref_items.filter_pref_id and filter_prefs.user_1_id = ? and filter_prefs.menu_tag=?) ";
	public static String GET_USER_PAGE_PREF_HEADER = "select id from filter_prefs where menu_tag=? and user_1_id=?";
	public static String INSERT_USER_PAGE_PREF_HEADER = Misc.G_DO_ORACLE ?  "insert into filter_prefs (id, menu_tag, user_1_id) values (?,?,?)" :  "insert into filter_prefs (menu_tag, user_1_id) values (?,?)";
	public static String DELETE_USER_PAGE_PREF_DETAILS = "delete from filter_pref_items where filter_pref_id=?";
	public static String INSERT_USER_PAGE_PREF_DETAILS = "insert into filter_pref_items(filter_pref_id, varname, val) values (?,?,?)";

}
		