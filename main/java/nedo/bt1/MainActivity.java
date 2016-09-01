package nedo.bt1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements DijalogKomande.Communicator {
    private static final String MY_PREFS_NAME = "komande" ;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle hamburger;
    private TextView mTitle;
    Toolbar toolbar;

    String kodOtkljucavanja, kodZakljucavanja;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BTService mCommandService = null;


    public static final String DEFAULT = "N/A";
    private ImageButton otkljucaj, zakljucaj;
    private Button konektujse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        provjeriKomande();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        hamburger = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_closed);
        drawerLayout.addDrawerListener(hamburger);

        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        ////
        otkljucaj = (ImageButton) findViewById(R.id.btnOtkljucaj);
        otkljucaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                posaljiKomandu(true);
            }
        });

        zakljucaj = (ImageButton) findViewById(R.id.btnZakljucaj);
        zakljucaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                posaljiKomandu(false);
            }
        });

        konektujse = (Button) findViewById(R.id.konektujSe);
        konektujse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                konektujSeMetoda();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.scan:
                        // Launch the DeviceListActivity to see devices and do scan
                        Intent serverIntent = new Intent(navigationView.getContext(), pretraga.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                        return true;

                    case R.id.help:
                        Intent i = new Intent(navigationView.getContext(), help.class);
                        startActivity(i);
                        return true;

                    case R.id.meniKomande:
                        android.app.FragmentManager fm = getFragmentManager();
                        DijalogKomande dijalog = new DijalogKomande();
                        dijalog.show(fm,"DijalogTag");

                        return true;
                }
                return false;
            }
        });


        mTitle = (TextView) findViewById(R.id.txtLijevo);
        mTitle.setText(R.string.app_name);



        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }

    private void posaljiKomandu(boolean b) {

        if(mCommandService != null){
            int state = mCommandService.getState();
            if(state == BTService.STATE_CONNECTED){

                if(b == true){
                    // otkljucaj ormar a
                    animacija(otkljucaj);
                    mCommandService.write(kodOtkljucavanja);

                }
                else {
                    //zakljucaj ormar A
                    animacija(zakljucaj);
                    mCommandService.write(kodZakljucavanja);
                }
            }
        }


    }

    private void konektujSeMetoda() {
        // uzmi adresu zadnjeg konektovatnog ako nema nista posalji na select device
        BluetoothDevice zadnji;
        provjeriMacAdresu();
        String MAC = getMacAdresu();
       if(MAC != null){
           zadnji = mBluetoothAdapter.getRemoteDevice(MAC);
           if (mCommandService != null){
               if (mCommandService.getState() != BTService.STATE_CONNECTED) {
                   mCommandService.connect(zadnji);
               }
           }
           else
               setupCommand();
       }





    }


    @Override
    protected void onStart() {

        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mCommandService == null)
                setupCommand();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCommandService != null) {
            if (mCommandService.getState() == BTService.STATE_NONE) {
                mCommandService.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCommandService != null) {
            mCommandService.stop();
        }
    }

    private void setupCommand() {
        mCommandService = new BTService(this, mHandler);
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BTService.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(mConnectedDeviceName);
                            break;
                        case BTService.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BTService.STATE_LISTEN:
                        case BTService.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:

                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(pretraga.EXTRA_DEVICE_ADDRESS);
                    //String address = "48:50:73:79:FB:BF";
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mCommandService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:

                if (resultCode == Activity.RESULT_OK) {
                    setupCommand();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

//



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hamburger.syncState();
    }

    private void animacija(ImageButton b) {
        final AlphaAnimation blinkanimation=   new AlphaAnimation(1, 0);
        blinkanimation.setDuration(300); // duration - half a second
        LinearInterpolator li = new LinearInterpolator();
        blinkanimation.setInterpolator(li); // do not alter animation rate
        blinkanimation.setRepeatCount(3);
        blinkanimation.setRepeatMode(Animation.REVERSE);
        b.startAnimation(blinkanimation);
    }

    @Override
    public void onDialogMessage(String unlock, String lock) {


        Toast.makeText(this,"Commands are saved.", Toast.LENGTH_LONG).show();

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("unlockk", unlock);
        editor.putString("lockk", lock);
        editor.apply();

        posaljiKomande(unlock, lock);

    }

    private void posaljiKomande(String unlock, String lock) {
        // uzima 2 proslijedjene komande i sprema ih u sp
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("unlockk", unlock);
        editor.putString("lockk", lock);
        editor.apply();
    }

    private void provjeriKomande() {
        // uzima komande, ako su null postavlja defaultne
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredUnlock = prefs.getString("unlockk", null);
        String restoredLock = prefs.getString("lockk", null);

        if (restoredUnlock == null || restoredLock== null) {

            posaljiKomande("a","A");
            String name = prefs.getString("name", "No name defined");//"No name defined" is the default value.
        }

        kodZakljucavanja = restoredLock;
        kodOtkljucavanja = restoredUnlock;
        //Toast.makeText(this,"MAIN: Komande iz SP su: " + restoredUnlock + "//" + restoredLock, Toast.LENGTH_LONG).show();


    }

    private void provjeriMacAdresu(){
        SharedPreferences prefs = getSharedPreferences("mac", MODE_PRIVATE);
        String restoredMac = prefs.getString("adresa", null);

        if (restoredMac == null) {

            Intent getAdresa = new Intent(this, pretraga.class);
            startActivityForResult(getAdresa, REQUEST_CONNECT_DEVICE);
        }


    }
    private String getMacAdresu(){
        SharedPreferences prefs = getSharedPreferences("mac", MODE_PRIVATE);
        String restoredMac = prefs.getString("adresa", null);

        if (restoredMac != null) {

            return restoredMac;
        }
        else
            return null;
    }

//

}
