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

public final class LeafrefBaseTypeBuilder extends BaseTypeBuilder<LeafrefTypeDefinition> {
    private RevisionAwareXPath pathStatement;

    LeafrefBaseTypeBuilder(final SchemaPath path) {
        super(path);
    }

    public void setPathStatement(@Nonnull final RevisionAwareXPath pathStatement) {
        Preconditions.checkState(pathStatement == null, "Path statement already set to %s", pathStatement);
        this.pathStatement = Preconditions.checkNotNull(pathStatement);
    }

    @Override
    public LeafrefBaseType build() {
        return new LeafrefBaseType(getPath(), pathStatement);
    }
}
