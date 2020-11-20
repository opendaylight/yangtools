/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.RequiredElementCountException;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;

final class MinMaxElementsValidation<T extends DataSchemaNode & ElementCountConstraintAware>
        extends AbstractValidation {
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
        final Optional<ElementCountConstraint> optConstraint = delegate.getSchema().getElementCountConstraint();
        if (!optConstraint.isPresent()) {
            return delegate;
        }

        final ElementCountConstraint constraint = optConstraint.get();
        return new MinMaxElementsValidation<>(delegate, constraint.getMinElements(), constraint.getMaxElements());
    }

    @Override
    void enforceOnData(final NormalizedNode<?, ?> data) {
        enforceOnData(data, (actual, message) -> new MinMaxElementsValidationFailedException(message));
    }

    @Override
    void enforceOnData(final ModificationPath path, final NormalizedNode<?, ?> data)
            throws RequiredElementCountException {
        enforceOnData(data, (actual, message) -> new RequiredElementCountException(path.toInstanceIdentifier(),
            minElements, maxElements, actual, message));
    }

    @FunctionalInterface
    @NonNullByDefault
    interface ExceptionSupplier<T extends Exception> {
        T get(int actual, String message);
    }

    private <X extends @NonNull Exception> void enforceOnData(final NormalizedNode<?, ?> value,
            final ExceptionSupplier<X> exceptionSupplier) throws X {
        final int children = numOfChildrenFromValue(value);
        if (minElements > children) {
            throw exceptionSupplier.get(children, value.getIdentifier()
                + " does not have enough elements (" + children + "), needs at least " + minElements);
        }
        if (maxElements < children) {
            throw exceptionSupplier.get(children, value.getIdentifier()
                + " has too many elements (" + children + "), can have at most " + maxElements);
        }
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("min", minElements).add("max", maxElements));
    }

    private static int numOfChildrenFromValue(final NormalizedNode<?, ?> value) {
        if (value instanceof NormalizedNodeContainer) {
            return ((NormalizedNodeContainer<?, ?, ?>) value).size();
        } else if (value instanceof UnkeyedListNode) {
            return ((UnkeyedListNode) value).getSize();
        }

        throw new IllegalArgumentException(String.format(
                "Unexpected type '%s', expected types are NormalizedNodeContainer and UnkeyedListNode",
                value.getClass()));
    }
}
