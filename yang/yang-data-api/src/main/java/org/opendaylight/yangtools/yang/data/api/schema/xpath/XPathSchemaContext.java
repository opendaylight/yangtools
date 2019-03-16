/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import com.google.common.annotations.Beta;
import com.google.common.base.Converter;
import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A schema-informed XPath context. It supports creation of {@link XPathDocument}s, which are bound to
 * a particular root node.
 */
@Beta
@Deprecated
public interface XPathSchemaContext {
    /**
     * Compile an XPath expression for execution on {@link XPathDocument}s produced by this context.
     *
     * <p>
     * The user must provide a prefix-to-mapping {@link Converter}, which will be used to convert any prefixes found
     * in the XPath expression being compiled in the resulting context.
     *
     * @param schemaPath Schema path of the node at which this expression is expected to be evaluated
     * @param prefixes Prefix-to-namespace converter
     * @param xpath XPath expression to compile
     * @return A compiled XPath expression
     * @throws XPathExpressionException if the provided expression is invalid, either syntactically or by referencing
     *         namespaces unknown to this schema context.
     */
    @NonNull XPathExpression compileExpression(@NonNull SchemaPath schemaPath,
            @NonNull Converter<String, QNameModule> prefixes, @NonNull String xpath) throws XPathExpressionException;

    /**
     * Create a new document context.
     *
     * @param documentRoot Root node of the document
     * @return A new {@link XPathDocument} on which queries may be executed.
     * @throws IllegalArgumentException if the document root is not known to this schema context.
     */
    @NonNull XPathDocument createDocument(@NonNull NormalizedNode<?, ?> documentRoot);
}
