package org.openott.ioclient.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openott.ioclient.R;
import org.openott.ioclient.parsers.BaseParser;
import org.openott.ioclient.parsers.SPIParser;
import org.openott.ioclient.parsers.SerialParser;
import com.futarque.mediarite.common.FutarqueByte;
import com.futarque.mediarite.common.FutarqueByteArray;
import com.futarque.mediarite.common.FutarqueString;
import com.futarque.mediarite.io.IOError;
import com.futarque.mediarite.io.ISpiChannel;
import com.futarque.mediarite.io.ISpiDevice;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;

public class SPIFragment extends BaseIOFragment {
    private OnFragmentInteractionListener mListener;
    private ISpiChannel mChannel;
    private ISpiDevice mDevice;

    public SPIFragment() {
        // Required empty public constructor
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    protected BaseParser getParserInstance(String cmd) {
        return new SPIParser(cmd);
    }

    private boolean closeDevice() {
        boolean success = true;
        try {
            if(mChannel!=null) {
                if (mDevice != null) {
                    mChannel.closeDevice(mDevice);
                    mDevice = null;
                }
                mChannel = null;
            }
        } catch (RemoteException e) {
            Log.e(TAG,"Error closing", e);
        }
        return success;
    }

    @Override
    protected boolean executeCommand(BaseParser parser) {
        SPIParser p = (SPIParser) parser;
        List<String> arguments = p.getArguments();
        boolean success = false;
        Log.d(TAG, "Execute Command " + p.getCommand());
        switch(p.getCommand()) {
            case HELP:
                mTextView.append(new SPIParser("").getUsage());
                success = true;
                break;
            case OPEN: {
                try {
                    success = closeDevice();
                    if(success) {
                        success = false;
                        int channel= Integer.parseInt(arguments.get(0));
                        mChannel = getMediaRite().getIOController().getSpiChannel(channel);
                        if(mChannel!=null) {
                            int deviceIdx = Integer.parseInt(arguments.get(1));
                            mDevice = mChannel.openDevice(deviceIdx);
                            if(mDevice!=null) {
                                success = true;
                            }
                        }
                    }
                } catch(RemoteException | NumberFormatException e) {
                    Log.e(TAG,"Error opening", e);
                }
                break;
            }
            case CLOSE:
                success = closeDevice();
                break;
            case WRITE_STRING:
                if(mDevice!=null) {
                    try {
                        String msg = arguments.get(0);
                        FutarqueString out = new FutarqueString();
                        IOError err = mDevice.writeString(msg,out);
                        if(err==IOError.IOE_NOERROR){
                            success = true;
                            if(!out.mValue.isEmpty()) {
                                mTextView.append(out.mValue);
                                mTextView.append("\n");
                            }
                        } else {
                            mTextView.append(err.toString()+"\n");
                        }
                    } catch(RemoteException e) {
                        Log.e(TAG,"Error writing buffer",e);
                    }
                }
                break;
            case WRITE_BUFFER:
                if(mDevice!=null) {
                    try {
                        byte[] b = new byte[arguments.size()];
                        for(int idx = 0; idx < b.length; idx++) {
                            b[idx] = (byte) (Integer.parseInt(arguments.get(idx),16) & 0xff);
                        }
                        FutarqueByteArray inBuf = new FutarqueByteArray(b);
                        FutarqueByteArray outBuf = new FutarqueByteArray();
                        IOError err = mDevice.write(inBuf,outBuf);
                        if(err==IOError.IOE_NOERROR) {
                            success = true;
                            for (int i = 0;i<outBuf.limit();i++) {
                                mTextView.append(String.format("%X",outBuf.get(i)));
                            }
                            mTextView.append("\n");
                        } else {
                            mTextView.append(err.toString() + "\n");
                        }

                    } catch(RemoteException e) {
                        Log.e(TAG,"Error writing buffer",e);
                    }
                }
                break;
        }
        return success;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name

    }

}
