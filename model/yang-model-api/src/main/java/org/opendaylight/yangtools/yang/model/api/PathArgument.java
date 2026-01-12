/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangPathExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;

/**
 * An expression as defined in <a href="https://www.rfc-editor.org/rfc/rfc7950#section-9.9.2">RFC7950 Section 9.9.2</a>,
 * i.e. the argument of a {@code path} statement.
 *
 * <p>Semantically a {@link PathArgument} is similar to a {@link YangXPathExpression} with guarantees around what
 * subexpressions it can contain:
 * <ul>
 *   <li>the root expression must be a {@link YangLocationPath}</li>
 *   <li>it can contain steps only along {@link YangXPathAxis#CHILD} and {@link YangXPathAxis#PARENT} axis</li>
 *   <li>all steps along {@link YangXPathAxis#CHILD} axis are {@link QNameStep}</li>
 *   <li>the only function invocation is {@link YangFunction#CURRENT}</li>
 *   <li>only {@link YangBinaryOperator#EQUALS} is allowed</li>
 *   <li>no literals nor numbers are allowed</li>
 *   <li>all qualified node identifiers must me resolved</li>
 * </ul>
 */
@NonNullByDefault
public sealed interface PathArgument extends Immutable {
    /**
     * Steps of a {@link PathArgument} which is a {@link YangLocationPath locationPath}, corresponding to RFC7950 base
     * specification.
     */
    record LocationPath(String originalString, YangLocationPath locationPath) implements PathArgument {
        public LocationPath {
            requireNonNull(originalString);
            requireNonNull(locationPath);
        }

        @Override
        public boolean isAbsolute() {
            return locationPath.isAbsolute();
        }
    }

    /**
     * Steps of a PathExpression which is a combination of {@code deref()} function call and a relative path,
     * corresponding to <a href="https://www.rfc-editor.org/errata/eid5617">Errata 5617</a>. The corresponding construct
     * is a {@link YangPathExpr} with filter being an invocation of {@link YangFunction#DEREF}.
     */
    record DerefExpr(String originalString, Relative derefArgument, Relative relativePath) implements PathArgument {
        public DerefExpr {
            requireNonNull(originalString);
            requireNonNull(derefArgument);
            requireNonNull(relativePath);
        }

        @Override
        public boolean isAbsolute() {
            return false;
        }
    }

    /**
     * {@return the path expression formatted string as is defined in model. For example:
     * {@code /prefix:container/prefix:container::cond[when()=foo]/prefix:leaf}}
     */
    String originalString();

    /**
     * Returns the path expression formatted string as is defined in model. For example:
     * {@code /prefix:container/prefix:container::cond[when()=foo]/prefix:leaf}
     *
     * @return the path expression formatted string as is defined in model
     * @deprecated Use {@link #originalString()} instead
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default String getOriginalString() {
        return originalString();
    }

    /**
     * {@return {@code true} if the XPath starts in root of YANG model, otherwise {@code false}}
     */
    boolean isAbsolute();
}
