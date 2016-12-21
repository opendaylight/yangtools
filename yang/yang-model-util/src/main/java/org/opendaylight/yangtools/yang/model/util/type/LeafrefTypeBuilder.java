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
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

public final class LeafrefTypeBuilder extends RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> {
    private RevisionAwareXPath pathStatement;

    LeafrefTypeBuilder(final SchemaPath path) {
        super(null, path);
    }

    public LeafrefTypeBuilder setPathStatement(@Nonnull final RevisionAwareXPath pathStatement) {
        Preconditions.checkState(this.pathStatement == null, "Path statement already set to %s", this.pathStatement);
        this.pathStatement = Preconditions.checkNotNull(pathStatement);
        return this;
    }

    @Override
    LeafrefTypeDefinition buildType() {
        return new BaseLeafrefType(getPath(), pathStatement, getRequireInstance(), getUnknownSchemaNodes());
    }
}
