/**
 * $LastChangedBy$:   lfoster
 * $LastChangedDate$: October 22, 2010
 * $Revision: 1.1 $: 1.0
 * $HeadURL$:
 * <p/>
 * Description:
 * Convenience class for allowing registration of header label, versus fetchable key, and allowing the user
 * to completely ignore the column numbers in header-labelled. fetching CSV data.
 *
 * FORMAT NOTE: first row is assumed to be header labels.
 */

package org.jtc.common.util.tsv;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.apache.log4j.Logger;

public class TsvMappedReader {

    private static final char QUOTE_CHAR = '\"';
    private static final char TAB_CHAR = '\t';

    private Map<Integer, String> columnNumVsHeader;
    private Map<String, String> headerVsFetchKey;

    private BufferedReader rdr;
    private UnmappedColumnResponse unmappedColumnResponse = UnmappedColumnResponse.EXCEPTION;

    private Logger logger = Logger.getLogger( TsvMappedReader.class );

    /**
     * Constructor sets up running collections.
     */
    public TsvMappedReader(File csvFile) throws Exception {
        columnNumVsHeader = new HashMap<Integer, String>();
        setCsvFile( csvFile );
    }

    /**
     * Quick-route to setting up the columns vs keys.
     * @param columnVsKey mapping of header name vs fetch-key.
     */
    public void setColumnsVsKeys(Map<String, String> columnVsKey) {
        headerVsFetchKey = columnVsKey;
    }

    /**
     * Override default handling of encountering a column which was NOT included in the
     * mapping (see headerVsFetchKey).  The default response is to throw an exception.
     *
     * @param response from among the enumeration.
     */
    public void setUnmappedColumnResponse( UnmappedColumnResponse response ) {
        this.unmappedColumnResponse = response;
    }

    /**
     * Another route for column-vs-keys.  This one assumes that the
     * column names ARE the keys, and maps x to x.  It will convert the
     * list to a set, and will force the set to be unique.
     *
     * @param columnNames all the columns that need to be mapped to themselves.
     */
    public void setColumnNames(List<String> columnNames) {
        Set columnNameSet = new HashSet<String>();
        columnNameSet.addAll( columnNames );
        if ( columnNameSet.size() > columnNames.size() ) {
            throw new IllegalArgumentException(
                "Two or more column names on the list are redundant, and do not uniquely identify single columns.");
        }

        setColumnNames( columnNameSet );
    }

    /**
     * Another route for column-vs-keys.  This one assumes that the
     * column names ARE the keys, and maps x to x.
     *
     * @param columnNames all the columns that need to be mapped to themselves.
     */
    public void setColumnNames(Set<String> columnNames) {
        headerVsFetchKey = new HashMap<String,String>();

        for ( String nextColumnName: columnNames ) {
            headerVsFetchKey.put( nextColumnName, nextColumnName );
        }

    }

    /**
     * This allows one to register the name heading up a column, with a key for fetching it by later.
     *
     * @param columnName what is over the column, in the CSV file.
     * @param keyName what user would like to pull it by later.
     */
    public void setColumnVsKey(String columnName, String keyName) {
        if (headerVsFetchKey == null)
            headerVsFetchKey = new HashMap<String, String>();
        headerVsFetchKey.put( columnName, keyName );
    }

