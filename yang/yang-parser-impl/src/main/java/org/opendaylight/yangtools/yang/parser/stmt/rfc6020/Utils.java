/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Arrays;
import java.util.List;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import com.google.common.base.CharMatcher;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import java.util.HashSet;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Date;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class Utils {

    private static final CharMatcher DOUBLE_QUOTE_MATCHER = CharMatcher.is('"');
    private static final CharMatcher SINGLE_QUOTE_MATCHER = CharMatcher.is('\'');

    public static final StatementSourceReference CONTEXT_REF = new StatementSourceReference() {

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.CONTEXT;
        }
    };

    public static String stringFromStringContext(final YangStatementParser.ArgumentContext context) {
        StringBuilder sb = new StringBuilder();
        List<TerminalNode> strings = context.STRING();
        if (strings.size() == 0) {
            strings = Arrays.asList(context.IDENTIFIER());
        }
        for (TerminalNode stringNode : strings) {
            final String str = stringNode.getText();
            char firstChar = str.charAt(0);
            final CharMatcher quoteMatcher;
            if(SINGLE_QUOTE_MATCHER.matches(firstChar)) {
                quoteMatcher = SINGLE_QUOTE_MATCHER;
            } else if (DOUBLE_QUOTE_MATCHER.matches(firstChar)) {
                quoteMatcher = DOUBLE_QUOTE_MATCHER;
            } else {
                sb.append(str);
                continue;
            }
            sb.append(quoteMatcher.removeFrom(str.substring(1, str.length()-1)));
        }
        return sb.toString();
    }

    public static QName qNameFromArgument(StmtContext<?, ?, ?> ctx, String value) {

        String prefix = null;
        QNameModule qNameModule = null;
        try {
            qNameModule = QNameModule.create(new URI(""), new Date(0));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String localName = null;

        String[] namesParts = value.split(":");
        switch (namesParts.length) {
        case 1:
            localName = namesParts[0];

            if (StmtContextUtils.producesDeclared(ctx.getRoot(),
                    ModuleStatement.class)) {
                prefix = firstAttributeOf(
                        ctx.getRoot().declaredSubstatements(),
                        PrefixStatement.class);
                qNameModule = ctx
                        .getFromNamespace(PrefixToModule.class, prefix);

            } else if (StmtContextUtils.producesDeclared(ctx.getRoot(),
                    SubmoduleStatement.class)) {
                String belongsToModuleName = firstAttributeOf(ctx.getRoot()
                        .declaredSubstatements(), BelongsToStatement.class);
                qNameModule = ctx.getFromNamespace(
                        ModuleNameToModuleQName.class, belongsToModuleName);
            }
            break;
        case 2:
            prefix = namesParts[0];
            localName = namesParts[1];

            ModuleIdentifier impModIdentifier = ctx.getRoot().getFromNamespace(
                    ImpPrefixToModuleIdentifier.class, prefix);
            qNameModule = ctx.getFromNamespace(
                    ModuleIdentifierToModuleQName.class, impModIdentifier);

            if (qNameModule == null
                    && StmtContextUtils.producesDeclared(ctx.getRoot(),
                            SubmoduleStatement.class)) {
                String moduleName = ctx.getRoot().getFromNamespace(
                        BelongsToPrefixToModuleName.class, prefix);
                qNameModule = ctx.getFromNamespace(
                        ModuleNameToModuleQName.class, moduleName);
            }

            break;
        default:
            break;
        }

        return QName.create(qNameModule, localName);
    }

    /**
     * @param sourceGrpStmtCtx
     * @param targetCtx
     * @throws SourceException
     */
    public static void copyFromSourceToTarget(
            StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            StatementContextBase<?, ?, ?> targetCtx) throws SourceException {

        QNameModule newQNameModule = getNewQNameModule(targetCtx,
                sourceGrpStmtCtx);
        copyDeclaredStmts(sourceGrpStmtCtx, targetCtx, newQNameModule);
        copyEffectiveStmts(sourceGrpStmtCtx, targetCtx, newQNameModule);

    }

    private static void copyDeclaredStmts(
            StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            StatementContextBase<?, ?, ?> targetCtx, QNameModule newQNameModule)
            throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> declaredSubstatements = sourceGrpStmtCtx
                .declaredSubstatements();
        for (StatementContextBase<?, ?, ?> originalStmtCtx : declaredSubstatements) {
            if (needToCopyByUses(originalStmtCtx)) {
                StatementContextBase<?, ?, ?> copy = originalStmtCtx
                        .createCopy(newQNameModule,targetCtx);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByUses(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    private static void copyEffectiveStmts(
            StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            StatementContextBase<?, ?, ?> targetCtx, QNameModule newQNameModule)
            throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> effectiveSubstatements = sourceGrpStmtCtx
                .effectiveSubstatements();
        for (StatementContextBase<?, ?, ?> originalStmtCtx : effectiveSubstatements) {
            if (needToCopyByUses(originalStmtCtx)) {
                StatementContextBase<?, ?, ?> copy = originalStmtCtx
                        .createCopy(newQNameModule,targetCtx);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByUses(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    private static QNameModule getNewQNameModule(
            StatementContextBase<?, ?, ?> targetCtx,
            StmtContext<?, ?, ?> stmtContext) {
        if (needToCreateNewQName(stmtContext.getPublicDefinition())) {
            Object targetStmtArgument = targetCtx.getStatementArgument();
            Object sourceStmtArgument = stmtContext.getStatementArgument();
            if (targetStmtArgument instanceof QName
                    && sourceStmtArgument instanceof QName) {
                QName targetQName = (QName) targetStmtArgument;
                QNameModule targetQNameModule = targetQName.getModule();

                QName sourceQName = (QName) sourceStmtArgument;
                QNameModule sourceQNameModule = sourceQName.getModule();

                if (targetQNameModule.equals(sourceQNameModule))
                    return null;
                else
                    return targetQNameModule;
            } else
                return null;
        } else
            return null;
    }

    public static boolean needToCreateNewQName(
            StatementDefinition publicDefinition) {
        return true;
    }

    public static boolean needToCopyByUses(StmtContext<?, ?, ?> stmtContext) {

        HashSet<StatementDefinition> noCopyDefSet = new HashSet<StatementDefinition>();
        noCopyDefSet.add(Rfc6020Mapping.Uses);

        StatementDefinition def = stmtContext.getPublicDefinition();
        if (noCopyDefSet.contains(def))
            return false;
        else
            return true;
    }

    public static boolean isReusedByUses(StmtContext<?, ?, ?> stmtContext) {

        HashSet<StatementDefinition> reusedDefSet = new HashSet<StatementDefinition>();
        reusedDefSet.add(Rfc6020Mapping.Typedef);

        StatementDefinition def = stmtContext.getPublicDefinition();
        if (reusedDefSet.contains(def))
            return true;
        else
            return false;
    }

}
