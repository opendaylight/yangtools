/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Contains methods for getting data (concrete XPath) and metadata (is XPath absolute) from XPath instance.
 */
// FIXME: 4.0.0: find a better name for this interface
public interface RevisionAwareXPath {
    /**
     * Returns <code>true</code> if the XPapth starts in root of Yang model, otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if the XPapth starts in root of Yang model, otherwise returns <code>false</code>
     */
    boolean isAbsolute();

    /**
     * Returns the XPath formatted string as is defined in model. For example:
     * /prefix:container/prefix:container::cond[when()=foo]/prefix:leaf
     *
     * @return the XPath formatted string as is defined in model.
     */
    @NonNull String getOriginalString();

    @Beta
    // FIXME: 4.0.0: integrate this interface into RevisionAwareXPath
    interface WithExpression extends RevisionAwareXPath {
        /**
         * Return the {@link YangXPathExpression} of this XPath. The expression is required to be at least
         * Qualified-bound.
         *
         * @return The location path
         */
        @NonNull QualifiedBound getXPathExpression();
    }
}
