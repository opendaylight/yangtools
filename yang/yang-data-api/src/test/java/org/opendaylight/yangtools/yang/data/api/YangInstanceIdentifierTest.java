/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Unit tests for InstanceIdentifier.
 *
 * @author Thomas Pantelis
 */
public class YangInstanceIdentifierTest {

    static QName nodeName1 = QName.create("test", "2014-5-28", "node1");
    static QName nodeName2 = QName.create("test", "2014-5-28", "node2");
    static QName nodeName3 = QName.create("test", "2014-5-28", "node3");
    static QName nodeName4 = QName.create("test", "2014-5-28", "node4");
    static QName key1 = QName.create("test", "2014-5-28", "key1");
    static QName key2 = QName.create("test", "2014-5-28", "key2");
    static QName key3 = QName.create("test", "2014-5-28", "key3");

    @Test
    public void testGetLastPathArgument() {
    	YangInstanceIdentifier id1 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));
    	assertEquals( "getLastPathArgument", new NodeIdentifier(nodeName2), id1.getLastPathArgument());
    	YangInstanceIdentifier id2 = YangInstanceIdentifier.create();
    	assertNull( id2.getLastPathArgument() );
    }

    @Test
    public void testHashCodeEquals() {

        YangInstanceIdentifier id1 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));
        YangInstanceIdentifier id2 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));

        assertEquals( "hashCode", id1.hashCode(), id2.hashCode() );
    }

    @Test
    public void testEquals() {

        YangInstanceIdentifier id1 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));
        YangInstanceIdentifier id2 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));
        YangInstanceIdentifier id3 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName2), new NodeIdentifier(nodeName1));
        YangInstanceIdentifier id4 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1));

        assertEquals( "equals", false, id1.equals( null ) );
        assertEquals( "equals", true, id1.equals( id1 ) );
        assertEquals( "equals", true, id1.equals( id2 ) );
        assertEquals( "equals", false, id1.equals( id3 ) );
        assertEquals( "equals", false, id1.equals( id4 ) );
        assertEquals( "equals", false, id1.equals( new Object() ) );
    }

    @Test
    public void testToString() {
    	YangInstanceIdentifier id = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));

    	assertNotNull( id.toString() );

    }

    @Test
    public void testNode() {

        YangInstanceIdentifier id = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));

        YangInstanceIdentifier newID = id.node( nodeName3 );

        assertNotNull( "InstanceIdentifier is null", newID );
        assertEquals( "Path size", 3, Iterables.size(newID.getPathArguments()) );

        Iterator<PathArgument> it = newID.getPathArguments().iterator();
        assertEquals( "PathArg 1 node type", nodeName1, it.next().getNodeType() );
        assertEquals( "PathArg 2 node type", nodeName2, it.next().getNodeType() );
        assertEquals( "PathArg 3 node type", nodeName3, it.next().getNodeType() );

        newID = id.node( new NodeIdentifier( nodeName3 ) );

        assertNotNull( "InstanceIdentifier is null", newID );
        assertEquals( "Path size", 3, Iterables.size(newID.getPathArguments()) );

        it = newID.getPathArguments().iterator();
        assertEquals( "PathArg 1 node type", nodeName1, it.next().getNodeType() );
        assertEquals( "PathArg 2 node type", nodeName2, it.next().getNodeType() );
        assertEquals( "PathArg 3 node type", nodeName3, it.next().getNodeType() );
    }

    @Test
    public void testRelativeTo() {

        YangInstanceIdentifier id1 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2),
                new NodeIdentifier(nodeName3), new NodeIdentifier(nodeName4));
        YangInstanceIdentifier id2 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));
        YangInstanceIdentifier id3 = YangInstanceIdentifier.create(
                Lists.newArrayList(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2)));

        Optional<YangInstanceIdentifier> relative = id1.relativeTo( id2 );
        assertEquals( "isPresent", true, relative.isPresent() );

        Iterable<PathArgument> p = relative.get().getPathArguments();
        assertEquals( "Path size", 2, Iterables.size(p) );
        assertEquals( "PathArg 1 node type", nodeName3, Iterables.get(p, 0).getNodeType() );
        assertEquals( "PathArg 2 node type", nodeName4, Iterables.get(p, 1).getNodeType() );

        relative = id2.relativeTo( id3 );
        assertEquals( "isPresent", true, relative.isPresent() );
        assertEquals( "Path size", 0, Iterables.size(relative.get().getPathArguments()) );

        relative = id2.relativeTo( id1 );
        assertEquals( "isPresent", false, relative.isPresent() );
    }

    @Test(expected=IllegalArgumentException.class)
    public void testContainsNull() {
        final YangInstanceIdentifier id = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1));

        id.contains( null );
    }

    @Test
    public void testContains() {

        YangInstanceIdentifier id1 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2),
                new NodeIdentifier(nodeName3), new NodeIdentifier(nodeName4));
        YangInstanceIdentifier id2 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));
        YangInstanceIdentifier id3 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));
        YangInstanceIdentifier id4 = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName3));

        assertEquals( "contains", true, id2.contains( id1 ) );
        assertEquals( "contains", true, id2.contains( id3 ) );
        assertEquals( "contains", false, id1.contains( id2 ) );
        assertEquals( "contains", false, id2.contains( id4 ) );
    }

    @Test
    public void testOf() {

        YangInstanceIdentifier newID = YangInstanceIdentifier.of( nodeName1 );

        assertNotNull( "InstanceIdentifier is null", newID );
        assertEquals( "Path size", 1, Iterables.size(newID.getPathArguments()) );
        assertEquals( "PathArg 1 node type", nodeName1, Iterables.get(newID.getPathArguments(), 0).getNodeType() );

        assertNotNull( newID.toString() ); // for code coverage
    }

    @Test
    public void testBuilder() {

        YangInstanceIdentifier newID = YangInstanceIdentifier.builder()
                .node( nodeName1 )
                .nodeWithKey( nodeName2, Collections.<QName,Object>singletonMap( key1, "foo" ) )
                .nodeWithKey( nodeName3, key2, "bar" ).build();

        assertNotNull( "InstanceIdentifier is null", newID );
        assertEquals( "Path size", 3, Iterables.size(newID.getPathArguments()) );

        Iterator<PathArgument> it = newID.getPathArguments().iterator();
        assertEquals( "PathArg 1 node type", nodeName1, it.next().getNodeType() );
        verifyNodeIdentifierWithPredicates( "PathArg 2", it.next(), nodeName2, key1, "foo" );
        verifyNodeIdentifierWithPredicates( "PathArg 3", it.next(), nodeName3, key2, "bar" );

        newID = YangInstanceIdentifier.builder( newID ).node( nodeName4 ).build();

        assertNotNull( "InstanceIdentifier is null", newID );
        assertEquals( "Path size", 4, Iterables.size(newID.getPathArguments()) );

        it = newID.getPathArguments().iterator();
        assertEquals( "PathArg 1 node type", nodeName1, it.next().getNodeType() );
        assertEquals( "PathArg 2 node type", nodeName2, it.next().getNodeType() );
        assertEquals( "PathArg 3 node type", nodeName3, it.next().getNodeType() );
        assertEquals( "PathArg 4 node type", nodeName4, it.next().getNodeType() );

        newID = YangInstanceIdentifier.builder().node( nodeName1 ).build();

        assertNotNull( "InstanceIdentifier is null", newID );
        assertEquals( "Path size", 1, Iterables.size(newID.getPathArguments()) );
        assertEquals( "PathArg 1 node type", nodeName1, Iterables.get(newID.getPathArguments(), 0).getNodeType() );
    }

    private void verifyNodeIdentifierWithPredicates(final String prefix,
            final PathArgument arg, final QName nodeName, final QName key, final Object value ) {

        assertNotNull( prefix + " is null", arg );
        assertEquals( prefix + " class", NodeIdentifierWithPredicates.class, arg.getClass() );
        NodeIdentifierWithPredicates node = (NodeIdentifierWithPredicates)arg;
        assertEquals( prefix + " node type", nodeName, node.getNodeType() );
        assertEquals( prefix + " key values map size", 1, node.getKeyValues().size() );
        Entry<QName, Object> entry = node.getKeyValues().entrySet().iterator().next();
        assertEquals( prefix + " key values map entry key", key, entry.getKey() );
        assertEquals( prefix + " key values map entry value", value, entry.getValue() );
    }

    @Test
    public void testNodeIdentifierWithPredicates() {

        NodeIdentifierWithPredicates node1 = new NodeIdentifierWithPredicates( nodeName1, key1, "foo" );
        verifyNodeIdentifierWithPredicates( "NodeIdentifierWithPredicates", node1, nodeName1, key1, "foo" );

        NodeIdentifierWithPredicates node2 = new NodeIdentifierWithPredicates( nodeName1, key1, "foo" );

        assertEquals( "hashCode", node1.hashCode(), node2.hashCode() );
        assertEquals( "equals", true, node1.equals( node2 ) );

        assertEquals( "equals", false,
                node1.equals( new NodeIdentifierWithPredicates( nodeName2, key1, "foo" ) ) );
        assertEquals( "equals", false,
                node1.equals( new NodeIdentifierWithPredicates( nodeName1, key2, "foo" ) ) );
        assertEquals( "equals", false,
                node1.equals( new NodeIdentifierWithPredicates( nodeName1, key1, "bar" ) ) );
        assertEquals( "equals", false, node1.equals( new Object() ) );

        assertNotNull( node1.toString() ); // for code coverage
        assertNotNull( node1.toRelativeString(node2) );

        NodeIdentifierWithPredicates node3 = new NodeIdentifierWithPredicates( nodeName1,
                ImmutableMap.<QName, Object>builder().put( key1, 10 ).put( key2, 20 ).build() );

        NodeIdentifierWithPredicates node4 = new NodeIdentifierWithPredicates( nodeName1,
                ImmutableMap.<QName, Object>builder().put( key1, 10 ).put( key2, 20 ).build() );

        assertEquals( "hashCode", node3.hashCode(), node4.hashCode() );
        assertEquals( "equals", true, node3.equals( node4 ) );

        assertEquals( "equals", false, node3.equals( node1 ) );
        assertEquals( "equals", false,
                node1.equals( new NodeIdentifierWithPredicates( nodeName1,
                        ImmutableMap.<QName, Object>builder().put( key1, 10 ).put( key3, 20 ).build() ) ) );

        node1 = new NodeIdentifierWithPredicates( nodeName1, key1, new byte[]{1,2} );
        node2 = new NodeIdentifierWithPredicates( nodeName1, key1, new byte[]{1,2} );

        assertEquals( "hashCode", node1.hashCode(), node2.hashCode() );
        assertEquals( "equals", true, node1.equals( node2 ) );

        assertEquals( "equals", false,
                node1.equals( new NodeIdentifierWithPredicates( nodeName1, key1, new byte[]{1,3} ) ) );
        assertEquals( "equals", false,
                node1.equals( new NodeIdentifierWithPredicates( nodeName1, key1, new byte[]{1} ) ) );
        assertEquals( "equals", false,
                node1.equals( new NodeIdentifierWithPredicates( nodeName1, key1, new byte[]{1,2,3} ) ) );
    }

    @Test
    public void testNodeWithValue() {

        NodeWithValue node1 = new NodeWithValue( nodeName1, "foo" );
        assertEquals( "getNodeType", nodeName1, node1.getNodeType() );
        assertEquals( "getValue", "foo", node1.getValue() );

        NodeWithValue node2 = new NodeWithValue( nodeName1, "foo" );

        assertEquals( "hashCode", node1.hashCode(), node2.hashCode() );
        assertEquals( "equals", true, node1.equals( node2 ) );

        assertEquals( "equals", false, node1.equals( new NodeWithValue( nodeName1, "bar" ) ) );
        assertEquals( "equals", false, node1.equals( new NodeWithValue( nodeName2, "foo" ) ) );
        assertEquals( "equals", false, node1.equals( new Object() ) );

        assertNotNull( node1.toString() ); // for code coverage
        assertNotNull( node1.toRelativeString(node2) );

        NodeWithValue node3 = new NodeWithValue( nodeName1, new byte[]{1,2} );
        NodeWithValue node4 = new NodeWithValue( nodeName1, new byte[]{1,2} );

        assertEquals( "hashCode", node3.hashCode(), node4.hashCode() );
        assertEquals( "equals", true, node3.equals( node4 ) );

        assertEquals( "equals", false, node3.equals( new NodeWithValue( nodeName1, new byte[]{1,3} ) ) );
        assertEquals( "equals", false, node3.equals( node1 ) );
    }

    @Test
    public void testNodeIdentifier() {

        NodeIdentifier node1 = new NodeIdentifier( nodeName1 );
        assertEquals( "getNodeType", nodeName1, node1.getNodeType() );
        NodeIdentifier node2 = new NodeIdentifier( nodeName1 );
        AugmentationIdentifier node3 = new AugmentationIdentifier( Sets.newHashSet( nodeName1, nodeName2 ) );

        assertEquals( "hashCode", node1.hashCode(), node2.hashCode() );
        assertEquals( "compareTo", 0, node1.compareTo( node2 ) );
        assertEquals( "compareTo", true, node1.compareTo( new NodeIdentifier( nodeName3 ) ) != 0 );

        assertEquals( "equals", false, node1.equals( null ) );
        assertEquals( "equals", false, node1.equals( node3 ) );
        assertEquals( "equals", true, node1.equals( node1 ) );
        assertEquals( "equals", true, node1.equals( node2 ) );
        assertEquals( "equals", false, node1.equals( new NodeIdentifier( nodeName3 ) ) );
        assertEquals( "equals", false, node1.equals( new Object() ) );

        assertNotNull( node1.toString() ); // for code coverage
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAugmentationIdentifierNodeType() {
        AugmentationIdentifier node1 = new AugmentationIdentifier( Sets.newHashSet( nodeName1, nodeName2 ) );
        node1.getNodeType();
    }

    @Test
    public void testAugmentationIdentifier() {

        AugmentationIdentifier node1 = new AugmentationIdentifier( Sets.newHashSet( nodeName1, nodeName2 ) );
        assertEquals( "getPossibleChildNames", Sets.newHashSet( nodeName1, nodeName2 ), node1.getPossibleChildNames() );
        AugmentationIdentifier node2 = new AugmentationIdentifier( Sets.newHashSet( nodeName2, nodeName1 ) );
        AugmentationIdentifier node3 = new AugmentationIdentifier( Sets.newHashSet( nodeName1, nodeName3 ) );
        AugmentationIdentifier node4 = new AugmentationIdentifier( Sets.newHashSet( nodeName1, nodeName2, nodeName3 ) );
        NodeIdentifier node5 = new NodeIdentifier( nodeName3 );

        assertEquals( "hashCode", node1.hashCode(), node2.hashCode() );

        assertEquals( "equals", true, node1.equals( node1 ) );
        assertEquals( "equals", true, node1.equals( node2 ) );
        assertEquals( "equals", false, node1.equals( node3 ) );
        assertEquals( "equals", false, node1.equals( new AugmentationIdentifier( Sets.newHashSet( nodeName1 ) ) ) );
        assertEquals( "equals", false, node1.equals( new Object() ) );

        assertEquals( "compareTo", -1, node1.compareTo( node5 ) );
        assertNotEquals( "compareTo", -1, node1.compareTo( node2 ) );
        assertEquals( "compareTo", 0, node1.compareTo( node2 ) );
        assertEquals( "compareTo", 1, node1.compareTo( node4 ) );
        assertEquals( "compareTo", -1, node4.compareTo( node1 ) );

        assertNotNull( node1.toString() ); // for code coverage
        assertNotNull( node1.toRelativeString(node5));
    }

    @Test
    public void serializationTest() throws IOException, ClassNotFoundException {
        final YangInstanceIdentifier expected = YangInstanceIdentifier.create(new NodeIdentifier(nodeName1), new NodeIdentifier(nodeName2));

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(expected);
        oos.close();

        final byte[] bytes = bos.toByteArray();
        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(bis);

        final YangInstanceIdentifier read = (YangInstanceIdentifier) ois.readObject();
        assertEquals(0, ois.available());
        ois.close();

        assertEquals(expected, read);
    }
}
