/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;

/**
 * A full-blown instance of {@link UsesEffectiveStatement}. Used when the argument does not match the declared instance
 * and we also have a refine substatement.
 */
final class FullCopiedUsesEffectiveStatement extends SimpleCopiedUsesEffectiveStatement {
    FullCopiedUsesEffectiveStatement(final UsesStatement declared, final QName argument,
            final GroupingDefinition sourceGrouping, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, argument, sourceGrouping, flags, substatements);
    }
}
