package org.jtc.common.util.platform_execution;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Date;
import java.util.concurrent.CyclicBarrier;

/**
 * $LastChangedBy: lfoster $:
 * $LastChangedDate: 2011-08-02 12:18:43 -0700 (Tue, 02 Aug 2011) $:
 * $Revision: 32262 $:
 * $HeadURL: http://svn.jcvi.org/SE/LIMS/branches/LIMSMigrationToMySQL/common/utilities/main/src/java/org/jtc/common/util/platform_execution/PlatformCommandExecution.java $:
 * <p/>
 * Description: Execute a command on platform (ie, Unix/Linux). NOTE: formerly in GSFLX Server module.
 */
public class PlatformCommandExecution {

    private static final String LINUX_BAT_EXT = ".sh";
    private static final String WIN_BAT_EXT = ".bat";
    private static final String OS_NAME_SYS_PROP = "os.name";
    private static final String LINUX_OS_IDENT = "linux";

    private static Logger logger = Logger.getLogger(PlatformCommandExecution.class);

    //-------------------------------------------Execute overloads: allow user to supply command and 0/1/2 handlers.
    /**
     * Execution method.  Runs a command on the platform.
     */
    public static void execute(String command) throws Exception {
        // Execute and follow-up the process.
        Process process = Runtime.getRuntime().exec(command);

        // Capture all the output chatter on both output streams.
        BufferGrabInputStreamHandler errHandler = new BufferGrabInputStreamHandler();
        InputStreamHandlerThread stdErrThread = new InputStreamHandlerThread( errHandler, process.getErrorStream() );

        BufferGrabInputStreamHandler outputHandler = new BufferGrabInputStreamHandler();
        InputStreamHandlerThread stdOutThread = new InputStreamHandlerThread( outputHandler, process.getInputStream() );

        CyclicBarrier barrier = new CyclicBarrier( 3 );
        stdErrThread.setCyclicBarrier( barrier );
        stdOutThread.setCyclicBarrier(barrier);

        //   need to use separate thread/each handler to keep them from awaiting each other.
        stdErrThread.start();
        stdOutThread.start();

        int commandResult = process.waitFor();

        // Must await barrier here, to ensure this process does not move on before all output issued.
        if ( ! barrier.isBroken() ) {
            barrier.await();
        }

        StringBuffer errorBuf = errHandler.getResult();
        StringBuffer stdoutBuf = outputHandler.getResult();

        if ( errorBuf.length() > 0 || commandResult != 0 ||
             stdErrThread.getException() != null || stdOutThread.getException() != null ) {

            logger.error("Received this error output from " + command + " {" + errorBuf.toString() + "}");
            logger.error("Received this stdout output from " + command + " {" + stdoutBuf.toString() + "}");
            logger.error("Received - if non null - stderr read exception from " + command + " " +
                    stdErrThread.getException() );
            logger.error("Received - if non null - stdout read exception from " + command + " " +
                    stdOutThread.getException() );

            throw new Exception("Process [" + command + "] failed with error " + commandResult);

        }

    }

    /**
     * Execution method.  Runs a command on the platform.  Feed its stdout to the handler provided.
     */
    public static void execute(String command, InputStreamHandler outHandler) throws Exception {
        // Execute and follow-up the process.
        Process process = Runtime.getRuntime().exec(command);

        // Capture all the output chatter on stderr output streams.
        BufferGrabInputStreamHandler errHandler = new BufferGrabInputStreamHandler();

        InputStreamHandlerThread stdErrThread = new InputStreamHandlerThread( errHandler, process.getErrorStream() );
        InputStreamHandlerThread stdOutThread = new InputStreamHandlerThread( outHandler, process.getInputStream() );

        CyclicBarrier barrier = new CyclicBarrier( 3 );
        stdErrThread.setCyclicBarrier( barrier );
        stdOutThread.setCyclicBarrier(barrier);

        stdErrThread.start();
        stdOutThread.start();

        int commandResult = process.waitFor();

        // Must await barrier here, to ensure this process does not move on before all output issued.
        if ( ! barrier.isBroken() ) {
            barrier.await();
        }

        StringBuffer errorBuf = errHandler.getResult();

        if ( commandResult != 0   ||  stdErrThread.getException() != null  ||  stdOutThread.getException() != null ) {
            logger.error("Received this error code from " + command + " " + commandResult +
                         ", stderr exception: " + stdErrThread.getException() +
                         ", stdout exception: " + stdOutThread.getException() );
            throw new Exception("Process [" + command + "] failed with error " + commandResult + " or exception.");
        }

    }

