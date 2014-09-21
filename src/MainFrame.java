import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wb.swt.SWTResourceManager;


public class MainFrame {

	public static CollectionWordTF tf = null;
	protected Shell shell;
	private Text text;
	private Table table;
	private Text text_2;
	private final FormToolkit formToolkit = new FormToolkit(Display
			.getDefault());
    public static QuerySimilarity QS = null;
    public static ArrayList<Entry<String, Double>> TopResults = new ArrayList<Entry<String, Double>>();//
    private Text text_3;
    private static String directoryname = new String();//¼ÇÂ¼cQAµÄÄ¿Â¼µØÖ·

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainFrame window = new MainFrame();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	private String[] ReadFileList(String filepath) throws IOException {
		File file = new File(filepath);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String filename = null;
		String[]  list = new String[500];
		int i = 0;
		while((filename=br.readLine())!=null)
		{
			list[i++] = filename;
		}
		
		return list;
	}
	public void open() throws IOException {
		Display display = Display.getDefault();
		createContents();
		shell.setSize(860, 810);
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() throws IOException {
		shell = new Shell(SWT.CLOSE|SWT.MIN);
		shell.setMinimumSize(new Point(300, 38));
		shell.setImage(SWTResourceManager.getImage("fudan.jpg"));
		shell.setSize(907, 650);
		shell.setText("cQA Question Search!");
		GridData gd_cmdIntro = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1);
		gd_cmdIntro.widthHint = 10;

		Label lblWelcomToCqas = new Label(shell, SWT.NONE);
		lblWelcomToCqas.setFont(SWTResourceManager.getFont("Î¢ÈíÑÅºÚ", 16, SWT.NORMAL));
		lblWelcomToCqas.setForeground(SWTResourceManager
				.getColor(SWT.COLOR_DARK_GREEN));
		lblWelcomToCqas.setFont(SWTResourceManager.getFont("Î¢ÈíÑÅºÚ", 23,
				SWT.NORMAL));
		lblWelcomToCqas.setBounds(74, 27, 536, 47);
		lblWelcomToCqas.setText("Welcome to cQA Question Search !");

		Composite composite = new Composite(shell, SWT.NONE|SWT.INHERIT_DEFAULT);
		composite.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		composite.setBounds(20, 151, 817, 621);

		Label lblQuery = new Label(composite, SWT.NONE);
		lblQuery.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		lblQuery.setBounds(10, 90, 61, 23);
		lblQuery.setFont(SWTResourceManager.getFont("Î¢ÈíÑÅºÚ", 12, SWT.NORMAL));
		lblQuery.setText("Query\uFF1A");

		text = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		text.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		text.setBounds(86, 63, 508, 50);

		Label lblResults = new Label(composite, SWT.NONE);
		lblResults.setForeground(SWTResourceManager.getColor(0, 0, 0));
		lblResults.setBounds(96, 119, 142, 27);
		lblResults.setFont(SWTResourceManager.getFont("Î¢ÈíÑÅºÚ", 13, SWT.NORMAL));
		lblResults.setText("Search Results");

		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.MULTI);
		table.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		table.setBounds(10, 152, 317, 469);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn tc0 = new TableColumn(table, SWT.CENTER);
		tc0.setText("id");
		tc0.setWidth(35);
		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		tc1.setText("Question_id");
		tc1.setWidth(183);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		tc2.setText("Similarity");
		tc2.setWidth(93);
		
