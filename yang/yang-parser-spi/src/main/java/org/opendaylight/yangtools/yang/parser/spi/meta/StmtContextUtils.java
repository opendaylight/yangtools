/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class StmtContextUtils {
    private StmtContextUtils() {
        // Hidden on purpose
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> A firstAttributeOf(
            final Iterable<? extends StmtContext<?, ?, ?>> contexts, final Class<D> declaredType) {
        for (final StmtContext<?, ?, ?> ctx : contexts) {
            if (ctx.producesDeclared(declaredType)) {
                return (A) ctx.getStatementArgument();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> A firstAttributeOf(final StmtContext<?, ?, ?> ctx,
            final Class<D> declaredType) {
        return ctx.producesDeclared(declaredType) ? (A) ctx.getStatementArgument() : null;
    }

    public static <A, D extends DeclaredStatement<A>> A firstSubstatementAttributeOf(
            final StmtContext<?, ?, ?> ctx, final Class<D> declaredType) {
        return firstAttributeOf(ctx.allSubstatements(), declaredType);
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> StmtContext<A, ?, ?> findFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (subStmtContext.producesDeclared(declaredType)) {
                return (StmtContext<A, ?, ?>) subStmtContext;
            }
        }
        return null;
    }

    @SafeVarargs
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static StmtContext<?, ?, ?> findFirstDeclaredSubstatement(final StmtContext<?, ?, ?> stmtContext,
            int startIndex, final Class<? extends DeclaredStatement<?>>... types) {
        if (startIndex >= types.length) {
            return null;
        }

        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (subStmtContext.producesDeclared((Class) types[startIndex])) {
                return startIndex + 1 == types.length ? subStmtContext : findFirstDeclaredSubstatement(subStmtContext,
                        ++startIndex, types);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> Collection<StmtContext<A, D, ?>> findAllDeclaredSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        final ImmutableList.Builder<StmtContext<A, D, ?>> listBuilder = ImmutableList.builder();
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (subStmtContext.producesDeclared(declaredType)) {
                listBuilder.add((StmtContext<A, D, ?>) subStmtContext);
            }
        }
        return listBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> Collection<StmtContext<A, D, ?>> findAllEffectiveSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> type) {
        final ImmutableList.Builder<StmtContext<A, D, ?>> listBuilder = ImmutableList.builder();
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.effectiveSubstatements()) {
            if (subStmtContext.producesDeclared(type)) {
                listBuilder.add((StmtContext<A, D, ?>) subStmtContext);
            }
        }
        return listBuilder.build();
    }

    public static <A, D extends DeclaredStatement<A>> Collection<StmtContext<A, D, ?>> findAllSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> type) {
        final ImmutableList.Builder<StmtContext<A, D, ?>> listBuilder = ImmutableList.builder();
        listBuilder.addAll(findAllDeclaredSubstatements(stmtContext, type));
        listBuilder.addAll(findAllEffectiveSubstatements(stmtContext, type));
        return listBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> StmtContext<A, ?, ?> findFirstEffectiveSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.effectiveSubstatements()) {
            if (subStmtContext.producesDeclared(declaredType)) {
                return (StmtContext<A, ?, ?>) subStmtContext;
            }
        }
        return null;
    }

    /**
     * Searches for the first substatement of the specified type in the specified statement context.
     * First, it tries to find the substatement in the effective substatements of the statement context.
     * If it was not found, then it proceeds to search in the declared substatements. If it still was not found,
     * the method returns null.
     *
     * @param stmtContext statement context to search in
     * @param declaredType substatement type to search for
     * @param <A> statement argument type
     * @param <D> declared statement type
     * @return statement context that was searched for or null if was not found
     * @deprecated Use {@link StmtContext#findSubstatementArgument(Class)} instead.
     */
    @Deprecated(forRemoval = true)
    public static <A, D extends DeclaredStatement<A>> StmtContext<A, ?, ?> findFirstSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        final StmtContext<A, ?, ?> effectiveSubstatement = findFirstEffectiveSubstatement(stmtContext, declaredType);
        return effectiveSubstatement != null ? effectiveSubstatement : findFirstDeclaredSubstatement(stmtContext,
                declaredType);
    }

    public static <D extends DeclaredStatement<?>> StmtContext<?, ?, ?> findFirstDeclaredSubstatementOnSublevel(
            final StmtContext<?, ?, ?> stmtContext, final Class<? super D> declaredType, int sublevel) {
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (sublevel == 1 && subStmtContext.producesDeclared(declaredType)) {
                return subStmtContext;
            }
            if (sublevel > 1) {
                final StmtContext<?, ?, ?> result = findFirstDeclaredSubstatementOnSublevel(subStmtContext,
                        declaredType, --sublevel);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static <D extends DeclaredStatement<?>> StmtContext<?, ?, ?> findDeepFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<? super D> declaredType) {
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (subStmtContext.producesDeclared(declaredType)) {
                return subStmtContext;
            }

            final StmtContext<?, ?, ?> result = findDeepFirstDeclaredSubstatement(subStmtContext, declaredType);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public static boolean isInExtensionBody(final StmtContext<?, ?, ?> stmtCtx) {
        StmtContext<?, ?, ?> current = stmtCtx;

        while (true) {
            final StmtContext<?, ?, ?> parent = current.coerceParentContext();
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
     * Returns true if supplied statement context represents unrecognized
     * statement, otherwise returns false.
     *
     * @param stmtCtx
     *            statement context to be checked
     * @return true if supplied statement context represents unrecognized
     *         statement, otherwise false
     * @throws NullPointerException
     *             if supplied statement context is null
     */
    public static boolean isUnrecognizedStatement(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.producesDeclared(UnrecognizedStatement.class);
    }

    public static boolean checkFeatureSupport(final StmtContext<?, ?, ?> stmtContext,
            final Set<QName> supportedFeatures) {
        boolean isSupported = false;
        boolean containsIfFeature = false;
        for (final StmtContext<?, ?, ?> stmt : stmtContext.declaredSubstatements()) {
            if (YangStmtMapping.IF_FEATURE.equals(stmt.publicDefinition())) {
                containsIfFeature = true;
                @SuppressWarnings("unchecked")
                final Predicate<Set<QName>> argument = (Predicate<Set<QName>>) stmt.coerceStatementArgument();
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
        if (!(stmtCtx.publicDefinition() instanceof YangStmtMapping)) {
            return false;
        }
        switch ((YangStmtMapping) stmtCtx.publicDefinition()) {
            case LEAF:
            case CHOICE:
            case ANYXML:
                return Boolean.TRUE.equals(firstSubstatementAttributeOf(stmtCtx, MandatoryStatement.class));
            case LIST:
            case LEAF_LIST:
                final Integer minElements = firstSubstatementAttributeOf(stmtCtx, MinElementsStatement.class);
                return minElements != null && minElements > 0;
            default:
                return false;
        }
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
     * @param stmt EffectiveStmtCtx to be checked
     * @param ancestorTypes collection of statement definitions
     * @return true if at least one ancestor of a StatementContext matches one
     *         from collection of statement definitions, otherwise false.
     */
    public static boolean hasAncestorOfType(final EffectiveStmtCtx stmt,
            final Collection<StatementDefinition> ancestorTypes) {
        requireNonNull(ancestorTypes);
        Parent current = stmt.effectiveParent();
        while (current != null) {
            if (ancestorTypes.contains(current.publicDefinition())) {
                return true;
            }
            current = current.effectiveParent();
        }
        return false;
    }

    /**
     * Checks whether all of StmtContext's ancestors of specified type have a child of specified type.
     *
     * @param stmt EffectiveStmtCtx to be checked
     * @param ancestorType type of ancestor to search for
     * @param ancestorChildType type of child to search for in the specified ancestor type
     * @return true if all of StmtContext's ancestors of specified type have a child of specified type, otherwise false
     */
    public static <A, D extends DeclaredStatement<A>> boolean hasAncestorOfTypeWithChildOfType(
            final EffectiveStmtCtx.Current<?, ?> stmt, final StatementDefinition ancestorType,
            final StatementDefinition ancestorChildType) {
        requireNonNull(stmt);
        requireNonNull(ancestorType);

        final Class<? extends EffectiveStatement<?, ?>> repr = ancestorChildType.getEffectiveRepresentationClass();
        StmtContext<?, ?, ?> current = stmt.caerbannog().getParentContext();
        StmtContext<?, ?, ?> parent = current.getParentContext();
        while (parent != null) {
            if (ancestorType.equals(current.publicDefinition()) && !current.hasSubstatement(repr)) {
                return false;
            }

            current = parent;
            parent = current.getParentContext();
        }

        return true;
    }

    /**
     * Checks whether the parent of EffectiveStmtCtx is of specified type.
     *
     * @param stmt EffectiveStmtCtx to be checked
     * @param parentType type of parent to check
     * @return true if the parent of StmtContext is of specified type, otherwise false
     */
    public static boolean hasParentOfType(final EffectiveStmtCtx.Current<?, ?> stmt,
            final StatementDefinition parentType) {
        return hasParentOfType(stmt.caerbannog(), parentType);
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
        final StmtContext<?, ?, ?> parentContext = ctx.getParentContext();
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

        final StmtContext<?, ?, ?> listStmtCtx = ctx.coerceParentContext();
        final StmtContext<Set<QName>, ?, ?> keyStmtCtx = findFirstDeclaredSubstatement(listStmtCtx, KeyStatement.class);

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
        return YangVersion.VERSION_1_1.equals(ctx.getRootVersion()) && hasParentOfType(ctx, YangStmtMapping.LIST)
                && findFirstDeclaredSubstatement(ctx.coerceParentContext(), KeyStatement.class) != null;
    }

    private static boolean isListKey(final StmtContext<?, ?, ?> leafStmtCtx,
            final StmtContext<Set<QName>, ?, ?> keyStmtCtx) {
        return keyStmtCtx.coerceStatementArgument().contains(leafStmtCtx.getStatementArgument());
    }

    private static void disallowIfFeatureAndWhenOnListKeys(final StmtContext<?, ?, ?> leafStmtCtx) {
        leafStmtCtx.allSubstatements().forEach(leafSubstmtCtx -> {
            final StatementDefinition statementDef = leafSubstmtCtx.publicDefinition();
            SourceException.throwIf(YangStmtMapping.IF_FEATURE.equals(statementDef)
                    || YangStmtMapping.WHEN.equals(statementDef), leafStmtCtx.getStatementSourceReference(),
                    "%s statement is not allowed in %s leaf statement which is specified as a list key.",
                    statementDef.getStatementName(), leafStmtCtx.getStatementArgument());
        });
    }

    public static QName qnameFromArgument(StmtContext<?, ?, ?> ctx, final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return ctx.publicDefinition().getStatementName();
        }

        String prefix;
        QNameModule qnameModule = null;
        String localName = null;

        final String[] namesParts = value.split(":");
        switch (namesParts.length) {
            case 1:
                localName = namesParts[0];
                qnameModule = getRootModuleQName(ctx);
                break;
            default:
                prefix = namesParts[0];
                localName = namesParts[1];
                qnameModule = getModuleQNameByPrefix(ctx, prefix);
                // in case of unknown statement argument, we're not going to parse it
                if (qnameModule == null && isUnknownStatement(ctx)) {
                    localName = value;
                    qnameModule = getRootModuleQName(ctx);
                }
                if (qnameModule == null && ctx.getCopyHistory().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                    ctx = ctx.getOriginalCtx().orElse(null);
                    qnameModule = getModuleQNameByPrefix(ctx, prefix);
                }
        }

        return internedQName(ctx,
            InferenceException.throwIfNull(qnameModule, ctx.getStatementSourceReference(),
            "Cannot resolve QNameModule for '%s'", value), localName);
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
    public static QName parseIdentifier(final StmtContext<?, ?, ?> ctx, final String str) {
        SourceException.throwIf(str.isEmpty(), ctx.getStatementSourceReference(),
                "Identifier may not be an empty string");
        return internedQName(ctx, str);
    }

    public static QName parseNodeIdentifier(final StmtContext<?, ?, ?> ctx, final String prefix,
            final String localName) {
        return internedQName(ctx,
            InferenceException.throwIfNull(getModuleQNameByPrefix(ctx, prefix), ctx.getStatementSourceReference(),
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
     */
    public static QName parseNodeIdentifier(final StmtContext<?, ?, ?> ctx, final String str) {
        SourceException.throwIf(str.isEmpty(), ctx.getStatementSourceReference(),
                "Node identifier may not be an empty string");

        final int colon = str.indexOf(':');
        if (colon == -1) {
            return internedQName(ctx, str);
        }

        final String prefix = str.substring(0, colon);
        SourceException.throwIf(prefix.isEmpty(), ctx.getStatementSourceReference(),
            "String '%s' has an empty prefix", str);
        final String localName = str.substring(colon + 1);
        SourceException.throwIf(localName.isEmpty(), ctx.getStatementSourceReference(),
            "String '%s' has an empty identifier", str);

        return parseNodeIdentifier(ctx, prefix, localName);
    }

    private static QName internedQName(final StmtContext<?, ?, ?> ctx, final String localName) {
        return internedQName(ctx, getRootModuleQName(ctx), localName);
    }

    private static QName internedQName(final StmtContext<?, ?, ?> ctx, final QNameModule module,
            final String localName) {
        final QName template;
        try {
            template = QName.create(module, localName);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid identifier '%s'", localName);
        }
        return template.intern();
    }

    public static QNameModule getRootModuleQName(final StmtContext<?, ?, ?> ctx) {
        if (ctx == null) {
            return null;
        }

        final StmtContext<?, ?, ?> rootCtx = ctx.getRoot();
        final QNameModule qnameModule;

        if (rootCtx.producesDeclared(ModuleStatement.class)) {
            qnameModule = rootCtx.getFromNamespace(ModuleCtxToModuleQName.class, rootCtx);
        } else if (rootCtx.producesDeclared(SubmoduleStatement.class)) {
            final String belongsToModuleName = firstAttributeOf(rootCtx.declaredSubstatements(),
                BelongsToStatement.class);
            qnameModule = rootCtx.getFromNamespace(ModuleNameToModuleQName.class, belongsToModuleName);
        } else {
            qnameModule = null;
        }

        checkArgument(qnameModule != null, "Failed to look up root QNameModule for %s", ctx);
        return qnameModule;
    }

    public static QNameModule getModuleQNameByPrefix(final StmtContext<?, ?, ?> ctx, final String prefix) {
        final StmtContext<?, ?, ?> root = ctx.getRoot();
        final StmtContext<?, ?, ?> importedModule = root.getFromNamespace(ImportPrefixToModuleCtx.class, prefix);
        final QNameModule qnameModule = ctx.getFromNamespace(ModuleCtxToModuleQName.class, importedModule);
        if (qnameModule != null) {
            return qnameModule;
        }

        if (root.producesDeclared(SubmoduleStatement.class)) {
            final String moduleName = root.getFromNamespace(BelongsToPrefixToModuleName.class, prefix);
            return ctx.getFromNamespace(ModuleNameToModuleQName.class, moduleName);
        }

        return null;
    }

    public static Optional<Revision> getLatestRevision(final Iterable<? extends StmtContext<?, ?, ?>> subStmts) {
        Revision revision = null;
        for (final StmtContext<?, ?, ?> subStmt : subStmts) {
            if (subStmt.producesDeclared(RevisionStatement.class)) {
                if (revision == null && subStmt.getStatementArgument() != null) {
                    revision = (Revision) subStmt.getStatementArgument();
                } else {
                    final Revision subArg = (Revision) subStmt.getStatementArgument();
                    if (subArg != null && subArg.compareTo(revision) > 0) {
                        revision = subArg;
                    }
                }
            }
        }
        return Optional.ofNullable(revision);
    }
}
