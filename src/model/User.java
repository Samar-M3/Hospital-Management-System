package model;

/**
 * Base class for all user types in the system.
 */
public abstract class User {

    private int    id;
    private String name;
    private String email;
    private String contact;

    protected User() {}

    protected User(int id, String name, String email, String contact) {
        this.id      = id;
        this.name    = name;
        this.email   = email;
        this.contact = contact;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDisplayLabel() {
        return name + " - " + (email != null ? email : "no email");
    }
}
