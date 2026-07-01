package tn.esprit.espritconnect2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to automatically track user activity.
 * The ActivityLoggingAspect intercepts methods annotated with this and logs the action to the activity_log table.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackActivity {
    
    /**
     * The action performed, e.g., "CREATE_PROJECT", "UPDATE_USER", "DELETE_POST"
     */
    String action();
    
    /**
     * The entity affected, e.g., "Project", "User", "Post"
     */
    String entity() default "Unknown";
    
    /**
     * A brief description of the activity
     */
    String description() default "";
}
