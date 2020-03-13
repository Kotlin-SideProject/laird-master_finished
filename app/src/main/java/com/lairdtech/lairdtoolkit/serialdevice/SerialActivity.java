/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.serialdevice;

import android.bluetooth.BluetoothGatt;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.lairdtech.lairdtoolkit.R;
import com.lairdtech.lairdtoolkit.bases.BaseActivity;

import java.util.StringTokenizer;

public class SerialActivity extends BaseActivity implements SerialManagerUiCallback{

	private Button mBtnSend;
	private EditText mValueVspInputEt;
	private ScrollView mScrollViewVspOut;
	private TextView mValueVspOutTv;
//	private TextView mValueRxCounterTv;
//	private TextView mValueTxCounterTv;

	private SerialManager mSerialManager;

	private boolean isPrefClearTextAfterSending = false;


	//Angus ADD BY 2019/05/31

	private LineChart mChart;
	private Thread thread;
	private boolean plotData = true;
	String s0 = "";
	private String TAG = SerialActivity.class.getSimpleName();

//	private EditText imeiInput;
//	private EditText zipcardInput;
//	private TextView resStartLabel;
//	private DatePicker resStartDateInput;
//	private TimePicker resStartTimeInput;
//	private TextView resEndLabel;
//	private DatePicker resEndDateInput;
//	private TimePicker resEndTimeInput;
//	private Switch lockOpToggle;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_serial);
		super.onCreate(savedInstanceState);

		mSerialManager = new SerialManager(this, this);
		setBleDeviceBase(mSerialManager.getVSPDevice());

		initialiseDialogAbout(getResources().getString(R.string.about_serial));
		initialiseDialogFoundDevices("VSP");
		mBtnSend.setEnabled(true);

		mChart = (LineChart)findViewById(R.id.chart1);
		mChart.getDescription().setEnabled(false);
//        mChart.getDescription().setText("Real Time EMG Signal");
		mChart.getDescription().setTextColor(Color.RED);

		mChart.setTouchEnabled(true);
		mChart.setDragEnabled(true);
		mChart.setScaleEnabled(true);
		mChart.setDrawGridBackground(true);
		mChart.setPinchZoom(true);
		mChart.setBackgroundColor(Color.BLACK);

		LineData data = new LineData();
		data.setValueTextColor(Color.WHITE);
		mChart.setData(data);

		Legend l = mChart.getLegend();

		l.setForm(Legend.LegendForm.LINE);
		l.setTextColor(Color.WHITE);

		XAxis x1 = mChart.getXAxis();
		x1.setTextColor(Color.WHITE);
		x1.setDrawGridLines(true);
		x1.setAvoidFirstLastClipping(true);
		x1.setEnabled(false);

		YAxis leftAxis = mChart.getAxisLeft();
		leftAxis.setTextColor(Color.WHITE);
		leftAxis.setAxisMaximum(10000f);
		leftAxis.setAxisMinimum(0f);
		leftAxis.setDrawGridLines(true);

		YAxis rightAxis = mChart.getAxisRight();
		rightAxis.setEnabled(false);

		mChart.getAxisLeft().setDrawGridLines(true);
		mChart.getXAxis().setDrawGridLines(true);
		mChart.setDrawBorders(true);

