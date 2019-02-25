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

import com.google.common.base.CharMatcher;
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
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class StmtContextUtils {
    private static final CharMatcher IDENTIFIER_START =
            CharMatcher.inRange('A', 'Z').or(CharMatcher.inRange('a', 'z').or(CharMatcher.is('_'))).precomputed();
    private static final CharMatcher NOT_IDENTIFIER_PART =
            IDENTIFIER_START.or(CharMatcher.inRange('0', '9')).or(CharMatcher.anyOf("-.")).negate().precomputed();

    private StmtContextUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> A firstAttributeOf(
            final Iterable<? extends StmtContext<?, ?, ?>> contexts, final Class<D> declaredType) {
        for (final StmtContext<?, ?, ?> ctx : contexts) {
            if (producesDeclared(ctx, declaredType)) {
                return (A) ctx.getStatementArgument();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> A firstAttributeOf(final StmtContext<?, ?, ?> ctx,
            final Class<D> declaredType) {
        return producesDeclared(ctx, declaredType) ? (A) ctx.getStatementArgument() : null;
    }

    public static <A, D extends DeclaredStatement<A>> A firstSubstatementAttributeOf(
            final StmtContext<?, ?, ?> ctx, final Class<D> declaredType) {
        return firstAttributeOf(ctx.allSubstatements(), declaredType);
    }

    @SuppressWarnings("unchecked")
    public static <A, D extends DeclaredStatement<A>> StmtContext<A, ?, ?> findFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (producesDeclared(subStmtContext, declaredType)) {
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

        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (producesDeclared(subStmtContext, types[startIndex])) {
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
            if (producesDeclared(subStmtContext, declaredType)) {
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
            if (producesDeclared(subStmtContext, type)) {
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
            if (producesDeclared(subStmtContext, declaredType)) {
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
     */
    public static <A, D extends DeclaredStatement<A>> StmtContext<A, ?, ?> findFirstSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        final StmtContext<A, ?, ?> effectiveSubstatement = findFirstEffectiveSubstatement(stmtContext, declaredType);
        return effectiveSubstatement != null ? effectiveSubstatement : findFirstDeclaredSubstatement(stmtContext,
                declaredType);
    }

    public static <D extends DeclaredStatement<?>> StmtContext<?, ?, ?> findFirstDeclaredSubstatementOnSublevel(
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType, int sublevel) {
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (sublevel == 1 && producesDeclared(subStmtContext, declaredType)) {
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
            final StmtContext<?, ?, ?> stmtContext, final Class<D> declaredType) {
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (producesDeclared(subStmtContext, declaredType)) {
                return subStmtContext;
            }

            final StmtContext<?, ?, ?> result = findDeepFirstDeclaredSubstatement(subStmtContext, declaredType);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public static boolean producesDeclared(final StmtContext<?, ?, ?> ctx,
            final Class<? extends DeclaredStatement<?>> type) {
        return type.isAssignableFrom(ctx.getPublicDefinition().getDeclaredRepresentationClass());
    }

    public static boolean isInExtensionBody(final StmtContext<?, ?, ?> stmtCtx) {
        StmtContext<?, ?, ?> current = stmtCtx;
        while (current.coerceParentContext().getParentContext() != null) {
            current = current.getParentContext();
            if (isUnknownStatement(current)) {
                return true;
            }
        }

        return false;
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
        return UnknownStatement.class
                .isAssignableFrom(stmtCtx.getPublicDefinition().getDeclaredRepresentationClass());
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
        return UnrecognizedStatement.class
                .isAssignableFrom(stmtCtx.getPublicDefinition().getDeclaredRepresentationClass());
    }

    public static boolean checkFeatureSupport(final StmtContext<?, ?, ?> stmtContext,
            final Set<QName> supportedFeatures) {
        boolean isSupported = false;
        boolean containsIfFeature = false;
        for (final StmtContext<?, ?, ?> stmt : stmtContext.declaredSubstatements()) {
            if (YangStmtMapping.IF_FEATURE.equals(stmt.getPublicDefinition())) {
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
        return stmtCtx.getPublicDefinition() == YangStmtMapping.CONTAINER && containsPresenceSubStmt(stmtCtx);
    }

    /**
     * Checks whether statement context is a non-presence container or not.
     *
     * @param stmtCtx
     *            statement context
     * @return true if it is a non-presence container
     */
    public static boolean isNonPresenceContainer(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.getPublicDefinition() == YangStmtMapping.CONTAINER && !containsPresenceSubStmt(stmtCtx);
    }

    private static boolean containsPresenceSubStmt(final StmtContext<?, ?, ?> stmtCtx) {
        return findFirstSubstatement(stmtCtx, PresenceStatement.class) != null;
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
        if (!(stmtCtx.getPublicDefinition() instanceof YangStmtMapping)) {
            return false;
        }
        switch ((YangStmtMapping) stmtCtx.getPublicDefinition()) {
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
        return stmtCtx.getPublicDefinition().equals(stmtDef) && !isMandatoryNode(stmtCtx);
    }

    /**
     * Checks whether at least one ancestor of a StatementContext matches one from a collection of statement
     * definitions.
     *
     * @param ctx
     *            StatementContext to be checked
     * @param ancestorTypes
     *            collection of statement definitions
     * @return true if at least one ancestor of a StatementContext matches one
     *         from collection of statement definitions, otherwise false.
     */
    public static boolean hasAncestorOfType(final StmtContext<?, ?, ?> ctx,
            final Collection<StatementDefinition> ancestorTypes) {
        requireNonNull(ancestorTypes);
        StmtContext<?, ?, ?> current = ctx.getParentContext();
        while (current != null) {
            if (ancestorTypes.contains(current.getPublicDefinition())) {
                return true;
            }
            current = current.getParentContext();
        }
        return false;
    }

    /**
     * Checks whether all of StmtContext's ancestors of specified type have a child of specified type.
     *
     * @param ctx StmtContext to be checked
     * @param ancestorType type of ancestor to search for
     * @param ancestorChildType type of child to search for in the specified ancestor type
     * @return true if all of StmtContext's ancestors of specified type have a child of specified type, otherwise false
     */
    public static <A, D extends DeclaredStatement<A>> boolean hasAncestorOfTypeWithChildOfType(
            final StmtContext<?, ?, ?> ctx, final StatementDefinition ancestorType,
            final StatementDefinition ancestorChildType) {
        requireNonNull(ctx);
        requireNonNull(ancestorType);
        requireNonNull(ancestorChildType);

        StmtContext<?, ?, ?> current = ctx.coerceParentContext();
        StmtContext<?, ?, ?> parent = current.getParentContext();
        while (parent != null) {
            if (ancestorType.equals(current.getPublicDefinition())) {
                @SuppressWarnings("unchecked")
                final Class<D> ancestorChildTypeClass = (Class<D>) ancestorChildType.getDeclaredRepresentationClass();
                if (findFirstSubstatement(current, ancestorChildTypeClass) == null) {
                    return false;
                }
            }

            current = parent;
            parent = current.getParentContext();
        }

        return true;
    }

    /**
     * Checks whether the parent of StmtContext is of specified type.
     *
     * @param ctx
     *            StmtContext to be checked
     * @param parentType
     *            type of parent to check
     * @return true if the parent of StmtContext is of specified type, otherwise false
     */
    public static boolean hasParentOfType(final StmtContext<?, ?, ?> ctx, final StatementDefinition parentType) {
        requireNonNull(parentType);
        final StmtContext<?, ?, ?> parentContext = ctx.getParentContext();
        return parentContext != null ? parentType.equals(parentContext.getPublicDefinition()) : false;
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

        final StmtContext<?, ?, ?> listStmtCtx = ctx.getParentContext();
        final StmtContext<Collection<SchemaNodeIdentifier>, ?, ?> keyStmtCtx =
                StmtContextUtils.findFirstDeclaredSubstatement(listStmtCtx, KeyStatement.class);

        if (YangStmtMapping.LEAF.equals(ctx.getPublicDefinition())) {
            if (isListKey(ctx, keyStmtCtx)) {
                disallowIfFeatureAndWhenOnListKeys(ctx);
            }
        } else if (YangStmtMapping.USES.equals(ctx.getPublicDefinition())) {
            StmtContextUtils.findAllEffectiveSubstatements(listStmtCtx, LeafStatement.class).forEach(leafStmtCtx -> {
                if (isListKey(leafStmtCtx, keyStmtCtx)) {
                    disallowIfFeatureAndWhenOnListKeys(leafStmtCtx);
                }
            });
        }
    }

    private static boolean isRelevantForIfFeatureAndWhenOnListKeysCheck(final StmtContext<?, ?, ?> ctx) {
        return YangVersion.VERSION_1_1.equals(ctx.getRootVersion())
                && StmtContextUtils.hasParentOfType(ctx, YangStmtMapping.LIST)
                && StmtContextUtils.findFirstDeclaredSubstatement(ctx.coerceParentContext(),
                    KeyStatement.class) != null;
    }

    private static boolean isListKey(final StmtContext<?, ?, ?> leafStmtCtx,
            final StmtContext<Collection<SchemaNodeIdentifier>, ?, ?> keyStmtCtx) {
        for (final SchemaNodeIdentifier keyIdentifier : keyStmtCtx.coerceStatementArgument()) {
            if (leafStmtCtx.getStatementArgument().equals(keyIdentifier.getLastComponent())) {
                return true;
            }
        }

        return false;
    }

    private static void disallowIfFeatureAndWhenOnListKeys(final StmtContext<?, ?, ?> leafStmtCtx) {
        leafStmtCtx.allSubstatements().forEach(leafSubstmtCtx -> {
            final StatementDefinition statementDef = leafSubstmtCtx.getPublicDefinition();
            SourceException.throwIf(YangStmtMapping.IF_FEATURE.equals(statementDef)
                    || YangStmtMapping.WHEN.equals(statementDef), leafStmtCtx.getStatementSourceReference(),
                    "%s statement is not allowed in %s leaf statement which is specified as a list key.",
                    statementDef.getStatementName(), leafStmtCtx.getStatementArgument());
        });
    }

    public static QName qnameFromArgument(StmtContext<?, ?, ?> ctx, final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return ctx.getPublicDefinition().getStatementName();
        }

        String prefix;
        QNameModule qnameModule = null;
        String localName = null;

        final String[] namesParts = value.split(":");
        switch (namesParts.length) {
            case 1:
                localName = namesParts[0];
                qnameModule = StmtContextUtils.getRootModuleQName(ctx);
                break;
            default:
                prefix = namesParts[0];
                localName = namesParts[1];
                qnameModule = StmtContextUtils.getModuleQNameByPrefix(ctx, prefix);
                // in case of unknown statement argument, we're not going to parse it
                if (qnameModule == null && isUnknownStatement(ctx)) {
                    localName = value;
                    qnameModule = StmtContextUtils.getRootModuleQName(ctx);
                }
                if (qnameModule == null && ctx.getCopyHistory().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                    ctx = ctx.getOriginalCtx().orElse(null);
                    qnameModule = StmtContextUtils.getModuleQNameByPrefix(ctx, prefix);
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

    /**
     * Parse a YANG node identifier string in context of a statement.
     *
     * @param ctx Statement context
     * @param str String to be parsed
     * @return An interned QName
     * @throws NullPointerException if any of the arguments are null
     * @throws SourceException if the string is not a valid YANG node identifier
     */
    public static QName parseNodeIdentifier(StmtContext<?, ?, ?> ctx, final String str) {
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

        final QNameModule module = StmtContextUtils.getModuleQNameByPrefix(ctx, prefix);
        if (module != null) {
            return internedQName(ctx, module, localName);
        }

        if (ctx.getCopyHistory().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
            final Optional<StmtContext<?, ?, ?>> optOrigCtx = ctx.getOriginalCtx();
            if (optOrigCtx.isPresent()) {
                ctx = optOrigCtx.get();
                final QNameModule origModule = StmtContextUtils.getModuleQNameByPrefix(ctx, prefix);
                if (origModule != null) {
                    return internedQName(ctx, origModule, localName);
                }
            }
        }

        throw new InferenceException(ctx.getStatementSourceReference(), "Cannot resolve QNameModule for '%s'", str);
    }

    private static QName internedQName(final StmtContext<?, ?, ?> ctx, final String localName) {
        return internedQName(ctx, StmtContextUtils.getRootModuleQName(ctx), localName);
    }

    private static QName internedQName(final StmtContext<?, ?, ?> ctx, final QNameModule module,
            final String localName) {
        final QName template;
        try {
            template = QName.create(module, localName);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid identifier %s", localName);
        }

        return ctx.getFromNamespace(QNameCacheNamespace.class, template);
    }

    public static QNameModule getRootModuleQName(final StmtContext<?, ?, ?> ctx) {
        if (ctx == null) {
            return null;
        }

        final StmtContext<?, ?, ?> rootCtx = ctx.getRoot();
        final QNameModule qnameModule;

        if (producesDeclared(rootCtx, ModuleStatement.class)) {
            qnameModule = rootCtx.getFromNamespace(ModuleCtxToModuleQName.class, rootCtx);
        } else if (producesDeclared(rootCtx, SubmoduleStatement.class)) {
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
        final StmtContext<?, ?, ?> importedModule = ctx.getRoot().getFromNamespace(ImportPrefixToModuleCtx.class,
            prefix);
        final QNameModule qnameModule = ctx.getFromNamespace(ModuleCtxToModuleQName.class, importedModule);
        if (qnameModule != null) {
            return qnameModule;
        }

        if (producesDeclared(ctx.getRoot(), SubmoduleStatement.class)) {
            final String moduleName = ctx.getRoot().getFromNamespace(BelongsToPrefixToModuleName.class, prefix);
            return ctx.getFromNamespace(ModuleNameToModuleQName.class, moduleName);
        }

        return null;
    }

    public static SourceIdentifier createSourceIdentifier(final StmtContext<?, ?, ?> root) {
        final QNameModule qNameModule = root.getFromNamespace(ModuleCtxToModuleQName.class, root);
        if (qNameModule != null) {
            // creates SourceIdentifier for a module
            return RevisionSourceIdentifier.create((String) root.getStatementArgument(), qNameModule.getRevision());
        }

        // creates SourceIdentifier for a submodule
        final Optional<Revision> revision = getLatestRevision(root.declaredSubstatements());
        return RevisionSourceIdentifier.create((String) root.getStatementArgument(), revision);
    }

    public static Optional<Revision> getLatestRevision(final Iterable<? extends StmtContext<?, ?, ?>> subStmts) {
        Revision revision = null;
        for (final StmtContext<?, ?, ?> subStmt : subStmts) {
            if (subStmt.getPublicDefinition().getDeclaredRepresentationClass().isAssignableFrom(
                    RevisionStatement.class)) {
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
