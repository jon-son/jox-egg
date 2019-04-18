/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ble;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.lee.circleseekbar.R;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */







public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_AVAILABLE1 =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE1";
    
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_DATA1 =
            "com.example.bluetooth.le.EXTRA_DATA1";
    
    public final static String EXTRA_UUID =
            "com.example.bluetooth.le.uuid_DATA";
    public final static String EXTRA_NAME =
            "com.example.bluetooth.le.name_DATA";
    public final static String EXTRA_PASSWORD =
            "com.example.bluetooth.le.password_DATA";
    
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    
    public static String Service_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String Characteristic_uuid_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String Characteristic_uuid_FUNCTION = "0000ffe2-0000-1000-8000-00805f9b34fb";
	
    
    //int tx_cnt = 1;
    byte tx_cnt = (byte)0x01;
    public enum function_type {
    	iBeacon_UUID, //0xe1  设置iBeacon UUID
    	iBeacon_Major,//0xe2/设置iBeacon Major
    	iBeacon_Minor,//0XE3:设置iBeacon Minor
    	adv_intverl,//0XE4:设置广播间隔
    	pin_password,//0XE5:连接密码   密码只能为4位数字       0XE5://连接密码
    	name,//0XE6:---------------------------------设备名  
    	GPIO,//0XE7:IO功能
    	PWM,// 0XE8:PWM功能
    	Other,//0XE9:复位模块、断开蓝牙、读取模块的版本
    	Power,//0XEA:设备功率
    	RTC,//0XEB:RTC功能
    	
    }

    public  String bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }
    public  byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        b = null;
        return b2;
    }
    
    
    void deley( int ms )
    {
    	try {  
            Thread.currentThread();  
            Thread.sleep( ms );  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }
    }
    
    
	/**
	 * byte[]转变为16进制String字符, 每个字节2位, 不足补0
	 */
	public  String getStringByBytes(byte[] bytes) {
		String result = null;
		String hex = null;
		if (bytes != null && bytes.length > 0) {
			final StringBuilder stringBuilder = new StringBuilder(bytes.length);
			for (byte byteChar : bytes) {
				hex = Integer.toHexString(byteChar & 0xFF);
				if (hex.length() == 1) {
					hex = '0' + hex;
				}
				stringBuilder.append(hex.toUpperCase());
			}
			result = stringBuilder.toString();
		}
		return result;
	}
    
    
    
	/**
	 * 取得在16进制字符串中各char所代表的16进制数
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	/**
	 * 把16进制String字符转变为byte[]
	 */
	public static byte[] getBytesByString(String data) //
	{
		byte[] bytes = null;
		if (data != null) {
			data = data.toUpperCase();
			int length = data.length() / 2;
			char[] dataChars = data.toCharArray();
			bytes = new byte[length];
			for (int i = 0; i < length; i++) {
				int pos = i * 2;
				bytes[i] = (byte) (charToByte(dataChars[pos]) << 4 | charToByte(dataChars[pos + 1]));
			}
		}
		return bytes;
	}
    public String bytesToHexString(byte[] src)//例子byte[]：0x11,0x22,0x33,0x44,0x55,0x66  转换后String:“112233445566”
    {  
      	 StringBuilder stringBuilder = new StringBuilder(src.length);
           for(byte byteChar : src)
              stringBuilder.append(String.format("%02X", byteChar));
          return stringBuilder.toString();  
      } 
    public String bytesToHexString1(byte[] src)//例子byte[]：0x11,0x22,0x33,0x44,0x55,0x66  转换后String:“11 22 33 44 55 66”
    {  
      	 StringBuilder stringBuilder = new StringBuilder(src.length);
           for(byte byteChar : src)
              stringBuilder.append(String.format(" %02X", byteChar));
          return stringBuilder.toString();  
      } 
    public String bytesToHexString1(byte[] src,int index )//例子byte[]：0x11,0x22,0x33,0x44,0x55,0x66  转换后String:“11 22 33 44 55 66”
    {  
    	if( src==null )return null;
      	 StringBuilder stringBuilder = new StringBuilder(src.length);
           //for(byte byteChar : src)
         for( int i=index;i<src.length;i++ )
              stringBuilder.append(String.format(" %02X", src[i]));
          return stringBuilder.toString();  
      } 
    
	public String String_to_HexString0( String str )//例子String“123456” 转换后String：“313233343536”
	{
     	String st =str.toString();
    	byte[] WriteBytes = new byte[st.length()];
    	WriteBytes = st.getBytes();
    	return bytesToHexString( WriteBytes );
	}
	public String String_to_HexString( String str )//例子String“123456” 转换后String：“31 32 33 34 35 36”
	{
     	String st =str.toString();
    	byte[] WriteBytes = new byte[st.length()];
    	WriteBytes = st.getBytes();
    	return bytesToHexString1( WriteBytes );
	}
	public byte[] String_to_byte( String str )//例子String“123456” 转换后byte[]：0x31,0x32,0x33,0x34,0x35,0x36
	{
     	String st =str.toString();
    	byte[] WriteBytes = new byte[st.length()];
    	return WriteBytes;
	}

	public String byte_to_String( byte[] byt )//例子byte[]：0x31,0x32,0x33,0x34,0x35,0x36  转换后String:“123456”
	{
	 String t = new String( byt );//bytep[]转换为String
	 return t;
	}
	public String byte_to_String( byte[] byt,int index )//例子byte[]：0x31,0x32,0x33,0x34,0x35,0x36  转换后String:“123456”
	{
		if( byt==null )return null;
		byte[] WriteBytes = new byte[ byt.length-index ];
		for( int i=index;i<byt.length;i++ )
			WriteBytes[ i-index ]=byt[i];
	 String t = new String( WriteBytes );//bytep[]转换为String
	 return t;
	}
	
    byte[] WriteBytes = new byte[200];
	public int txxx(String g ,boolean string_or_hex_data ){
		int ic=0;
		//g=""+g;
		if( string_or_hex_data )WriteBytes= g.getBytes();//getBytesByString( g );//  hex2byte(g.toString().getBytes());
		else WriteBytes= getBytesByString( g );
		int length = WriteBytes.length;
		int data_len_20 = length/20;
		int data_len_0 = length%20;
		
		
		int i=0;
		if( data_len_20>0 )
		{
			for( ;i<data_len_20;i++ )
			{
				byte[] da = new byte[20];
				for( int h=0;h<20;h++ )
				{
					da[h] = WriteBytes[ 20*i+h];
					//Log.d("20*i+h"," len = " + 20*i+h );
				}
				BluetoothGattCharacteristic gg;
				gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
				gg.setValue(da);
				mBluetoothGatt.writeCharacteristic(gg);
				deley(23);
				ic +=20;
			}
			
		}
		if( data_len_0>0 )
		{
			byte[] da = new byte[data_len_0];
			for( int h=0;h<data_len_0;h++ )
			{
				da[h] = WriteBytes[ 20*i+h];
			}
			BluetoothGattCharacteristic gg;
			gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
			gg.setValue(da);
			mBluetoothGatt.writeCharacteristic(gg);
			ic +=data_len_0;
			deley(23);
		}
		
		
//		Log.d("out"," len = " + WriteBytes.length );
//		Log.d("data_len_20"," len = " + data_len_20 );
//		Log.d("data_len_0"," len = " + data_len_0 );
		
		//mBluetoothGatt.setCharacteristicNotification(gg, true);
		
		//gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		//mBluetoothGatt.setCharacteristicNotification(gg, true);
		return ic;
	}
	
	public void function_data( byte []data  )
	{
    	
    	WriteBytes= data;
    	
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		//byte t[]={51,1,2};
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
	}
	
	public void function_data( String data  )
	{
    	
    	WriteBytes = data.getBytes();;
    	
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		//byte t[]={51,1,2};
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
	}
