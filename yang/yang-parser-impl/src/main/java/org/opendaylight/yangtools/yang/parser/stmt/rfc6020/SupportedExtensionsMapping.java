/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AnyxmlSchemaLocationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.SemanticVersionEffectiveStatementImpl;

@Beta
public enum SupportedExtensionsMapping implements StatementDefinition {
    ANYXML_SCHEMA_LOCATION("urn:opendaylight:yang:extension:yang-ext", "2013-07-09",
        AnyxmlSchemaLocationStatementImpl.class, AnyxmlSchemaLocationEffectiveStatementImpl.class,
        "anyxml-schema-location", "target-node"),
    SEMANTIC_VERSION("urn:opendaylight:yang:extension:semantic-version", "2016-02-02",
            SemanticVersionStatementImpl.class, SemanticVersionEffectiveStatementImpl.class,
            "module-version", "semantic-version");

    private final Class<? extends DeclaredStatement<?>> type;
    private final Class<? extends EffectiveStatement<?, ?>> effectiveType;
    private final QName name;
    private final QName argument;

    SupportedExtensionsMapping(final String namespace, final String revision,
            final Class<? extends DeclaredStatement<?>> declared,
                    final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr,
                            final String argumentStr) {
        type = Preconditions.checkNotNull(declared);
        effectiveType = Preconditions.checkNotNull(effective);
        name = createQName(namespace, revision, nameStr);
        argument = createQName(namespace, revision, argumentStr);
    }

    private SupportedExtensionsMapping(final String namespace,
            final Class<? extends DeclaredStatement<?>> declared,
                    final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr,
                            final String argumentStr) {
        type = Preconditions.checkNotNull(declared);
        effectiveType = Preconditions.checkNotNull(effective);
        name = createQName(namespace, nameStr);
        argument = createQName(namespace, argumentStr);
    }

    @Nonnull
    private static QName createQName(final String namespace, final String localName) {
        return QName.create(namespace, localName).intern();
    }

    @Nonnull
    private static QName createQName(final String namespace, final String revision, final String localName) {
        return QName.create(namespace, revision, localName).intern();
    }

    @Override
    public QName getStatementName() {
        return name;
    }

    @Override
    @Nullable
    public QName getArgumentName() {
        return argument;
    }

    @Override
    @Nonnull
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return type;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveType;
    }
}
