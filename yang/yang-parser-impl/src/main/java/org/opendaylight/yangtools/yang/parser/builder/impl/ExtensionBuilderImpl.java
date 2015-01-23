/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.ExtensionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractSchemaNodeBuilder;

public final class ExtensionBuilderImpl extends AbstractSchemaNodeBuilder implements ExtensionBuilder {
    private ExtensionDefinitionImpl instance;
    private String argument;
    private boolean yin;

    ExtensionBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
    }

    @Override
    public ExtensionDefinition build() {
        if (instance != null) {
            return instance;
        }

        instance = new ExtensionDefinitionImpl(qname, schemaPath);
        instance.argument = argument;
        instance.yin = yin;

        instance.description = description;
        instance.reference = reference;
        instance.status = status;

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    @Override
    public void setYinElement(final boolean yin) {
        this.yin = yin;
    }

    @Override
    public void setArgument(final String argument) {
        this.argument = argument;
    }

    @Override
    public String toString() {
        return "extension " + qname.getLocalName();
    }

}
