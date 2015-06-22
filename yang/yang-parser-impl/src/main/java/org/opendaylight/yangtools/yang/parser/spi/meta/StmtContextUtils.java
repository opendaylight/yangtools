/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.opendaylight.yangtools.yang.common.QName;
import com.google.common.base.Splitter;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.UnknownStatementImpl;
import com.google.common.base.Predicate;
import java.util.Collection;
import com.google.common.base.Function;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public final class StmtContextUtils {

    public static final char LIST_KEY_SEPARATOR = ' ';

    private static final Function<StmtContext<?, ?,?>, DeclaredStatement<?>> BUILD_DECLARED = new Function<StmtContext<?,?,?>, DeclaredStatement<?>>() {
        @Override
        public DeclaredStatement<?> apply(final StmtContext<?, ?, ?> input) {
            return input.buildDeclared();
        }
    };

    private static final Function<StmtContext<?, ?,?>, EffectiveStatement<?,?>> BUILD_EFFECTIVE = new Function<StmtContext<?,?,?>, EffectiveStatement<?,?>>() {
        @Override
        public EffectiveStatement<?, ?> apply(final StmtContext<?, ?, ?> input) {
            return input.buildEffective();
        }
    };

    public static final Predicate<StmtContext<?, ?,?>> IS_SUPPORTED_TO_BUILD_EFFECTIVE = new Predicate<StmtContext<?,?,?>>() {
        @Override
        public boolean apply(StmtContext<?, ?, ?> input) {
            return input.isSupportedToBuildEffective();
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
    public static final <E extends EffectiveStatement<?, ?>> Function<StmtContext<?, ?, ? extends E>, E> buildEffective() {
        return Function.class.cast(BUILD_EFFECTIVE);
    }

    @SuppressWarnings("unchecked")
    public static final <AT, DT extends DeclaredStatement<AT>> AT firstAttributeOf(
            final Iterable<? extends StmtContext<?, ?, ?>> contexts,
            final Class<DT> declaredType) {
        for (StmtContext<?, ?, ?> ctx : contexts) {
            if (producesDeclared(ctx, declaredType)) {
                return (AT) ctx.getStatementArgument();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static final <AT, DT extends DeclaredStatement<AT>> AT firstAttributeOf(
            final StmtContext<?, ?, ?> ctx, final Class<DT> declaredType) {

        if (producesDeclared(ctx, declaredType)) {
            return (AT) ctx.getStatementArgument();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static final <AT,DT extends DeclaredStatement<AT>> StmtContext<AT, ?, ?> findFirstDeclaredSubstatement(
            StmtContext<?, ?, ?> stmtContext, Class<DT> declaredType) {
        Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements = stmtContext
                .declaredSubstatements();
        for (StmtContext<?, ?, ?> subStmtContext : declaredSubstatements) {
            if (producesDeclared(subStmtContext,declaredType)) {
                return (StmtContext<AT, ?, ?>) subStmtContext;
            }
        }
        return null;
    }

    public static final StmtContext<?, ?, ?> findFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, int startIndex, final Class<? extends DeclaredStatement<?>>... types) {

        if (startIndex >= types.length) {
            return null;
        }

        Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements = stmtContext
                .declaredSubstatements();
        for (StmtContext<?, ?, ?> subStmtContext : declaredSubstatements) {
            if (producesDeclared(subStmtContext,types[startIndex])) {
                if (startIndex + 1 == types.length) {
                    return subStmtContext;
                } else {
                    return findFirstDeclaredSubstatement(subStmtContext,
                            ++startIndex, types);
                }
            }
        }
        return null;
    }

    public static final <DT extends DeclaredStatement<?>> StmtContext<?, ?, ?> findFirstDeclaredSubstatementOnSublevel(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType,
            int sublevel) {
        Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements = stmtContext
                .declaredSubstatements();
        for (StmtContext<?, ?, ?> subStmtContext : declaredSubstatements) {
            if (sublevel == 1 && producesDeclared(subStmtContext,declaredType)) {
                return subStmtContext;
            } else {
                if (sublevel > 1) {
                    StmtContext<?, ?, ?> result = findFirstDeclaredSubstatementOnSublevel(
                            subStmtContext, declaredType, --sublevel);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public static final <DT extends DeclaredStatement<?>> StmtContext<?, ?, ?> findDeepFirstDeclaredSubstatement(
            final StmtContext<?, ?, ?> stmtContext, final Class<DT> declaredType) {

        Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements = stmtContext
                .declaredSubstatements();

        for (StmtContext<?, ?, ?> subStmtContext : declaredSubstatements) {
            if (producesDeclared(subStmtContext,declaredType)) {
                return subStmtContext;
            } else {
                StmtContext<?, ?, ?> result = findDeepFirstDeclaredSubstatement(
                        subStmtContext, declaredType);
                if (result != null) {
                    return result;
                }

            }
        }
        return null;
    }

    public static final boolean producesDeclared(final StmtContext<?, ?, ?> ctx,
            final Class<? extends DeclaredStatement<?>> type) {
        return type.isAssignableFrom(ctx.getPublicDefinition()
                .getDeclaredRepresentationClass());
    }

    public static boolean isInExtensionBody(
            StmtContext<?,?,?> stmtCtx) {

        StmtContext<?,?,?> current = stmtCtx;
        while(!current.getParentContext().isRootContext()) {
            current = current.getParentContext();
            if(producesDeclared(current, UnknownStatementImpl.class)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isUnknownStatement(StmtContext<?, ?, ?> stmtCtx) {
        if (producesDeclared(stmtCtx, UnknownStatementImpl.class)) {
            return true;
        } else {
            return false;
        }
    }

    public static Set<TypeOfCopy> getCopyTypesFromOriginal(
            StmtContext<?, ?, ?> ctx) {

        Set<TypeOfCopy> copyTypesFromOriginal = new HashSet<>();
        StmtContext<?,?,?> current = ctx;

        while(current.getOriginalCtx()!=null){
            copyTypesFromOriginal.add(current.getTypeOfCopy());
            current = current.getOriginalCtx();
        }

        return copyTypesFromOriginal;
    }

    public static Collection<SchemaNodeIdentifier> replaceModuleQNameForKey(StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> keyStmtCtx,
            QNameModule newQNameModule) {

        Splitter keySplitter = Splitter.on(LIST_KEY_SEPARATOR).omitEmptyStrings().trimResults();
        List<String> keyTokens = keySplitter.splitToList(keyStmtCtx.rawStatementArgument());

        Set<SchemaNodeIdentifier> newKeys = new HashSet<>();

        for (String keyToken : keyTokens) {
            QName keyQName = QName.create(newQNameModule, keyToken);
            SchemaNodeIdentifier keyIdentifier = SchemaNodeIdentifier
                    .create(false, keyQName);
            newKeys.add(keyIdentifier);
        }

        return newKeys;
    }
}
