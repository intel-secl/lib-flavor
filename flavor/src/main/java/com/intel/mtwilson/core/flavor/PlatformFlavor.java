/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor;

import com.intel.mtwilson.core.flavor.model.SignedFlavor;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author ssbangal
 */
public abstract class PlatformFlavor {
    
    /**
     * Extracts the details of the flavor part requested by the caller from the host report used during the creation of the 
     * PlatformFlavor instance. This is an abstract method that the sub-classes need to implement.
     * @param flavorPartName of the flavor part to be extracted from the host report
     * @return Flavor part as a JSON string
     * @throws Exception If the flavor part specified is not supported or any other system exceptions.
     * @since IAT 1.0
     * <pre>testing</pre>
     */
    public abstract List<String> getFlavorPart(String flavorPartName) throws Exception;

    /**
     * Extracts the details of the flavor part requested by the caller from the host report used during the creation of the
     * PlatformFlavor instance and it's corresponding signature. This is an abstract method that the sub-classes need to implement.
     * @param flavorPartName of the flavor part to be extracted from the host report
     * @return Flavor part and corresponding signature as a JSON string
     * @throws Exception If the flavor part specified is not supported or any other system exceptions.
     * @since IAT 1.0
     * <pre>testing</pre>
     */
    public abstract List<SignedFlavor> getFlavorPartWithSignature(String flavorPartName) throws Exception;

    /**
     * Retrieves the list of flavor parts that can be obtained using the getFlavorPart method.
     * This is an abstract method that the sub-classes need to implement.
     * @return Collection of the flavor parts
     * @throws Exception If the flavor parts supported cannot be retrieved.
     * @since IAT 1.0
     */
    public abstract Collection<String> getFlavorPartNames() throws Exception;
        
}
