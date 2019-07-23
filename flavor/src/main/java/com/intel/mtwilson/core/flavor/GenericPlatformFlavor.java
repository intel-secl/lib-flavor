/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor;

import com.intel.mtwilson.core.flavor.common.ErrorCode;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorException;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorUtil;
import com.intel.mtwilson.core.flavor.model.*;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import java.util.*;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.ASSET_TAG;
import static com.intel.mtwilson.core.flavor.common.PlatformFlavorUtil.getSignedFlavorList;

/**
 *
 * @author ssbangal
 */
public class GenericPlatformFlavor extends PlatformFlavor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GenericPlatformFlavor.class);
    private X509AttributeCertificate tagCertificate;
    private String vendor;

    public GenericPlatformFlavor(String vendor, X509AttributeCertificate tagCertificate) {
        this.vendor = vendor;
        this.tagCertificate = tagCertificate;
    }

    @Override
    public List<SignedFlavor> getFlavorPartWithSignature(String name) throws Exception {
        List<String> flavors = getFlavorPart(name);
        return getSignedFlavorList(flavors);
    }

    @Override
    public List<String> getFlavorPart(String name) throws Exception {
        try {
            String flavorPartName = name.toUpperCase();
            switch (FlavorPart.valueOf(flavorPartName)) {
                case ASSET_TAG:
                    return getAssetTagFlavor();
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
        return new ArrayList(Arrays.asList(ASSET_TAG.getValue()));
    }

    /**
     * Retrieves the asset tag part of the flavor including the certificate and
     * all the key-value pairs that are part of the certificate.
     *
     * @return
     * @throws Exception
     */
    private List<String> getAssetTagFlavor() throws PlatformFlavorException {

        try {
            List<String> assetTagFlavors = new ArrayList();
            Flavor flavor = new Flavor(
                    PlatformFlavorUtil.getMetaSectionDetails(null, tagCertificate, null, ASSET_TAG, vendor),
                    null,
                    null,
                    null,
                    PlatformFlavorUtil.getExternalConfigurationDetails(null, tagCertificate), null);
            assetTagFlavors.add(Flavor.serialize(flavor));
            return assetTagFlavors;
        } catch (PlatformFlavorException pex) {
            throw pex;
        } catch (Exception ex) {
            String errorMessage = "Error during creation of Host Aggregated flavor.";
            log.error(errorMessage, ex);
            throw new PlatformFlavorException(ErrorCode.SYSTEM_ERROR, errorMessage, ex);
        }
    }

}
