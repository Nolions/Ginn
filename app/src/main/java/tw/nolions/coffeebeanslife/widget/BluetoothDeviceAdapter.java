package tw.nolions.coffeebeanslife.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.model.DeviceModel;

public class BluetoothDeviceAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<DeviceModel> mDeviceList;

    public BluetoothDeviceAdapter(Context c) {
        this.mInflater = LayoutInflater.from(c);
        this.mDeviceList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return this.mDeviceList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return this.mDeviceList.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return this.mDeviceList.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = this.mInflater.inflate(R.layout.item_device, null);
        TextView name = (TextView) v.findViewById(R.id.device_name);
        TextView address = (TextView) v.findViewById(R.id.device_address);

        DeviceModel device = this.mDeviceList.get(position);

        name.setText(device.getName());
        address.setText(device.getAddress());

        return v;
    }

    public void clearnItem() {
        this.mDeviceList.clear();
    }

    public void addItem(DeviceModel model) {
        this.mDeviceList.add(model);
    }
}
