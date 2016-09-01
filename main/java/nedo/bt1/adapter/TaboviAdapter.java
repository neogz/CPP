package nedo.bt1.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by fejzi on 21.08.2016..
 */
public class TaboviAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList;
    private List<String> nasloviLista;

    public TaboviAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> nasloviLista) {
        super(fm);
        this.fragmentList = fragmentList;
        this.nasloviLista = nasloviLista;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return nasloviLista.get(position);
    }
}
