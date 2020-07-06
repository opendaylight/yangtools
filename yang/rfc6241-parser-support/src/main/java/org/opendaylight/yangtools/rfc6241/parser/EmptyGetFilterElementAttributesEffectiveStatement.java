/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesEffectiveStatement;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesSchemaNode;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.UnknownSchemaNodeMixin;

class EmptyGetFilterElementAttributesEffectiveStatement
        extends DefaultArgument<Void, GetFilterElementAttributesStatement>
        implements GetFilterElementAttributesEffectiveStatement, GetFilterElementAttributesSchemaNode,
                UnknownSchemaNodeMixin<Void, GetFilterElementAttributesStatement> {

    EmptyGetFilterElementAttributesEffectiveStatement(final GetFilterElementAttributesStatement declared) {
        super(declared);
    }

    @Override
    public QName getNodeType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExtensionDefinition getExtensionDefinition() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull QName getQName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull SchemaPath getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int flags() {
        // TODO Auto-generated method stub
        return 0;
    }

}
