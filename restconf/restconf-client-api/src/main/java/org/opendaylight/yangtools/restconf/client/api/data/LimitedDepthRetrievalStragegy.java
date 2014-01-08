package org.opendaylight.yangtools.restconf.client.api.data;

public interface LimitedDepthRetrievalStragegy extends RetrievalStrategy {

    int getDepth();
}
