package org.openott.ioclient.fragments;

import android.app.Activity;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.RemoteException;
import android.util.Log;
import org.openott.ioclient.parsers.BaseParser;
import org.openott.ioclient.parsers.TunerRpcParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jap on 5/10/16.
 */

public class TunerRpcFragment extends BaseIOFragment {
    private static String TAG = TunerRpcFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private LocalSocket mLocalSocket = null;
    private ArrayList<String> mUrlList = new ArrayList<>();
    private ArrayList<Integer> mPids = new ArrayList<>();
    private Thread mDumpThread = null;
    private FileDumperTask mDumpTask = null;

    public TunerRpcFragment() {
        mUrlList.add("tune://dvb-c?frequency=34600000&bandwidth=8000");
        mUrlList.add("tune://dvb-c?frequency=57800000&bandwidth=8000");
        mUrlList.add("tune://dvb-t?frequency=53800000&bandwidth=8000");
    }

    private class FileDumperTask implements Runnable {
        public LocalSocket mLocalSocket = null;
        public int mId = 0;
        public File mFile = null;
        public long mTotal = 0;
        public boolean mTerminate = false;
        public FileDumperTask(LocalSocket socket, int id, File file) {
            super();
            mLocalSocket = socket;
            mId = id;
            mFile = file;
        }

        public void terminate(){
            mTerminate = true;
        }

