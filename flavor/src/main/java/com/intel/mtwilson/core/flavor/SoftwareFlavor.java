package com.intel.mtwilson.core.flavor;

import com.intel.mtwilson.core.flavor.common.*;
import com.intel.mtwilson.core.flavor.model.*;
import com.intel.mtwilson.core.common.utils.MeasurementUtils;
import com.intel.wml.measurement.xml.Measurement;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.SOFTWARE;


/**
 *
 * @author ddhawal
 */
public class SoftwareFlavor extends PlatformFlavor {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SoftwareFlavor.class);
    public static final String DEFAULT_FLAVOR_PREFIX = "ISecL_Default_Application_Flavor_v";
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
        Software software = SoftwareFlavorUtil.getSoftware(measurements);
        Meta flavorMeta = PlatformFlavorUtil.getMetaSectionDetails(null,null, measurement, SOFTWARE, null);
        Flavor flavor = new Flavor(flavorMeta, null, null, null, null, software);
        return Flavor.serialize(flavor);
    }

    @Override
    public String getFlavorPart(String name) throws Exception {
        try {
            String flavorPartName = name.toUpperCase();
            switch (FlavorPart.valueOf(flavorPartName)) {
                case SOFTWARE:
                    return getSoftwareFlavor();
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
