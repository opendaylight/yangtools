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
import java.lang.ref.Reference;
import java.nio.charset.Charset;
import java.util.HashSet;
import org.opendaylight.yangtools.binding.generator.dagger.BindingRuntimeGeneratorModule;
import org.opendaylight.yangtools.binding.meta.RootMeta;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.dagger.yang.parser.DaggerDefaultYangParserComponent;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;

public final class Main {
    private Main() {
        // hidden on purpose
    }

    // console output
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    public static void main(final String[] args) throws InterruptedException, IOException, YangParserException {
        final var classes = new HashSet<RootMeta<?>>();

        for (var arg : args) {
            classes.addAll(switch (arg) {
                case "openroadm" -> OpenRoadm1311.classes();
                case "tapi" -> Tapi240.classes();
                default -> throw new IllegalArgumentException("Unknown model set '" + arg + "'");
            });
        }

        System.out.println("Constructing BindingRuntimeContext from " + classes.size() + " root models");
        System.out.println("Hit enter when ready");
        final var in = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
        in.readLine();

        final var sw = Stopwatch.createStarted();
        final var runtimeContext = BindingRuntimeHelpers.createRuntimeContext(
            DaggerDefaultYangParserComponent.create().parserFactory(),
            BindingRuntimeGeneratorModule.provideBindingRuntimeGenerator(),
            classes);
        System.out.println("BindingRuntimeContext created in " + sw.stop());

        System.out.println("Hit enter to run GC");
        in.readLine();
        Runtime.getRuntime().gc();

        System.out.println("Hit enter to exit");
        in.readLine();

        Reference.reachabilityFence(runtimeContext);
    }
}
