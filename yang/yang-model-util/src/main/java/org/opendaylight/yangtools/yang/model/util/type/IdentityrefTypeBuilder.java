/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

public final class IdentityrefTypeBuilder extends TypeBuilder<IdentityrefTypeDefinition> {
    private IdentitySchemaNode identity;

    IdentityrefTypeBuilder(final SchemaPath path) {
        super(null, path);
    }

    public void setIdentity(@Nonnull final IdentitySchemaNode identity) {
        Preconditions.checkState(identity == null, "Identity already set to %s", identity);
        this.identity = Preconditions.checkNotNull(identity);
    }

    @Override
    public BaseIdentityrefType build() {
        return new BaseIdentityrefType(getPath(), getUnknownSchemaNodes(), identity);
    }
}
