package com.zxt.dlna.activity;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import com.zxt.dlna.R;

@SuppressWarnings("deprecation")
public class IndexActivity extends TabActivity {

	public static TabHost mTabHost;

	private static RadioButton mDeviceRb;

	private static RadioButton mContentRb;

	private static RadioButton mControlRb;

	private static RadioButton mSettingsRb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);
		findViews();
	}

	private void findViews() {

		mDeviceRb = (RadioButton) findViewById(R.id.main_tab_devices);
		mContentRb = (RadioButton) findViewById(R.id.main_tab_content);
		mControlRb = (RadioButton) findViewById(R.id.main_tab_control);
		mSettingsRb = (RadioButton) findViewById(R.id.main_tab_settings);

		mTabHost = this.getTabHost();

		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent().setClass(this, DevicesActivity.class);
		spec = mTabHost.newTabSpec(getString(R.string.device))
				.setIndicator(getString(R.string.device)).setContent(intent);
		mTabHost.addTab(spec);

		intent = new Intent().setClass(this, ContentActivity.class);
		spec = mTabHost.newTabSpec(getString(R.string.content))
				.setIndicator(getString(R.string.content)).setContent(intent);
		mTabHost.addTab(spec);

		intent = new Intent().setClass(this, ControlActivity.class);
		spec = mTabHost.newTabSpec(getString(R.string.control))
				.setIndicator(getString(R.string.control)).setContent(intent);
		mTabHost.addTab(spec);

		intent = new Intent().setClass(this, SettingActivity.class);
		spec = mTabHost.newTabSpec(getString(R.string.setting))
				.setIndicator(getString(R.string.setting)).setContent(intent);
		mTabHost.addTab(spec);
		mTabHost.setCurrentTab(0);

		RadioGroup radioGroup = (RadioGroup) this
				.findViewById(R.id.main_tab_group);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.main_tab_devices:
					mTabHost.setCurrentTabByTag(getString(R.string.device));
					break;
				case R.id.main_tab_content:
					mTabHost.setCurrentTabByTag(getString(R.string.content));
					break;
				case R.id.main_tab_control:
					mTabHost.setCurrentTabByTag(getString(R.string.control));
					break;
				case R.id.main_tab_settings:
					mTabHost.setCurrentTabByTag(getString(R.string.setting));
					break;
				default:
					break;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.menu_exit).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			finish();
			System.exit(0);
			break;
		}
		return false;
	}

	public static void setSelect() {
		mControlRb.setChecked(true);
	}
}
