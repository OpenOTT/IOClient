package org.openott.ioclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.openott.ioclient.R;
import com.futarque.mediarite.io.PinAlternateMode;
import com.futarque.mediarite.io.PinMode;

import java.util.List;

public class LegendAdapter extends BaseAdapter {
    private Context mContext;
    private List<LegendItem> mList;

    public static class LegendItem {
        public PinAlternateMode mAlternateMode;
        public PinMode mMode;

        public LegendItem(PinAlternateMode mode) {
            mAlternateMode = mode;
            mMode = null;
        }

        public LegendItem(PinMode mode) {
            mAlternateMode = null;
            mMode = mode;
        }
    }

    public LegendAdapter(Context context, List<LegendItem> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_legend, parent, false);
        }
        View bg = convertView.findViewById(R.id.legend_bg);
        TextView text = (TextView)convertView.findViewById(R.id.legend_text);

        LegendItem entry = mList.get(position);
        if(entry.mAlternateMode!=null) {
            switch (entry.mAlternateMode) {
                case FPAM_INVALID:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_gnd_background));
                    text.setText(R.string.legend_not_available);
                    break;
                case FPAM_TS_0:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_ts0_background));
                    text.setText(R.string.legend_ts0);
                    break;
                case FPAM_TS_1:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_ts1_background));
                    text.setText(R.string.legend_ts1);
                    break;
                case FPAM_SMC_0:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_smc0_background));
                    text.setText(R.string.legend_smc0);
                    break;
                case FPAM_SMC_1:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_smc1_background));
                    text.setText(R.string.legend_smc1);
                    break;
                case FPAM_UART_0:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_uart0_background));
                    text.setText(R.string.legend_uart0);
                    break;
                case FPAM_UART_1:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_uart1_background));
                    text.setText(R.string.legend_uart1);
                    break;
                case FPAM_SPI_0:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_spi0_background));
                    text.setText(R.string.legend_spi0);
                    break;
                case FPAM_SPI_1:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_spi1_background));
                    text.setText(R.string.legend_spi1);
                    break;
                case FPAM_I2C_0:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2c0_background));
                    text.setText(R.string.legend_i2c0);
                    break;
                case FPAM_I2C_1:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2c1_background));
                    text.setText(R.string.legend_i2c1);
                    break;
                case FPAM_I2S_IN_0:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2sin0_background));
                    text.setText(R.string.legend_i2sin0);
                    break;
                case FPAM_I2S_IN_1:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2sin1_background));
                    text.setText(R.string.legend_i2sin1);
                    break;
                case FPAM_I2S_OUT_0:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2sout0_background));
                    text.setText(R.string.legend_i2sout0);
                    break;
                case FPAM_I2S_OUT_1:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_i2sout1_background));
                    text.setText(R.string.legend_i2sout1);
                    break;
            }
        } else if(entry.mMode!=null) {
            switch(entry.mMode) {
                case FPM_PWM:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_pwm_background));
                    text.setText(R.string.legend_pwm);
                    break;
                case FPM_ADC:
                    bg.setBackgroundColor(mContext.getResources().getColor(R.color.pin_alt_adc_background));
                    text.setText(R.string.legend_adc);
                    break;
            }
        }

        return convertView;
    }
}
