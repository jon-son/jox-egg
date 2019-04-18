package com.example.ble;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//import com.example.jdy_type.Get_type;
import com.example.jdy_type.JDY_type;
import com.lee.circleseekbar.R;

public class DeviceListAdapter extends Activity
{
	int list_select_index = 0;
	
//	Get_type mGet_type;
  private	DeviceListAdapter1 list_cell_0;
	BluetoothAdapter apter;
	Context context;
	
	int scan_int = 0;
	int ip = 0;
	
		public String ibeacon_UUID = "";
		public String ibeacon_MAJOR = "";
		public String ibeacon_MINOR = "";
		
		public byte sensor_temp;//传感器温度值十六进制格式-----1byte
		public byte sensor_humid;//传感器湿度值 十六进制格式-----1byte
		
		public byte sensor_batt;//传感器电量十六进制格式-----1byte
		public byte[] sensor_VID;//传感器厂家识别码十六进制格式-----2byte
		
		
		public JDY_type DEV_TYPE ;
		
		Timer timer = new Timer();  
		boolean stop_timer = true;
		
		byte dev_VID = (byte)0x88;
		
		public JDY_type dv_type( byte[] p )
		{
			//Log.d( "out_3=","scan_byte_len:"+ p.length);
			if( p.length!=62 )return null;
			//if( p.length!=0 )return null;
			String str;
			
			/*
			str = String.format( "%02x", p[5] );
			//Log.d( "scan_byte_bit_0=",""+ str);
			str = String.format( "%02x", p[6] );
			//Log.d( "scan_byte_bit_0=",""+ str);
			
			str = String.format( "%02x", p[11] );
			//Log.d( "scan_byte_bit_0=",""+ str);
			str = String.format( "%02x", p[12] );
			//Log.d( "scan_byte_bit_0=",""+ str);
			*/
			
			byte m1 = (byte)((p[18+2]+1)^0x11);////透传模块密码位判断
			str = String.format( "%02x", m1 );
			//Log.d( "out_1","="+ str);
			
			byte m2 = (byte)((p[17+2]+1)^0x22);//透传模块密码位判断
			str = String.format( "%02x", m2 );
			//Log.d( "out_2","="+ str);
			
			/*
			str = String.format( "%02x", p[44] );//0x10 ibeacon
			//Log.d( "iBeacon_0=",""+ str);
			str = String.format( "%02x", p[45] );//0x16 
			//Log.d( "iBeacon_1=",""+ str);
			
			str = String.format( "%02x", p[52] );//major h
			//Log.d( "major_H=",""+ str);
			str = String.format( "%02x", p[53] );//major L
			//Log.d( "major_L=",""+ str);
			
			str = String.format( "%02x", p[54] );//minor h
			//Log.d( "minor_H=",""+ str);
			str = String.format( "%02x", p[55] );//minor L
			//Log.d( "minor_L=",""+ str);
			*/
			

			
			int ib1_major=0;
			int ib1_minor=0;
			if( p[52]==(byte)0xff )
			{
				if( p[53]==(byte)0xff )ib1_major=1;
			}
			if( p[54]==(byte)0xff )
			{
				if( p[55]==(byte)0xff )ib1_minor=1;
			}
			
			
			
			if( p[5]==(byte)0xe0 && p[6]==(byte)0xff &&p[11]==m1&&p[12]==m2 &&(dev_VID==p[19-6])  )//JDY
			{
				 byte[] WriteBytes = new byte[4];
				 WriteBytes[0]=p[19-6];
				 WriteBytes[1]=p[20-6];
				Log.d( "out_1","TC"+list_cell_0.bytesToHexString1( WriteBytes ) );
				
				if( p[20-6]==(byte)0xa0 )return JDY_type.JDY;//透传
				else if( p[20-6]==(byte)0xa5 )return JDY_type.JDY_AMQ;//按摩器
				else if( p[20-6]==(byte)0xb1 )return JDY_type.JDY_LED1;// LED灯
				else if( p[20-6]==(byte)0xb2 )return JDY_type.JDY_LED2;// LED灯
				else if( p[20-6]==(byte)0xc4 )return JDY_type.JDY_KG;// 开关控制
				else if( p[20-6]==(byte)0xc5 )return JDY_type.JDY_KG1;// 开关控制
				
				//Log.d( "JDY_type.JDY=","1");
				return JDY_type.JDY;
			}
			else if( p[44]==(byte)0x10 && p[45]==(byte)0x16 && ( ib1_major==1 || ib1_minor==1 ) )//sensor
			{
//				 byte[] WriteBytes1 = new byte[2];
//				 WriteBytes1[0]=p[56];
//				 WriteBytes1[1]=p[57];
//				 Log.d( "JDY_type.JDY_19=","SS"+list_cell_0.bytesToHexString1( WriteBytes1 ) );
				
				//Log.d( "JDY_type.JDY_sensor=","2");
				return JDY_type.sensor_temp;
			}
			else if( p[3]==(byte)0x1a && p[4]==(byte)0xff//p[44]==(byte)0x10 && p[45]==(byte)0x16              //sensor
					)
			{
//				 byte[] WriteBytes1 = new byte[2];
//				 WriteBytes1[0]=p[56];
//				 WriteBytes1[1]=p[57];
//				 Log.d( "JDY_type.JDY_19=","IB"+list_cell_0.bytesToHexString1( WriteBytes1 ) );
				/*
				if( p[57]==(byte)0xe0 ){return JDY_type.JDY_iBeacon;}//iBeacon模式
				else if( p[57]==(byte)0xe1 ){return JDY_type.sensor_temp;}////温度传感器
				else if( p[57]==(byte)0xe2 ){return JDY_type.sensor_humid;}////湿度传感器
				else if( p[57]==(byte)0xe3 ){return JDY_type.sensor_temp_humid;}////湿湿度传感器
				else if( p[57]==(byte)0xe4 ){return JDY_type.sensor_fanxiangji;}////芳香机香水用量显示仪
				else if( p[57]==(byte)0xe5 ){return JDY_type.sensor_zhilanshuibiao;}////智能水表传感器，抄表仪
				else if( p[57]==(byte)0xe6 ){return JDY_type.sensor_dianyabiao;}////电压传感器
				else if( p[57]==(byte)0xe7 ){return JDY_type.sensor_dianliu;}////电流传感器
				else if( p[57]==(byte)0xe8 ){return JDY_type.sensor_zhonglian;}////称重传感器
				else if( p[57]==(byte)0xe9 ){return JDY_type.sensor_pm2_5;}////PM2.5传感器
				*/
				 return JDY_type.JDY_iBeacon;
			}
			
			else  
			{
				//Log.d( "JDY_type.UNKW=","0");
				return JDY_type.UNKW;
			}
			
			//return JDY_type.JDY_iBeacon;
		}
	
	
	public DeviceListAdapter( BluetoothAdapter adapter,Context context1 )
	{
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
		apter = adapter;
		context = context1;
        list_cell_0 = new DeviceListAdapter1();
//        mGet_type = new Get_type();
        timer.schedule(task, 1000, 1000); // 1s后执行task,经过1s再次执行  
	}
	  Handler handler = new Handler() {  
	        public void handleMessage(Message msg) {  
	            if (msg.what == 1&&stop_timer) 
	            {  
	                //tvShow.setText(Integer.toString(i++));  
	            	loop_list();
//	            	Log.d( "out_1","time run" );
	            }  
	            super.handleMessage(msg);  
	        };  
	    };  
	    
