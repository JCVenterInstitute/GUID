package org.jtc.common.util.command_line;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Oct 31, 2007
 * Time: 12:13:54 PM
 *
 * Simple flag parameter.  Including this turns something on:  ex: "-b"
 * FORMAT: single letter setting.
 */
public class FlagParameter implements CommandLineParameter {
    private String name;
    private String usageInfo;
    private boolean value = false;

    /** Setup this parameter with its name. */
    public FlagParameter(String name) {
        this.name = name;
    }

    /** Setup prompt text for user. */
    public void setUsageInfo(String usageInfo) {
        this.usageInfo = usageInfo;
    }

    /** Turn flag on or off. It is expected that if this flag is on command line, this will be set to true. */
    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    /** Get the name of the parameter. */
    public String getName() {
        return name;
    }

    /** Get information to prompt user. */
    public String getUsageInfo() {
        return usageInfo;
    }
}
