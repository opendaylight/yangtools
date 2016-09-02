/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Converter;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.jaxen.BaseXPath;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.expr.Expr;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathBooleanResult;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathExpression;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathNodesetResult;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathNumberResult;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathResult;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathStringResult;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JaxenXPath implements XPathExpression {
    private static final Logger LOG = LoggerFactory.getLogger(JaxenXPath.class);

    private final Converter<String, QNameModule> converter;
    private final SchemaPath schemaPath;
    private final BaseXPath xpath;

    private JaxenXPath(final Converter<String, QNameModule> converter, final SchemaPath schemaPath,
            final BaseXPath xpath) {
        this.converter = Preconditions.checkNotNull(converter);
        this.schemaPath = Preconditions.checkNotNull(schemaPath);
        this.xpath = Preconditions.checkNotNull(xpath);
    }

    static JaxenXPath create(final Converter<String, QNameModule> converter, final SchemaPath schemaPath,
            final String xpath) throws JaxenException {
        final BaseXPath compiled = new BaseXPath(xpath) {
            private static final long serialVersionUID = 1L;

            @Override
            protected ContextSupport getContextSupport() {
                throw new UnsupportedOperationException(xpath);
            }
        };

        final Expr expr = compiled.getRootExpr();
        LOG.debug("Compiled {} to expression {}", xpath, expr);

        // FIXME: perform expression introspection to understand things like apex, etc.

        return new JaxenXPath(converter, schemaPath, compiled);
    }

    @Override
    public Optional<? extends XPathResult<?>> evaluate(final XPathDocument document, final YangInstanceIdentifier path)
            throws XPathExpressionException {
        Preconditions.checkArgument(document instanceof JaxenDocument);

        final NormalizedNodeContextSupport contextSupport = NormalizedNodeContextSupport.create(
            (JaxenDocument)document, converter);

        final Object result;
        try {
            result = xpath.evaluate(contextSupport.createContext(path));
        } catch (JaxenException e) {
            throw new XPathExpressionException(e);
        }

        if (result instanceof String) {
            return Optional.of(new XPathStringResult() {
                @Override
                public String getValue() {
                    return (String)result;
                }
            });
        } else if (result instanceof Number) {
            return Optional.of(new XPathNumberResult() {
                @Override
                public Number getValue() {
                    return (Number) result;
                }
            });
        } else if (result instanceof Boolean) {
            return Optional.of(new XPathBooleanResult() {
                @Override
                public Boolean getValue() {
                    return (Boolean) result;
                }
            });
        } else if (result != null){
            return Optional.of(new XPathNodesetResult() {
                @SuppressWarnings("unchecked")
                @Override
                public Collection<NormalizedNode<?, ?>> getValue() {
                    // XXX: Will this really work, or do we need to perform deep transformation?
                    return Lists.transform((List<NormalizedNodeContext>) result, NormalizedNodeContext::getNode);
                }
            });
        } else {
            return Optional.absent();
        }
    }

    @Override
    public SchemaPath getEvaluationPath() {
        return schemaPath;
    }

    @Override
    public SchemaPath getApexPath() {
        // TODO: improve this
        return SchemaPath.ROOT;
    }
}
