/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatements;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class YangDataStatementSupport
        extends AbstractStringStatementSupport<YangDataStatement, YangDataEffectiveStatement> {
    // As per RFC8040 page 81:
    //
    //    The substatements of this extension MUST follow the
    //    'data-def-stmt' rule in the YANG ABNF.
    //
    // As per RFC7950 page 185:
    //
    //    data-def-stmt = container-stmt /
    //                    leaf-stmt /
    //                    leaf-list-stmt /
    //                    list-stmt /
    //                    choice-stmt /
    //                    anydata-stmt /
    //                    anyxml-stmt /
    //                    uses-stmt
    //
    // The cardinality is not exactly constrained, but the entirety of substatements are required to resolve to a single
    // XML document (page 80). This is enforced when we arrive at full declaration.
    private static final SubstatementValidator VALIDATOR = SubstatementValidator.builder(YangDataStatements.YANG_DATA)
        .addAny(YangStmtMapping.CONTAINER)
        .addAny(YangStmtMapping.LEAF)
        .addAny(YangStmtMapping.LEAF_LIST)
        .addAny(YangStmtMapping.LIST)
        .addAny(YangStmtMapping.CHOICE)
        .addAny(YangStmtMapping.ANYDATA)
        .addAny(YangStmtMapping.ANYXML)
        .addAny(YangStmtMapping.USES)
        .build();

    public YangDataStatementSupport(final YangParserConfiguration config) {
        super(YangDataStatements.YANG_DATA, StatementPolicy.reject(), config, VALIDATOR);
    }

    @Override
    public void onStatementAdded(final Mutable<String, YangDataStatement, YangDataEffectiveStatement> ctx) {
        // as per https://tools.ietf.org/html/rfc8040#section-8,
        // yang-data is ignored unless it appears as a top-level statement
        if (ctx.coerceParentContext().getParentContext() != null) {
            ctx.setUnsupported();
        }
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<String, YangDataStatement, YangDataEffectiveStatement> ctx) {
        // If we are declared in an illegal place, this becomes a no-op
        if (!ctx.isSupportedToBuildEffective()) {
            return;
        }

        // Run SubstatementValidator-based validation first
        super.onFullDefinitionDeclared(ctx);

        // Support for 'operations' container semantics. For this we need to recognize when the model at hand matches
        // RFC8040 ietf-restconf module. In ordered to do that we hook onto this particular definition:
        //
        //   rc:yang-data yang-api {
        //     uses restconf;
        //   }
        //
        // If we find it, we hook an inference action which performs the next step when the module is fully declared.
        if ("yang-api".equals(ctx.argument())) {
            final var stmts = ctx.declaredSubstatements();
            if (stmts.size() == 1) {
                final var stmt = stmts.iterator().next();
                if (stmt.producesEffective(UsesEffectiveStatement.class) && "restconf".equals(stmt.rawArgument())) {
                    // The rc:yang-data shape matches, but we are not sure about the module identity, that needs to be
                    // done later multiple stages, the first one being initiated through this call.
                    OperationsValidateModuleAction.applyTo(ctx.coerceParentContext());
                }
            }
        }
    }

    @Override
    public boolean isIgnoringIfFeatures() {
        return true;
    }

    @Override
    public boolean isIgnoringConfig() {
        return true;
    }

    @Override
    protected YangDataStatement createDeclared(final BoundStmtCtx<String> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new YangDataStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected YangDataStatement attachDeclarationReference(final YangDataStatement stmt,
            final DeclarationReference reference) {
        return new RefYangDataStatement(stmt, reference);
    }

    @Override
    protected YangDataEffectiveStatement createEffective(final Current<String, YangDataStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // RFC8040 page 80 requires that:
        //    It MUST contain data definition statements
        //    that result in exactly one container data node definition.
        //    An instance of a YANG data template can thus be translated
        //    into an XML instance document, whose top-level element
        //    corresponds to the top-level container.
        //
        // We validate this additional constraint when we arrive at the effective model, with the view that
        // 'container data node definition' is really meant to say 'XML element'.
        //
        // This really boils down to the requirement to have a single schema tree substatement, which needs to either
        // be a data tree statement or a choice statement.
        final var schemaSub = substatements.stream()
            .filter(SchemaTreeEffectiveStatement.class::isInstance)
            .collect(Collectors.toUnmodifiableList());
        switch (schemaSub.size()) {
            case 0 -> throw new MissingSubstatementException(stmt, "yang-data requires at least one substatement");
            case 1 -> {
                final var substmt = schemaSub.get(0);
                SourceException.throwIf(
                    !(substmt instanceof ChoiceEffectiveStatement) && !(substmt instanceof DataTreeEffectiveStatement),
                    stmt, "%s is not a recognized container data node definition", substmt);
            }
            default -> throw new InvalidSubstatementException(stmt,
                "yang-data requires exactly one container data node definition, found %s", schemaSub);
        }

        return new YangDataEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
