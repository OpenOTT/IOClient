package org.openott.ioclient.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.openott.ioclient.R;
import org.openott.ioclient.SettingsActivity;
import com.futarque.mediarite.common.FutarqueDouble;
import com.futarque.mediarite.io.AdcConfig;
import com.futarque.mediarite.io.AlternateConfig;
import com.futarque.mediarite.io.IGpioPin;
import com.futarque.mediarite.io.IOError;
import com.futarque.mediarite.io.PinAlternateMode;
import com.futarque.mediarite.io.PinCapabilities;
import com.futarque.mediarite.io.PinMode;
import com.futarque.mediarite.io.PinState;
import com.futarque.mediarite.io.PioConfig;
import com.futarque.mediarite.io.PwmConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PinAdapter extends BaseAdapter {
    private static final String TAG = PinAdapter.class.getSimpleName();
    private Context mContext;
    private int mStart;
    private int mCount;
    private List<PinItem> mList;
    private OnUpdateListener mListener;
    private Lock mUpdateLock = new ReentrantLock();
    private Map<Integer,ViewHolderItem> mViewHolderMap = new HashMap<>();
    private Map<String,ArrayAdapter<PinMode>> mModeSpinnerAdapterMap = new HashMap<>();
    private Map<String,ArrayAdapter<PinAlternateMode>> mAlternateModeAdapterMap = new HashMap<>();
    private int mDisplayIdOffset;

    public static class PinItem {
        public PinItem(String label, IGpioPin pin, PinCapabilities caps) {
            mLabel = label;
            mGPIOPin = pin;
            mCaps = caps;
            mDirty = true;
        }

        public boolean mDirty;
        public IGpioPin mGPIOPin;
        public PinCapabilities mCaps;
        public String mLabel;

/*        @Override
        public String toString() {
            return "PinItem(mLabel="+mLabel+", mGPIOPin="+mGPIOPin;
        } */
    }

    private void markAllDirty() {
        Log.d(TAG,"Mark all dirty");
        for(PinItem entry : mList) {
            entry.mDirty = true;
        }
    }

    public interface OnUpdateListener {
        void notifyDataSetChanged();
    }

    public void setOnUpdateListener(OnUpdateListener listener) {
        mListener = listener;
    }


    protected static class ViewHolderItem {
        public boolean mLeftLayout;
        public TextView mPinLabelText;
        public TextView mPinNumberText;
        public View mPinNumberBgView;
        public View mLayoutView;
        public TextView mValueText;
        public Switch mStateSwitch;
        public Spinner mModeSpinner;
        public Spinner mAltModeSpinner;
        public SeekBar mPwmDutySeekbar;
        public PinMode mCurrentMode;
    }


    public PinAdapter(Context context, List<PinItem> list, int start, int count, int display_id_offset) {
        mContext = context;
        mStart = start;
        mCount = count;
        mList = list;
        mDisplayIdOffset = display_id_offset;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem vh;
        final PinItem entry = mList.get(position+mStart);
        boolean left_layout = (position%2==0);
        boolean change_layout = false;
        //Log.d(TAG,"pos="+(position+mStart)+" entry="+entry.toString());

        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_pin, parent, false);
            vh = mViewHolderMap.get(position);
            if(vh==null) {
                vh = new ViewHolderItem();
                vh.mCurrentMode = PinMode.FPM_NUMBER_OF; // Only unused mode
                vh.mLeftLayout = left_layout;
                change_layout = true;

                // Find Views
                vh.mPinLabelText = (TextView) convertView.findViewById(R.id.pin_label_text);
                vh.mPinNumberText = (TextView) convertView.findViewById(R.id.pin_num_text);
                vh.mPinNumberBgView = convertView.findViewById(R.id.pin_num_background);
                vh.mLayoutView = convertView.findViewById(R.id.data_layout);
                vh.mValueText = (TextView) convertView.findViewById(R.id.pin_value_text);
                vh.mStateSwitch = (Switch) convertView.findViewById(R.id.ping_state_toggle);
                vh.mAltModeSpinner = (Spinner) convertView.findViewById(R.id.pin_alt_mode_spinner);
                vh.mModeSpinner = (Spinner) convertView.findViewById(R.id.pin_mode_spinner);
                vh.mPwmDutySeekbar = (SeekBar) convertView.findViewById(R.id.pin_pwm_seekbar);

                // Set Adapters
                if (entry.mCaps != null) {
                    vh.mModeSpinner.setAdapter(getModeSpinnerAdapter(entry.mCaps));
                    vh.mAltModeSpinner.setAdapter(getAltModeSpinnerAdapter(entry.mCaps));
                }


                // store the holder with the view.
                //convertView.setTag(vh);
                mViewHolderMap.put(position, vh);
                // Set Listeners
                addListeners(vh, entry);
            }
        } else {
            vh = mViewHolderMap.get(position);
            if(vh!=null && vh.mLeftLayout!=left_layout) {
                change_layout = true;
            }
        }
        if(vh==null) { // Avoid null pointer errors
            return convertView;
        }
        if(change_layout) {
            setLayoutDirection(vh, left_layout);
        }

        vh.mPinLabelText.setText(entry.mLabel);
        if(position<=1) {
            /* HACK Spinner event listener wasn't firing correctly on position 0 views - hence and extra "empty" header */
            vh.mPinNumberBgView.setVisibility(View.GONE);
            vh.mPinNumberText.setVisibility(View.GONE);
            convertView.getLayoutParams().height=5;
            convertView.setLayoutParams(convertView.getLayoutParams());
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.standard_background));
        } else {
            vh.mPinNumberBgView.setVisibility(View.VISIBLE);
            vh.mPinNumberText.setVisibility(View.VISIBLE);
            vh.mPinNumberText.setText(Integer.toString(position + mStart + 1 + mDisplayIdOffset));
        }

        if(entry.mGPIOPin==null) {
            setModeVisibility(PinMode.FPM_INVALID, convertView, vh);
        } else {
            setCellBackgroundColor(entry, convertView);

            try {
                if(!entry.mDirty) {
                    return convertView;
                }
                PinMode hw_mode = entry.mGPIOPin.getMode();
                setModeVisibility(hw_mode, convertView, vh);
                setModeContext(hw_mode,vh,entry);
                if(hw_mode==PinMode.FPM_IN || hw_mode==PinMode.FPM_ADC) {
                    entry.mDirty = true;
                } else {
                    entry.mDirty = false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error", e);
            }
        }

        return convertView;
    }


    private int getModeSpinnerPosition(PinMode mode, PinCapabilities caps) {
        int pos = 0;
        for(PinMode m: caps.mSupportedModes) {
            if(m==mode) {
                break;
            }
            pos++;
        }
        return pos;
    }

    private int getAltModeSpinnerPosition(PinAlternateMode mode, PinCapabilities caps) {
        int pos = 0;
        for(PinAlternateMode m: caps.mAltCaps.mSupportedModes) {
            if(m==mode) {
                break;
            }
            pos++;
        }
        return pos;
    }


    private ArrayAdapter<PinMode> getModeSpinnerAdapter(PinCapabilities caps) {
        ArrayAdapter<PinMode> adapter = mModeSpinnerAdapterMap.get(caps.mSupportedModes.toString());
        if(adapter==null) {
            adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item);
            for(PinMode m : caps.mSupportedModes) {
                adapter.add(m);
            }
            mModeSpinnerAdapterMap.put(caps.mSupportedModes.toString(),adapter);
        }
        return adapter;
    }

    private ArrayAdapter<PinAlternateMode> getAltModeSpinnerAdapter(PinCapabilities caps) {
        ArrayAdapter<PinAlternateMode> adapter = mAlternateModeAdapterMap.get(caps.mAltCaps.toString());
        if(adapter==null) {
            adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item);
            for (PinAlternateMode m : caps.mAltCaps.mSupportedModes) {
                adapter.add(m);
            }
            mAlternateModeAdapterMap.put(caps.mAltCaps.toString(),adapter);
        }
        return adapter;
    }

    private void setSpinnerSelectionWithoutCallingListener(final Spinner spinner, final int selection) {
        final AdapterView.OnItemSelectedListener l = spinner.getOnItemSelectedListener();
        spinner.setOnItemSelectedListener(null);
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(selection);
                spinner.post(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setOnItemSelectedListener(l);
                    }
                });
            }
        });
    }

    private void setModeContext(PinMode mode, ViewHolderItem vh, PinItem entry) {
        switch (mode) {
            case FPM_INVALID:
                break;
            case FPM_OUT:
            case FPM_IN: {
                int pos = getModeSpinnerPosition(mode, entry.mCaps);
                if(pos!=vh.mModeSpinner.getSelectedItemPosition()) {
                    vh.mModeSpinner.setSelection(pos);
                }
                try {
                    PinState state = entry.mGPIOPin.getStateIgnoreError();
                    boolean is_checked = vh.mStateSwitch.isChecked();
                    if (state == PinState.HIGH && !is_checked) {
                        vh.mStateSwitch.setChecked(true);
                    } else if(state == PinState.LOW && is_checked){
                        vh.mStateSwitch.setChecked(false);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error getting state", e);
                }
                break;
            }
            case FPM_ALT: {
                int pos = getModeSpinnerPosition(mode, entry.mCaps);
                if(pos!=vh.mModeSpinner.getSelectedItemPosition()) {
                    vh.mModeSpinner.setSelection(pos);
                }
                try {
                    AlternateConfig alt_config = new AlternateConfig();
                    if (entry.mGPIOPin.getAlternateConfig(alt_config) == IOError.IOE_NOERROR) {
                        vh.mAltModeSpinner.setSelection(getAltModeSpinnerPosition(alt_config.mMode, entry.mCaps),false);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error getting alternate config", e);
                }
                break;
            }
            case FPM_ADC: {
                int pos = getModeSpinnerPosition(mode, entry.mCaps);
                if(pos!=vh.mModeSpinner.getSelectedItemPosition()) {
                    vh.mModeSpinner.setSelection(pos);
                }
                try {
                    FutarqueDouble value = new FutarqueDouble();
                    if (entry.mGPIOPin.getAdcValue(value) == IOError.IOE_NOERROR) {
                        AdcConfig config = new AdcConfig();
                        entry.mGPIOPin.getAdcConfig(config);
                        double voltage = value.mValue * config.mAdcRefLevelmV / 1000;
                        vh.mValueText.setText(String.format("%.3fV", voltage));
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error getting ADC value", e);
                }
                break;
            }
            case FPM_PWM: {
                int pos = getModeSpinnerPosition(mode, entry.mCaps);
                if(pos!=vh.mModeSpinner.getSelectedItemPosition()) {
                    vh.mModeSpinner.setSelection(pos);
                }
                break;
            }
        }

    }

    private void setModeVisibility(PinMode mode, View convertView, ViewHolderItem vh) {
        if(vh.mCurrentMode==mode) {
            return;
        }
        switch(mode) {
            case FPM_INVALID:
                vh.mPinNumberBgView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_gnd_foreground));
                vh.mPinNumberText.setTextColor(mContext.getResources().getColor(R.color.pin_gnd_text));
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_gnd_background));
                vh.mStateSwitch.setVisibility(View.GONE);
                vh.mValueText.setVisibility(View.GONE);
                vh.mModeSpinner.setVisibility(View.GONE);
                vh.mAltModeSpinner.setVisibility(View.GONE);
                vh.mPwmDutySeekbar.setVisibility(View.GONE);
                break;
            case FPM_IN:
            case FPM_OUT:
                //Log.d(TAG,"IN/OUT mode="+mode);
                vh.mPinNumberBgView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_gpio_foreground));
                vh.mPinNumberText.setTextColor(mContext.getResources().getColor(R.color.pin_text));
                vh.mStateSwitch.setEnabled(mode==PinMode.FPM_OUT);
                vh.mStateSwitch.setVisibility(View.VISIBLE);
                vh.mValueText.setVisibility(View.GONE);
                vh.mAltModeSpinner.setVisibility(View.GONE);
                vh.mModeSpinner.setVisibility(View.VISIBLE);
                vh.mPwmDutySeekbar.setVisibility(View.GONE);
                break;
            case FPM_ALT:
                vh.mPinNumberBgView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_foreground));
                vh.mPinNumberText.setTextColor(mContext.getResources().getColor(R.color.pin_text));
                vh.mStateSwitch.setVisibility(View.GONE);
                vh.mValueText.setVisibility(View.GONE);
                vh.mModeSpinner.setVisibility(View.VISIBLE);
                vh.mAltModeSpinner.setVisibility(View.VISIBLE);
                vh.mPwmDutySeekbar.setVisibility(View.GONE);
                break;
            case FPM_ADC:
                vh.mPinNumberBgView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_adc_foreground));
                vh.mPinNumberText.setTextColor(mContext.getResources().getColor(R.color.pin_text));
                vh.mValueText.setVisibility(View.VISIBLE);
                vh.mPwmDutySeekbar.setVisibility(View.GONE);
                vh.mStateSwitch.setVisibility(View.GONE);
                vh.mModeSpinner.setVisibility(View.VISIBLE);
                vh.mAltModeSpinner.setVisibility(View.GONE);
                break;
            case FPM_PWM:
                vh.mPinNumberBgView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_pwm_foreground));
                vh.mPinNumberText.setTextColor(mContext.getResources().getColor(R.color.pin_text));
                vh.mValueText.setVisibility(View.GONE);
                vh.mPwmDutySeekbar.setVisibility(View.VISIBLE);
                vh.mStateSwitch.setVisibility(View.GONE);
                vh.mModeSpinner.setVisibility(View.VISIBLE);
                vh.mAltModeSpinner.setVisibility(View.GONE);
                break;
        }
        vh.mCurrentMode=mode;
    }

    private void addListeners(final ViewHolderItem vh, final PinItem entry) {
        // Mode Spinner
        if (/*vh.mModeSpinner.getOnItemSelectedListener() == null*/true) {
            if(entry.mLabel.equals("RP_GPIO9")) {
                Log.d(TAG,"Adding Listener");
            }

            vh.mModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private int mLastSpinnerPosition = 0;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "selected pos=" + position);
                    mUpdateLock.lock();
                    try {

                        /*if(mLastSpinnerPosition == position){
                            entry.mDirty=true;
                            return; //do nothing
                        }*/
                        mLastSpinnerPosition = position;
                        boolean modified = false;
                        int pos = 0;
                        for (PinMode new_mode : entry.mCaps.mSupportedModes) {
                            if (position == pos) {
                                PinMode old_mode = entry.mGPIOPin.getMode();
                                Log.d(TAG,"old_mode="+old_mode+" new_mode="+new_mode);
                                if (new_mode != old_mode) {
                                    if (new_mode == PinMode.FPM_OUT || new_mode == PinMode.FPM_IN) {
                                        PioConfig cfg = new PioConfig();
                                        cfg.mIsOutput = (new_mode == PinMode.FPM_OUT);
                                        Log.d(TAG, "SetPioConfig output=" + cfg.mIsOutput);
                                        entry.mGPIOPin.setPioConfig(cfg);
                                        if (old_mode == PinMode.FPM_ALT) {
                                            markAllDirty();
                                        } else {
                                            Log.d(TAG, "Set Dirty " + entry.mLabel);
                                            entry.mDirty = true;
                                        }
                                        modified = true;
                                    } else if (new_mode == PinMode.FPM_ALT) {
                                        if (entry.mCaps.mAltCaps.mSupportedModes.size() > 0) {
                                            AlternateConfig cfg = new AlternateConfig();
                                            cfg.mMode = entry.mCaps.mAltCaps.mSupportedModes.iterator().next();
                                            entry.mGPIOPin.setAlternateConfig(cfg);
                                            Log.d(TAG, "SetAlternateConfig mode=" + cfg.mMode);
                                            markAllDirty();
                                            modified = true;
                                        }
                                    } else if (new_mode == PinMode.FPM_ADC) {
                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                                        String ref_level = prefs.getString(SettingsActivity.KEY_ADC_REF_LVL_MV, SettingsActivity.DEFAULT_ADC_REF_LVL_MV);
                                        AdcConfig config = new AdcConfig();
                                        config.mAdcRefLevelmV = Integer.parseInt(ref_level);
                                        entry.mGPIOPin.setAdcConfig(config);
                                        Log.d(TAG, "SetAdcConfig");
                                        if (old_mode == PinMode.FPM_ALT) {
                                            markAllDirty();
                                        } else {
                                            Log.d(TAG, "Set Dirty " + entry.mLabel);
                                            entry.mDirty = true;
                                        }
                                    } else if (new_mode == PinMode.FPM_PWM) {
                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                                        String period = prefs.getString(SettingsActivity.KEY_PWM_PERIOD_NS, SettingsActivity.DEFAULT_PWM_PERIOD_NS);
                                        PwmConfig config = new PwmConfig();
                                        config.mPeriodNs = Integer.parseInt(period);
                                        config.mDutyPpm = vh.mPwmDutySeekbar.getProgress() * 10000;
                                        entry.mGPIOPin.setPwmConfig(config);
                                        Log.d(TAG, "SetPwmConfig");
                                        if (old_mode == PinMode.FPM_ALT) {
                                            markAllDirty();
                                        } else {
                                            Log.d(TAG, "Set Dirty " + entry.mLabel);
                                            entry.mDirty = true;
                                        }
                                    }
                                }
                                if (modified) {
                                    if (mListener != null) {
                                        mListener.notifyDataSetChanged();
                                    }
                                }
                                break;
                            }
                            pos++;
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error setting config", e);
                    } finally {
                        mUpdateLock.unlock();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "nothing selected");
                }
            });
        }
        // GPIO State Switch
        vh.mStateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUpdateLock.lock();
                try {
                    PinMode hw_mode = entry.mGPIOPin.getMode();
                    PinState state = entry.mGPIOPin.getStateIgnoreError();
                    PinState input = isChecked ? PinState.HIGH : PinState.LOW;
                    Log.d(TAG,"Pin mode="+hw_mode+ " state old="+state+" new="+input);
                    if (hw_mode == PinMode.FPM_OUT && input != state) {
                        IOError err = entry.mGPIOPin.setState(input);
                        if(err!=IOError.IOE_NOERROR) {
                            Log.e(TAG,"Error setting pin state "+err);
                        }
                        entry.mDirty = true;
                        Log.d(TAG, "Mark dirty " + entry.mLabel);
                        if (mListener != null) {
                            mListener.notifyDataSetChanged();
                        }
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error setting state", e);
                } finally {
                    mUpdateLock.unlock();
                }
            }
        });

        // PWM Duty Seekbar
        vh.mPwmDutySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mUpdateLock.lock();
                try {
                    Log.d(TAG,"Duty "+progress);
                    entry.mGPIOPin.setPwmDuty(progress * 10000);
                } catch (RemoteException e) {
                    Log.e(TAG,"Error setting PWM Duty",e);
                } finally {
                    mUpdateLock.unlock();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setCellBackgroundColor(PinItem entry, View convertView) {
        if(entry.mCaps.mAltCaps.mSupportedModes.size()>0) {
            switch(entry.mCaps.mAltCaps.mSupportedModes.iterator().next()) {
                case FPAM_I2C_0:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2c0_background));
                    break;
                case FPAM_I2C_1:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2c1_background));
                    break;
                case FPAM_I2S_IN_0:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2sin0_background));
                    break;
                case FPAM_I2S_IN_1:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2sin1_background));
                    break;
                case FPAM_I2S_OUT_0:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2sout0_background));
                    break;
                case FPAM_I2S_OUT_1:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2sout1_background));
                    break;
                case FPAM_SMC_0:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_smc0_background));
                    break;
                case FPAM_SMC_1:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_smc1_background));
                    break;
                case FPAM_SPI_0:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_spi0_background));
                    break;
                case FPAM_SPI_1:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_spi1_background));
                    break;
                case FPAM_TS_0:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_ts0_background));
                    break;
                case FPAM_TS_1:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_ts1_background));
                    break;
                case FPAM_UART_0:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_uart0_background));
                    break;
                case FPAM_UART_1:
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_uart1_background));
                    break;
            }
        } else {
            if(entry.mCaps.mSupportedModes.contains(PinMode.FPM_PWM)) {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_pwm_background));
            } else if(entry.mCaps.mSupportedModes.contains(PinMode.FPM_ADC)) {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_adc_background));
            } else {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.pin_gpio_background));
            }
        }
    }

    private void setLayoutDirection(ViewHolderItem vh, boolean left_layout) {
        RelativeLayout.LayoutParams pin_bg_params = (RelativeLayout.LayoutParams)vh.mPinNumberBgView.getLayoutParams();
        RelativeLayout.LayoutParams label_params = (RelativeLayout.LayoutParams)vh.mPinLabelText.getLayoutParams();
        RelativeLayout.LayoutParams data_layout_params = (RelativeLayout.LayoutParams)vh.mLayoutView.getLayoutParams();
        RelativeLayout.LayoutParams mode_spinner_layout_params = (RelativeLayout.LayoutParams)vh.mModeSpinner.getLayoutParams();
        RelativeLayout.LayoutParams state_switch_layout_params = (RelativeLayout.LayoutParams)vh.mStateSwitch.getLayoutParams();
        RelativeLayout.LayoutParams value_text_layout_params = (RelativeLayout.LayoutParams)vh.mValueText.getLayoutParams();
        RelativeLayout.LayoutParams alt_spinner_layout_params = (RelativeLayout.LayoutParams)vh.mAltModeSpinner.getLayoutParams();
        RelativeLayout.LayoutParams duty_seek_layout_params = (RelativeLayout.LayoutParams)vh.mPwmDutySeekbar.getLayoutParams();
        if(left_layout) {
            pin_bg_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            pin_bg_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            label_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            label_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            data_layout_params.addRule(RelativeLayout.LEFT_OF,R.id.pin_num_background);
            data_layout_params.removeRule(RelativeLayout.RIGHT_OF);
            mode_spinner_layout_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            mode_spinner_layout_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            state_switch_layout_params.addRule(RelativeLayout.LEFT_OF,R.id.pin_mode_spinner);
            state_switch_layout_params.removeRule(RelativeLayout.RIGHT_OF);
            value_text_layout_params.addRule(RelativeLayout.LEFT_OF,R.id.pin_mode_spinner);
            value_text_layout_params.removeRule(RelativeLayout.RIGHT_OF);
            alt_spinner_layout_params.addRule(RelativeLayout.LEFT_OF,R.id.pin_mode_spinner);
            alt_spinner_layout_params.removeRule(RelativeLayout.RIGHT_OF);
            duty_seek_layout_params.addRule(RelativeLayout.LEFT_OF,R.id.pin_mode_spinner);
            duty_seek_layout_params.removeRule(RelativeLayout.RIGHT_OF);
        } else {
            pin_bg_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            pin_bg_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
            label_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            label_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
            data_layout_params.addRule(RelativeLayout.RIGHT_OF,R.id.pin_num_background);
            data_layout_params.removeRule(RelativeLayout.LEFT_OF);
            mode_spinner_layout_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            mode_spinner_layout_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
            state_switch_layout_params.addRule(RelativeLayout.RIGHT_OF,R.id.pin_mode_spinner);
            state_switch_layout_params.removeRule(RelativeLayout.LEFT_OF);
            value_text_layout_params.addRule(RelativeLayout.RIGHT_OF,R.id.pin_mode_spinner);
            value_text_layout_params.removeRule(RelativeLayout.LEFT_OF);
            alt_spinner_layout_params.addRule(RelativeLayout.RIGHT_OF,R.id.pin_mode_spinner);
            alt_spinner_layout_params.removeRule(RelativeLayout.LEFT_OF);
            duty_seek_layout_params.addRule(RelativeLayout.RIGHT_OF,R.id.pin_mode_spinner);
            duty_seek_layout_params.removeRule(RelativeLayout.LEFT_OF);
        }
    }

    public void update() {
        mUpdateLock.lock();
 //       Log.d(TAG,"Update... Lock");
        try {
            notifyDataSetChanged();
        } finally {
            mUpdateLock.unlock();
//            Log.d(TAG, "Update... Unlock");
        }
    }
}