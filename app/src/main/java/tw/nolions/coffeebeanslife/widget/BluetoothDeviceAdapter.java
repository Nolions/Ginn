package tw.nolions.coffeebeanslife.widget;

import android.bluetooth.BluetoothDevice;
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
    private ArrayList<BluetoothDevice> mPairedDevices, mNoPairedDevices;

    private Context mContext;
    final private int TYPE_Count = 2;
    final public static int PAIRED_ITEM_TYPE = 0, NoPAIRED_ITEM_TYPE =1;

    public BluetoothDeviceAdapter(Context c) {
        this.mContext = c;
        this.mInflater = LayoutInflater.from(c);
        this.mPairedDevices = new ArrayList<>();
        this.mNoPairedDevices = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return this.mPairedDevices.size() + this.mNoPairedDevices.size();
    }

    @Override
    public Object getItem(int arg0) {
//        return this.mDeviceList.get(arg0);

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < mPairedDevices.size())? PAIRED_ITEM_TYPE: NoPAIRED_ITEM_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_Count;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemType = getItemViewType(position);
        View v = this.mInflater.inflate(R.layout.item_device, null);
        TextView nameTextView = (TextView) v.findViewById(R.id.device_name);
        TextView statusTextView = (TextView) v.findViewById(R.id.device_status);
        BluetoothDevice device;

        String name = "";
        String status = "";

        switch (itemType) {
            case PAIRED_ITEM_TYPE:
                device = this.mPairedDevices.get(position);
                name = device.getName();
                break;
            case NoPAIRED_ITEM_TYPE:
                device = this.mNoPairedDevices.get(position - mPairedDevices.size());
                name =  device.getAddress();

                status = "裝置連接時將顯示裝置名稱";
                break;
        }

        nameTextView.setText(name);
        statusTextView.setText(status);

        return v;
    }

    public void clearData(int itemType) {
        if (itemType == NoPAIRED_ITEM_TYPE) {
            this.mNoPairedDevices.clear();
        } else if(itemType == PAIRED_ITEM_TYPE) {
            this.mPairedDevices.clear();
        }

    }

    public void setData(int itemType, ArrayList<BluetoothDevice> list) {
        if (itemType == NoPAIRED_ITEM_TYPE) {
            this.mNoPairedDevices = list;
        } else if(itemType == PAIRED_ITEM_TYPE) {
            this.mPairedDevices = list;
        }

    }

    public void addItem(int itemType, BluetoothDevice device) {
        if (itemType == NoPAIRED_ITEM_TYPE) {
            this.mNoPairedDevices.add(device);
        } else if(itemType == PAIRED_ITEM_TYPE) {
            this.mPairedDevices.add(device);
        }

    }
}
