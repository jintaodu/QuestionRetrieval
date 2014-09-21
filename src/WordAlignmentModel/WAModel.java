package WordAlignmentModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

public class WAModel {

	private HashMap<String, Integer> wordnumpair = new HashMap<String, Integer>();// ��¼���еĵ����Լ����Ｐ���±��

	HashSet<String> opinionlexicon = new HashSet<String>();// opinion�ֵ�
	HashSet<String> stopwords = new HashSet<String>();// stopword
	HashSet<String> opinioncandidates = new HashSet<String>();// opinion�ĺ�ѡ��
	HashSet<String> aspectcandidates = new HashSet<String>();// aspect�ĺ�ѡ��
	HashMap<String, Integer> opinioncandidatesubscript = new HashMap<String, Integer>();// opinioncandidates������ֵ��ʾ
	HashMap<String, Integer> aspectcandidatesubscript = new HashMap<String, Integer>();//  aspectcandidates������ֵ��ʾ
	
	private Vector<String> english = new Vector<String>();// ÿ�仰�е�ÿ������͵��ʶ�ת��Ϊһ��������ʾ����������
	private Vector<String> chinese = new Vector<String>();// ÿ�仰�е�ÿ������͵��ʶ�ת��Ϊһ��������ʾ����������
	private float[][] tef, countef;
	private float[] totalf, s_totale;

	private double Perplexity() {
		double PP = 1.0;

		for (int i = 0; i < english.size(); ++i) {
			String eng = english.get(i);
			String[] words = eng.split(" ");
			double epuxiu = 1.0 / Math.pow(words.length, words.length);
			double pef = 1.0;
			for (String e : words) {
				double p1 = 0.0;
				for (String f : words) {
					p1 += tef[Integer.parseInt(e.substring(0, e.indexOf('_')))][Integer
							.parseInt(f.substring(0, f.indexOf('_')))];
				}
				if (p1 != 0.0)
					pef *= p1;
			}
			// System.out.println("epuxiu = "+epuxiu);
			PP += Math.log10(1.0 / (epuxiu + pef));// ����perplexity
			// System.out.println("PP = "+PP);
		}

		return PP;
	}

	private void EMalgorithm() {
		int allwordsnum = wordnumpair.size();
		System.out.println("allwordsnum = " + allwordsnum);

		tef = new float[allwordsnum][allwordsnum];// ת�Ƹ��ʷ�����f->e
		countef = new float[allwordsnum][allwordsnum];// e��f��cooccurrence����
		totalf = new float[allwordsnum];//
		s_totale = new float[allwordsnum];//

		System.out.println("Uniform = " + 1.0 / (float) allwordsnum);

		for (int i = 0; i < allwordsnum; ++i)
			for (int j = 0; j < allwordsnum; ++j)
				if (i != j) {
					tef[i][j] = (float) (1.0 / (float) allwordsnum);
				} else {
					tef[i][j] = (float) 0.0;
				}

		double PP1 = 10000000.0;// ��ʼһ���ϴ����ֵ

		double PP2 = Perplexity();

		System.out.println("Initial PP = " + PP2);
		while (Math.abs(PP1 - PP2) > 5) {
			PP1 = PP2;

			for (int i = 0; i < allwordsnum; ++i)
				for (int j = 0; j < allwordsnum; ++j)
					countef[i][j] = (float) 0.0;// ÿ�ֵ�������Ҫ��ʼ��
			for (int i = 0; i < wordnumpair.size(); ++i)
				totalf[i] = (float) 0.0;
			for (int i = 0; i < english.size(); ++i)// for all sentence
													// pairs(e,f)
			{
				String eng = english.get(i);
				String fore = chinese.get(i);

				String[] wordsine = eng.split(" ");
				String[] wordsinf = fore.split(" ");

				// ��һ��forѭ��Ƕ��
				for (String wordine : wordsine) {
					String e = wordine.substring(0, wordine.indexOf('_'));
					s_totale[Integer.parseInt(e)] = (float) 0.0;
					for (String wordinf : wordsinf) {
						String f = wordinf.substring(0, wordinf.indexOf('_'));
						s_totale[Integer.parseInt(e)] += tef[Integer
								.parseInt(e)][Integer.parseInt(f)];
					}
				}// end of for for

				// �ڶ���forѭ��Ƕ��

				for (String wordine : wordsine) {
					String e = wordine.substring(0, wordine.indexOf('_'));

					for (String wordinf : wordsinf) {
						if (s_totale[Integer.parseInt(e)] != 0.0) {
							String f = wordinf.substring(0,
									wordinf.indexOf('_'));

							countef[Integer.parseInt(e)][Integer.parseInt(f)] += tef[Integer
									.parseInt(e)][Integer.parseInt(f)]
									/ s_totale[Integer.parseInt(e)];

							totalf[Integer.parseInt(f)] += tef[Integer
									.parseInt(e)][Integer.parseInt(f)]
									/ s_totale[Integer.parseInt(e)];
						}

					}
				}// end of for for
			}// end of all sentence pairs (e,f)
				// estimate probilities
			for (int i = 0; i < allwordsnum; ++i)
				for (int j = 0; j < allwordsnum; ++j) {
					if (totalf[j] != 0.0)
						tef[i][j] = countef[i][j] / totalf[j];
				}

			PP2 = Perplexity();
			System.out.println("PP2 = " + PP2);
		}// end of while ����ʱ����

		for (int i = 0; i < allwordsnum; ++i) {
			for (int j = 0; j < allwordsnum; ++j)
				System.out.print(tef[i][j] + " ");// ��ӡ���
			System.out.println();// ������з�
		}

	}// end of EM algorithm

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

