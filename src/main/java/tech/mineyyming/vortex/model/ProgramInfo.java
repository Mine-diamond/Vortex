package tech.mineyyming.vortex.model;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.nio.file.Path;


@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ProgramInfo {

    private String baseName;

    private String displayName;
    //对于普通应用是程序名，对UWP应用是aumid;
    private String programName;

    private String version;

    private String publisher;
    //安装位置
    private String installLocation;
    //Registry或UWP
    private ProgramSource source;

    private Path path = null;

    private Boolean enabled = true;

    private String id;

    public boolean equal(ProgramInfo other) {

        if(this.baseName.equals(other.baseName)
        && (this.version.equals(other.version) || (this.version == null && other.version == null))
        && (this.installLocation.equals(other.installLocation) || (this.installLocation == null && other.installLocation == null))
        && this.publisher.equals(other.publisher)){
            return true;
        }
        return false;
    }

}
