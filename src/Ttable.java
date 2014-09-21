import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;


public class Ttable {
	private static HashSet<String> sw = new HashSet<String>();
    private static String fileContent = new String();
	/**
	 * @param args
	 * @throws IOException 
	 */
	public void WriteQuesion_Description(String fname,String content) throws IOException
	{
		File f = new File(fname);
		FileWriter fw = new FileWriter(f,true);
		fw.write(content);
		fw.flush();
		
	}
	public String stringfilter(String content)//¹ýÂË
	{
		String words[] = content.split("[^^a-zA-Z|']");
		String filtered = new String();
		int count = 1;
		for (String w : words) {
			String w_lower = w.toLowerCase();
			if ((w.length() >= 3) && !sw.contains(w_lower)) {
				filtered += w_lower;
				if(count != words.length)filtered += " ";
			}
			count++;
		}
		filtered += "\r\n";
		return filtered;
	}
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
       CollectionWordTF cwtf = new CollectionWordTF();
       Ttable ttable = new Ttable();
       cwtf.Readsw("Stopwords");
       sw = cwtf.getStopWord();
       String collectionroot  = "C:\\Users\\mmdb\\Desktop\\collection";
       String file[] = cwtf.ReadDirectory(collectionroot);
       for(String filename:file)
       {
    	   System.out.println("Current file is "+filename);
           cwtf.ReadFile(collectionroot+"\\"+filename);
           fileContent = cwtf.getfileContent();
           Document doc = Jsoup.parse(fileContent);
           Elements es = doc.getElementsByTag("question");
           for(org.jsoup.nodes.Element e:es)
           {
        	   Document doc2 = Jsoup.parse(e.toString());
        	   //System.out.println(doc2.getElementsByTag("subject").text().toString());
        	   String subject = ttable.stringfilter(doc2.getElementsByTag("subject").text().toString());
        	   String content = ttable.stringfilter(doc2.getElementsByTag("content").text().toString());
        	   if(subject.length()>=3&&content.length()>=3)
        	   {
            	   ttable.WriteQuesion_Description("chinese.txt",subject);
            	   ttable.WriteQuesion_Description("english.txt",content);
        	   }//end of if

           }//end of for
       }//end of for
	}

}
