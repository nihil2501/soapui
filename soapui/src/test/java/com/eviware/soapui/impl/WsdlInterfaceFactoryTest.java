package com.eviware.soapui.impl;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

class WsdlInterfaceFactoryTest {
    @Test
    void fullyImportWsdl() throws Exception {
        String url = "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso?WSDL";
        WsdlProject project = new WsdlProject();
        WsdlInterface[] results = WsdlInterfaceFactory.fullyImportWsdl(project, url);

        for (WsdlInterface iface : results) {
            for (WsdlOperation operation : iface.getWsdlOperations()) {
                WsdlRequest request = operation.getRequestAt(0);
                String content = request.getRequestContent();
                assertThat(content, not(isEmptyString()));
            }
        }

        for (WsdlMockService mockService : project.getMockServiceList()) {
            for (WsdlOperation operation : mockService.getMockedOperations()) {
                WsdlMockOperation mockOperation = mockService.getMockOperation(operation);
                WsdlMockResponse response = mockOperation.getMockResponseAt(0);
                String content = response.getResponseContent();
                assertThat(content, not(isEmptyString()));
            }
        }
    }
}
