/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

final class RegularPatternConstraint extends AbstractPatternConstraint {
    private final String description;
    private final String reference;
    private final String errorMessage;
    private final ModifierKind modifier;

    RegularPatternConstraint(final PatternConstraint original, final String description, final String reference,
            final String errorAppTag, final String errorMessage, final ModifierKind modifier) {
        super(original.getJavaPatternString(), original.getRegularExpressionString(), errorAppTag);
        this.description = description;
        this.reference = reference;
        this.errorMessage = errorMessage;
        this.modifier = modifier;
    }

    @Override
    String description() {
        return description;
    }

    @Override
    String reference() {
        return reference;
    }

    @Override
    String errorMessage() {
        return errorMessage;
    }

    @Override
    ModifierKind modifier() {
        return modifier;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("description", description).add("reference", reference)
                .add("errorMessage", errorMessage).add("modifier", modifier);
    }
}
