/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

@Beta
public final class EnumPairBuilder extends AbstractSchemaNodeBuilder<EnumPair> {
    private String name;
    private Integer value;

    public EnumPairBuilder setName(final String name) {
        this.name = Preconditions.checkNotNull(name);
        return this;
    }

    public EnumPairBuilder setValue(final Integer value) {
        this.value = Preconditions.checkNotNull(value);
        return this;
    }

    @Override
    protected EnumPair buildNode(final SchemaPath path, final Status status, final String description, final String reference,
            final List<UnknownSchemaNode> unknownSchemaNodes) {
        return new EnumPairImpl(path, description, reference, status, name, value, unknownSchemaNodes);
    }
}
