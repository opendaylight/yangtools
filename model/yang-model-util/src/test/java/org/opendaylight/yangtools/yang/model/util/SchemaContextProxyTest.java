/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.AbstractSchemaContext;
import org.opendaylight.yangtools.yang.model.util.FilteringSchemaContextProxy.ModuleId;

class SchemaContextProxyTest {

    private static final String NAMESPACE = "urn:opendaylight:params:xml:ns:yang:controller:config";
    private static final Revision REVISION = Revision.of("2015-01-01");
    private static final Revision REVISION2 = Revision.of("2015-01-15");

    private static final String CONFIG_NAME = "config";
    private static final String ROOT_NAME = "root";
    private static final String MODULE2_NAME = "module2";
    private static final String MODULE3_NAME = "module3";
    private static final String MODULE4_NAME = "module4";
    private static final String MODULE41_NAME = "module41";
    private static final String MODULE5_NAME = "module5";

    private static EffectiveModelContext mockSchema(final Module... modules) {
        Arrays.sort(modules, AbstractSchemaContext.NAME_REVISION_COMPARATOR);
        var mock = mock(EffectiveModelContext.class);
        doReturn(ImmutableSet.copyOf(modules)).when(mock).getModules();
        return mock;
    }

    /*
     * CFG(R)
     *  | \
     *  |  \
     * M2 <- M3
     */
    @Test
    void testBasic() {
        var moduleConfig = mockModule(CONFIG_NAME);
        var module2 = mockModule(MODULE2_NAME);
        var module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2, moduleConfig);

