package org.bme.mit.iir;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Util {

	public static String readFileAsString(String fname) throws IOException {
		return join(readLinesIntoList(fname), "\n");
	}

	private static boolean isStopWord(String word) {
		boolean isStop = word.equals("\n") || word.equals("--") || word.equals(" ");
		isStop = isStop || word.equals("a") || word.equals("és") || word.equals("az");
		isStop = isStop || word.equals("\t") || word.equals(".") || word.equals(",");
		isStop = isStop || word.equals("_") || word.equals(">") || word.equals("<");
		isStop = isStop || word.equals("azok") || word.equals("amik") || word.equals("ezt");
		isStop = isStop || word.equals("ez") || word.equals("ezek") || word.equals("is");
		return isStop;
	}
	
	public static List<String> readLinesIntoList(String fname) throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(fname), Charset.forName("ISO-8859-1")));
		try {
			ArrayList<String> lines = new ArrayList<String>();
			String line;
			while ((line = br.readLine()) != null) {
				if(line.length() != 0) {
					String[] words = line.split(" ", 0); // apply the regex every time
					for(int i = 0; i<words.length; ++i) {
						words[i] = words[i].toLowerCase().replaceAll("[.,;:?!=///<>//(//)//[//]]", "");
						if(!isStopWord(words[i]))
							lines.add(words[i]); // collect the words from the file
					}
					//lines.add(line);
				}
			}
			return lines;
		} finally {
			br.close();
		}
	}

	public static <T> String join(final Iterable<T> objs, final String delimiter) {
		Iterator<T> iter = objs.iterator();
		if (!iter.hasNext())
			return "";
		StringBuilder sb = new StringBuilder(String.valueOf(iter.next()));
		while (iter.hasNext())
			sb.append(delimiter).append(String.valueOf(iter.next()));
		return sb.toString();
	}
}
