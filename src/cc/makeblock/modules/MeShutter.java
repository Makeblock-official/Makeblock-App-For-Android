package cc.makeblock.modules;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import cc.makeblock.makeblock.R;

@SuppressLint("NewApi")
public class MeShutter extends MeModule implements SeekBar.OnSeekBarChangeListener{
	static String devName = "shutter";
	String writeStr="";
	private EditText intevalText;
	private SeekBar timeSeekBar;
	private EditText timeText;
	private EditText startText;
	private Switch startSwitch;
	private Timer mTimer;
	private TimerTask mTask;
	private int mIndex;
	private Handler uiHandler;
	public MeShutter(int port, int slot) {
		super(devName, MeModule.DEV_SHUTTER, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_shutter;
		imageId = R.drawable.shutter;
		shouldSelectSlot = true;
		mTimer = new Timer();
		setEnable(null);
	}
	
	public MeShutter(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_shutter;
		imageId = R.drawable.shutter;
		shouldSelectSlot = true;
		mTimer = new Timer();
		mTask = new TimerTask(){public void run () {doShutter();}};
		setEnable(null);
	}
	
	public void setEnable(Handler handler){
		mHandler = handler;
		if(view==null){
			return;
		}
		 uiHandler = new Handler(){  
			  
	            @Override  
	            public void handleMessage(Message msg) {  
	                switch (msg.what) {  
	                case 2:  
	    				startSwitch.setChecked(false); 
	                    break;  
	                default:  
	                    break;  
	                }  
	            }  
	        };  
		intevalText = (EditText)view.findViewById(R.id.intervalText);
		timeSeekBar = (SeekBar)view.findViewById(R.id.timeSeekBar);
		timeSeekBar.setOnSeekBarChangeListener(this);
//		timeSeekBar.setProgress(5);
		timeText = (EditText)view.findViewById(R.id.timeText);
		startText = (EditText)view.findViewById(R.id.startText);
		startSwitch = (Switch)view.findViewById(R.id.startSwitch);
		OnCheckedChangeListener listener = new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				mIndex = 0;
				Log.d("mb", startText.getText().toString()+":"+intevalText.getText().toString());
				if(arg1){
					if(mTimer!=null){
						long interval = Integer.parseInt(startText.getText().toString())*1000;
						long during = Integer.parseInt(intevalText.getText().toString())*1000;
						Log.d("mb", "interval:"+interval+" - during:"+during);
						mTask = new TimerTask(){public void run () {doShutter();}};
						mTimer.cancel();
						mTimer = new Timer();
						mTimer.schedule(mTask, interval,during);
					}
				}else{
					if(mTimer!=null)
						mTimer.cancel();
				}
			}
		};
		startSwitch.setOnCheckedChangeListener(listener);
		return;
	}
	private void doShutter(){
		mIndex++;
		byte[] wr = buildWrite(type, port, slot, 1);
		mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
		
		if(Integer.parseInt(timeText.getText().toString())>0){
			if(mIndex>=Integer.parseInt(timeText.getText().toString())){
				Log.d("mb", "finish");
				if(mTimer!=null)
					mTimer.cancel();
				 if (uiHandler != null) {  
			            Message message = Message.obtain(uiHandler, 2);     
			            uiHandler.sendMessage(message);   
			        }  
			}
		}
	}
	public void setDisable(){
		
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int value =progress;
		timeText.setText(""+value);
//		if(valueTxt!=null){
//			valueTxt.setText(value+"");
//			byte[] wr = buildWrite(type, port, slot, value);
//			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
//		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//slider.setProgress(0);
	}
	
	//@"servorun(%d,%c)\n",servoCount,variableChar
	public String getScriptRun(String var){
//		varReg = var;
		String code = "shutter()\n";
		return code;
	}
	
	//@"servoattach(%@,%@,%d)\n",portStr,slotStr,servoCount]
	public String getScriptSetup(){
//		servoIndex = servoCount++;
		String code = "shutter()\n";
		return code;
	}

}
