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

@Beta
public interface YangXPathExpression extends Immutable {

    YangExpr getRootExpr();

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

    // API design: this really should be YangInstanceIdentifier without AugmentationIdentifier. Implementations are
    //             strongly encouraged to validate it as such.
    YangLocationPath interpretAsInstanceIdentifier(YangLiteralExpr expr) throws XPathExpressionException;
}
