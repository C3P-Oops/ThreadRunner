import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Dave Carlson
 * @version 1.0
 * @date 30 Nov 2020
 */
public class ThreadRunner {

//  Offers a printed help menu for users when they enter improper variables.
    public static void printHelp() {
        System.out.println("\n" +
                "ThreadRunner runs multiple threads with the option to cause and capture collision counts by\n" +
                "incrementing an integer concurrently.\n\n" +
                "Syntax:\n" +
                "ThreadRunner [-t num] [-i num] [-s num]\n" +
                "num represents any nonzero integer for -t and -i\n\n" +
                "-t num\t\tCreates num threads to run concurrent operations.\n" +
                "-i num\t\tSpecifies that each thread will iterate the integer num times.\n" +
                "-s num\t\tSpecifies whether the method called is synchronized (0 = non-synchronized, 1 = synchronized)" +
                "\n\n" +
                "CAUTION: RUNNING HIGH QUANTITIES OF THREADS AND/OR ITERATIONS MAY UNDESIRABLY LOAD YOUR CPU\n" +
                "Tested maximums were 1 billion total operations.");
    }

//  Main method
    public static void main(String[] args) throws InvalidSyntaxException, InterruptedException {

//  Main variable fields for the main method
        String key = null;
        int value = 0;
        int threadsUsed = 1;                // Defaulted to 1 to limit output
        long iterations = 1000;             // Defaulted to 1,000 to limit output
        boolean isSynchronized = false;     // Defaulted to false to allow collisions
        String syncString = "";
        int cores = Runtime.getRuntime().availableProcessors();


//  start, end, and elapsed times are all default to longs
//  totalCycles and collisions as longs to avoid overrunning the bounds of int (long max > 9q)
        long startTime, endTime, elapsedTime, totalCycles, collisions;

//  ArrayLists to hold the commands once they've been constructed, and the threads for start() and join() operations
        ArrayList<Command> commandsArrayList = new ArrayList<>();
        ArrayList<Thread> threadsArrayList = new ArrayList<>();

//  Check to ensure pairs of arguments, but allow one single argument for the -help function
        if (args.length % 2 != 0 && args.length > 1) {
            System.out.println("Invalid argument entry. Please type ThreadRunner.ThreadRunner -help for help information.");

//  Run the help function if -help is the only argument at the command line
        } else if (args.length == 1 && args[0].trim().equalsIgnoreCase("-help")) {
            printHelp();
            System.exit(0);
        }

//  Create Command k, v pairs from the entries in args
        for (int i = 0; i < args.length; i++) {
            try {
                key = args[i];

//  Check for syntax of key: should have a leading hyphen and only one letter. Throw InvalidSyntaxException at fail.
                if (key.charAt(0) != '-' || key.length() != 2) {
                    throw new InvalidSyntaxException("Invalid key syntax. Type ThreadRunner -help for help information.");
                } else {

//  Assign value, ensuring the value is an integer. Throw NumberFormatException at fail.
                    value = Integer.parseInt(args[i + 1]);
                }
            } catch (NumberFormatException e) {
                System.out.println("Improper number format. Enter only integers. Exiting...");
                System.exit(-1);
            }

//  If all has passed, create the Command and add it to the commandsArrayList
            Command newCommand = new Command(key.toLowerCase(), value);
            commandsArrayList.add(newCommand);
            //System.out.printf("%s - %d%n",newCommand.toString(), newCommand.key.length());  //---DEBUG LINE---

// Double iterator because of pairs
            i++;
        }

//  Iterate through the commandsArrayList to process the functions
        for (Command command : commandsArrayList) {
            switch (command.key) {

//  [-t num] assigns the thread count
                case "t" -> {
                    if (command.value > 0) {
                        threadsUsed = command.value;
                    } else {
                        throw new InvalidSyntaxException(
                                "num value may not be less than one. Exiting...");
                    }

                }

//  [-i num] assigns the quantity of iterations per thread
                case "i" -> {
                    if (iterations > 0) {
                        iterations = command.value;
                    } else {
                        throw new InvalidSyntaxException(
                                "num value may not be less than one. Exiting...");
                    }
                }

//  [-s num] determines whether the threads should target the synchronized function
                case "s" -> {
                    //todo target a method rather than set the boolean
                    if (command.value == 0) {
                        isSynchronized = false;
                        syncString = "unsynchronized";

                    } else if (command.value == 1) {
                        isSynchronized = true;
                        syncString = "synchronized";

                    } else {
                        throw new InvalidSyntaxException("num value for synchronization may " +
                                "only be 0 or 1. Exiting...");
                    }
                }default -> System.out.printf(
                        "-%s is unrecognized. Type ThreadRunner -help for help information.%n", command.key);
            }
        }

//  Echoing the input in a readable format.

        totalCycles = threadsUsed * iterations;

//  Does the user really want this many? Confirm when non-synchronized cycles > 2b or synchronized cycles > 10m
        if ((totalCycles > 2000000000 && !isSynchronized) || (totalCycles > 10000000 && isSynchronized)) {
            String userInput;
            System.out.printf("Dude...that's %,d total cycles! That's a lot! Are you sure? [y/n] ", totalCycles);
            Scanner input = new Scanner(System.in);
            userInput = input.next();
            if (userInput.equalsIgnoreCase("y")) {
                System.out.print("Only if you say please. ");
                userInput = input.next();
                if (userInput.trim().equalsIgnoreCase("please")) {
                    System.out.println("When this baby hits 88 miles per hour, you're going to see some serious shit!");
                } else {
                    System.out.println("Sorry. Please try again with a smaller request.");
                    System.exit(0);
                }
            }
        }

        System.out.printf("Running %d threads in %,d %s iterations.%n%n",
                threadsUsed, iterations, syncString);

//  Setting up an instance of the TargetObject class with the iterator methods for the threads to target
        TargetObject target = new TargetObject();

//  Create user-defined quantity of threads
        for (int i = 0; i < threadsUsed; i++) {
            long finalIterations = iterations;
            boolean finalIsSynchronized = isSynchronized;
            Thread newThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < finalIterations; i++) {
                        if (!finalIsSynchronized) {
                            target.nonSyncIncrement();
                        } else {
                            target.syncIncrement();
                        }
                    }
                }
            });
            threadsArrayList.add(newThread);
        }

//  Start the timer
        startTime = System.currentTimeMillis();

//  Start the engines
        for (Thread t : threadsArrayList) {
            t.start();
        }

//  Wait at the finish line
        for (Thread t : threadsArrayList) {
            t.join();
        }

//  Stop the timer, calculate, and output results.
        endTime = System.currentTimeMillis();
        elapsedTime = (int) (endTime - startTime);
        collisions = totalCycles - target.counter;

        System.out.printf("Ran %,d total %s cycles in %,d ms, and encountered %,d collisions using %d cores.%n",
                totalCycles, syncString, elapsedTime, collisions, cores);
        System.out.printf("Delimited format:%nTotal Cycles, Sync, Elapsed Time, Collisions, CPU Cores%n%d, %s, %d, %d, %d",
                totalCycles, syncString, elapsedTime, collisions, cores);
    }
}

// Simple class to create a target object for threads
class TargetObject {
    int counter;

    public TargetObject() {
        this.counter = 0;
    }

    public void nonSyncIncrement() {
        counter++;
    }

    public synchronized void syncIncrement() {
        counter++;
    }
}