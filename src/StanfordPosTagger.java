import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class StanfordPosTagger {

	public static MaxentTagger tagger = null;
	private static HashMap<String, Integer> Aspects_POS = new HashMap<String, Integer>();// �������pos��õ���aspect
	private static HashMap<String, Integer> manual_Aspects_Frequence = new HashMap<String, Integer>();

	public StanfordPosTagger() {
		tagger = new MaxentTagger(".\\english-bidirectional-distsim.tagger");
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

	private void WriteFile(File f, String content) throws IOException {
		BufferedWriter br = new BufferedWriter(new FileWriter(f, true));// ׷�ӷ�ʽд��
		br.write(content);
		br.flush();
		br.close();
	}

	public String Postagger(String input)// ���ʷ���ע�������
	{
		String tagged = tagger.tagString(input);
		System.out.println("Tagged = " + tagged);
		return tagged;
	}

	public void get_manual_aspects(String content, String productname)
			throws IOException {
		
		manual_Aspects_Frequence.clear();
		
		Document doc = Jsoup.parse(content);
		Elements aspects_es = doc.getElementsByTag("aspects");

		for (Element e : aspects_es) {
			String[] M_aspects = e.text().split(",");
			// System.out.println("F_aspects=" + Arrays.toString(F_aspects));
			//System.out.println("M_aspects=" + Arrays.toString(M_aspects));
			for (String s : M_aspects) {
				String S_lower = s.trim().toLowerCase();
				if (S_lower.length() >= 1) {
					if (manual_Aspects_Frequence.containsKey(S_lower))
						manual_Aspects_Frequence.put(s.trim(),
								manual_Aspects_Frequence.get(S_lower) + 1);
					else
						manual_Aspects_Frequence.put(S_lower, 1);
				}
			}
		}

		File manual_aspects = new File(".\\ManualAspectsOriginal\\"
				+ productname);// �ֹ���ע�õ���aspects��¼���ļ���
		if (manual_aspects.exists())
			manual_aspects.delete();
		manual_aspects.createNewFile();

		FileWriter manualaspectswriter = new FileWriter(manual_aspects, true);
		BufferedWriter manualaspectsbw = new BufferedWriter(manualaspectswriter);
		for (String aspect : manual_Aspects_Frequence.keySet()) {
			manualaspectsbw.write(aspect + ":"
					+ manual_Aspects_Frequence.get(aspect) + "\r\n");
			manualaspectsbw.flush();
		}
		manualaspectsbw.close();
	}

	public static void main(String[] args) throws Exception {

		StanfordPosTagger postagger = new StanfordPosTagger();
		/*
		 * String input =
		 * "if you are looking for an outstanding camera that can take you from simple to complex"
		 * ; String tagged = postagger.Postagger(input);
		 * System.out.println(tagged); Thread.sleep(10000);
		 */
        File f = new File(".\\XMLReviewsYu");
        String[] list = f.list();
        //for(int i=0;i<list.length;++i)
        for(int i=1;i<=1;++i)
        {
        	if(!(new File(".\\XMLReviewsYu\\"+list[i]).isDirectory()))
        	{
        		Aspects_POS.clear();
        		String productname = "Nokia_N95.txt";//list[i];// ����productname
        		System.out.println("Current file = "+list[i]);
        		Thread.sleep(5000);
        		String content = postagger.ReadFile(".\\XMLReviewsYu\\ProAndCon\\" + productname);

        		//postagger.get_manual_aspects(content, productname);//�ֹ���ע��aspect

        		Document doc = Jsoup.parse(content);

        		Elements es = doc.getElementsByTag("sentence");

        		File transactionfile = new File(".\\TransactionFiles\\" + productname);// �����ļ����Ŀ¼
        	/*	if (transactionfile.exists())
        			transactionfile.delete();
        		transactionfile.createNewFile();*/

        		File tagresult = new File(".\\TaggedResults\\" + productname);// ��ÿ�����ӽ��д��Ա�ע�Ľ��
        		/*if (tagresult.exists())
        			tagresult.delete();
        		tagresult.createNewFile();*/

        		File aspects_POS = new File(".\\AspectsPos\\" + productname);// ���Ա�ע�õ���aspect��¼���ļ���
        		/*if (aspects_POS.exists())
        			aspects_POS.delete();
        		aspects_POS.createNewFile();*/

        		FileWriter fw = new FileWriter(tagresult, true);
        		BufferedWriter bw = new BufferedWriter(fw);

        		for (Element e : es) {// �������еľ���

        			Document doc2 = Jsoup.parse(e.toString());
        			Elements es2 = doc2.getElementsByTag("sentcont");

        			for (Element ee : es2) {
        				// ʹ��stanfordpostagger
        				if(ee.text().trim().length() == 0)
        					continue;
        				String posresult = postagger.Postagger(ee.text());// added 20130718

        				bw.write(posresult + "\r\n");// ���Ա�ע���д���ļ�

        				String candidate_aspects = new String();
        				String[] taggedwords = posresult.split(" ");
        				String aspects = new String();
        				for (String taggedword : taggedwords) {
        					String[] word_tag = taggedword.split("_");
        					if (word_tag[1].equals("NN") || word_tag[1].equals("NNP")
        							|| word_tag[1].equals("NNS")
        							|| word_tag[1].equals("NP")
        							|| word_tag[1].equals("NNPS")) {
        						aspects += word_tag[0] + " ";
        					} else {
        						if (aspects.length() >= 1) {
        							candidate_aspects += aspects.trim() + "*";
        							aspects = new String();
        						}
        					}
        				}
        				if (aspects.length() > 0)
        					candidate_aspects += aspects.trim();
        				String[] Tag_aspects = candidate_aspects.split("\\*");// *��������ʽ������������
        				
        				//for (String s : Tag_aspects)
        					//System.out.println("Tag_aspects=" + s);
        				
        				for (String aspect : Tag_aspects)// added dujintao 20130719
        				{
        					if (aspect.trim().length() >= 1)
        						if (Aspects_POS.containsKey(aspect.trim().toLowerCase()))
        							Aspects_POS.put(aspect.trim().toLowerCase(),
        									Aspects_POS.get(aspect.trim().toLowerCase()) + 1);
        						else
        							Aspects_POS.put(aspect.trim().toLowerCase(), 1);
        				}

        				if (candidate_aspects.trim().length() >= 1) {// ���õ������ʶ���д�뵽transaction�ļ�����apriori�㷨������
        					//postagger.WriteFile(transactionfile, candidate_aspects.replace("*", " ").trim().toLowerCase()+ "\r\n");
        				}
        			}

        			//Elements es3 = doc2.getElementsByTag("aspects");
        			//String[] Manual_aspects = es3.text().split(",");

        			//System.out.println("Manual_aspects="+ Arrays.toString(Manual_aspects));
        		}//end of for elements es
        		/*for (String aspect : Aspects_POS.keySet()) {
        			postagger.WriteFile(aspects_POS,
        					aspect + "<---->" + Aspects_POS.get(aspect) + "\r\n");// ��aspects_POSд���ļ����ʷ�����������һ��
        		}*/
        		bw.close();
	
        	}//end of if(isDirectory())
        }
        

	}//end of main

}
