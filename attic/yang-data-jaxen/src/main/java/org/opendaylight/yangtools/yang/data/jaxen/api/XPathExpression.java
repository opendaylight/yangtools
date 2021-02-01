/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A compiled XPath expression. Each instance is bound to a particular {@link XPathSchemaContext} and may not be
 * evaluated on {@link XPathDocument}s from other context.
 */
@Beta
@Deprecated
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
    Optional<? extends XPathResult<?>> evaluate(@NonNull XPathDocument document, @NonNull YangInstanceIdentifier path)
            throws XPathExpressionException;

}
