package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.Collection;

import com.google.common.base.Function;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public class StmtContextUtils {

    public static final Function<StmtContext<?, ?, ?>, DeclaredStatement<?>> BUILD_DECLARED = new Function<StmtContext<?, ?, ?>, DeclaredStatement<?>>() {

        @Override
        public DeclaredStatement<?> apply(StmtContext<?, ?, ?> input) {
            return input.buildDeclared();
        }

    };

    public static final Function<StmtContext<?, ?, ?>, EffectiveStatement<?, ?>> BUILD_EFFECTIVE = new Function<StmtContext<?, ?, ?>, EffectiveStatement<?, ?>>() {

        @Override
        public EffectiveStatement<?, ?> apply(StmtContext<?, ?, ?> input) {
            return input.buildEffective();
        }

    };

    @SuppressWarnings("unchecked")
    public static final <D extends DeclaredStatement<?>> Function<StmtContext<?, ? extends D, ?>, D> buildDeclared() {
        return Function.class.cast(BUILD_DECLARED);
    }

    @SuppressWarnings("unchecked")
    public static final <E extends EffectiveStatement<?, ?>> Function<StmtContext<?, ?, ? extends E>, E> buildEffective() {
        return Function.class.cast(BUILD_EFFECTIVE);
    }

    @SuppressWarnings("unchecked")
    public static final <AT, DT extends DeclaredStatement<AT>> AT firstAttributeOf(
            Iterable<? extends StmtContext<?, ?, ?>> contexts,
            Class<DT> declaredType) {
        for (StmtContext<?, ?, ?> ctx : contexts) {
            if (producesDeclared(ctx, declaredType)) {
                return (AT) ctx.getStatementArgument();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static final <AT, DT extends DeclaredStatement<AT>> AT firstAttributeOf(
            StmtContext<?, ?, ?> ctx, Class<DT> declaredType) {

        if (producesDeclared(ctx, declaredType)) {
            return (AT) ctx.getStatementArgument();
        }

        return null;
    }

    public static final <DT extends DeclaredStatement<?>> StmtContext<?, ?, ?> findFirstDeclaredSubstatement(
            StmtContext<?, ?, ?> stmtContext, Class<DT> declaredType) {
        Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements = stmtContext
                .declaredSubstatements();
        for (StmtContext<?, ?, ?> subStmtContext : declaredSubstatements) {
            if (producesDeclared(subStmtContext,declaredType)) {
                return subStmtContext;
            }
        }
        return null;
    }

    public static final StmtContext<?, ?, ?> findFirstDeclaredSubstatement(
            StmtContext<?, ?, ?> stmtContext, int startIndex, Class<? extends DeclaredStatement<?>>... types) {

        if (startIndex >= types.length)
            return null;

        Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements = stmtContext
                .declaredSubstatements();
        for (StmtContext<?, ?, ?> subStmtContext : declaredSubstatements) {
            if (producesDeclared(subStmtContext,types[startIndex])) {
                if (startIndex + 1 == types.length)
                    return subStmtContext;
                else
                    return findFirstDeclaredSubstatement(subStmtContext,
                            ++startIndex, types);
            }
        }
        return null;
    }

    public static final <DT extends DeclaredStatement<?>> StmtContext<?, ?, ?> findFirstDeclaredSubstatementOnSublevel(
            StmtContext<?, ?, ?> stmtContext, Class<DT> declaredType,
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
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }

    public static final <DT extends DeclaredStatement<?>> StmtContext<?, ?, ?> findDeepFirstDeclaredSubstatement(
            StmtContext<?, ?, ?> stmtContext, Class<DT> declaredType) {

        Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements = stmtContext
                .declaredSubstatements();

        for (StmtContext<?, ?, ?> subStmtContext : declaredSubstatements) {
            if (producesDeclared(subStmtContext,declaredType)) {
                return subStmtContext;
            } else {
                StmtContext<?, ?, ?> result = findDeepFirstDeclaredSubstatement(
                        subStmtContext, declaredType);
                if (result != null)
                    return result;

            }
        }
        return null;
    }

    public static final boolean producesDeclared(StmtContext<?, ?, ?> ctx,
            Class<? extends DeclaredStatement<?>> type) {
        return type.isAssignableFrom(ctx.getPublicDefinition()
                .getDeclaredRepresentationClass());
    }
    //
    // @SuppressWarnings("unchecked")
    // public static final <AT,DT extends DeclaredStatement<AT>> AT
    // firstAttributeOf(Iterable<? extends StmtContext<?,?,?>> contexts,
    // Class<DT> declaredType) {
    // for(StmtContext<?, ?, ?> ctx : contexts) {
    // if(producesDeclared(ctx,declaredType)) {
    // return (AT) ctx.getStatementArgument();
    // }
    // }
    // return null;
    // }

}