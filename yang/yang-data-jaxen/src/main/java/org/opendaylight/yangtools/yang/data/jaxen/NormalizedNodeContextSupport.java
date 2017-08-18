/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Converter;
import com.google.common.base.Preconditions;
import java.util.Optional;
import org.jaxen.ContextSupport;
import org.jaxen.SimpleVariableContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class NormalizedNodeContextSupport extends ContextSupport {
    private static final long serialVersionUID = 1L;
    private final NormalizedNodeContext root;

    private NormalizedNodeContextSupport(final ConverterNamespaceContext context,
            final NormalizedNodeNavigator navigator) {
        super(context, YangFunctionContext.getInstance(), new SimpleVariableContext(), navigator);
        this.root = new NormalizedNodeContext(this, navigator.getRootNode(), null);
    }

    static NormalizedNodeContextSupport create(final JaxenDocument document,
            final Converter<String, QNameModule> prefixes) {
        final ConverterNamespaceContext context = new ConverterNamespaceContext(prefixes);
        final NormalizedNodeNavigator navigator = new NormalizedNodeNavigator(context, document);

        return new NormalizedNodeContextSupport(context, navigator);
    }

    NormalizedNodeContext createContext(final YangInstanceIdentifier path) {
        NormalizedNodeContext result = root;
        for (PathArgument arg : path.getPathArguments()) {
            final Optional<NormalizedNode<?, ?>> node = NormalizedNodes.getDirectChild(result.getNode(), arg);
            Preconditions.checkArgument(node.isPresent(), "Node %s has no child %s", result.getNode(), arg);
            result = new NormalizedNodeContext(this, node.get(), result);
        }

        return result;
    }

    SchemaContext getSchemaContext() {
        return getNavigator().getSchemaContext();
    }

    @Override
    public NormalizedNodeNavigator getNavigator() {
        return (NormalizedNodeNavigator) super.getNavigator();
    }
}
