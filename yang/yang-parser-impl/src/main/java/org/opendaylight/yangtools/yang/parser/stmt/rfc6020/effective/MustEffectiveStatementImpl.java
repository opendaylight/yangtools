/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class MustEffectiveStatementImpl extends
        EffectiveStatementBase<RevisionAwareXPath, MustStatement> implements
        MustDefinition {

    private RevisionAwareXPath xPath;
    private String description;
    private String errorAppTag;
    private String errorMessage;
    private String reference;

    public MustEffectiveStatementImpl(
            StmtContext<RevisionAwareXPath, MustStatement, ?> ctx) {
        super(ctx);

        initFields();

        xPath = ctx.getStatementArgument();
    }

    private void initFields() {

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {

            if (effectiveStatement instanceof DescriptionEffectiveStatementImpl) {
                description = ((DescriptionEffectiveStatementImpl) effectiveStatement)
                        .argument();
            }
            if (effectiveStatement instanceof ErrorAppTagEffectiveStatementImpl) {
                errorAppTag = ((ErrorAppTagEffectiveStatementImpl) effectiveStatement)
                        .argument();
            }
            if (effectiveStatement instanceof ErrorMessageEffectiveStatementImpl) {
                errorMessage = ((ErrorMessageEffectiveStatementImpl) effectiveStatement)
                        .argument();
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                reference = ((ReferenceEffectiveStatementImpl) effectiveStatement)
                        .argument();
            }
        }
    }

    @Override
    public RevisionAwareXPath getXpath() {
        return xPath;
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
        result = prime * result + ((xPath == null) ? 0 : xPath.hashCode());
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result
                + ((reference == null) ? 0 : reference.hashCode());
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
        if (xPath == null) {
            if (other.xPath != null) {
                return false;
            }
        } else if (!xPath.equals(other.xPath)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        } else if (!reference.equals(other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return xPath.toString();
    }
}