		startPlot();
	}


	private  void startPlot(){
		if (thread != null){
			thread.interrupt();
		}
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					plotData = true;
					try{
						Thread.sleep(1);

					}catch (InterruptedException e){
						e.printStackTrace();

					}
				}
			}
		});
		thread.start();

	}

	private void addEntry1(String dataReceived){
		LineData data = mChart.getData();
		if(data != null){
			LineDataSet set  = (LineDataSet) data.getDataSetByIndex(0);
			if (set == null){
				set = createSet();
				data.addDataSet(set);
			}
			 dataReceived = s0 + dataReceived;
			///string translate  1/n2/n3/n4/n5/n
			StringTokenizer st  = new StringTokenizer(dataReceived,"\n");
//			int n = st.countTokens();
			while (st.hasMoreTokens()){
				String s = st.nextToken();
				if(s.length() == 5){
					decoder d = new decoder(s);
					data.addEntry(new Entry(set.getEntryCount(),d.decoderData()),0);
					Log.d(TAG, "decoderData:" + d.decoderData());
				}else if(s.length()<5){
					s0 = s;
				}

			}
			data.notifyDataChanged();

			mChart.notifyDataSetChanged();

			mChart.setVisibleXRange(200,200);
			//mChart.setMaxVisibleValueCount(150);

			//mChart.moveViewToX(data.getEntryCount()-100);
			mChart.moveViewToX(data.getEntryCount());

		}
	}

	private LineDataSet createSet(){
		LineDataSet set  = new LineDataSet(null,"Real Time EMG Signal");
		set.setAxisDependency(YAxis.AxisDependency.LEFT);
		set.setLineWidth(3f);
		set.setColor(Color.MAGENTA);
		set.setHighlightEnabled(false);
		set.setDrawValues(false);
		set.setDrawCircles(false);
		set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
		set.setCubicIntensity(0.001f);
		return set;
	}

	/*
	 * *************************************
	 * UI methods
	 * *************************************
	 */
	@Override
	protected void bindViews(){
		super.bindViews();
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mScrollViewVspOut = (ScrollView) findViewById(R.id.scrollViewVspOut);
		mValueVspInputEt = (EditText) findViewById(R.id.valueVspInputEt);
		mValueVspOutTv = (TextView) findViewById(R.id.valueVspOutTv);
//		mValueRxCounterTv = (TextView) findViewById(R.id.valueRxCounterTv);
//		mValueTxCounterTv = (TextView) findViewById(R.id.valueTxCounterTv);

//		imeiInput = (EditText) findViewById(R.id.imeiInput_view);
//		zipcardInput = (EditText) findViewById(R.id.zipcardInput_view);
//		resStartLabel = (TextView) findViewById(R.id.resStartLabel_view);
//		resStartDateInput = (DatePicker) findViewById(R.id.resStartDateInput_view);
//		resStartTimeInput = (TimePicker) findViewById(R.id.resStartTimeInput_view);
//		resEndLabel = (TextView) findViewById(R.id.resEndLabel_view);
//		resEndDateInput = (DatePicker) findViewById(R.id.resEndDateInput_view);
//		resEndTimeInput = (TimePicker) findViewById(R.id.resEndTimeInput_view);
//		lockOpToggle = (Switch) findViewById(R.id.lockOpToggle_switch);
	}

	@Override
	protected void setListeners(){
		super.setListeners();

		mBtnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				/*
				 * *********************
				 * to send data to module
				 * *********************
				 */
//				String data = mValueVspInputEt.getText().toString();
//				if(data != null){
//					mBtnSend.setEnabled(false);
//					if(mValueVspOutTv.getText().length() <= 0){
//						mValueVspOutTv.append(">");
//					} else{
//						mValueVspOutTv.append("\n\n>");
//					}
//
//					mSerialManager.startDataTransfer(data + "\r");
//
//					InputMethodManager inputManager = (InputMethodManager)
//							getSystemService(Context.INPUT_METHOD_SERVICE);
//
//					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
//							InputMethodManager.HIDE_NOT_ALWAYS);
//
//					if(isPrefClearTextAfterSending == true){
//						mValueVspInputEt.setText("");
//					} else{
//						// do not clear the text from the editText
//					}
//
//				}


//				String imeiString = imeiInput.getText().toString();
//				String zipcardString = zipcardInput.getText().toString();
//				String resStart = Integer.toString(resStartDateInput.getMonth()+1) + "/" +
//						Integer.toString(resStartDateInput.getDayOfMonth()) + "/" +
//						Integer.toString(resStartDateInput.getYear()) + " " +
//						Integer.toString(resStartTimeInput.getCurrentHour()) + ":" +
//						Integer.toString(resStartTimeInput.getCurrentMinute());
//				String resEnd = Integer.toString(resEndDateInput.getMonth()+1) + "/" +
//						Integer.toString(resEndDateInput.getDayOfMonth()) + "/" +
//						Integer.toString(resEndDateInput.getYear()) + " " +
//						Integer.toString(resEndTimeInput.getCurrentHour()) + ":" +
//						Integer.toString(resEndTimeInput.getCurrentMinute());
//				String lockOp = "";
//				if(lockOpToggle.isChecked())
//					lockOp = lockOpToggle.getTextOn().toString();
//				else
//					lockOp = lockOpToggle.getTextOff().toString();
//
//				String zipData = "IMEI: " + imeiString + ", ZIPCARD: " + zipcardString +
//						", START: " + resStart + ", END: " + resEnd + ", OP: " + lockOp;
//				if(zipData != null){
//					mValueVspOutTv.append(zipData);
//				} else {
//					mValueVspOutTv.append("");
//				}

			}
		});
	}

	@Override
	protected void onPause(){
		super.onPause();

		if(isInNewScreen == true
				|| isPrefRunInBackground == true){
			// let the app run normally in the background
		} else{
			// stop scanning or disconnect if we are connected
			if(mBluetoothAdapterWrapper.isBleScanning()){
				mBluetoothAdapterWrapper.stopBleScan();

			} else if(getBleDeviceBase().isConnecting()
					|| getBleDeviceBase().isConnected()){
				getBleDeviceBase().disconnect();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.serial, menu);
		getActionBar().setIcon(R.drawable.icon_serial);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (mBluetoothAdapterWrapper.isBleScanning() == true) {
			menu.findItem(R.id.action_scanning_indicator).setActionView(R.layout.progress_indicator);
		} else {
			menu.findItem(R.id.action_scanning_indicator).setActionView(null);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		
		case R.id.action_clear:
			mValueVspOutTv.setText("");
			mSerialManager.getVSPDevice().clearRxAndTxCounter();

//			mValueRxCounterTv.setText("0");
//			mValueTxCounterTv.setText("0");

			break;
		}
		return super.onOptionsItemSelected(item);
	}


	/*
	 * *************************************
	 * SerialManagerUiCallback
	 * *************************************
	 */
	@Override
	public void onUiConnected(BluetoothGatt gatt) {
		uiInvalidateBtnState();
	}

	@Override
	public void onUiDisconnect(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnSend.setEnabled(false);
			}
		});
		uiInvalidateBtnState();
	}

	@Override
	public void onUiConnectionFailure(
			final BluetoothGatt gatt){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnSend.setEnabled(false);
			}
		});
		uiInvalidateBtnState();
	}

	@Override
	public void onUiBatteryReadSuccess(String result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUiReadRemoteRssiSuccess(int rssi) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUiBonded() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUiVspServiceNotFound(BluetoothGatt gatt) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				mBtnSend.setEnabled(false);
			}
		});
	}

	@Override
	public void onUiVspRxTxCharsNotFound(BluetoothGatt gatt) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				mBtnSend.setEnabled(false);
			}
		});
	}

	@Override
	public void onUiVspRxTxCharsFound(BluetoothGatt gatt) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				mBtnSend.setEnabled(true);
			}
		});
	}

	@Override
	public void onUiSendDataSuccess(
			final String dataSend) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mValueVspOutTv.append(dataSend);
//				mValueTxCounterTv.setText("" + mSerialManager.getVSPDevice().getTxCounter());
//				mScrollViewVspOut.smoothScrollTo(0, mValueVspOutTv.getBottom());
			}
		});
	}

	@Override
	public void onUiReceiveData(final String dataReceived) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				mValueVspOutTv.append(dataReceived);
				addEntry1(dataReceived);
//				mValueRxCounterTv.setText("" + mSeri
//				alManager.getVSPDevice().getRxCounter());
//				mScrollViewVspOut.smoothScrollTo(0, mValueVspOutTv.getBottom());
			}
		});
	}

	@Override
	public void onUiEMGChange(final String dataReceived) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				mValueHeartRate.setText(mCharHrMeasurement + " bpm");
				while(plotData == true){
					addEntry1(dataReceived);
//					plotData = false;
				}
			}
		});
	}

	@Override
	public void onUiUploaded() {
		mBtnSend.setEnabled(true);
	}


	/*
	 * *************************************
	 * other
	 * *************************************
	 */
	@Override
	protected void loadPref(){
		super.loadPref();
		isPrefClearTextAfterSending = mSharedPreferences.getBoolean("pref_clear_text_after_sending", false);
	}
}