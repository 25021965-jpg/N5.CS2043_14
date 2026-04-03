import java.io.Sarializable; 
abstract class Entity implement Serializable { 
    protected String id; 
    public Entity(String id) {
        this.id = id; 
    }
    public String getId() { 
        return id; 
    }
}