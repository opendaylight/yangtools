/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.anyxmlschema.AnyxmlSchemaLocationEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.stmt.anyxmlschema.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.parser.stmt.openconfig.OpenconfigVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.stmt.openconfig.OpenconfigVersionStatement;

@Beta
public enum SupportedExtensionsMapping implements StatementDefinition {
    ANYXML_SCHEMA_LOCATION("urn:opendaylight:yang:extension:yang-ext", "2013-07-09",
        AnyxmlSchemaLocationStatement.class, AnyxmlSchemaLocationEffectiveStatement.class,
        "anyxml-schema-location", "target-node", false),
    OPENCONFIG_VERSION("http://openconfig.net/yang/openconfig-ext",
        OpenconfigVersionStatement.class, OpenconfigVersionEffectiveStatement.class,
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
        type = requireNonNull(declared);
        effectiveType = requireNonNull(effective);
        name = createQName(namespace, revision, nameStr);
        argument = createQName(namespace, revision, argumentStr);
        this.yinElement = yinElement;
    }

    SupportedExtensionsMapping(final String namespace, final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr, final String argumentStr,
            final boolean yinElement) {
        type = requireNonNull(declared);
        effectiveType = requireNonNull(effective);
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
