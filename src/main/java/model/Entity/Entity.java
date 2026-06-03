package model.Entity;

import java.io.Serializable;

public abstract class Entity implements Serializable {

    public abstract String getId();

    public abstract String getSummary();
}