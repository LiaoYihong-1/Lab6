package Lab;

import Command.AbstractCommand;
import Collection.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class CommandPackage implements Serializable {
    CommandPackage(){
        this.arg = null;
        this.abstractCommand = null;
        this.FileName = null;
        this.P = null;
        this.linkedHashSet = null;
        this.list = null;
    }

    CommandPackage(String[] arg, AbstractCommand command,String FileName){
        this.arg = arg;
        this.abstractCommand = command;
        this.FileName = FileName;
        this.P = null;
        this.linkedHashSet = null;
        this.list = null;
    }

    CommandPackage(AbstractCommand command,Person people,String FileName){
        this.FileName = FileName;
        this.P = people;
        this.arg = null;
        this.abstractCommand = command;
        this.linkedHashSet = null;
        this.list = null;
    }

    CommandPackage(AbstractCommand command,LinkedHashSet<Person> linkedHashSet,String FileName){
        this.FileName = FileName;
        this.linkedHashSet=linkedHashSet;
        this.abstractCommand = command;
        this.arg = null;
        this.P=null;
        this.list = null;
    }

    CommandPackage (String[] S,AbstractCommand command, Person P,String FileName){
        this.FileName = FileName;
        this.arg = S;
        this.P=P;
        this.abstractCommand = command;
        this.linkedHashSet =null;
        this.list = null;
    }

    CommandPackage(LinkedHashSet<Person> linkedHashSet,String FileName){
        this.FileName = FileName;
        this.linkedHashSet = linkedHashSet;
        this.arg = null;
        this.abstractCommand = null;
        this.P = null;
        this.list = null;
    }

    CommandPackage(ArrayList<CommandPackage> list){
        this.list = list;
        this.abstractCommand = null;
        this.linkedHashSet = null;
        this.arg = null;
        this.FileName = null;
        this.P = null;
    }

    private final AbstractCommand abstractCommand;
    private final String[] arg;
    private final Person P;
    private final LinkedHashSet<Person> linkedHashSet;
    private static final long serialVersionUID = 1L;
    private final String FileName;
    private final ArrayList<CommandPackage> list;

    public String getFileName(){
        return this.FileName;
    }

    public AbstractCommand getAbstractCommand(){
        return this.abstractCommand;
    }

    public String[] getArg(){
        return this.arg;
    }

    public Person getPerson() {
        return P;
    }

    public LinkedHashSet<Person> getLinkedHashSet(){
        return linkedHashSet;
    }

    public ArrayList<CommandPackage> getList(){
        return this.list;
    }

}