	private void WriteFile(File f, HashMap<String, Integer> corpuscount)
			throws IOException {
		BufferedWriter br = new BufferedWriter(new FileWriter(f, true));// ׷�ӷ�ʽд��
		for (String key : corpuscount.keySet()) {
			br.write(key + ":" + corpuscount.get(key) + "\r\n");
			br.flush();
		}
		br.close();
	}

	private void ReadNecessaryInfo(String fpath1,String fpath2) throws IOException, InterruptedException {
		File f = new File(fpath1);
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = null;
		while ((line = br.readLine()) != null) {
			opinionlexicon.add(line);
		}
		System.out.println("��дʴʵ��СΪ = "+opinionlexicon.size());
		
		f = new File(fpath2);
		br = new BufferedReader(new FileReader(f));
		while ((line = br.readLine()) != null) {
			stopwords.add(line);
		}
		System.out.println("ֹͣ�ʴʵ��СΪ = "+stopwords.size());
		Thread.sleep(3000);
		br.close();
	}
	
	public void SaveResult(HashMap<String, Integer> result, BufferedWriter bw) throws IOException {// ��hashmap�е�Ԫ��������ӡ���
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
				//bw.write(e.getKey()+"  "+e.getValue()+"\r\n");
				bw.write(e.getKey()+"\r\n");
				bw.flush();
		}

	}
	
	private boolean isadjective(String word) {
		if (word.contains("_jj") || word.contains("_jjr")
				|| word.contains("_jjs"))
			return true;
		return false;
	}

	private boolean isnounphrase(String word) {
		if (word.contains("_nn") || word.contains("_nnp")
				|| word.contains("_nnps") || word.contains("_nns"))
			return true;
		return false;
	}

	public void generateparallelcorpus_forMyself(String productname)//��ȡ�����Լ����ʶ���ʱ��������
			throws IOException {

		HashSet<String> candidates = new HashSet<String>();// ��дʺ�aspect�ĺ�ѡ��

		File f = new File(".\\TaggedResults\\" + productname);

		BufferedReader br = new BufferedReader(new FileReader(f));
		File eng = new File(".\\Stage1\\english.txt");
		File chi = new File(".\\Stage1\\chinese.txt");
		if (eng.exists())
			eng.delete();
		eng.createNewFile();
		if (chi.exists())
			chi.delete();
		chi.createNewFile();
		BufferedWriter bweng = new BufferedWriter(new FileWriter(eng));
		BufferedWriter bwchi = new BufferedWriter(new FileWriter(chi));

		String line = null;
		while ((line = br.readLine()) != null) {
			String[] words = line.trim().toLowerCase().split(" ");
			String nouns = "", adjs = "";
			for (String word : words) {// �������е����ʺ����ʶ���
				if (word.trim().length() != 0) {
					if (isnounphrase(word))
						nouns += word.substring(0, word.indexOf('_')) + " ";
					else {
						if (nouns.length() > 0)
							candidates.add(nouns.trim());
						nouns = "";
					}
				}// end of if
			}
			if (nouns.trim().length() > 0)// �������һ����Ϊ���ʵ����
				candidates.add(nouns.trim());

			for (String word : words) {// �������е����ݴʼ����ݴʶ���
				if (word.trim().length() != 0) {
					if (isadjective(word))
						adjs += word.substring(0, word.indexOf('_')) + " ";
					else {
						if (adjs.length() > 0)
							candidates.add(adjs.trim());
						adjs = "";
					}
				}// end of if
			}
			if (adjs.trim().length() > 0)// �������һ����Ϊ���ݴʵ����
				candidates.add(adjs.trim());

			/*
			 * if (nounandopinion.trim().length() != 0) {
			 * 
			 * 
			 * bweng.write(nounandopinion.trim().toLowerCase() + "\r\n");
			 * bwchi.write(nounandopinion.trim().toLowerCase() + "\r\n");
			 * bweng.flush(); bwchi.flush(); }
			 */

		}// end of while
		Integer num = 0;
		for (String s : candidates)
			wordnumpair.put(s, num++);// ��ÿһ�����ݴʻ������ʸ�һ���������

		br = new BufferedReader(new FileReader(f));// ��ͷ��ʼ���ļ�
		while ((line = br.readLine()) != null) {
			String sentence = "";
			String[] words = line.toLowerCase().split(" ");
			String phrases = "";

			String flag = "INI";
			// ����Ĵ��ԣ�ֻ����Ϊ���ݴʺ������Լ����ʶ���ֱ��Ϊadj��noun������ͬһ�����ڲ�ͬ���ﾳ�п��ܴ��Բ�ͬ�����ֺ���Ҫ���ϴ���
			for (int i = 0; i < words.length; i++) {
				if (isnounphrase(words[i])
						&& (flag.equals("noun") || flag.equals("INI"))) {
					flag = "noun";
					phrases += words[i].substring(0, words[i].indexOf('_'))
							+ " ";
				} else if (isadjective(words[i])
						&& (flag.equals("adj") || flag.equals("INI"))) {
					flag = "adj";
					phrases += words[i].substring(0, words[i].indexOf('_'))
							+ " ";
				} else if (flag.equals("adj") && isnounphrase(words[i])) {// ��һ����Ϊ���ݴʣ���ǰ��Ϊ����
					if (phrases.length() > 0) {
						sentence += wordnumpair.get(phrases.trim()) + "_"
								+ flag + " ";
						phrases = "";

					}
					phrases += words[i].substring(0, words[i].indexOf('_'))// ����ǰ�μ���
							+ " ";
					flag = "noun";

				} else if (flag.equals("noun") && isadjective(words[i])) {// ��һ����Ϊ���ʣ���ǰ��Ϊ���ݴ�
					if (phrases.length() > 0) {
						sentence += wordnumpair.get(phrases.trim()) + "_"
								+ flag + " ";
						phrases = "";
					}
					phrases += words[i].substring(0, words[i].indexOf('_'))// ����ǰ�ʼ���
							+ " ";
					flag = "adj";

				} else {
					if (phrases.length() > 0) {
						sentence += wordnumpair.get(phrases.trim()) + "_"
								+ flag + " ";
						phrases = "";
					}
					continue;
				}
			}
			if (phrases.trim().length() > 0)
				sentence += wordnumpair.get(phrases.trim()) + "_" + flag;

			if (sentence.length() > 0) {
				chinese.add(sentence.trim());
				english.add(sentence.trim());
			}
		}
		for (String s : english)
			System.out.println(s);
		for (String s : wordnumpair.keySet())
			System.out.println(s + " " + wordnumpair.get(s));
		// System.out.println(wordnumpair);
		// System.out.println(candidates);
		bweng.close();
		bwchi.close();

		EMalgorithm();// ѵ��IBMModel1
	}

	public void generateparallelcorpus_phraseforGIZA(String productname)//��ȡ����ʱҲ��ȡ���ʶ���
			throws IOException {
        
		File f = new File(".\\TaggedResults\\" + productname);

		BufferedReader br = new BufferedReader(new FileReader(f));

		String line = null;
		while ((line = br.readLine()) != null) {
			String[] words = line.trim().toLowerCase().split(" ");
			String nouns = "", adjs = "";
			for (String word : words) {// �������е����ʺ����ʶ���
				if (word.trim().length() != 0) {
					if (isnounphrase(word))
						nouns += word.substring(0, word.indexOf('_')) + " ";
					else {
						if (nouns.length() > 0)
							aspectcandidates.add(nouns.trim());
						nouns = "";
					}
				}// end of if
			}
			if (nouns.trim().length() > 0)// �������һ����Ϊ���ʵ����
				aspectcandidates.add(nouns.trim());

			for (String word : words) {// �������е����ݴ�
				if (word.trim().length() != 0) {
					if (isadjective(word))
					{
        				adjs += word.substring(0, word.indexOf('_')) + " ";
        				opinioncandidates.add(adjs.trim());
        				adjs = "";
					}
				}// end of if
			}
		}// end of while
		
		
		Integer num = 0;
		for (String s : opinioncandidates)
			opinioncandidatesubscript.put(s, num++);// ��ÿһ�����ݴʸ�һ���������
		for (String s : aspectcandidates)
			aspectcandidatesubscript.put(s, num++);// ��ÿһ�������Լ����ʶ��︳һ���������

		System.out.println("���ݴʡ����ʡ����ʶ����ȡ��ϡ�����");
		br = new BufferedReader(new FileReader(f));// ���´�ͷ��ʼ���ļ�

		while ((line = br.readLine()) != null) {
			String[] words = line.trim().toLowerCase().split(" ");
			String nouns = "", adjs = "";
			String nounwords = "";
			for (String word : words) {// �������е����ʺ����ʶ���
				if (word.trim().length() != 0) {
					if (isnounphrase(word))
						nouns += word.substring(0, word.indexOf('_')) + " ";
					else {
						if (nouns.length() > 0)
							nounwords += aspectcandidatesubscript.get(nouns.trim()).toString()+" ";
						nounwords += "ID ";
						nouns = "";
					}
				}// end of if
			}
			if (nouns.trim().length() > 0)// �������һ����Ϊ���ʵ����
				nounwords += aspectcandidatesubscript.get(nouns.trim()).toString();
			if (nounwords.trim().length() > 0)
				english.add(nounwords);
			else
				english.add("NONEWORDS");// ��ʾ����û�����ݴ�

			String adjwords = "";
			for (String word : words) {// �������е����ݴʼ����ݴʶ���
				if (word.trim().length() != 0) {
					if (isadjective(word))
					{
						adjs += word.substring(0, word.indexOf('_')) + " ";
						adjwords += opinioncandidatesubscript.get(adjs.trim())+" ";//������ת��������
						adjs = "";
					}else
					{
						adjwords += "ID ";
					}
				}// end of if
			}

			if (adjwords.trim().length() > 0)
				chinese.add(adjwords);
			else
				chinese.add("NONEWORDS");// ��ʾ����û������

		}// end of while

		System.out.println("noun sentence size = " + english.size()
				+ "\nadj sentence size = " + chinese.size());

		File eng = new File(".\\Stage1\\english.segment.txt");
		File chi = new File(".\\Stage1\\chinese.segment.txt");
		if (eng.exists())
			eng.delete();
		eng.createNewFile();
		if (chi.exists())
			chi.delete();
		chi.createNewFile();
		BufferedWriter bweng = new BufferedWriter(new FileWriter(eng,true));
		BufferedWriter bwchi = new BufferedWriter(new FileWriter(chi,true));
		for (int i = 0; i < english.size(); ++i) {
			if (!english.get(i).equals("NONEWORDS") && !chinese.get(i).equals("NONEWORDS")) {

				bweng.write(english.get(i).trim() + "\r\n");
				bwchi.write(chinese.get(i).trim() + "\r\n");
				bweng.flush();
				bwchi.flush();
			}
		}

		for (String s : english)
			System.out.println(s);

		bweng.close();
		bwchi.close();
				
	}

	public void generateparallelcorpus_nophraseforGIZA(String productname)//��ȡ�����Լ����ʶ���ʱ������������ǵ����Ĵ�
	throws IOException {

        File f = new File(".\\TaggedResults\\" + productname);

        BufferedReader br = new BufferedReader(new FileReader(f));
        File eng = new File(".\\Stage1\\english.segment.txt");
        File chi = new File(".\\Stage1\\chinese.segment.txt");
        if (eng.exists())
        	eng.delete();
        eng.createNewFile();
        if (chi.exists())
        	chi.delete();
        chi.createNewFile();
        BufferedWriter bweng = new BufferedWriter(new FileWriter(eng,true));
        BufferedWriter bwchi = new BufferedWriter(new FileWriter(chi,true));

        String line = null;
        while ((line = br.readLine()) != null) {
        	String[] words = line.trim().toLowerCase().split(" ");
        	String nouns = "", adjs = "";
        	for (String word : words) {// �������е����ʺ����ʶ���
        		if (word.trim().length() != 0) {
        			if (isnounphrase(word)&&!stopwords.contains(word.substring(0,word.indexOf('_'))))
        			{
        				nouns += word.substring(0, word.indexOf('_')) + " ";
        				aspectcandidates.add(nouns.trim());
        				nouns = "";	
        			}
 
        		}// end of if
        	}

        	for (String word : words) {// �������е����ݴʼ����ݴʶ���
        		if (word.trim().length() != 0) {
        			//if (isadjective(word)&&opinionlexicon.contains(word.substring(0, word.indexOf('_'))))//������дʵ��ж�Ϊ��дʵĲſ���
        			if (isadjective(word))//������дʵ��ж�Ϊ��дʵĲſ���
        			{
        				adjs += word.substring(0, word.indexOf('_')) + " ";
        				opinioncandidates.add(adjs.trim());
        				adjs = "";
        			}
        		}// end of if
        	}

        }// end of while
        Integer num = 0;
        for (String s : opinioncandidates)
        	opinioncandidatesubscript.put(s, num++);// ��ÿһ�����ݴʸ�һ���������
        for (String s : aspectcandidates)
        	aspectcandidatesubscript.put(s, num++);// ��ÿһ�������Լ����ʶ��︳һ���������

        br = new BufferedReader(new FileReader(f));// ���´�ͷ��ʼ���ļ�

        while ((line = br.readLine()) != null) {
        	String[] words = line.trim().toLowerCase().split(" ");
        	String nouns = "", adjs = "";
        	String nounwords = "";
        	for (String word : words) {// �������е����ʺ����ʶ���
        		if (word.trim().length() != 0) {
        			if (isnounphrase(word)&&!stopwords.contains(word.substring(0,word.indexOf('_'))))
        			{
        				nouns += word.substring(0, word.indexOf('_')) + " ";
        				nounwords += aspectcandidatesubscript.get(nouns.trim()).toString()+" ";
        				nouns = "";
        			} else {
        				nounwords += "ID ";
        			}
        		}// end of if
        	}//end of for

        if (nounwords.trim().length() > 0)
        	english.add(nounwords);
        else
        	english.add("NONEWORDS");// ��ʾ����û������

        String adjwords = "";
        for (String word : words) {// �������е����ݴʼ����ݴʶ���
        	if (word.trim().length() != 0) {
        		//if (isadjective(word)&&opinionlexicon.contains(word.substring(0, word.indexOf('_'))))
        		if (isadjective(word))
					{
        			adjs += word.substring(0, word.indexOf('_')) + " ";
        			adjwords += opinioncandidatesubscript.get(adjs.trim())+" ";//������ת��������
        			adjs = "";
					}else{
						adjwords += "ID ";
					}
        	}// end of if
        }//end of for

        if (adjwords.trim().length() > 0)
        	chinese.add(adjwords);
        else
        	chinese.add("NONEWORDS");// ��ʾ����û�����ݴ�

        }// end of while

        System.out.println("noun sentence size = " + english.size()
        		+ "\nadj sentence size = " + chinese.size());

        for (int i = 0; i < english.size(); ++i) {
        	
        	if (!english.get(i).equals("NONEWORDS") && !chinese.get(i).equals("NONEWORDS")) {

        		bweng.write(english.get(i).trim() + "\r\n");
        		bwchi.write(chinese.get(i).trim() + "\r\n");
        		bweng.flush();
        		bwchi.flush();
        	}
        }

        for (String s : chinese)
        	System.out.println(s);

        bweng.close();
        bwchi.close();

}

	private String getadjnouns(HashMap<String,Integer> map,String num)
	{//����num��map�в���string
		for(String key:map.keySet())
			if(map.get(key).equals(Integer.parseInt(num)))
				return key;
		System.out.println("FINDNOWORDS = " + num);
		return "****NOWORDS****";
		
	}
    public void generateAssociation() throws IOException {
		
		HashMap<Pair,Double> e2cAssociation = new HashMap<Pair,Double>();//association����pair����Ϊ�����ʵ����ݴ�
		HashMap<Pair,Double> c2eAssociation = new HashMap<Pair,Double>();//association����pair����Ϊ�����ݴʵ�����
		
		File TrViResult = new File(".\\Stage1\\TranslationVisualResult.txt");//������ת���ɿ��ӵĵ��ʺͶ�����鷭��Ч��
		if(TrViResult.exists()) TrViResult.delete();TrViResult.createNewFile();
		BufferedWriter bwTrViResult = new BufferedWriter(new FileWriter(TrViResult,true));

		BufferedWriter bwTtablee2c = new BufferedWriter(new FileWriter(".\\Ttable\\e2c.actual.t1.final"));
		BufferedWriter bwTtablec2e = new BufferedWriter(new FileWriter(".\\Ttable\\c2e.actual.t1.final"));
		
		File c2e= new File(".\\Stage1\\c2e.actual.t1.final");
		File e2c = new File(".\\Stage1\\e2c.actual.t1.final");

		BufferedReader bwe2c = new BufferedReader(new FileReader(e2c));
		BufferedReader bwc2e = new BufferedReader(new FileReader(c2e));

		String line = null;
		bwTrViResult.write("���ʵ����ݴʷ�������\r\n");bwTrViResult.flush();
		while ((line = bwe2c.readLine()) != null) {
			String[] words = line.split(" ");
			if (!words[0].equals("NULL")&&!words[0].equals("ID")&&!words[1].equals("NULL")&&!words[1].equals("ID"))
			{
				e2cAssociation.put(new Pair(words[0],words[1]), Double.parseDouble(words[2]));//���ʵ����ݴʵ�ת�Ƹ���
				bwTrViResult.write(getadjnouns(aspectcandidatesubscript,words[0])+"::::"+getadjnouns(opinioncandidatesubscript,words[1])+"::::"+words[2]+"\r\n");
				bwTrViResult.flush();
				bwTtablee2c.write(getadjnouns(aspectcandidatesubscript,words[0])+"::::"+getadjnouns(opinioncandidatesubscript,words[1])+"::::"+words[2]+"\r\n");
				bwTtablee2c.flush();
			}
		}// end of while
		
		bwTrViResult.write("���ݴʵ����ʷ�������\r\n");bwTrViResult.flush();
		while ((line = bwc2e.readLine()) != null) {
			String[] words = line.split(" ");
			if (!words[0].equals("NULL")&&!words[0].equals("ID")&&!words[1].equals("NULL")&&!words[1].equals("ID"))
			{
				c2eAssociation.put(new Pair(words[0],words[1]), Double.parseDouble(words[2]));//���ݴʵ����ʵ�ת�Ƹ���
				bwTrViResult.write(getadjnouns(opinioncandidatesubscript,words[0])+"::::"+getadjnouns(aspectcandidatesubscript,words[1])+"::::"+words[2]+"\r\n");
				bwTrViResult.flush();
				bwTtablec2e.write(getadjnouns(opinioncandidatesubscript,words[0])+"::::"+getadjnouns(aspectcandidatesubscript,words[1])+"::::"+words[2]+"\r\n");
				bwTtablec2e.flush();
			}
					
		}// end of while

		bwTrViResult.close();
		
		File VisualAlignFrequency = new File(".\\Stage1\\VisualAlignFrequency.txt");
		if(VisualAlignFrequency.exists()) VisualAlignFrequency.delete();
		VisualAlignFrequency.createNewFile();
		BufferedWriter VAFbw = new BufferedWriter(new FileWriter(VisualAlignFrequency,true));
		WAMA3Table A3T = new WAMA3Table();
		A3T.processA3Table();
		HashMap<Pair,Integer> alignfrequency = A3T.getMap();
		for(Pair p:alignfrequency.keySet())
		{
			if(!p.getx().equals("ID")&&!p.getx().equals("NULL")&&!p.gety().equals("ID")&&!p.gety().equals("NULL"))
			VAFbw.write(getadjnouns(aspectcandidatesubscript,p.getx())+"::::"+getadjnouns(opinioncandidatesubscript,p.gety())+"::::"+alignfrequency.get(p)+"\r\n");
			VAFbw.flush();
		}
		VAFbw.close();
		
		File matrix = new File(".\\Stage1\\matrix.txt");
		if(matrix.exists()) matrix.delete();matrix.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(matrix,true));
		
		boolean[] rowzero = new boolean[aspectcandidates.size()];//�����Щ��ȫ��Ϊ0
		boolean[] colzero = new boolean[opinioncandidates.size()];//�����Щ��ȫ��Ϊ0
		for(int i=0;i<rowzero.length;++i)
			rowzero[i] = false;
		for(int i=0;i<colzero.length;++i)
			colzero[i] =false;
		double t = 0.5;
		Integer aspectsubstart = opinioncandidatesubscript.size();
		for(Integer i=0; i < aspectcandidatesubscript.size(); ++i)
		{
			for(Integer j=0; j < opinioncandidatesubscript.size(); ++j)
			{
				Integer aspectsub = i+aspectsubstart;
				if(e2cAssociation.containsKey(new Pair(aspectsub.toString(),j.toString()))||c2eAssociation.containsKey(new Pair(j.toString(),aspectsub.toString())))
				{
				    rowzero[i] = true;
				    colzero[j] = true;
				}
			}
		}
		
		System.out.println("ɾ������Ϊ������ʺ����ݴ�֮ǰ��\naspectcandidates size = "+aspectcandidates.size()+"\nopinioncandidates size = "+opinioncandidates.size());
		for(Integer i=0; i < aspectcandidatesubscript.size(); ++i)
		{
			Integer subscript = i+aspectsubstart;
			if(rowzero[i] == false) aspectcandidates.remove(getadjnouns(aspectcandidatesubscript,subscript.toString()));
			
		}
		for(Integer j=0; j < opinioncandidatesubscript.size(); ++j)
		{
			if(colzero[j] == false) opinioncandidates.remove(getadjnouns(opinioncandidatesubscript,j.toString()));	
		}
		
		System.out.println("ɾ������Ϊ������ʺ����ݴ�֮��\naspectcandidates size = "+aspectcandidates.size()+"\nopinioncandidates size = "+opinioncandidates.size());
		
        for(Integer i=0; i < aspectcandidatesubscript.size(); ++i)
		{
			for(Integer j=0; j < opinioncandidatesubscript.size(); ++j)
			{
				if(rowzero[i] == true && colzero[j] == true)//��֤i��j�в���ȫ���
				{
					Integer aspectsub = i+aspectsubstart;
					if(e2cAssociation.containsKey(new Pair(aspectsub.toString(),j.toString()))&&c2eAssociation.containsKey(new Pair(j.toString(),aspectsub.toString())))
					{
						bw.write(1.0/(t/e2cAssociation.get(new Pair(aspectsub.toString(),j.toString()))+(1-t)/c2eAssociation.get(new Pair(j.toString(),aspectsub.toString())))+" ");

					}else if(e2cAssociation.containsKey(new Pair(aspectsub.toString(),j.toString())))
							{
						    bw.write(e2cAssociation.get(new Pair(aspectsub.toString(),j.toString()))*0.5+" ");

							}else if(c2eAssociation.containsKey(new Pair(j.toString(),aspectsub.toString())))
									{
								       bw.write(c2eAssociation.get(new Pair(j.toString(),aspectsub.toString()))*0.5+" ");

									}else bw.write(0.0+" ");
					bw.flush();
				}
			}
			bw.write("\r\n");
			bw.flush();
		}
        
        
        Integer num = 0;
        Integer subscript;
        for (Integer i=0;i<rowzero.length;++i)
           if(rowzero[i] == false)
           {
        	 subscript = i+aspectsubstart;
        	 aspectcandidatesubscript.remove(getadjnouns(aspectcandidatesubscript,subscript.toString()));// ɾ����Ϊ������ʼ�����
           }
        	
        for (Integer i=0;i<colzero.length;++i)
            if(colzero[i] == false)
            {
            	opinioncandidatesubscript.remove(getadjnouns(opinioncandidatesubscript,i.toString()));// ɾ����Ϊ������ݴʼ�����
            }
        
        
        //����ѡ���ʺ����ݴ�д���ļ�
        File alladj = new File(".\\Stage1\\adjectives.txt");
        if(alladj.exists()) alladj.delete();alladj.createNewFile();
        BufferedWriter bwnounandadj = new BufferedWriter(new FileWriter(alladj));

        SaveResult(opinioncandidatesubscript,bwnounandadj);

        File allnoun = new File(".\\Stage1\\nouns.txt");
        if(alladj.exists()) alladj.delete();alladj.createNewFile();

        bwnounandadj = new BufferedWriter(new FileWriter(allnoun));
        SaveResult(aspectcandidatesubscript,bwnounandadj);

        bwnounandadj.close();
		

	}

	public void finalselectedaspects(String productname) throws IOException {
		File f = new File(".\\FinalSelectedAspects\\" + productname);
		if (f.exists())
			f.delete();
		f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));

		File spca = new File(".\\Stage1\\SPCA.txt");
		File nouns = new File(".\\Stage1\\nouns.txt");

		BufferedReader brspca = new BufferedReader(new FileReader(spca));
		BufferedReader brnouns = new BufferedReader(new FileReader(nouns));
		String line1 = null;
		String line2 = null;
		while ((line1 = brspca.readLine()) != null
				&& (line2 = brnouns.readLine()) != null) {
			if (Math.abs(Double.parseDouble(line1)) > 1.0e-2) {//Canon G3  0.0042
				bw.write(line2 + ":1\r\n");
				bw.flush();
			}else System.out.println("δ��ѡ�е�aspect candidate spca = "+line1+" noun = "+line2);
		}
		bw.close();
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
		// System.out.println(Float.MAX_VALUE+"  "+Double.MAX_VALUE);
		// Thread.sleep(20300);
		String productname = "Nokia_5800_XpressMusic.txt";
		WAModel ibm = new WAModel();
		
		ibm.ReadNecessaryInfo(".\\OpinionLexicon.txt",".\\Stopwords");//optical ������дʵ������ݴ�
		//ibm.generateparallelcorpus_nophraseforGIZA(productname);
		ibm.generateparallelcorpus_phraseforGIZA(productname);
		ibm.generateAssociation();
		//ibm.finalselectedaspects(productname);
	}

}
