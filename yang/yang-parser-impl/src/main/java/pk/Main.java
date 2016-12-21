/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package pk;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.Identifier_ref_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.If_feature_exprContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.If_feature_factorContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.If_feature_termContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParserBaseVisitor;
import org.opendaylight.yangtools.yang.common.QName;

public class Main {
    public static void main(final String[] args) {
        final IfFeatureExpressionLexer lexer = new IfFeatureExpressionLexer(new ANTLRInputStream(
                "p1:f1 or p2:f2 and p3:f3 and (f4 or p5:f5 and f6) and f7 or f8 and not ( f9 or f10 ) or f11"));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final IfFeatureExpressionParser parser = new IfFeatureExpressionParser(tokens);

        final IfFeaturePredicateVisitor visitor = new IfFeaturePredicateVisitor();
        final Predicate<Set<QName>> argument = visitor.visit(parser.if_feature_expr());

        Set<QName> set = ImmutableSet.of(QName.create("p1","f1"));
        System.out.println(argument.test(set));

        set = ImmutableSet.of(QName.create("foo","f11"));
        System.out.println(argument.test(set));

        set = ImmutableSet.of(QName.create("foo","f8"));
        System.out.println(argument.test(set));

        set = ImmutableSet.of(QName.create("foo","f8"), QName.create("foo","f9"));
        System.out.println(argument.test(set));
        // parser.if_feature_expr().inspect(parser);
        // final ParseTree tree = parser.if_feature_expr();
        // //System.out.println(parser.if_feature_expr().toStringTree(parser));

    }
}

/*
 *
 * if_feature_expr: if_feature_term (SEP OR SEP if_feature_expr)?;
 * if_feature_term: if_feature_factor (SEP AND SEP if_feature_term)?;
 * if_feature_factor: NOT SEP if_feature_factor
 *                  | LP SEP? if_feature_expr SEP? RP
 *                  | identifier_ref_arg;
 */
class IfFeaturePredicateVisitor extends IfFeatureExpressionParserBaseVisitor<Predicate<Set<QName>>> {

    @Override
    public Predicate<Set<QName>> visitIf_feature_expr(final If_feature_exprContext ctx) {
        if (ctx.if_feature_expr() != null) {
            return visitIf_feature_term(ctx.if_feature_term()).or(visitIf_feature_expr(ctx.if_feature_expr()));
        } else {
            return visitIf_feature_term(ctx.if_feature_term());
        }
    }

    @Override
    public Predicate<Set<QName>> visitIf_feature_term(final If_feature_termContext ctx) {
        if (ctx.if_feature_term() != null) {
            return visitIf_feature_factor(ctx.if_feature_factor()).and(visitIf_feature_term(ctx.if_feature_term()));
        } else {
            return visitIf_feature_factor(ctx.if_feature_factor());
        }
    }

    @Override
    public Predicate<Set<QName>> visitIf_feature_factor(final If_feature_factorContext ctx) {
        if(ctx.if_feature_expr() != null) {
            return visitIf_feature_expr(ctx.if_feature_expr());
        } else if (ctx.if_feature_factor() != null) {
            return visitIf_feature_factor(ctx.if_feature_factor()).negate();
        } else if (ctx.identifier_ref_arg() != null) {
            return visitIdentifier_ref_arg(ctx.identifier_ref_arg());
        }

        throw new IllegalArgumentException("Unexpected context");
    }

    @Override
    public Predicate<Set<QName>> visitIdentifier_ref_arg(final Identifier_ref_argContext ctx) {
        final List<TerminalNode> prefixedIdentifier = ctx.IDENTIFIER();

        QName qName = null;
        switch (prefixedIdentifier.size()) {
        case 1:
            qName = QName.create("foo", prefixedIdentifier.get(0).getText());
            break;
        case 2:
            qName = QName.create(prefixedIdentifier.get(0).getText(), prefixedIdentifier.get(1).getText());
            break;
        default:
            throw new IllegalArgumentException("Illegal colon in identifier ref arg");
        }

        final QName featureQName = qName;
        return setQNames -> setQNames.contains(featureQName);
    }
}