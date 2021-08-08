import javax.lang.model.element.PackageElement;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTag;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree;
import jdk.javadoc.internal.doclets.formats.html.markup.Navigation;

class PackageTreeWriter extends AbstractTreeWriter {
    /**
     * Get the package tree header.
     *
     * @return a content tree for the header
     */
    protected HtmlTree getPackageTreeHeader() {
	String packageName = packageElement.isUnnamed() ? "" : utils.getPackageName(packageElement);
	String title = packageName + " " + configuration.getText("doclet.Window_Class_Hierarchy");
	HtmlTree bodyTree = getBody(true, getWindowTitle(title));
	HtmlTree htmlTree = (configuration.allowTag(HtmlTag.HEADER)) ? HtmlTree.HEADER() : bodyTree;
	addTop(htmlTree);
	Content linkContent = getModuleLink(utils.elementUtils.getModuleOf(packageElement), contents.moduleLabel);
	navBar.setNavLinkModule(linkContent);
	navBar.setUserHeader(getUserHeaderFooter(true));
	htmlTree.addContent(navBar.getContent(true));
	if (configuration.allowTag(HtmlTag.HEADER)) {
	    bodyTree.addContent(htmlTree);
	}
	return bodyTree;
    }

    /**
     * Package for which tree is to be generated.
     */
    protected PackageElement packageElement;
    private final Navigation navBar;

}

