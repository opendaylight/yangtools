/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace.SupportedFeatures;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.UnknownStatementImpl;

public final class StmtContextUtils {
    public static final Splitter LIST_KEY_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

    private static final Function<StmtContext<?, ?,?>, DeclaredStatement<?>> BUILD_DECLARED =
            new Function<StmtContext<?,?,?>, DeclaredStatement<?>>() {
        @Override
        public DeclaredStatement<?> apply(final StmtContext<?, ?, ?> input) {
            return input.buildDeclared();
        }
    };

    private static final Function<StmtContext<?, ?,?>, EffectiveStatement<?,?>> BUILD_EFFECTIVE =
            new Function<StmtContext<?,?,?>, EffectiveStatement<?,?>>() {
        @Override
        public EffectiveStatement<?, ?> apply(final StmtContext<?, ?, ?> input) {
            return input.buildEffective();
        }
    };

    private StmtContextUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SuppressWarnings("unchecked")
    public static <D extends DeclaredStatement<?>> Function<StmtContext<?, ? extends D, ?>, D> buildDeclared() {
        return Function.class.cast(BUILD_DECLARED);
    }

    @SuppressWarnings("unchecked")
    public static <E extends EffectiveStatement<?, ?>> Function<StmtContext<?, ?, ? extends E>, E> buildEffective() {
        return Function.class.cast(BUILD_EFFECTIVE);
    }

    @SuppressWarnings("unchecked")
    public static <AT, DT extends DeclaredStatement<AT>> AT firstAttributeOf(
            final Iterable<? extends StmtContext<?, ?, ?>> contexts, final Class<DT> declaredType) {
        for (StmtContext<?, ?, ?> ctx : contexts) {
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
        AT firstAttribute = firstAttributeOf(ctx.effectiveSubstatements(), declaredType);
        return firstAttribute != null ? firstAttribute : firstAttributeOf(ctx.declaredSubstatements(), declaredType);
    }

    @SuppressWarnings("unchecked")
    public static <AT,DT extends DeclaredStatement<AT>> StmtContext<AT, ?, ?> findFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        for (StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (producesDeclared(subStmtContext,declaredType)) {
                return (StmtContext<AT, ?, ?>) subStmtContext;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <AT, DT extends DeclaredStatement<AT>> Collection<StmtContext<AT, DT, ?>> findAllDeclaredSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        ImmutableList.Builder<StmtContext<AT, DT, ?>> listBuilder = ImmutableList.builder();
        for (StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (producesDeclared(subStmtContext, declaredType)) {
                listBuilder.add((StmtContext<AT, DT, ?>) subStmtContext);
            }
        }
        return listBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public static <AT, DT extends DeclaredStatement<AT>> Collection<StmtContext<AT, DT, ?>> findAllEffectiveSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> type) {
        ImmutableList.Builder<StmtContext<AT, DT, ?>> listBuilder = ImmutableList.builder();
        for (StmtContext<?, ?, ?> subStmtContext : stmtContext.effectiveSubstatements()) {
            if (producesDeclared(subStmtContext, type)) {
                listBuilder.add((StmtContext<AT, DT, ?>) subStmtContext);
            }
        }
        return listBuilder.build();
    }

    public static <AT, DT extends DeclaredStatement<AT>> Collection<StmtContext<AT, DT, ?>> findAllSubstatements(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> type) {
        ImmutableList.Builder<StmtContext<AT, DT, ?>> listBuilder = ImmutableList.builder();
        listBuilder.addAll(findAllDeclaredSubstatements(stmtContext, type));
        listBuilder.addAll(findAllEffectiveSubstatements(stmtContext, type));
        return listBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public static <AT,DT extends DeclaredStatement<AT>> StmtContext<AT, ?, ?> findFirstEffectiveSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        for (StmtContext<?, ?, ?> subStmtContext : stmtContext.effectiveSubstatements()) {
            if (producesDeclared(subStmtContext,declaredType)) {
                return (StmtContext<AT, ?, ?>) subStmtContext;
            }
        }
        return null;
    }

    public static <AT,DT extends DeclaredStatement<AT>> StmtContext<AT, ?, ?> findFirstSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        StmtContext<AT, ?, ?> declaredSubstatement = findFirstDeclaredSubstatement(stmtContext, declaredType);
        return declaredSubstatement != null ? declaredSubstatement : findFirstEffectiveSubstatement(stmtContext, declaredType);
    }

    @SafeVarargs
    public static StmtContext<?, ?, ?> findFirstDeclaredSubstatement(final StmtContext<?, ?, ?> stmtContext,
            int startIndex, final Class<? extends DeclaredStatement<?>>... types) {
        if (startIndex >= types.length) {
            return null;
        }

        for (StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (producesDeclared(subStmtContext,types[startIndex])) {
                return startIndex + 1 == types.length ? subStmtContext
                        : findFirstDeclaredSubstatement(subStmtContext, ++startIndex, types);
            }
        }
        return null;
    }

    public static <DT extends DeclaredStatement<?>> StmtContext<?, ?, ?> findFirstDeclaredSubstatementOnSublevel(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType, int sublevel) {
        for (StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
            if (sublevel == 1 && producesDeclared(subStmtContext, declaredType)) {
                return subStmtContext;
            }
            if (sublevel > 1) {
                final StmtContext<?, ?, ?> result = findFirstDeclaredSubstatementOnSublevel(
                    subStmtContext, declaredType, --sublevel);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static <DT extends DeclaredStatement<?>> StmtContext<?, ?, ?> findDeepFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {
        for (StmtContext<?, ?, ?> subStmtContext : stmtContext.declaredSubstatements()) {
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

    public static boolean isInExtensionBody(final StmtContext<?,?,?> stmtCtx) {
        StmtContext<?,?,?> current = stmtCtx;
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
        for (SchemaNodeIdentifier arg : keyStmtCtx.getStatementArgument()) {
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

        // This makes sure we reuse the collection when a grouping is instantiated in the same module
        return replaced ? builder.build() : keyStmtCtx.getStatementArgument();
    }

    public static boolean areFeaturesSupported(final StmtContext<?, ?, ?> stmtContext) {
        Predicate<QName> isFeatureSupported = stmtContext.getFromNamespace(SupportedFeaturesNamespace.class,
                SupportedFeatures.SUPPORTED_FEATURES);
        Collection<StatementContextBase<?, ?, ?>> substatements = new ArrayList<>();
        substatements.addAll(stmtContext.declaredSubstatements());
        substatements.addAll(stmtContext.effectiveSubstatements());

        boolean isSupported = false;
        boolean containsIfFeature = false;
        for (StatementContextBase<?, ?, ?> stmt: substatements) {
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
}