		final TableItem tableItem = new TableItem(table, SWT.NONE);
		table.addMouseListener(new MouseListener()
		{
			public void mouseDoubleClick(MouseEvent e){
				int selindex = table.getSelectionIndex();
				TableItem item = table.getItem(selindex);				
				HashMap<String,String> QAContent = QS.getQAContent();
				text_3.setText(QAContent.get(item.getText(1)));
				//MessageDialog.openInformation(null, null, null);
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		/*.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				
			}
		});*/
		//tableItem.setText(new String[] { "1.","Timeeeee", "10.32" });
		
		final TableItem tableItem_1 = new TableItem(table, SWT.NONE);
		//tableItem_1.setText(new String[] { "1.","wqe", "10.32" });
		
		final TableItem tableItem_2 = new TableItem(table, SWT.NONE);
		//tableItem_2.setText(new String[] { "1.","wer", "10.32" });
		
		final TableItem tableItem_3 = new TableItem(table, SWT.NONE);
		//tableItem_3.setText(new String[] { "1.","erte", "10.32" });
		
		final TableItem tableItem_4 = new TableItem(table, SWT.NONE);
		//tableItem_4.setText(new String[] { "1.","ter", "10.32" });
		
		final TableItem tableItem_5 = new TableItem(table, SWT.NONE);
		//tableItem_5.setText(new String[] { "1.","Tter", "10.32" });
		
		final TableItem tableItem_6 = new TableItem(table, SWT.NONE);
		//tableItem_6.setText(new String[] { "1.","Timeeeee", "10.32" });
		
		final TableItem tableItem_7 = new TableItem(table, SWT.NONE);
		//tableItem_7.setText(new String[] { "1.","Timeeeee", "10.32" });
		
		final TableItem tableItem_8 = new TableItem(table, SWT.NONE);
		//tableItem_8.setText("New TableItem");
		
		final TableItem tableItem_9 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_10 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_11 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_12 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_13 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_14 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_15 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_16 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_17 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_18 = new TableItem(table, SWT.NONE);
		final TableItem tableItem_19 = new TableItem(table, SWT.NONE);
		
		
		final Combo combo = new Combo(composite, SWT.NONE);
		combo.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		combo.setBounds(443, 30, 128, 26);
		if(new File(".\\serializable_object\\FileList.txt").exists())
		{
			String [] collection_name = ReadFileList(".\\serializable_object\\FileList.txt");
			for(String w : collection_name)
			{
				if(w != null)
				combo.add(w);
			}
			combo.select(0);
		}
		
		formToolkit.adapt(combo);
		formToolkit.paintBordersFor(combo);
		
		final Combo combo_1 = new Combo(composite, SWT.NONE);
		combo_1.setBounds(178, 30, 142, 26);
		combo_1.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		combo_1.add("Language Model");
		combo_1.add("TrLMWeight Language Model");
		combo_1.add("Okapi BM25 Model");
		combo_1.add("Translation Model");
		combo_1.add("Translation Based LM");
		combo_1.add("TrLMWeight Translation Based LM");
		combo_1.select(0);
		formToolkit.adapt(combo_1);
		formToolkit.paintBordersFor(combo_1);
		
		Button btnSearch = new Button(composite, SWT.NONE);
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(text.getText().length() == 0)
					MessageDialog.openInformation(null, "Error", "Your Query's length is 0 ! Input Again !");
				else
					try {
						String ModelTpye = combo_1.getText();
						String Collection_name = combo.getText();
						
						System.out.println("\n1. Selected ModelTpye = "+ModelTpye+"\n2. Selected Collection name =  " +Collection_name+"\n");
						System.out.println("Instance QuerySimilarity strart... ");
						long start =System.currentTimeMillis();
						QS = new QuerySimilarity(Collection_name);
						System.out.println("Instance QuerySimilarity end... ");
						System.out.println("Instance time = "+(double)(System.currentTimeMillis()-start)/1000+" s.");
						text_3.setText("");

						String[] query = text.getText().split("\n");
						for(int i=0;i<query.length;++i)
						{
							System.out.println("Start query "+(i+1));
							TopResults = QS.startSearch(query[i],ModelTpye);
							System.out.println("End query "+(i+1));
						}
						
						boolean flag = true;
						for(int i =0;i<TopResults.size();++i)
						{
						   if(TopResults.get(i).getValue()!= -10000.0)
							   flag = false;
						}
						if(flag) {
							MessageDialog.openInformation(null, "OK", "There are 0 Results after searching! Check your query!");
							return;
						}
						tableItem.setText(new String[] { "1.",TopResults.get(0).getKey(), TopResults.get(0).getValue().toString()});
						tableItem_1.setText(new String[] { "2.",TopResults.get(1).getKey(), TopResults.get(1).getValue().toString()});
						tableItem_2.setText(new String[] { "3.",TopResults.get(2).getKey(), TopResults.get(2).getValue().toString()});
						tableItem_3.setText(new String[] { "4.",TopResults.get(3).getKey(), TopResults.get(3).getValue().toString()});
						tableItem_4.setText(new String[] { "5.",TopResults.get(4).getKey(), TopResults.get(4).getValue().toString()});
						tableItem_5.setText(new String[] { "6.",TopResults.get(5).getKey(), TopResults.get(5).getValue().toString()});
						tableItem_6.setText(new String[] { "7.",TopResults.get(6).getKey(), TopResults.get(6).getValue().toString()});
						tableItem_7.setText(new String[] { "8.",TopResults.get(7).getKey(), TopResults.get(7).getValue().toString()});
						tableItem_8.setText(new String[] { "9.",TopResults.get(8).getKey(), TopResults.get(8).getValue().toString()});
						tableItem_9.setText(new String[] { "10.",TopResults.get(9).getKey(), TopResults.get(9).getValue().toString()});
						tableItem_10.setText(new String[] { "11.",TopResults.get(10).getKey(), TopResults.get(10).getValue().toString()});
						tableItem_11.setText(new String[] { "12.",TopResults.get(11).getKey(), TopResults.get(11).getValue().toString()});
						tableItem_12.setText(new String[] { "13.",TopResults.get(12).getKey(), TopResults.get(12).getValue().toString()});
						tableItem_13.setText(new String[] { "14.",TopResults.get(13).getKey(), TopResults.get(13).getValue().toString()});
						tableItem_14.setText(new String[] { "15.",TopResults.get(14).getKey(), TopResults.get(14).getValue().toString()});
						tableItem_15.setText(new String[] { "16.",TopResults.get(15).getKey(), TopResults.get(15).getValue().toString()});
						tableItem_16.setText(new String[] { "17.",TopResults.get(16).getKey(), TopResults.get(16).getValue().toString()});
						tableItem_17.setText(new String[] { "18.",TopResults.get(17).getKey(), TopResults.get(17).getValue().toString()});
						tableItem_18.setText(new String[] { "19.",TopResults.get(18).getKey(), TopResults.get(18).getValue().toString()});
						tableItem_19.setText(new String[] { "20.",TopResults.get(19).getKey(), TopResults.get(19).getValue().toString()});
						
						MessageDialog.openInformation(null, "OK", " Searching end! You can scan the results in the Result Box... ");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
		});
		btnSearch.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {

			}
		});
		btnSearch.setBounds(616, 61, 87, 27);
		btnSearch.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		btnSearch.setFont(SWTResourceManager.getFont("Î¢ÈíÑÅºÚ", 13, SWT.NORMAL));
		btnSearch.setText("Search");
		
