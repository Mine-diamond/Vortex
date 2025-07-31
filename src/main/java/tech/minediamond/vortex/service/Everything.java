package tech.minediamond.vortex.service;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.ptr.LongByReference;

/**
 * JNA interface mapping for Everything's C SDK (Everything.dll).
 * Maps the native functions and structures required for searching.
 */
public interface Everything extends Library {

    Everything INSTANCE = Native.load("Everything64", Everything.class);

    // --- Windows API Constants ---
    int FILE_ATTRIBUTE_DIRECTORY = 0x00000010;

    // --- Core Functions ---
    void Everything_SetSearchW(WString searchString);
    void Everything_SetRequestFlags(int dwRequestFlags);
    void Everything_SetSort(int dwSortType);
    void Everything_SetMatchCase(boolean bEnable);
    void Everything_SetMatchWholeWord(boolean bEnable);
    void Everything_SetMatchPath(boolean bEnable);
    boolean Everything_QueryW(boolean bWait);
    int Everything_GetNumResults();
    void Everything_Reset();

    // --- Result Retrieval Functions ---
    void Everything_GetResultFullPathNameW(int nIndex, char[] lpString, int nMaxCount);
    void Everything_GetResultFileNameW(int nIndex, char[] lpString, int nMaxCount);
    WString Everything_GetResultExtensionW(int nIndex);
    int Everything_GetResultAttributes(int nIndex);
    boolean Everything_GetResultSize(int nIndex, LongByReference lpFileSize);
    boolean Everything_GetResultDateModified(int nIndex, FILETIME lpFileTime);
    boolean Everything_GetResultDateCreated(int nIndex, FILETIME lpFileTime);

    /**
     * JNA mapping for the Windows FILETIME structure.
     */
    @Structure.FieldOrder({"dwLowDateTime", "dwHighDateTime"})
    class FILETIME extends Structure {
        public int dwLowDateTime;
        public int dwHighDateTime;

        /**
         * Converts the Windows FILETIME to a Java-compatible time (milliseconds since epoch).
         * @return The time in milliseconds since 1970-01-01 UTC.
         */
        public long toJavaTime() {
            long time = ((long) dwHighDateTime << 32) | (dwLowDateTime & 0xFFFFFFFFL);
            final long NANO_SECONDS_BETWEEN_EPOCHS = 116444736000000000L;
            return (time - NANO_SECONDS_BETWEEN_EPOCHS) / 10000;
        }
    }
}



