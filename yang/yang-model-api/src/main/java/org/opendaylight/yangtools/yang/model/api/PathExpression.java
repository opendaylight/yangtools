/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;

/**
 * An expression as defined in <a href="https://tools.ietf.org/html/rfc7950#section-9.9.2">RFC7950 Section 9.9.2</a>,
 * i.e. the argument of a {@code path} statement.
 *
 * <p>
 * Semantically a {@link PathExpression} is similar to a {@link YangXPathExpression} with guarantees around what
 * subexpressions it can contain:
 * <ul>
 * <li>the root expression must be a {@link YangLocationPath}</li>
 * <li>it can contain steps only along {@link YangXPathAxis#DESCENDANT} and {@link YangXPathAxis#PARENT} axis</li>
 * <li>all steps along {@link YangXPathAxis#DESCENDANT} axis are {@link QNameStep}</li>
 * <li>the only function invocation is {@link YangFunction#CURRENT}</li>
 * <li>only {@link YangBinaryOperator#EQUALS} is allowed</li>
 * <li>no literals nor numbers are allowed</li>
 * </ul>
 *
 * @author Robert Varga
 */
@Beta
public interface PathExpression extends Immutable {
    /**
     * Return the {@link YangLocationPath} of this expression.
     *
     * @return The location path
     */
    @NonNull YangLocationPath getLocation();
}
