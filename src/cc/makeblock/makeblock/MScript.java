package cc.makeblock.makeblock;

import java.util.ArrayList;

public class MScript {
	public int numOfCode;
	public int numOfConst;
	
	public MScript(){
		numOfCode = 0;
		numOfConst = 0;
	}
	
	
	public String compile(String code){
		String n = compileJNI(code);
		String[] tmp = n.split(" ");
		numOfCode = Integer.parseInt(tmp[1].trim());
		numOfConst = Integer.parseInt(tmp[2].trim());
		return n;
	}
	
	public String getCode(int index){
		
		String n = getCodeJNI(index);
		return n;
	}
	
	public String getConst(int index){
		String n = getConstJNI(index);
		return n;
	}
	
	public String getIrq(String code){
		String n = getIrqJNI(code);
		return n;
	}
	
	
	// Mscript native
	public native String stringFromJNI();
	public native String compileJNI(String code);
	public native String getCodeJNI(int index);
	public native String getConstJNI(int index);
	public native String getIrqJNI(String code);
	public native String getRegName(int index);
	public native int getRegIndex(String reg);

	static {
        System.loadLibrary("hello-jni");
    }
}
