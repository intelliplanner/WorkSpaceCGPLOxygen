package com.ipssi.processor.utils;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.modeler.ModelSpec;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;

/**
 * immutable class
 * 
 * @author Kapil
 * 
 */
public class Dimension implements Comparable<Dimension>, Serializable {
	private static Logger logger = Logger.getLogger(Dimension.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static HashMap<Integer, Dimension> g_dimMap = new HashMap<Integer, Dimension>(100, 0.75f);
	   public static Collection<Dimension> getDimList() { return g_dimMap.values(); }
	   private static ArrayList<Dimension> dimsWithModel = new ArrayList<Dimension>();
	   private transient boolean isUsedInModel = false; //note that transients are restored using overridden 
	   private transient boolean isCummulative = false;
	   
	   public boolean isCummulative() {
		   return isCummulative;
	   }
	   public boolean isPowerOnIndicator() {
		   return getId() == 2;
	   }
	   public boolean isUsedInModel() {
		   return isUsedInModel ;
	   }
	   
	   public static ArrayList<Dimension> getDimsWithModel() {
		   return dimsWithModel;
	   }
	   
	   public static Dimension getDimInfo(int id) {
		   return g_dimMap.get(id);
	   }
	   
	   public static class Dependency implements Cloneable , Serializable{
		   private static final long serialVersionUID = 1L;
		   public Dimension m_dimInfo = null;
		   public int m_prevValNeeded = 0;
		   public Dependency(Dimension dimInfo, int prevValNeeded) {
			   m_dimInfo = dimInfo;
			   m_prevValNeeded = prevValNeeded;
		   }
		   public Dependency clone() {
			   return new Dependency(m_dimInfo, m_prevValNeeded);
		   }
		   public static void mergeIntoLHS(ArrayList<Dependency> lhs, ArrayList<Dependency> rhs) {
			   if (lhs == null)
				   return;
			   if (rhs != null) {
					for (Dimension.Dependency dep : rhs) {
						boolean merged = false;
						for (Dimension.Dependency ret : lhs) {
							if (ret.m_dimInfo.equals(dep.m_dimInfo)) {
								if (ret.m_prevValNeeded > dep.m_prevValNeeded)
									ret.m_prevValNeeded = dep.m_prevValNeeded;
								merged = true;
								break;
							}
						}
						if (!merged)
							lhs.add(dep.clone());
					}
			   }
		   }
	   }
	   
	   public transient ArrayList<Dependency> m_dependencies = new ArrayList<Dependency>();
	   
	   public int m_id = Misc.getUndefInt();
	   public transient ModelSpec modelSpec = null;
	   public Dimension(int id) {
		   m_id = id;
	   }
	   
	   public String toString() {
		   return "d"+Integer.toString(m_id);
	   }
	   
	   public static Dimension add(int id) {
		   Dimension retval = new Dimension(id);
		   g_dimMap.put(id, retval);
		   return retval;
	   }
	   
	   public static void init(String fileName) throws Exception {
		   if (fileName == null) {
				  fileName = Cache.serverConfigPath+System.getProperty("file.separator")+"rule_dim.xml";
		  }
		  try {
			  FileInputStream inp = new FileInputStream(fileName);
		      MyXMLHelper test = new MyXMLHelper(inp, null);
		      Document configXML = test.load();       
		      inp.close();
		      inp = null;
		      test = null;
		      if (configXML == null || configXML.getDocumentElement() == null)
		    	  return;
		      //format: <data>
		      //                 <dim id="x">
		      //                     <dep id="y" dep_till="1"/>
		     //                  </dim>
		     //                   </data>
		      //first get list of dim
		      for (Node n = configXML == null || configXML.getDocumentElement() == null ? null : configXML.getDocumentElement().getFirstChild();n != null ; n=n.getNextSibling()) {
		    	  if (n.getNodeType() != 1)
		    		  continue;
		    	  Element e = (Element) n;
		    	  int id = Misc.getParamAsInt(e.getAttribute("id"));
		    	  if (!Misc.isUndef(id)) {
		              Dimension dim = add(id);
		              dim.isCummulative = "1".equals(e.getAttribute("is_cumm"));
		    	  }
		      }
		      for (Node n = configXML == null || configXML.getDocumentElement() == null ? null : configXML.getDocumentElement().getFirstChild();n != null ; n=n.getNextSibling()) {
		    	  if (n.getNodeType() != 1)
		    		  continue;
		    	  Element e = (Element) n;
		    	  int id = Misc.getParamAsInt(e.getAttribute("id"));
		    	  Dimension dimInfo = getDimInfo(id);
		    	  boolean foundDep = false;
		    	  for (Node cn = e.getFirstChild();cn != null ; cn=cn.getNextSibling()) {
		    		  if (cn.getNodeType() != 1)
		    			  continue;
		    		  Element ce = (Element) cn;
		    		  Dimension depDim = getDimInfo(Misc.getParamAsInt(ce.getAttribute("id")));
		    		  if (depDim == null)
		    			  continue;
		    		  int depTill = Misc.getParamAsInt(ce.getAttribute("dep_till"),0);
		    	     dimInfo.m_dependencies.add(new Dependency(depDim, depTill));
		    	     foundDep = true;
		    	  }
		    	  if ( !foundDep && dimInfo.m_id != 0 ){
		    		  dimInfo.m_dependencies.add(new Dependency(getDimInfo(dimInfo.m_id),0));
		    	  }
		      }
		      try {
			      fileName = Cache.serverConfigPath+System.getProperty("file.separator")+"models.xml";
				  inp = new FileInputStream(fileName);
			      test = new MyXMLHelper(inp, null);
			      configXML = test.load();       
			      inp.close();
			      inp = null;
			      test = null;
			      if (configXML == null || configXML.getDocumentElement() == null)
			    	  return;
			      ArrayList<ModelSpec> specs = ModelSpec.readModels(configXML.getDocumentElement());
			      ModelSpec defaultSpec = null;
			      for (ModelSpec spec:specs) {
			    	  int forAttrib = spec.forAttribId;
			    	  if (forAttrib < 0)
			    		  defaultSpec = spec;
			    	  Dimension dimInfo = Dimension.getDimInfo(forAttrib);
			    	  if (dimInfo != null) {
			    		  dimInfo.modelSpec = spec;
			    		  dimInfo.isUsedInModel = true;
			    		  dimsWithModel.add(dimInfo);
			    		  if (!spec.deltaByTime && spec.deltaDimId >= 0) {
			    			  Dimension t1 = Dimension.getDimInfo(spec.deltaDimId);
			    			  if (t1 != null)
			    				  t1.isUsedInModel = true;
			    		  }
			    	  }
			      }
			      if (defaultSpec != null) {
		    		  if (!defaultSpec.deltaByTime && defaultSpec.deltaDimId >= 0) {
		    			  Dimension t1 = Dimension.getDimInfo(defaultSpec.deltaDimId);
		    			  if (t1 != null)
		    				  t1.isUsedInModel = true;
		    		  }
			    	  Collection<Dimension> dimList = g_dimMap.values();
			    	  for (Dimension dim:dimList) {
			    		  if (dim.modelSpec == null) {
			    			  dim.modelSpec = defaultSpec;
			    			  dim.isUsedInModel = true;
			    			  dimsWithModel.add(dim);
			    		  }
			    	  }
			      }
		      }
		      catch (Exception e1) {
		    	  //eat it
		      }
		  }
		  catch (Exception e) {
			  e.printStackTrace();
			  //eat it
		  }
	      Dimension distDim = getDimInfo(ApplicationConstants.DIST_DIM);
	      if (distDim == null) {
	    	  add(ApplicationConstants.DIST_DIM);
	      }
	   }

	   static {
	      try {
	   		   init(null);
	   	   }
	      catch (Exception e) {
	   		   e.printStackTrace();
	   		   //eat it .. cant do anything about it
	      }
	   }

   public int getId() {
	   return m_id;
   }
	public int compareTo(Dimension dimension) {
		return dimension == null ? 1 : m_id-dimension.getId();
	}


	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		} else {
			if (obj instanceof Dimension && obj != null) {
				return m_id == ((Dimension)obj).getId();
			}
		}
		return false;
	}
	
	public int hashConde() {
		return m_id;
	}
	public ModelSpec getModelSpec() {
		return modelSpec;
	}
	public void setModelSpec(ModelSpec modelSpec) {
		this.modelSpec = modelSpec;
	}

}
