package org.openott.ioclient.parsers;

import java.util.StringTokenizer;

public class TunerRpcParser extends BaseParser<TunerRpcParser.Command> {
    public String getUsage() {
        StringBuilder buf = new StringBuilder();
        buf.append("connectInfo[c]                  - Returns the info on where to connect").append('\n');
        buf.append("connect[cx]                     - Connects to the LocalSocket").append('\n');
        buf.append("disconnect[dx]                  - Disconnects from the LocalSocket").append('\n');
        buf.append("help[h] <OPTION>                - Displays This message").append('\n');
        buf.append("    OPTION [caps|tune|tune2|status]  - Extended help for the command").append('\n');
        buf.append(" **** TunerRpc REST API (JSON) ****").append('\n');
        buf.append("numTuners[n]                    - Returns number of tuners detected").append('\n');
        buf.append("caps[cp] <tunerIdx> <OPTION>    - Returns TunerCapabilities - <tunerIdx>: 0 is the first tuner").append('\n');
        buf.append("    OPTION []         - no supplied option returns the combined tuner Caps SupportedFrontEndTypes is a bitfield with the supported FrontEndTypes").append('\n');
        buf.append("    OPTION [#]        - # is the enum value of the FrontEndType to query").append('\n');
        buf.append("tune[t] <OPTION>                - Returns streamID").append('\n');
        buf.append("    OPTION [l]  - Lists build in test urls").append('\n');
        buf.append("    OPTION [#]  - Tunes to url item # in the build in List").append('\n');
        buf.append("    OPTION [url]  - Tunes to url string").append('\n');
        buf.append("status[s] <streamID>            - Returns tuner status Enum FrontEndStatus").append('\n');
        buf.append("info[i] <streamID>              - Returns tuner info from the Tuner").append('\n');
        buf.append(" **** Pid Subscription List methods****").append('\n');
        buf.append("getpids[gp]                     - Displays the Pids currently in the list").append('\n');
        buf.append("addpid[ap] <pid>                - Add a pid to the list - NOTE. The list is applied by calling setpids with an empty pid list").append('\n');
        buf.append("rempid[rp] <pid>                - Remove a pid from the list - NOTE. The list is applied by calling setpids with an empty pid list").append('\n');
        buf.append("setpids[sp] <streamID> <OPTIONS").append('\n');
        buf.append("    OPTIONS []            - no supplied option will apply the current pid list to the specified streamID").append('\n');
        buf.append("    OPTIONS [<pid> ...]   - the supplied pid list will replace the existing pid list and is applied to the specified streamID").append('\n');
        buf.append(" **** Dump Ts to file methods ****").append('\n');
        buf.append("dump[d] <streamID> <filename>   - Dumps the transport stream from the specified streamID to the file. Filename is full path to writable file").append('\n');
        buf.append("dumpStop[ds]                    - Stop file dumping").append('\n');
        buf.append("dumpInfo[di]                    - Print Dump progress").append('\n');
        return buf.toString();
    }
    public String getHelp(String cmd) {
        StringBuilder buf = new StringBuilder();
        buf.append(" ==== ").append(cmd).append(" ====").append('\n');
        if(cmd.equals("caps")) {
            buf.append("enum FrontEndType {").append('\n');
            buf.append("  DVB_S(0),").append('\n');
            buf.append("  DVB_S2(1),").append('\n');
            buf.append("  DVB_T(2),").append('\n');
            buf.append("  DVB_T2(3),").append('\n');
            buf.append("  DVB_C(4),").append('\n');
            buf.append("  ATSC(5),").append('\n');
            buf.append("  ANALOG(6),").append('\n');
            buf.append("  DVB_ASI(7),").append('\n');
            buf.append("  ISDB_T(8),").append('\n');
            buf.append("  DVB_C2(9),").append('\n');
            buf.append("  UNKNOWN(10),").append('\n');
            buf.append("}").append('\n');

        } else if(cmd.equals("tune")) {
            buf.append("ex.").append('\n');
            buf.append("  t tune://dvb-c?frequency=34600000&bandwidth=8000").append('\n');
            buf.append("  t tune://dvb-t?frequency=53800000&bandwidth=8000").append('\n');
            buf.append("").append('\n');
            buf.append("Url Format:").append('\n');
            buf.append("<mediaurl> ::= <protocol>\"://\"<protocol specific params>").append('\n');
            buf.append("<protocol> ::= \"tune\"").append('\n');
            buf.append("<protocol specific params> ::= <tune protocol specific params>").append('\n');
            buf.append("<tune protocol specific params> ::= <path>\"?\"<tuner params>{\"&\"<tuner params>}").append('\n');
            buf.append("<path> ::= \"dvb-s\"|\"dvb-s2\"|\"dvb-c\"|\"dvb-t\"").append('\n');
            buf.append("<tuner params> ::= <key>\"=\"<value>").append('\n');
            buf.append("<key> ::= <path specific keys>").append('\n');
            buf.append("<path specific keys> ::= <dvb-s required-keys> <dvb-s required-keys> | <dvb-s2 keys> | <dvb-c keys> | <dvb-t keys>").append('\n');
            buf.append("").append('\n');
            buf.append("<dvb-s keys> ::= <dvb-s required-keys> <dvb-s optional-keys>").append('\n');
            buf.append("<dvb-s2 keys> ::= <dvb-s required-keys> <dvb-s optional-keys>").append('\n');
            buf.append("<dvb-s required-keys> ::= \"frequency\"|\"symbolrate\"").append('\n');
            buf.append("<dvb-s required-keys> ::= \"polarity\"|\"lnbtype\"|\"lnblolow\"|\"lnblohigh\"|\"lnbtone\"| \"modulation\"|\"diseqc1\"|\"diseqc2\"|\"voltage\"|\"iqmode\"").append('\n');
            buf.append("<dvb-c keys> ::= <dvb-c required-keys> <dvb-c optional-keys>").append('\n');
            buf.append("<dvb-c required-keys> ::= \"frequency\"").append('\n');
            buf.append("<dvb-c optional-keys> ::= \"bandwidth\"|\"symbolrate\"|\"modulation\"").append('\n');
            buf.append("<dvb-t keys> ::= <dvb-t required-keys> <dvb-t optional-keys>").append('\n');
            buf.append("<dvb-t required-keys> ::= \"frequency\"|\"bandwidth\"").append('\n');
            buf.append("<dvb-t optional-keys> ::= \"modulation\"|\"coderatehp\"|\"coderatelp\"|\"guardinterval\"|\"transmissionmode\"").append('\n');
            buf.append("<value> ::= integer | string").append('\n');
        } else if(cmd.equals("tune2")) {
            buf.append("Tune protocol parameters").append('\n');
            buf.append("============================").append('\n');
            buf.append("frequency  : input frequency, integer value in decahertz (eg. 1132500000,53800000)").append('\n');
            buf.append("symbolrate : symbol rate, integer value (eg. 24500,6900)").append('\n');
            buf.append("polarity   : polarity, char value (eg. 'V' or 'H')").append('\n');
            buf.append("bandwidth  : Bandwidth, integer value in kHz").append('\n');
            buf.append("modulation : Modulation, integer value").append('\n');
            buf.append("(QPSK = 1, 8PSK = 2, QAM = 3, 4QAM = 4, 16QAM = 5, 32QAM = 6, 64QAM = 7, 128QAM = 8, 256QAM = 9, BPSK = 10)").append('\n');
            buf.append("lnbtype    : LNB Type, integer value").append('\n');
            buf.append("(1=universal, 2=5150C, 3=9750KU, 4=10000KU, 5=10050KU, 6=10600KU, 7=10750KU, 8=11300KU)").append('\n');
            buf.append("Default is universal if value is not set.").append('\n');
            buf.append("lnblolow   : LNB low frequency, integer value in decahertz, default is 975000000 if not set.").append('\n');
            buf.append("lnblohigh  : LNB high frequency, integer value in decahertz, default is 1060000000 if not set.").append('\n');
            buf.append("lnbtone    : Handling of 22Khz tone").append('\n');
            buf.append("(-1 = automatic, 0 = disabled, 1 = enabled)").append('\n');
            buf.append("default is automatic if not set.").append('\n');
            buf.append("modulation : Modulation, integer value").append('\n');
            buf.append("(QPSK = 1, 8PSK = 2, QAM = 3, 4QAM = 4, 16QAM = 5, 32QAM = 6, 64QAM = 7, 128QAM = 8, 256QAM = 9, BPSK = 10)").append('\n');
            buf.append("diseqc1    : DiseqC1 device, integer value").append('\n');
            buf.append("diseqc2    : DiseqC2 device, integer value").append('\n');
            buf.append("voltage    : Sets the specific LNB voltage (overrides polarity), integer value (eg. 14)").append('\n');
            buf.append("iqmode     : Input spectrum handling, char value").append('\n');
            buf.append("('N' = normal, 'I' = inverted) default when not set is automatic handling").append('\n');
        } else if(cmd.equals("status")) {
            buf.append("enum FutarqueFrontEndStatus{").append('\n');
            buf.append("    FFE_STATUS_UNKNOWN = 0,").append('\n');
            buf.append("    FFE_STATUS_SCANNING,").append('\n');
            buf.append("    FFE_STATUS_LOCKED,").append('\n');
            buf.append("    FFE_STATUS_UNLOCKED,  // Tuner Is Unlocked from here").append('\n');
            buf.append("    FFE_STATUS_TIMEOUT,").append('\n');
            buf.append("    FFE_STATUS_NOT_FOUND,").append('\n');
            buf.append("    FFE_STATUS_STANDBY,").append('\n');
            buf.append("    FFE_STATUS_TUNER_WAS_NICKED, // Somebody stole our network tuner").append('\n');
            buf.append("    FFE_STATUS_ANTENNA_ERROR").append('\n');
            buf.append("};").append('\n');
        }
        buf.append(" ===============================").append('\n');
        return buf.toString();
    }

