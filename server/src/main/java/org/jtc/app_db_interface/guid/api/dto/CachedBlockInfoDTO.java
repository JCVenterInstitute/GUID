package org.jtc.app_db_interface.guid.api.dto;

import org.jtc.app_db_interface.guid.api.intf.CachedBlockInfo;

import java.io.Serializable;

public class CachedBlockInfoDTO implements Cloneable, Serializable, CachedBlockInfo {
    private String guidNamespace;
    private long blockSize;
    private long blockStartGuid;
    private long nextAvailableGuid;

    public CachedBlockInfoDTO(String guidNamespace, long blockSize, long blockStartGuid) {
        this.guidNamespace = guidNamespace;
        this.blockSize = blockSize;
        this.blockStartGuid = blockStartGuid;
        this.nextAvailableGuid = blockStartGuid;
        if (this.guidNamespace == null) {
            throw new IllegalArgumentException("CachedBlockInfo namespace cannot be null");
        } else if (this.blockSize < 0L) {
            throw new IllegalArgumentException("CachedBlockInfo block size must be >= 0");
        } else if (this.blockStartGuid < 0L) {
            throw new IllegalArgumentException("CachedBlockInfo block start guid must be >= 0");
        }
    }

    public long getBlockSize() {
        return this.blockSize;
    }

    public long getBlockStartGuid() {
        return this.blockStartGuid;
    }

    public String getGuidNamespace() {
        return this.guidNamespace;
    }

    public boolean hasSufficientGuidsForRequest(long requestSize) {
        if (requestSize <= 0L) {
            throw new IllegalArgumentException("CachedBlockInfo guid request size must be > 0");
        } else {
            return requestSize <= this.getRemainingCachedGuids();
        }
    }

    public long allocateGuids(long requestSize) {
        if (requestSize <= 0L) {
            throw new IllegalArgumentException("CachedBlockInfo guid request size must be > 0");
        } else {
            long startGuid = this.nextAvailableGuid;
            if (requestSize <= this.getRemainingCachedGuids()) {
                this.nextAvailableGuid += requestSize;
                return startGuid;
            } else {
                throw new NegativeArraySizeException("Cannot allocate " + requestSize + " guids from cache block; " + "cache contains only " + this.getRemainingCachedGuids() + " available guids");
            }
        }
    }

    public long getNumberOfGuidsAllocated() {
        return this.blockSize - this.getRemainingCachedGuids();
    }

    public long getRemainingCachedGuids() {
        return this.blockStartGuid + this.blockSize - this.nextAvailableGuid;
    }

    public boolean isFullyAllocated() {
        return this.getRemainingCachedGuids() <= 0L;
    }

    public long getNextAvaliableGuid() {
        return !this.isFullyAllocated() ? this.nextAvailableGuid : -1L;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            CachedBlockInfoDTO that = (CachedBlockInfoDTO)o;
            if (this.blockStartGuid != that.blockStartGuid) {
                return false;
            } else if (!this.guidNamespace.equals(that.guidNamespace)) {
                return false;
            } else if (this.nextAvailableGuid != that.nextAvailableGuid) {
                return false;
            } else {
                return this.blockSize == that.blockSize;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.guidNamespace.hashCode();
        result = 31 * result + (int)(this.blockSize ^ this.blockSize >>> 32);
        result = 31 * result + (int)(this.blockStartGuid ^ this.blockStartGuid >>> 32);
        result = 31 * result + (int)(this.nextAvailableGuid ^ this.nextAvailableGuid >>> 32);
        return result;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("Cached Guid Block:");
        builder.append("\n\tGuid namespace: " + this.guidNamespace);
        builder.append("\n\tBlock size: " + this.blockSize);
        builder.append("\n\tStart guid: " + this.blockStartGuid);
        builder.append("\n\tNumber of guids allocated: " + this.getNumberOfGuidsAllocated());
        builder.append("\n\tRemaining guids available: " + this.getRemainingCachedGuids());
        builder.append("\n\tNext available guid: " + this.getNextAvaliableGuid());
        return builder.toString();
    }
}

