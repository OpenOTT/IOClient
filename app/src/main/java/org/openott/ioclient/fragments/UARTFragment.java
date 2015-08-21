package org.openott.ioclient.fragments;

import android.app.Activity;
import android.os.RemoteException;
import android.util.Log;

import org.openott.ioclient.parsers.BaseParser;
import org.openott.ioclient.parsers.SerialParser;
import com.futarque.mediarite.common.FutarqueByte;
import com.futarque.mediarite.common.FutarqueString;
import com.futarque.mediarite.io.IOError;
import com.futarque.mediarite.io.ISerialPort;

import java.math.BigInteger;
import java.util.List;

public class UARTFragment extends BaseIOFragment {
    private static String TAG = UARTFragment.class.getSimpleName();
    private ISerialPort mPort;

    private OnFragmentInteractionListener mListener;

    public UARTFragment() {
        // Required empty public constructor
    }


    private boolean closeDevice() {
        boolean success = true;
        try {
            if(mPort!=null) {
                Log.d(TAG,"Closing Serial");
                getMediaRite().getIOController().closeSerial(mPort);
                mPort=null;
            }
        } catch (RemoteException e) {
            success = false;
            Log.e(TAG, "Error closing", e);
        }
        return success;
    }

    @Override
    protected BaseParser getParserInstance(String cmd) {
        return new SerialParser(cmd);
    }

    protected boolean executeCommand(BaseParser parser) {
        SerialParser p = (SerialParser)parser;
        List<String> arguments = p.getArguments();
        boolean success = false;
        Log.d(TAG, "Execute Command "+p.getCommand());
        switch(p.getCommand()) {
            case HELP:
                mTextView.append(new SerialParser("").getUsage());
                success = true;
                break;
            case OPEN: {
                try {
                    success = closeDevice();
                    if(success) {
                        success = false;
                        String device = arguments.get(0);
                        int baud = Integer.parseInt(arguments.get(1));
                        Log.d(TAG,"Opening serial device="+device+" baud="+baud);
                        mPort = getMediaRite().getIOController().openSerial(device, baud);
                        if(mPort!=null) {
                            success = true;
                        } else {
                            Log.d(TAG,"Error opening serial device");
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
            case AVAILABLE_BYTES:
                if(mPort!=null) {
                    try {
                        int availableBytes = mPort.availableBytes();
                        mTextView.append(""+availableBytes+"\n");
                        success = true;
                    } catch(RemoteException | NumberFormatException e) {
                        Log.e(TAG,"Error reading byte",e);
                    }
                }
                break;
            case READ_BYTE:
                if(mPort!=null) {
                    try {
                        FutarqueByte b = new FutarqueByte();
                        IOError err = mPort.readByte(b);
                        if(err==IOError.IOE_NOERROR) {
                            mTextView.append(String.format("%X\n",b.mValue));
                            success = true;
                        } else {
                            mTextView.append(err.toString()+"\n");
                        }
                    } catch(RemoteException | NumberFormatException e) {
                        Log.e(TAG,"Error reading byte",e);
                    }
                }
                break;
            case READ_STRING:
                if(mPort!=null) {
                    try {
                        FutarqueString str = new FutarqueString();
                        IOError err = mPort.readString(str);
                        if(err==IOError.IOE_NOERROR) {
                            mTextView.append(str.mValue);
                            mTextView.append("\n");
                            success = true;
                        } else {
                            mTextView.append(err.toString()+"\n");
                        }
                    } catch(RemoteException e) {
                        Log.e(TAG,"Error reading buffer",e);
                    }
                }
                break;
            case WRITE_BYTE:
                if(mPort!=null) {
                    try {
                        IOError err = mPort.writeByte(Byte.parseByte(arguments.get(0),16));
                        if(err==IOError.IOE_NOERROR){
                            success = true;
                        } else{
                            mTextView.append(err.toString() + "\n");
                        }
                    } catch(RemoteException | NumberFormatException e) {
                        Log.e(TAG,"Error writing byte",e);
                    }
                }
                break;
            case WRITE_STRING:
                if(mPort!=null) {
                    try {
                        String msg = arguments.get(0);
                        IOError err = mPort.writeString(msg);
                        if(err==IOError.IOE_NOERROR){
                            success = true;
                        } else{
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
    }
}
