package cc.makeblock.makeblock;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public final static String ITEM_TITLE = "title";  
    public final static String ITEM_CAPTION = "caption"; 
	public final static String ITEM_TITLE_1 = "title1";  
    public final static String ITEM_INDEX_1 = "index1"; 
    public final static String ITEM_IMAGE_1 = "image1"; 
	public final static String ITEM_TITLE_2 = "title2";  
    public final static String ITEM_INDEX_2 = "index2"; 
    public final static String ITEM_IMAGE_2 = "image2"; 
	public final static String ITEM_TITLE_3 = "title3";  
    public final static String ITEM_INDEX_3 = "index3"; 
    public final static String ITEM_IMAGE_3 = "image3"; 
    public static float screenWidth;
    public static float screenHeight;
    private boolean isexit = false;
    private boolean hastask = false;
    private boolean ispause = false;
    private Timer texit = new Timer();
    private TimerTask task;
    private Intent serviceIntent = new Intent("cc.makeblock.makeblock");
    LocalLayout layouts;
    
	ListView historyListView;
	ArrayList<MeLayout> historyList;
	ArrayList<MeLayout> exampleList;
	Map<String,String>localizedStrings = new HashMap<String,String>();
	SeparatedListAdapter adapter;
	Bluetooth blt;
	public Map<String, ?> createSimpleItem(String title, String caption) {  
		Map<String, String> item = new HashMap<String, String>();  
        item.put(ITEM_TITLE, title);  
        item.put(ITEM_CAPTION, caption);
        return item;
	}
	public ComplexItem createComplexItem(String title1,int imageId1,int index1,String title2,int imageId2,int index2,String title3,int imageId3,int index3) {  
		ComplexItem item = new ComplexItem();  
        item.put(ITEM_TITLE_1, title1);  
        item.put(ITEM_IMAGE_1, imageId1);
        item.put(ITEM_INDEX_1, index1);
        item.put(ITEM_TITLE_2, title2);  
        item.put(ITEM_IMAGE_2, imageId2);
        item.put(ITEM_INDEX_2, index2);
        item.put(ITEM_TITLE_3, title3);  
        item.put(ITEM_IMAGE_3, imageId3);
        item.put(ITEM_INDEX_3, index3);
        return item;
    }
	
	 // SectionHeaders
    String[] Sections;
	
    private void setupViews() {
        // Create and Initialize the ListView Adapter
        initializeAdapter();
        // Get a reference to the ListView holder
        historyListView = (ListView) this.findViewById(R.id.centerList);
        historyListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try{
					Log.i("listview", "item "+position+" clicked");
					MeLayout layout = historyList.get(position-1);
					pushToLayout(layout);
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
			}
        });
        // Set the adapter on the ListView holder
        historyListView.setAdapter(adapter);
        // test read hex
        /*
        Resources res = getResources();
        InputStream in_s = res.openRawResource(R.raw.firmware);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in_s));
        try {
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				Log.d("txt", line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

    }

    private void initializeAdapter() {
        adapter = new SeparatedListAdapter(this);
        
        List<Map<String, ?>> history = new LinkedList<Map<String, ?>>();  
        for(MeLayout layout : historyList){
        	history.add(createSimpleItem(layout.name, layout.updateTime));
        }

        SimpleAdapter listHistoryAdapter = new SimpleAdapter(this, history,  
                    R.layout.list_simple,  
                    new String[] { ITEM_TITLE, ITEM_CAPTION }, 
                    new int[] {R.id.list_simple_title, R.id.list_simple_caption });
        adapter.addSection(Sections[0],listHistoryAdapter);

        List<ComplexItem> demos = new LinkedList<ComplexItem>();  
        demos.add(createComplexItem(getString(R.string.distancemeasure),R.drawable.distanceicon,1,getString(R.string.temperaturemeasure),R.drawable.temperatureicon,2,getString(R.string.rgbcontrol),R.drawable.rgbicon,3));  
        demos.add(createComplexItem(getString(R.string.robottank),R.drawable.car_controller,4,getString(R.string.roboticarmtank),R.drawable.robotarmcar,5,getString(R.string.balllauncher),R.drawable.balllauncher,6)); 
        demos.add(createComplexItem(getString(R.string.drinkcar),R.drawable.beercar,7,"",-1,-1,"",-1,-1)); 
        
        ComplexListAdapter listExamplesAdapter = new ComplexListAdapter(this,demos);
        listExamplesAdapter.delegate = this;
        adapter.addSection(Sections[1],listExamplesAdapter);
    }
	public void openExample(int indexId){
		MeLayout layout = exampleList.get(indexId-1);
		pushToLayout(layout);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Sections = new String[]{getString(R.string.history),getString(R.string.examples)};
		
		adapter = new SeparatedListAdapter(this);
		layouts = new LocalLayout(this);
		
		startService(serviceIntent);
        task = new TimerTask() {
            public void run() {
                isexit = false;
                hastask = true;
            }
        };

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        MeDevice.sharedManager().setWidth((int) screenWidth);
        MeDevice.sharedManager().setHeight((int) screenHeight);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
        }else{
        	Toast.makeText(this, "BLE is supported", Toast.LENGTH_SHORT).show();
        }
	}

    @Override
    public void onStart(){
        ispause = false;
        super.onStart();
    }
    @Override
    public void onPause(){
        ispause = true;
        //Debug.log("pause app");
        super.onPause();
        MobclickAgent.onPause(this);
    }
	@Override
	protected void onResume(){
		super.onResume();
		readLocalLayout();
		setupViews();
		MobclickAgent.onResume(this);
	}
	
	void readLocalLayout(){
		//layouts.initLocalLayout();
		String[] filelist = layouts.fileList();
		historyList = new ArrayList<MeLayout>();
		for(String filename : filelist){
			if(!filename.contains(".json"))
				continue;
			
			String jsonstr;
			try {
				jsonstr = layouts.FileRead(filename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			JSONObject json = layouts.toJson(jsonstr);
			MeLayout layout = new MeLayout(json);
			historyList.add(layout);
		}
		exampleList = new ArrayList<MeLayout>();

        localizedStrings.put("Distance Measure", Integer.toString(R.string.distancemeasure));
        localizedStrings.put("Temperature Measure", Integer.toString(R.string.temperaturemeasure));
        localizedStrings.put("RGB Control", Integer.toString(R.string.rgbcontrol));
        localizedStrings.put("General Controls", Integer.toString(R.string.generalcontrols));
        localizedStrings.put("Robot Tank", Integer.toString(R.string.robottank));
        localizedStrings.put("Robotic Arm Tank", Integer.toString(R.string.roboticarmtank));
        localizedStrings.put("Ball Launcher", Integer.toString(R.string.balllauncher));
        localizedStrings.put("Drink Car", Integer.toString(R.string.drinkcar));
        
		for(int i=1;i<8;i++){
			String jsonstr;
			try {
				jsonstr = readTextFile(getAssets().open(""+i+".json")); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			JSONObject json = layouts.toJson(jsonstr);
			MeLayout layout = new MeLayout(json);
			try{
//				if(localizedStrings.get(json.getString("name"))!=null){
//					Log.d("mb","name="+getString(Integer.parseInt(localizedStrings.get(json.getString("name")))));
//				}else{
//					Log.d("mb","s="+json.getString("name"));
//				}
				layout.setName(getString(Integer.parseInt(localizedStrings.get(json.getString("name")))));
			}catch(JSONException e){
				
			}
			exampleList.add(layout);
		}
	}
	private String readTextFile(InputStream inputStream) { 

		  ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
		  byte buf[] = new byte[1024]; 
		  int len; 
		  try { 
		   while ((len = inputStream.read(buf)) != -1) { 
		    outputStream.write(buf, 0, len); 
		   } 
		   outputStream.close(); 
		   inputStream.close(); 
		  } catch (IOException e) { 
			  
		  } 
		  return outputStream.toString(); 
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    // action with ID action_refresh was selected
	    case R.id.action_new:
	    	final EditText editText = new EditText(this); 
	    	new AlertDialog.Builder(this).setTitle(getString(R.string.input_layout_name)).setView(editText).setPositiveButton(getString(R.string.ok),
	    			new DialogInterface.OnClickListener() {                
	    			    @Override  
	    			    public void onClick(DialogInterface dialog, int which) {  
	    			        // TODO Auto-generated method stub  
	    			    	String name = editText.getText().toString();
	    			    	MeLayout newlayout = new MeLayout(name);
	    			    	historyList.add(0, newlayout);
	    			    	setupViews();
	    			    	try {
								layouts.FileSave(newlayout.name+".json", newlayout.toString());
							} catch (IOException e) {
								e.printStackTrace();
							}
	    			    	pushToLayout(newlayout);
	    			    }  
	    			}).setNegativeButton(getString(R.string.cancel), null).show();
	      break;
	    case R.id.action_help:
	    	Intent intentHelp = new Intent(MainActivity.this,WebviewActivity.class);
	    	intentHelp.putExtra("url", getString(R.string.url_help));
	    	startActivity(intentHelp);
	    	break;
	    case R.id.action_about:
	    	Intent intentAbout = new Intent(MainActivity.this,WebviewActivity.class);
	    	intentAbout.putExtra("url", getString(R.string.url_about));
	    	startActivity(intentAbout);
	    	break;
	    case R.id.action_feedback:
	    	Intent intentFeedback = new Intent(MainActivity.this,WebviewActivity.class);
	    	intentFeedback.putExtra("url", getString(R.string.url_feedback));
	    	startActivity(intentFeedback);
	    	break;
	    default:
	      break;
	    }

	    return true;
	  } 
	
	void pushToLayout(MeLayout layout){
		Intent intent= new Intent(this, LayoutView.class); 
		intent.putExtra("layout", layout.toString()); // use json string between activities
		startActivity(intent);
	}
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            if(isexit == false){
                isexit = true;
                Toast.makeText(getApplicationContext(), getString(R.string.pressbackagain), Toast.LENGTH_SHORT).show();
                if(!hastask) {
                    texit.schedule(task, 2000);
                }
            }else{
            	BluetoothAdapter.getDefaultAdapter().disable();
                stopService(serviceIntent);
                finish();
                System.exit(0);
            }
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
}
