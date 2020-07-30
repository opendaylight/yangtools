package org.opendaylight.yangtools.yang.model.util.ut;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;


public class YT1127Test {
    EffectiveModelContext context;
    Module module;
    QNameModule moduleName;


    @Before
    public void before() {
        context = YangParserTestUtils.parseYangResourceDirectory("/yt1127");
        module = context.findModule("a").get();
        moduleName = module.getQNameModule();
    }

    @Test
    public void testGroupingLeafRef() {
        final GroupingDefinition fooGrp = module.getGroupings().iterator().next();
        final LeafSchemaNode keyLeaf1 =
                (LeafSchemaNode) fooGrp.findDataChildByName(QName.create(moduleName, "key_leaf1")).get();

        final SchemaNode keyLeaf1Target =
                SchemaContextUtil.findDataSchemaNodeForRelativeXPath(context, module, keyLeaf1,
                ((LeafrefTypeDefinition) keyLeaf1.getType()).getPathStatement());

        assertNull(keyLeaf1Target);
    }

    @Test
    public void testContainerLeafRef() {
        final ContainerSchemaNode barCont =
                (ContainerSchemaNode) module.findDataChildByName(QName.create(moduleName, "bar_cont")).get();
        final LeafSchemaNode leaf2 =
                (LeafSchemaNode) barCont.findDataChildByName(QName.create(moduleName, "leaf2")).get();

        assertThrows(IllegalArgumentException.class, () ->
                SchemaContextUtil.findDataSchemaNodeForRelativeXPath(context, module, leaf2,
                ((LeafrefTypeDefinition) leaf2.getType()).getPathStatement()));
    }
}
