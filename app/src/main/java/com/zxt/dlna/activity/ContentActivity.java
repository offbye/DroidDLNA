package com.zxt.dlna.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import java.util.LinkedList;
import java.util.List;

import java.util.Map;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.seamless.util.MimeType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.zxt.dlna.R;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.application.ConfigData;
import com.zxt.dlna.dmc.GenerateXml;
import com.zxt.dlna.dmp.ContentItem;
import com.zxt.dlna.dmp.ImageDisplay;
import com.zxt.dlna.dms.ContentBrowseActionCallback;

public class ContentActivity extends Activity {

	public static final int CONTENT_GET_FAIL = 0;

	public static final int CONTENT_GET_SUC = 1;

	private ListView mContentLv;

	private TextView mTitleView;

	private ProgressBar mProgressBarPreparing;

	private ArrayList<ContentItem> mContentList = new ArrayList<ContentItem>();

	private ContentAdapter mContentAdapter;

	private BaseApplication mBaseApplication;

	private AndroidUpnpService upnpService;

	private String currentContentFormatMimeType = "";

	private Context mContext;

	private Map<Integer, ArrayList<ContentItem>> mSaveDirectoryMap;

	private Integer mCounter = 0;

	private String mLastDevice;

	private String mThumbUri;

	DisplayImageOptions options;

