import java.util.Iterator;

class RoleResult implements Serializable {
    /**
     * Sets list of roles successfully accessed.
     *
     * @param list  list of roles successfully accessed
     *
     * @see #getRoles
     */
    public void setRoles(RoleList list) {
	if (list != null) {

	    roleList = new RoleList();

	    for (Iterator&lt;?&gt; roleIter = list.iterator(); roleIter.hasNext();) {
		Role currRole = (Role) (roleIter.next());
		roleList.add((Role) (currRole.clone()));
	    }
	} else {
	    roleList = null;
	}
	return;
    }

    /**
     * @serial List of roles successfully accessed
     */
    private RoleList roleList = null;

}

