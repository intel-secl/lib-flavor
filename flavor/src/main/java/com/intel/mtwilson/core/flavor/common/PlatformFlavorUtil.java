/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.common;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.model.*;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.core.common.model.Measurement;
import com.intel.mtwilson.core.common.model.Pcr;
import com.intel.mtwilson.core.common.model.PcrEventLog;
import com.intel.mtwilson.core.common.model.PcrManifest;

import java.text.SimpleDateFormat;
import java.util.*;

import com.intel.mtwilson.core.common.model.HostInfo;
import com.intel.mtwilson.core.common.model.PcrIndex;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author ssbangal
 */
public class PlatformFlavorUtil {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlatformFlavorUtil.class);

    public static Meta getMetaSectionDetails(HostInfo hostDetails, X509AttributeCertificate tagCertificate, String flavorPartName, String vendor) {
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
            biosName = hostDetails.getBiosName().trim();
            biosVersion = hostDetails.getBiosVersion().trim();
            vmmName = hostDetails.getVmmName().trim();
            vmmVersion = hostDetails.getVmmVersion().trim();
            osName = hostDetails.getOsName().trim();
            osVersion = hostDetails.getOsVersion().trim();

            if (hostDetails.getTpmVersion() != null)
                description.setTpmVersion(hostDetails.getTpmVersion().trim());
        }
        switch (FlavorPart.valueOf(flavorPartName.toUpperCase())) {
            case PLATFORM:
                description.setLabel(getLabelFromDetails(meta.getVendor(), biosName, biosVersion, getCurrentTimeStamp()));
                description.setBiosName(biosName);
                description.setBiosVersion(biosVersion);
                description.setFlavorPart(flavorPartName);
                if (hostDetails != null && hostDetails.getHostName() != null)
                    description.setSource(hostDetails.getHostName().trim());
                break;
            case OS:
                description.setLabel(getLabelFromDetails(meta.getVendor(), osName, osVersion, vmmName, vmmVersion, getCurrentTimeStamp()));
                description.setOsName(osName);
                description.setOsVersion(osVersion);
                description.setVmmName(vmmName);
                description.setVmmVersion(vmmVersion);
                description.setFlavorPart(flavorPartName);
                if (hostDetails != null && hostDetails.getHostName() != null)
                    description.setSource(hostDetails.getHostName().trim());
                break;
            case SOFTWARE:
                break;
            case ASSET_TAG:
                description.setFlavorPart(flavorPartName);
                if (hostDetails != null) {
                    if (hostDetails.getHardwareUuid() != null)
                        description.setHardwareUuid(hostDetails.getHardwareUuid().trim());
                    if (hostDetails.getHostName() != null)
                        description.setSource(hostDetails.getHostName().trim());
                } else if (tagCertificate != null) {
                    description.setHardwareUuid(tagCertificate.getSubject().trim().toUpperCase());
                }
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
                description.setFlavorPart(flavorPartName);
                description.setLabel(getLabelFromDetails(meta.getVendor(), description.getHardwareUuid(), getCurrentTimeStamp()));
                break;
            default:
                break;
        }
        meta.setDescription(description);
        return meta;
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
        tpm.setEnabled(true);
        tpm.setPcrBanks(ObjectUtils.defaultIfNull(hostInfo.getPcrBanks(), null));
        txt.setEnabled(true);
        
        feature.setTPM(tpm);
        feature.setTXT(txt);
        hardware.setFeature(feature);
        return hardware;
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
    
