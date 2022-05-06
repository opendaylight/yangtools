/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagEffectiveStatement;
import org.opendaylight.yangtools.rfc8819.parser.ModuleTagSupport;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class ModuleTagTest {

    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                        new ModuleTagSupport(YangParserConfiguration.DEFAULT))
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void testModuleTagSupportExtension() throws ReactorException {
        final SchemaContext schemaContext = reactor.newBuild().addSources(
                        moduleFromResources("/example-tag-module.yang"),
                        moduleFromResources("/ietf-module-tags.yang"),
                        moduleFromResources("/ietf-yang-types.yang"),
                        moduleFromResources("/ietf-module-tags-state.yang"))
                .buildEffective();
        final List<ModuleTagEffectiveStatement> moduleTags = findAllModuleTags(schemaContext);

        assertEquals(8, moduleTags.size());
    }

    @Test
    public void throwExceptionWhenTagParentIsNotModuleOrSubmodule() {
        final CrossSourceStatementReactor.BuildAction action = reactor.newBuild().addSources(
                moduleFromResources("/foo-tag-module.yang"),
                moduleFromResources("/ietf-module-tags.yang"),
                moduleFromResources("/ietf-yang-types.yang"),
                moduleFromResources("/ietf-module-tags-state.yang"));

        assertThrows(SomeModifiersUnresolvedException.class, action::buildEffective);
    }

    private List<ModuleTagEffectiveStatement> findAllModuleTags(final SchemaContext schemaContext) {
        final ImmutableList.Builder<ModuleTagEffectiveStatement> builder = ImmutableList.builder();
        for (final Module module : schemaContext.getModules()) {
            for (final UnknownSchemaNode node : module.getUnknownSchemaNodes()) {
                if (node instanceof ModuleTagEffectiveStatement) {
                    builder.add((ModuleTagEffectiveStatement) node);
                }
            }
        }
        return builder.build();
    }

    private static YangStatementStreamSource moduleFromResources(final String resourceName) {
        try {
            return YangStatementStreamSource.create(YangTextSchemaSource.forResource(resourceName));
        } catch (final YangSyntaxErrorException | IOException e) {
            throw new IllegalArgumentException("Failed to find resource.", e);
        }
    }
}
