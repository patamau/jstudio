package jstudio.util;

public class CodeGenerator {
	
	private static String normalize(String src){
		src = src.trim().toUpperCase();
		return src;
	}
	
	private static boolean isVowel(char c){
		switch(c){
			case 'A':
			case 'E':
			case 'I':
			case 'O':
			case 'U':
				return true;
			default:
				return false;
		}
	}
	
	private static int insertName(String src, char[] code, int index){
		int idx=index;
		for(int i=0; i<src.length(); i++){
			char c = src.charAt(i);
			if(!isVowel(c)){
				code[idx++]=c;
			}
			if(idx-index==3) return idx;
		}
		for(int i=0; i<src.length(); i++){
			char c = src.charAt(i);
			if(isVowel(c)){
				code[idx++]=c;
			}
			if(idx-index==3) return idx;
		}
		while(idx-index<3){
			code[idx++]='X';
		}
		return idx;
	}
	
	private static int insertDate(int date, char[] code, int index){
		String d = Integer.toString(date);
		code[index++]=d.charAt(d.length()-2);
		code[index++]=d.charAt(d.length()-1);
		return index;
	}
	
	private static int insertCity(String pv, String mn, char[] code, int index){
		//TODO: find the code based on province and municipality
		code[index++] = 'X';
		code[index++] = '1';
		code[index++] = '1';
		code[index++] = '1';
		return index;
	}
	
	private static void insertCheck(char[] code){
		code[15]='C';
	}
	
	private static char[] months = { 'A', 'B', 'C', 'D', 'E', 'H', 'L', 'M', 'P', 'R', 'S', 'T' };

	public static String generate(String name, String last, boolean male, int year, int month, int day, String province, String munic){
		//preparing values
		name = normalize(name);
		last = normalize(last);
		province = normalize(province);
		munic = normalize(munic);
		day = male?day:day+40;
		
		//initializing code
		char code[] = new char[16];
		int index = 0; //position along the code
		
		//inserting stuff
		index = insertName(last, code, index);
		index = insertName(name, code, index);		
		index = insertDate(year, code, index);
		code[index++]=months[month-1];
		index = insertDate(day, code, index);
		index = insertCity(province, munic, code, index);
		insertCheck(code);
		
		return new String(code);
	}
	
	public static void main(String args[]){
		String code = generate("Matteo","Pedrotti",true, 1980, 12, 10, "TN", "Trento");
		System.out.println(code);
	}
}
