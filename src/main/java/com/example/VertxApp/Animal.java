package com.example.VertxApp;

public class Animal {
    Animal() {

        System.out.println("Animal");

    }

    public static void main(String argd[]) {

        Cat obj = new Cat();
        // Cat obj = new Cat("abc", 10);
        Dog obj2 = new Dog();

    }

    public Animal(int i) {
        System.out.println("Animal" + i);
    }
}

class Cat extends Animal {
    Cat() {
        // super();
        System.out.println("Cat");
    }

    Cat(int i) {
        this();
        System.out.println("" + i);
    }


    Cat(String name, int i) {
        this(i);
        //this(i);
        System.out.println("" + name + "\t" + i);

    }
}

class Dog extends Animal {
    Dog() {
        super();
        System.out.println("Dog");
    }
}


