package ua.kiev.prog.shared;

public class Client {
    @Id
    private int id;

    private String name;
    private int age;

    public Client() {
    }

    public Client(String name, int age) {
        this.name = name;
        this.age = age;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Client{");
        sb.append("id=").append(id);
        if (name != null){
            sb.append(", name='").append(name).append('\'');
        }
        if (age != 0){
            sb.append(", age=").append(age);
        }
        sb.append('}');
        return sb.toString();
    }
}
