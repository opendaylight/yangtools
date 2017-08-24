/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.xml.xpath.XPathExpressionException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Asynchronous interface to evaluation. It is functionally the same as an XPathExpression, but allows for asynchronous
 * execution of evaluation of the expression.
 *
 * <p>
 * FIXME: Whether or not the resulting XPathResult can perform blocking calls is up for grabs, but implementations are
 *        definitely allowed to perform things like on-demand data transformation from foreign object and data models.
 *
 * @deprecated PREVIEW API. DO NOT IMPLEMENT YET AS THIS NEEDS TO BE VALIDATED FOR USE IN CLIENT APPLICATIONS.
 *             APPLICATIONS WILLING TO USE THIS API PLEASE CONTACT
 *             <a href="mailto:yangtools-dev@lists.opendaylight.org">yangtools-dev</a>.
 */
@Beta
@Deprecated
public interface LazyXPathExpression {
    /**
     * Evaluate this expression at the specified path in a document. If evaluation succeeds, it will return an
     * {@link XPathResult} at some point it the future. If it fails to match anything, it {@link Future#get()} will
     * return {@link Optional#absent()}.
     *
     * <p>
     * FIXME: The amount of overhead an implementation can incur on the user as data from the resulting
     *        {@link XPathResult} is being accessed is left UNDEFINED.
     *        Specifically, the user is expected to store each result returned directly or indirectly in a local
     *        variable instead of repeated calls to the result's methods, as these may incur CPU processing overhead.
     *        Furthermore all method invocations can throw {@link LazyXPathExpressionException}, which the users are
     *        expected to handle gracefully. RESILIENT USERS ARE EXPECTED TO CATCH {@link LazyXPathExpressionException}
     *        AND RECOVER IN THE SAME MANNER THEY WOULD IF AN {@link XPathExpressionException} WOULD HAVE BEEN THROWN.
     *        [ FIXME: would it be appropriate to allow implementations to SneakyThrow {@link XPathExpressionException}
     *                 and not introduce a RuntimeExpcetion ? ]
     *
     * @param document {@link XPathDocument} on which evaluation should take place
     * @param path Path to the node on which to evaluate the expression
     * @return An optional {@link XPathResult}
     * @throws NullPointerException if any of the arguments are null
     * @throws IllegalArgumentException if the path does not match the path at which this expression was compiled
     */
    ListenableFuture<Optional<? extends XPathResult<?>>> evaluateLazily(@Nonnull XPathDocument document,
            @Nonnull YangInstanceIdentifier path);
}
