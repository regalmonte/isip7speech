package org.isip.states.speech;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.isip.Game;
import org.isip.GameControllerEntity;
import org.isip.entities.TextEntity;
import org.isip.models.Question;
import org.isip.models.UnitScore;
import org.isip.models.User;
import org.isip.models.UserAnswer;
import org.isip.states.practicedrills.entities.SpeechDrillButton;
import org.isip.states.speech.entities.Playback;
import org.isip.states.speech.entities.Recorder;
import org.isip.states.speech.entities.SpeechTextEntity;
import org.isip.states.speech.factories.models.SpeechItem;
import org.isip.utils.Constants;
import org.isip.utils.GameFlow;
import org.isip.utils.Globals;
import org.isip.utils.ResourceManager;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.geom.Vector2f;

public class SpeechController extends GameControllerEntity {

	private SpeechDrillButton playBtn;
	private SpeechDrillButton recordBtn;
	private SpeechDrillButton stopBtn;
	private SpeechDrillButton replayBtn;

	private Recorder recorder;
	private Sound voice;
	private Playback playback;
	private boolean isBusy = false;

	private UnicodeFont font;
	private SpeechTextEntity qtext;
	private SpeechTextEntity text;

	private TextEntity status;
	private TextEntity scoreText;
	private TextEntity phoneScoreText;
	private UnicodeFont sfont;

	private SpeechItem item;

	private String filename;
	private Thread playerThread;

	private final int PLAYING_SAMPLE = 1;
	private final int PLAYING_RECORDED = 2;
	private final int RECORDING = 3;
	private final int NO_ACTION = 3;

	private int playerStatus = 0;

	private String type = "";

	private int time = 0;

	public SpeechController(GameContainer container, SpeechItem item,
			String type) throws SlickException {
		super(container, 120000);
		this.container = container;
		this.item = item;
		this.type = type;

		init();
	}

	@SuppressWarnings("unchecked")
	public void init() throws SlickException {
		font = new UnicodeFont(Constants.FNT_MYRIAD_REG, 22, false, false);
		font.getEffects().add(new ColorEffect(Color.GRAY));

		sfont = new UnicodeFont(Constants.FNT_MYRIAD_LIGHTIT, 14, false, false);
		sfont.getEffects().add(new ColorEffect(Color.DARK_GRAY));

		status = new TextEntity(410, 600, "Turn on your microphone.",
				Constants.FNT_MYRIAD_REG, 18, Color.black, 800);

		scoreText = new TextEntity(420, 500, "", Constants.FNT_MYRIAD_BLACK,
				30, Color.BLUE, 500);
		phoneScoreText = new TextEntity(500, 350, "", Constants.FNT_MYRIAD_BLACK,
				50, Color.GREEN, 500);
		
	}

	@Override
	public void addEntitiesToWorld() throws SlickException {
		super.addEntitiesToWorld();
		initOtherEntities();

		submitBtn.setPosition(new Vector2f(718, 567));
		nextBtn.setPosition(new Vector2f(718, 567));

		this.world.add(stopBtn);
		this.world.add(playBtn);
		this.world.add(recordBtn);
		this.world.add(replayBtn);
		this.world.add(status);
		this.world.add(scoreText);
		this.world.add(phoneScoreText);

		status.depth = 12;
		scoreText.depth = 12;
		phoneScoreText.depth = 12;
		stopBtn.depth = 12;
		playBtn.depth = 12;
		recordBtn.depth = 12;
		replayBtn.depth = 12;

		recordBtn.visible = true;
		stopBtn.visible = false;
		replayBtn.visible = true;

		userSubmitted = false;
		// displayText();
	}

	private void initOtherEntities() throws SlickException {
		playBtn = new SpeechDrillButton(120, 530,
				ResourceManager.getSpriteSheet("sp_btn_play"));
		recordBtn = new SpeechDrillButton(285, 580,
				ResourceManager.getSpriteSheet("sp_btn_record"));
		stopBtn = new SpeechDrillButton(285, 580,
				ResourceManager.getSpriteSheet("sp_btn_stop"));
		replayBtn = new SpeechDrillButton(345, 580,
				ResourceManager.getSpriteSheet("sp_btn_replay"));
	}