        @Override
        public void run() {
            BufferedWriter output = null;
            try {
                final InputStream in = mLocalSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                output = new BufferedWriter(new FileWriter(mFile));
                int l = 0;
                char[] buf = new char[8192];
                while(!mTerminate && mLocalSocket.isConnected() && (l >= 0)){
                    l = reader.read(buf,0,buf.length);
                    mTotal+=l;
                    output.write(buf, 0, l);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if ( output != null ) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected BaseParser getParserInstance(String cmd) {
        return new TunerRpcParser(cmd);
    }
    private static final String mCmdHeader = "POST /json/@CMD@ HTTP/1.1\r\nUser-Agent: MediaRite Rpc IOControllerClient-Linux\r\nContent-Type: application/json; charset=utf-8\r\nContent-length: @SIZE@\r\n\r\n";
    private enum TunerRpcCmd {
        TuneUrl,
        GetStatus,
        Stop,
        EnterStreamMode,
        GetInfo,
        SetPids,
        GetNumberOfTuners,
        GetCapabilities,
        GetUdn
    }

    private String createCommand(TunerRpcCmd iCmd, String payload) {
        int len = payload.length();
        String slen = ""+len;
        Log.i(TAG,"CMD "+iCmd+" "+payload);
        String cmdStr = mCmdHeader.replace("@SIZE@",slen).replace("@CMD@",iCmd.name());
        Log.i(TAG,"HERE "+cmdStr);

        cmdStr += payload;
        return cmdStr;
    }

    public class jsonBuilder{
        private ArrayList<String> mData = new ArrayList<>();

        public jsonBuilder() {}
        jsonBuilder addString(String k, String v){
            mData.add("\""+k+"\""+":\""+v+"\"");
            return this;
        }
        jsonBuilder addInt(String k, int v){
            mData.add("\""+k+"\""+":"+v+"");
            return this;
        }
        jsonBuilder addIntArray(String k, int[] v){
            String data = "\""+k+"\""+":[";
            String sep = "";
            for(int i : v){
                data+=sep+i;
                sep = ",";
            }
            data+="]";
            mData.add(data);
            return this;
        }
        jsonBuilder addIntArray(String k, ArrayList<Integer> v){
            String data = "\""+k+"\""+":[";
            String sep = "";
            for(int i : v){
                data+=sep+i;
                sep = ",";
            }
            data+="]";
            mData.add(data);
            return this;
        }
        String build(){
            String s = "{";
            String sep = "";
            for(String k : mData){
                s += sep + k;
                sep = ",";
            }
            s += "}";
            return s;
        }
    }

    private int sendCmd(String request) {
        Log.i(TAG,"Sending\n"+request+"\n");
        byte[] bytes = request.getBytes(Charset.forName("UTF-8"));
        try {
            mLocalSocket.getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            mTextView.append(String.format("Error - writing bytes\n"));
            return -100;
        }
        return bytes.length;
    }

    public class TunerRpcResponse {
        public int mResponseCode = 0;
        public String mResponseMsg = "";
        public String mHeader = "";
        public int mContentLength = 0;
        public String mContent = "";
    }

    private int receiveReply(TunerRpcResponse response) {
        try {
            final InputStream in = mLocalSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            StringBuilder result = new StringBuilder();
            String line;
            line = reader.readLine();
            if(line==null || line.equals("")) {
                return -1;
            }

            Pattern line1 = Pattern.compile("HTTP/1\\.1\\s(\\d+)\\s(.*)");
            Matcher matcher1 = line1.matcher(line);
            if(!matcher1.matches()) {
                return -2;
            }
            result.append(line);

            response.mResponseCode = Integer.parseInt(matcher1.group(1));
            response.mResponseMsg = matcher1.group(2);

            line = reader.readLine();
            if(line==null || line.equals("")) {
                return -3;
            }

            Pattern line2 = Pattern.compile("Content-Length:\\s(\\d+).*");
            Matcher matcher2 = line2.matcher(line);
            if(!matcher2.matches()) {
                return -4;
            }
            result.append(line);
            response.mContentLength = Integer.parseInt(matcher2.group(1));

            while((line = reader.readLine()) != null && !line.equals("")) {
                result.append(line);
            }
            response.mHeader = result.toString();

            Log.i(TAG,result.toString());
            result = new StringBuilder();

            char[] buf = new char[1000];
            int l = 0;
            int total = 0;
            while (l >= 0 && total<response.mContentLength) {
                l = reader.read(buf,0,buf.length);
                total+=l;
                result.append(buf, 0, l);
            }

            response.mContent = result.toString();

            return response.mResponseCode;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return -10;
    }
    private int sendReceive(String request, TunerRpcResponse response) {
        if (mLocalSocket == null || !mLocalSocket.isConnected()) {
            mTextView.append(String.format("Error - NOT connected\n"));
            return -2;
        }
        if (request != null && response != null) {
            int sendErr = sendCmd(request);
            if (sendErr < 0) {
                return sendErr;
            }
            int reply = receiveReply(response);
            if (reply < 0) {
                mTextView.append(String.format("Error - Unable to read response\n"));
                return reply;
            }

            if (reply != 200) {
                mTextView.append(String.format("Error - [%d] %s\n", response.mResponseCode, response.mResponseMsg));
                return -3;
            }

            mTextView.append(String.format("SUCCESS - [%d] %s\n", response.mResponseCode, response.mContent));
            return 0;
        }
        return -11;
    }

    private int doTuneUrl(String url) {
        if(url!=null){
            String request = createCommand(TunerRpcCmd.TuneUrl,new jsonBuilder().addString("Url",url).build());
            TunerRpcResponse response = new TunerRpcResponse();
            int sendErr = sendReceive(request,response);
            return sendErr;
        }
        return -1;
    }

    private int doGetStatus(int id) {
        String request = createCommand(TunerRpcCmd.GetStatus,new jsonBuilder().addInt("Id",id).build());
        TunerRpcResponse response = new TunerRpcResponse();
        int sendErr = sendReceive(request,response);
        return sendErr;
    }

    private int doGetInfo(int id) {
        String request = createCommand(TunerRpcCmd.GetInfo,new jsonBuilder().addInt("Id",id).build());
        TunerRpcResponse response = new TunerRpcResponse();
        int sendErr = sendReceive(request,response);
        return sendErr;
    }
    private int doGetCapabilities(int idx, int feType) {
        String request = createCommand(TunerRpcCmd.GetCapabilities,new jsonBuilder().addInt("Tuner",idx).addInt("FutarqueFrontEndType",feType).build());
        TunerRpcResponse response = new TunerRpcResponse();
        int sendErr = sendReceive(request,response);
        return sendErr;
    }
    private int doGetNumberOfTuners() {
        String request = createCommand(TunerRpcCmd.GetNumberOfTuners,new jsonBuilder().build());
        TunerRpcResponse response = new TunerRpcResponse();
        int sendErr = sendReceive(request,response);
        return sendErr;
    }

    private int doSetPids(int id) {
        String request = createCommand(TunerRpcCmd.SetPids,new jsonBuilder().addInt("Id",id).addIntArray("Pids",mPids).build());
        TunerRpcResponse response = new TunerRpcResponse();
        int sendErr = sendReceive(request,response);
        return sendErr;
    }

    private int doEnterStreamMode(int id, File file) {
        String request = createCommand(TunerRpcCmd.EnterStreamMode,new jsonBuilder().addInt("Id",id).build());
        TunerRpcResponse response = new TunerRpcResponse();
        int sendErr = sendReceive(request,response);
        if(sendErr>=0){
            mDumpTask = new FileDumperTask(mLocalSocket,id,file);
            mDumpThread = new Thread(mDumpTask);
            mDumpThread.start();

            mLocalSocket = null;

            try {
                mLocalSocket = new LocalSocket();
                if(mLocalSocket.isConnected()){
                    mTextView.append(String.format("Error is already connected\n"));
                    return -21;
                }
                String localSocket = getMediaRite().getIOController().getTunerRpcLocalSocket();
                if(localSocket!=null && !localSocket.equals("")){
                    LocalSocketAddress localSocketAddress = new LocalSocketAddress(localSocket, LocalSocketAddress.Namespace.FILESYSTEM);
                    try {
                        mLocalSocket.connect(localSocketAddress);
                    } catch (IOException e) {
                        mTextView.append(String.format("Unable to connect to %s\n",localSocket));
                        e.printStackTrace();
                        return -22;
                    }
                    if(!mLocalSocket.isConnected()){
                        mTextView.append(String.format("Unable to connect to %s\n",localSocket));
                        return -23;
                    }
                }
            } catch(RemoteException | NumberFormatException e) {
                Log.e(TAG,"Error opening", e);
            }


        }
        return sendErr;
    }

    protected boolean executeCommand(BaseParser parser) {
        TunerRpcParser p = (TunerRpcParser)parser;
        List<String> arguments = p.getArguments();
        boolean success = false;
        Log.d(TAG, "Execute Command "+p.getCommand());
        switch(p.getCommand()) {
            case HELP: {
                if(arguments.size()>0){
                    mTextView.append(new TunerRpcParser("").getHelp(arguments.get(0)));
                } else {
                    mTextView.append(new TunerRpcParser("").getUsage());
                }
                success = true;
            }
                break;
            case CONFIG:
                try {
                    String localSocket = getMediaRite().getIOController().getTunerRpcLocalSocket();
                    int tcpPort = getMediaRite().getIOController().getTunerRpcTcpPort();
                    mTextView.append(String.format("TcpPort     = %d\n",tcpPort));
                    mTextView.append(String.format("LocalSocket = %s\n",localSocket));
                    success = true;
                } catch(RemoteException | NumberFormatException e) {
                    Log.e(TAG,"Error opening", e);
                }
                break;
            case CONNECT:
                try {
                    if(mLocalSocket==null) {
                        mLocalSocket = new LocalSocket();
                    }
                    if(mLocalSocket.isConnected()){
                        mTextView.append(String.format("Error is already connected\n"));
                        break;
                    }
                    String localSocket = getMediaRite().getIOController().getTunerRpcLocalSocket();
                    if(localSocket!=null && !localSocket.equals("")){
                        LocalSocketAddress localSocketAddress = new LocalSocketAddress(localSocket, LocalSocketAddress.Namespace.FILESYSTEM);
                        try {
                            mLocalSocket.connect(localSocketAddress);
                        } catch (IOException e) {
                            mTextView.append(String.format("Unable to connect to %s\n",localSocket));
                            e.printStackTrace();
                            break;
                        }
                        if(!mLocalSocket.isConnected()){
                            mTextView.append(String.format("Unable to connect to %s\n",localSocket));
                            break;
                        }
                        mTextView.append(String.format("Connected\n"));
                        success = true;
                    }
                } catch(RemoteException | NumberFormatException e) {
                    Log.e(TAG,"Error opening", e);
                }
                break;
            case DISCONNECT:
                if(mLocalSocket!=null) {
                    if(mLocalSocket.isConnected()){
                        try {
                            mLocalSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mTextView.append(String.format("Disconnected\n"));
                    }
                    success = true;
                    mLocalSocket = null;
                }
                break;
            case TUNE: {
                String url = arguments.get(0);
                Pattern line = Pattern.compile("(\\d+)");
                Matcher matcher = line.matcher(url);
                if (matcher.matches()) {
                    int pos = Integer.parseInt(matcher.group(1));
                    if (pos >= mUrlList.size()) {
                        mTextView.append(String.format("Url Index out of range\n"));
                        break;
                    }
                    url = mUrlList.get(pos);
                } else if (url.equals("l") || url.equals("list")) {
                    int pos = 0;
                    for (String l : mUrlList) {
                        mTextView.append(String.format("[%d] %s\n", pos, l));
                        pos++;
                    }
                    success = true;
                    break;
                }
                int ret = doTuneUrl(url);
                if (ret >= 0) {
                    success = true;
                }
            }
            break;
            case STATUS: {
                int id = Integer.parseInt(arguments.get(0));
                int ret = doGetStatus(id);
                if (ret >= 0) {
                    success = true;
                }
            }
            break;
            case DUMP_STOP: {
                if(mDumpThread==null){
                    mTextView.append(String.format("Not Dumping\n"));
                    break;
                }
                mDumpTask.terminate();
                mDumpThread.interrupt();
                try {
                    mTextView.append(String.format("Dump complete [%d] %s %d bytes\n", mDumpTask.mId, mDumpTask.mFile.getAbsoluteFile(), mDumpTask.mTotal));
                    mDumpThread.join();
                    mDumpThread = null;
                    mDumpTask = null;
                    success = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            break;
            case DUMP_INFO: {
                if(mDumpThread==null){
                    mTextView.append(String.format("Not Dumping\n"));
                    break;
                }

                mTextView.append(String.format("Currently Dumping [%d] %s\n", mDumpTask.mId, mDumpTask.mFile.getAbsoluteFile()));
                mTextView.append(String.format("Written %d bytes\n", mDumpTask.mTotal));
                success = true;
            }
            break;
            case DUMP:
            {
                if(mDumpThread!=null){
                    mTextView.append(String.format("Already Dumping file %s\n",mDumpTask.mFile.getName()));
                    break;
                }
                int id = Integer.parseInt(arguments.get(0));
                String filename = arguments.get(1);
                File targetFile = new File(filename);
                if(targetFile.exists()){
                    if(!targetFile.delete()){
                        mTextView.append(String.format("Unable to delete old file\n"));
                        break;
                    }
                }
                boolean createdFile = false;
                try {
                    if(targetFile.createNewFile()){
                        createdFile = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(!createdFile){
                     mTextView.append(String.format("Unable to create file\n"));
                    break;
                }

                int ret = doEnterStreamMode(id,targetFile);
                if (ret >= 0) {
                    success = true;
                }
            }
            break;
            case INFO: {
                int id = Integer.parseInt(arguments.get(0));
                int ret = doGetInfo(id);
                if (ret >= 0) {
                    success = true;
                }
            }
            break;
            case SET_PIDS: {
                int id = Integer.parseInt(arguments.get(0));
                int numArgs = arguments.size();
                if(numArgs>1){
                    mPids.clear();
                    boolean isFirst = true;
                    for(String arg : arguments){
                        if(!isFirst){
                            mPids.add(Integer.parseInt(arg));
                        }
                        isFirst = false;
                    }
                }
                String out = "Setting Pids [";
                for(Integer pid : mPids){
                    out+=" "+pid;
                }
                out+=" ]";
                mTextView.append(String.format("%s\n",out));
                int ret = doSetPids(id);
                if (ret >= 0) {
                    success = true;
                }
            }
            break;
            case GET_PIDS: {
                String out = "Pids [";
                for(Integer pid : mPids){
                    out+=" "+pid;
                }
                out+=" ]";
                mTextView.append(String.format("%s\n",out));
                success = true;
            }
            break;
            case ADD_PID: {
                int arg = Integer.parseInt(arguments.get(0));
                for(Integer pid : mPids){
                    if(pid==arg) {
                        mTextView.append(String.format("Pid already Added\n"));
                        break;
                    }
                }
                mPids.add(arg);
                String out = "Pids [";
                for(Integer pid : mPids){
                    out+=" "+pid;
                }
                out+=" ]";
                mTextView.append(String.format("%s\n",out));
                success = true;
            }
            break;
            case REM_PID: {
                int arg = Integer.parseInt(arguments.get(0));
                boolean isFound = false;
                for(Integer pid : mPids){
                    if(pid==arg) {
                        isFound = true;
                        mPids.remove(arg);
                        break;
                    }
                }
                if(!isFound){
                    mTextView.append(String.format("Pid not found\n"));
                    break;
                }
                String out = "Pids [";
                for(Integer pid : mPids){
                    out+=" "+pid;
                }
                out+=" ]";
                mTextView.append(String.format("%s\n",out));
                success = isFound;
            }
            break;
            case CAPS:{
                int idx = Integer.parseInt(arguments.get(0));
                int feType = 10;
                if(arguments.size()>1){
                    Pattern line = Pattern.compile("(\\d+)");
                    String arg2 = arguments.get(1);
                    Matcher matcher = line.matcher(arg2);
                    if (matcher.matches()) {
                        feType = Integer.parseInt(matcher.group(1));
                    } else if(arguments.get(1).equals("t")){
                        feType = 2;
                    } else if(arguments.get(1).equals("s")){
                        feType = 1;
                    } else if(arguments.get(1).equals("c")){
                        feType = 4;
                    }
                }
                int ret = doGetCapabilities(idx,feType);
                if (ret >= 0) {
                    success = true;
                }
            }
            break;
            case NUM_TUNERS: {
                int ret = doGetNumberOfTuners();
                if (ret >= 0) {
                    success = true;
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

