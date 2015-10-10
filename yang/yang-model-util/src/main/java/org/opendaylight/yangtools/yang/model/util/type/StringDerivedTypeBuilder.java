/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

public final class StringDerivedTypeBuilder extends LengthDerivedTypeBuilder<StringTypeDefinition> {
    private final List<PatternConstraint> patternConstraints;

    StringDerivedTypeBuilder(final StringTypeDefinition baseType, final SchemaPath path,
        final List<LengthConstraint> lengthConstraints, final List<PatternConstraint> patternConstraints) {
        super(baseType, path, lengthConstraints);
        this.patternConstraints = Preconditions.checkNotNull(patternConstraints);
    }

    @Override
    public StringDerivedType build() {
        return new StringDerivedType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
            getStatus(), getUnits(), getLengthConstraints(), patternConstraints);
    }
}
