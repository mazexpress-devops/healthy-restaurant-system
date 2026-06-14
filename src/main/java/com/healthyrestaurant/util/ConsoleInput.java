package com.healthyrestaurant.util;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleInput {
    private final Scanner scanner;

    public ConsoleInput(InputStream inputStream) {
        this.scanner = new Scanner(inputStream, "UTF-8");
    }

    public String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readText(prompt);
            try {
                int parsed = Integer.parseInt(value);
                if (parsed >= min && parsed <= max) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // Ask again.
            }
            System.out.println("قيمة غير صحيحة. أدخل رقما بين " + min + " و " + max + ".");
        }
    }

    public double readDouble(String prompt, double min, double max) {
        while (true) {
            String value = readText(prompt);
            try {
                double parsed = Double.parseDouble(value);
                if (parsed >= min && parsed <= max) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // Ask again.
            }
            System.out.println("قيمة غير صحيحة. أدخل رقما بين " + min + " و " + max + ".");
        }
    }

    public BigDecimal readMoney(String prompt) {
        while (true) {
            String value = readText(prompt);
            try {
                BigDecimal parsed = new BigDecimal(value);
                if (parsed.compareTo(BigDecimal.ZERO) >= 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // Ask again.
            }
            System.out.println("أدخل قيمة مالية صحيحة وغير سالبة.");
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String value = readText(prompt + " (y/n): ").toLowerCase();
            if ("y".equals(value) || "yes".equals(value) || "نعم".equals(value)) {
                return true;
            }
            if ("n".equals(value) || "no".equals(value) || "لا".equals(value)) {
                return false;
            }
            System.out.println("اكتب y للموافقة أو n للرفض.");
        }
    }

    public void pause() {
        readText("اضغط Enter للمتابعة...");
    }
}
