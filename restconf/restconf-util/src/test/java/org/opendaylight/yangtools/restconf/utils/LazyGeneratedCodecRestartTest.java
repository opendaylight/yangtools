package org.opendaylight.yangtools.restconf.utils;

import org.junit.Before;
import org.junit.Test;

public class LazyGeneratedCodecRestartTest {

    Bug527Test bug527;
    Bug1196Test bug1196;
    RestconfUtilsTest reUtils;


    @Before
    public void setup() {
        bug527 = new Bug527Test();
        bug1196 = new Bug1196Test();
        reUtils = new RestconfUtilsTest();

    }

    @Test
    public void test() {
       bug1196.setup();
       bug1196.testXmlDataToDataObjectUnixCase();

       reUtils.setup();
       reUtils.testToDataObjectMappingWithNestedAugmentations();
    }

}
