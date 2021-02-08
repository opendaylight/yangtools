/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

public final class IdentityrefTypeBuilder extends TypeBuilder<IdentityrefTypeDefinition> {
    private final Builder<IdentitySchemaNode> builder = ImmutableSet.builder();

    IdentityrefTypeBuilder(final QName qname) {
        super(null, qname);
    }

    public IdentityrefTypeBuilder addIdentity(final @NonNull IdentitySchemaNode identity) {
        builder.add(identity);
        return this;
    }

    @Override
    public IdentityrefTypeDefinition build() {
        final Set<IdentitySchemaNode> identities = builder.build();
        checkState(!identities.isEmpty(), "No identities specified in %s, at least one is required", getQName());
        return new BaseIdentityrefType(getQName(), getUnknownSchemaNodes(), identities);
    }
}
