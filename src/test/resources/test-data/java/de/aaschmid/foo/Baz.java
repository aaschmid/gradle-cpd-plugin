package de.aaschmid.foo;

public class Baz {

    private Integer integer;

    public Baz(Integer integer) {
        this.integer = integer;
    }

    public String baz() {
        return integer.toString();
    }
}