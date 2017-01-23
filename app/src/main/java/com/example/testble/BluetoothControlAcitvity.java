package com.example.testble;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class BluetoothControlAcitvity extends Activity
{
    private final static String TAG = BluetoothControlAcitvity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    
    private String mDeviceName;
    private String mDeviceAddress;
    private HolloBluetooth mble;
    private Context context;
    private EditText sendEdit; 
    private ScrollView scrollView;
    private Button sendBt;
    private Handler mHandler;  
	private static final int MSG_DATA_CHANGE = 0x11;  
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_control);
		
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        scrollView = (ScrollView)findViewById(R.id.scroll);
        sendBt = (Button)findViewById(R.id.send);
        sendEdit = (EditText)findViewById(R.id.sendData);
        sendBt.setEnabled(false);
        
        mble = HolloBluetooth.getInstance(getApplicationContext());	//获取蓝牙实例
        
        mHandler = new Handler()
        {  
            @Override  
            public void handleMessage(Message msg) 
            {  
                switch (msg.what) {  
                case MSG_DATA_CHANGE:  
                	int color = msg.arg1;
                	String strData = (String)msg.obj;
                	SpannableStringBuilder builder = new SpannableStringBuilder(strData);
                	  
                	  //ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色
                	ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
                	String string;
                	int num;
                	switch (color) {
					case Color.BLUE: //send
						TextView txView = (TextView)findViewById(R.id.Tx);
						string = txView.getText().toString();
						num = Integer.parseInt(string.subSequence(string.indexOf(':')+1, string.length()).toString())+msg.arg2;
						txView.setText("Tx:"+num);
						builder.setSpan(colorSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						break;
					case Color.RED:	//error
						builder.setSpan(colorSpan, 0, strData.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						break;
					case Color.BLACK: //tips
						builder.setSpan(colorSpan, 0, strData.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						break;

					default: //receive
						TextView rxView = (TextView)findViewById(R.id.Rx);
						string = rxView.getText().toString();
						num = Integer.parseInt(string.subSequence(string.indexOf(':')+1, string.length()).toString())+msg.arg2;
						rxView.setText("Rx:"+num);
						builder.setSpan(colorSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						break;
					}
                	TextView tView = new TextView(context);
					tView.setText(builder);
					LinearLayout layout = (LinearLayout)findViewById(R.id.scroll_layout);
					layout.addView(tView);
					scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    break;  
  
                default:  
                    break;  
                }  
                super.handleMessage(msg);  
            }  
        }; 
        
        Button clearBt = (Button)findViewById(R.id.clear_log);
        clearBt.setOnClickListener(new View.OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
				LinearLayout layout = (LinearLayout)findViewById(R.id.scroll_layout);
				layout.removeAllViews();
				TextView rxView = (TextView)findViewById(R.id.Rx);
				rxView.setText("Rx:"+0);
				TextView txView = (TextView)findViewById(R.id.Tx);
				txView.setText("Tx:"+0);
			}
		});
        
        sendBt.setOnClickListener(new View.OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
				new Handler().post(new Runnable() 
				{
					@Override
					public void run()
					{
						String sendString = sendEdit.getText().toString();
						if(sendString == null || sendString.isEmpty())
							return ;
						sendEdit.setText("");

						byte[] bytes = ConvertData.hexStringToBytes(sendString);
						if (bytes == null)
							return;
						
						addLogText("发送：\r\n      "+ConvertData.bytesToHexString(bytes, false),Color.BLUE, bytes.length);
						if(!mble.sendData(bytes))
							addLogText("发送失败！",Color.RED,0);
					}
				});

			}
		});
        
        addLogText("http://www.tshjdz.com\r\nHJ-580 BLE串口透传测试程序（For Android,V1.7）\r\n唐山宏佳电子科技有限公司",Color.BLACK,0);
        
        new Handler().post(new Runnable() 
        {
            @Override
            public void run() 
            {    
            	addLogText("正在连接。。。",Color.BLACK,0);
            	int i;
            	for (i = 0; i < 5; i++) 
            	{
            		 if(mble.connectDevice(mDeviceAddress,bleCallBack))	//连接蓝牙设备
                     	break;

					try {
						Thread.sleep(500,0);//200ms
					}
					catch (Exception e){

					}
				}
            	if(i == 5)
            	{
            		addLogText("连接失败",Color.RED,0);
            		return ;
            	}

				try {
					Thread.sleep(200,0);//200ms
				}
				catch (Exception e){

				}


            	addLogText("已连接，正在打开通知。。。",Color.BLACK,0);
            	if(mble.wakeUpBle())
            	{
            		addLogText("通知已打开",Color.BLACK,0);
            		sendBt.setEnabled(true);
            	}
            	else
            	{
            		addLogText("通知打开失败 ",Color.RED,0);
				}
            	
            }
        });
        
	}
	
	
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
		switch (item.getItemId()) 
        {
        case R.id.about_us:
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("http://www.tshjdz.com\r\nHJ-580 BLE串口透传测试程序（For Android,V1.7）\r\n唐山宏佳电子科技有限公司")
        	       .setCancelable(false)
        	       .setNegativeButton("确定", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	AlertDialog alert = builder.create();
        	alert.show();
            break;
        }
		return super.onMenuItemSelected(featureId, item);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.main, menu);

		menu.findItem(R.id.menu_refresh).setActionView(null);
		
		return super.onCreateOptionsMenu(menu);
	}



	void addLogText(final String log, final int color, int byteLen)
	{
		Message message = new Message();
        message.what = MSG_DATA_CHANGE;  
        message.arg1 = color;
        message.arg2 = byteLen;
        message.obj = log;
        mHandler.sendMessage(message);
	}
	
	HolloBluetooth.OnHolloBluetoothCallBack bleCallBack = new HolloBluetooth.OnHolloBluetoothCallBack()
	{
		
		@Override
		public void OnHolloBluetoothState(int state)
		{
			if(state == HolloBluetooth.HOLLO_BLE_DISCONNECTED)
			{
				addLogText("蓝牙已断开",Color.BLACK,0);
				onBackPressed();
			}
		}

		@Override
		public void OnReceiveData(byte[] recvData)
		{
			addLogText("接收：\r\n      "+ConvertData.bytesToHexString(recvData, false),Color.rgb(139, 0, 255),recvData.length);
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
        case android.R.id.home:
            onBackPressed();
            return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mble.disconnectDevice();
		Log.d(TAG, "destroy");
		mble.disconnectLocalDevice();
		Log.d(TAG, "销毁");
	}
}
