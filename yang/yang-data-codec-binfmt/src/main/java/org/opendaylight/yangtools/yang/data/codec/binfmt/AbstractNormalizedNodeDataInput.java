/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.DataInput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

abstract class AbstractNormalizedNodeDataInput extends ForwardingDataInput implements NormalizedNodeDataInput {
    // Visible for subclasses
    final @NonNull DataInput input;

    AbstractNormalizedNodeDataInput(final DataInput input) {
        this.input = requireNonNull(input);
    }

    @Override
    final DataInput delegate() {
        return input;
    }

    @Override
    public final SchemaPath readSchemaPath() throws IOException {
        final boolean absolute = input.readBoolean();
        return SchemaPath.create(readQNames(), absolute);
    }

    @Override
    public final SchemaNodeIdentifier readSchemaNodeIdentifier() throws IOException {
        final boolean absolute = input.readBoolean();
        final ImmutableList<QName> qnames = readQNames();
        return absolute ? Absolute.of(qnames) : Descendant.of(qnames);
    }

    private ImmutableList<QName> readQNames() throws IOException {
        final int size = input.readInt();
        final Builder<QName> qnames = ImmutableList.builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            qnames.add(readQName());
        }
        return qnames.build();
    }
}
