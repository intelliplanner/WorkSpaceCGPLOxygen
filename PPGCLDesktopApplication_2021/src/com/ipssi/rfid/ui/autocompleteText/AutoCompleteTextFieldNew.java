package com.ipssi.rfid.ui.autocompleteText;

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

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovDao.LovItemType;
import com.jfoenix.controls.JFXTextField;

import impl.org.controlsfx.autocompletion.SuggestionProvider;

/**
 * This class is a TextField which implements an "autocomplete" functionality, based on a supplied list of entries.
 * @author Virendra
 */
public class AutoCompleteTextFieldNew
{
  /** The existing autocomplete entries. */
  private final SortedSet<String> entries;
  /** The popup used to select an entry. */
  private ContextMenu entriesPopup;
  TextField selectedTextField = null;
  List<String> suggesstionList = null;
  private static AutoCompletionBinding<String> autoCompletionBinding;
  /** Construct a new AutoCompleteTextField. */
  public AutoCompleteTextFieldNew(TextField selectedTextField,LovItemType lovItemType) {
    super();
    this.selectedTextField=selectedTextField;
    entries = new TreeSet<>();
    entriesPopup = new ContextMenu();
    selectedTextField.textProperty().addListener(new ChangeListener<String>()
    {
      @Override
      public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
    	  String inputText = CacheTrack.standardizeName(selectedTextField.getText().trim());
    	  if (selectedTextField.getText().trim().length() <= 4)
        {
          entriesPopup.hide();
        } else
        {
        suggesstionList = new ArrayList<>();
        suggesstionList = LovDao.getSuggestion(TokenManager.portNodeId, lovItemType,inputText);
          if (suggesstionList.size() > 0)
          {		
             populatePopup(suggesstionList);
            if (!entriesPopup.isShowing())
            {
            	entriesPopup.show(selectedTextField, Side.BOTTOM, 3,0);
            }
          }
        else
          {
            entriesPopup.hide();
          }
        }
      }

    });

    selectedTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
        entriesPopup.hide();
      }
    });

  }

  

/**
   * Get the existing set of autocomplete entries.
   * @return The existing autocomplete entries.
   */
  public SortedSet<String> getEntries() { return entries; }

  /**
   * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
   * @param searchResult The set of matching strings.
   */
  private void populatePopup(List<String> searchResult) {
    List<CustomMenuItem> menuItems = new LinkedList<>();
    // If you'd like more entries, modify this line.
    int maxEntries = 10;
    int count = Math.min(searchResult.size(), maxEntries);
    for (int i = 0; i < count; i++)
    {
      final String result = searchResult.get(i);
      Label entryLabel = new Label(result);
      CustomMenuItem item = new CustomMenuItem(entryLabel, true);
      item.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override
        public void handle(ActionEvent actionEvent) {
        	selectedTextField.setText(result);
          entriesPopup.hide();
        }
      });
      menuItems.add(item);
    }
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);

  }
}