	protected ImageLoader imageLoader = ImageLoader.getInstance();

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CONTENT_GET_FAIL: {

				break;
			}
			case CONTENT_GET_SUC: {

				mContentAdapter.notifyDataSetChanged();
				mProgressBarPreparing.setVisibility(View.GONE);

				mCounter++;

				ArrayList<ContentItem> tempContentList = new ArrayList<ContentItem>();
				tempContentList.addAll(mContentList);
				mSaveDirectoryMap.put(mCounter - 1, tempContentList);
				break;
			}

			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_lay);

		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.icon_image)
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error).cacheInMemory()
				.cacheOnDisc().displayer(new RoundedBitmapDisplayer(20))
				.build();

		initView();
		// setAnimation();
	}

	@Override
	protected void onResume() {
		mContext = ContentActivity.this;
		mBaseApplication = (BaseApplication) this.getApplication();
		if (null == mBaseApplication.deviceItem) {
			Toast.makeText(mContext, R.string.not_select_dev,
					Toast.LENGTH_SHORT).show();
		} else if (null == mLastDevice || "" == mLastDevice
				|| mLastDevice != mBaseApplication.deviceItem.toString()) {
			initData();
		} else {

		}
		super.onResume();
	}

	private void initView() {
		mTitleView = (TextView) findViewById(R.id.dev_name_tv);
		mProgressBarPreparing = (ProgressBar) findViewById(R.id.player_prepairing);
		mContentLv = (ListView) findViewById(R.id.content_list);
		mContentAdapter = new ContentAdapter(ContentActivity.this, mContentList);
		mContentLv.setAdapter(mContentAdapter);
		mContentLv.setOnItemClickListener(contentItemClickListener);
	}

	private void initData() {

		mCounter = 0;
		if (null == mSaveDirectoryMap) {
			mSaveDirectoryMap = new HashMap<Integer, ArrayList<ContentItem>>();
		} else {
			mSaveDirectoryMap.clear();
		}
		upnpService = mBaseApplication.upnpService;
		mTitleView.setText(mBaseApplication.deviceItem.toString());
		Device device = mBaseApplication.deviceItem.getDevice();
		Service service = device.findService(new UDAServiceType(
				"ContentDirectory"));
		upnpService.getControlPoint().execute(
				new ContentBrowseActionCallback(ContentActivity.this,
						service, createRootContainer(service), mContentList,
						mHandler));

		mLastDevice = mBaseApplication.deviceItem.toString();
	}

	protected Container createRootContainer(Service service) {
		Container rootContainer = new Container();
		rootContainer.setId("0");
		rootContainer.setTitle("Content Directory on "
				+ service.getDevice().getDisplayString());
		return rootContainer;
	}

	private void setAnimation() {

		AnimationSet set = new AnimationSet(false);
		Animation animation = new AlphaAnimation(0, 1); // AlphaAnimation
														// 控制渐变透明的动画效果
		animation.setDuration(500); // 动画时间毫秒数
		set.addAnimation(animation); // 加入动画集合

		animation = new TranslateAnimation(1, 13, 10, 50); // ScaleAnimation
															// 控制尺寸伸缩的动画效果
		animation.setDuration(300);
		set.addAnimation(animation);

		animation = new RotateAnimation(30, 10); // TranslateAnimation
													// 控制画面平移的动画效果
		animation.setDuration(300);
		set.addAnimation(animation);

		animation = new ScaleAnimation(5, 0, 2, 0); // RotateAnimation
													// 控制画面角度变化的动画效果
		animation.setDuration(300);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 1);

		mContentLv.setLayoutAnimation(controller); // ListView 设置动画效果

	}

	OnItemClickListener contentItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			// TODO Auto-generated method stub
			ContentItem content = mContentList.get(position);
			if (content.isContainer()) {
				mProgressBarPreparing.setVisibility(View.VISIBLE);
				upnpService.getControlPoint()
						.execute(
								new ContentBrowseActionCallback(
										ContentActivity.this, content
												.getService(), content
												.getContainer(), mContentList,
										mHandler));
			} else {
				MimeType localMimeType = content.getItem().getResources()
						.get(0).getProtocolInfo().getContentFormatMimeType();
				if (null == localMimeType) {
					return;
				}
				String type = localMimeType.getType();
				if (null == type) {
					return;
				}
				currentContentFormatMimeType = localMimeType.toString();

				Intent intent = new Intent();
				if (type.equals("image")) {
					ConfigData.photoPosition = position;
					jumpToImage(content);
				} else {
					jumpToControl(content);
				}

			}
		}
	};

	private void jumpToControl(ContentItem localContentItem) {
		Intent localIntent = new Intent("com.transport.info");
		localIntent.putExtra("name", localContentItem.toString());
		localIntent.putExtra("playURI", localContentItem.getItem()
				.getFirstResource().getValue());
		localIntent.putExtra("currentContentFormatMimeType",
				currentContentFormatMimeType);
		try {
			localIntent.putExtra("metaData",
					new GenerateXml().generate(localContentItem));
		} catch (Exception e) {
			e.printStackTrace();
		}
		IndexActivity.mTabHost.setCurrentTabByTag(getString(R.string.control));
		IndexActivity.setSelect();
		this.sendBroadcast(localIntent);
	}

	private void jumpToImage(ContentItem localContentItem) {
		Intent localIntent = new Intent(ContentActivity.this,
				ImageDisplay.class);
		localIntent.putExtra("name", localContentItem.toString());
		localIntent.putExtra("playURI", localContentItem.getItem()
				.getFirstResource().getValue());
		localIntent.putExtra("currentContentFormatMimeType",
				currentContentFormatMimeType);
		try {
			// localIntent.putExtra("metaData",
			// new GenerateXml().generate(localContentItem));
			localIntent.putExtra("metaData",
					new GenerateXml().generate(localContentItem));
		} catch (Exception e) {
			e.printStackTrace();
		}
		ContentActivity.this.startActivity(localIntent);
	}

	private String getThumbUri(ContentItem contentItem) {
		String thumbUri = null;
		int i = contentItem.getItem().getProperties().size();
		for (int j = 0; j < i; j++) {
			if (null != contentItem.getItem()
					&& null != contentItem.getItem().getProperties()
					&& null != contentItem.getItem().getProperties().get(j)
					&& ((DIDLObject.Property) contentItem.getItem()
							.getProperties().get(j)).getDescriptorName()
							.equals("albumArtURI")) {

				thumbUri = ((DIDLObject.Property) contentItem.getItem()
						.getProperties().get(j)).getValue().toString();
				break;
			}
		}

		return thumbUri;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mCounter > 1) {
				mSaveDirectoryMap.remove(mCounter - 1);
				mCounter--;
				mContentList.clear();
				mContentList.addAll(mSaveDirectoryMap.get(mCounter - 1));
				mContentAdapter.notifyDataSetChanged();
				return true;
			}else{
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	class ContentAdapter extends BaseAdapter {

		private static final String TAG = "ContentAdapter";

		private Context context;

		private LayoutInflater mInflater;

		private Bitmap imageIcon;

		private Bitmap videoIcon;

		private Bitmap audioIcon;

		private Bitmap folderIcon;

		public int dmrPosition = 0;

		private ArrayList<ContentItem> mDeviceItems;

		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

		public ContentAdapter(Context paramContext,
				ArrayList<ContentItem> paramArrayList) {
			this.mInflater = ((LayoutInflater) paramContext
					.getSystemService("layout_inflater"));
			this.context = paramContext;
			this.mDeviceItems = paramArrayList;
			imageIcon = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.icon_image);
			videoIcon = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.icon_video);
			audioIcon = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.icon_audio);
			folderIcon = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.icon_folder);
		}

		public int getCount() {
			return this.mDeviceItems.size();
		}

		public Object getItem(int paramInt) {
			return this.mDeviceItems.get(paramInt);
		}

		public long getItemId(int paramInt) {
			return paramInt;
		}

		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			final ContentHolder localHolder;
			if (paramView == null) {
				paramView = this.mInflater.inflate(R.layout.content_item, null);
				localHolder = new ContentHolder();
				localHolder.filename = (TextView) paramView
						.findViewById(R.id.content_title_tv);
				localHolder.folder = (ImageView) paramView
						.findViewById(R.id.icon_folder);
				localHolder.arrow = (ImageView) paramView
						.findViewById(R.id.icon_arrow);
				paramView.setTag(localHolder);
			} else {
				localHolder = (ContentHolder) paramView.getTag();
			}
			ContentItem contentItem = (ContentItem) this.mDeviceItems
					.get(paramInt);

			localHolder.filename.setText(contentItem.toString());

			if (!contentItem.isContainer()) {
				String imageUrl = null;
				if (null != contentItem.getItem().getResources().get(0)
						.getProtocolInfo().getContentFormatMimeType()) {
					String type = contentItem.getItem().getResources().get(0)
							.getProtocolInfo().getContentFormatMimeType()
							.getType();
					if (type.equals("image")) {
						localHolder.folder.setImageBitmap(imageIcon);
						// if is image, display it
						imageUrl = contentItem.getItem().getFirstResource()
								.getValue();
					} else if (type.equals("video")) {
						localHolder.folder.setImageBitmap(videoIcon);
					} else if (type.equals("audio")) {
						localHolder.folder.setImageBitmap(audioIcon);
					}

				}

				int i = contentItem.getItem().getProperties().size();
				for (int j = 0; j < i; j++) {
					if (null != contentItem.getItem()
							&& null != contentItem.getItem().getProperties()
							&& null != contentItem.getItem().getProperties()
									.get(j)
							&& ((DIDLObject.Property) contentItem.getItem()
									.getProperties().get(j))
									.getDescriptorName().equals("albumArtURI")) {

						imageUrl = ((DIDLObject.Property) contentItem.getItem()
								.getProperties().get(j)).getValue().toString();
						break;
					}
				}
				imageLoader.displayImage(imageUrl, localHolder.folder, options,
						animateFirstListener);
				localHolder.arrow.setVisibility(View.GONE);
			} else {
				localHolder.folder.setImageBitmap(folderIcon);
				localHolder.arrow.setVisibility(View.VISIBLE);

			}
			return paramView;
		}

		class ContentHolder {

			public TextView filename;

			public ImageView folder;

			public ImageView arrow;

			public ContentHolder() {
			}
		}

	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

}
