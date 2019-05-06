package com.intel.mtwilson.core.flavor;

import com.intel.mtwilson.core.flavor.common.*;
import com.intel.mtwilson.core.flavor.model.*;
import com.intel.mtwilson.core.common.utils.MeasurementUtils;
import com.intel.wml.measurement.xml.Measurement;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Arrays;
import java.util.*;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.SOFTWARE;


/**
 *
 * @author ddhawal
 */
public class SoftwareFlavor extends PlatformFlavor {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SoftwareFlavor.class);
    public static final String DEFAULT_APPLICATION_FLAVOR_PREFIX = "ISecL_Default_Application_Flavor_v";
    public static final String DEFAULT_WORKLOAD_FLAVOR_PREFIX = "ISecL_Default_Workload_Flavor_v";
    private String measurement;

    public SoftwareFlavor(String measurement) {
        this.measurement = measurement;
    }

    /**
     * Method to create a software flavor. This method would create a
     * software flavor that would include all the measurements provided
     * in input.
     *
     * @return Software flavor as a JSON string.
     * @throws IOException If the flavor parts cannot be merged.Tpm
     * @since IAT 1.0
     */
    public String getSoftwareFlavor() throws IOException, JAXBException, XMLStreamException {
        Measurement measurements = MeasurementUtils.parseMeasurementXML(measurement);
        log.debug("********************************************************************");
        log.debug("the software mesurement {}", measurement);
        log.debug("the software uuid from measurement {}", measurements.getUuid());
        log.debug("the software digest algorithm from measurement {}", measurements.getDigestAlg());
        log.debug("the software label from measurement {}", measurements.getLabel());
        log.debug("the software measurement size from measurement list {}", measurements.getMeasurements().size());
        log.debug("the software hash from measurement {}", measurements.getCumulativeHash().toString());
        log.debug("********************************************************************");
        Software software = SoftwareFlavorUtil.getSoftware(measurements);
        Meta flavorMeta = PlatformFlavorUtil.getMetaSectionDetails(null,null, measurement, SOFTWARE, null);
        Flavor flavor = new Flavor(flavorMeta, null, null, null, null, software);
        return Flavor.serialize(flavor);
    }

    @Override
    public List<String> getFlavorPart(String name) throws Exception {
        try {
            String flavorPartName = name.toUpperCase();
            switch (FlavorPart.valueOf(flavorPartName)) {
                case SOFTWARE:
                    List<String> getFlavors = new ArrayList();
                    getFlavors.add(getSoftwareFlavor());
                    return getFlavors;
                default:
                    throw new PlatformFlavorException(ErrorCode.UNKNOWN_FLAVOR_PART, "Unknown flavor part specified by the user");
            }
        } catch(IllegalArgumentException lex) {
            String errorMessage = "Unknown flavor part specified by the user";
            log.error(errorMessage, lex);
            throw new PlatformFlavorException(ErrorCode.UNKNOWN_FLAVOR_PART, errorMessage);
        } catch (PlatformFlavorException pex) {
            throw pex;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of flavor.";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    @Override
    public Collection<String> getFlavorPartNames() throws Exception {
        return Arrays.asList(SOFTWARE.getValue());
    }
}
