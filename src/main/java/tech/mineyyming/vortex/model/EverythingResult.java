package tech.mineyyming.vortex.model;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;

public class EverythingResult {
    private final ResultType type;
    private final String fileName;
    private final String extension;
    private final String fullPath;
    private final long size;
    private final Date dateModified;
    private final Date dateCreated;

    public EverythingResult(ResultType type, String fileName, String extension, String fullPath, long size, Date dateModified, Date dateCreated) {
        this.type = type;
        this.fileName = fileName;
        this.extension = extension;
        this.fullPath = fullPath;
        this.size = size;
        this.dateModified = dateModified;
        this.dateCreated = dateCreated;
    }

    public ResultType getType() { return type; }
    public String getFileName() { return fileName; }
    public String getExtension() { return extension; }
    public String getFullPath() { return fullPath; }
    public Path getPath() { return Paths.get(fullPath); }
    public long getSize() { return size; }
    public Date getDateModified() { return dateModified; }
    public Date getDateCreated() { return dateCreated; }

    @Override
    public String toString() {
        return "EverythingResult{" +
                "type=" + type +
                ", fileName='" + fileName + '\'' +
                ", fullPath='" + fullPath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(fullPath, ((EverythingResult) o).fullPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullPath);
    }
}

