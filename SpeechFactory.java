package org.isip.states.speech;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.isip.Game;
import org.isip.models.Question;
import org.isip.models.QuestionAnswer;
import org.isip.models.Unit;
import org.isip.models.UnitQuestion;
import org.isip.states.cloze.factories.models.Cloze;
import org.isip.states.cloze.factories.models.ClozePassage;
import org.isip.states.speech.factories.models.Speech;
import org.isip.states.speech.factories.models.SpeechItem;
import org.isip.utils.GameFlow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SpeechFactory {

	private Speech speech;
	private Unit unitModel;
	private String path;
	
	/**
	 * Default constructor
	 * 
	 * @param xmlFile
	 *            the xml file to be parsed
	 */
	public SpeechFactory(String xmlFile, String path) {
		this.path = path;
		parseXMLFile(xmlFile);
	}

	/**
	 * Parse xml file
	 * 
	 * @param xmlFile
	 */
	public void parseXMLFile(String xmlFile) {
		try {
			DocumentBuilder builder;
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document;
			document = builder.parse(xmlFile);

			Element unit = document.getDocumentElement();
			if (!unit.getNodeName().equals("unit")) {
				throw new IOException("Not a unit configuration file");
			}

			speech = new Speech(Integer.parseInt(unit.getAttribute("id")));
			speech.setGameType(getNodeValue(unit, "gameType"));
			speech.setInstructions(getNodeValue(unit, "instructions"));
			speech.setPath(path);
			
			if (!getNodeValue(unit, "resources").equals("")) {
				speech.setResources(getNodeValue(unit, "resources"));
			}
			
			this.unitModel = Game.unitDao.queryForId(speech.getUnitId());
			speech.setTitle(unitModel.getProperty("title"));
			speech.setTopic(unitModel.getProperty("topic"));

			List<UnitQuestion> unitQuestions = GameFlow.getUnitQuestions(unitModel);

			for (int i = 0; i < unitQuestions.size(); i++) {
				speech.addItem(getItem(unitQuestions.get(i)
						.getQuestion()));
			}

		} catch (IOException e) {
			System.out.println("Error opening file: " + xmlFile);
			System.out.println(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get cloze question object from question node element
	 * 
	 * @param questionNode
	 * @return cloze question object
	 * @throws SQLException 
	 */
	
	private SpeechItem getItem(Question q) throws SQLException {
		SpeechItem item = new SpeechItem(q.getQuestionId());
		
		item.setFilename(q.getProperty("voice"));
		item.setPhoneme(q.getProperty("phoneme"));
		item.setPhoneme2(q.getProperty("phoneme2"));
		item.setQuestion(q.getProperty("question"));
	
		item.setText(q.getText());
		
		List<QuestionAnswer> questionAnswers = Game.questionAnswerDao.queryForEq(QuestionAnswer.QUESTION_FIELD, q);
		
		String[] keywords = new String[questionAnswers.size()];
		int i = 0;
		for (QuestionAnswer qa : questionAnswers) {
			keywords[i] = qa.getAnswer();
			i++;
		}
		item.setKeywords(keywords);
		return item;
	}
	

	private String getNodeValue(Element element, String tagName) {
		String textValue = null;
		NodeList nodeList = element.getElementsByTagName(tagName);
		if (nodeList != null && nodeList.getLength() > 0) {
			Element temp = (Element) nodeList.item(0);
			textValue = temp.getFirstChild().getNodeValue();
		}
		if (textValue == null) {
			return "";
		}
		return textValue.trim();
	}

	public Speech getSpeech() {
		return speech;
	}

}
