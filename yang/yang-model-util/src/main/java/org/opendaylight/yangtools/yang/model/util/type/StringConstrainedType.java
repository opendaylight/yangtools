/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

public final class StringConstrainedType extends LengthConstrainedType<StringTypeDefinition>
        implements StringTypeDefinition {
    private final List<PatternConstraint> patternConstraints;

    StringConstrainedType(final StringTypeDefinition baseType, final SchemaPath path,
        final Collection<UnknownSchemaNode> unknownSchemaNodes, final Collection<LengthConstraint> lengthConstraints,
        final List<PatternConstraint> patternConstraints) {
        super(baseType, path, unknownSchemaNodes, lengthConstraints);
        this.patternConstraints = ImmutableList.copyOf(patternConstraints);
    }

    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return patternConstraints;
    }

    @Override
    public StringDerivedTypeBuilder newDerivedTypeBuilder(final SchemaPath path) {
        return new StringDerivedTypeBuilder(getBaseType(), path, getLengthConstraints(), patternConstraints);
    }
}
