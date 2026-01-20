/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyKeyStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement;

abstract class AbstractKeyEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.Default<KeyArgument, KeyStatement>
        implements KeyEffectiveStatement {
    abstract static class Foreign extends AbstractKeyEffectiveStatement {
        // Polymorphic: either a QName or KeyArgument.OfMore
        private final @NonNull Object argument;

        Foreign(final KeyStatement declared, final KeyArgument argument) {
            super(declared);
            this.argument = EmptyKeyStatement.maskArgument(argument);
        }

        @Override
        public final KeyArgument argument() {
            return EmptyKeyStatement.unmaskArgument(argument);
        }
    }

    abstract static class Local extends AbstractKeyEffectiveStatement {
        Local(final KeyStatement declared) {
            super(declared);
        }

        @Override
        public final KeyArgument argument() {
            return declared().argument();
        }
    }

    AbstractKeyEffectiveStatement(final KeyStatement declared) {
        super(declared);
    }
}
