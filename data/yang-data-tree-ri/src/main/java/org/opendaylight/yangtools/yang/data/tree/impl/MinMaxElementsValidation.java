/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.RequiredElementCountException;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;

final class MinMaxElementsValidation<T extends DataSchemaNode & ElementCountConstraintAware>
        extends AbstractValidation {
    @FunctionalInterface
    @NonNullByDefault
    interface ExceptionSupplier<T extends Exception> {
        T get(int actual, String message);
    }

    private final int minElements;
    private final int maxElements;

    private MinMaxElementsValidation(final SchemaAwareApplyOperation<T> delegate, final Integer minElements,
            final Integer maxElements) {
        super(delegate);
        this.minElements = minElements != null ? minElements : 0;
        this.maxElements = maxElements != null ? maxElements : Integer.MAX_VALUE;
    }

    static <T extends DataSchemaNode & ElementCountConstraintAware> ModificationApplyOperation from(
            final SchemaAwareApplyOperation<T> delegate) {
        final var optConstraint = delegate.getSchema().getElementCountConstraint();
        if (optConstraint.isEmpty()) {
            return delegate;
        }

        final var constraint = optConstraint.orElseThrow();
        return new MinMaxElementsValidation<>(delegate, constraint.getMinElements(), constraint.getMaxElements());
    }

    @Override
    void enforceOnData(final NormalizedNode data) {
        enforceOnData(data, (actual, message) -> new MinMaxElementsValidationFailedException(message, minElements,
            maxElements, actual));
    }

    @Override
    void enforceOnData(final ModificationPath path, final NormalizedNode data) throws RequiredElementCountException {
        enforceOnData(data, (actual, message) -> new RequiredElementCountException(path.toInstanceIdentifier(),
            minElements, maxElements, actual, message));
    }

    private <X extends @NonNull Exception> void enforceOnData(final NormalizedNode value,
            final ExceptionSupplier<X> exceptionSupplier) throws X {
        checkArgument(value instanceof NormalizedNodeContainer, "Value %s is not a NormalizedNodeContainer", value);
        final int children = ((NormalizedNodeContainer<?>) value).size();
        if (minElements > children) {
            throw exceptionSupplier.get(children, value.name()
                + " does not have enough elements (" + children + "), needs at least " + minElements);
        }
        if (maxElements < children) {
            throw exceptionSupplier.get(children, value.name()
                + " has too many elements (" + children + "), can have at most " + maxElements);
        }
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("min", minElements).add("max", maxElements));
    }
}
