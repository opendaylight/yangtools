/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.model.api;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * {@link StatementDefinition}s for statements defined by <a href="https://tools.ietf.org/html/rfc8791">RFC8791</a>.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public enum YangDataStructureStatements implements StatementDefinition {
    STRUCTURE("structure", "name", StructureStatement.class, StructureEffectiveStatement.class),
    AUGMENT_STRUCTURE("augment-structure", "path", AugmentStructureStatement.class,
        AugmentStructureEffectiveStatement.class);

    private final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation;
    private final Class<? extends DeclaredStatement<?>> declaredRepresentation;
    private final QName statementName;
    private final ArgumentDefinition argumentDef;

    YangDataStructureStatements(final String statementName, final String argumentName,
            final Class<? extends DeclaredStatement<?>> declaredRepresentation,
                    final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation) {
        this.statementName = QName.create(YangDataStructureConstants.RFC8791_MODULE, statementName).intern();
        this.argumentDef = ArgumentDefinition.of(QName.create(statementName, argumentName).intern(), true);
        this.declaredRepresentation = requireNonNull(declaredRepresentation);
        this.effectiveRepresentation = requireNonNull(effectiveRepresentation);
    }

    @Override
    public Optional<ArgumentDefinition> getArgumentDefinition() {
        return Optional.of(argumentDef);
    }

    @Override
    public QName getStatementName() {
        return statementName;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveRepresentation;
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return declaredRepresentation;
    }
}
