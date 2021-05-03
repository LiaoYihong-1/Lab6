package Lab;

import Collection.Person;

import java.io.Serializable;
import java.util.LinkedHashSet;

public class FirstResponse implements Serializable {

    FirstResponse(LinkedHashSet<Person> people,String managerout){
        this.people = people;
        this.managerout = managerout;
    }

    private final LinkedHashSet<Person> people;

    private final String managerout;

    public LinkedHashSet<Person> getPeople() {
        return people;
    }

    public String getManagerout() {
        return managerout;
    }
}
