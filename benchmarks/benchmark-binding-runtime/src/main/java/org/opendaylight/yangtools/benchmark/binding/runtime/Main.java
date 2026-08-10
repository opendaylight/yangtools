/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.benchmark.binding.runtime;

import com.google.common.base.Stopwatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {
        // hidden on purpose
    }

    public static void main(final String[] args) throws InterruptedException, IOException {
        final var classes = new HashSet<Class<?>>();

        for (var arg : args) {
            classes.addAll(switch (arg) {
                case "openconfig" -> OpenConfig240119.classes();
                case "openroadm" -> OpenRoadm1311.classes();
                case "tapi" -> Tapi240.classes();
                default -> throw new IllegalArgumentException("Unknown model set '" + arg + "'");
            });
        }

        final var array = classes.toArray(Class<?>[]::new);
        LOG.info("Constructing BindingRuntimeContext from {} root models", array.length);

        final var sw = Stopwatch.createStarted();
        final var runtimeContext = BindingRuntimeHelpers.createRuntimeContext(array);
        LOG.info("BindingRuntimeContext created in {}, running GC", sw.stop());
        Runtime.getRuntime().freeMemory();

        LOG.info("Type any line to exit");
        new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset())).readLine();

        // holds down runtimeContext to prevent GC
        LOG.trace("Context was {}", runtimeContext);
    }
}
