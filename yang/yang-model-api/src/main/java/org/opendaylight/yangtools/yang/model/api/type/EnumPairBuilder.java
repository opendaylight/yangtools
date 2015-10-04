/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

@Beta
public final class EnumPairBuilder implements Builder<EnumPair> {
    private List<UnknownSchemaNode> unknownSchemaNodes = Collections.emptyList();
    private SchemaPath path;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    private String name;
    private Integer value;

    public EnumPairBuilder setPath(SchemaPath path) {
        path = Preconditions.checkNotNull(path);
        return this;
    }

    public EnumPairBuilder setDescription(String description) {
        description = Preconditions.checkNotNull(description);
        return this;
    }

    public EnumPairBuilder setReference(final String reference) {
        this.reference = Preconditions.checkNotNull(reference);
        return this;
    }

    public EnumPairBuilder setStatus(final Status status) {
        this.status = Preconditions.checkNotNull(status);
        return this;
    }

    public EnumPairBuilder setUnknownSchemaNodes(final List<UnknownSchemaNode> unknownSchemaNodes) {
        this.unknownSchemaNodes = Preconditions.checkNotNull(unknownSchemaNodes);
        return this;
    }

    public EnumPairBuilder setName(final String name) {
        this.name = Preconditions.checkNotNull(name);
        return this;
    }

    public EnumPairBuilder setValue(final Integer value) {
        this.value = Preconditions.checkNotNull(value);
        return this;
    }

    @Override
    public EnumPair build() {
        return new EnumPairImpl(path, description, reference, status, name, value, unknownSchemaNodes);
    }
}
