/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.core.flavor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;
import static com.intel.mtwilson.core.flavor.common.PlatformFlavorUtil.getVendorName;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author ssbangal
 */
public class PlatformFlavorTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlatformFlavorTest.class);
    private HostManifest hostManifest;
    X509AttributeCertificate tagCer;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        String hostReportAsJson = Resources.toString(Resources.getResource("RHEL_Manifest.json"), Charsets.UTF_8);
        hostManifest = mapper.readValue(hostReportAsJson, HostManifest.class);
        String tagCerAsJson = Resources.toString(Resources.getResource("AssetTag_Certificate.json"), Charsets.UTF_8);
        tagCer= mapper.readValue(tagCerAsJson, X509AttributeCertificate.class);                
        
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test of getFlavorPartNames method, of class PlatformFlavor.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetUbuntuTpm12FlavorPartNames() throws Exception {
        System.out.println("testGetUbuntuTpm12FlavorPartNames");
        Collection<String> expResult = new ArrayList(Arrays.asList(PLATFORM.getValue(), OS.getValue(), HOST_UNIQUE.getValue(), SOFTWARE.getValue(), ASSET_TAG.getValue()));

        PlatformFlavorFactory factory = new PlatformFlavorFactory();
        PlatformFlavor platformFlavor = factory.getPlatformFlavor(hostManifest, null); // since we are passing null for tagcertificate, it should have only 2 flavor parts
               
        Collection<String> result = platformFlavor.getFlavorPartNames();
        System.out.println(result);
        platformFlavor = factory.getPlatformFlavor(hostManifest, tagCer);
               
        result = platformFlavor.getFlavorPartNames();
        System.out.println(result);
        assertEquals(expResult, result);
        
        List<String> actualFlavor = platformFlavor.getFlavorPart(PLATFORM.getValue());
        String expectedFlavor = Resources.toString(Resources.getResource("RHEL_BIOS_TPM12.json"),Charset.defaultCharset());
        System.out.println("Actual PLATFORM flavor is *** " + actualFlavor.get(0) + " ***");
        System.out.println("Expected PLATFORM flavor is ***" + expectedFlavor + "***");
        String actualIdValue = actualFlavor.get(0).substring(actualFlavor.get(0).indexOf("\"id\":")+("\"id\":").length(), (actualFlavor.get(0).indexOf("\"id\":")+("\"id\":").length()+38));
        String expIdValue = expectedFlavor.substring(expectedFlavor.indexOf("\"id\":")+("\"id\":").length(), (expectedFlavor.indexOf("\"id\":")+("\"id\":").length()+38));
        actualFlavor.add(actualFlavor.get(0).replace(actualIdValue, expIdValue));
        System.out.println("After ID replacement:");
        System.out.println("Actual PLATFORM flavor is *** " + actualFlavor + " ***");
        System.out.println("Expected PLATFORM flavor is ***" + expectedFlavor + "***");
    }
    
    @Test
    public void testCreateAssetTagFlavorOnly() throws Exception {
        Collection<String> expResult = new ArrayList(Arrays.asList(ASSET_TAG.getValue()));
        
        PlatformFlavorFactory factory = new PlatformFlavorFactory();
        PlatformFlavor platformFlavor = factory.getPlatformFlavor(getVendorName(hostManifest.getHostInfo()), tagCer);
        
        Collection<String> actualResult = platformFlavor.getFlavorPartNames();
        assertEquals(expResult, actualResult);
                
        List<String> actualFlavor = platformFlavor.getFlavorPart(ASSET_TAG.getValue());
        String expectedFlavor = Resources.toString(Resources.getResource("RHEL_Asset_Tag_Flavor.json"),Charset.defaultCharset());
        System.out.println("Actual ASSET_TAG flavor is *** " + actualFlavor.get(0) + " ***");
        System.out.println("Expected ASSET_TAG flavor is ***" + expectedFlavor + "***");
        String actualIdValue = actualFlavor.get(0).substring(actualFlavor.get(0).indexOf("\"id\":")+("\"id\":").length(), (actualFlavor.get(0).indexOf("\"id\":")+("\"id\":").length()+38));
        String expIdValue = expectedFlavor.substring(expectedFlavor.indexOf("\"id\":")+("\"id\":").length(), (expectedFlavor.indexOf("\"id\":")+("\"id\":").length()+38));
        actualFlavor.add(actualFlavor.get(0).replace(actualIdValue, expIdValue));
        System.out.println("After ID replacement:");
        System.out.println("Actual ASSET_TAG flavor is *** " + actualFlavor + " ***");
        System.out.println("Expected ASSET_TAG flavor is ***" + expectedFlavor + "***");
    }
}
