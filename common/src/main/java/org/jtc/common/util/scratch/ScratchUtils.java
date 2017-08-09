package org.jtc.common.util.scratch;

import org.jtc.common.util.property.PropertyHelper;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * $LastChangedBy: lfoster $:
 * $LastChangedDate: 2011-08-02 12:18:43 -0700 (Tue, 02 Aug 2011) $:
 * $Revision: 32262 $:
 * $HeadURL: http://svn.jcvi.org/SE/LIMS/branches/LIMSMigrationToMySQL/common/utilities/main/src/java/org/jtc/common/util/scratch/ScratchUtils.java $:
 * <p/>
 * Description: Utility function(s) for dealing with scratch directories, and/or files.
 */
public class ScratchUtils {

    public static final String SCRATCH_DIR_PROP = "ScratchUtils.ScratchDirectory";

    static final Logger logger = Logger.getLogger(ScratchUtils.class);

    private static String scratchBaseLocation;

    private ScratchUtils() {
        // Do not construct.  Utility/static class only.        
    }

    /** Forceably set the scratch location, if need to avoid dependence on properties load. */
    public static void setScratchBaseLocation(String scratchBaseLocation) {
        ScratchUtils.scratchBaseLocation = scratchBaseLocation;
    }

    /**
     * Given an appropriate prefix, and an ID for some increment (task) to carry out,
     * this method finds or creates the required scratch directory location.  Once
     * this has been called, there will be a directory with the name that is returned,
     * or an exception will have been thrown.
     *
     * @param name of the scratch directory.
     * @return the directory.
     * @throws Exception thrown if non-dir file of same name exists, or for IOs.
     */
    public static File getScratchLocation(String name) throws Exception {
        return getScratchLocation(new Long(0), name, null);
    }

    /**
     * Given the unique id, only, locates the scratch location for it.  Will NOT create a new one.
     * If this exists but cannot be read, return null.
     *
     * @param uniqueId of the scratch directory.
     * @return the directory.
     * @throws Exception thrown for IOs.
     */
    public static File getExistingScratchLocation(long uniqueId) throws Exception {
        File returnFile = new File(scratchBaseLocation, new Long(uniqueId).toString());
        if (! returnFile.canRead())
            returnFile = null;
        return returnFile;
    }

    /**
     * Given an appropriate prefix, and an ID for some increment (task) to carry out,
     * this method finds or creates the required scratch directory location.  Once
     * this has been called, there will be a directory with the name that is returned,
     * or an exception will have been thrown.
     *
     * @param uniqueID ID for task.  May be 0.
     * @param prefix to prepend to the scratch directory.
     * @return the directory.
     * @throws Exception thrown if non-dir file of same name exists, or for IOs.
     */
    public static File getScratchLocation(Long uniqueID, String prefix) throws Exception {
        return getScratchLocation(uniqueID, prefix, null);
    }

    /**
     * Given an appropriate prefix, and an ID for some increment (task) to carry out,
     * this method finds or creates the required scratch directory location.  Once
     * this has been called, there will be a directory with the name that is returned,
     * or an exception will have been thrown.
     *
     * @param uniqueID ID for task.  May be 0.
     * @param prefix to prepend to the scratch directory name.
     * @param suffix to append to end of scratch directory name.
     * @return the directory.
     * @throws Exception thrown if non-dir file of same name exists, or for IOs.
     */
    public static File getScratchLocation(Long uniqueID, String prefix, String suffix) throws Exception {
        // Two chances to get a scratch base location.
        if (scratchBaseLocation == null)
            scratchBaseLocation = PropertyHelper.getInstance().getProperty(ScratchUtils.SCRATCH_DIR_PROP, (String)null);
        if (scratchBaseLocation == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Could not get property for ");
            buffer.append(ScratchUtils.SCRATCH_DIR_PROP);
            logger.error(buffer.toString());
            throw new Exception(buffer.toString());
        }

        // Ensure base location is created.  If on scratch, could get deleted between runs.
        File scratchBaseLocationFile = new File( scratchBaseLocation );
        if ( ! scratchBaseLocationFile.exists() ) {
            scratchBaseLocationFile.mkdirs();
        }

        // Now ensure that a subdirectory named for the uniqueID is available.
        // All scratch directories will be rooted by one directory for the increment ID.
        File testIncrementScratchDir = new File(scratchBaseLocation, uniqueID.toString());
        if (! testIncrementScratchDir.exists()) {
            testIncrementScratchDir.mkdir();
        }
        else if (! testIncrementScratchDir.isDirectory()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("While attempting to create directory ")
                  .append(testIncrementScratchDir.getAbsolutePath())
                  .append(" found that a non-directory file of that name already exists.");
            logger.error(buffer.toString());
            throw new Exception(buffer.toString());
        }

        // Make the scratch directory name.
        StringBuffer nameBuffer = new StringBuffer();
        nameBuffer.append(prefix);
        if (suffix != null) {
            nameBuffer.append("_")
                    .append(suffix);
        }
        File scratchDirFile = new File(testIncrementScratchDir,
                nameBuffer.toString());

        // Ensure that it exists and is a directory.
        if (! scratchDirFile.exists()) {
            logger.debug("Creating scratch directory " + scratchDirFile.getAbsolutePath());
            scratchDirFile.mkdir();
            logger.info("Created a scratch location of " + scratchDirFile.getAbsolutePath());
        }
        else if (! scratchDirFile.isDirectory()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Cannot create scratch directory.  A file called '")
                    .append(scratchDirFile.getAbsolutePath())
                    .append("' already exists, and is not a directory.");
            logger.error(buffer.toString());
            throw new Exception(buffer.toString());
        }

        return scratchDirFile;
    }
}