/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

public final class LeafrefTypeBuilder extends RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> {
    private PathExpression pathStatement;

    LeafrefTypeBuilder(final SchemaPath path) {
        super(null, path);
    }

    public LeafrefTypeBuilder setPathStatement(final @NonNull PathExpression pathStatement) {
        checkState(this.pathStatement == null, "Path statement already set to %s", this.pathStatement);
        this.pathStatement = requireNonNull(pathStatement);
        return this;
    }

    @Override
    LeafrefTypeDefinition buildType() {
        return new BaseLeafrefType(getPath(), pathStatement, getRequireInstance(), getUnknownSchemaNodes());
    }
}
