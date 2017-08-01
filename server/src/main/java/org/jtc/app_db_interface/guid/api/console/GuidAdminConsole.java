package org.jtc.app_db_interface.guid.api.console;

import java.util.Iterator;
import java.util.List;
import org.jtc.app_db_interface.guid.api.factory.GuidAdminDBInterfaceFactory;
import org.jtc.app_db_interface.guid.api.intf.GUIDBlock;
import org.jtc.app_db_interface.guid.api.intf.GUIDNamespace;

public class GuidAdminConsole {
    static final String usage = "\n\nThis class provides several command-line functions that directly access the GUIDBlockServer database:\n\n   -c <namespace> <comment>   creates a new namespace if it does not already exist\n   -a                         downloads the entire GUIDBlockTable and prints it out\n   -N                         downloads a list of all valid Namespaces\n   -g <guid value>            queries the GUIDBlockTable for the entry associated with a given GUID\n   -n <namespace>             queries the GUIDBlockTable for a list of entries associated with a given namespace\n   -b <namespace> <size> <comment>  gets a new block from the given namespace of the specified size and prints to command line\n            Example: -b CLIENT_TEST 1000000000 \"This is a test of the CLIENT_TEST namespace\"\n   -p <string>                ping the server - it should return the string you send\n\n";

    public GuidAdminConsole() {
    }

    public static void main(String[] args) throws Exception {
        try {
            if (args.length < 1) {
                System.err.println("\n\nThis class provides several command-line functions that directly access the GUIDBlockServer database:\n\n   -c <namespace> <comment>   creates a new namespace if it does not already exist\n   -a                         downloads the entire GUIDBlockTable and prints it out\n   -N                         downloads a list of all valid Namespaces\n   -g <guid value>            queries the GUIDBlockTable for the entry associated with a given GUID\n   -n <namespace>             queries the GUIDBlockTable for a list of entries associated with a given namespace\n   -b <namespace> <size> <comment>  gets a new block from the given namespace of the specified size and prints to command line\n            Example: -b CLIENT_TEST 1000000000 \"This is a test of the CLIENT_TEST namespace\"\n   -p <string>                ping the server - it should return the string you send\n\n");
                System.exit(1);
            } else if (args[0].equals("-c")) {
                if (args.length < 3) {
                    System.out.println("\n\nThis class provides several command-line functions that directly access the GUIDBlockServer database:\n\n   -c <namespace> <comment>   creates a new namespace if it does not already exist\n   -a                         downloads the entire GUIDBlockTable and prints it out\n   -N                         downloads a list of all valid Namespaces\n   -g <guid value>            queries the GUIDBlockTable for the entry associated with a given GUID\n   -n <namespace>             queries the GUIDBlockTable for a list of entries associated with a given namespace\n   -b <namespace> <size> <comment>  gets a new block from the given namespace of the specified size and prints to command line\n            Example: -b CLIENT_TEST 1000000000 \"This is a test of the CLIENT_TEST namespace\"\n   -p <string>                ping the server - it should return the string you send\n\n");
                    return;
                }

                createGUIDNamespace(args[1], args[2]);
            } else if (args[0].equals("-a")) {
                getGUIDAllocationInfoAll();
            } else if (args[0].equals("-N")) {
                getGUIDNamespaceInfoAll();
            } else if (args[0].equals("-g")) {
                if (args.length < 2) {
                    System.out.println("\n\nThis class provides several command-line functions that directly access the GUIDBlockServer database:\n\n   -c <namespace> <comment>   creates a new namespace if it does not already exist\n   -a                         downloads the entire GUIDBlockTable and prints it out\n   -N                         downloads a list of all valid Namespaces\n   -g <guid value>            queries the GUIDBlockTable for the entry associated with a given GUID\n   -n <namespace>             queries the GUIDBlockTable for a list of entries associated with a given namespace\n   -b <namespace> <size> <comment>  gets a new block from the given namespace of the specified size and prints to command line\n            Example: -b CLIENT_TEST 1000000000 \"This is a test of the CLIENT_TEST namespace\"\n   -p <string>                ping the server - it should return the string you send\n\n");
                    return;
                }

                getGUIDAllocationInfoByGUID(Long.parseLong(args[1]));
            } else if (args[0].equals("-n")) {
                if (args.length < 2) {
                    System.out.println("\n\nThis class provides several command-line functions that directly access the GUIDBlockServer database:\n\n   -c <namespace> <comment>   creates a new namespace if it does not already exist\n   -a                         downloads the entire GUIDBlockTable and prints it out\n   -N                         downloads a list of all valid Namespaces\n   -g <guid value>            queries the GUIDBlockTable for the entry associated with a given GUID\n   -n <namespace>             queries the GUIDBlockTable for a list of entries associated with a given namespace\n   -b <namespace> <size> <comment>  gets a new block from the given namespace of the specified size and prints to command line\n            Example: -b CLIENT_TEST 1000000000 \"This is a test of the CLIENT_TEST namespace\"\n   -p <string>                ping the server - it should return the string you send\n\n");
                    return;
                }

                getGUIDAllocationInfoByNamespace(args[1]);
            } else if (args[0].equals("-b")) {
                if (args.length < 4) {
                    System.out.println("\n\nThis class provides several command-line functions that directly access the GUIDBlockServer database:\n\n   -c <namespace> <comment>   creates a new namespace if it does not already exist\n   -a                         downloads the entire GUIDBlockTable and prints it out\n   -N                         downloads a list of all valid Namespaces\n   -g <guid value>            queries the GUIDBlockTable for the entry associated with a given GUID\n   -n <namespace>             queries the GUIDBlockTable for a list of entries associated with a given namespace\n   -b <namespace> <size> <comment>  gets a new block from the given namespace of the specified size and prints to command line\n            Example: -b CLIENT_TEST 1000000000 \"This is a test of the CLIENT_TEST namespace\"\n   -p <string>                ping the server - it should return the string you send\n\n");
                    return;
                }

                getGUIDBlock(args[1], Long.parseLong(args[2]), args[3]);
            } else if (args[0].equals("-p")) {
                ping(args[1]);
            }

        } catch (Throwable var2) {
            var2.printStackTrace();
        }
    }

