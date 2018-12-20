import java.io.File;

public class Tests {

    public static void main(String[] args) {
        String name = new File("./pâté.csv").getName();
        System.out.println(name);
    }
}
