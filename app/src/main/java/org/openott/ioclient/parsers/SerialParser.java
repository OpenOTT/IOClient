package org.openott.ioclient.parsers;

import java.util.StringTokenizer;

public class SerialParser extends BaseParser<SerialParser.Command> {
    public String getUsage() {
        StringBuilder buf = new StringBuilder();
        buf.append("open[o] <device> <baud>").append('\n');
        buf.append("close[c]").append('\n');
        buf.append("available_bytes[b]").append('\n');
        buf.append("read_byte[rb]").append('\n');
        buf.append("read_string[r]").append('\n');
        buf.append("write_byte[wb] <hex>").append('\n');
        buf.append("write_string[w] <message>").append('\n');
        buf.append("help[h]").append('\n');
        return buf.toString();
    }

    public enum Command {
        AVAILABLE_BYTES,
        READ_BYTE,
        READ_STRING,
        WRITE_BYTE,
        WRITE_STRING,
        OPEN,
        CLOSE,
        HELP,
        INVALID
    }

    public Command getInvalidCommand() {
        return Command.INVALID;
    }

    public SerialParser(String command_line) {
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
        if(mCommand==Command.INVALID) {
            if (command.equalsIgnoreCase("o")) {
                mCommand = Command.OPEN;
            } else if (command.equalsIgnoreCase("c")) {
                mCommand = Command.CLOSE;
            } else if (command.equalsIgnoreCase("b")) {
                mCommand = Command.AVAILABLE_BYTES;
            } else if (command.equalsIgnoreCase("rb")) {
                mCommand = Command.READ_BYTE;
            } else if (command.equalsIgnoreCase("r")) {
                mCommand = Command.READ_STRING;
            } else if (command.equalsIgnoreCase("wb")) {
                mCommand = Command.WRITE_BYTE;
            } else if (command.equalsIgnoreCase("w")) {
                mCommand = Command.WRITE_STRING;
            } else if (command.equalsIgnoreCase("h")) {
                mCommand = Command.HELP;
            }
        }
    }

    protected boolean parse(String command_line) {
        StringTokenizer token = new StringTokenizer(command_line," ");
        String cmd = token.nextToken();
        mError = Error.NO_ERROR;
        setCommand(cmd);
        if(mCommand== Command.INVALID) {
            mError = Error.INVALID_COMMAND;
            return false;
        }
        switch (mCommand) {
            case OPEN:
                if(token.countTokens()!=2) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case CLOSE:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case AVAILABLE_BYTES:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case READ_BYTE:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case READ_STRING:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case WRITE_BYTE:
                if(token.countTokens()!=1) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case WRITE_STRING:
                if(token.countTokens()!=1) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case HELP:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
        }
        if(mError!=Error.INVALID_NUM_ARGUMENTS) {
            while(token.hasMoreTokens()) {
                mArguments.add(token.nextToken());
            }
        }
        return true;
    }

}
