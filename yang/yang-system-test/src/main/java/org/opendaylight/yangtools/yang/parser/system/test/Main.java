/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.system.test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of Yang parser system test.
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_YANG_DIR_PATH = "./src/main/yang";

    public static void main(final String[] args) throws SourceException, FileNotFoundException, ReactorException,
            URISyntaxException {
        final String yangDirPath = args.length == 1 ? args[0] : DEFAULT_YANG_DIR_PATH;
        LOG.info("Yang models dir path: {} ", yangDirPath);
        SchemaContext context = null;
        try {
            context = YangParserUtils.parseYangSources(yangDirPath);
        } catch (final Exception e) {
            LOG.error("Failed to create SchemaContext.", e);
            System.exit(1);
        }
        LOG.info("SchemaContext resolved Successfully. {}", context);
    }
}
