package com.ipssi.rfid.integration;

import com.ipssi.gen.utils.Misc;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.processor.Utils;

public class WaveFormPlayer { 
	
    private static final String fileAddr = "C:\\ipssi\\LocTracker\\waveform\\";
    private String filename;
 
    private Position curPosition;
    
    private static HashMap<Integer, String> waveMap = new HashMap<Integer, String>(); 
 
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb 
 
    enum Position { 
        LEFT, RIGHT, NORMAL
    };
    static{
    	waveMap.clear();

waveMap.put(Status.TPRQuestion.seatBeltWorm, "0101001");	
waveMap.put(Status.TPRQuestion.cleanFinger, "0100002");
waveMap.put(Status.TPRQuestion.thankYou , "0100003");
waveMap.put(Status.TPRQuestion.tryAgainFinger , "0100004");
waveMap.put(Status.TPRQuestion.tryOnceAgainFinger , "0100005");
waveMap.put(Status.TPRQuestion.fingerNotMatch , "0100006");
waveMap.put(Status.TPRQuestion.minesAndTransporterFromChallan, "0101007");
waveMap.put(Status.TPRQuestion.tarpaulinOk , "0101008");
waveMap.put(Status.TPRQuestion.sealOk , "0101009");
waveMap.put(Status.TPRQuestion.numberVisible , "0101010");
waveMap.put(Status.TPRQuestion.sideMirror , "0101011");
waveMap.put(Status.TPRQuestion.reverseHornOk , "0101021");
waveMap.put(Status.TPRQuestion.hornPlay, "0101022");
waveMap.put(Status.TPRQuestion.pushBrake, "0100023");
waveMap.put(Status.TPRQuestion.brakeLightOn, "0101024");
waveMap.put(Status.TPRQuestion.headLightOk, "0100025");
waveMap.put(Status.TPRQuestion.headLightOn, "0101026");
waveMap.put(Status.TPRQuestion.leftSideIndicator, "0100027");
waveMap.put(Status.TPRQuestion.leftSideIndicatorOn, "0101028");
waveMap.put(Status.TPRQuestion.rightSideIndicator, "0100029");
waveMap.put(Status.TPRQuestion.rightSideIndicatorOn, "0101030");
waveMap.put(Status.TPRQuestion.enterDriverIdAndDriverName, "0101041");
waveMap.put(Status.TPRQuestion.driverAppearsDrunk, "0101051");
waveMap.put(Status.TPRQuestion.goToWB, "0101052");
waveMap.put(Status.TPRQuestion.vehicleBlackListed, "0101053");
waveMap.put(Status.TPRQuestion.fixedLr, "0101054");
waveMap.put(Status.TPRQuestion.registrationNewVehicle, "0101055");
waveMap.put(Status.TPRQuestion.issueRfidTag, "0101056");
waveMap.put(Status.TPRQuestion.challanEntry, "0101057");
waveMap.put(Status.TPRQuestion.updateFingerPrint, "0101058");
waveMap.put(Status.TPRQuestion.getDriverRegistration, "0101059");
waveMap.put(Status.TPRQuestion.paperNotValid, "0101060");
waveMap.put(Status.TPRQuestion.goToRestrationCenter, "0101061");
waveMap.put(Status.TPRQuestion.InformControlRoom, "0101062");
waveMap.put(Status.TPRQuestion.dumpCoal, "0300001");
waveMap.put(Status.TPRQuestion.getQcStamp, "0300031");
waveMap.put(Status.TPRQuestion.getGpsRepairedForGateOut, "0300032");
waveMap.put(Status.TPRQuestion.getGpsRepaired, "0400001");
waveMap.put(Status.TPRQuestion.qcStampDonegoParking, "0400002");
waveMap.put(Status.TPRQuestion.saveDetail, "0400003");
waveMap.put(Status.TPRQuestion.startFingerCapturing, "0400004");
waveMap.put(Status.TPRQuestion.barrierGps, "BarrierGps");
waveMap.put(Status.TPRQuestion.barrierQc, "BarrierQc");
waveMap.put(Status.TPRQuestion.bedOne, "Bed1");
waveMap.put(Status.TPRQuestion.bedTwo, "Bed2");
waveMap.put(Status.TPRQuestion.bedThree, "Bed3");
waveMap.put(Status.TPRQuestion.bedFour, "Bed4");
waveMap.put(Status.TPRQuestion.bedFive, "Bed5");
waveMap.put(Status.TPRQuestion.bedSix, "Bed6");
waveMap.put(Status.TPRQuestion.hopperOne, "Hopper1");
waveMap.put(Status.TPRQuestion.hopperTwo, "Hopper2");
waveMap.put(Status.TPRQuestion.hopperThree, "Hopper3");
waveMap.put(Status.TPRQuestion.hopperFour, "Hopper4");
waveMap.put(Status.TPRQuestion.hopperFive, "Hopper5");
waveMap.put(Status.TPRQuestion.hopperSix, "Hopper6");
    }
    private WaveFormPlayer(String wavfile) { 
        filename = wavfile;
        curPosition = Position.NORMAL;
    } 
 
    private WaveFormPlayer(String wavfile, Position p) { 
        filename = wavfile;
        curPosition = p;
    } 
 
