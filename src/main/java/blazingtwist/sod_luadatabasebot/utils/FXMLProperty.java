package blazingtwist.sod_luadatabasebot.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Because FXML is well-thought-out, fields are only accessible through FXML if a PUBLIC setter and getter method is available.
 * Even though IntelliJ will redirect you straight to the field when ctrl-clicking.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface FXMLProperty {
}
