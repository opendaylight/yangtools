/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

public final class IdentityrefTypeBuilder extends TypeBuilder<IdentityrefTypeDefinition> {
    private IdentitySchemaNode identity;
    private final Builder<IdentitySchemaNode> builder = ImmutableSet.builder();

    IdentityrefTypeBuilder(final SchemaPath path) {
        super(null, path);
    }

    public IdentityrefTypeBuilder addIdentity(@Nonnull final IdentitySchemaNode identity) {
        if (this.identity == null) {
            this.identity = Preconditions.checkNotNull(identity);
        }

        builder.add(identity);
        return this;
    }

    @Override
    public IdentityrefTypeDefinition build() {
        final Set<IdentitySchemaNode> identities = builder.build();
        return new BaseIdentityrefType(getPath(), getUnknownSchemaNodes(), identity, identities);
    }
}
