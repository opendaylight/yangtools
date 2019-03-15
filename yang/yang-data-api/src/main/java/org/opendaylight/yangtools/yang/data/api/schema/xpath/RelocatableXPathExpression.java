/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Interface implemented by {@link XPathExpression}s which can be recompiled to execute more efficiently at a
 * at a different {@link SchemaPath} than they were originally compiled at. This can result in the expression being
 * moved either up or down in a SchemaPath tree, usually closer to their {@link #getApexPath()}.
 */
@Beta
@Deprecated
public interface RelocatableXPathExpression extends XPathExpression {
    /**
     * Return a new XPathExpression relocated to a SchemaPath of the implementation's choosing. Note that
     * {@link #getApexPath()} must not change during this operation.
     *
     * @return A new XPathExpression instance.
     */
    @NonNull XPathExpression relocateExpression();
}
