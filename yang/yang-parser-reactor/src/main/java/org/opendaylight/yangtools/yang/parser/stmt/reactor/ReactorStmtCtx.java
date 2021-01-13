/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
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
        extends NamespaceStorageSupport implements Mutable<A, D, E>, Current<A, D> {
    private static final Logger LOG = LoggerFactory.getLogger(ReactorStmtCtx.class);

    /**
     * Substatement refcount tracking. This mechanics deals with retaining substatements for the purposes of
     * instantiating their lazy copies in InferredStatementContext. It works in concert with {@link #buildEffective()}
     * and {@link #declared()}: declared/effective statement views hold an implicit reference and refcount-based
     * sweep is not activated until they are done (or this statement is not {@link #isSupportedToBuildEffective}).
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

    private @Nullable E effectiveInstance;

    // Master flag controlling whether this context can yield an effective statement
    // FIXME: investigate the mechanics that are being supported by this, as it would be beneficial if we can get rid
    //        of this flag -- eliminating the initial alignment shadow used by below gap-filler fields.
    private boolean isSupportedToBuildEffective = true;

    // Flag bit assignments
    private static final int IS_SUPPORTED_BY_FEATURES   = 0x10;
    private static final int HAVE_SUPPORTED_BY_FEATURES = 0x20;
    private static final int IS_IGNORE_IF_FEATURE       = 0x40;
    private static final int HAVE_IGNORE_IF_FEATURE     = 0x80;
    // Have-and-set flag constants, also used as masks
    private static final int SET_SUPPORTED_BY_FEATURES  = HAVE_SUPPORTED_BY_FEATURES | IS_SUPPORTED_BY_FEATURES;
    private static final int SET_IGNORE_IF_FEATURE      = HAVE_IGNORE_IF_FEATURE | IS_IGNORE_IF_FEATURE;

    // EffectiveConfig mapping
    private static final int MASK_CONFIG = 0x03;
    private static final int HAVE_CONFIG = 0x04;
    private static final EffectiveConfig[] EFFECTIVE_CONFIGS;

    static {
        final EffectiveConfig[] values = EffectiveConfig.values();
        final int length = values.length;
        verify(length == 4, "Unexpected EffectiveConfig cardinality %s", length);
        EFFECTIVE_CONFIGS = values;
    }

    // Flags for use with SubstatementContext. These are hiding in the alignment shadow created by above boolean and
    // hence improve memory layout.
    private byte flags;

    // Flag for use with AbstractResumedStatement. This is hiding in the alignment shadow created by above boolean
    // FIXME: move this out once we have JDK15+
    private boolean fullyDefined;

    // SchemaPath cache for use with SubstatementContext and InferredStatementContext. This hurts RootStatementContext
    // a bit in terms of size -- but those are only a few and SchemaPath is on its way out anyway.
    @Deprecated
    private volatile SchemaPath schemaPath;

    ReactorStmtCtx() {
        // Empty on purpose
    }

    ReactorStmtCtx(final ReactorStmtCtx<A, D, E> original) {
        isSupportedToBuildEffective = original.isSupportedToBuildEffective;
        fullyDefined = original.fullyDefined;
        flags = original.flags;
    }

    //
    //
    // Common public interface contracts with simple mechanics. Please keep this in one logical block, so we do not end
    // up mixing concerns and simple details with more complex logic.
    //
    //

    @Override
    public abstract StatementContextBase<?, ?, ?> getParentContext();

    @Override
    public abstract RootStatementContext<?, ?, ?> getRoot();

    @Override
    public abstract Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements();

    @Override
    public final @NonNull Registry getBehaviourRegistry() {
        return getRoot().getBehaviourRegistryImpl();
    }

    @Override
    public final YangVersion yangVersion() {
        return getRoot().getRootVersionImpl();
    }

    @Override
    public final void setRootVersion(final YangVersion version) {
        getRoot().setRootVersionImpl(version);
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

    @Override
    public final ModelActionBuilder newInferenceAction(final ModelProcessingPhase phase) {
        return getRoot().getSourceContext().newInferenceAction(phase);
    }

    @Override
    public final StatementDefinition publicDefinition() {
        return definition().getPublicView();
    }

    @Override
    public final Parent effectiveParent() {
        return getParentContext();
    }

    @Override
    public final CommonStmtCtx root() {
        return getRoot();
    }

    @Override
    public final EffectiveStatement<?, ?> original() {
        return getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
    }

    @Override
    // Non-final due to InferredStatementContext's override
    public <X, Z extends EffectiveStatement<X, ?>> @NonNull Optional<X> findSubstatementArgument(
            final @NonNull Class<Z> type) {
        return allSubstatementsStream()
            .filter(ctx -> ctx.isSupportedToBuildEffective() && ctx.producesEffective(type))
            .findAny()
            .map(ctx -> (X) ctx.getArgument());
    }

    @Override
    // Non-final due to InferredStatementContext's override
    public boolean hasSubstatement(final @NonNull Class<? extends EffectiveStatement<?, ?>> type) {
        return allSubstatementsStream()
            .anyMatch(ctx -> ctx.isSupportedToBuildEffective() && ctx.producesEffective(type));
    }

    @Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public final <Z extends EffectiveStatement<A, D>> StmtContext<A, D, Z> caerbannog() {
        return (StmtContext<A, D, Z>) this;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("definition", definition()).add("rawArgument", rawArgument());
    }

    /**
     * Return the context in which this statement was defined.
     *
     * @return statement definition
     */
    abstract @NonNull StatementDefinitionContext<A, D, E> definition();

    //
    //
    // NamespaceStorageSupport/Mutable integration methods. Keep these together.
    //
    //

    @Override
    public final <K, V, T extends K, N extends IdentifierNamespace<K, V>> V namespaceItem(final Class<@NonNull N> type,
            final T key) {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getFrom(this, key);
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> namespace(final Class<@NonNull N> type) {
        return getNamespace(type);
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>>
            Map<K, V> localNamespacePortion(final Class<@NonNull N> type) {
        return getLocalNamespace(type);
    }

    @Override
    protected final void checkLocalNamespaceAllowed(final Class<? extends IdentifierNamespace<?, ?>> type) {
        definition().checkNamespaceAllowed(type);
    }

    @Override
    protected <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceElementAdded(final Class<N> type, final K key,
            final V value) {
        // definition().onNamespaceElementAdded(this, type, key, value);
    }

    //
    //
    // Statement build entry points -- both public and package-private.
    //
    //

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
        declared();

        final E ret = effectiveInstance = createEffective();
        // we have called createEffective(), substatements are no longer guarded by us. Let's see if we can clear up
        // some residue.
        if (refcount == REFCOUNT_NONE) {
            sweepOnDecrement();
        }
        return ret;
    }

    abstract @NonNull E createEffective();

    /**
     * Try to execute current {@link ModelProcessingPhase} of source parsing. If the phase has already been executed,
     * this method does nothing.
     *
     * @param phase to be executed (completed)
     * @return true if phase was successfully completed
     * @throws SourceException when an error occurred in source parsing
     */
    final boolean tryToCompletePhase(final ModelProcessingPhase phase) {
        return phase.isCompletedBy(getCompletedPhase()) || doTryToCompletePhase(phase);
    }

    abstract boolean doTryToCompletePhase(ModelProcessingPhase phase);

    //
    //
    // Flags-based mechanics. These include public interfaces as well as all the crud we have lurking in our alignment
    // shadow.
    //
    //

    @Override
    public final boolean isSupportedToBuildEffective() {
        return isSupportedToBuildEffective;
    }

    @Override
    public final void setIsSupportedToBuildEffective(final boolean isSupportedToBuildEffective) {
        this.isSupportedToBuildEffective = isSupportedToBuildEffective;
    }

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
     * every time {@link #effectiveConfig()} is invoked. This is quite expensive because it causes a linear search
     * for the (usually non-existent) config statement.
     *
     * <p>
     * This method maintains a resolution cache, so once we have returned a result, we will keep on returning the same
     * result without performing any lookups, solely to support {@link #effectiveConfig()}.
     *
     * <p>
     * Note: use of this method implies that {@link #isIgnoringConfig()} is realized with
     *       {@link #isIgnoringConfig(StatementContextBase)}.
     */
    final @NonNull EffectiveConfig effectiveConfig(final ReactorStmtCtx<?, ?, ?> parent) {
        return (flags & HAVE_CONFIG) != 0 ? EFFECTIVE_CONFIGS[flags & MASK_CONFIG] : loadEffectiveConfig(parent);
    }

    private @NonNull EffectiveConfig loadEffectiveConfig(final ReactorStmtCtx<?, ?, ?> parent) {
        final EffectiveConfig parentConfig = parent.effectiveConfig();

        final EffectiveConfig myConfig;
        if (parentConfig != EffectiveConfig.IGNORED && !definition().support().isIgnoringConfig()) {
            final Optional<Boolean> optConfig = findSubstatementArgument(ConfigEffectiveStatement.class);
            if (optConfig.isPresent()) {
                if (optConfig.orElseThrow()) {
                    // Validity check: if parent is config=false this cannot be a config=true
                    InferenceException.throwIf(parentConfig == EffectiveConfig.FALSE, this,
                        "Parent node has config=false, this node must not be specifed as config=true");
                    myConfig = EffectiveConfig.TRUE;
                } else {
                    myConfig = EffectiveConfig.FALSE;
                }
            } else {
                // If "config" statement is not specified, the default is the same as the parent's "config" value.
                myConfig = parentConfig;
            }
        } else {
            myConfig = EffectiveConfig.IGNORED;
        }

        flags = (byte) (flags & ~MASK_CONFIG | HAVE_CONFIG | myConfig.ordinal());
        return myConfig;
    }

    protected abstract boolean isIgnoringConfig();

    /**
     * This method maintains a resolution cache for ignore config, so once we have returned a result, we will
     * keep on returning the same result without performing any lookups. Exists only to support
     * {@link SubstatementContext#isIgnoringConfig()}.
     *
     * <p>
     * Note: use of this method implies that {@link #isConfiguration()} is realized with
     *       {@link #effectiveConfig(StatementContextBase)}.
     */
    final boolean isIgnoringConfig(final StatementContextBase<?, ?, ?> parent) {
        return EffectiveConfig.IGNORED == effectiveConfig(parent);
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

    // These two exists only due to memory optimization, should live in AbstractResumedStatement. We are also reusing
    // this for ReplicaStatementContext's refcount tracking.
    final boolean fullyDefined() {
        return fullyDefined;
    }

    final void setFullyDefined() {
        fullyDefined = true;
    }

    //
    //
    // Common SchemaPath cache. All of this is bound to be removed once YANGTOOLS-1066 is done.
    //
    //

    // Exists only to support {SubstatementContext,InferredStatementContext}.schemaPath()
    @Deprecated
    final @NonNull Optional<SchemaPath> substatementGetSchemaPath() {
        SchemaPath local = schemaPath;
        if (local == null) {
            synchronized (this) {
                local = schemaPath;
                if (local == null) {
                    schemaPath = local = createSchemaPath((StatementContextBase<?, ?, ?>) coerceParentContext());
                }
            }
        }

        return Optional.ofNullable(local);
    }

    @Deprecated
    private SchemaPath createSchemaPath(final StatementContextBase<?, ?, ?> parent) {
        final Optional<SchemaPath> maybeParentPath = parent.schemaPath();
        verify(maybeParentPath.isPresent(), "Parent %s does not have a SchemaPath", parent);
        final SchemaPath parentPath = maybeParentPath.get();

        if (StmtContextUtils.isUnknownStatement(this)) {
            return parentPath.createChild(publicDefinition().getStatementName());
        }
        final Object argument = argument();
        if (argument instanceof QName) {
            final QName qname = (QName) argument;
            if (producesDeclared(UsesStatement.class)) {
                return maybeParentPath.orElse(null);
            }

            return parentPath.createChild(qname);
        }
        if (argument instanceof String) {
            // FIXME: This may yield illegal argument exceptions
            final Optional<StmtContext<A, D, E>> originalCtx = getOriginalCtx();
            final QName qname = StmtContextUtils.qnameFromArgument(originalCtx.orElse(this), (String) argument);
            return parentPath.createChild(qname);
        }
        if (argument instanceof SchemaNodeIdentifier
                && (producesDeclared(AugmentStatement.class) || producesDeclared(RefineStatement.class)
                        || producesDeclared(DeviationStatement.class))) {

            return parentPath.createChild(((SchemaNodeIdentifier) argument).getNodeIdentifiers());
        }

        // FIXME: this does not look right
        return maybeParentPath.orElse(null);
    }

    //
    //
    // Reference counting mechanics start. Please keep these methods in one block for clarity. Note this does not
    // contribute to state visible outside of this package.
    //
    //

    /**
     * Local knowledge of {@link #refcount} values up to statement root. We use this field to prevent recursive lookups
     * in {@link #noParentRefs(StatementContextBase)} -- once we discover a parent reference once, we keep that
     * knowledge and update it when {@link #sweep()} is invoked.
     */
    private byte parentRef = PARENTREF_UNKNOWN;
    private static final byte PARENTREF_UNKNOWN = -1;
    private static final byte PARENTREF_ABSENT  = 0;
    private static final byte PARENTREF_PRESENT = 1;

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

        if (refcount == REFCOUNT_NONE) {
            lastDecRef();
        }
    }

    private void lastDecRef() {
        if (noImplictRef()) {
            // We are no longer guarded by effective instance
            sweepOnDecrement();
            return;
        }

        final byte prevRefs = parentRef;
        if (prevRefs == PARENTREF_ABSENT) {
            // We are the last reference towards root, any children who observed PARENTREF_PRESENT from us need to be
            // updated
            markNoParentRef();
        } else if (prevRefs == PARENTREF_UNKNOWN) {
            // Noone observed our parentRef, just update it
            loadParentRefcount();
        }
    }

    static final void markNoParentRef(final Collection<? extends ReactorStmtCtx<?, ?, ?>> substatements) {
        for (ReactorStmtCtx<?, ?, ?> stmt : substatements) {
            final byte prevRef = stmt.parentRef;
            stmt.parentRef = PARENTREF_ABSENT;
            if (prevRef == PARENTREF_PRESENT && stmt.refcount == REFCOUNT_NONE) {
                // Child thinks it is pinned down, update its perspective
                stmt.markNoParentRef();
            }
        }
    }

    abstract void markNoParentRef();

    static final void sweep(final Collection<? extends ReactorStmtCtx<?, ?, ?>> substatements) {
        for (ReactorStmtCtx<?, ?, ?> stmt : substatements) {
            stmt.sweep();
        }
    }

    /**
     * Sweep this statement context as a result of {@link #sweepSubstatements()}, i.e. when parent is also being swept.
     */
    private void sweep() {
        parentRef = PARENTREF_ABSENT;
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
     * Implementation-specific sweep action. This is expected to perform a recursive {@link #sweep(Collection)} on all
     * {@link #declaredSubstatements()} and {@link #effectiveSubstatements()} and report the result of the sweep
     * operation.
     *
     * <p>
     * {@link #effectiveSubstatements()} as well as namespaces may become inoperable as a result of this operation.
     *
     * @return True if the entire tree has been completely swept, false otherwise.
     */
    abstract int sweepSubstatements();

    // Called when this statement does not have an implicit reference and have reached REFCOUNT_NONE
    private void sweepOnDecrement() {
        LOG.trace("Sweeping on decrement {}", this);
        if (noParentRef()) {
            // No further parent references, sweep our state.
            sweepState();
        }

        // Propagate towards parent if there is one
        final ReactorStmtCtx<?, ?, ?> parent = getParentContext();
        if (parent != null) {
            parent.sweepOnChildDecrement();
        }
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
        if (noParentRef()) {
            LOG.trace("Cleanup {} of parent {}", refcount, this);
            if (sweepState()) {
                final ReactorStmtCtx<?, ?, ?> parent = getParentContext();
                if (parent != null) {
                    parent.sweepOnChildDecrement();
                }
            }
        }
    }

    private boolean noImplictRef() {
        return effectiveInstance != null || !isSupportedToBuildEffective();
    }

    private boolean noParentRef() {
        return parentRefcount() == PARENTREF_ABSENT;
    }

    private byte parentRefcount() {
        final byte refs;
        return (refs = parentRef) != PARENTREF_UNKNOWN ? refs : loadParentRefcount();
    }

    private byte loadParentRefcount() {
        return parentRef = calculateParentRefcount();
    }

    private byte calculateParentRefcount() {
        final ReactorStmtCtx<?, ?, ?> parent = getParentContext();
        if (parent == null) {
            return PARENTREF_ABSENT;
        }
        // There are three possibilities:
        // - REFCOUNT_NONE, in which case we need to search next parent
        // - negative (< REFCOUNT_NONE), meaning parent is in some stage of sweeping, hence it does not have
        //   a reference to us
        // - positive (> REFCOUNT_NONE), meaning parent has an explicit refcount which is holding us down
        final int refs = parent.refcount;
        if (refs == REFCOUNT_NONE) {
            return parent.parentRefcount();
        }
        return refs < REFCOUNT_NONE ? PARENTREF_ABSENT : PARENTREF_PRESENT;
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
            final ReactorStmtCtx<?, ?, ?> parent = getParentContext();
            LOG.trace("Propagating to parent {}", parent);
            if (parent != null && parent.isAwaitingChildren()) {
                parent.sweepOnChildDone();
            }
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
