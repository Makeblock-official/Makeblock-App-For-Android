package cc.makeblock.modules;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import cc.makeblock.makeblock.R;

public class MeDigiSeg extends MeModule implements TextWatcher,OnCheckedChangeListener{
	static String devName = "digiseg";
	SeekBar slider;
	TextView valueTxt;
	EditText ed;
	CheckBox synTime;
	Timer mTimer;
	TimerTask mTimerTask;
	
	String writeStr="";
	public MeDigiSeg(int port, int slot) {
		super(devName, MeModule.DEV_SEVSEG, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_edit_view;
		imageId = R.drawable.sevseg;
	}
	
	public MeDigiSeg(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_edit_view;
		imageId = R.drawable.sevseg;
	}
	
	public void setEnable(Handler handler){
		mHandler = handler;
		ed = (EditText) view.findViewById(R.id.editTxt);
		ed.setEnabled(true);
		ed.addTextChangedListener(this);
		synTime = (CheckBox)view.findViewById(R.id.syncTime);
		synTime.setOnCheckedChangeListener(this);
		if(synTime.isChecked()){
			startTimer();
		}
		
		sendNumber(ed.getText().toString());
		return;
	}
	
	public void setDisable(){
		ed = (EditText) view.findViewById(R.id.editTxt);
		ed.setEnabled(false);
		ed.removeTextChangedListener(this);
		stopTimer();
	}
	
	//digiseg(%@,%c)
	public String getScriptRun(String var){
		varReg = var;
		String code = "digiseg("+getPortString(port)+","+var+")\n";
		return code;
	}
	
	void sendNumber(String str){
		if(str.equals("")) str="0";
		float number = Float.parseFloat(str);
		byte[] wr = buildWrite(type, port, slot, number);
		mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		String num = s.toString();
		sendNumber(num);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			stopTimer();
			startTimer();
		}else{			
			stopTimer();
		}
		
	}
	
	final Handler updateTime = new Handler(){
		@Override
		public void handleMessage(Message msg){
			String dts = (String)msg.obj;
			ed.setText(dts);
		}
	};
	
	void startTimer(){
		if(mTimer == null){
			mTimer = new Timer(true);
		}
		
		if(mTimerTask == null){
			mTimerTask = new TimerTask(){
				@Override
				public void run() {
					SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
					Date dt = new Date();
					String dts = sdf.format(dt);
					updateTime.obtainMessage(MSG_VALUECHANGED,dts).sendToTarget();
					sendNumber(dts);
				}
			};
		}
		
		if(mTimer!=null && mTimerTask !=null){
			mTimer.schedule(mTimerTask,1000,60*1000);
		}
		
	}
	
	void stopTimer(){
		if(mTimer!=null){
			mTimer.cancel();
			mTimer=null;
		}
		
		if(mTimerTask != null){
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}		
}
