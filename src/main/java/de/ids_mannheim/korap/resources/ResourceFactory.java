package de.ids_mannheim.korap.resources;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class ResourceFactory {

    public static final List<Class<? extends KustvaktResource>> subTypes = new ArrayList<>();
    public static final int CORPUS = 0;
    public static final int FOUNDRY = 1;
    public static final int LAYER = 2;
    public static final int VIRTUALCOLLECTION = 3;
    public static final int USERQUERY = 4;

    static {
        subTypes.add(CORPUS, Corpus.class);
        subTypes.add(FOUNDRY, Foundry.class);
        subTypes.add(LAYER, Layer.class);
        subTypes.add(VIRTUALCOLLECTION, VirtualCollection.class);
//        subTypes.add(USERQUERY, UserQuery.class);
    }

    public static KustvaktResource getResource(
            Class<? extends KustvaktResource> clazz) {
        try {
            return (KustvaktResource) clazz.newInstance();
        }catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getResourceMapping(Class<? extends KustvaktResource> r) {
        int value = -1;
        if (r != null) {
            for (int i = 0; i < subTypes.size(); i++) {
                if (subTypes.get(i).getName().equals(r.getName()))
                    value = i;
            }
        }
        return value;
    }

    public static KustvaktResource getResource(String type) {
        return getResource(getResourceClass(type));
    }

    public static KustvaktResource getResource(int j) {
        Class s = subTypes.get(j);
        if (s != null) {
            return getResource(s);
        }
        return null;
    }


    public static <T extends KustvaktResource> Class<T> getResourceClass(
            String type) {
        for (Class value : subTypes) {
            if (value == VirtualCollection.class && type
                    .equalsIgnoreCase("collection"))
                return (Class<T>) VirtualCollection.class;
                    //todo
//            else if (value == UserQuery.class && type.equalsIgnoreCase("query"))
//                return (Class<T>) UserQuery.class;
            else if (value.getSimpleName().equalsIgnoreCase(type.trim())) {
                return value; // do nothing
            }
        }
        return null;
    }
    // all deprecated!

    public static VirtualCollection getCachedCollection(String query) {
        VirtualCollection v = new VirtualCollection();
        v.setQuery(query);
        v.setName("");
        v.setDescription("");
        v.setPersistentID(v.createID());
        return v;
    }

    public static VirtualCollection getPermanentCollection(
            VirtualCollection mergable, String corpusName, String description,
            Integer owner) {
        VirtualCollection v = new VirtualCollection();
        v.merge(mergable);
        v.setPersistentID(v.createID());
        v.setName(corpusName);
        v.setDescription(description);
        v.setOwner(owner);
        return v;
    }

    public static VirtualCollection createCollection(String name, String query,
            Integer owner) {
        VirtualCollection v = new VirtualCollection();
        v.setName(name);
        v.setQuery(query);
        v.setOwner(owner);
        return v;
    }

    public static VirtualCollection createCollection(String name, String query,
            long time, Integer owner) {
        VirtualCollection v = new VirtualCollection(0, owner, time);
        v.setName(name);
        v.setQuery(query);
        return v;
    }

    public static VirtualCollection getCollection(Integer collectionID,
            boolean cache) {
        VirtualCollection v = new VirtualCollection();
        v.setId(collectionID);
        v.setDescription("");
        v.setName("");
        return v;
    }

    public static VirtualCollection createContainer(String name,
            String description, String query, Integer owner) {
        VirtualCollection v = new VirtualCollection();
        v.setName(name);
        v.setDescription(description);
        v.setQuery(query);
        v.setOwner(owner);
        v.setManaged(true);
        v.setPersistentID(v.createID());
        return v;
    }

    public static VirtualCollection getIDContainer(Integer id, Integer owner) {
        return new VirtualCollection(id, owner);
    }
}