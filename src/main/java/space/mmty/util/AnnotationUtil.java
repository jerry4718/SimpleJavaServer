package space.mmty.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class AnnotationUtil {
    public static <T extends Annotation> Consumer<Consumer<T>> execIfIsPresent(Class<?> klass, Class<T> aKlass) {
        return exec -> {
            T annotation = null;
            if (klass.isAnnotationPresent(aKlass)) {
                annotation = klass.getAnnotation(aKlass);
            }
            if (annotation != null) {
                exec.accept(annotation);
            }
        };
    }

    public static <T extends Annotation> Consumer<Consumer<T>> execIfIsPresent(Method method, Class<T> aKlass) {
        return exec -> {
            T annotation = null;
            if (method.isAnnotationPresent(aKlass)) {
                annotation = method.getAnnotation(aKlass);
            }
            if (annotation != null) {
                exec.accept(annotation);
            }
        };
    }
}
