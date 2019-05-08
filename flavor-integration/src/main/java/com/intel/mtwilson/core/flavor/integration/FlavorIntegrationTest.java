/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.integration;

import com.intel.mtwilson.core.host.connector.HostConnector;
import com.intel.mtwilson.core.host.connector.HostConnectorFactory;
import com.intel.mtwilson.core.host.connector.VendorHostConnectorFactory;
import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.kunit.annotations.BeforeAll;
import com.intel.kunit.annotations.Integration;
import com.intel.mtwilson.core.flavor.PlatformFlavor;
import com.intel.mtwilson.core.flavor.PlatformFlavorFactory;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorException;
import com.intel.mtwilson.core.host.connector.intel.IntelHostConnectorFactory;
import com.intel.mtwilson.core.host.connector.intel.MicrosoftHostConnectorFactory;
import com.intel.mtwilson.core.host.connector.vmware.VmwareHostConnectorFactory;
import com.intel.mtwilson.core.common.model.HostManifest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author purvades
 */
public class FlavorIntegrationTest {

    final TlsPolicy tlsPolicy = new InsecureTlsPolicy();
    final PlatformFlavorFactory flavorFactory = new PlatformFlavorFactory();

    public FlavorIntegrationTest() throws Exception {
    }

    @BeforeAll
    public static void setup() throws IOException {
        WhiteboardExtensionProvider.register(VendorHostConnectorFactory.class, IntelHostConnectorFactory.class);
        WhiteboardExtensionProvider.register(VendorHostConnectorFactory.class, MicrosoftHostConnectorFactory.class);
        WhiteboardExtensionProvider.register(VendorHostConnectorFactory.class, VmwareHostConnectorFactory.class);
    }

    @Integration
    public Collection<String> getFlavorPartNames(String hostConnectionString) throws IOException, Exception {

        HostConnectorFactory factory = new HostConnectorFactory();
        HostConnector hostConnector = factory.getHostConnector(hostConnectionString, tlsPolicy);
        HostManifest hostManifest = hostConnector.getHostManifest();

        PlatformFlavor platformFlavor = flavorFactory.getPlatformFlavor(hostManifest, null);

        Collection<String> result = platformFlavor.getFlavorPartNames();

        return result;
    }

    @Integration
    public void getFlavorParts(String hostConnectionString, Collection<String> flavorParts) throws PlatformFlavorException, MalformedURLException, IOException, Exception {

        Logger.getLogger(FlavorIntegrationTest.class.getName()).log(Level.INFO, String.format("Flavor parts specified are {%s}", flavorParts.toString()));

        HostConnectorFactory factory = new HostConnectorFactory();
        HostConnector hostConnector = factory.getHostConnector(hostConnectionString, tlsPolicy);
        HostManifest hostManifest = hostConnector.getHostManifest();

        PlatformFlavor platformFlavor = flavorFactory.getPlatformFlavor(hostManifest, null);

        for (String flavorPart : flavorParts) {
            try {
                Logger.getLogger(FlavorIntegrationTest.class.getName()).log(Level.INFO, String.format("About to retrieve flavor details for {%s}", flavorPart));
                List<String> detailedFlavorPart = platformFlavor.getFlavorPart(flavorPart);
                Logger.getLogger(FlavorIntegrationTest.class.getName()).log(Level.INFO, String.format("Retrieved flavor {%s} with content {%s}", flavorPart, detailedFlavorPart.get(0)));
            } catch (Exception ex) {
                Logger.getLogger(FlavorIntegrationTest.class.getName()).log(Level.SEVERE, String.format("Error retrieving flavor part {%s}", flavorPart), ex);
            }
        }
    }
}
