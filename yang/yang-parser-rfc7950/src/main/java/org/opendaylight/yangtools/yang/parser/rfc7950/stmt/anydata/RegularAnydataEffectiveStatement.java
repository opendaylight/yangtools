/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anydata;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;

/**
 * YANG 1.1 AnyData effective statement implementation.
 */
final class RegularAnydataEffectiveStatement extends EmptyAnydataEffectiveStatement {
    private final @NonNull Object substatements;

    RegularAnydataEffectiveStatement(final AnydataStatement declared, final SchemaPath path, final int flags,
            final @Nullable AnydataSchemaNode original,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, path, flags, original);
        this.substatements = maskList(substatements);
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }
}
