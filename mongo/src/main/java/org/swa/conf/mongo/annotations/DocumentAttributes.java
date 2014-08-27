package org.swa.conf.mongo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.inject.Stereotype;
import javax.xml.bind.annotation.XmlRootElement;

@Stereotype
@XmlRootElement
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentAttributes {
	DocumentCriticality criticality();
}