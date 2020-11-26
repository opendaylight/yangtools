/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;

import com.google.common.base.VerifyException;
import java.util.Collection;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Real "core" reactor statement implementation of {@link Mutable}, supporting basic reactor lifecycle.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
abstract class ReactorStmtCtx<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends NamespaceStorageSupport implements Mutable<A, D, E> {
    private static final Logger LOG = LoggerFactory.getLogger(StatementContextBase.class);

    /**
     * Substatement refcount tracking. This mechanics deals with retaining substatements for the purposes of
     * instantiating their lazy copies in InferredStatementContext. It works in concert with {@link #loadEffective()}
     * and {@link #buildDeclared()}/{@link #builtDeclared()}: declared/effective statement views hold an implicit
     * reference and refcount-based sweep is not activated until they are done (or this statement is not
     * {@link #isSupportedToBuildEffective}).
     *
     * <p>
     * Reference count is hierarchical in that parent references also pin down their child statements and do not allow
     * them to be swept.
     *
     * <p>
     * The counter's positive values are tracking incoming references via {@link #incRef()}/{@link #decRef()} methods.
     * Once we transition to sweeping, this value becomes negative counting upwards to {@link #REFCOUNT_NONE} based on
     * {@link #sweepOnChildDone()}. Once we reach that, we transition to {@link #REFCOUNT_SWEPT}.
     */
    private int refcount = REFCOUNT_NONE;
    /**
     * No outstanding references, this statement is a potential candidate for sweeping, provided it has populated its
     * declared and effective views and {@link #parentRef} is known to be absent.
     */
    private static final int REFCOUNT_NONE = 0;
    /**
     * Reference count overflow or some other recoverable logic error. Do not rely on refcounts and do not sweep
     * anything.
     *
     * <p>
     * Note on value assignment:
     * This allow our incRef() to naturally progress to being saturated. Others jump there directly.
     * It also makes it  it impossible to observe {@code Interger.MAX_VALUE} children, which we take advantage of for
     * {@link #REFCOUNT_SWEEPING}.
     */
    private static final int REFCOUNT_DEFUNCT = Integer.MAX_VALUE;
    /**
     * This statement is being actively swept. This is a transient value set when we are sweeping our children, so that
     * we prevent re-entering this statement.
     *
     * <p>
     * Note on value assignment:
     * The value is lower than any legal child refcount due to {@link #REFCOUNT_DEFUNCT} while still being higher than
     * {@link #REFCOUNT_SWEPT}.
     */
    private static final int REFCOUNT_SWEEPING = -Integer.MAX_VALUE;
    /**
     * This statement, along with its entire subtree has been swept and we positively know all our children have reached
     * this state. We {@link #sweepNamespaces()} upon reaching this state.
     *
     * <p>
     * Note on value assignment:
     * This is the lowest value observable, making it easier on checking others on equality.
     */
    private static final int REFCOUNT_SWEPT = Integer.MIN_VALUE;

    /**
     * Acquire a reference on this context. As long as there is at least one reference outstanding,
     * {@link #buildEffective()} will not result in {@link #effectiveSubstatements()} being discarded.
     *
     * @throws VerifyException if {@link #effectiveSubstatements()} has already been discarded
     */
    final void incRef() {
        final int current = refcount;
        verify(current >= REFCOUNT_NONE, "Attempted to access reference count of %s", this);
        if (current != REFCOUNT_DEFUNCT) {
            // Note: can end up becoming REFCOUNT_DEFUNCT on overflow
            refcount = current + 1;
        } else {
            LOG.debug("Disabled refcount increment of {}", this);
        }
    }

    /**
     * Release a reference on this context. This call may result in {@link #effectiveSubstatements()} becoming
     * unavailable.
     */
    final void decRef() {
        final int current = refcount;
        if (current == REFCOUNT_DEFUNCT) {
            // no-op
            LOG.debug("Disabled refcount decrement of {}", this);
            return;
        }
        if (current <= REFCOUNT_NONE) {
            // Underflow, become defunct
            LOG.warn("Statement refcount underflow, reference counting disabled for {}", this, new Throwable());
            refcount = REFCOUNT_DEFUNCT;
            return;
        }

        refcount = current - 1;
        LOG.trace("Refcount {} on {}", refcount, this);
        if (refcount == REFCOUNT_NONE && noImplictRef()) {
            // We are no longer guarded by effective instance
            sweepOnDecrement();
        }
    }

    final void releaseImplicitRef() {
        if (refcount == REFCOUNT_NONE) {
            sweepOnDecrement();
        }
    }

