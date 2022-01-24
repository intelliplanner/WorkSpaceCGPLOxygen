package com.ipssi.rfid.ui.autocompleteText;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.controller.MainController;
import com.ipssi.rfid.ui.data.LovDao;
import com.jfoenix.controls.JFXTextField;

import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.event.ActionEvent;

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
	private static final Logger log = Logger.getLogger(AutoCompleteTextField.class.getName());
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