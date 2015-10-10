/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

public final class StringConstrainedTypeBuilder extends LengthConstrainedTypeBuilder<StringTypeDefinition> {
    private final List<PatternConstraint> patternConstraints = new ArrayList<>(0);

    StringConstrainedTypeBuilder(final StringTypeDefinition baseType, final SchemaPath path) {
        super(baseType, path);
    }

    @Override
    public StringConstrainedType build() {
        final List<PatternConstraint> basePatterns = getBaseType().getPatternConstraints();
        final List<PatternConstraint> patterns;
        if (!patternConstraints.isEmpty()) {
            patterns = new ArrayList<>(patternConstraints.size() + basePatterns.size());
            patterns.addAll(patternConstraints);
            patterns.addAll(basePatterns);
        } else {
            patterns = getBaseType().getPatternConstraints();
        }

        return new StringConstrainedType(getBaseType(), getPath(), getUnknownSchemaNodes(),
            calculateLenghtConstraints(getBaseType().getLengthConstraints()), patterns);
    }
}