    /**
     * Sweep this statement context as a result of {@link #sweepSubstatements()}, i.e. when parent is also being swept.
     */
    final void sweep() {
        if (refcount == REFCOUNT_NONE && noImplictRef()) {
            LOG.trace("Releasing {}", this);
            sweepState();
        }
    }

    static final int countUnswept(final Collection<? extends ReactorStmtCtx<?, ?, ?>> substatements) {
        int result = 0;
        for (ReactorStmtCtx<?, ?, ?> stmt : substatements) {
            if (stmt.refcount > REFCOUNT_NONE || !stmt.noImplictRef()) {
                result++;
            }
        }
        return result;
    }

    /**
     * Implementation-specific sweep action. This is expected to perform a recursive {@link #sweep()} on all
     * {@link #declaredSubstatements()} and {@link #effectiveSubstatements()} and report the result of the sweep
     * operation.
     *
     * <p>
     * {@link #effectiveSubstatements()} as well as namespaces may become inoperable as a result of this operation.
     *
     * @return True if the entire tree has been completely swept, false otherwise.
     */
    abstract int sweepSubstatements();

    abstract boolean noImplictRef();

    abstract @Nullable ReactorStmtCtx<?, ?, ?> parentStmtCtx();

    // Called when this statement does not have an implicit reference and have reached REFCOUNT_NONE
    private void sweepOnDecrement() {
        LOG.trace("Sweeping on decrement {}", this);
        final ReactorStmtCtx<?, ?, ?> parent = parentStmtCtx();
        if (parent == null) {
            // We are the top-level object and have lost a reference. Trigger sweep if possible and we are done.
            sweepState();
            return;
        }

        parent.sweepOnChildDecrement();
    }

    // Called from child when it has lost its final reference
    private void sweepOnChildDecrement() {
        if (isAwaitingChildren()) {
            // We are a child for which our parent is waiting. Notify it and we are done.
            sweepOnChildDone();
            return;
        }

        // Check parent reference count
        final int refs = refcount;
        if (refs > REFCOUNT_NONE || refs <= REFCOUNT_SWEEPING || !noImplictRef()) {
            // No-op
            return;
        }

        // parent is potentially reclaimable
        if (noParentRefs()) {
            LOG.trace("Cleanup {} of parent {}", refcount, this);
            sweepState();
        }
    }

    // FIXME: cache the resolution of this
    private boolean noParentRefs() {
        final ReactorStmtCtx<?, ?, ?> parent = parentStmtCtx();
        if (parent != null) {
            final int refs = parent.refcount;
            // FIXME: 'noImplicitRef' is too strict here?
            if (refs > REFCOUNT_NONE || !parent.noImplictRef()) {
                // parent with refcount or protected by views
                return false;
            }
            if (refs < REFCOUNT_NONE) {
                // parent is being swept already
                return true;
            }
            // REFCOUNT_NONE and reclaimable, look forward
            return parent.noParentRefs();
        }
        return true;
    }

    private boolean isAwaitingChildren() {
        return refcount > REFCOUNT_SWEEPING && refcount < REFCOUNT_NONE;
    }

    private void sweepOnChildDone() {
        LOG.trace("Sweeping on child done {}", this);
        final int current = refcount;
        if (current >= REFCOUNT_NONE) {
            // no-op, perhaps we want to handle some cases differently?
            LOG.trace("Ignoring child sweep of {} for {}", this, current);
            return;
        }
        verify(current != REFCOUNT_SWEPT, "Attempt to sweep a child of swept %s", this);

        refcount = current + 1;
        LOG.trace("Child refcount {}", refcount);
        if (refcount == REFCOUNT_NONE) {
            sweepDone();
            sweepParent();
        }
    }

    private void sweepParent() {
        final ReactorStmtCtx<?, ?, ?> parent = parentStmtCtx();
        LOG.trace("Propagating to parent {}", parent);
        if (parent != null && parent.isAwaitingChildren()) {
            parent.sweepOnChildDone();
        }
    }

    private void sweepDone() {
        LOG.trace("Sweep done for {}", this);
        refcount = REFCOUNT_SWEPT;
        sweepNamespaces();
    }

    private boolean sweepState() {
        refcount = REFCOUNT_SWEEPING;
        final int childRefs = sweepSubstatements();
        if (childRefs == 0) {
            sweepDone();
            return true;
        }
        if (childRefs < 0 || childRefs >= REFCOUNT_DEFUNCT) {
            LOG.warn("Negative child refcount {} cannot be stored, reference counting disabled for {}", childRefs, this,
                new Throwable());
            refcount = REFCOUNT_DEFUNCT;
        } else {
            LOG.trace("Still {} outstanding children of {}", childRefs, this);
            refcount = -childRefs;
        }
        return false;
    }
}
