/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

import java.io.IOException;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagEffectiveStatement;
import org.opendaylight.yangtools.rfc8819.parser.ModuleTagSupport;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class ModuleTagTest {

    @Test
    public void testModuleTagSupport() throws ReactorException {
        StatementStreamSource tagExampleModule = moduleFromResources("/tag-example-module.yang");
        StatementStreamSource ietfModuleTags = moduleFromResources("/ietf-module-tags.yang");
        StatementStreamSource ietfYangTypes = moduleFromResources("/ietf-yang-types.yang");
        StatementStreamSource ietfModuleTagsState = moduleFromResources("/ietf-module-tags-state.yang");

        SchemaContext schemaContext = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.SOURCE_LINKAGE,
                        new ModuleTagSupport(YangParserConfiguration.DEFAULT))
                .build()
                .newBuild()
                .addSources(tagExampleModule, ietfModuleTags, ietfYangTypes, ietfModuleTagsState)
                .buildEffective();
        var module = Objects.requireNonNull(schemaContext
                .findModule("tag-example-module", Revision.of("2022-04-05"))
                .orElse(null));

        ModuleTagEffectiveStatement effectiveSubStatement = Objects.requireNonNull(schemaContext
                        .findModule("tag-example-module", Revision.of("2022-04-05"))
                        .orElse(null))
                .asEffectiveStatement()
                .findFirstEffectiveSubstatement(ModuleTagEffectiveStatement.class)
                .orElse(null);

        Assertions.assertNotNull(effectiveSubStatement);
    }

    private static YangStatementStreamSource moduleFromResources(String resourceName) {
        try {
            return YangStatementStreamSource.create(YangTextSchemaSource.forResource(resourceName));
        } catch (YangSyntaxErrorException | IOException e) {
            throw new IllegalArgumentException("Failed to find resource.", e);
        }
    }

}
