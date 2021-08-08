import java.util.Objects;

class ImageFileCreator {
    /**
     * Returns the path of the resource.
     */
    public static String resourceName(String path) {
	Objects.requireNonNull(path);
	String s = path.substring(1);
	int index = s.indexOf("/");
	return s.substring(index + 1);
    }

}

