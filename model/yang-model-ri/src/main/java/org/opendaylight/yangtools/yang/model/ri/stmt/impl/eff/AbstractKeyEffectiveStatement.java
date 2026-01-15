/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement;

abstract class AbstractKeyEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.Default<Set<QName>, KeyStatement>
        implements KeyEffectiveStatement {
    abstract static class Foreign extends AbstractKeyEffectiveStatement {
        // Polymorphic, with single value or a collection
        private final Object argument;

        Foreign(final KeyStatement declared, final Set<QName> argument) {
            super(declared);
            this.argument = maskSet(argument);
        }

        @Override
        public final Set<QName> argument() {
            return unmaskSet(argument);
        }

        private static @NonNull Object maskSet(final @NonNull Set<QName> set) {
            return set.size() == 1 ? set.iterator().next() : set;
        }

        @SuppressWarnings("unchecked")
        private static @NonNull Set<QName> unmaskSet(final @NonNull Object masked) {
            if (masked instanceof Set) {
                return (Set<QName>) masked;
            }
            verify(masked instanceof QName, "Unexpected argument %s", masked);
            return ImmutableSet.of((QName) masked);
        }
    }

    abstract static class Local extends AbstractKeyEffectiveStatement {
        Local(final KeyStatement declared) {
            super(declared);
        }

        @Override
        public final Set<QName> argument() {
            return declared().argument();
        }
    }

    AbstractKeyEffectiveStatement(final KeyStatement declared) {
        super(declared);
    }
}
