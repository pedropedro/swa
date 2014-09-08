package org.swa.conf.mongo.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Target({METHOD, PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
public @interface ObjectId {

}
