package com.intel.mtwilson.core.flavor;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.mtwilson.core.common.model.*;
import com.intel.mtwilson.core.common.utils.MeasurementUtils;
import com.intel.mtwilson.core.flavor.common.*;
import com.intel.mtwilson.core.flavor.model.*;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import com.intel.wml.measurement.xml.Measurement;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;
import com.intel.mtwilson.core.common.model.SoftwareFlavorPrefix;
/**
 *
 * @author ssbangal
 */
public class RHELPlatformFlavor extends PlatformFlavor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RHELPlatformFlavor.class);
    private HostManifest hostManifest;
    private HostInfo hostInfo;
    private X509AttributeCertificate tagCertificate;
    
    private final List<String> PLATFORM_MODULES = Arrays.asList("LCP_DETAILS_HASH", "BIOSAC_REG_DATA", "OSSINITDATA_CAP_HASH", "STM_HASH", "MLE_HASH", "NV_INFO_HASH", "tb_policy", "CPU_SCRTM_STAT", "HASH_START", "SINIT_PUBKEY_HASH", "LCP_AUTHORITIES_HASH",
            "EVTYPE_KM_HASH", "EVTYPE_BPM_HASH", "EVTYPE_KM_INFO_HASH", "EVTYPE_BPM_INFO_HASH", "EVTYPE_BOOT_POL_HASH"); // CBnT modules
    private final List<String> OS_MODULES = Arrays.asList("vmlinuz");
    private final List<String> HOST_UNIQUE_MODULES = Arrays.asList("initrd", "LCP_CONTROL_HASH");

    public RHELPlatformFlavor(HostManifest hostReport) {
        this.hostInfo = hostReport.getHostInfo();
        this.hostManifest = hostReport;
    }

    public RHELPlatformFlavor(HostManifest hostReport, X509AttributeCertificate tagCertificate) {
        this.hostInfo = hostReport.getHostInfo();
        this.hostManifest = hostReport;
        this.tagCertificate = tagCertificate;
    }

    public RHELPlatformFlavor(X509AttributeCertificate tagCertificate) {
        this.tagCertificate = tagCertificate;
    }

    @Override
    public List<String> getFlavorPart(String name) throws Exception {
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
                    return getHostUniqueFlavor();
                case SOFTWARE:
                    return getDefaultSoftwareFlavor();
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
    public Collection<String> getFlavorPartNames() throws Exception{
        List<String> flavorParts = new ArrayList(Arrays.asList(PLATFORM.getValue(), OS.getValue(),
                HOST_UNIQUE.getValue(), SOFTWARE.getValue()));
        
        // For each of the flavor parts, check what PCRs are required and if those required PCRs are present in the host report.
        Iterator iterator = flavorParts.iterator();        
        while (iterator.hasNext()) {
            String nextElement = (String) iterator.next();
            nextElement = nextElement.toUpperCase();
            List<Integer> pcrList = getPcrList(hostInfo, FlavorPart.valueOf(nextElement));
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
     * @param hostInfo Information of the host including hardware details
     * @param flavorPartName Name of the flavor part.
     * @return List of PCRs for the specified flavor part and TPM version.
     */
    private List<Integer> getPcrList(HostInfo hostInfo, FlavorPart flavorPartName) {
        Set<Integer> pcrs = new HashSet<>();
        boolean isTbootInstalled = isTbootInstalled(hostInfo);
        switch (flavorPartName) {
            case PLATFORM:
                pcrs.add(0);
                if(hostInfo.getHardwareFeatures() != null &&
                        isCbntMeasureProfile(hostInfo.getHardwareFeatures().get(HardwareFeature.CBNT))) {
                    pcrs.add(7);
                }
                if(hostInfo.getHardwareFeatures() != null &&
                        isFeatureEnabled(hostInfo.getHardwareFeatures().get(HardwareFeature.SUEFI))) {
                    pcrs.addAll(Arrays.asList(0,1,2,3,4,5,6,7));
                }
                if(isTbootInstalled) {
                    if (hostInfo.getTpmVersion() != null && hostInfo.getTpmVersion().equals("2.0")) {
                        pcrs.addAll(Arrays.asList(17, 18));
                    } else {
                        pcrs.add(17);
                    }
                }
                break;
            case OS:
                if(isTbootInstalled) {
                    if (hostInfo.getTpmVersion() != null && hostInfo.getTpmVersion().equals("2.0")) {
                        pcrs.add(17);
                    } else {
                        pcrs.add(18);
                    }
                }
                break;
            case HOST_UNIQUE:
                if(isTbootInstalled) {
                    if (hostInfo.getTpmVersion() != null && hostInfo.getTpmVersion().equals("2.0")) {
                        pcrs.addAll(Arrays.asList(17, 18));
                    } else {
                        pcrs.add(19);
                    }
                }
                break;
            case SOFTWARE:
                pcrs.add(14);
                break;
            default:
                break;
        }

        return new ArrayList<>(pcrs);
    }

    private boolean isFeatureEnabled(HardwareFeatureDetails feature) {
        return feature != null && feature.getEnabled();
    }

    private boolean isCbntMeasureProfile(HardwareFeatureDetails cbnt) {
        return cbnt != null && cbnt.getEnabled() && cbnt.getMeta().get("profile") != null && cbnt.getMeta().get("profile").equals(BootGuardProfile.BTGP5.getName());
    }

    private Boolean isTbootInstalled(HostInfo hostInfo) {
        return Boolean.valueOf(hostInfo.getTbootInstalled());
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
                if (tpmVersion != null && tpmVersion.equals("2.0")) {
                    eventLogRequired = true;
                } else {
                    eventLogRequired = false;
                }
                break;
            case OS:
                if (tpmVersion != null && tpmVersion.equals("2.0")) {
                    eventLogRequired = true; 
                } else {
                    eventLogRequired = false;
                }
                break;
            case HOST_UNIQUE:
                if (tpmVersion != null && tpmVersion.equals("2.0")) {
                    eventLogRequired = true; 
                } else {
                    eventLogRequired = true; 
                }
                break;
            case SOFTWARE:
                eventLogRequired = true;
                break;
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
    private List<String> getPlatformFlavor() throws Exception {
        try {
            List<String> platformFlavors = new ArrayList();
            List<Integer> platformPcrs = getPcrList(hostInfo, PLATFORM);
            boolean includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), PLATFORM);
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> filteredPcrDetails = PlatformFlavorUtil.includeModulesToEventLog(
                    PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), platformPcrs, includeEventLog),
                    PLATFORM_MODULES);
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, null, PLATFORM, null),
                    PlatformFlavorUtil.getBiosSectionDetails(hostInfo),
                    PlatformFlavorUtil.getHardwareSectionDetails(hostInfo),
                    filteredPcrDetails, null, null);
            platformFlavors.add(Flavor.serialize(flavor));
            return platformFlavors;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of PLATFORM flavor";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }
    
    /**
     * Returns a json document having all the good known PCR values and
     * corresponding event logs that can be used for evaluating the OS Kernel of
     * a host.
     *
     * @return Collection of OS flavors as a JSON document.
     * @throws Exception
     */
    private List<String> getOsFlavor() throws Exception {
        try {
            List<String> osFlavors = new ArrayList();
            List<Integer> osPcrs = getPcrList(hostInfo, OS);
            boolean includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), OS);
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> filteredPcrDetails = PlatformFlavorUtil.includeModulesToEventLog(
                    PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), osPcrs, includeEventLog),
                    OS_MODULES);
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, null, OS, null),
                    PlatformFlavorUtil.getBiosSectionDetails(hostInfo),
                    null,
                    filteredPcrDetails, null, null);
            osFlavors.add(Flavor.serialize(flavor));
            return osFlavors;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of OS flavor";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }
    
    /**
     * Returns a json document having all the good known PCR values and
     * corresponding event logs that can be used for evaluating the unique part
     * of the PCR configurations of a host. These include PCRs/modules getting
     * extended to PCRs that would vary from host to host.
     *
     * @return HostUnique flavor as a JSON document.
     * @throws Exception
     */
    private List<String> getHostUniqueFlavor() throws Exception {
        try {
            List<String> hostUniqueFlavors = new ArrayList();
            List<Integer> hostUniquePcrs = getPcrList(hostInfo, HOST_UNIQUE);
            boolean includeEventLog = eventLogRequired(hostInfo.getTpmVersion(), HOST_UNIQUE);
            Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> filteredPcrDetails = PlatformFlavorUtil.includeModulesToEventLog(
                    PlatformFlavorUtil.getPcrDetails(hostManifest.getPcrManifest(), hostUniquePcrs, includeEventLog),
                    HOST_UNIQUE_MODULES);
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, null, HOST_UNIQUE, null),
                    PlatformFlavorUtil.getBiosSectionDetails(hostInfo),
                    null,
                    filteredPcrDetails, null, null);
            hostUniqueFlavors.add(Flavor.serialize(flavor));
            return hostUniqueFlavors;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of HOST_UNIQUE flavor";
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
    private List<String> getAssetTagFlavor() throws PlatformFlavorException {
        try {
            List<String> assetTagFlavors = new ArrayList();
            if (tagCertificate == null)
                throw new PlatformFlavorException(ErrorCode.FLAVOR_PART_CANNOT_BE_SUPPORTED, ErrorCode.FLAVOR_PART_CANNOT_BE_SUPPORTED.getMessage());
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(hostInfo, tagCertificate, null, ASSET_TAG, null),
                    PlatformFlavorUtil.getBiosSectionDetails(hostInfo),
                    null,
                    null, PlatformFlavorUtil.getExternalConfigurationDetails(hostManifest, tagCertificate), null);
            assetTagFlavors.add(Flavor.serialize(flavor));
            return assetTagFlavors;
        } catch (PlatformFlavorException pex) {
            throw pex;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of ASSET_TAG flavor";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

    /**
     * Method to create a software flavor. This method would create a
     * software flavor that would include all the measurements provided
     * from host.
     *
     * @return Software flavor as a JSON string.
     * @since IAT 1.0
     */
    private List<String> getDefaultSoftwareFlavor() throws PlatformFlavorException {
        try {
            List<String> softwareFlavors = new ArrayList();
            if (hostManifest.getPcrManifest() != null && hostManifest.getPcrManifest().getMeasurementXmls() != null &&
                    !hostManifest.getPcrManifest().getMeasurementXmls().isEmpty()) {
                List<String> measurementXmls = getDefaultMeasurement();
                if(measurementXmls.size() > 0) {
                    for(String measurementXml : measurementXmls) {
                        SoftwareFlavor softwareFlavor = new SoftwareFlavor(measurementXml);
                        softwareFlavors.add(softwareFlavor.getSoftwareFlavor());
                    }
                }
            }
            return softwareFlavors;
        } catch (JAXBException | IOException | XMLStreamException e) {
            throw new PlatformFlavorException(ErrorCode.SOFTWARE_FLAVOR_CANNOT_BE_CREATED, "Unable to parse measurements");
        }
    }

    private List<String> getDefaultMeasurement() throws JAXBException, IOException, XMLStreamException {
        List<String> measurementXmlCollection = new ArrayList();
        for (String measurementXml : hostManifest.getPcrManifest().getMeasurementXmls()) {
            Measurement measurement = MeasurementUtils.parseMeasurementXML(measurementXml);
            if(measurement.getLabel().contains(SoftwareFlavorPrefix.DEFAULT_APPLICATION_FLAVOR_PREFIX.getValue())|| 
                    measurement.getLabel().contains(SoftwareFlavorPrefix.DEFAULT_WORKLOAD_FLAVOR_PREFIX.getValue())) {
                measurementXmlCollection.add(measurementXml);
            }
        }
        return measurementXmlCollection;
    }
}
