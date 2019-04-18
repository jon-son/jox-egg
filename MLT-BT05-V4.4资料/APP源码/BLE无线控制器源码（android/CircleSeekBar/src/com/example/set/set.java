package com.example.set;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lee.circleseekbar.R;



public class set extends Activity
{
  String password_value = "123456";
  EditText pss_value_txt ;
  
  TextView textView11;
  String resultStr = "";
  
  private String path = "http://szony.blog.163.com/blog/static/24529305020151244823112/";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.set );
        
        pss_value_txt = (EditText)findViewById(R.id.set_pass_value);
//        textView11 = (TextView)findViewById(R.id.textView11);
        

        
        
       // textView11.setText("3432423423"); 
        
        
        
        
        setTitle("设置");
        
        ActionBar actionBar = getActionBar();  
        actionBar.setDisplayHomeAsUpEnabled(true);  
        
        password_value = getSharedPreference( "DEV_PASSWORD_LEY_1000" );
        if( password_value!=null||password_value!="")
        {
        	if( password_value.length()==6 )
        	{
        		pss_value_txt.setText( password_value );
        	}
        	else password_value = "123456" ;
        }else pss_value_txt.setText( "123456" );
        
        
//        Message message = new Message();  
//        message.what = 1;  
//        handler.sendMessage(message);  
        
//		JDYHtmlService df = new JDYHtmlService();
//		String hdf = df.h5_url();
//		textView11.setText ( hdf );;
        
    }
    
	
	
    Handler handler = new Handler() {  
        public void handleMessage(Message msg) {  
        	if (msg.what == 1) 
        	{  
                
//                try {  
//                    String htmlContent = JDYHtmlService.getHtml(path);
//                    textView11.setText(htmlContent);  
//                } catch (Exception e) {     
//                	textView11.setText("程序出现异常："+e.toString());
//                }
        		
        		
                
                
            }  
        	
        	
            super.handleMessage(msg);  
        };  
    };  
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
    
    
	@Override
	protected void onResume() {//打开APP时扫描设备
		super.onResume();

	}

	@Override
	protected void onPause() {//停止扫描
		super.onPause();

	}
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.set_menu, menu);
    	menu.findItem(R.id.set_menu).setVisible(true);
        return true;
    } 
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        	case R.id.set_menu:
        	{
        		String ts = pss_value_txt.getText().toString();
        		if( ts==null||ts==""){Toast.makeText(set.this, "提示！密码不能为空", Toast.LENGTH_SHORT).show(); break;}
        		
        		int len = ts.length();
        		if(len!=6){Toast.makeText(set.this, "提示！密码必须为6位数字", Toast.LENGTH_SHORT).show(); break;}
        		Toast.makeText(set.this, "提示！密码保存成功", Toast.LENGTH_SHORT).show(); 
        		
        		setSharedPreference( "DEV_PASSWORD_LEY_1000",ts );
        	}
            break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    
    
    
}
