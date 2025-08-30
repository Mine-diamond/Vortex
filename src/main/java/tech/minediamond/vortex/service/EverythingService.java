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

package tech.minediamond.vortex.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import tech.minediamond.vortex.model.EverythingQuery;
import tech.minediamond.vortex.model.EverythingResult;
import tech.minediamond.vortex.service.interfaces.Everything;

import java.util.List;

@Singleton
public class EverythingService {

    @Inject
    public EverythingService() {

    }

    private static final int MAX_PATH_LENGTH = 1024; // Increased buffer size for safety
    private final Everything lib = Everything.INSTANCE;

    public EverythingQueryBuilder newBuilder() {
        return new EverythingQueryBuilder(this);
    }

    public List<EverythingResult> query(EverythingQuery query) {
        return null;
    }

}
