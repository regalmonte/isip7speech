package org.isip.states.speech;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeechFeedback {

	private static ArrayList<Integer> store = new ArrayList<Integer>();

	public static String result(String input, String text) {

		StringBuffer result = new StringBuffer();
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(input);

		store.clear();
		
		
		while (m.find()) {
			int n = Integer.parseInt(m.group());
			store.add(n);
		}
		// sentences
		if (text.contains(" ")) {

			String[] words = text.split(" ");
			String[] combine = new String[words.length];

			ArrayList<String> wordList = new ArrayList<String>();

			for (String splitWords : words) {
				wordList.add(splitWords);
			}

			for (int i = 0; i < words.length; i++) {
				if (store.contains(i)) {
					combine[i] = wordList.get(i).replace(wordList.get(i),
							"<c>" + wordList.get(i) + "</c>");
				} else {
					combine[i] = wordList.get(i);
				}
			}

			for (int i = 0; i < combine.length; i++) {
				if (i != 0) {
					result.append(" " + combine[i]);
				} else {
					result.append(combine[i]);
				}
			}

			// words
		} else {

			String s;
			String[] combine = new String[text.length()];
			
			for (int i = 0; i < text.length(); i++) {
				s = text.substring(i, i + 1);
				if (store.contains(i)) {
					combine[i] = s.replace(s, "<c>" + s + "</c>");
				} else {
					combine[i] = s;
				}
			}

			// Combine strings inside array
			for (int i = 0; i < combine.length; i++) {
				result.append(combine[i]);
			}
			
		}

		return result.toString();

	}

	public ArrayList<Integer> feedbackIndex() {
		return store;
	}

}
