package org.isip.states.speech;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Logger;

import org.isip.states.title.TitleState;
import org.isip.utils.GameFlow;
import org.newdawn.slick.SlickException;
import java.io.File;

public class SpeechScoring {

	private final static Logger LOGGER = Logger.getLogger(SpeechGameState.class
			.getName());
	private static String err;
	private static String comment;
	private static String testWav;
	private static String refWords;
	private static String refPhones;
	private static String refPhones2;
	private static String[] answers;
	private static String filename;
	private static Boolean debugmode = false;

	public SpeechScoring(String wav, String refw, String refp, String refp2,
			String[] ans, String fn) {
		err = "";
		comment = "";
		testWav = wav;
		refWords = refw;
		refPhones = refp;
		refPhones2 = refp2;
		answers = ans;
		filename = fn;

	}

	public double computescore() throws SlickException, IOException,
			InterruptedException {
		double Score = 0.0;

		int underscore = testWav.indexOf('_');
		String lesson = testWav.substring(0, underscore);

		/* Debug mode -- Delete after */
		if (debugmode) {
			String testWavLoc = "assets/unitassessment/speech/lesson_"
					+ GameFlow.getCurrLessonSeqNo() + "/unit_"
					+ GameFlow.getCurrUnit().getUnitId() + "/" + testWav
					+ ".ogg";
			if (testWavLoc.endsWith(".ogg")) {
				Runtime r = Runtime.getRuntime();
				Process p = r.exec("oggdec -o" + "temp.wav" + " " + testWavLoc);
				p.waitFor();
				// Thread.sleep(10);
				// System.out.println(p.exitValue());
				filename = "temp.wav";
			}
		}
		// location of the sample wav files used in unitassessment (exercises)

		// double[] pitch = SpeechPitch.speechPitch(filename);
		// /Runtime r = Runtime.getRuntime();
		String test = "";
		int i;
		boolean success = true;
		boolean phoneme = true;
		String[] ASR = new String[1];
		/* Phone/Word Recognition */
		if (refWords.indexOf(' ') != -1) {
			phoneme = false;
			i = (refWords.length() - refWords.replaceAll(" ", "").length() + 1) * 2 + 10;
			try {
				ASR = WordASR(i, refWords, filename, lesson);
				refWords = refWords.toUpperCase();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("\nFound \"" + refWords
					+ "\". Decoding Phonemes...");
			i = (refPhones.length() - refPhones.replaceAll(" ", "").length() + 1) * 2 + 10;
			try {
				ASR = PhonemeASR(i, refWords.toLowerCase(), refPhones,
						filename, lesson);
			} catch (Exception e) {
				e.printStackTrace();
			}
			refWords = refPhones;
		}

		/* Post-processing */
		if (ASR[0] == null) {
			ASR[0] = "";
			success = false;
		}
		LOGGER.info("JANUS: " + ASR[0]);
		String[] decoded = new String[3];
		if (ASR[0].indexOf(":") > -1)
			decoded = ASR[0].split(":");
		int alignmentScore = 0;
		if (decoded.length < 3) {
			decoded[2] = "";
		}
		if (decoded[0] != null && decoded[1] != null) {
			test = ((decoded[2] == null) ? "" : decoded[2]);
			Double scoreDiff = Double.valueOf(decoded[1])
					- Double.valueOf(decoded[0]);
			// System.out.println("FA Score = " + decoded[0] + " Dec Score = "
			// + decoded[1] + " Diff: " + scoreDiff);
			if (scoreDiff >= 50) { // Decoding Score - Forced Alignment Score
				alignmentScore = 100;
				System.out.println(filename
						+ " aligned with the text accordingly");
			} else if (scoreDiff >= -50) {
				alignmentScore = scoreDiff.intValue() + 50;
				System.out.println(filename
						+ " aligned with the text accordingly");
			} else { // if (scoreDiff <= -50) {
				alignmentScore = 0;
				System.out.println(filename + " did not align very well");
			}
		} else
			test = "+ERROR+";

		/* LDistance computation */
		LDistance ld = new LDistance();
		ld.processLD(refWords, test);
		LOGGER.info("Reference: " + refWords + "; Decoded: " + test);
		System.out.println("Reference: " + refWords);
		System.out.println("Decoded: " + test);
		System.out.println("EDIT SENT: "
				+ ld.getLDString(ld.getLDMatrix(), ld.getSArr(), ld.getTArr()));
		Vector<Integer> wer_err = ld.getLDStringStats(ld.getLDString(
				ld.getLDMatrix(), ld.getSArr(), ld.getTArr()));
		Score = 100.0 - ((float) wer_err.get(0) / (float) wer_err.get(1) * 100.0);
		LOGGER.info("Recog Rate: " + Score);
		System.out.println("\nRecog Rate: " + Score);

		/* Score Comment */
		if (Score >= 90)
			comment = "Excellent!";
		else if (Score >= 75)
			comment = "Very Good!";
		else if (Score >= 50)
			comment = "Good!";
		else if (Score >= 30)
			comment = "Nice try!";
		else
			comment = "Practice makes perfect :)";

		wer_err.remove(0);
		wer_err.remove(0);

		/* Error Feedback & Scoring */
		System.out.println("Original ERR: " + wer_err);

		/*
		 * err_length is the number of errors to be shown show 3/4 of the errors
		 * if number of error is > 3
		 */
		int err_length = wer_err.size();
		if (err_length > 3)
			err_length = (int) (0.75 * err_length);

		if (success) {
			if (phoneme) {
				err = "";
				LOGGER.info("Raw Score = (" + Score + " + " + alignmentScore
						+ ")/2 = " + (Score + alignmentScore) / 2);
				Score = (Score + alignmentScore) / 2.0;
				if (Score >= 50) { // 75 is the perfect score; passing is 50/75
									// = 1 pt.
					Score = 1;
				} else {
					String[] ans = answers[0].split(" ");
					i = 0;
					while (!wer_err.isEmpty() && i < err_length) {
						if (err != "")
							err = err + ",";
						int tmp = wer_err.get(0);
						wer_err.remove(0);
						err = err + ans[tmp];
						i++;
					}
					Score = 0;
				}
			} else { // word
				/* Process the key words - find the index of the keywords */
				Score = (Score + alignmentScore) / 2; // highest score = 100
				// String[] ans = answers.split(" ");
				err = wer_err.toString();
				err = err.substring(1, err.length() - 1).replace(" ", "");
				// boolean keyWordErr = false;
				// check if user get the key words wrong
				for (i = 0; i < answers.length; i++) {
					if (err.indexOf(answers[i]) > -1) {
						// keyWordErr = true;
						// err.replaceAll(regex, replacement)
						err = err.replace(answers[i], "");
						err = answers[i] + "," + err;
						err = err.replace(",,", ",");
						Score = Score
								- (1.0 / (4.0 * (double) answers.length) - 1.0 / (double) refWords
										.length()); // the score is adjusted so
													// that
													// a total of 25% penalty is
													// deducted from the perfect
													// score
													// if all key words are
													// wrong
					}
				}
				/*
				 * else if (err.length() > 7) err = err.substring(0,
				 * err.indexOf(",", 6)); else if (err.length() > 5) err =
				 * err.substring(0, err.indexOf(",", 4));
				 */
				/*
				 * Code is removed because errors should be shown regardless the
				 * score if (Score < 50 || keyWordErr) { if (err.length() > 5)
				 * err = err.substring(0, err.indexOf(",", 4)); } else err = "";
				 */
				Score = (Score + 25) / 12.5; // additional pts
				if (Score < 0)
					Score = 0.0;
				else if (Score > 10)
					Score = 10.0;
			}
		} else { // no need to compute score
			Score = 0;
			err = wer_err.toString();
			err = err.replace("[", "").replace("]", "");
		}

		if (!phoneme) {
			err_length = (int) ((double) ((10 - Score) / 10) * (refWords
					.length() - refWords.replace(" ", "").length() + 1));
			if (err.length() > 2 * err_length) // x2 including ",")
				err = err.substring(0, err.indexOf(",", 2 * err_length - 1));
		}
		System.out.println("ERR: " + err);
		System.out.println("Score: " + Score);
		LOGGER.info("Final Score: " + Score);

		return Score;
	}

	public static String[] WordASR(int i, String refWords, String filename,
			String lesson) {
		String[] line = new String[i];
		int timeout = 0;
		int trial = 0;

		try {
			do {
				if (trial > 0)
					System.out.print("\nDecoding speech again: " + trial);
				Runtime r = Runtime.getRuntime();
				Process p = r.exec("janus -f janus/words/words.tcl " + filename
						+ " \"" + refWords.toUpperCase() + "\" " + lesson);
				Thread.sleep(timeout);
				BufferedReader b = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				i = 0;
				while ((line[i] = b.readLine()) != null) {
					if (line[i].indexOf('*') == 0) {
						p.destroy();
					}
					i++;
				}
				timeout = timeout + 10;
				trial++;
			} while (line[0] == null && trial < 3);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Debug mode */
		if (line[0] == null)
			System.out.print("\nCannot recognize sample wav file");
		else {
			for (i = 0; i < line.length; i++) {
				if (line[i] != null)
					System.out.print("\n" + line[i]);
			}
		}
		/* Debug mode end */

		return line;
	}

	public static String[] PhonemeASR(int i, String refWords, String refPhones,
			String filename, String lesson) {
		String[] line = new String[i];
		int timeout = 0;
		int trial = 0;
		String ref = refWords.toLowerCase();
		String jcommand = "janus -f janus/phonemes/phonemes.tcl " + filename
				+ " " + ref + " \"" + refPhones + "\" " + lesson;
		try {
			do {
				if (trial > 0)
					System.out.print("\nDecoding speech again: " + trial);
				Runtime r = Runtime.getRuntime();
				Process p = r.exec(jcommand);

				BufferedReader b = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				i = 0;
				while ((line[i] = b.readLine()) != null) {
					if (line[i].equals("*")) {
						p.destroy();
					}
					i++;
				}
				timeout = timeout + 10;
				trial++;
			} while (line[0] == null && trial < 3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return line;
	}

	public static String getErr() {
		return err;
	}

	public String getComment() {
		return comment;
	}

}