package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovDao.LovItemType;
import com.jfoenix.controls.JFXTextField;

import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

/**
 * This class is a TextField which implements an "autocomplete" functionality,
 * based on a supplied list of entries.
 * 
 * @author Caleb Brinkman
 */
public class AutoCompleteTextField {
	private static AutoCompletionBinding<String> autoCompletionBinding;
	LovDao.LovItemType lovItemType;
	ArrayList<String> suggesstionList = null;
	JFXTextField selectedTextBox = null;
	MainController parent=null;
	private int maxLength = 5;

	/** Construct a new AutoCompleteTextField. */
	public AutoCompleteTextField(MainController parent, JFXTextField selectedTextBox, LovDao.LovItemType lovItemType) {
		super();
		this.lovItemType = lovItemType;
		this.selectedTextBox = selectedTextBox;
		this.parent=parent;
	}

	public void setAutoCompleteTextBox() {
		if (selectedTextBox == null)
			return;
		if (this.suggesstionList == null) {
			updateSuggesstionList();
		}

		TextFields.bindAutoCompletion(selectedTextBox, SuggestionProvider.create(suggesstionList));
//		selectedTextBox.setOnKeyPressed((KeyEvent e) -> {
//			switch (e.getCode()) {
//			case ENTER:case TAB:
//				if(lovItemType == LovItemType.VEHICLE) {
//					parent.currentViewController.vehicleNameAction();
//					
//				}else if(lovItemType == LovItemType.DL_NUMBER) {
//					parent.currentViewController.dlNoAction();
//				}
//				String inputText = CacheTrack.standardizeName(selectedTextBox.getText());
//				learnWord(inputText);
//
//				break;
//			
//			default:
//				break;
//			}
//		});
		
		selectedTextBox.setOnAction((ActionEvent e)->{
//				if(lovItemType == LovItemType.VEHICLE) {
//					parent.currentViewController.vehicleNameAction();
//					
//				}else if(lovItemType == LovItemType.DL_NUMBER) {
//					parent.currentViewController.dlNoAction();
//				}
				String inputText = CacheTrack.standardizeName(selectedTextBox.getText());
				learnWord(inputText);
		
		});
			
	}

	private void learnWord(String inputStr) {
		
		if (!suggesstionList.contains(inputStr)) {
			updateSuggesstionList();
		}
		if (autoCompletionBinding != null)
			autoCompletionBinding.dispose();

		autoCompletionBinding = TextFields.bindAutoCompletion(selectedTextBox,
				SuggestionProvider.create(suggesstionList));
		
		
	}

	private void updateSuggesstionList() {
		suggesstionList = LovDao.getVehicleSuggestion(TokenManager.portNodeId, lovItemType);
	}
	/**
	 * Get the existing set of autocomplete entries.
	 * 
	 * @return The existing autocomplete entries.
	 */
}