/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

class CompiledPatternContext {

    private final Pattern pattern;
    private final String errorMessage;

    CompiledPatternContext(final PatternConstraint yangConstraint) {
        pattern = Pattern.compile("^" + yangConstraint.getRegularExpression() + "$");
        final String yangMessage = yangConstraint.getErrorMessage();
        if (Strings.isNullOrEmpty(yangMessage)) {
            errorMessage = "Value %s does not match regular expression <" + pattern.pattern() + ">";
        } else {
            errorMessage = yangMessage;
        }
    }

    public void validate(final String s) {
        checkArgument(pattern.matcher(s).matches(), errorMessage, s);
    }
}