package space.mmty.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServerMain {
    String[] controllerPackages() default {};
    String[] filterPackages() default {};
    int port() default 8000;
}