    /**
     * Execution method.  Runs a command on the platform.  Feed its stdout and stderr to the handlers provided.
     * Waits for all output to stdout and stderr from the executed process to complete.  Synchronizes this
     * using Cyclic Barrier.
     */
    public static void execute(String command, InputStreamHandler stdOutHandler, InputStreamHandler stdErrHandler)
            throws Exception {

        // Execute and follow-up the process.
        Process process = Runtime.getRuntime().exec(command);

        // Capture all the output chatter from streams.
        CyclicBarrier barrier = new CyclicBarrier( 3 );
        InputStreamHandlerThread stdErrThread = new InputStreamHandlerThread( stdErrHandler, process.getErrorStream() );
        stdErrThread.setCyclicBarrier( barrier );
        InputStreamHandlerThread stdOutThread = new InputStreamHandlerThread( stdOutHandler, process.getInputStream() );
        stdOutThread.setCyclicBarrier(barrier);

        //   need to use separate thread/each handler to keep them from awaiting each other.
        stdErrThread.start();
        stdOutThread.start();

        int commandResult = process.waitFor();

        // Must await barrier here, to ensure this process does not move on before all output issued.
        if ( ! barrier.isBroken() ) {
            barrier.await();
        }

        if ( commandResult != 0   ||  stdErrThread.getException() != null  ||  stdOutThread.getException() != null ) {
            logger.error("Received this error code from " + command + " " + commandResult +
                         ", stderr exception: " + stdErrThread.getException() +
                         ", stdout exception: " + stdOutThread.getException() );
            throw new Exception("Process [" + command + "] failed with error " + commandResult + " or exception.");
        }

    }

    /**
     * Execution method.  Runs a command on the platform.  Feed its stdout and stderr to the handlers provided.
     * THIS version uses joins instead of cyclic barrier.
     */
    public static void executeJoined(String command, InputStreamHandler stdOutHandler, InputStreamHandler stdErrHandler)
            throws Exception {

        // Execute and follow-up the process.
        Process process = Runtime.getRuntime().exec(command);

        // Capture all the output chatter from streams.
        InputStreamHandlerThread stdErrThread = new InputStreamHandlerThread( stdErrHandler, process.getErrorStream() );
        InputStreamHandlerThread stdOutThread = new InputStreamHandlerThread( stdOutHandler, process.getInputStream() );

        //   need to use separate thread/each handler to keep them from awaiting each other.
        stdErrThread.start();
        stdOutThread.start();

        stdOutThread.join();
        stdErrThread.join();

        int commandResult = process.waitFor();

        if ( commandResult != 0   ||  stdErrThread.getException() != null  ||  stdOutThread.getException() != null ) {
            logger.error("Received this error code from " + command + " " + commandResult +
                         ", stderr exception: " + stdErrThread.getException() +
                         ", stdout exception: " + stdOutThread.getException() );
            throw new Exception("Process [" + command + "] failed with error " + commandResult + " or exception.");
        }

    }

    /**
     * Execution method.  Runs a command on the platform.  Feed its stdout and stderr to the handlers provided.
     * This version captures streams as requested, but does not wait for their completion.  No join, and no
     * cyclic barrier.
     */
    public static void executeIgnoredStreams(
            String command, InputStreamHandler stdOutHandler, InputStreamHandler stdErrHandler)
            throws Exception {

        // Execute and follow-up the process.
        Process process = Runtime.getRuntime().exec(command);

        // Capture all the output chatter from streams.
        InputStreamHandlerThread stdErrThread = new InputStreamHandlerThread( stdErrHandler, process.getErrorStream() );
        InputStreamHandlerThread stdOutThread = new InputStreamHandlerThread( stdOutHandler, process.getInputStream() );

        //   need to use separate thread/each handler to keep them from awaiting each other.
        stdErrThread.start();
        stdOutThread.start();

        int commandResult = process.waitFor();

        if ( commandResult != 0   ||  stdErrThread.getException() != null  ||  stdOutThread.getException() != null ) {
            logger.error("Received this error code from " + command + " " + commandResult +
                         ", stderr exception: " + stdErrThread.getException() +
                         ", stdout exception: " + stdOutThread.getException() );
            throw new Exception("Process [" + command + "] failed with error " + commandResult + " or exception.");
        }

    }

