package org.opendaylight.yangtools.yang.binding.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.test.mock.FooChild;
import org.opendaylight.yangtools.yang.binding.test.mock.InstantiatedFoo;
import org.opendaylight.yangtools.yang.binding.test.mock.Node;
import org.opendaylight.yangtools.yang.binding.test.mock.NodeChild;
import org.opendaylight.yangtools.yang.binding.test.mock.NodeChildKey;
import org.opendaylight.yangtools.yang.binding.test.mock.NodeKey;
import org.opendaylight.yangtools.yang.binding.test.mock.Nodes;

public class InstanceIdentifierTest {

    @Test
    public void constructWithPredicates() {
        
        InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).toInstance();
        
        assertNotNull(nodes);
        assertEquals(Nodes.class, nodes.getTargetType());
        
        
        InstanceIdentifier<Node> node = InstanceIdentifier.builder(nodes).node(Node.class).toInstance();
        
        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());
        
        assertTrue(nodes.contains(node));
    }

    @Test
    public void fluentConstruction() {

        InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).toInstance();
        InstanceIdentifier<Node> node = InstanceIdentifier.builder(Nodes.class).child(Node.class,new NodeKey(10)).toInstance();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }
   
    
    @Test
    public void negativeContains() {
        InstanceIdentifier<FooChild> fooChild = InstanceIdentifier.builder(Nodes.class).child(InstantiatedFoo.class).child(FooChild.class).build();
        
        InstanceIdentifier<Node> nodeTen = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).toInstance();
        InstanceIdentifier<Node> nodeOne = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).toInstance();
        InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).toInstance();
    
        assertFalse(fooChild.contains(nodeTen));
        assertFalse(nodeTen.contains(nodes));
        
        assertFalse(nodeOne.contains(nodes));
        assertTrue(nodes.contains(nodeOne));
    }
    
    @Test
    public void containsWildcarded() {
        InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).toInstance();
        InstanceIdentifier<Node> wildcarded = InstanceIdentifier.builder(Nodes.class).child(Node.class).build();
        InstanceIdentifier<NodeChild> wildcardedChildren = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class) //
                .child(NodeChild.class).build();
        
        assertTrue(wildcarded.isWildcarded());
        assertTrue(wildcardedChildren.isWildcarded());
        
        
        InstanceIdentifier<Node> nodeTen = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).toInstance();
        InstanceIdentifier<Node> nodeOne = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).toInstance();
        
        assertFalse(nodeTen.isWildcarded());
        assertFalse(nodeOne.isWildcarded());
        assertTrue(nodes.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeTen));
        
        
        InstanceIdentifier<NodeChild> nodeTenChildWildcarded = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).child(NodeChild.class).toInstance();
        
        assertTrue(nodeTenChildWildcarded.isWildcarded());
        
        InstanceIdentifier<NodeChild> nodeTenChild = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).child(NodeChild.class, new NodeChildKey(10)).toInstance();
        InstanceIdentifier<NodeChild> nodeOneChild = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).child(NodeChild.class, new NodeChildKey(1)).toInstance();

        
        assertFalse(nodeTenChildWildcarded.containsWildcarded(nodeOneChild));
        assertTrue(nodeTenChildWildcarded.containsWildcarded(nodeTenChild));
        
    }
    
    
    void childOfTest() {
        InstanceIdentifier.builder(Nodes.class).child(InstantiatedFoo.class).child(FooChild.class);
    }

}
