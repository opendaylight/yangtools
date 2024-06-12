/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import com.google.common.collect.ImmutableSet;
import java.util.SequencedSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithRawArgument;

abstract class AbstractKeyStatement extends WithRawArgument<SequencedSet<QName>> implements KeyStatement {
    final @NonNull Object argument;

    AbstractKeyStatement(final @NonNull String rawArgument, final @NonNull SequencedSet<QName> argument) {
        super(rawArgument);
        this.argument = maskSet(ImmutableSet.copyOf(argument));
    }

    @Override
    public final SequencedSet<QName> argument() {
        return unmaskSet(argument, QName.class);
    }
}
