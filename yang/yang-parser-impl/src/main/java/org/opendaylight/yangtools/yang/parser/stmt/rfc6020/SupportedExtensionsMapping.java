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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.OpenconfigVersionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc8040.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.stmt.rfc8040.YangDataStatement;

@Beta
public enum SupportedExtensionsMapping implements StatementDefinition {
    ANYXML_SCHEMA_LOCATION("urn:opendaylight:yang:extension:yang-ext", "2013-07-09",
        AnyxmlSchemaLocationStatementImpl.class, AnyxmlSchemaLocationEffectiveStatementImpl.class,
        "anyxml-schema-location", "target-node", false),
    OPENCONFIG_VERSION("http://openconfig.net/yang/openconfig-ext",
        OpenconfigVersionStatementImpl.class, OpenconfigVersionEffectiveStatementImpl.class,
        "openconfig-version", "semver", false),
    YANG_DATA("urn:ietf:params:xml:ns:yang:ietf-restconf", "2017-01-26", YangDataStatement.class,
            YangDataEffectiveStatement.class, "yang-data", "name", true);

    private final Class<? extends DeclaredStatement<?>> type;
    private final Class<? extends EffectiveStatement<?, ?>> effectiveType;
    private final QName name;
    private final QName argument;
    private final boolean yinElement;

    SupportedExtensionsMapping(final String namespace, final String revision,
            final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr, final String argumentStr,
            final boolean yinElement) {
        type = Preconditions.checkNotNull(declared);
        effectiveType = Preconditions.checkNotNull(effective);
        name = createQName(namespace, revision, nameStr);
        argument = createQName(namespace, revision, argumentStr);
        this.yinElement = yinElement;
    }

    private SupportedExtensionsMapping(final String namespace, final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr, final String argumentStr,
            final boolean yinElement) {
        type = Preconditions.checkNotNull(declared);
        effectiveType = Preconditions.checkNotNull(effective);
        name = createQName(namespace, nameStr);
        argument = createQName(namespace, argumentStr);
        this.yinElement = yinElement;
    }

    @Nonnull
    private static QName createQName(final String namespace, final String localName) {
        return QName.create(namespace, localName).intern();
    }

    @Nonnull
    private static QName createQName(final String namespace, final String revision, final String localName) {
        return QName.create(namespace, revision, localName).intern();
    }

    @Nonnull
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

    @Nonnull
    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveType;
    }

    @Override
    public boolean isArgumentYinElement() {
        return yinElement;
    }
}
