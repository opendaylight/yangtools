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
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

abstract class AbstractRangeRestrictedType<T extends TypeDefinition<T>> extends AbstractRestrictedType<T> {
    private final List<RangeConstraint> rangeConstraints;

    AbstractRangeRestrictedType(final T baseType, final SchemaPath path,
        final Collection<UnknownSchemaNode> unknownSchemaNodes, final Collection<RangeConstraint> rangeConstraints) {
        super(baseType, path, unknownSchemaNodes);
        this.rangeConstraints = ImmutableList.copyOf(rangeConstraints);
    }

    @Nonnull
    public final List<RangeConstraint> getRangeConstraints() {
        return rangeConstraints;
    }
}
