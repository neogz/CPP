package nedo.bt1;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import nedo.bt1.adapter.TaboviAdapter;
import nedo.bt1.fragments.fragment_paired;
import nedo.bt1.fragments.fragment_unpaired;

public class pretraga extends AppCompatActivity {

    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    private ViewPager vp;
    private TaboviAdapter adapter;
    private TabLayout tabLayout;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";


    ///////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pretraga);

        initialize();

        prepareDataResoursce();
        adapter = new TaboviAdapter(getSupportFragmentManager(), fragmentList, titleList);
        vp.setAdapter(adapter);
        tabLayout.setupWithViewPager(vp);


        ////////////////////////////////////////////////////////////


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private void initialize() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbartext);
        toolbar.setTitle("SEARCH FOR DEVICES ");
        toolbar.setTitleTextColor(Color.WHITE);
//

        vp = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabovi);
    }

    private void prepareDataResoursce() {

        addData(new fragment_paired(), "PAIRED DEVICES");
        addData(new fragment_unpaired(), "UNPAIRED DEVICES");

    }

    private void addData(Fragment fragment, String naslov) {
        fragmentList.add(fragment);
        titleList.add(naslov);
    }
    /////////////////////////////////////////////////////////////////
}
