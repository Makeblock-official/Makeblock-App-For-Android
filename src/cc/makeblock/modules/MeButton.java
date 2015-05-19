package cc.makeblock.modules;

import org.json.JSONObject;

import android.widget.TextView;
import cc.makeblock.makeblock.R;

public class MeButton extends MeModule {
	static String devName = "button";
	public MeButton(int port, int slot) {
		super(devName, MeModule.DEV_BUTTON, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_value_view;
		imageId = R.drawable.button;
	}
	
	public MeButton(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_value_view;
		imageId = R.drawable.button;
	}

	public String getScriptRun(String var){
		varReg = var;
		String code = var+" = button("+getPortString(port)+")\n";
		return code;
	}
	
	public byte[] getQuery(int index){
		// use the lightsensor type to read adc value
		byte[] query = buildQuery(DEV_LIGHTSENSOR, port, slot, index);
		return query;
	}
	
	public void setEchoValue(String value){
		float adc = Float.parseFloat(value);
		TextView txt =  (TextView)view.findViewById(R.id.textValue);
		if(adc<=5){
			txt.setText("KEY1");
		}else if(adc<=490){
			txt.setText("KEY2");
		}else if(adc<=653){
			txt.setText("KEY3");
		}else if(adc<=734){
			txt.setText("KEY4");
		}else{
			txt.setText("NONE");
		}
		return;
	}
}
