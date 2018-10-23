/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;

abstract class AbstractRangeRestrictedBaseType<T extends RangeRestrictedTypeDefinition<T, N>,
    N extends Number & Comparable<N>> extends AbstractBaseType<T> implements RangeRestrictedTypeDefinition<T, N> {
    private static final ConstraintMetaDefinition BUILTIN_CONSTRAINT = new ConstraintMetaDefinition() {

        @Override
        public Optional<String> getReference() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getErrorMessage() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getErrorAppTag() {
            return Optional.empty();
        }
    };

    private final RangeConstraint<N> rangeConstraint;

    AbstractRangeRestrictedBaseType(final QName qname, final N minValue, final N maxValue) {
        super(qname);
        this.rangeConstraint = new ResolvedRangeConstraint<>(BUILTIN_CONSTRAINT, ImmutableRangeSet.of(
            Range.closed(minValue, maxValue)));
    }

    AbstractRangeRestrictedBaseType(final SchemaPath path, final List<UnknownSchemaNode> unknownSchemaNodes,
        final RangeConstraint<N> rangeConstraint) {
        super(path, unknownSchemaNodes);
        this.rangeConstraint = requireNonNull(rangeConstraint);
    }

    @Override
    public final Optional<RangeConstraint<N>> getRangeConstraint() {
        return Optional.of(rangeConstraint);
    }
}
