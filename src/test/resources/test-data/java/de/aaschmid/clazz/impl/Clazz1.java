package de.aaschmid.clazz.impl;

import de.aaschmid.clazz.Clazz;

public class Clazz1 implements Clazz {

    @Override
    public boolean isClazz(Clazz clazz) {
        return clazz != null;
    }
}
