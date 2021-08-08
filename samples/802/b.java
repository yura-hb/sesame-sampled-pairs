import javax.lang.model.element.PackageElement;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;

class HtmlDocletWriter {
    /**
     * Given a package, return the name to be used in HTML anchor tag.
     * @param packageElement the package.
     * @return the name to be used in HTML anchor tag.
     */
    public String getPackageAnchorName(PackageElement packageElement) {
	return packageElement == null || packageElement.isUnnamed() ? SectionName.UNNAMED_PACKAGE_ANCHOR.getName()
		: utils.getPackageName(packageElement);
    }

    protected final Utils utils;

}

