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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

final class RestrictedStringType extends AbstractLengthRestrictedType<StringTypeDefinition>
        implements StringTypeDefinition {
    private final List<PatternConstraint> patternConstraints;

    RestrictedStringType(final StringTypeDefinition baseType, final SchemaPath path,
        final Collection<UnknownSchemaNode> unknownSchemaNodes,
        final @Nullable LengthConstraint lengthConstraints,
        final List<PatternConstraint> patternConstraints) {
        super(baseType, path, unknownSchemaNodes, lengthConstraints);
        this.patternConstraints = ImmutableList.copyOf(patternConstraints);
    }

    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return patternConstraints;
    }

    @Override
    public int hashCode() {
        return TypeDefinitions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return TypeDefinitions.equals(this, obj);
    }

    @Override
    public String toString() {
        return TypeDefinitions.toString(this);
    }
}
