import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Cache {

	private static ArrayList<String> instructionList = new ArrayList<String>();
	private static HashMap<Integer, String> cacheLine0 = new HashMap <Integer, String>();
	private static HashMap<Integer, String> cacheLine1 = new HashMap <Integer, String>();
	private static HashMap<Integer, String> cacheLine2 = new HashMap <Integer, String>();
	private static ArrayList<HashMap<Integer, String>> cacheList = new ArrayList<HashMap<Integer, String>>();
	private static boolean probeFlag = false;
	private static boolean sharedFlag = false;
	
	static void init()
	{
		for(int i = 0; i<4; i++)
		{
			cacheLine0.put(i, "I");	
			cacheLine1.put(i, "I");
			cacheLine2.put(i, "I");
		}
		cacheList.add(0,cacheLine0);
		cacheList.add(1,cacheLine1);
		cacheList.add(2,cacheLine2);
	}
	
	public static void main (String args[]) throws IOException
	{
		init();
		BufferedReader readFile = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		String line = null;
		while( (line = readFile.readLine()) != null )
		{
			instructionList.add(line);
		}
		for(int i = 0; i < instructionList.size(); i++)
		{
			process(instructionList.get(i));
		}
				
	}
	
	static void process (String instr)
	{
		System.out.println("\nInput: " + instr);
		String state = null;
		sharedFlag = false;
		int cacheNumber = Character.getNumericValue(instr.charAt(0));
		String command = "" + instr.charAt(1);
		int lineNumber = Character.getNumericValue(instr.charAt(2));
		state = cacheList.get(cacheNumber).get(lineNumber);
		if("I".equalsIgnoreCase(state))
		{
			invalid(cacheNumber, lineNumber, command, state, instr);
		}
		else if("E".equalsIgnoreCase(state))
		{
			exclusive(cacheNumber, lineNumber, command, state, instr);
		}
		else if("S".equalsIgnoreCase(state))
		{
			shared(cacheNumber, lineNumber, command, state, instr);
		}
		else if("M".equalsIgnoreCase(state))
		{
			modified(cacheNumber, lineNumber, command, state, instr);
		}
		else if("O".equalsIgnoreCase(state))
		{
			owned(cacheNumber, lineNumber, command, state, instr);
		}
		
	}
	
	//Invalid
	static void invalid(int cacheNumber, int lineNumber, String command, String state, String instr)
	{
		System.out.print("Cache "+ cacheNumber +", Miss " + lineNumber);
		if("r".equalsIgnoreCase(command)) // Read
		{
			for(int i = 0; i<3; i++)
			{
				if(i!=cacheNumber)
				{
					System.out.println("\n\tCache "+ i +" Probe read " + lineNumber);
					if("I".equalsIgnoreCase(cacheList.get(i).get(lineNumber)))
					{
						System.out.println("\tMiss \n\t"+cacheList.get(i).get(lineNumber) + " -> I");
						cacheList.get(i).put(lineNumber, "I");
					}
					else if("E".equalsIgnoreCase(cacheList.get(i).get(lineNumber)))
					{
						System.out.println("\tHit \n \t"+cacheList.get(i).get(lineNumber) + " -> S");
						cacheList.get(i).put(lineNumber, "S");
						sharedFlag = true;
					}
					else if("S".equalsIgnoreCase(cacheList.get(i).get(lineNumber)))
					{
						System.out.println("\tHit \n \t"+cacheList.get(i).get(lineNumber) + " -> S");
						cacheList.get(i).put(lineNumber, "S");
						sharedFlag = true;
					}
					else if("M".equalsIgnoreCase(cacheList.get(i).get(lineNumber)))
					{
						System.out.println("\tDirty Hit, Writeback \n \t"+cacheList.get(i).get(lineNumber) + " -> O");
						cacheList.get(i).put(lineNumber, "O");
						sharedFlag = true;
					}
					else if("O".equalsIgnoreCase(cacheList.get(i).get(lineNumber)))
					{	
						System.out.println("\tHit \n \t"+cacheList.get(i).get(lineNumber) + " -> O");
						cacheList.get(i).put(lineNumber, "O");
						sharedFlag = true;
					}
					System.out.println("\tEnd Probe Read");
				}
			}
			if(sharedFlag)
			{
				cacheList.get(cacheNumber).put(lineNumber, "S"); //bus read
				System.out.println("Cache " + cacheNumber + "\n" + state + " -> S");
			}
			else
			{
				cacheList.get(cacheNumber).put(lineNumber, "E"); //PrRd 
				System.out.println("Cache " + cacheNumber + "\n" + state + " -> E");
			}
		}
		else // Write
		{
			for(int i = 0; i<3; i++)
			{
				if(i!=cacheNumber)
				{
					System.out.println("\n\tCache "+ i +" Probe write " + lineNumber);
					if("I".equalsIgnoreCase(cacheList.get(i).get(lineNumber)))
					{
						System.out.println("\tMiss \n\t"+cacheList.get(i).get(lineNumber) + " -> I");
						cacheList.get(i).put(lineNumber, "I"); //BusWr
					}
					else if("M".equalsIgnoreCase(cacheList.get(i).get(lineNumber)))
					{
						System.out.println("\tDirty Hit, Flush \n\t"+cacheList.get(i).get(lineNumber) + " -> I");
						cacheList.get(i).put(lineNumber, "I"); //BusWr
					}
					else
					{
						System.out.println("\tHit \n\t"+cacheList.get(i).get(lineNumber) + " -> I");
						cacheList.get(i).put(lineNumber, "I"); //BusWr
					}
					System.out.println("\tEnd Probe write");
					
				}
			}
			cacheList.get(cacheNumber).put(lineNumber, "M"); //PrWr
			System.out.println("Cache " + cacheNumber + "\n" + state + " -> M");
		}
		
	}
	
	//Exclusive
	static void exclusive(int cacheNumber, int lineNumber, String command, String state, String instr)
	{
		System.out.print("Cache "+ cacheNumber +", Hit " + lineNumber);
		if("r".equalsIgnoreCase(command))
		{
			System.out.println("\nE -> E");
		}
		else
		{
			cacheList.get(cacheNumber).put(lineNumber, "M");
			System.out.println("\nE -> M");
		}
	}
	
	//Shared
	static void shared(int cacheNumber, int lineNumber, String command, String state, String instr)
	{
		System.out.print("Cache "+ cacheNumber +", Hit " + lineNumber);
		if("r".equalsIgnoreCase(command))
		{
			System.out.println("\nS -> S");
		}
		else
		{
			for(int i = 0; i<3; i++)
			{
				if(i!=cacheNumber)
				{
					System.out.println("\n\tCache "+ i +" Probe write " + lineNumber);
					if("I".equalsIgnoreCase(cacheList.get(i).get(lineNumber)))
					{
						System.out.println("\tMiss \n\t"+cacheList.get(i).get(lineNumber) + " -> I");
						cacheList.get(i).put(lineNumber, "I");
					}
					else if("M".equalsIgnoreCase(cacheList.get(i).get(lineNumber)))
					{
						System.out.println("\tDirty Hit \n\t"+cacheList.get(i).get(lineNumber) + " -> I");
						cacheList.get(i).put(lineNumber, "I"); 
					}
					else
					{
						System.out.println("\tHit \n\t"+cacheList.get(i).get(lineNumber) + " -> I");
						cacheList.get(i).put(lineNumber, "I"); 
					}
					System.out.println("\tEnd Probe write");
					
				}
			}
			cacheList.get(cacheNumber).put(lineNumber, "M");
			System.out.println("\nS -> M");
		}
	}
	
	//Modified
	static void modified(int cacheNumber, int lineNumber, String command, String state, String instr)
	{
		System.out.print("Cache "+ cacheNumber +", Hit " + lineNumber);
		if("r".equalsIgnoreCase(command))
		{
			System.out.println("\nM -> M");
		}
		else
		{
			cacheList.get(cacheNumber).put(lineNumber, "M");
			System.out.println("\nM -> M");
		}
	}
	
	//Owned
	static void owned(int cacheNumber, int lineNumber, String command, String state, String instr)
	{
		System.out.print("Cache "+ cacheNumber +", Hit " + lineNumber);
		if("r".equalsIgnoreCase(command))
		{
			System.out.println("\nO -> O");
		}
		else
		{
			cacheList.get(cacheNumber).put(lineNumber, "M");
			System.out.println("\nO -> M");
		}
	}
}
