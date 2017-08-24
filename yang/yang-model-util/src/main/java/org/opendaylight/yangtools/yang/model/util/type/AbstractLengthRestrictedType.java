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
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;

abstract class AbstractLengthRestrictedType<T extends LengthRestrictedTypeDefinition<T>>
        extends AbstractRestrictedType<T> implements LengthRestrictedTypeDefinition<T> {
    private final List<LengthConstraint> lengthConstraints;

    AbstractLengthRestrictedType(final T baseType, final SchemaPath path,
        final Collection<UnknownSchemaNode> unknownSchemaNodes, final Collection<LengthConstraint> lengthConstraints) {
        super(baseType, path, unknownSchemaNodes);
        this.lengthConstraints = ImmutableList.copyOf(lengthConstraints);
    }

    @Override
    public final List<LengthConstraint> getLengthConstraints() {
        return lengthConstraints;
    }
}
