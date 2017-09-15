/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class MustEffectiveStatementImpl extends DeclaredEffectiveStatementBase<RevisionAwareXPath, MustStatement>
        implements MustDefinition {

    private final RevisionAwareXPath xpath;
    private final String description;
    private final String errorAppTag;
    private final String errorMessage;
    private final String reference;

    public MustEffectiveStatementImpl(final StmtContext<RevisionAwareXPath, MustStatement, ?> ctx) {
        super(ctx);
        this.xpath = ctx.getStatementArgument();

        DescriptionEffectiveStatementImpl descriptionStmt = firstEffective(DescriptionEffectiveStatementImpl.class);
        this.description = descriptionStmt == null ? null : descriptionStmt.argument();

        ErrorAppTagEffectiveStatementImpl errorAppTagStmt = firstEffective(ErrorAppTagEffectiveStatementImpl.class);
        this.errorAppTag = errorAppTagStmt == null ? null : errorAppTagStmt.argument();

        ErrorMessageEffectiveStatementImpl errorMessageStmt = firstEffective(ErrorMessageEffectiveStatementImpl.class);
        this.errorMessage = errorMessageStmt == null ? null : errorMessageStmt.argument();

        ReferenceEffectiveStatementImpl referenceStmt = firstEffective(ReferenceEffectiveStatementImpl.class);
        this.reference = referenceStmt == null ? null : referenceStmt.argument();
    }

    @Override
    public RevisionAwareXPath getXpath() {
        return xpath;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getErrorAppTag() {
        return errorAppTag;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(xpath);
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(reference);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MustEffectiveStatementImpl other = (MustEffectiveStatementImpl) obj;
        if (!Objects.equals(xpath, other.xpath)) {
            return false;
        }
        if (!Objects.equals(description, other.description)) {
            return false;
        }
        if (!Objects.equals(reference, other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return xpath.toString();
    }
}
