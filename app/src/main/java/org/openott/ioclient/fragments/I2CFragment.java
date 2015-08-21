package org.openott.ioclient.fragments;

import android.app.Activity;
import android.os.RemoteException;
import android.util.Log;

import org.openott.ioclient.parsers.BaseParser;
import org.openott.ioclient.parsers.I2CParser;
import com.futarque.mediarite.common.FutarqueByte;
import com.futarque.mediarite.common.FutarqueByteArray;
import com.futarque.mediarite.io.II2CBus;
import com.futarque.mediarite.io.II2CDevice;
import com.futarque.mediarite.io.IOError;

import java.math.BigInteger;
import java.util.List;

public class I2CFragment extends BaseIOFragment {
    private static String TAG = I2CFragment.class.getSimpleName();
    private II2CBus mBus;
    private II2CDevice mDevice;
    private OnFragmentInteractionListener mListener;

    public I2CFragment() {
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


    private void handleSystemInput(String input) {
        mTextView.append("> "+input+"\n");
    }

    private boolean closeDevice() {
        boolean success = true;
        try {
            if(mBus!=null) {
                if (mDevice != null) {
                    mBus.closeDevice(mDevice);
                    mDevice = null;
                }
                mBus = null;
            }
        } catch (RemoteException e) {
            success = false;
            Log.e(TAG,"Error closing", e);
        }
        return success;
    }

    @Override
    protected BaseParser getParserInstance(String cmd) {
        return new I2CParser(cmd);
    }

    protected boolean executeCommand(BaseParser parser) {
        I2CParser p = (I2CParser)parser;
        List<String> arguments = p.getArguments();
        boolean success = false;
        Log.d(TAG, "Execute Command "+p.getCommand());
        switch(p.getCommand()) {
            case HELP:
                mTextView.append(p.getUsage());
                success = true;
                break;
            case OPEN: {
                try {
                    success = closeDevice();
                    if(success) {
                        success = false;
                        int bus_num = Integer.parseInt(arguments.get(0));
                        int device_addr = Integer.parseInt(arguments.get(1), 16);
                        mBus = getMediaRite().getIOController().getI2CBus(bus_num);
                        mDevice = mBus.openDeviceAddress(device_addr);
                        if(mDevice!=null) {
                            success = true;
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
            case READ_BYTE:
                if(mDevice!=null) {
                    try {
                        FutarqueByte b = new FutarqueByte();
                        IOError err = mDevice.readByte(b);
                        if(err==IOError.IOE_NOERROR) {
                            mTextView.append(new BigInteger(b.mValue.toString()).toString(16));
                            mTextView.append("\n");
                            success = true;
                        } else {
                            mTextView.append(err.toString()+"\n");
                        }
                    } catch(RemoteException | NumberFormatException e) {
                        Log.e(TAG,"Error reading byte",e);
                    }
                }
                break;
            case READ_BUFFER:
                if(mDevice!=null) {
                    try {
                        int s = Integer.parseInt(arguments.get(0));
                        byte[] b = new byte[s];
                        IOError err = mDevice.readBuffer(b, s);
                        if(err==IOError.IOE_NOERROR) {
                            for(int idx = 0; idx < s; idx++) {
                                mTextView.append(new BigInteger(String.valueOf(b[idx])).toString(16));
                                if(idx<s-1) {
                                    mTextView.append(":");
                                }
                            }
                            mTextView.append("\n");
                            success = true;
                        } else {
                            mTextView.append(err.toString()+"\n");
                        }
                    } catch(RemoteException | NumberFormatException e) {
                        Log.e(TAG,"Error reading buffer",e);
                    }
                }
                break;
            case WRITE_BYTE:
                if(mDevice!=null) {
                    try {
                        IOError err = mDevice.writeByte((byte) (Integer.parseInt(arguments.get(0),16) & 0xff));
                        if(err==IOError.IOE_NOERROR) {
                            success = true;
                        } else {
                            mTextView.append(err.toString() + "\n");
                        }
                    } catch(RemoteException | NumberFormatException e) {
                        Log.e(TAG,"Error writing byte",e);
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
                        IOError err = mDevice.writeBuffer(b);
                        if(err==IOError.IOE_NOERROR) {
                            success = true;
                        } else {
                            mTextView.append(err.toString() + "\n");
                        }
                    } catch(RemoteException | NumberFormatException e) {
                        Log.e(TAG,"Error writing buffer",e);
                    }
                }
                break;
            case WRITE_READ_BUFFER:
                if(mDevice!=null) {
                    try {
                        int readsize = Integer.parseInt(arguments.get(0));
                        byte[] b = new byte[arguments.size()];
                        for(int idx = 1; idx < b.length; idx++) {
                            b[idx] = (byte) (Integer.parseInt(arguments.get(idx),16) & 0xff);
                        }

                        FutarqueByteArray inBuf = new FutarqueByteArray(b);
                        FutarqueByteArray outBuf = new FutarqueByteArray(10);
                        IOError err = mDevice.writeAndRead(outBuf, 10, inBuf);
                        if(err==IOError.IOE_NOERROR) {
                            success = true;
                            for (int i = 0;i<outBuf.limit();i++) {
                                mTextView.append(String.format("%X",outBuf.get(i)));
                            }
                            mTextView.append("\n");
                        } else {
                            mTextView.append(err.toString() + "\n");
                        }

                    } catch(RemoteException | NumberFormatException e) {
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