    public enum Command {
        INFO,
        CONFIG,
        SET_PIDS,
        GET_PIDS,
        ADD_PID,
        REM_PID,
        NUM_TUNERS,
        CAPS,
        HELP,
        CONNECT,
        DISCONNECT,
        TUNE,
        DUMP,
        DUMP_STOP,
        DUMP_INFO,
        STATUS,
        INVALID
    }

    public Command getInvalidCommand() {
        return Command.INVALID;
    }

    public TunerRpcParser(String command_line) {
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
        if(mCommand== Command.INVALID) {
            if (command.equalsIgnoreCase("i") || command.equalsIgnoreCase("info")) {
                mCommand = Command.INFO;
            } else if (command.equalsIgnoreCase("c") || command.equalsIgnoreCase("connectinfo")) {
                mCommand = Command.CONFIG;
            } else if (command.equalsIgnoreCase("n") || command.equalsIgnoreCase("numtuners")) {
                mCommand = Command.NUM_TUNERS;
            } else if (command.equalsIgnoreCase("cp") || command.equalsIgnoreCase("caps")) {
                mCommand = Command.CAPS;
            } else if (command.equalsIgnoreCase("cx") || command.equalsIgnoreCase("connect")) {
                mCommand = Command.CONNECT;
            } else if (command.equalsIgnoreCase("dx") || command.equalsIgnoreCase("disconnect")) {
                mCommand = Command.DISCONNECT;
            } else if (command.equalsIgnoreCase("t") || command.equalsIgnoreCase("tune")) {
                mCommand = Command.TUNE;
            } else if (command.equalsIgnoreCase("d") || command.equalsIgnoreCase("dump")) {
                mCommand = Command.DUMP;
            } else if (command.equalsIgnoreCase("ds") || command.equalsIgnoreCase("dumpstop")) {
                mCommand = Command.DUMP_STOP;
            } else if (command.equalsIgnoreCase("di") || command.equalsIgnoreCase("dumpinfo")) {
                mCommand = Command.DUMP_INFO;
            } else if (command.equalsIgnoreCase("sp") || command.equalsIgnoreCase("setpids")) {
                mCommand = Command.SET_PIDS;
            } else if (command.equalsIgnoreCase("gp") || command.equalsIgnoreCase("getpids")) {
                mCommand = Command.GET_PIDS;
            } else if (command.equalsIgnoreCase("ap") || command.equalsIgnoreCase("addpid")) {
                mCommand = Command.ADD_PID;
            } else if (command.equalsIgnoreCase("rp") || command.equalsIgnoreCase("rempid")) {
                mCommand = Command.REM_PID;
            } else if (command.equalsIgnoreCase("s") || command.equalsIgnoreCase("status")) {
                mCommand = Command.STATUS;
            } else if (command.equalsIgnoreCase("h") || command.equalsIgnoreCase("help")) {
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
            case INFO:
                if(token.countTokens()!=1) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case HELP:
                if(token.countTokens()!=0 && token.countTokens()!=1) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case CONFIG:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case NUM_TUNERS:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case CAPS:
                if(token.countTokens()!=1 && token.countTokens()!=2) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case SET_PIDS:
                if(token.countTokens()<1) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case GET_PIDS:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case ADD_PID:
                if(token.countTokens()!=1) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case REM_PID:
                if(token.countTokens()!=1) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case CONNECT:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case DISCONNECT:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case TUNE:
                if(token.countTokens()!=1) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case DUMP:
                if(token.countTokens()!=2) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case DUMP_STOP:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case DUMP_INFO:
                if(token.countTokens()!=0) {
                    mCommand= Command.INVALID;
                    mError = Error.INVALID_NUM_ARGUMENTS;
                }
                break;
            case STATUS:
                if(token.countTokens()!=1) {
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
