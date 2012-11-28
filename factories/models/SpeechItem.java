package org.isip.states.speech.factories.models;

import java.util.ArrayList;
import java.util.List;

public class SpeechItem {
	
	private Integer questionId;
	private String text;
	private String phoneme;
	private String phoneme2;
	private String question;
	private String[] keywords;
	private String filename;
	
	public SpeechItem(Integer questionId) {
		this.questionId = questionId;
	}

	/**
	 * @return the questionId
	 */
	public Integer getQuestionId() {
		return questionId;
	}

	/**
	 * @param questionId the questionId to set
	 */
	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the phoneme
	 */
	public String getPhoneme() {
		return phoneme;
	}
	
	public String getPhoneme2() {
		return phoneme2;
	}
	
	public String getQuestion() {
		return question;
	}
	

	/**
	 * @param phoneme the phoneme to set
	 */
	public void setPhoneme(String phoneme) {
		this.phoneme = phoneme;
	}
	
	public void setPhoneme2(String phoneme2) {
		this.phoneme2 = phoneme2;
	}

	public void setQuestion(String question) {
		this.question= question;
	}
	
	/**
	 * @return the keywords
	 */
	public String[] getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

}
