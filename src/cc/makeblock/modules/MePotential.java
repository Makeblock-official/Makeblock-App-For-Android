package cc.makeblock.modules;

import org.json.JSONObject;

import android.widget.TextView;
import cc.makeblock.makeblock.R;

public class MePotential extends MeModule {
	static String devName = "potentiometer";
	public MePotential(int port, int slot) {
		super(devName, MeModule.DEV_POTENTIALMETER, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_value_view;
		imageId = R.drawable.potentiometer;
	}
	
	public MePotential(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_value_view;
		imageId = R.drawable.potentiometer;
	}

	public String getScriptRun(String var){
		varReg = var;
		String code = var+" = potentiometer("+getPortString(port)+")\n";
		return code;
	}
	
	public byte[] getQuery(int index){
		byte[] query = buildQuery(type, port, slot, index);
		return query;
	}
	
	public void setEchoValue(String value){
		TextView txt =  (TextView)view.findViewById(R.id.textValue);
		txt.setText(value);
		return;
	}
}
