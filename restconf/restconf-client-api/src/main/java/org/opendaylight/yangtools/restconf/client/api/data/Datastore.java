package org.opendaylight.yangtools.restconf.client.api.data;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

public interface Datastore {

    /**
     * Reads data from data store and return's result in future.
     * 
     * This call is equivalent to invocation of {@link #readData(InstanceIdentifier, RetrievalStrategy)}
     * with {@link DefaultRetrievalStrategy}.
     * 
     * @param path InstanceIdentifier representing path in YANG schema to be retrieved.
     * @return Readed data. if the requested data are not present returns value of {@link Optional#absent()} .
     * 
     */
    <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier<T> path);
    
    /**
     * Reads data from data store and return's result in future.
     * 
     * @param path Representing path in YANG schema to be retrieved.
     * @param strategy Strategy which should be used to retrieve data
     * @return Readed data. if the requested data are not present returns value of {@link Optional#absent()} .
     * 
     */
    <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier<T> path,RetrievalStrategy strategy);

}
