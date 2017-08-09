package org.jtc.common.util.command_line;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Oct 31, 2007
 * Time: 12:09:21 PM
 *
 * A command line parameter that takes a value, such as "-a this".
 * FORMAT: expect command line parameters such as "-l val" (or one-letter name, with string for its value).
 */
public class OptionParameter implements CommandLineParameter {
    private StringBuffer value;
    private String name;
    private String usageInfo;

    /** Setup the object with its command-line name. */
    public OptionParameter(String name) {
        this.name = name;
        value = new StringBuffer();
    }

    /** Setup a usage string to prompt the user. */
    public void setUsageInfo(String usageInfo) {
        this.usageInfo = usageInfo;
    }

    /** Set the value of the parameter here. */
    public void setValue(String value) {
        this.value.setLength(0);
        this.value.append(value);
    }

    public String getValue() {
        return value.toString();
    }

    public String getName() {
        return name;
    }


    public String getUsageInfo() {
        return usageInfo;
    }

}
