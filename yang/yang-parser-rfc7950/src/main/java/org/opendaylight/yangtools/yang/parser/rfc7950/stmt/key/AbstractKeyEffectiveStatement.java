/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement;

abstract class AbstractKeyEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.Default<Collection<SchemaNodeIdentifier>, KeyStatement>
        implements KeyEffectiveStatement {
    abstract static class Foreign extends AbstractKeyEffectiveStatement {
        // Polymorphic, with single value or a collection
        private final Object argument;

        Foreign(final KeyStatement declared, final Collection<SchemaNodeIdentifier> argument) {
            super(declared);
            this.argument = KeyStatementSupport.maskCollection(argument);
        }

        @Override
        public final Collection<SchemaNodeIdentifier> argument() {
            return KeyStatementSupport.unmaskCollection(argument);
        }
    }

    abstract static class Local extends AbstractKeyEffectiveStatement {
        Local(final KeyStatement declared) {
            super(declared);
        }

        @Override
        public final Collection<SchemaNodeIdentifier> argument() {
            return getDeclared().argument();
        }
    }

    AbstractKeyEffectiveStatement(final KeyStatement declared) {
        super(declared);
    }
}
