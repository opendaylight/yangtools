/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Converter;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.xml.xpath.XPathExpressionException;
import org.jaxen.JaxenException;
import org.jaxen.JaxenHandler;
import org.jaxen.XPathSyntaxException;
import org.jaxen.expr.Expr;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
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
    private final Expr expr;

    private JaxenXPath(final Converter<String, QNameModule> converter, final SchemaPath schemaPath,
            final Expr expr) {
        this.converter = requireNonNull(converter);
        this.schemaPath = requireNonNull(schemaPath);
        this.expr = requireNonNull(expr);
    }

    static JaxenXPath create(final Converter<String, QNameModule> converter, final SchemaPath schemaPath,
            final String xpath) throws JaxenException {

        final Expr parsed;
        try {
            final XPathReader reader = new org.jaxen.saxpath.base.XPathReader();
            final JaxenHandler handler = new JaxenHandler();
            reader.setXPathHandler(handler);
            reader.parse(xpath);
            parsed = handler.getXPathExpr().getRootExpr();
        } catch (org.jaxen.saxpath.XPathSyntaxException e) {
            throw new XPathSyntaxException(e);
        } catch (SAXPathException e) {
            throw new JaxenException(e);
        }

        LOG.debug("Compiled {} to expression {}", xpath, parsed);

        new ExprWalker(new ExprListener() {
            // FIXME: perform expression introspection to understand things like apex, etc.
        }).walk(parsed);

        return new JaxenXPath(converter, schemaPath, parsed);
    }

    @Override
    public Optional<? extends XPathResult<?>> evaluate(@Nonnull final XPathDocument document,
            @Nonnull final YangInstanceIdentifier path) throws XPathExpressionException {
        checkArgument(document instanceof JaxenDocument);

        final NormalizedNodeContextSupport contextSupport = NormalizedNodeContextSupport.create(
            (JaxenDocument)document, converter);

        final Object result = evaluate(contextSupport.createContext(path));
        if (result instanceof String) {
            return Optional.of((XPathStringResult) () -> (String) result);
        } else if (result instanceof Number) {
            return Optional.of((XPathNumberResult) () -> (Number) result);
        } else if (result instanceof Boolean) {
            return Optional.of((XPathBooleanResult) () -> (Boolean) result);
        } else if (result == null) {
            return Optional.empty();
        }

        Verify.verify(result instanceof List, "Unhandled result %s", result);
        @SuppressWarnings("unchecked")
        final List<NormalizedNodeContext> resultList = (List<NormalizedNodeContext>) result;
        return Optional.of((XPathNodesetResult) () -> {
            // XXX: Will this really work, or do we need to perform deep transformation?
            return Lists.transform(resultList,
                context -> new SimpleImmutableEntry<>(context.getPath(), context.getNode()));
        });
    }

    private Object evaluate(final NormalizedNodeContext context) throws XPathExpressionException {
        final Object result;
        try {
            result = expr.evaluate(context);
        } catch (JaxenException e) {
            throw new XPathExpressionException(e);
        }

        if (result instanceof List) {
            final List<?> list = (List<?>) result;
            if (list.size() == 1) {
                final Object first = list.get(0);
                if (first instanceof String || first instanceof Number || first instanceof Boolean) {
                    return first;
                }
            }
        }

        return result;
    }

    @Nonnull
    @Override
    public SchemaPath getEvaluationPath() {
        return schemaPath;
    }

    @Nonnull
    @Override
    public SchemaPath getApexPath() {
        // TODO: improve this
        return SchemaPath.ROOT;
    }
}
