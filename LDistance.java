package org.isip.states.speech;

import java.util.Vector;

public class LDistance {

	int d[][]; // matrix
	String[] sarr; // source sentence array
	String[] tarr; // target sentence array

	/**
	 * Function needed to process the Levenshtein matrix and initialize the
	 * other variables such as the string array versions of the source and
	 * target sentence strings. Code obtained from
	 * http://www.merriampark.com/ld.htm.
	 * 
	 * @param s
	 *            - Source String
	 * @param t
	 *            - Target String
	 */

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public void processLD(String s, String t) {
		/*
		 * System.out.println("THE SOURCE AND TARGET SENTENCES ARE:");
		 * System.out.println("Source: " + s); System.out.println("Target: " +
		 * t);
		 */

		String sclean = s.trim();
		String tclean = t.trim();
		sarr = sclean.split(" ");
		tarr = tclean.split(" ");

		/*
		 * //PRINTS SOURCE AND TARGET SENTENCES IN ARRAY FORMAT for(int i = 0; i
		 * < sarr.length; i++){ System.out.print(sarr[i]+", "); }
		 * System.out.println(); for(int i = 0; i < tarr.length; i++){
		 * System.out.print(tarr[i]+", "); } System.out.println();
		 */

		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t

		n = sarr.length;
		m = tarr.length;

		d = new int[n + 1][m + 1];

		for (i = 0; i <= n; i++)
			d[i][0] = i;
		for (j = 0; j <= m; j++)
			d[0][j] = j;
		for (i = 1; i <= n; i++)
			for (j = 1; j <= m; j++) {
				d[i][j] = minimum(d[i - 1][j] + 1, d[i][j - 1] + 1,
						d[i - 1][j - 1]
								+ ((sarr[i - 1].equals(tarr[j - 1])) ? 0 : 1));

			}
	}

	/**
	 * Function that returns the Levenshtein distance using the generated
	 * matrices that was initialized in the processLD() method.
	 * 
	 * @return ldistance
	 */
	public int getLDistance() {

		int n = sarr.length;
		int m = tarr.length;

		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}

		return d[n][m];
	}

	/**
	 * Returns the matrice containing the values obtained during the Levenshtein
	 * process.
	 * 
	 * @return the matrix that generated during the Levenshtein process
	 */
	public int[][] getLDMatrix() {
		return d;
	}

	/**
	 * This method returns the string array needed for the getLDString() method.
	 * 
	 * @return the String Array of the source sentence
	 */
	public String[] getSArr() {
		return sarr;
	}

	/**
	 * This method returns the string array needed for the getLDString() method.
	 * 
	 * @return the String Array of the target sentence
	 */
	public String[] getTArr() {
		return tarr;
	}

	/**
	 * Processes EDIT TEXT String by using the information obtained during the
	 * Levenshtein process. It will the return the various hit words,
	 * insertions, deletions at substitions that happened within the source
	 * sentence to obtain the target sentence.
	 * 
	 * @param d
	 *            - value returned by getLDMatrix()
	 * @param sarr
	 *            - value returned by getSArr()
	 * @param tarr
	 *            - value returned by getTArr()
	 * @return the EDIT TEXT String
	 */
	public String getLDString(int[][] d, String[] sarr, String[] tarr) {
		String finalsent = "";

		int i = d.length - 1;
		int j = d[1].length - 1;

		while (i != 0 || j != 0) {
			if (i > 0 && j > 0) {
				if (d[i][j] - d[i - 1][j - 1] == 0
						&& sarr[i - 1].equals(tarr[j - 1])) {
					finalsent = sarr[i - 1] + " " + finalsent;
					j--;
					i--;
				}
			}
			if (i > 0)
				if (d[i][j] - d[i - 1][j] == 1) {
					finalsent = "<del> " + finalsent;
					i--;
				}
			if (j > 0) {
				if (d[i][j] - d[i][j - 1] == 1) {
					finalsent = "<ins> " + finalsent;
					j--;
				}
			}
			if (i > 0 && j > 0) {
				if (d[i][j] - d[i - 1][j - 1] == 1) {
					finalsent = "<sub> " + finalsent;
					j--;
					i--;
				}
			}
		}
		return finalsent;
	}

	/**
	 * 
	 * This creates a list of the results of the different indices, ignoring the
	 * insertions that occured. The best statistic (hit, del, ins, sub) will be
	 * also displayed.
	 * 
	 * @param LDString
	 * @return errorlist with the positions of the deletions and substitutions
	 *         occured
	 */
	public Vector<Integer> getLDStringStats(String LDString) {
		String[] ldarr = LDString.split(" ");
		Vector<String> revisedString = new Vector<String>();
		Vector<Integer> errorlist = new Vector<Integer>();
		int hit = 0;
		int del = 0;
		int ins = 0;
		int sub = 0;

		for (int i = 0; i < ldarr.length; i++) {
			if (!ldarr[i].equals("<ins>")) {
				revisedString.add(ldarr[i]);
			}
			if (ldarr[i].equals("<ins>")) {
				ins++;
			} else if (ldarr[i].equals("<del>")) {
				del++;
			} else if (ldarr[i].equals("<sub>")) {
				sub++;
			} else {
				hit++;
			}
		}

		errorlist.add(sub + del);
		errorlist.add(sub + del + hit);

		for (int i = 0; i < revisedString.size(); i++) {
			if (revisedString.get(i).equals("<del>")) {
				errorlist.add(i);
			} else if (revisedString.get(i).equals("<sub>")) {
				errorlist.add(i);
			}
		}

		System.out.println("BEST STATISTIC: wer = " + errorlist.firstElement()
				+ "; hit = " + hit + "; del = " + del + "; insert = " + ins
				+ "; sub =" + sub);

		return errorlist;
	}

}
