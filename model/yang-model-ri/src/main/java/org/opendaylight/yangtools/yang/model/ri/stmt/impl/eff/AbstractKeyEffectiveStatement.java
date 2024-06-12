/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableSet;
import java.util.SequencedSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement;

abstract class AbstractKeyEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.Default<SequencedSet<QName>, KeyStatement>
        implements KeyEffectiveStatement {
    abstract static class Foreign extends AbstractKeyEffectiveStatement {
        // Polymorphic, with single value or a collection
        private final Object argument;

        Foreign(final KeyStatement declared, final SequencedSet<QName> argument) {
            super(declared);
            this.argument = maskSequencedSet(argument);
        }

        @Override
        public final SequencedSet<QName> argument() {
            return unmaskSequencedSet(argument);
        }

        private static @NonNull Object maskSequencedSet(final @NonNull SequencedSet<QName> set) {
            return set.size() == 1 ? set.getFirst() : set;
        }

        @SuppressWarnings("unchecked")
        private static @NonNull SequencedSet<QName> unmaskSequencedSet(final @NonNull Object masked) {
            return switch (masked) {
                case SequencedSet<?> set -> (SequencedSet<QName>) set;
                case QName qname -> ImmutableSet.of(qname);
                default -> throw new VerifyException("Unexpected argument " + masked);
            };
        }
    }

    abstract static class Local extends AbstractKeyEffectiveStatement {
        Local(final KeyStatement declared) {
            super(declared);
        }

        @Override
        public final SequencedSet<QName> argument() {
            return getDeclared().argument();
        }
    }

    AbstractKeyEffectiveStatement(final KeyStatement declared) {
        super(declared);
    }
}
