package cc.makeblock.modules;

import org.json.JSONObject;

import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;
import cc.makeblock.makeblock.R;

public class MeDcMotor extends MeModule implements SeekBar.OnSeekBarChangeListener{
	static String devName = "dcmotor";
	SeekBar slider;
	TextView valueTxt;
	private Handler mStopHandler=new Handler();
	private Runnable mStopRunnable=new Runnable() {
	    @Override
	    public void run() {
			byte[] wr = buildWrite(type, port, slot, 0);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
	    }
	};
	public MeDcMotor(int port, int slot) {
		super(devName, MeModule.DEV_DCMOTOR, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_slider_view;
		imageId = R.drawable.motor;
	}
	
	public MeDcMotor(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_slider_view;
		imageId = R.drawable.motor;
	}
	
	public void setEnable(Handler handler){
		mHandler = handler;
		valueTxt = (TextView) view.findViewById(R.id.slideBarValue);
		slider = (SeekBar) view.findViewById(R.id.sliderBar);
		slider.setOnSeekBarChangeListener(this);
		slider.setProgress(256);
		return;
	}
	
	public void setDisable(){
		slider = (SeekBar) view.findViewById(R.id.sliderBar);
		slider.setOnSeekBarChangeListener(null);
	}

	long ctime=System.currentTimeMillis();

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int value = progress-256;
		if(valueTxt!=null){

			if(System.currentTimeMillis()-ctime>100){
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
		slider.setProgress(256);
	}
	
	//dcrun(%@,%c)
	public String getScriptRun(String var){
		varReg = var;
		String code = "dcrun("+getPortString(port)+","+var+")\n";
		return code;
	}


}
