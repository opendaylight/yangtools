/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.validation.tool;

import java.io.IOException;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.opendaylight.yangtools.dagger.yang.parser.DaggerDefaultYangParserComponent;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {

    }

    public static void main(final String[] args) {
        final var parser = Params.getParser();
        final Params params = new Params();
        try {
            parser.parseArgs(args, params);
        } catch (final ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        final var files = params.listFiles();
        if (files == null) {
            return;
        }

        final var yangParser = DaggerDefaultYangParserComponent.create().parserFactory().createParser();
        for (var file : files) {
            try {
                yangParser.addSource(new FileYangTextSource(file.toPath()));
            } catch (IOException e) {
                LOG.error("Failed to read {}", file, e);
                return;
            } catch (YangSyntaxErrorException e) {
                LOG.error("Failed to parse {}", file, e);
                return;
            }
        }

        final EffectiveModelContext modelContext;
        try {
            modelContext = yangParser.buildEffectiveModel();
        } catch (YangParserException e) {
            LOG.error("YANG files could not be assembled", e);
            return;
        }

        LOG.info("{} YANG files resulted in {} modules", files.length, modelContext.getModuleStatements().size());
    }
}
