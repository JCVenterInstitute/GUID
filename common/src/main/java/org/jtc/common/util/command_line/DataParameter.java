package org.jtc.common.util.command_line;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Oct 31, 2007
 * Time: 12:16:19 PM
 *
 * Parameter given by itself, probably last on command line, or following a flag.  Parameter
 * will represent a filename.
 *
 * FORMAT: "filename" string by itself.
 */
public class DataParameter implements CommandLineParameter {
    private StringBuffer value;
    private String usageInfo;

    /** Setup this param.  Note: it has no name. */
    public DataParameter() {
        value = new StringBuffer();
    }

    public void setUsageInfo(String usageInfo) {
        this.usageInfo = usageInfo;
    }

    /** Set the value of a file name. */
    public void setValue(String value) {
        this.value.setLength(0);
        this.value.append(value);
    }

    /** Return whatever the value happens to be. */
    public String getValue() {
        return value.toString();
    }

    /** Data Parameters do not have a name. */
    public String getName() {
        return "";
    }

    public String getUsageInfo() {
        return usageInfo;
    }
}
