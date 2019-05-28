package com.intel.mtwilson.core.flavor.common;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.common.model.*;
import com.intel.mtwilson.core.flavor.model.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;

import java.util.ArrayList;

import com.intel.mtwilson.core.common.utils.MeasurementUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import static com.intel.mtwilson.core.flavor.model.Meta.ISL_MEASUREMENT_SCHEMA;

/**
 * @author ssbangal
 */
public class PlatformFlavorUtil {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlatformFlavorUtil.class);

    public static Meta getMetaSectionDetails(HostInfo hostDetails, X509AttributeCertificate tagCertificate, String xmlMeasurement, FlavorPart flavorPartName, String vendor) throws JAXBException, IOException, XMLStreamException {
        Meta meta = new Meta();
        meta.setId(new UUID().toString());
        if(vendor == null) {
            meta.setVendor(getVendorName(hostDetails));
        } else {
            meta.setVendor(vendor);
        }
        Description description = new Description();
        String biosName = null;
        String biosVersion = null;
        String osName = null;
        String osVersion = null;
        String vmmName = null;
        String vmmVersion = null;
        if (hostDetails != null) {
            description.setTbootInstalled(hostDetails.getTbootInstalled());
            biosName = hostDetails.getBiosName().trim();
            biosVersion = hostDetails.getBiosVersion().trim();
            vmmName = hostDetails.getVmmName().trim();
            vmmVersion = hostDetails.getVmmVersion().trim();
            osName = hostDetails.getOsName().trim();
            osVersion = hostDetails.getOsVersion().trim();
            if (hostDetails.getTpmVersion() != null)
                description.setTpmVersion(hostDetails.getTpmVersion().trim());
        }
        switch (flavorPartName) {
            case PLATFORM:
                List<String> features = getSupportedHardwareFeatures(hostDetails);
                description.setLabel(getLabelFromDetails(meta.getVendor(), biosName, biosVersion, StringUtils.join(features, '_'), getCurrentTimeStamp()));
                description.setBiosName(biosName);
                description.setBiosVersion(biosVersion);
                description.setFlavorPart(flavorPartName.getValue());
                if (hostDetails != null && hostDetails.getHostName() != null)
                    description.setSource(hostDetails.getHostName().trim());
                break;
            case OS:
                description.setLabel(getLabelFromDetails(meta.getVendor(), osName, osVersion, vmmName, vmmVersion, getCurrentTimeStamp()));
                description.setOsName(osName);
                description.setOsVersion(osVersion);
                description.setFlavorPart(flavorPartName.getValue());
                if (hostDetails != null) {
                    if (hostDetails.getHostName() != null)
                        description.setSource(hostDetails.getHostName().trim());
                    if (hostDetails.getVmmName() != null)
                        description.setVmmName(hostDetails.getVmmName().trim());
                    if (hostDetails.getVmmVersion() != null)
                        description.setVmmVersion(hostDetails.getVmmVersion().trim());
                }
             break;
            case SOFTWARE:
                com.intel.wml.measurement.xml.Measurement measurements = MeasurementUtils.parseMeasurementXML(xmlMeasurement);
                description.setLabel(measurements.getLabel());
                description.setFlavorPart(flavorPartName.getValue());
                description.setDigestAlgorithm(DigestAlgorithm.valueOf(measurements.getDigestAlg()));
                meta.setId(getSoftwareFlavorUuid(measurements.getUuid()));
                meta.setSchema(getSchema());
                break;
            case ASSET_TAG:
                description.setFlavorPart(flavorPartName.getValue());
                if (hostDetails != null) {
                    if (hostDetails.getHardwareUuid() != null)
                        description.setHardwareUuid(hostDetails.getHardwareUuid().trim());
                    if (hostDetails.getHostName() != null)
                        description.setSource(hostDetails.getHostName().trim());
              } else if (tagCertificate != null)
                    description.setHardwareUuid(tagCertificate.getSubject().trim().toUpperCase());
                description.setLabel(getLabelFromDetails(meta.getVendor(), description.getHardwareUuid(), getCurrentTimeStamp()));
                break;
            case HOST_UNIQUE:
              if (hostDetails != null) {
                    if (hostDetails.getHostName() != null)
                        description.setSource(hostDetails.getHostName().trim());
                    if (hostDetails.getHardwareUuid() != null)
                        description.setHardwareUuid(hostDetails.getHardwareUuid().trim());
                }
                description.setBiosName(biosName);
                description.setBiosVersion(biosVersion);
                description.setOsName(osName);
                description.setOsVersion(osVersion);
                description.setFlavorPart(flavorPartName.getValue());
                description.setLabel(getLabelFromDetails(meta.getVendor(), description.getHardwareUuid(), getCurrentTimeStamp()));
                break;
            default:
                break;
        }
        meta.setDescription(description);
        return meta;
    }

    public static Bios getBiosSectionDetails(HostInfo hostDetails) {
        Bios bios = null;
        if (hostDetails != null) {
            bios = new Bios();
            bios.setBiosName(hostDetails.getBiosName().trim());
            bios.setBiosVersion(hostDetails.getBiosVersion().trim());
        }
        return bios;
    }

    private static String getSoftwareFlavorUuid(String uuid) {
        return StringUtils.isEmpty(uuid) ? String.valueOf(new UUID()) : uuid;
    }

    private static Meta.Schema getSchema() {
        Meta.Schema schema = new Meta.Schema();
        schema.setUri(ISL_MEASUREMENT_SCHEMA);
        return schema;
    }

    // TODO: Need to get the host details parameter passed into this so that all the fields can be 
    // populated accordingly.

    public static Hardware getHardwareSectionDetails(HostInfo hostInfo) {
        Hardware hardware = new Hardware();
        if (hostInfo.getProcessorInfo() != null)
            hardware.setProcessorInfo(hostInfo.getProcessorInfo().trim());
        if (hostInfo.getProcessorFlags() != null)
            hardware.setProcessorFlags(hostInfo.getProcessorFlags().trim());
        Feature feature = new Feature();
        Feature.TPM tpm = new Feature.TPM();
        Feature.TXT txt = new Feature.TXT();

        // TODO: evaluate processor flags to set these fields
        tpm.setEnabled(Boolean.valueOf(hostInfo.getTpmEnabled()));
        tpm.setVersion(hostInfo.getTpmVersion());
        tpm.setPcrBanks(ObjectUtils.defaultIfNull(hostInfo.getPcrBanks(), null));
        txt.setEnabled(Boolean.valueOf(hostInfo.getTxtEnabled()));

        if(hostInfo.getHardwareFeatures() != null && hostInfo.getHardwareFeatures().size() > 0) {
            Feature.CBNT cbnt = getCbntHwFeatureDetails(hostInfo.getHardwareFeatures().get(HardwareFeature.CBNT));
            txt.setEnabled(hostInfo.getHardwareFeatures().get(HardwareFeature.TXT).getEnabled());
            tpm = getTpmHwFeatureDetails(hostInfo.getHardwareFeatures().get(HardwareFeature.TPM));
            Feature.SUEFI suefi = getSuefiHwFeatureDetails(hostInfo.getHardwareFeatures().get(HardwareFeature.SUEFI));
            feature.setCBNT(cbnt);
            feature.setSUEFI(suefi);
        }
        feature.setTPM(tpm);
        feature.setTXT(txt);
        hardware.setFeature(feature);
        return hardware;
    }

    private static Feature.SUEFI getSuefiHwFeatureDetails(HardwareFeatureDetails hardwareFeatureDetails) {
        Feature.SUEFI suefi = null;
        if(hardwareFeatureDetails != null) {
            suefi = new Feature.SUEFI();
            suefi.setEnabled(hardwareFeatureDetails.getEnabled());
        }
        return suefi;
    }

    private static Feature.TPM getTpmHwFeatureDetails(HardwareFeatureDetails hardwareFeatureDetails) {
        Feature.TPM tpm = null;
        if(hardwareFeatureDetails != null) {
            tpm = new Feature.TPM();
            tpm.setEnabled(hardwareFeatureDetails.getEnabled());
            tpm.setVersion(hardwareFeatureDetails.getMeta().get("tpm_version"));
            if (hardwareFeatureDetails.getMeta().get("pcr_banks") != null) {
                tpm.setPcrBanks(Arrays.asList(hardwareFeatureDetails.getMeta().get("pcr_banks").split("_")));
            }
        }
        return tpm;
    }
    
    private static Feature.CBNT getCbntHwFeatureDetails(HardwareFeatureDetails hardwareFeatureDetails) {
        Feature.CBNT cbnt = null;
        if(hardwareFeatureDetails != null) {
            cbnt = new Feature.CBNT();
            cbnt.setEnabled(hardwareFeatureDetails.getEnabled());
            cbnt.setProfile(hardwareFeatureDetails.getMeta().get("profile"));
        }
        return cbnt;
    }

    public static boolean pcrExists(PcrManifest pcrManifest, List<Integer> pcrList) {

        boolean pcrExists = false;

        if (pcrList == null || pcrList.isEmpty())
            return pcrExists;

        for (DigestAlgorithm digestAlgorithm : pcrManifest.getPcrEventLogMap().keySet()) {
            boolean pcrExistsForDigestAlg = false;
            for (Integer pcrIndex : pcrList) {
                Pcr pcrInfo = pcrManifest.getPcr(digestAlgorithm, pcrIndex);
                if (pcrInfo != null) {
                    pcrExistsForDigestAlg = true;
                } else {
                    pcrExistsForDigestAlg = false;
                }
            }
            // This check ensures that even if PCRs exist for one supported algorithm, we return back true.
            if (pcrExistsForDigestAlg && !pcrExists) {
                pcrExists = pcrExistsForDigestAlg;
            }
        }

        return pcrExists;
    }

    public static Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> getPcrDetails(PcrManifest pcrManifest, List<Integer> pcrList, boolean includeEventLog) {
        Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrsWithDigestAlgorithm = new HashMap();
        Map<PcrIndex, PcrEx> pcrs = new HashMap();

        for (DigestAlgorithm digestAlgorithm : pcrManifest.getPcrEventLogMap().keySet()) {
            for (Integer pcrIndex : pcrList) {
                Pcr pcrInfo = pcrManifest.getPcr(digestAlgorithm, pcrIndex);
                if (pcrInfo != null) {
                    PcrEventLog pcrEventLog = null;
                    if (includeEventLog)
                        pcrEventLog = pcrManifest.getPcrEventLog(digestAlgorithm, pcrInfo.getIndex());

                    List<Measurement> event = pcrEventLog == null ? null : pcrEventLog.getEventLog();
                    pcrs.put(new PcrIndex(pcrInfo.getIndex().toInteger()), new PcrEx(pcrInfo.getValue().toString(), event));
                }
            }
            pcrsWithDigestAlgorithm.put(digestAlgorithm, pcrs);
            pcrs = new HashMap<>();
        }

        return pcrsWithDigestAlgorithm;
    }


    public static External getExternalConfigurationDetails(HostManifest hostManifest, X509AttributeCertificate tagCertificate) throws PlatformFlavorException {

        External externalConfiguration = new External();
        AssetTag assetTag = new AssetTag();

        if (tagCertificate == null) {
            throw new PlatformFlavorException(ErrorCode.INVALID_INPUT, "Specified tagcertificate is not valid.");
        }
        assetTag.setTagCertificate(tagCertificate);
        externalConfiguration.setAssetTag(assetTag);
        return externalConfiguration;
    }

    public static String getVendorName(HostInfo hostInfo) {
        if (hostInfo == null)
            return null;

        String vendor;
        switch (hostInfo.getOsName().trim().toUpperCase()) {
            case "REDHATENTERPRISESERVER":
            case "RHEL":
            case "UBUNTU":
                vendor = "INTEL";
                break;
            case "WINDOWS":
            case "MICROSOFT WINDOWS SERVER 2016 DATACENTER":
                vendor = "MICROSOFT";
                break;
            case "VMWARE ESXI":
                vendor = "VMWARE";
                break;
            default:
                vendor = "UNKNOWN";
        }
        return vendor;
    }

    private static Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> copyInstanceOfPcrDetails(Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetails) {
        Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetailsCopy = new HashMap();
        for (Map.Entry<DigestAlgorithm, Map<PcrIndex, PcrEx>> digestAlgorithmEntry : pcrDetails.entrySet()) {
            Map<PcrIndex, PcrEx> newPcrIndexMap = new HashMap();
            for (Map.Entry<PcrIndex, PcrEx> pcrIndexEntry : digestAlgorithmEntry.getValue().entrySet()) {
                if (pcrIndexEntry.getValue() != null) {
                    PcrIndex newPcrIndex = new PcrIndex(pcrIndexEntry.getKey().toInteger());
                    String newPcrValue = null;
                    if (pcrIndexEntry.getValue().getValue() != null && !pcrIndexEntry.getValue().getValue().isEmpty()) {
                        newPcrValue = pcrIndexEntry.getValue().getValue();
                    }
                    List<Measurement> newMeasurements = null;
                    if (pcrIndexEntry.getValue().getEvent() != null && !pcrIndexEntry.getValue().getEvent().isEmpty()) {
                        newMeasurements = new ArrayList();
                        newMeasurements.addAll(pcrIndexEntry.getValue().getEvent());
                    }
                    PcrEx newPcrEx = new PcrEx(newPcrValue, newMeasurements);
                    newPcrIndexMap.put(newPcrIndex, newPcrEx);
                }
            }
            pcrDetailsCopy.put(digestAlgorithmEntry.getKey(), newPcrIndexMap);
        }
        return pcrDetailsCopy;
    }

    public static Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> includeModulesToEventLog(Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetails, List<String> modulesToInclude) {
        Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> filteredPcrDetails = copyInstanceOfPcrDetails(pcrDetails);
        for (Map.Entry<DigestAlgorithm, Map<PcrIndex, PcrEx>> digestAlgorithmEntry : filteredPcrDetails.entrySet()) {
            Map<PcrIndex, PcrEx> digestAlgorigthmValue = digestAlgorithmEntry.getValue();
            for (Map.Entry<PcrIndex, PcrEx> pcrIndexEntry : digestAlgorigthmValue.entrySet()) {
                List<Measurement> toExclude = new ArrayList();
                PcrIndex key = pcrIndexEntry.getKey();
                PcrEx value = pcrIndexEntry.getValue();

                if (value.getEvent() != null && !value.getEvent().isEmpty()) {
                    for (Measurement eventDetails : value.getEvent()) {
                        if (eventDetails != null && eventDetails.getInfo() != null && !eventDetails.getInfo().isEmpty()) {
                            log.debug("Processing module {} - {} for PCR {}", eventDetails.getLabel(), eventDetails.getInfo().get("ComponentName"), key.toString());
                            if (((String) eventDetails.getInfo().get("ComponentName") != null) && !modulesToInclude.contains(((String) eventDetails.getInfo().get("ComponentName")))) {
                                toExclude.add(eventDetails);
                                continue;
                            } else {
                                log.debug("INCLUDING module {} - {} for PCR {}", eventDetails.getLabel(), eventDetails.getInfo().get("ComponentName"), key.toString());
                            }

                            // Remove the dynamic modules for VMware
                            String evenName = ((String) eventDetails.getInfo().get("EventName"));
                            String packageName = ((String) eventDetails.getInfo().get("PackageName"));
                            if (evenName != null && evenName.equalsIgnoreCase("Vim25Api.HostTpmSoftwareComponentEventDetails")
                                    && packageName != null && packageName.trim().isEmpty()) {
                                toExclude.add(eventDetails);
                            }
                        }
                    }
                    value.getEvent().removeAll(toExclude);
                }
            }
        }
        return filteredPcrDetails;
    }

    public static Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> excludeModulesFromEventLog(Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrDetails, List<String> modulesToExclude) {
        Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> filteredPcrDetails = copyInstanceOfPcrDetails(pcrDetails);
        for (Map.Entry<DigestAlgorithm, Map<PcrIndex, PcrEx>> digestAlgorithmEntry : filteredPcrDetails.entrySet()) {
            Map<PcrIndex, PcrEx> digestAlgorigthmValue = digestAlgorithmEntry.getValue();
            for (Map.Entry<PcrIndex, PcrEx> pcrIndexEntry : digestAlgorigthmValue.entrySet()) {
                List<Measurement> toExclude = new ArrayList();
                PcrIndex key = pcrIndexEntry.getKey();
                PcrEx value = pcrIndexEntry.getValue();

                if (value.getEvent() != null && !value.getEvent().isEmpty()) {
                    for (Measurement eventDetails : value.getEvent()) {
                        if (eventDetails != null && eventDetails.getInfo() != null && !eventDetails.getInfo().isEmpty()) {
                            log.debug("Processing module {} - {} for PCR {}", eventDetails.getLabel(), eventDetails.getInfo().get("ComponentName"), key.toString());
                            if (((String) eventDetails.getInfo().get("ComponentName") != null) && modulesToExclude.contains(((String) eventDetails.getInfo().get("ComponentName")))) {
                                log.debug("EXCLUDING module {} - {} for PCR {}", eventDetails.getLabel(), eventDetails.getInfo().get("ComponentName"), key.toString());
                                toExclude.add(eventDetails);
                                continue;
                            }

                            // Remove the dynamic modules for VMware
                            String evenName = ((String) eventDetails.getInfo().get("EventName"));
                            String packageName = ((String) eventDetails.getInfo().get("PackageName"));
                            if (evenName != null && evenName.equalsIgnoreCase("Vim25Api.HostTpmSoftwareComponentEventDetails")
                                    && packageName != null && packageName.trim().isEmpty()) {
                                log.debug("EXCLUDING module {} - {} for PCR {}", eventDetails.getLabel(), eventDetails.getInfo().get("ComponentName"), key.toString());
                                toExclude.add(eventDetails);
                            }
                        }
                    }
                    value.getEvent().removeAll(toExclude);
                }
            }
        }
        return filteredPcrDetails;
    }

    private static List<String> getSupportedHardwareFeatures(HostInfo hostDetails) {
        List<String> features = new ArrayList<>();
        if(hostDetails != null && hostDetails.getHardwareFeatures() != null) {
            hostDetails.getHardwareFeatures().forEach((key, value) -> {
                /*
                Check whether the hardware feature is a type of AttestationExemptFeature,
                if it is not then include it in flavor label
                 */
                if (!EnumUtils.isValidEnum(AttestationExemptFeature.class, key.getValue())) {
                    features.add(key.getValue());
                    if(key.equals(HardwareFeature.CBNT)) {
                        features.add(value.getMeta().get("profile"));
                    }
                }
            });
        }
        return features;
    }

    private static String getLabelFromDetails(String... names) {
        List<String> labels = new ArrayList<>();
        for (String s : names)
            labels.add(s.replaceAll("\\s+", ""));
        return StringUtils.join(labels, '_');
    }
    
    private static String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }
}
