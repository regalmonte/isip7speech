package org.isip.states.speech;

import it.randomtower.engine.World;
import it.randomtower.engine.entity.Entity;

import java.io.IOException;

import org.isip.Game;
import org.isip.entities.ImageButton;
import org.isip.entities.UnitInstructionsPage;
import org.isip.states.speech.factories.models.Speech;
import org.isip.utils.Globals;
import org.isip.utils.ResourceManager;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class SpeechGameState extends World {

	private SpeechController controller;
	private Speech speech;
	private Speech oldSpeech;
	
	private ImageButton readyBtn;

	private int questionNo = 0;
	private int score;
	private int gameTimeTotal = 0;
	private int totalCount = 0;

	// private String type;

	public SpeechGameState(int id, SpeechFactory factory) {
		super(id);
		this.speech = factory.getSpeech();

	}

	/**
	 * Does this when transitioned into this state
	 */
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.enter(container, game);

		if (!Globals.speechmoduleInitialized) {
			super.init(container, game);
			Globals.speechmoduleInitialized = true;
		}
		try {
			ResourceManager.loadResources("assets/resources-gamestates.xml");
			ResourceManager.loadResources("assets/resources-speech.xml");
			if (speech.getResources() != null) {
				ResourceManager.loadResources(speech.getResources());
			}
			oldSpeech = speech;
		} catch (IOException e) {
			e.printStackTrace();
		}

		add(Globals.leftmenu);
		Globals.leftmenu.addEntitiesToWorld();

		add(Globals.confirmationPopup);
		Globals.confirmationPopup.addEntitiesToWorld();

		
		Game.bg.changeImage("bg_speech");
		add(Game.bg);
		displayInstructions();
	}

	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		super.update(container, game, delta);

		if (!Globals.paused) {
			if (readyBtn.isMouseReleased()) {
				initController();
				hideInstructionsPage();
				resetGame();
			}
		} else if (Globals.instructionsPage.visible) {
			if (Globals.confirmationPopup.visible){
				if(Globals.confirmationPopup.isConfirmed()){
					hideInstructionsPage();
					if(Globals.confirmationPopup.getStatus().equals("logout")){
						Globals.logoutClicked = true;
					}else if (Globals.confirmationPopup.getStatus().equals("exit")){
						Globals.exitClicked = true;
					}else if (Globals.confirmationPopup.getStatus().equals("lessons")){
						Globals.lessonsMenuClicked = true;
					}
					Globals.confirmationPopup.resetEntities();

				}else if(Globals.confirmationPopup.isCancelled()){
					Globals.confirmationPopup.resetEntities();
				}
			} else if (Globals.leftmenu.isLessonsMenuClicked()) {
				Globals.confirmationPopup.resetInitialize("Are you sure you want to go to lessons menu?", "lessons");
			} else if (Globals.leftmenu.isLogoutClicked()) {
				Globals.confirmationPopup.resetInitialize("Are you sure you want to logout?", "logout");
			} else if (Globals.leftmenu.isExitClicked()) {
				Globals.confirmationPopup.resetInitialize("Are you sure to quit?", "exit");
			}
//			if (Globals.leftmenu.isLessonsMenuClicked()) {
//				hideInstructionsPage();
//				Globals.lessonsMenuClicked = true;
//			} else if (Globals.leftmenu.isLogoutClicked()) {
//				hideInstructionsPage();
//				Globals.logoutClicked = true;
//			} else if (Globals.leftmenu.isExitClicked()) {
//				hideInstructionsPage();
//				Globals.exitClicked = true;
//			}
		}
	}

	private void displayInstructions() throws SlickException {
		if (Globals.instructionsPage == null) {
			Globals.instructionsPage = new UnitInstructionsPage();
		}
		readyBtn = new ImageButton(700, 590,
				ResourceManager.getSpriteSheet("btn_im_done"));

		Globals.instructionsPage.resetText(speech.getTitle(), speech.getTopic(),
				speech.getInstructions(), speech.getItems().size(),
				speech.getGameType());

		add(readyBtn);
		add(Globals.instructionsPage);
		Globals.instructionsPage.addEntitiesToWorld();

		readyBtn.visible = true;
		Globals.instructionsPage.setVisible(true);
	}

	private void hideInstructionsPage() {
		Globals.instructionsPage.setVisible(false);
		this.readyBtn.visible = false;
	}

	private void initController() throws SlickException {
		if (controller == null) {
			controller = new SpeechController(container, this.speech.getItems()
					.get(questionNo), this.speech.getGameType());
		}
		add(controller);
		controller.addEntitiesToWorld();
	}

	public void nextQuestion() throws SlickException {

		if (hasNextQuestion()) {
			questionNo++;
			setQuestionNumbering();
			controller.resetEntities(speech.getItems().get(questionNo));
		} else {
			controller.setVisible(false);
			controller.displaySummary();
			controller.resetTimer();
		}
	}

	public boolean hasNextQuestion() {
		int temp = questionNo + 1;
		return temp < speech.getItems().size();
	}

	public void resetGame() throws SlickException {
		controller.setScore(this.score);
		controller.setUnitTitle(speech.getTopic(), speech.getTitle());
		controller.setGameTimeTotal(gameTimeTotal);
		controller.setTotalCount(this.totalCount);
		
		controller.setType(speech.getGameType());
		
		controller.setVisible(true);
		questionNo--;
		nextQuestion();
	}

	public void resetFactory(SpeechFactory factory, int questionNo, int score,
			int totalCount, int time) {
		this.speech = factory.getSpeech();
		this.questionNo = questionNo;
		this.score = score;
		this.gameTimeTotal = time;
		this.totalCount = totalCount;
	}

	public void setQuestionNumbering() {
		controller.setQuestionNumbering(questionNo + 1, speech.getItems()
				.size());
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		for (Entity e : this.getEntities()) {
			this.remove(e);
		}
		this.clear();
		ResourceManager.unloadResources("assets/resources-gamestates.xml");
		ResourceManager.unloadResources("assets/resources-speech.xml");
		ResourceManager.unloadResources("assets/resources-popup.xml");
		if (oldSpeech.getResources() != null) {
			ResourceManager.unloadResources(oldSpeech.getResources());
		}
	}

}
