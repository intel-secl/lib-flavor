package com.intel.mtwilson.core.flavor;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.mtwilson.core.flavor.common.*;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.flavor.model.PcrEx;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.core.common.model.HostInfo;
import com.intel.mtwilson.core.common.model.PcrIndex;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;

/**
 *
 * @author ssbangal
 */
public class WindowsPlatformFlavor extends PlatformFlavor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RHELPlatformFlavor.class);
    private HostManifest hostManifest;
    private HostInfo hostInfo;
    private X509AttributeCertificate tagCertificate;

    public WindowsPlatformFlavor(HostManifest hostManifest) {
        this.hostInfo = hostManifest.getHostInfo();
        this.hostManifest = hostManifest;
    }

    public WindowsPlatformFlavor(HostManifest hostReport, X509AttributeCertificate tagCertificate) {
        this.hostInfo = hostReport.getHostInfo();
        this.hostManifest = hostReport;
        this.tagCertificate = tagCertificate;
    }

    public WindowsPlatformFlavor(X509AttributeCertificate tagCertificate) {
        this.tagCertificate = tagCertificate;
    }

    @Override
    public String getFlavorPart(String name) throws Exception {
        try {
            String flavorPartName = name.toUpperCase();
            switch (FlavorPart.valueOf(flavorPartName)) {
                case PLATFORM:
                    return getPlatformFlavor();
                case OS:
                    return getOsFlavor();
                case ASSET_TAG:
                    return getAssetTagFlavor();
                case HOST_UNIQUE:
//                    return getHostUniqueFlavor();
                    throw new PlatformFlavorException(ErrorCode.FLAVOR_PART_CANNOT_BE_SUPPORTED, "Windows does not support HOST_UNIQUE flavor part");
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
        List<String> flavorParts = new ArrayList(Arrays.asList(PLATFORM.getValue(), OS.getValue(), HOST_UNIQUE.getValue(), SOFTWARE.getValue()));
        
        // For each of the flavor parts, check what PCRs are required and if those required PCRs are present in the host report.
        Iterator iterator = flavorParts.iterator();        
        while (iterator.hasNext()) {
            String nextElement = (String) iterator.next();
            nextElement = nextElement.toUpperCase();
            List<Integer> pcrList = getPcrList(hostInfo.getTpmVersion(), FlavorPart.valueOf(nextElement));
            boolean pcrExists = PlatformFlavorUtil.pcrExists(hostManifest.getPcrManifest(), pcrList);
            if (!pcrExists) {
                iterator.remove();
            }
        }
        
        // If asset tag is configured, add it to the list of flavor parts.
        if (tagCertificate != null)
            flavorParts.add(ASSET_TAG.getValue());

        return flavorParts;

    }

    /**
     * Helper function to calculate the list of PCRs for the flavor part specified based
     * on the version of the TPM. 
     *
     * @param tpmVersion Version of the TPM in the host
     * @param flavorPartName Name of the flavor part.
     * @return List of PCRs for the specified flavor part and TPM version.
     */
    private List<Integer> getPcrList(String tpmVersion, FlavorPart flavorPartName) {
        List<Integer> pcrs = new ArrayList<>();

        switch (flavorPartName) {
            case PLATFORM:
                pcrs.add(0);
                break;
            case OS:
                pcrs.addAll(Arrays.asList(13,14));
                break;
            case HOST_UNIQUE:
                pcrs.add(13);   //not verified, but enables the getFlavorPartNames return list contains Host_UNIQUE flavor
                break;
            case SOFTWARE:
            default:
                break;
        }

        return pcrs;
    }

    /**
     * Helper function to determine if the event log associated with the PCR
     * should be included in the flavor for the specified flavor part.
     *
     * @param tpmVersion Version of the TPM in the host
     * @param flavorPartName Name of the flavor part.
     * @return Boolean flag indicating whether the event log should be included or not.
     */
    private boolean eventLogRequired(String tpmVersion, FlavorPart flavorPartName) {
        boolean eventLogRequired = false;

        switch (flavorPartName) {
            case PLATFORM:
                eventLogRequired = false;
                break;
            case OS:
                eventLogRequired = false;
                break;
            case HOST_UNIQUE:
            case SOFTWARE:
            default:
                break;
        }

        return eventLogRequired;
    }
    
    /**
     * Returns a json document having all the good known PCR values and
     * corresponding event logs that can be used for evaluating the PLATFORM of a
     * host.
     *
     * @return PLATFORM flavor as a JSON document.
     * @throws Exception
     */
    private String getPlatformFlavor() throws Exception {
        List<Integer> platformPcrs;
        Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetails;
        boolean includeEventLog;

        try {
            platformPcrs = getPcrList(hostInfo.getTpmVersion(), PLATFORM);
            includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), PLATFORM);
            pcrDetails = PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), platformPcrs, includeEventLog);

            Flavor flavor = new Flavor(PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, null, PLATFORM, null),
                    PlatformFlavorUtil.getBiosSectionDetails(hostInfo),
                    PlatformFlavorUtil.getHardwareSectionDetails(hostInfo), pcrDetails, null, null);
            return Flavor.serialize(flavor);
        } catch (Exception ex) {
            String errorMessage = "Error during creation of PLATFORM flavor.";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }

    }

    /**
     * Returns a json document having all the good known PCR values and
     * corresponding event logs that can be used for evaluating the OS Kernel of
     * a host.
     *
     * @return OS flavor as a JSON document.
     * @throws Exception
     */
    private String getOsFlavor() throws Exception {
        List<Integer> osPcrs;
        Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetails;
        boolean includeEventLog;

        try {
            osPcrs = getPcrList(hostInfo.getTpmVersion(), OS);
            includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), OS);
            pcrDetails = PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), osPcrs, includeEventLog);
            Flavor flavor = new Flavor(PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, null, OS, null),
                    PlatformFlavorUtil.getBiosSectionDetails(hostInfo),
                    null, pcrDetails, null, null);
            return Flavor.serialize(flavor);
        } catch (Exception ex) {
            String errorMessage = "Error during creation of OS flavor.";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    /**
     * Retrieves the asset tag part of the flavor including the certificate and all the key-value pairs
     * that are part of the certificate.
     * @return
     * @throws Exception 
     */
    private String getAssetTagFlavor() throws PlatformFlavorException {

        try {
            if (tagCertificate == null)
                throw new PlatformFlavorException(ErrorCode.FLAVOR_PART_CANNOT_BE_SUPPORTED, ErrorCode.FLAVOR_PART_CANNOT_BE_SUPPORTED.getMessage());
            
            Flavor flavor = new Flavor(PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, null, ASSET_TAG, null),
                    PlatformFlavorUtil.getBiosSectionDetails(hostInfo), null,
                    null, PlatformFlavorUtil.getExternalConfigurationDetails(hostManifest, tagCertificate), null);
            return Flavor.serialize(flavor);
        } catch (PlatformFlavorException pex) {
            throw pex;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of Host Aggregated flavor.";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }
}
