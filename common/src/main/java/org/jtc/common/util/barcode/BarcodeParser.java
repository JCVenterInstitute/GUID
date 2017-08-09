package org.jtc.common.util.barcode;

/**
 * User: aresnick
 * Date: Oct 5, 2010
 * Time: 4:34:35 PM
 * <p/>
 * $HeadURL$
 * $LastChangedRevision$
 * $LastChangedBy$
 * $LastChangedDate$
 * <p/>
 * Description: Stolen from sanger code base
 */
import java.util.StringTokenizer;

public class BarcodeParser
{

    public BarcodeParser()
    {
    }

    public static String[] getBarcode(String barcodeString)
    {
        StringTokenizer st = new StringTokenizer(barcodeString, System.getProperty("line.separator"));
        String barcodeArray[] = new String[st.countTokens()];
        for(int i = 0; st.hasMoreTokens(); i++)
            barcodeArray[i] = st.nextToken().trim();

        return barcodeArray;
    }
}