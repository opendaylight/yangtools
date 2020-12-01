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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace.SupportedFeatures;
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

    // Master flag controlling whether this context can yield an effective statement
    // FIXME: investigate the mechanics that are being supported by this, as it would be beneficial if we can get rid
    //        of this flag -- eliminating the initial alignment shadow used by below gap-filler fields.
    private boolean isSupportedToBuildEffective = true;

    // Flag for use with AbstractResumedStatement. This is hiding in the alignment shadow created by above boolean
    private boolean fullyDefined;

    // Flags for use with SubstatementContext. These are hiding in the alignment shadow created by above boolean and
    // hence improve memory layout.
    private byte flags;
    // Flag bit assignments
    private static final int IS_SUPPORTED_BY_FEATURES    = 0x01;
    private static final int HAVE_SUPPORTED_BY_FEATURES  = 0x02;
    private static final int IS_IGNORE_IF_FEATURE        = 0x04;
    private static final int HAVE_IGNORE_IF_FEATURE      = 0x08;
    // Note: these four are related
    private static final int IS_IGNORE_CONFIG            = 0x10;
    private static final int HAVE_IGNORE_CONFIG          = 0x20;
    private static final int IS_CONFIGURATION            = 0x40;
    private static final int HAVE_CONFIGURATION          = 0x80;

    // Have-and-set flag constants, also used as masks
    private static final int SET_SUPPORTED_BY_FEATURES = HAVE_SUPPORTED_BY_FEATURES | IS_SUPPORTED_BY_FEATURES;
    private static final int SET_CONFIGURATION = HAVE_CONFIGURATION | IS_CONFIGURATION;
    // Note: implies SET_CONFIGURATION, allowing fewer bit operations to be performed
    private static final int SET_IGNORE_CONFIG = HAVE_IGNORE_CONFIG | IS_IGNORE_CONFIG | SET_CONFIGURATION;
    private static final int SET_IGNORE_IF_FEATURE = HAVE_IGNORE_IF_FEATURE | IS_IGNORE_IF_FEATURE;

    private @Nullable E effectiveInstance;

    ReactorStmtCtx() {

    }

    ReactorStmtCtx(final ReactorStmtCtx<A, D, E> original) {
        isSupportedToBuildEffective = original.isSupportedToBuildEffective;
        fullyDefined = original.fullyDefined;
        this.flags = original.flags;
    }

    @Override
    public final E buildEffective() {
        final E existing;
        return (existing = effectiveInstance) != null ? existing : loadEffective();
    }

    private E loadEffective() {
        // Creating an effective statement does not strictly require a declared instance -- there are statements like
        // 'input', which are implicitly defined.
        // Our implementation design makes an invariant assumption that buildDeclared() has been called by the time
        // we attempt to create effective statement:
        buildDeclared();

        final E ret = effectiveInstance = createEffective();
        // we have called createEffective(), substatements are no longer guarded by us. Let's see if we can clear up
        // some residue.
        if (refcount == REFCOUNT_NONE) {
            sweepOnDecrement();
        }
        return ret;
    }

    // Exposed for ReplicaStatementContext
    E createEffective() {
        return definition().getFactory().createEffective(new BaseCurrentEffectiveStmtCtx<>(this), streamDeclared(),
            streamEffective());
    }

    abstract Stream<? extends StmtContext<?, ?, ?>> streamDeclared();

    abstract Stream<? extends StmtContext<?, ?, ?>> streamEffective();

    @Override
    public final boolean isSupportedByFeatures() {
        final int fl = flags & SET_SUPPORTED_BY_FEATURES;
        if (fl != 0) {
            return fl == SET_SUPPORTED_BY_FEATURES;
        }
        if (isIgnoringIfFeatures()) {
            flags |= SET_SUPPORTED_BY_FEATURES;
            return true;
        }

        /*
         * If parent is supported, we need to check if-features statements of this context.
         */
        if (isParentSupportedByFeatures()) {
            // If the set of supported features has not been provided, all features are supported by default.
            final Set<QName> supportedFeatures = getFromNamespace(SupportedFeaturesNamespace.class,
                    SupportedFeatures.SUPPORTED_FEATURES);
            if (supportedFeatures == null || StmtContextUtils.checkFeatureSupport(this, supportedFeatures)) {
                flags |= SET_SUPPORTED_BY_FEATURES;
                return true;
            }
        }

        // Either parent is not supported or this statement is not supported
        flags |= HAVE_SUPPORTED_BY_FEATURES;
        return false;
    }

    protected abstract boolean isParentSupportedByFeatures();

    /**
     * Config statements are not all that common which means we are performing a recursive search towards the root
     * every time {@link #isConfiguration()} is invoked. This is quite expensive because it causes a linear search
     * for the (usually non-existent) config statement.
     *
     * <p>
     * This method maintains a resolution cache, so once we have returned a result, we will keep on returning the same
     * result without performing any lookups, solely to support {@link SubstatementContext#isConfiguration()}.
     *
     * <p>
     * Note: use of this method implies that {@link #isIgnoringConfig()} is realized with
     *       {@link #isIgnoringConfig(StatementContextBase)}.
     */
    final boolean isConfiguration(final StatementContextBase<?, ?, ?> parent) {
        final int fl = flags & SET_CONFIGURATION;
        if (fl != 0) {
            return fl == SET_CONFIGURATION;
        }
        if (isIgnoringConfig(parent)) {
            // Note: SET_CONFIGURATION has been stored in flags
            return true;
        }

        final boolean isConfig;
        final Optional<Boolean> optConfig = findSubstatementArgument(ConfigEffectiveStatement.class);
        if (optConfig.isPresent()) {
            isConfig = optConfig.orElseThrow();
            if (isConfig) {
                // Validity check: if parent is config=false this cannot be a config=true
                InferenceException.throwIf(!parent.isConfiguration(), sourceReference(),
                        "Parent node has config=false, this node must not be specifed as config=true");
            }
        } else {
            // If "config" statement is not specified, the default is the same as the parent's "config" value.
            isConfig = parent.isConfiguration();
        }

        // Resolved, make sure we cache this return
        flags |= isConfig ? SET_CONFIGURATION : HAVE_CONFIGURATION;
        return isConfig;
    }

    protected abstract boolean isIgnoringConfig();

    /**
     * This method maintains a resolution cache for ignore config, so once we have returned a result, we will
     * keep on returning the same result without performing any lookups. Exists only to support
     * {@link SubstatementContext#isIgnoringConfig()}.
     *
     * <p>
     * Note: use of this method implies that {@link #isConfiguration()} is realized with
     *       {@link #isConfiguration(StatementContextBase)}.
     */
    final boolean isIgnoringConfig(final StatementContextBase<?, ?, ?> parent) {
        final int fl = flags & SET_IGNORE_CONFIG;
        if (fl != 0) {
            return fl == SET_IGNORE_CONFIG;
        }
        if (definition().support().isIgnoringConfig() || parent.isIgnoringConfig()) {
            flags |= SET_IGNORE_CONFIG;
            return true;
        }

        flags |= HAVE_IGNORE_CONFIG;
        return false;
    }

    protected abstract boolean isIgnoringIfFeatures();

    /**
     * This method maintains a resolution cache for ignore if-feature, so once we have returned a result, we will
     * keep on returning the same result without performing any lookups. Exists only to support
     * {@link SubstatementContext#isIgnoringIfFeatures()}.
     */
    final boolean isIgnoringIfFeatures(final StatementContextBase<?, ?, ?> parent) {
        final int fl = flags & SET_IGNORE_IF_FEATURE;
        if (fl != 0) {
            return fl == SET_IGNORE_IF_FEATURE;
        }
        if (definition().support().isIgnoringIfFeatures() || parent.isIgnoringIfFeatures()) {
            flags |= SET_IGNORE_IF_FEATURE;
            return true;
        }

        flags |= HAVE_IGNORE_IF_FEATURE;
        return false;
    }

    @Override
    public final boolean isSupportedToBuildEffective() {
        return isSupportedToBuildEffective;
    }

    @Override
    public final void setIsSupportedToBuildEffective(final boolean isSupportedToBuildEffective) {
        this.isSupportedToBuildEffective = isSupportedToBuildEffective;
    }

    @Override
    public abstract StatementContextBase<?, ?, ?> getParentContext();

    /**
     * Returns the model root for this statement.
     *
     * @return root context of statement
     */
    @Override
    public abstract RootStatementContext<?, ?, ?> getRoot();

    @Override
    public final @NonNull Registry getBehaviourRegistry() {
        return getRoot().getBehaviourRegistryImpl();
    }

    @Override
    public final YangVersion getRootVersion() {
        return getRoot().getRootVersionImpl();
    }

    @Override
    public final void setRootVersion(final YangVersion version) {
        getRoot().setRootVersionImpl(version);
    }

    @Override
    public final void addMutableStmtToSeal(final MutableStatement mutableStatement) {
        getRoot().addMutableStmtToSealImpl(mutableStatement);
    }

    @Override
    public final void addRequiredSource(final SourceIdentifier dependency) {
        getRoot().addRequiredSourceImpl(dependency);
    }

    @Override
    public final void setRootIdentifier(final SourceIdentifier identifier) {
        getRoot().setRootIdentifierImpl(identifier);
    }

    @Override
    public final boolean isEnabledSemanticVersioning() {
        return getRoot().isEnabledSemanticVersioningImpl();
    }

    /**
     * Return a value associated with specified key within a namespace.
     *
     * @param type Namespace type
     * @param key Key
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param <N> namespace type
     * @param <T> key type
     * @return Value, or null if there is no element
     * @throws NamespaceNotAvailableException when the namespace is not available.
     */
    @Override
    public final <K, V, T extends K, N extends IdentifierNamespace<K, V>> V getFromNamespace(
            final Class<@NonNull N> type, final T key) {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getFrom(this, key);
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(
            final Class<N> type) {
        return getLocalNamespace(type);
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromNamespace(final Class<N> type) {
        return getNamespace(type);
    }

    // These two exists only due to memory optimization, should live in AbstractResumedStatement
    final boolean fullyDefined() {
        return fullyDefined;
    }

    final void setFullyDefined() {
        fullyDefined = true;
    }

    /**
     * Return the context in which this statement was defined.
     *
     * @return statement definition
     */
    abstract @NonNull StatementDefinitionContext<A, D, E> definition();

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

    abstract boolean builtDeclared();

    private boolean noImplictRef() {
        return builtDeclared() && effectiveInstance != null || !isSupportedToBuildEffective();
    }

    private void sweepOnDecrement() {
        LOG.trace("Sweeping on decrement {}", this);
        final ReactorStmtCtx<?, ?, ?> parent = getParentContext();
        if (parent == null) {
            // We are the top-level object and have lost a reference. Trigger sweep if possible and we are done.
            sweepState();
            return;
        }

        // Check whether there the parent needs to know any references to any of our parents
        int refs = parent.refcount;
        if (refs > REFCOUNT_SWEEPING && refs < REFCOUNT_NONE) {
            // We are a child for which our parent is waiting. Notify it and we are done.
            parent.sweepOnChildDone();
            return;
        } else if (refs > REFCOUNT_NONE || refs <= REFCOUNT_SWEEPING || !parent.noImplictRef()) {
            // No-op
            return;
        }

        // parent is potentially reclaimable
        if (parent.noParentRefs()) {
            LOG.trace("Cleanup {} from {} to parent {}", parent.refcount, this, parent);
            parent.sweepState();
        }
    }

    // FIXME: cache the resolution of this
    private boolean noParentRefs() {
        final ReactorStmtCtx<?, ?, ?> parent = getParentContext();
        if (parent != null) {
            final int refs = parent.refcount;
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
        final ReactorStmtCtx<?, ?, ?> parent = getParentContext();
        LOG.trace("Propagating to parent {}", parent);
        if (parent != null && parent.refcount > REFCOUNT_SWEEPING && parent.refcount < REFCOUNT_NONE) {
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
