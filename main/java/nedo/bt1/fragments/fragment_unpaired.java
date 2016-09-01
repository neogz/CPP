package nedo.bt1.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

import nedo.bt1.R;

/**
 * Created by fejzi on 21.08.2016..
 */
public class fragment_unpaired extends Fragment {

    private static final String TAG = "FragmentTwo";
    private static final int REQUEST_PAIR_DEVICE = 6;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    ListView unpairedListView;
    View roditelj;
    ProgressBar progresija;

    String address;

    public fragment_unpaired() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        roditelj = inflater.inflate(R.layout.fragment_unpaired, container, false);
        return roditelj;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.naziv_uredjaja);


        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /////////////////////////////////
    // :) :(

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progresija = (ProgressBar) roditelj.findViewById(R.id.samoProgres);

        unpairedListView = (ListView) roditelj.findViewById(R.id.novi_uredjaji);
        unpairedListView.setOnItemClickListener(mDeviceClickListener);
        unpairedListView.setAdapter(mNewDevicesArrayAdapter);


        Button scanButton = (Button) roditelj.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.getActivity().registerReceiver(mReceiver, filter);




    }

    private void doDiscovery() {


        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mBtAdapter.startDiscovery();

        progresija.setVisibility(View.VISIBLE);
        progresija.setScaleY(3f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        this.getActivity().unregisterReceiver(mReceiver);
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());


                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(getActivity().getBaseContext(), "spasen", Toast.LENGTH_SHORT).show();

                }
            }
        }
    };

    private void pairDevice(String address) {
        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // android kitkat+
        //device.createBond();
    }


    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int pozicija, long arg3) {


            mBtAdapter.cancelDiscovery();
            String info = ((TextView) v).getText().toString();
            address = info.substring(info.length() - 17);

            pairDevice(address);

            progresija.setIndeterminate(false);
            String izbrisati = mNewDevicesArrayAdapter.getItem(pozicija);
            mNewDevicesArrayAdapter.remove(izbrisati);
            mNewDevicesArrayAdapter.notifyDataSetChanged();

        }
    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();


    }
}
