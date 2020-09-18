/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

@Beta
public enum IetfYangSmiv2ExtensionsMapping implements StatementDefinition {
    DISPLAY_HINT(IetfYangSmiv2Constants.NAMESPACE, IetfYangSmiv2Constants.REVISION,
            DisplayHintStatement.class, DisplayHintEffectiveStatement.class, "display-hint", "format", false),
    MAX_ACCESS(IetfYangSmiv2Constants.NAMESPACE, IetfYangSmiv2Constants.REVISION,
            MaxAccessStatement.class, MaxAccessEffectiveStatement.class, "max-access", "access", false),
    DEFVAL(IetfYangSmiv2Constants.NAMESPACE, IetfYangSmiv2Constants.REVISION,
            DefValStatement.class, DefValEffectiveStatement.class, "defval", "value", false),
    IMPLIED(IetfYangSmiv2Constants.NAMESPACE, IetfYangSmiv2Constants.REVISION,
            ImpliedStatement.class, ImpliedEffectiveStatement.class, "implied", "index", false),
    SUB_ID(IetfYangSmiv2Constants.NAMESPACE, IetfYangSmiv2Constants.REVISION,
            SubIdStatement.class, SubIdEffectiveStatement.class, "subid", "value", false),
    OBJECT_ID(IetfYangSmiv2Constants.NAMESPACE, IetfYangSmiv2Constants.REVISION,
            OidStatement.class, OidEffectiveStatement.class, "oid", "value", false),
    ALIAS(IetfYangSmiv2Constants.NAMESPACE, IetfYangSmiv2Constants.REVISION,
            AliasStatement.class, AliasEffectiveStatement.class, "alias", "descriptor", false);

    private final Class<? extends DeclaredStatement<?>> type;
    private final Class<? extends EffectiveStatement<?, ?>> effectiveType;
    private final QName name;
    private final ArgumentDefinition argument;

    IetfYangSmiv2ExtensionsMapping(final String namespace, final String revision,
                                   final Class<? extends DeclaredStatement<?>> declared,
                                   final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr,
                                   final String argumentStr,
                                   final boolean yinElement) {
        type = requireNonNull(declared);
        effectiveType = requireNonNull(effective);
        name = createQName(namespace, revision, nameStr);
        argument = ArgumentDefinition.of(createQName(namespace, revision, argumentStr), yinElement);
    }

    @NonNull
    private static QName createQName(final String namespace, final String revision, final String localName) {
        return QName.create(namespace, revision, localName).intern();
    }

    @Override
    public QName getStatementName() {
        return name;
    }

    @Override
    public Optional<ArgumentDefinition> getArgumentDefinition() {
        return Optional.of(argument);
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return type;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveType;
    }

    private static class IetfYangSmiv2Constants {
        public static final String NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-yang-smiv2";
        public static final String REVISION = "2012-06-22";
    }
}
