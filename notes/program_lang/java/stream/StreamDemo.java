package notes.program_lang.java.stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamDemo {
    public static void main(String[] args) {
        //简单的一个求平方和的例子
        List<Integer> immut = Arrays.asList(1, 2, 3, 4, 5);
        Integer squSum = immut.stream().map(x -> x * x).reduce((a, b) -> a + b).get();
        System.out.println(squSum);

        List<String> strings = Arrays.asList("abc", "ef", "xx", "", "ch", "");
        //filter操作，过滤非空元素个数
        Stream<String> stream = strings.stream();
        long count = stream.filter(s -> !s.isEmpty()).count();
        System.out.println("not empty elements:" + count);

        //filter操作，过滤元素长度个数
        count = strings.stream().filter(s -> s.length() > 2).count();
        System.out.println("length large than 2 elements:" + count);

        //filter操作，过滤非空元素Collectors清单
        List<String> collected = strings.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
        System.out.println("collected list elements:" + collected);

        //filter操作，过滤非空元素Collectors joining清单
        String collect = strings.stream().filter(s -> !s.isEmpty()).collect(Collectors.joining(","));
        System.out.println("collected list string:" + collect);

        //mapping操作，求平方操作 元素清单
        List<Integer> squares = immut.stream().map(x -> x * x).distinct().collect(Collectors.toList());
        System.out.println("distinct square list:" + squares);
    }
}
