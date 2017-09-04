package assignment5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class BayesClassifierImpl {

	static String inputFile;
	static Scanner in = new Scanner(System.in);
	static String target_Attribute="";
	static String path="";
	static String testPath;
	static BufferedReader bufferReader = null;
	static String[] attributes;
	static String s;
	static final String FILENAME = "output.txt";
	static BufferedWriter bw = null;
	static FileWriter fw = null;
	static List<String[]> data = new ArrayList<String[]>();
	static List<List<String[]>> data_split = new ArrayList<List<String[]>>();
	static int columns;
	static List<String> colValue=new ArrayList<>();
	static List<String[]> testData = new ArrayList<String[]>();
	static Map<String, Double> column_Data = new HashMap<String, Double>();
	static Map<String, Set<String>> attr_uniq_val = new HashMap<String, Set<String>>();
	static Map<String,Map<String,Map<String,Double>>> target_attr_name=new HashMap<String,Map<String,Map<String,Double>>>();
	static double tv_Sum=0.0;
	static String tv_1 = null;
	static String tv_2=null;
	static double accuracy_count=0.0;
	static double accuracy=0.0;
	static long start_time;
	static long end_time;
	static long execution_time;
	static int selection;
	static double constantValue=0.1;

/*
 * main function from where the control begins
 * which in turn calls the classifier method
 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {

			fw = new FileWriter(FILENAME);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bw = new BufferedWriter(fw);


		//Function to read file input from user
		inputReader();

		

		//Call for actual method which builds the model
		bayesClassifier();


		//Execution time
		execution_time=end_time-start_time;
		execution_time=execution_time/1000000;

		try {
			bw.write("\n\nExecution time:"+execution_time+"ms");

			if (bw != null)
				bw.close();

			if (fw != null)
				fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	/* 
	 * Accepts/prompts for user inputs such as training filename,test filename 
	 * and target attribute,also validates the inputs
	 * 
	 */
	public static void inputReader()
	{
		System.out.println("Please enter a training file\n");
		path = in.nextLine();

		while (!(path.matches("[_a-zA-Z0-9._]+"))) {
			System.out.println("Please input valid file name \n");
			path = in.nextLine();		

		}

		data=fileReader(path);

		System.out.println("Please input the test file name to determine accuracy\n");
		testPath = in.nextLine();

		while (!(testPath.matches("[_a-zA-Z0-9._]+"))) {
			System.out.println("Please input valid file name \n");
			testPath = in.nextLine();		

		}

		testData=fileReader(testPath);

		System.out.println("Please input the target attribute from below list by adding index number");
		for(int s=0;s<attributes.length;s++)
		{
			System.out.println(s+"."+attributes[s]);
		}
		selection=in.nextInt();
		while (selection < 0 || selection > attributes.length-1) {
			System.out.println("Please select from above list \n");
			selection = in.nextInt();		

		}

		target_Attribute=attributes[selection];
	}



	/*
	 * This function reads the input file data and stores into header and data format
	 * for further processing
	 *  Output- Generation of
	 * List<String[]> data
	 */

	public static List<String[]> fileReader(String filePath)
	{
		List<String[]> data_add = new ArrayList<String[]>();
		try {
			bufferReader = new BufferedReader(
					new FileReader(filePath));

			try {
				s = bufferReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			attributes=s.split("\\s+");
			columns = attributes.length;
			String row;
			try {

				while (!((row = bufferReader.readLine()) == null)) { 
					String[] rowData = new String[columns];
					for (int i = 0; i < columns; i++) { 
						String[] value = row.split("\\s+");     
						rowData[i] = value[i].trim();

					}
					data_add.add(rowData);
				}


			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data_add;
	}


	/*
	 * Calculates the target attributes unique values
	 * along with count of occurrences for each unique attributes
	 */
	public static void targetCalculation()
	{
		Double temp_count=0.0;
		for (int i = 0; i< data.size(); i++) {
			String item = data.get(i)[selection];

			if (column_Data.containsKey(item)) {
				temp_count = column_Data.get(data.get(i)[selection]);
				column_Data.put(item, ++temp_count);
			} else {

				column_Data.put(item.trim(), 1.0);
			}
		}
	}


	/*
	 * Implementation of Bayes classifier
	 * Calculates the probability of each unique values under each attribute
	 * and stores in Map<String,Map<String,Map<String,Double>>> target_attr_name
	 */
	public static void bayesClassifier()
	{
		targetCalculation();

		for (String key : column_Data.keySet()) {
			List<String[]> data2= new ArrayList<String[]>();

			for(int b=0;b<data.size();b++)
			{
				//check for target attributes unique values and splits the input training dataset
				if(data.get(b)[selection].equalsIgnoreCase(key))

				{
					data2.add(data.get(b));
				}
			}
			data_split.add(data2);
		}

		String attr=new String();

		for (String key : column_Data.keySet()) {
			Map<String,Map<String,Double>> attr_name=new HashMap<String,Map<String,Double>>();

			for(int i=0;i<attributes.length;i++)
			{
				//checks if the attribute[i] is same as the target attribute and skips the iteration 
				if(attributes[i].equalsIgnoreCase(target_Attribute))
				{
					continue;
				}

				Set<String> attr_uniq = new HashSet<String>();
				attr=attributes[i];

				for (int k = 0; k< data.size(); k++) {
					String item = data.get(k)[i];
					attr_uniq.add(item);
				}

				Iterator<String> iterator = attr_uniq.iterator();
				Map<String,Double> attr_val=new HashMap<String,Double>();

				//Iterating over unique elements in an attribute
				while(iterator.hasNext()) {

					String element = iterator.next();
					double attr_count=0.0;
					for(int m=0;m<data.size();m++)
					{
						if(data.get(m)[i].equalsIgnoreCase(element) && data.get(m)[selection].equalsIgnoreCase(key))
						{
							attr_count++;
						}

					}
					attr_val.put(element, attr_count);
				}
				attr_name.put(attributes[i], attr_val);
			}
			//Final object which stores the complete probability values of all elements
			target_attr_name.put(target_Attribute+"="+key, attr_name);
		}

		accuracyCalculation();

	}


	/*
	 * Calculates the probability for test dataset and
	 * determines classifier value based on the probability values
	 */
	public static void accuracyCalculation()
	{
		//Calculating execution time
				start_time=System.nanoTime();
		//stores the classifier values for output file
		String[] classification=new String[testData.size()];
		String attrStr = "";

		for(int p=0;p<attributes.length;p++)
		{
			attrStr = attrStr.concat(String.format("%1$-15s", attributes[p]));
		}

		attrStr=attrStr.concat(String.format("%1$-15s","classification"));

		try {
			bw.write(attrStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(int x=0;x<testData.size();x++)
		{
			Double value=0.0;
			for (String key : column_Data.keySet()) {
				Double probability=column_Data.get(key)/data.size();

				for(int y=0;y<attributes.length-1;y++)
				{
					if(attributes[y].equalsIgnoreCase(target_Attribute))
					{
						continue;
					}
					Double exp;

					Double nc=target_attr_name.get(target_Attribute+"="+key).get(attributes[y]).get(testData.get(x)[y]);

					if(nc==null)
					{
						nc=0.0;

						Double countVal=(nc)+
								(constantValue * 1/target_attr_name.get(target_Attribute+"="+key).get(attributes[y]).size()) /(column_Data.get(key)+constantValue) ;


						exp=(Math.log10(probability))+Math.log10(countVal);

						probability=Math.pow(10, exp);
					}

					else
					{
						exp=(Math.log10(probability))+Math.log10(nc/column_Data.get(key));
						probability=Math.pow(10, exp);

					}
				}

				if (probability>value)
				{
					classification[x]=key;
					value=probability;
				}
			}

			String str="";

			for(int j=0;j<attributes.length;j++)
			{
				str = str.concat(String.format("%1$-15s", testData.get(x)[j]));
			}

			str=str.concat(String.format("%1$-15s",classification[x]));

			try {
				bw.write(str);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(testData.get(x)[selection].equalsIgnoreCase(classification[x]))	
			{
				accuracy_count++;
			}

		}
		
		end_time=System.nanoTime();

		accuracy=accuracy_count/testData.size();

		try {

			bw.write("\n\nAccuracy = "+accuracy_count+"/"+testData.size()+"="+accuracy);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}