//    public static HostUniqueAssetTag getHostUniqueAssetTagDetails(HostManifest hostManifest) {
//    
//        HostUniqueAssetTag assetTag = new HostUniqueAssetTag();
//        assetTag.setProvisionedTagValue(hostManifest.getAssetTagDigest());
//        assetTag.setDigestAlgorithm(DigestAlgorithm.SHA256.algorithm());
//        
//        return assetTag;
//    }
    
    
    public static External getExternalConfigurationDetails(HostManifest hostManifest, X509AttributeCertificate tagCertificate) throws PlatformFlavorException {
        
        External externalConfiguration = new External();
        AssetTag assetTag = new AssetTag();       

        if (tagCertificate == null) {
            throw new PlatformFlavorException(ErrorCode.INVALID_INPUT, "Specified tagcertificate is not valid.");
        }
        
//        Certificate certificate = new Certificate();
//        Map<String, Map<String, Set<String>>> certificateAttributes = new HashMap<>();
//
//        List<Attribute> tagAttributes = tagCertificate.getAttribute();
//        for (Attribute tagAttribute : tagAttributes) {
//            String attrObjectId  = tagAttribute.getAttrType().getId();
//            String[] assetTagAttributestrObjectValue = tagAttribute.getAttrValues().toArray()[0].toString().split("=");
//            
//            if (certificateAttributes.containsKey(attrObjectId)) {
//                Map<String, Set<String>> getCert = certificateAttributes.get(attrObjectId);
//                if (getCert.containsKey(assetTagAttributestrObjectValue[0])) {
//                    Set<String> get = getCert.get(assetTagAttributestrObjectValue[0]);
//                    get.add(assetTagAttributestrObjectValue[1]);
//                    getCert.replace(attrObjectId, get);
//                } else {
//                    Set<String> h = new HashSet<>(Arrays.asList(assetTagAttributestrObjectValue[1]));
//                    getCert.put(assetTagAttributestrObjectValue[0], h);
//                }
//            } else {
//                Set<String> h = new HashSet<>(Arrays.asList(assetTagAttributestrObjectValue[1]));
//                Map<String, Set<String>> newAttributeValues = new HashMap<>();
//                newAttributeValues.put(assetTagAttributestrObjectValue[0], h);
//                certificateAttributes.put(attrObjectId, newAttributeValues);                
//            }
//            
//        }
//        certificate.setAttribute(certificateAttributes);
//        certificate.setIssuer(new Certificate.Issuer(tagCertificate.getIssuer()));
        
        //assetTag.setCertificate(certificate);
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
                        if(eventDetails!=null && eventDetails.getInfo()!=null && !eventDetails.getInfo().isEmpty()){
                            log.debug("Processing module {} - {} for PCR {}", eventDetails.getLabel(), eventDetails.getInfo().get("ComponentName"), key.toString());
                            if (((String)eventDetails.getInfo().get("ComponentName")!=null) && !modulesToInclude.contains(((String)eventDetails.getInfo().get("ComponentName")))) {
                                toExclude.add(eventDetails);
                                continue;
                            } else {
                                log.debug("INCLUDING module {} - {} for PCR {}", eventDetails.getLabel(), eventDetails.getInfo().get("ComponentName"), key.toString());
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
                        if(eventDetails!=null && eventDetails.getInfo()!=null && !eventDetails.getInfo().isEmpty()){
                            log.debug("Processing module {} - {} for PCR {}", eventDetails.getLabel(), eventDetails.getInfo().get("ComponentName"), key.toString());
                            if (((String)eventDetails.getInfo().get("ComponentName")!=null) && modulesToExclude.contains(((String)eventDetails.getInfo().get("ComponentName")))) {
                                log.debug("EXCLUDING module {} - {} for PCR {}", eventDetails.getLabel(), eventDetails.getInfo().get("ComponentName"), key.toString());
                                toExclude.add(eventDetails);
                                continue;
                            }

                            // Remove the dynamic modules for VMware
                            String evenName = ((String)eventDetails.getInfo().get("EventName"));
                            String packageName = ((String)eventDetails.getInfo().get("PackageName"));
                            if (evenName!=null && evenName.equalsIgnoreCase("Vim25Api.HostTpmSoftwareComponentEventDetails")                                    
                                    && packageName!=null &&  packageName.trim().isEmpty()) {
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
