/*
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
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedNodeWithoutStatus;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class MustEffectiveStatementImpl
        extends AbstractEffectiveDocumentedNodeWithoutStatus<RevisionAwareXPath, MustStatement>
        implements MustDefinition, MustEffectiveStatement {

    private final RevisionAwareXPath xpath;
    private final String errorAppTag;
    private final String errorMessage;

    MustEffectiveStatementImpl(final StmtContext<RevisionAwareXPath, MustStatement, ?> ctx) {
        super(ctx);
        xpath = ctx.getStatementArgument();
        errorAppTag = findFirstEffectiveSubstatementArgument(ErrorAppTagEffectiveStatement.class).orElse(null);
        errorMessage = findFirstEffectiveSubstatementArgument(ErrorMessageEffectiveStatement.class).orElse(null);
    }

    @Override
    public RevisionAwareXPath getXpath() {
        return xpath;
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
    public int hashCode() {
        return Objects.hash(xpath, getDescription().orElse(null), getReference().orElse(null));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MustEffectiveStatementImpl)) {
            return false;
        }
        final MustEffectiveStatementImpl other = (MustEffectiveStatementImpl) obj;
        return Objects.equals(xpath, other.xpath)
                && getDescription().equals(other.getDescription())
                && getReference().equals(other.getReference());
    }

    @Override
    public String toString() {
        return xpath.getOriginalString();
    }
}
