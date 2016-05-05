/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ErrorAppTagEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ErrorMessageEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;

public class LengthEffectiveStatementImpl extends
        DeclaredEffectiveStatementBase<List<LengthConstraint>, LengthStatement> {
    private final String description;
    private final String reference;
    private final String errorAppTag;
    private final String errorMessage;
    private final List<LengthConstraint> lengthConstraints;

    public LengthEffectiveStatementImpl(final StmtContext<List<LengthConstraint>, LengthStatement, ?> ctx) {
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

        if(isCustomLengthStatement()) {

        } else {
            lengthConstraints = argument();
        }
    }

    public List<LengthConstraint> getLengthConstraints() {
        return lengthConstraints;
    }

    public boolean isCustomLengthStatement() {
        return this.description != null || this.reference != null || this.errorAppTag != null
                || this.errorMessage != null;
    }
}