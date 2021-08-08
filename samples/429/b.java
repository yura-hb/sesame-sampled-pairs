import java.awt.*;

abstract class XRSurfaceData extends XSurfaceData {
    /**
     * Returns the XRender SurfaceType which is able to fullfill the specified
     * transparency requirement.
     */
    public static SurfaceType getSurfaceType(XRGraphicsConfig gc, int transparency) {
	SurfaceType sType = null;

	switch (transparency) {
	case Transparency.OPAQUE:
	    sType = XRSurfaceData.IntRgbX11;
	    break;

	case Transparency.BITMASK:
	case Transparency.TRANSLUCENT:
	    sType = XRSurfaceData.IntArgbPreX11;
	    break;
	}

	return sType;
    }

    public static final SurfaceType IntRgbX11 = SurfaceType.IntRgb.deriveSubType(DESC_INT_RGB_X11);
    public static final SurfaceType IntArgbPreX11 = SurfaceType.IntArgbPre.deriveSubType(DESC_INT_ARGB_X11);

}

