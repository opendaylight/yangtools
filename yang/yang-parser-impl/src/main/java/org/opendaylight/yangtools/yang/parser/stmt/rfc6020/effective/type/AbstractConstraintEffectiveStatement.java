/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ErrorAppTagEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ErrorMessageEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;

abstract class AbstractConstraintEffectiveStatement<A, D extends DeclaredStatement<A>> extends
        DeclaredEffectiveStatementBase<A, D> {
    private final String description;
    private final String reference;
    private final String errorAppTag;
    private final String errorMessage;
    private final A constraints;

    public AbstractConstraintEffectiveStatement(final StmtContext<A, D, ?> ctx, final ConstraintFactory<A> constraintFactory) {
        super(ctx);
        String descriptionInit = null;
        String referenceInit = null;
        String errorAppTagInit = null;
        String errorMessageInit = null;

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
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
        }

        this.description = descriptionInit;
        this.reference = referenceInit;
        this.errorAppTag = errorAppTagInit;
        this.errorMessage = errorMessageInit;
        this.constraints = constraintFactory.createConstraints(this, super.argument());
    }

    @Override
    public final A argument() {
        return constraints;
    }

    public final boolean isCustomizedStatement() {
        return this.description != null || this.reference != null || this.errorAppTag != null
                || this.errorMessage != null;
    }

    public final String getDescription() {
        return description;
    }

    public final String getReference() {
        return reference;
    }

    public final String getErrorAppTag() {
        return errorAppTag;
    }

    public final String getErrorMessage() {
        return errorMessage;
    }
}

abstract class ConstraintFactory<A> {
    abstract protected A createConstraints(AbstractConstraintEffectiveStatement<A, ?> stmt, A argument);
}

abstract class ListConstraintFactory<A> extends ConstraintFactory<List<A>> {
    @Override
    protected List<A> createConstraints(final AbstractConstraintEffectiveStatement<List<A>, ?> stmt, final List<A> argument) {
        if (!stmt.isCustomizedStatement()) {
            return ImmutableList.copyOf(argument);
        }

        final List<A> customizedConstraints = new ArrayList<>(argument.size());
        for (A constraint : argument) {
            customizedConstraints.add(createCustomizedConstraint(constraint, stmt));
        }
        return ImmutableList.copyOf(customizedConstraints);
    }

    abstract protected A createCustomizedConstraint(A constraint, AbstractConstraintEffectiveStatement<List<A>, ?> stmt);
}

final class LengthConstraintFactory extends ListConstraintFactory<LengthConstraint> {
    @Override
    protected LengthConstraint createCustomizedConstraint(final LengthConstraint lengthConstraint,
            final AbstractConstraintEffectiveStatement<List<LengthConstraint>, ?> stmt) {
        return new LengthConstraintEffectiveImpl(lengthConstraint.getMin(), lengthConstraint.getMax(),
                stmt.getDescription(), stmt.getReference(), stmt.getErrorAppTag(), stmt.getErrorMessage());
    }
}

final class RangeConstraintFactory extends ListConstraintFactory<RangeConstraint> {
    @Override
    protected RangeConstraint createCustomizedConstraint(final RangeConstraint rangeConstraint,
            final AbstractConstraintEffectiveStatement<List<RangeConstraint>, ?> stmt) {
        return new RangeConstraintEffectiveImpl(rangeConstraint.getMin(), rangeConstraint.getMax(),
                stmt.getDescription(), stmt.getReference(), stmt.getErrorAppTag(), stmt.getErrorMessage());
    }
}

final class PatternConstraintFactory extends ConstraintFactory<PatternConstraint> {
    @Override
    protected PatternConstraint createConstraints(final AbstractConstraintEffectiveStatement<PatternConstraint, ?> stmt, final PatternConstraint argument) {
        if (!stmt.isCustomizedStatement()) {
            return argument;
        }

        return createCustomizedConstraint(argument, stmt);
    }

    private static PatternConstraint createCustomizedConstraint(final PatternConstraint patternConstraint,
            final AbstractConstraintEffectiveStatement<?, ?> stmt) {
        return new PatternConstraintEffectiveImpl(patternConstraint.getRegularExpression(), stmt.getDescription(),
                stmt.getReference(), stmt.getErrorAppTag(), stmt.getErrorMessage());
    }
}