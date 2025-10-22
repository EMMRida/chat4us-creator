/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.util;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * The AudioClip class is used to play audio files.
 *
 * @author El Mhadder Mohamed Rida
 */
public class AudioClip {

	private Clip clip;

	/**
	 * Init the audio clip
	 * @param file The audio file
	 */
	public AudioClip(String file) {
		try {
			File audioFile = new File(file);
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			clip = AudioSystem.getClip();
			clip.open(audioStream);
		} catch (Exception ex) {
			clip = null;
			Helper.logError(ex, Messages.getString("AudioClip.AUDIO_LOAD_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Play the audio
	 */
	public void play() {
		if(clip != null) {
			new Thread(()-> {
				clip.setFramePosition(0);
				clip.start();
				try {Thread.sleep(clip.getMicrosecondLength()/1000); } catch(InterruptedException ignored) { }
			}).start();
		}
	}

	/**
	 * Close the audio
	 */
	public void close() {
		if(clip != null)
			clip.close();
	}
}
