package org.mycore.plugins.datamodel;

public class LayoutDefinition {

    private String objectType;

    private String layouts[];

    /**
     * @return the objectType
     */
    protected final String getObjectType() {
        return objectType;
    }

    /**
     * @param objectType the objectType to set
     */
    protected final void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * @return the layout
     */
    protected final String[] getLayouts() {
        return layouts;
    }

    /**
     * @param layouts the layouts to set
     */
    protected final void setLayouts(String[] layouts) {
        this.layouts = layouts;
    }
}