        var schemaContext = mockSchema(moduleConfig, module2, module3);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3);
    }

    /*
     * No root or additional modules
     *  | \
     *  |  \
     * M2 <- M3
     */
    @Test
    void testNull() {
        var moduleConfig = mockModule(CONFIG_NAME);
        var module2 = mockModule(MODULE2_NAME);
        var module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2, moduleConfig);

        var schemaContext = mockSchema(moduleConfig, module2, module3);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, null);
        assertProxyContext(filteringSchemaContextProxy, null);
    }

    /*
     *  Config
     *  | \ (NR)
     *  |  \
     * M2 <- M3
     */
    @Test
    void testConfigDifferentRevisions() {
        var moduleConfigNullRevision = mockModule(CONFIG_NAME, null);
        var moduleConfig = mockModule(CONFIG_NAME, REVISION);
        var moduleConfig2 = mockModule(CONFIG_NAME, REVISION2);
        var module2 = mockModule(MODULE2_NAME);
        var module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2, moduleConfigNullRevision);

        var schemaContext = mockSchema(moduleConfig, moduleConfig2, module2, module3);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, moduleConfig2, module2, module3);
    }

    /*
     *     CFG(R)
     *    |      \
     *   |         \
     * M2<-(NullRev)M3
     */
    @Test
    void testBasicNullRevision() throws Exception {
        final var moduleConfig = mockModule(CONFIG_NAME, Revision.of("2013-04-05"));
        final var module2 = mockModule(MODULE2_NAME, Revision.of("2014-06-17"));
        final var module20 = mockModule(MODULE2_NAME, null);
        final var module3 = mockModule(MODULE3_NAME, Revision.of("2014-06-12"));
        final var module30 = mockModule(MODULE3_NAME, null);

        mockModuleImport(module20, moduleConfig);
        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module20, moduleConfig);
        mockModuleImport(module30, module20, moduleConfig);

        var schemaContext = mockSchema(moduleConfig, module2, module3);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3);
    }

    /*
     * CFG(R)   ROOT(R)
     *  |         \
     *  |          \
     * M2          M3
     */
    @Test
    void testBasicMoreRootModules() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var moduleRoot = mockModule(ROOT_NAME);
        final var module2 = mockModule(MODULE2_NAME);
        final var module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, moduleRoot);

        var schemaContext = mockSchema(moduleConfig, moduleRoot, module2, module3);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleRoot, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleRoot, module3, moduleConfig, module2);
    }

    /*
     * CFG(R)
     *  |
     *  |
     * M2 <- M3
     */
    @Test
    void testChainNotDepend() {
        var moduleConfig = mockModule(CONFIG_NAME);
        var module2 = mockModule(MODULE2_NAME);
        var module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2);

        var schemaContext = mockSchema(moduleConfig, module2, module3);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2);
    }

    /*
     * CFG(R)
     *  |
     *  |
     * M2 -> M3 -> M4 -> M5
     */
    @Test
    void testChainDependMulti() {
        var moduleConfig = mockModule(CONFIG_NAME);
        var module2 = mockModule(MODULE2_NAME);
        var module3 = mockModule(MODULE3_NAME);
        var module4 = mockModule(MODULE4_NAME);
        var module5 = mockModule(MODULE5_NAME);

        mockModuleImport(module2, moduleConfig, module3);
        mockModuleImport(module3, module4);
        mockModuleImport(module4, module5);

        var schemaContext = mockSchema(moduleConfig, module2, module3, module4, module5);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3, module4, module5);
    }

    /*
     * CFG(R)
     *  |
     *  |
     * M2 -> M3 <- M4
     */
    @Test
    void testChainNotDependMulti() {
        var moduleConfig = mockModule(CONFIG_NAME);
        var module2 = mockModule(MODULE2_NAME);
        var module3 = mockModule(MODULE3_NAME);
        var module4 = mockModule(MODULE4_NAME);

        mockModuleImport(module2, moduleConfig, module3);
        mockModuleImport(module4, module3);

        var schemaContext = mockSchema(moduleConfig, module2, module3, module4);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3);
    }

    /*
     *  CFG(R)
     *  | \ \ \
     *  |  \ \ \
     * M2 M3 M4 M5
     */
    @Test
    void testChainNotMulti() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var module2 = mockModule(MODULE2_NAME);
        final var module3 = mockModule(MODULE3_NAME);
        final var module4 = mockModule(MODULE4_NAME);
        final var module5 = mockModule(MODULE5_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, moduleConfig);
        mockModuleImport(module4, moduleConfig);
        mockModuleImport(module5, moduleConfig);

        var schemaContext = mockSchema(moduleConfig, module2, module3, module4, module5);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3, module4, module5);
    }

    /*
     * CFG(R)
     *  | \
     *  |  \
     * M2 <- M3 M4=M3(Different revision)
     */
    @Test
    void testBasicRevisionChange() throws Exception {
        var moduleConfig = mockModule(CONFIG_NAME);
        var module2 = mockModule(MODULE2_NAME);
        var module3 = mockModule(MODULE3_NAME);
        var module4 = mockModule(MODULE3_NAME, Revision.of("2015-10-10"));

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2, moduleConfig);

        var schemaContext = mockSchema(moduleConfig, module2, module3, module4);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3);
    }

    /*
     * CFG(R)
     * |
     * M2 -(no revision)-> M3(R2) ... M3(R1)
     */
    @Test
    void testImportNoRevision() {
        var moduleConfig = mockModule(CONFIG_NAME, REVISION);
        var module2 = mockModule(MODULE2_NAME, REVISION);

        var module3 = mockModule(MODULE3_NAME, null);
        var module30 = mockModule(MODULE3_NAME, REVISION);
        var module31 = mockModule(MODULE3_NAME, REVISION2);
        mockModuleImport(module2, moduleConfig, module3);

        var schemaContext = mockSchema(moduleConfig, module2, module30, module31);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module31);
    }

    /*
     * CFG(R)
     * |   \
     * |    \
     * |    M2 -> M3
     * |
     * M41(S) => M4
     */
    @Test
    void testBasicSubmodule() {
        var moduleConfig = mockModule(CONFIG_NAME);
        var module2 = mockModule(MODULE2_NAME);
        var module3 = mockModule(MODULE3_NAME);
        var module4 = mockModule(MODULE4_NAME);
        var module41 = mockSubmodule(MODULE41_NAME);

        mockSubmodules(module4, module41);
        mockModuleImport(module2, moduleConfig, module3);
        mockModuleImport(module41, moduleConfig);

        var schemaContext = mockSchema(moduleConfig, module2, module3, module4);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3, module4);
    }

    /*
     * M2 -> M3 -> M4 -> M5
     */
    @Test
    void testChainAdditionalModules() {
        var module2 = mockModule(MODULE2_NAME);
        var module3 = mockModule(MODULE3_NAME);
        var module4 = mockModule(MODULE4_NAME);
        var module5 = mockModule(MODULE5_NAME);

        mockModuleImport(module2, module3);
        mockModuleImport(module3, module4);
        mockModuleImport(module4, module5);

        var schemaContext = mockSchema(module2, module3, module4, module5);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(module2), null);
        assertProxyContext(filteringSchemaContextProxy, module2, module3, module4, module5);
    }

    /*
     *
     * CFG(R)
     *  |
     *  |       M5
     * M2
     *
     * M3 -> M4
     */
    @Test
    void testChainAdditionalModulesConfig() {
        var moduleConfig = mockModule(CONFIG_NAME);
        var module2 = mockModule(MODULE2_NAME);

        var module3 = mockModule(MODULE3_NAME);
        var module4 = mockModule(MODULE4_NAME);
        var module5 = mockModule(MODULE5_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module4);

        var schemaContext = mockSchema(moduleConfig, module2, module3, module4, module5);

        var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(module3), moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3, module4);
    }

    @Test
    void testGetDataDefinitions() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var schemaContext = mockSchema(moduleConfig);
        final var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(), moduleConfig);

        final var mockedContainer = mock(ContainerSchemaNode.class);
        doReturn(Set.of(mockedContainer)).when(moduleConfig).getChildNodes();

        final var dataDefinitions = filteringSchemaContextProxy.getDataDefinitions();
        assertTrue(dataDefinitions.contains(mockedContainer));
    }

    @Test
    void testGetNotifications() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var schemaContext = mockSchema(moduleConfig);
        final var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(), moduleConfig);

        final var mockedNotification = mock(NotificationDefinition.class);
        doReturn(Set.of(mockedNotification)).when(moduleConfig).getNotifications();

        final var schemaContextProxyNotifications = filteringSchemaContextProxy.getNotifications();
        assertTrue(schemaContextProxyNotifications.contains(mockedNotification));
    }

    @Test
    void testGetOperations() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var schemaContext = mockSchema(moduleConfig);
        final var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(), moduleConfig);

        final var mockedRpc = mock(RpcDefinition.class);
        doReturn(Set.of(mockedRpc)).when(moduleConfig).getRpcs();

        final var operations = filteringSchemaContextProxy.getOperations();
        assertTrue(operations.contains(mockedRpc));
    }

    @Test
    void testGetExtensions() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var schemaContext = mockSchema(moduleConfig);
        final var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(), moduleConfig);

        final var mockedExtension = mock(ExtensionDefinition.class);
        doReturn(List.of(mockedExtension)).when(moduleConfig).getExtensionSchemaNodes();

        final var schemaContextProxyExtensions = filteringSchemaContextProxy.getExtensions();
        assertTrue(schemaContextProxyExtensions.contains(mockedExtension));
    }

    @Test
    void testGetUnknownSchemaNodes() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var schemaContext = mockSchema(moduleConfig);
        final var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(), moduleConfig);

        final var mockedUnknownSchemaNode = mock(UnknownSchemaNode.class);
        doReturn(List.of(mockedUnknownSchemaNode)).when(moduleConfig).getUnknownSchemaNodes();

        final var schemaContextProxyUnknownSchemaNodes = filteringSchemaContextProxy.getUnknownSchemaNodes();
        assertTrue(schemaContextProxyUnknownSchemaNodes.contains(mockedUnknownSchemaNode));
    }

    @Test
    void testGetTypeDefinitions() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var schemaContext = mockSchema(moduleConfig);
        final var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(), moduleConfig);

        final var mockedTypeDefinition = mock(TypeDefinition.class);
        doReturn(Set.of(mockedTypeDefinition)).when(moduleConfig).getTypeDefinitions();

        final var schemaContextProxyTypeDefinitions = filteringSchemaContextProxy.getTypeDefinitions();
        assertTrue(schemaContextProxyTypeDefinitions.contains(mockedTypeDefinition));
    }

    @Test
    void testGetChildNodes() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var schemaContext = mockSchema(moduleConfig);
        final var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(), moduleConfig);

        final var mockedContainer = mock(ContainerSchemaNode.class);
        doReturn(Set.of(mockedContainer)).when(moduleConfig).getChildNodes();

        final var schemaContextProxyChildNodes = filteringSchemaContextProxy.getChildNodes();
        assertTrue(schemaContextProxyChildNodes.contains(mockedContainer));
    }

    @Test
    void testGetGroupings() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var schemaContext = mockSchema(moduleConfig);
        final var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(), moduleConfig);

        final var mockedGrouping = mock(GroupingDefinition.class);
        doReturn(Set.of(mockedGrouping)).when(moduleConfig).getGroupings();

        final var schemaContextProxyGroupings = filteringSchemaContextProxy.getGroupings();
        assertTrue(schemaContextProxyGroupings.contains(mockedGrouping));
    }

    @Test
    void testGetDataChildByName() {
        final var moduleConfig = mockModule(CONFIG_NAME);
        final var schemaContext = mockSchema(moduleConfig);
        final var filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Set.of(), moduleConfig);

        final var qname = QName.create("config-namespace", "2016-08-11", "cont");
        final var mockedContainer = mock(ContainerSchemaNode.class);
        doReturn(mockedContainer).when(moduleConfig).dataChildByName(any(QName.class));

        final var dataSchemaNode = filteringSchemaContextProxy.getDataChildByName(qname);
        assertTrue(dataSchemaNode instanceof ContainerSchemaNode);
    }

    private static void assertProxyContext(final FilteringSchemaContextProxy filteringSchemaContextProxy,
            final Module... expected) {

        final var modSet = expected != null ? ImmutableSet.copyOf(expected) : Set.of();
        var modSetFiltering = filteringSchemaContextProxy.getModules();

        assertEquals(modSet, modSetFiltering);

        //asserting collections
        if (expected != null) {
            for (var module : expected) {
                assertEquals(Optional.of(module),
                    filteringSchemaContextProxy.findModule(module.getName(), module.getRevision()));

                final var mod = filteringSchemaContextProxy.findModules(module.getNamespace());
                assertTrue(mod.contains(module));
                assertEquals(Optional.of(module),
                    filteringSchemaContextProxy.findModule(module.getNamespace(), module.getRevision().orElse(null)));
            }
        }
    }

    private static FilteringSchemaContextProxy createProxySchemaCtx(final EffectiveModelContext schemaContext,
            final Set<Module> additionalModules, final Module... modules) {
        final var modulesSet = modules != null ? ImmutableSet.copyOf(modules) : Set.<Module>of();
        return new FilteringSchemaContextProxy(schemaContext, createModuleIds(modulesSet),
                createModuleIds(additionalModules));
    }

    private static Set<ModuleId> createModuleIds(final Set<Module> modules) {
        final var moduleIds = new HashSet<ModuleId>();
        if (modules != null) {
            for (var module : modules) {
                moduleIds.add(new ModuleId(module.getName(), module.getRevision()));
            }
        }

        return moduleIds;
    }

    private static void mockSubmodules(final Module mainModule, final Submodule... submodules) {
        doReturn(Set.of(submodules)).when(mainModule).getSubmodules();
    }

    private static void mockModuleImport(final ModuleLike importer, final Module... imports) {
        final var mockedImports = new HashSet<ModuleImport>();
        for (var module : imports) {
            mockedImports.add(new ModuleImport() {
                @Override
                public Unqualified getModuleName() {
                    return Unqualified.of(module.getName());
                }

                @Override
                public Optional<Revision> getRevision() {
                    return module.getRevision();
                }

                @Override
                public String getPrefix() {
                    return module.getName();
                }

                @Override
                public Optional<String> getDescription() {
                    return module.getDescription();
                }

                @Override
                public Optional<String> getReference() {
                    return module.getReference();
                }

                @Override
                public String toString() {
                    return String.format("Module: %s, revision:%s", module.getName(), module.getRevision());
                }

                @Override
                public ImportEffectiveStatement asEffectiveStatement() {
                    throw new UnsupportedOperationException();
                }
            });
        }
        doReturn(mockedImports).when(importer).getImports();
    }

    //mock module with revision
    private static Module mockModule(final String name, final Revision rev) {
        final var mod = mockModule(name);

        doReturn(QNameModule.ofRevision(mod.getNamespace(), rev)).when(mod).getQNameModule();
        doReturn(Optional.ofNullable(rev)).when(mod).getRevision();
        doReturn(mod.getQNameModule().toString()).when(mod).toString();

        return mod;
    }

    //mock module with default revision
    private static Module mockModule(final String name) {
        final var mockedModule = mock(Module.class);
        mockModuleLike(mockedModule, name);
        return mockedModule;
    }

    private static Submodule mockSubmodule(final String name) {
        final var mockedModule = mock(Submodule.class);
        mockModuleLike(mockedModule, name);
        return mockedModule;
    }

    private static void mockModuleLike(final ModuleLike mockedModule, final String name) {
        doReturn(name).when(mockedModule).getName();
        doReturn(Optional.of(REVISION)).when(mockedModule).getRevision();
        final var newNamespace = XMLNamespace.of(NAMESPACE + ":" + name);
        doReturn(newNamespace).when(mockedModule).getNamespace();
        doReturn(QNameModule.of(newNamespace, REVISION)).when(mockedModule).getQNameModule();
        doReturn(Set.of()).when(mockedModule).getSubmodules();
        doReturn(mockedModule.getQNameModule().toString()).when(mockedModule).toString();
        mockModuleImport(mockedModule);
    }
}
