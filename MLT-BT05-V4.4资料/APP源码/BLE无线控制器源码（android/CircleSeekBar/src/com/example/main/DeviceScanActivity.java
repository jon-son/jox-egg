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

package com.example.main;

import java.util.Timer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ble.DeviceListAdapter;
import com.example.jdy_touchuang.AV_Stick;
import com.example.jdy_touchuang.jdy_Activity;
import com.example.jdy_touchuang.jdy_switch_Activity;
import com.example.jdy_touchuang.shengjiangji;
import com.example.jdy_type.Get_type;
import com.example.sensor.jdy_ibeacon_Activity;
import com.example.set.set;
import com.lee.circleseekbar.R;
 
/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public  class DeviceScanActivity extends Activity implements OnClickListener
{
   // private LeDeviceListAdapter mLeDeviceListAdapter;
	Get_type mGet_type;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;
    
    private DeviceListAdapter mDevListAdapter;
	ToggleButton tb_on_off;
	TextView btn_searchDev;
	Button btn_aboutUs;
	ListView lv_bleList;
	
	byte dev_bid;
	
	Timer timer;
	
	String APP_VERTION = "1002";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jdy_activity_main);
        
        this.setTitle("BLE无线控制器");
        //getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();
        
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 如果本地蓝牙没有开启，则开启  
        if (!mBluetoothAdapter.isEnabled()) 
        {
            // 我们通过startActivityForResult()方法发起的Intent将会在onActivityResult()回调方法中获取用户的选择，比如用户单击了Yes开启，  
            // 那么将会收到RESULT_OK的结果，  
            // 如果RESULT_CANCELED则代表用户不愿意开启蓝牙  
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);  
            startActivityForResult(mIntent, 1);  
            // 用enable()方法来开启，无需询问用户(实惠无声息的开启蓝牙设备),这时就需要用到android.permission.BLUETOOTH_ADMIN权限。  
            // mBluetoothAdapter.enable();  
            // mBluetoothAdapter.disable();//关闭蓝牙  
        }
        
        
        lv_bleList = (ListView) findViewById(R.id.lv_bleList);
        
        
