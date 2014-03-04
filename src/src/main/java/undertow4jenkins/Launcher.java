package undertow4jenkins;

/**
 * @author Jakub Bartecek <jbartece@redhat.com>
 * 
 */
public class Launcher implements Runnable {

    /**
     * Field for usage, which can be overridden outside this class
     */
    public static String USAGE;
    
    public void run() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Undertow4Jenkins: main");
        
        for(String arg : args) {
            System.out.println("Arg: " + arg);
        }
    }

}
