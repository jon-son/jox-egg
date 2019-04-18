package com.example.jdy_type;

public enum JDY_type {
	UNKW,
	JDY,//为标准透传模块
	JDY_LED1,//为RGB LED灯条设备  测试版本
	JDY_LED2,//为RGB LED灯条设备   正试版本APP
	
	JDY_AMQ,//为按摩器设备
	
	JDY_KG,//为继电器控制、IO控制等设备
	JDY_KG1,//升降机上下停止IO控制
	
	JDY_WMQ,//为纹眉器设备
	JDY_LOCK,//为蓝牙电子锁设备
	
	
	
   JDY_iBeacon,//iBeacon设备
   sensor_temp,//温度传感器
   sensor_humid,//湿度传感器
   sensor_temp_humid,//湿湿度传感器
   sensor_fanxiangji,//芳香机香水用量显示仪
   sensor_zhilanshuibiao,//智能水表传感器，抄表仪
   sensor_dianyabiao,//电压传感器
   sensor_dianliu,//电流传感器
   sensor_zhonglian,//称重传感器
   sensor_pm2_5,//PM2.5传感器
	
	
	
	
	
	
	
	
	
	
//	JDY_06,//无天线BLE多功能模块（主从一体、微信透传、IO、PWM、RTC）
//	JDY_08,//有天线BLE多功能模块（主从一体、微信透传、IO、PWM、RTC）
//	JDY_09,//有天线BLE主从一体模块
//	JDY_10,//有天线BLE从模块
	
}