//		//tb_on_off = (ToggleButton) findViewById(R.id.tb_on_off);
//		btn_searchDev = (TextView) findViewById(R.id.btn_searchDev);
//		btn_aboutUs = (Button) findViewById(R.id.btn_aboutUs);
//		
//		btn_aboutUs.setText("");
//		btn_aboutUs.setOnClickListener(this);
//		btn_searchDev.setOnClickListener(this);
		
		mDevListAdapter = new DeviceListAdapter( mBluetoothAdapter,DeviceScanActivity.this );
		dev_bid = (byte)0x88;//88 是JDY厂家VID码
		mDevListAdapter.set_vid( dev_bid );//用于识别自家的VID相同的设备，只有模块的VID与APP的VID相同才会被搜索得到
		lv_bleList.setAdapter( mDevListAdapter.init_adapter( ) );
		
		//mGet_type = new Get_type();
		
		/*
		// ��������Ƿ�������toggleButton״̬
		if (mBluetoothAdapter.isEnabled()) {
			tb_on_off.setChecked(true);
		} else {
			tb_on_off.setChecked(false);
		}*/
		
		lv_bleList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mDevListAdapter.get_count() > 0) 
				{
					
					Byte vid_byte =  mDevListAdapter.get_vid( position );//返回136表示是JDY厂家模块
					//String vid_str =String.format("%02x", vid_byte );
					//Toast.makeText( DeviceScanActivity.this,"设备VID:"+vid_str, Toast.LENGTH_SHORT).show();
//				    Toast.makeText( DeviceScanActivity.this, "type:"+mDevListAdapter.get_item_type(position), Toast.LENGTH_SHORT).show(); 
					
					if( vid_byte==dev_bid )//JDY厂家VID为0X88， 用户的APP不想搜索到其它厂家的JDY-08模块的话，可以设备一下 APP的VID，此时模块也需要设置，
						                      //模块的VID与厂家APP的VID要一样，APP才可以搜索得到模块VID与APP一样的设备
					switch( mDevListAdapter.get_item_type(position) )
					{
						case JDY:////为标准透传模块
						{
							 BluetoothDevice device1 = mDevListAdapter.get_item_dev(position);
						        if (device1 == null) return;
						        Intent intent1 = new Intent(DeviceScanActivity.this,jdy_Activity.class);;
						        intent1.putExtra(jdy_Activity.EXTRAS_DEVICE_NAME, device1.getName());
						        intent1.putExtra(jdy_Activity.EXTRAS_DEVICE_ADDRESS, device1.getAddress());
						       // if (mScanning) 
						        {
						        	
						        	mDevListAdapter.scan_jdy_ble( false );;
						            mScanning = false;
						        }
						        startActivity(intent1);
							break;
						}
						case JDY_iBeacon:////为iBeacon设备
						{
							 BluetoothDevice device1 = mDevListAdapter.get_item_dev(position);
						        if (device1 == null) return;
						        Intent intent1 = new Intent(DeviceScanActivity.this,jdy_ibeacon_Activity.class);;
						        intent1.putExtra( jdy_ibeacon_Activity.EXTRAS_DEVICE_NAME, device1.getName());
						        intent1.putExtra( jdy_ibeacon_Activity.EXTRAS_DEVICE_ADDRESS, device1.getAddress());
						        
						        intent1.putExtra( jdy_ibeacon_Activity.EXTRAS_DEVICE_UUID, mDevListAdapter.get_iBeacon_uuid( position ));
						        intent1.putExtra( jdy_ibeacon_Activity.EXTRAS_DEVICE_MAJOR, mDevListAdapter.get_ibeacon_major( position ));
						        intent1.putExtra( jdy_ibeacon_Activity.EXTRAS_DEVICE_MINOR, mDevListAdapter.get_ibeacon_minor( position ));
						        
						       // if (mScanning) 
						        {
						        	mDevListAdapter.scan_jdy_ble( false );;
						            mScanning = false;
						        }
						        startActivity(intent1);
							break;
						}
						case sensor_temp://温度传感器
						{
							
							break;
						}
						case JDY_KG://开关控制APP
						{
							 BluetoothDevice device1 = mDevListAdapter.get_item_dev(position);
						        if (device1 == null) return;
						        Intent intent1 = new Intent(DeviceScanActivity.this,jdy_switch_Activity.class);;
						        intent1.putExtra(jdy_switch_Activity.EXTRAS_DEVICE_NAME, device1.getName());
						        intent1.putExtra(jdy_switch_Activity.EXTRAS_DEVICE_ADDRESS, device1.getAddress());
						       // if (mScanning) 
						        {
						        	mDevListAdapter.scan_jdy_ble( false );;
						            mScanning = false;
						        }
						        startActivity(intent1);
							break;
						}
						case JDY_KG1://开关控制APP
						{
							 BluetoothDevice device1 = mDevListAdapter.get_item_dev(position);
						        if (device1 == null) return;
						        Intent intent1 = new Intent(DeviceScanActivity.this,shengjiangji.class);;
						        intent1.putExtra(jdy_switch_Activity.EXTRAS_DEVICE_NAME, device1.getName());
						        intent1.putExtra(jdy_switch_Activity.EXTRAS_DEVICE_ADDRESS, device1.getAddress());
						       // if (mScanning) 
						        {
						        	mDevListAdapter.scan_jdy_ble( false );;
						            mScanning = false;
						        }
						        startActivity(intent1);
							break;
						}
						case JDY_AMQ://massager 按摩器APP
						{
							 BluetoothDevice device1 = mDevListAdapter.get_item_dev(position);
						        if (device1 == null) return;
						        Intent intent1 = new Intent(DeviceScanActivity.this,AV_Stick.class);
						        intent1.putExtra( AV_Stick.EXTRAS_DEVICE_NAME, device1.getName() );
						        intent1.putExtra( AV_Stick.EXTRAS_DEVICE_ADDRESS, device1.getAddress() );
						       // if (mScanning) 
						        { 
						        	mDevListAdapter.scan_jdy_ble( false );;
						            mScanning = false;
						        }
						        startActivity(intent1);
							break;
						}
						case JDY_LED1:// LED灯 APP 测试版本
						{/*
							 BluetoothDevice device1 = mDevListAdapter.get_item_dev(position);
						        if (device1 == null) return;
						        Intent intent1 = new Intent(DeviceScanActivity.this,MainActivity.class);
						        intent1.putExtra( MainActivity.EXTRAS_DEVICE_NAME, device1.getName() );
						        intent1.putExtra( MainActivity.EXTRAS_DEVICE_ADDRESS, device1.getAddress() );
						       // if (mScanning) 
						        {
						        	mDevListAdapter.scan_jdy_ble( false );;
						            mScanning = false;
						        }
						        startActivity(intent1);*/
							break;
						}
						case JDY_LED2:// LED灯 APP 正试版本
						{
							
							break;
						}
						
						
						default:
							break;
					}
					
					
					/*
					BluetoothDevice device = mDevListAdapter.getItem(position);
					Intent intent = new Intent(DeviceScanActivity.this,
							DeviceControlActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("BLEDevName", device.getName());
					bundle.putString("BLEDevAddress", device.getAddress());
					intent.putExtras(bundle);
					DeviceScanActivity.this.startActivity(intent);
					*/
					
					
				}
			}
		});
		
		
      Message message = new Message();  
      message.what = 100;  
      handler.sendMessage(message);  
		

		
    }
    
    Handler handler = new Handler() {  
        public void handleMessage(Message msg) {  
        	if (msg.what == 100) 
        	{  
            }  
        	
        	
            super.handleMessage(msg);  
        }

		private void setTitle(String hdf) {
			// TODO 自动生成的方法存根
			
		};  
    }; 
    
    public static boolean turnOnBluetooth()
        {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null)
            {
                return bluetoothAdapter.enable();
            }
            return false;
        }
    
	public void onClick(View v) {
		switch (v.getId()) {
		case 0:
			break;
//		case R.id.btn_searchDev:
//			//scanLeDevice(true);
//			break;

//		case R.id.btn_aboutUs:
//			 Intent intent = new Intent();
//		        intent.setAction("android.intent.action.VIEW");
//		        Uri content_url = Uri.parse("https://item.taobao.com/item.htm?spm=a1z10.1-c.w4004-11559702484.2.uKkX9H&id=44163359933");
//		        intent.setData(content_url);
//		        startActivity(intent);
//			break;
		}
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_menu, menu);

            menu.findItem(R.id.scan_menu_set).setVisible(true);
            menu.findItem(R.id.scan_menu_id).setActionView(null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_menu_set:
            	//mDevListAdapter.clear();
            	//mDevListAdapter.scan_jdy_ble( true );
			        Intent intent1 = new Intent(DeviceScanActivity.this,set.class);;
			        startActivity(intent1);
                break;
            case R.id.scan_menu_set1:
            {
            	mDevListAdapter.clear();
            	scanLeDevice( true );
            }
            break;
        }
        return true;
    }
    
