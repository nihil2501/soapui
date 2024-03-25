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
    void saveCatalogFromWsdlUrls() throws Exception {
        buildCatalogFromWsdlUrls(true);
    }

    @Test
    void trialCatalogFromWsdlUrls() throws Exception {
        buildCatalogFromWsdlUrls(false);
    }

    private void buildCatalogFromWsdlUrls(boolean shouldSave) throws Exception {
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

                for (WsdlInterface wsdlInterface : results) {
                    WsdlMockService mockService = project.addNewMockService(wsdlInterface.getName() + " MockService");
                    mockService.setPath("/mock" + wsdlInterface.getName());
                    mockService.setPort(8088);

                    wsdlInterface.setDefinition(url, false);
                    wsdlInterface.addEndpoint(mockService.getLocalEndpoint());

                    Path portBindingPath = wsdlPath.resolve(wsdlInterface.getName());

                    for (Operation operation : wsdlInterface.getOperationList()) {
                        Path operationPath = portBindingPath.resolve(operation.getName());
                        WsdlRequest request = addRequest((WsdlOperation) operation);
                        WsdlMockResponse response = addNewMockOperationResponse(mockService, (WsdlOperation) operation);

                        if (shouldSave) {
                            Files.createDirectories(operationPath);
                            Files.write(operationPath.resolve("request.xml"), request.getRequestContent().getBytes());
                            Files.write(operationPath.resolve("response.xml"), response.getResponseContent().getBytes());
                        }
                    }
                }
            }
        }
    }

    private WsdlRequest addRequest(WsdlOperation operation) {
        WsdlRequest request = operation.addNewRequest("Request  1");
        request.setRequestContent(operation.createRequest(true));
        return request;
    }

    private WsdlMockResponse addNewMockOperationResponse(WsdlMockService service, WsdlOperation operation) {
        WsdlMockOperation mockOperation = (WsdlMockOperation) service.addNewMockOperation(operation);
        return mockOperation.addNewMockResponse("Response 1", true);
    }
}
