package com.ipssi.gen.utils;

import java.util.*;
import org.w3c.dom.*;
// Referenced classes of package com.ipssi.gen.utils:
//            Misc

public class WkspStepNew
{
    public static class CurrStateInfo
    {

        public StateMap m_start;
        public HashMap m_lookup;
        public StateMap m_current;
        public StateMap m_previousState = null;
        public String m_inComment = null;
        public HashMap m_criteriaInfo = null;


        public CurrStateInfo()
        {
            m_start = null;
            m_lookup = null;
            m_current = null;
        }
    }

    public static class StateMap
    {

        public WkspStepNew m_stateInfo;
        public boolean m_manual;
        public boolean m_primary;
        public ArrayList m_approvals;//
        public ArrayList m_acceptNext;
//        public ArrayList m_altNextImm;
        public ArrayList m_rejectNext;
        public int m_seq;
        public ArrayList m_actionsTrans = null; //we will see if we need a transition based actions ... if not currently not used nor loaded
        public boolean m_isHidden = false;

        public StateMap()
        {
            m_stateInfo = null;
            m_manual = false;
            m_primary = false;
            m_approvals = new ArrayList(5);
            m_acceptNext = new ArrayList(5);
//            m_altNextImm = new ArrayList(5);
            m_rejectNext = new ArrayList(5);
            m_seq = 0;

        }
    }

    public static class Approval
    {

        public Date m_start;
        public Date m_end;
        public int m_uid;
        public String m_uname;
        public String m_email;
        public int m_approvalType;
        public String m_comments;
        public int m_approverRole;

        public Approval()
        {
            m_start = null;
            m_end = null;
            m_uid = Misc.getUndefInt();
            m_uname = null;
            m_email = null;
            m_approvalType = 0;
            m_comments = null;
            m_approverRole = Misc.getUndefInt();
        }
    }

    public static class Approver
    {

        public int m_uid;
        public String m_uname;
        public String m_email;

        public Approver()
        {
            m_uid = Misc.getUndefInt();
            m_uname = null;
            m_email = null;
        }
    }

    public static class Action
    {
		public static final int g_updFlagAction = 0; //081808 dxxx=-undef, then d2=tells approach (from session param 0, from parameter 1), d3=tells the parameter id or parmaterName prefix d4=tells the parameter dimId to use to get val if the session var is empty,  d5=tells deault from the action written if the d4 is undef
		public static final int g_copyData = 1; //081808 from is d0, to is d1, d2 is winscope, d3 is winval, d4 is ratelisttyp d5=1 tells if doing cutoff
		//d6=tells approach (from session param, from parameter) for getting window, d7=tells the parameter name or dimId, d8=tells default to get from prop, d9=tells deault from the action written
        public static final int g_setBaseline = 2; //no param
		public static final int g_setYear = 3; //081808 dxxx=-1001 BY, dxxx=0 relative to current year; dxxx=-undef, then d1=tells approach (from session param, from parameter), d2=tells the parameter name or dimId, d3=tells default to get from prop, d4=tells deault from the action written
		//dxxx=-undef, then d2=tells approach (from session param 0, from parameter 1), d3=tells the parameter id or parmaterName prefix d4=tells the parameter dimId to use to get val if the session var is empty,  d5=tells deault from the action written if the d4 is undef
		public static final int g_setMSStartComplete = 4; //081808 d0 for ms, d1=0 -> start, 1 ->end        
		//d2=tells approach (from session param, from parameter) for getting window, d3=tells the parameter name or dimId, d4=tells default to get from prop, d6=tells deault from the action written
        public static final int g_labelWorkspace = 5; //d0 ... name parameter if it exists
        public static final int g_changeSectionReadAbility = 6; //0 string of context, 1=>if read
        public static final int g_makeNonPrimaryAltNonvisible = 7;
        public static final int g_clearData = 8; //d0 ... measure        
        public static final int g_customAction = 9; //d0 ... custom action code
		public static final int g_setMSUnstartedAndFromField = 10; //081808 d0=ms, d1=0 -> start, 1 -> finish , d2=from field, if undef
        public int m_actionId;
        public ArrayList m_actionParam;
        public int getParamInt(int dimId) {
               for (int i=0,is=m_actionParam.size();i<is;i++) {
                  Pair pair = (Pair) m_actionParam.get(i);
                  if (pair.first == dimId)
                    return pair.second;
               }
               return Misc.getUndefInt();
        }
        public String getParamString(int dimId) {
               for (int i=0,is=m_actionParam.size();i<is;i++) {
                  Pair pair = (Pair) m_actionParam.get(i);
                  if (pair.first == dimId)
                    return pair.secondStr;
               }
               return null;
        }

        public Action()
        {
            m_actionId = -1;
            m_actionParam = new ArrayList();
        }
    }

    public static class CondAction {
        public ArrayList m_conditions = null; //ArrayList of CondPair .... OR to be achieved through multiple CondActions
        public ArrayList m_actions = null; //ArrayList of Action ... remember each action itself has an actionParamVector!
        public CondAction(ArrayList cond, ArrayList act) {
           m_conditions = cond;
           m_actions = act;
        }
    }

    public static class Pair //misnomer really want a union of AtomicDataTypes or an object for second with type info
    {
        public int first;
        public int second;
        public String secondStr;
        public Pair(int f, int s, String str)
        {
            first = f;
            second = s;
            secondStr = str;
            if (secondStr == null)
               secondStr = Integer.toString(second);
        }
    }

    public static class CompletionRule {
        public ArrayList m_condition = new ArrayList(); //if project meets these conditions
        public ArrayList m_sectionsToFill = new ArrayList(); //list of pgContext that needs to be filled

