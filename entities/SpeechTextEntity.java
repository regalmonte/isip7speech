package org.isip.states.speech.entities;

import it.randomtower.engine.entity.Entity;

import java.awt.Color;
import java.util.List;

import org.isip.states.speech.SpeechFeedback;
import org.isip.utils.Constants;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

public class SpeechTextEntity extends Entity {

	private UnicodeFont font;
	private UnicodeFont fontB;
	private Color color;
	private org.newdawn.slick.Color normalColor;
	private org.newdawn.slick.Color highlightColor;
	private String text;

	private int s2;
	private int width;
	private int fontSize;
	private int maxWidth = 0;
	private int lineWidth = 0;

	private boolean highlight = false;
	private boolean clear = false;
	float posX;

	public SpeechTextEntity(float x, float y, String text, int type,
			Integer fontSize, Color color, int width) throws SlickException {
		super(x, y);
		this.text = text;
		this.color = color;
		this.fontSize = fontSize;
		this.width = width;
		depth = 0;
		this.lineWidth = fontSize / 16;

		init(type);
	}

	public void init(int type) throws SlickException {

		normalColor = org.newdawn.slick.Color.black;
		highlightColor = org.newdawn.slick.Color.red;
		
		switch (type) {
		case Constants.SPEECH1:
			font = Constants.speechFont1;
			fontB = Constants.speechFont1B;
			break;
		case Constants.SPEECH2:
			font = Constants.speechFont2;
			fontB = Constants.speechFont2B;
			break;
		case Constants.SPEECH35:
			font = Constants.speechFont35;
			fontB = Constants.speechFont35B;
			break;
		case Constants.SPEECH4:
			font = Constants.speechFont4;
			fontB = Constants.speechFont4B;
			break;
		case Constants.SPEECH4Q:
			font = Constants.speechFont4Q;
			fontB = Constants.speechFont4B;
			break;
		case Constants.PDRILL:
			font = Constants.speechFont;
			fontB = Constants.speechFontB;
			normalColor = org.newdawn.slick.Color.white;
			highlightColor = org.newdawn.slick.Color.green;
			break;
		}

		define("RIGHT", Input.KEY_RIGHT);
	}

