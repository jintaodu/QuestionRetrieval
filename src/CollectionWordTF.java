import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;

public class CollectionWordTF {

	private static HashMap<String, Integer> WordCount = new HashMap<String, Integer>();// 每个txt文件中word的整数词频
	private static HashSet<String> sw = new HashSet<String>();
	private static HashMap<String, Double> WordTF = new HashMap<String, Double>();// 每个txt文件中word的小数词频
	private static HashMap<String, Integer> Wordft = new HashMap<String, Integer>();// Okapi
	// BM25
	// Model中的ft，记录每个词t在多少个question中出现过
	private static String currentfname = new String();// 当前扫描文件的文件名包括路径
	private static StringBuffer fileContent = new StringBuffer();// 当前所读文件的内容
	private static HashMap<String, String> QACon = new HashMap<String, String>();// 记录QA的questionid和内容
	private static HashMap<String, Integer> questionWd = new HashMap<String, Integer>();// 记录QA的questionid和内容的长度
	private static Integer questionlength = new Integer(0);// 此全局变量无需序列化

	private static HashMap<String, Integer> WordCountAll = new HashMap<String, Integer>();//
	private static HashMap<String, Double> WordTFAll = new HashMap<String, Double>();//
	private static HashMap<String, Integer> WordftAll = new HashMap<String, Integer>();//
	private static HashMap<String, Integer> questionWdAll = new HashMap<String, Integer>();
	private static StringBuffer fileContentAll = new StringBuffer();
	//private static HashMap<String, Integer> Q2D = new HashMap<String, Integer>();// question到Description的转移概率表中，单词标号
	//private static HashMap<String, Integer> D2Q = new HashMap<String, Integer>();// Description到question的转移概率表中，单词标号
	private static HashMap<Coordinate, Double> T = new HashMap<Coordinate, Double>();
	private static String directoryname = null;

	public CollectionWordTF() {
	}

