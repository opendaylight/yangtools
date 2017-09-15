/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ErrorAppTagEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ErrorMessageEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective.ModifierEffectiveStatementImpl;

abstract class AbstractConstraintEffectiveStatement<A, D extends DeclaredStatement<A>> extends
        DeclaredEffectiveStatementBase<A, D> implements ConstraintMetaDefinition {
    private final String description;
    private final String reference;
    private final String errorAppTag;
    private final String errorMessage;
    private final ModifierKind modifier;
    private final A constraints;

    AbstractConstraintEffectiveStatement(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        String descriptionInit = null;
        String referenceInit = null;
        String errorAppTagInit = null;
        String errorMessageInit = null;
        ModifierKind modifierInit = null;

        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DescriptionEffectiveStatementImpl) {
                descriptionInit = ((DescriptionEffectiveStatementImpl) stmt).argument();
            }
            if (stmt instanceof ReferenceEffectiveStatementImpl) {
                referenceInit = ((ReferenceEffectiveStatementImpl) stmt).argument();
            }
            if (stmt instanceof ErrorAppTagEffectiveStatementImpl) {
                errorAppTagInit = ((ErrorAppTagEffectiveStatementImpl) stmt).argument();
            }
            if (stmt instanceof ErrorMessageEffectiveStatementImpl) {
                errorMessageInit = ((ErrorMessageEffectiveStatementImpl) stmt).argument();
            }
            if (stmt instanceof ModifierEffectiveStatementImpl) {
                modifierInit = ((ModifierEffectiveStatementImpl) stmt).argument();
            }
        }

        this.description = descriptionInit;
        this.reference = referenceInit;
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
        return this.description != null || this.reference != null || this.errorAppTag != null
                || this.errorMessage != null || this.modifier != null;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    public final ModifierKind getModifier() {
        return modifier;
    }

    @Override
    public final String getReference() {
        return reference;
    }

    @Override
    public final String getErrorAppTag() {
        return errorAppTag;
    }

    @Override
    public final String getErrorMessage() {
        return errorMessage;
    }

    abstract A createConstraints(A argument);
}

