/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.beans;

/**
 *
 * @author IPSSI
 */
public class MenuItemInfo {

	private String screenTitle;
	private String screenURL;
	private boolean isLoad = true;
	private String menuTag = null;
	private int privId;
	private String menuTitle;

	public MenuItemInfo(String menuTag, String screenTitle, String screenURL, boolean createNewVehicle, int privId,
			String menuTitle) {
		super();
		// this.workstationType = workstationType;
		// this.materialCat = materialCat;
		this.menuTag = menuTag;
		this.screenTitle = screenTitle;
		this.screenURL = screenURL;
		this.privId = privId;
		this.menuTitle = menuTitle;

	}

	public String getMenuTitle() {
		return menuTitle;
	}

	public void setMenuTitle(String menuTitle) {
		this.menuTitle = menuTitle;
	}

	public String getScreenTitle() {
		return screenTitle;
	}

	public void setScreenTitle(String screenTitle) {
		this.screenTitle = screenTitle;
	}

	public String getScreenURL() {
		return screenURL;
	}

	public void setScreenURL(String screenURL) {
		this.screenURL = screenURL;
	}

	public boolean isIsLoad() {
		return isLoad;
	}

	public void setIsLoad(boolean isLoad) {
		this.isLoad = isLoad;
	}

	public String getMenuTag() {
		return menuTag;
	}

	public void setMenuTag(String menuTag) {
		this.menuTag = menuTag;
	}

	public int getPrivId() {
		return privId;
	}

	public void setPrivId(int privId) {
		this.privId = privId;
	}
}
