package cc.makeblock.makeblock;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cc.makeblock.makeblock.BluetoothLeClass.OnDataAvailableListener;
import cc.makeblock.makeblock.BluetoothLeClass.OnDisconnectListener;
import cc.makeblock.makeblock.BluetoothLeClass.OnServiceDiscoverListener;
import android.R.bool;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

@SuppressLint("NewApi")
public class BluetoothLE extends Service {
	private final static String TAG = BluetoothLE.class.getSimpleName();  
    private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";  
  
    private LeDeviceListAdapter mDevices;  
    /**搜索BLE终端*/  
    private BluetoothAdapter mBluetoothAdapter;  
    /**读写BLE终端*/  
    private BluetoothLeClass mBLE;  
    private boolean mScanning;  
    private Handler mHandler;  
    private Context mContext;
    public Handler leHandler;
    private BluetoothDevice mCurrentDevice;
    private boolean mIsConnected = false;
	static final int MSG_CONNECTED=1;
	static final int MSG_DISCONNECTED=2;
	static final int MSG_RX = 3;
	static final int MSG_FOUNDDEVICE = 4;
	static final int MSG_CONNECT_FAIL = 5;
	static final int MSG_DISCOVERY_FINISHED = 6;
	static final int MSG_RX_FIRMUPLOAD = 7;
	static final int MSG_SCAN_START = 8;
	static final int MSG_SCAN_END = 9;
	static final int MSG_CONNECTING = 10;
    // Stops scanning after 10 seconds.  
    private static final long SCAN_PERIOD = 10000;  

	static final int MODE_LINE = 0;
	static final int MODE_FORWARD = 1;
	public int commMode = MODE_LINE;
	
    private static BluetoothLE _instance;

	public static BluetoothLE sharedManager() {
		if (_instance == null) {
			_instance = new BluetoothLE();
		}
		return _instance;

	}
	public BluetoothLE(){
		// bluetoothLE classic
		
		
	}
	public void setup(Context context){
		mDevices = new LeDeviceListAdapter();
		mContext = context;
		mHandler = new Handler();
		final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);  
        mBluetoothAdapter = bluetoothManager.getAdapter();  
          
        //开启蓝牙  
        mBluetoothAdapter.enable();  
          
