class ClassDeclaration implements Constants {
    /**
     * Check if the class is defined
     */
    public boolean isDefined() {
	switch (status) {
	case CS_BINARY:
	case CS_PARSED:
	case CS_CHECKED:
	case CS_COMPILED:
	    return true;
	}
	return false;
    }

    int status;

}