    /**
     * Get the next row of values from the CSV file being read.
     *
     * @return map or null, if end-of-file.
     * @throws Exception for any called methods.
     */
    public Map<String, String> getRowValues() throws Exception {
        // If return null, means end-of-file.
        String inLine = rdr.readLine(); //utf16toAscii();
        if (inLine == null || inLine.trim().length() == 0) {
            rdr.close();
            return null;
        }
        else {
            Map<String, String> returnMap = new HashMap<String, String>();
            String[] values = digestLine( inLine );
            for (int i = 0; i < columnNumVsHeader.size(); i++) {
                String headerName = columnNumVsHeader.get(i);
                if (headerName == null)
                    continue;

                // Some rows end abruptly, not continuing to pad with tabs.
                if ((values.length - 1) < i)
                    continue;

                String fetchKey = headerVsFetchKey.get( headerName );

                // The Great Unmapped!
                if (fetchKey == null) {
                    // Same message regardless of notification "weight".
                    String message = headerName + " not included in the mapping provided.  " +
                            "This could indicate that the intended settings will not be used.";

                    // Must decide course of action.
                    switch ( unmappedColumnResponse ) {
                        case EXCEPTION: throw new Exception( message );
                        case IGNORE:    continue;
                        case WARNING:   logger.warn( message );
                                        continue;
                    }
                }

                returnMap.put( fetchKey, values[i] );
            }
            return returnMap;
        }

    }

    /** Call this to end the process early. */
    public void close() {
        try {
            rdr.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Read a line from the input reader, and convert it to ascii for internal use.
     *
     * @return ascii version of (expected) UTF-16 Big-Endian input line.
     * @throws Exception for called methods.
     */
    private String utf16toAscii() throws Exception {
        int i = 0;
        String inLine = rdr.readLine();
        // If this is the first line of the input file, it could begin with a
        // big-endian UTF16 encoding magic number prefix.
        StringBuilder builder = new StringBuilder();
        int offset = 0;
        if (inLine.charAt(0) == 0XFF) {
            if (inLine.charAt(1) == 0xFE) {
                i += 2;
            }
            else {
                throw new Exception("Unknown encoding. Magic number " + inLine.substring(0, 2));
            }
        }
        else {
            offset = 1;
        }

        for ( ; i < inLine.length(); i++) {
            char nextChar = inLine.charAt(i);
            if (i % 2 == offset)
                builder.append(nextChar);
        }

        return builder.toString();
    }

    /**
     * For the duration, set the CSV file to use.  This will have the effect of pre-setting header vs column num
     *
     * @param csvFile what file.
     * @throws Exception thown by any called method.
     */
    private void setCsvFile(File csvFile) throws Exception {
        rdr = new BufferedReader( new FileReader( csvFile ) );

        String inLine = rdr.readLine(); //utf16toAscii();
        String[] headers = digestLine( inLine );

        // Collect up all the headers into the mapping vs col-number.
        int i = 0;
        for (String nextHeader : headers) {
            columnNumVsHeader.put( i++, nextHeader );
        }
    }

    /** Convenience method to split up a line. */
    private String[] digestLine( String inline ) {
        if (inline.indexOf(QUOTE_CHAR) > -1) {
            char[] inchars = inline.toCharArray();
            List<String> fieldList = new ArrayList<String>();
            boolean inQuotedString = false;
            StringBuilder currentField = new StringBuilder();
            for (int i = 0; i < inchars.length; i++) {
                char ch = inchars[i];

                // Handle start/termination of quoted strings within the data.
                if (inQuotedString) {
                    if (ch == QUOTE_CHAR) {
                        // Detecting this first should have the effect of keeping tabs in the contents of fields.
                        inQuotedString = false;
                    }
                    currentField.append( ch );
                }
                else if ((ch == QUOTE_CHAR)  &&  (currentField.length() == 0)) {
                    // Detecting this should have the effect of keeping 'embedded double quotes' inside their
                    // tab-delimited content fields.
                    inQuotedString = true;
                    currentField.append( ch );
                }
                else if (ch == TAB_CHAR) {
                    fieldList.add( currentField.toString() );
                    currentField = new StringBuilder();
                }
                else {
                    currentField.append( ch );
                }
            }

            // Add the trailing field.
            fieldList.add( currentField.toString() );

            String[] returnArray = new String[ fieldList.size() ];
            for (int i = 0; i < fieldList.size(); i++) {
                returnArray[i] = fieldList.get(i);
            }

            return returnArray;
        }
        else {
            return inline.split("\t");
        }
    }

    public static enum UnmappedColumnResponse {
        IGNORE, EXCEPTION, WARNING
    }
}
