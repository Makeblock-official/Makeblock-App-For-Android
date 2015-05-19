package cc.makeblock.makeblock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

@SuppressLint("NewApi")
public class Bluetooth extends Service{
	static final String dbg = "bluetooth";
	static final int MSG_CONNECTED=1;
	static final int MSG_DISCONNECTED=2;
	static final int MSG_RX = 3;
	static final int MSG_FOUNDDEVICE = 4;
	static final int MSG_CONNECT_FAIL = 5;
	static final int MSG_DISCOVERY_FINISHED = 6;
	static final int MSG_RX_FIRMUPLOAD = 7;
    static final int REQUEST_CONNECT_DEVICE = 1;
    static final int REQUEST_ENABLE_BT = 2;
    
	BluetoothAdapter mBTAdapter;
	static final String BTName = "BTMakeblock";
	static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
	BluetoothDevice connDev;
	ConnectThread mConnectThread;
	ConnectedThread mConnectedThread;
	ArrayList<BluetoothDevice> btDevices;
	ArrayList<BluetoothDevice> prDevices; // paired bt devices
	//public ArrayAdapter<String> devAdapter;
	Handler mHandler;
	static final int MODE_LINE = 0;
	static final int MODE_FORWARD = 1;
	public int commMode = MODE_LINE;
	
	private static Bluetooth _instance;

	public static Bluetooth sharedManager() {
		if (_instance == null) {
			_instance = new Bluetooth();
		}
		return _instance;

	}
	
