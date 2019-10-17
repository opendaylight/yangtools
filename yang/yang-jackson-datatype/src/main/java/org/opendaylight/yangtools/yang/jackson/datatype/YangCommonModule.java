package org.opendaylight.yangtools.yang.jackson.datatype;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public final class YangCommonModule extends Module {

    @Override
    public String getModuleName() {
        return "YangCommonModule";
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        // No-op

    }

}
