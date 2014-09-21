import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class QuerySimilarity {

	private static HashMap<String, Double> QueryTF = new HashMap<String, Double>();// 记录每个查询语句的TF
	private static HashMap<String, Double> DocumentTF = new HashMap<String, Double>();// 记录每个Question的TF
	private static HashMap<String, Integer> QueryCount = new HashMap<String, Integer>();// 记录每个查询语句的word
	// count
	private static HashMap<String, Integer> DocumentCount = new HashMap<String, Integer>();// 记录每个Question的word
	// count
	private static HashSet<String> sw = new HashSet<String>();// stopword集合的引用
	private static HashMap<String, Double> CollectionTF = new HashMap<String, Double>();
	private static ArrayList<Entry<String, Double>> TopResults = new ArrayList<Entry<String, Double>>();// 记录查询的前topnum个结果
	private static HashMap<String, Double> Results = new HashMap<String, Double>();// 记录所有的结果
	private static String fileContent = new String();// 当前所读文件的内容
	private static HashMap<String, String> QAContent = new HashMap<String, String>();// 记录questionid和QA内容
	private static HashMap<String, Integer> questionWd = new HashMap<String, Integer>();// 记录QA的questionid和内容的长度
	private static HashMap<String, Integer> Wordft = new HashMap<String, Integer>();// Okapi
	// BM25
	// Model中的ft，记录每个词t在多少个question中出现过
	// private static HashMap<String, Integer> Q2D = new HashMap<String,
	// Integer>();// question到Description的转移概率表中，单词标号
	// private static HashMap<String, Integer> D2Q = new HashMap<String,
	// Integer>();// Description到question的转移概率表中，单词标号
	private static HashMap<Coordinate, Double> T = new HashMap<Coordinate, Double>();
    private static HashMap<String,Double> TrLMweight = new HashMap<String,Double>();//用TranslationLM得到的Weight
	private static double WA = 0.0;
	private static long N = 0;
	private static String Pre_instanceName = new String();// 记录上一次在那个目录里进行了查询如果本次相同则不进行初始化

	/**
	 * @param args
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public QuerySimilarity(String collection_name) throws IOException,
			InterruptedException, ClassNotFoundException {

		if (!Pre_instanceName.equals(collection_name)) {
			String SerializableObjDir = ".\\serializable_object";
			QueryTF.clear();
			DocumentTF.clear();
			CollectionTF.clear();
			Results.clear();
			QAContent.clear();
			questionWd.clear();
			Wordft.clear();
			sw.clear();
			ObjectInputStream oor = new ObjectInputStream(new FileInputStream(
					new File(SerializableObjDir + "\\" + "stopword.save")));
			sw = (HashSet<String>) oor.readObject();
			oor = new ObjectInputStream(new FileInputStream(new File(
					SerializableObjDir + "\\" + "T.save")));
			T = (HashMap<Coordinate, Double>) oor.readObject();
			oor = new ObjectInputStream(new FileInputStream(new File(
					SerializableObjDir + "\\" + collection_name
							+ "_WordTF.save")));
			CollectionTF = (HashMap<String, Double>) oor.readObject();
			oor = new ObjectInputStream(new FileInputStream(new File(
					SerializableObjDir + "\\" + collection_name
							+ "_fileContent.save")));
			fileContent = (String) oor.readObject();
			oor = new ObjectInputStream(new FileInputStream(new File(
					SerializableObjDir + "\\" + collection_name
							+ "_questionWd.save")));
			questionWd = (HashMap<String, Integer>) oor.readObject();
			oor = new ObjectInputStream(new FileInputStream(new File(
					SerializableObjDir + "\\" + collection_name
							+ "_Wordft.save")));
			Wordft = (HashMap<String, Integer>) oor.readObject();

			/*
			 * System.out.println("collectionTF size = " + CollectionTF.size() +
			 * " filecontent length = " + fileContent.length() + " sw size = " +
			 * sw.size());
			 */

			oor.close();
			WA = question_aver_length();
			N = questionWd.size();
			
			File f = new File("..\\ProductReviewOpinionMining\\TrLMWeight\\Nokia_N95.txt");//读入TrLMWeight文件
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = null;
			while((line = br.readLine())!=null)
			{
				String[] words = line.split(":");
				if(!words[1].equals("-Infinity"))
				{
					TrLMweight.put(words[0], Double.parseDouble(words[1]));
				}
			}
			
		}// end of if
		Pre_instanceName=collection_name;
	}

	private String[] ReadDirectory(String filepath) {
		File file = new File(filepath);
		String list[] = file.list();
		System.out.println("filelist = ");
		int num = 1;
		for (String s : list) {
			System.out.println("          " + num + ". " + s);
			num++;
		}
		System.out.println();
		return list;
	}

	public HashMap<String, String> getQAContent() {
		return QAContent;
	}

	public ArrayList<Entry<String, Double>> PrintTopResultes(int topnum) {

		ArrayList<Entry<String, Double>> array = new ArrayList<Entry<String, Double>>(
				Results.entrySet());
		TopResults.clear();
		long start = System.currentTimeMillis();
		Collections.sort(array, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
					Map.Entry<String, Double> o2) {
				if (o2.getValue() - o1.getValue() > 0)
					return 1;
				else if (o2.getValue() - o1.getValue() == 0)
					return 0;
				else
					return -1;
			}
		});
		System.out.println("Sort similatiries time = "
				+ (System.currentTimeMillis() - start) + "ms");
		Integer top = 0;
		for (Entry<String, Double> e : array) {
			if (top++ < topnum) {
				System.out.println(top.toString() + ". " + e.getKey() + "  "
						+ e.getValue());
				TopResults.add(e);
			} else
				break;
		}

		return TopResults;// 将前Topnum的结果返回

	}

	public HashMap<String, Double> Query_Question_WordTF(String content)
			throws InterruptedException//
	{
		HashMap<String, Double> TF = new HashMap<String, Double>();
		HashMap<String, Integer> WC = new HashMap<String, Integer>();
		String words[] = content.split("[^^a-zA-Z0-9|$|']");
		for (String w : words) {
			String w_lower = w.toLowerCase();
			if ((w.length() >= 3) && !sw.contains(w_lower)) {

				if (WC.containsKey(w_lower)) {
					int num = WC.get(w_lower) + 1;
					WC.put(w_lower, num);
				} else {
					WC.put(w_lower, 1);
				}
			}
		}

		long Totalwordcount = 0;
		for (String s : WC.keySet()) {
			Totalwordcount += WC.get(s);
		}
		// System.out.println("totalwordcount = "+ Totalwordcount);
		for (String s : WC.keySet()) {
			TF.put(s, (double) WC.get(s) / Totalwordcount);
		}
		return TF;
	}

	public HashMap<String, Integer> Query_Question_WordCount(String content)
			throws InterruptedException//
	{
		HashMap<String, Integer> WC = new HashMap<String, Integer>();
		String words[] = content.split("[^^a-zA-Z0-9|$|']");
		for (String w : words) {
			String w_lower = w.toLowerCase();
			if ((w.length() >= 3) && !sw.contains(w_lower)) {

				if (WC.containsKey(w_lower)) {
					int num = WC.get(w_lower) + 1;
					WC.put(w_lower, num);
				} else {
					WC.put(w_lower, 1);
				}
			}

		}
		return WC;
	}

	private double question_aver_length() {
		long totallength = 0;
		Iterator it = questionWd.keySet().iterator();
		while (it.hasNext()) {
			totallength += questionWd.get(it.next());
		}
		return (double) totallength / questionWd.size();
	}

	public double getSimilarity(String ModelType,
			String questionid) throws InterruptedException {
		double similarity = 0.0;
		boolean overlap = false;

		if (ModelType.equals("Okapi BM25 Model")) {
			
			System.out.println("QueryCount = "+QueryCount);
			double Kd = 1.2 * (0.25 + 0.75
					* (double) questionWd.get(questionid) / WA);
			for (String s : QueryCount.keySet()) {
				if (DocumentCount.containsKey(s)) {
					double p = Math.log((N - Wordft.get(s) + 0.5)
							/ Wordft.get(s) + 0.5)
							* QueryCount.get(s)
							* (1.2 + 1)
							* DocumentCount.get(s)
							/ (Kd + DocumentCount.get(s));
					if (p != 0.0)
						overlap = true;
					similarity += p;
				}
			}

		} else if (ModelType.equals("Language Model")) {

			double lamda = 0.2;
			double p = 0.0;
			for (String s : QueryTF.keySet()) {
				double Pmltd = 0.0, PmltColl = 0.0;
				if (DocumentTF.containsKey(s))
					Pmltd = DocumentTF.get(s);
				if (CollectionTF.containsKey(s))
					PmltColl = CollectionTF.get(s);
				if (Pmltd!=0.0 || PmltColl!=0.0) {
					p = Math.log10(((1 - lamda) * Pmltd + lamda
							* PmltColl));

					if (p != 0.0)
						overlap = true;
					similarity += p;
				}
			}
		} else if (ModelType.equals("TrLMWeight Language Model")) {

			double lamda = 0.2;
			double p = 0.0;
			double tweight = 1.0;
			double alfa = 1.0;
			for (String s : QueryTF.keySet()) {
				if(TrLMweight.containsKey(s))
				{
				  tweight = 1.0+Math.exp(TrLMweight.get(s)+alfa);
				}else {
					for(String aspect:TrLMweight.keySet())
						if(aspect.contains(s))
							tweight = 1.0+Math.exp(TrLMweight.get(aspect)+alfa);
				}
				//System.out.println("Term "+s+" weight is "+tweight);
				double Pmltd = 0.0, PmltColl = 0.0;
				if (DocumentTF.containsKey(s))
					Pmltd = DocumentTF.get(s);
				if (CollectionTF.containsKey(s))
					PmltColl = CollectionTF.get(s);
				if (Pmltd!=0.0 || PmltColl!=0.0) {
					p = Math.log10(tweight*((1 - lamda) * Pmltd + lamda
							* PmltColl));

					if (p != 0.0)
						overlap = true;
					similarity += p;
				}
				if((s.equals("n95")||s.equals("n95")||s.equals("battery"))&Pmltd!=0.0) System.out.println(s+" = "+p+" tweight = "+tweight);
				tweight = 1.0;//保证下一轮循环时tweight = 1.0
			}
		} else if (ModelType.equals("Translation Model")) {

			double lamda = 0.2;
			double p = 0.0;
			for (String s : QueryTF.keySet()) {
				double sum = 0.0, PmltColl = 0.0;
				for (String w : DocumentTF.keySet()) {
					if (w.equals(s))
						sum += DocumentTF.get(s);
					else if (T.containsKey(new Coordinate(s, w)))
						sum += T.get(new Coordinate(s, w)) * DocumentTF.get(w);
				}// end of for
				if (CollectionTF.containsKey(s))
					PmltColl = CollectionTF.get(s);
				if (sum != 0.0 || PmltColl != 0.0) {
					p = Math.log10(((1 - lamda) * sum + lamda * PmltColl));

					if (p != 0.0)
						overlap = true;
					similarity += p;
				}

			}
		} else if (ModelType.equals("Translation Based LM")) {
			
			double lamda = 0.2;
			double beita = 0.2;
			for (String s : QueryTF.keySet()) {
				double sum = 0.0;
				double Pmltd = 0.0;
				double p = 0.0, PmltColl = 0.0;
				if (DocumentTF.containsKey(s))
					Pmltd = DocumentTF.get(s);
				for (String w : DocumentTF.keySet()) {
					if (w.equals(s))
						sum += DocumentTF.get(s);
					else if (T.containsKey(new Coordinate(s, w)))
						sum += T.get(new Coordinate(s, w)) * DocumentTF.get(w);
				}// end of for2
				if (CollectionTF.containsKey(s))
					PmltColl = CollectionTF.get(s);
				if (sum != 0.0 || Pmltd != 0.0 || PmltColl != 0.0) {
					p = Math.log10(((1 - lamda)
							* (beita * sum + (1 - beita) * Pmltd) + lamda
							* PmltColl));

					if (p != 0.0)
						overlap = true;
					similarity += p;
				}

			}// end of for1
		}else if (ModelType.equals("TrLMWeight Translation Based LM")) {
			
			double lamda = 0.2;
			double beita = 0.2;
			for (String s : QueryTF.keySet()) {
				double sum = 0.0;
				double Pmltd = 0.0;
				double p = 0.0, PmltColl = 0.0;
				if (DocumentTF.containsKey(s))
					Pmltd = DocumentTF.get(s);
				for (String w : DocumentTF.keySet()) {
					if (w.equals(s))
						sum += DocumentTF.get(s);
					else if (T.containsKey(new Coordinate(s, w)))
						sum += T.get(new Coordinate(s, w)) * DocumentTF.get(w);
				}// end of for2
				if (CollectionTF.containsKey(s))
					PmltColl = CollectionTF.get(s);
				if (sum != 0.0 || Pmltd != 0.0 || PmltColl != 0.0) {
					p = Math.log10(((1 - lamda)
							* (beita * sum + (1 - beita) * Pmltd) + lamda
							* PmltColl));

					if (p != 0.0)
						overlap = true;
					similarity += p;
				}

			}// end of for1
		}

		// System.out.println(similarity);
		if (similarity == 0.0)
			return -10000.0;
		else
			return similarity;
	}

	public ArrayList<Entry<String, Double>> startSearch(String query,
			String ModelType) throws IOException, InterruptedException,
			ClassNotFoundException {
		String Query = query;
		System.out.println("query=" + query);
		QAContent.clear();
		Results.clear();

		String questionid = null;

		System.out.println("start Search...");
		// int question_num = 0;
		
		QueryCount.clear();
		QueryCount = Query_Question_WordCount(query);
		QueryTF.clear();
		QueryTF = Query_Question_WordTF(query);
		
		System.out.println("QueryCount = "+QueryCount);
		System.out.println("QueryTF = "+QueryTF);
		if (fileContent != null) {
			Document doc = Jsoup.parse(fileContent);
			Elements es = doc.getElementsByTag("question");//
			double similarity = 0.0;
			long start = System.currentTimeMillis();
			for (Element e : es) {//for each question

				DocumentTF.clear();
				DocumentCount.clear();
				
				Document doc2 = Jsoup.parse(e.toString());
				questionid = doc2.select("question_id").text();
				if (ModelType.equals("Okapi BM25 Model")) {

					DocumentCount = Query_Question_WordCount(e.toString());
					similarity = getSimilarity(ModelType, questionid);

				} else if (ModelType.equals("Language Model")) {

					DocumentTF = Query_Question_WordTF(e.toString());
					similarity = getSimilarity(ModelType, questionid);
				} else if (ModelType.equals("TrLMWeight Language Model")) {
					DocumentTF = Query_Question_WordTF(e.toString());
					similarity = getSimilarity(ModelType, questionid);
				}else if (ModelType.equals("Translation Model")) {
					DocumentTF = Query_Question_WordTF(e.toString());
					similarity = getSimilarity(ModelType, questionid);
				} else if (ModelType.equals("Translation Based LM")) {
					DocumentTF = Query_Question_WordTF(e.toString());
					similarity = getSimilarity(ModelType, questionid);
				}else if (ModelType.equals("TrLMWeight Translation Based LM")) {
					DocumentTF = Query_Question_WordTF(e.toString());
					similarity = getSimilarity(ModelType, questionid);
				}

				Results.put(questionid, similarity);
				QAContent.put(questionid, e.toString());
				// System.out.println("question_num = "+ question_num++);

			}
			System.out.println("GetSimilarity Time = "
					+ (System.currentTimeMillis() - start) + "ms");
			/*
			 * System.out.println("Total question size = " + es.size() +
			 * " Results size = " + Results.size() + " sim=" +
			 * Results.get("20120611184528AAxKWE8") + "  QASize=" +
			 * QAContent.size());
			 */
		}
		return PrintTopResultes(20);
	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		// TODO Auto-generated method stub

		QuerySimilarity QS = new QuerySimilarity("music converter.txt");
		String Query = "How to get frostwire videos onto a rca opal mp3 player?";

		QS.startSearch(Query, "Okapi BM25 Model");

	}
}
