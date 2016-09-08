/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.IfFeaturePredicates;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace.SupportedFeatures;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.UnknownStatementImpl;

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

    public static <AT, DT extends DeclaredStatement<AT>> StmtContext<AT, ?, ?> findFirstSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        final StmtContext<AT, ?, ?> declaredSubstatement = findFirstDeclaredSubstatement(stmtContext, declaredType);
        return declaredSubstatement != null ? declaredSubstatement : findFirstEffectiveSubstatement(stmtContext,
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

    public static boolean areFeaturesSupported(final StmtContext.Mutable<?, ?, ?> stmtContext) {
        switch (stmtContext.getSupportedByFeatures()) {
        case SUPPORTED:
            return true;
        case NOT_SUPPORTED:
            return false;
        default:
            break;
        }

        final Predicate<QName> isFeatureSupported = stmtContext.getFromNamespace(SupportedFeaturesNamespace.class,
                SupportedFeatures.SUPPORTED_FEATURES);
        if (IfFeaturePredicates.ALL_FEATURES.equals(isFeatureSupported)) {
            stmtContext.setSupportedByFeatures(true);
            return true;
        }

        final boolean result = checkFeatureSupport(stmtContext, isFeatureSupported);
        stmtContext.setSupportedByFeatures(result);
        return result;
    }

    private static boolean checkFeatureSupport(final StmtContext.Mutable<?, ?, ?> stmtContext,
            final Predicate<QName> isFeatureSupported) {
        final Collection<StatementContextBase<?, ?, ?>> substatements = stmtContext.declaredSubstatements();

        boolean isSupported = false;
        boolean containsIfFeature = false;
        for (final StatementContextBase<?, ?, ?> stmt : substatements) {
            if (stmt.getPublicDefinition().equals(Rfc6020Mapping.IF_FEATURE)) {
                containsIfFeature = true;
                if (isFeatureSupported.test((QName) stmt.getStatementArgument())) {
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
    public static boolean isPresenceContainer(final StatementContextBase<?, ?, ?> stmtCtx) {
        return stmtCtx.getPublicDefinition() == Rfc6020Mapping.CONTAINER && containsPresenceSubStmt(stmtCtx);
    }

    /**
     * Checks whether statement context is a non-presence container or not.
     *
     * @param stmtCtx
     *            statement context
     * @return true if it is a non-presence container
     */
    public static boolean isNonPresenceContainer(final StatementContextBase<?, ?, ?> stmtCtx) {
        return stmtCtx.getPublicDefinition() == Rfc6020Mapping.CONTAINER && !containsPresenceSubStmt(stmtCtx);
    }

    private static boolean containsPresenceSubStmt(final StatementContextBase<?, ?, ?> stmtCtx) {
        return findFirstSubstatement(stmtCtx, PresenceStatement.class) != null;
    }

    /**
     * Checks whether statement context is a mandatory node according to RFC6020
     * or not.
     *
     * @param stmtCtx
     *            statement context
     * @return true if it is a mandatory node according to RFC6020
     */
    public static boolean isMandatoryNode(final StatementContextBase<?, ?, ?> stmtCtx) {
        return isMandatoryListOrLeafList(stmtCtx) || isMandatoryLeafChoiceOrAnyXML(stmtCtx);
    }

    private static boolean isMandatoryLeafChoiceOrAnyXML(final StatementContextBase<?, ?, ?> stmtCtx) {
        if (!(stmtCtx.getPublicDefinition() instanceof Rfc6020Mapping)) {
            return false;
        }
        switch ((Rfc6020Mapping) stmtCtx.getPublicDefinition()) {
        case LEAF:
        case CHOICE:
        case ANYXML:
            return Boolean.TRUE.equals(firstSubstatementAttributeOf(stmtCtx, MandatoryStatement.class));
        default:
            return false;
        }
    }

    private static boolean isMandatoryListOrLeafList(final StatementContextBase<?, ?, ?> stmtCtx) {
        if (!(stmtCtx.getPublicDefinition() instanceof Rfc6020Mapping)) {
            return false;
        }
        switch ((Rfc6020Mapping) stmtCtx.getPublicDefinition()) {
        case LIST:
        case LEAF_LIST:
            final Integer minElements = firstSubstatementAttributeOf(stmtCtx, MinElementsStatement.class);
            return minElements != null && minElements > 0;
        default:
            return false;
        }
    }
}
