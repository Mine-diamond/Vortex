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

package tech.minediamond.vortex.model.fileData;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
public class FileData {
    private FileType type;
    private String fileName;
    private String extension;
    private String fullPath;
    private long size;
    private Date dateModified;
    private Date dateCreated;

    public FileData(){

    }

    public FileData(FileType type, String fileName, String extension, String fullPath, long size, Date dateModified, Date dateCreated) {
        this.type = type;
        this.fileName = fileName;
        this.extension = extension;
        this.fullPath = fullPath;
        this.size = size;
        this.dateModified = dateModified;
        this.dateCreated = dateCreated;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(fullPath, ((FileData) o).fullPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullPath);
    }
}

