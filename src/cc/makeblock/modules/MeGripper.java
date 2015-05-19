package cc.makeblock.modules;

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

public class MeGripper extends MeModule{
	static String devName = "carcontroller";
	private ImageButton mLeftButton;
	private ImageButton mRightButton;
	private ImageButton mSpeedButton;
	private TextView mSpeedLabel;
	private TextView mPortLabel;
	private int motorSpeed = 100;

	private Handler mStopHandler=new Handler();
	private Runnable mStopRunnable=new Runnable() {
	    @Override
	    public void run() {
			byte[] wr = buildWrite(DEV_DCMOTOR, port, slot, 0);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
	    }
	};
	public MeGripper(int port, int slot) {
		super(devName, MeModule.DEV_DCMOTOR, port, slot);
		viewLayout = R.layout.dev_gripper_controller;
		this.scale = 1.33f;
	}
	
	public MeGripper(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_gripper_controller;
		this.scale = 1.33f;
	}
	
	public void setEnable(Handler handler){
		mHandler = handler;
	    mSpeedLabel = (TextView)view.findViewById(R.id.speedLabel);
		mLeftButton = (ImageButton)view.findViewById(R.id.leftButton);
		mRightButton = (ImageButton)view.findViewById(R.id.rightButton);
		mSpeedButton = (ImageButton)view.findViewById(R.id.speedButton);
		mPortLabel = (TextView)view.findViewById(R.id.textPort);
	    mPortLabel.setText((port>8?("M"+(port-8)):("PORT "+port)));
		View.OnTouchListener touchListener = new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent evt) {
				// TODO Auto-generated method stub
				if(evt.getAction()==MotionEvent.ACTION_UP){
					//MeDevice.sharedManager().manualMode = false;
					byte[] wr = buildWrite(DEV_DCMOTOR, port, slot, 0);
					mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
					mStopHandler.postDelayed(mStopRunnable, 150);
					return true;
				}else if(evt.getAction()==MotionEvent.ACTION_DOWN){
					//MeDevice.sharedManager().manualMode = true;
					Log.d("mb", "port:"+port);
					if(v.equals(mLeftButton)){
						byte[] wr = buildWrite(DEV_DCMOTOR, port, slot, -motorSpeed);
						mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
					}else if(v.equals(mRightButton)){
						byte[] wr = buildWrite(DEV_DCMOTOR, port, slot, motorSpeed);
						mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
					}
				}
				return false;
			}
		};
		mLeftButton.setClickable(true);
		mRightButton.setClickable(true);
		mLeftButton.setEnabled(true);
		mRightButton.setEnabled(true);


		mLeftButton.setOnTouchListener(touchListener);
		mRightButton.setOnTouchListener(touchListener);

	    mSpeedLabel.setText("Speed:"+motorSpeed);
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
		mLeftButton = (ImageButton)view.findViewById(R.id.leftButton);
		mRightButton = (ImageButton)view.findViewById(R.id.rightButton);
		mSpeedButton = (ImageButton)view.findViewById(R.id.speedButton);
	    mSpeedLabel = (TextView)view.findViewById(R.id.speedLabel);
	    mPortLabel = (TextView)view.findViewById(R.id.textPort);
		mLeftButton.setClickable(false);
		mRightButton.setClickable(false);
		mLeftButton.setEnabled(false);
		mRightButton.setEnabled(false);
		mSpeedButton.setClickable(false);
		mSpeedButton.setEnabled(false);
	    mSpeedLabel.setText("Speed:"+motorSpeed);
	    mPortLabel.setText((port>8?("M"+(port-8)):("PORT "+port)));
	}
}
