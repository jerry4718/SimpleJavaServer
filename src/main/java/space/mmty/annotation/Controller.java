package space.mmty.annotation;

import space.mmty.constant.HttpMethods;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Control {
        Api[] api();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Api {
        HttpMethods[] methods() default {HttpMethods.GET};

        String url();
    }
}
