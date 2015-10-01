/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.Leafref;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PathEffectiveStatementImpl;

public final class LeafrefSpecificationEffectiveStatementImpl extends EffectiveStatementBase<String, LeafrefSpecification>
        implements TypeEffectiveStatement<LeafrefSpecification> {

    public static final String LOCAL_NAME = "leafref";
    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, LOCAL_NAME);
    private static final String DESCRIPTION = "The leafref type is used to reference a particular leaf instance in the data tree.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.9";
    private static final String UNITS = "";

    private final SchemaPath path;
    private RevisionAwareXPath xpath;
    private final Leafref leafrefInstance = null;

    public LeafrefSpecificationEffectiveStatementImpl(final StmtContext<String, TypeStatement.LeafrefSpecification, EffectiveStatement<String, TypeStatement.LeafrefSpecification>> ctx) {
        super(ctx);
    }

    @Override
    public TypeDefinitionBuilder<LeafrefTypeDefinition> newTypeDefinitionBuilder() {
        return new AbstractTypeDefinitionBuilder<LeafrefTypeDefinition>() {
            private RevisionAwareXPath xpath = null;

            @Override
            protected void addEffectiveStatement(@Nonnull final EffectiveStatement<?, ?> stmt) {
                if (stmt instanceof PathEffectiveStatementImpl) {
                    xpath = ((PathEffectiveStatementImpl) stmt).argument();
                } else {
                    super.addEffectiveStatement(stmt);
                }
            }

            @Override
            public LeafrefTypeDefinition build() {
                // FIXME: this is not quite right
                return Leafref.create(getPath(), xpath);
            }
        }.addEffectiveStatements(effectiveSubstatements());
    }
}