//	public void enable_noty()
//	{
//		BluetoothGattService service =mBluetoothGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
//		BluetoothGattCharacteristic ale =service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
//		boolean set = mBluetoothGatt.setCharacteristicNotification(ale, true);
//		Log.d(TAG," setnotification = " + set);
//		BluetoothGattDescriptor dsc =ale.getDescriptor(UUID.fromString(  "00002902-0000-1000-8000-00805f9b34fb"));
//		byte[]bytes = {0x01,0x00};
//		dsc.setValue(bytes);
//		mBluetoothGatt.writeDescriptor(dsc);
//	}
	
	
    public void enable_JDY_ble( int p ){
    	
    	try {
		//if( p )
	    {
			BluetoothGattService service =mBluetoothGatt.getService(UUID.fromString(Service_uuid));
			BluetoothGattCharacteristic ale;// =service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
			switch( p )
			{
				case 0://0xFFE1 //透传
				{
					ale =service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
				}break;
				case 1:// 0xFFE2 //iBeacon_UUID
				{
					ale =service.getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
				}break;
				default:
					ale =service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
					break;
			} 
			boolean set = mBluetoothGatt.setCharacteristicNotification(ale, true);
			//Log.d(TAG," setnotification = " + set);
			BluetoothGattDescriptor dsc =ale.getDescriptor(UUID.fromString(  "00002902-0000-1000-8000-00805f9b34fb"));
			byte[]bytes = {0x01,0x00};
			dsc.setValue(bytes);
			boolean success =mBluetoothGatt.writeDescriptor(dsc);
			//Log.d(TAG, "writing enabledescriptor:" + success);
	    }
//	    else
//	    {
//		   BluetoothGattService service =mBluetoothGatt.getService(UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455"));
//		   BluetoothGattCharacteristic ale =service.getCharacteristic(UUID.fromString(Service_uuid));
//		   boolean set = mBluetoothGatt.setCharacteristicNotification(ale, false);
//		   Log.d(TAG," setnotification = " + set);
//		   BluetoothGattDescriptor dsc =ale.getDescriptor(UUID.fromString(Characteristic_uuid_TX));
//		   byte[]bytes = {0x00, 0x00};
//		   dsc.setValue(bytes);
//		   boolean success =mBluetoothGatt.writeDescriptor(dsc);
//		   Log.d(TAG, "writing enabledescriptor:" + success);
//	    }
        	
        	
//        	jdy=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//        	mBluetoothGatt.setCharacteristicNotification(jdy, p);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
    }
    
    
    
    
	public String get_mem_data(String key) 
	{
		//同样，在读取SharedPreferences数据前要实例化出一个SharedPreferences对象 
		SharedPreferences sharedPreferences= getSharedPreferences("jdy-ble", 
		Activity.MODE_PRIVATE); 
		// 使用getString方法获得value，注意第2个参数是value的默认值 
		String name =sharedPreferences.getString(key, ""); 
		if( name==null||name=="") return "123456";
		return name;
	}
	public void set_mem_data(String key, String values) 
	{
		//实例化SharedPreferences对象（第一步） 
		SharedPreferences mySharedPreferences= getSharedPreferences("jdy-ble", 
		Activity.MODE_PRIVATE);
		//实例化SharedPreferences.Editor对象（第二步） 
		SharedPreferences.Editor editor = mySharedPreferences.edit(); 
		//用putString的方法保存数据 
		editor.putString(key, values ); 
		//提交当前数据 
		editor.commit(); 
		//使用toast信息提示框提示成功写入数据 
		//Toast.makeText(this, values , 
		//Toast.LENGTH_LONG).show(); 
	}
    
    
    public boolean get_password( String password )//读取设备密码
    {
    	boolean p = true;
    	if( password==null )return false;
    	else if( password.length()!=6 )return false;
    	
    	String txt="E552";
    	String value=bin2hex(password);
    	txt=txt+value;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		return p;
    }
    public boolean set_password( String password,String new_password )//读取设备密码
    {
    	boolean p = true;
    	if( password==null||new_password==null )return false;
    	//else if( password.length()!=6||new_password.length()!=6 )return false;
    	
    	String txt="E551";
    	String value=String_to_HexString0(password);
    	txt=txt+value;
    	value=String_to_HexString0(new_password);
    	txt=txt+value;
    	
    	//Log.d(TAG, "out_1" + txt);
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		return p;
    }
    
    
    String getutf8FromString(String str) {
        StringBuffer utfcode = new StringBuffer();
        try {
            for(byte bit : str.getBytes("utf-8")){
                char hex = (char) (bit & 0xFF);
                utfcode.append(Integer.toHexString(hex));
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return utfcode.toString();
    }
    
//    public boolean set_name_hz( String name )//更新设备名
//    {
//    	boolean p = true;
//    	if( name==null)return false;
//    	
//    	String txt="E661";
//    	String value=   bin2hex(name);
//    	
//    	 byte[] WriteBytes1 = new byte[name.length()];
//    	 WriteBytes1 = name.getBytes();
//    	
//    	txt=txt+value;
//    	WriteBytes= hex2byte( txt.toString().getBytes() );
//    	
//    	byte[] WriteBytes3 = new byte[name.length()+2];
//    	WriteBytes3[ 0 ] = (byte)0xe6;
//    	WriteBytes3[ 1 ] = (byte)0x61;	
//    	for( int i=0;i<name.length();i++ )
//    	{
//    		WriteBytes3[i+2] = WriteBytes1[i];
//    	}
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//		gg.setValue( WriteBytes3 );
//		mBluetoothGatt.writeCharacteristic(gg);
//		return p;
//    }
    public boolean set_name( String name )//更新设备名
    {
    	boolean p = true;
    	if( name==null)return false;
    	
    	String txt="E661";
    	String value=   bin2hex(name);
    	txt=txt+value;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		return p;
    }
    
    
    
   
    
    
    
    public boolean MC_Set_angle( String angle )//
    {
    	int length = angle.length();
    	if( angle==null ) return false;
    	if( length==0 )return false;
    	int angle_int_value =Integer.valueOf(angle).intValue();
    	
    	byte m2 = (byte)(0x22);//透传模块密码位判断
    	boolean p11 = true;
    	String txt="E7f3";
    	
    	if( angle_int_value<=0x09 )
    	{
	    	 txt=txt+"0"+angle_int_value;
	    	
			//byte[] WriteBytes1 = new byte[1];
			//WriteBytes1[0] = (byte)(byt);
	    	//txt = txt+byte_to_String( WriteBytes1 );
	    	
	    	WriteBytes= hex2byte( txt.toString().getBytes() );
	    	/*
	    	byte[] p1 = new byte[6];
	    	p1[0] = (byte) 0xaa;// HEAD
	    	p1[1] = (byte) ((0xaa+tx_cnt)^0x01);// CMD
	    	p1[2] = (byte) ((0xaa+tx_cnt)^0x02);// LEN
	    	p1[3] = (byte) ((0xaa+tx_cnt)^0xa1);// DATA
	    	p1[4] = (byte) ((0xaa+tx_cnt)^0xf0);// CRC
	    	p1[5] = (byte)(p1[0]^p1[1]^p1[2]^p1[3]^p1[4]);
	    	tx_cnt++;
	    	if( tx_cnt>=100 )tx_cnt=1;
	    	*/
	    	BluetoothGattCharacteristic gg;
			gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
			gg.setValue( WriteBytes );
			mBluetoothGatt.writeCharacteristic(gg);
    	}
		return p11;
    }
    public boolean MC_set_button( boolean p )//
    {
    	byte m2 = (byte)(0x22);//透传模块密码位判断
    	boolean p11 = true;
    	String txt="E7f1";
    	if( p )
    	{
    		txt="E7f1";
    		txt=txt+"01";
    	}
    	else 
    	{
    		txt="E7f2";
    		txt=txt+"01";
    	}
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	/*
    	byte[] p1 = new byte[6];
    	p1[0] = (byte) 0xaa;// HEAD
    	p1[1] = (byte) ((0xaa+tx_cnt)^0x01);// CMD
    	p1[2] = (byte) ((0xaa+tx_cnt)^0x02);// LEN
    	p1[3] = (byte) ((0xaa+tx_cnt)^0xa1);// DATA
    	p1[4] = (byte) ((0xaa+tx_cnt)^0xf0);// CRC
    	p1[5] = (byte)(p1[0]^p1[1]^p1[2]^p1[3]^p1[4]);
    	tx_cnt++;
    	if( tx_cnt>=100 )tx_cnt=1;
    	*/
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		//gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
		gg.setValue( WriteBytes );
		mBluetoothGatt.writeCharacteristic(gg);
		return p11;
    	
    }
    public boolean MC_set_password( String password,String new_password )//读取设备密码
    {
    	boolean p = true;
    	if( password==null||new_password==null )return false;
    	//else if( password.length()!=6||new_password.length()!=6 )return false;
    	
    	String txt="E551";
    	String value=String_to_HexString0(password);
    	txt=txt+value;
    	value=String_to_HexString0(new_password);
    	txt=txt+value;
    	
    	//Log.d(TAG, "out_1" + txt);
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		return p;
    }
    
    
    public boolean set_IO1( boolean p )//设置IO1电平
    {
    	boolean p11 = true;
    	String txt="E7f1";
    	if( p )txt=txt+"01";
    	else txt=txt+"00";
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		return p11;
    }
    public boolean set_IO2( boolean p )///设置IO2电平
    {
    	boolean p11 = true;
    	String txt="E7f2";
    	if( p )txt=txt+"01";
    	else txt=txt+"00";
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		return p11;
    }
    public boolean set_IO3( boolean p )///设置IO3电平
    {
    	boolean p11 = true;
    	String txt="E7f3";
    	if( p )txt=txt+"01";
    	else txt=txt+"00";
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		return p11;
    }
    public boolean set_IO4( boolean p )///设置IO4电平
    {
    	boolean p11 = true;
    	String txt="E7f4";
    	if( p )txt=txt+"01";
    	else txt=txt+"00";
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		return p11;
    }
    public boolean set_IO_ALL( boolean p )///设置IO4电平
    {
    	boolean p11 = true;
    	String txt;
    	if( p )txt="E7f5";
    	else txt="E7f0";
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		return p11;
    }
    public void get_IO_ALL(  )///读取4路IO状态
    {
    	String txt="E7f6";
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    public void set_APP_PASSWORD( String pss )///设置APP连接密码
    {
    	boolean p11 = true;
    	String txt="E555";
    	String value=   bin2hex(pss);
    	txt = txt+value;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		
    }
    
    public boolean set_ibeacon_UUID( String uuid )//设置iBeacon UUID
    {
    	if( uuid.length()==36 )
		{
			String v1="",v2="",v3="",v4="";
			v1=uuid.substring(8,9);
			v2=uuid.substring(13,14);
			v3=uuid.substring(18,19);
			v4=uuid.substring(23,24);
			if( v1.equals("-")&&v2.equals("-")&&v3.equals("-")&&v4.equals("-") )
			{
				uuid=uuid.replace("-","");
				uuid="E111"+uuid;
		    	WriteBytes= hex2byte(uuid.toString().getBytes());
		    	
		    	BluetoothGattCharacteristic gg;
				gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
				//byte t[]={51,1,2};
				gg.setValue(WriteBytes);
				mBluetoothGatt.writeCharacteristic(gg);
				return true;
			}else{
				//Toast toast = Toast.makeText(DeviceControlActivity.this, "提示！UUID输入格式不对", Toast.LENGTH_SHORT); 
    			//toast.show(); 
				return false;
			}
		}else {
			//Toast toast = Toast.makeText(DeviceControlActivity.this, "提示！UUID输入不对", Toast.LENGTH_SHORT); 
			//toast.show(); 
			return false;
		}
    }
    public boolean set_ibeacon_MAJOR( String major )//设置iBeacon Major
    {
    	if( major==null )return false;
    	else if( major.length()==0 )return false;
    	
    	String sss=major;
    	int i = Integer.valueOf(sss).intValue();
		String vs=String.format("%02x", i);
		if( vs.length()==2)vs="00"+vs;
		else if( vs.length()==3)vs="0"+vs;
    	
    	String txt="E221";
    	txt =txt+vs;
    	
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    	
    	return true;
    }
    public boolean set_ibeacon_MIMOR( String minor )//设置iBeacon Minor
    {
    	if( minor==null )return false;
    	else if( minor.length()==0 )return false;
    	String sss=minor;
    	int i = Integer.valueOf(sss).intValue();
		String vs=String.format("%02x", i);
		if( vs.length()==2)vs="00"+vs;
		else if( vs.length()==3)vs="0"+vs;
    	
    	String txt="E331";
    	txt =txt+vs;
    	
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    	
    	return true;
    }
    public void set_BroadInterval( int interval )///设置广播间隔
    {
    	String txt="E441";
    	String vs=String.format("%02x", interval );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void get_BroadInterval(  )///读取广播间隔
    {
    	String txt="E442";
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    
    public void set_PWM_OPEN( int pwm )//设置PWM开关
    {
    	String txt="E8a1";
    	String vs=String.format("%02x", pwm );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_PWM_frequency( int frequency )//设置PWM频率
    {
    	String txt="E8a2";
		String vs=String.format("%02x", frequency);
		if( vs.length()==2)vs="00"+vs;
		else if( vs.length()==3)vs="0"+vs;
		txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_PWM1_pulse( int pulse )//设置PWM1脉宽
    {
    	String txt="E8a3";
    	String vs=String.format("%02x", pulse );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_PWM2_pulse( int pulse )//设置PWM2脉宽
    {
    	String txt="E8a4";
    	String vs=String.format("%02x", pulse );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_PWM3_pulse( int pulse )//设置PWM3脉宽
    {
    	String txt="E8a5";
    	String vs=String.format("%02x", pulse );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_PWM4_pulse( int pulse )//设置PWM4脉宽
    {
    	String txt="E8a6";
    	String vs=String.format("%02x", pulse );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    public void set_PWM_ALL_pulse( int PWM1_pulse,int PWM2_pulse,int PWM3_pulse,int PWM4_pulse )//设置PWM4脉宽
    {
    	String txt="E8a7";
    	String vs=String.format("%02x", PWM1_pulse );
    	txt =txt+vs;
    	vs=String.format("%02x", PWM2_pulse );
    	txt =txt+vs;
    	vs=String.format("%02x", PWM3_pulse );
    	txt =txt+vs;
    	vs=String.format("%02x", PWM4_pulse );
    	txt =txt+vs;
    	
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    
    
    public void set_AV_OPEN( int p )//设置AV棒工作模式
    {
    	String txt="E9a501";
    	String vs=String.format("%02x", p );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_AV_PULSE( int p )//设置AV棒工作模式
    {
    	String txt="E9a502";
    	String vs=String.format("%02x", p );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    
    public void set_LED_Mode( int i )//设备LED工作模式   设置LED灯模式 可设置--（面板模式 = 51、固定模式 =（1-25）、自定义模式 = 50
    {
    	String txt="E9b101";
    	String vs=String.format("%02x", i );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_LED_Brightness( int i )//设备亮度( 0 - 255 )
    {
    	String txt="E9b102";
    	String vs=String.format("%02x", i );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_LED_T_J_F( int i )//设置LED灯( 跳变=1、渐变=2、频闪=3 ）
    {
    	String txt="E9b103";
    	String vs=String.format("%02x", i );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_LED_Speed( int i )//设置LED灯速度  ( 0-100 )
    {
    	String txt="E9b104";
    	String vs=String.format("%02x", i );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_LED_Custom_LEN( int i )//设置自定义模式的数据长度
    {
    	String txt="E9b1A0";
    	String vs=String.format("%02x", i );
    	txt =txt+vs;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    public boolean set_LED_Custom1( String dd )//设置自定义模式的数据1
    {
    	if( dd==null )return false;
    	int len = dd.length();
    	if( len==24 )
    	{
	    	String txt="E9b1A1";
	    	txt =txt+dd;
	    	WriteBytes= hex2byte( txt.toString().getBytes() );
	    	BluetoothGattCharacteristic gg;
			gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
			gg.setValue(WriteBytes);
			mBluetoothGatt.writeCharacteristic(gg);
			return true;
    	}
    	else return false;
    }
    public boolean set_LED_Custom2( String dd )//设置自定义模式的数据2
    {
    	if( dd==null )return false;
    	int len = dd.length();
    	if( len==24 )
    	{
	    	String txt="E9b1A2";
	    	txt =txt+dd;
	    	WriteBytes= hex2byte( txt.toString().getBytes() );
	    	BluetoothGattCharacteristic gg;
			gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
			gg.setValue(WriteBytes);
			mBluetoothGatt.writeCharacteristic(gg);
			return true;
    	}
    	else return false;
    }
    public boolean set_LED_Custom3( String dd )//设置自定义模式的数据3
    {
    	if( dd==null )return false;
    	int len = dd.length();
    	if( len==24 )
    	{
	    	String txt="E9b1A3";
	    	txt =txt+dd;
	    	WriteBytes= hex2byte( txt.toString().getBytes() );
	    	BluetoothGattCharacteristic gg;
			gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
			gg.setValue(WriteBytes);
			mBluetoothGatt.writeCharacteristic(gg);
			return true;
    	}
    	else return false;
    }
    public boolean set_LED_Custom4( String dd )//设置自定义模式的数据4
    {
    	if( dd==null )return false;
    	int len = dd.length();
    	if( len==24 )
    	{
	    	String txt="E9b1A4";
	    	txt =txt+dd;
	    	WriteBytes= hex2byte( txt.toString().getBytes() );
	    	BluetoothGattCharacteristic gg;
			gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
			gg.setValue(WriteBytes);
			mBluetoothGatt.writeCharacteristic(gg);
			return true;
    	}
    	else return false;
    }
    
    public boolean set_LED_PAD_color( String dd )//设置面板颜色 
    {
    	if( dd==null )return false;
    	int len = dd.length();
    	if( len==8 )
    	{
	    	String txt="E9b1A5";
	    	txt =txt+dd;
	    	WriteBytes= hex2byte( txt.toString().getBytes() );
	    	BluetoothGattCharacteristic gg;
			gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
			gg.setValue(WriteBytes);
			mBluetoothGatt.writeCharacteristic(gg);
			return true;
    	}
    	else return false;
    }
    public void set_LED_OPEN( boolean p )//设置面板颜色 
    {
    	
		String txt="E9b1A9";
		
		if( p==true )txt =txt+"01";
		else  txt =txt+"00";
		
		WriteBytes= hex2byte( txt.toString().getBytes() );
		BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);

    }
    
    
    
    
//  	WriteBytes = data.getBytes();;
//	
//	BluetoothGattCharacteristic gg;
//	gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//	//byte t[]={51,1,2};
//	gg.setValue(WriteBytes);
//	mBluetoothGatt.writeCharacteristic(gg);
    
    
    public void Delay_ms( int ms )
    {
		 try {  
	            Thread.currentThread();  
	            Thread.sleep( ms );  
	        } catch (InterruptedException e) {  
	            e.printStackTrace();  
	        } 
    }
    
    
 
    
    
//    public void read_uuid(  ){
//    	String txt="AAE50111";
//    	WriteBytes= hex2byte(txt.toString().getBytes());
//    	
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//		//byte t[]={51,1,2};
//		gg.setValue(WriteBytes);
//		mBluetoothGatt.writeCharacteristic(gg);
//    }
    
    public Boolean set_uuid( String txt ){//uuid
    	
    	if( txt.length()==36 )
		{
			String v1="",v2="",v3="",v4="";
			v1=txt.substring(8,9);
			v2=txt.substring(13,14);
			v3=txt.substring(18,19);
			v4=txt.substring(23,24);
			if( v1.equals("-")&&v2.equals("-")&&v3.equals("-")&&v4.equals("-") )
			{
				txt=txt.replace("-","");
		    	txt="AAF1"+txt;
		    	WriteBytes= hex2byte(txt.toString().getBytes());
		    	
		    	BluetoothGattCharacteristic gg;
				gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
				//byte t[]={51,1,2};
				gg.setValue(WriteBytes);
				mBluetoothGatt.writeCharacteristic(gg);
				return true;
			}else{
				//Toast toast = Toast.makeText(DeviceControlActivity.this, "提示！UUID输入格式不对", Toast.LENGTH_SHORT); 
    			//toast.show(); 
				return false;
			}
		}else {
			//Toast toast = Toast.makeText(DeviceControlActivity.this, "提示！UUID输入不对", Toast.LENGTH_SHORT); 
			//toast.show(); 
			return false;
		}
    	
    }
//    public void set_func( String mayjor0,String minor0 ){//mayjor minor
//    	
//    	String mayjor="",minor="";
//		String sss=mayjor0;
//		int i = Integer.valueOf(sss).intValue();
//		String vs=String.format("%02x", i);
//		if( vs.length()==2)vs="00"+vs;
//		else if( vs.length()==3)vs="0"+vs;
//		
//		mayjor=vs;
//		
//		sss=minor0;
//		i = Integer.valueOf(sss).intValue();
//		vs=String.format("%02x", i);
//		if( vs.length()==2)vs="00"+vs;
//		else if( vs.length()==3)vs="0"+vs;
//		minor=vs;
//    	
//    	
//    	String txt="AAF21AFF4C000215"+mayjor+minor+"CD00";
//    	WriteBytes= hex2byte(txt.toString().getBytes());
//    	
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//		//byte t[]={51,1,2};
//		gg.setValue(WriteBytes);
//		mBluetoothGatt.writeCharacteristic(gg);
//    }
//    public void uuid_1001_send_data( String value )
//    {
//    	WriteBytes= hex2byte(value.toString().getBytes());
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//
//		gg.setValue(WriteBytes);
//		mBluetoothGatt.writeCharacteristic(gg);
//    }
	
//    public void set_dev_name( String name ){
//    	int length=name.length();
//    	String len=String.valueOf(length);
//    	int ilen=len.length();
//    	String he=String.format("%02X", length);
//    	
//    	name=bin2hex(name);
//    	String txt="AAE4"+he+name;
//    	WriteBytes= hex2byte(txt.toString().getBytes());
//    	
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//		gg.setValue(WriteBytes);
//		mBluetoothGatt.writeCharacteristic(gg);
//    }
    
//    public void out_io_set( String value )
//    {
//    	WriteBytes= hex2byte(value.toString().getBytes());
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//		gg.setValue(WriteBytes);
//		mBluetoothGatt.writeCharacteristic(gg);
//    }
    
    
//    public void set_password( String value ){
//    	String st1=bin2hex(value);
//    	st1="AAE2"+st1;
//    	WriteBytes= hex2byte(st1.toString().getBytes());
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//		gg.setValue(WriteBytes);
//		mBluetoothGatt.writeCharacteristic(gg);
//    }
//    public void set_adv_time( int i )
//    {
//    	if( i==0 ){
//			out_io_set("AA0900");
//		}else if( i==0 ){
//			out_io_set("AA0901");
//		}else if( i==0 ){
//			out_io_set("AA0902");
//		}else if( i==0 ){
//			out_io_set("AA0903");
//		}else {
//			out_io_set("AA0901");
//		}
//    }
    
//    public void password_value( String value )
//    {
//    	//String txt="AAE2"+he+name;
//    	//WriteBytes= hex2byte(value.toString().getBytes());
//    	String txt="AAE2";
//    	value=bin2hex(value);
//    	txt=txt+value;
//    	WriteBytes= hex2byte( txt.toString().getBytes() );
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//		gg.setValue(WriteBytes);
//		mBluetoothGatt.writeCharacteristic(gg);
//    }
//    public void password_enable( boolean p )
//    {
//    	String g_pass="";
//    	if(p){
//    		g_pass="AAE101";
//    	}else{
//    		g_pass="AAE100";
//    	}
//    	WriteBytes= hex2byte(g_pass.toString().getBytes());
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//		gg.setValue(WriteBytes);
//		mBluetoothGatt.writeCharacteristic(gg);
//    }
//    public void userkey( String key )
//    {
//    	String g_pass="AA20";
//    	key=bin2hex(key);
//    	g_pass+=key;
//    	WriteBytes= hex2byte(g_pass.toString().getBytes());
//    	BluetoothGattCharacteristic gg;
//		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//		gg.setValue(WriteBytes);
//		mBluetoothGatt.writeCharacteristic(gg);
//    }
    
    
    public int get_connected_status( List<BluetoothGattService> gattServices )
    {
    	int jdy_ble_server = 0;
    	int jdy_ble_ffe1 = 0;
    	int jdy_ble_ffe2 = 0;
    	
        final String LIST_NAME1 = "NAME";
        final String LIST_UUID1 = "UUID";
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        //mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        
        int count_char = 0;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME1, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID1, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            //Log.d("server_uuid", uuid );
            if( Service_uuid.equals( uuid ) )
            {
            	//Log.d("server_uuid", "jdy ble" );
            	jdy_ble_server = 1;
            }
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME1, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID1, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                count_char++;
                
                if( jdy_ble_server==1 )
                {
                	//Log.d("Characteristic_uuid", uuid );
                	if( Characteristic_uuid_TX.equals( uuid ) )jdy_ble_ffe1=1;
                	else if( Characteristic_uuid_FUNCTION.equals( uuid ) )jdy_ble_ffe2=1;
                }
            }
            //mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        if( jdy_ble_ffe1==1&&jdy_ble_ffe2==1 )return 2;//JDY-06,JDY-06
        else if( jdy_ble_ffe1==1&&jdy_ble_ffe2==0 )return 1;//JDY-09,JDY-10
        else return 0;//不为JDY系列BLE蓝牙
        //return count_char;
    }
    
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	if( UUID.fromString(Characteristic_uuid_TX).equals(characteristic.getUuid()) )
            	{
            		broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            	}
            	else if( UUID.fromString(Characteristic_uuid_FUNCTION).equals(characteristic.getUuid()) )
            	{
            		broadcastUpdate(ACTION_DATA_AVAILABLE1, characteristic);
            	}
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        	if( UUID.fromString(Characteristic_uuid_TX).equals(characteristic.getUuid()) )
        	{
        		broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        	}
        	else if( UUID.fromString(Characteristic_uuid_FUNCTION).equals(characteristic.getUuid()) )
        	{
        		broadcastUpdate(ACTION_DATA_AVAILABLE1, characteristic);
        	}
        }
    };

    

    
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        Log.d("getUuid"," len = " + characteristic.getUuid() );

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
              //  Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
               // Log.d(TAG, "Heart rate format UINT8.");
            }
            //final int heartRate = characteristic.getIntValue(format, 1);
            //Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            //intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }
        else if( UUID.fromString(Characteristic_uuid_TX).equals(characteristic.getUuid()) )
        {
        	//Log.d("Characteristic_uuid_TX", ""+characteristic.getUuid());
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) 
            {
               // final StringBuilder stringBuilder = new StringBuilder(data.length);
               // for(byte byteChar : data)
                  // stringBuilder.append(String.format("%02X", byteChar));
                
                intent.putExtra(EXTRA_DATA, data );// stringBuilder.toString());
            }
        }
        else if( UUID.fromString(Characteristic_uuid_FUNCTION).equals(characteristic.getUuid()) )
        {
        	//Log.i(TAG, "8");
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for(byte byteChar : data)
//                   stringBuilder.append(String.format("%02X", byteChar));
//                intent.putExtra(EXTRA_DATA,stringBuilder.toString());
            	intent.putExtra(EXTRA_DATA1, data );
            }
        }
        
        
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        
        mBluetoothGatt.disconnect();
    }
    public boolean isconnect() {
       
        
       return mBluetoothGatt.connect();
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
