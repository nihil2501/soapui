package com.eviware.soapui.impl;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.model.iface.Operation;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class WsdlInterfaceFactoryTest {
    @Test
    void buildCatalogFromWsdlUrls() throws Exception {
        WsdlProject project = new WsdlProject();

        Path directoryPath = Paths.get("/path/to/bgs-catalog");
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

                for (WsdlInterface wsdlInterface : results) {
                    WsdlMockService mockService = project.addNewMockService(wsdlInterface.getName() + " MockService");
                    mockService.setPath("/mock" + wsdlInterface.getName());
                    mockService.setPort(8088);

                    wsdlInterface.setDefinition(url, false);
                    wsdlInterface.addEndpoint(mockService.getLocalEndpoint());

                    Path portBindingPath = wsdlPath.resolve(wsdlInterface.getName());

                    for (Operation operation : wsdlInterface.getOperationList()) {
                        Path operationPath = portBindingPath.resolve(operation.getName());
                        Files.createDirectories(operationPath);

                        WsdlRequest request = addRequest((WsdlOperation) operation, "Request  1");
                        Files.write(operationPath.resolve("request.xml"), request.getRequestContent().getBytes());

                        WsdlMockResponse response = addNewMockOperationResponse(mockService, "Response 1", (WsdlOperation) operation);
                        Files.write(operationPath.resolve("response.xml"), response.getResponseContent().getBytes());
                    }
                }
            }
        }
    }

    public WsdlRequest addRequest(WsdlOperation operation, String name) {
        WsdlRequest request = operation.addNewRequest(name);
        request.setRequestContent(operation.createRequest(true));
        return request;
    }

    public WsdlMockResponse addNewMockOperationResponse(WsdlMockService service, String name, WsdlOperation operation) {
        WsdlMockOperation mockOperation = (WsdlMockOperation) service.addNewMockOperation(operation);
        return mockOperation.addNewMockResponse(name, true);
    }
}