	public Bluetooth(){
		// bluetooth classic
		btDevices = new ArrayList<BluetoothDevice>();
		prDevices = new ArrayList<BluetoothDevice>(); 
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBTAdapter==null){
			Log.i(dbg, "blue tooth not support");
		}
		
	}
	
	@Override
	public void onCreate() {
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mBTDevDiscover,filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mBTDevDiscover,filter);	
		_instance = this;
		if(!mBTAdapter.isEnabled()){
			mBTAdapter.enable();
		}else{
			startDiscovery();
		}
	}
	
	@Override
	public void onDestroy() {
		// Debug.log("onDestroy");
		unregisterReceiver(mBTDevDiscover);
	}
	
	public void startDiscovery(){
		mBTAdapter.startDiscovery();
	}
	public boolean isDiscovery(){
		return mBTAdapter.isDiscovering();
	}
	public boolean isEnabled(){
		return mBTAdapter.isEnabled();
	}
	
	private class ConnectThread extends Thread{
		private BluetoothSocket mmSocket = null;
		private BluetoothDevice mmDevice = null;
		
		public ConnectThread(BluetoothDevice device) 
		{
			mmDevice = device;
			
		}
		public void setBluetoothPairingPin(BluetoothDevice device)
		{
		    byte[] pinBytes = ("0000").getBytes();
		    try {
		          Log.d("mb", "Try to set the PIN");
		          Method m = device.getClass().getMethod("setPin", byte[].class);
		          m.invoke(device, pinBytes);
		          Log.d("mb", "Success to add the PIN.");
		          try {
		                device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
		            Log.d("mb", "Success to setPairingConfirmation.");
		        } catch (Exception e) {
		            // TODO Auto-generated catch block
		            Log.e("mb", e.getMessage());
		            e.printStackTrace();
		        } 
		    } catch (Exception e) {
		      Log.e("mb", e.getMessage());
		      e.printStackTrace();
		    }
		}
		public void run(){
			// stop discovery, otherwise the pair window won't popup
			mBTAdapter.cancelDiscovery();
			BluetoothSocket tmp = null;
			try {
				
				
				mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
				mmSocket.connect();
			} catch (IOException e) {
				try {
					/*if(mmDevice.getBondState()==BluetoothDevice.BOND_NONE){
						setBluetoothPairingPin(mmDevice);
						Method createBondMethod = mmDevice.getClass().getMethod("createBond"); 
						createBondMethod.invoke(mmDevice);
//						setBluetoothPairingPin(mmDevice);
			        }else{
//						Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//						tmp = (BluetoothSocket)m.invoke(device, Integer.valueOf(1));
						tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
			        }*/
					//tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
					Method m = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
					Object[] params = new Object[] {Integer.valueOf(1)};
					mmSocket = (BluetoothSocket)m.invoke(mmDevice, params);
					mmSocket.connect();
				} catch (IOException err) {
					Log.d("mb", "connect:"+err.getMessage());
					e.printStackTrace();
					try {
						mmSocket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					if(mHandler!=null){
						Message msg = mHandler.obtainMessage(MSG_CONNECT_FAIL);
						mHandler.sendMessage(msg);
					}
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				return;
			}
			// start connection manager in another thread
			bluetoothConnected(mmDevice,mmSocket);
		}
		
		public void cancel(){
			try {
				mmSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private class ConnectedThread extends Thread{
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private List<Byte> mRx;
		public boolean txBusy; 
		
		public ConnectedThread(BluetoothSocket socket){
			mmSocket = socket;
			mRx = new ArrayList<Byte>();
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}
		
		
		public void run(){
			byte[] buffer = new byte[1024];
			int bytes;
			
			while(true){
				try {
					bytes = mmInStream.read(buffer);
					if (bytes > 0) {
						for (int i = 0; i < bytes; i++) {
							Byte c = buffer[i];
							mRx.add(c);
							// line end or bootloader end
							if ((c == 0x0a && commMode==MODE_LINE) || (c==0x10 && commMode==MODE_FORWARD)) {
								// TODO: post msg to UI
								//write(mReceiveString.getBytes());
								Byte[] rxbtyes = mRx.toArray(new Byte[mRx.size()]);
								///*
								String hexStr="";
								int[] buf = new int[mRx.size()];
								for(int i1=0;i1<rxbtyes.length;i1++){
									hexStr+=String.format("%02X ", rxbtyes[i1]);
									buf[i1] = rxbtyes[i1];
								}
								Log.i("mb", "rx:"+hexStr);
								//*/
								mHandler.obtainMessage(MSG_RX,buf).sendToTarget();
								mRx.clear();
							}
						}
					}
				} catch (IOException e) {
					Log.i(dbg, "disconnected");
					connDev = null;
					if(mHandler!=null){
						Message msg = mHandler.obtainMessage(MSG_DISCONNECTED);
						mHandler.sendMessage(msg);
					}
					break;
				}
			}
		}
		
		public void write(byte[] bytes){
			try {
				txBusy=true;
				mmOutStream.write(bytes);
				mmOutStream.flush();
				txBusy=false;
			} catch (IOException e) {
				Log.e(dbg, "Exception during write", e);
			}
		}
		
		public void cancel(){
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(dbg, "Exception during cancel", e);
			}
		}
	}
	
	
	public void devListClear(){
		btDevices.clear();
		// don't forget the connecting device
		if(connDev!=null){
			btDevices.add(connDev);

		}
	}
	
	final BroadcastReceiver mBTDevDiscover = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("mb", "broadcast:"+action);
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//Log.d("mb", "bluetooth found:"+device.getName()+" "+device.getAddress()+" "+device.getBondState()+" "+BluetoothDevice.BOND_NONE+" "+BluetoothDevice.BOND_BONDED);
				if(btDevices.indexOf(device)==-1){
					btDevices.add(device);
					if(mHandler!=null){
						Message msg = mHandler.obtainMessage(MSG_FOUNDDEVICE);
						mHandler.sendMessage(msg);
					}
				}
				//bluetoothConnect(device);
			}else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

				if(mHandler!=null){
					Message msg = mHandler.obtainMessage(MSG_DISCOVERY_FINISHED);
					mHandler.sendMessage(msg);
				}
				Log.i(dbg,"bluetooth discover finished");
			}else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
				Log.i(dbg,"bluetooth ACTION_STATE_CHANGED:"+mBTAdapter.isEnabled());
			}else if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					//setBluetoothPairingPin(device);
				}
			}
		}		
	};
	
	public List<String> getPairedList(){
        List<String> data = new ArrayList<String>();
        Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
        prDevices.clear();
		if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                prDevices.add(device);
            }
        }
        for(BluetoothDevice dev : prDevices){
        	String s = dev.getName();
        	s=s+" "+dev.getAddress();
        	if(connDev!=null && connDev.equals(dev)){
        		s="-> "+s;
        	}
        	data.add(s);
        }
        return data;
	}
	
	public List<String> getBtDevList(){
         
        List<String> data = new ArrayList<String>();
        Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
//      prDevices.clear();
		if (pairedDevices.size() > 0) {
          for (BluetoothDevice device : pairedDevices) {
          	if(!btDevices.contains(device))
          	btDevices.add(device);
          }
      }
        for(BluetoothDevice dev : btDevices){
        	String s = dev.getName();
        	if(s!=null){
	        	if(s.indexOf("null")>-1){
	        		s = "Bluetooth";
	        	}
        	}else{
        		s = "Bluetooth";
        	}
        	//String[] a = dev.getAddress().split(":");
        	s=s+" "+dev.getAddress()+" "+(dev.getBondState()==BluetoothDevice.BOND_BONDED?((connDev!=null && connDev.equals(dev))?getString(R.string.connected):getString(R.string.bonded)):getString(R.string.unbond));
        	
        	data.add(s);
        }
      
//        for(BluetoothDevice dev : prDevices){
//        	String s = dev.getName();
//        	s=s+" "+dev.getAddress();
//        	if(connDev!=null && connDev.equals(dev)){
//        		s="-> "+s;
//        	}
//        	data.add(s);
//        }
        return data;
    }

	public void bluetoothWrite(String str){
		if(mConnectedThread==null) return;
		//Log.i(dbg, "tx:"+str);
		mConnectedThread.write(str.getBytes());
	}
	
	public void bluetoothWrite(byte[] data){
		if(mConnectedThread==null) return;
		///*
		String hexStr="";
		for(int i1=0;i1<data.length;i1++){
			hexStr+=String.format("%02X ", data[i1]);
		}
		Log.d("mb", "tx:"+hexStr);
		//*/
		if(mConnectedThread.txBusy==false){
			mConnectedThread.write(data);
		}else{
			Log.d("mb", "tx busy");
		}
	}
	
	public void bluetoothDisconnect(BluetoothDevice device){
		Log.i(dbg, "disconnect to "+device.getName());
		if(mConnectThread != null){mConnectThread.cancel();mConnectThread = null;}
		if(mConnectedThread != null){mConnectedThread.cancel();mConnectedThread = null;}
	}
	
	public void bluetoothConnect(BluetoothDevice device) throws Exception{
		Log.i(dbg, "try connect to "+device.getName());
		if(mConnectThread != null){mConnectThread.cancel();mConnectThread = null;}
		if(mConnectedThread != null){mConnectedThread.cancel();mConnectedThread = null;}
		
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
//		if(device.getBondState()==BluetoothDevice.BOND_NONE){
//		  String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
//		    Intent intent = new Intent(ACTION_PAIRING_REQUEST);
//		    String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
//		    intent.putExtra(EXTRA_DEVICE, device);
//		    String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
//		    int PAIRING_VARIANT_PIN = 0;
//		    intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
//		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		   startActivity(intent);
//		}

		Intent intent =new Intent(this,DialogActivity.class); 
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("msg", getString(R.string.connecting));
        startActivity(intent);
		
	}
	
	public void bluetoothConnected(BluetoothDevice device, BluetoothSocket socket){
		Log.i(dbg, "bluetooth connected:"+device.getAddress());
		connDev = device;
		if(mHandler!=null){
			Message msg = mHandler.obtainMessage(MSG_CONNECTED);
			mHandler.sendMessage(msg);
		}
		if(mConnectedThread != null){mConnectedThread.cancel();mConnectedThread = null;}
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		Intent intent =new Intent(this,DialogActivity.class); 
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("msg", "connected");
        startActivity(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