    /**
     * Execution method.  Runs a command on the platform.  Feed its stdout and stderr to the handlers provided.
     * THIS version will send stderr and stdout to redirected output locations.  This will cause the handlers
     * to have nothing to do.
     */
    public static void executeBypassedStreams(
            String command, InputStreamHandler stdOutHandler, InputStreamHandler stdErrHandler, File scratchDirectory)
            throws Exception {

        // Execute and follow-up the process.
        File logFile = File.createTempFile(
                "__" + PlatformCommandExecution.class.getName(), ".test_log", scratchDirectory);

        File newCommandFile = PlatformCommandExecution.wrapCommandInFile(
                scratchDirectory,
                command + " 2>&1 >" + logFile.getAbsolutePath(),
                PlatformCommandExecution.class );

        logger.info( "Creating command " + newCommandFile.toString() );
        logger.info( "Writing to log " + logFile.toString() );

        Process process = Runtime.getRuntime().exec( newCommandFile.getAbsolutePath() );

        // Capture all the output chatter from streams.
        InputStreamHandlerThread stdErrThread = new InputStreamHandlerThread( stdErrHandler, process.getErrorStream() );
        InputStreamHandlerThread stdOutThread = new InputStreamHandlerThread( stdOutHandler, process.getInputStream() );

        //   need to use separate thread/each handler to keep them from awaiting each other.
        stdErrThread.start();
        stdOutThread.start();

        int commandResult = process.waitFor();

        if ( commandResult != 0   ||  stdErrThread.getException() != null  ||  stdOutThread.getException() != null ) {
            logger.error("Received this error code from " + command + " " + commandResult +
                         ", stderr exception: " + stdErrThread.getException() +
                         ", stdout exception: " + stdOutThread.getException() );
            throw new Exception("Process [" + command + "] failed with error " + commandResult + " or exception.");
        }

    }

    //--------------------------------------------------Wrap a command to avoid stumbling over parameters.
    /**
     * Move a command into a script file, suitable for execution.
     *
     * @param outputLocation where to place the command.
     * @param command what to put in script.
     * @param clazz what class to name the script after.
     * @return file pointing to script.
     * @throws Exception thrown by any called methods.
     */
    public static File wrapCommandInFile(File outputLocation, String command, Class clazz) throws Exception {
        boolean isLinux = System.getProperty( OS_NAME_SYS_PROP ).toLowerCase().indexOf( LINUX_OS_IDENT ) > -1;
        String extension = isLinux ? LINUX_BAT_EXT : WIN_BAT_EXT;
        File tempCmdFile = File.createTempFile("temp_" + clazz.getName(), extension, outputLocation);
        PrintWriter writer = new PrintWriter(new FileWriter(tempCmdFile));
        writer.println(command);
        writer.close();
        // Linux platform requires chmod to execute the script.
        if (isLinux) {
            PlatformCommandExecution.execute("chmod +x " + tempCmdFile.getAbsolutePath());
        }
        return tempCmdFile;
    }

    //------------------------------------------------INNER CLASSES and INTERFACES
    /**
     * Implement this interface, to make a class that can handle output coming off a process' stdout or stderr.
     */
    public static interface InputStreamHandler {
        void handleInputStream(InputStream is);
    }

    /** Implementation of input stream handler, which just captures all output to a buffer. */
    public static class BufferGrabInputStreamHandler implements InputStreamHandler {
        private StringBuffer resultBuf = new StringBuffer();
        private Exception cachedException;

        public void handleInputStream( InputStream is ) {
            BufferedReader rdr = null;
            try {
                rdr = new BufferedReader(new InputStreamReader( is ));
                String inLine;
                while (null != (inLine = rdr.readLine())  &&  (! Thread.interrupted())) {
                    resultBuf.append("[");
                    resultBuf.append(inLine);
                    resultBuf.append("] ");
                }

                if ( Thread.interrupted() )
                    throw new InterruptedException();

            } catch (Exception ex) {
                cachedException = ex;
                resultBuf.append( "[[ Exception thrown during InputStream read: " + ex.getMessage() + " ]]");
            } finally {
                if ( rdr != null ) {
                    try {
                        rdr.close();
                    } catch ( IOException ex ) {
                        logger.warn( "Failed to close a reader while grabbing output buffer." );
                    }
                }
            }
        }

        public StringBuffer getResult() { return resultBuf; }
        public Exception getException() { return cachedException; }
    }

    /**
     * Will use these threads to handle streams.
     */
    static class InputStreamHandlerThread extends Thread {
        private static int sThreadCount = 0;
        private InputStreamHandler handler;
        private InputStream stream;
        private CyclicBarrier barrier;
        private Exception cachedException;

        /* construct with the handler. */
        public InputStreamHandlerThread( InputStreamHandler handler, InputStream stream ) {
            // Name need only be unique within this JVM.
            super.setName( "InputStreamHandlerThread__" + sThreadCount ++ );
            this.handler = handler;
            this.stream = stream;
        }

        public void setCyclicBarrier( CyclicBarrier barrier ) {
            this.barrier = barrier;
        }

        public Exception getException() {
            return cachedException;
        }

        /** Runs in the thread to handle the stream, so multiple input streams may be handled. */
        @Override
        public void run() {
            try {
                handler.handleInputStream( stream );

                // Wait for all threads in this group to complete.
                if ( barrier != null  &&  ! barrier.isBroken() ) {
                    barrier.await();
                }

            } catch (Exception ex) {
                // Log any message.  InputStreamHandlers should not throw exceptions, but if they do,
                // here stands ready.
                logger.error( "Failed to complete handling of input stream: " + ex.getMessage() );
                cachedException = ex;
            }
        }
    }
}

