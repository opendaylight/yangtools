/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import javax.xml.xpath.XPathExpressionException;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QualifiedQName;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;

/**
 * An XPath expression. This interface defines a parsed representation of an XPath defined in a YANG context, as
 * specified in <a href="https://tools.ietf.org/html/rfc7950#section-6.4">RFC7950, Section 6.4</a>.
 *
 * <p>
 * The specification along with rules for {@code path} statement evaluation rules end up defining four incremental
 * levels to which an XPath expression can be bound:
 * <ul>
 * <li>Unbound Expressions, which is a essentially a parse tree. No namespace binding has been performed, i.e. all
 *     node identifiers are in {@link QualifiedQName} or {@link UnqualifiedQName} form. This level is typically not used
 *     when dealing with YANG models directly, but can be useful for validating a String conforms to XPath syntax.
 * </li>
 * <li>Qualified-bound Expressions, where all {@link QualifiedQName}s are resolved and bound to {@link QName}s, but
 *     {@link UnqualifiedQName}s are still present. This level corresponds to how far a YANG parser can interpret XPath
 *     expressions defined in {@code typedef} statements and statements which are not fully instantiated, i.e. are
 *     descendants of a {@code grouping} statement.
 * </li>
 * <li>Namespace-bound Expressions, where all node identifier references are resolved to {@link QName}s. This level
 *     corresponds to how far a YANG parser can interpret XPath expressions at their place of instantiation, either in
 *     the data tree or an {@code action}/@{code rpc}/{@code notification} or similar context.
 * </li>
 * <li>Context-bound Expressions, where the expression is bound to a {code context node}, i.e. {@code current()}
 *     function result is know. This API does not handle this state, as it is inherently bound to a particular data
 *     object model.
 * </li>
 * </ul>
 *
 * @author Robert Varga
 */
@Beta
public interface YangXPathExpression extends Immutable {
    /**
     * A Qualified-bound expression. All {@link QualifiedQName}s are eliminated and replaced with {@link QName}s.
     */
    interface QualifiedBound extends YangXPathExpression {

    }

    interface UnqualifiedBound extends QualifiedBound {
        @Override
        YangQNameExpr.Resolved interpretAsQName(YangLiteralExpr expr) throws XPathExpressionException;
    }

    /**
     * Return the root {@link YangExpr}.
     *
     * @return Root expression.
     */
    YangExpr getRootExpr();

    /**
     * Return the {@link YangXPathMathMode} used in this expression. All {@link YangNumberExpr} objects used by this
     * expression are expected to be handled by this mode's {@link YangXPathMathSupport}.
     *
     * @return YangXPathMathMode
     */
    YangXPathMathMode getMathMode();

    /**
     * Attempt to interpret a {@link YangLiteralExpr} referenced by this expression as a {@link QName}. This method
     * is required to perform late value binding of the expression when the literal needs to be interpreted as
     * a reference to an {@code identity}.
     *
     * <p>
     * The syntax of expr is required to conform to
     * <a href="https://www.w3.org/TR/REC-xml-names/#NT-QName">XML QName format</a>, as further restricted by
     * <a href="https://tools.ietf.org/html/rfc7950#section-9.10.3">YANG {@code identityref} Lexical Representation</a>.
     *
     * <p>
     * Unfortunately we do not know when a literal will need to be interpreted in this way, as that can only be known
     * at evaluation.
     *
     * @param expr Literal to be reinterpreted
     * @return YangQNameExpr result of interpretation
     * @throws XPathExpressionException when the literal cannot be interpreted as a QName
     */
    YangQNameExpr interpretAsQName(YangLiteralExpr expr) throws XPathExpressionException;

    // FIXME: this really should be YangInstanceIdentifier without AugmentationIdentifier. Implementations are
    //        strongly encouraged to validate it as such.
    YangLocationPath interpretAsInstanceIdentifier(YangLiteralExpr expr) throws XPathExpressionException;
}
