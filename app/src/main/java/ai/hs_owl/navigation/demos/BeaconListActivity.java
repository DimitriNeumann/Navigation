package ai.hs_owl.navigation.demos;

import java.util.ArrayList;

import ai.hs_owl.navigation.R;
import ai.hs_owl.navigation.protocol.IBeacon;
import ai.hs_owl.navigation.protocol.IBeaconListener;
import ai.hs_owl.navigation.protocol.IBeaconProtocol;
import ai.hs_owl.navigation.protocol.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

// This activity implements IBeaconListener to receive events about iBeacon discovery
public class BeaconListActivity extends ListActivity implements IBeaconListener{
	
	private static final int REQUEST_BLUETOOTH_ENABLE = 1;	

	private static ArrayList<IBeacon> _beacons;
	private ArrayAdapter<IBeacon> _beaconsAdapter;
	private static IBeaconProtocol _ibp;
	
	private Menu _menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(_beacons == null)
			_beacons = new ArrayList<IBeacon>();
		_beaconsAdapter = new ArrayAdapter<IBeacon>(this, android.R.layout.simple_list_item_2, android.R.id.text1, _beacons){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				
				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				TextView text2 = (TextView) view.findViewById(android.R.id.text2);
				
				IBeacon beacon = _beacons.get(position);
				
				text1.setText(beacon.getUuidHexStringDashed());
				text2.setText("Major: " + beacon.getMajor() + " Minor: " + beacon.getMinor() + " Distance: " + beacon.getProximity() + "m.");
				return view;
			}
		};
		
		setListAdapter(_beaconsAdapter);
		
		_ibp = IBeaconProtocol.getInstance(this);
		_ibp.setListener(this);

	}

	@Override
	protected void onStop() {
		_ibp.stopScan();
		super.onStop();
	}
	
	private void scanBeacons(){
		// Check Bluetooth every time
		Log.i(Utils.LOG_TAG,"Scanning");
		
		// Filter based on default easiBeacon UUID, remove if not required
		//_ibp.setScanUUID(UUID here);

		if(!IBeaconProtocol.configureBluetoothAdapter(this)){
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE );
		}else{
			if(_ibp.isScanning())
				_ibp.stopScan();
			_ibp.reset();
			_ibp.startScan();		
		}		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_BLUETOOTH_ENABLE){
			if(resultCode == Activity.RESULT_OK){
				scanBeacons();
			}
		}
	}	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.beacon_list, menu);
		_menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			_beacons.clear();
			_beaconsAdapter.notifyDataSetChanged();
			scanBeacons();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("InflateParams")
	private void startRefreshAnimation(){
		MenuItem item = _menu.findItem(R.id.action_refresh);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ImageView iv = (ImageView)inflater.inflate(R.layout.refresh_button, null);
		Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
		rotation.setRepeatCount(Animation.INFINITE);
		iv.startAnimation(rotation);
		item.setActionView(iv);		
	}
	
	private void stopRefreshAnimation(){
		MenuItem item = _menu.findItem(R.id.action_refresh);
        if(item.getActionView()!=null){
            // Remove the animation.
            item.getActionView().clearAnimation();
            item.setActionView(null);
        }
	}
	
	
	// The following methods implement the IBeaconListener interface
	
	@Override
	public void beaconFound(IBeacon ibeacon) {
		_beacons.add(ibeacon);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				_beaconsAdapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public void enterRegion(IBeacon ibeacon) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void exitRegion(IBeacon ibeacon) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void operationError(int status) {
		Log.i(Utils.LOG_TAG, "Bluetooth error: " + status);	
		
	}
	
	@Override
	public void searchState(int state) {
		if(state == IBeaconProtocol.SEARCH_STARTED){
			startRefreshAnimation();
		}else if (state == IBeaconProtocol.SEARCH_END_EMPTY || state == IBeaconProtocol.SEARCH_END_SUCCESS){
			stopRefreshAnimation();
		}
		
	}
}
