/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

@Beta
public abstract class AbstractToStringDeclaredStatement<A> extends AbstractDeclaredStatement<A> {
    private final @NonNull A argument;

    protected AbstractToStringDeclaredStatement(final A argument) {
        this.argument = requireNonNull(argument);
    }

    @Override
    public final @NonNull A argument() {
        return argument;
    }

    @Override
    public final String rawArgument() {
        return argument.toString();
    }
}
