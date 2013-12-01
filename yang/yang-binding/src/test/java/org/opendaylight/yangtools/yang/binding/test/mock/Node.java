package org.opendaylight.yangtools.yang.binding.test.mock;

import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;

public interface Node extends //
    DataObject, //
    Identifiable<NodeKey>, //
    ChildOf<Nodes> {

}
