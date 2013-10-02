import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import net.sf.json.*;

public class FeatureDiff {
	private static String help = "Usage: java -jar FeatureDiff.jar <options> <input1 .json file> <input2 .json file> [input3 .json file] <output .json file>\n" +
			"Options:\n" +
			"\t-oc  Compute the diff between original and cloned variants.\n" +
			"\t-cc  Compute the diff between two commits of the same variant.\n" +
			"\t-occ Compute the diff between two commits, then intersect it with a clone. Argument files: clone.json, originV1.json, originV2.json, output.json\n";
	private static String optionCodes[] = {"-oc","-cc","-occ"};
	private static int numOfArgs[] = {3,3,4};
	public static String diffForOriginClone(String original, String clone){
		JSONObject originalObj,cloneObj,diffObj;
		diffObj = new JSONObject();
		originalObj = JSONObject.fromString(original);
		cloneObj = JSONObject.fromString(clone);
		Iterator<String> originIterator = originalObj.keys();
		while(originIterator.hasNext()){
			String currentKey = originIterator.next();
			JSONArray originalArray = originalObj.optJSONArray(currentKey);
			JSONArray cloneArray = cloneObj.optJSONArray(currentKey);
			JSONArray outputArray = new JSONArray();
			if(originalArray != null && cloneArray != null){
				Object[] originalObjArray = originalArray.toArray();
				Object[] cloneObjArray = cloneArray.toArray();
				for(int i = 0; i < originalObjArray.length; i++){
					for(int j = 0; j < cloneObjArray.length;j++){
						if(originalObjArray[i].toString().equals(cloneObjArray[j].toString())){
							outputArray.put(originalObjArray[i].toString());
							break;
						}
					}
				}	
			}
			diffObj.put(currentKey, outputArray);			
		}
		return diffObj.toString(4);
	}
	
	public static String diffForTwoCommits(String commit1, String commit2){
		JSONObject commit1Obj,commit2Obj,diffObj;
		diffObj = new JSONObject();
		commit1Obj = JSONObject.fromString(commit1);
		commit2Obj = JSONObject.fromString(commit2);
		Iterator<String> commit1Iterator = commit1Obj.keys();
		while(commit1Iterator.hasNext()){
			String currentKey = commit1Iterator.next();
			JSONObject commit1FeatureObj = commit1Obj.optJSONObject(currentKey);
			JSONObject commit2FeatureObj = commit2Obj.optJSONObject(currentKey);
			JSONArray outputArray = new JSONArray();
			if(commit1FeatureObj != null && commit2FeatureObj != null){
				outputArray = diffTwoFeatureObject(commit1FeatureObj,commit2FeatureObj);
			}
			diffObj.put(currentKey, outputArray);
		}
		return diffObj.toString(4);
	}
	private static JSONArray diffTwoFeatureObject(JSONObject o1, JSONObject o2){
		Iterator<String> o1Iterator = o1.keys();
		JSONArray outputArray = new JSONArray();
		while(o1Iterator.hasNext()){
			String currentKey = o1Iterator.next();
			String content1 = o1.optString(currentKey);
			String content2 = o2.optString(currentKey);
			if(!(content1.isEmpty() || content2.isEmpty())){
				if(!content1.equals(content2))
					outputArray.put(currentKey);
			}		
		}
		return outputArray;
	}
	private static String readWholeTextFile(File in){
		BufferedReader inReader = null;
		StringBuilder rel = new StringBuilder();
		try {			
			inReader = new BufferedReader(new FileReader(in));			
			char s[] = new char[100];
			int n;
			while((n = inReader.read(s)) != -1){
				rel.append(s, 0, n);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				if(inReader != null)
					inReader.close();
			} catch (IOException e) {				
			}
			
		}
		return rel.toString();
		
	}
	public static void main(String args[]){
		if(!checkArgs(args)){
			System.out.println("Error: Incorrect usage.");
			System.out.println(help);
			return;
		}
		String option = args[0];
		File input1 = new File(args[1]);
		File input2 = new File(args[2]);
		File input3 = new File(args[3]);
		File output = (args.length == 5)?(new File(args[4])):(new File(args[3]));
		String input1String,input2String,input3String;		
		BufferedWriter out = null;
		try {
			input1String = readWholeTextFile(input1);
		    input2String = readWholeTextFile(input2);
		    String diff;
		    if(option.equals("-oc")){
		    	diff = diffForOriginClone(input1String, input2String);
		    }
		    else if(option.equals("-cc")){
		    	diff = diffForTwoCommits(input1String, input2String);
		    }else if(option.equals("-occ")){
		    	input3String = readWholeTextFile(input3);
		    	diff = diffForOriginClone(input1String, diffForTwoCommits(input2String, input3String));
		    }else{
		    	diff = "";
		    }
		    out = new BufferedWriter(new FileWriter(output));
		    out.write(diff);		    
		} catch (IOException e) {			
			e.printStackTrace();			
		}finally{
			try {
				if(out != null)
					out.close();
			} catch (IOException e) {				
			}
		}
				
	}
	private static boolean checkArgs(String args[]){
		for(int i = 0; i < optionCodes.length;i++){
			if(args.length == (numOfArgs[i] + 1) && optionCodes[i].equals(args[0]))
				return true;
		}
		return false;		
	}
	
}
