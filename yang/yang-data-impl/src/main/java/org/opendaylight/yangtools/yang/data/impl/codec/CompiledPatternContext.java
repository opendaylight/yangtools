/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import java.util.Optional;
import java.util.regex.Pattern;

import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.codec.IllegalYangValueException;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

class CompiledPatternContext {

    private final Pattern pattern;
    private final String errorMessage;
    private final String regEx;
    private final boolean invert;

    CompiledPatternContext(final PatternConstraint yangConstraint) {
        pattern = Pattern.compile(yangConstraint.getJavaPatternString());
        errorMessage = yangConstraint.getErrorMessage().orElse(null);
        regEx = errorMessage == null ? yangConstraint.getRegularExpressionString() : null;

        final Optional<ModifierKind> optModifier = yangConstraint.getModifier();
        if (optModifier.isPresent()) {
            final ModifierKind modifier = optModifier.get();
            switch (modifier) {
                case INVERT_MATCH:
                    invert = true;
                    break;
                default:
                    throw new IllegalStateException("Unhandled modifier " + modifier);
            }
        } else {
            invert = false;
        }
    }

    void validate(final String str) {
        if (pattern.matcher(str).matches() == invert) {
            if (errorMessage != null) {
                throw new IllegalYangValueException(
                        RpcError.ErrorSeverity.ERROR,
                        RpcError.ErrorType.PROTOCOL,
                        "bad-element",
                        errorMessage);
            }

            throw new IllegalYangValueException(
                    RpcError.ErrorSeverity.ERROR,
                    RpcError.ErrorType.PROTOCOL,
                    "bad-element",
                    "Value '" + str + "' " + (invert ? "matches" : "does not match")
                            + " regular expression '" + regEx + "'");
        }
    }
}
