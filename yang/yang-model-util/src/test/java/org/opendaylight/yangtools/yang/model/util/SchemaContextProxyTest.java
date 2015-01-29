/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.html
 */

package org.opendaylight.yangtools.yang.model.util;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.FilteringSchemaContextProxy.ModuleId;

public class SchemaContextProxyTest {

    private Map<org.opendaylight.yangtools.yang.model.api.ModuleIdentifier, String> sources;

    private URI namespace;
    private Date revision;
    private Date revision2;

    private static final String CONFIG_NAME = "config";
    private static final String ROOT_NAME = "root";
    private static final String MODULE2_NAME = "module2";
    private static final String MODULE3_NAME = "module3";
    private static final String MODULE4_NAME = "module4";
    private static final String MODULE41_NAME = "module41";
    private static final String MODULE5_NAME = "module5";
    private List<ModuleId> ModulesIdList = new ArrayList<>();

    ModuleId rm;

    Set<ModuleImport> mi = Sets.newHashSet();

    @Before
    public void setUp() throws ParseException, URISyntaxException {
        MockitoAnnotations.initMocks(this);

        namespace = new URI("urn:opendaylight:params:xml:ns:yang:controller:config");

        revision = SimpleDateFormatUtil.getRevisionFormat().parse("2015-01-01");
        revision2 = SimpleDateFormatUtil.getRevisionFormat().parse("2015-01-15");

        rm = new ModuleId(CONFIG_NAME, revision);

        ModulesIdList.add(rm);

        mockSchema();

        sources = Collections.emptyMap();
    }

    private SchemaContext mockSchema(Module... module) {
        SchemaContext mock = mock(SchemaContext.class);
        doReturn(Sets.newHashSet(module)).when(mock).getModules();
        return mock;
    }

    /**
     * <pre>
     * CFG(R)
     *  | \
     *  |  \
     * M2 <- M3
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
     *     CFG(R)
     *    |      \
     *   |         \
     * M2<-(NullRev)M3
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
     * M2 <- M3
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
     * M2 -> M3 -> M4 -> M5
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

        //Module moduleByName = filteringSchemaContextProxy.findModuleByName(MODULE5_NAME, revision);

    }

    /**
     * <pre>
     * CFG(R)
     *  |
     *  |
     * M2 -> M3 <- M4
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
     * M2 <- M3 M4=M3(Different revision)
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
     * M2 -(no revision)-> M3(R2) ... M3(R1)
     * </pre>
     */
    @Test
    public void testImportNoRevision() throws Exception {

        Module moduleConfig = mockModule(CONFIG_NAME, revision);
        Module module2 = mockModule(MODULE2_NAME, revision);

        //doReturn(null).when(module2).getRevision();
        //default revision1
        Module module3  = mockModule(MODULE3_NAME, null);
        Module module30 = mockModule(MODULE3_NAME, revision);
        Module module31 = mockModule(MODULE3_NAME, revision2);
        mockModuleImport(module2, moduleConfig, module3);

        SchemaContext schemaContext = mockSchema(moduleConfig, module2, module30, module31);

        //System.err.println("mod2:"+module2.getRevision());
        //System.err.println("mod4:"+module31.getRevision());

        FilteringSchemaContextProxy filteringSchemaContextProxy = createProxySchemaCtx(schemaContext, null, moduleConfig);

        //System.err.println("mod2:"+module2.getRevision());
        //System.err.println("mod4:"+module31.getRevision());

        assertProxyContext(filteringSchemaContextProxy, moduleConfig, module2, module31);

    }

