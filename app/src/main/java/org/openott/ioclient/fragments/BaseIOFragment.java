package org.openott.ioclient.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.openott.ioclient.R;
import org.openott.ioclient.parsers.BaseParser;

public abstract class BaseIOFragment extends BaseFragment {
    public static final String TAG = BaseIOFragment.class.getSimpleName();
    protected TextView mTextView;
    protected EditText mSendTextEdit;
    protected Button mSendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_i2c, container, false);
        mSendButton = (Button)v.findViewById(R.id.send_button);
        mSendTextEdit = (EditText)v.findViewById(R.id.send_text);
        mTextView = (TextView)v.findViewById(R.id.text_view);

        mTextView.setMovementMethod(new ScrollingMovementMethod());
        mSendTextEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event!=null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER) {
                    handleUserInput();
                    return true;
                }
                return false;
            }
        });
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleUserInput();
            }
        });

        mTextView.append(getParserInstance("").getUsage());

        return v;
    }


    abstract protected BaseParser getParserInstance(String cmd);
    abstract protected boolean executeCommand(BaseParser parser);

    protected void handleUserInput() {
        String cmd = mSendTextEdit.getText().toString();
        if(cmd.isEmpty()) {
            return;
        }
        Log.d(TAG, "UserInput=" + cmd);
        mTextView.append("> "+cmd+"\n");
        BaseParser parser = getParserInstance(cmd);
        if(parser.getCommand() == parser.getInvalidCommand()) {
            mTextView.append(BaseParser.getErrorMessage(parser.getError()));
        } else {
            if(executeCommand(parser)) {
                mSendTextEdit.setText("");
            } else {
                mTextView.append("Execution failed\n");
            }
        }
        mSendTextEdit.requestFocus();
    }

}
