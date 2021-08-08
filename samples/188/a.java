abstract class Entity {
    class InternalEntity extends Entity {
	/** Sets the values of the entity. */
	public void setValues(InternalEntity entity) {
	    super.setValues(entity);
	    text = entity.text;
	}

	/** Text value of entity. */
	public String text;

    }

    /** Entity name. */
    public String name;
    public boolean inExternalSubset;

    /** Sets the values of the entity. */
    public void setValues(Entity entity) {
	name = entity.name;
	inExternalSubset = entity.inExternalSubset;
    }

}

