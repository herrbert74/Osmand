package net.osmand.plus.utils;

import android.annotation.TargetApi;
import net.osmand.PlatformUtil;
import org.apache.commons.logging.Log;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author Koen Rabaey
 */
public class Kml2Gpx {

	public static final Log LOG = PlatformUtil.getLog(Kml2Gpx.class);

	@TargetApi(8)
	public static String toGpx(final InputStream kml) {
		try {
			final Source xmlSource = new StreamSource(kml);
			final Source xsltSource = new StreamSource(Kml2Gpx.class.getResourceAsStream("kml2gpx.xslt"));

			final StringWriter sw = new StringWriter();

			TransformerFactory.newInstance().newTransformer(xsltSource).transform(xmlSource, new StreamResult(sw));

			return sw.toString();

		} catch (TransformerConfigurationException e) {
			LOG.error(e.toString(), e);
		} catch (TransformerFactoryConfigurationError e) {
			LOG.error(e.toString(), e);
		} catch (TransformerException e) {
			LOG.error(e.toString(), e);
		}

		return null;
	}
}