	@Override
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		if (visible) {
			super.render(container, g);

			int line = 0;
			posX = x;
			text = text.replace("\t", " ").replace("\n", "");

			int width = 0;
			g.setLineWidth(this.lineWidth);
			g.setColor(new org.newdawn.slick.Color(color.getRGB()));

			for (String s : text.split(" ")) {
				if (s.contains("<br>")) {
					width = 0;
					posX = x;
					line++;
				} else if (s.startsWith("<b>")) {

					s = s.replace("<b>", "").replace("</b>", "");

					if (s.contains("<c>")) {
						s = s.replace("<c>", "").replace("</c>", "");

						int temp2 = width + fontB.getWidth(s);
						if (temp2 > this.width) {
							if (maxWidth < this.width) {
								maxWidth = this.width;
							}
							width = 0;
							posX = x;
							line++;
						}

						for (int index = 0; index < s.length(); index++) {

							String s1 = s.charAt(index) + "";
							SpeechFeedback sf = new SpeechFeedback();

							if (sf.feedbackIndex().contains(index)) {

								int posY = (int) (y + (line * fontSize));
								// fontB.drawString(posX, posY - 5, s1);
								fontB.drawString(posX, posY - 5, s1, highlightColor);

								// g.drawLine(posX, posY+ fontSize,posX +
								// fontB.getWidth(s1) + 1, posY+ fontSize);
								posX += fontB.getWidth(s1);
								width += fontB.getWidth(s1);

							} else {
								if (!highlight) {
									fontB.drawString(posX, y
											+ (line * fontSize) - 5, s1, normalColor);
								} else {
									fontB.drawString(posX, y
											+ (line * fontSize) - 5, s1, highlightColor);
								}

								posX += fontB.getWidth(s1);
								width += fontB.getWidth(s1);
							}
						}

						width += fontB.getSpaceWidth();
						posX += fontB.getSpaceWidth();
					} else {
						int temp1 = width + fontB.getWidth(s)
								+ fontB.getSpaceWidth();
						if (temp1 > this.width) {
							if (maxWidth < this.width) {
								maxWidth = this.width;
							}
							width = 0;
							posX = x;
							line++;
						}

						if (!highlight) {
							fontB.drawString(posX, y + (line * fontSize) - 5, s
									+ " ", normalColor);
						} else {
							fontB.drawString(posX, y + (line * fontSize) - 5,
									s, highlightColor);
						}

						posX += fontB.getWidth(s) + fontB.getSpaceWidth();
						width += fontB.getWidth(s) + fontB.getSpaceWidth();
					}
				} else if (s.contains("<c>") && !s.contains("<b>")) {

					s = s.replace("<c>", "").replace("</c>", "");

					int temp1 = width + font.getWidth(s)
							+ font.getSpaceWidth();
					if (temp1 > this.width) {
						if (maxWidth < this.width) {
							maxWidth = this.width;
						}
						width = 0;
						posX = x;
						line++;
					}

					font.drawString(posX, y + (line * fontSize), s + " ", highlightColor);
					// g.setColor(new org.newdawn.slick.Color(25,25,255));
					// g.drawLine(posX, y + fontSize - 5,posX +
					// fontC.getWidth(s) + 1, y+ fontSize - 5);
					posX += font.getWidth(s) + font.getSpaceWidth();
					width += font.getWidth(s) + font.getSpaceWidth();
				} else if (s.contains("<c>") && s.contains("<b>")) {

					s = s.replace("<c>", "").replace("</c>", "");
					s = s.replace("<b>", "").replace("</b>", "");

					int temp1 = width + fontB.getWidth(s)
							+ fontB.getSpaceWidth();
					if (temp1 > this.width) {
						if (maxWidth < this.width) {
							maxWidth = this.width;
						}
						width = 0;
						posX = x;
						line++;
					}

					fontB.drawString(posX, y + (line * fontSize) - 5, s + " ", highlightColor);
					// g.setColor(new org.newdawn.slick.Color(25,25,255));
					// g.drawLine(posX, y + fontSize - 5,posX +
					// fontBC.getWidth(s) + 1, y+ fontSize - 5);
					posX += fontB.getWidth(s) + fontB.getSpaceWidth();
					width += fontB.getWidth(s) + fontB.getSpaceWidth();
				} else if (s.contains("<b>")) {

					s = s.replace("<c>", "").replace("</c>", "");
					s = s.replace("<b>", "").replace("</b>", "");

					int temp1 = width + fontB.getWidth(s)
							+ fontB.getSpaceWidth();
					if (temp1 > this.width) {
						if (maxWidth < this.width) {
							maxWidth = this.width;
						}
						width = 0;
						posX = x;
						line++;
					}

					if (!highlight) {
						fontB.drawString(posX, y + (line * fontSize) - 5, s
								+ " ", normalColor);
					} else {
						fontB.drawString(posX, y + (line * fontSize) - 5, s, highlightColor);
					}

					posX += fontB.getWidth(s) + fontB.getSpaceWidth();
					width += fontB.getWidth(s) + fontB.getSpaceWidth();
				}

				else {
					int temp = width + font.getWidth(s) + font.getSpaceWidth();
					if (temp > this.width) {
						if (maxWidth < this.width) {
							maxWidth = this.width;
						}
						width = 0;
						posX = x;
						line++;
					}

					if (!highlight) {
						font.drawString(posX, y + (line * fontSize) - 2, s
								+ " ", normalColor);
					} else {
						font.drawString(posX, y + (line * fontSize) - 2, s, highlightColor);
					}

					posX += font.getWidth(s) + font.getSpaceWidth();
					width += font.getWidth(s) + font.getSpaceWidth();
				}
			}
			if (maxWidth < this.width) {
				maxWidth = this.width;
			}
		}
		g.setLineWidth(1);
	}

	@Override
	public void update(GameContainer container, int delta)
			throws SlickException {
		super.update(container, delta);

		if (!clear) {
			font.loadGlyphs();
			fontB.loadGlyphs();
			
		} 

	}

	public void setText(String text) {
		this.text = text;
	}

	public float getWidth(String text) {
		return font.getWidth(text.replace("<c>", "").replace("</c>", "")
				.replace("<b>", "").replace("</b>", ""));
	}

	public float getLastX() {
		return posX;
	}

	public String getText() {
		return this.text;
	}

	public int centerText(String s) {
		s2 = (Constants.WIDTH - font.getWidth(s.replace("<c>", "")
				.replace("</c>", "").replace("<b>", "").replace("</b>", ""))) / 2;
		return s2;
	}

	public boolean highlightText(boolean b) {
		this.highlight = b;
		return b;
	}

	public boolean clearText(boolean c) {
		this.clear = c;
		return c;
	}

}
