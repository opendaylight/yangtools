/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;

final class EmptyIdentityEffectiveStatement extends AbstractIdentityEffectiveStatement {
    private static final int CURRENT_FLAGS = new FlagsBuilder().setStatus(Status.CURRENT).toFlags();

    EmptyIdentityEffectiveStatement(final IdentityStatement declared, final SchemaPath path) {
        super(declared, path);
    }

    @Override
    public Collection<? extends IdentitySchemaNode> getBaseIdentities() {
        return ImmutableSet.of();
    }

    @Override
    public int flags() {
        return CURRENT_FLAGS;
    }
}
