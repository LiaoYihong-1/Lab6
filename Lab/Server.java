package Lab;
import Collection.*;
import Command.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public static void main (String [] args) throws IOException{
        //initialize collection of people
        new CollectionsofPerson().doInitialization();
        CommandManager manager = new CommandManager();
        DatagramSocket datagramSocket = new DatagramSocket(5555);

        while (true) {
            //receive
            byte[] buffer = new byte[102400];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
            datagramSocket.receive(datagramPacket);
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(datagramPacket.getData()));
            //main part
            try {
                //deal with different command
                runningcommand(inputStream,manager,datagramSocket,datagramPacket);
            }catch (ClassNotFoundException|ParaInapproException|NullException C) {
                manager.setOut(C.getMessage(),false);
            }

            //send
            byte [] data = manager.getOut().getBytes();
            DatagramPacket send = new DatagramPacket(data,data.length,datagramPacket.getSocketAddress());
            datagramSocket.send(send);
        }
    }

    private static LinkedHashSet<Person> sort(LinkedHashSet<Person> linkedHashSet) throws NullException{
        LinkedHashSet<Person> newone = new LinkedHashSet<>();
        Comparator<Person> comparator;
        comparator = Comparator.comparingInt(a -> a.getId());
        linkedHashSet.stream().sorted(comparator).forEach(newone::add);
        return newone;
    }

    public static void runningcommand(ObjectInputStream inputStream,CommandManager manager,DatagramSocket datagramSocket,DatagramPacket datagramPacket)throws ClassNotFoundException,NullException,IOException{
        CommandPackage commandPackage = (CommandPackage) inputStream.readObject();
        if (commandPackage != null) {
            if(commandPackage.isSet()){
                CollectionsofPerson.setPeople(commandPackage.getLinkedHashSet());
                manager.setOut("Your collection is synchronized by file\n",false);
            } else if (commandPackage.getList()==null) {
                AbstractCommand command = commandPackage.getAbstractCommand();
                new History().getHistory().add(command.getName() + "\n");
                if (command.getName().equalsIgnoreCase("exit")) {
                    command.execute(manager,commandPackage);
                    datagramSocket.send(new DatagramPacket(manager.getOut().getBytes(), manager.getOut().getBytes().length, datagramPacket.getSocketAddress()));
                    new Save().execute(manager, commandPackage);
                    System.exit(2);
                } else {
                    command.execute(manager, commandPackage);
                    if (command.getName().equalsIgnoreCase("updateid")) {
                        CollectionsofPerson.setPeople(sort(new CollectionsofPerson().getPeople()));
                    }
                }
            }else{
                String S = "";
                List<CommandPackage> list = commandPackage.getList();
                Iterator<CommandPackage> iterator = list.iterator();
                while(iterator.hasNext()){
                    CommandPackage used = iterator.next();
                    AbstractCommand command = used.getAbstractCommand();
                    new History().getHistory().add(command.getName()+"\n");
                    command.execute(manager,used);
                    S=S+manager.getOut()+"\n";
                }
                manager.setOut(S,false);
            }
        }
    }
}
