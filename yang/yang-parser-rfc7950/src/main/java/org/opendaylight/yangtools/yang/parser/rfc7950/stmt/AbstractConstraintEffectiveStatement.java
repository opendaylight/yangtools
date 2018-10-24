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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class AbstractConstraintEffectiveStatement<A, D extends DeclaredStatement.WithArgument<A>> extends
        DeclaredEffectiveStatementBase.WithArgument<A, D> implements ConstraintMetaDefinition {
    private final String description;
    private final String reference;
    private final String errorAppTag;
    private final String errorMessage;
    private final ModifierKind modifier;
    private final @NonNull A constraints;

    protected AbstractConstraintEffectiveStatement(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        String descriptionInit = null;
        String referenceInit = null;
        String errorAppTagInit = null;
        String errorMessageInit = null;
        ModifierKind modifierInit = null;

        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DescriptionEffectiveStatement) {
                descriptionInit = ((DescriptionEffectiveStatement) stmt).argument();
            }
            if (stmt instanceof ReferenceEffectiveStatement) {
                referenceInit = ((ReferenceEffectiveStatement) stmt).argument();
            }
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
    public final Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public final ModifierKind getModifier() {
        return modifier;
    }

    @Override
    public final Optional<String> getReference() {
        return Optional.ofNullable(reference);
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

