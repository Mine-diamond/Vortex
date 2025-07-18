package tech.mineyyming.vortex.service;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.ptr.LongByReference;
import tech.mineyyming.vortex.model.EverythingResult;
import tech.mineyyming.vortex.model.RequestFlag;
import tech.mineyyming.vortex.model.ResultType;
import tech.mineyyming.vortex.model.SearchMode;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class EverythingManager {

    private static final int MAX_PATH_LENGTH = 1024; // Increased buffer size for safety
    private final Everything lib = Everything.INSTANCE;

    // --- Query State ---
    private String query;
    private SearchMode searchMode = SearchMode.ALL;
    private Set<RequestFlag> requestFlags = new HashSet<>();
    private List<Path> targetFolders = Collections.emptyList();

    // --- Fluent API for Configuration ---

    public EverythingManager searchFor(String query) {
        this.query = query;
        return this;
    }

    public EverythingManager inFolders(Path... folders) {
        if (folders == null || folders.length == 0) {
            this.targetFolders = Collections.emptyList();
        } else {
            this.targetFolders = Arrays.stream(folders).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return this;
    }

    public EverythingManager mode(SearchMode mode) {
        this.searchMode = Objects.requireNonNull(mode, "Search mode cannot be null");
        return this;
    }

    public EverythingManager request(RequestFlag... flags) {
        this.requestFlags = new HashSet<>(Arrays.asList(flags));
        return this;
    }

    // --- Execution ---

    public List<EverythingResult> query() {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        lib.Everything_Reset();

        String finalQuery = buildFinalQuery();
        lib.Everything_SetSearchW(new WString(finalQuery));

        return executeQueryAndBuildResults();
    }

    // --- Private Helper Methods ---

    private String buildFinalQuery() {
        String pathQueryPart = buildPathQueryPart();
        String modeQueryPart = searchMode.getQueryPrefix();
        String finalQuery = pathQueryPart + modeQueryPart + this.query;
        System.out.println("DEBUG: Executing Everything Query -> " + finalQuery);
        return finalQuery;
    }

    private String buildPathQueryPart() {
        if (targetFolders.isEmpty()) {
            return "";
        }
        String joinedPaths = targetFolders.stream()
                .map(Path::toString)
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining("|"));
        return "path:" + joinedPaths + " ";
    }

    private List<EverythingResult> executeQueryAndBuildResults() {
        Set<RequestFlag> finalFlags = EnumSet.of(
                RequestFlag.FULL_PATH_AND_FILE_NAME,
                RequestFlag.FILE_NAME,
                RequestFlag.EXTENSION,
                RequestFlag.ATTRIBUTES
        );
        finalFlags.addAll(this.requestFlags);

        int flagsValue = finalFlags.stream().mapToInt(RequestFlag::getValue).reduce(0, (a, b) -> a | b);
        lib.Everything_SetRequestFlags(flagsValue);

        if (!lib.Everything_QueryW(true)) {
            throw new RuntimeException("Everything query failed. Is the Everything service running?");
        }

        int numResults = lib.Everything_GetNumResults();
        List<EverythingResult> results = new ArrayList<>(numResults);

        char[] pathBuffer = new char[MAX_PATH_LENGTH];
        char[] fileNameBuffer = new char[MAX_PATH_LENGTH];

        for (int i = 0; i < numResults; i++) {
            lib.Everything_GetResultFullPathNameW(i, pathBuffer, pathBuffer.length);
            String fullPath = Native.toString(pathBuffer);

            lib.Everything_GetResultFullPathNameW(i, fileNameBuffer, fileNameBuffer.length);
            String fileName = Native.toString(fileNameBuffer);

            WString wExtension = lib.Everything_GetResultExtensionW(i);
            String extension = (wExtension != null) ? wExtension.toString() : "";

            int attributes = lib.Everything_GetResultAttributes(i);
            ResultType type = ((attributes & Everything.FILE_ATTRIBUTE_DIRECTORY) != 0) ? ResultType.FOLDER : ResultType.FILE;

            long size = 0;
            if (finalFlags.contains(RequestFlag.SIZE)) {
                LongByReference sizeRef = new LongByReference();
                if (lib.Everything_GetResultSize(i, sizeRef)) size = sizeRef.getValue();
            }

            Date dateModified = null;
            if (finalFlags.contains(RequestFlag.DATE_MODIFIED)) {
                Everything.FILETIME ft = new Everything.FILETIME();
                if (lib.Everything_GetResultDateModified(i, ft)) dateModified = new Date(ft.toJavaTime());
            }

            Date dateCreated = null;
            if (finalFlags.contains(RequestFlag.DATE_CREATED)) {
                Everything.FILETIME ft = new Everything.FILETIME();
                if (lib.Everything_GetResultDateCreated(i, ft)) dateCreated = new Date(ft.toJavaTime());
            }

            results.add(new EverythingResult(type, fileName, extension, fullPath, size, dateModified, dateCreated));
        }
        return results;
    }

}

