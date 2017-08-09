package org.jtc.common.util.command_line;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Oct 31, 2007
 * Time: 11:54:53 AM
 *
 * Breaks up command line arguments logically into option+value, flags, and data.
 */
public class CommandLineHandler {
    private static final String DATA_PARAMETER_NAME_PREFIX = "__dataParameter_";
    private static final String GENERAL_JAVA_USAGE = "java ";
    private static final int MAX_LINE_LENGTH = 70;

    public static final String NEWLINE = System.getProperty("line.separator");

    // todo make this a unit test.
    public static void main(String[] args) {
        CommandLineHandler handler = new CommandLineHandler();
        try {
            String[] cmdArgs;
            if (args.length == 0) {
                cmdArgs = new String [] {
                        "-lowMemory",
                        "c:\\data\\text.txt",
                        "-if", "snow",
                };
            }
            else {
                cmdArgs = args;
            }
            handler.addParameter("if", CommandLineParameterEnum.option);
            handler.addParameter("lowMemory", CommandLineParameterEnum.flag);
            handler.addDataParameter();

            OptionParameter sizeParameter = handler.addOptionParameter("size", "20000");
            DataParameter configParameter = handler.addDataParameter("c:\\data\\config.xml");
            FlagParameter bypassParam = handler.addFlagParameter("bypassRelay", false);

            handler.acceptCommandLine(cmdArgs);
            System.out.println("'" + handler.getDataValue(0) + "'");
            System.out.println(handler.getFlagValue("lowMemory"));
            System.out.println(handler.getOptionValue("if"));

            System.out.println(sizeParameter.getValue());
            System.out.println(bypassParam.getValue());
            System.out.println("'" + configParameter.getValue() + "'");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HashMap<String,CommandLineParameter> parameters;
    private int dataParameterCount = 0;

    /** Constructor sets up collections. */
    public CommandLineHandler() {
        parameters = new HashMap<String,CommandLineParameter>();
    }

    /**
     * Convenience wrapper method, to assume preformatting of blurb, and
     * no additional java parameters.
     *
     * @param clazz which class is the user using.
     * @param blurb what does this app do?
     * @return string describing the first part of the usage.
     */
    public static String getGeneralJavaUsage(
            Class clazz,
            String blurb) {
        return getGeneralJavaUsage(clazz, "", null, blurb, true);
    }

    /** Utility method to help with usages. */
    public static String getGeneralJavaUsage(
            Class clazz,
            String additionalJavaParameters,
            CommandLineParameter[] commandLineParameters,
            String blurb,
            boolean preformattedBlurb) {

        StringBuilder builder = new StringBuilder();
        builder.append(GENERAL_JAVA_USAGE);
        builder.append(additionalJavaParameters.trim());
        builder.append(" ");
        builder.append(clazz.getName());
        builder.append(" ");
        if (commandLineParameters != null) {
            for (CommandLineParameter nextParam: commandLineParameters) {
                if ( nextParam instanceof DataParameter ) {
                    continue;
                }

                builder.append("-");
                builder.append(nextParam.getName());
                // If option, add on the 
                if ( nextParam instanceof OptionParameter ) {
                    builder.append(" <");
                    builder.append(nextParam.getName());
                    builder.append(">");
                }
                builder.append(" ");
            }
        }
        builder.append(NEWLINE);
        builder.append(NEWLINE);
        if (blurb != null) {
            if (preformattedBlurb) {
                builder.append(blurb);
            }
            else {
                builder.append(formatBlurb(blurb));
                builder.append(NEWLINE);
            }
        }
        if (commandLineParameters != null) {
            for (CommandLineParameter nextParam: commandLineParameters) {
                builder.append(CommandLineHandler.NEWLINE);
                builder.append(CommandLineHandler.formUsageLine(nextParam));
            }
        }

        return builder.toString();
    }

    /** Utility method to help with individual parameter usage. */
    public static String formUsageLine(CommandLineParameter param) {
        return param.getName() + " ... " + param.getUsageInfo() + NEWLINE;
    }

    /**
     * Gets value of a parameter.
     *
     * @param name what is param called.
     * @return whatever is the result of adding the param and parsing the command line.
     */
    public boolean getFlagValue(String name) throws Exception {
        FlagParameter param = (FlagParameter)parameters.get(name);
        return param.getValue();
    }

    /** Return the option whose name was given. */
    public String getOptionValue(String name) throws Exception {
        OptionParameter param = (OptionParameter)parameters.get(name);
        return param.getValue();
    }

    /** Return the offset'th data param. */
    public String getDataValue(int offset) throws Exception {
        DataParameter param = (DataParameter)parameters.get(DATA_PARAMETER_NAME_PREFIX + offset);
        return param.getValue();
    }

    /**
     * A common case: just one data file is used, and all the other parameters modify HOW it is used.
     *
     * @return first data value.
     * @throws Exception from any called methods.
     */
    public String getDataValue() throws Exception {
        return getDataValue(0);        
    }

    /** Call this to add expected / possible parameter with no default value. */
    public void addParameter(String name, CommandLineParameterEnum type) throws Exception {
        addParameter(name, type, null);
    }

    /** Call this to add an expected/possible parameter. */
    public CommandLineParameter addParameter(String name, CommandLineParameterEnum type, String defaultValue) throws Exception {
        if (name == null)
            name = DATA_PARAMETER_NAME_PREFIX + dataParameterCount++;

        CommandLineParameter param = null;
        switch (type) {
            case option: {
                OptionParameter o_param = new OptionParameter(name);
                if (defaultValue != null)
                    o_param.setValue(defaultValue);
                param = o_param;
                break;
            }
            case flag: {
                FlagParameter f_param = new FlagParameter(name);
                if (defaultValue != null) {
                    f_param.setValue(Boolean.parseBoolean(defaultValue));
                }
                param = f_param;
                break;
            }
            case data: {
                DataParameter d_param = new DataParameter();
                if (defaultValue != null)
                    d_param.setValue(defaultValue);
                param = d_param;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown enum type: " + type);
            }
        }
        parameters.put(name, param);
        return param;
    }

    /** Convenience: call this to add an expected/possible parameter of a specific type. */
    public DataParameter addDataParameter() throws Exception {
        return addDataParameter(null);
    }

    public FlagParameter addFlagParameter(String name) throws Exception {
        return addFlagParameter(name, false);
    }

    public OptionParameter addOptionParameter(String name) throws Exception {
        return addOptionParameter(name, null);
    }

    public DataParameter addDataParameter(String defaultValue) throws Exception {
        return (DataParameter)addParameter(null, CommandLineParameterEnum.data, defaultValue);
    }

    public FlagParameter addFlagParameter(String name, boolean defaultValue) throws Exception {
        return (FlagParameter)addParameter(name, CommandLineParameterEnum.flag, new Boolean(defaultValue).toString());
    }

    public OptionParameter addOptionParameter(String name, String defaultValue) throws Exception {
        return (OptionParameter)addParameter(name, CommandLineParameterEnum.option, defaultValue);
    }

    /**
     * Take the command line arguments.  Given the parameter settings above, when parameters are encountered here,
     * they are interpretted as values of their appropriate parameter objects, based on encounter order.
     * NOTE: all parameters should be added, using addParameter(), prior to accepting the command line.
     *
     * @param args command line arguments given by a user.
     */
    public void acceptCommandLine(String[] args) throws Exception {
        int dataParamCounter = 0;
        // Go through matching up parameters to objects.
        for (int i = 0; i < args.length; ) {
            if (args[i] == null || args[i].length() == 0)
                continue;

            String arg = args[i];
            String argName = "";
            if (arg.startsWith("-")) {
                argName = arg.substring(1);
            }
            else {
                argName = DATA_PARAMETER_NAME_PREFIX  + dataParamCounter ++;
            }
            CommandLineParameter param = parameters.get(argName);
            i = process(param, i, args);
        }
    }

    private int process(CommandLineParameter param, int i, String[] args) throws Exception {
        if (param instanceof OptionParameter) {
            return process((OptionParameter) param, i, args);
        }
        else if (param instanceof FlagParameter) {
            return process((FlagParameter) param, i, args);
        }
        else if (param instanceof DataParameter) {
            return process((DataParameter) param, i, args);
        }
        return i + 1; // bypass untyped.
    }

    /**
     * Processor methods take as many parameters from the arguments as needed, and bump the
     * offset to the next available parameter.  Here, next arg is the value for the option given by this arg.
     *
     * @param param what to populate.
     * @param i where in the args list.
     * @param args whole list of args.
     * @return where to start processing the next available argument.
     * @throws Exception
     */
    private int process(OptionParameter param, int i, String[] args) throws Exception {
        if ( args.length <= i+1 ) {
            throw new IllegalArgumentException("Failed to add a proper value for " + param.getName());
        }
        String value = args[i+1];
        if (value.startsWith("-"))
            throw new IllegalArgumentException("Failed to add a proper value for " + param.getName());
        param.setValue(value);
        return i + 2;
    }

    /**
     * Processor methods take as many parameters from the arguments as needed, and bump the
     * offset to the next available parameter.  Here, this flag is set to true.
     *
     * @param param what to populate.
     * @param i where in the args list.
     * @param args whole list of args.
     * @return where to start processing the next available argument.
     * @throws Exception
     */
    private int process(FlagParameter param, int i, String[] args) throws Exception {
        param.setValue(true);
        return i + 1;
    }

    /**
     * Processor methods take as many parameters from the arguments as needed, and bump the
     * offset to the next available parameter.  Here, this arg represents the file of data. 
     *
     * @param param what to populate.
     * @param i where in the args list.
     * @param args whole list of args.
     * @return where to start processing the next available argument.
     * @throws Exception
     */
    private int process(DataParameter param, int i, String[] args) throws Exception {
        param.setValue(args[i]);
        return i + 1;
    }

    /**
     * Formats a general text message about what the application does.
     *
     * @param blurb string to tell user all about the app.
     * @return formatted for easy reading.
     */
    private static String formatBlurb(String blurb) {
        StringBuilder formatBuffer = new StringBuilder();
        String[] wordsInBlurb = blurb.split(" ");
        int currentLineLength = 0;
        for (int i = 0; i < wordsInBlurb.length; i++) {
            if (wordsInBlurb[i] != null && wordsInBlurb[i].trim().length() > 0) {
                if (currentLineLength > MAX_LINE_LENGTH) {
                    formatBuffer.append(NEWLINE);
                    currentLineLength = 0;
                }
                formatBuffer.append(wordsInBlurb[i]);
                formatBuffer.append(" ");
                currentLineLength += wordsInBlurb[i].length() + 1;
            }
        }

        return formatBuffer.toString();
    }
}
