/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.ExecutionOrder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
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
        extends AbstractNamespaceStorage implements Mutable<A, D, E>, Current<A, D> {
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

    /**
     * Effective instance built from this context. This field as dual types. Under normal circumstances in matches the
     * {@link #buildEffective()} instance. If this context is reused, it can be inflated to {@link EffectiveInstances}
     * and also act as a common instance reuse site.
     */
    private @Nullable Object effectiveInstance;

    // Master flag controlling whether this context can yield an effective statement
    // FIXME: investigate the mechanics that are being supported by this, as it would be beneficial if we can get rid
    //        of this flag -- eliminating the initial alignment shadow used by below gap-filler fields.
    private boolean isSupportedToBuildEffective = true;

    // EffectiveConfig mapping
    private static final int MASK_CONFIG                = 0x03;
    private static final int HAVE_CONFIG                = 0x04;
    // Effective instantiation mechanics for StatementContextBase: if this flag is set all substatements are known not
    // change when instantiated. This includes context-independent statements as well as any statements which are
    // ignored during copy instantiation.
    private static final int ALL_INDEPENDENT            = 0x08;
    // Flag bit assignments
    private static final int IS_SUPPORTED_BY_FEATURES   = 0x10;
    private static final int HAVE_SUPPORTED_BY_FEATURES = 0x20;
    private static final int IS_IGNORE_IF_FEATURE       = 0x40;
    private static final int HAVE_IGNORE_IF_FEATURE     = 0x80;
    // Have-and-set flag constants, also used as masks
    private static final int SET_SUPPORTED_BY_FEATURES  = HAVE_SUPPORTED_BY_FEATURES | IS_SUPPORTED_BY_FEATURES;
    private static final int SET_IGNORE_IF_FEATURE      = HAVE_IGNORE_IF_FEATURE | IS_IGNORE_IF_FEATURE;

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

    ReactorStmtCtx() {
        // Empty on purpose
    }

    ReactorStmtCtx(final ReactorStmtCtx<A, D, E> original) {
        isSupportedToBuildEffective = original.isSupportedToBuildEffective;
        flags = original.flags;
    }

    // Used by ReplicaStatementContext only
    ReactorStmtCtx(final ReactorStmtCtx<A, D, E> original, final Void dummy) {
        isSupportedToBuildEffective = original.isSupportedToBuildEffective;
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
    public abstract Collection<? extends @NonNull StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements();

    @Override
    final <K, V> NamespaceAccess<K, V> accessNamespace(final ParserNamespace<K, V> type) {
        return getRoot().getSourceContext().accessNamespace(type);
    }

    @Override
    public final YangVersion yangVersion() {
        return getRoot().getRootVersionImpl();
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
    public final QName moduleName() {
        final var root = getRoot();
        return QName.create(StmtContextUtils.getModuleQName(root), root.getRawArgument());
    }

    //
    // In the next two methods we are looking for an effective statement. If we already have an effective instance,
    // defer to it's implementation of the equivalent search. Otherwise we search our substatement contexts.
    //
    // Note that the search function is split, so as to allow InferredStatementContext to do its own thing first.
    //

    @Override
    public final <X, Z extends EffectiveStatement<X, ?>> @NonNull Optional<X> findSubstatementArgument(
            final @NonNull Class<Z> type) {
        final E existing = effectiveInstance();
        return existing != null ? existing.findFirstEffectiveSubstatementArgument(type)
            : findSubstatementArgumentImpl(type);
    }

    @Override
    public final boolean hasSubstatement(final @NonNull Class<? extends EffectiveStatement<?, ?>> type) {
        final E existing = effectiveInstance();
        return existing != null ? existing.findFirstEffectiveSubstatement(type).isPresent() : hasSubstatementImpl(type);
    }

    private E effectiveInstance() {
        final Object existing = effectiveInstance;
        return existing != null ? EffectiveInstances.local(existing) : null;
    }

    // Visible due to InferredStatementContext's override. At this point we do not have an effective instance available.
    <X, Z extends EffectiveStatement<X, ?>> @NonNull Optional<X> findSubstatementArgumentImpl(
            final @NonNull Class<Z> type) {
        return allSubstatementsStream()
            .filter(ctx -> ctx.isSupportedToBuildEffective() && ctx.producesEffective(type))
            .findAny()
            .map(ctx -> (X) ctx.getArgument());
    }

    // Visible due to InferredStatementContext's override. At this point we do not have an effective instance available.
    boolean hasSubstatementImpl(final @NonNull Class<? extends EffectiveStatement<?, ?>> type) {
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
        return toStringHelper.add("definition", definition()).add("argument", argument()).add("refCount", refString());
    }

    private String refString() {
        final int current = refcount;
        return switch (current) {
            case REFCOUNT_DEFUNCT -> "DEFUNCT";
            case REFCOUNT_SWEEPING -> "SWEEPING";
            case REFCOUNT_SWEPT -> "SWEPT";
            default -> String.valueOf(refcount);
        };
    }

    /**
     * Return the context in which this statement was defined.
     *
     * @return statement definition
     */
    abstract @NonNull StatementDefinitionContext<A, D, E> definition();

    //
    //
    // AbstractNamespaceStorage/Mutable integration methods. Keep these together.
    //
    //

    @Override
    public StorageType getStorageType() {
        // Common to all subclasses except RootStatementContext
        return StorageType.STATEMENT_LOCAL;
    }

    @Override
    public final <K, V> V namespaceItem(final ParserNamespace<K, V> namespace, final K key) {
        return accessNamespace(namespace).valueFrom(this, key);
    }

    @Override
    public final <K, V> Map<K, V> namespace(final ParserNamespace<K, V> namespace) {
        return getNamespace(namespace);
    }

    @Override
    public final <K, V> Map<K, V> localNamespacePortion(final ParserNamespace<K, V> namespace) {
        return getLocalNamespace(namespace);
    }

    @Override
    protected <K, V> void onNamespaceElementAdded(final ParserNamespace<K, V> type, final K key, final V value) {
        // definition().onNamespaceElementAdded(this, type, key, value);
    }

    /**
     * Return the effective statement view of a copy operation. This method may return one of:
     * <ul>
     *   <li>{@code this}, when the effective view did not change</li>
     *   <li>an InferredStatementContext, when there is a need for inference-equivalent copy</li>
     *   <li>{@code null}, when the statement failed to materialize</li>
     * </ul>
     *
     * @param parent Proposed new parent
     * @param type Copy operation type
     * @param targetModule New target module
     * @return {@link ReactorStmtCtx} holding effective view
     */
    abstract @Nullable ReactorStmtCtx<?, ?, ?> asEffectiveChildOf(StatementContextBase<?, ?, ?> parent, CopyType type,
        QNameModule targetModule);

    @Override
    public final ReplicaStatementContext<A, D, E> replicaAsChildOf(final Mutable<?, ?, ?> parent) {
        checkArgument(parent instanceof StatementContextBase, "Unsupported parent %s", parent);
        final var ret = replicaAsChildOf((StatementContextBase<?, ?, ?>) parent);
        definition().onStatementAdded(ret);
        return ret;
    }

    abstract @NonNull ReplicaStatementContext<A, D, E> replicaAsChildOf(@NonNull StatementContextBase<?, ?, ?> parent);

    //
    //
    // Statement build entry points -- both public and package-private.
    //
    //

    @Override
    public final E buildEffective() {
        final Object existing;
        return (existing = effectiveInstance) != null ? EffectiveInstances.local(existing) : loadEffective();
    }

    private @NonNull E loadEffective() {
        final E ret = createEffective();
        effectiveInstance = ret;
        // we have called createEffective(), substatements are no longer guarded by us. Let's see if we can clear up
        // some residue.
        if (refcount == REFCOUNT_NONE) {
            sweepOnDecrement();
        }
        return ret;
    }

    abstract @NonNull E createEffective();

    /**
     * Routing of the request to build an effective statement from {@link InferredStatementContext} towards the original
     * definition site. This is needed to pick the correct instantiation method: for declared statements we will
     * eventually land in {@link AbstractResumedStatement}, for underclared statements that will be
     * {@link UndeclaredStmtCtx}.
     *
     * @param factory Statement factory
     * @param ctx Inferred statement context, i.e. where the effective statement is instantiated
     * @return Built effective stateue
     */
    abstract @NonNull E createInferredEffective(@NonNull StatementFactory<A, D, E> factory,
        @NonNull InferredStatementContext<A, D, E> ctx, Stream<? extends ReactorStmtCtx<?, ?, ?>> declared,
        Stream<? extends ReactorStmtCtx<?, ?, ?>> effective);

    /**
     * Attach an effective copy of this statement. This essentially acts as a map, where we make a few assumptions:
     * <ul>
     *   <li>{@code copy} and {@code this} statement share {@link #getOriginalCtx()} if it exists</li>
     *   <li>{@code copy} did not modify any statements relative to {@code this}</li>
     * </ul>
     *
     * @param state effective statement state, acting as a lookup key
     * @param stmt New copy to append
     * @return {@code stmt} or a previously-created instances with the same {@code state}
     */
    @SuppressWarnings("unchecked")
    final @NonNull E attachEffectiveCopy(final @NonNull EffectiveStatementState state, final @NonNull E stmt) {
        final Object local = effectiveInstance;
        final EffectiveInstances<E> instances;
        if (local instanceof EffectiveInstances) {
            instances = (EffectiveInstances<E>) local;
        } else {
            effectiveInstance = instances = new EffectiveInstances<>((E) local);
        }
        return instances.attachCopy(state, stmt);
    }

    /**
     * Walk this statement's copy history and return the statement closest to original which has not had its effective
     * statements modified. This statement and returned substatement logically have the same set of substatements, hence
     * share substatement-derived state.
     *
     * @return Closest {@link ReactorStmtCtx} with equivalent effective substatements
     */
    abstract @NonNull ReactorStmtCtx<A, D, E> unmodifiedEffectiveSource();

    @Override
    public final ModelProcessingPhase getCompletedPhase() {
        return ModelProcessingPhase.ofExecutionOrder(executionOrder());
    }

    abstract byte executionOrder();

    /**
     * Try to execute current {@link ModelProcessingPhase} of source parsing. If the phase has already been executed,
     * this method does nothing. This must not be called with {@link ExecutionOrder#NULL}.
     *
     * @param phase to be executed (completed)
     * @return true if phase was successfully completed
     * @throws SourceException when an error occurred in source parsing
     */
    final boolean tryToCompletePhase(final byte executionOrder) {
        return executionOrder() >= executionOrder || doTryToCompletePhase(executionOrder);
    }

    abstract boolean doTryToCompletePhase(byte targetOrder);

    //
    //
    // Flags-based mechanics. These include public interfaces as well as all the crud we have lurking in our alignment
    // shadow.
    //
    //

    // Non-final for ImplicitStmtCtx/InferredStatementContext
    @Override
    public boolean isSupportedToBuildEffective() {
        return isSupportedToBuildEffective;
    }

    @Override
    public final void setUnsupported() {
        isSupportedToBuildEffective = false;
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

        // If parent is supported, we need to check if-features statements of this context.
        if (isParentSupportedByFeatures() && computeSupportedByFeatures()) {
            flags |= SET_SUPPORTED_BY_FEATURES;
            return true;
        }

        // Either parent is not supported or this statement is not supported
        flags |= HAVE_SUPPORTED_BY_FEATURES;
        return false;
    }

    /**
     * Compute whether this statement is supported by features. Returned value is combined with
     * {@link #isParentSupportedByFeatures()} and cached.
     *
     * @return {@code true} if the current feature set matches {@code if-feature} of this statement
     */
    abstract boolean computeSupportedByFeatures();

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

    // These two exist only for StatementContextBase. Since we are squeezed for size, with only a single bit available
    // in flags, we default to 'false' and only set the flag to true when we are absolutely sure -- and all other cases
    // err on the side of caution by taking the time to evaluate each substatement separately.
    final boolean allSubstatementsContextIndependent() {
        return (flags & ALL_INDEPENDENT) != 0;
    }

    final void setAllSubstatementsContextIndependent() {
        flags |= ALL_INDEPENDENT;
    }

    //
    //
    // Various functionality from AbstractTypeStatementSupport. This used to work on top of SchemaPath, now it still
    // lives here. Ultimate future is either proper graduation or (more likely) move to AbstractTypeStatementSupport.
    //
    //

    @Override
    public final QName argumentAsTypeQName() {
        // FIXME: This may yield illegal argument exceptions
        return StmtContextUtils.qnameFromArgument(getOriginalCtx().orElse(this), getRawArgument());
    }

    @Override
    public final QNameModule effectiveNamespace() {
        if (StmtContextUtils.isUnknownStatement(this)) {
            return publicDefinition().getStatementName().getModule();
        }
        if (producesDeclared(UsesStatement.class)) {
            return coerceParent().effectiveNamespace();
        }

        final Object argument = argument();
        if (argument instanceof QName qname) {
            return qname.getModule();
        }
        if (argument instanceof String str) {
            // FIXME: This may yield illegal argument exceptions
            return StmtContextUtils.qnameFromArgument(getOriginalCtx().orElse(this), str).getModule();
        }
        if (argument instanceof SchemaNodeIdentifier sni
                && (producesDeclared(AugmentStatement.class) || producesDeclared(RefineStatement.class)
                        || producesDeclared(DeviationStatement.class))) {
            return sni.lastNodeIdentifier().getModule();
        }

        return coerceParent().effectiveNamespace();
    }

    private ReactorStmtCtx<?, ?, ?> coerceParent() {
        return (ReactorStmtCtx<?, ?, ?>) coerceParentContext();
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
            // FIXME: add a global 'warn once' flag
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

    /**
     * Return {@code true} if this context has no outstanding references.
     *
     * @return True if this context has no outstanding references.
     */
    final boolean noRefs() {
        final int local = refcount;
        return local < REFCOUNT_NONE || local == REFCOUNT_NONE && noParentRef();
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
        sweepParent();
    }

    private void sweepParent() {
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
            LOG.trace("Cleanup {} of parent {}", refs, this);
            if (sweepState()) {
                sweepParent();
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
        return parent == null ? PARENTREF_ABSENT : parent.refcountForChild();
    }

    private byte refcountForChild() {
        // A slight wrinkle here is that our machinery handles only PRESENT -> ABSENT invalidation and we can reach here
        // while inference is still ongoing and hence we may not have a complete picture about existing references. We
        // could therefore end up caching an ABSENT result and then that information becoming stale as a new reference
        // is introduced.
        if (executionOrder() < ExecutionOrder.EFFECTIVE_MODEL) {
            return PARENTREF_UNKNOWN;
        }

        // There are three possibilities:
        // - REFCOUNT_NONE, in which case we need to check if this statement or its parents are holding a reference
        // - negative (< REFCOUNT_NONE), meaning parent is in some stage of sweeping, hence it does not have
        //   a reference to us
        // - positive (> REFCOUNT_NONE), meaning parent has an explicit refcount which is holding us down
        final int refs = refcount;
        if (refs == REFCOUNT_NONE) {
            return noImplictRef() && noParentRef() ? PARENTREF_ABSENT : PARENTREF_PRESENT;
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
            // FIXME: add a global 'warn once' flag
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
