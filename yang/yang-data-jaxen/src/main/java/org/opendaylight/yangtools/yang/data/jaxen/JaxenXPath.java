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
import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.NonNull;
import org.jaxen.JaxenException;
import org.jaxen.JaxenHandler;
import org.jaxen.XPathSyntaxException;
import org.jaxen.expr.Expr;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathBooleanResult;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathExpression;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathNodesetResult;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathNumberResult;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathResult;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathStringResult;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JaxenXPath implements XPathExpression {
    private static final Logger LOG = LoggerFactory.getLogger(JaxenXPath.class);

    private final @NonNull Converter<String, QNameModule> converter;
    private final @NonNull SchemaPath schemaPath;
    private final @NonNull Expr expr;

    private JaxenXPath(final @NonNull Converter<String, QNameModule> converter, final @NonNull SchemaPath schemaPath,
            final @NonNull Expr expr) {
        this.converter = requireNonNull(converter);
        this.schemaPath = requireNonNull(schemaPath);
        this.expr = requireNonNull(expr);
    }

    static @NonNull JaxenXPath create(final @NonNull Converter<String, QNameModule> converter,
            final @NonNull SchemaPath schemaPath, final @NonNull String xpath) throws JaxenException {

        final @NonNull Expr parsed;
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
    public Optional<? extends XPathResult<?>> evaluate(final XPathDocument document,
            final YangInstanceIdentifier path) throws XPathExpressionException {
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
