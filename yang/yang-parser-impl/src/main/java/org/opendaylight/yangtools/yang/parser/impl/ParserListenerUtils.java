/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.base.Optional;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class ParserListenerUtils {

    private ParserListenerUtils() {
    }


    /**
     * Check this base type.
     *
     * @param typeName
     *            base YANG type name
     * @param moduleName
     *            name of current module
     * @param line
     *            line in module
     * @throws YangParseException
     *             if this is one of YANG type which MUST contain additional
     *             informations in its body
     */
    public static void checkMissingBody(final String typeName, final String moduleName, final int line) {
        switch (typeName) {
        case "decimal64":
            throw new YangParseException(moduleName, line,
                    "The 'fraction-digits' statement MUST be present if the type is 'decimal64'.");
        case "identityref":
            throw new YangParseException(moduleName, line,
                    "The 'base' statement MUST be present if the type is 'identityref'.");
        case "leafref":
            throw new YangParseException(moduleName, line,
                    "The 'path' statement MUST be present if the type is 'leafref'.");
        case "bits":
            throw new YangParseException(moduleName, line, "The 'bit' statement MUST be present if the type is 'bits'.");
        case "enumeration":
            throw new YangParseException(moduleName, line,
                    "The 'enum' statement MUST be present if the type is 'enumeration'.");
        }
    }

    public static <T extends ParserRuleContext> Optional<T> getFirstContext(final ParserRuleContext context,final Class<T> contextType) {
        List<T> potential = context.getRuleContexts(contextType);
        if (potential.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(potential.get(0));
    }

}
