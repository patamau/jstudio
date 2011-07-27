package jstudio.util;

public class CodeGenerator {
	
	private static char[] months = { 'A', 'B', 'C', 'D', 'E', 'H', 'L', 'M', 'P', 'R', 'S', 'T' };
	private static int[] odd = new int[43];
	private static int[] even = new int[43];
	
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
	
	private static int insertLastname(String src, char[] code, int index){
		int idx=index;
		//1. fill in avail. consonants
		for(int i=0; i<src.length(); i++){
			char c = src.charAt(i);
			if(!isVowel(c)){
				code[idx++]=c;
			}
			if(idx-index==3) return idx;
		}
		//2. fill in avail. vowels
		for(int i=0; i<src.length(); i++){
			char c = src.charAt(i);
			if(isVowel(c)){
				code[idx++]=c;
			}
			if(idx-index==3) return idx;
		}
		//3. fill in X
		while(idx-index<3){
			code[idx++]='X';
		}
		return idx;
	}
	
	private static int insertFirstname(String src, char[] code, int idx){
		//1. count consonants
		char[] cs = new char[4];
		int ics = 0;
		for(int i=0; i<src.length(); i++){
			char c = src.charAt(i);
			if(!isVowel(c)){
				cs[ics++]=c;
				if(ics>3) break;
			}
		}
		if(ics==4){
			//insert consonants { 1, 3, 4 } if more than 3 were found
			code[idx++]=cs[0];
			code[idx++]=cs[2];
			code[idx++]=cs[3];
		}else{
			//insert exactly like the lastname
			idx = insertLastname(src, code, idx);
		}
		return idx;
	}
	
	private static int insertDate(int date, char[] code, int index){
		String d = Integer.toString(date);
		if(d.length()<2) code[index++]='0';
		else code[index++]=d.charAt(d.length()-2);
		code[index++]=d.charAt(d.length()-1);
		return index;
	}
	
	private static int insertCity(String idloc, char[] code, int index){
		code[index++] = idloc.charAt(0);
		code[index++] = idloc.charAt(1);
		code[index++] = idloc.charAt(2);
		code[index++] = idloc.charAt(3);
		return index;
	}
	
	private static int[] getEvenCodes(){
		//verify even hasn't been initialized yet
		if(even[1]==1) return even;
		int in=0, il=0;
		for(int i=0; i<10; i++){
			even[i]=in++;
		}
		for(int i=17; i<43; i++){
			even[i]=il++;
		}
		return even;
	}
	
	private static int[] getOddCodes(){
		//verify odd hasn't been initialized yet
		if(odd[0]==1) return odd;
		odd[0]=1;
		odd[1]=0;
		odd[2]=5;
		odd[3]=7;
		odd[4]=9;
		odd[5]=13;
		odd[6]=15;
		odd[7]=17;
		odd[8]=19;
		odd[9]=21;
		odd[17]=1;
		odd[18]=0;
		odd[19]=5;
		odd[20]=7;
		odd[21]=9;
		odd[22]=13;
		odd[23]=15;
		odd[24]=17;
		odd[25]=19;
		odd[26]=21;
		odd[27]=2;  
		odd[28]=4;  
		odd[29]=18;  
		odd[30]=20;  
		odd[31]=11;  
		odd[32]=3;  
		odd[33]=6;  
		odd[34]=8;  
		odd[35]=12;  
		odd[36]=14;  
		odd[37]=16;  
		odd[38]=10;  
		odd[39]=22;  
		odd[40]=25;  
		odd[41]=24;  
		odd[42]=23;  
		return odd;
	}
	
	private static void insertCheck(char[] code){
		int[] even = getEvenCodes();
		int evensum = 0;
		for (int i=1;i<=13;i+=2){
			evensum += even[code[i]-(int)'0'];
		}
		int[] odd = getOddCodes();
		int oddsum = 0;
		for (int i=0;i<=14;i+=2) {
			oddsum += odd[code[i]-(int)'0'];
		}
		int r = (oddsum+evensum)%26;
		code[15]= (char)(r+(int)'A');
	}

	public static String generate(String name, String last, boolean male, int year, int month, int day, String idloc){
		//preparing values
		name = normalize(name);
		last = normalize(last);
		idloc= normalize(idloc);
		day = male?day:day+40;
		
		//initializing code
		char code[] = new char[16];
		int index = 0; //position along the code
		
		//inserting stuff
		index = insertLastname(last, code, index);
		index = insertFirstname(name, code, index);		
		index = insertDate(year, code, index);
		code[index++]=months[month-1];
		index = insertDate(day, code, index);
		index = insertCity(idloc, code, index);
		insertCheck(code);
		
		return new String(code);
	}
	
	public static void main(String args[]){
		String code = generate("Matteo","Pedrotti",true, 1980, 12, 10, "L378");
		System.out.println(code);
		code = generate("Sara","Pedrotti",false, 2010, 9, 20, "L378");
		System.out.println(code);
		code = generate("Francesca","Vezzoli",false, 1981, 6, 6, "L378");
		System.out.println(code);
	}
}
