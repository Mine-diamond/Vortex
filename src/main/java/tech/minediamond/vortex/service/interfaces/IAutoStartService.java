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

package tech.minediamond.vortex.service.interfaces;

/**
 * An interface for managing the application's auto-start functionality.
 * This abstracts the underlying implementation, allowing for different behaviors
 * in development vs. production environments.
 */
public interface IAutoStartService {

    /**
     * Enables the application to start automatically on system boot.
     */
    void enableAutoStart();

    /**
     * Disables the application from starting automatically on system boot.
     */
    void disableAutoStart();

    /**
     * Checks if auto-start is currently enabled.
     * @return true if auto-start is enabled, false otherwise.
     */
    boolean isAutoStartEnabled();
}
