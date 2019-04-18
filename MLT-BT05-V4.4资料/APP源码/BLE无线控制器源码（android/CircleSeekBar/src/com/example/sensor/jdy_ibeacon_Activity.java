
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

package com.example.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ble.BluetoothLeService;
import com.example.jdy_touchuang.jdy_Activity;
import com.lee.circleseekbar.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class jdy_ibeacon_Activity extends Activity {
    private final static String TAG = jdy_Activity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    
    public static final String EXTRAS_DEVICE_UUID = "DEVICE_UUID";
    public static final String EXTRAS_DEVICE_MAJOR = "DEVICE_MAJOR";
    public static final String EXTRAS_DEVICE_MINOR = "DEVICE_MINOR";
    
    
    
	private StringBuffer sbValues;
    
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private String ibeacon_uuid_value;
    private String ibeacon_major_value;
    private String ibeacon_minor_value;
    private String ibeacon_password_value;
    private String ibeacon_new_password_value;
    private String ibeacon_ad_intvel_value;
    
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    
    
    boolean connect_status_bit=false;
    
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 1000;
    
    private int i = 0;  
    private int TIME = 1000; 
    
    ToggleButton key1,key2,key3,key4;
    
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //mConnected = true;
                
                
                connect_status_bit=true;
               
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                
//                updateConnectionState(R.string.disconnected);
                connect_status_bit=false;
                show_view(false);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } 
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) //接收FFE1串口透传数据通道数据
            {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            	//byte data1;
            	//intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);//  .getByteExtra(BluetoothLeService.EXTRA_DATA, data1);
                displayData( intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA) );
            } 
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE1.equals(action)) //接收FFE2功能配置返回的数据
            {
                displayData1( intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA1) );
            }
            //Log.d("", msg)
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                	
//                	Log.i("tag", "uu");
//                    if (mGattCharacteristics != null) {
//                        final BluetoothGattCharacteristic characteristic =
//                                mGattCharacteristics.get(groupPosition).get(childPosition);
//                        final int charaProp = characteristic.getProperties();
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                            // If there is an active notification on a characteristic, clear
//                            // it first so it doesn't update the data field on the user interface.
//                            if (mNotifyCharacteristic != null) {
//                                mBluetoothLeService.setCharacteristicNotification(
//                                        mNotifyCharacteristic, false);
//                                mNotifyCharacteristic = null;
//                            }
//                            mBluetoothLeService.readCharacteristic(characteristic);
//                        }
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                            mNotifyCharacteristic = characteristic;
//                            mBluetoothLeService.setCharacteristicNotification(
//                                    characteristic, true);
//                        }
//                        return true;
//                    }
                    return false;
                }
    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
       // mDataField.setText(R.string.no_data);
    }

    Button send_button;
    Button enable_button;
    Button IBeacon_set_button;
    
    EditText txd_txt,uuid_1001_ed,rx_data_id_1;
    
    EditText ibeacon_uuid;
    EditText mayjor_txt,minor_txt;
    
    EditText dev_Name;
    Button name_button;
    
    EditText password_ed;//密码值
    Button password_enable_bt;//密码开关
    Button password_wrt;//密码写入Button
    
    Button adv_time1,adv_time2,adv_time3,adv_time4;
    
    boolean pass_en=false;
    
    Button clear_button;
    
    
    private Button IO_H_button,IO_L_button;//out io
    Timer timer = new Timer();  

    
    
    TextView device_address;//ibeacon mac address
    EditText ib_name_text;// ibeacon name
    EditText ib_uuid_text;//ibeacon uuid
    EditText ib_mayjor_text;//ibeacon major
    EditText ib_minor_text;//ibeacon minor
    EditText pass_value;//当前设备密码
    EditText new_pass_value;//新密码
    Button password_write;//密码写入按钮
    Button ib_button,set_dev_name;//配置参数按钮
    
    private int[] color_arry;  
    
    String dev_password = "123456";//设备默认出厂密码为 123456
    String current_dev_password = "123456";//当前APP 密码
    String new_dev_password = "";
    
    TextView set_name_success_id,set_password_success_id,set_major_success_id,set_minor_success_id,set_uuid_success_id;
    
    
    String current_uuid,current_major,current_minor;
    
    void show_view( boolean p )
    {
    	if(p){
    		ib_button.setEnabled(true);
    		password_write.setEnabled(true);
    		set_dev_name.setEnabled(true);
    	}else{
    		ib_button.setEnabled(false);
    		password_write.setEnabled(false);
    		set_dev_name.setEnabled(false);
    	}
    }
    
    public void delay(int ms){
		try {
            Thread.currentThread();
			Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
	 }	
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ibeacon_view);
        setTitle("iBeacon");
        
        //color_arry=new int[]{R.color.gray,R.color.bule,R.color.green,R.color.yellow}; 
        //int myColor=getResources().getColor(color_arry[0]);  
        
        device_address = (TextView)findViewById(R.id.device_address);//mac 地址
        ib_name_text = (EditText)findViewById(R.id.ib_name_text);//设备名
        ib_uuid_text = (EditText)findViewById(R.id.ib_uuid_text);//ibeacon uuid
        ib_mayjor_text = (EditText)findViewById(R.id.ib_mayjor_text);//ibeacon major
        ib_minor_text = (EditText)findViewById(R.id.ib_minor_text);//ibeacon minor
        pass_value = (EditText)findViewById(R.id.pass_value);//当前设备密码
        new_pass_value = (EditText)findViewById(R.id.new_pass_value);//新密码
        password_write = (Button)findViewById(R.id.password_write);//密码写入按钮
        //password_write.setTextColor(myColor);  
        
        ib_button = (Button)findViewById(R.id.ib_button);//配置按钮
        //ib_button.setTextColor(myColor);  
        
        set_dev_name = (Button)findViewById(R.id.set_dev_name);//更新蓝牙名
        
        set_dev_name.setOnClickListener( listener );//配置按钮
        password_write.setOnClickListener( listener );///密码写入按钮
        ib_button.setOnClickListener( listener );//配置按钮
        
        
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        
        ibeacon_uuid_value = intent.getStringExtra(EXTRAS_DEVICE_UUID);
        ibeacon_major_value = intent.getStringExtra(EXTRAS_DEVICE_MAJOR);
        ibeacon_minor_value = intent.getStringExtra(EXTRAS_DEVICE_MINOR);
        
        current_uuid = ibeacon_uuid_value;
        current_major = ibeacon_major_value;
        current_minor = ibeacon_minor_value;
        
        
        ib_name_text.setText( mDeviceName );
        device_address.setText( mDeviceAddress );
        ib_uuid_text.setText( ibeacon_uuid_value );
        ib_mayjor_text.setText( ibeacon_major_value );
        ib_minor_text.setText( ibeacon_minor_value );
        
        
		set_name_success_id = (TextView)findViewById(R.id.set_name_success_id);
		set_password_success_id = (TextView)findViewById(R.id.set_password_success_id);
		set_major_success_id = (TextView)findViewById(R.id.set_major_success_id);
		set_minor_success_id = (TextView)findViewById(R.id.set_minor_success_id);
		set_uuid_success_id = (TextView)findViewById(R.id.set_uuid_success_id);
        
        //setTitle( mDeviceName );
        
        /*
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        //mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
       // mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        
        
        
        send_button=(Button)findViewById(R.id.tx_button);//send data 1002
        send_button.setOnClickListener(listener);//设置监听  
        
        clear_button=(Button)findViewById(R.id.clear_button);//send data 1002
        clear_button.setOnClickListener(listener);//设置监听  
        
        txd_txt=(EditText)findViewById(R.id.tx_text);//1002 data
        txd_txt.setText("0102030405060708090A0102030405060708090A0102030405060708090A0102030405060708090A");
        txd_txt.clearFocus();
        
        rx_data_id_1=(EditText)findViewById(R.id.rx_data_id_1);//1002 data
        rx_data_id_1.setText("");

        key1 = (ToggleButton)findViewById(R.id.toggleButton1);
        key2 = (ToggleButton)findViewById(R.id.toggleButton2);
        key3 = (ToggleButton)findViewById(R.id.toggleButton3);
        key4 = (ToggleButton)findViewById(R.id.toggleButton4);
        
        key1.setOnClickListener( OnClickListener_listener );//设置监听  
        key2.setOnClickListener( OnClickListener_listener );//设置监听  
        key3.setOnClickListener( OnClickListener_listener );//设置监听  
        key4.setOnClickListener( OnClickListener_listener );//设置监听  
        
        
        sbValues = new StringBuffer();
        */
        
        show_view(false);
        mHandler = new Handler();
        
        
        //timer.schedule(task, 500, 500); // 1s后执行task,经过1s再次执行  
        
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
        	
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
       // boolean sg;
        getActionBar().setTitle(mDeviceName+"   iBeacon");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //getActionBar().setTitle( "="+BluetoothLeService );
        //mDataField.setText("="+sg );
        //updateConnectionState(R.string.connecting);
        
        
        Message message = new Message();  
        message.what = 1;  
        handler.sendMessage(message);  
       // mBluetoothLeService.connect(mDeviceAddress);
        
        get_pass();
    }
    

    public void enable_pass()
    {
		 mBluetoothLeService.Delay_ms( 100 ); 
		 mBluetoothLeService.set_APP_PASSWORD( password_value );
    }
    String password_value = "123456";
    public void get_pass()
    {
        password_value = getSharedPreference( "DEV_PASSWORD_LEY_1000" );
        if( password_value!=null||password_value!="")
        {
        	if( password_value.length()==6 )
        	{
        		
        	}else password_value = "123456" ;
        }else password_value = "123456" ;
        
    }
	//---------------------------------------------------------------------------------应用于存储选择TAB的列表index
	public String getSharedPreference(String key) 
	{
		//同样，在读取SharedPreferences数据前要实例化出一个SharedPreferences对象 
		SharedPreferences sharedPreferences= getSharedPreferences("test", 
		Activity.MODE_PRIVATE); 
		// 使用getString方法获得value，注意第2个参数是value的默认值 
		String name =sharedPreferences.getString(key, ""); 
		return name;
	}
	public void setSharedPreference(String key, String values) 
	{
		//实例化SharedPreferences对象（第一步） 
		SharedPreferences mySharedPreferences= getSharedPreferences("test", 
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
    
    
    Handler handler = new Handler() {  
        public void handleMessage(Message msg) {  
        if (msg.what == 1) 
        	{
                //tvShow.setText(Integer.toString(i++));  
            	//scanLeDevice(true);
            	if (mBluetoothLeService != null) 
            	{
                	if( mConnected==false )
                	{
                		//updateConnectionState(R.string.connecting);
                		final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                		Log.d(TAG, "Connect request result=" + result);
                	}
                }
            	
            }  
        if ( msg.what == 2 ) 
        {
//        	if( mBluetoothLeService!=null )
//        	{
//        		String ss = mBluetoothLeService.get_mem_data( "pass_value");
//        		if( ss!=null )pass_value.setText( ss );//
//        		
//        	}
        	
        }
            super.handleMessage(msg);  
        };  
    };  
    TimerTask task = new TimerTask() {  
    	  
        @Override  
        public void run() {  
            // 需要做的事:发送消息  
            Message message = new Message();  
            message.what = 2;  
            handler.sendMessage(message);  
        }  
    }; 

    
    int sele_adv=0;
    Button.OnClickListener listener = new Button.OnClickListener(){//创建监听对象    
        public void onClick(View v){
            //String strTmp="点击Button02";    
            //Ev1.setText(strTmp);   
        	switch( v.getId())
        	{
        	case R.id.set_dev_name://更新蓝牙名
        	{
        		set_name_success_id.setText( "" );
        		mBluetoothLeService.set_name( ib_name_text.getText().toString() );
        		break;
        	}
        	case R.id.password_write ://更改密码
        		if( connect_status_bit )
      		  {
        			set_password_success_id.setText( "" );
        			
//        			if( mBluetoothLeService.set_password( pass_value.getText().toString(), new_pass_value.getText().toString() )==false )
//        			{
//        				Toast.makeText(jdy_ibeacon_Activity.this, "密码长度不对，密码必须为6位数字", Toast.LENGTH_SHORT).show();
//        			}
        		    
        			mBluetoothLeService.set_password( pass_value.getText().toString(), new_pass_value.getText().toString() );
        			
//        	    	String txt="E551";
//        	    	String value=mBluetoothLeService.String_to_HexString0("123456");
//        	    	txt=txt+value;
//        	    	value=mBluetoothLeService.String_to_HexString0("123456");
//        	    	txt=txt+value;
//        	    	set_password_success_id.setText( txt );
//        	    	Toast.makeText(jdy_ibeacon_Activity.this, "ps:"+txt, Toast.LENGTH_SHORT).show();
//        	    	
//        	    	mBluetoothLeService.function_data( txt );
        	    	
            		//String tx_string=txd_txt.getText().toString().trim();
            		//mBluetoothLeService.txxx( tx_string,true );//发送字符串数据
            		//mBluetoothLeService.txxx( tx_string,false );//发送HEX数据
      		  }else{
      			  //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show(); 
      			  Toast toast = Toast.makeText(jdy_ibeacon_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT); 
      			  toast.show(); 
      		  }
        		break;
        	case R.id.ib_button://一键配置
        	{
        		set_major_success_id.setText( "" );
        		set_minor_success_id.setText( "" );
        		set_uuid_success_id.setText( "" );
        		current_dev_password = pass_value.getText().toString();
        		if( dev_password.equals( current_dev_password ) )
        		{
        			if( current_uuid.equals( ib_uuid_text.getText().toString() ) )//相同不更新
        			{
        				set_uuid_success_id.setText( "输入的iBeacon UUID与当前设备UUID相同无需更新" );
        			}else//不相同更新
        			{
        				if( mBluetoothLeService.set_ibeacon_UUID( ib_uuid_text.getText().toString() )==false )
        					set_uuid_success_id.setText( "输入的iBeacon UUID格式有错！" );
        			}
        			
        			mBluetoothLeService.Delay_ms( 20);
        			if( current_major.equals( ib_mayjor_text.getText().toString() ) )//相同不更新
        			{
        				set_major_success_id.setText( "输入的iBeacon major与当前设备major相同无需更新" );
        			}else//不相同更新
        			{
        				if( mBluetoothLeService.set_ibeacon_MAJOR( ib_mayjor_text.getText().toString() )==false )
        					set_major_success_id.setText( "iBeacon Major值不能为空" );
        			}
        			
        			mBluetoothLeService.Delay_ms( 20);
        			if( current_minor.equals( ib_minor_text.getText().toString() ) )//相同不更新
        			{
        				set_minor_success_id.setText( "输入的iBeacon minor与当前设备minor相同无需更新" );
        			}else//不相同更新
        			{
        				if( mBluetoothLeService.set_ibeacon_MIMOR( ib_minor_text.getText().toString() )==false )
        					set_minor_success_id.setText( "iBeacon Minor值不能为空" );
        			}
        	
        		}else
        		{
        			set_major_success_id.setText( "APP密码与设备密码不符！" );
        		}
        		


        	}
        	break;
        		default :
        			break;
        	}
        }    
  
    };  
    @Override
    protected void onResume() {
        super.onResume();
        
//        if (mBluetoothLeService != null) {
//        	
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.d(TAG, "Connect request result=" + result);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mBluetoothLeService.disconnect();
//        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeService.disconnect();
        mBluetoothLeService.set_mem_data( mDeviceAddress,  pass_value.getText().toString() );
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        timer.cancel();
        timer=null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    } 
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void updateConnectionState(final int resourceId) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                //mConnectionState.setText(resourceId);
//            }
//        });
//    }
String da="";
int len_g = 0;
    private void displayData( byte[] data1 ) //接收FFE1串口透传数据通道数据
    {
		//String head1,data_0;
		/*
		head1=data1.substring(0,2);
		data_0=data1.substring(2);
		*/
    	//da = da+data1+"\n";
    	if (data1 != null )
    	{
    		if( data1.length > 0)
    		{
//    		//sbValues.insert(0, data1);
//    		//sbValues.indexOf( data1 );
//    		String res = new String( data1 );
//    		
//    		sbValues.append( res ) ;
//			//mDataField.setText( data1 );
//    		len_g += data1.length;
//    		//da = data1+da;
//    		
//    		rx_data_id_1.setText( sbValues );// data1 );
//    		rx_data_id_1.setSelection(sbValues.length());
//    		if( sbValues.length()>=5000 )sbValues.delete(0,sbValues.length());
//    		mDataField.setText( ""+len_g );
    		
    		//rx_data_id_1.setGravity(Gravity.BOTTOM);
    		//rx_data_id_1.setSelection(rx_data_id_1.getText().length());
    		}
    	}
    	
    }
    private void displayData1( byte[] data1 ) //接收FFE2功能配置返回的数据
    {
    	String str1 = mBluetoothLeService.bytesToHexString1( data1,0 );//将接收的十六进制数据转换成十六进制字符串
    	//String str2 = mBluetoothLeService.byte_to_String( data1,1 );
    	
    	//Toast.makeText(jdy_ibeacon_Activity.this, "function_rx:"+str1, Toast.LENGTH_SHORT).show(); 
    	
    	if( data1.length==2&&data1[0]==(byte) 0x51 )//更新密码成功
    	{
    		if(data1[1]==(byte) 0x01)
    		{
    			set_password_success_id.setText( "密码更新成功" );
    			//Toast.makeText(jdy_ibeacon_Activity.this, "密码更新成功", Toast.LENGTH_SHORT).show(); 
    		}
    		else 
    		{
    			set_password_success_id.setText( "密码更新失败" );
    			//Toast.makeText(jdy_ibeacon_Activity.this, "密码更新成功", Toast.LENGTH_SHORT).show(); 
    		}
    	}
    	else if( data1.length==7&&data1[0]==(byte) 0x52 )//读取密码
    	{
    		dev_password = mBluetoothLeService.byte_to_String( data1,1 );
    		//Toast.makeText(jdy_ibeacon_Activity.this, "function_rx:"+current_dev_password, Toast.LENGTH_SHORT).show(); 
    	}
    	else if( data1.length==2&&data1[0]==(byte) 0x61&&data1[1]==(byte) 0x01 )//更新蓝牙名成功
    	{
    		//Toast.makeText(jdy_ibeacon_Activity.this, "蓝牙名更新成功", Toast.LENGTH_SHORT).show(); 
    		set_name_success_id.setText( "iBeacon 名更新成功" );
    	}
    	else if( data1.length==2&&data1[0]==(byte) 0x11&&data1[1]==(byte) 0x01 )//
    	{
    		set_uuid_success_id.setText( "iBeacon uuid更新成功" );
    	}
    	else if( data1.length==2&&data1[0]==(byte) 0x21&&data1[1]==(byte) 0x01 )//
    	{
    		set_major_success_id.setText( "iBeacon Major更新成功" );
    	}
    	else if( data1.length==2&&data1[0]==(byte) 0x31&&data1[1]==(byte) 0x01 )//
    	{
    		set_minor_success_id.setText( "iBeacon Minor更新成功" );
    	}
    	
    	
    	
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
       

    	if (gattServices == null) return;

        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )==2 )//表示为JDY-06、JDY-08系列蓝牙模块
        {
	        if( connect_status_bit )
			  {
	        	mConnected = true;
	        	show_view( true );
				
	        	String ss = mBluetoothLeService.get_mem_data( mDeviceAddress);
	        	if( ss!=null )pass_value.setText( ss );//
	        	
	        	 mBluetoothLeService.Delay_ms( 100 );  
				 mBluetoothLeService.enable_JDY_ble( 0 );
				 mBluetoothLeService.Delay_ms( 100 );  
				 mBluetoothLeService.enable_JDY_ble( 1 );
				 mBluetoothLeService.Delay_ms( 100 );  
				 enable_pass();
//				 byte[] WriteBytes = new byte[2];
//				 WriteBytes[0] = (byte) 0xE7;
//				 WriteBytes[1] = (byte) 0xf6;
//				 mBluetoothLeService.function_data( WriteBytes );// 发送读取所有IO状态
				 mBluetoothLeService.get_password( pass_value.getText().toString() );
				 
				 //updateConnectionState(R.string.connected);
			  }else{
				  //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show(); 
				  Toast toast = Toast.makeText(jdy_ibeacon_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT); 
				  toast.show(); 
			  }
        }
        else if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )==1 )//表示为JDY-09、JDY-10系列蓝牙模块
        {
	        if( connect_status_bit )
			  {
	        	mConnected = true;
	        	show_view( true );
				
	        	String ss = mBluetoothLeService.get_mem_data( mDeviceAddress );
	        	if( ss!=null )pass_value.setText( ss );//
	        	
	        	mBluetoothLeService.Delay_ms( 100 );  
				mBluetoothLeService.enable_JDY_ble( 0 );
				enable_pass();
				// updateConnectionState(R.string.connected);
			  }else{
				  //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show(); 
				  Toast toast = Toast.makeText(jdy_ibeacon_Activity.this, "设备没有连接！", Toast.LENGTH_SHORT); 
				  toast.show(); 
			  }
        }else
        {
        	 Toast toast = Toast.makeText(jdy_ibeacon_Activity.this, "提示！此设备不为JDY系列BLE模块", Toast.LENGTH_SHORT); 
			  toast.show(); 
        }
//        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
//                this,
//                gattServiceData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[] {LIST_NAME, LIST_UUID},
//                new int[] { android.R.id.text1, android.R.id.text2 },
//                gattCharacteristicData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[] {LIST_NAME, LIST_UUID},
//                new int[] { android.R.id.text1, android.R.id.text2 }
//        );
//        
//        mGattServicesList.setAdapter(gattServiceAdapter);
        
    }
 
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE1);
        return intentFilter;
    }
}
