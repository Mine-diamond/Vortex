/*
 * Vortex
 * Copyright (C) 2025 Mine-diamond
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package tech.minediamond.vortex.program;

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
