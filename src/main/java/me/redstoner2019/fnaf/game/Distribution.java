package me.redstoner2019.fnaf.game;

import java.util.Random;

public class Distribution {
    public static int distribution(int...chances){
        if(chances.length <= 1) return 0;
        Random random = new Random();
        int totalValue = 0;
        for(int i : chances) totalValue+=i;

        int randomValue = random.nextInt(0,totalValue);

        int i0 = 0;
        int i1 = 0;

        for (int i = 0; i < chances.length; i++) {
            i1+=chances[i];
            if(randomValue >= i0 && randomValue < i1) return i;
            i0=i1;
        }
        return -1;
    }

    public static void main(String[] args) {
        int a = 0;
        int b = 0;
        int c = 0;

        for (int i = 0; i < 10000000; i++) {
            switch (distribution(1,3,1)){
                case 0 : {
                    a++;
                    break;
                }
                case 1 : {
                    b++;
                    break;
                }
                case 2 : {
                    c++;
                    break;
                }
                default: {
                    System.out.println("what the hell");
                }
            }
        }

        System.out.println(a + " / " + b + " / " + c);
    }
}
