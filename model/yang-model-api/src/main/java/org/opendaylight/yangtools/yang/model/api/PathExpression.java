/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * An expression as defined in <a href="https://tools.ietf.org/html/rfc7950#section-9.9.2">RFC7950 Section 9.9.2</a>,
 * i.e. the argument of a {@code path} statement.
 *
 * <p>
 * Semantically a {@link PathExpression} is similar to a {@link YangXPathExpression} with guarantees around what
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
public interface PathExpression extends Immutable {
    /**
     * Abstract base class for expressing steps of a PathExpression.
     */
    abstract sealed class Steps {
        Steps() {
            // Prevent external subclassing
        }

        @Override
        public abstract int hashCode();

        @Override
        public abstract boolean equals(@Nullable Object obj);

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
        }

        abstract ToStringHelper addToStringAttributes(ToStringHelper helper);
    }

    /**
     * Steps of a PathExpression which is a LocationPath, corresponding to RFC7950 base specification.
     */
    final class LocationPathSteps extends Steps {
        private final YangLocationPath locationPath;

        public LocationPathSteps(final YangLocationPath locationPath) {
            this.locationPath = requireNonNull(locationPath);
        }

        public YangLocationPath getLocationPath() {
            return locationPath;
        }

        @Override
        public int hashCode() {
            return locationPath.hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof LocationPathSteps other && locationPath.equals(other.locationPath);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("locationPath", locationPath);
        }
    }

    /**
     * Steps of a PathExpression which is a combination of {@code deref()} function call and a relative path,
     * corresponding to Errata 5617.
     */
    final class DerefSteps extends Steps {
        private final Relative derefArgument;
        private final Relative relativePath;

        public DerefSteps(final Relative derefArgument, final Relative relativePath) {
            this.derefArgument = requireNonNull(derefArgument);
            this.relativePath = requireNonNull(relativePath);
        }

        public Relative getDerefArgument() {
            return derefArgument;
        }

        public Relative getRelativePath() {
            return relativePath;
        }

        @Override
        public int hashCode() {
            return 31 * derefArgument.hashCode() + relativePath.hashCode();
        }

        @Override
        public boolean equals(@Nullable final Object obj) {
            return this == obj || obj instanceof DerefSteps other
                && derefArgument.equals(other.derefArgument) && relativePath.equals(other.relativePath);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("derefArgument", derefArgument).add("relativePath", relativePath);
        }
    }

    /**
     * Returns the path expression formatted string as is defined in model. For example:
     * {@code /prefix:container/prefix:container::cond[when()=foo]/prefix:leaf}
     *
     * @return the path expression formatted string as is defined in model.
     */
    String getOriginalString();

    /**
     * Return the path of this expression, which can either be a {@link YangLocationPath} (compliant to RFC7950) or
     * a {@link YangPathExpr} with filter being an invocation of {@link YangFunction#DEREF}.
     *
     * @return The path's steps
     * @throws UnsupportedOperationException if the implementation has not parsed the string. Implementations are
     *         strongly encouraged to perform proper parsing.
     */
    Steps getSteps();

    /**
     * Returns <code>true</code> if the XPapth starts in root of YANG model, otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if the XPapth starts in root of YANG model, otherwise returns <code>false</code>
     */
    default boolean isAbsolute() {
        return getSteps() instanceof LocationPathSteps locationSteps && locationSteps.getLocationPath().isAbsolute();
    }
}
