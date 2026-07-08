package org.ginsim.core.utils.data;

import java.util.ArrayList;

/**
 * Help handling a list of named objects
 * @param <T> NameObject
 */
public class NamedList<T extends NamedObject> extends ArrayList<T> {

    /**
     * Pick a name (for a new object)
     * @param prefix the prefix
     * @return name as string
     */
    public String findUniqueName(String prefix) {
        String name = prefix.trim();
        int i=0;
        while (findName(name) != -1) {
            i++;
            name = prefix+i;
        }
        return name;
    }

    /**
     * Change the name of one element.
     *
     * @param idx the position of the element to rename
     * @param name the new name
     * @return true if it could be renamed
     */
    public boolean rename(int idx, String name) {
        if (isNameValid(idx, name)) {
            get(idx).setName(name);
            return true;
        }
        return false;
    }

    private int findName(String name) {
        int i=0;
        for (T element: this) {
            if (name.equals(element.getName())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public T getByName(String name) {
        int idx = findName(name);
        if (idx >= 0) {
            return get(idx);
        }
        return null;
    }

    public boolean isNameValid(int idx, String name) {
        if (name == null) {
            return false;
        }

        int i = findName(name);
        return i==-1 || i==idx;
    }
}
