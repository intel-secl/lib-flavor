package com.intel.mtwilson.core.flavor;

import org.junit.*;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.SOFTWARE;

public class SoftwareFlavorTest {
    private String measurement;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        measurement = "<?xml version='1.0' encoding='UTF-8'?><Measurement xmlns='lib:wml:measurements:1.0' xmlns:xs='http://www.w3.org/2001/XMLSchema' Label='application' Uuid='' DigestAlg='SHA256'><Symlink Path='/opt/trustagent/bin/tpm2_takeownership' searchType='regex'>2172553CEF5B8806AD19FFB587387BE4784A16C9AAC98BE2FCC647DF88AF5C9E</Symlink><Dir Path='/opt/trustagent/hypertext/WEB-INF' Include='.•' FilterType='regex' Exclude=''>4B5E57F6EB2F42B9039B3D1E13929295F231749C510CBE341CD68036D9AF97E2</Dir><File Path='/opt/trustagent/bin/module_analysis_da.sh'>120970D812836F19888625587A4606A5AD23CEF31C8684E601771552548FC6B9</File><CumulativeHash>2DFDA8E4279BDADBCDFC1F4EEFEFCF2413B628C4282D0739121E75FCE490038A</CumulativeHash></Measurement>";
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test of testGetSoftwareFlavor method, of class SoftwareFlavorTest.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetSoftwareFlavor() throws Exception {
        System.out.println("testGetSoftwareFlavor");
        System.out.println("Input measurement : " + measurement);
        SoftwareFlavor softwareFlavor = new SoftwareFlavor(measurement);
        System.out.println("Generated software flavor : "  + softwareFlavor.getFlavorPart(SOFTWARE.getValue()));
    }
}
