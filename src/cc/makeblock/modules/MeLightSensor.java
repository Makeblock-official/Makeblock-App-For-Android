package cc.makeblock.modules;

import org.json.JSONObject;

import android.os.Handler;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import cc.makeblock.makeblock.R;

public class MeLightSensor extends MeModule implements OnCheckedChangeListener{
	static String devName = "lightsensor";
	CheckBox ledCheck;
	public MeLightSensor(int port, int slot) {
		super(devName, MeModule.DEV_LIGHTSENSOR, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_value_check;
		imageId = R.drawable.lightsensor;
	}
	
	public MeLightSensor(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_value_check;
		imageId = R.drawable.lightsensor;
	}

	public String getScriptRun(String var){
		varReg = var;
		String code = var+" = lightsensor("+getPortString(port)+")\n";
		return code;
	}
	
	public void setEnable(Handler handler){
		mHandler = handler;
		ledCheck = (CheckBox)view.findViewById(R.id.ledCheck);
		ledCheck.setEnabled(true);
		ledCheck.setOnCheckedChangeListener(this);
		if(ledCheck.isChecked()){
			byte[] wr = buildWrite(type, port, slot, 1);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
		}else{
			byte[] wr = buildWrite(type, port, slot, 0);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
		}
	}
	
	public void setDisable(){
		ledCheck = (CheckBox)view.findViewById(R.id.ledCheck);
		ledCheck.setEnabled(false);
	}
	
	public byte[] getQuery(int index){
		byte[] query = buildQuery(type, port, slot, index);
		return query;
	}
	
	public void setEchoValue(String value){
		TextView txt =  (TextView)view.findViewById(R.id.textValue);
		txt.setText(value+" lux");
		return;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			byte[] wr = buildWrite(type, port, slot, 1);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
		}else{
			byte[] wr = buildWrite(type, port, slot, 0);
			mHandler.obtainMessage(MSG_VALUECHANGED,wr).sendToTarget();
		}
		
	}
}
