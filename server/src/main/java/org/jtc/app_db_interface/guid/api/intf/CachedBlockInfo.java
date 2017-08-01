package org.jtc.app_db_interface.guid.api.intf;

public interface CachedBlockInfo {
    String getGuidNamespace();

    long getBlockSize();

    long getBlockStartGuid();

    long getNumberOfGuidsAllocated();

    long getRemainingCachedGuids();

    boolean isFullyAllocated();

    long getNextAvaliableGuid();
}
