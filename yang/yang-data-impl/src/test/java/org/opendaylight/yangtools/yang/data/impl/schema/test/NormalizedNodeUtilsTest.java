package org.opendaylight.yangtools.yang.data.impl.schema.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableNodes.mapNodeBuilder;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.TreeUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

import com.google.common.base.Optional;

/**
 *
 * Schema structure of document is
 *
 * <pre>
 * container root { 
 *      list list-a {
 *              key leaf-a;
 *              leaf leaf-a;
 *              choice choice-a {
 *                      case one {
 *                              leaf one;
 *                      }
 *                      case two-three {
 *                              leaf two;
 *                              leaf three;
 *                      }
 *              }
 *              list list-b {
 *                      key leaf-b;
 *                      leaf leaf-b;
 *              }
 *      }
 * }
 * </pre>
 *
 */
public class TreeUtilsTest {

    private static final QName ROOT_QNAME = QName.create("urn:opendalight:controller:sal:dom:store:test", "2014-03-13",
            "root");
    private static final QName LIST_A_QNAME = QName.create(ROOT_QNAME, "list-a");
    private static final QName LIST_B_QNAME = QName.create(ROOT_QNAME, "list-b");
    private static final QName CHOICE_A_QNAME = QName.create(ROOT_QNAME, "choice-a");
    private static final QName LEAF_A_QNAME = QName.create(ROOT_QNAME, "leaf-a");
    private static final QName LEAF_B_QNAME = QName.create(ROOT_QNAME, "leaf-b");
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String ONE = "one";
    private static final String TWO = "two";

    private static final InstanceIdentifier LIST_A_FOO_PATH = InstanceIdentifier.builder()
                //.node(ROOT_QNAME)
                .node(LIST_A_QNAME)
                .nodeWithKey(LIST_A_QNAME, LEAF_A_QNAME, FOO)
                .build();
    private static final InstanceIdentifier LIST_B_TWO_PATH = InstanceIdentifier.builder()
                //.node(ROOT_QNAME)
                .node(LIST_A_QNAME)
                .nodeWithKey(LIST_A_QNAME, LEAF_A_QNAME, BAR)
                .node(LIST_B_QNAME)
                .nodeWithKey(LIST_B_QNAME,LEAF_B_QNAME,TWO)
                .build();

    /**
     * Returns a test document
     *
     * <pre>
     * root
     *     list-a
     *          leaf-a "foo"
     *     list-a
     *          leaf-a "bar"
     *          list-b
     *                  leaf-b "one"
     *          list-b
     *                  leaf-b "two"
     *
     * </pre>
     *
     * @return
     */
    public NormalizedNode<?, ?> createDocumentOne() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
                .withChild(
                        mapNodeBuilder(LIST_A_QNAME)
                                .withChild(mapEntry(LIST_A_QNAME, LEAF_A_QNAME, FOO))
                                .withChild(
                                        mapEntryBuilder(LIST_A_QNAME, LEAF_A_QNAME, BAR).withChild(
                                                mapNodeBuilder(LIST_B_QNAME)
                                                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, ONE))
                                                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, TWO)).build())
                                                .build()).build()).build();

    }

    @Test
    public void findNodeTest() {
        NormalizedNode<?, ?> tree = createDocumentOne();
        assertNotNull(tree);

        Optional<NormalizedNode<?, ?>> listFooResult = TreeUtils.findNode(tree, LIST_A_FOO_PATH);
        assertTrue(listFooResult.isPresent());

        Optional<NormalizedNode<?, ?>> listTwoResult = TreeUtils.findNode(tree, LIST_B_TWO_PATH);
        assertTrue(listTwoResult.isPresent());
    }

}
