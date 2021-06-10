package notes.program_lang.java.lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * java 8 lambda 表达式的简单举例
 */
public class LambdaDemo {
    public static void main(String[] args) {
        //线程runnable下的使用
        new Thread(
                () -> System.out.println("hw 123.")
        ).start();

        //集合的遍历
        List<Integer> immut = Arrays.asList(1, 2, 3, 4, 5);
        immut.forEach(n -> System.out.println(n));
        immut.forEach(System.out::println);

        //注意asList方法生成的还是数组，无法进行修改;需要转为ArrayList
        ArrayList<Integer> muts = new ArrayList<>(immut);
        muts.add(6);

        //lambda函数作为参数传入，使用function内的谓语，实现简化代码
        System.out.println("print all:");
        eval(muts,(n) -> true);

        System.out.println("print none:");
        eval(muts,(n) -> false);

        System.out.println("print even:");
        eval(muts,(n) -> n % 2 == 0);

        System.out.println("print odd:");
        eval(muts,(n) -> n % 2 == 1);

        System.out.println("print large than 5:");
        eval(muts,(n) -> n > 5);

    }

    private static void eval(ArrayList<Integer> muts,
                             Predicate<Integer> predicate) {
        muts.stream().filter(predicate).forEach(System.out::println);
    }
}