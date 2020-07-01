/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
@Deprecated(forRemoval = true)
//FIXME: 6.0.0: remove this class
public abstract class AbstractConstraintEffectiveStatement<A, D extends DeclaredStatement<A>> extends
        AbstractEffectiveDocumentedNode<A, D> implements ConstraintMetaDefinition {
    private final String errorAppTag;
    private final String errorMessage;
    private final ModifierKind modifier;
    private final A constraints;

    protected AbstractConstraintEffectiveStatement(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        String errorAppTagInit = null;
        String errorMessageInit = null;
        ModifierKind modifierInit = null;

        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof ErrorAppTagEffectiveStatement) {
                errorAppTagInit = ((ErrorAppTagEffectiveStatement) stmt).argument();
            }
            if (stmt instanceof ErrorMessageEffectiveStatement) {
                errorMessageInit = ((ErrorMessageEffectiveStatement) stmt).argument();
            }
            if (stmt instanceof ModifierEffectiveStatement) {
                modifierInit = ((ModifierEffectiveStatement) stmt).argument();
            }
        }

        this.errorAppTag = errorAppTagInit;
        this.errorMessage = errorMessageInit;
        this.modifier = modifierInit;
        this.constraints = createConstraints(super.argument());
    }

    @Override
    public final A argument() {
        return constraints;
    }

    public final boolean isCustomizedStatement() {
        return getDescription().isPresent() || getReference().isPresent() || this.errorAppTag != null
                || this.errorMessage != null || this.modifier != null;
    }

    public final ModifierKind getModifier() {
        return modifier;
    }

    @Override
    public final Optional<String> getErrorAppTag() {
        return Optional.ofNullable(errorAppTag);
    }

    @Override
    public final Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    protected abstract A createConstraints(A argument);
}

