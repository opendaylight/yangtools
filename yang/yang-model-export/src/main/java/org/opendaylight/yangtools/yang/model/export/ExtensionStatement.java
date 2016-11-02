/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

final class ExtensionStatement implements StatementDefinition {

    private QName argumentName;
    private QName statementName;
    private boolean yinElement;

    private ExtensionStatement(final ExtensionDefinition def) {
        statementName = def.getQName();
        argumentName = def.getArgument() != null ? QName.create(statementName, def.getArgument()) : null;
        yinElement = def.isYinElement();
    }

    static StatementDefinition from(final ExtensionDefinition def) {
        return new ExtensionStatement(def);
    }

    static Map<QName,StatementDefinition> mapFrom(final Collection<ExtensionDefinition> definitions) {
        final HashMap<QName,StatementDefinition> ret = new HashMap<>(definitions.size());
        for (final ExtensionDefinition def : definitions) {
            final StatementDefinition value = from(def);
            ret.put(value.getStatementName(), value);
        }
        return ret;
    }

    @Override
    public QName getArgumentName() {
        return argumentName;
    }

    @Nonnull
    @Override
    public QName getStatementName() {
        return statementName;
    }

    @Override
    public boolean isArgumentYinElement() {
        return yinElement;
    }

    @Nonnull
    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        throw new UnsupportedOperationException("Not defined");
    }

    @Nonnull
    @Override
    public Class<? extends EffectiveStatement<?,?>> getEffectiveRepresentationClass() {
        throw new UnsupportedOperationException("Not defined");
    }

}
