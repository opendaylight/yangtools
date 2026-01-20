/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagStatement;
import org.opendaylight.yangtools.rfc8819.model.api.Tag;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString.WithSubstatements;

@NonNullByDefault
final class ModuleTagStatementImpl extends WithSubstatements<Tag> implements ModuleTagStatement {
    ModuleTagStatementImpl(final Tag argument, final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(argument, substatements);
    }
}
