import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

class Element extends AccessibleObject implements Member {
    /** Returns true if the element is package-private. */
    public final boolean isPackagePrivate() {
	return !isPrivate() && !isPublic() && !isProtected();
    }

    private final Member member;

    /** Returns true if the element is private. */
    public final boolean isPrivate() {
	return Modifier.isPrivate(getModifiers());
    }

    /** Returns true if the element is public. */
    public final boolean isPublic() {
	return Modifier.isPublic(getModifiers());
    }

    /** Returns true if the element is protected. */
    public final boolean isProtected() {
	return Modifier.isProtected(getModifiers());
    }

    @Override
    public final int getModifiers() {
	return member.getModifiers();
    }

}

