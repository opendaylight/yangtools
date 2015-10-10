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
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public abstract class AbstractTypeBuilder<T extends TypeDefinition<T>> {
    private final Collection<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>(0);
    private final SchemaPath path;
    private final T baseType;

    AbstractTypeBuilder(final T baseType, final SchemaPath path) {
        this.path = Preconditions.checkNotNull(path);
        this.baseType = baseType;
    }

    final T getBaseType() {
        return baseType;
    }

    final SchemaPath getPath() {
        return path;
    }

    final Collection<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }

    public void addUnknownSchemaNode(final UnknownSchemaNode node) {
        unknownSchemaNodes.add(node);
    }
}
