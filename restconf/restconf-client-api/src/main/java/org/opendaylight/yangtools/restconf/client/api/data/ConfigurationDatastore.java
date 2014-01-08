package org.opendaylight.yangtools.restconf.client.api.data;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.util.concurrent.ListenableFuture;

public interface ConfigurationDatastore extends Datastore {

    ListenableFuture<RpcResult<Boolean>> deleteData(InstanceIdentifier<?> path);
    
    ListenableFuture<RpcResult<Boolean>> putData(InstanceIdentifier<?> path);
}
