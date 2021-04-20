package Lab;
import CSV.CSVReader;
import Collection.*;
import Command.*;
import Tools.Tools;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class Client {
    public static void main(String[] args) throws IOException{
        Selector selector = Selector.open();
        //initialize id with file, make sure the property of id

        //create channel
        new CollectionsofPerson().doInitialization();
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress("localhost",5664));

        //prepare
        int idset = 0;
        LinkedHashSet<Person> People = new LinkedHashSet<>();
        File f = new File("Person.csv");
        if(!f.exists()){
            f.createNewFile();
        }
        new CSVReader().ReadFile(People,"Person.csv");
        CollectionsofPerson.setPeople(People);
        Iterator<Person> preiterator = People.iterator();
        Person preP;
        while(preiterator.hasNext()){
            if(idset <= (preP = preiterator.next()).getId()){
                idset = preP.getId();
            }
        }
        Person.idcode = idset;

        //send
        CommandPackage firstset = new CommandPackage(People,"Person.csv");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
        outputStream.writeObject(firstset);
        channel.send(ByteBuffer.wrap(buffer.toByteArray(), 0, buffer.toByteArray().length), new InetSocketAddress("localhost", 5555));
        outputStream.close();

        channel.register(selector,SelectionKey.OP_READ|SelectionKey.OP_WRITE);

        set:while(selector.select() > 0){
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if(selectionKey.isReadable()){
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024*10);
                    channel.receive(byteBuffer);
                    System.out.print(new String(byteBuffer.array(),0,byteBuffer.array().length));
                    byteBuffer.clear();
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    break set;
                }
            }
        }
        
        CommandManager commandManager = new CommandManager();
        while (true) {
            if (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isWritable()) {
                        System.out.print("input you command:\n");
                        String[] commarg = Tools.Input().split(" ");
                        //make sure command exists
                        boolean exist = false;
                        AbstractCommand command;
                        AbstractCommand abstractCommand;
                        CommandPackage commandPackage = null;
                        Iterator<AbstractCommand> commandIterator = new CommandManager().getCommands().iterator();
                        while (commandIterator.hasNext()) {
                            if ((abstractCommand = commandIterator.next()).getName().equalsIgnoreCase(commarg[0])) {
                                command = abstractCommand;
                                exist = true;
                                //initialize commandPackage
                                try {
                                    //Packing different command and their parameters
                                    if (!command.getName().equalsIgnoreCase("executeScript")) {
                                        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
                                        commandPackage = PackCommand(commarg, commandManager, commandPackage, command,"Person.csv");
                                        ByteArrayOutputStream BAO = new ByteArrayOutputStream();
                                        ObjectOutputStream OS = new ObjectOutputStream(BAO);
                                        OS.writeObject(commandPackage);
                                        datagramChannel.send(ByteBuffer.wrap(BAO.toByteArray(), 0, BAO.toByteArray().length), new InetSocketAddress("localhost", 5555));
                                        OS.close();
                                        key.interestOps(SelectionKey.OP_READ);
                                    }else{
                                        DatagramChannel datagramChannel =(DatagramChannel) key.channel();
                                        ArrayList<CommandPackage> packageList = new ArrayList<>();
                                        ArrayList<AbstractCommand> commandList = new ArrayList<>();
                                        ArrayList<String> commandnameList = new ArrayList<>();
                                        ForScript(commarg,commandnameList);
                                        Iterator<String> iterator1 = commandnameList.iterator();
                                        while(iterator1.hasNext()){
                                            AbstractCommand abstractCommand1 ;
                                            String S = iterator1.next();
                                            Iterator<AbstractCommand> abstractCommandIterator = commandManager.getCommands().iterator();
                                            while(abstractCommandIterator.hasNext()){
                                                if((abstractCommand1=abstractCommandIterator.next()).getName().equalsIgnoreCase(S.split(" ")[0])){
                                                    commandList.add(abstractCommand1);
                                                }
                                            }
                                        }
                                        Iterator<AbstractCommand> commandIterator1 = commandList.iterator();
                                        Iterator<String> stringIterator = commandnameList.iterator();
                                        while(commandIterator1.hasNext()){
                                            packageList.add(PackCommand(stringIterator.next().split(" "),commandManager,new CommandPackage(),commandIterator1.next(),"Person.csv"));
                                        }
                                        CommandPackage finalone =new CommandPackage(packageList);
                                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                        ObjectOutputStream oos = new ObjectOutputStream(bao);
                                        oos.writeObject(finalone);
                                        datagramChannel.send(ByteBuffer.wrap(bao.toByteArray(),0,bao.toByteArray().length),new InetSocketAddress("localhost",5555));
                                        oos.close();
                                        key.interestOps(SelectionKey.OP_READ);
                                    }
                                } catch (ParaInapproException | ValueTooSmallException | ValueTooBigException | NullException P)/*when parameter more than one,because even the commands with parameter only accept one para*/ {
                                    System.out.print(P.getMessage());
                                } catch (NumberFormatException N) {
                                    System.out.print("Next time remember enter a number\n");
                                } catch (IllegalArgumentException I) {
                                    System.out.print("remember enter a color,which can be found in the list\n");
                                }catch (FileNotFoundException F){
                                    System.out.print(F.getMessage()+"\n");
                                }
                            }
                        }
                        if(!exist){
                            System.out.print("No such a command, try again\n");
                        }
                    } else if (key.isReadable()) {
                        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
                        ByteBuffer buffer1 = ByteBuffer.allocate(102400);
                        buffer1.clear();
                        datagramChannel.receive(buffer1);
                        buffer1.flip();
                        String E = new String(buffer1.array(), 0, 4, StandardCharsets.UTF_8);
                        String Print = new String(buffer1.array());
                        if (E.equalsIgnoreCase("Exit")) {
                            channel.close();
                            System.exit(2);
                        } else {
                            System.out.print(Print);
                        }
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }

    public static CommandPackage PackCommand(String [] commarg,CommandManager commandManager,CommandPackage commandPackage,AbstractCommand command,String FileName) throws IOException,ParaInapproException{
        if (commarg.length == 2) {
            String name = command.getName();
            //all command don't accept any para
            if (name.equals(new Add().getName()) || name.equals(new Addifmin().getName()) || name.equals(new Average().getName()) || name.equals(new Clear().getName()) || name.equals(new Exit().getName()) || name.equals(new Help().getName()) || name.equals(new History().getName()) || name.equals(new Info().getName()) || name.equals(new Print().getName()) || name.equals(new Show().getName()) || name.equals(new Save().getName())) {
                throw new ParaInapproException("this command don't accept parameter\n");
            }
            //for command Removebyid
            if (commarg[0].equalsIgnoreCase("Removebyid")) {
                try {
                    Person P = commandManager.findByid(Integer.valueOf(commarg[1]));
                    commandPackage = new CommandPackage(command, P,FileName);
                } catch (ParaInapproException P) {
                    System.out.print(P.getMessage());
                }
            }//for command Removegreater
            else if (commarg[0].equalsIgnoreCase("Removegreater")) {
                Person P = commandManager.findByid(Integer.valueOf(commarg[1]));
                commandPackage = new CommandPackage(command, P,FileName);
            }//for command Removebyeyecolor
            else if (commarg[0].equalsIgnoreCase("removeeyecolor")) {
                LinkedHashSet<Person> linkedHashSet = commandManager.findbyEye(commarg[1]);
                commandPackage = new CommandPackage(command, linkedHashSet,FileName);
            } else if (commarg[0].equalsIgnoreCase("UpdateID")) {
                new CollectionsofPerson().doInitialization();
                if(new CollectionsofPerson().getPeople().size() == 0){
                    throw new NullException("Empty collection\n");
                }
                Person after = Person.PeopleCreate();
                Person.balaceicode();
                String[] I = {commarg[1]};
                commandPackage = new CommandPackage(I, command, after,FileName);
            }else if(commarg[0].equalsIgnoreCase("Executescript")){
                File F = new File(commarg[1]);
                if(!F.exists()){
                    throw new FileNotFoundException("no such a file,choose a available script please\n");
                }else {
                    commandPackage = new CommandPackage(commarg,command,FileName);
                }
            }
            else {
                commandPackage = new CommandPackage(commarg, command,FileName);
            }
        } else if (commarg.length == 1) {
            //for command Add and Addifmin
            if(commarg[0].equalsIgnoreCase("Removegreater")||commarg[0].equalsIgnoreCase("Removebyid")||commarg[0].equalsIgnoreCase("updateid")||commarg[0].equalsIgnoreCase("removeeyecolor")){
                throw new ParaInapproException("this command accept 1 parameter\n");
            } else if (commarg[0].equalsIgnoreCase("add") || commarg[0].equalsIgnoreCase("addifmin")) {
                Person P = Person.PeopleCreate();
                commandPackage = new CommandPackage(command, P,FileName);
            } else {
                commandPackage = new CommandPackage(commarg, command,FileName);
            }
        } else {
            throw new ParaInapproException("Commands only accept one parameter\n");
        }
        return commandPackage;
    }

    /**
     * this command is set for analysing script. It will open all script and create a list of all commands in scripts.
     * @param commarg String [],
     * @param commandnameList Arraylist<String>, where save list
     * @return
     * @throws IOException
     */
    public static ArrayList<String> ForScript(String [] commarg,ArrayList<String> commandnameList)throws IOException{
        commandnameList.add(commarg[0]+" "+commarg[1]);
        FileReader fileReader = new FileReader(new File(commarg[1]));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String s;
        boolean found = false;
        while((s = bufferedReader.readLine())!=null){
            AbstractCommand abstractCommand1;
            String [] judge = s.split(" ");
            if(judge.length>=3){
                throw new ParaInapproException("Make sure that your commands in a proper format\n");
            }
            Iterator<AbstractCommand> iterator1 = new CommandManager().getCommands().iterator();
            while(iterator1.hasNext()){
                if((abstractCommand1 = iterator1.next()).getName().equalsIgnoreCase(judge[0])){
                    found = true;
                    break;
                }
            }
            if(!found){
                throw new NullException("Make sure that every command is available in your script\n");
            }
            if(!judge[0].equalsIgnoreCase("executescript")) {
                commandnameList.add(s);
            }else {
                if(judge[1].equals(commarg[1])){
                    continue;
                }else {
                    ForScript(judge,commandnameList);
                }
            }
        }
        return commandnameList;
    }
}
