/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective;

import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ImportEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;

public class ImportEffectiveStatementRfc7950Impl extends ImportEffectiveStatementImpl {

    private final String description;
    private final String reference;

    public ImportEffectiveStatementRfc7950Impl(StmtContext<String, ImportStatement, ?> ctx) {
        super(ctx);

        DescriptionEffectiveStatementImpl descriptionStmt = firstEffective(DescriptionEffectiveStatementImpl.class);
        this.description = (descriptionStmt != null) ? descriptionStmt.argument() : null;

        ReferenceEffectiveStatementImpl referenceStmt = firstEffective(ReferenceEffectiveStatementImpl.class);
        this.reference = (referenceStmt != null) ? referenceStmt.argument() : null;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(getDescription());
        result = prime * result + Objects.hash(getReference());
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
        ImportEffectiveStatementRfc7950Impl other = (ImportEffectiveStatementRfc7950Impl) obj;
        if (!Objects.equals(getModuleName(), other.getModuleName())) {
            return false;
        }
        if (!Objects.equals(getRevision(), other.getRevision())) {
            return false;
        }
        if (!Objects.equals(getPrefix(), other.getPrefix())) {
            return false;
        }
        if (!Objects.equals(getSemanticVersion(), other.getSemanticVersion())) {
            return false;
        }
        if (!Objects.equals(getDescription(), other.getDescription())) {
            return false;
        }
        if (!Objects.equals(getReference(), other.getReference())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ImportEffectiveStatementRfc7950Impl.class.getSimpleName() + "[moduleName=" + getModuleName() + ", revision="
                + getRevision() + ", semantic version=" + getSemanticVersion() + ", prefix=" + getPrefix()
                + ", description=" + getDescription() + ", reference=" + getReference() + "]";
    }
}
