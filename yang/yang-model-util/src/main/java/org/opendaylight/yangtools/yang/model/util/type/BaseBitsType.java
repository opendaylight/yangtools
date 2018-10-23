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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

final class BaseBitsType extends AbstractBaseType<BitsTypeDefinition> implements BitsTypeDefinition {
    private final @NonNull List<Bit> bits;

    BaseBitsType(final SchemaPath path, final List<UnknownSchemaNode> unknownSchemaNodes, final Collection<Bit> bits) {
        super(path, unknownSchemaNodes);
        this.bits = ImmutableList.copyOf(bits);
    }

    @Override
    public List<Bit> getBits() {
        return bits;
    }

    @Override
    public int hashCode() {
        return BitsTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return BitsTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return BitsTypeDefinition.toString(this);
    }
}