        mBLE = new BluetoothLeClass(mContext);  
        if (!mBLE.initialize()) {  
            Log.e(TAG, "Unable to initialize Bluetooth");  
        }  
        //发现BLE终端的Service时回调  
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);  
        //收到BLE终端数据交互的事件  
        mBLE.setOnDataAvailableListener(mOnDataAvailable);
        
	} 
	public void start(){
        scanLeDevice(true);
	}
	public void stop(){
        scanLeDevice(false);  
        mBLE.disconnect();  
        mIsConnected = false;
	}
    public void close(){  
        mBLE.close();  
        mIsConnected = false;
        mCurrentDevice = null;
    } 
    public void clear(){
    	mDevices.clear();
    }
    public List<String> getDeviceList(){
        
        List<String> data = new ArrayList<String>();
//      prDevices.clear();
        for(int i=0;i<mDevices.getCount();i++){
        	BluetoothDevice dev = mDevices.getDevice(i);
        	String s = dev.getName();
        	if(s!=null){
	        	if(s.indexOf("null")>-1){
	        		s = "Bluetooth";
	        	}
        	}else{
        		s = "Bluetooth";
        	}
        	//String[] a = dev.getAddress().split(":");
        	if(mCurrentDevice!=null){
        		s=s+" "+dev.getAddress()+" "+(dev.getBondState()==BluetoothDevice.BOND_NONE?((mCurrentDevice!=null && mCurrentDevice.equals(dev))?mContext.getString(R.string.connected):mContext.getString(R.string.unbond)):mContext.getString(R.string.bonded));
        	}else{
        		s=s+" "+dev.getAddress()+" "+(dev.getBondState()==BluetoothDevice.BOND_BONDED?mContext.getString(R.string.bonded):mContext.getString(R.string.unbond));
        	}
        	data.add(s);
        }
        return data;
    }
	public boolean selectDevice(int position) {  
        final BluetoothDevice device = mDevices.getDevice(position);  
        if (device == null) return false;  
        if (mScanning) {  
            mBluetoothAdapter.stopLeScan(mLeScanCallback);  
            mScanning = false;  
        }  
        if(leHandler!=null){
        	Message msg = leHandler.obtainMessage(MSG_CONNECTING);
        	leHandler.sendMessage(msg);
        }
        boolean conn = mBLE.connect(device.getAddress());
		if(conn){
	        mCurrentDevice = device;
		}
        return conn;  
    }  
	public boolean isConnected(){
		return mIsConnected;
	}
	private byte[] mBuffers = new byte[1024];
	private int mBuffersIndex = 0;
	public void writeBuffer(byte[] buf){
		for(int i=0;i<buf.length;i++){
			mBuffers[mBuffersIndex]=buf[i];
			mBuffersIndex++;
		}
		MeTimer.startWrite();
	}
	public boolean writeSingleBuffer(){
		BluetoothGattCharacteristic ch = characteristicForProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
		if(ch!=null){
			if(mBuffersIndex>0){
				int len = mBuffersIndex>20?20:mBuffersIndex;
				byte[] buf = new byte[len];
				for(int i=0;i<len;i++){
					buf[i] = mBuffers[i];
				}
				mBuffersIndex-=len;
				byte[] _clone = mBuffers.clone();
				for(int i=0;i<mBuffersIndex;i++){
					mBuffers[i]=_clone[len+i];
				}
				ch.setValue(buf);
//				Log.d("mb", "le tx:"+buf.length);
				mBLE.writeCharacteristic(ch);
				return true;
			}
//			String hexStr="";
//			for(int i1=0;i1<buf.length;i1++){
//				hexStr+=String.format("%02X ", buf[i1]);
//			}
		}
		return false;
	}
	private byte[] mProbeBytes;
	public void resetIO(byte[] probeBytes){
		mProbeBytes = probeBytes;
		if(resetIndex==0){
	        resetIndex++;
			resetHandler.postDelayed(resetRunnable, 30);
		}
	}
	private void resetLow(){
		BluetoothGattCharacteristic ch = characteristicForProperty(BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ);
		if(ch!=null){
			byte[] buf = {0};
			ch.setValue(buf);
			mBLE.writeCharacteristic(ch);
        	Log.d("mb","reset low");
		}
	}
	private void resetHigh(){
		BluetoothGattCharacteristic ch = characteristicForProperty(BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ);
		if(ch!=null){
			byte[] buf = {1};
			ch.setValue(buf);
			mBLE.writeCharacteristic(ch);

        	Log.d("mb","reset high");
		}
	}
	private int resetIndex = 0;
	Handler resetHandler=new Handler();
	Runnable resetRunnable=new Runnable() {
	    @Override
	    public void run() {
	        // TODO Auto-generated method stub
	    	if(resetIndex==4){
		       	resetHigh();
		    }else if(resetIndex<4){
		        if(resetIndex%2==1){
		        	resetHigh();
		        }else{
		        	resetLow();
		        }
		    }else if(resetIndex==5){
		    	writeBuffer(mProbeBytes);
		    }else{
		    	
		    }
	        resetIndex++;
	       
	        if(resetIndex<7){
	        	resetHandler.postDelayed(this, resetIndex==5?500:(resetIndex==6?2000:100));
	        }else{
	        	resetIndex = 0;
	        	Log.d("mb","reset end");
	        }
	    }
	};
	private BluetoothGattCharacteristic characteristicForProperty(int property){
		List<BluetoothGattService> list = mBLE.getSupportedGattServices();
		if (list == null) return null;  
        for (BluetoothGattService gattService : list) {  
            //-----Service的字段信息-----//  
            int type = gattService.getType();  
            String uuid = gattService.getUuid().toString();
            //-----Characteristics的字段信息-----//  
            List<BluetoothGattCharacteristic> gattCharacteristics =gattService.getCharacteristics();  
            for (final BluetoothGattCharacteristic  gattCharacteristic: gattCharacteristics) {  
                  
                int permission = gattCharacteristic.getPermissions();  
                  
                int properties = gattCharacteristic.getProperties();  
//                Log.d("mb","---->char property:"+Utils.getCharPropertie(property)+" - "+(property&BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)+" - "+BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);  
                int p = (property&properties);
                int np = property;
                if(np==p){
                	if(np==(int)(BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ)){
	                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic 
                		if((int)(uuid.indexOf("ffe4"))>0){
                			return gattCharacteristic; 
                		}
                	}else{
                		return gattCharacteristic; 
                	}
                }  
            }
        }//  
        return null;
	}
	private void scanLeDevice(final boolean enable) {  
        if (enable) {  
            // Stops scanning after a pre-defined scan period.  
            mHandler.postDelayed(new Runnable() {  
                @Override  
                public void run() {  
                    mScanning = false;  
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);  
                    if(leHandler!=null){
        				Message msg = leHandler.obtainMessage(MSG_SCAN_END);
        				leHandler.sendMessage(msg);
        			}
                }  
            }, SCAN_PERIOD);  
  
            mScanning = true;  
            mBluetoothAdapter.startLeScan(mLeScanCallback);  
            if(leHandler!=null){
				Message msg = leHandler.obtainMessage(MSG_SCAN_START);
				leHandler.sendMessage(msg);
			}
            
        } else {  
            mScanning = false;  
            mBluetoothAdapter.stopLeScan(mLeScanCallback);  
        }  
    }
	/** 
     * 搜索到BLE终端服务的事件 
     */  
	private OnDisconnectListener mOnDisconnectListener = new OnDisconnectListener() {
		
		@Override
		public void onDisconnect(BluetoothGatt gatt) {
			// TODO Auto-generated method stub
			stop();
			Log.d("mb","ble disconnected");
			if(leHandler!=null){
				Message msg = leHandler.obtainMessage(MSG_DISCONNECTED);
				leHandler.sendMessage(msg);
			}
		}
	};
    private OnServiceDiscoverListener mOnServiceDiscover = new OnServiceDiscoverListener() {
		
		public void onServiceDiscover(BluetoothGatt gatt) {
			 displayGattServices(mBLE.getSupportedGattServices());  
			
		}
	};  
      
    /** 
     * 收到BLE终端数据交互的事件 
     */  
    private OnDataAvailableListener mOnDataAvailable = new OnDataAvailableListener(){  
  
        /** 
         * BLE终端数据被读的事件 
         */  
        @Override  
        public void onCharacteristicRead(BluetoothGatt gatt,  
                BluetoothGattCharacteristic characteristic, int status) {  
            if (status == BluetoothGatt.GATT_SUCCESS)   
                Log.d("mb","onCharRead "+gatt.getDevice().getName()  
                        +" read "  
                        +characteristic.getUuid().toString()  
                        +" -> "  
                        +Utils.bytesToHexString(characteristic.getValue()));  
        }  
          
        /** 
         * 收到BLE终端写入数据回调 
         */  
        byte[] buffer = new byte[1024];
        int bytesLen;
		private List<Integer> mRx = new ArrayList<Integer>();
        @Override  
        public void onCharacteristicWrite(BluetoothGatt gatt,  
                BluetoothGattCharacteristic characteristic) {  
            
        	buffer = characteristic.getValue();
//			for(int i1=0;i1<buffer.length;i1++){
//				hexStr+=String.format("%02X ", buffer[i1]);
//			}
//			Log.d("mb", "le buffer:"+hexStr);
        	bytesLen = buffer.length;
			if (bytesLen > 0) {
				for (int i = 0; i < bytesLen; i++) {
					Integer c = buffer[i]&0xff;
					mRx.add(c);
					// line end or bootloader end
					if ((c == 0x0a && commMode==MODE_LINE) || (c==0x10 && commMode==MODE_FORWARD)) {
						// TODO: post msg to UI
						//write(mReceiveString.getBytes());
						int[] rxbtyes = new int[mRx.size()];// = mRx.toArray(new Byte[mRx.size()]);

			        	String hexStr="";
						for(int i1=0;i1<rxbtyes.length;i1++){
							rxbtyes[i1] = mRx.get(i1);
							hexStr+=String.format("%02X ", rxbtyes[i1]);
						}
						Log.d("mb", "le rx:"+hexStr);
						
						leHandler.obtainMessage(MSG_RX,rxbtyes).sendToTarget();
						mRx.clear();
					}
				}
			}
        }  
    };  
  
    // Device scan callback.  
    private LeScanCallback mLeScanCallback =  
            new LeScanCallback() {  
  
        @Override  
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {  
            ((Activity) mContext).runOnUiThread(new Runnable() {  
                @Override  
                public void run() {  
                	mDevices.addDevice(device);  
                	//push notify
                	if(leHandler!=null){
    					Message msg = leHandler.obtainMessage(MSG_FOUNDDEVICE);
    					leHandler.sendMessage(msg);
    				}
                }  
            });  
        }  
    };  
    private void displayGattServices(List<BluetoothGattService> gattServices) {  
        if (gattServices == null) return;  
        mIsConnected = true;
        for (BluetoothGattService gattService : gattServices) {  
            //-----Service的字段信息-----//  
            int type = gattService.getType();  
            Log.e(TAG,"-->service type:"+Utils.getServiceType(type));  
            Log.e(TAG,"-->includedServices size:"+gattService.getIncludedServices().size());  
            Log.e(TAG,"-->service uuid:"+gattService.getUuid());  
              
            //-----Characteristics的字段信息-----//  
            List<BluetoothGattCharacteristic> gattCharacteristics =gattService.getCharacteristics();  
            for (final BluetoothGattCharacteristic  gattCharacteristic: gattCharacteristics) {  
                Log.e(TAG,"---->char uuid:"+gattCharacteristic.getUuid());  
                  
                int permission = gattCharacteristic.getPermissions();  
                Log.e(TAG,"---->char permission:"+Utils.getCharPermission(permission));  
                  
                int property = gattCharacteristic.getProperties();  
                Log.e(TAG,"---->char property:"+Utils.getCharPropertie(property));  
  
                byte[] data = gattCharacteristic.getValue();  
                if (data != null && data.length > 0) {  
                    Log.e(TAG,"---->char value:"+new String(data));  
                }  
                if((gattCharacteristic.getProperties()&BluetoothGattCharacteristic.PROPERTY_NOTIFY)==BluetoothGattCharacteristic.PROPERTY_NOTIFY){
            		mBLE.setCharacteristicNotification(gattCharacteristic, true);  
            	}
                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic  
//                if(gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)){                    
                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()  
//                    mHandler.postDelayed(new Runnable() {  
//                        @Override  
//                        public void run() {  
//                            mBLE.readCharacteristic(gattCharacteristic);  
//                        }  
//                    }, 500);  
                    
                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()  
//                	if((gattCharacteristic.getProperties()&BluetoothGattCharacteristic.PROPERTY_NOTIFY)==BluetoothGattCharacteristic.PROPERTY_NOTIFY){
//                		mBLE.setCharacteristicNotification(gattCharacteristic, true);  
//                	}
                    //设置数据内容  
//                    gattCharacteristic.setValue("send data->");  
                    //往蓝牙模块写入数据  
//                    mBLE.writeCharacteristic(gattCharacteristic);  
//                }  
                  
                //-----Descriptors的字段信息-----//  
//                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();  
//                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {  
//                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());  
//                    int descPermission = gattDescriptor.getPermissions();  
//                    Log.e(TAG,"-------->desc permission:"+ Utils.getDescPermission(descPermission));  
//                    
//                    byte[] desData = gattDescriptor.getValue();  
//                    if (desData != null && desData.length > 0) {  
//                        Log.e(TAG, "-------->desc value:"+ new String(desData));  
//                    }  
//                 }  
            }  
        }//  

    	if(leHandler!=null){
			Message msg = leHandler.obtainMessage(MSG_CONNECTED);
			leHandler.sendMessage(msg);
		}
  
    }
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}  
}  
