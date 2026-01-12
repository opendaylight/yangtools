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
 * <p>Semantically a {@link PathExpression} is similar to a {@link YangXPathExpression} with guarantees around what
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
 *
 * @param originalString the path expression formatted string as is defined in model. For example:
 *                       {@code /prefix:container/prefix:container::cond[when()=foo]/prefix:leaf}
 * @param steps the path of this expression, which can either be a {@link YangLocationPath} (compliant to RFC7950) or
 *              a {@link YangPathExpr} with filter being an invocation of {@link YangFunction#DEREF}
 */
@NonNullByDefault
public record PathExpression(String originalString, Steps steps) implements Immutable {
    /**
     * Abstract base class for expressing steps of a PathExpression.
     */
    public sealed interface Steps {
        // Marker interface
    }

    /**
     * Steps of a PathExpression which is a LocationPath, corresponding to RFC7950 base specification.
     */
    public record LocationPathSteps(YangLocationPath locationPath) implements Steps {
        public LocationPathSteps {
            requireNonNull(locationPath);
        }

        @Deprecated(since = "15.0.0", forRemoval = true)
        public YangLocationPath getLocationPath() {
            return locationPath;
        }
    }

    /**
     * Steps of a PathExpression which is a combination of {@code deref()} function call and a relative path,
     * corresponding to Errata 5617.
     */
    public record DerefSteps(Relative derefArgument, Relative relativePath) implements Steps {
        public DerefSteps {
            requireNonNull(derefArgument);
            requireNonNull(relativePath);
        }

        @Deprecated(since = "15.0.0", forRemoval = true)
        public Relative getDerefArgument() {
            return derefArgument;
        }

        @Deprecated(since = "15.0.0", forRemoval = true)
        public Relative getRelativePath() {
            return relativePath;
        }
    }

    /**
     * Default constructor.
     *
     * @param originalString the path expression formatted string as is defined in model. For example:
     *                       {@code /prefix:container/prefix:container::cond[when()=foo]/prefix:leaf}
     * @param steps the path of this expression, which can either be a {@link YangLocationPath} (compliant to RFC7950)
     *              or a {@link YangPathExpr} with filter being an invocation of {@link YangFunction#DEREF}
     */
    public PathExpression {
        requireNonNull(originalString);
        requireNonNull(steps);
    }

    /**
     * Returns the path expression formatted string as is defined in model. For example:
     * {@code /prefix:container/prefix:container::cond[when()=foo]/prefix:leaf}
     *
     * @return the path expression formatted string as is defined in model
     * @deprecated Use {@link #originalString()} instead
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    public String getOriginalString() {
        return originalString;
    }

    /**
     * Return the path of this expression, which can either be a {@link YangLocationPath} (compliant to RFC7950) or
     * a {@link YangPathExpr} with filter being an invocation of {@link YangFunction#DEREF}.
     *
     * @return The path's steps
     * @deprecated Use {@link #steps()} instead
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    public Steps getSteps() {
        return steps;
    }

    /**
     * Returns <code>true</code> if the XPapth starts in root of YANG model, otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if the XPapth starts in root of YANG model, otherwise returns <code>false</code>
     */
    public boolean isAbsolute() {
        return steps instanceof LocationPathSteps(var locationPath) && locationPath.isAbsolute();
    }
}
