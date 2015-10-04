/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

/**
 * Internal implementation of EnumPair.
 */
final class EnumPairImpl extends AbstractSchemaNode implements EnumPair, Immutable {

    private final Integer value;
    private final String name;

    EnumPairImpl(final SchemaPath path, final String description, final String reference, final Status status,
            final String name, final Integer value, final List<UnknownSchemaNode> unknownNodes) {
        super(path, description, reference, status, unknownNodes);
        this.name = Preconditions.checkNotNull(name);
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(value);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        EnumPairImpl other = (EnumPairImpl) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!Objects.equals(value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return EnumPair.class.getSimpleName() + "[name=" + name + ", value=" + value + "]";
    }
}
