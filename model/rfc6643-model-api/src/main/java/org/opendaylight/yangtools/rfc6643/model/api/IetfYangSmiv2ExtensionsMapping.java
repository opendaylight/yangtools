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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

@Beta
public enum IetfYangSmiv2ExtensionsMapping implements StatementDefinition {
    DISPLAY_HINT(DisplayHintStatement.class, DisplayHintEffectiveStatement.class, "display-hint", "format"),
    MAX_ACCESS(MaxAccessStatement.class, MaxAccessEffectiveStatement.class, "max-access", "access"),
    DEFVAL(DefValStatement.class, DefValEffectiveStatement.class, "defval", "value"),
    IMPLIED(ImpliedStatement.class, ImpliedEffectiveStatement.class, "implied", "index"),
    SUB_ID(SubIdStatement.class, SubIdEffectiveStatement.class, "subid", "value"),
    OBJECT_ID(OidStatement.class, OidEffectiveStatement.class, "oid", "value"),
    ALIAS(AliasStatement.class, AliasEffectiveStatement.class, "alias", "descriptor");

    private final @NonNull Class<? extends DeclaredStatement<?>> type;
    private final @NonNull Class<? extends EffectiveStatement<?, ?>> effectiveType;
    private final @NonNull QName name;
    private final @NonNull ArgumentDefinition argument;

    IetfYangSmiv2ExtensionsMapping(final Class<? extends DeclaredStatement<?>> declared,
                                   final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr,
                                   final String argumentStr) {
        type = requireNonNull(declared);
        effectiveType = requireNonNull(effective);
        name = createQName(nameStr);
        argument = ArgumentDefinition.of(createQName(argumentStr), false);
    }

    private static @NonNull QName createQName(final String localName) {
        return QName.create(IetfYangSmiv2Constants.RFC6643_MODULE, localName).intern();
    }

    @Override
    public QName statementName() {
        return name;
    }

    @Override
    public ArgumentDefinition argumentDefinition() {
        return argument;
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return type;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveType;
    }
}
