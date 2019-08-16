package com.ripple.topology.spring;

import com.ripple.topology.TopologyFactory;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Repeatable(LoadTopology.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LoadTopologyConfigurator.class)
public @interface LoadTopology {
    String name();

    String yaml() default "";
    
    Class<? extends TopologyFactory> factory() default TopologyFactory.class;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Import(LoadTopologyConfigurator.class)
    @interface List {
        LoadTopology[] value();
    }
}
