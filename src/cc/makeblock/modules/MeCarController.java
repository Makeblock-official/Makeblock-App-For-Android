package cc.makeblock.modules;

import java.util.Timer;

import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import cc.makeblock.makeblock.MeDevice;
import cc.makeblock.makeblock.R;

public class MeCarController extends MeModule{
	static String devName = "carcontroller";
	private ImageButton mLeftUpButton;
	private ImageButton mLeftDownButton;
	private ImageButton mRightUpButton;
	private ImageButton mRightDownButton;
	private ImageButton mLeftButton;
	private ImageButton mDownButton;
	private ImageButton mRightButton;
	private ImageButton mUpButton;
	private ImageButton mSpeedButton;
	private TextView mSpeedLabel;
	private int motorSpeed = 180;
	private Handler mStopHandler=new Handler();
	private Runnable mStopRunnable=new Runnable() {
	    @Override
	    public void run() {
			byte[] wr = buildJoystickWrite(DEV_JOYSTICK, 0, 0);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
	    }
	};
	public MeCarController(int port, int slot) {
		super(devName, MeModule.DEV_DCMOTOR, port, slot);
		viewLayout = R.layout.dev_car_controller;
		this.scale = 1.33f;
	}
	
	public MeCarController(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_car_controller;
		this.scale = 1.33f;
	}

