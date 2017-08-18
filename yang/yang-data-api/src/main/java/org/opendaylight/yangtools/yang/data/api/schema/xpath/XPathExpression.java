/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import com.google.common.annotations.Beta;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.xml.xpath.XPathExpressionException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A compiled XPath expression. Each instance is bound to a particular {@link XPathSchemaContext} and may not be
 * evaluated on {@link XPathDocument}s from other context.
 */
@Beta
public interface XPathExpression {
    /**
     * Evaluate this expression at the specified path in a document. If evaluation succeeds, it will return an
     * {@link XPathResult}. If it fails to match anything, it will return {@link Optional#empty()}. Implementations
     * of this method are expected to perform complete evaluation such that accessing data via the resulting
     * {@link XPathResult} will not incur large overhead.
     *
     * @param document {@link XPathDocument} on which evaluation should take place
     * @param path Path to the node on which to evaluate the expression
     * @return An optional {@link XPathResult}
     * @throws NullPointerException if any of the arguments are null
     * @throws XPathExpressionException if the expression cannot be evaluated
     * @throws IllegalArgumentException if the path does not match the path at which this expression was compiled
     */
    Optional<? extends XPathResult<?>> evaluate(@Nonnull XPathDocument document, @Nonnull YangInstanceIdentifier path)
            throws XPathExpressionException;

    /**
     * Return the evaluation context SchemaPath of this expression. This is corresponds to the SchemaPath at which this
     * expression was compiled at, or relocated to via {@link RelocatableXPathExpression#relocateExpression()}.
     *
     * @return The evaluation {@link SchemaPath}
     */
    @Nonnull SchemaPath getEvaluationPath();

    /**
     * Return the SchemaPath of the topmost node which affects the result of evaluation of this expression. This
     * information is useful for large evolving documents (such as
     * {@link org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree} implementations) to minimize the frequency
     * of evaluation. The apex can be either logically higher or lower in the SchemaPath tree than
     * {@link #getEvaluationPath()}.
     *
     * @return The apex node evaluation of this expression can reference, or {@link SchemaPath#ROOT} if it cannot
     *         cannot be conclusively determined.
     */
    @Nonnull SchemaPath getApexPath();
}
