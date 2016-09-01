package nedo.bt1.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

import nedo.bt1.MainActivity;
import nedo.bt1.R;

/**
 * Created by fejzi on 21.08.2016..
 */
public class fragment_paired extends Fragment {

    private static final String TAG = "FragmentOne";

    private BluetoothAdapter btAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    ListView pairedListView;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    View roditelj;
    TextView test;

    public fragment_paired() {
        //setResult(Activity.RESULT_CANCELED);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        roditelj = inflater.inflate(R.layout.fragment_paired, container, false);


        return roditelj;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // do your variables initialisations here except Views!!!
        // Initialize array adapter for paired devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.naziv_uredjaja);


        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            mPairedDevicesArrayAdapter.add("no devices paired");
        }

    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pairedListView = (ListView) roditelj.findViewById(R.id.paired_devices);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);


    }


    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(getActivity(), MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);



            getActivity().setResult(Activity.RESULT_OK, i);
            getActivity().finish();

        }
    };

}
