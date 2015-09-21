/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.validation.tool.retest;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

final class ParamsUtil {
    private ParamsUtil() {

    }

    public static Params parseArgs(final String[] args, final ArgumentParser parser) {
        final Params params = new Params();
        try {
            parser.parseArgs(args, params);
            return params;
        } catch (final ArgumentParserException e) {
            parser.handleError(e);
        }
        System.exit(1);
        return null;
    }
}
