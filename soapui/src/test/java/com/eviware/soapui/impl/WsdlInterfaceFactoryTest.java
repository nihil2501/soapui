package com.eviware.soapui.impl;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class WsdlInterfaceFactoryTest {
    @Test
    void fullyImportWsdls() throws Exception {
        WsdlProject project = new WsdlProject();

        Path directoryPath = Paths.get("/Users/oren.mittman/src/work/bgs-catalog");
        Path wsdlsPath = directoryPath.resolve("wsdl-urls");
        List<String> urls = Files.readAllLines(wsdlsPath);

        for (String url : urls) {
            WsdlInterface[] results;

            try {
                results = WsdlImporter.importWsdl(project, url);
            } catch (Exception e) {
                System.err.println("Error importing WSDL from " + url + ": " + e.getMessage());
                continue;
            }

            if (results != null) {
                Path wsdlPath = directoryPath.resolve(new URI(url).getPath().substring(1));

                for (WsdlInterface iface : results) {
                    WsdlMockService mockService = project.addNewMockService(iface.getName() + " MockService");
                    mockService.setPath("/mock" + iface.getName());
                    mockService.setPort(8088);

                    iface.setDefinition(url, false);
                    iface.addEndpoint(mockService.getLocalEndpoint());

                    Path portBindingPath = wsdlPath.resolve(iface.getName());

                    for (WsdlOperation operation : iface.getWsdlOperations()) {
                        Path operationPath = portBindingPath.resolve(operation.getName());
                        Files.createDirectories(operationPath);

                        WsdlRequest request = operation.addRequest("Request  1");
                        Files.write(operationPath.resolve("request.xml"), request.getRequestContent().getBytes());

                        WsdlMockResponse response = mockService.addNewMockOperationResponse("Response 1", operation);
                        Files.write(operationPath.resolve("response.xml"), response.getResponseContent().getBytes());
                    }
                }
            }
        }
    }
}
