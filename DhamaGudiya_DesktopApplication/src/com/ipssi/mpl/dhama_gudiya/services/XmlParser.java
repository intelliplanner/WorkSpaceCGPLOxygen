/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.mpl.dhama_gudiya.services;

import com.ipssi.beans.ComboItemNew;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.User;
import com.ipssi.rfid.ui.controller.MainController;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author IPSSI
 */
public class XmlParser {

    public static User userDataParser(String userXmlData) {
        //      userXmlData="<result><object id='40011' ><field id='1' userName='3' password='3' name='3'/></object></result>";    
        User user = null;
        try {
            org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(userXmlData);
            if (xmlDoc == null) {
                return null;
            }
            org.w3c.dom.NodeList root = xmlDoc.getElementsByTagName("field");
            if (root != null && root.item(0) != null) {
                user = new User();
                user.setId(Integer.valueOf(root.item(0).getAttributes().getNamedItem("id").getNodeValue()));
                user.setUsername(root.item(0).getAttributes().getNamedItem("userName").getNodeValue());
                user.setName(root.item(0).getAttributes().getNamedItem("name").getNodeValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static ArrayList<Integer> userPrivilegeParser(String privilegeData) {
        //<result><object id="40010" ><field prev="80001" />
        //<field prev="80002" /><field prev="80003" /><field prev="80004" /></object></result>	
        ArrayList<Integer> retval = null;
        try {
            org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(privilegeData);
            if (xmlDoc == null) {
                return null;
            }

            org.w3c.dom.NodeList root = xmlDoc.getElementsByTagName("field");
            if (root != null && root.getLength() > 0) {
                retval = new ArrayList<Integer>();
                for (int i = 0, js = root.getLength(); i < js; i++) {
                    retval.add(Integer.valueOf(root.item(i).getAttributes().getNamedItem("prev").getNodeValue()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }

    public static void transporterDataParser(String privilegeData) {
//<result><object id="40003" ><field id="1" name="AKAL" prefix="lr" /><field id="2" name="BKB" prefix="lr" /><field id="6" name="Ipssi" prefix="lr" /></object></result>
        //       ArrayList<ComboItemNew> retval = null;
        ComboItemNew item = null;
        try {
            org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(privilegeData);
            if (xmlDoc == null) {
                return;
            }
            org.w3c.dom.NodeList root = xmlDoc.getElementsByTagName("field");
            if (root != null && root.getLength() > 0) {
                MainController.transporterList = new ArrayList<ComboItemNew>();
                for (int i = 0, js = root.getLength(); i < js; i++) {
                    int id = Integer.valueOf(root.item(i).getAttributes().getNamedItem("id").getNodeValue());
//                  int minesId= Integer.valueOf(root.item(i).getAttributes().getNamedItem("mines_id").getNodeValue());
                    String name = root.item(i).getAttributes().getNamedItem("name").getNodeValue();
                    String prefix = root.item(i).getAttributes().getNamedItem("prefix").getNodeValue();
                    item = new ComboItemNew(id, name, Misc.getUndefInt(), prefix);
                    MainController.transporterList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // return retval;
    }

    public static void minesDataParser(String privilegeData) {
//        <result><object id="40001" ><field id="52" name="Govindpur" prefix="" />
//<field id="3" name="Dhansar" prefix="" /><field id="33" name="Nichitpur" prefix="" />
//<field id="21" name="Khasmahal" prefix="" /><field id="21" name="Khasmahal" prefix="" />
//<field id="1" name="Bansjora" prefix="" /><field id="1" name="Bansjora" prefix="" />
//<field id="1" name="Bansjora" prefix="" /></object></result>
        //     ArrayList<ComboItemNew> retval = null;
        ComboItemNew item = null;
        try {
            org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(privilegeData);
            if (xmlDoc == null) {
                return;
            }
            org.w3c.dom.NodeList root = xmlDoc.getElementsByTagName("field");
            if (root != null && root.getLength() > 0) {
                MainController.minesList = new ArrayList<ComboItemNew>();
                for (int i = 0, js = root.getLength(); i < js; i++) {
                    int id = Integer.valueOf(root.item(i).getAttributes().getNamedItem("id").getNodeValue());
//                    int minesId= Integer.valueOf(root.item(i).getAttributes().getNamedItem("mines_id").getNodeValue());
                    String name = root.item(i).getAttributes().getNamedItem("name").getNodeValue();
                    String prefix = root.item(i).getAttributes().getNamedItem("prefix").getNodeValue();
                    item = new ComboItemNew(id, name, Misc.getUndefInt(), prefix);
                    MainController.minesList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doNoDataParser(String privilegeData) {
        //<result><object id="40002" ><field id="1" name="do123" mines_id="1" /><field id="2" name="do1234" mines_id="1" /></object></result>
        // ArrayList<ComboItemNew> retval = null;
        ComboItemNew item = null;
        try {
            org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(privilegeData);
            if (xmlDoc == null) {
                return;
            }
            org.w3c.dom.NodeList root = xmlDoc.getElementsByTagName("field");
            if (root != null && root.getLength() > 0) {
                MainController.doList = new ArrayList<ComboItemNew>();
                for (int i = 0, js = root.getLength(); i < js; i++) {
                    int id = Integer.valueOf(root.item(i).getAttributes().getNamedItem("id").getNodeValue());
                    int minesId = Integer.valueOf(root.item(i).getAttributes().getNamedItem("mines_id").getNodeValue());
                    String name = root.item(i).getAttributes().getNamedItem("name").getNodeValue();
                    item = new ComboItemNew(id, name, minesId, null);
                    MainController.doList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return retval;
    }

    public static void gradeDataParser(String privilegeData) {
        //<result><object id="40004" ><field id="16" name="Grade One" /><field id="17" name="Grade Two" />
        //<field id="18" name="Grade Three" /><field id="19" name="Grade Four" /><field id="20" name="BITUMINOUS COAL" />
        //      </object></result>
//        ArrayList<ComboItem> retval = null;
        ComboItem item = null;
        try {
            org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(privilegeData);
            if (xmlDoc == null) {
                return;
            }
            org.w3c.dom.NodeList root = xmlDoc.getElementsByTagName("field");
            if (root != null && root.getLength() > 0) {
                MainController.gradeList = new ArrayList<ComboItem>();
                for (int i = 0, js = root.getLength(); i < js; i++) {
                    int id = Integer.valueOf(root.item(i).getAttributes().getNamedItem("id").getNodeValue());
                    String name = root.item(i).getAttributes().getNamedItem("name").getNodeValue();
                    item = new ComboItem(id, name);
                    MainController.gradeList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return retval;
    }
    
    
    

    public static void main(String[] args) {
        userDataParser("");
    }

    public static Pair<Integer, String> getVehicleInformation(String vehicleName, String epcId) throws IOException {
        String vehicleData = HttpClientServicesImpl.getVehicleInformation(vehicleName, epcId);
        Pair<Integer,String> valPair = null;
        try {
            org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(vehicleData);
            if (xmlDoc == null) {
                return null;
            }
            org.w3c.dom.NodeList root = xmlDoc.getElementsByTagName("field");
            if (root != null && root.getLength() > 0) {
                for (int i = 0, js = root.getLength(); i < js; i++) {
                    int vehicle_id = Integer.valueOf(root.item(i).getAttributes().getNamedItem("vehicleId").getNodeValue());
                    String vehicle_name = root.item(i).getAttributes().getNamedItem("vehicleName").getNodeValue();
                    valPair = new Pair<Integer,String>(vehicle_id,vehicle_name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valPair;
    }
}