	@Override
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		if (visible) {

			g.setColor(new org.newdawn.slick.Color(160, 159, 157));
			g.fillRoundRect(270, 570, 435, 70, 20);
			g.setColor(new org.newdawn.slick.Color(255, 204, 0));
			g.fillRoundRect(405, 580, 286, 50, 10);

			if (unitTitle.visible) {
				icon.draw(40, 10);
			}
			updateProgressBar(g);
		}
	}

	private void initRecorder() {

		filename = "assets/speech/recorded/" + Globals.user.getUserId() + "_"
				+ GameFlow.getCurrUnit().getUnitId() + "_" + item.getFilename()
				+ ".wav";

		/*
		 * filename = System.getenv("APPDATA")+"/Global English for Pinoys/" +
		 * Globals.user.getUserId() + "_" + GameFlow.getCurrUnit().getUnitId() +
		 * "_" + this.questionNoText.getText() + ".wav";
		 */

		File outputFile = new File(filename);

		// Using PCM 44.1 kHz, 16 bit signed,stereo.
		AudioFormat audioFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, 16000.0F, 16, 1, 2, 16000.0F,
				false);

		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
				audioFormat);
		TargetDataLine targetDataLine = null;

		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			targetDataLine.open(audioFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			System.exit(1);
		}

		AudioFileFormat.Type targetType = AudioFileFormat.Type.WAVE;
		recorder = new Recorder(targetDataLine, targetType, outputFile);
	}

	public void update(GameContainer container, int delta)
			throws SlickException {
		// super.update(container, delta);

		if (!Globals.paused) {
			if (!isBusy) {
				if (!userSubmitted) {
					time += delta;
				}
				replayBtn.setDisabled(filename == null);

				if (playBtn.isMouseReleased()) {
					playSampleAudio();
					disableButtons();

				} else if (voice.playing()) {
					status.setText("Playing sample...");
					disableButtons();

				} else if (recordBtn.isMouseReleased()) {
					startRecorder();
					disableButtons();
					stopBtn.visible = true;
					recordBtn.visible = false;
				} else if (replayBtn.isMouseReleased()) {
					playRecordedVoice();
					disableButtons();

				} else if (submitBtn.isMouseReleased()) {
					if (filename != null) {
						try {
							checkAnswer();
							recordBtn.setDisabled(true);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else {
						status.setText("No recorded voice.");

						// *******************TEMP************************
						userSubmitted = true;
						submitBtn.visible = false;
						nextBtn.visible = true;
						submitBtnClicked = false;
						// *******************TEMP************************
					}
				} else if (nextBtn.isMouseReleased()) {
					text.clearText(true);
					clearAll();
					recordBtn.setDisabled(false);
					status.setText("Turn on your microphone.");
					filename = null;

					// Globals.score += scoreCount;
					Globals.scoreBoard.setScore(scoreCount);
					nextBtnClicked = false;

					((SpeechGameState) this.world).nextQuestion();

				}
			} else {
				if (playerStatus == PLAYING_RECORDED) {
					if (!playback.isPlaying()) {

						playerStatus = NO_ACTION;
						status.setText("Finished playing your recorded voice.");
						enableButtons();
					}
				}

				else if (playerStatus == PLAYING_SAMPLE) {
					if (!voice.playing()) {

						playerStatus = NO_ACTION;
						status.setText("Finished playing sample.");
						enableButtons();
					}
				}
				/* Check the recording! */
				else if (playerStatus == RECORDING) {

				}

			}

			if (stopBtn.isMouseReleased()) {

				if (playerStatus == RECORDING) {
					stopRecorder();
					stopBtn.visible = false;
					recordBtn.visible = true;
					replayBtn.visible = true;
					status.setText("Recording has stopped...");

				} else if (playerStatus == PLAYING_SAMPLE) {
					voice.stop();
					status.setText("Playing sample has stopped...");

				} else if (playerStatus == PLAYING_RECORDED) {
					playback.stop();
					status.setText("Playing recorded has stopped...");

				}

				playerStatus = NO_ACTION;
				enableButtons();
			}
		} else if (Globals.confirmationPopup.visible) {
			if (Globals.confirmationPopup.isConfirmed()) {
				saveProgressAndExit();
				if (Globals.confirmationPopup.getStatus().equals("logout")) {
					Globals.logoutClicked = true;
				} else if (Globals.confirmationPopup.getStatus().equals("exit")) {
					Globals.exitClicked = true;
				} else if (Globals.confirmationPopup.getStatus().equals(
						"lessons")) {
					Globals.lessonsMenuClicked = true;
				}
				Globals.confirmationPopup.resetEntities();

			} else if (Globals.confirmationPopup.isCancelled()) {
				Globals.confirmationPopup.resetEntities();
			}
		} else if (Globals.leftmenu.isLessonsMenuClicked()) {
			Globals.confirmationPopup.resetInitialize(
					"Are you sure you want to go to lessons menu?", "lessons");
		} else if (Globals.leftmenu.isLogoutClicked()) {
			Globals.confirmationPopup.resetInitialize(
					"Are you sure you want to logout?", "logout");
		} else if (Globals.leftmenu.isExitClicked()) {
			Globals.confirmationPopup.resetInitialize("Are you sure to quit?",
					"exit");
		}
		// else if (Globals.leftmenu.isLessonsMenuClicked()) {
		// saveProgressAndExit();
		// Globals.lessonsMenuClicked = true;
		// } else if (Globals.leftmenu.isLogoutClicked()) {
		// saveProgressAndExit();
		// Globals.logoutClicked = true;
		// } else if (Globals.leftmenu.isExitClicked()) {
		// saveProgressAndExit();
		// Globals.exitClicked = true;
		// }

	}

	private void saveProgressAndExit() throws SlickException {
		clearAll();
		super.setVisible(false);
		if (this.userSubmitted) {
			if (((SpeechGameState) this.world).hasNextQuestion()) {
				GameFlow.saveProgress(null, null, currentQuestion + 1);
			} else {
				GameFlow.saveProgress(this.scoreCount, null, null);
			}
		} else {
			GameFlow.saveProgress(null, null, currentQuestion);
		}
	}

	private void playSampleAudio() {
		playerStatus = PLAYING_SAMPLE;
		status.setText("Playing sample...");
		voice = ResourceManager.getSound(item.getFilename());
		voice.play();
	}

	private void disableButtons() {
		isBusy = true;
		playBtn.setDisabled(true);
		recordBtn.setDisabled(true);
		replayBtn.setDisabled(true);
		stopBtn.setDisabled(false);
	}

	private void enableButtons() {
		isBusy = false;
		playBtn.setDisabled(false);
		recordBtn.setDisabled(false);
		replayBtn.setDisabled(false);
		stopBtn.setDisabled(true);
	}

	private void playRecordedVoice() {
		playerStatus = PLAYING_RECORDED;
		status.setText("Playing your recorded voice...");
		playback = new Playback(filename);
		this.playerThread = new Thread(playback);
		this.playerThread.start();
	}

	private void stopRecorder() {
		recorder.stopRecording();
	}

	private void startRecorder() {
		playerStatus = RECORDING;
		status.setText("Recording...");
		initRecorder();
		recorder.start();
	}

	public void resetEntities(SpeechItem item) throws SlickException {
		super.resetEntities(0);

		this.item = item;
		stopBtn.visible = false;
		playBtn.visible = true;
		recordBtn.visible = true;
		replayBtn.visible = true;
		status.visible = true;
		scoreText.visible = false;
		phoneScoreText.visible = false;

		displayText();
	}

	private void displayText() throws SlickException {
		playSampleAudio();

		// ****************** TEMP ******************** //
		System.out.println("\n QUESTION ID: " + item.getQuestionId()
				+ ", FILE LOCATION: assets/unitassessment/speech/lesson_"
				+ GameFlow.getCurrLessonSeqNo() + "/unit_"
				+ GameFlow.getCurrUnit().getUnitId() + "/" + item.getFilename()
				+ ".ogg");
		// ****************** TEMP ******************** //

		if (this.type.equals("speech1")) {
			initFonts(Constants.SPEECH1);
			String s = "<b>" + item.getText() + "</b>";
			text = new SpeechTextEntity(0, 220, s, Constants.SPEECH1, 75,
					Color.black, 800);
			text.x = text.centerText(item.getText());
		} else if (this.type.equals("speech2")) {
			initFonts(Constants.SPEECH2);
			text = new SpeechTextEntity(0, 190, item.getText(),
					Constants.SPEECH2, 55, Color.black, 800);
		} else if (this.type.equals("speech5")) {
			initFonts(Constants.SPEECH35);
			text = new SpeechTextEntity(0, 170, item.getText(),
					Constants.SPEECH35, 40, Color.black, 800);
		} else if (this.type.equals("speech3")) {
			initFonts(Constants.SPEECH35);
			text = new SpeechTextEntity(0, 200, item.getText(),
					Constants.SPEECH35, 40, Color.black, 800);
		} else if (this.type.equals("speech4")) {
			initFonts(Constants.SPEECH4);
			initFonts(Constants.SPEECH4Q);
			qtext = new SpeechTextEntity(0, 110, item.getQuestion(),
					Constants.SPEECH4Q, 40, Color.black, 800);
			text = new SpeechTextEntity(0, 210, item.getText(),
					Constants.SPEECH4, 35, Color.black, 800);
			qtext.x = 130;
			world.add(qtext);
		}
		world.add(text);

		if (text.getWidth(item.getText()) <= 800) {
			text.x = text.centerText(item.getText());
		} else {
			text.x = 130;
		}

	}

	@SuppressWarnings("unchecked")
	private void initFonts(int type) throws SlickException {
		switch (type) {
		case Constants.SPEECH1:
			if (Constants.speechFont1 == null) {
				Constants.speechFont1 = new UnicodeFont(
						Constants.FNT_MYRIAD_LIGHT, 75, false, false);
				Constants.speechFont1.getEffects().add(
						new ColorEffect(Color.white));

				Constants.speechFont1B = new UnicodeFont(
						Constants.FNT_MYRIAD_SBOLD, 75, false, false);
				Constants.speechFont1B.getEffects().add(
						new ColorEffect(Color.white));
			}
			break;
		case Constants.SPEECH2:
			if (Constants.speechFont2 == null) {
				Constants.speechFont2 = new UnicodeFont(
						Constants.FNT_MYRIAD_LIGHT, 55, false, false);
				Constants.speechFont2.getEffects().add(
						new ColorEffect(Color.white));

				Constants.speechFont2B = new UnicodeFont(
						Constants.FNT_MYRIAD_SBOLD, 55, false, false);
				Constants.speechFont2B.getEffects().add(
						new ColorEffect(Color.white));
			}
			break;
		case Constants.SPEECH35:
			if (Constants.speechFont35 == null) {
				Constants.speechFont35 = new UnicodeFont(
						Constants.FNT_MYRIAD_LIGHT, 40, false, false);
				Constants.speechFont35.getEffects().add(
						new ColorEffect(Color.white));

				Constants.speechFont35B = new UnicodeFont(
						Constants.FNT_MYRIAD_SBOLD, 40, false, false);
				Constants.speechFont35B.getEffects().add(
						new ColorEffect(Color.white));
			}
			break;
		case Constants.SPEECH4:
			if (Constants.speechFont4 == null) {
				Constants.speechFont4 = new UnicodeFont(
						Constants.FNT_MYRIAD_LIGHT, 35, false, false);
				Constants.speechFont4.getEffects().add(
						new ColorEffect(Color.white));

				Constants.speechFont4B = new UnicodeFont(
						Constants.FNT_MYRIAD_SBOLD, 35, false, false);
				Constants.speechFont4B.getEffects().add(
						new ColorEffect(Color.white));
			}
			break;
		case Constants.SPEECH4Q:
			if (Constants.speechFont4Q == null) {
				Constants.speechFont4Q = new UnicodeFont(
						Constants.FNT_MYRIAD_LIGHT, 40, false, false);
				Constants.speechFont4Q.getEffects().add(
						new ColorEffect(Color.white));
			}
			break;
		}
	}

	private void checkAnswer() throws SlickException, IOException,
			InterruptedException {

		SpeechScoring ss = new SpeechScoring(item.getFilename(), item.getText()
				.replace("<b>", "").replace("</b>", "").replace(".", "")
				.replace(",", "").replace("!", "").replace("?", "")
				.replace(";", ""), item.getPhoneme(), item.getPhoneme2(),
				item.getKeywords(), filename);
		double itemscore = ss.computescore();
		DecimalFormat df = new DecimalFormat("###");
		
		// if (itemscore > 0) {
		saveUserAnswer(String.valueOf(itemscore));
		scoreCount += itemscore;
		// } else {
		// saveUserAnswer("");
		// }

		//String comment = ss.getComment();

		// for feedback and scoring
		if (this.type.equals("speech1")) {
			text.setText("<b>"
					+ SpeechFeedback.result(SpeechScoring.getErr(),
							item.getText()) + "</b>");
			if (itemscore == 1) {
				phoneScoreText.setText("+ 1");
				phoneScoreText.visible = true;
			}
		} else if (this.type.equals("speech2")) {
			text.setText(SpeechFeedback.result(SpeechScoring.getErr(),
					item.getText()));
			scoreText.setText("SCORE:  " + df.format(itemscore));
			scoreText.visible = true;
		} else {
			text.setText(SpeechFeedback.result(SpeechScoring.getErr(),
					item.getText()));
		}
		// FIXME:Get total count according to speech type
		totalCount++;

		this.submitBtn.visible = false;
		this.nextBtn.visible = true;

	}

	private void saveUserAnswer(String answer) {
		try {
			User user = Globals.user;
			Question question = Game.questionDao.queryForId(this.item
					.getQuestionId());
			Date now = new Date();
			UnitScore u = GameFlow.getLatestSavePt(GameFlow.getCurrUnit());
			UserAnswer useranswer = null;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(UserAnswer.USER_FIELD, user);
			map.put(UserAnswer.QUESTION_FIELD, question);
			map.put(UserAnswer.PART_NO_FIELD, 1);

			if (u == null) {
				map.put(UserAnswer.REPEAT_NO_FIELD, 1);
				List<UserAnswer> list = Game.userAnswerDao
						.queryForFieldValues(map);

				if (list.size() == 0) {
					useranswer = new UserAnswer();
					useranswer.setRepeatNo(1);
				} else {
					useranswer = list.get(0);
				}
			} else {
				if (u.getQuestionNo() == null) {
					map.put(UserAnswer.REPEAT_NO_FIELD, u.getRepeat_no() + 1);
					List<UserAnswer> list = Game.userAnswerDao
							.queryForFieldValues(map);

					if (list.size() == 0) {
						useranswer = new UserAnswer();
						useranswer.setRepeatNo(u.getRepeat_no() + 1);
					} else {
						useranswer = list.get(0);
					}

				} else {
					map.put(UserAnswer.REPEAT_NO_FIELD, u.getRepeat_no());
					List<UserAnswer> list = Game.userAnswerDao
							.queryForFieldValues(map);

					if (list.size() == 0) {
						useranswer = new UserAnswer();
						useranswer.setRepeatNo(u.getRepeat_no());
					} else {
						useranswer = list.get(0);
					}
				}
			}

			useranswer.setUser(user);
			useranswer.setQuestion(question);
			useranswer.setPartNo(1);
			useranswer.setAnswer(answer);

			SimpleDateFormat myFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			useranswer.setDate(myFormat.format(now));
			useranswer.setTime(time);
			Game.userAnswerDao.createOrUpdate(useranswer);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void clearAll() throws SlickException {
		// super.clearAll();

		text.destroy();
		world.remove(text);

		nextBtn.visible = false;
		submitBtn.visible = false;
		unitLesson.visible = false;
		unitTitle.visible = false;

		gameTimeTotal += time;

		stopBtn.visible = false;
		playBtn.visible = false;
		recordBtn.visible = false;
		replayBtn.visible = false;
		status.visible = false;
		scoreText.visible = false;
		phoneScoreText.visible = false;

		if (this.type.equals("speech4")) {
			world.remove(qtext);
		}

	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		this.visible = b;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void resetTimer() {
		time = 0;
	}

}