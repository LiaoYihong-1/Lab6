package Lab;
import CSV.CSVReader;
import CSV.CSVWriter;
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
        int port = Integer.parseInt(args[0]);
        DatagramSocket datagramSocket = new DatagramSocket(port);

        //boolean connect = true;
        while (true) {
            //receive
            byte[] buffer = new byte[102400];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
            datagramSocket.receive(datagramPacket);
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(datagramPacket.getData()));
            //main part
            try {
                //deal with different command
                CommandPackage commandPackage = (CommandPackage) inputStream.readObject();
                if (commandPackage != null) {
                    //first load file
                    if(commandPackage.isSet()){
                        LinkedHashSet<Person> people = new LinkedHashSet<>();
                        File F = new File(commandPackage.getFileName());
                        if(!F.exists()){
                            F.createNewFile();
                            new CSVWriter().writetofile(people,commandPackage.getFileName());
                        }else {
                            people = new CSVReader().readfile(people,commandPackage.getFileName());
                        }
                        System.out.printf("The port of client is %d\n",datagramPacket.getPort());
                        CollectionsofPerson.setPeople(people);
                        manager.setOut("Your collection is synchronized by file\n",false);
                        FirstResponse response = new FirstResponse(people,manager.getOut());
                        ByteArrayOutputStream BAO = new ByteArrayOutputStream();
                        ObjectOutputStream OOS = new ObjectOutputStream(BAO);
                        OOS.writeObject(response);
                        DatagramPacket send = new DatagramPacket(BAO.toByteArray(),0,BAO.toByteArray().length,datagramPacket.getSocketAddress());
                        datagramSocket.send(send);
                        System.out.print(manager.getOut());
                        System.out.print("\n");
                        OOS.close();
                        //for commands except executescript
                    } else if (commandPackage.getList()==null&!commandPackage.isSet()) {
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
                        byte [] data = manager.getOut().getBytes();
                        DatagramPacket send = new DatagramPacket(data,data.length,datagramPacket.getSocketAddress());
                        datagramSocket.send(send);
                        System.out.print(manager.getOut());
                        System.out.print("\n");
                        //for command executescript
                    }else if(!commandPackage.isSet()){
                        StringBuilder S = new StringBuilder();
                        List<CommandPackage> list = commandPackage.getList();
                        Iterator<CommandPackage> iterator = list.iterator();
                        while(iterator.hasNext()){
                            CommandPackage used = iterator.next();
                            AbstractCommand command = used.getAbstractCommand();
                            new History().getHistory().add(command.getName()+"\n");
                            command.execute(manager,used);
                            S.append(manager.getOut()).append("\n");
                        }
                        manager.setOut(S.toString(),false);
                        byte [] data = manager.getOut().getBytes();
                        DatagramPacket send = new DatagramPacket(data,data.length,datagramPacket.getSocketAddress());
                        datagramSocket.send(send);
                        System.out.print(manager.getOut());
                        System.out.print("\n");
                    }
                }
            }catch (ClassNotFoundException|ParaInapproException|NullException C) {
                manager.setOut(C.getMessage(),false);
            }
        }
    }

    private static LinkedHashSet<Person> sort(LinkedHashSet<Person> linkedHashSet) throws NullException{
        LinkedHashSet<Person> newone = new LinkedHashSet<>();
        Comparator<Person> comparator;
        comparator = Comparator.comparingInt(a -> a.getId());
        linkedHashSet.stream().sorted(comparator).forEach(newone::add);
        return newone;
    }
}
