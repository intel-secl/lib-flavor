package com.intel.mtwilson.core.flavor.common;

import com.intel.mtwilson.core.flavor.model.Software;
import com.intel.wml.measurement.xml.MeasurementType;
import com.intel.wml.measurement.xml.Measurement;

import java.util.LinkedHashMap;
/**
 *
 * @author ddhawal
 */
public class SoftwareFlavorUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SoftwareFlavorUtil.class);

    public static Software getSoftware(Measurement measurements) {
        LinkedHashMap<String, MeasurementType> measurementMap = new LinkedHashMap<>();
        String measuredPath;
        for(MeasurementType mt : measurements.getMeasurements()) {
            measuredPath = mt.getPath().replace("/", "-");
            if (measuredPath.charAt(measuredPath.length() - 1) == '-'){
                measuredPath = measuredPath.substring(1, mt.getPath().length() - 1);
            } else {
                measuredPath = measuredPath.substring(1);
            }
            measurementMap.put(measuredPath, mt);
        }
        Software software = new Software();
        software.setMeasurements(measurementMap);
        software.setCumulativeHash(measurements.getCumulativeHash().getValue());
        return software;
    }
}
