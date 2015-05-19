package cc.makeblock.modules;

import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import cc.makeblock.makeblock.MScript;
import cc.makeblock.makeblock.MeDevice;
import cc.makeblock.makeblock.R;

public class MeUltrasonic extends MeModule {
	static String devName = "ultrasonic";
	private ToggleButton toggleBt;
	private Handler mLoopHandler = new Handler();

	public MeUltrasonic(int port, int slot) {
		super(devName, MeModule.DEV_ULTRASOINIC, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_auto_driver;
		imageId = R.drawable.ultrasonic;
	}
	
	public MeUltrasonic(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_auto_driver;
		imageId = R.drawable.ultrasonic;
	}

	public String getScriptRun(String var){
		varReg = var;
		String code = var+" = distance("+getPortString(port)+")\n";
		return code;
	}
	
	public byte[] getQuery(int index){
		byte[] query = buildQuery(type, port, slot, index);
		return query;
	}
	private int motorSpeed = 0;
	private int mBackTime = 0;
	private int mFrontTime = 0;
	private Runnable mRunnable = new Runnable() {
		public void run () {
			if(isAuto){
				mLoopHandler.postDelayed(this,100);  
				if(view!=null){
					motorSpeed = MeDevice.sharedManager().motorSpeed;
					if(!MeDevice.sharedManager().manualMode){
						if((mCurrentValue>0.0&&mCurrentValue<40)||mBackTime>0){
							if(mBackTime<5){
								mBackTime++;
								byte[] wr = buildWrite(DEV_DCMOTOR, PORT_M1, slot, -motorSpeed);
								mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
								byte[] wr2 = buildWrite(DEV_DCMOTOR, PORT_M2, slot, -motorSpeed);
								mHandler.obtainMessage(MSG_VALUECHANGED,wr2).sendToTarget();
							}else if(mBackTime<10){
								mBackTime++;
								byte[] wr = buildWrite(DEV_DCMOTOR, PORT_M1, slot, motorSpeed);
								mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
								byte[] wr2 = buildWrite(DEV_DCMOTOR, PORT_M2, slot, -motorSpeed);
								mHandler.obtainMessage(MSG_VALUECHANGED,wr2).sendToTarget();
							}else{
								mBackTime = 0;
							}
						}else{
							if(mFrontTime<10){
								byte[] wr = buildWrite(DEV_DCMOTOR, PORT_M1, slot, motorSpeed);
								mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
								byte[] wr2 = buildWrite(DEV_DCMOTOR, PORT_M2, slot, motorSpeed);
								mHandler.obtainMessage(MSG_VALUECHANGED,wr2).sendToTarget();
							}
							if(mCurrentValue==0){
								mFrontTime++;
							}else{
								mFrontTime=0;
							}
						}
					}
				}
			}
		}
	};
	private boolean isAuto = false;
	public void setEnable(Handler handler){
		mHandler = handler;
		toggleBt = (ToggleButton)view.findViewById(R.id.autoSwitch);
		OnCheckedChangeListener listener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				isAuto = arg1;
				if(isAuto == true){
					mLoopHandler.postDelayed(mRunnable,100);
				}else{
					byte[] wr = buildWrite(DEV_DCMOTOR, PORT_M1, slot, 0);
					mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
					byte[] wr2 = buildWrite(DEV_DCMOTOR, PORT_M2, slot, 0);
					mHandler.obtainMessage(MSG_VALUECHANGED,wr2).sendToTarget();
				}
			}
		};
		toggleBt.setOnCheckedChangeListener(listener);
	}
	public void setDisable(){
		toggleBt = (ToggleButton)view.findViewById(R.id.autoSwitch);
		toggleBt.setOnCheckedChangeListener(null);
	}
	private float mCurrentValue = 0.0f;
	public void setEchoValue(String value){
		TextView txt =  (TextView)view.findViewById(R.id.textValue);
		mCurrentValue = Float.parseFloat(value);
		txt.setText(""+Math.floor(Float.parseFloat(value)*10.0)/10.0+" cm");
		return;
	}
}