    /**
     * <pre>
     * CFG(R)
     * |   \
     * |    \
     * |    M2 -> M3
     * |
     * M41(S) => M4
     * </pre>
     */
    @Test
    public void testBasicSubmodule() throws Exception {

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
     * M2 -> M3 -> M4 -> M5
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
     * M3 -> M4
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

    private void assertProxyContext(FilteringSchemaContextProxy filteringSchemaContextProxy, Module... expected) {

        Set<Module> modSet = Sets.newHashSet(expected);
        Set<Module> modSetFiltering = filteringSchemaContextProxy.getModules();

        /* blbost na vypisanie
        for(Module mods : modSet){
            System.err.println(mods.getName());
            System.err.println(mods.getRevision());
        }

        for(Module modsF : modSetFiltering){
            System.err.println(modsF.getName());
            System.err.println(modsF.getRevision());
        }
        */
        assertEquals(modSet, modSetFiltering);
        for (Module module : expected) {
            assertEquals(module, filteringSchemaContextProxy.findModuleByName(module.getName(), module.getRevision()));
            Set<Module> mod = filteringSchemaContextProxy.findModuleByNamespace(module.getNamespace());
            for(Module md : mod){
                assertEquals(module, md);
            }
        }
//        for (Module module : expected) {
//            assertEquals("", filteringSchemaContextProxy.getModuleSource(new ModuleIdentifier() {
//                @Override
//                public QNameModule getQNameModule() {
//                    return null;
//                }
//
//                @Override
//                public String getName() {
//                    return null;
//                }
//
//                @Override
//                public URI getNamespace() {
//                    return null;
//                }
//
//                @Override
//                public Date getRevision() {
//                    return null;
//                }
//            }));
//        }
    }

    /*
    private FilteringSchemaContextProxy createProxySchemaCtx(SchemaContext schemaContext, Module... roots) {
        List<ModuleId> ModulesIds = Lists.newArrayList();
        for (Module root : roots) {
            rootModules.add(new ModuleId(root.getName(), revision));
        }

        return new FilteringSchemaContextProxy(schemaContext, rootModules);
    }
    */

    private FilteringSchemaContextProxy createProxySchemaCtx(SchemaContext schemaContext, Set<Module> additionalModules, Module... modules) {

        /*
        List<ModuleId> moduleIds = Lists.newArrayList();
        List<ModuleId> additionalModuleIds = Lists.newArrayList();


        if(modules!=null && modules.length>0) {

            for (Module module : modules) {

                moduleIds.add(new ModuleId(module.getName(), module.getRevision()));
            }
        }

        if(additionalModules!=null&&additionalModules.size()>0)
            for (Module moduleAdd : additionalModules) {

            additionalModuleIds.add(new ModuleId(moduleAdd.getName(), moduleAdd.getRevision()));
        }

        return new FilteringSchemaContextProxy(schemaContext, moduleIds, additionalModuleIds);
        */

        Set<Module> modulesSet = new HashSet();

        if(modules!=null) {

            modulesSet = Sets.newHashSet(modules);

        }
        //return new FilteringSchemaContextProxy(schemaContext, null , createModuleIds(additionalModules));
        return new FilteringSchemaContextProxy(schemaContext, createModuleIds(modulesSet) , createModuleIds(additionalModules));
    }

    private List<ModuleId> createModuleIds(Set<Module> modules) {

        List<ModuleId> moduleIds = Lists.newArrayList();

        if(modules!=null && modules.size()>0) {



            for (Module module : modules) {

                moduleIds.add(new ModuleId(module.getName(), module.getRevision()));
            }
        }

        return moduleIds;
    }


    private void mockSubmodules(Module mainModule, Module... submodules){

        Set<Module> submodulesSet = new HashSet<>();
        submodulesSet.addAll(Arrays.asList(submodules));

        doReturn(submodulesSet).when(mainModule).getSubmodules();

    }

    private void mockModuleImport(Module importer, Module... imports) {
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
                public String toString() {

                    return String.format("Module: %s, revision:%s", module.getName(), module.getRevision());
                }
            });
        }
        doReturn(mockedImports).when(importer).getImports();
    }

    //mock module with revision
    private Module mockModule(String name, final Date rev){

        final Module mod = mockModule(name);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return QNameModule.create(mod.getNamespace(), rev);
            }
        }).when(mod).getQNameModule();
        doReturn(rev).when(mod).getRevision();

        return mod;

    }

    //mock module with default revision
    private Module mockModule(String mName) {
        Module mockedModule = mock(Module.class);
        doReturn(mName).when(mockedModule).getName();
        doReturn(revision).when(mockedModule).getRevision();
        final URI newNamespace = URI.create(namespace.toString() + ":" + mName);
        doReturn(newNamespace).when(mockedModule).getNamespace();


        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return QNameModule.create(newNamespace, revision);
            }
        }).when(mockedModule).getQNameModule();

        doReturn("").when(mockedModule).getSource();
        doReturn(Sets.newHashSet()).when(mockedModule).getSubmodules();
        doReturn(mockedModule.getQNameModule().toString()).when(mockedModule).toString();
        mockModuleImport(mockedModule);

        return mockedModule;
    }

}
