import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class QuestionAnalysis {

	/**
	 * 本类完成Recognizing productname、Identifying aspects、classifying the
	 * opinions、identifying the question type
	 */
	private String[] ProductNames = new String[] { "Canon EOS 450D",
			"Apple MacBook Pro", "Samsung NC10", "Apple iPod Touch",
			"Sony NWZ-S639", "BlackBerry Bold 9700", "Iphone 3GS 16GB",
			"Nokia 5800 XpressMusic", "Nokia N95" };

	private void WriteFile(File f, String content) throws IOException {
		BufferedWriter br = new BufferedWriter(new FileWriter(f, true));// 追加方式写入
		br.write(content);
		br.flush();
		br.close();
	}

	private String ReadFile(String fpath) throws IOException {
		File f = new File(fpath);
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = null;
		StringBuffer content = new StringBuffer();
		while ((line = br.readLine()) != null) {
			content.append(line);
		}
		br.close();
		return content.toString();
	}

	public String[] Recognize_ProductName(String input) {

		String RecognizedProductNames = "";
		for (int i = 0; i < ProductNames.length; ++i) {
			if (input.contains(ProductNames[i]))
				RecognizedProductNames += ProductNames[i] + "<-->";
		}
		String substring = RecognizedProductNames.substring(0,
				RecognizedProductNames.lastIndexOf("<-->"));
		String[] Names = substring.split("<-->");

		return Names;

	}

	public String[] Identify_aspects(String input) {
		String[] aspects = new String[] {};
		File manualaspects = new File("..\\ProductSearch\\ManualAspects");// ..为上一级目录
		return aspects;
	}

	public int countspecificchar(String sentence, char ch) {
		int count = 0;
		for (int i = 0; i < sentence.length(); ++i) {
			if (ch == sentence.charAt(i))
				count++;
		}
		return count;
	}

	public void printresult(HashMap<String, Integer> result) {// 将hashmap中的元素排序后打印输出
		System.out.println("Sort Result :");
		ArrayList<Entry<String, Integer>> array = new ArrayList<Entry<String, Integer>>(
				result.entrySet());

		Collections.sort(array, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				if (o2.getValue() - o1.getValue() > 0)
					return -1;
				else if (o2.getValue() - o1.getValue() == 0)
					return 0;
				else
					return 1;
			}
		});

		for (Entry<String, Integer> e : array) {
			System.out.println(e.getKey() + ":" + e.getValue());

		}
	}
	
	public static void main(String[] args) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub

		QuestionAnalysis QA = new QuestionAnalysis();

		HashMap<String, String> questions = new HashMap<String, String>();
		File dataset = new File("E:\\DataSet\\Filtered\\");
		String[] filelist = dataset.list();
		for (String filename : filelist) {
			String content = QA.ReadFile("E:\\DataSet\\Filtered\\" + filename);
			Document doc = Jsoup.parse(content);
			Elements es = doc.getElementsByTag("question");

			for (Element e : es) {
				Document doc2 = Jsoup.parse(e.toString());
				Elements questionid = doc2.getElementsByTag("question_id");
				questions.put(questionid.text(), e.toString());
			}

		}// end of for (String filename : filelist)
		System.out.println("All questions number = " + questions.size());
		
		HashMap<String,Integer> aspectsupport = new HashMap<String,Integer>();
		File manualaspects = new File(
				"C:\\JAVA WorkSpace\\eclipse helios  64bit\\ProductReviewOpinionMining\\ManualAspects\\");
		String[] filelist1 = manualaspects.list();
		for (String filename : filelist1) {
			System.out.println("Product Name = "+filename);
			aspectsupport.clear();
			BufferedReader br = new BufferedReader(
					new FileReader(
							new File(
									"C:\\JAVA WorkSpace\\eclipse helios  64bit\\ProductReviewOpinionMining\\ManualAspects\\"
											+ filename)));
			String line = null;
			int allsupport = 0;
			int aspectnum = 0;
			int onewordaspect = 0;
			int twowordsaspect = 0;
			int threewordsaspect = 0;
			int morethanthreewordsaspects = 0;
			int relevantquestionnum = 0;
			String productname = filename.subSequence(0, filename.indexOf(".")).toString().replace('_', ' ');
			System.out.println("product name = "+ productname);
			for (String qa : questions.keySet()) {
				if ( questions.get(qa).toLowerCase().contains(productname.toLowerCase()))
						relevantquestionnum ++;
			}
			while ((line = br.readLine()) != null) {
				int num = 0;
				aspectnum++;
				String[] words = line.split(":");
				 if(QA.countspecificchar(words[0], ' ')==1) twowordsaspect++;
				 else if(QA.countspecificchar(words[0], ' ')==2) threewordsaspect++;
				 else if(QA.countspecificchar(words[0], ' ')==3) morethanthreewordsaspects++;
				 else onewordaspect++;
				for (String qa : questions.keySet()) {
					if ( questions.get(qa).toLowerCase().contains(productname.toLowerCase())
							&&questions.get(qa).toLowerCase()
									.contains(words[0]))
						{
						num++;
						if(aspectsupport.containsKey(words[0]))
							aspectsupport.put(words[0], aspectsupport.get(words[0])+1);
						else aspectsupport.put(words[0], 1);
						}

				}//end of for
				allsupport += num;
				System.out.println(words[0] + "=" + num);
			}// end of while
			QA.printresult(aspectsupport);
			System.out.println("onewordaspect"+onewordaspect+"\nTwowordsAspecrts = "+twowordsaspect+"\nThreewordsAspects = "+threewordsaspect+"\nmorethanthreewordsaspects"+morethanthreewordsaspects);
			System.out.println("All Questions about "+filename+" is "+relevantquestionnum+"\nAverage support = " + (double) allsupport
					/ aspectnum);
			
		}//end of for



	}

}
