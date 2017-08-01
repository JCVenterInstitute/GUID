package org.jtc.app_db_interface.guid.api.console;

import org.jtc.app_db_interface.guid.api.factory.GuidClientDBInterfaceFactory;

public class GuidClientConsole {
    static final String usage = " -s <desired block size>  [ -n <namespace> ]\n";

    public GuidClientConsole() {
    }

    public static void main(String[] args) throws Exception {
        String namespace = null;
        Long blockSize = null;
        if (args.length < 1) {
            System.err.println(" -s <desired block size>  [ -n <namespace> ]\n");
            System.exit(1);
        } else {
            try {
                for(int i = 0; i < args.length; ++i) {
                    if (args[i].equals("-n")) {
                        ++i;
                        namespace = args[i];
                    } else if (args[i].equals("-s")) {
                        ++i;
                        blockSize = new Long(args[i]);
                    }
                }

                GuidClientDBInterfaceFactory clientFactory = GuidClientDBInterfaceFactory.getInstance();
                long blockStart;
                if (namespace == null) {
                    blockStart = clientFactory.getGUIDBlock(blockSize.longValue());
                    System.out.println("Success. Starting value for GUID block: " + blockStart);
                } else {
                    blockStart = clientFactory.getGUIDBlock(blockSize.longValue(), namespace);
                    System.out.println("Success. Starting value for GUID block in namespace " + namespace + " is " + blockStart);
                }
            } catch (Throwable var6) {
                var6.printStackTrace();
                System.exit(1);
            }
        }

    }
}