	    TimerTask task = new TimerTask() {  
	  
	        @Override  
	        public void run() {  
	            // 需要做的事:发送消息  
	            Message message = new Message();  
	            message.what = 1;  
	            handler.sendMessage(message);  
	        }  
	    };  
	
	
	
	
	
	public DeviceListAdapter1 init_adapter( )
	{
		
		return list_cell_0;
	}
	public BluetoothDevice get_item_dev( int pos )
	{
		return list_cell_0.dev_ble.get( pos );
	}
	
	public JDY_type get_item_type( int pos )
	{
		return list_cell_0.dev_type.get( pos );
	}
	public int get_count( )
	{
		return list_cell_0.getCount();
	}
	
	//  ibeacon
	public String get_iBeacon_uuid( int pos )
	{
		return list_cell_0.get_ibeacon_uuid( pos );
	}
	public String get_ibeacon_major( int pos )
	{
		return list_cell_0.get_ibeacon_major(pos);
	}
	public String get_ibeacon_minor( int pos )
	{
		return list_cell_0.get_ibeacon_minor(pos);
	}
	
	//sensor
	public String get_sensor_temp( int pos )
	{
		return list_cell_0.get_sensor_temp(pos);
	}
	public String get_sensor_humid( int pos )
	{
		return list_cell_0.get_sensor_humid(pos);
	}
	