		Label lblQuestionContent = new Label(composite, SWT.NONE);
		lblQuestionContent.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		lblQuestionContent.setBounds(533, 119, 123, 27);
		lblQuestionContent.setFont(SWTResourceManager.getFont("Î¢ÈíÑÅºÚ", 12, SWT.NORMAL));
		lblQuestionContent.setText("QA Content");
			
		
		text_3 = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text_3.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		text_3.setBounds(371, 152, 436, 449);
		formToolkit.adapt(text_3, true, true);
		
		
		Label lblModelType = new Label(composite, SWT.NONE);
		lblModelType.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		lblModelType.setFont(SWTResourceManager.getFont("Calibri", 13, SWT.NORMAL));
		lblModelType.setBounds(61, 31, 87, 23);
		lblModelType.setText("Model Type:");
		
		Label lblCollection = new Label(composite, SWT.NONE);
		lblCollection.setFont(SWTResourceManager.getFont("Calibri", 13, SWT.NORMAL));
		lblCollection.setBounds(346, 31, 79, 23);
		lblCollection.setText("Collection :");
		
				Button btnNewButton = new Button(composite, SWT.NONE);
				btnNewButton.setBounds(708, 10, 79, 27);
				btnNewButton
						.setFont(SWTResourceManager.getFont("Calibri", 13, SWT.NORMAL));
				btnNewButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							if(directoryname==null||directoryname.length()==0)
							{
								MessageDialog.openInformation(null, "Error", "cQA Collection Path is empty!");
								return;
							}
								
							System.out.println("Start initializing...");
							tf = new CollectionWordTF(directoryname);
							MessageDialog.openInformation(null, "OK", "Initializing Over. You can search cQA through inputing your query!");
							String [] collection_name = ReadFileList(".\\serializable_object\\FileList.txt");
							combo.removeAll();//¸üÐÂÏÂÀ­ÁÐ±í£¬ÏÈ½«ÏÖÓÐµÄÉ¾³ý
							for(String w : collection_name)
							{
								if(w != null)
								combo.add(w);
							}
							combo.select(0);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}
				});
				btnNewButton.setText("Initialize");
		


		text_2 = new Text(shell, SWT.BORDER);
		text_2.setBounds(200, 122, 364, 23);

		Button btnCqaCategory = new Button(shell, SWT.NONE);
		btnCqaCategory.setFont(SWTResourceManager.getFont("Calibri", 13, SWT.NORMAL));
		btnCqaCategory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirselect=new DirectoryDialog(shell,SWT.SINGLE);
				dirselect.setText("Select cQA Collection Category");
				directoryname = dirselect.open();
				if(directoryname == null)
					directoryname = "";
				text_2.setText(directoryname);
				System.out.println(directoryname);
			}
		});
		btnCqaCategory.setBounds(585, 120, 76, 27);
		btnCqaCategory.setText("Select");

		Label lblCqaCategory = new Label(shell, SWT.NONE);
		lblCqaCategory.setFont(SWTResourceManager.getFont("Calibri", 14, SWT.NORMAL));
		
		lblCqaCategory.setBounds(20, 122, 154, 23);
		lblCqaCategory.setText("cQA Category Path\uFF1A");
		
		Label label = new Label(shell, SWT.NONE);
		label.setImage(SWTResourceManager.getImage("mmdb.jpg"));
		label.setBounds(683, 27, 145, 81);
		formToolkit.adapt(label, true, true);
		
	}

}
