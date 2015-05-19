package cc.makeblock.makeblock;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class MeDevice {
	private int mWidth;
    private int mHeight;
    public float scale;
    public int motorSpeed=0;
    public boolean manualMode = true;
    private static MeDevice mInstance;
	public MeDevice(){
	}
	public static MeDevice sharedManager(){
		if(mInstance==null){
			mInstance = new MeDevice();
		}
		return mInstance;
	}
	public void setWidth(int v){
		mWidth = v;
	}
	public void setHeight(int v){
		scale =  Math.min(1.6f,(float)v/720.0f);
		mHeight = v;
	}
	public int width(){
		return mWidth;
	}
	public int height(){
		return mHeight;
	}
}
