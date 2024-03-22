package com.eviware.soapui.impl;

import org.junit.jupiter.api.Test;

class WsdlInterfaceFactoryTest {
    @Test
    void fullyImportWsdls() throws Exception {
        WsdlInterfaceFactory.fullyImportWsdls("/Users/oren.mittman/src/work/bgs-catalog");
    }
}
