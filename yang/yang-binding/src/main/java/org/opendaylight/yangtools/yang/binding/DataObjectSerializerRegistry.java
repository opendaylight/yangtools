package org.opendaylight.yangtools.yang.binding;


public interface DataObjectSerializerRegistry {

    DataObjectSerializer getSerializer(Class<? extends DataObject> binding);

}
