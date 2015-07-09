
package com.zxt.dlna.dmr;

import java.util.ArrayList;

import com.zxt.dlna.dmp.DeviceItem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.zxt.dlna.R;

public class DevicesAdapter extends BaseAdapter {
    private static final String TAG = "DevicesAdapter";

    // private AsyncImageLoader asyncImageLoader;
    private Context context;

    private ArrayList<DeviceItem> deviceItems;

    public int dmrPosition = 0;

    private ListView listView;

    private LayoutInflater mInflater;

    private DeviceItem selectDeviceItem = null;

    public DevicesAdapter(Context paramContext, ArrayList<DeviceItem> paramArrayList,
            ListView paramListView) {
        this.deviceItems = paramArrayList;
        this.context = paramContext;
        this.listView = paramListView;
        // this.asyncImageLoader = new AsyncImageLoader();
        this.mInflater = ((LayoutInflater) paramContext.getSystemService("layout_inflater"));
    }

    public int getCount() {
        return this.deviceItems.size();
    }

    public Object getItem(int paramInt) {
        return this.deviceItems.get(paramInt);
    }

    public long getItemId(int paramInt) {
        return paramInt;
    }

    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {

        Holder localHolder;
        if (paramView == null) {
            paramView = this.mInflater.inflate(R.layout.dmr_item, null);
            localHolder = new Holder();
            localHolder.filename = ((TextView) paramView.findViewById(R.id.dmr_name_tv));
            paramView.setTag(localHolder);
        } else {
            localHolder = (Holder) paramView.getTag();
        }

        DeviceItem localDeviceItem = (DeviceItem) this.deviceItems.get(paramInt);
        localHolder.filename.setText(localDeviceItem.toString());
        localHolder.filename.setTextColor(Color.WHITE);
        return paramView;

    }

    public void setSelectDevices() {
        // ((BaseApplication)((HomeActivity)this.context).getApplication()).dmrDeviceItem
        // = null;
        this.selectDeviceItem = null;
        this.dmrPosition = 0;
    }

    class Holder {
        TextView filename;

        ImageView imageView;

        Holder() {
        }
    }
}
