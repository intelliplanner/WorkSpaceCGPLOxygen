package com.ipssi.rfid.ui.secl.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ipssi.rfid.processor.Utils;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class DigitalClockExt{
	Label label = null;
	Timeline timeline = null;
	String dateFormat = "dd/MM/yyyy HH:mm:ss";
	SimpleDateFormat sdf = null;
	public DigitalClockExt(Label label,String format) {
		this.label = label;
		if(!Utils.isNull(format))
			this.dateFormat = format;
		sdf = new SimpleDateFormat(dateFormat);
		bindToTime();
	}
	private void bindToTime() {
		timeline = new Timeline(
				new KeyFrame(Duration.seconds(0),
						new EventHandler<ActionEvent>() {
					@Override public void handle(ActionEvent actionEvent) {
						setTime();
						/*Calendar time = Calendar.getInstance();
						String day = pad(2, '0', time.get(Calendar.DAY_OF_MONTH)+"");
						String month = pad(2, '0', (time.get(Calendar.MONTH)+1) + "");
						String year = pad(2, '0', time.get(Calendar.YEAR) + "");
						String hourString = pad(2, '0', time.get(Calendar.HOUR_OF_DAY)+"");
						String minuteString = pad(2, '0', time.get(Calendar.MINUTE) + "");
						String secondString = pad(2, '0', time.get(Calendar.SECOND) + "");
						String ampmString = time.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
						label.setText(day+"/"+month+"/"+year+" "+hourString + ":" + minuteString + ":" + secondString );*/
						
					}
				}
						),
				new KeyFrame(Duration.seconds(1))
				);
		timeline.setCycleCount(Animation.INDEFINITE);
	}
	public void play(){
		timeline.play();
	}
	public void stop(){
		timeline.stop();
		label.setText("");;
	}
	private void setTime(){
		Calendar time = Calendar.getInstance();
		label.setText(sdf.format(time.getTime()));
	}
	private String pad(int fieldWidth, char padChar, String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = s.length(); i < fieldWidth; i++) {
			sb.append(padChar);
		}
		sb.append(s);
		return sb.toString();
	}
}