	public String get_sensor_batt( int pos )
	{
		return list_cell_0.get_sensor_batt(pos);
	}
	
	public byte get_vid( int pos )
	{
		return (byte) list_cell_0.get_vid(pos);
	}
	public void set_vid( byte vid )
	{
		dev_VID = vid;
	}
	
	
	public void loop_list(  )
	{
		list_cell_0.loop();
	}
	
	
	
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) 
        {
        	scan_int++;
        	if( scan_int>1 )
        	{
        		scan_int = 0;
	            if (Looper.myLooper() == Looper.getMainLooper()) 
	            {
	            	JDY_type m_tyep = dv_type( scanRecord  );
	            	if( m_tyep!=JDY_type.UNKW && m_tyep!=null )
	            	{
	            		list_cell_0.addDevice(device,scanRecord,rssi,m_tyep );
	            		//mDevListAdapter.notifyDataSetChanged();
	            		list_cell_0.notifyDataSetChanged();
	            	}
	            }
	            else 
	            {
	                runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                    	JDY_type m_tyep = dv_type( scanRecord  );
	    	            	if( m_tyep!=JDY_type.UNKW && m_tyep!=null )
	    	            	{
	    	            		list_cell_0.addDevice(device,scanRecord,rssi,m_tyep );
	    	            		//mDevListAdapter.notifyDataSetChanged();
	    	            		list_cell_0.notifyDataSetChanged();
	    	            	}
	                    }
	                });
	            }
        	}
        }
    };// public void addDevice(BluetoothDevice device,byte[] scanRecord,Integer  RSSI,JDY_type type ) 
	
    
    public void stop_flash( )
    {
    	stop_timer = false;
    }
    public void start_flash( )
    {
    	stop_timer = true;
    }
    
	public void clear()
	{
		list_cell_0.clear();
	}
	public void scan_jdy_ble( Boolean p )//扫描BLE蓝牙
	{
		if( p )
		{
			
			list_cell_0.notifyDataSetChanged();
			apter.startLeScan( mLeScanCallback );
			start_flash();
		}
		else 
		{
			apter.stopLeScan( mLeScanCallback );
			stop_flash();
		}
	}
	
	
	
	
	class DeviceListAdapter1 extends BaseAdapter 
	{
		private List<BluetoothDevice> dev_ble;
		private List<JDY_type>dev_type;
		private List<byte[]> dev_scan_data;
		private List<Integer> dev_rssi;
		private List<Integer> remove;
		
		private ViewHolder viewHolder;
		int count = 0;
		int ip = 0;
		
		public DeviceListAdapter1() {
			dev_ble = new ArrayList<BluetoothDevice>();
			dev_scan_data = new ArrayList<byte[]>();
			dev_rssi = new ArrayList<Integer>();
			dev_type = new ArrayList<JDY_type>();
			remove = new ArrayList<Integer>();
		}
		
		public void loop()
		{
			if( remove!=null&&remove.size()>0&&ip==0 )
			{
				
				if( count>=remove.size() )
				{
					count = 0;
				}
				Integer it = remove.get( count );
				if( it>=3 )
				{
					dev_ble.remove(count);
					dev_scan_data.remove(count);
					dev_rssi.remove(count);
					dev_type.remove(count);
					remove.remove(count);
					notifyDataSetChanged();
				}
				else
				{
					it++;
					remove.add(count+1, it);
					remove.remove(count);
				}
				count++;
				
			}
		}
		public void addDevice(BluetoothDevice device,byte[] scanRecord,Integer  RSSI,JDY_type type ) 
		{
			ip = 1;
			if (!dev_ble.contains(device)) 
			{
				dev_ble.add(device);
				dev_scan_data.add(scanRecord);
				dev_rssi.add(RSSI);
				dev_type.add(type);
				Integer it =0;
				remove.add(it);
			}
			else
			{
				for(int i=0;i<dev_ble.size();i++)
				{
					String btAddress = dev_ble.get(i).getAddress();
					if(btAddress.equals(device.getAddress()))
					{
						//if( dev_type.get( i )==JDY_type.JDY_iBeacon||dev_type.get( i )==JDY_type.JDY_sensor )
						{
						dev_ble.add(i+1, device);
						dev_ble.remove(i);
						
						dev_scan_data.add(i+1, scanRecord);
						dev_scan_data.remove(i);
						
						dev_rssi.add(i+1, RSSI);
						dev_rssi.remove(i);
						
						dev_type.add(i+1, type);
						dev_type.remove(i);
						
						Integer it =0;// remove.get( i );
						remove.add(i+1, it);
						remove.remove(i);
						
						
						}
					}
				}
			}
			notifyDataSetChanged();
			ip = 0;
		}
		public void clear(){
			dev_ble.clear();
			dev_scan_data.clear();
			dev_rssi.clear();
			dev_type.clear();
			remove.clear();
		}

		@Override
		public int getCount() {
			return dev_ble.size();
		}

		@Override
		public BluetoothDevice getItem(int position) {
			return dev_ble.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			if (convertView == null) 
			{
				//Log.d( "convertView == null","0");
			}
			if( position<=dev_ble.size()  )
			{
				JDY_type type_0 = dev_type.get( position );
				if( type_0==JDY_type.JDY )//为标准透传模块
				{
					//Log.d( "JDY_type.JDY=","1000");
					// if (convertView == null) 
					{
						convertView = LayoutInflater.from( context ).inflate(R.layout.listitem_device, null);
						viewHolder = new ViewHolder();
						viewHolder.tv_devName = (TextView) convertView.findViewById(R.id.device_name);
						viewHolder.tv_devAddress = (TextView) convertView.findViewById(R.id.device_address);
						viewHolder.device_rssi = (TextView) convertView.findViewById(R.id.device_rssi);
						viewHolder.scan_data = (TextView) convertView.findViewById(R.id.scan_data);
						viewHolder.type0 = (TextView) convertView.findViewById(R.id.type0);
						convertView.setTag(viewHolder);
					} 
//					else {
//						convertView.getTag();
//					}
					list_select_index=1;
					
					
					// add-Parameters
					BluetoothDevice device = dev_ble.get(position);
					String devName = device.getName();
					devName = "Name:"+devName;
					if( viewHolder.tv_devName!=null )
						viewHolder.tv_devName.setText(devName);

					String mac = device.getAddress();
					mac = "MAC:"+mac;
					if( viewHolder.tv_devAddress!=null )
						viewHolder.tv_devAddress.setText( mac );
					
					String rssi_00 = ""+dev_rssi.get( position );
					rssi_00 = "RSSI:-"+rssi_00;
					if( viewHolder.device_rssi!=null )
						viewHolder.device_rssi.setText( rssi_00 );
					
					String tp = null;
					tp = "Type:" + "标准模式";
					if( viewHolder.type0!=null )
						viewHolder.type0.setText( tp );
					
					if( viewHolder.scan_data!=null )
						viewHolder.scan_data.setText( "scanRecord:"+bytesToHexString1(dev_scan_data.get( position )) );
					
				}
				else if( type_0==JDY_type.JDY_iBeacon )//为iBeacon设备
				{
					//Log.d( "JDY_type.JDY_iBeacon=","1001");
						//if (convertView == null) 
					{
						convertView = LayoutInflater.from( context ).inflate(R.layout.listitem_ibeacon, null);
						viewHolder = new ViewHolder();
						viewHolder.ibeacon_name = (TextView) convertView.findViewById(R.id.ibeacon_Name);
						viewHolder.ibeacon_mac = (TextView) convertView.findViewById(R.id.iBeacon_mac);
						viewHolder.ibeacon_uuid = (TextView) convertView.findViewById(R.id.ibeacon_uuid);
						viewHolder.ibeacon_major = (TextView) convertView.findViewById(R.id.ibeacon_major);
						viewHolder.ibeacon_minor = (TextView) convertView.findViewById(R.id.ibeacon_minor);
						viewHolder.ibeacon_rssi = (TextView) convertView.findViewById(R.id.ibeacon_rssi);
						viewHolder.type0 = (TextView) convertView.findViewById(R.id.type0);
						convertView.setTag(viewHolder);
						//Log.d( "JDY_type.JDY_iBeacon=","900000");
					}
					//else {
					//	convertView.getTag();
					//}
					
					String tp = null;
					tp = "Type:" + "iBeacon";
					viewHolder.type0.setText( tp );
					BluetoothDevice device = dev_ble.get(position);
					
					if( viewHolder.ibeacon_name!=null )
						viewHolder.ibeacon_name.setText( device.getName() );
					if( viewHolder.ibeacon_mac!=null )
						viewHolder.ibeacon_mac.setText( device.getAddress() );
					
					if( viewHolder.ibeacon_uuid!=null )
						viewHolder.ibeacon_uuid.setText( get_ibeacon_uuid( position ) );
					
					if( viewHolder.ibeacon_major!=null )
						viewHolder.ibeacon_major.setText( get_ibeacon_major( position ) );
					
					if( viewHolder.ibeacon_minor!=null )
						viewHolder.ibeacon_minor.setText( get_ibeacon_minor( position ) );
					
					if( viewHolder.ibeacon_rssi!=null )
						viewHolder.ibeacon_rssi.setText( "-"+dev_rssi.get(position  ) );
					
					list_select_index=2;
				}
				else if( type_0==JDY_type.sensor_temp )//为传感器设备
				{
					
					//Log.d( "JDY_type.JDY_iBeacon=","1002");
					
					//if (convertView == null) 
					{
						convertView = LayoutInflater.from( context ).inflate(
								R.layout.listitem_sensor_temp, null);
						viewHolder = new ViewHolder();
						viewHolder.sensor_name = (TextView) convertView.findViewById(R.id.sensor_name);
						viewHolder.sensor_mac = (TextView) convertView.findViewById(R.id.sensor_mac);
						viewHolder.sensor_rssi = (TextView) convertView.findViewById(R.id.sensor_rssi);
						viewHolder.sensor_type0 = (TextView) convertView.findViewById(R.id.sensor_type0);
						viewHolder.sensor_temp = (TextView) convertView.findViewById(R.id.sensor_thermo_c);
						viewHolder.sensor_humid = (TextView) convertView.findViewById(R.id.sensor_thermo_f);
						viewHolder.sensor_batt = (TextView) convertView.findViewById(R.id.sensor_batt);
						
						
						convertView.setTag(viewHolder);
						//Log.d( "JDY_type.JDY_iBeacon=","A00000");
					} 
//					else {
//						convertView.getTag();
//					}
					
					list_select_index=2;
					
					
					BluetoothDevice device = dev_ble.get(position);
					String devName = device.getName();
					
					if( viewHolder.sensor_name!=null )
					{
						devName = "Name:"+devName;
						viewHolder.sensor_name.setText(devName);
					}
					if( viewHolder.sensor_mac!=null )
					{
						String mac = device.getAddress();
						mac = "MAC:"+mac;
						viewHolder.sensor_mac.setText( mac );
					}
					if( viewHolder.sensor_rssi!=null )
					{
						String rssi_00 = ""+dev_rssi.get( position );
						rssi_00 = "RSSI:-"+rssi_00;
						viewHolder.sensor_rssi.setText( rssi_00 );
					}
					String tp = null;
					tp = "Type:" + "sensor";
					if( viewHolder.sensor_type0!=null )
						viewHolder.sensor_type0.setText( tp );
					
					if( viewHolder.sensor_temp!=null )
					{
						viewHolder.sensor_temp.setText( get_sensor_temp(position) );
					}
					if( viewHolder.sensor_humid!=null )
					{
						viewHolder.sensor_humid.setText( get_sensor_humid(position)+"%" );
					}
					if( viewHolder.sensor_batt!=null )
					{
						viewHolder.sensor_batt.setText( get_sensor_batt(position) );
					}
					
				}
				else if( type_0==JDY_type.JDY_LED1 )//为RGB LED灯条设备
				{
				//  switch_name,switch_mac,switch_rssi,switch_type;
					{
						convertView = LayoutInflater.from( context ).inflate(R.layout.listitem_led, null);
						viewHolder = new ViewHolder();
						viewHolder.led_name = (TextView) convertView.findViewById(R.id.led_name);
						viewHolder.led_mac = (TextView) convertView.findViewById(R.id.led_mac);
						viewHolder.led_rssi = (TextView) convertView.findViewById(R.id.led_rssi);
						viewHolder.led_type113 = (TextView) convertView.findViewById(R.id.led_type113);
						//viewHolder.type_imageView2 = (ImageView) convertView.findViewById(R.id.type_imageView2);
						convertView.setTag(viewHolder);
					}
					
					BluetoothDevice device = dev_ble.get(position);
					
					if( viewHolder.led_name!=null )
						viewHolder.led_name.setText( "Name:"+device.getName() );
//					
					if( viewHolder.led_mac!=null )
						viewHolder.led_mac.setText( "MAC:"+device.getAddress() );
					
					if( viewHolder.led_rssi!=null )
					{
						String rssi_00 = ""+dev_rssi.get( position );
						rssi_00 = "RSSI:-"+rssi_00;
						viewHolder.led_rssi.setText( rssi_00 );
					}
					if( viewHolder.led_type113!=null )
					{	
						String tp = "";
						tp = "Type:" + "LED灯带";
						viewHolder.led_type113.setText( tp );
					}
				}
				else if( type_0==JDY_type.JDY_LED2 )//为RGB LED灯条设备
				{
					
				}
				else if( type_0==JDY_type.JDY_AMQ )//为按摩器设备
				{
					//  switch_name,switch_mac,switch_rssi,switch_type;
					{
						convertView = LayoutInflater.from( context ).inflate(R.layout.listitem_massager, null);
						viewHolder = new ViewHolder();
						viewHolder.massager_name = (TextView) convertView.findViewById(R.id.massager_name);
						viewHolder.massager_mac = (TextView) convertView.findViewById(R.id.massager_mac);
						viewHolder.massager_rssi = (TextView) convertView.findViewById(R.id.massager_rssi);
						viewHolder.massager_type113 = (TextView) convertView.findViewById(R.id.massager_type113);
						//viewHolder.type_imageView2 = (ImageView) convertView.findViewById(R.id.type_imageView2);
						convertView.setTag(viewHolder);
					}
					
					BluetoothDevice device = dev_ble.get(position);
					
					if( viewHolder.massager_name!=null )
						viewHolder.massager_name.setText( "Name:"+device.getName() );
//					
					if( viewHolder.massager_mac!=null )
						viewHolder.massager_mac.setText( "MAC:"+device.getAddress() );
					
					if( viewHolder.massager_rssi!=null )
					{
						String rssi_00 = ""+dev_rssi.get( position );
						rssi_00 = "RSSI:-"+rssi_00;
						viewHolder.massager_rssi.setText( rssi_00 );
					}
					if( viewHolder.massager_type113!=null )
					{	
						String tp = "";
						tp = "Type:" + "AV棒";
						viewHolder.massager_type113.setText( tp );
					}
				}
				else if( type_0==JDY_type.JDY_KG )//为继电器控制、IO控制等设备
				{
					//  switch_name,switch_mac,switch_rssi,switch_type;
					{
						convertView = LayoutInflater.from( context ).inflate(R.layout.listitem_switch, null);
						viewHolder = new ViewHolder();
						viewHolder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
						viewHolder.switch_mac = (TextView) convertView.findViewById(R.id.switch_mac);
						viewHolder.switch_rssi = (TextView) convertView.findViewById(R.id.switch_rssi);
						viewHolder.switch_type113 = (TextView) convertView.findViewById(R.id.switch_type113);
						//viewHolder.type_imageView2 = (ImageView) convertView.findViewById(R.id.type_imageView2);
						convertView.setTag(viewHolder);
					}
					
					BluetoothDevice device = dev_ble.get(position);
					
					if( viewHolder.switch_name!=null )
						viewHolder.switch_name.setText( device.getName() );
//					
					if( viewHolder.switch_mac!=null )
						viewHolder.switch_mac.setText( device.getAddress() );
					
					if( viewHolder.switch_rssi!=null )
					{
						String rssi_00 = ""+dev_rssi.get( position );
						rssi_00 = "RSSI:-"+rssi_00;
						viewHolder.switch_rssi.setText( rssi_00 );
					}
					if( viewHolder.switch_type113!=null )
					{	
						String tp = "";
						tp = "Type:" + "开关控制器";
						viewHolder.switch_type113.setText( tp );
					}
//					if( viewHolder.type_imageView2!=null )
//					{
//						viewHolder.type_imageView2.setImageDrawable(getResources().getDrawable(R.drawable.switch_img));
//					}
					
				}
				else if( type_0==JDY_type.JDY_KG1 )//为继电器控制、IO控制等设备
				{
					//  switch_name,switch_mac,switch_rssi,switch_type;
					{
						convertView = LayoutInflater.from( context ).inflate(R.layout.listitem_switch, null);
						viewHolder = new ViewHolder();
						viewHolder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
						viewHolder.switch_mac = (TextView) convertView.findViewById(R.id.switch_mac);
						viewHolder.switch_rssi = (TextView) convertView.findViewById(R.id.switch_rssi);
						viewHolder.switch_type113 = (TextView) convertView.findViewById(R.id.switch_type113);
						//viewHolder.type_imageView2 = (ImageView) convertView.findViewById(R.id.type_imageView2);
						convertView.setTag(viewHolder);
					}
					
					BluetoothDevice device = dev_ble.get(position);
					
					if( viewHolder.switch_name!=null )
						viewHolder.switch_name.setText( device.getName() );
//					
					if( viewHolder.switch_mac!=null )
						viewHolder.switch_mac.setText( device.getAddress() );
					
					if( viewHolder.switch_rssi!=null )
					{
						String rssi_00 = ""+dev_rssi.get( position );
						rssi_00 = "RSSI:-"+rssi_00;
						viewHolder.switch_rssi.setText( rssi_00 );
					}
					if( viewHolder.switch_type113!=null )
					{	
						String tp = "";
						tp = "Type:" + "升降机控制器";
						viewHolder.switch_type113.setText( tp );
					}
					
				}
				else if( type_0==JDY_type.JDY_WMQ )//为纹眉器设备
				{
				//  switch_name,switch_mac,switch_rssi,switch_type;
					{
						convertView = LayoutInflater.from( context ).inflate(R.layout.listitem_switch, null);
						viewHolder = new ViewHolder();
						viewHolder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
						viewHolder.switch_mac = (TextView) convertView.findViewById(R.id.switch_mac);
						viewHolder.switch_rssi = (TextView) convertView.findViewById(R.id.switch_rssi);
						viewHolder.switch_type113 = (TextView) convertView.findViewById(R.id.switch_type113);
						viewHolder.type_imageView2 = (ImageView) convertView.findViewById(R.id.type_imageView2);
						convertView.setTag(viewHolder);
					}
					
					BluetoothDevice device = dev_ble.get(position);
					
					if( viewHolder.switch_name!=null )
						viewHolder.switch_name.setText( device.getName() );
//					
					if( viewHolder.switch_mac!=null )
						viewHolder.switch_mac.setText( device.getAddress() );
					
					if( viewHolder.switch_rssi!=null )
					{
						String rssi_00 = ""+dev_rssi.get( position );
						rssi_00 = "RSSI:-"+rssi_00;
						viewHolder.switch_rssi.setText( rssi_00 );
					}
					

					if( viewHolder.switch_type113!=null )
					{	
						String tp = "";
						tp = "Type:" + "开关控制器";
						viewHolder.switch_type113.setText( tp );
					}
					if( viewHolder.type_imageView2!=null )
					{
						viewHolder.type_imageView2.setImageDrawable(getResources().getDrawable(R.drawable.massager_img));
					}
				}
				else if( type_0==JDY_type.JDY_LOCK )//为蓝牙电子锁设备
				{
					
				}else{
					
					//list_select_index=0;
				}
	
				return convertView;
			}return null;
			
		}

		
		public String get_ibeacon_uuid( int pos )
		{
			String uuid=null;
			HashMap<String, String> map = new HashMap<String, String>();
			String Beacon_UUID;
			byte[] byte1000=(byte[])dev_scan_data.get( pos );
			if( byte1000.length<32 )return null;
			byte[] proximityUuidBytes = new byte[16];
			System.arraycopy(byte1000, 9, proximityUuidBytes, 0, 16); 
			Beacon_UUID = bytesToHexString(proximityUuidBytes);
			String uuid_8,uuid_4,uuid_44,uuid_444,uuid_12;
			uuid_8=Beacon_UUID.substring(0,8);
			uuid_4=Beacon_UUID.substring(8, 12);
			uuid_44=Beacon_UUID.substring(12, 16);
			uuid_444=Beacon_UUID.substring(16, 20);
			uuid_12=Beacon_UUID.substring(20, 32);
			uuid=uuid_8+"-"+uuid_4+"-"+uuid_44+"-"+uuid_444+"-"+uuid_12;
			
			return uuid;
		}
		public String get_ibeacon_major( int pos )
		{
			String major=null;
			byte[] byte1000=(byte[])dev_scan_data.get( pos );
			if( byte1000.length<60 )return null;
			byte[] result = new byte[4];  
			result[0]=0X00;
			result[1]=0;
			result[2]=byte1000[25];
			result[3]=byte1000[26];
			int ii100=byteArrayToInt1(result);
			major=String.valueOf(ii100);
			return major;
		}
		public String get_ibeacon_minor( int pos )
		{
			String major=null;
			byte[] byte1000=(byte[])dev_scan_data.get( pos );
			if( byte1000.length<60 )return null;
			byte[] result = new byte[4];  
			result[0]=0X00;
			result[1]=0;
			result[2]=byte1000[27];
			result[3]=byte1000[28];
			int ii100=byteArrayToInt1(result);
			major=String.valueOf(ii100);
			return major;
		}
		
		public String get_sensor_temp( int pos )
		{
			byte[] byte1000=(byte[])dev_scan_data.get( pos );
			//if( byte1000.length<32 )return null;
			byte[] result = new byte[1];  
			result[0]=byte1000[58];
			//result[1]=byte1000[32];
			//int ii100 = byteArrayToInt1(result);
			//vid=String.valueOf(ii100);
			return bytesToHexString( result );
		}
		public String get_sensor_humid( int pos )
		{
			byte[] byte1000=(byte[])dev_scan_data.get( pos );
			//if( byte1000.length<32 )return null;
			byte[] result = new byte[1];  
			result[0]=byte1000[59];
			//int ii100 = byteArrayToInt1(result);
			//vid=String.valueOf(ii100);
			return bytesToHexString( result );
		}
		
		public String get_sensor_batt( int pos )
		{
			byte[] byte1000=(byte[])dev_scan_data.get( pos );
			byte[] result = new byte[1];  
			result[0]=byte1000[60];
			return bytesToHexString( result );
		}
		
		public int get_vid( int pos )
		{
			String vid=null;
			byte[] byte1000=(byte[])dev_scan_data.get( pos );
			byte[] result = new byte[4];  
			result[0]=0X00;
			result[1]=0X00;
			result[2]=0X00;
			JDY_type tp = dev_type.get( pos );
			if( tp==JDY_type.JDY||tp==JDY_type.JDY_LED1||tp==JDY_type.JDY_LED2||tp==JDY_type.JDY_AMQ||tp==JDY_type.JDY_KG||tp==JDY_type.JDY_KG1||tp==JDY_type.JDY_WMQ||tp==JDY_type.JDY_LOCK )
			{
				result[3]=byte1000[19-6];
			}
			else 
			{
				result[3]=byte1000[56];
			}
			
			int ii100 = byteArrayToInt1(result);
			//vid=String.valueOf(ii100);
			return ii100;
		}
		
		
		
		
		
		
		
		  public int byteArrayToInt1(byte[] bytes) {
              int value= 0;
              //由高位到低位
              for (int i = 0; i < 4; i++) {
                  int shift= (4 - 1 - i) * 8;
                  value +=(bytes[i] & 0x000000FF) << shift;//往高位游
              }
              return value;
        }
		  
		    private String bytesToHexString(byte[] src){  
		    	 StringBuilder stringBuilder = new StringBuilder(src.length);
	             for(byte byteChar : src)
	                stringBuilder.append(String.format("%02X", byteChar));
		        return stringBuilder.toString();  
		    }  
		
	    private String bytesToHexString1(byte[] src){  
	    	 StringBuilder stringBuilder = new StringBuilder(src.length);
             for(byte byteChar : src)
                stringBuilder.append(String.format(" %02X", byteChar));
	        return stringBuilder.toString();  
	    }  
		
		
		
		
		
		
		
		
		
		
		
	}

	class ViewHolder {
		TextView tv_devName, tv_devAddress,device_rssi,type0,scan_data;//透传 
		TextView ibeacon_name,ibeacon_mac,ibeacon_uuid,ibeacon_major,ibeacon_minor,ibeacon_rssi;//ibeacon
		TextView sensor_name,sensor_mac,sensor_rssi,sensor_type0,sensor_temp,sensor_humid,sensor_batt;//ibeacon
		TextView switch_name,switch_mac,switch_rssi,switch_type113;
		ImageView type_imageView2;
		TextView massager_name,massager_mac,massager_rssi,massager_type113;
		
		TextView led_name,led_mac,led_rssi,led_type113;
		
	}
	
	
	
	
	
	
	
	
	
	
}

