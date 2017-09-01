/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.validation.tool;

import java.io.File;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {

    }

    public static void main(final String[] args) throws URISyntaxException {
        final Params params = ParamsUtil.parseArgs(args, Params.getParser());

        if (params.isValid()) {
            final File[] yangModels = params.getYangSourceDir().listFiles();

            try {
                YangParserTestUtils.parseYangFiles(yangModels);
            } catch (Exception e) {
                LOG.error("Yang files could not be parsed.", e);
            }
        }
    }
}
