/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.parserperf;

import com.google.common.base.Stopwatch;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {
        // Hidden
    }

    public static void main(final String[] args) throws Exception {
        final String dir = args[0];
        final Runtime rt = Runtime.getRuntime();
        LOG.info("Start warmup on {}", dir);
        parse(dir);
        rt.gc();

        LOG.info("Start real run on {}", dir);
        EffectiveModelContext ctx = parse(dir);
        rt.gc();
        final double total = rt.totalMemory() / 1048576.0;
        final double free = rt.freeMemory() / 1048576.0;
        LOG.info("Total {}MiB free {}MiB used {}MiB", total, free, total - free);

        // This is just to retain ctx
        LOG.trace("Context is {}", ctx);
    }

    private static EffectiveModelContext parse(final String directory) throws Exception {
        final YangParser parser = ServiceLoader.load(YangParserFactory.class).findFirst().orElseThrow().createParser();
        final Stopwatch sw = Stopwatch.createStarted();
        for (Path path : Files.list(Paths.get(directory)).collect(Collectors.toList())) {
            parser.addSource(YangTextSchemaSource.forFile(path.toFile()));
        }
        final EffectiveModelContext ctx = parser.buildEffectiveModel();
        LOG.info("Parsed {} in {}", directory, sw);
        return ctx;
    }
}
