/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.RequiredElementCountException;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher.TooFewElements;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher.TooManyElements;

final class MinMaxElementsValidation<T extends DataSchemaNode & ElementCountConstraintAware>
        extends AbstractValidation {
    private final ElementCountMatcher matcher;

    private MinMaxElementsValidation(final SchemaAwareApplyOperation<T> delegate, final ElementCountMatcher matcher) {
        super(delegate);
        this.matcher = requireNonNull(matcher);
    }

    static <T extends DataSchemaNode & ElementCountConstraintAware> ModificationApplyOperation from(
            final SchemaAwareApplyOperation<T> delegate) {
        final var matcher = delegate.getSchema().elementCountMatcher();
        return matcher == null ? delegate : new MinMaxElementsValidation<>(delegate, matcher);
    }

    @Override
    void enforceOnData(final NormalizedNode data) {
        try {
            enforceOnData(data, null);
        } catch (RequiredElementCountException e) {
            throw new MinMaxElementsValidationFailedException(e);
        }
    }

    @Override
    void enforceOnData(final ModificationPath path, final NormalizedNode data) throws RequiredElementCountException {
        enforceOnData(data, requireNonNull(path));
    }

    private void enforceOnData(final NormalizedNode value, final @Nullable ModificationPath path)
            throws RequiredElementCountException {
        if (!(value instanceof NormalizedNodeContainer<?> container)) {
            throw new IllegalArgumentException(value + " is not a NormalizedNodeContainer");
        }

        final int count = container.size();
        switch (matcher.matches(count)) {
            case null -> {
                // No-op
            }
            case TooFewElements violation -> {
                throw new RequiredElementCountException(
                    path == null ? YangInstanceIdentifier.of() : path.toInstanceIdentifier(), violation.errorAppTag(),
                    value.name() + " does not have enough elements (" + count + "), needs at least "
                        + violation.atLeast());
            }
            case TooManyElements violation -> {
                throw new RequiredElementCountException(
                    path == null ? YangInstanceIdentifier.of() : path.toInstanceIdentifier(), violation.errorAppTag(),
                    value.name() + " has too many elements (" + count + "), can have at most " + violation.atMost());
            }
        }
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        final ElementCountMatcher.AtLeast min;
        final ElementCountMatcher.AtMost max;
        switch (matcher) {
            case ElementCountMatcher.AtLeast atLeast -> {
                min = atLeast;
                max = null;
            }
            case ElementCountMatcher.AtMost atMost -> {
                min = null;
                max = atMost;
            }
            case ElementCountMatcher.InRange inRange -> {
                min = inRange.atLeast();
                max = inRange.atMost();
            }
        }
        return super.addToStringAttributes(helper.add("min", min).add("max", max));
    }
}
