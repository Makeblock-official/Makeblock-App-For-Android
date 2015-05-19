package cc.makeblock.makeblock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import cc.makeblock.modules.MeModule;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class UpgradeFirm {

static byte STK_OK              = 0x10;
static byte STK_FAILED          = 0x11;  // Not used
static byte STK_UNKNOWN         = 0x12;  // Not used
static byte STK_NODEVICE        = 0x13;  // Not used
static byte STK_INSYNC          = 0x14;  // ' '
static byte STK_NOSYNC          = 0x15;  // Not used
static byte ADC_CHANNEL_ERROR   = 0x16;  // Not used
static byte ADC_MEASURE_OK      = 0x17;  // Not used
static byte PWM_CHANNEL_ERROR   = 0x18;  // Not used
static byte PWM_ADJUST_OK       = 0x19;  // Not used
static byte CRC_EOP             = 0x20;  // 'SPACE'
static byte STK_GET_SYNC        = 0x30;  // '0'
static byte STK_GET_SIGN_ON     = 0x31;  // '1'
static byte STK_SET_PARAMETER   = 0x40;  // '@'
static byte STK_GET_PARAMETER   = 0x41;  // 'A'
static byte STK_SET_DEVICE      = 0x42;  // 'B'
static byte STK_SET_DEVICE_EXT  = 0x45;  // 'E'
static byte STK_ENTER_PROGMODE  = 0x50;  // 'P'
static byte STK_LEAVE_PROGMODE  = 0x51;  // 'Q'
static byte STK_CHIP_ERASE      = 0x52;  // 'R'
static byte STK_CHECK_AUTOINC   = 0x53;  // 'S'
static byte STK_LOAD_ADDRESS    = 0x55;  // 'U'
static byte STK_UNIVERSAL       = 0x56;  // 'V'
static byte STK_PROG_FLASH      = 0x60;  // '`'
static byte STK_PROG_DATA       = 0x61; // 'a'
static byte STK_PROG_FUSE       = 0x62;  // 'b'
static byte STK_PROG_LOCK       = 0x63;  // 'c'
static byte STK_PROG_PAGE       = 0x64;  // 'd'
static byte STK_PROG_FUSE_EXT   = 0x65;  // 'e'
static byte STK_READ_FLASH      = 0x70;  // 'p'
static byte STK_READ_DATA       = 0x71;  // 'q'
static byte STK_READ_FUSE       = 0x72;  // 'r'
static byte STK_READ_LOCK       = 0x73;  // 's'
static byte STK_READ_PAGE       = 0x74;  // 't'
static byte STK_READ_SIGN       = 0x75;  // 'u'
static byte STK_READ_OSCCAL     = 0x76;  // 'v'
static byte STK_READ_FUSE_EXT   = 0x77;  // 'w'
static byte STK_READ_OSCCAL_EXT = 0x78;  // 'x'

static byte CAT_SETADDR = 0x41; // 'A'
static byte CAT_WRITE = 0x42; // 'B'
static byte CAT_QUIT = 0x45; // 'E'

static byte QUERY_HW_VER = (byte) 0x80;
static byte QUERY_SW_MAJOR = (byte) 0x81;
static byte QUERY_SW_MINOR = (byte) 0x82;

static byte DOWNLOAD_SENDADDR = (byte) 0xE0;
static byte DOWNLOAD_SENDCODE = (byte) 0xE1;

static int ST_NULLDEVICE = 0;
static int ST_PROBING = 1;
static int ST_FOUND = 2;
static int ST_DOWNLOADING = 3;
static int ST_READ = 4;

Context context;
byte[] hex;

byte prevCmd;
int state = ST_NULLDEVICE;
int ARDUINO_PAGE_SIZE=128;
int hexLen = 0;
int hexPtr = 0;
	public UpgradeFirm(Context context){
		this.context=context;
		hex = new byte[64*1024]; // the hex file is about 57k, and rom should be less than 32k
		state = ST_PROBING;
		loadFirm();
	}
	
	public int getDowningProcess(){
		return (int)((float)hexPtr*100/hexLen);
	}
	
	public byte[] getProbeCmd(){
		byte[] cmd = new byte[2];
		cmd[0] = STK_GET_SYNC;
		cmd[1] = CRC_EOP;
		//cmd[2] = STK_GET_PARAMETER;
		//cmd[3] = QUERY_HW_VER;
		//cmd[4] = CRC_EOP;
		
		prevCmd = STK_GET_SYNC;
		return cmd;
	}
	
	public int sendQuery()
	{
		
		return 0;
	}
	
	public int loadFirm()
	{
		// load hex file
        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(R.raw.firmware);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in_s));
        try {
			String line = reader.readLine();
			parseHexLine(line);
			while (line != null) {
				line = reader.readLine();
				if(line==null)
					break;
				int ret = parseHexLine(line);
				if(ret>0) hexLen = ret;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public int startDownload()
	{
		// start downloading
        hexPtr = 0;
        state = ST_DOWNLOADING;
        prevCmd = STK_PROG_PAGE; // init ping-pong cmd of page upload
        
		return 0;
	}

	public int parseCmd(int[] c){
	    if(prevCmd==STK_GET_SYNC && state==ST_PROBING){
	    	if(c[0]==0x14 && c[1]==0x10){
	            // stop probing
	            state = ST_FOUND;
	            return 1;
	        }
	    }
	    
	    return 0;
	}
	
	public byte[] getHexPage(){
		byte[] ret;
		if(prevCmd == STK_PROG_PAGE){
			if(hexPtr>=hexLen){
				ret = new byte[2];
				ret[0] = STK_LEAVE_PROGMODE;
				ret[1] = CRC_EOP;
				prevCmd = STK_LEAVE_PROGMODE;
				return ret;
			}else{
				ret = new byte[4];
				int address = hexPtr/2;
				byte addrl = (byte) (address & 0xff);
			    byte addrh = (byte) ((address>>8) & 0xff);
				ret[0] = STK_LOAD_ADDRESS;
				ret[1] = addrl;
				ret[2] = addrh;
				ret[3] = CRC_EOP;
				prevCmd = STK_LOAD_ADDRESS;
				return ret;
			}
		}else if(prevCmd==STK_LOAD_ADDRESS){
			int len=((hexLen-hexPtr)>ARDUINO_PAGE_SIZE)?ARDUINO_PAGE_SIZE:hexLen-hexPtr;
			ret = new byte[4+len+1];
			byte lenl = (byte) (len & 0xff);
			byte lenh = (byte) ((len>>8) & 0xff);
			ret[0]=STK_PROG_PAGE;
			ret[1]=lenh;
			ret[2]=lenl;
			for(int i=0;i<len;i++){
				ret[4+i] = hex[hexPtr];
				hexPtr++;
			}
			ret[3]='F';
			ret[4+len] = ' '; // the last space of page
			prevCmd = STK_PROG_PAGE;
			return ret;
		}
		return null;
	}
	
	int parseHexLine(String str)
	{
		if(str.charAt(0)!=':') return -2;
		int cnt = Integer.parseInt(str.substring(1, 3),16);
		int addr = Integer.parseInt(str.substring(3, 7),16);
		int type = Integer.parseInt(str.substring(7, 9),16);
		if(type==1) return -1;
		int bias = 0;
		for(int i=9;i<(9+cnt*2);i+=2){
			String tmp = str.substring(i, i+2);
			int inte =  Integer.parseInt(tmp,16);
			hex[addr+bias] = (byte) inte;
			bias+=1;
		}
		
		return addr+cnt;
	}
	
	

}
