/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.PredicateSet;
import org.jaxen.saxpath.Axis;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A {@link NameStep} optimized for finding a child based on a {@link PathArgument}.
 *
 * @author Robert Varga
 */
@NonNullByDefault
@ThreadSafe
final class PathArgumentNameStep implements Immutable, NameStep, Serializable {
    private static final long serialVersionUID = 1L;

    private final PathArgument arg;

    PathArgumentNameStep(final PathArgument arg) {
        this.arg = requireNonNull(arg);
    }

    @Override
    public void simplify() {
        // No-op
    }

    /**
     * Returns the local name of the matched node
     *
     * @return the local name of the test
     */
    @Override
    public String getLocalName() {
        return arg.getNodeType().getLocalName();
    }

    @Override
    public int getAxis() {
        return Axis.CHILD;
    }

    @Override
    public List<?> evaluate(@Nullable Context context) throws JaxenException {
        final Optional<NormalizedNodeContext> child = NormalizedNodeContext.cast(context).findChild(arg);
        return child.isPresent() ? ImmutableList.of(child.get()) : ImmutableList.of();
    }

    @Override
    public String toString() {
        return arg.toString();
    }

    @Override
    public Iterator<?> axisIterator(@Nullable Object contextNode, @Nullable ContextSupport support)
            throws UnsupportedAxisException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPredicate(@Nullable Predicate predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Predicate> getPredicates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PredicateSet getPredicateSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrefix() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getText() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean matches(@Nullable Object node, @Nullable ContextSupport contextSupport) throws JaxenException {
        throw new UnsupportedOperationException();
    }
}
