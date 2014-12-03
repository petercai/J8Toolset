package cai.peter.eclipse.j8marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.internal.ide.IMarkerImageProvider;

public class J8markerImageProvider implements IMarkerImageProvider {
    public J8markerImageProvider() {
        super();
    }

    public String getImagePath(IMarker marker) {
        String iconPath = "icons/";//$NON-NLS-1$
        int markerNumber = marker.getAttribute("number", -1);
        if (markerNumber >= 0) {
            return iconPath + "marker" + markerNumber + ".gif"; //$NON-NLS-1$//$NON-NLS-2$
        }
        return null;
    }
}