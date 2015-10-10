/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

public final class LeafrefBaseType extends BaseType<LeafrefTypeDefinition> implements LeafrefTypeDefinition {
    private final RevisionAwareXPath pathStatement;

    LeafrefBaseType(final SchemaPath path, final RevisionAwareXPath pathStatement) {
        super(path);
        this.pathStatement = Preconditions.checkNotNull(pathStatement);
    }

    @Override
    public RevisionAwareXPath getPathStatement() {
        return pathStatement;
    }

    @Override
    public LeafrefConstrainedTypeBuilder newConstrainedTypeBuilder(final SchemaPath path) {
        return new LeafrefConstrainedTypeBuilder(this, path);
    }
}
