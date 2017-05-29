/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.FilteringSchemaContextProxy.ModuleId;

public class SchemaContextProxyTest {

    private static URI namespace;
    private static Date revision;
    private static Date revision2;

    private static final String CONFIG_NAME = "config";
    private static final String ROOT_NAME = "root";
    private static final String MODULE2_NAME = "module2";
    private static final String MODULE3_NAME = "module3";
    private static final String MODULE4_NAME = "module4";
    private static final String MODULE41_NAME = "module41";
    private static final String MODULE5_NAME = "module5";
    private static final String TEST_SOURCE = "test source";

    @BeforeClass
    public static void setUp() throws ParseException, URISyntaxException {

        namespace = new URI("urn:opendaylight:params:xml:ns:yang:controller:config");

        revision = SimpleDateFormatUtil.getRevisionFormat().parse("2015-01-01");
        revision2 = SimpleDateFormatUtil.getRevisionFormat().parse("2015-01-15");
    }

    private static SchemaContext mockSchema(final Module... module) {
        SchemaContext mock = mock(SchemaContext.class);
        doReturn(Sets.newHashSet(module)).when(mock).getModules();
        return mock;
    }

    /**
     * <pre>
     * CFG(R)
     *  | \
     *  |  \
     * M2 &lt;- M3
     * </pre>
     */
    @Test
    public void testBasic() {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2, moduleConfig);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3);
    }

    /**
     * <pre>
     * No root or additional modules
     *  | \
     *  |  \
     * M2 &lt;- M3
     * </pre>
     */
    @Test
    public void testNull() {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2, moduleConfig);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, null);
        assertProxyContext(filteringSchemaContextProxy, null);
    }

    /**
     * <pre>
     *  Config
     *  | \ (NR)
     *  |  \
     * M2 &lt;- M3
     * </pre>
     */
    @Test
    public void testConfigDifferentRevisions() {
        Module moduleConfigNullRevision = mockModule(CONFIG_NAME, null);
        Module moduleConfig = mockModule(CONFIG_NAME, revision);
        Module moduleConfig2 = mockModule(CONFIG_NAME, revision2);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2, moduleConfigNullRevision);

        SchemaContext schemaContext = mockSchema(moduleConfig, moduleConfig2, module2, module3);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, moduleConfig2, module2, module3);
    }

    /**
     * <pre>
     *     CFG(R)
     *    |      \
     *   |         \
     * M2&lt;-(NullRev)M3
     * </pre>
     */
    @Test
    public void testBasicNullRevision() throws Exception {
        Module moduleConfig = mockModule(CONFIG_NAME,SimpleDateFormatUtil.getRevisionFormat().parse("2013-04-05"));
        Module module2 = mockModule(MODULE2_NAME, SimpleDateFormatUtil.getRevisionFormat().parse("2014-06-17"));
        Module module20 = mockModule(MODULE2_NAME, null);
        Module module3 = mockModule(MODULE3_NAME, SimpleDateFormatUtil.getRevisionFormat().parse("2014-06-12"));
        Module module30 = mockModule(MODULE3_NAME, null);

        mockModuleImport(module20, moduleConfig);
        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module20, moduleConfig);
        mockModuleImport(module30, module20, moduleConfig);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);

        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3);
    }

    /**
     * <pre>
     * CFG(R)   ROOT(R)
     *  |         \
     *  |          \
     * M2          M3
     * </pre>
     */
    @Test
    public void testBasicMoreRootModules() {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module moduleRoot = mockModule(ROOT_NAME);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, moduleRoot);

        SchemaContext schemaContext = mockSchema(moduleConfig, moduleRoot, module2, module3);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleRoot, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleRoot, module3, moduleConfig, module2);
    }

    /**
     * <pre>
     * CFG(R)
     *  |
     *  |
     * M2 &lt;- M3
     * </pre>
     */
    @Test
    public void testChainNotDepend() {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2);
    }

    /**
     * <pre>
     * CFG(R)
     *  |
     *  |
     * M2 -&gt; M3 -&gt; M4 -&gt; M5
     * </pre>
     */
    @Test
    public void testChainDependMulti() {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);
        Module module4 = mockModule(MODULE4_NAME);
        Module module5 = mockModule(MODULE5_NAME);

        mockModuleImport(module2, moduleConfig, module3);
        mockModuleImport(module3, module4);
        mockModuleImport(module4, module5);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3, module4, module5);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3, module4, module5);
    }

    /**
     * <pre>
     * CFG(R)
     *  |
     *  |
     * M2 -&gt; M3 &lt;- M4
     * </pre>
     */
    @Test
    public void testChainNotDependMulti() {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);
        Module module4 = mockModule(MODULE4_NAME);

        mockModuleImport(module2, moduleConfig, module3);
        mockModuleImport(module4, module3);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3, module4);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3);
    }

    /**
     * <pre>
     *  CFG(R)
     *  | \ \ \
     *  |  \ \ \
     * M2 M3 M4 M5
     * </pre>
     */
    @Test
    public void testChainNotMulti() {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);
        Module module4 = mockModule(MODULE4_NAME);
        Module module5 = mockModule(MODULE5_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, moduleConfig);
        mockModuleImport(module4, moduleConfig);
        mockModuleImport(module5, moduleConfig);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3, module4, module5);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3, module4, module5);
    }

    /**
     * <pre>
     * CFG(R)
     *  | \
     *  |  \
     * M2 &lt;- M3 M4=M3(Different revision)
     * </pre>
     */
    @Test
    public void testBasicRevisionChange() throws Exception {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);

        Date dat = SimpleDateFormatUtil.getRevisionFormat().parse("2015-10-10");
        Module module4 = mockModule(MODULE3_NAME, dat);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module2, moduleConfig);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3, module4);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3);
    }

    /**
     * <pre>
     * CFG(R)
     * |
     * M2 -(no revision)-&gt; M3(R2) ... M3(R1)
     * </pre>
     */
    @Test
    public void testImportNoRevision() {
        Module moduleConfig = mockModule(CONFIG_NAME, revision);
        Module module2 = mockModule(MODULE2_NAME, revision);

        Module module3  = mockModule(MODULE3_NAME, null);
        Module module30 = mockModule(MODULE3_NAME, revision);
        Module module31 = mockModule(MODULE3_NAME, revision2);
        mockModuleImport(module2, moduleConfig, module3);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module30, module31);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);

        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module31);
    }

    /**
     * <pre>
     * CFG(R)
     * |   \
     * |    \
     * |    M2 -&gt; M3
     * |
     * M41(S) =&gt; M4
     * </pre>
     */
    @Test
    public void testBasicSubmodule() {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);
        Module module4 = mockModule(MODULE4_NAME);
        Module module41 = mockModule(MODULE41_NAME);

        mockSubmodules(module4, module41);
        mockModuleImport(module2, moduleConfig, module3);
        mockModuleImport(module41, moduleConfig);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3, module4);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3, module4);
    }

    /**
     * <pre>
     *
     * M2 -&gt; M3 -&gt; M4 -&gt; M5
     *
     * </pre>
     */
    @Test
    public void testChainAdditionalModules() {
        Module module2 = mockModule(MODULE2_NAME);
        Module module3 = mockModule(MODULE3_NAME);
        Module module4 = mockModule(MODULE4_NAME);
        Module module5 = mockModule(MODULE5_NAME);

        mockModuleImport(module2, module3);
        mockModuleImport(module3, module4);
        mockModuleImport(module4, module5);

        SchemaContext schemaContext = mockSchema(module2, module3, module4, module5);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Sets.newHashSet(module2), null);
        assertProxyContext(filteringSchemaContextProxy, module2, module3, module4, module5);
    }

    /**
     * <pre>
     *
     * CFG(R)
     *  |
     *  |       M5
     * M2
     *
     * M3 -&gt; M4
     *
     * </pre>
     */
    @Test
    public void testChainAdditionalModulesConfig() {
        Module moduleConfig = mockModule(CONFIG_NAME);
        Module module2 = mockModule(MODULE2_NAME);

        Module module3 = mockModule(MODULE3_NAME);
        Module module4 = mockModule(MODULE4_NAME);
        Module module5 = mockModule(MODULE5_NAME);

        mockModuleImport(module2, moduleConfig);
        mockModuleImport(module3, module4);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module3, module4, module5);

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, Sets.newHashSet(module3), moduleConfig);
        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module3, module4);
    }

    @Test
    public void testGetDataDefinitions() {
        final Module moduleConfig = mockModule(CONFIG_NAME);
        final SchemaContext schemaContext = mockSchema(moduleConfig);
        final FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext,
                Sets.newHashSet(), moduleConfig);

        final ContainerSchemaNode mockedContainer = mock(ContainerSchemaNode.class);
        final Set<DataSchemaNode> childNodes = Sets.newHashSet(mockedContainer);
        doReturn(childNodes).when(moduleConfig).getChildNodes();

        final Set<DataSchemaNode> dataDefinitions = filteringSchemaContextProxy.getDataDefinitions();
        assertTrue(dataDefinitions.contains(mockedContainer));
    }

    @Test
    public void testGetNotifications() {
        final Module moduleConfig = mockModule(CONFIG_NAME);
        final SchemaContext schemaContext = mockSchema(moduleConfig);
        final FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext,
                Sets.newHashSet(), moduleConfig);

        final NotificationDefinition mockedNotification = mock(NotificationDefinition.class);
        final Set<NotificationDefinition> notifications = Sets.newHashSet(mockedNotification);
        doReturn(notifications).when(moduleConfig).getNotifications();

        final Set<NotificationDefinition> schemaContextProxyNotifications = filteringSchemaContextProxy.getNotifications();
        assertTrue(schemaContextProxyNotifications.contains(mockedNotification));
    }

    @Test
    public void testGetOperations() {
        final Module moduleConfig = mockModule(CONFIG_NAME);
        final SchemaContext schemaContext = mockSchema(moduleConfig);
        final FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext,
                Sets.newHashSet(), moduleConfig);

        final RpcDefinition mockedRpc = mock(RpcDefinition.class);
        final Set<RpcDefinition> rpcs = Sets.newHashSet(mockedRpc);
        doReturn(rpcs).when(moduleConfig).getRpcs();

        final Set<RpcDefinition> operations = filteringSchemaContextProxy.getOperations();
        assertTrue(operations.contains(mockedRpc));
    }

    @Test
    public void testGetExtensions() {
        final Module moduleConfig = mockModule(CONFIG_NAME);
        final SchemaContext schemaContext = mockSchema(moduleConfig);
        final FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext,
                Sets.newHashSet(), moduleConfig);

        final ExtensionDefinition mockedExtension = mock(ExtensionDefinition.class);
        final List<ExtensionDefinition> extensions = Lists.newArrayList(mockedExtension);
        doReturn(extensions).when(moduleConfig).getExtensionSchemaNodes();

        final Set<ExtensionDefinition> schemaContextProxyExtensions = filteringSchemaContextProxy.getExtensions();
        assertTrue(schemaContextProxyExtensions.contains(mockedExtension));
    }

    @Test
    public void testGetUnknownSchemaNodes() {
        final Module moduleConfig = mockModule(CONFIG_NAME);
        final SchemaContext schemaContext = mockSchema(moduleConfig);
        final FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext,
                Sets.newHashSet(), moduleConfig);

        final UnknownSchemaNode mockedUnknownSchemaNode = mock(UnknownSchemaNode.class);
        final List<UnknownSchemaNode> unknownSchemaNodes = Lists.newArrayList(mockedUnknownSchemaNode);
        doReturn(unknownSchemaNodes).when(moduleConfig).getUnknownSchemaNodes();

        final List<UnknownSchemaNode> schemaContextProxyUnknownSchemaNodes =
                filteringSchemaContextProxy.getUnknownSchemaNodes();
        assertTrue(schemaContextProxyUnknownSchemaNodes.contains(mockedUnknownSchemaNode));
    }

    @Test
    public void testGetTypeDefinitions() {
        final Module moduleConfig = mockModule(CONFIG_NAME);
        final SchemaContext schemaContext = mockSchema(moduleConfig);
        final FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext,
                Sets.newHashSet(), moduleConfig);

        final TypeDefinition<?> mockedTypeDefinition = mock(TypeDefinition.class);
        final Set<TypeDefinition<?>> typeDefinitions = Sets.newHashSet(mockedTypeDefinition);
        doReturn(typeDefinitions).when(moduleConfig).getTypeDefinitions();

        final Set<TypeDefinition<?>> schemaContextProxyTypeDefinitions = filteringSchemaContextProxy.getTypeDefinitions();
        assertTrue(schemaContextProxyTypeDefinitions.contains(mockedTypeDefinition));
    }

    @Test
    public void testGetChildNodes() {
        final Module moduleConfig = mockModule(CONFIG_NAME);
        final SchemaContext schemaContext = mockSchema(moduleConfig);
        final FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext,
                Sets.newHashSet(), moduleConfig);

        final ContainerSchemaNode mockedContainer = mock(ContainerSchemaNode.class);
        final Set<DataSchemaNode> childNodes = Sets.newHashSet(mockedContainer);
        doReturn(childNodes).when(moduleConfig).getChildNodes();

        final Set<DataSchemaNode> schemaContextProxyChildNodes = filteringSchemaContextProxy.getChildNodes();
        assertTrue(schemaContextProxyChildNodes.contains(mockedContainer));
    }

    @Test
    public void testGetGroupings() {
        final Module moduleConfig = mockModule(CONFIG_NAME);
        final SchemaContext schemaContext = mockSchema(moduleConfig);
        final FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext,
                Sets.newHashSet(), moduleConfig);

        final GroupingDefinition mockedGrouping = mock(GroupingDefinition.class);
        final Set<GroupingDefinition> groupings = Sets.newHashSet(mockedGrouping);
        doReturn(groupings).when(moduleConfig).getGroupings();

        final Set<GroupingDefinition> schemaContextProxyGroupings = filteringSchemaContextProxy.getGroupings();
        assertTrue(schemaContextProxyGroupings.contains(mockedGrouping));
    }

    @Test
    public void testGetDataChildByName() {
        final Module moduleConfig = mockModule(CONFIG_NAME);
        final SchemaContext schemaContext = mockSchema(moduleConfig);
        final FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext,
                Sets.newHashSet(), moduleConfig);

        final QName qName = QName.create("config-namespace", "2016-08-11", "cont");
        final ContainerSchemaNode mockedContainer = mock(ContainerSchemaNode.class);
        doReturn(mockedContainer).when(moduleConfig).getDataChildByName(any(QName.class));

        final DataSchemaNode dataSchemaNode = filteringSchemaContextProxy.getDataChildByName(qName);
        assertTrue(dataSchemaNode instanceof ContainerSchemaNode);
    }

    private static void assertProxyContext(final FilteringSchemaContextProxy filteringSchemaContextProxy, final Module... expected) {

        Set<Module> modSet = Sets.newHashSet();

        if (expected!=null) {

            modSet = Sets.newHashSet(expected);
        }

        Set<Module> modSetFiltering = filteringSchemaContextProxy.getModules();

        assertEquals(modSet, modSetFiltering);

        //asserting collections
        if (expected!=null) {
            for (final Module module : expected) {
                assertEquals(module, filteringSchemaContextProxy.findModuleByName(module.getName(), module.getRevision()));

                Set<Module> mod = filteringSchemaContextProxy.findModuleByNamespace(module.getNamespace());
                assertTrue(mod.contains(module));

                assertEquals(module, filteringSchemaContextProxy.findModuleByNamespaceAndRevision(module.getNamespace(), module.getRevision()));

                assertEquals(module.getSource(), filteringSchemaContextProxy.getModuleSource(module).get());
            }
        }
    }

    private static FilteringSchemaContextProxy createProxySchemaCtx(final SchemaContext schemaContext,
            final Set<Module> additionalModules, final Module... modules) {

        Set<Module> modulesSet = new HashSet<>();

        if (modules!=null) {

            modulesSet = Sets.newHashSet(modules);

        }

        return new FilteringSchemaContextProxy(schemaContext, createModuleIds(modulesSet) , createModuleIds(additionalModules));
    }

    private static Set<ModuleId> createModuleIds(final Set<Module> modules) {

        Set<ModuleId> moduleIds = Sets.newHashSet();

        if (modules!=null && modules.size()>0) {

            for (Module module : modules) {

                moduleIds.add(new ModuleId(module.getName(), module.getRevision()));
            }
        }

        return moduleIds;
    }

    private static void mockSubmodules(final Module mainModule, final Module... submodules) {

        Set<Module> submodulesSet = new HashSet<>();
        submodulesSet.addAll(Arrays.asList(submodules));

        doReturn(submodulesSet).when(mainModule).getSubmodules();
    }

    private static void mockModuleImport(final Module importer, final Module... imports) {
        Set<ModuleImport> mockedImports = Sets.newHashSet();
        for (final Module module : imports) {
            mockedImports.add(new ModuleImport() {
                @Override
                public String getModuleName() {
                    return module.getName();
                }

                @Override
                public Date getRevision() {
                    return module.getRevision();
                }

                @Override
                public String getPrefix() {
                    return module.getName();
                }

                @Override
                public SemVer getOpenconfigVersion() {
                    return module.getOpenconfigVersion();
                }

                @Override
                public String toString() {

                    return String.format("Module: %s, revision:%s", module.getName(), module.getRevision());
                }
            });
        }
        doReturn(mockedImports).when(importer).getImports();
    }

    //mock module with revision
    private static Module mockModule(final String name, final Date rev) {

        final Module mod = mockModule(name);

        doReturn(QNameModule.create(mod.getNamespace(), rev)).when(mod).getQNameModule();
        doReturn(rev).when(mod).getRevision();
        doReturn(mod.getQNameModule().toString()).when(mod).toString();

        return mod;
    }

    //mock module with default revision
    private static Module mockModule(final String mName) {

        Module mockedModule = mock(Module.class);
        doReturn(mName).when(mockedModule).getName();
        doReturn(revision).when(mockedModule).getRevision();
        final URI newNamespace = URI.create(namespace.toString() + ":" + mName);
        doReturn(newNamespace).when(mockedModule).getNamespace();
        doReturn(QNameModule.create(newNamespace, revision)).when(mockedModule).getQNameModule();
        doReturn(TEST_SOURCE).when(mockedModule).getSource();
        doReturn(Sets.newHashSet()).when(mockedModule).getSubmodules();
        doReturn(mockedModule.getQNameModule().toString()).when(mockedModule).toString();
        mockModuleImport(mockedModule);

        return mockedModule;
    }
}
