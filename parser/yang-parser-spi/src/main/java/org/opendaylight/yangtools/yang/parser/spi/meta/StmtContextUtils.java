/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class StmtContextUtils {
    private StmtContextUtils() {
        // Hidden on purpose
    }

    public static <A, D extends DeclaredStatement<A>> @Nullable A firstAttributeOf(
            final Iterable<? extends @NonNull StmtContext<?, ?, ?>> contexts, final Class<D> declaredType) {
        for (var ctx : contexts) {
            final var declaring = ctx.tryDeclaring(declaredType);
            if (declaring != null) {
                return declaring.getArgument();
            }
        }
        return null;
    }

    public static <A, D extends DeclaredStatement<A>> @Nullable A firstAttributeOf(final StmtContext<?, ?, ?> ctx,
            final Class<D> declaredType) {
        final var declaring = ctx.tryDeclaring(declaredType);
        return declaring == null ? null : declaring.getArgument();
    }

    public static <A, D extends DeclaredStatement<A>> @Nullable A firstSubstatementAttributeOf(
            final StmtContext<?, ?, ?> ctx, final Class<D> declaredType) {
        return firstAttributeOf(ctx.allSubstatements(), declaredType);
    }

    public static <A, D extends DeclaredStatement<A>> @Nullable StmtContext<A, D, ?> findFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        for (var subStmtContext : stmtContext.declaredSubstatements()) {
            final var declaring = subStmtContext.tryDeclaring(declaredType);
            if (declaring != null) {
                return declaring;
            }
        }
        return null;
    }

    @SafeVarargs
    public static StmtContext<?, ?, ?> findFirstDeclaredSubstatement(final StmtContext<?, ?, ?> stmtContext,
            int startIndex, final Class<? extends DeclaredStatement<?>>... types) {
        if (startIndex >= types.length) {
            return null;
        }

        for (var subStmtContext : stmtContext.declaredSubstatements()) {
            if (subStmtContext.producesDeclared(types[startIndex])) {
                return startIndex + 1 == types.length ? subStmtContext : findFirstDeclaredSubstatement(subStmtContext,
                        ++startIndex, types);
            }
        }
        return null;
    }

    public static <A, D extends DeclaredStatement<A>> Collection<StmtContext<A, D, ?>> findAllDeclaredSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        final var listBuilder = ImmutableList.<StmtContext<A, D, ?>>builder();
        for (var subStmtContext : stmtContext.declaredSubstatements()) {
            final var declaring = subStmtContext.tryDeclaring(declaredType);
            if (declaring != null) {
                listBuilder.add(declaring);
            }
        }
        return listBuilder.build();
    }

    public static <A, D extends DeclaredStatement<A>> Collection<StmtContext<A, D, ?>> findAllEffectiveSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> type) {
        final var listBuilder = ImmutableList.<StmtContext<A, D, ?>>builder();
        for (var subStmtContext : stmtContext.effectiveSubstatements()) {
            final var declaring = subStmtContext.tryDeclaring(type);
            if (declaring != null) {
                listBuilder.add(declaring);
            }
        }
        return listBuilder.build();
    }

    public static <A, D extends DeclaredStatement<A>> Collection<StmtContext<A, D, ?>> findAllSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> type) {
        return ImmutableList.<StmtContext<A, D, ?>>builder()
            .addAll(findAllDeclaredSubstatements(stmtContext, type))
            .addAll(findAllEffectiveSubstatements(stmtContext, type))
            .build();
    }

    public static <A, D extends DeclaredStatement<A>> StmtContext<A, D, ?> findFirstEffectiveSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        for (var subStmtContext : stmtContext.effectiveSubstatements()) {
            final var declaring = subStmtContext.tryDeclaring(declaredType);
            if (declaring != null) {
                return declaring;
            }
        }
        return null;
    }

    public static <D extends DeclaredStatement<?>> StmtContext<?, ?, ?> findFirstDeclaredSubstatementOnSublevel(
            final StmtContext<?, ?, ?> stmtContext, final Class<? super D> declaredType, int sublevel) {
        for (var subStmtContext : stmtContext.declaredSubstatements()) {
            if (sublevel == 1 && subStmtContext.producesDeclared(declaredType)) {
                return subStmtContext;
            }
            if (sublevel > 1) {
                final var result = findFirstDeclaredSubstatementOnSublevel(subStmtContext, declaredType, --sublevel);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static <D extends DeclaredStatement<?>> StmtContext<?, ?, ?> findDeepFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<? super D> declaredType) {
        for (final var subStmtContext : stmtContext.declaredSubstatements()) {
            if (subStmtContext.producesDeclared(declaredType)) {
                return subStmtContext;
            }

            final var result = findDeepFirstDeclaredSubstatement(subStmtContext, declaredType);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    // FIXME: 8.0.0: This method goes back as far as YANGTOOLS-365, when we were build EffectiveStatements for
    //               unsupported YANG extensions. We are not doing that anymore, do we still need this method? Also, it
    //               is only used in augment support to disable mechanics on unknown nodes.
    //
    //               It would seem we can move this method to AbstractAugmentStatementSupport at the very least, but
    //               also: augments are defined to operate on schema tree nodes, hence even if we have an
    //               UnknownStatement, but its EffectiveStatement projection supports SchemaTreeAwareEffectiveStatement
    //               we should operate normally -- the StatementSupport exposing such semantics is responsible for
    //               arranging the backend details.
    public static boolean isInExtensionBody(final StmtContext<?, ?, ?> stmtCtx) {
        var current = stmtCtx;

        while (true) {
            final var parent = current.coerceParentContext();
            if (parent.getParentContext() == null) {
                return false;
            }
            if (parent.producesDeclared(UnknownStatement.class)) {
                return true;
            }
            current = parent;
        }
    }

    /**
     * Returns true if supplied statement context represents unknown statement, otherwise returns false.
     *
     * @param stmtCtx statement context to be checked
     * @return true if supplied statement context represents unknown statement, otherwise false
     * @throws NullPointerException if supplied statement context is null
     * @deprecated Use {@code stmtCtx.producesDeclared(UnknownStatement.class)} instead
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    public static boolean isUnknownStatement(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.producesDeclared(UnknownStatement.class);
    }

    /**
     * Evaluate {@code if-feature} substatement of a statement and indicate whether they result in the statement being
     * supported.
     *
     * @param stmt Parent statement
     * @return {@code true} if the statement is indicated to be supported under currently-supported features
     */
    public static boolean evaluateIfFeatures(final @NonNull StmtContext<?, ?, ?> stmt) {
        final var supportedFeatures = stmt.namespaceItem(ParserNamespaces.SUPPORTED_FEATURES, Empty.value());
        return supportedFeatures == null || checkFeatureSupport(stmt, supportedFeatures);
    }

    public static boolean checkFeatureSupport(final StmtContext<?, ?, ?> stmtContext,
            final FeatureSet supportedFeatures) {
        boolean isSupported = false;
        boolean containsIfFeature = false;
        for (var stmt : stmtContext.declaredSubstatements()) {
            final var declaring = stmt.tryDeclaring(IfFeatureStatement.class);
            if (declaring != null) {
                containsIfFeature = true;
                if (!declaring.getArgument().test(supportedFeatures)) {
                    isSupported = false;
                    break;
                }
                isSupported = true;
            }
        }

        return !containsIfFeature || isSupported;
    }

    /**
     * Checks whether statement context is a presence container or not.
     *
     * @param stmtCtx
     *            statement context
     * @return true if it is a presence container
     */
    public static boolean isPresenceContainer(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.publicDefinition() == YangStmtMapping.CONTAINER && containsPresenceSubStmt(stmtCtx);
    }

    /**
     * Checks whether statement context is a non-presence container or not.
     *
     * @param stmtCtx
     *            statement context
     * @return true if it is a non-presence container
     */
    public static boolean isNonPresenceContainer(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.publicDefinition() == YangStmtMapping.CONTAINER && !containsPresenceSubStmt(stmtCtx);
    }

    private static boolean containsPresenceSubStmt(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.hasSubstatement(PresenceEffectiveStatement.class);
    }

    /**
     * Checks whether statement context is a mandatory leaf, choice, anyxml,
     * list or leaf-list according to RFC6020 or not.
     *
     * @param stmtCtx
     *            statement context
     * @return true if it is a mandatory leaf, choice, anyxml, list or leaf-list
     *         according to RFC6020.
     */
    public static boolean isMandatoryNode(final StmtContext<?, ?, ?> stmtCtx) {
        if (stmtCtx.publicDefinition() instanceof YangStmtMapping mapping) {
            return switch (mapping) {
                case LEAF, CHOICE, ANYXML -> Boolean.TRUE.equals(
                    firstSubstatementAttributeOf(stmtCtx, MandatoryStatement.class));
                case LIST, LEAF_LIST -> {
                    final var minElements = firstSubstatementAttributeOf(stmtCtx, MinElementsStatement.class);
                    yield minElements != null && minElements.lowerInt() > -1;
                }
                default -> false;
            };
        }
        return false;
    }

    /**
     * Checks whether a statement context is a statement of supplied statement
     * definition and whether it is not mandatory leaf, choice, anyxml, list or
     * leaf-list according to RFC6020.
     *
     * @param stmtCtx
     *            statement context
     * @param stmtDef
     *            statement definition
     * @return true if supplied statement context is a statement of supplied
     *         statement definition and if it is not mandatory leaf, choice,
     *         anyxml, list or leaf-list according to RFC6020
     */
    public static boolean isNotMandatoryNodeOfType(final StmtContext<?, ?, ?> stmtCtx,
            final StatementDefinition stmtDef) {
        return stmtCtx.publicDefinition().equals(stmtDef) && !isMandatoryNode(stmtCtx);
    }

    /**
     * Check whether all of StmtContext's {@code list} ancestors have a {@code key}.
     *
     * @param stmt EffectiveStmtCtx to be checked
     * @param name Human-friendly statement name
     * @throws SourceException if there is any keyless list ancestor
     */
    public static void validateNoKeylessListAncestorOf(final Mutable<?, ?, ?> stmt, final String name) {
        requireNonNull(stmt);

        // We do not expect this to by typically populated
        final var incomplete = new ArrayList<Mutable<?, ?, ?>>(0);

        var current = stmt.coerceParentContext();
        var parent = current.getParentContext();
        while (parent != null) {
            if (YangStmtMapping.LIST == current.publicDefinition()
                    && !current.hasSubstatement(KeyEffectiveStatement.class)) {
                if (ModelProcessingPhase.FULL_DECLARATION.isCompletedBy(current.getCompletedPhase())) {
                    throw new SourceException(stmt, "%s %s is defined within a list that has no key statement", name,
                        stmt.argument());
                }

                // Ancestor has not completed full declaration yet missing 'key' statement may materialize later
                incomplete.add(current);
            }

            current = parent;
            parent = current.getParentContext();
        }

        // Deal with whatever incomplete ancestors we encountered
        for (var ancestor : incomplete) {
            // This check must complete during the ancestor's FULL_DECLARATION phase, i.e. the ancestor must not reach
            // EFFECTIVE_MODEL until it is done.
            final var action = ancestor.newInferenceAction(ModelProcessingPhase.FULL_DECLARATION);
            action.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    if (!ancestor.hasSubstatement(KeyEffectiveStatement.class)) {
                        throw new SourceException(stmt, "%s %s is defined within a list that has no key statement",
                            name, stmt.argument());
                    }
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    throw new VerifyException("Should never happen");
                }
            });
        }
    }

    /**
     * Checks whether the parent of StmtContext is of specified type.
     *
     * @param ctx StmtContext to be checked
     * @param parentType type of parent to check
     * @return true if the parent of StmtContext is of specified type, otherwise false
     */
    public static boolean hasParentOfType(final StmtContext<?, ?, ?> ctx, final StatementDefinition parentType) {
        requireNonNull(parentType);
        final var parentContext = ctx.getParentContext();
        return parentContext != null && parentType.equals(parentContext.publicDefinition());
    }

    /**
     * Validates the specified statement context with regards to if-feature and when statement on list keys.
     * The context can either be a leaf which is defined directly in the substatements of a keyed list or a uses
     * statement defined in a keyed list (a uses statement may add leaves into the list).
     *
     * <p>If one of the list keys contains an if-feature or a when statement in YANG 1.1 model, an exception is thrown.
     *
     * @param ctx statement context to be validated
     */
    public static void validateIfFeatureAndWhenOnListKeys(final StmtContext<?, ?, ?> ctx) {
        // Preliminary checks
        if (YangVersion.VERSION_1_1 != ctx.yangVersion()) {
            return;
        }
        final var parent = ctx.getParentContext();
        if (parent == null) {
            return;
        }
        final var listCtx = parent.tryDeclaring(ListStatement.class);
        if (listCtx == null) {
            return;
        }
        final var keyCtx = findFirstDeclaredSubstatement(listCtx, KeyStatement.class);
        if (keyCtx == null) {
            return;
        }
        final var keyArg = keyCtx.argument();

        // deal with the case of a single leaf
        final var leafCtx = ctx.tryDeclaring(LeafStatement.class);
        if (leafCtx != null) {
            validateIfFeatureOnLeaf(listCtx, keyArg, leafCtx);
            return;
        }

        // otherwise deal with the case of a uses statement
        final var usesCtx = ctx.tryDeclaring(UsesStatement.class);
        if (usesCtx != null) {
            for (var subStmtContext : listCtx.effectiveSubstatements()) {
                final var declaring = subStmtContext.tryDeclaring(LeafStatement.class);
                if (declaring != null) {
                    validateIfFeatureOnLeaf(listCtx, keyArg, declaring);
                }
            }
        }
    }

    private static void validateIfFeatureOnLeaf(final StmtContext<QName, ListStatement, ?> listCtx,
            final KeyArgument keyArg, final StmtContext<QName, LeafStatement, ?> leafCtx) {
        // check if the leaf is part of the argument
        if (!keyArg.contains(leafCtx.getArgument())) {
            return;
        }

        // collect all offending statements
        final var it = leafCtx.allSubstatementsStream()
            .filter(stmt -> {
                final var repr = stmt.publicDefinition().declaredRepresentation();
                return IfFeatureStatement.class.isAssignableFrom(repr)
                    || WhenStatement.class.isAssignableFrom(repr);
            })
            .toList()
            .iterator();

        // check if we are happy
        if (!it.hasNext()) {
            return;
        }

        final var ex = reportConditionalLeaf(listCtx, leafCtx, it.next());
        it.forEachRemaining(stmt -> ex.addSuppressed(reportConditionalLeaf(listCtx, leafCtx, stmt)));
        throw ex;
    }

    private static @NonNull SourceException reportConditionalLeaf(final StmtContext<QName, ListStatement, ?> listCtx,
            final StmtContext<QName, LeafStatement, ?> leafCtx, final StmtContext<?, ?, ?> offender) {
        return new SourceException(leafCtx,
            "leaf statement %s is a key in list statement %s: it cannot be conditional on %s statement",
            leafCtx.argument(), listCtx.argument(), offender.publicDefinition().humanName());
    }

    /**
     * Parse a YANG identifier string in context of a statement.
     *
     * @param ctx Statement context
     * @param str String to be parsed
     * @return An interned QName
     * @throws NullPointerException if any of the arguments are null
     * @throws SourceException if the string is not a valid YANG identifier
     * @deprecated Use {@link StmtContext#identifierBinding()} instead
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    public static @NonNull QName parseIdentifier(final @NonNull StmtContext<?, ?, ?> ctx, final String str) {
        SourceException.throwIf(str.isEmpty(), ctx, "Identifier may not be an empty string");
        return internedQName(ctx, str);
    }

    @Deprecated(since = "15.0.0", forRemoval = true)
    public static @NonNull QName parseNodeIdentifier(final @NonNull StmtContext<?, ?, ?> ctx, final String prefix,
            final String localName) {
        return internedQName(ctx,
            InferenceException.throwIfNull(getModuleQNameByPrefix(ctx, prefix), ctx,
                "Cannot resolve QNameModule for '%s'", prefix),
            localName);
    }

    /**
     * Parse a YANG node identifier string in context of a statement.
     *
     * @param ctx Statement context
     * @param str String to be parsed
     * @return An interned QName
     * @throws NullPointerException if any of the arguments are null
     * @throws SourceException if the string is not a valid YANG node identifier
     * @deprecated Use {@link StmtContext#identifierBinding()} instead
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    public static @NonNull QName parseNodeIdentifier(final @NonNull StmtContext<?, ?, ?> ctx, final String str) {
        SourceException.throwIf(str.isEmpty(), ctx, "Node identifier may not be an empty string");

        final int colon = str.indexOf(':');
        if (colon == -1) {
            return internedQName(ctx, str);
        }

        final var prefix = str.substring(0, colon);
        SourceException.throwIf(prefix.isEmpty(), ctx, "String '%s' has an empty prefix", str);
        final var localName = str.substring(colon + 1);
        SourceException.throwIf(localName.isEmpty(), ctx, "String '%s' has an empty identifier", str);

        return parseNodeIdentifier(ctx, prefix, localName);
    }

    private static @NonNull QName internedQName(final @NonNull StmtContext<?, ?, ?> ctx, final String localName) {
        return internedQName(ctx, ctx.definingModule(), localName);
    }

    @Beta
    public static @NonNull QName internedQName(final @NonNull CommonStmtCtx ctx, final QNameModule module,
            final String localName) {
        final QName template;
        try {
            template = QName.create(module, localName);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid identifier '%s'", localName);
        }
        return template.intern();
    }

    /**
     * Return the {@link QNameModule} corresponding to a prefix in the specified {@link StmtContext}. The lookup
     * consults {@code import} and {@code belongs-to} statements.
     *
     * @param ctx the {@link StmtContext}
     * @param prefix the prefix
     * @return the {@link QNameModule}, or {@code null} if not found
     */
    private static @Nullable QNameModule getModuleQNameByPrefix(final @NonNull StmtContext<?, ?, ?> ctx,
            final String prefix) {
        return getModuleQNameByPrefix(ctx.getRoot(), prefix);
    }

    /**
     * Return the {@link QNameModule} corresponding to a prefix in the specified {@link RootStmtContext}. The lookup
     * consults {@code import} and {@code belongs-to} statements.
     *
     * @param ctx the {@link RootStmtContext}
     * @param prefix the prefix
     * @return the {@link QNameModule}, or {@code null} if not found
     */
    // FIXME: 15.0.0: hide/relocate this method?
    public static @Nullable QNameModule getModuleQNameByPrefix(final @NonNull RootStmtContext<?, ?, ?> ctx,
            final String prefix) {
        final var importedModule = ctx.namespaceItem(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, prefix);
        final var qnameModule = ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, importedModule);
        if (qnameModule != null) {
            return qnameModule;
        }

        // This is a submodule, so we also need consult 'belongs-to' mapping
        if (ctx.producesDeclared(SubmoduleStatement.class)) {
            return ctx.namespaceItem(ParserNamespaces.MODULE_NAME_TO_QNAME,
                ctx.namespaceItem(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME, prefix));
        }

        return null;
    }

    public static @Nullable Revision latestRevisionIn(final Collection<? extends @NonNull StmtContext<?, ?, ?>> stmts) {
        Revision revision = null;
        for (var subStmt : stmts) {
            final var revStmt = subStmt.tryDeclaring(RevisionStatement.class);
            if (revStmt != null) {
                final var rev = revStmt.argument();
                if (Revision.compare(rev, revision) > 0) {
                    revision = rev;
                }
            }
        }
        return revision;
    }
}
