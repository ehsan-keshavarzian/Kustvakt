package de.ids_mannheim.korap.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.ids_mannheim.korap.utils.TimeUtils;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanl on 5/21/14.
 */
@Getter
@Setter
public abstract class KustvaktResource {

    @JsonIgnore
    private Integer id;
    private String persistentID;
    private String name;
    private String description;
    @JsonIgnore
    private Integer owner;
    protected long created;
    @Deprecated
    private boolean managed;
    @Deprecated
    private boolean shared;
    private String path;
    // parents persistentid
    private String parentID;

    //    private static RandomStringUtils utils = new RandomStringUtils();

    protected KustvaktResource() {
        this.created = TimeUtils.getNow().getMillis();
        this.id = -1;
        this.parentID = null;
    }

    public KustvaktResource(Integer id, int creator, long created) {
        this.created = created;
        this.owner = creator;
        this.id = id;
        this.parentID = null;
    }

    public KustvaktResource(Integer id, int creator) {
        this.created = TimeUtils.getNow().getMillis();
        this.id = id;
        this.owner = creator;
        this.parentID = null;
    }

    public KustvaktResource(String persistentID, int creator) {
        this();
        this.owner = creator;
        this.persistentID = persistentID;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof KustvaktResource && this.id
                .equals(((KustvaktResource) other).getId());
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        return result;
    }

    /**
     * Merges another resource with this resource instance
     * Every implementation of KorAPResource should override this method!
     *
     * @param other
     */
    public void merge(KustvaktResource other) {
        if (other == null)
            return;

        this.setId(this.getId() == null || this.getId() == -1 ?
                other.getId() :
                other.getId());
        this.setPersistentID(this.getPersistentID() == null ?
                other.getPersistentID() :
                this.getPersistentID());
        this.setName(this.getName() == null || this.getName().isEmpty() ?
                other.getName() :
                this.getName());
        this.setDescription(
                this.getDescription() == null || this.getDescription()
                        .isEmpty() ?
                        other.getDescription() :
                        this.getDescription());
        this.setCreated(this.getCreated() < other.getCreated() ?
                this.getCreated() :
                other.getCreated());
        this.setPath(this.getPath() == null ? other.getPath() : this.getPath());
        this.setOwner(
                this.getOwner() == null ? other.getOwner() : this.getOwner());
        this.setManaged(
                !this.isManaged() ? other.isManaged() : this.isManaged());
        this.setShared(!this.isShared() ? other.isShared() : this.isShared());
    }

    /**
     * Checks this instance for null parameter and replaces them with default values.
     * Every implementation of KorAPResource should override this method!
     *
     * @return
     */
    public void checkNull() {
        setCreated(this.getCreated() == 0L ?
                TimeUtils.getNow().getMillis() :
                this.getCreated());
        setName(this.getName() == null ? "" : this.getName());
    }

    protected String createID() {
        //        return utils.randomAlphanumeric(10);
        return "";
    }

    /**
     * this method is used to return field information about the class
     * All subclasses should override this method
     *
     * @return
     */
    public Map toMap() {
        Map m = new HashMap();
        m.put("id", persistentID);
        m.put("name", name);
        m.put("path", path);
        m.put("description", description);
        m.put("created", TimeUtils.format(new DateTime(created)));
        m.put("managed", managed);
        m.put("shared", shared);
        return m;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id='" + id + '\'' +
                "persistentID='" + persistentID + '\'' +
                ", name='" + name + '\'' +
                ", created=" + created +
                ", path=" + path +
                ", owner=" + owner +
                '}';
    }

    @Getter
    public static class Container {
        private final Class type;
        //        private final Integer id;
        private final String persistentID;
        private final boolean set;

        public Container(String persistentID, Class type) {
            this.type = type;
            //            this.id = id;
            this.set = true;
            this.persistentID = persistentID;
        }

        public Container(Class type) {
            this.type = type;
            //            this.id = id;
            this.set = true;
            this.persistentID = null;
        }

        public Container() {
            this.set = false;
            this.type = null;
            //            this.id = null;
            this.persistentID = null;
        }

        @Override
        public String toString() {
            return persistentID + "@" + type.getSimpleName();
        }
    }

}