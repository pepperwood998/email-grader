package test_pkg;

public class DemoApp {

    public static void main(String[] args) {
        int month = 0;
        int year = 0;
        int res = 0;

        try {
            month = Integer.valueOf(args[0]);
            year = Integer.valueOf(args[1]);
            
            if (month == 2) {
                if (year % 4 == 0)
                    res = 29;
                else
                    res = 28;
            } else {
                if (month % 2 == 0) {
                    if (month < 8)
                        res = 30;
                    else
                        res = 31;
                } else {
                    if (month < 8)
                        res = 31;
                    else
                        res = 30;
                }
            }
            System.out.println(res);
            
        } catch (NumberFormatException e) {
            System.out.println("Wrong Input");
        }
    }
}