    private static void createGUIDNamespace(String nameString, String comment) {
        try {
            GuidAdminDBInterfaceFactory adminFactory = GuidAdminDBInterfaceFactory.getInstance();
            String response = adminFactory.createGUIDNamespace(nameString, comment);
            System.out.println("Completed with no errors. Response from server: " + response);
        } catch (Exception var4) {
            System.err.println("Exception thrown: " + var4);
            var4.printStackTrace();
        }

    }

    private static void getGUIDAllocationInfoAll() {
        try {
            GuidAdminDBInterfaceFactory adminFactory = GuidAdminDBInterfaceFactory.getInstance();
            GUIDBlock[] blockList = adminFactory.getGUIDAllocationInfoAll();
            System.out.println("NAMESPACE\tDATE\tSIZE\tFIRST\tLAST\tCOMMENT");

            for(int i = 0; i < blockList.length; ++i) {
                System.out.println(blockList[i].getNamespace() + "\t" + blockList[i].getDate() + "\t" + blockList[i].getSize() + "\t" + blockList[i].getFirstGUID() + "\t" + blockList[i].getLastGUID() + "\t" + blockList[i].getComment());
            }
        } catch (Exception var3) {
            System.err.println("Exception thrown: " + var3);
            var3.printStackTrace();
        }

    }

    private static void getGUIDNamespaceInfoAll() {
        try {
            GuidAdminDBInterfaceFactory adminFactory = GuidAdminDBInterfaceFactory.getInstance();
            List<GUIDNamespace> namespaceList = adminFactory.getGUIDNamespaceInfoAll();
            System.out.println("GUID NAMESPACES\n");
            Iterator i$ = namespaceList.iterator();

            while(i$.hasNext()) {
                GUIDNamespace guidNamespace = (GUIDNamespace)i$.next();
                System.out.println(guidNamespace);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    private static void getGUIDAllocationInfoByGUID(long guidValue) {
        try {
            GuidAdminDBInterfaceFactory adminFactory = GuidAdminDBInterfaceFactory.getInstance();
            GUIDBlock guidBlock = adminFactory.getGUIDAllocationInfoByGUID(guidValue);
            System.out.println(" namespace:   " + guidBlock.getNamespace());
            System.out.println(" date :       " + guidBlock.getDate());
            System.out.println(" size:        " + guidBlock.getSize());
            System.out.println(" firstGUID :  " + guidBlock.getFirstGUID());
            System.out.println(" lastGUID:    " + guidBlock.getLastGUID());
            System.out.println(" comment :    " + guidBlock.getComment());
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    private static void getGUIDAllocationInfoByNamespace(String nameString) {
        try {
            GuidAdminDBInterfaceFactory adminFactory = GuidAdminDBInterfaceFactory.getInstance();
            GUIDBlock[] guidBlockList = adminFactory.getGUIDAllocationInfoByNamespace(nameString);
            System.out.println("NAMESPACE\tDATE\tSIZE\tFIRST\tLAST\tCOMMENT");

            for(int i = 0; i < guidBlockList.length; ++i) {
                System.out.println(guidBlockList[i].getNamespace() + "\t" + guidBlockList[i].getDate() + "\t" + guidBlockList[i].getSize() + "\t" + guidBlockList[i].getFirstGUID() + "\t" + guidBlockList[i].getLastGUID() + "\t" + guidBlockList[i].getComment());
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    private static void getGUIDBlock(String nameString, long requestSize, String comment) {
        try {
            GuidAdminDBInterfaceFactory adminFactory = GuidAdminDBInterfaceFactory.getInstance();
            long blockStart = adminFactory.getGUIDBlock(nameString, requestSize, comment);
            System.out.println("Request approved. Starting GUID for block is: " + blockStart);
        } catch (Exception var7) {
            System.err.println("Request failed. Exception: " + var7);
            var7.printStackTrace();
        }

    }

    private static void ping(String message) {
        try {
            GuidAdminDBInterfaceFactory adminFactory = GuidAdminDBInterfaceFactory.getInstance();
            String reply = adminFactory.ping(message);
            System.out.println("Message back from server: " + reply);
        } catch (Exception var3) {
            System.err.println("ping FAILED. Exception: " + var3);
            var3.printStackTrace();
        }

    }
}
