package org.openott.ioclient.parsers;

import android.util.Log;

import java.util.StringTokenizer;

public class I2CParser extends BaseParser<I2CParser.Command> {

    public String getUsage() {
        StringBuilder buf = new StringBuilder();
        buf.append("open[o] <bus> <i2c_address:hex>").append('\n');
        buf.append("close[c]").append('\n');
        buf.append("read_byte[rb]").append('\n');
        buf.append("read_buffer[rbuf] <num_bytes>").append('\n');
        buf.append("write_byte[wb] <hex>").append('\n');
        buf.append("write_buffer[wbuf] <hex> <hex> ...").append('\n');
        buf.append("write_read_buffer[wrbuf] <readsize> <hex> <hex> ...").append('\n');
        buf.append("help[h]").append('\n');
        return buf.toString();
    }

    public enum Command {
        READ_BYTE,
        READ_BUFFER,
        WRITE_BYTE,
        WRITE_BUFFER,
        WRITE_READ_BUFFER,
        OPEN,
        CLOSE,
        HELP,
        INVALID
    }

    public Command getInvalidCommand() {
        return Command.INVALID;
    }

    public I2CParser(String command_line) {
        super(command_line);
    }

    private void setCommand(String command) {
        mCommand = Command.INVALID;
        for(Command c : Command.values()) {
            if(c.name().equalsIgnoreCase(command)) {
                mCommand = c;
                break;
            }
        }
        if(mCommand==Command.INVALID){
            if(command.equalsIgnoreCase("wb")){
                mCommand = Command.WRITE_BYTE;
            } else if(command.equalsIgnoreCase("rb")){
                mCommand = Command.READ_BYTE;
            } else if(command.equalsIgnoreCase("wbuf")){
                mCommand = Command.WRITE_BUFFER;
            } else if(command.equalsIgnoreCase("rbuf")){
                mCommand = Command.READ_BUFFER;
            } else if(command.equalsIgnoreCase("wrbuf")){
                mCommand = Command.WRITE_READ_BUFFER;
            } else if(command.equalsIgnoreCase("o")){
                mCommand = Command.OPEN;
            } else if(command.equalsIgnoreCase("c")){
                mCommand = Command.CLOSE;
            } else if(command.equalsIgnoreCase("h")){
                mCommand = Command.HELP;
            }
        }
    }

    protected boolean parse(String command_line) {
        StringTokenizer token = new StringTokenizer(command_line," ");
        String cmd = token.nextToken();
        mError = Error.NO_ERROR;
        setCommand(cmd);
        if(mCommand==Command.INVALID) {
            mError = Error.INVALID_COMMAND;
            return false;
        }
        switch (mCommand) {
            case OPEN:
                if(token.countTokens()!=2) {
                    mCommand=Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case CLOSE:
                if(token.countTokens()!=0) {
                    mCommand=Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case READ_BYTE:
                if(token.countTokens()!=0) {
                    mCommand=Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case READ_BUFFER:
                if(token.countTokens()!=1) {
                    mCommand=Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case WRITE_BYTE:
                if(token.countTokens()!=1) {
                    mCommand=Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case WRITE_BUFFER:
                if(token.countTokens()<1) {
                    mCommand=Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case WRITE_READ_BUFFER:
                if(token.countTokens()<2) {
                    mCommand=Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case HELP:
                if(token.countTokens()!=0) {
                    mCommand=Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
        }
        if(mError==Error.NO_ERROR) {
            while(token.hasMoreTokens()) {
                mArguments.add(token.nextToken());
            }
            Log.i("I2C CMD","mCommand "+mCommand.name()+" argc=" + mArguments.size());
            int i = 0;
            for(String a :mArguments) {
                Log.i("I2C CMD",String.format("[%d] %s",i,a));
                i++;
            }

        }

        return true;
    }

}
