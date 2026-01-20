/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithRawArgument;

@NonNullByDefault
abstract class AbstractKeyStatement extends WithRawArgument<KeyArgument> implements KeyStatement {
    final Object argument;

    AbstractKeyStatement(final String rawArgument, final KeyArgument argument) {
        super(rawArgument);
        this.argument = maskSet(ImmutableSet.copyOf(argument));
    }

    @Override
    public final KeyArgument argument() {
        return unmaskSet(argument, QName.class);
    }
}
