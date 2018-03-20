package com.example.VertxApp;

public class testing {

    // Static Variable
    static int count = 0;
    static String name = "Gagan";

    public static void main(String args[]) {

        testing conter1 = new testing();
        testing conter2 = new testing();
        testing conter3 = new testing();
        testing couter4 = new testing();
        // testing.staticMethod();

    }

    public testing() {
        count++;
        System.out.println("" + count);
    }


    public static void staticMethod() {
        name = "Rahul Garg";
        System.out.println("" + name);
    }

}
