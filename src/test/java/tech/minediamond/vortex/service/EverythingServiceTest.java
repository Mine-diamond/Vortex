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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import tech.minediamond.vortex.config.AppModule;
import tech.minediamond.vortex.model.EverythingResult;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EverythingServiceTest {
    EverythingService service;

    @Inject
    public EverythingServiceTest(EverythingService service) {
        this.service = service;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Injector injector = Guice.createInjector(new AppModule());
        EverythingServiceTest everythingServiceTest =injector.getInstance(EverythingServiceTest.class);
        //everythingServiceTest.service.StartEverything();//不需要
        TimeUnit.SECONDS.sleep(10);
        List<EverythingResult> results = everythingServiceTest.service.QueryBuilder()
                .searchFor("ChatGPT Image 2025年3月30日 23_17_56.png")
                .query();
        System.out.println(results);
    }
}
