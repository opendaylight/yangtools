/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

public final class StringTypeBuilder extends LengthRestrictedTypeBuilder<StringTypeDefinition> {
    private final List<PatternConstraint> patternConstraints = new ArrayList<>(0);

    StringTypeBuilder(final StringTypeDefinition baseType, final SchemaPath path) {
        super(baseType, path);
    }

    public void addPatternConstraint(final PatternConstraint constraint) {
        patternConstraints.add(Preconditions.checkNotNull(constraint));
        touch();
    }

    @Override
    public RestrictedStringType buildType() {
        final List<PatternConstraint> basePatterns = getBaseType().getPatternConstraints();
        final List<PatternConstraint> patterns;
        if (!patternConstraints.isEmpty()) {
            patterns = new ArrayList<>(patternConstraints.size() + basePatterns.size());
            patterns.addAll(patternConstraints);
            patterns.addAll(basePatterns);
        } else {
            patterns = getBaseType().getPatternConstraints();
        }

        return new RestrictedStringType(getBaseType(), getPath(), getUnknownSchemaNodes(),
            calculateLenghtConstraints(getBaseType().getLengthConstraints()), patterns);
    }
}
