package cc.makeblock.modules;

import org.json.JSONObject;

import android.widget.ImageView;
import cc.makeblock.makeblock.R;

public class MeLineFollower extends MeModule {
	static String devName = "linefinder";
	public MeLineFollower(int port, int slot) {
		super(devName, MeModule.DEV_LINEFOLLOWER, port, slot);
		// TODO Auto-generated constructor stub
		viewLayout = R.layout.dev_linefind_view;
		imageId = R.drawable.linefinder;
	}
	
	public MeLineFollower(JSONObject jobj) {
		super(jobj);
		viewLayout = R.layout.dev_linefind_view;
		imageId = R.drawable.linefinder;
	}

	public String getScriptRun(String var){
		varReg = var;
		String code = var+" = linefinder("+getPortString(port)+")\n";
		return code;
	}
	
	public byte[] getQuery(int index){
		byte[] query = buildQuery(type, port, slot, index);
		return query;
	}
	
	public void setEchoValue(String value){
		int v = (int) Float.parseFloat(value);
		ImageView imgL = (ImageView)view.findViewById(R.id.imgLeft);
		ImageView imgR = (ImageView)view.findViewById(R.id.imgRight);
		switch(v){
		case 0:
			imgL.setImageResource(R.drawable.line_off);
			imgR.setImageResource(R.drawable.line_off);
			break;
		case 1:
			imgL.setImageResource(R.drawable.line_on);
			imgR.setImageResource(R.drawable.line_off);
			break;
		case 2:
			imgL.setImageResource(R.drawable.line_off);
			imgR.setImageResource(R.drawable.line_on);
			break;
		case 3:
			imgL.setImageResource(R.drawable.line_on);
			imgR.setImageResource(R.drawable.line_on);
			break;
		}
		return;
	}
}
