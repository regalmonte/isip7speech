package org.isip.states.speech.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.newdawn.slick.util.Log;



// To play sound using Clip, the process need to be alive.
public class Playback implements Runnable {

	   private Clip clip;
	   private InputStream  in;
	   private AudioInputStream ais;
	   private String filename;
	   private boolean playing;
	   
	   // Constructor
	   public Playback(String filename) {
		   this.filename = filename;
	   }
	   
	   public void run(){
		   playing = true;
		   try {
  				Log.debug("filename: "+filename);
  				Log.debug("file exists:" +new File(filename).exists());
  				
   				in = new FileInputStream(filename);
				clip = AudioSystem.getClip();
				ais = AudioSystem.getAudioInputStream(loadStream(in));
				clip.open(ais);
				clip.loop(0);
		        in.close();	
		        
		   } catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		   } catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		   } catch (UnsupportedAudioFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		   } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		   }
	   }
	   
	   public boolean isPlaying() {
		   if (clip != null) {
				  return clip.isRunning();
		   }
		   return true;
	   }
	   

       public static InputStream loadStream(InputStream in) throws IOException {
       	  ByteArrayOutputStream bos = new ByteArrayOutputStream();
       	  byte[] buf = new byte[1024];
       	  int c;
       	  while ((c = in.read(buf)) != -1) {
       	    bos.write(buf, 0, c);
       	  }
       	  return new ByteArrayInputStream(bos.toByteArray());
       }
	   
	   public void stop() {
		   clip.stop();
	   }
	   /* // Listens for when the clip has stopped playing and closes it. (Part of the LineListener)
	   public void update(LineEvent event) {
	        if (event.getType().equals(LineEvent.Type.STOP)) {
	        	hasStopped = true;
	        	hasStopped();
	        	clip.close();
	        }
	   }*/

}