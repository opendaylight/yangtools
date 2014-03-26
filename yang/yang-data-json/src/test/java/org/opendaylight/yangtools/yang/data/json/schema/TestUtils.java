/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.data.impl.NodeFactory.createMutableCompositeNode;
import static org.opendaylight.yangtools.yang.data.impl.NodeFactory.createMutableSimpleNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    private static final String NAMESPACE_BASE = "simple:container:yang";
    private static final String NAMESPACE_AUGMENT = "augment:container:yang";
    private static Date revision_base;
    private static Date revision_augment;

    static {
        try {
            revision_base = new SimpleDateFormat("yyyy-MM-dd").parse("2013-11-12");
            revision_augment = new SimpleDateFormat("yyyy-MM-dd").parse("2014-03-19");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static Set<Module> loadModules(URI resourceURI) throws FileNotFoundException {
        final YangModelParser parser = new YangParserImpl();
        final File testDir = new File(resourceURI);
        final String[] fileList = testDir.list();
        final List<File> testFiles = new ArrayList<>();
        if (fileList == null) {
            throw new FileNotFoundException(resourceURI.toString());
        }
        for (String fileName : fileList) {
            testFiles.add(new File(testDir, fileName));
        }
        return parser.parseYangModels(testFiles);
    }

    public static Set<Module> loadModulesFrom(String yangPath) throws URISyntaxException {
        try {
            return loadModules(TestUtils.class.getResource(yangPath).toURI());
        } catch (FileNotFoundException e) {
            LOG.error("Yang files at path: " + yangPath + " weren't loaded.");
        }

        return null;
    }

    public static DataSchemaNode resolveDataSchemaNode(String searchedDataSchemaName, Module module) {
        assertNotNull("Module can't be null", module);

        if (searchedDataSchemaName != null) {
            for (DataSchemaNode dsn : module.getChildNodes()) {
                if (dsn.getQName().getLocalName().equals(searchedDataSchemaName)) {
                    return dsn;
                }
            }
        } else if (module.getChildNodes().size() == 1) {
            return module.getChildNodes().iterator().next();
        }
        return null;
    }

    public static Module resolveModule(String searchedModuleName, Set<Module> modules) {
        assertNotNull("Modules can't be null.", modules);
        if (searchedModuleName != null) {
            for (Module m : modules) {
                if (m.getName().equals(searchedModuleName)) {
                    return m;
                }
            }
        } else if (modules.size() == 1) {
            return modules.iterator().next();
        }
        return null;
    }

    /**
     * Prepare composite node structure according to
     * /cnsn-to-normalized-node/simple-conainer.json
     */
    public static CompositeNode prepareCompositeNodeStruct() {
        MutableCompositeNode cont = createMutableCompositeNode(new QName(URI.create(NAMESPACE_BASE), revision_base,
                "cont"), null, null, null, null);

        // cont1
        List<Node<?>> contChilds = new ArrayList<>();
        contChilds.add(createMutableCompositeNode(new QName(URI.create(NAMESPACE_BASE), revision_base, "cont1"), cont,
                Collections.<Node<?>> emptyList(), null, null));

        // cont2
        MutableCompositeNode cont2 = createMutableCompositeNode(new QName(URI.create(NAMESPACE_BASE), revision_base,
                "cont2"), cont, null, null, null);
        List<Node<?>> cont2Childs = new ArrayList<>();
        cont2Childs.add(createMutableSimpleNode(new QName(URI.create(NAMESPACE_BASE), revision_base, "lf21"), cont2,
                "value in cont2/lf21", null, null));
        cont2.setValue(cont2Childs);
        contChilds.add(cont2);

        // lst1
        contChilds.add(createMutableCompositeNode(new QName(URI.create(NAMESPACE_BASE), revision_base, "lst1"), cont,
                Collections.<Node<?>> emptyList(), null, null));

        // lst2
        MutableCompositeNode lst2_1 = createMutableCompositeNode(new QName(URI.create(NAMESPACE_BASE), revision_base,
                "lst2"), cont, null, null, null);
        List<Node<?>> lst2_1Childs = new ArrayList<>();
        lst2_1Childs.add(createMutableSimpleNode(new QName(URI.create(NAMESPACE_BASE), revision_base, "lf21"), lst2_1,
                "some value21", null, null));
        lst2_1.setValue(lst2_1Childs);
        contChilds.add(lst2_1);

        MutableCompositeNode lst2_2 = createMutableCompositeNode(new QName(URI.create(NAMESPACE_BASE), revision_base,
                "lst2"), cont, null, null, null);
        List<Node<?>> lst2_2Childs = new ArrayList<>();
        lst2_2Childs.add(createMutableSimpleNode(new QName(URI.create(NAMESPACE_BASE), revision_base, "lf22"), lst2_2,
                "some value22", null, null));
        lst2_2.setValue(lst2_2Childs);
        contChilds.add(lst2_2);

        // lflst1
        contChilds.add(createMutableSimpleNode(new QName(URI.create(NAMESPACE_BASE), revision_base, "lflst1"), cont,
                "lflst1_1", null, null));
        contChilds.add(createMutableSimpleNode(new QName(URI.create(NAMESPACE_BASE), revision_base, "lflst1"), cont,
                "lflst1_2", null, null));

        // lf1
        contChilds.add(createMutableSimpleNode(new QName(URI.create(NAMESPACE_BASE), revision_base, "lf1"), cont,
                "lf1", null, null));

        // lf11
        contChilds.add(createMutableSimpleNode(new QName(URI.create(NAMESPACE_BASE), revision_base, "lf11"), cont,
                "value from case (cs1)", null, null));

        // cont3
        MutableCompositeNode cont3 = createMutableCompositeNode(new QName(URI.create(NAMESPACE_AUGMENT),
                revision_augment, "cont3"), cont, null, null, null);
        List<Node<?>> cont3Childs = new ArrayList<>();
        cont3Childs.add(createMutableSimpleNode(new QName(URI.create(NAMESPACE_AUGMENT), revision_augment, "lf31"),
                cont3, "value in leaf in augment", null, null));
        cont3.setValue(cont3Childs);
        contChilds.add(cont3);

        cont.setValue(contChilds);
        return cont;
    }

    /**
     * Prepare composite node structure according to
     * /cnsn-to-normalized-node/simple-conainer.json
     */
    public static ContainerNode prepareNormalizedNodeStruct() throws URISyntaxException {
        DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder = Builders.containerBuilder();
        containerBuilder.withNodeIdentifier(getNodeIdentifier("cont"));
        containerBuilder.withChild(Builders.containerBuilder().withNodeIdentifier(getNodeIdentifier("cont1")).build());
        containerBuilder.withChild(Builders
                .containerBuilder()
                .withNodeIdentifier(getNodeIdentifier("cont2"))
                .withChild(
                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("lf21"))
                                .withValue("value in cont2/lf21").build()).build());

        CollectionNodeBuilder<MapEntryNode, MapNode> lst1 = Builders.mapBuilder().withNodeIdentifier(
                getNodeIdentifier("lst1"));
        lst1.withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(getNodeIdentifierPredicate("lst1", new HashMap<String, Object>()))
                .withValue(Collections.<DataContainerChild<? extends PathArgument, ?>> emptyList()).build());
        containerBuilder.withChild(lst1.build());

        CollectionNodeBuilder<MapEntryNode, MapNode> lst2 = Builders.mapBuilder().withNodeIdentifier(
                getNodeIdentifier("lst2"));

        Map<String, Object> lst2_1 = new HashMap<>();
        lst2_1.put("lf21", "some value21");

        List<DataContainerChild<? extends PathArgument, ?>> lst2_1_values = new ArrayList<>();
        lst2_1_values.add(Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("lf21"))
                .withValue("some value21").build());
        lst2.withChild(Builders.mapEntryBuilder().withNodeIdentifier(getNodeIdentifierPredicate("lst2", lst2_1))
                .withValue(lst2_1_values).build());

        Map<String, Object> lst2_2 = new HashMap<>();
        lst2_2.put("lf22", "some value22");
        List<DataContainerChild<? extends PathArgument, ?>> lst2_2_values = new ArrayList<>();
        lst2_2_values.add(Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("lf22"))
                .withValue("some value22").build());
        lst2.withChild(Builders.mapEntryBuilder().withNodeIdentifier(getNodeIdentifierPredicate("lst2", lst2_2))
                .withValue(lst2_2_values).build());
        containerBuilder.withChild(lst2.build());

        ListNodeBuilder<Object, LeafSetEntryNode<Object>> lflst1 = Builders.leafSetBuilder().withNodeIdentifier(
                getNodeIdentifier("lflst1"));
        lflst1.withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(getNodeIdentifier("lflst1", "lflst1_1"))
                .withValue("lflst1_1").build());
        lflst1.withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(getNodeIdentifier("lflst1", "lflst1_2"))
                .withValue("lflst1_2").build());
        containerBuilder.withChild(lflst1.build());

        containerBuilder.withChild(Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("lf1")).withValue("lf1")
                .build());

        containerBuilder.withChild(Builders
                .choiceBuilder()
                .withNodeIdentifier(getNodeIdentifier("chc"))
                .withChild(
                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("lf11"))
                                .withValue("value from case (cs1)").build()).build());

        Set<QName> children = new HashSet<>();
        children.add(new QName(new URI(NAMESPACE_AUGMENT), revision_augment, "cont3"));

        containerBuilder.withChild(Builders
                .augmentationBuilder()
                .withNodeIdentifier(getAugmentationIdentifier(null, null, null, children))
                .withChild(
                        Builders.containerBuilder()
                                .withNodeIdentifier(getNodeIdentifier("cont3", NAMESPACE_AUGMENT, revision_augment))
                                .withChild(
                                        Builders.leafBuilder()
                                                .withNodeIdentifier(
                                                        getNodeIdentifier("lf31", NAMESPACE_AUGMENT, revision_augment))
                                                .withValue("value in leaf in augment").build()).build()).build());

        ContainerNode build = containerBuilder.build();
        return build;
    }

    private static InstanceIdentifier.NodeIdentifier getNodeIdentifier(String localName) {
        return new InstanceIdentifier.NodeIdentifier(new QName(URI.create(NAMESPACE_BASE), revision_base, localName));
    }

    private static InstanceIdentifier.NodeIdentifier getNodeIdentifier(String localName, String namespace, Date revision) {
        return new InstanceIdentifier.NodeIdentifier(new QName(URI.create(namespace), revision, localName));
    }

    private static InstanceIdentifier.NodeWithValue getNodeIdentifier(String localName, Object value) {
        return new InstanceIdentifier.NodeWithValue(new QName(URI.create(NAMESPACE_BASE), revision_base, localName),
                value);
    }

    private static InstanceIdentifier.NodeIdentifierWithPredicates getNodeIdentifierPredicate(String localName,
            Map<String, Object> keys) {
        Map<QName, Object> predicate = new HashMap<>();
        for (String key : keys.keySet()) {
            predicate.put(new QName(URI.create(NAMESPACE_BASE), revision_base, key), keys.get(key));
        }

        return new InstanceIdentifier.NodeIdentifierWithPredicates(

        new QName(URI.create(NAMESPACE_BASE), revision_base, localName), predicate);
    }

    private static InstanceIdentifier.AugmentationIdentifier getAugmentationIdentifier(String localName,
            String namespace, Date revision, Set<QName> children) {
        return new InstanceIdentifier.AugmentationIdentifier(null, children);
    }

}
