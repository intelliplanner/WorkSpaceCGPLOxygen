
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

public class Sequence {
//   public static String GENSEQ = "genseq";
   public static String DW_ALT_SHORT_INFO = "seq_dw_alt_short_info";
   public static String PORT_WKSPS        = "seq_port_wksps";
   public static String CONSTRAINTS       = "seq_constraints";
   public static String WORKSPACES        = "seq_workspaces";
   public static String ALT_LISTS         = "seq_alt_lists";
   public static String ALT_MODELS        = "seq_alt_models";
   public static String HISTORY           = "seq_history";
   //public static String DATA              = "seq_data";
   public static String CASHFLOWS         = "seq_cashflows";
   public static String REV_SEGS          = "seq_rev_segs";
   public static String COST_ITEMS        = "seq_cost_items";
   public static String FTE_ITEMS         = "seq_fte_items";
   public static String ASSUMPTIONS       = "seq_assumptions";
   public static String SENSITIVITY       = "seq_sensitivity";
   public static String NPV               = "seq_npv";
   public static String ALT_DATES         = "seq_alt_dates";
   public static String MILESTONES        = "seq_milestones";
   public static String PLANS             = "seq_plans";
   public static String PJ_MAP_ITEMS      = "seq_pj_map_items";
   public static String LABELS            = "seq_labels";
   public static String PORT_RSETS        = "seq_port_rsets";
   public static String DETAILED_PORT_RSETS="seq_detailed_port_rsets";
   public static String MISC_SEQ          = "seq_misc_seq"; //for example new session id
   public static String ALT_TASKS         = "seq_alt_tasks"; //for the task model
   public static String TASKS             = "seq_tasks"; //for the list of tasks
   public static String DELIVERY          = "seq_delivery";
   public static String RESOURCE_REQ      = "seq_resource_req";
   public static String PJ_HISTORY        = "seq_pj_history";
   public static String ALTERNATIVES      = "seq_alternatives";
   public static String ALT_BASICS        = "seq_alt_basics";
   public static String ALT_MAP_ITEMS     = "seq_alt_map_items";
   public static String ALT_PROFILES      = "seq_alt_profiles";
   public static String CASES             = "seq_cases";
   public static String CLAIMS            = "seq_claims";
   public static String PROJECTS          = "seq_projects";
   public static String PJ_BASICS         = "seq_pj_basics";
   public static String FILES             = "seq_files";
   
   public static String CNSTRT_VALS       = "seq_cnstrt_vals";   
   public static String DETAILED_PORT_RSETS_MS="seq_detailed_port_rsets_ms";
   public static String PLAN_WF_STEPS     = "seq_plan_wf_steps";
   public static String PLAN_WF_STATUS     = "seq_plan_wf_status";
   public static String PROJECT_WF_STEPS     = "seq_project_wf_steps";
   public static String PROJECT_WF_STATUS     = "seq_project_wf_status";

   public static String USERS             = "seq_users";
   public static String ROLES             = "seq_roles";
   public static String USER_ROLES        = "seq_user_roles";

   public static String MEASURE_MAP_ITEMS   = "seq_measure_map_items";
   public static String ALT_MEASURES        = "seq_alt_measures";
   public static String MEASURE_CASE_INDEX  = "seq_measure_case_index";
   public static String MEASURE_UNCERT_SPEC = "seq_measure_uncert_spec";

   public static String ALT_WORKS = "seq_alt_works";
   public static String ALT_WORK_ITEMS = "seq_alt_work_items";

   public static String SEQ_ALT_FTE_MODEL = "seq_alt_fte_model";
   public static String SEQ_ALT_REV_MODEL = "seq_alt_rev_model";
   public static String SEQ_ALT_DEVCOST_MODEL = "seq_alt_devcost_model";
   public static String SEQ_ALT_OPCOST_MODEL = "seq_alt_opcost_model";
   public static String SEQ_ALT_COMBINED_MODEL = "seq_alt_combined_model";
   public static String SEQ_ALT_RATINGS = "seq_alt_rating_id";
   public static String FTE_HEADS = "seq_fte_head_id ";
   
   public static String SEQ_MKTG_IMPACT_DATA = "seq_mktg_impact_data";
   public static String SEQ_MKTG_ITEM_DETAIL = "seq_mktg_item_detail";
	public static String SEQ_MKTG_SKU_GROUP = "seq_mktg_sku_group";
	public static String SEQ_MKTG_OFFER = "seq_mktg_offer";
	public static String SEQ_MKTG_ITEM_GROUP_LIST = "seq_mktg_item_group_list";
	public static String SEQ_MKTG_CAMPAIGN = "seq_mktg_campaign";
	public static String SEQ_MKTG_SCHEDULE = "seq_mktg_schedule";
  public static String SEQ_MKTG_CONTENTS = "seq_mktg_contents";
  public static String SEQ_PRJ_DEP_ORS = "seq_prj_dep_ors";
  public static String SEQ_RISK_HEADERS = "seq_risk_header";
  public static String SEQ_RISK_ITEMS = "seq_risk_item_id";
  public static String SEQ_RISK_ACTION_ITEMS = "seq_risk_action_item_id";
  public static String SEQ_RISK_CONTINGENCY = "seq_risk_contingency_id";
  public static String SEQ_RISK_BUDGET_HEAD = "seq_risk_budget_head_id";
  public static String SEQ_PORT_NODES = "seq_port_nodes";
  public static String SEQ_WKF_APPROVAL_PACKAGES = "seq_wkf_approval_packages";  
  public static String SEQ_CURRENCY_LIST = "seq_currency_list";
  public static String SEQ_MEETING_LIST = "seq_meeting_lists";
  public static String SEQ_PUR_COST_ITEMS = "seq_pur_cost_items";
	public static String SEQ_ORDERS = "seq_orders";
	public static String SEQ_SUPPLIERS = "seq_suppliers";
  public static String SEQ_SEARCH = "seq_search";
  public static String SEQ_GENERAL_PARAM = "seq_general_param";
  public static String SEQ_PARAMETERS = "seq_parameters";
  public static String SEQ_PUR_COST_BREAKDOWN = "seq_pur_cost_breakdown";
  public static String SEQ_CONSTRAINT_HEADER = "seq_constraint_header";
  public static String SEQ_VERSION_GENERATOR = "seq_version_generator"; //AFTER_MERGE
   public Sequence() {}

}