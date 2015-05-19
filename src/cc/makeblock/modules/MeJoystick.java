package cc.makeblock.modules;

import org.json.JSONObject;

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

public class MeJoystick extends MeModule implements OnTouchListener{
	static String devName = "joystick";
	SeekBar slider;
	TextView valueTxt;
	ImageView bar;
	String writeStr="";
	int lastX, lastY,originX,originY;
	int lastMotorL,lastMotorR;
	int initTop,initLeft;
	int lastTop,lastLeft;
	long lastTime;
	public MeJoystick(int port, int slot) {
		super(devName, MeModule.DEV_JOYSTICK, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_joystick_view;
		this.scale = 0.9f;
	}
	
	public MeJoystick(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_joystick_view;
		this.scale = 0.9f;
	}
	
	public void setEnable(Handler handler){
		mHandler = handler;
		if(view==null)return;
		bar = (ImageView) view.findViewById(R.id.joystickBar);
		initTop = bar.getTop();
		initLeft = bar.getLeft();
		bar.setOnTouchListener(this);
		return;
	}
	
	public void setDisable(){
		if(bar!=null)
			bar.setOnTouchListener(null);
	}
	
	public String getScriptRun(String var,String var2){
		varReg = var;
		varReg2 = var2;
		String code = "dcrun(m1,"+var+")\n"+"dcrun(m2,"+var2+")\n";
		return code;
	}
	
	void sendXY(int x, int y){
		if(mHandler==null) return;
		//Log.i(dbg, "joystick x="+x+" y="+y);

		int dx = -x+100;
	    int dy = -y+100;
	    int motorL,motorR;

	    motorL=dy;motorR=dy;
	    motorL-=dx;motorR+=dx;
		if(lastMotorL!=motorL || lastMotorR!=motorR){
			//Log.i(dbg, "joystick l="+motorL+" r="+motorR);
			//writeStr = varReg+"="+motorL+";"+varReg2+"="+motorR;
			byte[] wr = buildWrite(DEV_DCMOTOR, PORT_M1, slot, motorL);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
			byte[] wr2 = buildWrite(DEV_DCMOTOR, PORT_M2, slot, motorR);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr2).sendToTarget();
			lastMotorL = motorL;lastMotorR = motorR;
		}
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {  
		case MotionEvent.ACTION_DOWN:
			lastX = (int) event.getRawX();  
            lastY = (int) event.getRawY(); 
            originX = lastX;
            originY = lastY;
            lastTime = System.currentTimeMillis();
            break;
       	case MotionEvent.ACTION_MOVE:
			int dx = (int) event.getRawX() - lastX;
            int dy = (int) event.getRawY() - lastY;
            int left = v.getLeft() + dx;  
            int top = v.getTop() + dy;  
            if(left<0) left=0; if(left>200) left=200;
            if(top<0) top=0; if(top>200) top=200;
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)v.getLayoutParams();
            params.gravity = Gravity.LEFT|Gravity.TOP;
            params.topMargin = top;
            params.leftMargin = left;
            if(top!=lastTop || left!=lastLeft){
	            v.setLayoutParams(params);
				lastX = (int) event.getRawX();
	            lastY = (int) event.getRawY();
	            long time= System.currentTimeMillis();
	            if((time-lastTime)>100){
	            	sendXY(left,top);
	            	lastTime = time;
	            }
	            lastTop = top; lastLeft = left;
            }
			break;
       	case MotionEvent.ACTION_UP:
            left = 100;//v.getLeft() + dx;  
            top = 100;//v.getTop() + dy;  
            params = (FrameLayout.LayoutParams)v.getLayoutParams();
            params.gravity = Gravity.LEFT|Gravity.TOP;
            params.topMargin = initTop;
            params.leftMargin = initLeft;
            sendXY(left,top);
            v.setLayoutParams(params);
       		break;
		}
		return true; 
	}
	
}