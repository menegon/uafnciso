/*
* Access and use of this software shall impose the following
* obligations and understandings on the user. The user is granted the
* right, without any fee or cost, to use, copy, modify, alter, enhance
* and distribute this software, and any derivative works thereof, and
* its supporting documentation for any purpose whatsoever, provided
* that this entire notice appears in all copies of the software,
* derivative works and supporting documentation. Further, the user
* agrees to credit NOAA/NGDC in any publications that result from
* the use of this software or in any product that includes this
* software. The names NOAA/NGDC, however, may not be used
* in any advertising or publicity to endorse or promote any products
* or commercial entity unless specific written permission is obtained
* from NOAA/NGDC. The user also understands that NOAA/NGDC
* is not obligated to provide the user with any support, consulting,
* training or assistance of any kind with regard to the use, operation
* and performance of this software nor to provide the user with any
* updates, revisions, new versions or "bug fixes".
*
* THIS SOFTWARE IS PROVIDED BY NOAA/NGDC "AS IS" AND ANY EXPRESS
* OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL NOAA/NGDC BE LIABLE FOR ANY SPECIAL,
* INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
* RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
* CONTRACT, NEGLIGENCE OR OTHER TORTUOUS ACTION, ARISING OUT OF OR IN
* CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE. 
 */
package thredds.server.metadata.util;

import java.util.Date;
import java.util.List;

import thredds.server.metadata.bean.Extent;

import org.apache.commons.lang.time.DurationFormatUtils;

import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.units.DateUnit;
import ucar.nc2.units.DateFormatter;

/**
* ThreddsExtentUtil
* @author: dneufeld
* Date: June 17, 2010
*/
public class ThreddsExtentUtil {
	private static org.slf4j.Logger _log = org.slf4j.LoggerFactory
        .getLogger(ThreddsExtentUtil.class);
	
	private static Extent doGetExtent(final String url) throws Exception {
		Extent ext = null;

		try {
			NetcdfDataset ncd = NetcdfDataset.openDataset(url);
			ext = doGetExtent(ncd);
		} catch (Exception e) {
			e.printStackTrace();
			String err = "Could not load NETCDF file: " + url
					+ " because of Exception. " + e.getLocalizedMessage();
			_log.error(err, e);
		}
		return ext;
	}

	private static Extent doGetExtent(final NetcdfDataset ncd) throws Exception {
		Extent ext = new Extent();
		
		List<CoordinateAxis> coordAxes = ncd.getCoordinateAxes();
		try {
			for (CoordinateAxis coordAxis : coordAxes) {
				
				if (coordAxis.getAxisType() == AxisType.Lat) {
					//logAvailableMemory("Retrieving Lat coordAxis values");
					ext._minLat = coordAxis.getMinValue();
					ext._maxLat = coordAxis.getMaxValue();
					ext._latUnits = coordAxis.getUnitsString();
					ext._latRes = ((coordAxis.getMaxValue() - coordAxis
							.getMinValue()) / coordAxis.getSize());
				}
				if (coordAxis.getAxisType() == AxisType.Lon) {
					//logAvailableMemory("Retrieving Lon coordAxis values");
					ext._minLon = coordAxis.getMinValue();
					ext._maxLon = coordAxis.getMaxValue();
					ext._lonUnits = coordAxis.getUnitsString();
					ext._lonRes = ((coordAxis.getMaxValue() - coordAxis
							.getMinValue()) / coordAxis.getSize());
				}
				if (coordAxis.getAxisType() == AxisType.Time) {
					//logAvailableMemory("Retrieving Time coordAxis values");
					ext._minTime = Double.toString(coordAxis.getMinValue());
					ext._maxTime = Double.toString(coordAxis.getMaxValue());
					ext._timeUnits = coordAxis.getUnitsString();
					

					//Add 2/8/2011
					String rawMinTime = Double.toString(coordAxis.getMinValue());
					String rawMaxTime = Double.toString(coordAxis.getMaxValue());
					_log.debug("udunits string = " + rawMinTime + " " + ext._timeUnits);
					Date startDate = DateUnit.getStandardDate(rawMinTime + " " + ext._timeUnits);
					Date endDate = DateUnit.getStandardDate(rawMaxTime + " " + ext._timeUnits);
					DateFormatter df = new DateFormatter();
					ext._minTime = df.toDateTimeStringISO(startDate);
					ext._maxTime = df.toDateTimeStringISO(endDate);
					//End Add 2/8/2011
					
					//Revised to get ISO Duration format
					long resolution = (endDate.getTime() - startDate.getTime())/coordAxis.getSize();
					long duration = endDate.getTime() - startDate.getTime();
					ext._timeRes = DurationFormatUtils.formatDurationISO(resolution);
					ext._timeDuration = DurationFormatUtils.formatDurationISO(duration);

				}
			
				if (coordAxis.getAxisType() == AxisType.Height) {
					//logAvailableMemory("Retrieving Height coordAxis values");
					ext._minHeight = coordAxis.getMinValue();
					ext._maxHeight = coordAxis.getMaxValue();
					ext._heightUnits = coordAxis.getUnitsString();
					ext._vOrientation = coordAxis.getPositive();
					ext._heightRes = ((coordAxis.getMaxValue() - coordAxis
							.getMinValue()) / coordAxis.getSize());
				}

			}
		} catch (Exception e) {
			_log.error(e.getMessage());
		}

		return ext;
	}

	/**
	 * Creates a spatial extent based upon
	 * a given url for a NetCDFDataset.
	 * 
	 * @param url
	 *            The fully qualified path to the netCDF file. Url must point to
	 *            a valid netCDF dataset.
	 * @return a spatial extent object
	 * @throws ThreddsUtilitiesException
	 */
	public static Extent getExtent(final String url) throws Exception {
		return doGetExtent(url);
	}

	/**
	 * Creates a spatial extent based upon
	 * a given NetcdfDataset.
	 * 
	 * @param ncd a valid netCDF dataset.
	 * @return a spatial extent object
	 * @throws ThreddsUtilitiesException
	 */
	public static Extent getExtent(final NetcdfDataset ncd) throws Exception {
		return doGetExtent(ncd);
	}	
	
	private static void logAvailableMemory(String message) {
		int mb = 1024*1024;
		
		_log.debug(message);
		_log.debug("Total Memory: "+ Runtime.getRuntime().totalMemory()/mb);    
		_log.debug("Free Memory: "+ Runtime.getRuntime().freeMemory()/mb);
	}
}
