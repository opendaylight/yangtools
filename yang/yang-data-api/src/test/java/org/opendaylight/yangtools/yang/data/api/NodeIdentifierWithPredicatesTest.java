package org.opendaylight.yangtools.yang.data.api;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.opendaylight.yangtools.yang.common.QName;

public class NodeIdentifierWithPredicatesTest extends TestCase {

    public void testName() throws Exception {
        final QName node = QName.create("namespace", "2012-12-12", "name");
        final Map<QName, Object> keys = new HashMap<QName, Object>() {{
            put(QName.create(node, "key1"), "value1");
            put(QName.create(node, "key2"), 22L);
        }};
        final YangInstanceIdentifier.NodeIdentifierWithPredicates keyedId = new YangInstanceIdentifier.NodeIdentifierWithPredicates(node, keys);
        final String asString = keyedId.toString();
        MatcherAssert.assertThat(asString, CoreMatchers.containsString("[(namespace?revision=2012-12-12)key1='value1']"));
        MatcherAssert.assertThat(asString, CoreMatchers.containsString("[(namespace?revision=2012-12-12)key2=22]"));
    }
}