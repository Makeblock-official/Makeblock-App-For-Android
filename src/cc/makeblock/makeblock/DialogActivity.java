package cc.makeblock.makeblock;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class DialogActivity extends Activity {
	public static DialogActivity shared;
    private TextView mMsgLabel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);
        mMsgLabel = (TextView)findViewById(R.id.dialogText);
        String msg = this.getIntent().getStringExtra("msg");
        mMsgLabel.setText(msg);
        if(shared!=null){
        	shared.finish();
        }
        shared = this;
        if(msg.equals(getString(R.string.connected))){
        	Timer t = new Timer();
        	TimerTask task = new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					shared.finish();
					shared = null;
				}
			};
        	t.schedule(task, 1000);
        }
    }
}