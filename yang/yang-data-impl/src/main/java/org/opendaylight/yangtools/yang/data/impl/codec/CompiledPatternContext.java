/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

class CompiledPatternContext {

    private final Pattern pattern;
    private final String errorMessage;
    private final String regEx;

    CompiledPatternContext(final PatternConstraint yangConstraint) {
        pattern = Pattern.compile("^" + yangConstraint.getJavaPatternString() + "$");
        errorMessage = yangConstraint.getErrorMessage().orElse(null);
        regEx = errorMessage == null ? yangConstraint.getRegularExpressionString() : null;
    }

    void validate(final String str) {
        if (!pattern.matcher(str).matches()) {
            if (errorMessage != null) {
                throw new IllegalArgumentException(errorMessage);
            }

            throw new IllegalArgumentException("Value " + str + "does not match regular expression '" + regEx + "'");
        }
    }
}
