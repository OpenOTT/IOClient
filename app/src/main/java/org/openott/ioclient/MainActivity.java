package org.openott.ioclient;

import java.util.List;
import java.util.Locale;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.openott.ioclient.fragments.GPIOFragment;
import org.openott.ioclient.fragments.I2CFragment;
import org.openott.ioclient.fragments.SPIFragment;
import org.openott.ioclient.fragments.UARTFragment;
import org.openott.ioclient.interfaces.IMediaRiteActivity;
import com.futarque.mediarite.IMediaRite;

public class MainActivity extends AppCompatActivity implements ActionBar.TabListener,
        GPIOFragment.OnFragmentInteractionListener,
        I2CFragment.OnFragmentInteractionListener,
        SPIFragment.OnFragmentInteractionListener,
        UARTFragment.OnFragmentInteractionListener,
        IMediaRiteActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private IMediaRite mIMediaRite = null;

    private void initialize() {
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount()-1);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Service Connection Established");
            mIMediaRite = IMediaRite.Stub.asInterface(service);
            initialize();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Service Disconnected");
            mIMediaRite = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if ((this.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            Log.d(TAG, "This is a system application");
        } else {
            Log.d(TAG, "This is not a system application");
        }

        if ((this.getApplicationInfo().flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            Log.d(TAG, "This is an updated system application");
        }
/*        if ((this.getApplicationInfo().flags & ApplicationInfo.FLAG_PRIVILEGED) != 0) {
            Log.d(TAG, "This is a privileged system application");
        } */

        Intent implicitIntent = new Intent("com.futarque.mediarite.IOService");
        List<ResolveInfo> resolveInfo = getPackageManager().queryIntentServices(implicitIntent, 0);

        if (resolveInfo == null) {
            Log.e(TAG, "No Mediarite service found");
            return;
        }

        if (resolveInfo.size() != 1) {
            Log.e(TAG, "More than one Mediarite service("+resolveInfo.size()+")!?");
            Log.e(TAG, "More than one Mediarite service("+resolveInfo.size()+")!?");
            Log.e(TAG, "More than one Mediarite service("+resolveInfo.size()+")!?");
            Log.e(TAG, "More than one Mediarite service("+resolveInfo.size()+")!?");
            Log.e(TAG, "More than one Mediarite service("+resolveInfo.size()+")!?");
            Log.e(TAG, "More than one Mediarite service("+resolveInfo.size()+")!?");
            Log.e(TAG, "More than one Mediarite service("+resolveInfo.size()+")!?");
            for(ResolveInfo info : resolveInfo) {
                Log.e(TAG, "resname: " + info.resolvePackageName);
                Log.e(TAG, "pkgname: " + info.serviceInfo.packageName);
                Log.e(TAG, "process: " + info.serviceInfo.processName);
                Log.e(TAG, "clsname: " + info.serviceInfo.applicationInfo.className);
            }
            return;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        startService(explicitIntent);
        bindService(explicitIntent, mServiceConnection, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mServiceConnection!=null) {
            unbindService(mServiceConnection);
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public IMediaRite getMediaRite() {
        return mIMediaRite;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private GPIOFragment mGpio;
        private I2CFragment mI2C;
        private SPIFragment mSPI;
        private UARTFragment mUart;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG,"GetItem="+position);
            switch(position) {
                case 0:
                    if(mGpio==null) {
                        mGpio = new GPIOFragment();
                    }
                    return mGpio;
                case 1:
                    if(mI2C==null) {
                        mI2C = new I2CFragment();
                    }
                    return mI2C;
                case 2:
                    if(mSPI==null) {
                        mSPI = new SPIFragment();
                    }
                    return mSPI;
                case 3:
                    if(mUart==null) {
                        mUart = new UARTFragment();
                    }
                    return mUart;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_gpio).toUpperCase(l);
                case 1:
                    return getString(R.string.title_i2c).toUpperCase(l);
                case 2:
                    return getString(R.string.title_spi).toUpperCase(l);
                case 3:
                    return getString(R.string.title_uart).toUpperCase(l);
            }
            return null;
        }
    }
}