	long ctime=System.currentTimeMillis();
	public void setEnable(Handler handler){
		mHandler = handler;
		mLeftUpButton = (ImageButton)view.findViewById(R.id.leftUpButton);
		mRightUpButton = (ImageButton)view.findViewById(R.id.rightUpButton);
		mLeftDownButton = (ImageButton)view.findViewById(R.id.leftDownButton);
		mRightDownButton = (ImageButton)view.findViewById(R.id.rightDownButton);
		mLeftButton = (ImageButton)view.findViewById(R.id.leftButton);
		mRightButton = (ImageButton)view.findViewById(R.id.rightButton);
		mUpButton = (ImageButton)view.findViewById(R.id.upButton);
		mDownButton = (ImageButton)view.findViewById(R.id.downButton);
		mSpeedButton = (ImageButton)view.findViewById(R.id.speedButton);
		mLeftUpButton.setClickable(true);
		mRightUpButton.setClickable(true);
		mLeftDownButton.setClickable(true);
		mRightDownButton.setClickable(true);
		mLeftUpButton.setEnabled(true);
		mRightUpButton.setEnabled(true);
		mLeftDownButton.setEnabled(true);
		mRightDownButton.setEnabled(true);
		View.OnTouchListener touchListener = new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent evt) {
				// TODO Auto-generated method stub
				if(evt.getAction()==MotionEvent.ACTION_UP){
					MeDevice.sharedManager().manualMode = false;
					byte[] wr = buildJoystickWrite(DEV_JOYSTICK, 0, 0);
					mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
					mStopHandler.postDelayed(mStopRunnable, 150);
					return true;
				}else if(evt.getAction()==MotionEvent.ACTION_DOWN){
					if(System.currentTimeMillis()-ctime>80){
						ctime=System.currentTimeMillis();
						MeDevice.sharedManager().manualMode = true;
						if(v.equals(mLeftUpButton)){
							byte[] wr = buildJoystickWrite(DEV_JOYSTICK, motorSpeed/2,motorSpeed);
							mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
						}else if(v.equals(mLeftDownButton)){
							byte[] wr = buildJoystickWrite(DEV_JOYSTICK, -motorSpeed/2, -motorSpeed);
							mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
						}else if(v.equals(mRightUpButton)){
							byte[] wr = buildJoystickWrite(DEV_JOYSTICK, motorSpeed, motorSpeed/2);
							mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
						}else if(v.equals(mRightDownButton)){
							byte[] wr = buildJoystickWrite(DEV_JOYSTICK, -motorSpeed, -motorSpeed/2);
							mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
						}else if(v.equals(mLeftButton)){
							byte[] wr = buildJoystickWrite(DEV_JOYSTICK, -motorSpeed, motorSpeed);
							mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
						}else if(v.equals(mRightButton)){
							byte[] wr = buildJoystickWrite(DEV_JOYSTICK, motorSpeed, -motorSpeed);
							mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
						}else if(v.equals(mUpButton)){
							byte[] wr = buildJoystickWrite(DEV_JOYSTICK, motorSpeed, motorSpeed);
							mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
						}else if(v.equals(mDownButton)){
							byte[] wr = buildJoystickWrite(DEV_JOYSTICK, -motorSpeed, -motorSpeed);
							mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
						}
					}
				}
				return false;
			}
		};
		mLeftButton.setClickable(true);
		mRightButton.setClickable(true);
		mUpButton.setClickable(true);
		mDownButton.setClickable(true);
		mLeftButton.setEnabled(true);
		mRightButton.setEnabled(true);
		mUpButton.setEnabled(true);
		mDownButton.setEnabled(true);

		mLeftButton.setOnTouchListener(touchListener);
		mRightButton.setOnTouchListener(touchListener);
		mUpButton.setOnTouchListener(touchListener);
		mDownButton.setOnTouchListener(touchListener);
		mLeftUpButton.setOnTouchListener(touchListener);
		mRightUpButton.setOnTouchListener(touchListener);
		mLeftDownButton.setOnTouchListener(touchListener);
		mRightDownButton.setOnTouchListener(touchListener);
		
		mSpeedButton.setClickable(true);
		mSpeedButton.setEnabled(true);
		mSpeedButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent evt) {
				// TODO Auto-generated method stub
				int x = (int) evt.getX();
			    int y = (int) evt.getY();
			    //Log.d("mb", ""+x+":"+y);
			    if(y<48){
			    	motorSpeed+=4;
			    }else{
			    	motorSpeed-=4;
			    }
			    
			    motorSpeed = motorSpeed>255?255:(motorSpeed<0?0:motorSpeed);
			    MeDevice.sharedManager().motorSpeed = motorSpeed;
			    mSpeedLabel = (TextView)view.findViewById(R.id.speedLabel);
			    mSpeedLabel.setText("Speed:"+motorSpeed);
				return false;
			}
		});
		mSpeedButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("mb", "speed");
			}
		});
		return;
	}
	
	public void setDisable(){
		mLeftUpButton = (ImageButton)view.findViewById(R.id.leftUpButton);
		mRightUpButton = (ImageButton)view.findViewById(R.id.rightUpButton);
		mLeftDownButton = (ImageButton)view.findViewById(R.id.leftDownButton);
		mRightDownButton = (ImageButton)view.findViewById(R.id.rightDownButton);
		mLeftButton = (ImageButton)view.findViewById(R.id.leftButton);
		mRightButton = (ImageButton)view.findViewById(R.id.rightButton);
		mUpButton = (ImageButton)view.findViewById(R.id.upButton);
		mDownButton = (ImageButton)view.findViewById(R.id.downButton);
		mSpeedButton = (ImageButton)view.findViewById(R.id.speedButton);
		mLeftUpButton.setOnClickListener(null);
		mRightUpButton.setOnClickListener(null);
		mLeftDownButton.setOnClickListener(null);
		mRightDownButton.setOnClickListener(null);
		mLeftButton.setOnClickListener(null);
		mRightButton.setOnClickListener(null);
		mUpButton.setOnClickListener(null);
		mDownButton.setOnClickListener(null);
		mSpeedButton.setOnClickListener(null);
		mLeftUpButton.setClickable(false);
		mRightUpButton.setClickable(false);
		mLeftDownButton.setClickable(false);
		mRightDownButton.setClickable(false);
		mLeftUpButton.setEnabled(false);
		mRightUpButton.setEnabled(false);
		mLeftDownButton.setEnabled(false);
		mRightDownButton.setEnabled(false);
		mLeftButton.setClickable(false);
		mRightButton.setClickable(false);
		mUpButton.setClickable(false);
		mDownButton.setClickable(false);
		mLeftButton.setEnabled(false);
		mRightButton.setEnabled(false);
		mUpButton.setEnabled(false);
		mDownButton.setEnabled(false);
		mSpeedButton.setClickable(false);
		mSpeedButton.setEnabled(false);
	}
}
