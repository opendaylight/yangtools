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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

@Beta
public final class BitsTypeBuilder extends AbstractTypeDefinitionBuilder<BitsTypeDefinition> {
    private final List<Bit> bits = new ArrayList<>();

    public BitsTypeBuilder addBit(@Nonnull final Bit bit) {
        bits.add(Preconditions.checkNotNull(bit));
        return this;
    }

    @Override
    protected BitsTypeDefinition buildNode(final SchemaPath path, final Status status, final String description, final String reference,
            final List<UnknownSchemaNode> unknownSchemaNodes, final BitsTypeDefinition baseType, final String units,
            final Object defaultValue) {
        return new BitsType(path, status, description, reference, unknownSchemaNodes, baseType, units, defaultValue,
            bits);
    }
}
