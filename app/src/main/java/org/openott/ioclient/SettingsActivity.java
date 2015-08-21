package org.openott.ioclient;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    public static String KEY_PWM_PERIOD_NS = "pwm_period_ns";
    public static String DEFAULT_PWM_PERIOD_NS = "50000";

    public static String KEY_ADC_REF_LVL_MV = "adc_reflevel_mv";
    public static String DEFAULT_ADC_REF_LVL_MV = "3300";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
