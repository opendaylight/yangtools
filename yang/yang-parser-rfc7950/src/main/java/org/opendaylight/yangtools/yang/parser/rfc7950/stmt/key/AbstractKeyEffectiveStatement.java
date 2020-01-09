/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement;

abstract class AbstractKeyEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.Default<Collection<SchemaNodeIdentifier>, KeyStatement>
        implements KeyEffectiveStatement {
    private final Collection<SchemaNodeIdentifier> argument;

    AbstractKeyEffectiveStatement(final KeyStatement declared, final Collection<SchemaNodeIdentifier> argument) {
        super(declared);
        this.argument = requireNonNull(argument);
    }

    @Override
    public Collection<SchemaNodeIdentifier> argument() {
        return argument;
    }
}