/*
    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
        
    }*/
    
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }
*/
	private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mDevListAdapter.scan_jdy_ble( false );
                    //invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mDevListAdapter.scan_jdy_ble( true );
        } else {
            mScanning = false;
            mDevListAdapter.scan_jdy_ble( false );
        }
       // invalidateOptionsMenu();
    }
/*
    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
            	//String gg=device.getAddress().toString().trim();
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	//String gg=device.getAddress().toString().trim();
                	//Log.i("tag", gg);
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
    */
    
    
    
//    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//        @Override
//        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
//            if (Looper.myLooper() == Looper.getMainLooper()) {
//            	if( mGet_type.dv_type( scanRecord  )==JDY_type.JDY )
//            	{
//	            	//mDevListAdapter..addDevice(device);
//					//mDevListAdapter.notifyDataSetChanged();
//            	}
//            } else {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                    	
//                    	JDY_type m_tyep = mGet_type.dv_type( scanRecord  );;
//                    	
//                    	if( m_tyep==JDY_type.JDY )//为JDY厂家BLE透传模块
//                    	{
//	                    //	mDevListAdapter.addDevice(device);
//	    					//mDevListAdapter.notifyDataSetChanged();
//                    	}
//                    	else if( m_tyep==JDY_type.JDY_iBeacon )//为iBeacon或传感器
//                    	{
//                    		
//                    	}
//                    	
//                    }
//                });
//            }
//        }
//    };
    

//	private BluetoothAdapter.LeScanCallback mLeScanCallback = new LeScanCallback() {
//
//		@Override
//		public void onLeScan(final BluetoothDevice device, int rssi,
//				byte[] scanRecord) {
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					mDevListAdapter.addDevice(device);
//					mDevListAdapter.notifyDataSetChanged();
//				}
//			});
//		}
//	};

	@Override
	protected void onResume() {//打开APP时扫描设备
		super.onResume();
		scanLeDevice(true);
		
		//mDevListAdapter.scan_jdy_ble( false );
	}

	@Override
	protected void onPause() {//停止扫描
		super.onPause();
		//scanLeDevice(false);
		mDevListAdapter.scan_jdy_ble( false );
	}

    
    
//
//	class DeviceListAdapter extends BaseAdapter {
//
//		private List<BluetoothDevice> mBleArray;
//		private ViewHolder viewHolder;
//
//		public DeviceListAdapter() {
//			mBleArray = new ArrayList<BluetoothDevice>();
//		}
//
//		public void addDevice(BluetoothDevice device) {
//			if (!mBleArray.contains(device)) {
//				mBleArray.add(device);
//			}
//		}
//		public void clear(){
//			mBleArray.clear();
//		}
//
//		@Override
//		public int getCount() {
//			return mBleArray.size();
//		}
//
//		@Override
//		public BluetoothDevice getItem(int position) {
//			return mBleArray.get(position);
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			if (convertView == null) {
//				convertView = LayoutInflater.from(DeviceScanActivity.this).inflate(
//						R.layout.listitem_device, null);
//				viewHolder = new ViewHolder();
//				viewHolder.tv_devName = (TextView) convertView
//						.findViewById(R.id.device_name);
//				viewHolder.tv_devAddress = (TextView) convertView
//						.findViewById(R.id.device_address);
//				convertView.setTag(viewHolder);
//			} else {
//				convertView.getTag();
//			}
//
//			// add-Parameters
//			BluetoothDevice device = mBleArray.get(position);
//			String devName = device.getName();
//			if (devName != null && devName.length() > 0) {
//				viewHolder.tv_devName.setText(devName);
//			} else {
//				viewHolder.tv_devName.setText("unknow-device");
//			}
//			viewHolder.tv_devAddress.setText(device.getAddress());
//
//			return convertView;
//		}
//
//	}
//
//	class ViewHolder {
//		TextView tv_devName, tv_devAddress;
//	}




	public boolean isNetworkConnected(Context context) {  
		      if (context != null) {  
		          ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
		                  .getSystemService(Context.CONNECTIVITY_SERVICE);  
		          NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
		          if (mNetworkInfo != null) {  
		              return mNetworkInfo.isAvailable();  
		          }  
		      }  
		     return false;  
		 }
    
    
    
    
    
}