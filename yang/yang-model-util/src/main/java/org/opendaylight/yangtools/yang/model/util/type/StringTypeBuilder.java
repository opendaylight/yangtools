/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.RangeMap;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

public final class StringTypeBuilder extends LengthRestrictedTypeBuilder<StringTypeDefinition> {
    private final List<PatternConstraint> patternConstraints = new ArrayList<>(0);

    StringTypeBuilder(final StringTypeDefinition baseType, final SchemaPath path) {
        super(baseType, path);
    }

    public StringTypeBuilder addPatternConstraint(final PatternConstraint constraint) {
        patternConstraints.add(Preconditions.checkNotNull(constraint));
        touch();
        return this;
    }

    @Override
    RangeMap<Integer, ConstraintMetaDefinition> typeLengthConstraints() {
        /**
         * Length constraint imposed on YANG string type by our implementation. {@link String#length()} is an integer,
         * capping our ability to support strings up to 18446744073709551615 as defined in
         * http://tools.ietf.org/html/rfc6020#section-9.4.4.
         *
         * FIXME: We could bump this number up to allow such models, but that could lead to unexpected run-time errors.
         *        In order to do that, the parser would need another pass on the effective statements, which would cap
         *        the constraints to the run-time environment.
         */
        return JavaLengthConstraints.INTEGER_SIZE_CONSTRAINTS;
    }

    @Override
    StringTypeDefinition buildType(final @Nullable LengthConstraint lengthConstraints) {
        return new RestrictedStringType(getBaseType(), getPath(), getUnknownSchemaNodes(),
            lengthConstraints, patternConstraints);
    }
}
