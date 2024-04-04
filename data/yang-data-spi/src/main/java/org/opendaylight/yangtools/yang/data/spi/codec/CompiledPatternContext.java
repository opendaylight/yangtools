/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.codec.YangInvalidValueException;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

final class CompiledPatternContext {
    private final PatternConstraint constraint;
    private final Pattern pattern;
    private final boolean invert;

    CompiledPatternContext(final PatternConstraint yangConstraint) {
        constraint = requireNonNull(yangConstraint);
        pattern = Pattern.compile(yangConstraint.getJavaPatternString());

        final Optional<ModifierKind> optModifier = yangConstraint.getModifier();
        if (optModifier.isPresent()) {
            invert = switch (optModifier.orElseThrow()) {
                case INVERT_MATCH -> true;
            };
        } else {
            invert = false;
        }
    }

    void validate(final String str) {
        if (pattern.matcher(str).matches() == invert) {
            throw new YangInvalidValueException(ErrorType.APPLICATION, constraint,
                "Value '" + str + "' " + (invert ? "matches" : "does not match") + " regular expression '"
                        + constraint.getRegularExpressionString() + "'");
        }
    }
}
