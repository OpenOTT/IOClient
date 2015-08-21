package org.openott.ioclient.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.openott.ioclient.R;
import org.openott.ioclient.adapters.LegendAdapter;
import org.openott.ioclient.adapters.PinAdapter;
import com.futarque.mediarite.io.IGpioPin;
import com.futarque.mediarite.io.IGpioProvider;
import com.futarque.mediarite.io.IIOController;
import com.futarque.mediarite.io.PinAlternateMode;
import com.futarque.mediarite.io.PinCapabilities;
import com.futarque.mediarite.io.PinMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GPIOFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GPIOFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GPIOFragment extends BaseFragment implements PinAdapter.OnUpdateListener {
    private static final String TAG = GPIOFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private GridView mPinConnectorView_1;
    private GridView mPinConnectorView_2;
    private PinAdapter mPinAdapter_1;
    private PinAdapter mPinAdapter_2;
    private LegendAdapter mLegendAdapter;
    private GridView mLegendView;
    private IIOController mIOController;
    private Handler mUpdateHandler = new Handler();
    private Runnable mUpdateRunnable = new Runnable()
    {
        public void run()
        {
            mPinAdapter_1.update();
            mPinAdapter_2.update();
            mUpdateHandler.postDelayed(this, 1000);
        }
    };

    private List<PinAdapter.PinItem> mPinList = new ArrayList<>();

    public static GPIOFragment newInstance(String param1, String param2) {
        GPIOFragment fragment = new GPIOFragment();
        return fragment;
    }

    public GPIOFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_gpio, container, false);
        mPinConnectorView_1 = (GridView)v.findViewById(R.id.gpio_connector_1);
        mPinConnectorView_2 = (GridView)v.findViewById(R.id.gpio_connector_2);
        mLegendView = (GridView)v.findViewById(R.id.gpio_legend_grid);

        try {
            mIOController = getMediaRite().getIOController();

            openPins();
            loadListView();
            mUpdateHandler.postDelayed(mUpdateRunnable, 1000);
        } catch(RemoteException e) {
            Log.e(TAG,"Error creating IOController",e);
        }
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUpdateHandler.removeCallbacks(mUpdateRunnable);
        closePins();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            mUpdateHandler.removeCallbacks(mUpdateRunnable);
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (!visible) {
            mUpdateHandler.removeCallbacks(mUpdateRunnable);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void notifyDataSetChanged() {
        /*mPinAdapter_1.notifyDataSetChanged();
        mPinAdapter_2.notifyDataSetChanged(); */
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
    }

    private void openPins() {
        try {
            IGpioProvider provider = mIOController.getGpioProvider();
            // HACK Add two dummy elements
            // Elements added because position 0 in a gridview doesn't work correctly
            mPinList.add(new PinAdapter.PinItem("",null, null));
            mPinList.add(new PinAdapter.PinItem("",null, null));
            for (int pin_num = 1; pin_num <= 40; pin_num++) {
                if(pin_num==21) {
                    // HACK Add two dummy elements
                    mPinList.add(new PinAdapter.PinItem("",null, null));
                    mPinList.add(new PinAdapter.PinItem("",null, null));
                }
                IGpioPin pin = null;
                PinCapabilities caps = null;
                if(provider.hasPinByNumber(pin_num)) {
                    pin = provider.openPinNumber(pin_num);
                    if(pin!=null) {
                        caps = pin.getPinCapabilities();
                    }
                }
                mPinList.add(new PinAdapter.PinItem(provider.getPinLabel(pin_num),pin, caps));
            }
        } catch(RemoteException e) {
            Log.e(TAG,"Error getting IOProvider",e);
        }
    }


    private void closePins() {
        try {
            IGpioProvider provider = mIOController.getGpioProvider();
            for (PinAdapter.PinItem entry : mPinList) {
                if(entry.mGPIOPin != null) {
                    provider.closePin(entry.mGPIOPin);
                }
            }
            mPinList.clear();
        } catch (RemoteException e) {
            Log.e(TAG,"Error closing pins",e);
        }
    }



    private void loadListView(){
        // HACK contains header elements which are handled by the PinAdapter
        mPinAdapter_1 = new PinAdapter(getActivity(), mPinList,0,22,-2);
        mPinAdapter_2 = new PinAdapter(getActivity(), mPinList,22,22,-4);
        mPinAdapter_1.setOnUpdateListener(this);
        mPinAdapter_2.setOnUpdateListener(this);
        mPinConnectorView_1.setAdapter(mPinAdapter_1);
        mPinConnectorView_2.setAdapter(mPinAdapter_2);

        // Build active alternate modes list for the legend display
        boolean pwm_mode_found = false;
        boolean adc_mode_found = false;
        Map<PinAlternateMode,Boolean> activeAltMode = new HashMap<>();
        for(PinAdapter.PinItem i : mPinList) {
            if(i.mCaps!=null) {
                for (PinAlternateMode a : i.mCaps.mAltCaps.mSupportedModes) {
                    if (!activeAltMode.containsKey(a)) {
                        activeAltMode.put(a, true);
                    }
                }
                for(PinMode m: i.mCaps.mSupportedModes) {
                    if(m == PinMode.FPM_PWM) {
                        pwm_mode_found = true;
                    } else if(m == PinMode.FPM_ADC) {
                        adc_mode_found = true;
                    }
                }
            } else {
                if(!activeAltMode.containsKey(PinAlternateMode.FPAM_INVALID)) {
                    activeAltMode.put(PinAlternateMode.FPAM_INVALID,true);
                }
            }
        }

        ArrayList<LegendAdapter.LegendItem> list = new ArrayList<>();
        for(PinAlternateMode m : PinAlternateMode.values()) {
            if(activeAltMode.containsKey(m)) {
                list.add(new LegendAdapter.LegendItem(m));
            }
        }
        if(pwm_mode_found) {
            list.add(new LegendAdapter.LegendItem(PinMode.FPM_PWM));
        }
        if(adc_mode_found) {
            list.add(new LegendAdapter.LegendItem(PinMode.FPM_ADC));
        }
        mLegendAdapter = new LegendAdapter(getActivity(),list);
        mLegendView.setAdapter(mLegendAdapter);
    }
}
