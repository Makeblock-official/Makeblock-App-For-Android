package cc.makeblock.modules;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import cc.makeblock.makeblock.R;

public class MeRgbLed extends MeModule implements OnTouchListener{
	static String devName = "rgbled";
	SeekBar slider;
	TextView valueTxt;
	String writeStr="";
	ImageView hue;
	ImageView mask;
	Bitmap bitmap;
	
	public MeRgbLed(int port, int slot) {
		super(devName, MeModule.DEV_RGBLED, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_rgb_view;
		imageId = R.drawable.rgbled;
		shouldSelectSlot = true;
		this.scale = 1.0f;
	}
	
	public MeRgbLed(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_rgb_view;
		imageId = R.drawable.rgbled;
		shouldSelectSlot = true;
		this.scale = 1.0f;
	}
	
	public void setEnable(Handler handler){
		mHandler = handler;
		hue = (ImageView) view.findViewById(R.id.rgbColorPick);
		bitmap = ((BitmapDrawable)hue.getDrawable()).getBitmap();
		hue.setOnTouchListener(this);
		mask = (ImageView) view.findViewById(R.id.rgbMask);
		
		return;
	}
	
	public void setDisable(){
		if(hue!=null)
			hue.setOnTouchListener(null);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {  
		case MotionEvent.ACTION_DOWN:
			
            break;
       	case MotionEvent.ACTION_MOVE:
       	case MotionEvent.ACTION_UP:
            // the original hue image is in 80x80
       		try{
	            int x = (int)(80*event.getX())/v.getMeasuredWidth();
	            int y = (int)(80*event.getY())/v.getMeasuredHeight();
//	            Log.d("mb", ""+x+":"+y);
	            x+=15;
	            y+=15;
	            
	            int pixel = bitmap.getPixel(x, y);
	            int R = (int)(Color.red(pixel)*0.6);
	            int G = (int)(Color.green(pixel)*0.6);
	            int B = (int)(Color.blue(pixel)*0.6);
	            sendColor(R,G,B);
	            mask.setColorFilter(pixel);
       		}catch(Exception e){
       			
       		}
       		break;
		}
		return true; 
	}
	
	//rgbrun(%@,%@,%c)
	public String getScriptRun(String var){
		varReg = var;
		String code = "rgbrun("+getPortString(port)+","+getSlotString(slot)+","+var+")\n";
		return code;
	}
	long ctime=System.currentTimeMillis();
	void sendColor(int r,int g,int b){
		if(System.currentTimeMillis()-ctime>80){
			ctime=System.currentTimeMillis();
			int irgb=(r<<8)+(g<<16)+(b<<24);
			byte[] wr = buildWrite(type, port, slot, irgb);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
		}
	}	
}
