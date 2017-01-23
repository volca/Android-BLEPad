package com.example.testble;

public class HolloBluetoothException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public static final int ERROR_WAKE_UP_FAILED = 1;
	public static final int ERROR_SEND_FAILED = 2;
	public static final int ERROR_RECEIVE_TIME_OUT = 3;
	public static final int ERROR_RECEIVE_DATA = 4;
	public static final int ERROR_NOT_CONNECT = 5;

	private int mError;
	
	public HolloBluetoothException(int error)
	{
		mError = error;
	}
	
    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
	public String getErrorMsg()
	{
		switch (mError)
		{
		case ERROR_WAKE_UP_FAILED:
			return "唤醒蓝牙失败";
			
		case ERROR_SEND_FAILED:
			return "发送失败";
			
		case ERROR_RECEIVE_TIME_OUT:
			return "接收超时";

		case ERROR_RECEIVE_DATA:
			return "接收数据异常";
			
		case ERROR_NOT_CONNECT:
			return "设备未连接";
					
		default:
			return "未知错误";
		}
	}
}
