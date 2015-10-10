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
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

public final class UnsignedConstrainedType extends ConstrainedType<UnsignedIntegerTypeDefinition>
        implements UnsignedIntegerTypeDefinition {
    private final List<RangeConstraint> rangeConstraints;

    UnsignedConstrainedType(final UnsignedIntegerTypeDefinition baseType, final SchemaPath path,
        final Collection<UnknownSchemaNode> unknownSchemaNodes, final Collection<RangeConstraint> rangeConstraints) {
        super(baseType, path, unknownSchemaNodes);
        this.rangeConstraints = ImmutableList.copyOf(rangeConstraints);
    }

    @Override
    public UnsignedDerivedTypeBuilder newDerivedTypeBuilder(final SchemaPath path) {
        return new UnsignedDerivedTypeBuilder(getBaseType(), path, rangeConstraints);
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return rangeConstraints;
    }
}
