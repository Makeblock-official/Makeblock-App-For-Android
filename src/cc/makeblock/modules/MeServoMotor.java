package cc.makeblock.modules;

import org.json.JSONObject;

import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;
import cc.makeblock.makeblock.R;

public class MeServoMotor extends MeModule implements SeekBar.OnSeekBarChangeListener{
	static String devName = "servo";
	SeekBar slider;
	TextView valueTxt;
	String writeStr="";
	static int servoCount=0;
	int servoIndex;
	
	public MeServoMotor(int port, int slot) {
		super(devName, MeModule.DEV_SERVO, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_slider_view;
		imageId = R.drawable.servo;
		shouldSelectSlot = true;
	}
	
	public MeServoMotor(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_slider_view;
		imageId = R.drawable.servo;
		shouldSelectSlot = true;
	}
	
	public void setEnable(Handler handler){
		mHandler = handler;
		valueTxt = (TextView) view.findViewById(R.id.slideBarValue);
		slider = (SeekBar) view.findViewById(R.id.sliderBar);
		slider.setOnSeekBarChangeListener(this);
		slider.setProgress(0);
		return;
	}
	
	public void setDisable(){
		slider = (SeekBar) view.findViewById(R.id.sliderBar);
		slider.setOnSeekBarChangeListener(null);
		servoCount = 0;
	}

	long ctime=System.currentTimeMillis();

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int value = (int)((float)progress/512*180);
		if(valueTxt!=null){

			if(System.currentTimeMillis()-ctime>80){
				ctime=System.currentTimeMillis();
				valueTxt.setText(value+"");
				byte[] wr = buildWrite(type, port, slot, value);
				mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
			}
		}
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
		varReg = var;
		String code = "servorun("+servoIndex+","+var+")\n";
		return code;
	}
	
	//@"servoattach(%@,%@,%d)\n",portStr,slotStr,servoCount]
	public String getScriptSetup(){
		servoIndex = servoCount++;
		String code = "servoattach("+getPortString(port)+","+getSlotString(slot)+","+servoIndex+")\n";
		return code;
	}

}
