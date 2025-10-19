/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.validation.tool;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

final class ParamsUtil {
    private ParamsUtil() {
        // Hidden on purpose
    }

    @SuppressWarnings("SystemExitOutsideMain")
    @SuppressFBWarnings(value = "DM_EXIT", justification = "We do expect to terminate the JVM")
    static Params parseArgs(final String[] args, final ArgumentParser parser) {
        final Params params = new Params();
        try {
            parser.parseArgs(args, params);
            return params;
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        System.exit(1);
        return null;
    }
}