    private static Object lock = new Object();
    public static synchronized void playSound(final String url, final Position pos) {
    	  new Thread(new Runnable() {
    	  // The wrapper thread is unnecessary, unless it blocks on the
    	  // Clip finishing; see comments.
    	    public void run() {
    	      try {
    	    	  synchronized (lock) {
    	    	  File soundFile = new File(url);
    	          if (!soundFile.exists()) { 
    	              System.err.println("Wave file not found: " + url);
    	              return;
    	          } 
    	   
    	          AudioInputStream audioInputStream = null;
    	          try { 
    	              audioInputStream = AudioSystem.getAudioInputStream(soundFile);
    	          } catch (UnsupportedAudioFileException e1) { 
    	              e1.printStackTrace();
    	              return;
    	          } catch (IOException e1) { 
    	              e1.printStackTrace();
    	              return;
    	          } 
    	   
    	          AudioFormat format = audioInputStream.getFormat();
    	          SourceDataLine auline = null;
    	          DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
    	   
    	          try { 
    	              auline = (SourceDataLine) AudioSystem.getLine(info);
    	              auline.open(format);
    	          } catch (LineUnavailableException e) { 
    	              e.printStackTrace();
    	              return;
    	          } catch (Exception e) { 
    	              e.printStackTrace();
    	              return;
    	          } 
    	   
    	          if (auline.isControlSupported(FloatControl.Type.PAN)) { 
    	              FloatControl pan = (FloatControl) auline
    	                      .getControl(FloatControl.Type.PAN);
    	              if (pos == Position.RIGHT) 
    	                  pan.setValue(1.0f);
    	              else if (pos == Position.LEFT) 
    	                  pan.setValue(-1.0f);
    	          } 
    	   
    	          auline.start();
    	          int nBytesRead = 0;
    	          byte[] abData = new byte[524288];
    	   
    	          try { 
    	              while (nBytesRead != -1) { 
    	                  nBytesRead = audioInputStream.read(abData, 0, abData.length);
    	                  if (nBytesRead >= 0) 
    	                      auline.write(abData, 0, nBytesRead);
    	              } 
    	          } catch (IOException e) { 
    	              e.printStackTrace();
    	              return;
    	          } finally { 
    	              auline.drain();
    	              auline.close();
    	          }
    	    	  }
    	      } catch (Exception e) {
    	        System.err.println(e.getMessage());
    	      }
    	      
    	    }
    	  }).start();
    	}
    /*public static void playSoundIn(int instructionId){
    	playSound(fileAddr + waveMap.get(instructionId)+".wav",Position.LEFT);
    	//playSound(waveMap.get(instructionId), Position.LEFT);
    }*/
    public static void playSoundOut(int instructionId){
    	//playSound(fileAddr + waveMap.get(instructionId)+".wav",Position.RIGHT);
    	//playSoundExt(waveMap.get(instructionId));
    	//playSound(waveMap.get(instructionId), Position.RIGHT);
    }
    public static void main(String[] arg){
    	try{
    		
    		ArrayList<Integer> list = new ArrayList<Integer>();
    		list.add(Status.TPRQuestion.numberVisible);
    		list.add(Status.TPRQuestion.numberVisible);
    		list.add(Status.TPRQuestion.numberVisible);
    		playSoundSequence(list);
    		//playSoundIn(Status.TPRQuestion.numberVisible);
    	
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    private static AudioStream audios = null;
    synchronized public static void playSoundIn(int instructionId) {
        try {
        	if(audios != null)
        		stopPlayer(audios);
                if(!Misc.isUndef(instructionId) && !Utils.isNull(waveMap.get(instructionId))){
                    audios = new AudioStream(new FileInputStream(fileAddr + waveMap.get(instructionId)+".wav"));
                    AudioPlayer.player.start(audios);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void stopPlayer(AudioStream audios) {
        AudioPlayer.player.stop(audios);
    }
    
    public static void playSoundSequence(ArrayList<Integer> instructionList){
    	if(instructionList == null || instructionList.size() == 0)
    		return;
    	try{
    		ArrayList<String> files = new ArrayList<String>();
    		for(int i=0,is=instructionList == null ? 0 :instructionList.size();i<is;i++){
				files.add(fileAddr + waveMap.get(instructionList.get(i))+".wav");
			}
    		play(files);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    public static void playSoundSequenceStr(ArrayList<String> instructionList){
    	if(instructionList == null || instructionList.size() == 0)
    		return;
    	try{
    		ArrayList<String> files = new ArrayList<String>();
    		for(int i=0,is=instructionList == null ? 0 :instructionList.size();i<is;i++){
				files.add(fileAddr + instructionList.get(i)+".wav");
			}
    		play(files);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    public static void play(final ArrayList<String> files){
        try{
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[4096];
        for (String filePath : files) {
            File file = new File(filePath);
            try {
                AudioInputStream is = AudioSystem.getAudioInputStream(file);
                AudioFormat format = is.getFormat();
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                while (is.available() > 0) {
                    int len = is.read(buffer);
                    line.write(buffer, 0, len);
                }
                line.drain();
                line.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            }
                }
            });
            t.start();
        } catch(Exception ex){
            ex.printStackTrace();
        }         
        
    }
} 
 