        //for a workflowState .. there would be list of CompletionRules
    }
    public static class TransPair
    {

        public int m_toId;
        public boolean m_isPrimary;
        public boolean m_isMandatory;
        public boolean m_isReject;
        public boolean m_isManual;
        public ArrayList m_condition; //ArrayList of CondPair ... the condition that should be apply for this to be picked up
        public ArrayList m_actions; //ArrayList of CondAction ... each is pair of condition and actions
        public boolean m_isHidden = false;


        public TransPair()
        {
            m_toId = -1;
            m_isPrimary = true;
            m_isMandatory = true;
            m_isReject = false;
            m_isManual = false;
            m_condition = new ArrayList(); //each condition is an and of dims having one values from valList
            m_isHidden = false;
        }
    }

    public static class CondPair
    {

        public int m_dimId;
        public ArrayList m_vals;

        public CondPair()
        {
            m_dimId = -1;
            m_vals = new ArrayList();
        }
        public CondPair (int d, ArrayList vl) {
            m_dimId = d;
            m_vals = vl;
        }
    }
    public static void readCondition(ArrayList condition, Element e)
    {
        for(Node n = e != null ? e.getFirstChild() : null; n != null; n = n.getNextSibling()) {
            if(n.getNodeType() != 1)
                continue;
            Element ne = (Element)n;

            WkspStepNew.CondPair cond = readCondInfo(ne);
            if(cond != null)
                condition.add(cond);
        }

    }

    public static WkspStepNew.CondPair readCondInfo(Element e)
    {
        if(e == null)
            return null;
        if (!"dim".equals(e.getTagName()))
           return null;
        int dimId = Misc.getParamAsInt(e.getAttribute("id"));
        if(dimId < 0)
            return null;
        WkspStepNew.CondPair retval = new WkspStepNew.CondPair();
        retval.m_dimId = dimId;
        for(Node n = e.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if(n.getNodeType() != 1)
                continue;
            Element ne = (Element)n;
            int val = Misc.getParamAsInt(ne.getAttribute("id"));
            if(val == Misc.G_HACKANYVAL)
                return null;
            retval.m_vals.add(new Integer(val));
        }

        if(retval.m_vals.size() == 0)
            return null;
        else
            return retval;
    }

    public WkspStepNew()
    {
        m_stepId = -1;
        m_name = null;
        m_desc = null;
        m_duration = 10;
        m_toList = null;//new ArrayList(); //ArrayList of TransPair
        m_skippable = false;
        m_visited = false;
        m_start = false;
        m_level = 0;
        m_templateId = Misc.getUndefInt();
        m_approverRolesNew = null; //new ArrayList();// ArrayList of CondApproverRoles
        m_completionRules = null; //new ArrayList(); //of CompletionRule
    }
    
    public static class CondApproverRoles {
        public ArrayList m_conditions = null; //the condition to be met
        public ArrayList m_approverRoles = null; //the approvals needed ArrayList of Integer
        public boolean m_getApprovalInSeq = false;
        public boolean m_isbakup = false;
        public CondApproverRoles(ArrayList conditions, ArrayList approverRoles, boolean getApprovalInSeq, boolean isbakup) {
           m_conditions = conditions;
           m_approverRoles = approverRoles;
           m_getApprovalInSeq = getApprovalInSeq;
        }
    }
    
    public static TransPair getTransPairEntry(ArrayList theList, int toId)
    {
        TransPair retval = null;
        int i = 0;
     //   for(int is = theList.size(); i < is; i++)
     //   {
     //       retval = (TransPair)theList.get(i);
     //       if(retval.m_toId == toId)
     //           return retval;
     //   }

        retval = new TransPair();
        retval.m_toId = toId;
        theList.add(retval);
        return retval;
    }

    public static class MenuNext {
        public static final String g_approveTag = "workflow_prj";
        public static final String g_selfTag = "_self";
        public String m_tag = ""; //
        public ArrayList m_conditions = new ArrayList();
    }

    public static final int SET_STATE = 1;
    public static final int SEND_EMAIL = 2;
    public static final int SET_BASELINE = 3;
    public static final int MAKE_CURRENT = 4;
    public static final int SET_BUDGET = 5;
    public static final int UNSET_BUDGET = 6;
    public static final int APPROVAL_PENDING = 0;
    public static final int APPROVAL_NORMAL = 1;
    public static final int APPROVAL_FORCED = 2;
    public static final int APPROVAL_REJECTED = 3;
    public static final int APPROVAL_MOVED = 9;
    public static final int APPROVAL_REWORK = 10;
    public static final int CURR_NO_APPROVAL_NEEDED = 0;
    public static final int CURR_PENDING_APPROVAL = 1;
    public static final int CURR_LAST_APPROVAL = 2;
    public static final int CURR_REJECTED = 3;
    public static final int CURR_APPROVED = 4;
    public int m_stepId;
    public String m_name;
    public String m_desc;
    public int m_duration;
 //   public ArrayList m_actions;
    public ArrayList m_preaction = null;
    public ArrayList m_postaction = null;
    public ArrayList m_toList;
    public boolean m_skippable;
    public boolean m_visited;
    public boolean m_start;
    public int m_level;
    public int m_templateId;
//    public ArrayList m_approverRoles;
    public ArrayList m_completionRules = null;
    public ArrayList m_approverRolesNew = null; //CondApproverRoles
    
    public static class OverrideInfo {
       int m_forRole = Misc.getUndefInt();
       int m_forUser = Misc.getUndefInt();
       public OverrideInfo(int forRole, int forUser) {
          m_forRole = forRole;
          m_forUser = forUser;
       }
    }
}