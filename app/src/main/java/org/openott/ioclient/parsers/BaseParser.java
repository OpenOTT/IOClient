package org.openott.ioclient.parsers;


import java.util.ArrayList;
import java.util.List;

public abstract class BaseParser<T extends Enum> {
    public enum Error {
        NO_ERROR,
        INVALID_COMMAND,
        INVALID_NUM_ARGUMENTS,
        INVALID_ARGUMENT
    }

    protected BaseParser(String cmd) {
        if(!cmd.isEmpty()) {
            parse(cmd);
        } else {
            mCommand = getInvalidCommand();
        }
    }

    protected T mCommand;

    protected List<String> mArguments = new ArrayList<>();
    protected Error mError;


    public T getCommand() {
        return mCommand;
    }

    public List<String> getArguments() {
        return mArguments;
    }

    public Error getError() {
        return mError;
    }

    abstract public String getUsage();
    abstract protected boolean parse(String cmd);
    abstract public T getInvalidCommand();

    public static String getErrorMessage(Error error) {
        switch(error) {
            case NO_ERROR:
                return "No error\n";
            case INVALID_ARGUMENT:
                return "Invalid argument\n";
            case INVALID_COMMAND:
                return "Invalid command\n";
            case INVALID_NUM_ARGUMENTS:
                return "Invalid number of arguments\n";
        }
        return "";
    }


}
