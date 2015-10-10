/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

abstract class AbstractBaseType<T extends TypeDefinition<T>> implements Immutable, TypeDefinition<T> {
    private final List<UnknownSchemaNode> unknownSchemaNodes;
    private final SchemaPath path;

    AbstractBaseType(final QName qname) {
        this(SchemaPath.create(true, qname), ImmutableList.<UnknownSchemaNode>of());
    }

    AbstractBaseType(final SchemaPath path, final List<UnknownSchemaNode> unknownSchemaNodes) {
        this.path = Preconditions.checkNotNull(path);
        this.unknownSchemaNodes = Preconditions.checkNotNull(unknownSchemaNodes);
    }

    @Override
    public final T getBaseType() {
        return null;
    }

    @Override
    public final String getUnits() {
        return null;
    }

    @Override
    public final Object getDefaultValue() {
        return null;
    }

    @Override
    public final QName getQName() {
        return path.getLastComponent();
    }

    @Override
    public final SchemaPath getPath() {
        return path;
    }

    @Override
    public final String getDescription() {
        return null;
    }

    @Override
    public final String getReference() {
        return null;
    }

    @Override
    public final Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public final List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }
}
