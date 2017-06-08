/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
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
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.UnknownStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangDataStatementImpl;

public final class StmtContextUtils {
    public static final Splitter LIST_KEY_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

    private StmtContextUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SuppressWarnings("unchecked")
    public static <AT, DT extends DeclaredStatement<AT>> AT firstAttributeOf(
            final Iterable<? extends StmtContext<?, ?, ?>> contexts, final Class<DT> declaredType) {
        for (final StmtContext<?, ?, ?> ctx : contexts) {
            if (producesDeclared(ctx, declaredType)) {
                return (AT) ctx.getStatementArgument();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <AT, DT extends DeclaredStatement<AT>> AT firstAttributeOf(final StmtContext<?, ?, ?> ctx,
            final Class<DT> declaredType) {
        return producesDeclared(ctx, declaredType) ? (AT) ctx.getStatementArgument() : null;
    }

    public static <AT, DT extends DeclaredStatement<AT>> AT firstSubstatementAttributeOf(
            final StmtContext<?, ?, ?> ctx, final Class<DT> declaredType) {
        final AT firstAttribute = firstAttributeOf(ctx.effectiveSubstatements(), declaredType);
        return firstAttribute != null ? firstAttribute : firstAttributeOf(ctx.declaredSubstatements(), declaredType);
    }

    @SuppressWarnings("unchecked")
    public static <AT, DT extends DeclaredStatement<AT>> StmtContext<AT, ?, ?> findFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (producesDeclared(subStmtContext, declaredType)) {
                return (StmtContext<AT, ?, ?>) subStmtContext;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <AT, DT extends DeclaredStatement<AT>> Collection<StmtContext<AT, DT, ?>> findAllDeclaredSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        final ImmutableList.Builder<StmtContext<AT, DT, ?>> listBuilder = ImmutableList.builder();
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (producesDeclared(subStmtContext, declaredType)) {
                listBuilder.add((StmtContext<AT, DT, ?>) subStmtContext);
            }
        }
        return listBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public static <AT, DT extends DeclaredStatement<AT>> Collection<StmtContext<AT, DT, ?>> findAllEffectiveSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> type) {
        final ImmutableList.Builder<StmtContext<AT, DT, ?>> listBuilder = ImmutableList.builder();
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.effectiveSubstatements()) {
            if (producesDeclared(subStmtContext, type)) {
                listBuilder.add((StmtContext<AT, DT, ?>) subStmtContext);
            }
        }
        return listBuilder.build();
    }

    public static <AT, DT extends DeclaredStatement<AT>> Collection<StmtContext<AT, DT, ?>> findAllSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> type) {
        final ImmutableList.Builder<StmtContext<AT, DT, ?>> listBuilder = ImmutableList.builder();
        listBuilder.addAll(findAllDeclaredSubstatements(stmtContext, type));
        listBuilder.addAll(findAllEffectiveSubstatements(stmtContext, type));
        return listBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public static <AT, DT extends DeclaredStatement<AT>> StmtContext<AT, ?, ?> findFirstEffectiveSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        for (final StmtContext<?, ?, ?> subStmtContext : stmtContext.effectiveSubstatements()) {
            if (producesDeclared(subStmtContext, declaredType)) {
                return (StmtContext<AT, ?, ?>) subStmtContext;
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
     * @param <AT> statement argument type
     * @param <DT> declared statement type
     * @return statement context that was searched for or null if was not found
     */
    public static <AT, DT extends DeclaredStatement<AT>> StmtContext<AT, ?, ?> findFirstSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        final StmtContext<AT, ?, ?> effectiveSubstatement = findFirstEffectiveSubstatement(stmtContext, declaredType);
        return effectiveSubstatement != null ? effectiveSubstatement : findFirstDeclaredSubstatement(stmtContext,
                declaredType);
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

    public static <DT extends DeclaredStatement<?>> StmtContext<?, ?, ?> findFirstDeclaredSubstatementOnSublevel(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType, int sublevel) {
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

    public static <DT extends DeclaredStatement<?>> StmtContext<?, ?, ?> findDeepFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
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
        while (!current.getParentContext().isRootContext()) {
            current = current.getParentContext();
            if (producesDeclared(current, UnknownStatementImpl.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the statement context has a 'yang-data' extension node as its parent.
     *
     * @param stmtCtx statement context to be checked
     * @return true if the parent node is a 'yang-data' node, otherwise false
     */
    public static boolean hasYangDataExtensionParent(final StmtContext<?, ?, ?> stmtCtx) {
        return producesDeclared(stmtCtx.getParentContext(), YangDataStatementImpl.class);
    }

    public static boolean isUnknownStatement(final StmtContext<?, ?, ?> stmtCtx) {
        return producesDeclared(stmtCtx, UnknownStatementImpl.class);
    }

    public static Collection<SchemaNodeIdentifier> replaceModuleQNameForKey(
            final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> keyStmtCtx,
            final QNameModule newQNameModule) {

        final Builder<SchemaNodeIdentifier> builder = ImmutableSet.builder();
        boolean replaced = false;
        for (final SchemaNodeIdentifier arg : keyStmtCtx.getStatementArgument()) {
            final QName qname = arg.getLastComponent();
            if (!newQNameModule.equals(qname)) {
                final QName newQname = keyStmtCtx.getFromNamespace(QNameCacheNamespace.class,
                        QName.create(newQNameModule, qname.getLocalName()));
                builder.add(SchemaNodeIdentifier.create(false, newQname));
                replaced = true;
            } else {
                builder.add(arg);
            }
        }

        // This makes sure we reuse the collection when a grouping is
        // instantiated in the same module
        return replaced ? builder.build() : keyStmtCtx.getStatementArgument();
    }

    public static boolean checkFeatureSupport(final StmtContext<?, ?, ?> stmtContext,
            final Set<QName> supportedFeatures) {
        boolean isSupported = false;
        boolean containsIfFeature = false;
        for (final StmtContext<?, ?, ?> stmt : stmtContext.declaredSubstatements()) {
            if (YangStmtMapping.IF_FEATURE.equals(stmt.getPublicDefinition())) {
                if (stmtContext.isInYangDataExtensionBody()) {
                    break;
                }

                containsIfFeature = true;
                if (((Predicate<Set<QName>>) stmt.getStatementArgument()).test(supportedFeatures)) {
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
     * Checks whether at least one ancestor of a StatementContext matches one
     * from collection of statement definitions.
     *
     * @param ctx
     *            StatementContext to be checked
     * @param ancestorTypes
     *            collection of statement definitions
     *
     * @return true if at least one ancestor of a StatementContext matches one
     *         from collection of statement definitions, otherwise false.
     */
    public static boolean hasAncestorOfType(final StmtContext<?, ?, ?> ctx,
            final Collection<StatementDefinition> ancestorTypes) {
        Preconditions.checkNotNull(ctx);
        Preconditions.checkNotNull(ancestorTypes);
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
     * Checks whether all of StmtContext's ancestors of specified type have a child of specified type
     *
     * @param ctx StmtContext to be checked
     * @param ancestorType type of ancestor to search for
     * @param ancestorChildType type of child to search for in the specified ancestor type
     *
     * @return true if all of StmtContext's ancestors of specified type have a child of specified type, otherwise false
     */
    public static <AT, DT extends DeclaredStatement<AT>> boolean hasAncestorOfTypeWithChildOfType(final StmtContext<?, ?, ?> ctx,
            final StatementDefinition ancestorType, final StatementDefinition ancestorChildType) {
        Preconditions.checkNotNull(ctx);
        Preconditions.checkNotNull(ancestorType);
        Preconditions.checkNotNull(ancestorChildType);

        StmtContext<?, ?, ?> current = ctx.getParentContext();
        StmtContext<?, ?, ?> parent = current.getParentContext();
        while (parent != null) {
            if (ancestorType.equals(current.getPublicDefinition())) {
                @SuppressWarnings("unchecked")
                final Class<DT> ancestorChildTypeClass = (Class<DT>) ancestorChildType.getDeclaredRepresentationClass();
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
     * Checks whether the parent of StmtContext is of specified type
     *
     * @param ctx
     *            StmtContext to be checked
     * @param parentType
     *            type of parent to check
     *
     * @return true if the parent of StmtContext is of specified type, otherwise
     *         false
     */
    public static boolean hasParentOfType(final StmtContext<?, ?, ?> ctx, final StatementDefinition parentType) {
        Preconditions.checkNotNull(ctx);
        Preconditions.checkNotNull(parentType);
        final StmtContext<?, ?, ?> parentContext = ctx.getParentContext();
        return parentContext != null ? parentType.equals(parentContext.getPublicDefinition()) : false;
    }

    /**
     * Validates the specified statement context with regards to if-feature and when statement on list keys.
     * The context can either be a leaf which is defined directly in the substatements of a keyed list or a uses
     * statement defined in a keyed list (a uses statement may add leaves into the list).
     *
     * If one of the list keys contains an if-feature or a when statement in YANG 1.1 model, an exception is thrown.
     *
     * @param ctx statement context to be validated
     */
    public static void validateIfFeatureAndWhenOnListKeys(final StmtContext<?, ?, ?> ctx) {
        Preconditions.checkNotNull(ctx);

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
                && StmtContextUtils.findFirstDeclaredSubstatement(ctx.getParentContext(), KeyStatement.class) != null;
    }

    private static boolean isListKey(final StmtContext<?, ?, ?> leafStmtCtx,
            final StmtContext<Collection<SchemaNodeIdentifier>, ?, ?> keyStmtCtx) {
        for (final SchemaNodeIdentifier keyIdentifier : keyStmtCtx.getStatementArgument()) {
            if (leafStmtCtx.getStatementArgument().equals(keyIdentifier.getLastComponent())) {
                return true;
            }
        }

        return false;
    }

    private static void disallowIfFeatureAndWhenOnListKeys(final StmtContext<?, ?, ?> leafStmtCtx) {
        Iterables.concat(leafStmtCtx.declaredSubstatements(), leafStmtCtx.effectiveSubstatements()).forEach(
                leafSubstmtCtx -> {
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
        QNameModule qNameModule = null;
        String localName = null;

        final String[] namesParts = value.split(":");
        switch (namesParts.length) {
        case 1:
            localName = namesParts[0];
            qNameModule = StmtContextUtils.getRootModuleQName(ctx);
            break;
        default:
            prefix = namesParts[0];
            localName = namesParts[1];
            qNameModule = StmtContextUtils.getModuleQNameByPrefix(ctx, prefix);
            // in case of unknown statement argument, we're not going to parse it
            if (qNameModule == null
                    && ctx.getPublicDefinition().getDeclaredRepresentationClass()
                    .isAssignableFrom(UnknownStatementImpl.class)) {
                localName = value;
                qNameModule = StmtContextUtils.getRootModuleQName(ctx);
            }
            if (qNameModule == null
                    && ctx.getCopyHistory().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                ctx = ctx.getOriginalCtx();
                qNameModule = StmtContextUtils.getModuleQNameByPrefix(ctx, prefix);
            }
            break;
        }

        qNameModule = InferenceException.throwIfNull(qNameModule, ctx.getStatementSourceReference(),
            "Cannot resolve QNameModule for '%s'", value);

        final QNameModule resultQNameModule;
        if (qNameModule.getRevision() == null) {
            resultQNameModule = QNameModule.create(qNameModule.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV)
                .intern();
        } else {
            resultQNameModule = qNameModule;
        }

        return ctx.getFromNamespace(QNameCacheNamespace.class, QName.create(resultQNameModule, localName));
    }

    public static QNameModule getRootModuleQName(final StmtContext<?, ?, ?> ctx) {
        if (ctx == null) {
            return null;
        }

        final StmtContext<?, ?, ?> rootCtx = ctx.getRoot();
        final QNameModule qNameModule;

        if (producesDeclared(rootCtx, ModuleStatement.class)) {
            qNameModule = rootCtx.getFromNamespace(ModuleCtxToModuleQName.class, rootCtx);
        } else if (producesDeclared(rootCtx, SubmoduleStatement.class)) {
            final String belongsToModuleName = firstAttributeOf(rootCtx.declaredSubstatements(),
                BelongsToStatement.class);
            qNameModule = rootCtx.getFromNamespace(ModuleNameToModuleQName.class, belongsToModuleName);
        } else {
            qNameModule = null;
        }

        Preconditions.checkArgument(qNameModule != null, "Failed to look up root QNameModule for %s", ctx);
        if (qNameModule.getRevision() != null) {
            return qNameModule;
        }

        return QNameModule.create(qNameModule.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV).intern();
    }

    public static QNameModule getModuleQNameByPrefix(final StmtContext<?, ?, ?> ctx, final String prefix) {
        final ModuleIdentifier modId = ctx.getRoot().getFromNamespace(ImpPrefixToModuleIdentifier.class, prefix);
        final QNameModule qNameModule = ctx.getFromNamespace(ModuleIdentifierToModuleQName.class, modId);

        if (qNameModule == null && producesDeclared(ctx.getRoot(), SubmoduleStatement.class)) {
            final String moduleName = ctx.getRoot().getFromNamespace(BelongsToPrefixToModuleName.class, prefix);
            return ctx.getFromNamespace(ModuleNameToModuleQName.class, moduleName);
        }
        return qNameModule;
    }

    public static SourceIdentifier createSourceIdentifier(final StmtContext<?, ?, ?> root) {
        final QNameModule qNameModule = root.getFromNamespace(ModuleCtxToModuleQName.class, root);
        if (qNameModule != null) {
            // creates SourceIdentifier for a module
            return RevisionSourceIdentifier.create((String) root.getStatementArgument(),
                qNameModule.getFormattedRevision());
        }

        // creates SourceIdentifier for a submodule
        final Date revision = Optional.ofNullable(getLatestRevision(root.declaredSubstatements()))
                .orElse(SimpleDateFormatUtil.DEFAULT_DATE_REV);
        final String formattedRevision = SimpleDateFormatUtil.getRevisionFormat().format(revision);
        return RevisionSourceIdentifier.create((String) root.getStatementArgument(), formattedRevision);
    }

    public static Date getLatestRevision(final Iterable<? extends StmtContext<?, ?, ?>> subStmts) {
        Date revision = null;
        for (final StmtContext<?, ?, ?> subStmt : subStmts) {
            if (subStmt.getPublicDefinition().getDeclaredRepresentationClass().isAssignableFrom(RevisionStatement
                    .class)) {
                if (revision == null && subStmt.getStatementArgument() != null) {
                    revision = (Date) subStmt.getStatementArgument();
                } else if (subStmt.getStatementArgument() != null && ((Date) subStmt.getStatementArgument()).compareTo
                        (revision) > 0) {
                    revision = (Date) subStmt.getStatementArgument();
                }
            }
        }
        return revision;
    }

    public static boolean isUnknownNode(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx != null && stmtCtx.getPublicDefinition().getDeclaredRepresentationClass()
                .isAssignableFrom(UnknownStatementImpl.class);
    }
}
