package org.opendaylight.yangtools.yang.data.json.schema;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.opendaylight.controller.sal.restconf.impl.CompositeNodeWrapper;
import org.opendaylight.controller.sal.restconf.impl.NodeWrapper;
import org.opendaylight.controller.sal.restconf.impl.test.TestUtils;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTestUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(JsonTestUtils.class);    
    
    private final static YangModelParser parser = new YangParserImpl();

    private static Set<Module> loadModules(String resourceDirectory) throws FileNotFoundException {
        final File testDir = new File(resourceDirectory);
        final String[] fileList = testDir.list();
        final List<File> testFiles = new ArrayList<File>();
        if (fileList == null) {
            throw new FileNotFoundException(resourceDirectory);
        }
        for (int i = 0; i < fileList.length; i++) {
            String fileName = fileList[i];
            if (new File(testDir, fileName).isDirectory() == false) {
                testFiles.add(new File(testDir, fileName));
            }
        }
        return parser.parseYangModels(testFiles);
    }

    public static Set<Module> loadModulesFrom(String yangPath) {
        try {
            return loadModules(JsonTestUtils.class.getResource(yangPath).getPath());
        } catch (FileNotFoundException e) {
            LOG.error("Yang files at path: " + yangPath + " weren't loaded.");
        }

        return null;
    }
    
    public static SchemaContext loadSchemaContext(Set<Module> modules) {
        return parser.resolveSchemaContext(modules);
    }

    public static SchemaContext loadSchemaContext(String resourceDirectory) throws FileNotFoundException {
        return parser.resolveSchemaContext(loadModulesFrom(resourceDirectory));
    }

    public static List<JsonNode> toList(Iterator<JsonNode> elements) {

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
    
    public static void addDummyNamespaceToAllNodes(NodeWrapper<?> wrappedNode,String namespace) throws URISyntaxException {
        wrappedNode.setNamespace(new URI(namespace));
        if (wrappedNode instanceof CompositeNodeWrapper) {
            for (NodeWrapper<?> childNodeWrapper : ((CompositeNodeWrapper) wrappedNode).getValues()) {
                addDummyNamespaceToAllNodes(childNodeWrapper,namespace);
            }
        }
    }    
    

}
