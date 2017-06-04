package com.coa.project2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Map.Entry;

public class ScoreBoard {
	
	private static Integer[] dumpArray      = new Integer[2];
	private static Integer   issueWidth     = 0;
	private static Integer   cacheLatency   = 0;
//	private static Integer   noOfInst	    = 0;
	private static Boolean   dFlagEnable    = true;
	private static Boolean   completionFlag = false;
	private static String   processor = null;
	private static HashMap<String, String> scoreBoard = new HashMap<String, String>();
//	private static ArrayList<String> waitingList = new ArrayList<String>();
	private static ArrayList<String> instructionList = new ArrayList<String>();
	private static Integer instructionReadCount = 0;
	private static Integer cycle = -1;
	private static final String seperator = ".";
	private static ArrayList<Integer> outOfOrder = new ArrayList<Integer>();
	private static ArrayList<String> inSeq = new ArrayList<String>();
	private static Integer instIssued = 0;
	
	public static void main(String[] args) {
		BufferedReader readFile = null;
		try {
			processor = args[1];
			if("in".equalsIgnoreCase(processor))
			{
				System.out.println("In-order execution");
			}
			else
			{
				System.out.println("Out-of-order execution");
			}
			readFile = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
			String line = null;
			StringTokenizer strTok = new StringTokenizer(readFile.readLine(), " \t\f\r\n");
			strTok.nextToken(); dumpArray[0] = Integer.parseInt(strTok.nextToken()); dumpArray[1] = Integer.parseInt(strTok.nextToken());
			
			strTok = new StringTokenizer(readFile.readLine(), " \t\f\r\n");
			strTok.nextToken(); issueWidth = Integer.parseInt(strTok.nextToken());
			
			strTok = new StringTokenizer(readFile.readLine(), " \t\f\r\n");
			strTok.nextToken(); cacheLatency = Integer.parseInt(strTok.nextToken());
			
			while( (line = readFile.readLine()) != null ){
				instructionList.add(line);
			}
			
			while(!completionFlag){
				processInstructions();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(readFile != null){
				try {
					readFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String[] getInst(String instr){ 
		StringTokenizer currInstTok = new StringTokenizer(instr, " \t\f\r\n");
		String[] currInst = new String[currInstTok.countTokens()];
		for(int i=0; i<currInst.length; i++){
			currInst[i] = currInstTok.nextToken();
		}
		for(int i=1; i<currInst.length; i++){
			if( scoreBoard.containsKey(currInst[i]) ){
				currInst = null;
				break;
			}
		}
		return currInst;
	}
	
	public static Integer getInstLatency(String inst){
		if("ADD".equalsIgnoreCase(inst) || "SUB".equalsIgnoreCase(inst) || "MOV".equalsIgnoreCase(inst) || "XOR".equalsIgnoreCase(inst) || "OR".equalsIgnoreCase(inst) || "AND".equalsIgnoreCase(inst)){
			return 1;
		}else if ("MUL".equalsIgnoreCase(inst)) {
			return 4;
		}else if ("DIV".equalsIgnoreCase(inst)) {
			if(dFlagEnable){
				dFlagEnable = false;
				return 8;
			}else{
				return 0;
			}
		}else if ("LDH".equalsIgnoreCase(inst)) {
			return 4;
		}else if ("LDM".equalsIgnoreCase(inst)) {
			return 4 + cacheLatency;
		}
		return 0;
	}
	
	public static Integer getInstCycle(String inst){
		if("ADD".equalsIgnoreCase(inst) || "SUB".equalsIgnoreCase(inst) || "MOV".equalsIgnoreCase(inst) || "XOR".equalsIgnoreCase(inst) || "OR".equalsIgnoreCase(inst) || "AND".equalsIgnoreCase(inst)){
			return 1;
		}else if ("MUL".equalsIgnoreCase(inst)) {
			return 4;
		}else if ("DIV".equalsIgnoreCase(inst)) {
				return 8;
		}else if ("LDH".equalsIgnoreCase(inst)) {
			return 4;
		}else if ("LDM".equalsIgnoreCase(inst)) {
			return 4 + cacheLatency;
		}
		return 0;
	}
	
	public static void insertInst(Integer iRLoc){
		if(iRLoc >= instructionList.size()){
			
		}else{
			String instTemp = instructionList.get(iRLoc);
			String[] currInst = getInst(instTemp);
			if(currInst != null){
				Integer instrLatency = getInstLatency(currInst[0]);
				if(instrLatency > 0){
	//				if(issueWidth > scoreBoard.size()){
						scoreBoard.put(currInst[1], (instrLatency+1) + seperator + instTemp);
						System.out.println("Cycle " + cycle + " Issue \t- \t " + instTemp);
	//					noOfInst++;
						instructionReadCount++;
						instIssued++;
						outOfOrder.add(iRLoc);
	//				}
				}else{
					//do not read / error
				}
			}else{
				// to do: get next inst - in case of ooo
			}
		}
	}
	
	public static void processInstructions(){
		if(instructionReadCount == instructionList.size() && scoreBoard.size() == 0){
			completionFlag = true;
		}
		
		cycle++;
		
		if(cycle<instructionList.size()){
			inSeq.add(instructionList.get(cycle));
		}
		
		HashMap<String, String> scoreBoardTemp = new HashMap<String, String>();
		for(Entry<String, String> entry : scoreBoard.entrySet()) {
			String[] instrScore = entry.getValue().split("\\.");
			Integer value = Integer.parseInt(instrScore[0]) - 1;
			if(value == 0){
//				scoreBoard.remove(entry.getKey());
				System.out.println("Cycle " + cycle + " Complete- \t " + instrScore[1]);
				if(instrScore[1].startsWith("DIV")){
					dFlagEnable = true;
				}
				inSeq.remove(instrScore[1]);
//				noOfInst--;
			}else{
				if(instrScore[1].startsWith("LD") && value < 3){
				//	System.out.println("Cycle " + cycle + " Report\t- \t" + instrScore[1]);
					System.out.println("Cycle " + cycle + " [" + instrScore[1].split("\\s")[0] + "] \t- " + entry.getKey() + " " + (Integer.parseInt(instrScore[0])-1) + " : " + instrScore[1]);
				}
				
				scoreBoardTemp.put(entry.getKey(), value + seperator + instrScore[1]);
			}
		}
		scoreBoard.clear();
		scoreBoard.putAll(scoreBoardTemp);
		
		instIssued = 0;
		if(instructionReadCount < instructionList.size()){
			if("in".equalsIgnoreCase(processor))
			{
				//in order
				Integer issueCnt = (cycle+1) - instructionReadCount;//Math.min(((cycle+1) - instructionReadCount) , (issueWidth-scoreBoard.size()) );
				for(int i=0; i<issueCnt; i++){
					if(instIssued<issueWidth){
						insertInst(instructionReadCount);
					}
				}
			}
			else if ("ooo".equalsIgnoreCase(processor))
			{
				//out of order
				for(int i=0; i<(cycle+1) && i<instructionList.size(); i++ ){
					if(!outOfOrder.contains(i)){
						if(instIssued<issueWidth){
							if(!(scoreBoard.isEmpty() && !inSeq.isEmpty())){
								Boolean exeI = true;
								for(int j=0;j<inSeq.size();j++){
									if(inSeq.get(j).equalsIgnoreCase(instructionList.get(i))){
										
									}else{
										StringTokenizer currInstTok = new StringTokenizer(inSeq.get(j), " \t\f\r\n");
										String[] currInst = new String[currInstTok.countTokens()];
										currInst[0] = currInstTok.nextToken();
										for(int k=1; k<currInst.length; k++){
											currInst[k] = currInstTok.nextToken();
										}
										StringTokenizer currInstTokExe = new StringTokenizer(instructionList.get(i), " \t\f\r\n");
										String[] currInst1 = new String[currInstTokExe.countTokens()];
										currInst1[0] = currInstTokExe.nextToken();
										for(int k=1; k<currInst.length; k++){
											currInst1[k] = currInstTokExe.nextToken();
										}
										for(int k=2; k<currInst1.length; k++){
											if(currInst[1].equalsIgnoreCase(currInst1[k])){
												exeI = false;
												break;
											}
										}
										for(int k=2; k<currInst.length && exeI; k++){
											if(currInst[k].equalsIgnoreCase(currInst1[1])){
												if(scoreBoard.get(currInst[1]) != null){
													String[] instrScore = scoreBoard.get(currInst[1]).split("\\.");
													Integer score = Integer.parseInt(instrScore[0]);
													if(score > getInstCycle(currInst[0])){
														exeI = false;
														break;
													}
												}else{
													exeI = false;
												}
											}
										}
									}
								}
								if(exeI){
									insertInst(i);
								}
							}else{
								insertInst(i);
							}
						}
					}
				}
			}
			
			
		}
		
		for(int i=0; i<dumpArray.length; i++){
			if(dumpArray[i] == cycle){
				System.out.println("Cycle " + cycle + " Dump \t-" );
				for(Entry<String, String> entry : scoreBoard.entrySet()) {
					String[] instrScore = entry.getValue().split("\\.");
					System.out.println("\t\t" + entry.getKey() + " " + (Integer.parseInt(instrScore[0])-1) + " : " + instrScore[1]);
				}
			}
		}
		
	}
}