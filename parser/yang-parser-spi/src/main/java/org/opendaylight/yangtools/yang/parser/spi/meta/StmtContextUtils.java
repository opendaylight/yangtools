/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
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

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> A firstAttributeOf(
            final Iterable<? extends StmtContext<?, ?, ?>> contexts, final Class<D> declaredType) {
        for (var ctx : contexts) {
            if (ctx.producesDeclared(declaredType)) {
                return (A) ctx.argument();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> A firstAttributeOf(final StmtContext<?, ?, ?> ctx,
            final Class<D> declaredType) {
        return ctx.producesDeclared(declaredType) ? (A) ctx.argument() : null;
    }

    public static <A, D extends DeclaredStatement<A>> A firstSubstatementAttributeOf(
            final StmtContext<?, ?, ?> ctx, final Class<D> declaredType) {
        return firstAttributeOf(ctx.allSubstatements(), declaredType);
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> StmtContext<A, ?, ?> findFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        for (var subStmtContext : stmtContext.declaredSubstatements()) {
            if (subStmtContext.producesDeclared(declaredType)) {
                return (StmtContext<A, ?, ?>) subStmtContext;
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

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> Collection<StmtContext<A, D, ?>> findAllDeclaredSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        final var listBuilder = ImmutableList.<StmtContext<A, D, ?>>builder();
        for (var subStmtContext : stmtContext.declaredSubstatements()) {
            if (subStmtContext.producesDeclared(declaredType)) {
                listBuilder.add((StmtContext<A, D, ?>) subStmtContext);
            }
        }
        return listBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> Collection<StmtContext<A, D, ?>> findAllEffectiveSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> type) {
        final var listBuilder = ImmutableList.<StmtContext<A, D, ?>>builder();
        for (var subStmtContext : stmtContext.effectiveSubstatements()) {
            if (subStmtContext.producesDeclared(type)) {
                listBuilder.add((StmtContext<A, D, ?>) subStmtContext);
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

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> StmtContext<A, ?, ?> findFirstEffectiveSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        for (var subStmtContext : stmtContext.effectiveSubstatements()) {
            if (subStmtContext.producesDeclared(declaredType)) {
                return (StmtContext<A, ?, ?>) subStmtContext;
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
            if (isUnknownStatement(parent)) {
                return true;
            }
            current = parent;
        }
    }

    /**
     * Returns true if supplied statement context represents unknown statement,
     * otherwise returns false.
     *
     * @param stmtCtx
     *            statement context to be checked
     * @return true if supplied statement context represents unknown statement,
     *         otherwise false
     * @throws NullPointerException
     *             if supplied statement context is null
     */
    public static boolean isUnknownStatement(final StmtContext<?, ?, ?> stmtCtx) {
        return UnknownStatement.class.isAssignableFrom(stmtCtx.publicDefinition().getDeclaredRepresentationClass());
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
            if (YangStmtMapping.IF_FEATURE.equals(stmt.publicDefinition())) {
                containsIfFeature = true;
                final var argument = (IfFeatureExpr) stmt.getArgument();
                if (argument.test(supportedFeatures)) {
                    isSupported = true;
                } else {
                    isSupported = false;
                    break;
                }
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
                    final Integer minElements = firstSubstatementAttributeOf(stmtCtx, MinElementsStatement.class);
                    yield minElements != null && minElements > 0;
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
     * Checks whether at least one ancestor of a StatementContext matches one from a collection of statement
     * definitions.
     *
     * @param stmt Statement context to be checked
     * @param ancestorTypes collection of statement definitions
     * @return true if at least one ancestor of a StatementContext matches one
     *         from collection of statement definitions, otherwise false.
     */
    public static boolean hasAncestorOfType(final StmtContext<?, ?, ?> stmt,
            final Collection<StatementDefinition> ancestorTypes) {
        requireNonNull(ancestorTypes);
        var current = stmt.getParentContext();
        while (current != null) {
            if (ancestorTypes.contains(current.publicDefinition())) {
                return true;
            }
            current = current.getParentContext();
        }
        return false;
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
     * <p>
     * If one of the list keys contains an if-feature or a when statement in YANG 1.1 model, an exception is thrown.
     *
     * @param ctx statement context to be validated
     */
    public static void validateIfFeatureAndWhenOnListKeys(final StmtContext<?, ?, ?> ctx) {
        if (!isRelevantForIfFeatureAndWhenOnListKeysCheck(ctx)) {
            return;
        }

        final var listStmtCtx = ctx.coerceParentContext();
        final var keyStmtCtx = findFirstDeclaredSubstatement(listStmtCtx, KeyStatement.class);

        if (YangStmtMapping.LEAF.equals(ctx.publicDefinition())) {
            if (isListKey(ctx, keyStmtCtx)) {
                disallowIfFeatureAndWhenOnListKeys(ctx);
            }
        } else if (YangStmtMapping.USES.equals(ctx.publicDefinition())) {
            findAllEffectiveSubstatements(listStmtCtx, LeafStatement.class).forEach(leafStmtCtx -> {
                if (isListKey(leafStmtCtx, keyStmtCtx)) {
                    disallowIfFeatureAndWhenOnListKeys(leafStmtCtx);
                }
            });
        }
    }

    private static boolean isRelevantForIfFeatureAndWhenOnListKeysCheck(final StmtContext<?, ?, ?> ctx) {
        return YangVersion.VERSION_1_1.equals(ctx.yangVersion()) && hasParentOfType(ctx, YangStmtMapping.LIST)
                && findFirstDeclaredSubstatement(ctx.coerceParentContext(), KeyStatement.class) != null;
    }

    private static boolean isListKey(final StmtContext<?, ?, ?> leafStmtCtx,
            final StmtContext<Set<QName>, ?, ?> keyStmtCtx) {
        return keyStmtCtx.getArgument().contains(leafStmtCtx.argument());
    }

    private static void disallowIfFeatureAndWhenOnListKeys(final StmtContext<?, ?, ?> leafStmtCtx) {
        leafStmtCtx.allSubstatements().forEach(leafSubstmtCtx -> {
            final var statementDef = leafSubstmtCtx.publicDefinition();
            SourceException.throwIf(YangStmtMapping.IF_FEATURE.equals(statementDef)
                    || YangStmtMapping.WHEN.equals(statementDef), leafStmtCtx,
                    "%s statement is not allowed in %s leaf statement which is specified as a list key.",
                    statementDef.getStatementName(), leafStmtCtx.argument());
        });
    }

    public static @NonNull QName qnameFromArgument(StmtContext<?, ?, ?> ctx, final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return ctx.publicDefinition().getStatementName();
        }

        QNameModule qnameModule;
        String localName;
        final var namesParts = value.split(":");
        switch (namesParts.length) {
            case 1:
                localName = namesParts[0];
                qnameModule = getModuleQName(ctx);
                break;
            default:
                final var prefix = namesParts[0];
                localName = namesParts[1];
                qnameModule = getModuleQNameByPrefix(ctx, prefix);
                // in case of unknown statement argument, we're not going to parse it
                if (qnameModule == null && isUnknownStatement(ctx)) {
                    localName = value;
                    qnameModule = getModuleQName(ctx);
                }
                if (qnameModule == null && ctx.history().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                    ctx = ctx.getOriginalCtx().orElse(null);
                    qnameModule = getModuleQNameByPrefix(ctx, prefix);
                }
        }

        return internedQName(ctx, ctx.inferNotNull(qnameModule, "Cannot resolve QNameModule for '%s'", value),
            localName);
    }

    /**
     * Parse a YANG identifier string in context of a statement.
     *
     * @param ctx Statement context
     * @param str String to be parsed
     * @return An interned QName
     * @throws NullPointerException if any of the arguments are null
     * @throws SourceException if the string is not a valid YANG identifier
     */
    public static @NonNull QName parseIdentifier(final @NonNull StmtContext<?, ?, ?> ctx, final String str) {
        SourceException.throwIf(str.isEmpty(), ctx, "Identifier may not be an empty string");
        return internedQName(ctx, str);
    }

    public static @NonNull QName parseNodeIdentifier(final @NonNull StmtContext<?, ?, ?> ctx, final String prefix,
            final String localName) {
        return internedQName(ctx,
            ctx.inferNotNull(getModuleQNameByPrefix(ctx, prefix), "Cannot resolve QNameModule for '%s'", prefix),
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
     */
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
        return internedQName(ctx, getModuleQName(ctx), localName);
    }

    private static @NonNull QName internedQName(final @NonNull CommonStmtCtx ctx, final QNameModule module,
            final String localName) {
        final QName template;
        try {
            template = QName.create(module, localName);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid identifier '%s'", localName);
        }
        return template.intern();
    }

    public static @NonNull QNameModule getModuleQName(final @NonNull StmtContext<?, ?, ?> ctx) {
        return getModuleQName(ctx.getRoot());
    }

    public static @NonNull QNameModule getModuleQName(final @NonNull RootStmtContext<?, ?, ?> ctx) {
        if (ctx.producesDeclared(ModuleStatement.class)) {
            return lookupModuleQName(ctx, ctx);
        } else if (ctx.producesDeclared(SubmoduleStatement.class)) {
            final var belongsTo = ctx.namespace(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULECTX);
            if (belongsTo == null || belongsTo.isEmpty()) {
                throw new IllegalArgumentException(ctx + " does not have belongs-to linkage resolved");
            }
            return lookupModuleQName(ctx, belongsTo.values().iterator().next());
        } else {
            throw new IllegalArgumentException("Unsupported root " + ctx);
        }
    }

    private static @NonNull QNameModule lookupModuleQName(final NamespaceStmtCtx storage,
            final StmtContext<?, ?, ?> module) {
        final var ret = storage.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, module);
        if (ret == null) {
            throw new IllegalArgumentException("Failed to look up QNameModule for " + module + " in " + storage);
        }
        return ret;
    }

    public static QNameModule getModuleQNameByPrefix(final StmtContext<?, ?, ?> ctx, final String prefix) {
        final var root = ctx.getRoot();
        final var importedModule = root.namespaceItem(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, prefix);
        final var qnameModule = ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, importedModule);
        if (qnameModule != null) {
            return qnameModule;
        }

        if (root.producesDeclared(SubmoduleStatement.class)) {
            return ctx.namespaceItem(ParserNamespaces.MODULE_NAME_TO_QNAME,
                root.namespaceItem(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME, prefix));
        }

        return null;
    }

    public static Optional<Revision> getLatestRevision(final Iterable<? extends StmtContext<?, ?, ?>> subStmts) {
        Revision revision = null;
        for (var subStmt : subStmts) {
            if (subStmt.producesDeclared(RevisionStatement.class)) {
                if (revision == null && subStmt.argument() != null) {
                    revision = (Revision) subStmt.argument();
                } else {
                    final var subArg = (Revision) subStmt.argument();
                    if (subArg != null && subArg.compareTo(revision) > 0) {
                        revision = subArg;
                    }
                }
            }
        }
        return Optional.ofNullable(revision);
    }
}