	public CollectionWordTF(String dirname) throws IOException,
			InterruptedException// 完成WordTF、sw、fileContent的对象序列化
	{

		String directoryname = dirname;
		String SerializableObjDir = ".\\serializable_object";
		if (!new File(SerializableObjDir).exists())
			new File(SerializableObjDir).mkdir();
		Readsw("Stopwords");
		CreatTtable();// 创建Translation Model中的Ttable
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(SerializableObjDir + "\\" + "stopword.save")));
		oos.writeObject(sw);
		oos.flush();
		oos = new ObjectOutputStream(new FileOutputStream(new File(
				SerializableObjDir + "\\" + "T.save")));
		oos.writeObject(T);
		oos.flush();
		/*oos = new ObjectOutputStream(new FileOutputStream(new File(
				SerializableObjDir + "\\" + "Q2D.save")));
		oos.writeObject(Q2D);
		oos.flush();
		oos = new ObjectOutputStream(new FileOutputStream(new File(
				SerializableObjDir + "\\" + "D2Q.save")));
		oos.writeObject(D2Q);
		oos.flush();*/
		oos.close();

		String list[] = ReadDirectory(directoryname);
		System.out.println("Traverse files in  directory: " + directoryname);

		File f = new File(SerializableObjDir + "\\" + "FileList.txt");
		FileWriter fw = new FileWriter(f, false);// 不追加文件，重新写文件

		for (String fname : list)// 遍历collection目录下的每一个txt文件
		{
			CopeSubCollection(fname, directoryname, SerializableObjDir);
			fw.write(fname + "\r\n");
			/*
			 * Iterator it = QACon.keySet().iterator();
			 * while(it.hasNext())//进行merge操作 { fw.write(QACon.get(it.next()));
			 * fw.flush(); }
			 */

		}
		// fw.close();/*将不用的数据结构清空*/
		WordCount.clear();
		WordTF.clear();
		Wordft.clear();
		T.clear();
		fileContent.delete(0, fileContent.length());
		QACon.clear();
		questionWd.clear();
		System.gc();

		CopeAllCollection("AllCollections.txt", directoryname,
				SerializableObjDir);
		fw.write("AllCollections.txt");
		fw.flush();
		fw.close();
		System.out.println("Initializing Process Over...");
	}

	private void CopeAllCollection(String fname, String directoryname,
			String SerializableObjDir) throws FileNotFoundException,
			IOException {
		currentfname = fname;
		WordCount.clear();// 每个txt文件中word的整数词频
		WordTF.clear();// 每个txt文件中word的小数词频

		long Totalwordcount = 0;
		for (String s : WordCountAll.keySet()) {
			Totalwordcount += WordCountAll.get(s);
		}
		System.out.println("totalwordcount = " + Totalwordcount);
		for (String s : WordCountAll.keySet()) {
			WordTFAll.put(s, (double) WordCountAll.get(s) / Totalwordcount);
		}

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(SerializableObjDir + "\\" + fname + "_WordTF.save")));
		oos.writeObject(WordTFAll);
		oos.flush();
		oos = new ObjectOutputStream(new FileOutputStream(new File(
				SerializableObjDir + "\\" + fname + "_fileContent.save")));
		oos.writeObject(fileContentAll.toString());
		oos = new ObjectOutputStream(new FileOutputStream(new File(
				SerializableObjDir + "\\" + fname + "_Wordft.save")));
		oos.writeObject(WordftAll);
		oos.flush();
		oos = new ObjectOutputStream(new FileOutputStream(new File(
				SerializableObjDir + "\\" + fname + "_questionWd.save")));
		oos.writeObject(questionWdAll);
		oos.flush();
		oos.close();
		WordCountAll.clear();
		WordTFAll.clear();
		WordftAll.clear();
		questionWdAll.clear();
		fileContentAll.delete(0, fileContentAll.length());
		System.gc();
		System.out.println("**************************************");
	}

	private void CopeSubCollection(String fname, String directoryname,
			String SerializableObjDir) throws IOException, InterruptedException {

		currentfname = fname;
		WordCount.clear();// 每个txt文件中word的整数词频
		WordTF.clear();// 每个txt文件中word的小数词频

		ReadFile(directoryname + "\\" + fname);// 将collection目录下的txt文档读入

		filter(fileContent.toString());// 过滤重复的问题

		// WriteFile("WordCount.txt");

		long Totalwordcount = 0;
		for (String s : WordCount.keySet()) {
			Totalwordcount += WordCount.get(s);
		}
		System.out.println("totalwordcount = " + Totalwordcount);
		for (String s : WordCount.keySet()) {
			WordTF.put(s, (double) WordCount.get(s) / Totalwordcount);
		}

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(SerializableObjDir + "\\" + fname + "_WordTF.save")));
		oos.writeObject(WordTF);
		oos.flush();
		oos = new ObjectOutputStream(new FileOutputStream(new File(
				SerializableObjDir + "\\" + fname + "_fileContent.save")));
		oos.writeObject(fileContent.toString());
		oos = new ObjectOutputStream(new FileOutputStream(new File(
				SerializableObjDir + "\\" + fname + "_Wordft.save")));
		oos.writeObject(Wordft);
		oos.flush();
		oos = new ObjectOutputStream(new FileOutputStream(new File(
				SerializableObjDir + "\\" + fname + "_questionWd.save")));
		oos.writeObject(questionWd);
		oos.flush();
		oos.close();

		System.out.println("**************************************");

	}

	public String[] ReadDirectory(String filepath) {
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

	public void WriteFile(String fname1) throws IOException// 写文件
			, InterruptedException {
		File file = new File(fname1);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		ArrayList<Entry<String, Integer>> array = new ArrayList<Entry<String, Integer>>(
				WordCount.entrySet());

		Collections.sort(array, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o2.getValue() - o1.getValue());
			}
		});
		for (Entry<String, Integer> e : array) {

			output.write(e.getKey() + "  " + e.getValue() + "\n");

		}
		output.flush();

	}

	public HashSet<String> getStopWord() {
		return sw;
	}

	public String getfileContent() {
		return fileContent.toString();
	}

	public static void setdirectoryname(String dir) {
		directoryname = dir;
	}

	public static String getdirectoryname() {
		return directoryname;
	}

	/*private int Init_Q2D_D2Q(String fpath, String flag) throws IOException {
		File f = new File(fpath);
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = null;
		int max_num = 1;
		while ((line = br.readLine()) != null) {
			String word[] = line.split(" ");
			int num = Integer.parseInt(word[0]);
			if (flag.equals("Q2D")) {
				Q2D.put(word[1], num);
			} else {
				D2Q.put(word[1], num);
			}
			max_num++;
		}
		br.close();
		return max_num;
	}*/

	private void Comp_Ttable(String fpath, String flag) throws IOException {
		File f = new File(fpath);
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = null;
		while ((line = br.readLine()) != null) {
			String word[] = line.split(" ");
			double sim = Double.parseDouble(word[2]);
			if(!(word[0].equals("NULL"))&&!(word[1].equals("NULL")))
			{
				Coordinate co = new Coordinate(word[0], word[1]);
				if(T.containsKey(co))
					T.put(co, 0.5 * sim + 0.5 * T.get(co));
				else {
						T.put(co, sim);
				}// end of if
			}
		}
	}

	public void CreatTtable() throws IOException {
		/*Q2D.clear();
		int x_axis = Init_Q2D_D2Q(".\\Ttable\\chinese.vcb", "Q2D");
		System.out.println("Q2D size = " + Q2D.size());
		D2Q.clear();
		int y_axis = Init_Q2D_D2Q(".\\Ttable\\english.vcb", "D2Q");
		System.out.println("D2Q size = " + D2Q.size());
		System.out.println("x=" + x_axis + " y= " + y_axis);*/

		Comp_Ttable(".\\Ttable\\c2e.actual.t1.final", "Q2D");
		// System.out.println("====="+T.get(new Coordinate(5512 ,131902)));
		Comp_Ttable(".\\Ttable\\e2c.actual.t1.final", "D2Q");
		// System.out.println("====="+T.get(new Coordinate(5512,131902)));
	}

	public void Readsw(String fname) throws IOException// 读文件
	{

		String line = new String();
		File file = new File(fname);
		BufferedReader input = new BufferedReader(new FileReader(file));
		while ((line = input.readLine()) != null) {
			if (line.length() > 0)
				sw.add(line);
		}
		System.out.println("Stop words have read done! sw size = " + sw.size());

	}

	public void ReadFile(String fname) throws IOException// 读文件
			, InterruptedException {

		System.out.println("Start read file content " + fname);
		String line = new String();
		File file = new File(fname);
		if (fileContent.length() != 0)
			fileContent.delete(0, fileContent.length());
		// System.out.println("filecontent length = " + fileContent.length());
		BufferedReader input = new BufferedReader(new FileReader(file));
		int line_num = 0;
		while ((line = input.readLine()) != null) {
			fileContent.append(line);
			// System.out.println("Now at Line " + line_num++);

		}
		System.out.println("End read file content " + fname);
	}

	public void filter(String content) throws InterruptedException {
		if (content.length() == 0) {
			System.out.println("File : " + currentfname
					+ "\'s content is empty!");
			return;
		}
		QACon.clear();// 将记录QA内容的HashMap清空
		questionWd.clear();
		Wordft.clear();
		System.out.println("Filtering Begining...");
		Document doc = Jsoup.parse(content);
		Elements es = doc.getElementsByTag("question");
		for (Element e : es) {
			Document doc2 = Jsoup.parse(e.toString());
			String questionid = doc2.select("question_id").text();
			QACon.put(questionid, e.toString());
		}
		Iterator it = QACon.keySet().iterator();
		fileContent.delete(0, fileContent.length());
		while (it.hasNext()) {
			String questionid = it.next().toString();
			String str = QACon.get(questionid);
			fileContent.append(str);
			fileContentAll.append(str);
			enrichWordCount(str, questionid);
		}

		System.out.println("Filtering Over... Question num = " + QACon.size());
	}

	public void enrichWordCount(String question, String questionid)
			throws InterruptedException// 对每个问题的文本抽取词
	{

		String words[] = question.split("[^^a-zA-Z0-9|$|']");
		for (String w : words) {
			String w_lower = w.toLowerCase();
			if ((w.length() >= 3) && !sw.contains(w_lower)) {
				// System.out.println(w);
				if (WordCount.containsKey(w_lower)) {
					int num = WordCount.get(w_lower) + 1;
					WordCount.put(w_lower, num);
				} else {
					WordCount.put(w_lower, 1);
				}
				if (WordCountAll.containsKey(w_lower)) {
					int num = WordCountAll.get(w_lower) + 1;
					WordCountAll.put(w_lower, num);
				} else {
					WordCountAll.put(w_lower, 1);
				}
			}
		}// end of for

		HashSet<String> word = ReduceRedundancyword(words);
		questionWd.put(questionid, questionlength);
		questionWdAll.put(questionid, questionlength);

		for (String s : word) {// 为了计算ft下面循环时，word不能重复

			if (Wordft.containsKey(s)) {
				int num = Wordft.get(s) + 1;
				Wordft.put(s, num);
			} else
				Wordft.put(s, 1);
			if (WordftAll.containsKey(s)) {
				int num = WordftAll.get(s) + 1;
				WordftAll.put(s, num);
			} else
				WordftAll.put(s, 1);
		}// end of for

	}

	private HashSet<String> ReduceRedundancyword(String[] words) {// 将重复的词去掉，并计算question的长度
		questionlength = 0;
		HashSet<String> word = new HashSet<String>();
		for (String s : words) {
			if (s.length() >= 3 && !sw.contains(s.toLowerCase())) {
				word.add(s.toLowerCase());
				questionlength++;
			}
		}
		return word;
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		//CollectionWordTF lm = new CollectionWordTF();
		//lm.CreatTtable();
		File f = new File(".\\collection");
		if (!f.exists()) {
			System.out.println(".\\collection not exist!");
			return;
		}
		CollectionWordTF lm = new CollectionWordTF(".\\collection");

	}
}
