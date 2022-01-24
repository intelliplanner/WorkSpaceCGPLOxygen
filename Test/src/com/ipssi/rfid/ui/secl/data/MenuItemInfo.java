package com.ipssi.rfid.ui.secl.data;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.readers.TPRWorkstationConfig;

/**
 * Created by ipssi11 on 16-Oct-16.
 */
public class MenuItemInfo {
	public static enum ActionPanelType{
		TPR,
		NON_TPR,
		NONE
	}
    /*private int workstationType;
    private int materialCat;*/
	private String screenTitle;
    public String getScreenTitle() {
		return screenTitle;
	}
	public void setScreenTitle(String screenTitle) {
		this.screenTitle = screenTitle;
	}
	private String screenURL;
    private boolean createNewVehicle;
    private boolean processAuto = false;
    private boolean isLoad = true;
    private String menuTag=null;
    private int privId;
    
    private ActionPanelType actionPanelType = ActionPanelType.TPR;
    
	public ActionPanelType getActionPanelType() {
		return actionPanelType;
	}
	public void setActionPanelType(ActionPanelType actionPanelType) {
		this.actionPanelType = actionPanelType;
	}
	public boolean isLoad() {
		return isLoad;
	}
	public void setLoad(boolean isLoad) {
		this.isLoad = isLoad;
	}
	public boolean isProcessAuto() {
		return processAuto;
	}
	public void setProcessAuto(boolean processAuto) {
		this.processAuto = processAuto;
	}
	private ArrayList<Triple<Integer,Boolean,TPRWorkstationConfig>> registeredReaderList = new ArrayList<>();
	private String menuTitle;
    
    public void registerReaderNotification(int readerId,boolean notify, TPRWorkstationConfig tprWorkstationConfig){
    	if(Misc.isUndef(readerId))
    		return;
    	int readerIndex = getRegisterReaderById(readerId);
    	if(Misc.isUndef(readerIndex)){
    		registeredReaderList.add(new Triple<Integer, Boolean,TPRWorkstationConfig>(readerId, notify,tprWorkstationConfig));
    		readerIndex = registeredReaderList.size()-1;
    	}else{
    		registeredReaderList.get(readerIndex).second = notify;
    	}
    }
    private int getRegisterReaderById(int readerId){
    	for (int i = 0; i < registeredReaderList.size(); i++) {
			if(registeredReaderList.get(i).first == readerId)
				return i;
		}
    	return Misc.getUndefInt();
    }
    public boolean isNotifyRfidUpdates(int readerId){
    	int readerIndex = getRegisterReaderById(readerId);
    	return Misc.isUndef(readerIndex) ? false : registeredReaderList.get(readerIndex).second;
    }
    public TPRWorkstationConfig getTprWorkstionConfig(int readerId){
    	int readerIndex = getRegisterReaderById(Misc.isUndef(readerId) ? Type.Reader.IN :  readerId);
    	return Misc.isUndef(readerIndex) ? null : registeredReaderList.get(readerIndex).third;
    }
    

	public boolean isCreateNewVehicle() {
        return createNewVehicle;
    }

    public void setCreateNewVehicle(boolean createNewVehicle) {
        this.createNewVehicle = createNewVehicle;
    }

    

    public MenuItemInfo() {
    }

    



	public MenuItemInfo(String menuTag,String screenTitle, String screenURL, boolean createNewVehicle,int privId, String menuTitle) {
		super();
		//this.workstationType = workstationType;
		//this.materialCat = materialCat;
		this.menuTag = menuTag;
		this.screenTitle = screenTitle;
		this.screenURL = screenURL;
		this.createNewVehicle = createNewVehicle;
		this.privId = privId;
		this.menuTitle = menuTitle;
		
	}

	/*public int getWorkstationType() {
        return workstationType;
    }

    public void setWorkstationType(int workstationType) {
        this.workstationType = workstationType;
    }

    public int getMaterialCat() {
        return materialCat;
    }
    public void setMaterialCat(int materialCat) {
        this.materialCat = materialCat;
    }*/
    public String getScreenURL() {
        return screenURL;
    }
    public void setScreenURL(String screenURL) {
        this.screenURL = screenURL;
    }
	public int getPrivId() {
		return privId;
	}
	public String getMenuTitle() {
		return menuTitle;
	}
	public String getMenuTag() {
		return menuTag;
	}
    
}
