/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.must;

import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class MustEffectiveStatementImpl extends DeclaredEffectiveStatementBase<RevisionAwareXPath, MustStatement>
        implements MustDefinition, MustEffectiveStatement {

    private final RevisionAwareXPath xpath;
    private final String description;
    private final String errorAppTag;
    private final String errorMessage;
    private final String reference;

    MustEffectiveStatementImpl(final StmtContext<RevisionAwareXPath, MustStatement, ?> ctx) {
        super(ctx);
        this.xpath = ctx.getStatementArgument();

        DescriptionEffectiveStatement descriptionStmt = firstEffective(DescriptionEffectiveStatement.class);
        this.description = descriptionStmt == null ? null : descriptionStmt.argument();

        ErrorAppTagEffectiveStatement errorAppTagStmt = firstEffective(ErrorAppTagEffectiveStatement.class);
        this.errorAppTag = errorAppTagStmt == null ? null : errorAppTagStmt.argument();

        ErrorMessageEffectiveStatement errorMessageStmt = firstEffective(ErrorMessageEffectiveStatement.class);
        this.errorMessage = errorMessageStmt == null ? null : errorMessageStmt.argument();

        ReferenceEffectiveStatement referenceStmt = firstEffective(ReferenceEffectiveStatement.class);
        this.reference = referenceStmt == null ? null : referenceStmt.argument();
    }

    @Override
    public RevisionAwareXPath getXpath() {
        return xpath;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public Optional<String> getErrorAppTag() {
        return Optional.ofNullable(errorAppTag);
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    @Override
    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
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
