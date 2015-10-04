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
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

@Beta
public final class BitBuilder extends AbstractSchemaNodeBuilder<Bit> {
    private Long position;

    public BitBuilder setPosition(final Long position) {
        Preconditions.checkArgument(position >= 0 && position <= 4294967295L,
                "Position %s is not in valid range [0..4294967295]", position);

        this.position = Preconditions.checkNotNull(position);
        return this;
    }

    @Override
    protected Bit buildNode(final SchemaPath path, final Status status, final String description, final String reference,
            final List<UnknownSchemaNode> unknownSchemaNodes) {
        Preconditions.checkState(position != null, "Position has not been set");
        return new BitImpl(path, description, reference, status, unknownSchemaNodes, position);
    }
}
